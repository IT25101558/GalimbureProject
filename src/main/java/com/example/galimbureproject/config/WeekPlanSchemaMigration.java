package com.example.galimbureproject.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(3)
public class WeekPlanSchemaMigration implements ApplicationRunner {

    private static final String WEEK_PLAN_TABLE = "weekplan_table";
    private static final String LEGACY_WEEK_CONSTRAINT = "uk_weekplan_table_week_number";
    private static final String LEGACY_STUDENT_MARK_CONSTRAINT = "uk_student_marks_student_week";
    private static final String MONTH_PLAN_COLUMN = "month_plan_id";
    private static final String LEGACY_BATCH_COLUMN = "batch_id";
    private static final String LEGACY_BATCH_PLACE = "Legacy batch";

    private final JdbcTemplate jdbcTemplate;

    public WeekPlanSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureBatchDateColumnIsText();

        if (!tableExists(WEEK_PLAN_TABLE)) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE weekplan_table DROP CONSTRAINT IF EXISTS " + LEGACY_WEEK_CONSTRAINT);
        jdbcTemplate.execute("ALTER TABLE student_marks DROP CONSTRAINT IF EXISTS " + LEGACY_STUDENT_MARK_CONSTRAINT);

        if (columnExists(WEEK_PLAN_TABLE, LEGACY_BATCH_COLUMN)) {
            backfillLegacyBatchAssignmentsIfNeeded();
        }

        ensureMonthPlanColumnExists();
        ensureExistingBatchesHaveYearsAndMonths();
        backfillWeekPlansToMonths();
    }

    private void backfillLegacyBatchAssignmentsIfNeeded() {
        Integer legacyWeekPlanCount = jdbcTemplate.queryForObject(
                "select count(*) from weekplan_table where batch_id is null",
                Integer.class
        );

        if (legacyWeekPlanCount == null || legacyWeekPlanCount == 0) {
            return;
        }

        LocalDate legacyBatchDate = resolveLegacyBatchDate();
        Integer legacyBatchYear = legacyBatchDate.getYear();
        Long legacyBatchId = resolveOrCreateLegacyBatch(legacyBatchYear, legacyBatchDate.toString());

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

    private void ensureMonthPlanColumnExists() {
        if (columnExists(WEEK_PLAN_TABLE, MONTH_PLAN_COLUMN)) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE weekplan_table ADD COLUMN month_plan_id BIGINT");
    }

    private void ensureExistingBatchesHaveYearsAndMonths() {
        List<BatchRow> batches = loadBatches();
        for (BatchRow batch : batches) {
            ensureDefaultYearsForBatch(batch);
        }
    }

    private void ensureDefaultYearsForBatch(BatchRow batch) {
        ensureYearAndMonths(batch, batch.batchYear());
        ensureYearAndMonths(batch, batch.batchYear() + 1);
    }

    private void ensureYearAndMonths(BatchRow batch, int yearValue) {
        Long yearPlanId = findYearPlanId(batch.id(), yearValue);
        if (yearPlanId == null) {
            OffsetDateTime now = OffsetDateTime.now();
            jdbcTemplate.update(
                    """
                            insert into yearplan_table (batch_id, year_value, created_at, updated_at)
                            values (?, ?, ?, ?)
                            """,
                    batch.id(),
                    yearValue,
                    now,
                    now
            );
            yearPlanId = findYearPlanId(batch.id(), yearValue);
        }

        if (yearPlanId == null) {
            return;
        }

        for (int monthNumber = 1; monthNumber <= 12; monthNumber++) {
            ensureMonthPlan(yearPlanId, monthNumber);
        }
    }

    private Long findYearPlanId(Long batchId, int yearValue) {
        return jdbcTemplate.query(
                """
                        select id
                        from yearplan_table
                        where batch_id = ?
                          and year_value = ?
                        order by id
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                batchId,
                yearValue
        ).stream().findFirst().orElse(null);
    }

    private Long ensureMonthPlan(Long yearPlanId, int monthNumber) {
        Long monthPlanId = findMonthPlanId(yearPlanId, monthNumber);
        if (monthPlanId != null) {
            return monthPlanId;
        }

        OffsetDateTime now = OffsetDateTime.now();
        jdbcTemplate.update(
                """
                        insert into monthplan_table (year_plan_id, month_number, paid_status, created_at, updated_at)
                        values (?, ?, ?, ?, ?)
                        """,
                yearPlanId,
                monthNumber,
                false,
                now,
                now
        );
        return findMonthPlanId(yearPlanId, monthNumber);
    }

    private Long findMonthPlanId(Long yearPlanId, int monthNumber) {
        return jdbcTemplate.query(
                """
                        select id
                        from monthplan_table
                        where year_plan_id = ?
                          and month_number = ?
                        order by id
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                yearPlanId,
                monthNumber
        ).stream().findFirst().orElse(null);
    }

    private void backfillWeekPlansToMonths() {
        boolean hasBatchColumn = columnExists(WEEK_PLAN_TABLE, LEGACY_BATCH_COLUMN);

        Map<Long, BatchRow> batchesById = new HashMap<>();
        for (BatchRow batch : loadBatches()) {
            batchesById.put(batch.id(), batch);
        }

        String weekSelectSql = hasBatchColumn
                ? """
                        select id, batch_id, week_start_date, week_number
                        from weekplan_table
                        where month_plan_id is null
                        order by id
                        """
                : """
                        select id, week_start_date, week_number
                        from weekplan_table
                        where month_plan_id is null
                        order by id
                        """;

        List<WeekRow> weeksNeedingBackfill = jdbcTemplate.query(
                weekSelectSql,
                (rs, rowNum) -> new WeekRow(
                        rs.getLong("id"),
                        hasBatchColumn ? getNullableLong(rs, LEGACY_BATCH_COLUMN) : null,
                        getNullableLocalDate(rs, "week_start_date"),
                        getNullableInteger(rs, "week_number")
                )
        );

        for (WeekRow weekRow : weeksNeedingBackfill) {
            BatchRow batch = resolveBatchForWeekRow(weekRow, batchesById);
            if (batch == null) {
                continue;
            }

            int yearValue = resolveYearValue(batch, weekRow.weekStartDate());
            int monthNumber = resolveMonthNumber(weekRow.weekStartDate());

            Long yearPlanId = findYearPlanId(batch.id(), yearValue);
            if (yearPlanId == null) {
                ensureYearAndMonths(batch, yearValue);
                yearPlanId = findYearPlanId(batch.id(), yearValue);
            }

            if (yearPlanId == null) {
                continue;
            }

            Long monthPlanId = ensureMonthPlan(yearPlanId, monthNumber);
            if (monthPlanId == null) {
                continue;
            }

            jdbcTemplate.update(
                    "update weekplan_table set month_plan_id = ? where id = ?",
                    monthPlanId,
                    weekRow.id()
            );
        }

        if (countNullMonthPlanLinks() == 0) {
            jdbcTemplate.execute("ALTER TABLE weekplan_table ALTER COLUMN month_plan_id SET NOT NULL");
        }
    }

    private BatchRow resolveBatchForWeekRow(WeekRow weekRow, Map<Long, BatchRow> batchesById) {
        if (weekRow.batchId() != null) {
            BatchRow existingBatch = batchesById.get(weekRow.batchId());
            if (existingBatch != null) {
                return existingBatch;
            }
        }

        LocalDate fallbackDate = weekRow.weekStartDate() != null ? weekRow.weekStartDate() : LocalDate.now();
        Long fallbackBatchId = resolveOrCreateLegacyBatch(fallbackDate.getYear(), fallbackDate.toString());
        BatchRow fallbackBatch = loadBatchById(fallbackBatchId);
        if (fallbackBatch != null) {
            batchesById.put(fallbackBatch.id(), fallbackBatch);
        }
        return fallbackBatch;
    }

    private int resolveYearValue(BatchRow batch, LocalDate weekStartDate) {
        if (weekStartDate != null) {
            int weekYear = weekStartDate.getYear();
            if (weekYear == batch.batchYear() || weekYear == batch.batchYear() + 1) {
                return weekYear;
            }
        }

        return batch.batchYear();
    }

    private int resolveMonthNumber(LocalDate weekStartDate) {
        if (weekStartDate == null) {
            return 1;
        }

        int month = weekStartDate.getMonthValue();
        return month >= 1 && month <= 12 ? month : 1;
    }

    private Integer countNullMonthPlanLinks() {
        return jdbcTemplate.queryForObject(
                "select count(*) from weekplan_table where month_plan_id is null",
                Integer.class
        );
    }

    private List<BatchRow> loadBatches() {
        return jdbcTemplate.query(
                """
                        select id, batch_year, batch_date
                        from batch_table
                        order by batch_year asc, id asc
                        """,
                (rs, rowNum) -> new BatchRow(
                        rs.getLong("id"),
                        rs.getInt("batch_year"),
                        rs.getString("batch_date")
                )
        );
    }

    private BatchRow loadBatchById(Long batchId) {
        List<BatchRow> batches = jdbcTemplate.query(
                """
                        select id, batch_year, batch_date
                        from batch_table
                        where id = ?
                        """,
                (rs, rowNum) -> new BatchRow(
                        rs.getLong("id"),
                        rs.getInt("batch_year"),
                        rs.getString("batch_date")
                ),
                batchId
        );

        return batches.stream().findFirst().orElse(null);
    }

    private Long resolveOrCreateLegacyBatch(Integer batchYear, String batchDate) {
        List<Long> batchIds = jdbcTemplate.query(
                """
                        select id
                        from batch_table
                        where batch_year = ?
                          and lower(place) = lower(?)
                        order by id
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                batchYear,
                LEGACY_BATCH_PLACE
        );

        Long batchId = batchIds.stream().findFirst().orElse(null);
        if (batchId != null) {
            return batchId;
        }

        jdbcTemplate.update(
                "insert into batch_table (batch_year, place, batch_date) values (?, ?, ?)",
                batchYear,
                LEGACY_BATCH_PLACE,
                batchDate
        );

        return jdbcTemplate.query(
                """
                        select id
                        from batch_table
                        where batch_year = ?
                          and lower(place) = lower(?)
                        order by id
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                batchYear,
                LEGACY_BATCH_PLACE
        ).stream().findFirst().orElseThrow();
    }

    private LocalDate resolveLegacyBatchDate() {
        Date legacyDate = jdbcTemplate.queryForObject(
                "select min(week_start_date) from weekplan_table where batch_id is null",
                Date.class
        );
        return legacyDate != null ? legacyDate.toLocalDate() : LocalDate.now();
    }

    private void ensureBatchDateColumnIsText() {
        if (!tableExists("batch_table") || !columnExists("batch_table", "batch_date")) {
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

    private boolean tableExists(String tableName) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                        select count(*) > 0
                        from information_schema.tables
                        where lower(table_name) = lower(?)
                        """,
                Boolean.class,
                tableName
        );
        return Boolean.TRUE.equals(exists);
    }

    private boolean columnExists(String tableName, String columnName) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                        select count(*) > 0
                        from information_schema.columns
                        where lower(table_name) = lower(?)
                          and lower(column_name) = lower(?)
                        """,
                Boolean.class,
                tableName,
                columnName
        );
        return Boolean.TRUE.equals(exists);
    }

    private Long getNullableLong(java.sql.ResultSet resultSet, String columnName) throws java.sql.SQLException {
        long value = resultSet.getLong(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private Integer getNullableInteger(java.sql.ResultSet resultSet, String columnName) throws java.sql.SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private LocalDate getNullableLocalDate(java.sql.ResultSet resultSet, String columnName) throws java.sql.SQLException {
        Date date = resultSet.getDate(columnName);
        return date != null ? date.toLocalDate() : null;
    }

    private record BatchRow(Long id, Integer batchYear, String batchDate) {
    }

    private record WeekRow(Long id, Long batchId, LocalDate weekStartDate, Integer weekNumber) {
    }
}
