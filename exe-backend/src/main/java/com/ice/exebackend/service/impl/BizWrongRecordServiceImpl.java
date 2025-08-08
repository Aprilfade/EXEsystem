package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.PaperStatsVO;
import com.ice.exebackend.dto.WrongRecordDTO;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.BizWrongRecord;
import com.ice.exebackend.mapper.BizWrongRecordMapper;
import com.ice.exebackend.service.BizWrongRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class BizWrongRecordServiceImpl extends ServiceImpl<BizWrongRecordMapper, BizWrongRecord> implements BizWrongRecordService {

    @Override
    @Transactional
    public boolean createWrongRecord(WrongRecordDTO dto) {
        if (dto == null || dto.getQuestionId() == null || CollectionUtils.isEmpty(dto.getStudentIds())) {
            return false;
        }
        for (Long studentId : dto.getStudentIds()) {
            BizWrongRecord record = new BizWrongRecord();
            record.setStudentId(studentId);
            record.setQuestionId(dto.getQuestionId());
            record.setPaperId(dto.getPaperId());
            record.setWrongReason(dto.getWrongReason());
            this.save(record);
        }
        return true;
    }

    @Override
    public Page<WrongRecordVO> getWrongRecordPage(Page<WrongRecordVO> page, Long studentId, Long questionId) {
        return this.baseMapper.selectWrongRecordPage(page, studentId, questionId);
    }

    @Override
    public List<PaperStatsVO> getPaperErrorStats(Long paperId) {
        return this.baseMapper.countErrorsByPaper(paperId);
    }
}