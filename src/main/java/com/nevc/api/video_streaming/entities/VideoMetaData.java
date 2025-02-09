package com.nevc.api.video_streaming.entities;

import com.nevc.api.video_streaming.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "videos_meta_data")
public class VideoMetaData implements Serializable {

    @Serial
    private static final long serialVersionUID = 11423L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Schema(description = "Title of the video", example = "The Dark Knight")
    @Column(name = "title", nullable = false)
    private String title;

    @Schema(description = "Synopsis of the video", example = "The Dark Knight is a 2008 superhero film directed by Christopher Nolan.")
    @Column(name = "synopsis", length = 3000)
    private String synopsis;

    @NotEmpty
    @Schema(description = "Director of the video", example = "Christopher Nolan")
    @Column(name = "director", nullable = false)
    private String directorName;

    @NotEmpty
    @Schema(description = "Main Actor of the video", example = "Christian Bale")
    @Column(name = "main_actor", nullable = false)
    private String mainActor;

    @ManyToMany
    @JoinTable(
            name = "videos_meta_data_actors",
            joinColumns = @JoinColumn(name = "video_meta_data_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @Builder.Default
    @Schema(description = "Cast of the video")
    private Set<Actor> cast = new HashSet<>();

    @Min(1900)
    @Max(2050)
    @Schema(description = "Year of release of the video", example = "2008")
    @Column(name = "year_of_release", nullable = false)
    private int yearOfRelease;

    @NotNull
    @Schema(description = "Publish date of the video", example = "2024-12-31")
    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Schema(description = "Last update date of the video", example = "2024-12-31")
    @Column(name = "updated_date")
    private LocalDate lastUpdatedDate;

    @Schema(description = "Deletion date of the video", example = "2024-12-31")
    @Column(name = "deleted_date")
    private LocalDate deletedDate;

    @NotNull
    @ManyToOne(optional = false)
    @Schema(description = "User who published the video")
    @JoinColumn(name = "published_by", nullable = false)
    private User publishedBy;

    @ManyToOne
    @Schema(description = "User who last updated the video")
    @JoinColumn(name = "last_updated_by")
    private User lastUpdatedBy;

    @ManyToOne
    @Schema(description = "User who deleted the video")
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    @NotNull
    @Schema(description = "Genres of the video", example = "[\"ACTION\", \"THRILLER\"]")
    @Column(name = "genre")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(name = "videos_meta_data_genres", joinColumns = @JoinColumn(name = "video_meta_data_id"))
    private Set<Genre> genre = new HashSet<>();

    @Schema(description = "Running time of the video in minutes", example = "152")
    @Column(name = "running_time", nullable = false)
    private int runningTime;

    @Schema(description = "File size of the video in bytes", example = "8096")
    @Column(name = "file_size")
    private long fileSize;

    @Schema(description = "File path of the video", example = "/home/user/videos/")
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Schema(description = "File name of the video", example = "the_dark_knight.mp4")
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Schema(description = "File extension of the video", example = "mp4")
    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @Schema(description = "Active status of the video, (soft-deleted if false)", example = "true")
    @Builder.Default
    @Column(name = "active")
    private boolean active = true;
}
