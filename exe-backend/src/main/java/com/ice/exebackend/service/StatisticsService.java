package com.ice.exebackend.service;

import com.ice.exebackend.dto.KnowledgePointErrorStatsDTO;
import com.ice.exebackend.dto.PaperDifficultyDTO;
import com.ice.exebackend.dto.StudentAbilityDTO;
import java.util.List;

public interface StatisticsService {
    List<KnowledgePointErrorStatsDTO> getKnowledgePointErrorStats();
    StudentAbilityDTO getStudentAbilityStats(Long studentId);
    PaperDifficultyDTO getPaperDifficultyStats(Long paperId);
}