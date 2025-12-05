package com.ice.exebackend.enums;

/**
 * 境界枚举配置
 * 手动添加了构造器和Getter方法，解决Lombok不生效的问题
 */
public enum RealmEnum {
    // 序号, 名称, 基础突破成功率, 该境界初始经验上限, 攻击加成, 防御加成
    MORTAL(0, "凡人", 1.00, 100L, 0, 0),
    QI_PRACTICE(1, "炼气期", 0.90, 200L, 10, 5),
    FOUNDATION(2, "筑基期", 0.80, 500L, 50, 20),
    GOLDEN_CORE(3, "金丹期", 0.70, 1200L, 150, 60),
    NASCENT_SOUL(4, "元婴期", 0.60, 3000L, 400, 150),
    SPIRIT_SEVERING(5, "化神期", 0.50, 8000L, 1000, 400),
    VOID_REFINING(6, "炼虚期", 0.40, 20000L, 2500, 1000),
    BODY_INTEGRATION(7, "合体期", 0.30, 50000L, 6000, 2500),
    MAHAYANA(8, "大乘期", 0.20, 150000L, 15000, 6000),
    TRIBULATION(9, "渡劫期", 0.10, 500000L, 40000, 15000),
    ASCENSION(10, "飞升仙界", 0.0, 999999999L, 100000, 50000);

    // 字段定义
    private final int index;          // 境界索引
    private final String name;        // 境界名称
    private final double successRate; // 基础成功率
    private final long baseMaxExp;    // 初始经验上限
    private final int atkBonus;       // 攻击加成
    private final int defBonus;       // 防御加成

    // === 手动添加构造函数 (解决 "需要: 没有参数" 报错) ===
    RealmEnum(int index, String name, double successRate, long baseMaxExp, int atkBonus, int defBonus) {
        this.index = index;
        this.name = name;
        this.successRate = successRate;
        this.baseMaxExp = baseMaxExp;
        this.atkBonus = atkBonus;
        this.defBonus = defBonus;
    }

    // === 手动添加 Getter 方法 (解决 "找不到符号" 报错) ===
    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public long getBaseMaxExp() {
        return baseMaxExp;
    }

    public int getAtkBonus() {
        return atkBonus;
    }

    public int getDefBonus() {
        return defBonus;
    }

    /**
     * 根据等级获取对应的大境界配置
     */
    public static RealmEnum getByLevel(int level) {
        int index = level / 10;
        for (RealmEnum realm : values()) {
            if (realm.index == index) {
                return realm;
            }
        }
        return ASCENSION; // 超过最高等级视为飞升
    }
}