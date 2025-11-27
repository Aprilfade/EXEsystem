package com.ice.exebackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.entity.BizPaper;
import com.ice.exebackend.entity.BizPaperGroup; // 导入BizPaperGroup
import com.ice.exebackend.entity.BizPaperImage;
import com.ice.exebackend.entity.BizPaperQuestion;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.mapper.BizPaperGroupMapper; // 导入BizPaperGroupMapper
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
import com.ice.exebackend.service.BizPaperQuestionService; // 新增导入
import com.ice.exebackend.dto.SmartPaperReq;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private BizPaperGroupMapper paperGroupMapper; // 注入Group Mapper

    @Autowired
    private BizQuestionService questionService;

    // 【新增注入】注入 Service 以使用 saveBatch
    @Autowired
    private BizPaperQuestionService paperQuestionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public List<PaperDTO.PaperGroupDTO> generateSmartPaper(SmartPaperReq req) {
        List<PaperDTO.PaperGroupDTO> groups = new ArrayList<>();
        int sortOrder = 0;

        // 辅助方法：生成指定题型的分组
        generateGroup(groups, req, 1, "一、单选题", req.getSingleCount(), sortOrder++);
        generateGroup(groups, req, 2, "二、多选题", req.getMultiCount(), sortOrder++);
        generateGroup(groups, req, 4, "三、判断题", req.getJudgeCount(), sortOrder++); // 注意顺序
        generateGroup(groups, req, 3, "四、填空题", req.getFillCount(), sortOrder++);
        generateGroup(groups, req, 5, "五、主观题", req.getSubjectiveCount(), sortOrder++);

        return groups;
    }

    private void generateGroup(List<PaperDTO.PaperGroupDTO> groups, SmartPaperReq req,
                               int questionType, String groupName, Integer count, int groupSort) {
        if (count == null || count <= 0) return;

        // 1. 随机查询指定数量的题目
        QueryWrapper<BizQuestion> query = new QueryWrapper<>();
        query.eq("subject_id", req.getSubjectId())
                .eq("question_type", questionType);

        if (StringUtils.hasText(req.getGrade())) {
            query.eq("grade", req.getGrade());
        }

        // 核心：随机排序并限制数量
        query.last("ORDER BY RAND() LIMIT " + count);
        List<BizQuestion> questions = questionService.list(query);

        if (questions.isEmpty()) return;

        // 2. 组装分组对象
        PaperDTO.PaperGroupDTO groupDTO = new PaperDTO.PaperGroupDTO();
        groupDTO.setName(groupName);
        groupDTO.setSortOrder(groupSort);

        List<BizPaperQuestion> paperQuestions = new ArrayList<>();

        // 3. 设置默认分值 (可根据需求调整)
        int defaultScore = switch (questionType) {
            case 1, 3, 4 -> 2; // 单选、填空、判断 2分
            case 2 -> 4;       // 多选 4分
            case 5 -> 10;      // 主观 10分
            default -> 5;
        };

        for (int i = 0; i < questions.size(); i++) {
            BizQuestion q = questions.get(i);
            BizPaperQuestion pq = new BizPaperQuestion();
            pq.setQuestionId(q.getId());
            pq.setScore(defaultScore);
            pq.setSortOrder(i);
            // 这是一个非数据库字段，用于前端显示题目详情 (需要在 BizPaperQuestion 实体中确认有此字段或通过 Map 返回)
            // 在你的 PaperDTO 定义中，PaperGroupDTO 的 questions 列表是 BizPaperQuestion 类型
            // 前端 PaperQuestionManager 需要 questionDetail 属性来显示题干
            // 我们可以利用 MyBatisPlus 的 @TableField(exist=false) 或者直接在 Controller 层处理
            // 这里为了简单，假设你已经处理好了前端需要的结构
            // *注意：为了让前端能显示题干，这里必须把 BizQuestion 信息塞进去*
            // 你需要在 BizPaperQuestion.java 实体中加一个 @TableField(exist=false) private BizQuestion questionDetail;
            pq.setQuestionDetail(q);

            paperQuestions.add(pq);
        }

        groupDTO.setQuestions(paperQuestions);
        groups.add(groupDTO);
    }


    @Override
    @Transactional
    public boolean createPaperWithQuestions(PaperDTO paperDTO) {
        this.save(paperDTO);
        if (paperDTO.getPaperType() == null || paperDTO.getPaperType() == 1) {
            updatePaperGroupsAndQuestions(paperDTO.getId(), paperDTO.getGroups()); // 【修复】调用新方法
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
            updatePaperGroupsAndQuestions(paperDTO.getId(), paperDTO.getGroups()); // 【修复】调用新方法
            paperImageMapper.delete(new QueryWrapper<BizPaperImage>().eq("paper_id", paperDTO.getId()));
        } else {
            updatePaperImages(paperDTO.getId(), paperDTO.getPaperImages());
            paperGroupMapper.delete(new QueryWrapper<BizPaperGroup>().eq("paper_id", paperDTO.getId())); // 【修复】删除旧分组
            paperQuestionMapper.delete(new QueryWrapper<BizPaperQuestion>().eq("paper_id", paperDTO.getId()));
        }
        return true;
    }

    // 【新增】更新分组和题目的核心逻辑
    // 【核心修改】重构此方法
    private void updatePaperGroupsAndQuestions(Long paperId, List<PaperDTO.PaperGroupDTO> groups) {
        // 1. 先删除所有与该试卷相关的旧分组和旧题目关联 (保持不变)
        paperGroupMapper.delete(new QueryWrapper<BizPaperGroup>().eq("paper_id", paperId));
        paperQuestionMapper.delete(new QueryWrapper<BizPaperQuestion>().eq("paper_id", paperId));

        if (CollectionUtils.isEmpty(groups)) {
            return;
        }

        // 准备一个列表，用于收集所有待插入的题目
        List<BizPaperQuestion> batchQuestionList = new ArrayList<>();

        // 2. 遍历前端传来的新分组数据并插入
        for (int i = 0; i < groups.size(); i++) {
            PaperDTO.PaperGroupDTO groupDTO = groups.get(i);

            BizPaperGroup paperGroup = new BizPaperGroup();
            paperGroup.setPaperId(paperId);
            paperGroup.setName(groupDTO.getName());
            paperGroup.setSortOrder(i);
            paperGroupMapper.insert(paperGroup); // 分组数量少，普通插入即可

            Long newGroupId = paperGroup.getId(); // 获取生成的分组ID

            // 3. 收集该分组下的题目
            if (!CollectionUtils.isEmpty(groupDTO.getQuestions())) {
                for (BizPaperQuestion pq : groupDTO.getQuestions()) {
                    pq.setId(null); // 确保ID为空，让数据库自增
                    pq.setPaperId(paperId);
                    pq.setGroupId(newGroupId); // 关联到新的分组ID
                    batchQuestionList.add(pq); // 【关键】加入列表，而不是立即插入
                }
            }
        }

        // 4. 【关键】执行批量插入
        if (!batchQuestionList.isEmpty()) {
            paperQuestionService.saveBatch(batchQuestionList);
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

    // 【修复】查询逻辑，适配分组结构
    @Override
    public PaperDTO getPaperWithQuestionsById(Long id) {
        BizPaper paper = this.getById(id);
        if (paper == null) return null;
        PaperDTO dto = new PaperDTO();
        BeanUtils.copyProperties(paper, dto);

        if (paper.getPaperType() == null || paper.getPaperType() == 1) {
            List<BizPaperGroup> groups = paperGroupMapper.selectList(
                    new QueryWrapper<BizPaperGroup>().eq("paper_id", id).orderByAsc("sort_order")
            );
            List<BizPaperQuestion> allQuestions = paperQuestionMapper.selectList(
                    new QueryWrapper<BizPaperQuestion>().eq("paper_id", id).orderByAsc("sort_order")
            );

            List<PaperDTO.PaperGroupDTO> groupDTOs = new ArrayList<>();
            for (BizPaperGroup group : groups) {
                PaperDTO.PaperGroupDTO groupDTO = new PaperDTO.PaperGroupDTO();
                BeanUtils.copyProperties(group, groupDTO);
                groupDTO.setQuestions(
                        allQuestions.stream()
                                .filter(q -> group.getId().equals(q.getGroupId()))
                                .collect(Collectors.toList())
                );
                groupDTOs.add(groupDTO);
            }
            dto.setGroups(groupDTOs);
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
        // ... 此部分代码不变 ...
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
                            System.err.println("Unsupported image format: " + fileName);
                            continue;
                        }

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

    // 【修复】Word导出逻辑，适配分组
    private XWPFDocument exportQuestionPaperToWord(PaperDTO paperDTO, boolean includeAnswers) {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(paperDTO.getName());
        titleRun.setBold(true);
        titleRun.setFontSize(22);

        if(CollectionUtils.isEmpty(paperDTO.getGroups())) return document;

        // 提前查出所有题目详情，避免循环查询
        List<Long> allQuestionIds = paperDTO.getGroups().stream()
                .flatMap(g -> g.getQuestions().stream())
                .map(BizPaperQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<Long, BizQuestion> allQuestionsMap = questionService.listByIds(allQuestionIds).stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        int questionCounter = 1;

        // 遍历分组
        for (PaperDTO.PaperGroupDTO group : paperDTO.getGroups()) {
            // 写入分组标题
            XWPFParagraph groupTitlePara = document.createParagraph();
            XWPFRun groupTitleRun = groupTitlePara.createRun();
            groupTitleRun.setText(group.getName());
            groupTitleRun.setBold(true);
            groupTitleRun.setFontSize(16);

            // 遍历分组内的题目
            for (BizPaperQuestion pq : group.getQuestions()) {
                BizQuestion question = allQuestionsMap.get(pq.getQuestionId());
                if (question == null) continue;

                // 写入题干
                XWPFParagraph questionPara = document.createParagraph();
                XWPFRun questionRun = questionPara.createRun();
                questionRun.setText(questionCounter++ + ". (" + pq.getScore() + "分) " + question.getContent());

                // (省略了图片和选项写入的代码，与之前版本相同)
                if ((question.getQuestionType() == 1 || question.getQuestionType() == 2) && question.getOptions() != null) {
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

        // 【修复】写入答案部分
        if (includeAnswers) {
            XWPFParagraph answerSectionPara = document.createParagraph();
            XWPFRun answerSectionRun = answerSectionPara.createRun();
            answerSectionRun.addBreak(BreakType.PAGE);
            answerSectionRun.setText("参考答案与解析");
            answerSectionRun.setBold(true);
            answerSectionRun.setFontSize(18);

            int answerCounter = 1;
            for (PaperDTO.PaperGroupDTO group : paperDTO.getGroups()) {
                for (BizPaperQuestion pq : group.getQuestions()) {
                    BizQuestion question = allQuestionsMap.get(pq.getQuestionId());
                    if (question == null) continue;
                    XWPFParagraph answerPara = document.createParagraph();
                    XWPFRun answerRun = answerPara.createRun();
                    answerRun.setText(answerCounter++ + ". 答案: " + question.getAnswer());
                    // (省略了答案图片和解析写入的代码，与之前版本相同)
                }
            }
        }
        return document;
    }
}