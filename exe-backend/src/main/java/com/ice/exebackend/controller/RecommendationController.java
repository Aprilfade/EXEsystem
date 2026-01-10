package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI智能推荐控制器
 *
 * @author AI功能组
 * @version v3.03
 */
@RestController
@RequestMapping("/api/v1/student/recommendation")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private BizStudentService bizStudentService;

    /**
     * 获取当前登录学生ID
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 【修复】学生端应该从BizStudent表中查询，使用studentNo字段
        BizStudent student = bizStudentService.lambdaQuery()
                .eq(BizStudent::getStudentNo, username)
                .one();
        return student != null ? student.getId() : null;
    }

    /**
     * 获取题目推荐
     *
     * @param subjectId 科目ID（可选）
     * @param limit 推荐数量
     * @return 推荐题目列表
     */
    @GetMapping("/questions")
    public Result getQuestionRecommendations(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.fail("无法获取当前用户信息");
        }

        List<RecommendationService.QuestionRecommendation> recommendations =
                recommendationService.recommendQuestions(userId, subjectId, limit);

        return Result.suc(recommendations);
    }

    /**
     * 获取课程推荐
     *
     * @param limit 推荐数量
     * @return 推荐课程列表
     */
    @GetMapping("/courses")
    public Result getCourseRecommendations(
            @RequestParam(defaultValue = "5") int limit
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.fail("无法获取当前用户信息");
        }

        List<RecommendationService.CourseRecommendation> recommendations =
                recommendationService.recommendCourses(userId, limit);

        return Result.suc(recommendations);
    }
}
