package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizAchievement;
import com.ice.exebackend.entity.BizLearningActivity;
import com.ice.exebackend.entity.BizSignIn;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.mapper.BizSignInMapper;
import com.ice.exebackend.service.BizAchievementService;
import com.ice.exebackend.service.BizLearningActivityService;
import com.ice.exebackend.service.BizSignInService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizSignInServiceImpl extends ServiceImpl<BizSignInMapper, BizSignIn> implements BizSignInService {

    @Autowired
    private BizStudentService studentService;
    @Autowired
    private BizLearningActivityService activityService;

    @Autowired
    private BizAchievementService achievementService; // 注入

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> doSignIn(Long studentId) {
        LocalDate today = LocalDate.now();

        // 1. 检查今日是否已签
        long count = this.count(new QueryWrapper<BizSignIn>()
                .eq("student_id", studentId)
                .eq("sign_date", today));
        if (count > 0) {
            throw new RuntimeException("今日已签到，请勿重复操作");
        }

        // 2. 插入签到记录
        BizSignIn signIn = new BizSignIn();
        signIn.setStudentId(studentId);
        signIn.setSignDate(today);
        signIn.setCreateTime(LocalDateTime.now());
        this.save(signIn);

        // 3. 计算连续签到天数 (向前回溯)
        int streak = 1;
        for (int i = 1; i <= 30; i++) { // 最多回溯30天
            long exists = this.count(new QueryWrapper<BizSignIn>()
                    .eq("student_id", studentId)
                    .eq("sign_date", today.minusDays(i)));
            if (exists > 0) {
                streak++;
            } else {
                break; // 断签，停止计算
            }
        }

        // 4. 计算奖励积分
        int points = 5; // 基础分
        String tip = "签到成功！积分 +5";

        // 连续签到7天奖励
        if (streak % 7 == 0) {
            points += 20;
            tip = "连续签到 " + streak + " 天！获得额外奖励，积分 +" + points;
        }

        // 5. 更新学生积分
        BizStudent student = studentService.getById(studentId);
        student.setPoints(student.getPoints() + points);
        studentService.updateById(student);

        // 6. 记录日志
        BizLearningActivity log = new BizLearningActivity();
        log.setStudentId(studentId);
        log.setActivityType("SIGN_IN");
        log.setDescription("每日签到 (连续 " + streak + " 天)");
        log.setCreateTime(LocalDateTime.now());
        activityService.save(log);

        // 【修复】先定义 result Map，然后再进行成就检查
        Map<String, Object> result = new HashMap<>();
        result.put("points", points);
        result.put("streak", streak);
        result.put("message", tip);

        // 7. 检查连签成就
        List<BizAchievement> newAchievements = achievementService.checkAndAward(studentId, "SIGN_IN_STREAK", streak);

        // 如果有新成就，放入 result 返回给前端弹窗
        if (!newAchievements.isEmpty()) {
            result.put("newAchievements", newAchievements); // 现在 result 已经定义了，不会报错
        }

        return result;
    }

    @Override
    public Map<String, Object> getSignInStatus(Long studentId, String yearMonth) {
        // yearMonth 格式 "2023-10"
        LocalDate start = LocalDate.parse(yearMonth + "-01");
        LocalDate end = start.plusMonths(1).minusDays(1);

        // 查询当月所有签到记录
        List<BizSignIn> list = this.list(new QueryWrapper<BizSignIn>()
                .eq("student_id", studentId)
                .between("sign_date", start, end));

        List<String> signedDates = list.stream()
                .map(s -> s.getSignDate().toString())
                .collect(Collectors.toList());

        // 查询今日是否已签
        boolean todaySigned = this.count(new QueryWrapper<BizSignIn>()
                .eq("student_id", studentId)
                .eq("sign_date", LocalDate.now())) > 0;

        Map<String, Object> result = new HashMap<>();
        result.put("signedDates", signedDates);
        result.put("todaySigned", todaySigned);
        return result;
    }
}