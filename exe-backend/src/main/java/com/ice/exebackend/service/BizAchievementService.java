package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizAchievement;

import java.util.List;

public interface BizAchievementService extends IService<BizAchievement> {
    // 检查并颁发成就
    List<BizAchievement> checkAndAward(Long studentId, String type, int currentValue);

    // 获取学生的成就列表（包含已解锁和未解锁）
    List<BizAchievement> getStudentAchievements(Long studentId);
}