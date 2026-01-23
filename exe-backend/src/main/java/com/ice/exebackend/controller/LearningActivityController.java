package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizLearningActivityService;
import com.ice.exebackend.service.BizStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习活动统计Controller
 * 基于真实数据库查询的学习时长统计功能
 */
@Tag(name = "学习活动统计")
@RestController
@RequestMapping("/api/v1/student/learning-activities")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class LearningActivityController {

    @Autowired
    private BizLearningActivityService learningActivityService;

    @Autowired
    private BizStudentService studentService;

    /**
     * 获取当前学生ID（支持管理员通过参数指定）
     */
    private Long getStudentId(Authentication authentication, Long studentIdParam) {
        // 如果提供了studentId参数（管理员查看指定学生数据），直接使用
        if (studentIdParam != null) {
            return studentIdParam;
        }

        // 否则获取当前学生ID
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();

        // 如果是管理员，返回第一个学生ID用于测试
        if ("superadmin".equals(username) || "admin".equals(username)) {
            BizStudent firstStudent = studentService.lambdaQuery().last("LIMIT 1").one();
            return firstStudent != null ? firstStudent.getId() : 1L;
        }

        // 学生登录，通过学号查询
        BizStudent student = studentService.lambdaQuery()
                .eq(BizStudent::getStudentNo, username)
                .one();
        return student != null ? student.getId() : null;
    }

    @Operation(summary = "获取学习时长统计")
    @GetMapping("/stats")
    public Result<StudyTimeStatsDTO> getStats(
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {
        Long actualStudentId = getStudentId(authentication, studentId);
        if (actualStudentId == null) {
            return Result.fail("学生信息不存在");
        }
        StudyTimeStatsDTO stats = learningActivityService.getStudyTimeStats(actualStudentId);
        return Result.suc(stats);
    }

    @Operation(summary = "获取每日学习时长")
    @GetMapping("/daily")
    public Result<List<DailyStudyTimeDTO>> getDailyStudyTime(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {
        Long actualStudentId = getStudentId(authentication, studentId);
        if (actualStudentId == null) {
            return Result.fail("学生信息不存在");
        }
        List<DailyStudyTimeDTO> list = learningActivityService.getDailyStudyTime(actualStudentId, days);
        return Result.suc(list);
    }

    @Operation(summary = "按活动类型统计")
    @GetMapping("/by-type")
    public Result<List<ActivityTypeStatsDTO>> getActivityTypeStats(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {
        Long actualStudentId = getStudentId(authentication, studentId);
        if (actualStudentId == null) {
            return Result.fail("学生信息不存在");
        }
        List<ActivityTypeStatsDTO> list = learningActivityService.getActivityTypeStats(actualStudentId, days);
        return Result.suc(list);
    }

    @Operation(summary = "按科目统计")
    @GetMapping("/by-subject")
    public Result<List<SubjectStudyTimeDTO>> getSubjectStudyTime(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {
        Long actualStudentId = getStudentId(authentication, studentId);
        if (actualStudentId == null) {
            return Result.fail("学生信息不存在");
        }
        List<SubjectStudyTimeDTO> list = learningActivityService.getSubjectStudyTime(actualStudentId, days);
        return Result.suc(list);
    }

    @Operation(summary = "获取学习排名")
    @GetMapping("/ranking")
    public Result<StudyRankingDTO> getStudyRanking(
            @RequestParam(required = false) Long studentId,
            Authentication authentication) {
        Long actualStudentId = getStudentId(authentication, studentId);
        if (actualStudentId == null) {
            return Result.fail("学生信息不存在");
        }
        StudyRankingDTO ranking = learningActivityService.getStudyRanking(actualStudentId);
        return Result.suc(ranking);
    }

    @Operation(summary = "记录学习活动")
    @PostMapping
    public Result<Void> recordActivity(
            @RequestBody RecordActivityRequest request,
            Authentication authentication) {
        Long actualStudentId = getStudentId(authentication, request.getStudentId());
        if (actualStudentId == null) {
            return Result.fail("学生信息不存在");
        }

        // 前端传分钟，转为秒
        Integer durationSeconds = request.getDuration() != null ? request.getDuration() * 60 : 0;

        learningActivityService.recordActivity(
            actualStudentId,
            request.getActivityType(),
            durationSeconds,
            request.getSubjectId(),
            request.getRelatedId(),
            request.getDescription()
        );

        return Result.suc();
    }

    @Operation(summary = "开始学习会话")
    @PostMapping("/session/start")
    public Result<String> startSession(
            @RequestBody SessionStartRequest request,
            Authentication authentication) {
        Long actualStudentId = getStudentId(authentication, request.getStudentId());
        if (actualStudentId == null) {
            return Result.fail("学生信息不存在");
        }

        String sessionId = learningActivityService.startSession(
            actualStudentId,
            request.getActivityType(),
            request.getSubjectId()
        );

        return Result.suc(sessionId);
    }

    @Operation(summary = "更新会话时长（心跳）")
    @PutMapping("/session/{sessionId}/heartbeat")
    public Result<Void> updateSessionHeartbeat(
            @PathVariable String sessionId,
            @RequestBody HeartbeatRequest request) {
        learningActivityService.updateSessionDuration(sessionId, request.getDurationSeconds());
        return Result.suc();
    }

    @Operation(summary = "结束学习会话")
    @PostMapping("/session/{sessionId}/end")
    public Result<Void> endSession(@PathVariable String sessionId) {
        learningActivityService.endSession(sessionId);
        return Result.suc();
    }

    /**
     * 记录学习活动请求DTO
     */
    @Data
    public static class RecordActivityRequest {
        private Long studentId;        // 可选，管理员查看指定学生数据时使用
        private String activityType;
        private Integer duration;      // 分钟
        private Long subjectId;
        private Long relatedId;
        private String description;
    }

    /**
     * 开始会话请求DTO
     */
    @Data
    public static class SessionStartRequest {
        private Long studentId;        // 可选，管理员查看指定学生数据时使用
        private String activityType;
        private Long subjectId;
    }

    /**
     * 心跳请求DTO
     */
    @Data
    public static class HeartbeatRequest {
        private Integer durationSeconds;
    }
}
