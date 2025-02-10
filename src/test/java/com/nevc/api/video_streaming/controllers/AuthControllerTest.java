package com.nevc.api.video_streaming.controllers;

import com.nevc.api.video_streaming.auth.AuthRequest;
import com.nevc.api.video_streaming.auth.AuthResponse;
import com.nevc.api.video_streaming.auth.JwtUtil;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.enums.Role;
import com.nevc.api.video_streaming.services.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserServiceImpl userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginWithValidCredentials() {
        AuthRequest authRequest = new AuthRequest("user", "user@example.com", "password");
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.CREATOR);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token");

        ResponseEntity<?> response = authController.login(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(AuthResponse.class, response.getBody());
        assertEquals("token", ((AuthResponse) response.getBody()).getToken());
    }

    @Test
    void loginWithInvalidCredentials() {
        AuthRequest authRequest = new AuthRequest("user", "user@example.com", "wrongPassword");

        doThrow(new RuntimeException("Bad credentials")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseEntity<?> response = authController.login(authRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void registerWithValidDetails() {
        AuthRequest authRequest = new AuthRequest("name","newuser@example.com", "password");
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("encodedPassword");
        newUser.setRole(Role.CREATOR);

        when(userDetailsService.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userDetailsService.saveUser(any(User.class))).thenReturn(newUser);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token");

        ResponseEntity<?> response = authController.register(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(AuthResponse.class, response.getBody());
        assertEquals("token", ((AuthResponse) response.getBody()).getToken());
    }

    @Test
    void registerWithExistingEmail() {
        AuthRequest authRequest = new AuthRequest("name","existinguser@example.com", "password");

        when(userDetailsService.findByEmail("existinguser@example.com")).thenReturn(Optional.of(new User()));

        ResponseEntity<?> response = authController.register(authRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already in use", response.getBody());
    }

    @Test
    void logoutWithValidToken() {
        AuthResponse authResponse = new AuthResponse("validToken");

        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        doNothing().when(jwtUtil).invalidateToken("validToken");

        ResponseEntity<?> response = authController.logout(authResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User logged out successfully", response.getBody());
    }

    @Test
    void logoutWithInvalidToken() {
        AuthResponse authResponse = new AuthResponse("invalidToken");

        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = authController.logout(authResponse);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
    }
}
