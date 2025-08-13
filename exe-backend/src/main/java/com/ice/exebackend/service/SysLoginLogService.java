// src/main/java/com/ice/exebackend/service/SysLoginLogService.java
package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.SysLoginLog;

public interface SysLoginLogService extends IService<SysLoginLog> {
    void recordLoginLog(String username, String logType, String ipAddress, String userAgent);
}