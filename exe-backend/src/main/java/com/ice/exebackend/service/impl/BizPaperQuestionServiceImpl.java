package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizPaperQuestion;
import com.ice.exebackend.mapper.BizPaperQuestionMapper;
import com.ice.exebackend.service.BizPaperQuestionService;
import org.springframework.stereotype.Service;

@Service
public class BizPaperQuestionServiceImpl extends ServiceImpl<BizPaperQuestionMapper, BizPaperQuestion> implements BizPaperQuestionService {
}