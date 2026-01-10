package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizAiTutorNote;
import com.ice.exebackend.entity.BizAiTutorStudyRecord;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizAiTutorNoteService;
import com.ice.exebackend.service.BizAiTutorStudyRecordService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI家教控制器
 * 提供学习记录、笔记管理等功能
 */
@RestController
@RequestMapping("/api/v1/student/ai-tutor")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentAiTutorController {

    @Autowired
    private BizAiTutorStudyRecordService studyRecordService;

    @Autowired
    private BizAiTutorNoteService noteService;

    @Autowired
    private BizStudentService studentService;

    /**
     * 获取当前登录学生ID
     */
    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent student = studentService.lambdaQuery()
                .eq(BizStudent::getStudentNo, studentNo)
                .one();
        return student != null ? student.getId() : null;
    }

    // ==================== 学习记录相关 ====================

    /**
     * 保存学习记录
     */
    @PostMapping("/study-record")
    public Result saveStudyRecord(@RequestBody BizAiTutorStudyRecord record, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        if (studentId == null) {
            return Result.fail("学生信息不存在");
        }

        record.setStudentId(studentId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        // 检查是否已存在该章节的学习记录
        BizAiTutorStudyRecord existing = studyRecordService.lambdaQuery()
                .eq(BizAiTutorStudyRecord::getStudentId, studentId)
                .eq(BizAiTutorStudyRecord::getSubject, record.getSubject())
                .eq(BizAiTutorStudyRecord::getChapterId, record.getChapterId())
                .one();

        if (existing != null) {
            // 更新现有记录
            existing.setStudyTime(existing.getStudyTime() + record.getStudyTime());
            existing.setExerciseCount(existing.getExerciseCount() + record.getExerciseCount());
            existing.setCorrectCount(existing.getCorrectCount() + record.getCorrectCount());
            existing.setProgress(record.getProgress());
            existing.setCompleted(record.getCompleted());
            existing.setUpdateTime(LocalDateTime.now());
            studyRecordService.updateById(existing);
            return Result.suc(existing);
        } else {
            // 新建记录
            studyRecordService.save(record);
            return Result.suc(record);
        }
    }

    /**
     * 获取学习统计数据
     */
    @GetMapping("/stats")
    public Result getStudyStats(@RequestParam(required = false) String subject, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        if (studentId == null) {
            return Result.fail("学生信息不存在");
        }

        if (subject == null || subject.isEmpty()) {
            subject = "math"; // 默认数学
        }

        Map<String, Object> stats = studyRecordService.getStudyStats(studentId, subject);
        return Result.suc(stats);
    }

    /**
     * 获取学习记录列表
     */
    @GetMapping("/study-records")
    public Result getStudyRecords(
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Authentication auth
    ) {
        Long studentId = getCurrentStudentId(auth);
        if (studentId == null) {
            return Result.fail("学生信息不存在");
        }

        QueryWrapper<BizAiTutorStudyRecord> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId);
        if (subject != null && !subject.isEmpty()) {
            qw.eq("subject", subject);
        }
        qw.orderByDesc("update_time");

        Page<BizAiTutorStudyRecord> p = studyRecordService.page(new Page<>(page, pageSize), qw);
        return Result.suc(p);
    }

    // ==================== 笔记相关 ====================

    /**
     * 保存笔记
     */
    @PostMapping("/note")
    public Result saveNote(@RequestBody BizAiTutorNote note, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        if (studentId == null) {
            return Result.fail("学生信息不存在");
        }

        note.setStudentId(studentId);
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());

        noteService.save(note);
        return Result.suc(note);
    }

    /**
     * 获取笔记列表
     */
    @GetMapping("/notes")
    public Result getNotes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            Authentication auth
    ) {
        Long studentId = getCurrentStudentId(auth);
        if (studentId == null) {
            return Result.fail("学生信息不存在");
        }

        QueryWrapper<BizAiTutorNote> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId);

        if (keyword != null && !keyword.isEmpty()) {
            qw.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("content", keyword)
            );
        }

        if (tag != null && !tag.isEmpty()) {
            qw.eq("tag", tag);
        }

        qw.orderByDesc("create_time");

        Page<BizAiTutorNote> p = noteService.page(new Page<>(page, pageSize), qw);
        return Result.suc(p);
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/note/{id}")
    public Result deleteNote(@PathVariable Long id, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        if (studentId == null) {
            return Result.fail("学生信息不存在");
        }

        // 验证笔记是否属于当前学生
        BizAiTutorNote note = noteService.getById(id);
        if (note == null) {
            return Result.fail("笔记不存在");
        }

        if (!note.getStudentId().equals(studentId)) {
            return Result.fail("无权限删除此笔记");
        }

        noteService.removeById(id);
        return Result.suc("删除成功");
    }

    /**
     * 更新笔记
     */
    @PutMapping("/note/{id}")
    public Result updateNote(@PathVariable Long id, @RequestBody BizAiTutorNote note, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        if (studentId == null) {
            return Result.fail("学生信息不存在");
        }

        // 验证笔记是否属于当前学生
        BizAiTutorNote existing = noteService.getById(id);
        if (existing == null) {
            return Result.fail("笔记不存在");
        }

        if (!existing.getStudentId().equals(studentId)) {
            return Result.fail("无权限修改此笔记");
        }

        existing.setTitle(note.getTitle());
        existing.setContent(note.getContent());
        existing.setTag(note.getTag());
        existing.setUpdateTime(LocalDateTime.now());

        noteService.updateById(existing);
        return Result.suc(existing);
    }
}
