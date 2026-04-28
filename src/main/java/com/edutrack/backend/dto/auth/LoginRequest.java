package com.edutrack.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Login identifier is required")
    private String login;

    @NotBlank(message = "Password is required")
    private String password;
}
