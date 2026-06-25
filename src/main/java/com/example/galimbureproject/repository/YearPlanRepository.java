package com.example.galimbureproject.repository;

import com.example.galimbureproject.model.YearPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface YearPlanRepository extends JpaRepository<YearPlan, Long> {

    @Query("""
            select yearPlan
            from YearPlan yearPlan
            join fetch yearPlan.batch
            order by yearPlan.batch.batchYear asc, yearPlan.yearValue asc
            """)
    List<YearPlan> findAllOrderedWithBatch();

    @Query("""
            select yearPlan
            from YearPlan yearPlan
            join fetch yearPlan.batch
            where yearPlan.batch.id = :batchId
            order by yearPlan.yearValue asc
            """)
    List<YearPlan> findAllByBatch_IdOrderByYearValueAsc(@Param("batchId") Long batchId);

    @Query("""
            select yearPlan
            from YearPlan yearPlan
            join fetch yearPlan.batch
            where yearPlan.id = :id
            """)
    Optional<YearPlan> findByIdWithBatch(@Param("id") Long id);

    boolean existsByBatch_IdAndYearValue(Long batchId, Integer yearValue);
}
