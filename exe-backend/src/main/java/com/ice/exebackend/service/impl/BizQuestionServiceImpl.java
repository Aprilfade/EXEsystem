package com.ice.exebackend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.QuestionDTO;
import com.ice.exebackend.dto.QuestionExcelDTO;
import com.ice.exebackend.dto.QuestionPageParams;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizQuestionKnowledgePoint;
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.service.BizQuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException; // 【新增】 导入

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BizQuestionServiceImpl extends ServiceImpl<BizQuestionMapper, BizQuestion> implements BizQuestionService {

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Override
    @Transactional
    public boolean createQuestionWithKnowledgePoints(QuestionDTO questionDTO) {
        // 1. 保存试题基本信息
        this.save(questionDTO);

        // 2. 更新关联的知识点
        updateKnowledgePoints(questionDTO.getId(), questionDTO.getKnowledgePointIds());
        return true;
    }

    @Override
    @Transactional
    public boolean updateQuestionWithKnowledgePoints(QuestionDTO questionDTO) {
        // 1. 更新试题基本信息
        this.updateById(questionDTO);

        // 2. 更新关联的知识点
        updateKnowledgePoints(questionDTO.getId(), questionDTO.getKnowledgePointIds());
        return true;
    }

    @Override
    public QuestionDTO getQuestionWithKnowledgePointsById(Long id) {
        BizQuestion question = this.getById(id);
        if (question == null) {
            return null;
        }

        QuestionDTO dto = new QuestionDTO();
        BeanUtils.copyProperties(question, dto);

        List<BizQuestionKnowledgePoint> relations = questionKnowledgePointMapper.selectList(
                new QueryWrapper<BizQuestionKnowledgePoint>().eq("question_id", id)
        );

        List<Long> knowledgePointIds = relations.stream()
                .map(BizQuestionKnowledgePoint::getKnowledgePointId)
                .collect(Collectors.toList());

        dto.setKnowledgePointIds(knowledgePointIds);

        return dto;
    }

    /**
     * 私有辅助方法，用于更新试题与知识点的关联关系
     * @param questionId 试题ID
     * @param knowledgePointIds 新的知识点ID列表
     */
    private void updateKnowledgePoints(Long questionId, List<Long> knowledgePointIds) {
        // 1. 先删除该试题旧的所有关联关系
        questionKnowledgePointMapper.delete(new QueryWrapper<BizQuestionKnowledgePoint>().eq("question_id", questionId));

        // 2. 如果新的知识点列表不为空，则批量插入新的关联关系
        if (!CollectionUtils.isEmpty(knowledgePointIds)) {
            List<BizQuestionKnowledgePoint> newRelations = knowledgePointIds.stream().map(kpId -> {
                BizQuestionKnowledgePoint relation = new BizQuestionKnowledgePoint();
                relation.setQuestionId(questionId);
                relation.setKnowledgePointId(kpId);
                return relation;
            }).collect(Collectors.toList());

            // Mybatis-Plus没有批量插入，循环插入
            for(BizQuestionKnowledgePoint relation : newRelations) {
                questionKnowledgePointMapper.insert(relation);
            }
        }
    }
    @Override
    @Transactional
    public void importQuestions(MultipartFile file) throws IOException {
        List<QuestionExcelDTO> excelData = EasyExcel.read(file.getInputStream())
                .head(QuestionExcelDTO.class)
                .sheet()
                .doReadSync();

        for (QuestionExcelDTO dto : excelData) {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(dto, questionDTO);

            // 处理知识点ID
            if (StringUtils.hasText(dto.getKnowledgePointIds())) {
                List<Long> kpIds = Arrays.stream(dto.getKnowledgePointIds().split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                questionDTO.setKnowledgePointIds(kpIds);
            }

            this.createQuestionWithKnowledgePoints(questionDTO);
        }
    }
    @Override
    public List<QuestionExcelDTO> getQuestionsForExport(QuestionPageParams params) {
        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();
        // ... (此处省略与 Controller 中相同的查询条件构造逻辑) ...

        List<BizQuestion> questions = this.list(queryWrapper);

        // 将 BizQuestion 转换为 QuestionExcelDTO
        return questions.stream().map(q -> {
            QuestionDTO fullQuestion = this.getQuestionWithKnowledgePointsById(q.getId());
            QuestionExcelDTO dto = new QuestionExcelDTO();
            BeanUtils.copyProperties(fullQuestion, dto);

            if (!CollectionUtils.isEmpty(fullQuestion.getKnowledgePointIds())) {
                String kpIds = fullQuestion.getKnowledgePointIds().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                dto.setKnowledgePointIds(kpIds);
            }
            return dto;
        }).collect(Collectors.toList());
    }

}