package com.ice.exebackend.dto;

import com.ice.exebackend.entity.SysRole;
// 不再依赖 @Data，所以可以不导入 lombok.Data
// import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// @Data // 我们可以暂时不用 @Data 注解，完全手动实现
public class UserInfoDTO {
    private Long id;
    private String username;
    private String nickName;
    private Integer isEnabled;
    private LocalDateTime createTime;
    private List<SysRole> roles;
    private Integer status;

    // --- 手动为所有字段添加 getter 和 setter 方法 ---

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(List<SysRole> roles) {
        this.roles = roles;
    }
}