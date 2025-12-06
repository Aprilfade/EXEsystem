package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_cultivation")
public class BizCultivation {
    // 【修复】指定 type = IdType.INPUT，因为主键是关联的学生ID，不是自动生成的
    @TableId(type = IdType.INPUT)
    private Long studentId;

    private Integer realmLevel;
    private Long currentExp;
    private Long maxExp;
    private Integer attack;
    private Integer defense;

    // 【新增】最大生命值字段
    private Integer maxHp;
    private LocalDateTime updateTime;
    // 【新增】上次结算时间
    private LocalDateTime lastSettlementTime;
    /**
     * 五行灵根数据 (JSON存储)
     * Key: 属性 (METAL, WOOD, WATER, FIRE, EARTH)
     * Value: 经验值 (Integer)
     */
    @TableField("spirit_roots")
    private String spiritRoots;


    // 【新增】maxHp 的 Getter 和 Setter
    public Integer getMaxHp() { return maxHp; }
    public void setMaxHp(Integer maxHp) { this.maxHp = maxHp; }

    public String getSpiritRoots() {
        return spiritRoots;
    }

    public void setSpiritRoots(String spiritRoots) {
        this.spiritRoots = spiritRoots;
    }


    // ... 手动 Getter/Setter (如果未启用 Lombok) ...
    public LocalDateTime getLastSettlementTime() { return lastSettlementTime; }
    public void setLastSettlementTime(LocalDateTime lastSettlementTime) { this.lastSettlementTime = lastSettlementTime; }

    // === 手动添加 Getter 和 Setter ===
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Integer getRealmLevel() { return realmLevel; }
    public void setRealmLevel(Integer realmLevel) { this.realmLevel = realmLevel; }
    public Long getCurrentExp() { return currentExp; }
    public void setCurrentExp(Long currentExp) { this.currentExp = currentExp; }
    public Long getMaxExp() { return maxExp; }
    public void setMaxExp(Long maxExp) { this.maxExp = maxExp; }
    public Integer getAttack() { return attack; }
    public void setAttack(Integer attack) { this.attack = attack; }
    public Integer getDefense() { return defense; }
    public void setDefense(Integer defense) { this.defense = defense; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}