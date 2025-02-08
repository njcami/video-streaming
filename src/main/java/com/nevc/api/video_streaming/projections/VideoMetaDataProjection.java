package com.nevc.api.video_streaming.projections;

import com.nevc.api.video_streaming.enums.Genre;

import java.util.Set;

public interface VideoMetaDataProjection {

    String getTitle();

    String getDirectorName();

    String getMainActor();

    int getYearOfRelease();

    Set<Genre> getGenre();

    int getRunningTime();
}
