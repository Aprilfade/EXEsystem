package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizAchievementService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // 【修复】添加此行导入
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student/achievements")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentAchievementController {

    @Autowired
    private BizAchievementService achievementService;
    @Autowired
    private BizStudentService studentService;

    @GetMapping
    public Result getMyAchievements(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return Result.suc(achievementService.getStudentAchievements(s.getId()));
    }
}