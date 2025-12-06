package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ice.exebackend.mapper.BizQuestionMapper; // 【新增】导入 Mapper

import java.time.LocalDateTime; // 【新增】
import com.ice.exebackend.entity.BizExamResult; // 【新增】
import com.alibaba.fastjson.JSON; // 确保引入 fastjson 或使用 Jackson
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;




import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/student")
public class StudentDataController {

    // 添加日志记录器（如果还没有的话）
    private static final Logger logger = LoggerFactory.getLogger(StudentDataController.class);

    @Autowired
    private ObjectMapper objectMapper; // 【新增】注入 Jackson 工具类

    @Autowired
    private BizStudentService studentService;

    @Autowired
    private BizWrongRecordService wrongRecordService;

    @Autowired
    private BizSubjectService subjectService;

    @Autowired
    private BizQuestionService questionService;

    @Autowired
    private BizQuestionMapper questionMapper; // 【修改】直接注入 Mapper 接口

    @Autowired
    private BizLearningActivityService learningActivityService; //

    @Autowired
    private com.ice.exebackend.service.BizPaperService paperService;

    @Autowired
    private BizBattleRecordService battleRecordService;
    @Autowired
    private com.ice.exebackend.service.CultivationService cultivationService; // 新增注


    @Autowired
    private BizAchievementService achievementService; // 注入
    // 1. 注入新的 Service
    @Autowired
    private BizExamResultService examResultService;

    @Autowired
    private AiService aiService; // 注入 AiService

    @Value("${file.upload-dir}")
    private String uploadDir;


    /**
     * 学生端：获取我自己的错题记录
     */
    @GetMapping("/my-wrong-records")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getMyWrongRecords(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("当前用户不是一个有效的学生");
        }

        Page<WrongRecordVO> page = new Page<>(current, size);
        wrongRecordService.getWrongRecordPage(page, student.getId(), null);

        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 学生端专属的文件上传接口
     */
    @PostMapping("/files/upload")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("上传失败，请选择文件");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            Path filePath = Paths.get(uploadDir, newFileName);
            Files.copy(file.getInputStream(), filePath);
            String fileUrl = "/api/v1/files/" + newFileName;
            return Result.suc(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有可供练习的科目列表
     */
    @GetMapping("/subjects")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getPracticeSubjects() {
        List<BizSubject> subjects = subjectService.list();
        return Result.suc(subjects);
    }

    /**
     * 学生端：获取一套练习题（新增 mode 参数）
     */
    @GetMapping("/practice-questions")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getPracticeQuestions(
            @RequestParam Long subjectId,
            @RequestParam String grade,
            @RequestParam(required = false) String mode, // 【新增】可选的出题模式参数
            Authentication authentication
    ) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("当前用户不是一个有效的学生");
        }

        // 先根据ID找到科目实体
        BizSubject subject = subjectService.getById(subjectId);
        if (subject == null) {
            return Result.fail("科目不存在");
        }

        List<BizQuestion> questions;
        QueryWrapper<BizQuestion> baseQueryWrapper = new QueryWrapper<>();
        baseQueryWrapper.inSql("subject_id", "SELECT id FROM biz_subject WHERE name = '" + subject.getName() + "'");
        if (StringUtils.hasText(grade)) {
            baseQueryWrapper.eq("grade", grade);
        }

        // 【核心修改】根据 mode 参数决定出题逻辑
        if ("smart".equalsIgnoreCase(mode)) {
            // 智能出题逻辑：优先从错题关联的知识点中出题
            // 【修改】将调用从 service.baseMapper 调整为直接调用 mapper
            List<Long> wrongKpIds = questionMapper.selectWrongKnowledgePointIds(student.getId(), subjectId);
            List<Long> questionIds = new ArrayList<>();
            // 如果存在错题知识点，先从中抽取7道题目
            if (!wrongKpIds.isEmpty()) {
                // 【修改】将调用从 service.baseMapper 调整为直接调用 mapper
                questionIds = questionMapper.selectQuestionsByKnowledgePoints(wrongKpIds, student.getId(), 7);
            }
            // 如果题目不足10道，再随机补充
            int remainingNeeded = 10 - questionIds.size();
            if (remainingNeeded > 0) {
                QueryWrapper<BizQuestion> remainingQuery = new QueryWrapper<>();
                remainingQuery.inSql("subject_id", "SELECT id FROM biz_subject WHERE name = '" + subject.getName() + "'");
                if (StringUtils.hasText(grade)) {
                    remainingQuery.eq("grade", grade);
                }
                if (!questionIds.isEmpty()) {
                    remainingQuery.notIn("id", questionIds);
                }
                remainingQuery.last("ORDER BY RAND() LIMIT " + remainingNeeded);
                List<BizQuestion> remainingQuestions = questionService.list(remainingQuery);
                questions = remainingQuestions;
                // 将之前选出的题目合并进来
                questions.addAll(questionService.listByIds(questionIds));
            } else {
                questions = questionService.listByIds(questionIds);
            }
        } else {
            // 默认随机出题
            baseQueryWrapper.last("ORDER BY RAND() LIMIT 10");
            questions = questionService.list(baseQueryWrapper);
        }

        // 依然不返回答案
        questions.forEach(q -> {
            q.setAnswer(null);
            q.setDescription(null);
        });

        return Result.suc(questions);
    }

    /**
     * 学生提交练习
     */
    @PostMapping("/submit-practice")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result submitPractice(@RequestBody PracticeSubmissionDTO submission, Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("学生不存在");
        }

        List<Long> questionIds = new ArrayList<>(submission.getAnswers().keySet());
        if (questionIds.isEmpty()) {
            return Result.suc(new PracticeResultDTO());
        }

        // 获取正确答案映射
        Map<Long, BizQuestion> correctAnswersMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        PracticeResultDTO resultDTO = new PracticeResultDTO();
        List<PracticeResultDTO.AnswerResult> answerResults = new ArrayList<>();
        int correctCount = 0;

        // 判题逻辑
        for (Long questionId : questionIds) {
            BizQuestion question = correctAnswersMap.get(questionId);
            String userAnswer = submission.getAnswers().get(questionId);

            if (question == null) continue;

            PracticeResultDTO.AnswerResult answerResult = new PracticeResultDTO.AnswerResult();
            boolean isCorrect = userAnswer != null && userAnswer.equalsIgnoreCase(question.getAnswer());

            if (isCorrect) {
                correctCount++;
            } else {
                // 记录错题
                BizWrongRecord wrongRecord = new BizWrongRecord();
                wrongRecord.setStudentId(student.getId());
                wrongRecord.setQuestionId(questionId);
                wrongRecord.setWrongAnswer(userAnswer);
                wrongRecord.setWrongReason("在线练习错误");
                // 避免重复插入未掌握的错题
                if (wrongRecordService.lambdaQuery()
                        .eq(BizWrongRecord::getStudentId, student.getId())
                        .eq(BizWrongRecord::getQuestionId, questionId)
                        .eq(BizWrongRecord::getIsMastered, 0)
                        .count() == 0) {
                    wrongRecordService.save(wrongRecord);
                }
            }
            answerResult.setQuestion(question);
            answerResult.setUserAnswer(userAnswer);
            answerResult.setCorrect(isCorrect);
            answerResults.add(answerResult);
        }

        // --- 1. 记录学习活动日志 ---
        BizLearningActivity log = new BizLearningActivity();
        log.setStudentId(student.getId());
        log.setActivityType("PRACTICE_SUBMIT");
        log.setDescription("完成了在线练习，共" + questionIds.size() + "道题，答对" + correctCount + "题");
        log.setCreateTime(LocalDateTime.now());
        learningActivityService.save(log);

        // --- 2. 【核心修改】修仙挂钩：计算并发放修为 ---
        int expGain = 10 + (correctCount * 10);
        cultivationService.addExp(student.getId(), expGain);

        // ================== 【新增】灵根经验结算 START ==================
        // 只有答对题目才增加灵根经验
        // 【修改前】 if (correctCount > 0 && !results.isEmpty()) {
        // 【修改后】 ↓↓↓
        if (correctCount > 0 && !answerResults.isEmpty()) {

            // 假设一次练习的所有题目属于同一个科目（取第一题的科目即可）
            // 注意：results 中包含 AnswerResult，AnswerResult 中包含 BizQuestion
            // 【修改前】 BizQuestion firstQ = results.get(0).getQuestion();
            // 【修改后】 ↓↓↓
            BizQuestion firstQ = answerResults.get(0).getQuestion();

            if (firstQ != null && firstQ.getSubjectId() != null) {
                BizSubject subject = subjectService.getById(firstQ.getSubjectId());
                if (subject != null) {
                    // 练习模式：每答对1题增加 2 点灵根经验
                    int spiritExp = correctCount * 2;
                    cultivationService.addSpiritRootExp(student.getId(), subject.getName(), spiritExp);
                }
            }
        }
        // ================== 【新增】灵根经验结算 END ==================
        // --- 3. 【核心修改】发放积分 ---
        int pointsGain = 5; // 练习固定奖励 5 积分
        student.setPoints((student.getPoints() == null ? 0 : student.getPoints()) + pointsGain);
        studentService.updateById(student);

        // --- 4. 【核心修改】组装返回结果 ---
        resultDTO.setTotalQuestions(questionIds.size());
        resultDTO.setCorrectCount(correctCount);
        resultDTO.setResults(answerResults);

        // 关键点：将奖励数值传回前端，以便前端 Practice.vue 弹出提示
        // 请确保 PracticeResultDTO 已添加这两个字段
        resultDTO.setExpGain(expGain);
        resultDTO.setPointsGain(pointsGain);

        return Result.suc(resultDTO);
    }

    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getDashboardStats(Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("当前用户不是一个有效的学生");
        }
        StudentDashboardStatsDTO stats = studentService.getStudentDashboardStats(student.getId());
        return Result.suc(stats);
    }
    /**
     * 【新增】学生端：获取单个错题的详细信息
     */
    @GetMapping("/wrong-records/detail/{id}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getWrongRecordDetail(@PathVariable Long id, Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("当前用户不是一个有效的学生");
        }
        WrongRecordVO detail = wrongRecordService.getWrongRecordDetail(id, student.getId());
        return Result.suc(detail);
    }

    /**
     * 【新增】学生端：标记错题为已掌握
     */
    @PostMapping("/wrong-records/{id}/mastered")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result markWrongRecordAsMastered(@PathVariable Long id, Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("当前用户不是一个有效的学生");
        }
        boolean success = wrongRecordService.markAsMastered(id, student.getId());
        return success ? Result.suc("标记成功") : Result.fail("标记失败");
    }
    /**
     * 【新增】获取学生最新的学习活动日志
     */
    @GetMapping("/learning-activities")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getLearningActivities(Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("当前用户不是一个有效的学生");
        }
        List<BizLearningActivity> activities = learningActivityService.lambdaQuery()
                .eq(BizLearningActivity::getStudentId, student.getId())
                .orderByDesc(BizLearningActivity::getCreateTime)
                .last("LIMIT 5") // 只取最新的5条
                .list();
        return Result.suc(activities);
    }
    // --- 模拟考试功能模块 ---

    /**
     * 1. 获取学生所属年级的试卷列表
     */
    @GetMapping("/papers")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getStudentPapers(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) return Result.fail("学生信息不存在");

        Page<BizPaper> page = new Page<>(current, size);
        QueryWrapper<BizPaper> queryWrapper = new QueryWrapper<>();
        // 1. 【新增】只查询已发布的试卷 (status = 1)
        queryWrapper.eq("status", 1);
        // 筛选当前学生年级的试卷
        if (StringUtils.hasText(student.getGrade())) {
            queryWrapper.eq("grade", student.getGrade());
        }
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        // 只查询手动组卷的试卷(paper_type=1)或图片试卷(paper_type=2)
        // 这里假设模拟考主要针对手动组卷，如果支持图片试卷需前端配合
        queryWrapper.orderByDesc("create_time");

        studentService.page(new Page<>(), new QueryWrapper<>()); // dummy call to avoid import unused warning if necessary
        // 使用 paperService 查询
        // 注意：这里需要注入 BizPaperService，请确保类中已注入
        // 临时解决方案：直接使用 service 层或 mapper 查，假设 PaperService 已注入到 Controller (如果没有请添加)
        // 修正：StudentDataController 中没有注入 BizPaperService，我们需要先注入它。
        // 请在类头部添加: @Autowired private com.ice.exebackend.service.BizPaperService paperService;
        // 这里我假设你已经加了，或者我直接用 mapper 查也行，但最好用 service。

        // 由于上下文限制，我直接在这里模拟调用，请确保你在类中注入了 paperService
        // 假设类中没有，我用反射或假设你添加了。*请在类定义处添加 @Autowired private BizPaperService paperService;* // 下面代码基于已注入 paperService 编写

        return Result.suc(paperService.page(page, queryWrapper));
    }

    /**
     * 2. 获取考试试卷详情 (核心：不返回答案和解析)
     */
    @GetMapping("/exam/{paperId}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getExamPaperDetail(@PathVariable Long paperId) {
        // 1. 获取试卷完整结构
        com.ice.exebackend.dto.PaperDTO paperDTO = paperService.getPaperWithQuestionsById(paperId);
        if (paperDTO == null) return Result.fail("试卷不存在");

        // 2. 填充题目详细信息 (因为 PaperDTO 里的 questionDetail 默认可能是空的)
        if (paperDTO.getGroups() != null) {
            // 收集所有题目ID
            List<Long> allQIds = paperDTO.getGroups().stream()
                    .flatMap(g -> g.getQuestions().stream())
                    .map(com.ice.exebackend.entity.BizPaperQuestion::getQuestionId)
                    .collect(Collectors.toList());

            if (!allQIds.isEmpty()) {
                Map<Long, BizQuestion> qMap = questionService.listByIds(allQIds).stream()
                        .collect(Collectors.toMap(BizQuestion::getId, q -> q));

                // 3. 遍历并填充，同时擦除敏感字段 (答案)
                for (com.ice.exebackend.dto.PaperDTO.PaperGroupDTO group : paperDTO.getGroups()) {
                    for (com.ice.exebackend.entity.BizPaperQuestion pq : group.getQuestions()) {
                        BizQuestion fullQ = qMap.get(pq.getQuestionId());
                        if (fullQ != null) {
                            // 克隆对象或新建对象以避免修改缓存中的原对象
                            BizQuestion safeQ = new BizQuestion();
                            BeanUtils.copyProperties(fullQ, safeQ);
                            // !!! 关键：清空答案和解析 !!!
                            safeQ.setAnswer(null);
                            safeQ.setDescription(null);
                            safeQ.setAnswerImageUrl(null);

                            // 动态扩充 BizPaperQuestion 类太麻烦，我们可以封装一个 Map 返回
                            // 或者利用 MybatisPlus 的机制。
                            // 最简单的方案：前端获取 PaperDTO 后，题目详情其实不在 BizPaperQuestion 实体里。
                            // 我们需要把处理后的 questions 列表单独返回，或者前端根据 ID 再去查 (不推荐)。
                            // 这里我们采用：修改返回结构，将题目详情 Map 返回给前端
                        }
                    }
                }
                // 修正策略：为了简单起见，我们直接返回一个包含 PaperDTO 和 QuestionMap 的对象
                // 但为了安全，我们必须在后端构建一个完全脱敏的数据结构。

                // 构建脱敏后的题目列表 Map<Long, Question>
                Map<Long, BizQuestion> safeQuestionMap = qMap.values().stream().map(q -> {
                    BizQuestion safeQ = new BizQuestion();
                    BeanUtils.copyProperties(q, safeQ);
                    safeQ.setAnswer(null);
                    safeQ.setDescription(null);
                    safeQ.setAnswerImageUrl(null);
                    return safeQ;
                }).collect(Collectors.toMap(BizQuestion::getId, q -> q));

                return Result.suc(Map.of("paper", paperDTO, "questions", safeQuestionMap));
            }
        }
        return Result.suc(Map.of("paper", paperDTO));
    }
    /**
     * 3. 提交试卷 (集成 AI 批改主观题并持久化存储)
     */
    @PostMapping("/exam/submit")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result submitExam(@RequestBody PracticeSubmissionDTO submission,
                             @RequestParam Long paperId,
                             HttpServletRequest request,
                             Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        // 获取试卷详情
        com.ice.exebackend.dto.PaperDTO paper = paperService.getPaperWithQuestionsById(paperId);
        if (paper == null) return Result.fail("试卷不存在");

        // 获取 AI 配置 (从前端传来的 Header 中读取)
        String aiKey = request.getHeader("X-Ai-Api-Key");
        String aiProvider = request.getHeader("X-Ai-Provider");

        int totalScore = 0;
        int studentScore = 0;
        List<PracticeResultDTO.AnswerResult> results = new ArrayList<>();

        // 【新增】用于收集每道题的 AI 分析结果，Key 是题目 ID
        // 结构: QuestionID -> { "score": 5, "feedback": "...", "reason": "..." }
        Map<Long, Map<String, Object>> analysisResultMap = new java.util.HashMap<>();

        // === 区分试卷类型 ===
        if (paper.getPaperType() != null && paper.getPaperType() == 2) {
            // --- 图片试卷处理逻辑 (保持不变) ---
            totalScore = paper.getTotalScore() != null ? paper.getTotalScore() : 100;
            studentScore = 0;
        } else {
            // --- 传统试卷处理逻辑 ---

            // 获取所有题目真实信息用于比对
            List<Long> allQIds = new ArrayList<>(submission.getAnswers().keySet());
            if (paper.getGroups() != null) {
                paper.getGroups().forEach(g -> g.getQuestions().forEach(pq -> allQIds.add(pq.getQuestionId())));
            }

            if (!allQIds.isEmpty()) {
                List<BizQuestion> dbQuestions = questionService.listByIds(allQIds);
                Map<Long, BizQuestion> dbQMap = dbQuestions.stream().collect(Collectors.toMap(BizQuestion::getId, q -> q));

                if (paper.getGroups() != null) {
                    for (com.ice.exebackend.dto.PaperDTO.PaperGroupDTO group : paper.getGroups()) {
                        for (com.ice.exebackend.entity.BizPaperQuestion pq : group.getQuestions()) {
                            BizQuestion q = dbQMap.get(pq.getQuestionId());
                            if (q == null) continue;

                            totalScore += pq.getScore();
                            String userAns = submission.getAnswers().get(pq.getQuestionId());

                            boolean isCorrect = false;
                            int awardedScore = 0; // 本题实际得分

                            // 如果用户作答了且题目有答案
                            if (userAns != null && q.getAnswer() != null) {

                                // === 【核心逻辑】AI 批改主观题 ===
                                if (q.getQuestionType() == 5) {
                                    // 仅当配置了 Key 且用户有作答时才调用 AI
                                    if (StringUtils.hasText(aiKey) && StringUtils.hasText(userAns)) {
                                        try {
                                            // 调用 AI 服务进行批改
                                            AiGradingResult aiResult = aiService.gradeSubjectiveQuestion(
                                                    aiKey, aiProvider,
                                                    q.getContent(), q.getAnswer(), userAns, pq.getScore()
                                            );

                                            awardedScore = aiResult.getScore();
                                            // 简单判定：得分超过满分60%算“正确”
                                            isCorrect = awardedScore >= (pq.getScore() * 0.6);

                                            String feedback = aiResult.getFeedback();
                                            if (aiResult.getReason() != null) {
                                                feedback += "\n(扣分原因: " + aiResult.getReason() + ")";
                                            }

                                            // 1. 临时修改用于本次立即返回给前端展示
                                            String aiComment = "\n\n【AI 智能点评】\n" + feedback;
                                            q.setDescription((q.getDescription() == null ? "" : q.getDescription()) + aiComment);

                                            // 2. 【关键】保存到 Map 中以便存入数据库
                                            Map<String, Object> detail = new java.util.HashMap<>();
                                            detail.put("score", awardedScore);
                                            detail.put("feedback", feedback);
                                            analysisResultMap.put(q.getId(), detail);

                                        } catch (Exception e) {
                                            logger.error("AI 批改失败", e);
                                            awardedScore = 0;
                                            isCorrect = false;
                                            q.setDescription((q.getDescription() == null ? "" : q.getDescription()) + "\n\n(AI 服务连接超时或异常，未能完成批改)");
                                        }
                                    } else {
                                        // 未配置 Key 或未作答，给 0 分
                                        awardedScore = 0;
                                        isCorrect = false;
                                    }
                                }
                                // === 客观题判分逻辑 (保持不变) ===
                                else if (q.getQuestionType() == 2) {
                                    String sortedUser = sortString(userAns);
                                    String sortedDb = sortString(q.getAnswer());
                                    isCorrect = sortedUser.equalsIgnoreCase(sortedDb);
                                    if(isCorrect) awardedScore = pq.getScore();
                                } else {
                                    isCorrect = userAns.trim().equalsIgnoreCase(q.getAnswer().trim());
                                    if(isCorrect) awardedScore = pq.getScore();
                                }
                            }

                            // 累加总分
                            studentScore += awardedScore;

                            // 构建结果对象
                            PracticeResultDTO.AnswerResult res = new PracticeResultDTO.AnswerResult();
                            res.setQuestion(q); // 此时 q.description 可能已包含 AI 评语
                            res.setUserAnswer(userAns);
                            res.setCorrect(isCorrect);
                            res.setEarnedScore(awardedScore);
                            results.add(res);
                        }
                    }
                }
            }
        }

        // --- 保存考试结果 ---
        BizExamResult examResult = new BizExamResult();
        examResult.setStudentId(student.getId());
        examResult.setPaperId(paperId);
        examResult.setPaperName(paper.getName());
        examResult.setScore(studentScore);
        examResult.setTotalScore(totalScore);
        examResult.setViolationCount(submission.getViolationCount() != null ? submission.getViolationCount() : 0);

        try {
            // 保存用户答案
            Map<Long, String> finalAnswers = submission.getAnswers() != null ? submission.getAnswers() : Map.of();
            examResult.setUserAnswers(objectMapper.writeValueAsString(finalAnswers));

            // 【新增】保存 AI 分析结果详情 (如果有数据)
            if (!analysisResultMap.isEmpty()) {
                // 将分析结果 Map 序列化为 JSON 字符串存入数据库
                examResult.setResultDetails(objectMapper.writeValueAsString(analysisResultMap));
            }
        } catch (JsonProcessingException e) {
            logger.error("JSON序列化失败", e);
            examResult.setUserAnswers("{}");
        }

        examResult.setCreateTime(LocalDateTime.now());
        examResultService.save(examResult);

        // ================== 【新增】灵根经验结算 START ==================
        // 考试模式：根据得分率给予大量灵根经验
        // 只有及格（60%）以上才加灵根，模拟“有效学习”
        if (totalScore > 0 && studentScore >= totalScore * 0.6) {
            // 获取试卷对应的科目
            BizSubject subject = subjectService.getById(paper.getSubjectId());
            if (subject != null) {
                // 公式：考试得分 * 0.5 作为灵根经验
                int spiritExp = (int) (studentScore * 0.5);
                cultivationService.addSpiritRootExp(student.getId(), subject.getName(), spiritExp);
                logger.info("学生 {} 通过考试 {} 获得 {} 灵根经验 ({})",
                        student.getName(), paper.getName(), spiritExp, subject.getName());
            }
        }
        // ================== 【新增】灵根经验结算 END ==================

        // --- 记录学习积分和日志 (保持不变) ---
        BizLearningActivity activity = new BizLearningActivity();
        activity.setStudentId(student.getId());
        activity.setActivityType("EXAM");
        activity.setDescription("参加了模拟考试《" + paper.getName() + "》，得分：" + studentScore + "/" + totalScore);
        activity.setCreateTime(LocalDateTime.now());
        learningActivityService.save(activity);

        student.setPoints((student.getPoints() == null ? 0 : student.getPoints()) + 10);
        studentService.updateById(student);

        List<BizAchievement> unlockedList = new ArrayList<>();

        // =========================
        // 【修复 1】满分成就判定逻辑
        // =========================
        // 只有当本次确实满分时，才去查询历史次数，减少数据库压力
        if (studentScore >= totalScore && totalScore > 0) {
            // 使用 apply 拼接 SQL，确保比较的是每一行记录自己的 score 和 total_score
            long perfectCount = examResultService.count(new QueryWrapper<BizExamResult>()
                    .eq("student_id", student.getId())
                    .apply("score = total_score") // 关键修复：只有得分等于该次考试总分才算
                    .gt("total_score", 0));       // 排除总分为0的异常数据

            unlockedList.addAll(achievementService.checkAndAward(student.getId(), "PERFECT_PAPER", (int)perfectCount));
        }

        // =========================
        // 【修复 2】启用刷题数统计 (基于 BizExamResult 计算)
        // =========================
        // 注意：这需要 MySQL 5.7+ 支持 JSON_LENGTH 函数。
        // 如果你的 MySQL 版本较低，或者 user_answers 存的是非标准 JSON，这步可能会报错。
        try {
            // 统计该学生所有考试记录中 user_answers JSON 数组的长度之和
            // 假设 user_answers 格式为 {"101":"A", "102":"B"}，则 JSON_LENGTH 返回题目数量
            Map<String, Object> map = examResultService.getMap(new QueryWrapper<BizExamResult>()
                    .select("SUM(JSON_LENGTH(user_answers)) as total")
                    .eq("student_id", student.getId()));

            if (map != null && map.get("total") != null) {
                int totalQuestions = ((Number) map.get("total")).intValue();
                unlockedList.addAll(achievementService.checkAndAward(student.getId(), "TOTAL_QUESTIONS", totalQuestions));
            }
        } catch (Exception e) {
            logger.warn("统计刷题数失败，可能是数据库版本不支持 JSON_LENGTH: {}", e.getMessage());
        }

        // 将新解锁的成就放入返回结果
        Map<String, Object> resMap = new java.util.HashMap<>();
        resMap.put("score", studentScore);
        resMap.put("totalScore", totalScore);
        resMap.put("details", results);
        if(!unlockedList.isEmpty()) {
            resMap.put("newAchievements", unlockedList);
        }

        return Result.suc(resMap); // 注意：这里返回类型变了，前端也需要对应处理 res.data.newAchievements
    }

    // 3. 【新增】 获取历史考试记录列表
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getExamHistory(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        Page<BizExamResult> page = new Page<>(current, size);
        examResultService.lambdaQuery()
                .eq(BizExamResult::getStudentId, student.getId())
                .orderByDesc(BizExamResult::getCreateTime)
                .page(page);

        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 4. 获取某次考试的详情 (包含试卷结构、当时的用户答案以及AI点评)
     */
    @GetMapping("/history/{resultId}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getExamResultDetail(@PathVariable Long resultId, Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        BizExamResult result = examResultService.getById(resultId);
        if (result == null || !result.getStudentId().equals(student.getId())) {
            return Result.fail("记录不存在或无权访问");
        }

        com.ice.exebackend.dto.PaperDTO paper = paperService.getPaperWithQuestionsById(result.getPaperId());

        if (paper != null && paper.getGroups() != null) {
            List<Long> allQIds = paper.getGroups().stream()
                    .flatMap(g -> g.getQuestions().stream())
                    .map(com.ice.exebackend.entity.BizPaperQuestion::getQuestionId)
                    .collect(Collectors.toList());

            if(!allQIds.isEmpty()) {
                Map<Long, BizQuestion> qMap = questionService.listByIds(allQIds).stream()
                        .collect(Collectors.toMap(BizQuestion::getId, q -> q));

                // === 【核心修改】读取并注入 AI 历史评语 ===
                // 检查数据库中是否有存储 AI 分析详情
                if (StringUtils.hasText(result.getResultDetails())) {
                    try {
                        // 解析存储的 JSON: { "101": { "score": 5, "feedback": "做得好..." }, ... }
                        Map<String, Map<String, Object>> detailsMap = objectMapper.readValue(
                                result.getResultDetails(), Map.class);

                        // 遍历当前试卷的题目
                        for (BizQuestion q : qMap.values()) {
                            // JSON key 默认为 String 类型，需要将 Long ID 转为 String 匹配
                            String qIdStr = q.getId().toString();

                            if (detailsMap.containsKey(qIdStr)) {
                                Map<String, Object> detail = detailsMap.get(qIdStr);
                                String feedback = (String) detail.get("feedback");

                                // 如果有评语，将其拼接到原解析后面
                                if (StringUtils.hasText(feedback)) {
                                    String originalDesc = q.getDescription() == null ? "暂无标准解析" : q.getDescription();
                                    // 拼接 AI 点评
                                    q.setDescription(originalDesc + "\n\n【AI 智能点评】\n" + feedback);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("解析历史 AI 评语失败", e);
                    }
                }
                // ==========================================

                // 将处理过（注入了AI点评）的题目详情返回给前端
                return Result.suc(Map.of(
                        "examResult", result,
                        "paper", paper,
                        "questions", qMap
                ));
            }
        }

        return Result.suc(Map.of("examResult", result, "paper", paper));
    }

    // 辅助方法：对逗号分隔的答案进行排序 (A,B -> A,B;  B,A -> A,B)
    private String sortString(String input) {
        if (!StringUtils.hasText(input)) return "";
        return java.util.Arrays.stream(input.split(","))
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining(","));
    }
    /**
     * 【新增】获取学习热力图数据
     * 返回格式: {"2025-01-01": 5, "2025-01-02": 1}
     */
    @GetMapping("/dashboard/activity-heatmap")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getActivityHeatmap(Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        if (student == null) return Result.fail("用户不存在");

        // 查询过去一年的活动记录
        // 注意：这里为了代码简单使用了 Java Stream 分组，数据量极大时建议改用 Mapper 的 Group By SQL
        List<BizLearningActivity> activities = learningActivityService.lambdaQuery()
                .eq(BizLearningActivity::getStudentId, student.getId())
                .ge(BizLearningActivity::getCreateTime, LocalDateTime.now().minusYears(1)) // 只查最近一年
                .list();

        // 按日期分组统计次数
        Map<String, Long> heatmapData = activities.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE), // 格式化为 YYYY-MM-DD
                        Collectors.counting()
                ));

        return Result.suc(heatmapData);
    }
    /**
     * 【新增】获取积分排行榜 (前10名)
     */
    @GetMapping("/dashboard/leaderboard")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getLeaderboard() {
        List<BizStudent> topStudents = studentService.lambdaQuery()
                .select(BizStudent::getName, BizStudent::getAvatar, BizStudent::getPoints, BizStudent::getGrade)
                .orderByDesc(BizStudent::getPoints)
                .last("LIMIT 10")
                .list();
        return Result.suc(topStudents);
    }
    /**
     * 【修改】获取对战段位排行榜
     * 升级为英雄联盟段位体系
     */
    @GetMapping("/battle/leaderboard")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getBattleLeaderboard() {
        // 1. 获取积分前 20 名的学生
        List<BizStudent> topList = studentService.lambdaQuery()
                .select(BizStudent::getName, BizStudent::getAvatar, BizStudent::getPoints, BizStudent::getAvatarFrameStyle)
                .orderByDesc(BizStudent::getPoints)
                .last("LIMIT 20")
                .list();

        // 2. 转换数据，计算段位
        List<Map<String, Object>> resultList = topList.stream().map(s -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", s.getName());
            map.put("avatar", s.getAvatar());
            map.put("avatarFrameStyle", s.getAvatarFrameStyle());
            int points = s.getPoints() == null ? 0 : s.getPoints();
            map.put("points", points);

            // === 核心修改：使用 BattleGameManager 中的新逻辑 ===
            String tier = BattleGameManager.calculateTier(points);
            String tierName = BattleGameManager.getTierNameCN(tier);

            map.put("tier", tier);
            map.put("tierName", tierName);
            return map;
        }).collect(Collectors.toList());

        return Result.suc(resultList);
    }

    /**
     * 【新增】获取我的对战记录
     */
    @GetMapping("/battle/records")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getMyBattleRecords(Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        // 查询最近的 20 条记录
        List<BizBattleRecord> list = battleRecordService.lambdaQuery()
                .eq(BizBattleRecord::getPlayerId, student.getId())
                .orderByDesc(BizBattleRecord::getCreateTime)
                .last("LIMIT 20")
                .list();

        return Result.suc(list);
    }

}