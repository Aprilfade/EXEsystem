// src/main/java/com/ice/exebackend/service/impl/SysLoginLogServiceImpl.java
package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.SysLoginLog;
import com.ice.exebackend.mapper.SysLoginLogMapper;
import com.ice.exebackend.service.SysLoginLogService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public void recordLoginLog(String username, String logType, String ipAddress, String userAgent) {
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setLogType(logType);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setLogTime(LocalDateTime.now());
        this.save(log);
    }
}