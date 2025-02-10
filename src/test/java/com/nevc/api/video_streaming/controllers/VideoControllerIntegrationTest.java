package com.nevc.api.video_streaming.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevc.api.video_streaming.auth.JwtUtil;
import com.nevc.api.video_streaming.dto.VideoMetaDataDTO;
import com.nevc.api.video_streaming.entities.VideoImpression;
import com.nevc.api.video_streaming.entities.VideoMetaData;
import com.nevc.api.video_streaming.entities.VideoView;
import com.nevc.api.video_streaming.enums.Genre;
import com.nevc.api.video_streaming.enums.Role;
import com.nevc.api.video_streaming.repositories.UserRepository;
import com.nevc.api.video_streaming.repositories.VideoImpressionRepository;
import com.nevc.api.video_streaming.repositories.VideoMetaDataRepository;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.repositories.VideoViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class VideoControllerIntegrationTest {

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
    private VideoMetaDataRepository videoMetaDataRepository;

    @Autowired
    private VideoImpressionRepository videoImpressionRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private String validToken;
    private VideoMetaData testVideo;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        videoImpressionRepository.deleteAll();
        videoViewRepository.deleteAll();
        videoMetaDataRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void publishVideo_Success() throws Exception {
        // Register and authenticate a user
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.CREATOR);
        userRepository.save(testUser);

        String token = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                testUser.getEmail(), testUser.getPassword(), testUser.getRole().getAuthorities()));

        // Prepare test data
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        videoMetaDataDTO.setTitle("Test Video");
        videoMetaDataDTO.setDirectorName("Test Director");
        videoMetaDataDTO.setMainActor("Test Actor");
        videoMetaDataDTO.setYearOfRelease(2024);
        videoMetaDataDTO.setRunningTime(120);
        videoMetaDataDTO.setFileName("test_video.mp4");
        videoMetaDataDTO.setFileExtension("mp4");

        MockMultipartFile file = new MockMultipartFile("file", "test_video.mp4", "video/mp4", "test video content".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("videoMetaDataDTO", "", "application/json", objectMapper.writeValueAsBytes(videoMetaDataDTO));

        mockMvc.perform(multipart("/videos")
                        .file(file)
                        .file(metadata)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Video"));
    }

    @Test
    void updateVideoMetaData_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        VideoMetaDataDTO updateDto = VideoMetaDataDTO.builder()
                .id(testVideo.getId())
                .title("Updated Title")
                .directorName("Updated Director")
                .mainActor("Updated Actor")
                .yearOfRelease(2025)
                .genre(Set.of(Genre.ACTION))
                .runningTime(150)
                .fileName("updated.mp4")
                .fileExtension("mp4")
                .build();

        mockMvc.perform(put("/videos/{id}", testVideo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.directorName").value("Updated Director"));
    }

    @Test
    void playVideo_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();
        mockMvc.perform(get("/videos/play/{id}", testVideo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"test_video.mp4\""));
    }

    @Test
    void getVideoMetaData_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();
        mockMvc.perform(get("/videos/{id}", testVideo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Video"))
                .andExpect(jsonPath("$.directorName").value("Test Director"));
    }

    @Test
    void deleteVideo_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();
        mockMvc.perform(delete("/videos/{id}", testVideo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk());

        assertFalse(videoMetaDataRepository.findById(testVideo.getId()).get().isActive());
    }

    @Test
    void findAllVideos_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        // Add another video
        VideoMetaData anotherVideo = VideoMetaData.builder()
                .title("Another Video")
                .directorName("Another Director")
                .mainActor("Another Actor")
                .yearOfRelease(2023)
                .publishedDate(LocalDate.now())
                .runningTime(90)
                .fileName("another.mp4")
                .fileExtension("mp4")
                .filePath("uploads/")
                .publishedBy(testUser)
                .active(true)
                .build();
        videoMetaDataRepository.save(anotherVideo);

        mockMvc.perform(get("/videos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Test Video"))
                .andExpect(jsonPath("$[1].title").value("Another Video"));
    }

    @Test
    void findAllVideoImpressions_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        // Add an impression for the test video
        VideoImpression impression = VideoImpression.builder()
                .videoMetaData(testVideo)
                .user(testUser)
                .impressionDate(LocalDateTime.now())
                .userIp("127.0.0.1")
                .userAgent("Test Agent")
                .build();
        videoImpressionRepository.save(impression);

        mockMvc.perform(get("/videos/{id}/impressions", testVideo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userIp").value("127.0.0.1"))
                .andExpect(jsonPath("$[0].userAgent").value("Test Agent"));
    }

    @Test
    void findAllVideoImpressions_VideoNotFound() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/{id}/impressions", 999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllVideoViews_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        // Add a view for the test video
        VideoView view = VideoView.builder()
                .videoMetaData(testVideo)
                .user(testUser)
                .viewDate(LocalDateTime.now())
                .userIp("127.0.0.1")
                .userAgent("Test Agent")
                .build();
        videoViewRepository.save(view);

        mockMvc.perform(get("/videos/{id}/views", testVideo.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userIp").value("127.0.0.1"))
                .andExpect(jsonPath("$[0].userAgent").value("Test Agent"));
    }

    @Test
    void findAllVideoViews_VideoNotFound() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/{id}/views", 999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByTitle_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/title")
                        .param("title", "Test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Video"));
    }

    @Test
    void searchByTitle_NoResults() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/title")
                        .param("title", "NonExistent")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByDirector_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/director")
                        .param("director", "Test Director")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].directorName").value("Test Director"));
    }

    @Test
    void searchByDirector_NoResults() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/director")
                        .param("director", "NonExistent Director")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByMainActor_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/mainActor")
                        .param("mainActor", "Test Actor")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].mainActor").value("Test Actor"));
    }

    @Test
    void searchByMainActor_NoResults() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/mainActor")
                        .param("mainActor", "NonExistent Actor")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByRunningTime_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/runningTime")
                        .param("runningTime", "120")
                        .param("comparator", "EQUAL")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].runningTime").value(120));
    }

    @Test
    void searchByRunningTime_NoResults() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/runningTime")
                        .param("runningTime", "999")
                        .param("comparator", "EQUAL")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByGenre_Success() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        testVideo.setGenre(Set.of(Genre.ACTION));
        videoMetaDataRepository.save(testVideo);

        mockMvc.perform(get("/videos/search/genre")
                        .param("genre", "ACTION")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].genre[0]").value("ACTION"));
    }

    @Test
    void searchByGenre_NoResults() throws Exception {
        saveUserAndGetJWTToken();
        saveVideoMetaData();

        mockMvc.perform(get("/videos/search/genre")
                        .param("genre", "COMEDY")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }


    private void saveUserAndGetJWTToken() {
        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.ADMIN);
        userRepository.save(testUser);

        // Generate JWT token
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities(testUser.getRole().name())
                .build();
        validToken = jwtUtil.generateToken(userDetails);
    }

    private void saveVideoMetaData() {
        testVideo = VideoMetaData.builder()
                .title("Test Video")
                .directorName("Test Director")
                .mainActor("Test Actor")
                .yearOfRelease(2024)
                .publishedDate(LocalDate.now())
                .runningTime(120)
                .fileName("test_video.mp4")
                .fileExtension("mp4")
                .filePath("uploads/")
                .publishedBy(testUser)
                .active(true)
                .build();
        videoMetaDataRepository.save(testVideo);
    }
}