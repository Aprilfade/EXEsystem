package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BizQuestionMapper extends BaseMapper<BizQuestion> {
    // 【新增】根据学生和科目，获取其错题记录对应的知识点ID列表
    List<Long> selectWrongKnowledgePointIds(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    // 【新增】根据知识点ID列表获取题目ID列表，并排除已做错的题目

    List<Long> selectQuestionsByKnowledgePoints(@Param("kpIds") List<Long> kpIds, @Param("studentId") Long studentId, @Param("limit") int limit);
    // 【新增】随机获取一道该学生的错题（用于心魔）
    BizQuestion selectRandomWrongQuestion(@Param("studentId") Long studentId);

    // 【新增】如果没错题，随机获取一道普通题目
    BizQuestion selectRandomQuestion();



}