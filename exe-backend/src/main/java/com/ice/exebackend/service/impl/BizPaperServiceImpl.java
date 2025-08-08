package com.ice.exebackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.entity.BizPaper;
import com.ice.exebackend.entity.BizPaperQuestion;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.mapper.BizPaperMapper;
import com.ice.exebackend.mapper.BizPaperQuestionMapper;
import com.ice.exebackend.service.BizPaperService;
import com.ice.exebackend.service.BizQuestionService;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizPaperServiceImpl extends ServiceImpl<BizPaperMapper, BizPaper> implements BizPaperService {

    @Autowired
    private BizPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private BizQuestionService questionService;

    @Override
    @Transactional
    public boolean createPaperWithQuestions(PaperDTO paperDTO) {
        this.save(paperDTO);
        updatePaperQuestions(paperDTO.getId(), paperDTO.getQuestions());
        return true;
    }

    @Override
    @Transactional
    public boolean updatePaperWithQuestions(PaperDTO paperDTO) {
        this.updateById(paperDTO);
        updatePaperQuestions(paperDTO.getId(), paperDTO.getQuestions());
        return true;
    }

    private void updatePaperQuestions(Long paperId, List<BizPaperQuestion> questions) {
        paperQuestionMapper.delete(new QueryWrapper<BizPaperQuestion>().eq("paper_id", paperId));
        if (!CollectionUtils.isEmpty(questions)) {
            for (BizPaperQuestion pq : questions) {
                pq.setPaperId(paperId);
                paperQuestionMapper.insert(pq);
            }
        }
    }


    @Override
    public PaperDTO getPaperWithQuestionsById(Long id) {
        BizPaper paper = this.getById(id);
        if (paper == null) return null;

        PaperDTO dto = new PaperDTO();
        BeanUtils.copyProperties(paper, dto);

        List<BizPaperQuestion> relations = paperQuestionMapper.selectList(
                new QueryWrapper<BizPaperQuestion>().eq("paper_id", id)
        );
        dto.setQuestions(relations);
        return dto;
    }

    @Override
    public XWPFDocument exportPaperToWord(Long id, boolean includeAnswers) {
        PaperDTO paperDTO = this.getPaperWithQuestionsById(id);
        if (paperDTO == null) return new XWPFDocument();

        // 1. 获取所有相关的试题详情
        List<Long> questionIds = paperDTO.getQuestions().stream()
                .map(BizPaperQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<Long, BizQuestion> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        // 2. 创建Word文档
        XWPFDocument document = new XWPFDocument();

        // 3. 写入试卷标题
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(paperDTO.getName());
        titleRun.setBold(true);
        titleRun.setFontSize(22);

        // 4. 按题型分组
        Map<Integer, List<BizPaperQuestion>> groupedQuestions = paperDTO.getQuestions().stream()
                .sorted(Comparator.comparing(BizPaperQuestion::getSortOrder))
                .collect(Collectors.groupingBy(pq -> questionMap.get(pq.getQuestionId()).getQuestionType()));

        String[] typeNames = {"", "一、单选题", "二、多选题", "三、填空题", "四、判断题", "五、主观题"};
        int questionCounter = 1;

        // 5. 遍历题型并写入
        for (Map.Entry<Integer, List<BizPaperQuestion>> entry : groupedQuestions.entrySet()) {
            Integer questionType = entry.getKey();
            List<BizPaperQuestion> paperQuestions = entry.getValue();

            // 写入题型标题
            XWPFParagraph typeTitleParagraph = document.createParagraph();
            XWPFRun typeTitleRun = typeTitleParagraph.createRun();
            typeTitleRun.setText(typeNames[questionType]);
            typeTitleRun.setBold(true);
            typeTitleRun.setFontSize(16);

            // 写入该题型下的所有题目
            for (BizPaperQuestion pq : paperQuestions) {
                BizQuestion question = questionMap.get(pq.getQuestionId());
                if (question == null) continue;

                // 写入题干
                XWPFParagraph questionPara = document.createParagraph();
                XWPFRun questionRun = questionPara.createRun();
                questionRun.setText(questionCounter++ + ". (" + pq.getScore() + "分) " + question.getContent());

                // 写入选项 (如果是选择题)
                if (questionType == 1 || questionType == 2) {
                    List<Map> options = JSON.parseArray(question.getOptions(), Map.class);
                    for(Map<String, String> option : options) {
                        XWPFParagraph optionPara = document.createParagraph();
                        optionPara.setIndentationLeft(360); // 缩进
                        XWPFRun optionRun = optionPara.createRun();
                        optionRun.setText(option.get("key") + ". " + option.get("value"));
                    }
                }

                document.createParagraph().createRun().addBreak(); // 添加空行
            }
        }

        if (includeAnswers) {
            XWPFParagraph answerSectionPara = document.createParagraph();
            XWPFRun answerSectionRun = answerSectionPara.createRun();
            answerSectionRun.addBreak(BreakType.PAGE); // 新起一页
            answerSectionRun.setText("参考答案与解析");
            answerSectionRun.setBold(true);
            answerSectionRun.setFontSize(18);

            int answerCounter = 1;
            for (BizPaperQuestion pq : paperDTO.getQuestions()) {
                BizQuestion question = questionMap.get(pq.getQuestionId());
                if (question == null) continue;

                XWPFParagraph answerPara = document.createParagraph();
                XWPFRun answerRun = answerPara.createRun();
                answerRun.setText(answerCounter++ + ". 答案: " + question.getAnswer());
                answerRun.addBreak();
                answerRun.setText("   解析: " + (question.getDescription() != null ? question.getDescription() : "无"));
                answerRun.addBreak();
            }
        }


        return document;
    }
}