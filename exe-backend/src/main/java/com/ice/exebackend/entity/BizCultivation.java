package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private LocalDateTime updateTime;

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