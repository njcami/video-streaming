package com.nevc.api.video_streaming.repositories;

import com.nevc.api.video_streaming.entities.VideoImpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoImpressionRepository extends JpaRepository<VideoImpression, Long> {
    List<VideoImpression> findAllByVideoMetaData_Id(Long videoId);
}
