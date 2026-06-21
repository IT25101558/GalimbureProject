package com.example.galimbureproject.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(Environment environment) {
        String rawUrl = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("spring.datasource.url")
        );

        if (rawUrl == null) {
            throw new IllegalStateException("Set DATABASE_URL or SPRING_DATASOURCE_URL for the PostgreSQL connection.");
        }

        DatabaseConnection connection = DatabaseConnection.from(rawUrl);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(connection.jdbcUrl());
        dataSource.setUsername(firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_USERNAME"),
                environment.getProperty("spring.datasource.username"),
                connection.username()
        ));
        dataSource.setPassword(firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
                environment.getProperty("spring.datasource.password"),
                connection.password()
        ));
        if (connection.jdbcUrl().startsWith("jdbc:postgresql://")) {
            dataSource.setDriverClassName("org.postgresql.Driver");
        }
        return dataSource;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record DatabaseConnection(String jdbcUrl, String username, String password) {
        static DatabaseConnection from(String rawUrl) {
            if (rawUrl.startsWith("jdbc:")) {
                return new DatabaseConnection(rawUrl, null, null);
            }

            URI uri = URI.create(rawUrl.replaceFirst("^postgres://", "postgresql://"));
            String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();
            String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + port + uri.getPath() + query;

            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String[] userInfoParts = uri.getUserInfo().split(":", 2);
                username = decode(userInfoParts[0]);
                if (userInfoParts.length > 1) {
                    password = decode(userInfoParts[1]);
                }
            }

            return new DatabaseConnection(jdbcUrl, username, password);
        }

        private static String decode(String value) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }
    }
}
