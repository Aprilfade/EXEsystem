package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizLearningActivity;
import com.ice.exebackend.mapper.BizLearningActivityMapper;
import com.ice.exebackend.service.BizLearningActivityService;
import org.springframework.stereotype.Service;

@Service // 【关键】使用 @Service 注解，让 Spring 能够识别它
public class BizLearningActivityServiceImpl extends ServiceImpl<BizLearningActivityMapper, BizLearningActivity> implements BizLearningActivityService {
    // MyBatis-Plus 的大部分基础 CRUD 操作已由 ServiceImpl 自动实现，此处无需额外代码
}