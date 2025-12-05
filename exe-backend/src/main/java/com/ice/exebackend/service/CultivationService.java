package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizCultivation;

public interface CultivationService extends IService<BizCultivation> {
    // 获取或初始化档案
    BizCultivation getOrCreateProfile(Long studentId);

    // 增加修为 (刷题调用)
    void addExp(Long studentId, int amount);

    // 尝试突破
    String breakthrough(Long studentId);

    // 获取境界名称辅助方法
    String getRealmName(int level);
}