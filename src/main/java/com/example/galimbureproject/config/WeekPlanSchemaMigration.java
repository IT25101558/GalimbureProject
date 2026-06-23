package com.example.galimbureproject.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

@Component
@Order(3)
public class WeekPlanSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public WeekPlanSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE weekplan_table DROP CONSTRAINT IF EXISTS uk_weekplan_table_week_number");
        jdbcTemplate.execute("ALTER TABLE student_marks DROP CONSTRAINT IF EXISTS uk_student_marks_student_week");

        Integer legacyWeekPlanCount = jdbcTemplate.queryForObject(
                "select count(*) from weekplan_table where batch_id is null",
                Integer.class
        );
        if (legacyWeekPlanCount == null || legacyWeekPlanCount == 0) {
            return;
        }

        LocalDate legacyBatchDate = resolveLegacyBatchDate();
        Integer legacyBatchYear = legacyBatchDate.getYear();
        Long legacyBatchId = resolveOrCreateLegacyBatch(legacyBatchYear, legacyBatchDate);

        jdbcTemplate.update("update weekplan_table set batch_id = ? where batch_id is null", legacyBatchId);
        jdbcTemplate.update("""
                update student_marks sm
                set week_plan_id = (
                    select wp.id
                    from weekplan_table wp
                    where wp.batch_id = ?
                      and wp.week_number = sm.week_number
                    limit 1
                )
                where sm.week_plan_id is null
                  and sm.week_number is not null
                """, legacyBatchId);
    }

    private LocalDate resolveLegacyBatchDate() {
        Date legacyDate = jdbcTemplate.queryForObject(
                "select min(week_start_date) from weekplan_table where batch_id is null",
                Date.class
        );
        return legacyDate != null ? legacyDate.toLocalDate() : LocalDate.now();
    }

    private Long resolveOrCreateLegacyBatch(Integer batchYear, LocalDate batchDate) {
        Long batchId = jdbcTemplate.query(
                "select id from batch_table where batch_year = ? and lower(place) = lower(?)",
                (rs, rowNum) -> rs.getLong(1),
                batchYear,
                "Legacy batch"
        ).stream().findFirst().orElse(null);

        if (batchId != null) {
            return batchId;
        }

        jdbcTemplate.update(
                "insert into batch_table (batch_year, place, batch_date) values (?, ?, ?)",
                batchYear,
                "Legacy batch",
                batchDate
        );

        return jdbcTemplate.query(
                "select id from batch_table where batch_year = ? and lower(place) = lower(?)",
                (rs, rowNum) -> rs.getLong(1),
                batchYear,
                "Legacy batch"
        ).stream().findFirst().orElseThrow();
    }
}
