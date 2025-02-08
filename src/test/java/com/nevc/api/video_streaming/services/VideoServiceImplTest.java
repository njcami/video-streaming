package com.nevc.api.video_streaming.services;

import com.nevc.api.video_streaming.dto.VideoMetaDataDTO;
import com.nevc.api.video_streaming.entities.Actor;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.entities.VideoImpression;
import com.nevc.api.video_streaming.entities.VideoMetaData;
import com.nevc.api.video_streaming.entities.VideoView;
import com.nevc.api.video_streaming.enums.Genre;
import com.nevc.api.video_streaming.exceptions.BadRequestException;
import com.nevc.api.video_streaming.exceptions.ResourceNotFoundException;
import com.nevc.api.video_streaming.exceptions.UnAuthorizedException;
import com.nevc.api.video_streaming.repositories.VideoImpressionRepository;
import com.nevc.api.video_streaming.repositories.VideoMetaDataRepository;
import com.nevc.api.video_streaming.repositories.VideoViewRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoServiceImplTest {

    @Mock
    private VideoMetaDataRepository videoMetaDataRepository;

    @Mock
    private VideoImpressionRepository videoImpressionRepository;

    @Mock
    private VideoViewRepository videoViewRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private VideoServiceImpl videoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetVideoMetaData_Valid() {
        User user = new User();
        user.setId(1L);
        VideoMetaData videoMetaData = new VideoMetaData();
        videoMetaData.setId(1L);
        when(videoMetaDataRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(videoMetaData));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        VideoMetaDataDTO result = videoService.getVideoMetaData(user, request, 1L);

        assertNotNull(result);
        verify(videoImpressionRepository, times(1)).save(any(VideoImpression.class));
    }

    @Test
    void testGetVideoMetaData_NotFound() {
        when(videoMetaDataRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> videoService.getVideoMetaData(null, null, 1L));
    }

    @Test
    void testSaveVideoMetaData_Valid() {
        User user = new User();
        user.setId(1L);
        VideoMetaData videoMetaData = new VideoMetaData();
        videoMetaData.setId(1L);
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        videoMetaDataDTO.setId(1L);
        when(videoMetaDataRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(videoMetaData));
        when(videoMetaDataRepository.save(videoMetaData)).thenReturn(videoMetaData);

        VideoMetaDataDTO result = videoService.saveVideoMetaData(user, videoMetaDataDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(videoMetaDataRepository, times(1)).save(videoMetaData);
    }

    @Test
    void testSaveVideoMetaData_NotFound() {
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        videoMetaDataDTO.setId(1L);
        when(videoMetaDataRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> videoService.saveVideoMetaData(null, videoMetaDataDTO));
    }

    @Test
    void testPublishVideo_Valid() {
        User user = new User();
        user.setId(1L);

        MultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4",
                "This is a dummy video file content.".getBytes(StandardCharsets.UTF_8));
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        videoMetaDataDTO.setTitle("Test Video");

        VideoMetaData videoMetaData = VideoMetaData.builder()
                .id(1L)
                .title("Test Video")
                .filePath("/videos/")
                .fileName("test.mp4")
                .fileExtension("mp4")
                .genre(Set.of(Genre.ACTION, Genre.THRILLER))
                .cast(Set.of(new Actor(1L, "Christian Bale", null), new Actor(2L, "Heath Ledger", null)))
                .directorName("Christopher Nolan")
                .yearOfRelease(2008)
                .publishedDate(LocalDate.now())
                .fileSize(8096)
                .publishedBy(user)
                .runningTime(120)
                .active(true)
                .build();
        when(videoMetaDataRepository.findByIdAndActiveTrue(anyLong())).thenReturn(Optional.of(videoMetaData));
        when(videoMetaDataRepository.save(any(VideoMetaData.class))).thenReturn(videoMetaData);

        VideoMetaDataDTO result = videoService.publishVideo(user, file, videoMetaDataDTO);

        assertNotNull(result);

        // Capture the argument passed to the save method
        ArgumentCaptor<VideoMetaData> captor = ArgumentCaptor.forClass(VideoMetaData.class);
        verify(videoMetaDataRepository, times(1)).save(captor.capture());

        // Verify that the captured argument is an instance of VideoMetaData
        VideoMetaData capturedArgument = captor.getValue();
        assertNotNull(capturedArgument);
    }

    @Test
    void testPublishVideo_EmptyFile() {
        User user = new User();
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        when(file.isEmpty()).thenReturn(true);

        assertThrows(BadRequestException.class, () -> videoService.publishVideo(user, file, videoMetaDataDTO));
    }

    @Test
    void testPublishVideo_UnauthenticatedUser() {
        User user = null;
        VideoMetaDataDTO videoMetaDataDTO = new VideoMetaDataDTO();
        when(file.isEmpty()).thenReturn(false);

        assertThrows(UnAuthorizedException.class, () -> videoService.publishVideo(user, file, videoMetaDataDTO));
    }

    @Test
    void testPlayVideoAsResource_Valid() throws MalformedURLException {
        User user = new User();
        user.setId(1L);
        VideoMetaData videoMetaData = new VideoMetaData();
        videoMetaData.setFilePath("/videos/");
        videoMetaData.setFileName("test.mp4");
        when(videoMetaDataRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(videoMetaData));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        Resource result = videoService.playVideoAsResource(user, request, 1L);

        assertNotNull(result);
        assertInstanceOf(UrlResource.class, result);
        verify(videoViewRepository, times(1)).save(any(VideoView.class));
    }

    @Test
    void testPlayVideoAsResource_NotFound() {
        when(videoMetaDataRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> videoService.playVideoAsResource(null, null, 1L));
    }

    @Test
    void testDeleteVideo_Valid() {
        User user = new User();
        user.setId(1L);
        VideoMetaData videoMetaData = new VideoMetaData();
        videoMetaData.setId(1L);
        when(videoMetaDataRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(videoMetaData));

        videoService.deleteVideo(user, 1L);

        verify(videoMetaDataRepository, times(1)).save(videoMetaData);
        assertFalse(videoMetaData.isActive());
    }

    @Test
    void testDeleteVideo_NotFound() {
        User user = new User();
        when(videoMetaDataRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> videoService.deleteVideo(user, 1L));
    }
}
