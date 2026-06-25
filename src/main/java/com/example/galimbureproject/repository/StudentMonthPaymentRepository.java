package com.example.galimbureproject.repository;

import com.example.galimbureproject.model.StudentMonthPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentMonthPaymentRepository extends JpaRepository<StudentMonthPayment, Long> {

    @Query("""
            select payment
            from StudentMonthPayment payment
            join fetch payment.student student
            join fetch payment.monthPlan monthPlan
            join fetch monthPlan.yearPlan yearPlan
            join fetch yearPlan.batch
            where monthPlan.id = :monthPlanId
            order by student.fullName asc
            """)
    List<StudentMonthPayment> findAllByMonthPlan_IdOrderByStudent_FullNameAsc(@Param("monthPlanId") Long monthPlanId);

    Optional<StudentMonthPayment> findByStudent_IdAndMonthPlan_Id(Long studentId, Long monthPlanId);
}
