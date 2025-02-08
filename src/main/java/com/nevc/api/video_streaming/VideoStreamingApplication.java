package com.nevc.api.video_streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class VideoStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoStreamingApplication.class, args);
    }

}
