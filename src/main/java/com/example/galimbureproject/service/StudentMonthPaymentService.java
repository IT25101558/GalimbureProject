package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.StudentMonthPaymentEntryForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.MonthPlan;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.StudentMonthPayment;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.MonthPlanRepository;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.repository.StudentMonthPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentMonthPaymentService {

    private final StudentMonthPaymentRepository studentMonthPaymentRepository;
    private final RegisteredUserRepository registeredUserRepository;
    private final MonthPlanRepository monthPlanRepository;

    public StudentMonthPaymentService(
            StudentMonthPaymentRepository studentMonthPaymentRepository,
            RegisteredUserRepository registeredUserRepository,
            MonthPlanRepository monthPlanRepository
    ) {
        this.studentMonthPaymentRepository = studentMonthPaymentRepository;
        this.registeredUserRepository = registeredUserRepository;
        this.monthPlanRepository = monthPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentMonthPayment> getPaymentsForMonth(Long monthPlanId) {
        return studentMonthPaymentRepository.findAllByMonthPlan_IdOrderByStudent_FullNameAsc(monthPlanId);
    }

    @Transactional
    public void saveMonthPayments(Long monthPlanId, List<StudentMonthPaymentEntryForm> entries) {
        if (monthPlanId == null) {
            throw new IllegalArgumentException("Select a month.");
        }

        MonthPlan monthPlan = monthPlanRepository.findByIdWithHierarchy(monthPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Selected month was not found."));

        if (entries == null) {
            return;
        }

        for (StudentMonthPaymentEntryForm entry : entries) {
            if (entry == null || entry.getStudentId() == null) {
                continue;
            }

            saveOrUpdate(entry.getStudentId(), monthPlan, entry.isPaidStatus());
        }
    }

    private StudentMonthPayment saveOrUpdate(Long studentId, MonthPlan monthPlan, boolean paidStatus) {
        RegisteredUser student = registeredUserRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Selected student was not found."));

        if (student.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Payment status can only be assigned to students.");
        }

        Batch studentBatch = student.getBatch();
        Batch monthBatch = monthPlan.getBatch();
        if (studentBatch != null && monthBatch != null) {
            Long studentBatchId = studentBatch.getId();
            Long monthBatchId = monthBatch.getId();
            if (studentBatchId == null || monthBatchId == null || !studentBatchId.equals(monthBatchId)) {
                throw new IllegalArgumentException("Selected student does not belong to the selected batch.");
            }
        } else {
            Integer studentBatchYear = student.getBatchYear();
            Integer monthBatchYear = monthBatch != null ? monthBatch.getBatchYear() : null;
            if (studentBatchYear == null || monthBatchYear == null || !studentBatchYear.equals(monthBatchYear)) {
                throw new IllegalArgumentException("Selected student does not belong to the selected batch.");
            }
        }

        StudentMonthPayment payment = studentMonthPaymentRepository
                .findByStudent_IdAndMonthPlan_Id(student.getId(), monthPlan.getId())
                .orElseGet(StudentMonthPayment::new);
        payment.setStudent(student);
        payment.setMonthPlan(monthPlan);
        payment.setPaidStatus(paidStatus);
        return studentMonthPaymentRepository.save(payment);
    }
}
