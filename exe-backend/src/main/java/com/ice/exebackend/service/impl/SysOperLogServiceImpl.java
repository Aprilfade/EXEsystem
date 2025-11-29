package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.SysOperLog;
import com.ice.exebackend.mapper.SysOperLogMapper;
import com.ice.exebackend.service.SysOperLogService;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {}