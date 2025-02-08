package com.nevc.api.video_streaming.controllers;

import com.nevc.api.video_streaming.dto.VideoMetaDataDTO;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.entities.VideoImpression;
import com.nevc.api.video_streaming.entities.VideoView;
import com.nevc.api.video_streaming.enums.Genre;
import com.nevc.api.video_streaming.enums.SearchComparator;
import com.nevc.api.video_streaming.projections.VideoMetaDataProjection;
import com.nevc.api.video_streaming.services.UserService;
import com.nevc.api.video_streaming.services.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class VideoControllerTest {

    @Mock
    private VideoService videoService;

    @Mock
    private UserService userService;

    @Mock
    private MultipartFile file;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private VideoController videoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPublishVideo_Success() {
        User user = new User();
        user.setId(1L);
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.publishVideo(user, file, videoMetaDataDTO)).thenReturn(videoMetaDataDTO);

        ResponseEntity<?> response = videoController.publishVideo(file, videoMetaDataDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(videoMetaDataDTO, response.getBody());
        verify(videoService, times(1)).publishVideo(user, file, videoMetaDataDTO);
    }

    @Test
    void testPublishVideo_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.publishVideo(file, new VideoMetaDataDTO());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testPublishVideo_BadRequest() {
        User user = new User();
        user.setId(1L);
        when(userService.getLoggedInUser()).thenReturn(user);

        ResponseEntity<?> response = videoController.publishVideo(null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateVideoMetaData_Success() {
        User user = new User();
        user.setId(1L);
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.saveVideoMetaData(user, videoMetaDataDTO)).thenReturn(videoMetaDataDTO);

        ResponseEntity<?> response = videoController.updateVideoMetaData(1L, videoMetaDataDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(videoMetaDataDTO, response.getBody());
        verify(videoService, times(1)).saveVideoMetaData(user, videoMetaDataDTO);
    }

    @Test
    void testUpdateVideoMetaData_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.updateVideoMetaData(1L, new VideoMetaDataDTO());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testUpdateVideoMetaData_BadRequest() {
        User user = new User();
        user.setId(1L);
        when(userService.getLoggedInUser()).thenReturn(user);

        ResponseEntity<?> response = videoController.updateVideoMetaData(1L, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testPlayVideo_Success() {
        User user = new User();
        user.setId(1L);
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        Resource resource = mock(Resource.class);
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.getVideoMetaData(user, null, 1L)).thenReturn(videoMetaDataDTO);
        when(videoService.playVideoAsResource(user, request, 1L)).thenReturn(resource);

        ResponseEntity<?> response = videoController.playVideo(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resource, response.getBody());
        verify(videoService, times(1)).getVideoMetaData(user, null, 1L);
        verify(videoService, times(1)).playVideoAsResource(user, request, 1L);
    }

    @Test
    void testPlayVideo_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.playVideo(1L, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetVideoMetaData_Success() {
        User user = new User();
        user.setId(1L);
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.getVideoMetaData(user, request, 1L)).thenReturn(videoMetaDataDTO);

        ResponseEntity<?> response = videoController.getVideoMetaData(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(videoMetaDataDTO, response.getBody());
        verify(videoService, times(1)).getVideoMetaData(user, request, 1L);
    }

    @Test
    void testGetVideoMetaData_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.getVideoMetaData(1L, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testDeleteVideo_Success() {
        User user = new User();
        user.setId(1L);
        when(userService.getLoggedInUser()).thenReturn(user);

        ResponseEntity<?> response = videoController.deleteVideo(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(videoService, times(1)).deleteVideo(user, 1L);
    }

    @Test
    void testDeleteVideo_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.deleteVideo(1L);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testFindAllVideos_Success() {
        User user = new User();
        user.setId(1L);
        List<VideoMetaDataProjection> videos = List.of(mock(VideoMetaDataProjection.class));
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.getAllVideos()).thenReturn(videos);

        ResponseEntity<?> response = videoController.findAllVideos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(videos, response.getBody());
        verify(videoService, times(1)).getAllVideos();
    }

    @Test
    void testFindAllVideos_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.findAllVideos();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testFindAllVideoImpressions_Success() {
        User user = new User();
        user.setId(1L);
        List<VideoImpression> impressions = List.of(mock(VideoImpression.class));
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.getVideoImpressions(1L)).thenReturn(impressions);

        ResponseEntity<?> response = videoController.findAllVideoImpressions(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(impressions, response.getBody());
        verify(videoService, times(1)).getVideoImpressions(1L);
    }

    @Test
    void testFindAllVideoImpressions_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.findAllVideoImpressions(1L);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testFindAllVideoViews_Success() {
        User user = new User();
        user.setId(1L);
        List<VideoView> views = List.of(mock(VideoView.class));
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.getVideoViews(1L)).thenReturn(views);

        ResponseEntity<?> response = videoController.findAllVideoViews(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(views, response.getBody());
        verify(videoService, times(1)).getVideoViews(1L);
    }

    @Test
    void testFindAllVideoViews_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.findAllVideoViews(1L);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetByTitle_Success() {
        User user = new User();
        user.setId(1L);
        List<VideoMetaDataProjection> videos = List.of(mock(VideoMetaDataProjection.class));
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.searchByTitle("test title")).thenReturn(videos);

        ResponseEntity<?> response = videoController.getByTitle("test title");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(videos, response.getBody());
        verify(videoService, times(1)).searchByTitle("test title");
    }

    @Test
    void testGetByTitle_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.getByTitle("test title");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetByTitle_BadRequest() {
        User user = new User();
        user.setId(1L);
        when(userService.getLoggedInUser()).thenReturn(user);

        ResponseEntity<?> response = videoController.getByTitle("");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetByDirector_Success() {
        User user = new User();
        user.setId(1L);
        List<VideoMetaDataProjection> videos = List.of(mock(VideoMetaDataProjection.class));
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.searchByDirector("test director")).thenReturn(videos);

        ResponseEntity<?> response = videoController.getByDirector("test director");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(videos, response.getBody());
        verify(videoService, times(1)).searchByDirector("test director");
    }

    @Test
    void testGetByDirector_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.getByDirector("test director");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetByDirector_BadRequest() {
        User user = new User();
        user.setId(1L);
        when(userService.getLoggedInUser()).thenReturn(user);

        ResponseEntity<?> response = videoController.getByDirector("");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetByMainActor_Success() {
        User user = new User();
        user.setId(1L);
        List<VideoMetaDataProjection> videos = List.of(mock(VideoMetaDataProjection.class));
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.searchByMainActor("test actor")).thenReturn(videos);

        ResponseEntity<?> response = videoController.getByMainActor("test actor");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(videos, response.getBody());
        verify(videoService, times(1)).searchByMainActor("test actor");
    }

    @Test
    void testGetByMainActor_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.getByMainActor("test actor");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetByMainActor_BadRequest() {
        User user = new User();
        user.setId(1L);
        when(userService.getLoggedInUser()).thenReturn(user);

        ResponseEntity<?> response = videoController.getByMainActor("");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetByRunningTime_Success() {
        User user = new User();
        user.setId(1L);
        List<VideoMetaDataProjection> videos = List.of(mock(VideoMetaDataProjection.class));
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.searchByRunningTime(120, SearchComparator.GREATER_OR_EQUAL)).thenReturn(videos);

        ResponseEntity<?> response = videoController.getByRunningTime(120, SearchComparator.GREATER_OR_EQUAL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(videos, response.getBody());
        verify(videoService, times(1)).searchByRunningTime(120, SearchComparator.GREATER_OR_EQUAL);
    }

    @Test
    void testGetByRunningTime_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.getByRunningTime(120, SearchComparator.GREATER_OR_EQUAL);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetByRunningTime_BadRequest() {
        User user = new User();
        user.setId(1L);
        when(userService.getLoggedInUser()).thenReturn(user);

        ResponseEntity<?> response = videoController.getByRunningTime(120, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetByGenre_Success() {
        User user = new User();
        user.setId(1L);
        List<VideoMetaDataProjection> videos = List.of(mock(VideoMetaDataProjection.class));
        when(userService.getLoggedInUser()).thenReturn(user);
        when(videoService.searchByGenre(Genre.ACTION)).thenReturn(videos);

        ResponseEntity<?> response = videoController.getByGenre(Genre.ACTION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(videos, response.getBody());
        verify(videoService, times(1)).searchByGenre(Genre.ACTION);
    }

    @Test
    void testGetByGenre_Unauthorized() {
        when(userService.getLoggedInUser()).thenReturn(null);

        ResponseEntity<?> response = videoController.getByGenre(Genre.ACTION);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetByGenre_BadRequest() {
        User user = new User();
        user.setId(1L);
        when(userService.getLoggedInUser()).thenReturn(user);

        ResponseEntity<?> response = videoController.getByGenre(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
