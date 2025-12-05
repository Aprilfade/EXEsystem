package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_cultivation")
public class BizCultivation {
    @TableId // 主键是 student_id，非自增
    private Long studentId;
    private Integer realmLevel;
    private Long currentExp;
    private Long maxExp;
    private Integer attack;
    private Integer defense;
    private LocalDateTime updateTime;

    // === 手动添加 Getter 和 Setter 以解决找不到符号的问题 ===

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Integer getRealmLevel() {
        return realmLevel;
    }

    public void setRealmLevel(Integer realmLevel) {
        this.realmLevel = realmLevel;
    }

    public Long getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(Long currentExp) {
        this.currentExp = currentExp;
    }

    public Long getMaxExp() {
        return maxExp;
    }

    public void setMaxExp(Long maxExp) {
        this.maxExp = maxExp;
    }

    public Integer getAttack() {
        return attack;
    }

    public void setAttack(Integer attack) {
        this.attack = attack;
    }

    public Integer getDefense() {
        return defense;
    }

    public void setDefense(Integer defense) {
        this.defense = defense;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}