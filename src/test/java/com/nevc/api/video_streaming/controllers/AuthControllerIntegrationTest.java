package com.nevc.api.video_streaming.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevc.api.video_streaming.auth.AuthRequest;
import com.nevc.api.video_streaming.auth.AuthResponse;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.enums.Role;
import com.nevc.api.video_streaming.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class AuthControllerIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("video_streaming_test")
            .withUsername("test_app_user")
            .withPassword("test_app_password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_Success() throws Exception {
        AuthRequest authRequest = new AuthRequest("John Doe", "john.doe@example.com", "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void registerUser_EmptyPassword() throws Exception {
        AuthRequest authRequest = new AuthRequest("John Doe", "john.doe@example.com", "");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_Success() throws Exception {
        // Register a user first
        User user = new User();
        user.setName("Joe Smith");
        user.setEmail("joe.smith@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        AuthRequest authRequest = new AuthRequest("Joe Smith", "joe.smith@example.com", "password123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginUser_WrongPassword() throws Exception {
        // Register a user first
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword(passwordEncoder.encode("correctPassword"));
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        // Attempt login with wrong password
        AuthRequest authRequest = new AuthRequest("John Doe", "john.doe@example.com", "wrongPassword");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void logoutUser_Success() throws Exception {
        // Register a user
        AuthRequest registerRequest = new AuthRequest("John Doe", "john.doe@example.com", "password123");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login to get a token
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract token from login response
        String token = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthResponse.class
        ).getToken();

        // Perform logout with the valid token
        AuthResponse logoutRequest = new AuthResponse(token);
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User logged out successfully"));

        // Verify token is invalidated by trying to access a protected endpoint
        mockMvc.perform(get("/videos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutUser_InvalidToken() throws Exception {
        // Try to logout with an invalid token
        AuthResponse invalidTokenRequest = new AuthResponse("invalid.token.here");
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTokenRequest)))
                .andExpect(status().isBadRequest());
    }
}