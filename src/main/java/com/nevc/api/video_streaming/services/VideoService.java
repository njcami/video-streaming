package com.nevc.api.video_streaming.services;

import com.nevc.api.video_streaming.dto.VideoMetaDataDTO;
import com.nevc.api.video_streaming.entities.User;
import com.nevc.api.video_streaming.entities.VideoImpression;
import com.nevc.api.video_streaming.entities.VideoView;
import com.nevc.api.video_streaming.enums.Genre;
import com.nevc.api.video_streaming.enums.SearchComparator;
import com.nevc.api.video_streaming.exceptions.ResourceNotFoundException;
import com.nevc.api.video_streaming.projections.VideoMetaDataProjection;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    VideoMetaDataDTO getVideoMetaData(User user, HttpServletRequest request, Long videoId);

    VideoMetaDataDTO saveVideoMetaData(User user, VideoMetaDataDTO videoMetaDataDTO);

    VideoMetaDataDTO publishVideo(User user, MultipartFile file, VideoMetaDataDTO videoMetaDataDTO);

    Resource playVideoAsResource(User user, HttpServletRequest request, Long videoId);

    void deleteVideo(User user, Long videoId) throws ResourceNotFoundException;

    List<VideoView> getVideoViews(Long videoId);

    List<VideoImpression> getVideoImpressions(Long videoId);

    List<VideoMetaDataProjection> getAllVideos();

    List<VideoMetaDataProjection> searchByTitle(String title);

    List<VideoMetaDataProjection> searchByDirector(String directorName);

    List<VideoMetaDataProjection> searchByMainActor(String mainActor);

    List<VideoMetaDataProjection> searchByRunningTime(int runningTime, SearchComparator searchComparator);

    List<VideoMetaDataProjection> searchByGenre(Genre genre);
}
