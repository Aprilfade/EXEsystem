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
     * 【核心修复】根据科目ID和年级获取一套练习题
     */
    @GetMapping("/practice-questions")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getPracticeQuestions(@RequestParam Long subjectId, @RequestParam String grade) {
        // 先根据ID找到科目实体，以获取科目名称
        BizSubject subject = subjectService.getById(subjectId);
        if (subject == null) {
            return Result.fail("科目不存在");
        }

        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();
        // 关键：查询所有与该科目名称相同的试题
        queryWrapper.inSql("subject_id", "SELECT id FROM biz_subject WHERE name = '" + subject.getName() + "'");

        // 并且年级必须匹配
        if (StringUtils.hasText(grade)) {
            queryWrapper.eq("grade", grade);
        }

        // 随机抽取10道题
        queryWrapper.last("ORDER BY RAND() LIMIT 10");

        List<BizQuestion> questions = questionService.list(queryWrapper);

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
}