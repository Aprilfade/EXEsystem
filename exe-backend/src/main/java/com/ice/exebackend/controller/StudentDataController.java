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
import com.ice.exebackend.mapper.BizQuestionMapper;

import java.time.LocalDateTime;
import com.ice.exebackend.entity.BizExamResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;

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

    private static final Logger logger = LoggerFactory.getLogger(StudentDataController.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BizStudentService studentService;

    @Autowired
    private BizWrongRecordService wrongRecordService;

    @Autowired
    private BizSubjectService subjectService;

    @Autowired
    private BizQuestionService questionService;

    @Autowired
    private BizQuestionMapper questionMapper;

    @Autowired
    private BizLearningActivityService learningActivityService;

    @Autowired
    private com.ice.exebackend.service.BizPaperService paperService;

    @Autowired
    private BizBattleRecordService battleRecordService;

    // 【删除】CultivationService 注入
    // @Autowired
    // private com.ice.exebackend.service.CultivationService cultivationService;

    @Autowired
    private BizAchievementService achievementService;

    @Autowired
    private BizExamResultService examResultService;

    @Autowired
    private AiService aiService;

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
            logger.error("学生端文件上传失败", e);
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getPracticeSubjects() {
        List<BizSubject> subjects = subjectService.list();
        return Result.suc(subjects);
    }

    @GetMapping("/practice-questions")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getPracticeQuestions(
            @RequestParam Long subjectId,
            @RequestParam String grade,
            @RequestParam(required = false) String mode,
            Authentication authentication
    ) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("当前用户不是一个有效的学生");
        }

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

        if ("smart".equalsIgnoreCase(mode)) {
            List<Long> wrongKpIds = questionMapper.selectWrongKnowledgePointIds(student.getId(), subjectId);
            List<Long> questionIds = new ArrayList<>();
            if (!wrongKpIds.isEmpty()) {
                questionIds = questionMapper.selectQuestionsByKnowledgePoints(wrongKpIds, student.getId(), 7);
            }
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
                questions.addAll(questionService.listByIds(questionIds));
            } else {
                questions = questionService.listByIds(questionIds);
            }
        } else {
            baseQueryWrapper.last("ORDER BY RAND() LIMIT 10");
            questions = questionService.list(baseQueryWrapper);
        }

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

        BizLearningActivity log = new BizLearningActivity();
        log.setStudentId(student.getId());
        log.setActivityType("PRACTICE_SUBMIT");
        log.setDescription("完成了在线练习，共" + questionIds.size() + "道题，答对" + correctCount + "题");
        log.setCreateTime(LocalDateTime.now());
        learningActivityService.save(log);

        // 【修改】删除增加修为和灵根经验的逻辑
        // int expGain = 10 + (correctCount * 10);
        // cultivationService.addExp(student.getId(), expGain);

        // if (correctCount > 0 && !answerResults.isEmpty()) {
        //     BizQuestion firstQ = answerResults.get(0).getQuestion();
        //     if (firstQ != null && firstQ.getSubjectId() != null) {
        //         BizSubject subject = subjectService.getById(firstQ.getSubjectId());
        //         if (subject != null) {
        //             int spiritExp = correctCount * 2;
        //             cultivationService.addSpiritRootExp(student.getId(), subject.getName(), spiritExp);
        //         }
        //     }
        // }

        int pointsGain = 5;
        student.setPoints((student.getPoints() == null ? 0 : student.getPoints()) + pointsGain);
        studentService.updateById(student);

        resultDTO.setTotalQuestions(questionIds.size());
        resultDTO.setCorrectCount(correctCount);
        resultDTO.setResults(answerResults);

        // resultDTO.setExpGain(expGain); // 可删除或设为 null
        resultDTO.setExpGain(0);
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
                .last("LIMIT 5")
                .list();
        return Result.suc(activities);
    }

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
        queryWrapper.eq("status", 1);
        if (StringUtils.hasText(student.getGrade())) {
            queryWrapper.eq("grade", student.getGrade());
        }
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        queryWrapper.orderByDesc("create_time");

        studentService.page(new Page<>(), new QueryWrapper<>());
        return Result.suc(paperService.page(page, queryWrapper));
    }

    @GetMapping("/exam/{paperId}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getExamPaperDetail(@PathVariable Long paperId) {
        com.ice.exebackend.dto.PaperDTO paperDTO = paperService.getPaperWithQuestionsById(paperId);
        if (paperDTO == null) return Result.fail("试卷不存在");

        if (paperDTO.getGroups() != null) {
            List<Long> allQIds = paperDTO.getGroups().stream()
                    .flatMap(g -> g.getQuestions().stream())
                    .map(com.ice.exebackend.entity.BizPaperQuestion::getQuestionId)
                    .collect(Collectors.toList());

            if (!allQIds.isEmpty()) {
                Map<Long, BizQuestion> qMap = questionService.listByIds(allQIds).stream()
                        .collect(Collectors.toMap(BizQuestion::getId, q -> q));

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

    @PostMapping("/exam/submit")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result submitExam(@RequestBody PracticeSubmissionDTO submission,
                             @RequestParam Long paperId,
                             HttpServletRequest request,
                             Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        com.ice.exebackend.dto.PaperDTO paper = paperService.getPaperWithQuestionsById(paperId);
        if (paper == null) return Result.fail("试卷不存在");

        String aiKey = request.getHeader("X-Ai-Api-Key");
        String aiProvider = request.getHeader("X-Ai-Provider");

        int totalScore = 0;
        int studentScore = 0;
        List<PracticeResultDTO.AnswerResult> results = new ArrayList<>();
        Map<Long, Map<String, Object>> analysisResultMap = new java.util.HashMap<>();

        if (paper.getPaperType() != null && paper.getPaperType() == 2) {
            totalScore = paper.getTotalScore() != null ? paper.getTotalScore() : 100;
            studentScore = 0;
        } else {
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
                            int awardedScore = 0;

                            if (userAns != null && q.getAnswer() != null) {
                                if (q.getQuestionType() == 5) {
                                    if (StringUtils.hasText(aiKey) && StringUtils.hasText(userAns)) {
                                        try {
                                            AiGradingResult aiResult = aiService.gradeSubjectiveQuestion(
                                                    aiKey, aiProvider,
                                                    q.getContent(), q.getAnswer(), userAns, pq.getScore()
                                            );

                                            awardedScore = aiResult.getScore();
                                            isCorrect = awardedScore >= (pq.getScore() * 0.6);

                                            String feedback = aiResult.getFeedback();
                                            if (aiResult.getReason() != null) {
                                                feedback += "\n(扣分原因: " + aiResult.getReason() + ")";
                                            }
                                            String aiComment = "\n\n【AI 智能点评】\n" + feedback;
                                            q.setDescription((q.getDescription() == null ? "" : q.getDescription()) + aiComment);

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
                                        awardedScore = 0;
                                        isCorrect = false;
                                    }
                                }
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

                            studentScore += awardedScore;

                            // 【新增】自动记录错题到错题本
                            if (!isCorrect) {
                                // 检查该错题是否已存在（避免重复记录）
                                long existingCount = wrongRecordService.lambdaQuery()
                                        .eq(BizWrongRecord::getStudentId, student.getId())
                                        .eq(BizWrongRecord::getQuestionId, pq.getQuestionId())
                                        .eq(BizWrongRecord::getIsMastered, 0)
                                        .count();

                                if (existingCount == 0) {
                                    BizWrongRecord wrongRecord = new BizWrongRecord();
                                    wrongRecord.setStudentId(student.getId());
                                    wrongRecord.setQuestionId(pq.getQuestionId());
                                    wrongRecord.setWrongAnswer(userAns);
                                    wrongRecord.setWrongReason("模拟考试《" + paper.getName() + "》中答错");
                                    wrongRecord.setCreateTime(LocalDateTime.now());
                                    wrongRecordService.save(wrongRecord);
                                }
                            }

                            PracticeResultDTO.AnswerResult res = new PracticeResultDTO.AnswerResult();
                            res.setQuestion(q);
                            res.setUserAnswer(userAns);
                            res.setCorrect(isCorrect);
                            res.setEarnedScore(awardedScore);
                            results.add(res);
                        }
                    }
                }
            }
        }

        BizExamResult examResult = new BizExamResult();
        examResult.setStudentId(student.getId());
        examResult.setPaperId(paperId);
        examResult.setPaperName(paper.getName());
        examResult.setScore(studentScore);
        examResult.setTotalScore(totalScore);
        examResult.setViolationCount(submission.getViolationCount() != null ? submission.getViolationCount() : 0);

        try {
            Map<Long, String> finalAnswers = submission.getAnswers() != null ? submission.getAnswers() : Map.of();
            examResult.setUserAnswers(objectMapper.writeValueAsString(finalAnswers));

            if (!analysisResultMap.isEmpty()) {
                examResult.setResultDetails(objectMapper.writeValueAsString(analysisResultMap));
            }
        } catch (JsonProcessingException e) {
            logger.error("JSON序列化失败", e);
            examResult.setUserAnswers("{}");
        }

        examResult.setCreateTime(LocalDateTime.now());
        examResultService.save(examResult);

        // 【修改】删除灵根经验结算逻辑
        // if (totalScore > 0 && studentScore >= totalScore * 0.6) {
        //     BizSubject subject = subjectService.getById(paper.getSubjectId());
        //     if (subject != null) {
        //         int spiritExp = (int) (studentScore * 0.5);
        //         cultivationService.addSpiritRootExp(student.getId(), subject.getName(), spiritExp);
        //         logger.info("学生 {} 通过考试 {} 获得 {} 灵根经验 ({})",
        //                 student.getName(), paper.getName(), spiritExp, subject.getName());
        //     }
        // }

        BizLearningActivity activity = new BizLearningActivity();
        activity.setStudentId(student.getId());
        activity.setActivityType("EXAM");
        activity.setDescription("参加了模拟考试《" + paper.getName() + "》，得分：" + studentScore + "/" + totalScore);
        activity.setCreateTime(LocalDateTime.now());
        learningActivityService.save(activity);

        student.setPoints((student.getPoints() == null ? 0 : student.getPoints()) + 10);
        studentService.updateById(student);

        List<BizAchievement> unlockedList = new ArrayList<>();

        if (studentScore >= totalScore && totalScore > 0) {
            long perfectCount = examResultService.count(new QueryWrapper<BizExamResult>()
                    .eq("student_id", student.getId())
                    .apply("score = total_score")
                    .gt("total_score", 0));

            unlockedList.addAll(achievementService.checkAndAward(student.getId(), "PERFECT_PAPER", (int)perfectCount));
        }

        try {
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

        Map<String, Object> resMap = new java.util.HashMap<>();
        resMap.put("score", studentScore);
        resMap.put("totalScore", totalScore);
        resMap.put("details", results);
        if(!unlockedList.isEmpty()) {
            resMap.put("newAchievements", unlockedList);
        }

        return Result.suc(resMap);
    }

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

                if (StringUtils.hasText(result.getResultDetails())) {
                    try {
                        Map<String, Map<String, Object>> detailsMap = objectMapper.readValue(
                                result.getResultDetails(), Map.class);

                        for (BizQuestion q : qMap.values()) {
                            String qIdStr = q.getId().toString();

                            if (detailsMap.containsKey(qIdStr)) {
                                Map<String, Object> detail = detailsMap.get(qIdStr);
                                String feedback = (String) detail.get("feedback");

                                if (StringUtils.hasText(feedback)) {
                                    String originalDesc = q.getDescription() == null ? "暂无标准解析" : q.getDescription();
                                    q.setDescription(originalDesc + "\n\n【AI 智能点评】\n" + feedback);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("解析历史 AI 评语失败", e);
                    }
                }

                return Result.suc(Map.of(
                        "examResult", result,
                        "paper", paper,
                        "questions", qMap
                ));
            }
        }

        return Result.suc(Map.of("examResult", result, "paper", paper));
    }

    private String sortString(String input) {
        if (!StringUtils.hasText(input)) return "";
        return java.util.Arrays.stream(input.split(","))
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining(","));
    }

    @GetMapping("/dashboard/activity-heatmap")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getActivityHeatmap(Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        if (student == null) return Result.fail("用户不存在");

        List<BizLearningActivity> activities = learningActivityService.lambdaQuery()
                .eq(BizLearningActivity::getStudentId, student.getId())
                .ge(BizLearningActivity::getCreateTime, LocalDateTime.now().minusYears(1))
                .list();

        Map<String, Long> heatmapData = activities.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        Collectors.counting()
                ));

        return Result.suc(heatmapData);
    }

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

    @GetMapping("/battle/leaderboard")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getBattleLeaderboard() {
        List<BizStudent> topList = studentService.lambdaQuery()
                .select(BizStudent::getName, BizStudent::getAvatar, BizStudent::getPoints, BizStudent::getAvatarFrameStyle)
                .orderByDesc(BizStudent::getPoints)
                .last("LIMIT 20")
                .list();

        List<Map<String, Object>> resultList = topList.stream().map(s -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", s.getName());
            map.put("avatar", s.getAvatar());
            map.put("avatarFrameStyle", s.getAvatarFrameStyle());
            int points = s.getPoints() == null ? 0 : s.getPoints();
            map.put("points", points);

            String tier = BattleGameManager.calculateTier(points);
            String tierName = BattleGameManager.getTierNameCN(tier);

            map.put("tier", tier);
            map.put("tierName", tierName);
            return map;
        }).collect(Collectors.toList());

        return Result.suc(resultList);
    }

    @GetMapping("/battle/records")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getMyBattleRecords(Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        List<BizBattleRecord> list = battleRecordService.lambdaQuery()
                .eq(BizBattleRecord::getPlayerId, student.getId())
                .orderByDesc(BizBattleRecord::getCreateTime)
                .last("LIMIT 20")
                .list();

        return Result.suc(list);
    }
}