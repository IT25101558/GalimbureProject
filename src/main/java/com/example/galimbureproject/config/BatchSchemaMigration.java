package com.example.galimbureproject.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(0)
public class BatchSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public BatchSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE batch_table DROP CONSTRAINT IF EXISTS uk_batch_table_batch_year");

        Boolean compositeConstraintExists = jdbcTemplate.queryForObject(
                """
                        select count(*) > 0
                        from information_schema.table_constraints
                        where table_name = 'batch_table'
                          and constraint_name = 'uk_batch_table_batch_year_place'
                        """,
                Boolean.class
        );

        if (!Boolean.TRUE.equals(compositeConstraintExists)) {
            jdbcTemplate.execute(
                    "ALTER TABLE batch_table ADD CONSTRAINT uk_batch_table_batch_year_place UNIQUE (batch_year, place)"
            );
        }
    }
}
