package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.impl.BizWrongRecordServiceImpl; // 注意引用Impl以调用新方法
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student/review")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentReviewController {

    @Autowired
    private BizWrongRecordServiceImpl wrongRecordService; // 注入 Impl

    @Autowired
    private BizStudentService studentService;

    // 获取当前登录学生ID
    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return s != null ? s.getId() : null;
    }

    /**
     * 获取今日待复习列表
     */
    @GetMapping("/daily")
    public Result getDailyReviewList(Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        List<WrongRecordVO> list = wrongRecordService.getDailyReviewRecords(studentId);
        return Result.suc(list);
    }

    /**
     * 提交复习结果
     * body: { "isCorrect": true }
     */
    @PostMapping("/{recordId}/result")
    public Result submitReview(@PathVariable Long recordId, @RequestBody Map<String, Boolean> body) {
        Boolean isCorrect = body.get("isCorrect");
        if (isCorrect == null) return Result.fail("参数错误");

        int nextInterval = wrongRecordService.submitReviewResult(recordId, isCorrect);

        if (nextInterval == 999) {
            return Result.suc("恭喜！该知识点已完全掌握，移出复习队列。");
        } else if (isCorrect) {
            return Result.suc("回答正确！将在 " + nextInterval + " 天后再次复习。");
        } else {
            return Result.suc("回答错误，已重置复习进度，请明天继续加油！");
        }
    }
}