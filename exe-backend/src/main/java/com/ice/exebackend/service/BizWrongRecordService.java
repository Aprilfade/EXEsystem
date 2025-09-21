package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.PaperStatsVO;
import com.ice.exebackend.dto.WrongRecordDTO;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.BizWrongRecord;

import java.util.List;

public interface BizWrongRecordService extends IService<BizWrongRecord> {
    boolean createWrongRecord(WrongRecordDTO dto);
    Page<WrongRecordVO> getWrongRecordPage(Page<WrongRecordVO> page, Long studentId, Long questionId);
    List<PaperStatsVO> getPaperErrorStats(Long paperId);
    // 【新增】根据学生和科目，获取出错最多的知识点ID列表
    List<Long> getTopErrorKnowledgePointIds(Long studentId, Long subjectId, int limit);
    // 【新增】根据学生ID和错题ID获取错题详情（用于重练）
    WrongRecordVO getWrongRecordDetail(Long recordId, Long studentId);

    // 【新增】标记错题为已掌握
    boolean markAsMastered(Long recordId, Long studentId);
}
