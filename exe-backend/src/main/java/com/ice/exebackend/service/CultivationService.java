package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizCultivation;
import java.util.Map;

public interface CultivationService extends IService<BizCultivation> {
    BizCultivation getOrCreateProfile(Long studentId);
    void addExp(Long studentId, int amount);
    String breakthrough(Long studentId);
    String getRealmName(int level);
    String breakthroughWithQuiz(Long studentId, Long questionId, String userAnswer);

    // 【新增】带丹药的突破
    String breakthroughWithItem(Long studentId, Long goodsId);

    // 【新增】带奇遇的打坐
    Map<String, Object> meditateWithEvent(Long studentId);

    // 【新增】结算离线收益
    Map<String, Object> settleAfkReward(Long studentId);

    void addSpiritRootExp(Long studentId, String subjectName, int score);
}