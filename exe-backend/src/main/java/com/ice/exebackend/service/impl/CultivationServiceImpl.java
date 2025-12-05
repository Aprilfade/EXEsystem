package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizCultivation;
import com.ice.exebackend.mapper.BizCultivationMapper;
import com.ice.exebackend.service.CultivationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CultivationServiceImpl extends ServiceImpl<BizCultivationMapper, BizCultivation> implements CultivationService {

    // 简单的境界名称映射
    private static final String[] REALM_NAMES = {
            "凡人", "炼气期", "筑基期", "金丹期", "元婴期", "化神期", "炼虚期", "合体期", "大乘期", "渡劫期"
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
        profile.setCurrentExp(profile.getCurrentExp() + amount);

        // 简单的属性成长：每刷一道题，攻击力微弱提升
        profile.setAttack(profile.getAttack() + 1);

        this.updateById(profile);
    }

    @Override
    @Transactional
    public String breakthrough(Long studentId) {
        BizCultivation profile = getById(studentId);
        if (profile.getCurrentExp() < profile.getMaxExp()) {
            throw new RuntimeException("修为不足，无法突破瓶颈！当前需 " + profile.getMaxExp());
        }

        // 突破逻辑
        profile.setCurrentExp(profile.getCurrentExp() - profile.getMaxExp());
        profile.setRealmLevel(profile.getRealmLevel() + 1);
        // 下一境界所需经验翻倍
        profile.setMaxExp((long)(profile.getMaxExp() * 1.5));
        // 属性大幅提升
        profile.setAttack(profile.getAttack() + 50);
        profile.setDefense(profile.getDefense() + 20);

        this.updateById(profile);
        return "恭喜道友！突破至【" + getRealmName(profile.getRealmLevel()) + "】！";
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