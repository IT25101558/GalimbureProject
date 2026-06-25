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
        ensureBatchDateColumnIsText();

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

    private void ensureBatchDateColumnIsText() {
        Boolean batchTableExists = jdbcTemplate.queryForObject(
                """
                        select count(*) > 0
                        from information_schema.tables
                        where lower(table_name) = lower(?)
                        """,
                Boolean.class,
                "batch_table"
        );

        if (!Boolean.TRUE.equals(batchTableExists)) {
            return;
        }

        Boolean batchDateColumnExists = jdbcTemplate.queryForObject(
                """
                        select count(*) > 0
                        from information_schema.columns
                        where lower(table_name) = lower(?)
                          and lower(column_name) = lower(?)
                        """,
                Boolean.class,
                "batch_table",
                "batch_date"
        );

        if (!Boolean.TRUE.equals(batchDateColumnExists)) {
            return;
        }

        String dataType = jdbcTemplate.queryForObject(
                """
                        select data_type
                        from information_schema.columns
                        where lower(table_name) = lower(?)
                          and lower(column_name) = lower(?)
                        limit 1
                        """,
                String.class,
                "batch_table",
                "batch_date"
        );

        if (dataType != null && !dataType.equalsIgnoreCase("text")) {
            jdbcTemplate.execute("ALTER TABLE batch_table ALTER COLUMN batch_date TYPE text USING batch_date::text");
        }
    }
}
