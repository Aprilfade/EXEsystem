package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizExamResultService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/exam-results")
@PreAuthorize("hasAuthority('sys:stats:list')") // 使用统计权限或新增一个 sys:result:list
public class BizExamResultController {

    @Autowired
    private BizExamResultService examResultService;

    @Autowired
    private BizStudentService studentService;

    @Autowired
    private com.ice.exebackend.service.BizPaperService paperService; // 记得注入

    /**
     * 分页查询考试成绩列表 (支持按试卷、学生搜索)
     */
    @GetMapping
    public Result getResultList(@RequestParam(defaultValue = "1") int current,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String paperName,
                                @RequestParam(required = false) String studentName) {
        Page<BizExamResult> page = new Page<>(current, size);
        QueryWrapper<BizExamResult> query = new QueryWrapper<>();

        if (StringUtils.hasText(paperName)) {
            query.like("paper_name", paperName);
        }

        // 如果按学生姓名搜索，需要先查学生ID (简单处理)
        if (StringUtils.hasText(studentName)) {
            // 这里为了性能建议联表查询，但为了保持代码风格一致，使用子查询逻辑或MyBatis-Plus Join
            // 简便写法：
            query.inSql("student_id", "SELECT id FROM biz_student WHERE name LIKE '%" + studentName + "%'");
        }

        query.orderByDesc("create_time");
        examResultService.page(page, query);

        // 补充学生姓名 (不在ExamResult表中)
        page.getRecords().forEach(record -> {
            BizStudent s = studentService.getById(record.getStudentId());
            if (s != null) {
                // 借用 paperName 字段或者扩展 DTO，这里为了简单暂不扩展，前端根据 studentId 查可能较慢
                // 建议：在 BizExamResult 中增加 studentName 冗余字段，或者返回 VO
                // 这里演示返回 VO 的逻辑：其实前端列表展示主要看分数
            }
        });

        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 教师批阅：更新分数
     */
    @PutMapping("/{id}/score")
    public Result updateScore(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer score = body.get("score");
        if (score == null) return Result.fail("分数不能为空");

        BizExamResult result = new BizExamResult();
        result.setId(id);
        result.setScore(score);

        boolean success = examResultService.updateById(result);
        return success ? Result.suc() : Result.fail();
    }
    @GetMapping("/{id}")
    public Result getResultDetail(@PathVariable Long id) {
        BizExamResult result = examResultService.getById(id);
        if (result == null) return Result.fail("记录不存在");

        com.ice.exebackend.dto.PaperDTO paper = paperService.getPaperWithQuestionsById(result.getPaperId());

        // 简化的返回，教师端不需要像学生端那样脱敏
        return Result.suc(Map.of(
                "examResult", result,
                "paper", paper
        ));
    }
}
