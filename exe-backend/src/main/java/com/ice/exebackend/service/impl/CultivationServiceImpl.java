package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.enums.RealmEnum;
import com.ice.exebackend.enums.SpiritRootEnum;
import com.ice.exebackend.mapper.BizCultivationMapper;
import com.ice.exebackend.mapper.BizGoodsMapper;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.mapper.BizUserGoodsMapper;
import com.ice.exebackend.service.BizQuestionService;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.CultivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ice.exebackend.exception.TribulationException; // 导入刚才建的异常

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom; // 推荐使用 ThreadLocalRandom
import org.slf4j.Logger; // 导入日志
import org.slf4j.LoggerFactory;





@Service
public class CultivationServiceImpl extends ServiceImpl<BizCultivationMapper, BizCultivation> implements CultivationService {


    private static final Logger log = LoggerFactory.getLogger(CultivationServiceImpl.class);

    @Autowired
    private BizQuestionService questionService; // 注入题库服务用于校验答案

    @Autowired
    private BizUserGoodsMapper userGoodsMapper; // 注入用户背包 Mapper

    @Autowired
    private BizGoodsMapper goodsMapper; // 注入商品 Mapper

    @Autowired
    private BizStudentService studentService; // 注入学生服务（用于发放积分奖励）

    @Autowired
    private BizQuestionMapper questionMapper; // 确保注入这个


    @Override
    public BizCultivation getOrCreateProfile(Long studentId) {
        BizCultivation profile = this.getById(studentId);
        if (profile == null) {
            profile = new BizCultivation();
            profile.setStudentId(studentId);
            profile.setRealmLevel(0); // 默认为凡人

            // 【修改】从枚举获取初始配置
            RealmEnum initRealm = RealmEnum.MORTAL;
            profile.setCurrentExp(0L);
            profile.setMaxExp(initRealm.getBaseMaxExp());
            profile.setAttack(10);
            profile.setDefense(5);
            profile.setLastSettlementTime(LocalDateTime.now());

            this.save(profile);
        }
        // 【新增】应用灵根加成
        applySpiritRootBonuses(profile);
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

    @Override
    @Transactional
    public String breakthroughWithItem(Long studentId, Long goodsId) {
        BizCultivation profile = getById(studentId);
        if (profile.getCurrentExp() < profile.getMaxExp()) {
            throw new RuntimeException("修为不足，无法感应天劫！当前需 " + profile.getMaxExp());
        }

        // 【修改】使用枚举获取当前境界的基础成功率
        RealmEnum currentRealm = RealmEnum.getByLevel(profile.getRealmLevel());
        double successRate = currentRealm.getSuccessRate();
        String itemMsg = "";

        // --- 道具逻辑 (保持不变) ---
        if (goodsId != null) {
            QueryWrapper<BizUserGoods> query = new QueryWrapper<BizUserGoods>()
                    .eq("student_id", studentId).eq("goods_id", goodsId).last("LIMIT 1");
            BizUserGoods userGoods = userGoodsMapper.selectOne(query);

            if (userGoods != null) {
                BizGoods goods = goodsMapper.selectById(goodsId);
                if (goods != null && "PILL".equals(goods.getType())) {
                    try {
                        double bonus = Double.parseDouble(goods.getResourceValue());
                        successRate += bonus;
                        itemMsg = "（" + goods.getName() + "护体，成功率+" + (int)(bonus * 100) + "%）";
                        userGoodsMapper.deleteById(userGoods.getId());
                    } catch (NumberFormatException e) {}
                }
            } else {
                throw new RuntimeException("你的背包中没有该丹药！");
            }
        }

        // 封顶 95%
        successRate = Math.min(0.95, successRate);

        if (ThreadLocalRandom.current().nextDouble() < successRate) {
            doSuccess(profile);
            return "⚡️ 渡劫成功！" + itemMsg + " 晋升为【" + getRealmName(profile.getRealmLevel()) + "】！";
        } else {
            // 【修改点】失败时不直接 doFail，而是抛出心魔异常

            // 1. 尝试获取一道错题
            BizQuestion question = questionMapper.selectRandomWrongQuestion(studentId);

            // 2. 如果没有错题，随机抽一道普通题
            if (question == null) {
                question = questionMapper.selectRandomQuestion();
            }

            // 3. 如果题库是空的 (极少见)，则直接失败
            if (question == null) {
                doFail(profile);
                throw new RuntimeException("💔 渡劫失败！天雷击碎了你的防御，修为受损！");
            }

            // 4. 抛出心魔异常，携带题目
            // 注意：这里不要调用 doFail，因为还有机会挽救
            throw new TribulationException("天劫降临！心魔滋生！", question);
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

        // 【优化1】移除严格的经验校验，或改为 warn 日志。
        // 因为如果是“心魔”补救，此时可能因为并发或其他逻辑导致经验略有变动，不应直接阻断。
        // 只要是当前境界的满经验附近即可（例如允许少一点点，或者干脆只在完全不够时才拦）
        // 这里建议：如果是为了救场，只要等级没变，就允许尝试。
        /* if (profile.getCurrentExp() < profile.getMaxExp()) {
            // 如果你非常确定逻辑严密，可以保留，但为了防止死锁，建议先注释掉或放宽
            // throw new RuntimeException("修为不足，无法感应天劫！");
        }
        */

        BizQuestion question = questionService.getById(questionId);
        if (question == null) {
            throw new RuntimeException("天劫异象（题目不存在），请稍后再试");
        }

        // 【优化2】答案标准化处理 (兼容 T/F 和 正确/错误)
        String dbAnswer = question.getAnswer().trim();
        String input = userAnswer == null ? "" : userAnswer.trim();

        // 判断题特殊兼容
        if (question.getQuestionType() != null && question.getQuestionType() == 4) {
            if ("T".equalsIgnoreCase(input)) input = "正确";
            if ("F".equalsIgnoreCase(input)) input = "错误";

            if ("T".equalsIgnoreCase(dbAnswer)) dbAnswer = "正确";
            if ("F".equalsIgnoreCase(dbAnswer)) dbAnswer = "错误";
        }

        log.info("心魔校验 - 题目: {}, 用户输入(转换后): {}, 正确答案(转换后): {}", questionId, input, dbAnswer);

        boolean isCorrect = dbAnswer.equalsIgnoreCase(input);

        if (isCorrect) {
            // 答对了！
            // 【关键】必须检查是否需要恢复被错误扣除的经验 (如果你的 breakthroughWithItem 误扣了的话)
            // 但标准逻辑是：doSuccess 会自动重置当前经验为 0 (升级了)，所以不用手动恢复。
            doSuccess(profile);
            return "⚡️ 智慧破天劫！你答对了【天劫试炼】，成功晋升【" + getRealmName(profile.getRealmLevel()) + "】！";
        } else {
            // 只有答错了，才执行失败惩罚
            doFail(profile);
            // 抛出异常告诉前端
            throw new RuntimeException("💔 试炼失败！你的回答无法抗衡天劫（正确答案：" + question.getAnswer() + "），修为受损！");
        }
    }

    // === 私有辅助方法：处理成功逻辑 (核心修改) ===
    private void doSuccess(BizCultivation profile) {
        // 1. 扣除升级所需的经验
        profile.setCurrentExp(Math.max(0, profile.getCurrentExp() - profile.getMaxExp()));

        // 2. 等级提升
        int newLevel = profile.getRealmLevel() + 1;
        profile.setRealmLevel(newLevel);

        // 3. 获取新境界的配置
        RealmEnum newRealm = RealmEnum.getByLevel(newLevel);

        // 4. 【核心优化】计算新的经验上限
        // 逻辑：如果是跨大境界（如炼气9 -> 筑基0），使用枚举定义的 baseMaxExp
        // 如果是小境界提升（如炼气1 -> 炼气2），在当前基础上 * 1.2
        if (newLevel % 10 == 0) {
            // 跨大境界，直接使用新境界的基准值
            profile.setMaxExp(newRealm.getBaseMaxExp());
        } else {
            // 小境界，平滑增长 (例如 1.2倍)
            profile.setMaxExp((long)(profile.getMaxExp() * 1.2));
        }

        // 5. 【核心优化】属性提升
        // 如果是跨大境界，获得枚举定义的巨额加成；如果是小境界，获得少量成长
        if (newLevel % 10 == 0) {
            profile.setAttack(profile.getAttack() + newRealm.getAtkBonus());
            profile.setDefense(profile.getDefense() + newRealm.getDefBonus());
        } else {
            // 小境界成长：攻击+10%，防御+10% (或者固定值)
            profile.setAttack((int)(profile.getAttack() * 1.1) + 10);
            profile.setDefense((int)(profile.getDefense() * 1.1) + 5);
        }

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
        // 【修改】使用枚举生成名称
        RealmEnum realm = RealmEnum.getByLevel(level);
        int smallLevel = level % 10;

        if (realm == RealmEnum.MORTAL && smallLevel == 0) return "凡人";
        if (realm == RealmEnum.ASCENSION) return realm.getName();

        // 例如：炼气期 3层
        return realm.getName() + (smallLevel == 0 ? "初期" : " " + smallLevel + "层");
    }
    // 解析灵根数据
    private Map<String, Integer> parseSpiritRoots(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        return JSON.parseObject(json, new TypeReference<Map<String, Integer>>(){});
    }

    // 根据经验值计算等级 (例如: 等级 = 根号(经验/10))
    private int calculateLevel(int exp) {
        return (int) Math.sqrt(exp / 10.0);
    }

    // 计算灵根带来的属性加成
    private void applySpiritRootBonuses(BizCultivation profile) {
        Map<String, Integer> roots = parseSpiritRoots(profile.getSpiritRoots());

        // 基础属性
        int baseAttack = profile.getAttack() != null ? profile.getAttack() : 0;
        int baseDefense = profile.getDefense() != null ? profile.getDefense() : 0;
        int baseHp = profile.getMaxHp() != null ? profile.getMaxHp() : 100;

        // 遍历枚举计算加成
        for (SpiritRootEnum root : SpiritRootEnum.values()) {
            int exp = roots.getOrDefault(root.name(), 0);
            int level = calculateLevel(exp);

            if (level > 0) {
                switch (root) {
                    case METAL: // 金生攻
                        baseAttack += level * 5; // 每级加5点攻击
                        break;
                    case EARTH: // 土生防
                        baseDefense += level * 3; // 每级加3点防御
                        break;
                    case WOOD: // 木生血
                        baseHp += level * 20; // 每级加20点血
                        break;
                    // 水和火的特殊属性（暴击/速度）可能需要你在 Cultivation 实体里加新字段，
                    // 这里暂时把火加到攻击，水加到防御演示
                    case FIRE:
                        baseAttack += level * 8; // 火系攻击加成更高
                        break;
                    case WATER:
                        baseDefense += level * 2;
                        break;
                }
            }
        }

        // 更新内存中的对象（不存库，只用于返回给前端展示或战斗计算）
        profile.setAttack(baseAttack);
        profile.setDefense(baseDefense);
        profile.setMaxHp(baseHp);
    }
    @Override
    public void addSpiritRootExp(Long studentId, String subjectName, int score) {
        BizCultivation profile = getOrCreateProfile(studentId);
        Map<String, Integer> roots = parseSpiritRoots(profile.getSpiritRoots());

        // 1. 匹配灵根
        SpiritRootEnum rootType = SpiritRootEnum.matchBySubject(subjectName);

        // 2. 计算增加的经验 (假设分数即经验，可调整系数)
        int expGain = score;

        // 3. 更新数据
        String key = rootType.name();
        int currentExp = roots.getOrDefault(key, 0);
        roots.put(key, currentExp + expGain);

        // 4. 保存回数据库
        profile.setSpiritRoots(JSON.toJSONString(roots));
        this.updateById(profile);

        // 可以在这里通过 WebSocket 推送通知： "恭喜！你的金灵根经验 +10"
    }

}