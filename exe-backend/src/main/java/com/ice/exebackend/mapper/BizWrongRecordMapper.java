package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.dto.PaperStatsVO;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.BizWrongRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizWrongRecordMapper extends BaseMapper<BizWrongRecord> {

    Page<WrongRecordVO> selectWrongRecordPage(Page<WrongRecordVO> page,
                                              @Param("studentId") Long studentId,
                                              @Param("questionId") Long questionId);

    List<PaperStatsVO> countErrorsByPaper(@Param("paperId") Long paperId);


    WrongRecordVO selectWrongRecordDetail(@Param("recordId") Long recordId, @Param("studentId") Long studentId);
}