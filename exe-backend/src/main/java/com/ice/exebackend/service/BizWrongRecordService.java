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
}