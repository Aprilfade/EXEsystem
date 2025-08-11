package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.SysNotification;
import com.ice.exebackend.mapper.SysNotificationMapper;
import com.ice.exebackend.service.SysNotificationService;
import org.springframework.stereotype.Service;

@Service
public class SysNotificationServiceImpl extends ServiceImpl<SysNotificationMapper, SysNotification> implements SysNotificationService {
}