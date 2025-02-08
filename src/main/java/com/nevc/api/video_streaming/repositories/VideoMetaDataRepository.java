package com.nevc.api.video_streaming.repositories;

import com.nevc.api.video_streaming.entities.VideoMetaData;
import com.nevc.api.video_streaming.enums.Genre;
import com.nevc.api.video_streaming.projections.VideoMetaDataProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoMetaDataRepository extends JpaRepository<VideoMetaData, Long>, JpaSpecificationExecutor<VideoMetaData> {
    Optional<VideoMetaData> findByIdAndActiveTrue(Long videoId);

    List<VideoMetaDataProjection> findAllByActiveTrue();

    List<VideoMetaDataProjection> findByTitleContainingIgnoreCaseAndActiveTrue(String title);

    List<VideoMetaDataProjection> findByDirectorNameContainingIgnoreCaseAndActiveTrue(String directorName);

    List<VideoMetaDataProjection> findByMainActorContainingIgnoreCaseAndActiveTrue(String mainActor);

    List<VideoMetaDataProjection> findByRunningTimeGreaterThanEqualAndActiveTrue(int runningTime);

    List<VideoMetaDataProjection> findByRunningTimeLessThanEqualAndActiveTrue(int runningTime);

    List<VideoMetaDataProjection> findByRunningTimeAndActiveTrue(int runningTime);

    List<VideoMetaDataProjection> findByGenreContainingAndActiveTrue(Genre genre);
}
