package com.edutrack.backend.controller;

import com.edutrack.backend.dto.auth.AuthResponse;
import com.edutrack.backend.dto.auth.LoginRequest;
import com.edutrack.backend.dto.auth.RegisterTeacherRequest;
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
}
