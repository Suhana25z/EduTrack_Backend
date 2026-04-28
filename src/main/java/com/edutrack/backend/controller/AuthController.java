package com.edutrack.backend.controller;

import com.edutrack.backend.dto.auth.ForgotPasswordRequest;
import com.edutrack.backend.dto.auth.AuthResponse;
import com.edutrack.backend.dto.auth.LoginRequest;
import com.edutrack.backend.dto.auth.RegisterTeacherRequest;
import com.edutrack.backend.dto.auth.ResetPasswordRequest;
import com.edutrack.backend.dto.auth.VerifyOtpRequest;
import com.edutrack.backend.dto.common.ApiResponse;
import com.edutrack.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/teacher")
    @Operation(summary = "Register a teacher")
    public ResponseEntity<AuthResponse> registerTeacher(@Valid @RequestBody RegisterTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerTeacher(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with teacher username/email or student ID/email")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "Send password reset OTP to registered email")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.sendPasswordResetOtp(request);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("OTP sent if the email is registered").build());
    }

    @PostMapping("/password/verify-otp")
    @Operation(summary = "Verify password reset OTP")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyPasswordResetOtp(request);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("OTP verified").build());
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password using verified OTP")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Password changed successfully").build());
    }
}
