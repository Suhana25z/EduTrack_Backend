package com.edutrack.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.edutrack.backend.dto.dashboard.ClassSummaryResponse;
import com.edutrack.backend.dto.dashboard.InsightResponse;
import com.edutrack.backend.dto.dashboard.RecommendationResponse;
import com.edutrack.backend.dto.dashboard.ReportResponse;
import com.edutrack.backend.dto.dashboard.StudentDashboardResponse;
import com.edutrack.backend.dto.student.CreateStudentRequest;
import com.edutrack.backend.dto.student.StudentResponse;
import com.edutrack.backend.dto.student.SubjectMarkRequest;
import com.edutrack.backend.dto.student.SubjectMarkResponse;
import com.edutrack.backend.dto.student.UpdateMarksRequest;
import com.edutrack.backend.dto.student.UpdateStudentProfileRequest;
import com.edutrack.backend.entity.Mark;
import com.edutrack.backend.entity.Role;
import com.edutrack.backend.entity.Student;
import com.edutrack.backend.entity.User;
import com.edutrack.backend.exception.BadRequestException;
import com.edutrack.backend.exception.ResourceNotFoundException;
import com.edutrack.backend.repository.StudentRepository;
import com.edutrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request, User teacher) {
        if (teacher.getRole() != Role.TEACHER) {
            throw new BadRequestException("Only teachers can create students");
        }
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new BadRequestException("Student ID already exists");
        }
        if (studentRepository.existsByEmail(request.getEmail()) || userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getStudentId())) {
            throw new BadRequestException("Student ID already exists as a username");
        }

        User user = User.builder()
                .username(request.getStudentId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();
        userRepository.save(user);

        Student student = Student.builder()
                .studentId(request.getStudentId())
                .name(request.getName())
                .branch(request.getBranch())
                .email(request.getEmail())
                .teacher(teacher)
                .user(user)
                .build();
        student.setMarks(buildMarks(student, request.getMarks()));
        return toResponse(studentRepository.save(student));
    }

    public List<StudentResponse> getTeacherStudents(User teacher) {
        return studentRepository.findByTeacher(teacher).stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(StudentResponse::getAverage).reversed())
                .toList();
    }

    @Transactional
    public StudentResponse updateMarks(String studentId, UpdateMarksRequest request, User teacher) {
        Student student = getTeacherOwnedStudent(studentId, teacher);
        student.getMarks().clear();
        student.getMarks().addAll(buildMarks(student, request.getMarks()));
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse updateProfile(String studentId, UpdateStudentProfileRequest request, User teacher) {
        Student student = getTeacherOwnedStudent(studentId, teacher);
        student.setBranch(request.getBranch());
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse addStudentSubject(String studentId, SubjectMarkRequest request, User teacher) {
        Student student = getTeacherOwnedStudent(studentId, teacher);
        boolean exists = student.getMarks().stream()
                .anyMatch(mark -> mark.getSubjectName().equalsIgnoreCase(request.getSubjectName()));
        if (exists) {
            throw new BadRequestException("Subject already exists for this student");
        }

        int nextOrder = student.getMarks().stream()
                .mapToInt(Mark::getSubjectOrder)
                .max()
                .orElse(0) + 1;
        student.getMarks().add(Mark.builder()
                .student(student)
                .subjectName(request.getSubjectName())
                .subjectOrder(nextOrder)
                .score(request.getScore() == null ? 0 : request.getScore())
                .build());
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse removeStudentSubject(String studentId, Integer subjectOrder, User teacher) {
        Student student = getTeacherOwnedStudent(studentId, teacher);
        if (student.getMarks().size() == 1) {
            throw new BadRequestException("At least one subject is required");
        }

        boolean removed = student.getMarks().removeIf(mark -> mark.getSubjectOrder().equals(subjectOrder));
        if (!removed) {
            throw new ResourceNotFoundException("Subject not found for this student");
        }

        student.getMarks().stream()
                .filter(mark -> mark.getSubjectOrder() > subjectOrder)
                .forEach(mark -> mark.setSubjectOrder(mark.getSubjectOrder() - 1));
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void deleteStudent(String studentId, User teacher) {
        Student student = getTeacherOwnedStudent(studentId, teacher);
        User user = student.getUser();
        studentRepository.delete(student);
        userRepository.delete(user);
    }

    public StudentDashboardResponse getStudentDashboard(User user) {
        Student student = getStudentByUser(user);
        List<Student> classmates = studentRepository.findByTeacher(student.getTeacher());
        double classAverage = calculateClassAverage(classmates);
        double average = calculateAverage(student);
        int highest = student.getMarks().stream().mapToInt(Mark::getScore).max().orElse(0);
        int lowest = student.getMarks().stream().mapToInt(Mark::getScore).min().orElse(0);
        int consistency = Math.max(0, 100 - (highest - lowest));

        return StudentDashboardResponse.builder()
                .studentId(student.getStudentId())
                .studentName(student.getName())
                .branch(student.getBranch())
                .total(student.getMarks().stream().mapToInt(Mark::getScore).sum())
                .average(round(average))
                .highest(highest)
                .lowest(lowest)
                .classAverage(round(classAverage))
                .consistencyScore(consistency)
                .performanceLevel(performanceLevel(average))
                .trend(calculateTrend(student))
                .marks(toSubjectResponses(student.getMarks()))
                .strengths(buildStrengths(student, average))
                .weaknesses(buildWeaknesses(student, average))
                .build();
    }

    public RecommendationResponse getStudentRecommendations(User user) {
        Student student = getStudentByUser(user);
        List<Student> classmates = studentRepository.findByTeacher(student.getTeacher());
        double classAverage = calculateClassAverage(classmates);
        double average = calculateAverage(student);

        List<String> overall = new ArrayList<>();
        if (average >= classAverage) {
            overall.add("You are performing above the class average. Keep reinforcing your stronger subjects.");
        } else {
            overall.add("You are below the class average. Focus on one weaker subject at a time for steady improvement.");
        }
        overall.add("Review your marks weekly and schedule revision for the lowest-scoring subjects first.");

        List<RecommendationResponse.SubjectRecommendation> subjectSpecific = student.getMarks().stream()
                .filter(mark -> mark.getScore() < 75)
                .map(mark -> RecommendationResponse.SubjectRecommendation.builder()
                        .subjectName(mark.getSubjectName())
                        .currentScore(mark.getScore())
                        .strategies(List.of(
                                "Spend 30 minutes daily revising key concepts in " + mark.getSubjectName(),
                                "Solve at least 5 extra practice questions every week",
                                "Ask your teacher for targeted feedback on recurring mistakes"))
                        .build())
                .toList();

        List<String> trends = List.of(
                "Current trend: " + calculateTrend(student),
                "Consistency score: " + Math.max(0, 100 - (student.getMarks().stream().mapToInt(Mark::getScore).max().orElse(0)
                        - student.getMarks().stream().mapToInt(Mark::getScore).min().orElse(0))) + "%");

        List<String> nextSteps = List.of(
                "Prioritize the two lowest subject scores this week",
                "Track your progress after every assessment update",
                "Target an average improvement of 5 points over the next month");

        return RecommendationResponse.builder()
                .overallSuggestions(overall)
                .subjectSpecific(subjectSpecific)
                .trends(trends)
                .nextSteps(nextSteps)
                .build();
    }

    public ClassSummaryResponse getClassSummary(User teacher) {
        List<Student> students = studentRepository.findByTeacher(teacher);
        List<Double> averages = students.stream().map(this::calculateAverage).toList();
        double classAverage = averages.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        return ClassSummaryResponse.builder()
                .totalStudents(students.size())
                .classAverage(round(classAverage))
                .highestAverage(round(averages.stream().mapToDouble(Double::doubleValue).max().orElse(0)))
                .lowestAverage(round(averages.stream().mapToDouble(Double::doubleValue).min().orElse(0)))
                .aboveAverageCount(averages.stream().filter(avg -> avg >= classAverage).count())
                .needsAttentionCount(averages.stream().filter(avg -> avg < 60).count())
                .build();
    }

    public ReportResponse getTeacherReport(User teacher) {
        List<StudentResponse> students = getTeacherStudents(teacher);
        return ReportResponse.builder()
                .summary(getClassSummary(teacher))
                .toppers(students.stream().limit(3).toList())
                .attentionNeeded(students.stream().filter(student -> student.getAverage() < 60).toList())
                .build();
    }

    public Student getTeacherOwnedStudent(String studentId, User teacher) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.getTeacher().getId().equals(teacher.getId())) {
            throw new BadRequestException("You can only manage your own students");
        }
        return student;
    }

    public Student getStudentByUser(User user) {
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    public StudentResponse toResponse(Student student) {
        StudentResponse response = modelMapper.map(student, StudentResponse.class);
        response.setAverage(round(calculateAverage(student)));
        response.setTotal(student.getMarks().stream().mapToInt(Mark::getScore).sum());
        response.setMarks(toSubjectResponses(student.getMarks()));
        return response;
    }

    private List<Mark> buildMarks(Student student, List<SubjectMarkRequest> requestMarks) {
        List<Mark> marks = new ArrayList<>();
        for (int i = 0; i < requestMarks.size(); i++) {
            SubjectMarkRequest item = requestMarks.get(i);
            marks.add(Mark.builder()
                    .student(student)
                    .subjectName(item.getSubjectName())
                    .subjectOrder(i + 1)
                    .score(item.getScore())
                    .build());
        }
        return marks;
    }

    private List<SubjectMarkResponse> toSubjectResponses(List<Mark> marks) {
        return marks.stream()
                .sorted(Comparator.comparing(Mark::getSubjectOrder))
                .map(mark -> SubjectMarkResponse.builder()
                        .subjectName(mark.getSubjectName())
                        .score(mark.getScore())
                        .grade(toGrade(mark.getScore()))
                        .build())
                .toList();
    }

    private List<InsightResponse> buildStrengths(Student student, double average) {
        return student.getMarks().stream()
                .filter(mark -> mark.getScore() >= average)
                .sorted(Comparator.comparing(Mark::getScore).reversed())
                .limit(3)
                .map(mark -> InsightResponse.builder()
                        .title(mark.getSubjectName())
                        .detail(mark.getScore() + " score, " + round(mark.getScore() - average) + " points above average")
                        .color("#10b981")
                        .build())
                .toList();
    }

    private List<InsightResponse> buildWeaknesses(Student student, double average) {
        return student.getMarks().stream()
                .filter(mark -> mark.getScore() < average)
                .sorted(Comparator.comparing(Mark::getScore))
                .limit(3)
                .map(mark -> InsightResponse.builder()
                        .title(mark.getSubjectName())
                        .detail(mark.getScore() + " score, " + round(average - mark.getScore()) + " points below average")
                        .color("#ef4444")
                        .build())
                .toList();
    }

    private double calculateAverage(Student student) {
        return student.getMarks().stream().mapToInt(Mark::getScore).average().orElse(0);
    }

    private double calculateClassAverage(List<Student> students) {
        return students.stream().mapToDouble(this::calculateAverage).average().orElse(0);
    }

    private String performanceLevel(double average) {
        if (average >= 90) return "Excellent";
        if (average >= 80) return "Very Good";
        if (average >= 70) return "Good";
        if (average >= 60) return "Satisfactory";
        return "Needs Improvement";
    }

    private String calculateTrend(Student student) {
        if (student.getMarks().size() < 2) {
            return "stable";
        }
        int midpoint = student.getMarks().size() / 2;
        int firstHalf = student.getMarks().stream().limit(midpoint).mapToInt(Mark::getScore).sum();
        int secondHalf = student.getMarks().stream().skip(midpoint).mapToInt(Mark::getScore).sum();
        if (secondHalf > firstHalf) return "improving";
        if (secondHalf < firstHalf) return "declining";
        return "stable";
    }

    private String toGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
