package com.nevc.api.video_streaming.mapper;

import com.nevc.api.video_streaming.dto.VideoMetaDataDTO;
import com.nevc.api.video_streaming.entities.VideoMetaData;

public class VideoMetaDataMapper {

    public static VideoMetaData mapToVideoMetaData(VideoMetaDataDTO videoMetaDataDto) {
        return VideoMetaData.builder()
                .id(videoMetaDataDto.getId())
                .title(videoMetaDataDto.getTitle())
                .synopsis(videoMetaDataDto.getSynopsis())
                .fileName(videoMetaDataDto.getFileName())
                .fileExtension(videoMetaDataDto.getFileExtension())
                .yearOfRelease(videoMetaDataDto.getYearOfRelease())
                .genre(videoMetaDataDto.getGenre())
                .cast(videoMetaDataDto.getCast())
                .directorName(videoMetaDataDto.getDirectorName())
                .mainActor(videoMetaDataDto.getMainActor())
                .runningTime(videoMetaDataDto.getRunningTime())
                .build();
    }

    public static VideoMetaDataDTO mapToVideoMetaDataDto(VideoMetaData videoMetaData) {
        return VideoMetaDataDTO.builder()
                .id(videoMetaData.getId())
                .title(videoMetaData.getTitle())
                .synopsis(videoMetaData.getSynopsis())
                .fileName(videoMetaData.getFileName())
                .fileExtension(videoMetaData.getFileExtension())
                .yearOfRelease(videoMetaData.getYearOfRelease())
                .genre(videoMetaData.getGenre())
                .cast(videoMetaData.getCast())
                .directorName(videoMetaData.getDirectorName())
                .mainActor(videoMetaData.getMainActor())
                .runningTime(videoMetaData.getRunningTime())
                .build();
    }
}
