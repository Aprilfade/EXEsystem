package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.StudentKnowledgeMasteryDTO;
import com.ice.exebackend.entity.BizStudentKnowledgeMastery;

import java.util.List;
import java.util.Map;

/**
 * 学生知识点掌握度服务接口
 *
 * @author ice
 * @since 2026-01-08
 */
public interface BizStudentKnowledgeMasteryService extends IService<BizStudentKnowledgeMastery> {

    /**
     * 考试后更新学生的知识点掌握度
     *
     * @param studentId     学生ID
     * @param examResultId  考试结果ID
     */
    void updateMasteryAfterExam(Long studentId, Long examResultId);

    /**
     * 获取学生所有知识点的掌握度
     *
     * @param studentId 学生ID
     * @return 知识点掌握度列表
     */
    List<StudentKnowledgeMasteryDTO> getStudentMastery(Long studentId);

    /**
     * 获取学生某科目下的知识点掌握度
     *
     * @param studentId 学生ID
     * @param subjectId 科目ID
     * @return 知识点掌握度列表
     */
    List<StudentKnowledgeMasteryDTO> getStudentMasteryBySubject(Long studentId, Long subjectId);

    /**
     * 获取学生知识点掌握度雷达图数据
     *
     * @param studentId 学生ID
     * @param subjectId 科目ID（可选）
     * @return 雷达图数据
     */
    Map<String, Object> getStudentMasteryRadar(Long studentId, Long subjectId);
}
