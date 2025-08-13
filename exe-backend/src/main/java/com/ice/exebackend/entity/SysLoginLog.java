// src/main/java/com/ice/exebackend/entity/SysLoginLog.java
package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String username;
    private String ipAddress;
    private String logType; // LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT
    private String userAgent;
    private LocalDateTime logTime;

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
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
                this.ipAddress = ipAddress;
    }
    public String getLogType() {
        return logType;
    }
    public void setLogType(String logType) {
        this.logType = logType;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public LocalDateTime getLogTime() {
        return logTime;
    }
    public void setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
    }
}