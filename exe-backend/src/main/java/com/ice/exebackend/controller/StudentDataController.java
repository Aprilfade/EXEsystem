package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.PracticeResultDTO;
import com.ice.exebackend.dto.PracticeSubmissionDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
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
@RequestMapping("/api/v1/student") // 关键：所有方法的URL都会带上/student前缀
public class StudentDataController {

    @Autowired
    private BizStudentService studentService;

    @Autowired
    private BizWrongRecordService wrongRecordService;

    @Autowired
    private BizSubjectService subjectService;

    @Autowired
    private BizQuestionService questionService;

    // 【新增】注入文件上传路径配置
    @Value("${file.upload-dir}")
    private String uploadDir;


    /**
     * 学生端：获取我自己的错题记录
     * 新的URL将是: GET /api/v1/student/my-wrong-records
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
     * 【新增】学生端专属的文件上传接口
     * 新的URL将是: POST /api/v1/student/files/upload
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

            // 返回可供前端访问的相对URL
            String fileUrl = "/api/v1/files/" + newFileName;
            return Result.suc(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    /**
     * 【新增】获取所有可供练习的科目列表
     * URL: GET /api/v1/student/subjects
     */
    @GetMapping("/subjects")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getPracticeSubjects() {
        // 简单地返回所有科目，未来可以根据学生选课情况进行过滤
        List<BizSubject> subjects = subjectService.list();
        return Result.suc(subjects);
    }

    /**
     * 【修改】根据科目ID和年级获取一套练习题
     * URL: GET /api/v1/student/practice-questions
     */
    @GetMapping("/practice-questions")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getPracticeQuestions(@RequestParam Long subjectId, @RequestParam String grade) { // 新增 grade 参数
        QueryWrapper<BizQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("subject_id", subjectId);
        queryWrapper.eq("grade", grade); // 新增按年级筛选的条件
        queryWrapper.last("ORDER BY RAND() LIMIT 10"); // 随机取10道

        List<BizQuestion> questions = questionService.list(queryWrapper);

        // 依然不返回答案
        questions.forEach(q -> {
            q.setAnswer(null);
            q.setDescription(null); // 练习时也不返回解析
        });

        return Result.suc(questions);
    }
    /**
     * 【新增】提交在线练习并获取批改结果
     * URL: POST /api/v1/student/submit-practice
     */
    @PostMapping("/submit-practice")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result submitPractice(@RequestBody PracticeSubmissionDTO submission, Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null) {
            return Result.fail("学生不存在");
        }

        // 1. 获取所有提交的题目ID
        List<Long> questionIds = new ArrayList<>(submission.getAnswers().keySet());
        if (questionIds.isEmpty()) {
            return Result.suc(new PracticeResultDTO());
        }

        // 2. 一次性从数据库查出所有题目的正确答案和信息
        Map<Long, BizQuestion> correctAnswersMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        // 3. 构建返回结果并记录错题
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
                // 自动记录错题
                BizWrongRecord wrongRecord = new BizWrongRecord();
                wrongRecord.setStudentId(student.getId());
                wrongRecord.setQuestionId(questionId);
                wrongRecord.setWrongAnswer(userAnswer);
                wrongRecord.setWrongReason("在线练习错误");
                // 避免重复记录同一道错题（可选，但推荐）
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
}


    // 未来所有学生端的业务接口，如获取练习题、获取考试列表等，都应放在这个文件里。
