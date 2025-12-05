package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizGoods;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizCultivation;
import com.ice.exebackend.entity.BizUserGoods;
import com.ice.exebackend.mapper.BizUserGoodsMapper;
import com.ice.exebackend.service.BizGoodsService;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.CultivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ice.exebackend.exception.TribulationException;
import com.ice.exebackend.entity.BizQuestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/student/game")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class CultivationController {

    @Autowired private CultivationService cultivationService;
    @Autowired private BizStudentService studentService;
    @Autowired private BizUserGoodsMapper userGoodsMapper; // 直接注入 Mapper 查背包
    @Autowired private BizGoodsService goodsService;
    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return s.getId();
    }

    @GetMapping("/profile")
    public Result getProfile(Authentication auth) {
        Long uid = getCurrentStudentId(auth);

        // 1. 结算离线收益
        Map<String, Object> afkReward = cultivationService.settleAfkReward(uid);

        // 2. 获取最新档案
        BizCultivation profile = cultivationService.getOrCreateProfile(uid);
        String realmName = cultivationService.getRealmName(profile.getRealmLevel());

        // 3. 返回组合数据
        return Result.suc(Map.of(
                "data", profile,
                "realmName", realmName,
                "afkReward", afkReward != null ? afkReward : "NONE" // 如果有收益，前端会弹窗
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

    // 【修改】打坐接口改用 meditateWithEvent
    @PostMapping("/meditate")
    public Result meditate(Authentication auth) {
        Long uid = getCurrentStudentId(auth);
        Map<String, Object> eventResult = cultivationService.meditateWithEvent(uid);
        return Result.suc(eventResult);
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
    // 【新增】获取我的丹药
    @GetMapping("/my-pills")
    public Result getMyPills(Authentication auth) {
        Long uid = getCurrentStudentId(auth);
        // 查询背包里 type = PILL 的物品
        // 这里为了简单，先查 user_goods 再查 goods，实际可用联表
        List<BizUserGoods> userGoods = userGoodsMapper.selectList(new QueryWrapper<BizUserGoods>().eq("student_id", uid));
        if(userGoods.isEmpty()) return Result.suc(new ArrayList<>());

        List<Long> goodsIds = userGoods.stream().map(BizUserGoods::getGoodsId).collect(Collectors.toList());
        List<BizGoods> pills = goodsService.list(new QueryWrapper<BizGoods>()
                .in("id", goodsIds)
                .eq("type", "PILL")); // 只返回丹药

        return Result.suc(pills);
    }

    @PostMapping("/breakthrough-v2")
    public Result breakthroughWithItem(@RequestBody Map<String, Long> body, Authentication auth) {
        Long uid = getCurrentStudentId(auth);
        Long goodsId = body.get("goodsId");
        try {
            String msg = cultivationService.breakthroughWithItem(uid, goodsId);
            return Result.suc(msg);
        } catch (TribulationException e) {
            // 【核心】捕获心魔异常，返回特殊状态码 401 (或者自定义业务码如 2001)
            // 这里为了前端方便识别，我们用 code: 202 代表 "需进一步操作"
            Map<String, Object> data = new HashMap<>();
            BizQuestion q = e.getQuestion();

            // 简单脱敏，去掉答案
            q.setAnswer(null);
            q.setDescription(null);

            data.put("message", "突破遭遇心魔！请回答问题以稳定道心！");
            data.put("question", q);

            // 返回 Result.result(code, msg, total, data)
            // 我们复用 Result 类，假设它有全参构造或setter，这里用 Result.suc 的变体或手动构造
            Result res = new Result();
            res.setCode(202); // 202: Accepted / Need Action
            res.setMsg("TRIBULATION_TRIGGERED");
            res.setData(data);
            return res;

        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
