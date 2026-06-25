package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.StudentMarkEntryForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.MonthPlan;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.model.WeekPlan;
import com.example.galimbureproject.model.YearPlan;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.repository.StudentMarkRepository;
import com.example.galimbureproject.repository.WeekPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentMarkServiceTest {

    @Mock
    private StudentMarkRepository studentMarkRepository;

    @Mock
    private RegisteredUserRepository registeredUserRepository;

    @Mock
    private WeekPlanRepository weekPlanRepository;

    @InjectMocks
    private StudentMarkService studentMarkService;

    @Test
    void saveWeekMarksRejectsStudentsOutsideSelectedBatch() {
        WeekPlan weekPlan = new WeekPlan();
        ReflectionTestUtils.setField(weekPlan, "id", 1L);
        YearPlan yearPlan = new YearPlan();
        ReflectionTestUtils.setField(yearPlan, "id", 21L);
        Batch weekPlanBatch = new Batch();
        ReflectionTestUtils.setField(weekPlanBatch, "id", 11L);
        weekPlanBatch.setBatchYear(2026);
        weekPlanBatch.setPlace("Gampaha");
        yearPlan.setBatch(weekPlanBatch);
        yearPlan.setYearValue(2026);
        MonthPlan monthPlan = new MonthPlan();
        ReflectionTestUtils.setField(monthPlan, "id", 31L);
        monthPlan.setYearPlan(yearPlan);
        monthPlan.setMonthNumber(1);
        weekPlan.setMonthPlan(monthPlan);

        RegisteredUser student = new RegisteredUser();
        ReflectionTestUtils.setField(student, "id", 7L);
        student.setRole(UserRole.STUDENT);
        student.setBatchYear(2026);
        Batch studentBatch = new Batch();
        ReflectionTestUtils.setField(studentBatch, "id", 12L);
        studentBatch.setBatchYear(2026);
        studentBatch.setPlace("Mirigama");
        student.setBatch(studentBatch);

        StudentMarkEntryForm entry = new StudentMarkEntryForm();
        entry.setStudentId(7L);
        entry.setMark(82);

        when(weekPlanRepository.findByIdWithHierarchy(1L)).thenReturn(Optional.of(weekPlan));
        when(registeredUserRepository.findById(7L)).thenReturn(Optional.of(student));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> studentMarkService.saveWeekMarks(1L, List.of(entry))
        );

        assertEquals("Selected student does not belong to the selected batch.", exception.getMessage());
        verify(studentMarkRepository, never()).save(any());
    }
}
