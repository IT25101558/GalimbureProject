package com.example.galimbureproject.repository;

import com.example.galimbureproject.model.StudentMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentMarkRepository extends JpaRepository<StudentMark, Long> {

    @Query("""
            select mark
            from StudentMark mark
            join fetch mark.student
            join fetch mark.weekPlan weekPlan
            join fetch weekPlan.batch
            where mark.student.id = :studentId
            order by weekPlan.batch.batchYear asc, weekPlan.batch.place asc, weekPlan.weekNumber asc
            """)
    List<StudentMark> findForStudentOrdered(@Param("studentId") Long studentId);

    @Query("""
            select mark
            from StudentMark mark
            join fetch mark.student
            join fetch mark.weekPlan weekPlan
            join fetch weekPlan.batch
            where weekPlan.id = :weekPlanId
            order by mark.student.fullName asc
            """)
    List<StudentMark> findByWeekPlanIdOrderedWithStudent(@Param("weekPlanId") Long weekPlanId);

    @Query("""
            select mark
            from StudentMark mark
            join fetch mark.student
            join fetch mark.weekPlan weekPlan
            join fetch weekPlan.batch
            order by mark.student.fullName asc, weekPlan.batch.batchYear asc, weekPlan.batch.place asc, weekPlan.weekNumber asc
            """)
    List<StudentMark> findAllOrderedWithStudent();

    Optional<StudentMark> findByStudent_IdAndWeekPlan_Id(Long studentId, Long weekPlanId);
}
