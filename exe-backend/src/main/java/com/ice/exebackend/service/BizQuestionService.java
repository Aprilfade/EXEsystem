package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.QuestionDTO;
import com.ice.exebackend.dto.QuestionExcelDTO;
import com.ice.exebackend.dto.QuestionPageParams;
import com.ice.exebackend.entity.BizQuestion;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException; // 【新增】 导入
import java.util.List; // 【新增】 导入

import java.util.List;

public interface BizQuestionService extends IService<BizQuestion> {

    /**
     * 创建试题及其与知识点的关联
     * @param questionDTO 包含试题信息和知识点ID列表的DTO
     * @return 是否创建成功
     */
    boolean createQuestionWithKnowledgePoints(QuestionDTO questionDTO);

    /**
     * 更新试题及其与知识点的关联
     * @param questionDTO 包含试题信息和知识点ID列表的DTO
     * @return 是否更新成功
     */
    boolean updateQuestionWithKnowledgePoints(QuestionDTO questionDTO);

    /**
     * 根据ID获取试题详情，包含关联的知识点ID
     * @param id 试题ID
     * @return QuestionDTO
     */
    QuestionDTO getQuestionWithKnowledgePointsById(Long id);
    // 【新增】导入试题
    void importQuestions(MultipartFile file) throws IOException;

    // 【新增】根据查询条件导出试题
    List<QuestionExcelDTO> getQuestionsForExport(QuestionPageParams params);
}