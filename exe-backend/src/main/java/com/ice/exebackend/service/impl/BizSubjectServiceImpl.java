package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizSubject;
import com.ice.exebackend.mapper.BizSubjectMapper;
import com.ice.exebackend.service.BizSubjectService;
import org.springframework.stereotype.Service;

@Service
public class BizSubjectServiceImpl extends ServiceImpl<BizSubjectMapper, BizSubject> implements BizSubjectService {
    // 目前实现基础的增删改查即可
}