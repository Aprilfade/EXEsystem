package com.ice.exebackend.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizBattleRecord;
import com.ice.exebackend.mapper.BizBattleRecordMapper;
import com.ice.exebackend.service.BizBattleRecordService;
import org.springframework.stereotype.Service;

@Service
public class BizBattleRecordServiceImpl extends ServiceImpl<BizBattleRecordMapper, BizBattleRecord> implements BizBattleRecordService {}