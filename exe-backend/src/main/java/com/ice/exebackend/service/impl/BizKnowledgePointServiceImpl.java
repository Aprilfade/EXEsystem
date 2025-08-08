package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.mapper.BizKnowledgePointMapper;
import com.ice.exebackend.service.BizKnowledgePointService;
import org.springframework.stereotype.Service;

@Service
public class BizKnowledgePointServiceImpl extends ServiceImpl<BizKnowledgePointMapper, BizKnowledgePoint> implements BizKnowledgePointService {
    // 目前实现基础的增删改查即可
}