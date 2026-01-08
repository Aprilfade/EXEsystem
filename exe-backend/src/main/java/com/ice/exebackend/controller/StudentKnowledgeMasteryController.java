package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.StudentKnowledgeMasteryDTO;
import com.ice.exebackend.service.BizStudentKnowledgeMasteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生知识点掌握度Controller
 *
 * @author ice
 * @since 2026-01-08
 */
@Tag(name = "学生知识点掌握度管理")
@RestController
@RequestMapping("/api/v1/students")
public class StudentKnowledgeMasteryController {

    @Autowired
    private BizStudentKnowledgeMasteryService masteryService;

    /**
     * 获取学生所有知识点的掌握度
     */
    @Operation(summary = "获取学生知识点掌握度列表")
    @GetMapping("/{studentId}/knowledge-mastery")
    @PreAuthorize("hasAnyAuthority('sys:student:list', 'sys:stats:list')")
    public Result<List<StudentKnowledgeMasteryDTO>> getStudentMastery(
            @Parameter(description = "学生ID") @PathVariable Long studentId
    ) {
        List<StudentKnowledgeMasteryDTO> masteries = masteryService.getStudentMastery(studentId);
        return Result.ok(masteries);
    }

    /**
     * 获取学生某科目下的知识点掌握度
     */
    @Operation(summary = "获取学生某科目的知识点掌握度")
    @GetMapping("/{studentId}/knowledge-mastery/subject/{subjectId}")
    @PreAuthorize("hasAnyAuthority('sys:student:list', 'sys:stats:list')")
    public Result<List<StudentKnowledgeMasteryDTO>> getStudentMasteryBySubject(
            @Parameter(description = "学生ID") @PathVariable Long studentId,
            @Parameter(description = "科目ID") @PathVariable Long subjectId
    ) {
        List<StudentKnowledgeMasteryDTO> masteries = masteryService.getStudentMasteryBySubject(studentId, subjectId);
        return Result.ok(masteries);
    }

    /**
     * 获取学生知识点掌握度雷达图数据
     */
    @Operation(summary = "获取学生知识点雷达图数据")
    @GetMapping("/{studentId}/mastery-radar")
    @PreAuthorize("hasAnyAuthority('sys:student:list', 'sys:stats:list')")
    public Result<Map<String, Object>> getStudentMasteryRadar(
            @Parameter(description = "学生ID") @PathVariable Long studentId,
            @Parameter(description = "科目ID（可选）", required = false) @RequestParam(required = false) Long subjectId
    ) {
        Map<String, Object> radarData = masteryService.getStudentMasteryRadar(studentId, subjectId);
        return Result.ok(radarData);
    }
}
