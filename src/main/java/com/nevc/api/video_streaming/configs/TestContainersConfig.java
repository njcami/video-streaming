package com.nevc.api.video_streaming.configs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;

@Profile("!test")
@Configuration
public class TestContainersConfig {

    @Bean
    public MySQLContainer<?> mysqlContainer() {
        MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("video_streaming")
                .withUsername("app_user")
                .withPassword("app_password");
        mysqlContainer.start();
        System.setProperty("DB_URL", mysqlContainer.getJdbcUrl());
        System.setProperty("DB_USERNAME", mysqlContainer.getUsername());
        System.setProperty("DB_PASSWORD", mysqlContainer.getPassword());
        return mysqlContainer;
    }

    @Bean
    public DataSource dataSource(MySQLContainer<?> mysqlContainer) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlContainer.getJdbcUrl());
        config.setUsername(mysqlContainer.getUsername());
        config.setPassword(mysqlContainer.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return new HikariDataSource(config);
    }
}
