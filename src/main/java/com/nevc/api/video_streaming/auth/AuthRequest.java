package com.nevc.api.video_streaming.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Name is mandatory")
    private final String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    private final String email;

    @NotBlank(message = "Password is mandatory")
    private final String password;
}
