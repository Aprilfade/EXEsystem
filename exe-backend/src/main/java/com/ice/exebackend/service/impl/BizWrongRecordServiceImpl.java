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

import java.util.Collections;
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

    // 【修改】 getWrongRecordPage 方法，增加过滤is_mastered的条件
    @Override
    public Page<WrongRecordVO> getWrongRecordPage(Page<WrongRecordVO> page, Long studentId, Long questionId) {
        return this.baseMapper.selectWrongRecordPage(page, studentId, questionId);
    }

    @Override
    public List<PaperStatsVO> getPaperErrorStats(Long paperId) {
        return this.baseMapper.countErrorsByPaper(paperId);
    }

    // 【新增】 获取错题详情
    @Override
    public WrongRecordVO getWrongRecordDetail(Long recordId, Long studentId) {
        return this.baseMapper.selectWrongRecordDetail(recordId, studentId);
    }

    // 【新增】 标记错题为已掌握
    @Override
    public boolean markAsMastered(Long recordId, Long studentId) {
        return this.lambdaUpdate()
                .eq(BizWrongRecord::getId, recordId)
                .eq(BizWrongRecord::getStudentId, studentId)
                .set(BizWrongRecord::getIsMastered, 1)
                .update();
    }
    @Override
    public List<Long> getTopErrorKnowledgePointIds(Long studentId, Long subjectId, int limit) {
        // 确保你已经注入了 BizQuestionKnowledgePointMapper 和 BizKnowledgePointMapper
        // 这里只是一个简化的逻辑，在实际项目中需要更复杂的SQL联表查询
        return Collections.emptyList(); // 这里暂时返回空列表，实际逻辑需要实现
    }
}

