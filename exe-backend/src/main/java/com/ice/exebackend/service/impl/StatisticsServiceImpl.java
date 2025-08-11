package com.ice.exebackend.service.impl;

import com.ice.exebackend.dto.KnowledgePointErrorStatsDTO;
import com.ice.exebackend.dto.PaperDifficultyDTO;
import com.ice.exebackend.dto.StudentAbilityDTO;
import com.ice.exebackend.mapper.StatisticsMapper;
import com.ice.exebackend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public List<KnowledgePointErrorStatsDTO> getKnowledgePointErrorStats() {
        return statisticsMapper.getKnowledgePointErrorStats();
    }

    @Override
    public StudentAbilityDTO getStudentAbilityStats(Long studentId) {
        StudentAbilityDTO dto = new StudentAbilityDTO();
        List<String> labels = new ArrayList<>();
        List<Double> errorRates = new ArrayList<>();

        // 1. 按题型统计
        List<Map<String, Object>> typeErrors = statisticsMapper.getStudentErrorRateByQuestionType(studentId);
        Map<Integer, Long> typeErrorMap = typeErrors.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("questionType")).intValue(),
                        m -> ((Number) m.get("errorCount")).longValue()
                ));

        String[] typeNames = {"", "单选题", "多选题", "填空题", "判断题", "主观题"};
        for(int i = 1; i <= 5; i++) {
            labels.add(typeNames[i]);
            // 此处错误率是简化的，更精确的需要知道总答题数，这里暂时用错误次数代替
            errorRates.add(typeErrorMap.getOrDefault(i, 0L).doubleValue());
        }

        // 2. 按知识点统计
        List<KnowledgePointErrorStatsDTO> kpErrors = statisticsMapper.getStudentErrorRateByKnowledgePoint(studentId);
        for(KnowledgePointErrorStatsDTO kp : kpErrors) {
            labels.add(kp.getKnowledgePointName());
            errorRates.add(kp.getTotalErrors().doubleValue());
        }

        dto.setRadarLabels(labels);
        dto.setErrorRates(errorRates); // 注意：这里是错误次数，不是错误率
        dto.setErrorsByKnowledgePoint(kpErrors);

        return dto;
    }

    @Override
    public PaperDifficultyDTO getPaperDifficultyStats(Long paperId) {
        return statisticsMapper.getPaperDifficultyStats(paperId);
    }
}