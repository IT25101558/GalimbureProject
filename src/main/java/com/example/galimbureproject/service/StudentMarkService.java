package com.example.galimbureproject.service;

import com.example.galimbureproject.dto.StudentMarkEntryForm;
import com.example.galimbureproject.dto.StudentMarkForm;
import com.example.galimbureproject.model.Batch;
import com.example.galimbureproject.model.WeekPlan;
import com.example.galimbureproject.model.RegisteredUser;
import com.example.galimbureproject.model.StudentMark;
import com.example.galimbureproject.model.UserRole;
import com.example.galimbureproject.repository.RegisteredUserRepository;
import com.example.galimbureproject.repository.StudentMarkRepository;
import com.example.galimbureproject.repository.WeekPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentMarkService {

    private final StudentMarkRepository studentMarkRepository;
    private final RegisteredUserRepository registeredUserRepository;
    private final WeekPlanRepository weekPlanRepository;

    public StudentMarkService(
            StudentMarkRepository studentMarkRepository,
            RegisteredUserRepository registeredUserRepository,
            WeekPlanRepository weekPlanRepository
    ) {
        this.studentMarkRepository = studentMarkRepository;
        this.registeredUserRepository = registeredUserRepository;
        this.weekPlanRepository = weekPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentMark> getMarksForStudent(Long studentId) {
        return studentMarkRepository.findForStudentOrdered(studentId);
    }

    @Transactional(readOnly = true)
    public List<StudentMark> getMarksForWeekPlan(Long weekPlanId) {
        return studentMarkRepository.findByWeekPlanIdOrderedWithStudent(weekPlanId);
    }

    @Transactional(readOnly = true)
    public List<StudentMark> getAllMarks() {
        return studentMarkRepository.findAllOrderedWithStudent();
    }

    @Transactional
    public StudentMark saveOrUpdate(StudentMarkForm form) {
        return saveOrUpdate(form.getStudentId(), form.getWeekPlanId(), form.getMark());
    }

    @Transactional
    public void saveWeekMarks(Long weekPlanId, List<StudentMarkEntryForm> entries) {
        if (weekPlanId == null) {
            throw new IllegalArgumentException("Select a week plan.");
        }

        WeekPlan weekPlan = weekPlanRepository.findByIdWithBatch(weekPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Selected week plan was not found."));

        if (entries == null) {
            return;
        }

        for (StudentMarkEntryForm entry : entries) {
            if (entry == null || entry.getStudentId() == null || entry.getMark() == null) {
                continue;
            }

            saveOrUpdate(entry.getStudentId(), weekPlan, entry.getMark());
        }
    }

    private StudentMark saveOrUpdate(Long studentId, Long weekPlanId, Integer markValue) {
        if (weekPlanId == null) {
            throw new IllegalArgumentException("Select a week plan.");
        }

        WeekPlan weekPlan = weekPlanRepository.findByIdWithBatch(weekPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Selected week plan was not found."));
        return saveOrUpdate(studentId, weekPlan, markValue);
    }

    private StudentMark saveOrUpdate(Long studentId, WeekPlan weekPlan, Integer markValue) {
        if (weekPlan == null || weekPlan.getId() == null) {
            throw new IllegalArgumentException("Select a week plan.");
        }

        if (markValue == null || markValue < 0 || markValue > 100) {
            throw new IllegalArgumentException("Mark must be between 0 and 100.");
        }

        RegisteredUser student = registeredUserRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Selected student was not found."));

        if (student.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Marks can only be assigned to students.");
        }

        Batch studentBatch = student.getBatch();
        Batch weekPlanBatch = weekPlan.getBatch();
        if (studentBatch != null && weekPlanBatch != null) {
            Long studentBatchId = studentBatch.getId();
            Long weekPlanBatchId = weekPlanBatch.getId();
            if (studentBatchId == null || weekPlanBatchId == null || !studentBatchId.equals(weekPlanBatchId)) {
                throw new IllegalArgumentException("Selected student does not belong to the selected batch.");
            }
        } else {
            Integer studentBatchYear = student.getBatchYear();
            Integer weekPlanBatchYear = weekPlanBatch != null ? weekPlanBatch.getBatchYear() : null;
            if (studentBatchYear == null || weekPlanBatchYear == null || !studentBatchYear.equals(weekPlanBatchYear)) {
                throw new IllegalArgumentException("Selected student does not belong to the selected batch.");
            }
        }

        StudentMark mark = studentMarkRepository.findByStudent_IdAndWeekPlan_Id(student.getId(), weekPlan.getId())
                .orElseGet(StudentMark::new);
        mark.setStudent(student);
        mark.setWeekPlan(weekPlan);
        mark.setMark(markValue);
        return studentMarkRepository.save(mark);
    }
}
