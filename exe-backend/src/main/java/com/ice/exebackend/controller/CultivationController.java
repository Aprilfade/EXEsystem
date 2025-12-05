package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizCultivation;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.CultivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/student/game")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class CultivationController {

    @Autowired
    private CultivationService cultivationService;
    @Autowired
    private BizStudentService studentService;

    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return s.getId();
    }

    @GetMapping("/profile")
    public Result getProfile(Authentication auth) {
        Long uid = getCurrentStudentId(auth);
        BizCultivation profile = cultivationService.getOrCreateProfile(uid);

        // 包装返回，带上境界名称
        String realmName = cultivationService.getRealmName(profile.getRealmLevel());

        // 使用 Map 混合返回原始数据和计算后的名称
        return Result.suc(Map.of(
                "data", profile,
                "realmName", realmName
        ));
    }

    @PostMapping("/breakthrough")
    public Result doBreakthrough(Authentication auth) {
        try {
            String msg = cultivationService.breakthrough(getCurrentStudentId(auth));
            return Result.suc(msg);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 【新增】静心打坐（签到式的各种小功能入口）
    @PostMapping("/meditate")
    public Result meditate(Authentication auth) {
        Long uid = getCurrentStudentId(auth);
        cultivationService.addExp(uid, 10); // 打坐加10点修为
        return Result.suc("打坐结束，吸纳天地灵气，修为 +10");
    }
    // 【新增】天劫试炼突破接口
    @PostMapping("/breakthrough-with-quiz")
    public Result doBreakthroughWithQuiz(@RequestBody Map<String, Object> body, Authentication auth) {
        Long uid = getCurrentStudentId(auth);
        Long questionId = Long.valueOf(body.get("questionId").toString());
        String answer = (String) body.get("answer");

        try {
            String msg = cultivationService.breakthroughWithQuiz(uid, questionId, answer);
            return Result.suc(msg);
        } catch (Exception e) {
            // 失败时返回 400，并带上错误信息
            return Result.fail(e.getMessage());
        }
    }
}
