package com.nevc.api.video_streaming.dto;

import com.nevc.api.video_streaming.entities.Actor;
import com.nevc.api.video_streaming.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VideoMetaDataDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1431423L;

    private Long id;

    @NotEmpty
    @Schema(description = "Title of the video", example = "The Dark Knight")
    private String title;

    @Schema(description = "Synopsis of the video", example = "The Dark Knight is a 2008 superhero film directed by Christopher Nolan.")
    private String synopsis;

    @NotEmpty
    @Schema(description = "Director of the video", example = "Christopher Nolan")
    private String directorName;

    @NotEmpty
    @Schema(description = "Main Actor of the video", example = "Christian Bale")
    private String mainActor;

    @Builder.Default
    @Schema(description = "Cast of the video")
    private Set<Actor> cast = new HashSet<>();

    @NotNull
    @Schema(description = "Year of release of the video", example = "2008")
    private int yearOfRelease;

    @Builder.Default
    @Schema(description = "Genres of the video", example = "[\"ACTION\", \"THRILLER\"]")
    private Set<Genre> genre = new HashSet<>();

    @NotNull
    @Schema(description = "Running time of the video in minutes", example = "152")
    private int runningTime;

    @NotEmpty
    @Schema(description = "File name of the video", example = "the_dark_knight.mp4")
    @Column(name = "file_name")
    private String fileName;

    @NotEmpty
    @Schema(description = "File extension of the video", example = "mp4")
    @Column(name = "file_extension")
    private String fileExtension;
}
