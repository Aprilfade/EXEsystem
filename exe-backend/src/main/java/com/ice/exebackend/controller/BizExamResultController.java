package com.ice.exebackend.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.ExamResultDetailDTO;
import com.ice.exebackend.dto.KnowledgePointScoreAnalysisDTO;
import com.ice.exebackend.dto.ScoreExportDTO;
import com.ice.exebackend.dto.ScoreStatsDTO;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.BizExamResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/exam-results")
@PreAuthorize("hasAuthority('sys:stats:list')") // 使用统计权限
public class BizExamResultController {

    @Autowired
    private BizExamResultService examResultService;

    /**
     * 分页查询考试成绩列表（增强版，包含关联信息）
     */
    @GetMapping
    public Result getResultList(@RequestParam(defaultValue = "1") int current,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String paperName,
                                @RequestParam(required = false) String studentName,
                                @RequestParam(required = false) Long classId,
                                @RequestParam(required = false) Long subjectId,
                                @RequestParam(required = false) Long paperId,
                                @RequestParam(required = false) Integer minScore,
                                @RequestParam(required = false) Integer maxScore,
                                @RequestParam(required = false) String startTime,
                                @RequestParam(required = false) String endTime,
                                @RequestParam(required = false) Boolean published,
                                @RequestParam(defaultValue = "createTime") String sortBy,
                                @RequestParam(defaultValue = "desc") String sortOrder) {

        Map<String, Object> params = new HashMap<>();
        params.put("paperName", paperName);
        params.put("studentName", studentName);
        params.put("classId", classId);
        params.put("subjectId", subjectId);
        params.put("paperId", paperId);
        params.put("minScore", minScore);
        params.put("maxScore", maxScore);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("published", published);
        params.put("sortBy", sortBy);
        params.put("sortOrder", sortOrder);

        Page<ExamResultDetailDTO> page = examResultService.getExamResultsWithDetails(current, size, params);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 获取成绩统计数据
     */
    @GetMapping("/stats")
    public Result getStats(@RequestParam(required = false) Long paperId,
                          @RequestParam(required = false) Long classId,
                          @RequestParam(required = false) Long subjectId,
                          @RequestParam(required = false) String startTime,
                          @RequestParam(required = false) String endTime) {

        Map<String, Object> params = new HashMap<>();
        params.put("paperId", paperId);
        params.put("classId", classId);
        params.put("subjectId", subjectId);
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        ScoreStatsDTO stats = examResultService.getScoreStats(params);
        return Result.suc(stats);
    }

    /**
     * 获取学生成绩趋势
     */
    @GetMapping("/student/{studentId}/trend")
    public Result getStudentTrend(@PathVariable Long studentId,
                                  @RequestParam(required = false) Long subjectId) {
        List<Map<String, Object>> trend = examResultService.getStudentScoreTrend(studentId, subjectId);
        return Result.suc(trend);
    }

    /**
     * 导出成绩Excel
     */
    @GetMapping("/export")
    public void exportScores(HttpServletResponse response,
                            @RequestParam(required = false) String paperName,
                            @RequestParam(required = false) String studentName,
                            @RequestParam(required = false) Long classId,
                            @RequestParam(required = false) Long subjectId,
                            @RequestParam(required = false) Long paperId,
                            @RequestParam(required = false) Integer minScore,
                            @RequestParam(required = false) Integer maxScore,
                            @RequestParam(required = false) String startTime,
                            @RequestParam(required = false) String endTime,
                            @RequestParam(required = false) Boolean published) throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("paperName", paperName);
        params.put("studentName", studentName);
        params.put("classId", classId);
        params.put("subjectId", subjectId);
        params.put("paperId", paperId);
        params.put("minScore", minScore);
        params.put("maxScore", maxScore);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("published", published);

        List<ScoreExportDTO> data = examResultService.getExportData(params);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("成绩单_" + System.currentTimeMillis(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), ScoreExportDTO.class)
                .sheet("成绩单")
                .doWrite(data);
    }

    /**
     * 获取成绩详情（用于批阅）
     */
    @GetMapping("/{id}")
    public Result getResultDetail(@PathVariable Long id) {
        BizExamResult result = examResultService.getById(id);
        if (result == null) {
            return Result.fail("记录不存在");
        }

        return Result.suc(result);
    }

    /**
     * 【知识点功能增强】获取考试成绩的知识点分析
     */
    @GetMapping("/{id}/knowledge-analysis")
    @PreAuthorize("hasAuthority('sys:stats:list')")
    public Result<List<KnowledgePointScoreAnalysisDTO>> getKnowledgePointAnalysis(@PathVariable Long id) {
        List<KnowledgePointScoreAnalysisDTO> analysis = examResultService.getKnowledgePointAnalysis(id);
        return Result.ok(analysis);
    }

    /**
     * 更新成绩分数
     */
    @PutMapping("/{id}/score")
    @Log(title = "成绩管理", businessType = BusinessType.UPDATE)
    public Result updateScore(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer score = (Integer) body.get("score");
        if (score == null) {
            return Result.fail("分数不能为空");
        }

        BizExamResult result = new BizExamResult();
        result.setId(id);
        result.setScore(score);

        boolean success = examResultService.updateById(result);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 更新成绩评语
     */
    @PutMapping("/{id}/comment")
    @Log(title = "成绩管理", businessType = BusinessType.UPDATE)
    public Result updateComment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String comment = body.get("comment");

        BizExamResult result = new BizExamResult();
        result.setId(id);
        result.setComment(comment);

        boolean success = examResultService.updateById(result);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 批量删除成绩
     */
    @DeleteMapping("/batch")
    @Log(title = "成绩管理", businessType = BusinessType.DELETE)
    public Result batchDelete(@RequestBody List<Object> idsObj) {
        // 将 Integer/Long 转换为 Long
        List<Long> ids = idsObj.stream()
                .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
                .collect(Collectors.toList());

        boolean success = examResultService.batchDelete(ids);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 批量发布成绩
     */
    @PutMapping("/batch/publish")
    @Log(title = "成绩管理", businessType = BusinessType.UPDATE)
    public Result batchPublish(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Object> idsObj = (List<Object>) body.get("ids");
        Boolean published = (Boolean) body.get("published");

        if (idsObj == null || idsObj.isEmpty()) {
            return Result.fail("请选择要操作的记录");
        }

        // 将 Integer/Long 转换为 Long
        List<Long> ids = idsObj.stream()
                .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
                .collect(Collectors.toList());

        boolean success = examResultService.batchPublish(ids, published);
        return success ? Result.suc() : Result.fail();
    }
}
