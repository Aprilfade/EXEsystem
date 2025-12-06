package com.ice.exebackend.enums;

import java.util.Arrays;

public enum SpiritRootEnum {
    METAL("金", "数学", "ATTACK"),
    WOOD("木", "语文", "HP"),
    WATER("水", "英语", "SPEED"),
    FIRE("火", "物理", "CRIT"), // 也可以映射 "理综"
    EARTH("土", "历史", "DEFENSE"); // 也可以映射 "文综"

    private final String elementName;
    private final String subjectKeyword; // 用于模糊匹配学科名
    private final String attributeEffect;

    SpiritRootEnum(String elementName, String subjectKeyword, String attributeEffect) {
        this.elementName = elementName;
        this.subjectKeyword = subjectKeyword;
        this.attributeEffect = attributeEffect;
    }

    public String getElementName() { return elementName; }
    public String getAttributeEffect() { return attributeEffect; }

    // 根据学科名称自动匹配灵根
    public static SpiritRootEnum matchBySubject(String subjectName) {
        if (subjectName == null) return null;
        for (SpiritRootEnum root : values()) {
            // 简单的包含匹配，比如 "高中数学" 包含 "数学"
            if (subjectName.contains(root.subjectKeyword)) {
                return root;
            }
        }
        // 默认或者是其他科目归为土系（包容万物）
        return EARTH;
    }
}