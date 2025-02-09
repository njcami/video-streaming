package com.nevc.api.video_streaming.services;

import com.nevc.api.video_streaming.dto.VideoMetaDataDTO;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.entities.VideoImpression;
import com.nevc.api.video_streaming.entities.VideoMetaData;
import com.nevc.api.video_streaming.entities.VideoView;
import com.nevc.api.video_streaming.enums.Genre;
import com.nevc.api.video_streaming.enums.SearchComparator;
import com.nevc.api.video_streaming.exceptions.BadRequestException;
import com.nevc.api.video_streaming.exceptions.ResourceNotFoundException;
import com.nevc.api.video_streaming.exceptions.UnAuthorizedException;
import com.nevc.api.video_streaming.exceptions.VideoProcessingException;
import com.nevc.api.video_streaming.mapper.VideoMetaDataMapper;
import com.nevc.api.video_streaming.projections.VideoMetaDataProjection;
import com.nevc.api.video_streaming.repositories.VideoImpressionRepository;
import com.nevc.api.video_streaming.repositories.VideoMetaDataRepository;
import com.nevc.api.video_streaming.repositories.VideoViewRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoMetaDataRepository videoMetaDataRepository;
    private final VideoImpressionRepository videoImpressionRepository;
    private final VideoViewRepository videoViewRepository;

    @Value("${video.publishPath}")
    private String publishPath;

    @Override
    public VideoMetaDataDTO getVideoMetaData(User user, HttpServletRequest request, Long videoId) {
        VideoMetaData videoMetaData = videoMetaDataRepository.findByIdAndActiveTrue(videoId).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Video with id:%d not found", videoId)));
        if (request != null) {
            VideoImpression videoImpression = VideoImpression.builder()
                    .user(user)
                    .videoMetaData(videoMetaData)
                    .userIp(request.getRemoteAddr())
                    .impressionDate(LocalDateTime.now())
                    .userAgent(request.getHeader("User-Agent"))
                    .build();
            videoImpressionRepository.save(videoImpression);
        }
        return VideoMetaDataMapper.mapToVideoMetaDataDto(videoMetaData);
    }

    @Override
    public VideoMetaDataDTO saveVideoMetaData(User user, VideoMetaDataDTO videoMetaDataDTO) {
        VideoMetaData videoMetaData = videoMetaDataRepository.findByIdAndActiveTrue(videoMetaDataDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Video with id:%d not found", videoMetaDataDTO.getId())));
        videoMetaData.setTitle(videoMetaDataDTO.getTitle());
        videoMetaData.setSynopsis(videoMetaDataDTO.getSynopsis());
        videoMetaData.setCast(videoMetaDataDTO.getCast());
        videoMetaData.setDirectorName(videoMetaDataDTO.getDirectorName());
        videoMetaData.setGenre(videoMetaDataDTO.getGenre());
        videoMetaData.setYearOfRelease(videoMetaDataDTO.getYearOfRelease());
        videoMetaData.setRunningTime(videoMetaDataDTO.getRunningTime());
        videoMetaData.setLastUpdatedDate(LocalDate.now());
        videoMetaData.setLastUpdatedBy(user);
        return VideoMetaDataMapper.mapToVideoMetaDataDto(videoMetaDataRepository.save(videoMetaData));
    }

    @Override
    public VideoMetaDataDTO publishVideo(User user, MultipartFile file, VideoMetaDataDTO videoMetaData) {
        if (file == null || file.isEmpty()) {
            log.error("File for metadata:{} is empty", videoMetaData);
            throw new BadRequestException("File is empty");
        }
        if (user == null) {
            log.error("User is not authenticated");
            throw new UnAuthorizedException("User is not authenticated");
        }
        log.info("Publishing video with metadata:{} by user with id:{}", videoMetaData, user.getId());

        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(Objects.requireNonNullElse(publishPath, "uploads/"), fileName);
        log.info("File path:{}", filePath);

        String publishStage = "createDirectory";
        try {
            Files.createDirectories(filePath.getParent());
            publishStage = "copyFile";
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save file during {} : {}", publishStage, e.getMessage());
            throw new VideoProcessingException("Failed to save file during " + publishStage + " : " + e.getMessage());
        }

        VideoMetaData video = VideoMetaData.builder()
                .title(videoMetaData.getTitle())
                .synopsis(videoMetaData.getSynopsis())
                .cast(videoMetaData.getCast())
                .directorName(videoMetaData.getDirectorName())
                .mainActor(videoMetaData.getMainActor())
                .genre(videoMetaData.getGenre())
                .fileSize(file.getSize())
                .yearOfRelease(videoMetaData.getYearOfRelease())
                .filePath(filePath.toString())
                .fileExtension(file.getContentType())
                .fileName(fileName)
                .runningTime(videoMetaData.getRunningTime())
                .publishedDate(LocalDate.now())
                .publishedBy(user)
                .active(true)
                .build();

        VideoMetaData savedMetaData = videoMetaDataRepository.save(video);
        log.info("Video with id:{} is published by user with id:{}", savedMetaData.getId(), user.getId());
        return VideoMetaDataMapper.mapToVideoMetaDataDto(savedMetaData);
    }

    @Override
    public Resource playVideoAsResource(User user, HttpServletRequest request, Long videoId) {
        VideoMetaData videoMetaData = videoMetaDataRepository.findByIdAndActiveTrue(videoId).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Video with id:%d not found", videoId)));
        if (request != null) {
            VideoView videoView = VideoView.builder()
                    .user(user)
                    .videoMetaData(videoMetaData)
                    .userIp(request.getRemoteAddr())
                    .viewDate(LocalDateTime.now())
                    .userAgent(request.getHeader("User-Agent"))
                    .build();
            videoViewRepository.save(videoView);
        }
        log.info("Loading video file by user id: {}, video id: {}", user.getId(), videoId);
        Path path = Paths.get(videoMetaData.getFilePath() + videoMetaData.getFileName());
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new BadRequestException("Failed to load video file: " + e.getMessage());
        }
    }

    @Override
    public void deleteVideo(User user, Long videoId) {
        VideoMetaData videoMetaData = videoMetaDataRepository.findByIdAndActiveTrue(videoId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Video with id:%d not found", videoId)));
        videoMetaData.setDeletedDate(LocalDate.now());
        videoMetaData.setDeletedBy(user);
        videoMetaData.setActive(false);
        videoMetaDataRepository.save(videoMetaData);
    }

    @Override
    public List<VideoView> getVideoViews(Long videoId) {
        return videoViewRepository.findAllByVideoMetaData_Id(videoId);
    }

    @Override
    public List<VideoImpression> getVideoImpressions(Long videoId) {
        return videoImpressionRepository.findAllByVideoMetaData_Id(videoId);
    }

    @Override
    public List<VideoMetaDataProjection> getAllVideos() {
        return videoMetaDataRepository.findAllByActiveTrue();
    }

    @Override
    public List<VideoMetaDataProjection> searchByTitle(String title) {
        return videoMetaDataRepository.findByTitleContainingIgnoreCaseAndActiveTrue(title);
    }

    @Override
    public List<VideoMetaDataProjection> searchByDirector(String directorName) {
        return videoMetaDataRepository.findByDirectorNameContainingIgnoreCaseAndActiveTrue(directorName);
    }

    @Override
    public List<VideoMetaDataProjection> searchByMainActor(String mainActor) {
        return videoMetaDataRepository.findByMainActorContainingIgnoreCaseAndActiveTrue(mainActor);
    }

    @Override
    public List<VideoMetaDataProjection> searchByRunningTime(int runningTime, SearchComparator searchComparator) {
        return switch (searchComparator) {
            case GREATER_OR_EQUAL ->
                    videoMetaDataRepository.findByRunningTimeGreaterThanEqualAndActiveTrue(runningTime);
            case LESS_OR_EQUAL -> videoMetaDataRepository.findByRunningTimeLessThanEqualAndActiveTrue(runningTime);
            default -> videoMetaDataRepository.findByRunningTimeAndActiveTrue(runningTime);
        };
    }

    @Override
    public List<VideoMetaDataProjection> searchByGenre(Genre genre) {
        return videoMetaDataRepository.findByGenreContainingAndActiveTrue(genre);
    }
}
