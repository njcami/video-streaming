package com.nevc.api.video_streaming.controllers;

import com.nevc.api.video_streaming.auth.AuthRequest;
import com.nevc.api.video_streaming.auth.AuthResponse;
import com.nevc.api.video_streaming.auth.JwtUtil;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.enums.Role;
import com.nevc.api.video_streaming.services.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8080"
        },
        methods = {
                RequestMethod.OPTIONS,
                RequestMethod.GET,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.POST
        })
@Getter
@Setter
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${registration.default.role:ADMIN}")
    private Role defaultRole;

    @PostMapping("/login")
    @Operation(summary = "Login a video streaming user")
    @ApiResponse(responseCode = "200", description = "If user is found and token is generated.")
    @ApiResponse(responseCode = "400", description = "In case of a bad login request.")
    @ApiResponse(responseCode = "404", description = "In case the user by email is not found.")
    @SecurityRequirements // This disables the Bearer token security for this endpoint
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest authRequest) {
        log.debug("Authenticating user with email: {}", authRequest.getEmail());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        } catch (Exception e) {
            log.info("Invalid credentials for user with email: {}", authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        Optional<User> userOptional = userDetailsService.findByEmail(authRequest.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(String.valueOf(user.getRole()))
                .build();
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a video streaming user")
    @ApiResponse(responseCode = "200", description = "If user is correctly registered.")
    @ApiResponse(responseCode = "400", description = "In case of a bad request or user already exists.")
    @SecurityRequirements // This disables the Bearer token security for this endpoint
    public ResponseEntity<?> register(@RequestBody @Valid AuthRequest authRequest) {
        log.debug("Registering user with email: {}", authRequest.getEmail());
        if (userDetailsService.findByEmail(authRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }

        User newUser = User.builder()
            .name(authRequest.getName())
            .email(authRequest.getEmail())
            .password(passwordEncoder.encode(authRequest.getPassword()))
            .role(defaultRole)
            .build();

        newUser = userDetailsService.saveUser(newUser);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(newUser.getEmail())
                .password(newUser.getPassword())
                .authorities(String.valueOf(newUser.getRole()))
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logging out a video streaming user by auth token")
    @ApiResponse(responseCode = "200", description = "If user is correctly logged out.")
    @ApiResponse(responseCode = "400", description = "In case of a bad request.")
    public ResponseEntity<?> logout(@RequestBody @Valid AuthResponse authResponse) {
        log.debug("Invalidating token: {}", authResponse.getToken());
        jwtUtil.invalidateToken(authResponse.getToken());
        return ResponseEntity.ok("User logged out successfully");
    }
}