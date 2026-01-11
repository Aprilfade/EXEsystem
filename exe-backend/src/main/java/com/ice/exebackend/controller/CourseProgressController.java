package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.ChapterTreeDTO;
import com.ice.exebackend.dto.ProgressUpdateDTO;
import com.ice.exebackend.dto.SessionStartDTO;
import com.ice.exebackend.entity.BizCourseProgress;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.CourseChapterService;
import com.ice.exebackend.service.CourseProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程学习进度控制器
 * 学生端专用API
 */
@RestController
@RequestMapping("/api/v1/student/course-progress")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class CourseProgressController {

    private static final Logger logger = LoggerFactory.getLogger(CourseProgressController.class);

    @Autowired
    private CourseProgressService progressService;

    @Autowired
    private CourseChapterService chapterService;

    @Autowired
    private BizStudentService bizStudentService;

    /**
     * 获取当前登录学生ID
     */
    private Long getCurrentStudentId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BizStudent student = bizStudentService.lambdaQuery()
            .eq(BizStudent::getStudentNo, username)
            .one();
        return student != null ? student.getId() : null;
    }

    /**
     * 更新学习进度
     */
    @PostMapping("/update")
    public Result updateProgress(@RequestBody ProgressUpdateDTO dto) {
        Long studentId = getCurrentStudentId();
        if (studentId == null) {
            return Result.fail("未获取到学生信息");
        }

        logger.info("学生更新学习进度: studentId={}, resourceId={}, percent={}",
            studentId, dto.getResourceId(), dto.getProgressPercent());

        boolean success = progressService.updateProgress(studentId, dto);
        return success ? Result.suc("进度更新成功") : Result.fail("进度更新失败");
    }

    /**
     * 获取课程学习进度列表
     */
    @GetMapping("/course/{courseId}")
    public Result getCourseProgress(@PathVariable Long courseId) {
        Long studentId = getCurrentStudentId();
        if (studentId == null) {
            return Result.fail("未获取到学生信息");
        }

        logger.info("获取课程学习进度: studentId={}, courseId={}", studentId, courseId);

        List<BizCourseProgress> progressList = progressService.getCourseProgress(studentId, courseId);
        return Result.suc(progressList);
    }

    /**
     * 获取资源学习进度
     */
    @GetMapping("/resource/{resourceId}")
    public Result getResourceProgress(@PathVariable Long resourceId) {
        Long studentId = getCurrentStudentId();
        if (studentId == null) {
            return Result.fail("未获取到学生信息");
        }

        BizCourseProgress progress = progressService.getResourceProgress(studentId, resourceId);
        return Result.suc(progress);
    }

    /**
     * 获取课程完成率
     */
    @GetMapping("/course/{courseId}/completion")
    public Result getCourseCompletion(@PathVariable Long courseId) {
        Long studentId = getCurrentStudentId();
        if (studentId == null) {
            return Result.fail("未获取到学生信息");
        }

        logger.info("获取课程完成率: studentId={}, courseId={}", studentId, courseId);

        double completion = progressService.calculateCourseCompletion(studentId, courseId);
        return Result.suc(completion);
    }

    /**
     * 获取课程章节树（含进度）
     */
    @GetMapping("/course/{courseId}/chapter-tree")
    public Result getChapterTree(@PathVariable Long courseId) {
        logger.info("获取课程章节树: courseId={}", courseId);

        List<ChapterTreeDTO> tree = chapterService.getCourseChapterTree(courseId);
        return Result.suc(tree);
    }

    /**
     * 开始学习会话
     */
    @PostMapping("/session/start")
    public Result startSession(@RequestBody SessionStartDTO dto) {
        Long studentId = getCurrentStudentId();
        if (studentId == null) {
            return Result.fail("未获取到学生信息");
        }

        logger.info("开始学习会话: studentId={}, courseId={}, resourceId={}",
            studentId, dto.getCourseId(), dto.getResourceId());

        Long sessionId = progressService.startSession(studentId, dto.getCourseId(), dto.getResourceId());
        return Result.suc(sessionId);
    }

    /**
     * 结束学习会话
     */
    @PostMapping("/session/{sessionId}/end")
    public Result endSession(@PathVariable Long sessionId) {
        logger.info("结束学习会话: sessionId={}", sessionId);

        boolean success = progressService.endSession(sessionId);
        return success ? Result.suc("会话结束成功") : Result.fail("会话结束失败");
    }
}
