package com.example.galimbureproject.repository;

import com.example.galimbureproject.model.MonthPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthPlanRepository extends JpaRepository<MonthPlan, Long> {

    @Query("""
            select monthPlan
            from MonthPlan monthPlan
            join fetch monthPlan.yearPlan yearPlan
            join fetch yearPlan.batch
            order by yearPlan.batch.batchYear asc, yearPlan.yearValue asc, monthPlan.monthNumber asc
            """)
    List<MonthPlan> findAllOrderedWithHierarchy();

    @Query("""
            select monthPlan
            from MonthPlan monthPlan
            join fetch monthPlan.yearPlan yearPlan
            join fetch yearPlan.batch
            where yearPlan.id = :yearPlanId
            order by monthPlan.monthNumber asc
            """)
    List<MonthPlan> findAllByYearPlan_IdOrderByMonthNumberAsc(@Param("yearPlanId") Long yearPlanId);

    @Query("""
            select monthPlan
            from MonthPlan monthPlan
            join fetch monthPlan.yearPlan yearPlan
            join fetch yearPlan.batch
            where yearPlan.batch.id = :batchId
            order by yearPlan.yearValue asc, monthPlan.monthNumber asc
            """)
    List<MonthPlan> findAllByBatch_IdOrderByYearPlan_YearValueAscMonthNumberAsc(@Param("batchId") Long batchId);

    @Query("""
            select monthPlan
            from MonthPlan monthPlan
            join fetch monthPlan.yearPlan yearPlan
            join fetch yearPlan.batch
            where monthPlan.id = :id
            """)
    Optional<MonthPlan> findByIdWithHierarchy(@Param("id") Long id);

    boolean existsByYearPlan_IdAndMonthNumber(Long yearPlanId, Integer monthNumber);
}
