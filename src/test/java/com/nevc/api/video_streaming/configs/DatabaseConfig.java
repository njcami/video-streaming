package com.nevc.api.video_streaming.configs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;

@Profile("test")
@Configuration
public class DatabaseConfig {

    @Bean
    public MySQLContainer<?> mysqlTestContainer() {
        MySQLContainer<?> mysqlTestContainer = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("video_streaming_test")
                .withUsername("test_app_user")
                .withPassword("test_app_password");
        mysqlTestContainer.start();
        System.setProperty("TEST_DB_URL", mysqlTestContainer.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", mysqlTestContainer.getUsername());
        System.setProperty("TEST_DB_PASSWORD", mysqlTestContainer.getPassword());
        return mysqlTestContainer;
    }

    @Bean
    public DataSource dataSource(MySQLContainer<?> mysqlTestContainer) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlTestContainer.getJdbcUrl());
        config.setUsername(mysqlTestContainer.getUsername());
        config.setPassword(mysqlTestContainer.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return new HikariDataSource(config);
    }
}
