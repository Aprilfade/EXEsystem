package com.ice.exebackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.entity.BizPaper;
import com.ice.exebackend.entity.BizPaperImage;
import com.ice.exebackend.entity.BizPaperQuestion;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.mapper.BizPaperImageMapper;
import com.ice.exebackend.mapper.BizPaperMapper;
import com.ice.exebackend.mapper.BizPaperQuestionMapper;
import com.ice.exebackend.service.BizPaperService;
import com.ice.exebackend.service.BizQuestionService;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizPaperServiceImpl extends ServiceImpl<BizPaperMapper, BizPaper> implements BizPaperService {

    @Autowired
    private BizPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private BizPaperImageMapper paperImageMapper;

    @Autowired
    private BizQuestionService questionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public boolean createPaperWithQuestions(PaperDTO paperDTO) {
        this.save(paperDTO);
        if (paperDTO.getPaperType() == null || paperDTO.getPaperType() == 1) {
            updatePaperQuestions(paperDTO.getId(), paperDTO.getQuestions());
        } else {
            updatePaperImages(paperDTO.getId(), paperDTO.getPaperImages());
        }
        return true;
    }

    @Override
    @Transactional
    public boolean updatePaperWithQuestions(PaperDTO paperDTO) {
        this.updateById(paperDTO);
        if (paperDTO.getPaperType() == null || paperDTO.getPaperType() == 1) {
            updatePaperQuestions(paperDTO.getId(), paperDTO.getQuestions());
            paperImageMapper.delete(new QueryWrapper<BizPaperImage>().eq("paper_id", paperDTO.getId()));
        } else {
            updatePaperImages(paperDTO.getId(), paperDTO.getPaperImages());
            paperQuestionMapper.delete(new QueryWrapper<BizPaperQuestion>().eq("paper_id", paperDTO.getId()));
        }
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

    private void updatePaperImages(Long paperId, List<BizPaperImage> images) {
        paperImageMapper.delete(new QueryWrapper<BizPaperImage>().eq("paper_id", paperId));
        if (!CollectionUtils.isEmpty(images)) {
            for (int i = 0; i < images.size(); i++) {
                BizPaperImage pi = images.get(i);
                pi.setPaperId(paperId);
                pi.setSortOrder(i);
                paperImageMapper.insert(pi);
            }
        }
    }

    @Override
    public PaperDTO getPaperWithQuestionsById(Long id) {
        BizPaper paper = this.getById(id);
        if (paper == null) return null;
        PaperDTO dto = new PaperDTO();
        BeanUtils.copyProperties(paper, dto);
        if (paper.getPaperType() == null || paper.getPaperType() == 1) {
            List<BizPaperQuestion> relations = paperQuestionMapper.selectList(
                    new QueryWrapper<BizPaperQuestion>().eq("paper_id", id).orderByAsc("sort_order")
            );
            dto.setQuestions(relations);
        } else {
            List<BizPaperImage> images = paperImageMapper.selectList(
                    new QueryWrapper<BizPaperImage>().eq("paper_id", id).orderByAsc("sort_order")
            );
            dto.setPaperImages(images);
        }
        return dto;
    }

    @Override
    public XWPFDocument exportPaperToWord(Long id, boolean includeAnswers) {
        PaperDTO paperDTO = this.getPaperWithQuestionsById(id);
        if (paperDTO == null) return new XWPFDocument();
        if (paperDTO.getPaperType() != null && paperDTO.getPaperType() == 2) {
            return exportImagePaperToWord(paperDTO);
        } else {
            return exportQuestionPaperToWord(paperDTO, includeAnswers);
        }
    }

    private XWPFDocument exportImagePaperToWord(PaperDTO paperDTO) {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(paperDTO.getName());
        titleRun.setBold(true);
        titleRun.setFontSize(22);

        if (CollectionUtils.isEmpty(paperDTO.getPaperImages())) {
            return document;
        }

        for (BizPaperImage paperImage : paperDTO.getPaperImages()) {
            try {
                String fileName = paperImage.getImageUrl().substring(paperImage.getImageUrl().lastIndexOf("/") + 1);
                Path imagePath = Paths.get(uploadDir, fileName);

                if (Files.exists(imagePath)) {
                    try (InputStream is = new FileInputStream(imagePath.toFile())) {
                        XWPFParagraph p = document.createParagraph();
                        p.setAlignment(ParagraphAlignment.CENTER);
                        XWPFRun run = p.createRun();

                        // --- 【核心修复】根据文件后缀动态判断图片类型 ---
                        int format;
                        if (fileName.toLowerCase().endsWith(".emf")) {
                            format = XWPFDocument.PICTURE_TYPE_EMF;
                        } else if (fileName.toLowerCase().endsWith(".wmf")) {
                            format = XWPFDocument.PICTURE_TYPE_WMF;
                        } else if (fileName.toLowerCase().endsWith(".pict")) {
                            format = XWPFDocument.PICTURE_TYPE_PICT;
                        } else if (fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".jpg")) {
                            format = XWPFDocument.PICTURE_TYPE_JPEG;
                        } else if (fileName.toLowerCase().endsWith(".png")) {
                            format = XWPFDocument.PICTURE_TYPE_PNG;
                        } else {
                            // 默认或不支持的格式，可以跳过或记录日志
                            System.err.println("Unsupported image format: " + fileName);
                            continue;
                        }
                        // --- 【修复结束】 ---

                        run.addPicture(is, format, fileName, Units.toEMU(450), Units.toEMU(600));
                        run.addBreak(BreakType.PAGE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return document;
    }

    private XWPFDocument exportQuestionPaperToWord(PaperDTO paperDTO, boolean includeAnswers) {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(paperDTO.getName());
        titleRun.setBold(true);
        titleRun.setFontSize(22);

        List<Long> questionIds = paperDTO.getQuestions().stream()
                .map(BizPaperQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<Long, BizQuestion> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        Map<Integer, List<BizPaperQuestion>> groupedQuestions = paperDTO.getQuestions().stream()
                .sorted(Comparator.comparing(BizPaperQuestion::getSortOrder))
                .collect(Collectors.groupingBy(pq -> questionMap.get(pq.getQuestionId()).getQuestionType()));

        String[] typeNames = {"", "一、单选题", "二、多选题", "三、填空题", "四、判断题", "五、主观题"};
        int questionCounter = 1;

        for (Map.Entry<Integer, List<BizPaperQuestion>> entry : groupedQuestions.entrySet()) {
            Integer questionType = entry.getKey();
            List<BizPaperQuestion> paperQuestions = entry.getValue();
            XWPFParagraph typeTitleParagraph = document.createParagraph();
            XWPFRun typeTitleRun = typeTitleParagraph.createRun();
            typeTitleRun.setText(typeNames[questionType]);
            typeTitleRun.setBold(true);
            typeTitleRun.setFontSize(16);

            for (BizPaperQuestion pq : paperQuestions) {
                BizQuestion question = questionMap.get(pq.getQuestionId());
                if (question == null) continue;
                XWPFParagraph questionPara = document.createParagraph();
                XWPFRun questionRun = questionPara.createRun();
                questionRun.setText(questionCounter++ + ". (" + pq.getScore() + "分) " + question.getContent());

                if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
                    try {
                        String fileName = question.getImageUrl().substring(question.getImageUrl().lastIndexOf("/") + 1);
                        Path imagePath = Paths.get(uploadDir, fileName);
                        if (Files.exists(imagePath)) {
                            try (InputStream is = new FileInputStream(imagePath.toFile())) {
                                int format = fileName.toLowerCase().endsWith(".png") ? XWPFDocument.PICTURE_TYPE_PNG : XWPFDocument.PICTURE_TYPE_JPEG;
                                questionRun.addPicture(is, format, fileName, Units.toEMU(400), Units.toEMU(300));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        questionPara.createRun().setText("\n[图片加载失败: " + question.getImageUrl() + "]");
                    }
                }

                if ((questionType == 1 || questionType == 2) && question.getOptions() != null) {
                    List<Map> options = JSON.parseArray(question.getOptions(), Map.class);
                    for (Map<String, String> option : options) {
                        XWPFParagraph optionPara = document.createParagraph();
                        optionPara.setIndentationLeft(360);
                        XWPFRun optionRun = optionPara.createRun();
                        optionRun.setText(option.get("key") + ". " + option.get("value"));
                    }
                }
                document.createParagraph().createRun().addBreak();
            }
        }

        if (includeAnswers) {
            XWPFParagraph answerSectionPara = document.createParagraph();
            XWPFRun answerSectionRun = answerSectionPara.createRun();
            answerSectionRun.addBreak(BreakType.PAGE);
            answerSectionRun.setText("参考答案与解析");
            answerSectionRun.setBold(true);
            answerSectionRun.setFontSize(18);
            int answerCounter = 1;
            for (BizPaperQuestion pq : paperDTO.getQuestions().stream().sorted(Comparator.comparing(BizPaperQuestion::getSortOrder)).collect(Collectors.toList())) {
                BizQuestion question = questionMap.get(pq.getQuestionId());
                if (question == null) continue;
                XWPFParagraph answerPara = document.createParagraph();
                XWPFRun answerRun = answerPara.createRun();
                answerRun.setText(answerCounter++ + ". 答案: " + question.getAnswer());

                if (question.getAnswerImageUrl() != null && !question.getAnswerImageUrl().isEmpty()) {
                    try {
                        String fileName = question.getAnswerImageUrl().substring(question.getAnswerImageUrl().lastIndexOf("/") + 1);
                        Path imagePath = Paths.get(uploadDir, fileName);
                        if (Files.exists(imagePath)) {
                            try (InputStream is = new FileInputStream(imagePath.toFile())) {
                                answerRun.addBreak();
                                int format = fileName.toLowerCase().endsWith(".png") ? XWPFDocument.PICTURE_TYPE_PNG : XWPFDocument.PICTURE_TYPE_JPEG;
                                answerRun.addPicture(is, format, fileName, Units.toEMU(300), Units.toEMU(200));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        answerPara.createRun().setText("\n[答案图片加载失败: " + question.getAnswerImageUrl() + "]");
                    }
                }
                answerRun.addBreak();
                answerRun.setText("   解析: " + (question.getDescription() != null ? question.getDescription() : "无"));
                answerRun.addBreak();
            }
        }
        return document;
    }
}