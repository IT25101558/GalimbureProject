package com.example.galimbureproject.repository;

import com.example.galimbureproject.model.WeekPlan;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeekPlanRepository extends JpaRepository<WeekPlan, Long> {

    @Query("""
            select weekPlan
            from WeekPlan weekPlan
            join fetch weekPlan.monthPlan monthPlan
            join fetch monthPlan.yearPlan yearPlan
            join fetch yearPlan.batch
            order by yearPlan.batch.batchYear asc, yearPlan.yearValue asc, monthPlan.monthNumber asc, weekPlan.weekNumber asc
            """)
    List<WeekPlan> findAllOrderedWithHierarchy();

    @Query("""
            select weekPlan
            from WeekPlan weekPlan
            join fetch weekPlan.monthPlan monthPlan
            join fetch monthPlan.yearPlan yearPlan
            join fetch yearPlan.batch
            where monthPlan.id = :monthPlanId
            order by weekPlan.weekNumber asc
            """)
    List<WeekPlan> findAllByMonthPlan_IdOrderByWeekNumberAsc(@Param("monthPlanId") Long monthPlanId);

    @Query("""
            select weekPlan
            from WeekPlan weekPlan
            join fetch weekPlan.monthPlan monthPlan
            join fetch monthPlan.yearPlan yearPlan
            join fetch yearPlan.batch
            where weekPlan.id = :id
            """)
    Optional<WeekPlan> findByIdWithHierarchy(@Param("id") Long id);

    boolean existsByMonthPlan_IdAndWeekNumber(Long monthPlanId, Integer weekNumber);
}
