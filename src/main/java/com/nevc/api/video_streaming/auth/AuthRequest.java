package com.nevc.api.video_streaming.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
