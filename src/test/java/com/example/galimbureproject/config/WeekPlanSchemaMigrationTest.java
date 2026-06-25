package com.example.galimbureproject.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeekPlanSchemaMigrationTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private WeekPlanSchemaMigration migration;

    @Test
    void runDropsLegacyWeekConstraintsBeforeBackfill() {
        lenient().when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), any(Object[].class)))
                .thenReturn(true);
        lenient().when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(Object[].class)))
                .thenReturn("text");
        lenient().when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenReturn(0);
        lenient().when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of());

        migration.run(new DefaultApplicationArguments(new String[0]));

        verify(jdbcTemplate).execute("ALTER TABLE weekplan_table DROP CONSTRAINT IF EXISTS uk_weekplan_table_week_number");
        verify(jdbcTemplate).execute("ALTER TABLE weekplan_table DROP CONSTRAINT IF EXISTS uk_weekplan_table_batch_week_number");
        verify(jdbcTemplate).execute("ALTER TABLE student_marks DROP CONSTRAINT IF EXISTS uk_student_marks_student_week");
    }
}
