package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizFavoriteService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student/favorites")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class BizFavoriteController {

    @Autowired
    private BizFavoriteService favoriteService;

    @Autowired
    private BizStudentService studentService;

    // 辅助方法：获取当前学生ID
    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return s != null ? s.getId() : null;
    }

    @PostMapping("/{questionId}")
    public Result toggleFavorite(@PathVariable Long questionId, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        if (studentId == null) return Result.fail("用户未登录");

        boolean isNowFavorited = !favoriteService.isFavorited(studentId, questionId); // 预测操作后的状态
        favoriteService.toggleFavorite(studentId, questionId);

        return Result.suc(isNowFavorited ? "收藏成功" : "已取消收藏");
    }

    @GetMapping("/check/{questionId}")
    public Result checkFavorite(@PathVariable Long questionId, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        boolean isFavorited = favoriteService.isFavorited(studentId, questionId);
        return Result.suc(isFavorited);
    }

    @GetMapping
    public Result getMyFavorites(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        Page<BizQuestion> page = new Page<>(current, size);
        return Result.suc(favoriteService.getMyFavorites(page, studentId));
    }
}