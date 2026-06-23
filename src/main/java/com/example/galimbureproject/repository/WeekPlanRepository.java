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
            join fetch weekPlan.batch
            order by weekPlan.batch.batchYear asc, weekPlan.batch.place asc, weekPlan.weekNumber asc
            """)
    List<WeekPlan> findAllOrderedWithBatch();

    @Query("""
            select weekPlan
            from WeekPlan weekPlan
            join fetch weekPlan.batch
            where weekPlan.batch.id = :batchId
            order by weekPlan.weekNumber asc
            """)
    List<WeekPlan> findAllByBatch_IdOrderByWeekNumberAsc(@Param("batchId") Long batchId);

    @Query("""
            select weekPlan
            from WeekPlan weekPlan
            join fetch weekPlan.batch
            where weekPlan.id = :id
            """)
    Optional<WeekPlan> findByIdWithBatch(@Param("id") Long id);

    boolean existsByBatch_IdAndWeekNumber(Long batchId, Integer weekNumber);
}
