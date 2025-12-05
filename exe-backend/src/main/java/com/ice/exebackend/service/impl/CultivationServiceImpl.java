package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizCultivation;
import com.ice.exebackend.mapper.BizCultivationMapper;
import com.ice.exebackend.service.CultivationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Random;

@Service
public class CultivationServiceImpl extends ServiceImpl<BizCultivationMapper, BizCultivation> implements CultivationService {

    // 境界名称映射
    private static final String[] REALM_NAMES = {
            "凡人", "炼气期", "筑基期", "金丹期", "元婴期", "化神期", "炼虚期", "合体期", "大乘期", "渡劫期"
    };

    // 基础成功率配置 (索引对应境界等级，越后期越难)
    // 0:凡人(100%), 1:炼气(90%), ..., 9:渡劫(10%)
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

        // 【优化1】顿悟机制：5%概率触发暴击，获得 2 倍修为
        // 增加随机性，让用户有“中奖”的快感
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

    @Override
    @Transactional
    public String breakthrough(Long studentId) {
        BizCultivation profile = getById(studentId);
        if (profile.getCurrentExp() < profile.getMaxExp()) {
            throw new RuntimeException("修为不足，无法感应天劫！当前需 " + profile.getMaxExp());
        }

        // 【优化2】概率突破机制
        int currentRealm = profile.getRealmLevel();
        // 获取当前境界的成功率，如果超出数组范围则默认为 10%
        double successRate = currentRealm < BASE_SUCCESS_RATE.length ? BASE_SUCCESS_RATE[currentRealm] : 0.1;

        // 运气判定 (生成 0.0 到 1.0 之间的随机数)
        double roll = Math.random();

        if (roll < successRate) {
            // --- 突破成功 ---

            // 1. 扣除升级所需的经验
            profile.setCurrentExp(profile.getCurrentExp() - profile.getMaxExp());

            // 2. 境界提升
            profile.setRealmLevel(currentRealm + 1);

            // 3. 下一境界所需经验指数级提升 (1.8倍)
            profile.setMaxExp((long)(profile.getMaxExp() * 1.8));

            // 4. 属性大幅提升 (根据新境界等级给予额外加成)
            int bonus = currentRealm + 1;
            profile.setAttack(profile.getAttack() + 50 + (10 * bonus));
            profile.setDefense(profile.getDefense() + 20 + (5 * bonus));

            this.updateById(profile);
            return "⚡️ 渡劫成功！天雷淬体，晋升为【" + getRealmName(profile.getRealmLevel()) + "】！";
        } else {
            // --- 突破失败 ---

            // 惩罚：扣除当前境界经验上限的 20%
            // 这会让玩家感到“心痛”，从而更珍惜成功
            long lostExp = (long)(profile.getMaxExp() * 0.2);
            long newExp = profile.getCurrentExp() - lostExp;

            // 保证经验不为负数
            profile.setCurrentExp(Math.max(newExp, 0L));

            this.updateById(profile);

            // 抛出异常，让前端捕获并显示红色错误提示
            throw new RuntimeException("💔 渡劫失败！心魔入侵，损失了 " + lostExp + " 点修为，请重整道心！");
        }
    }

    @Override
    public String getRealmName(int level) {
        // 简化逻辑：每10级一个大境界
        int bigRealm = level / 10;
        int smallRealm = level % 10;
        if(bigRealm >= REALM_NAMES.length) return "飞升仙界";

        if (bigRealm == 0) return "凡人";

        return REALM_NAMES[bigRealm] + (smallRealm == 0 ? "初期" : smallRealm + "层");
    }
}