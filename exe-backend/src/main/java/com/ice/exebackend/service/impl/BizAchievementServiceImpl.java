package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizAchievement;
import com.ice.exebackend.entity.BizLearningActivity;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizUserAchievement;
import com.ice.exebackend.mapper.BizAchievementMapper;
import com.ice.exebackend.mapper.BizUserAchievementMapper;
import com.ice.exebackend.service.BizAchievementService;
import com.ice.exebackend.service.BizLearningActivityService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BizAchievementServiceImpl extends ServiceImpl<BizAchievementMapper, BizAchievement> implements BizAchievementService {

    @Autowired
    private BizUserAchievementMapper userAchievementMapper;
    @Autowired
    private BizStudentService studentService;
    @Autowired
    private BizLearningActivityService activityService;

    @Override
    @Transactional
    public List<BizAchievement> checkAndAward(Long studentId, String type, int currentValue) {
        List<BizAchievement> newUnlocked = new ArrayList<>();

        // 1. 查询该类型的所有成就规则
        List<BizAchievement> rules = this.list(new QueryWrapper<BizAchievement>().eq("type", type));
        if (rules.isEmpty()) return newUnlocked;

        // 2. 查询用户已获得的该类型成就ID
        List<BizUserAchievement> obtained = userAchievementMapper.selectList(
                new QueryWrapper<BizUserAchievement>()
                        .eq("student_id", studentId)
                        .in("achievement_id", rules.stream().map(BizAchievement::getId).collect(Collectors.toList()))
        );
        Set<Long> obtainedIds = obtained.stream().map(BizUserAchievement::getAchievementId).collect(Collectors.toSet());

        // 3. 遍历规则，判断是否达成且未获得
        for (BizAchievement rule : rules) {
            if (!obtainedIds.contains(rule.getId()) && currentValue >= rule.getThreshold()) {
                // 达成成就！
                awardAchievement(studentId, rule);
                newUnlocked.add(rule);
            }
        }
        return newUnlocked;
    }

    private void awardAchievement(Long studentId, BizAchievement achievement) {
        // 1. 记录成就
        BizUserAchievement ua = new BizUserAchievement();
        ua.setStudentId(studentId);
        ua.setAchievementId(achievement.getId());
        ua.setCreateTime(LocalDateTime.now());
        userAchievementMapper.insert(ua);

        // 2. 发放奖励积分
        if (achievement.getRewardPoints() > 0) {
            BizStudent student = studentService.getById(studentId);
            student.setPoints(student.getPoints() + achievement.getRewardPoints());
            studentService.updateById(student);
        }

        // 3. 记录动态日志
        BizLearningActivity log = new BizLearningActivity();
        log.setStudentId(studentId);
        log.setActivityType("ACHIEVEMENT");
        log.setDescription("解锁成就：【" + achievement.getName() + "】" +
                (achievement.getRewardPoints() > 0 ? "，获得 " + achievement.getRewardPoints() + " 积分！" : ""));
        log.setCreateTime(LocalDateTime.now());
        activityService.save(log);
    }

    @Override
    public List<BizAchievement> getStudentAchievements(Long studentId) {
        // 1. 获取所有成就
        List<BizAchievement> all = this.list(new QueryWrapper<BizAchievement>().orderByAsc("type", "threshold"));

        // 2. 获取已解锁的
        List<BizUserAchievement> userAchievements = userAchievementMapper.selectList(
                new QueryWrapper<BizUserAchievement>().eq("student_id", studentId)
        );
        Map<Long, BizUserAchievement> uaMap = userAchievements.stream()
                .collect(Collectors.toMap(BizUserAchievement::getAchievementId, ua -> ua));

        // 3. 标记状态
        for (BizAchievement a : all) {
            if (uaMap.containsKey(a.getId())) {
                a.setIsUnlocked(true);
                a.setUnlockTime(uaMap.get(a.getId()).getCreateTime());
            } else {
                a.setIsUnlocked(false);
            }
        }
        return all;
    }
}