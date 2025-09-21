package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.PracticeResultDTO;
import com.ice.exebackend.dto.PracticeSubmissionDTO;
import com.ice.exebackend.dto.StudentDashboardStatsDTO;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizSubject;
import com.ice.exebackend.entity.BizWrongRecord;
import com.ice.exebackend.service.BizQuestionService;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.BizSubjectService;
import com.ice.exebackend.service.BizWrongRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ice.exebackend.mapper.BizQuestionMapper; // 【新增】导入 Mapper
import com.ice.exebackend.entity.BizLearningActivity; // 【新增】
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

}