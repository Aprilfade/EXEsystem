package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.PracticeResultDTO;
import com.ice.exebackend.dto.PracticeSubmissionDTO;
import com.ice.exebackend.dto.StudentDashboardStatsDTO;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.service.BizQuestionService;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.BizSubjectService;
import com.ice.exebackend.service.BizWrongRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ice.exebackend.mapper.BizQuestionMapper; // 【新增】导入 Mapper
import com.ice.exebackend.service.BizLearningActivityService; // 【新增】
import java.time.LocalDateTime; // 【新增】


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

        Map<Long, BizQuestion> correctAnswersMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        PracticeResultDTO resultDTO = new PracticeResultDTO();
        List<PracticeResultDTO.AnswerResult> answerResults = new ArrayList<>();
        int correctCount = 0;

        for (Long questionId : questionIds) {
            BizQuestion question = correctAnswersMap.get(questionId);
            String userAnswer = submission.getAnswers().get(questionId);

            if (question == null) continue;

            PracticeResultDTO.AnswerResult answerResult = new PracticeResultDTO.AnswerResult();
            boolean isCorrect = userAnswer != null && userAnswer.equalsIgnoreCase(question.getAnswer());

            if (isCorrect) {
                correctCount++;
            } else {
                BizWrongRecord wrongRecord = new BizWrongRecord();
                wrongRecord.setStudentId(student.getId());
                wrongRecord.setQuestionId(questionId);
                wrongRecord.setWrongAnswer(userAnswer);
                wrongRecord.setWrongReason("在线练习错误");
                if (wrongRecordService.lambdaQuery().eq(BizWrongRecord::getStudentId, student.getId()).eq(BizWrongRecord::getQuestionId, questionId).count() == 0) {
                    wrongRecordService.save(wrongRecord);
                }
            }
            answerResult.setQuestion(question);
            answerResult.setUserAnswer(userAnswer);
            answerResult.setCorrect(isCorrect);
            answerResults.add(answerResult);
        }
        // 【新增】记录学习活动日志
        BizLearningActivity log = new BizLearningActivity();
        log.setStudentId(student.getId());
        log.setActivityType("PRACTICE_SUBMIT");
        log.setDescription("完成了在线练习，共" + questionIds.size() + "道题");
        log.setCreateTime(LocalDateTime.now());
        learningActivityService.save(log);


        resultDTO.setTotalQuestions(questionIds.size());
        resultDTO.setCorrectCount(correctCount);
        resultDTO.setResults(answerResults);


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
     * 3. 提交试卷
     */
    @PostMapping("/exam/submit")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result submitExam(@RequestBody PracticeSubmissionDTO submission,
                             @RequestParam Long paperId,
                             Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        com.ice.exebackend.dto.PaperDTO paper = paperService.getPaperWithQuestionsById(paperId);
        if (paper == null) return Result.fail("试卷不存在");

        // 计算分数
        int totalScore = 0;
        int studentScore = 0;
        List<PracticeResultDTO.AnswerResult> results = new ArrayList<>();

        // 获取所有题目真实信息用于比对
        List<Long> allQIds = new ArrayList<>(submission.getAnswers().keySet());
        // 也要包含试卷里有但学生没做的题
        if (paper.getGroups() != null) {
            paper.getGroups().forEach(g -> g.getQuestions().forEach(pq -> allQIds.add(pq.getQuestionId())));
        }
        List<BizQuestion> dbQuestions = questionService.listByIds(allQIds);
        Map<Long, BizQuestion> dbQMap = dbQuestions.stream().collect(Collectors.toMap(BizQuestion::getId, q -> q));

        // 遍历试卷结构来计分（确保按照试卷的分值设定）
        if (paper.getGroups() != null) {
            for (com.ice.exebackend.dto.PaperDTO.PaperGroupDTO group : paper.getGroups()) {
                for (com.ice.exebackend.entity.BizPaperQuestion pq : group.getQuestions()) {
                    BizQuestion q = dbQMap.get(pq.getQuestionId());
                    if (q == null) continue;

                    totalScore += pq.getScore(); // 累加卷面总分
                    String userAns = submission.getAnswers().get(pq.getQuestionId());

                    boolean isCorrect = false;
                    // 简易判题逻辑：忽略大小写和首尾空格
                    if (userAns != null && q.getAnswer() != null) {
                        // 对于多选题（逗号分隔），需要排序后比较
                        if (q.getQuestionType() == 2) {
                            // 简单处理：都转为数组排序再转字符串比较
                            String sortedUser = sortString(userAns);
                            String sortedDb = sortString(q.getAnswer());
                            isCorrect = sortedUser.equalsIgnoreCase(sortedDb);
                        } else {
                            isCorrect = userAns.trim().equalsIgnoreCase(q.getAnswer().trim());
                        }
                    }

                    if (isCorrect) {
                        studentScore += pq.getScore();
                    } else {
                        // 记录错题
                        BizWrongRecord wr = new BizWrongRecord();
                        wr.setStudentId(student.getId());
                        wr.setQuestionId(q.getId());
                        wr.setPaperId(paperId);
                        wr.setWrongAnswer(userAns);
                        wr.setWrongReason("模拟考试错误");
                        // 避免重复插入同一题同一试卷的错题
                        if (wrongRecordService.lambdaQuery()
                                .eq(BizWrongRecord::getStudentId, student.getId())
                                .eq(BizWrongRecord::getPaperId, paperId)
                                .eq(BizWrongRecord::getQuestionId, q.getId())
                                .count() == 0) {
                            wrongRecordService.save(wr);
                        }
                    }

                    // 构建返回结果详情
                    PracticeResultDTO.AnswerResult res = new PracticeResultDTO.AnswerResult();
                    res.setQuestion(q);
                    res.setUserAnswer(userAns);
                    res.setCorrect(isCorrect);
                    results.add(res);
                }
            }
        }

        // 记录学习活动
        BizLearningActivity activity = new BizLearningActivity();
        activity.setStudentId(student.getId());
        activity.setActivityType("EXAM");
        activity.setDescription("参加了模拟考试《" + paper.getName() + "》，得分：" + studentScore + "/" + totalScore);
        activity.setCreateTime(LocalDateTime.now());
        learningActivityService.save(activity);

        PracticeResultDTO resultDTO = new PracticeResultDTO();
        resultDTO.setTotalQuestions(results.size()); // 这里复用字段表示得分
        resultDTO.setCorrectCount(studentScore); // 这里复用字段表示得分
        // 稍微hack一下，用 ResultDTO 返回分数信息
        // 建议：ResultDTO 可以加个 score 字段，这里暂时用 msg 返回或者前端计算

        return Result.suc(Map.of(
                "score", studentScore,
                "totalScore", totalScore,
                "details", results
        ));
    }

    // 辅助方法：对逗号分隔的答案进行排序 (A,B -> A,B;  B,A -> A,B)
    private String sortString(String input) {
        if (!StringUtils.hasText(input)) return "";
        return java.util.Arrays.stream(input.split(","))
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining(","));
    }

}