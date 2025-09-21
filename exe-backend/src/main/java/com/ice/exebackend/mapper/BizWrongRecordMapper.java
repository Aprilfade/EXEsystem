package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.dto.PaperStatsVO;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.BizWrongRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select; // 【新增】

import java.util.List;

@Mapper
public interface BizWrongRecordMapper extends BaseMapper<BizWrongRecord> {

    Page<WrongRecordVO> selectWrongRecordPage(Page<WrongRecordVO> page,
                                              @Param("studentId") Long studentId,
                                              @Param("questionId") Long questionId);

    List<PaperStatsVO> countErrorsByPaper(@Param("paperId") Long paperId);

    // 【新增】查询单个错题记录的详细信息
    @Select("SELECT wr.id, q.id as questionId, q.content, q.image_url, q.options, q.answer, q.description " +
            "FROM biz_wrong_record wr " +
            "JOIN biz_question q ON wr.question_id = q.id " +
            "WHERE wr.id = #{recordId} AND wr.student_id = #{studentId}")
    WrongRecordVO selectWrongRecordDetail(@Param("recordId") Long recordId, @Param("studentId") Long studentId);
}