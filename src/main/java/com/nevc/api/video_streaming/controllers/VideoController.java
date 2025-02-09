package com.nevc.api.video_streaming.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.NotFoundException;
import com.nevc.api.video_streaming.dto.VideoMetaDataDTO;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.entities.VideoImpression;
import com.nevc.api.video_streaming.entities.VideoView;
import com.nevc.api.video_streaming.enums.Genre;
import com.nevc.api.video_streaming.enums.SearchComparator;
import com.nevc.api.video_streaming.exceptions.BadRequestException;
import com.nevc.api.video_streaming.projections.VideoMetaDataProjection;
import com.nevc.api.video_streaming.services.UserService;
import com.nevc.api.video_streaming.services.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
public class VideoController {
    private final VideoService videoService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Publish a new video from the user.")
    @ApiResponse(responseCode = "201", description = "Video publishing is successful.")
    @ApiResponse(responseCode = "400", description = "Request to publish video is not correct.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "500", description = "An internal error has occurred while trying to publish the video.")
    public ResponseEntity<?> publishVideo(@RequestPart("file") MultipartFile file,
                                          @RequestPart("videoMetaDataDTO") String videoMetaDataJson) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (file == null || file.getOriginalFilename() == null || videoMetaDataJson == null || videoMetaDataJson.isEmpty()) {
            return ResponseEntity.badRequest().body("File and video metadata must be provided.");
        }
        VideoMetaDataDTO videoMetaDataDTO;
        try {
            videoMetaDataDTO = objectMapper.readValue(videoMetaDataJson, VideoMetaDataDTO.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid video metadata format.");
        }
        if (videoMetaDataDTO.getId() != null && videoMetaDataDTO.getId() != 0) {
            return ResponseEntity.badRequest().body("New video cannot have an id.");
        }
        // Validate the videoMetaDataDTO manually
        Set<ConstraintViolation<VideoMetaDataDTO>> violations = validator.validate(videoMetaDataDTO);
        if (!violations.isEmpty()) {
            String errorMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body("Validation failed: " + errorMessages);
        }
        log.info("Publishing video by user with id: {}, video meta data: {}, filename:{}", user.getId(),
                videoMetaDataDTO, file.getOriginalFilename());
        try {
            VideoMetaDataDTO videoMetaData = videoService.publishVideo(user, file, videoMetaDataDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(videoMetaData);
        } catch (BadRequestException e) {
            log.error("Invalid video request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid Video Request.");
        } catch (Exception e) {
            log.error("Internal error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the metadata of a video.")
    @ApiResponse(responseCode = "200", description = "Video meta data update is successful.")
    @ApiResponse(responseCode = "400", description = "Request to update video meta data is not correct.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "User or video not found.")
    @ApiResponse(responseCode = "500", description = "An internal error has occurred while trying to update the video meta data.")
    public ResponseEntity<?> updateVideoMetaData(@PathVariable("id") Long videoId, @RequestBody @Valid VideoMetaDataDTO videoMetaDataDTO) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (videoMetaDataDTO == null) {
            return ResponseEntity.badRequest().body("Video metadata must be provided.");
        }
        if (videoId.equals(videoMetaDataDTO.getId())) {
            return ResponseEntity.badRequest().body("URL Path video id and object video id do not match.");
        }
        log.info("Updating video meta data by user id: {}, video meta data: {}", user.getId(), videoMetaDataDTO);
        try {
            VideoMetaDataDTO updatedVideoMetaData = videoService.saveVideoMetaData(user, videoMetaDataDTO);
            if (updatedVideoMetaData == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found.");
            }
            return ResponseEntity.ok(updatedVideoMetaData);
        } catch (BadRequestException e) {
            log.error("Invalid video metadata: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid video metadata.");
        } catch (Exception e) {
            log.error("Internal error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/play/{id}")
    @Operation(summary = "Play video resource by id.")
    @ApiResponse(responseCode = "200", description = "Video file is found and can be played.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Video file or metadata not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> playVideo(@PathVariable Long id, HttpServletRequest request) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Loading video for user id: {}, video id: {}", user.getId(), id);
        try {
            VideoMetaDataDTO videoMetaData = videoService.getVideoMetaData(user, null, id);
            if (videoMetaData == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video metadata not found.");
            }
            Resource resource = videoService.playVideoAsResource(user, request, id);
            if (resource == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video file not found.");
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + videoMetaData.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error playing video with id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Load video meta data by id.")
    @ApiResponse(responseCode = "200", description = "Video meta data is found.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Video meta data not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> getVideoMetaData(@PathVariable Long id, HttpServletRequest request) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Getting video meta data for user id: {}, video id: {}", user.getId(), id);
        try {
            VideoMetaDataDTO videoMetaData = videoService.getVideoMetaData(user, request, id);
            if (videoMetaData == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video metadata not found.");
            }
            return ResponseEntity.ok(videoMetaData);
        } catch (Exception e) {
            log.error("Error getting video metadata with id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete video by id.")
    @ApiResponse(responseCode = "200", description = "Video is soft deleted.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Video not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> deleteVideo(@PathVariable Long id) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Deleting video for user id: {}, video id: {}", user.getId(), id);
        try {
            videoService.deleteVideo(user, id);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            log.error("Video with id {} not found: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found.");
        } catch (Exception e) {
            log.error("Error deleting video with id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping
    @Operation(summary = "Get all videos.")
    @ApiResponse(responseCode = "200", description = "Videos are found.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Videos not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> findAllVideos() {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("Finding all videos for user id: {}", user.getId());
        try {
            List<VideoMetaDataProjection> allVideos = videoService.getAllVideos();
            if (allVideos != null && !allVideos.isEmpty()) {
                return ResponseEntity.ok(allVideos);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No videos found.");
            }
        } catch (Exception e) {
            log.error("Error retrieving videos: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/{id}/impressions")
    @Operation(summary = "Get all impressions of a video.")
    @ApiResponse(responseCode = "200", description = "Impressions are found.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Impressions not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> findAllVideoImpressions(@PathVariable Long id) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("Finding all impressions for video id: {} by user id: {}", id, user.getId());
        try {
            List<VideoImpression> allVideoImpressions = videoService.getVideoImpressions(id);
            if (allVideoImpressions != null && !allVideoImpressions.isEmpty()) {
                return ResponseEntity.ok(allVideoImpressions);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No impressions found.");
            }
        } catch (Exception e) {
            log.error("Error retrieving impressions for video id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/{id}/views")
    @Operation(summary = "Get all views of a video.")
    @ApiResponse(responseCode = "200", description = "Impressions are found.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Impressions not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> findAllVideoViews(@PathVariable Long id) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("Finding all views for video id: {} by user id: {}", id, user.getId());
        try {
            List<VideoView> allVideoViews = videoService.getVideoViews(id);
            if (allVideoViews != null && !allVideoViews.isEmpty()) {
                return ResponseEntity.ok(allVideoViews);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No views found.");
            }
        } catch (Exception e) {
            log.error("Error retrieving views for video id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/search/title")
    @Operation(summary = "Search videos by title.")
    @ApiResponse(responseCode = "200", description = "Videos are found.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Videos not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> getByTitle(@RequestParam String title) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest().body("Title parameter must be provided.");
        }
        log.debug("Searching videos by title: {} for user id: {}", title, user.getId());
        try {
            List<VideoMetaDataProjection> videos = videoService.searchByTitle(title);
            if (videos != null && !videos.isEmpty()) {
                return ResponseEntity.ok(videos);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No videos found.");
            }
        } catch (Exception e) {
            log.error("Error searching videos by title {}: {}", title, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/search/director")
    @Operation(summary = "Search videos by director.")
    @ApiResponse(responseCode = "200", description = "Videos are found.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Videos not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> getByDirector(@RequestParam String director) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (director == null || director.isEmpty()) {
            return ResponseEntity.badRequest().body("Director parameter must be provided.");
        }
        log.debug("Searching videos by director: {} for user id: {}", director, user.getId());
        try {
            List<VideoMetaDataProjection> videos = videoService.searchByDirector(director);
            if (videos != null && !videos.isEmpty()) {
                return ResponseEntity.ok(videos);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No videos found.");
            }
        } catch (Exception e) {
            log.error("Error searching videos by director {}: {}", director, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/search/mainActor")
    @Operation(summary = "Search videos by main actor.")
    @ApiResponse(responseCode = "200", description = "Videos are found.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Videos not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> getByMainActor(@RequestParam String mainActor) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (mainActor == null || mainActor.isEmpty()) {
            return ResponseEntity.badRequest().body("Main actor parameter must be provided.");
        }
        log.debug("Searching videos by main actor: {} for user id: {}", mainActor, user.getId());
        try {
            List<VideoMetaDataProjection> videos = videoService.searchByMainActor(mainActor);
            if (videos != null && !videos.isEmpty()) {
                return ResponseEntity.ok(videos);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No videos found.");
            }
        } catch (Exception e) {
            log.error("Error searching videos by main actor {}: {}", mainActor, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/search/runningTime")
    @Operation(summary = "Search videos by running time.")
    @ApiResponse(responseCode = "200", description = "Videos are found.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Videos not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> getByRunningTime(@RequestParam int runningTime,
                                              @RequestParam SearchComparator comparator) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (comparator == null) {
            return ResponseEntity.badRequest().body("Search comparator must be provided.");
        }
        log.debug("Searching videos by running time: {} with comparator: {} for user id: {}", runningTime, comparator, user.getId());
        try {
            List<VideoMetaDataProjection> videos = videoService.searchByRunningTime(runningTime, comparator);
            if (videos != null && !videos.isEmpty()) {
                return ResponseEntity.ok(videos);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No videos found.");
            }
        } catch (Exception e) {
            log.error("Error searching videos by running time {}: {}", runningTime, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }

    @GetMapping("/search/genre")
    @Operation(summary = "Search videos by genre.")
    @ApiResponse(responseCode = "200", description = "Videos are found.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "401", description = "User is not authenticated.")
    @ApiResponse(responseCode = "404", description = "Videos not found.")
    @ApiResponse(responseCode = "500", description = "Internal server error.")
    public ResponseEntity<?> getByGenre(@RequestParam Genre genre) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (genre == null) {
            return ResponseEntity.badRequest().body("Genre parameter must be provided.");
        }
        log.debug("Searching videos by genre: {} for user id: {}", genre, user.getId());
        try {
            List<VideoMetaDataProjection> videos = videoService.searchByGenre(genre);
            if (videos != null && !videos.isEmpty()) {
                return ResponseEntity.ok(videos);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No videos found.");
            }
        } catch (Exception e) {
            log.error("Error searching videos by genre {}: {}", genre, e.getMessage());
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }
}
