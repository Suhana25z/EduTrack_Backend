package com.edutrack.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.edutrack.backend.dto.auth.AuthResponse;
import com.edutrack.backend.dto.auth.ForgotPasswordRequest;
import com.edutrack.backend.dto.auth.LoginRequest;
import com.edutrack.backend.dto.auth.RegisterTeacherRequest;
import com.edutrack.backend.dto.auth.ResetPasswordRequest;
import com.edutrack.backend.dto.auth.VerifyOtpRequest;
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

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final Map<String, PasswordResetOtp> passwordResetOtps = new ConcurrentHashMap<>();

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

    public void sendPasswordResetOtp(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
            passwordResetOtps.put(normalizeEmail(request.getEmail()),
                    new PasswordResetOtp(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES), false));
            System.out.println("Password reset OTP for " + user.getEmail() + ": " + otp);
        });
    }

    public void verifyPasswordResetOtp(VerifyOtpRequest request) {
        PasswordResetOtp otp = getValidOtp(request.getEmail(), request.getOtp());
        passwordResetOtps.put(normalizeEmail(request.getEmail()),
                new PasswordResetOtp(otp.value(), otp.expiresAt(), true));
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetOtp otp = getValidOtp(request.getEmail(), request.getOtp());
        if (!otp.verified()) {
            throw new BadRequestException("Verify OTP before changing password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetOtps.remove(normalizeEmail(request.getEmail()));
    }

    private PasswordResetOtp getValidOtp(String email, String value) {
        PasswordResetOtp otp = passwordResetOtps.get(normalizeEmail(email));
        if (otp == null || !otp.value().equals(value)) {
            throw new BadRequestException("Invalid OTP");
        }
        if (otp.expiresAt().isBefore(LocalDateTime.now())) {
            passwordResetOtps.remove(normalizeEmail(email));
            throw new BadRequestException("OTP expired");
        }
        return otp;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
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

    private record PasswordResetOtp(String value, LocalDateTime expiresAt, boolean verified) {
    }
}
