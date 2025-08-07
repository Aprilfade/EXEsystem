package com.ice.exebackend.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;

@Data
@TableName("sys_user") // 映射数据库表
public class SysUser {

    @TableId(value = "id", type = IdType.AUTO) // 声明主键及自增策略
    private Long id;

    private String username; // 登录用户名

    private String password; // 加密后的密码

    private String nickName; // 用户昵称

    private Integer isEnabled; // 账户是否启用

    private Integer isDeleted; // 逻辑删除标记

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 手动添加setId方法，确保编译通过
    public void setId(Long id) {
        this.id = id;
    }

    // 手动添加其他getter和setter方法，确保编译通过
    public Long getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}
