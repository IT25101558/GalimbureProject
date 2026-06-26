package com.example.galimbureproject.service;

import com.example.galimbureproject.model.StudentMonthPayment;
import com.example.galimbureproject.repository.MonthPlanRepository;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.repository.StudentMonthPaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentMonthPaymentServiceTest {

    @Mock
    private StudentMonthPaymentRepository studentMonthPaymentRepository;

    @Mock
    private RegisteredUserRepository registeredUserRepository;

    @Mock
    private MonthPlanRepository monthPlanRepository;

    @InjectMocks
    private StudentMonthPaymentService studentMonthPaymentService;

    @Test
    void getLatestPaymentForStudentReturnsFirstOrderedRecord() {
        StudentMonthPayment latestPayment = new StudentMonthPayment();
        latestPayment.setPaidStatus(true);
        StudentMonthPayment olderPayment = new StudentMonthPayment();
        olderPayment.setPaidStatus(false);

        when(studentMonthPaymentRepository.findAllByStudent_IdOrderByHierarchyDesc(7L))
                .thenReturn(List.of(latestPayment, olderPayment));

        Optional<StudentMonthPayment> result = studentMonthPaymentService.getLatestPaymentForStudent(7L);

        assertTrue(result.isPresent());
        assertSame(latestPayment, result.get());
        assertTrue(studentMonthPaymentService.isStudentPaidForLatestMonth(7L));
    }

    @Test
    void getLatestPaymentForStudentHandlesMissingStudentId() {
        Optional<StudentMonthPayment> result = studentMonthPaymentService.getLatestPaymentForStudent(null);

        assertFalse(result.isPresent());
        assertFalse(studentMonthPaymentService.isStudentPaidForLatestMonth(null));
    }

    @Test
    void getPaymentsForStudentDelegatesToRepository() {
        StudentMonthPayment payment = new StudentMonthPayment();
        when(studentMonthPaymentRepository.findAllByStudent_IdOrderByHierarchyDesc(7L))
                .thenReturn(List.of(payment));

        assertSame(payment, studentMonthPaymentService.getPaymentsForStudent(7L).get(0));
    }
}
