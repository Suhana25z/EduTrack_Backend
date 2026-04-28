package com.edutrack.backend.controller;

import java.util.List;

import com.edutrack.backend.dto.common.ApiResponse;
import com.edutrack.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping
    public ResponseEntity<ApiResponse> verifyToken(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("JWT token is valid for " + user.getRole() + " " + user.getUsername())
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @GetMapping("/ping")
    public ResponseEntity<List<String>> ping() {
        return ResponseEntity.ok(List.of("EduTrack backend is running"));
    }
}
