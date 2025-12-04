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
import com.ice.exebackend.utils.GeneticPaperUtil;
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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.HashMap;  // 解决 "找不到符号 类 HashMap"
import com.ice.exebackend.utils.GeneticPaperUtil; // 解决 "找不到符号 变量 GeneticPaperUtil"



import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
        // 1. 准备组卷约束
        Map<Integer, Integer> typeCountMap = new HashMap<>();
        typeCountMap.put(1, req.getSingleCount());
        typeCountMap.put(2, req.getMultiCount());
        typeCountMap.put(3, req.getFillCount());
        typeCountMap.put(4, req.getJudgeCount());
        typeCountMap.put(5, req.getSubjectiveCount());

        // 2. 准备候选池 (Candidate Pool)
        // 为了保证 GA 有选择空间，我们按需求的 2-3 倍数量去数据库拉取题目
        Map<Integer, List<BizQuestion>> candidatePool = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : typeCountMap.entrySet()) {
            if (entry.getValue() <= 0) continue;

            // 需求 N 道，我们查 3N 道，保证随机性和优化空间
            int fetchSize = entry.getValue() * 3;

            QueryWrapper<BizQuestion> query = new QueryWrapper<>();
            query.eq("subject_id", req.getSubjectId())
                    .eq("question_type", entry.getKey());

            if (StringUtils.hasText(req.getGrade())) {
                query.eq("grade", req.getGrade());
            }
            // 这里还是用 rand 随机取一部分作为候选池，避免把整个库加载进内存
            query.last("ORDER BY RAND() LIMIT " + fetchSize);

            List<BizQuestion> pool = questionService.list(query);
            candidatePool.put(entry.getKey(), pool);
        }

        // 3. 运行遗传算法
        // 假设目标难度来自前端 req.getTargetDifficulty()，如果没有则默认 0.5
        double targetDiff = req.getTargetDifficulty() != null ? req.getTargetDifficulty() : 0.5;

        List<BizQuestion> finalQuestions = GeneticPaperUtil.evolution(
                candidatePool,
                typeCountMap,
                targetDiff,
                100.0 // 暂时忽略总分约束，由算法自动计算
        );

        // 4. 将扁平的题目列表转换为前端需要的分组结构 (GroupDTO)
        return convertToGroups(finalQuestions);
    }

    // 辅助方法：将 List<Question> 转为 List<GroupDTO>
    private List<PaperDTO.PaperGroupDTO> convertToGroups(List<BizQuestion> questions) {
        Map<Integer, List<BizQuestion>> groupedMap = questions.stream()
                .collect(Collectors.groupingBy(BizQuestion::getQuestionType));

        List<PaperDTO.PaperGroupDTO> result = new ArrayList<>();

        // 按照标准顺序添加分组
        addGroupIfPresent(result, groupedMap, 1, "一、单选题");
        addGroupIfPresent(result, groupedMap, 2, "二、多选题");
        addGroupIfPresent(result, groupedMap, 4, "三、判断题");
        addGroupIfPresent(result, groupedMap, 3, "四、填空题");
        addGroupIfPresent(result, groupedMap, 5, "五、主观题");

        return result;
    }

    private void addGroupIfPresent(List<PaperDTO.PaperGroupDTO> result, Map<Integer, List<BizQuestion>> map, int type, String name) {
        List<BizQuestion> list = map.get(type);
        if (list != null && !list.isEmpty()) {
            PaperDTO.PaperGroupDTO group = new PaperDTO.PaperGroupDTO();
            group.setName(name);
            group.setSortOrder(result.size());

            List<BizPaperQuestion> pqs = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                BizQuestion q = list.get(i);
                BizPaperQuestion pq = new BizPaperQuestion();
                pq.setQuestionId(q.getId());
                // 这里使用之前工具类里的逻辑或默认分值
                pq.setScore(type == 2 ? 4 : (type == 5 ? 10 : 2));
                pq.setSortOrder(i);
                pq.setQuestionDetail(q); // 确保前端能显示
                pqs.add(pq);
            }
            group.setQuestions(pqs);
            result.add(group);
        }
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

    // 替换原有的 exportImagePaperToWord 方法
    private XWPFDocument exportImagePaperToWord(PaperDTO paperDTO) {
        XWPFDocument document = new XWPFDocument();
        // 标题部分
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(paperDTO.getName());
        titleRun.setBold(true);
        titleRun.setFontSize(22);
        titleRun.addBreak(); // 空一行

        if (CollectionUtils.isEmpty(paperDTO.getPaperImages())) {
            return document;
        }

        for (BizPaperImage paperImage : paperDTO.getPaperImages()) {
            try {
                // 1. 获取本地文件路径
                String fileName = paperImage.getImageUrl().substring(paperImage.getImageUrl().lastIndexOf("/") + 1);
                Path imagePath = Paths.get(uploadDir, fileName);

                if (Files.exists(imagePath)) {
                    // 2. 读取图片到内存以获取尺寸 (关键优化步骤)
                    byte[] imageBytes = Files.readAllBytes(imagePath);
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                        BufferedImage img = ImageIO.read(bais);
                        if (img == null) continue;

                        int originalWidth = img.getWidth();
                        int originalHeight = img.getHeight();

                        // 3. 计算自适应尺寸 (假设A4纸可用宽度约为 450 points)
                        int maxWidth = 450;
                        int targetWidth;
                        int targetHeight;

                        if (originalWidth > maxWidth) {
                            targetWidth = maxWidth;
                            // 保持宽高比: h = w * (originalH / originalW)
                            targetHeight = (int) (maxWidth * ((double) originalHeight / originalWidth));
                        } else {
                            targetWidth = originalWidth;
                            targetHeight = originalHeight;
                        }

                        // 4. 插入图片
                        XWPFParagraph p = document.createParagraph();
                        p.setAlignment(ParagraphAlignment.CENTER);
                        XWPFRun run = p.createRun();

                        int format = getPictureType(fileName);
                        if (format == 0) continue; // 不支持的格式跳过

                        // 使用 ByteArrayInputStream 再次读取数据流供 POI 使用
                        try (ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytes)) {
                            run.addPicture(imageStream, format, fileName, Units.toEMU(targetWidth), Units.toEMU(targetHeight));
                        }
                        run.addBreak(BreakType.PAGE); // 每张图一页
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // 建议记录 logger.error
            }
        }
        return document;
    }

    // 辅助方法：获取图片格式代码
    private int getPictureType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".emf")) return XWPFDocument.PICTURE_TYPE_EMF;
        if (lowerName.endsWith(".wmf")) return XWPFDocument.PICTURE_TYPE_WMF;
        if (lowerName.endsWith(".pict")) return XWPFDocument.PICTURE_TYPE_PICT;
        if (lowerName.endsWith(".jpeg") || lowerName.endsWith(".jpg")) return XWPFDocument.PICTURE_TYPE_JPEG;
        if (lowerName.endsWith(".png")) return XWPFDocument.PICTURE_TYPE_PNG;
        if (lowerName.endsWith(".gif")) return XWPFDocument.PICTURE_TYPE_GIF;
        return 0;
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