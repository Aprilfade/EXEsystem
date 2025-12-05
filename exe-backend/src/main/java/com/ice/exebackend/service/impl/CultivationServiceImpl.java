package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizCultivation;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.mapper.BizCultivationMapper;
import com.ice.exebackend.service.CultivationService;
import com.ice.exebackend.service.BizQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Random;

@Service
public class CultivationServiceImpl extends ServiceImpl<BizCultivationMapper, BizCultivation> implements CultivationService {

    @Autowired
    private BizQuestionService questionService; // 注入题库服务用于校验答案

    // 境界名称映射
    private static final String[] REALM_NAMES = {
            "凡人", "炼气期", "筑基期", "金丹期", "元婴期", "化神期", "炼虚期", "合体期", "大乘期", "渡劫期"
    };

    // 基础成功率配置 (索引对应境界等级，越后期越难)
    private static final double[] BASE_SUCCESS_RATE = {
            1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1
    };

    @Override
    public BizCultivation getOrCreateProfile(Long studentId) {
        BizCultivation profile = this.getById(studentId);
        if (profile == null) {
            profile = new BizCultivation();
            profile.setStudentId(studentId);
            profile.setRealmLevel(0); // 凡人
            profile.setCurrentExp(0L);
            profile.setMaxExp(100L); // 初始上限
            profile.setAttack(10);
            profile.setDefense(5);
            this.save(profile);
        }
        return profile;
    }

    @Override
    @Transactional
    public void addExp(Long studentId, int amount) {
        BizCultivation profile = getOrCreateProfile(studentId);

        // 顿悟机制：5%概率触发暴击，获得 2 倍修为
        boolean isEpiphany = new Random().nextInt(100) < 5;
        if (isEpiphany) {
            amount *= 2;
        }

        profile.setCurrentExp(profile.getCurrentExp() + amount);

        // 属性微量成长：每获得10点修为，攻击力+1（保底+1）
        int atkGrowth = amount > 10 ? amount / 10 : 1;
        profile.setAttack(profile.getAttack() + atkGrowth);

        this.updateById(profile);
    }

    /**
     * 原有的概率突破方法 (必须保留以满足接口定义)
     */
    @Override
    @Transactional
    public String breakthrough(Long studentId) {
        BizCultivation profile = getById(studentId);
        if (profile.getCurrentExp() < profile.getMaxExp()) {
            throw new RuntimeException("修为不足，无法感应天劫！当前需 " + profile.getMaxExp());
        }

        // 获取当前境界的成功率
        int currentRealm = profile.getRealmLevel();
        double successRate = currentRealm < BASE_SUCCESS_RATE.length ? BASE_SUCCESS_RATE[currentRealm] : 0.1;

        // 运气判定
        if (Math.random() < successRate) {
            doSuccess(profile);
            return "⚡️ 渡劫成功！天雷淬体，晋升为【" + getRealmName(profile.getRealmLevel()) + "】！";
        } else {
            doFail(profile);
            // 抛出异常让前端显示红色错误
            throw new RuntimeException("💔 渡劫失败！心魔入侵，损失了修为，请重整道心！");
        }
    }

    /**
     * 【新增】带答题校验的突破方法
     * (请确保你在 CultivationService 接口中也定义了这个方法，否则请删除 @Override)
     */
    @Override
    @Transactional
    public String breakthroughWithQuiz(Long studentId, Long questionId, String userAnswer) {
        BizCultivation profile = getById(studentId);
        if (profile.getCurrentExp() < profile.getMaxExp()) {
            throw new RuntimeException("修为不足，无法感应天劫！");
        }

        // 1. 校验题目和答案
        BizQuestion question = questionService.getById(questionId);
        if (question == null) {
            throw new RuntimeException("天劫异象（题目不存在），请稍后再试");
        }

        // 简单比对答案（忽略首尾空格和大小写）
        boolean isCorrect = question.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());

        if (isCorrect) {
            // --- 答对：护体金光（100% 突破成功）---
            doSuccess(profile);
            return "⚡️ 智慧破天劫！你答对了【天劫试炼】，成功晋升【" + getRealmName(profile.getRealmLevel()) + "】！";
        } else {
            // --- 答错：天雷击顶（突破失败并扣除修为）---
            doFail(profile);
            throw new RuntimeException("💔 试炼失败！你的回答无法抗衡天劫（正确答案：" + question.getAnswer() + "），修为受损！");
        }
    }

    // === 私有辅助方法：处理成功逻辑 ===
    private void doSuccess(BizCultivation profile) {
        int currentRealm = profile.getRealmLevel();

        // 1. 扣除升级所需的经验
        profile.setCurrentExp(profile.getCurrentExp() - profile.getMaxExp());

        // 2. 境界提升
        profile.setRealmLevel(currentRealm + 1);

        // 3. 下一境界所需经验指数级提升 (1.8倍)
        profile.setMaxExp((long)(profile.getMaxExp() * 1.8));

        // 4. 属性大幅提升
        int bonus = currentRealm + 1;
        profile.setAttack(profile.getAttack() + 50 + (10 * bonus));
        profile.setDefense(profile.getDefense() + 20 + (5 * bonus));

        this.updateById(profile);
    }

    // === 私有辅助方法：处理失败逻辑 ===
    private void doFail(BizCultivation profile) {
        // 惩罚：扣除当前境界经验上限的 20%
        long lostExp = (long)(profile.getMaxExp() * 0.2);
        long newExp = profile.getCurrentExp() - lostExp;

        // 保证经验不为负数
        profile.setCurrentExp(Math.max(newExp, 0L));

        this.updateById(profile);
    }

    @Override
    public String getRealmName(int level) {
        int bigRealm = level / 10;
        int smallRealm = level % 10;
        if(bigRealm >= REALM_NAMES.length) return "飞升仙界";
        if (bigRealm == 0) return "凡人";
        return REALM_NAMES[bigRealm] + (smallRealm == 0 ? "初期" : smallRealm + "层");
    }
}