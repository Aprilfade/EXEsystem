package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.mapper.*;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/student/classes")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentClassController {

    @Autowired private BizClassMapper classMapper;
    @Autowired private BizClassStudentMapper classStudentMapper;
    @Autowired private BizHomeworkMapper homeworkMapper;
    @Autowired private BizStudentService studentService;
    @Autowired private BizPaperMapper paperMapper;
    // 【新增】注入考试结果 Mapper，用于检查是否已完成
    @Autowired private BizExamResultMapper examResultMapper;

    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return s != null ? s.getId() : null;
    }

    // 1. 加入班级
    @PostMapping("/join")
    public Result joinClass(@RequestBody Map<String, String> body, Authentication auth) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) return Result.fail("请输入邀请码");

        BizClass bizClass = classMapper.selectOne(new QueryWrapper<BizClass>().eq("code", code));
        if (bizClass == null) return Result.fail("邀请码无效，班级不存在");

        Long studentId = getCurrentStudentId(auth);

        // 检查是否已加入
        Long count = classStudentMapper.selectCount(new QueryWrapper<BizClassStudent>()
                .eq("class_id", bizClass.getId())
                .eq("student_id", studentId));
        if (count > 0) return Result.fail("您已加入该班级");

        BizClassStudent relation = new BizClassStudent();
        relation.setClassId(bizClass.getId());
        relation.setStudentId(studentId);
        relation.setCreateTime(LocalDateTime.now());
        classStudentMapper.insert(relation);

        return Result.suc("加入成功");
    }

    // 2. 我的班级列表
    @GetMapping("/my")
    public Result getMyClasses(Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        List<BizClassStudent> relations = classStudentMapper.selectList(
                new QueryWrapper<BizClassStudent>().eq("student_id", studentId)
        );

        if (relations.isEmpty()) {
            return Result.suc(Collections.emptyList());
        }

        List<Long> classIds = relations.stream()
                .map(BizClassStudent::getClassId)
                .collect(Collectors.toList());

        // 【修复】替换 selectBatchIds 为 selectList + in 查询
        List<BizClass> classes = classMapper.selectList(
                new QueryWrapper<BizClass>().in("id", classIds)
        );
        return Result.suc(classes);
    }

    // 3. 获取班级作业 (修改了这个方法)
    @GetMapping("/{classId}/homework")
    public Result getClassHomework(@PathVariable Long classId, Authentication auth) { // 【修改】增加 Authentication 参数
        Long studentId = getCurrentStudentId(auth); // 获取当前学生ID

        List<BizHomework> homeworks = homeworkMapper.selectList(new QueryWrapper<BizHomework>()
                .eq("class_id", classId)
                .orderByDesc("create_time"));

        // 填充试卷名称和完成状态
        for (BizHomework hw : homeworks) {
            BizPaper paper = paperMapper.selectById(hw.getPaperId());
            if (paper != null) {
                hw.setPaperName(paper.getName());
            }

            // 【核心修复逻辑】检查该学生是否已经提交过这张试卷
            Long count = examResultMapper.selectCount(new QueryWrapper<BizExamResult>()
                    .eq("paper_id", hw.getPaperId())
                    .eq("student_id", studentId));

            // 如果 count > 0，说明已考过，状态设为 1 (已完成)，否则为 0
            hw.setStatus(count > 0 ? 1 : 0);
        }
        return Result.suc(homeworks);
    }
}
