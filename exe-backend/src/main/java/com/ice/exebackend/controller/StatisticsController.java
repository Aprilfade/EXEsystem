package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statistics")
// 【重要修改】更新权限注解
@PreAuthorize("hasAuthority('sys:stats:list')")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/knowledge-point-errors")
    public Result getKnowledgePointErrorStats() {
        return Result.suc(statisticsService.getKnowledgePointErrorStats());
    }

    @GetMapping("/student-ability/{studentId}")
    public Result getStudentAbilityStats(@PathVariable Long studentId) {
        return Result.suc(statisticsService.getStudentAbilityStats(studentId));
    }

    @GetMapping("/paper-difficulty/{paperId}")
    public Result getPaperDifficultyStats(@PathVariable Long paperId) {
        return Result.suc(statisticsService.getPaperDifficultyStats(paperId));
    }
}