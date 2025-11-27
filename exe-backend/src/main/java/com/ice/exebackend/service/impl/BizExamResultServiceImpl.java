package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.mapper.BizExamResultMapper;
import com.ice.exebackend.service.BizExamResultService;
import org.springframework.stereotype.Service;

@Service
public class BizExamResultServiceImpl extends ServiceImpl<BizExamResultMapper, BizExamResult> implements BizExamResultService {
}