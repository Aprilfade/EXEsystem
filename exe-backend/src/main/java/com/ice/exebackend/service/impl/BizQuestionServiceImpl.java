package com.ice.exebackend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.QuestionBatchUpdateDTO;
import com.ice.exebackend.dto.QuestionDTO;
import com.ice.exebackend.dto.QuestionExcelDTO;
import com.ice.exebackend.dto.QuestionPageParams;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizQuestionKnowledgePoint;
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.service.BizQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException; // 【新增】 导入

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizQuestionServiceImpl extends ServiceImpl<BizQuestionMapper, BizQuestion> implements BizQuestionService {

    private static final Logger log = LoggerFactory.getLogger(BizQuestionServiceImpl.class);

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
    /**
     * 【优化后】Excel 导入试题
     * 使用 ReadListener 实现分批处理，防止大文件导致 OOM
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importQuestions(MultipartFile file) throws IOException {
        // 定义每批处理的数量
        final int BATCH_COUNT = 100;

        // 使用匿名内部类实现 ReadListener（也可以抽离成单独的类）
        EasyExcel.read(file.getInputStream(), QuestionExcelDTO.class, new ReadListener<QuestionExcelDTO>() {

            // 缓存数据的 List，大小固定为 BATCH_COUNT，防止扩容消耗性能
            private List<QuestionExcelDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

            @Override
            public void invoke(QuestionExcelDTO data, AnalysisContext context) {
                cachedDataList.add(data);
                // 达到 BATCH_COUNT 了，需要去存储一次了，防止数据几万条数据在内存，容易OOM
                if (cachedDataList.size() >= BATCH_COUNT) {
                    saveData();
                    // 存储完成清理 list
                    cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 这里也要 save 一次，确保最后遗留的数据也存储到数据库
                saveData();
            }

            /**
             * 将缓存的数据保存到数据库
             * 注意：这里调用的是外部类的 saveSingleQuestion 方法
             */
            private void saveData() {
                if (CollectionUtils.isEmpty(cachedDataList)) {
                    return;
                }

                // 遍历当前批次的数据进行处理
                for (QuestionExcelDTO dto : cachedDataList) {
                    // 这里复用你原有的转换和保存逻辑
                    // 建议将转换逻辑抽离成一个小方法，或者直接写在这里
                    processAndSaveSingle(dto);
                }

                // 提示：如果你想进一步优化数据库性能，可以将 createQuestionWithKnowledgePoints
                // 改造成支持 List<QuestionDTO> 的批量插入，但这涉及两张表的操作，逻辑会复杂一些。
                // 目前这个方案已经解决了 OOM 问题。
            }
        }).sheet().doRead();
    }

    /**
     * 辅助方法：处理单个 DTO 的转换并保存
     */
    private void processAndSaveSingle(QuestionExcelDTO dto) {
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(dto, questionDTO);

        // 处理知识点ID字符串转 List<Long>
        if (StringUtils.hasText(dto.getKnowledgePointIds())) {
            try {
                List<Long> kpIds = Arrays.stream(dto.getKnowledgePointIds().split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                questionDTO.setKnowledgePointIds(kpIds);
            } catch (NumberFormatException e) {
                // 【修改点】这里改成了 dto.getContent()
                System.err.println("试题导入格式错误，跳过该条，Question: " + dto.getContent());
                // log.error("试题导入格式错误，跳过该条，Question: {}", dto.getContent(), e);
                return;
            }
        }

        this.createQuestionWithKnowledgePoints(questionDTO);
    }
    @Override
    public List<QuestionExcelDTO> getQuestionsForExport(QuestionPageParams params) {
        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();

        // ✅ 添加查询条件过滤
        if (params.getSubjectId() != null) {
            queryWrapper.eq("subject_id", params.getSubjectId());
        }
        if (params.getQuestionType() != null) {
            queryWrapper.eq("question_type", params.getQuestionType());
        }
        if (StringUtils.hasText(params.getGrade())) {
            queryWrapper.eq("grade", params.getGrade());
        }
        if (StringUtils.hasText(params.getContent())) {
            queryWrapper.like("content", params.getContent());
        }
        queryWrapper.orderByDesc("id");

        List<BizQuestion> questions = this.list(queryWrapper);

        // ✅ 优化：批量加载知识点关联，避免 N+1 查询
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 收集所有试题 ID
        List<Long> questionIds = questions.stream()
                .map(BizQuestion::getId)
                .collect(Collectors.toList());

        // 2. 批量查询所有试题的知识点关联关系
        List<BizQuestionKnowledgePoint> allRelations = questionKnowledgePointMapper.selectList(
                new QueryWrapper<BizQuestionKnowledgePoint>().in("question_id", questionIds)
        );

        // 3. 构建 questionId -> List<KnowledgePointId> 的映射
        Map<Long, List<Long>> questionKpMap = allRelations.stream()
                .collect(Collectors.groupingBy(
                        BizQuestionKnowledgePoint::getQuestionId,
                        Collectors.mapping(BizQuestionKnowledgePoint::getKnowledgePointId, Collectors.toList())
                ));

        // 4. 转换为 QuestionExcelDTO
        return questions.stream().map(q -> {
            QuestionExcelDTO dto = new QuestionExcelDTO();
            BeanUtils.copyProperties(q, dto);

            // 从映射中获取知识点 ID 列表
            List<Long> kpIds = questionKpMap.getOrDefault(q.getId(), Collections.emptyList());
            if (!kpIds.isEmpty()) {
                String kpIdsStr = kpIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                dto.setKnowledgePointIds(kpIdsStr);
            }
            return dto;
        }).collect(Collectors.toList());
    }
    /**
     * 【新增】实现批量更新试题的方法
     */
    @Override
    @Transactional
    public boolean batchUpdateQuestions(QuestionBatchUpdateDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getQuestionIds())) {
            return false; // 如果没有提供试题ID，则不执行任何操作
        }

        // 至少要提供一个新的科目或年级
        if (dto.getSubjectId() == null && !StringUtils.hasText(dto.getGrade())) {
            return false;
        }

        LambdaUpdateWrapper<BizQuestion> updateWrapper = new LambdaUpdateWrapper<>();
        // 设置更新条件：IN (id1, id2, id3...)
        updateWrapper.in(BizQuestion::getId, dto.getQuestionIds());

        // 动态设置要更新的字段
        boolean hasUpdate = false;
        if (dto.getSubjectId() != null) {
            updateWrapper.set(BizQuestion::getSubjectId, dto.getSubjectId());
            hasUpdate = true;
        }
        if (StringUtils.hasText(dto.getGrade())) {
            updateWrapper.set(BizQuestion::getGrade, dto.getGrade());
            hasUpdate = true;
        }

        if(hasUpdate) {
            return this.update(updateWrapper);
        }

        return false;
    }


}