package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.mapper.BizCultivationMapper;
import com.ice.exebackend.mapper.BizGoodsMapper;
import com.ice.exebackend.mapper.BizUserGoodsMapper;
import com.ice.exebackend.service.BizQuestionService;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.CultivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class CultivationServiceImpl extends ServiceImpl<BizCultivationMapper, BizCultivation> implements CultivationService {

    @Autowired
    private BizQuestionService questionService; // 注入题库服务用于校验答案

    @Autowired
    private BizUserGoodsMapper userGoodsMapper; // 注入用户背包 Mapper

    @Autowired
    private BizGoodsMapper goodsMapper; // 注入商品 Mapper

    @Autowired
    private BizStudentService studentService; // 注入学生服务（用于发放积分奖励）

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
            // 初始化上次结算时间，避免新用户第一次登录就触发大量离线收益
            profile.setLastSettlementTime(LocalDateTime.now());
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
     * 【新增】结算离线挂机收益
     */
    @Override
    @Transactional
    public Map<String, Object> settleAfkReward(Long studentId) {
        BizCultivation profile = getOrCreateProfile(studentId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastTime = profile.getLastSettlementTime();

        // 如果数据异常没有上次时间，重置并返回
        if (lastTime == null) {
            profile.setLastSettlementTime(now);
            this.updateById(profile);
            return null;
        }

        // 计算时间差（分钟）
        long minutes = Duration.between(lastTime, now).toMinutes();

        // 限制：离线少于 10 分钟不结算，避免频繁刷新刷分
        if (minutes < 10) {
            profile.setLastSettlementTime(now); // 更新时间但不结算
            this.updateById(profile);
            return null;
        }

        // 限制：单次结算上限 24 小时 (1440分钟)
        if (minutes > 1440) minutes = 1440;

        // 收益公式：每分钟 = (境界等级 + 1) * 基础系数
        // 道心加成：防御力越高，挂机收益越高 (1% 加成)
        long baseRate = (profile.getRealmLevel() + 1);
        long totalExp = (long) (minutes * baseRate * (1 + profile.getDefense() * 0.01));

        addExp(studentId, (int) totalExp);

        // 更新结算时间
        profile.setLastSettlementTime(now);
        this.updateById(profile);

        Map<String, Object> result = new HashMap<>();
        result.put("minutes", minutes);
        result.put("exp", totalExp);
        return result;
    }

    /**
     * 【新增】打坐奇遇 (随机事件)
     */
    @Override
    @Transactional
    public Map<String, Object> meditateWithEvent(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        BizCultivation profile = getOrCreateProfile(studentId);

        int baseExp = 10 + (profile.getRealmLevel() * 2);
        Random rand = new Random();
        int eventRoll = rand.nextInt(100); // 0-99

        // 事件概率判定
        if (eventRoll < 5) {
            // [5% 概率] 奇遇：捡到残卷 (大量修为)
            int bonusExp = baseExp * 10;
            addExp(studentId, bonusExp);
            result.put("type", "LUCKY");
            result.put("msg", "✨ 奇遇：你在入定中神游太虚，捡到一页【上古残卷】，修为暴涨 " + bonusExp + " 点！");
        } else if (eventRoll < 10) {
            // [5% 概率] 奇遇：高人指点 (获得积分)
            int points = 20;
            BizStudent s = studentService.getById(studentId);
            if (s != null) {
                s.setPoints((s.getPoints() == null ? 0 : s.getPoints()) + points);
                studentService.updateById(s);
            }
            addExp(studentId, baseExp);
            result.put("type", "LUCKY");
            result.put("msg", "🧙‍♂️ 奇遇：一位神秘扫地僧指点了你的迷津，积分 +" + points + "，修为 +" + baseExp);
        } else if (eventRoll < 15) {
            // [5% 概率] 厄运：走火入魔 (扣除修为)
            // 保护机制：如果当前修为太低，不扣除，改为正常打坐
            if (profile.getCurrentExp() > baseExp * 2) {
                profile.setCurrentExp(profile.getCurrentExp() - baseExp * 2);
                this.updateById(profile);
                result.put("type", "BAD");
                result.put("msg", "💀 厄运：你急于求成，导致气息紊乱走火入魔！修为倒退 " + (baseExp * 2) + " 点！");
            } else {
                addExp(studentId, baseExp);
                result.put("type", "NORMAL");
                result.put("msg", "🧘 打坐结束，险些走火入魔，幸好根基尚浅，有惊无险。修为 +" + baseExp);
            }
        } else {
            // [85% 概率] 正常打坐
            addExp(studentId, baseExp);
            result.put("type", "NORMAL");
            result.put("msg", "🧘 打坐结束，吸纳天地灵气，修为 +" + baseExp);
        }

        return result;
    }

    /**
     * 【新增】使用道具辅助突破
     */
    @Override
    @Transactional
    public String breakthroughWithItem(Long studentId, Long goodsId) {
        BizCultivation profile = getById(studentId);
        if (profile.getCurrentExp() < profile.getMaxExp()) {
            throw new RuntimeException("修为不足，无法感应天劫！当前需 " + profile.getMaxExp());
        }

        // 获取当前境界的基础成功率
        int currentRealm = profile.getRealmLevel();
        double successRate = currentRealm < BASE_SUCCESS_RATE.length ? BASE_SUCCESS_RATE[currentRealm] : 0.1;
        String itemMsg = "";

        // --- 道具逻辑开始 ---
        if (goodsId != null) {
            // 1. 校验背包是否有该道具
            QueryWrapper<BizUserGoods> query = new QueryWrapper<BizUserGoods>()
                    .eq("student_id", studentId)
                    .eq("goods_id", goodsId)
                    .last("LIMIT 1");
            BizUserGoods userGoods = userGoodsMapper.selectOne(query);

            if (userGoods != null) {
                // 2. 获取道具详情，读取加成数值
                BizGoods goods = goodsMapper.selectById(goodsId);
                // 假设 type='PILL' 为丹药
                if (goods != null && "PILL".equals(goods.getType())) {
                    // 解析 resource_value (如 "0.2" 代表 +20%)
                    try {
                        double bonus = Double.parseDouble(goods.getResourceValue());
                        successRate += bonus;
                        itemMsg = "（" + goods.getName() + "护体，成功率+" + (int)(bonus * 100) + "%）";

                        // 3. 消耗道具
                        userGoodsMapper.deleteById(userGoods.getId());
                    } catch (NumberFormatException e) {
                        // 防止配置错误导致崩溃，忽略该道具效果
                    }
                }
            } else {
                throw new RuntimeException("你的背包中没有该丹药！");
            }
        }
        // --- 道具逻辑结束 ---

        // 封顶 95% 成功率，保留一丝天意
        successRate = Math.min(0.95, successRate);

        // 随机判定
        if (Math.random() < successRate) {
            doSuccess(profile);
            return "⚡️ 渡劫成功！" + itemMsg + " 晋升为【" + getRealmName(profile.getRealmLevel()) + "】！";
        } else {
            doFail(profile);
            throw new RuntimeException("💔 渡劫失败！" + itemMsg + " 天雷击碎了你的防御，修为受损！");
        }
    }

    /**
     * 原有的概率突破 (保持兼容性)
     */
    @Override
    @Transactional
    public String breakthrough(Long studentId) {
        // 直接复用带 Item 的方法，传 null 即可
        return breakthroughWithItem(studentId, null);
    }

    @Override
    @Transactional
    public String breakthroughWithQuiz(Long studentId, Long questionId, String userAnswer) {
        BizCultivation profile = getById(studentId);
        if (profile.getCurrentExp() < profile.getMaxExp()) {
            throw new RuntimeException("修为不足，无法感应天劫！");
        }

        BizQuestion question = questionService.getById(questionId);
        if (question == null) {
            throw new RuntimeException("天劫异象（题目不存在），请稍后再试");
        }

        boolean isCorrect = question.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());

        if (isCorrect) {
            doSuccess(profile);
            return "⚡️ 智慧破天劫！你答对了【天劫试炼】，成功晋升【" + getRealmName(profile.getRealmLevel()) + "】！";
        } else {
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
        if (bigRealm == 0 && smallRealm == 0) return "凡人"; // 修正0级显示
        return REALM_NAMES[bigRealm] + (smallRealm == 0 ? "初期" : smallRealm + "层");
    }
}