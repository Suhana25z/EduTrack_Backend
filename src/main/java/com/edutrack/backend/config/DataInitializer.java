package com.edutrack.backend.config;

import java.util.List;

import com.edutrack.backend.entity.Mark;
import com.edutrack.backend.entity.Role;
import com.edutrack.backend.entity.Student;
import com.edutrack.backend.entity.User;
import com.edutrack.backend.repository.StudentRepository;
import com.edutrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("Ramesh12345")) {
            return;
        }

        User teacher = userRepository.save(User.builder()
                .username("Ramesh12345")
                .email("ramesh@edutrack.com")
                .password(passwordEncoder.encode("teacher123"))
                .role(Role.TEACHER)
                .build());

        createDemoStudent("STU001", "Ananya", "ananya@edutrack.com", "student123", "Computer Science Engineering", teacher, List.of(88, 91, 84, 90, 86));
        createDemoStudent("STU002", "Rahul", "rahul@edutrack.com", "student123", "Electronics and Communication Engineering", teacher, List.of(65, 70, 60, 72, 68));
    }

    private void createDemoStudent(String studentId, String name, String email, String password,
                                   String branch, User teacher, List<Integer> scores) {
        User studentUser = userRepository.save(User.builder()
                .username(studentId)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.STUDENT)
                .build());

        Student student = Student.builder()
                .studentId(studentId)
                .name(name)
                .branch(branch)
                .email(email)
                .teacher(teacher)
                .user(studentUser)
                .build();

        List<String> subjects = List.of("Mathematics", "Physics", "Chemistry", "English", "Computer Science");
        for (int i = 0; i < subjects.size(); i++) {
            student.getMarks().add(Mark.builder()
                    .student(student)
                    .subjectName(subjects.get(i))
                    .subjectOrder(i + 1)
                    .score(scores.get(i))
                    .build());
        }
        studentRepository.save(student);
    }
}
