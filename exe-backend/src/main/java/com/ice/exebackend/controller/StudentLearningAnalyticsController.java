package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.LearningAnalyticsDTO;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.LearningAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 学习分析控制器
 *
 * @author Claude Code
 * @since v3.08
 */
@RestController
@RequestMapping("/api/v1/student/learning-analytics")
public class StudentLearningAnalyticsController {

    @Autowired
    private LearningAnalyticsService learningAnalyticsService;

    @Autowired
    private BizStudentService studentService;

    /**
     * 获取学生的学习分析数据
     *
     * @param days 分析天数（7或30，默认7）
     * @param authentication 认证信息
     * @return 学习分析数据
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getLearningAnalytics(
            @RequestParam(defaultValue = "7") Integer days,
            Authentication authentication
    ) {
        try {
            // 从认证信息中获取学生学号
            String studentNo = authentication.getName();

            // 通过学号查询学生信息
            BizStudent student = studentService.lambdaQuery()
                    .eq(BizStudent::getStudentNo, studentNo)
                    .one();

            if (student == null) {
                return Result.fail("当前用户不是一个有效的学生");
            }

            LearningAnalyticsDTO analytics = learningAnalyticsService.getStudentLearningAnalytics(student.getId(), days);

            return Result.suc(analytics);
        } catch (Exception e) {
            return Result.fail("获取学习分析失败: " + e.getMessage());
        }
    }
}
