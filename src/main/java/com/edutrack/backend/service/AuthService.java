package com.edutrack.backend.service;

import java.util.Map;

import com.edutrack.backend.dto.auth.AuthResponse;
import com.edutrack.backend.dto.auth.LoginRequest;
import com.edutrack.backend.dto.auth.RegisterTeacherRequest;
import com.edutrack.backend.entity.Role;
import com.edutrack.backend.entity.Student;
import com.edutrack.backend.entity.User;
import com.edutrack.backend.exception.BadRequestException;
import com.edutrack.backend.repository.StudentRepository;
import com.edutrack.backend.repository.UserRepository;
import com.edutrack.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse registerTeacher(RegisterTeacherRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.TEACHER)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user, null);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));

        User user = userRepository.findByUsername(request.getLogin())
                .or(() -> userRepository.findByEmail(request.getLogin()))
                .or(() -> studentRepository.findByStudentId(request.getLogin()).map(Student::getUser))
                .orElseThrow(() -> new BadRequestException("Invalid login credentials"));

        Student student = user.getRole() == Role.STUDENT
                ? studentRepository.findByUser(user).orElse(null)
                : null;
        return buildAuthResponse(user, student);
    }

    private AuthResponse buildAuthResponse(User user, Student student) {
        String token = jwtService.generateToken(user, Map.of("role", user.getRole().name()));
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .studentId(student != null ? student.getStudentId() : null)
                .build();
    }
}
