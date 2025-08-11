package com.ice.exebackend.mapper;

import com.ice.exebackend.dto.KnowledgePointErrorStatsDTO;
import com.ice.exebackend.dto.PaperDifficultyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {
    List<KnowledgePointErrorStatsDTO> getKnowledgePointErrorStats();
    List<Map<String, Object>> getStudentErrorRateByQuestionType(@Param("studentId") Long studentId);
    List<KnowledgePointErrorStatsDTO> getStudentErrorRateByKnowledgePoint(@Param("studentId") Long studentId);
    PaperDifficultyDTO getPaperDifficultyStats(@Param("paperId") Long paperId);
}