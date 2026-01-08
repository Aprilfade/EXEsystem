package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.ExamResultDetailDTO;
import com.ice.exebackend.dto.KnowledgePointScoreAnalysisDTO;
import com.ice.exebackend.dto.ScoreExportDTO;
import com.ice.exebackend.dto.ScoreStatsDTO;
import com.ice.exebackend.entity.BizExamResult;

import java.util.List;
import java.util.Map;

public interface BizExamResultService extends IService<BizExamResult> {
    /**
     * 分页查询成绩（包含学生、班级、科目等关联信息）
     */
    Page<ExamResultDetailDTO> getExamResultsWithDetails(int current, int size, Map<String, Object> params);

    /**
     * 获取成绩统计数据
     */
    ScoreStatsDTO getScoreStats(Map<String, Object> params);

    /**
     * 获取学生成绩趋势
     */
    List<Map<String, Object>> getStudentScoreTrend(Long studentId, Long subjectId);

    /**
     * 获取导出数据
     */
    List<ScoreExportDTO> getExportData(Map<String, Object> params);

    /**
     * 批量删除成绩
     */
    boolean batchDelete(List<Long> ids);

    /**
     * 批量发布成绩
     */
    boolean batchPublish(List<Long> ids, Boolean published);

    /**
     * 【知识点功能增强】获取考试成绩的知识点分析
     * @param examResultId 考试成绩ID
     * @return 知识点分析列表
     */
    List<KnowledgePointScoreAnalysisDTO> getKnowledgePointAnalysis(Long examResultId);
}