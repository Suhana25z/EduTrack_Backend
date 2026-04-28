package com.edutrack.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.edutrack.backend.dto.subject.CreateSubjectRequest;
import com.edutrack.backend.dto.subject.SubjectResponse;
import com.edutrack.backend.entity.Mark;
import com.edutrack.backend.entity.Role;
import com.edutrack.backend.entity.Student;
import com.edutrack.backend.entity.TeacherSubject;
import com.edutrack.backend.entity.User;
import com.edutrack.backend.exception.BadRequestException;
import com.edutrack.backend.exception.ResourceNotFoundException;
import com.edutrack.backend.repository.StudentRepository;
import com.edutrack.backend.repository.TeacherSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private static final List<String> DEFAULT_SUBJECTS = List.of(
            "Mathematics",
            "Physics",
            "Chemistry",
            "English",
            "Computer Science"
    );

    private final TeacherSubjectRepository teacherSubjectRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public List<SubjectResponse> getSubjects(User teacher) {
        validateTeacher(teacher);
        return toResponses(ensureSubjects(teacher));
    }

    @Transactional
    public SubjectResponse addSubject(CreateSubjectRequest request, User teacher) {
        validateTeacher(teacher);
        List<TeacherSubject> subjects = ensureSubjects(teacher);
        String subjectName = request.getName().trim();

        if (teacherSubjectRepository.existsByTeacherAndNameIgnoreCase(teacher, subjectName)) {
            throw new BadRequestException("Subject already exists");
        }

        TeacherSubject subject = teacherSubjectRepository.save(TeacherSubject.builder()
                .teacher(teacher)
                .name(subjectName)
                .subjectOrder(subjects.size() + 1)
                .build());

        List<Student> students = studentRepository.findByTeacher(teacher);
        for (Student student : students) {
            student.getMarks().stream()
                    .filter(mark -> mark.getSubjectOrder() >= subject.getSubjectOrder())
                    .forEach(mark -> mark.setSubjectOrder(mark.getSubjectOrder() + 1));
            student.getMarks().add(Mark.builder()
                    .student(student)
                    .subjectName(subjectName)
                    .subjectOrder(subject.getSubjectOrder())
                    .score(0)
                    .build());
        }
        studentRepository.saveAll(students);

        return toResponse(subject);
    }

    @Transactional
    public void removeSubject(Long subjectId, User teacher) {
        validateTeacher(teacher);
        List<TeacherSubject> subjects = ensureSubjects(teacher);
        TeacherSubject subject = subjects.stream()
                .filter(item -> item.getId().equals(subjectId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        if (subjects.size() == 1) {
            throw new BadRequestException("At least one subject is required");
        }

        int removedOrder = subject.getSubjectOrder();
        teacherSubjectRepository.delete(subject);

        subjects.remove(subject);
        for (int i = 0; i < subjects.size(); i++) {
            subjects.get(i).setSubjectOrder(i + 1);
        }
        teacherSubjectRepository.saveAll(subjects);

        List<Student> students = studentRepository.findByTeacher(teacher);
        for (Student student : students) {
            student.getMarks().removeIf(mark -> mark.getSubjectOrder().equals(removedOrder));
            student.getMarks().stream()
                    .filter(mark -> mark.getSubjectOrder() > removedOrder)
                    .forEach(mark -> mark.setSubjectOrder(mark.getSubjectOrder() - 1));
        }
        studentRepository.saveAll(students);
    }

    private List<TeacherSubject> ensureSubjects(User teacher) {
        List<TeacherSubject> subjects = teacherSubjectRepository.findByTeacherOrderBySubjectOrderAsc(teacher);
        if (subjects.isEmpty()) {
            subjects = createDefaultSubjects(teacher);
        }

        syncStudentMarks(teacher, subjects);
        return teacherSubjectRepository.findByTeacherOrderBySubjectOrderAsc(teacher);
    }

    private List<TeacherSubject> createDefaultSubjects(User teacher) {
        List<TeacherSubject> subjects = new ArrayList<>();
        for (int i = 0; i < DEFAULT_SUBJECTS.size(); i++) {
            subjects.add(TeacherSubject.builder()
                    .teacher(teacher)
                    .name(DEFAULT_SUBJECTS.get(i))
                    .subjectOrder(i + 1)
                    .build());
        }
        return teacherSubjectRepository.saveAll(subjects);
    }

    private void syncStudentMarks(User teacher, List<TeacherSubject> subjects) {
        List<Student> students = studentRepository.findByTeacher(teacher);
        for (Student student : students) {
            for (TeacherSubject subject : subjects) {
                Optional<Mark> existingMark = student.getMarks().stream()
                        .filter(mark -> mark.getSubjectOrder().equals(subject.getSubjectOrder()))
                        .findFirst();

                if (existingMark.isPresent()) {
                    existingMark.get().setSubjectName(subject.getName());
                } else {
                    student.getMarks().add(Mark.builder()
                            .student(student)
                            .subjectName(subject.getName())
                            .subjectOrder(subject.getSubjectOrder())
                            .score(0)
                            .build());
                }
            }

            student.getMarks().sort(Comparator.comparing(Mark::getSubjectOrder));
        }
        studentRepository.saveAll(students);
    }

    private void validateTeacher(User teacher) {
        if (teacher.getRole() != Role.TEACHER) {
            throw new BadRequestException("Only teachers can manage subjects");
        }
    }

    private List<SubjectResponse> toResponses(List<TeacherSubject> subjects) {
        return subjects.stream().map(this::toResponse).toList();
    }

    private SubjectResponse toResponse(TeacherSubject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .subjectOrder(subject.getSubjectOrder())
                .build();
    }
}
