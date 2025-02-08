package com.nevc.api.video_streaming.repositories;

import com.nevc.api.video_streaming.entities.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoViewRepository extends JpaRepository<VideoView, Long> {
    List<VideoView> findAllByVideoMetaData_Id(Long videoId);
}
