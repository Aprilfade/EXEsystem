package com.ice.exebackend.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.ExamResultDetailDTO;
import com.ice.exebackend.dto.GradingHistoryExportDTO;
import com.ice.exebackend.dto.KnowledgePointScoreAnalysisDTO;
import com.ice.exebackend.dto.ScoreExportDTO;
import com.ice.exebackend.dto.ScoreStatsDTO;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.entity.BizGradingHistory;
import com.ice.exebackend.entity.BizGradingApproval;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.BizExamResultService;
import com.ice.exebackend.service.BizGradingHistoryService;
import com.ice.exebackend.service.BizGradingApprovalService;
import com.ice.exebackend.service.SysUserService;
import com.ice.exebackend.service.SysUserNotificationService;
import com.ice.exebackend.handler.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

    @Autowired
    private SysUserService userService;

    @Autowired
    private BizGradingHistoryService gradingHistoryService;

    @Autowired
    private BizGradingApprovalService approvalService;

    @Autowired
    private SysUserNotificationService notificationService;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

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
                                @RequestParam(required = false) Integer status,
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
        params.put("status", status);
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
     * 更新成绩分数（增强版：添加权限校验、状态管理、乐观锁支持、历史记录）
     * TODO: 未来可在biz_paper表添加created_by字段，实现更细粒度的权限控制
     */
    @PutMapping("/{id}/score")
    @Log(title = "成绩批阅-修改分数", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('sys:stats:list')") // 使用统计权限
    public Result updateScore(@PathVariable Long id,
                             @RequestBody Map<String, Object> body,
                             Authentication authentication) {
        Integer score = (Integer) body.get("score");
        if (score == null) {
            return Result.fail("分数不能为空");
        }

        // 1. 获取成绩记录
        BizExamResult examResult = examResultService.getById(id);
        if (examResult == null) {
            return Result.fail("成绩记录不存在");
        }

        // 2. 分数合理性校验
        if (score < 0 || score > examResult.getTotalScore()) {
            return Result.fail("分数必须在 0 到 " + examResult.getTotalScore() + " 之间");
        }

        // 3. 获取当前登录用户
        String username = authentication.getName();
        SysUser currentUser = userService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();

        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        // 4. 记录修改前的分数（用于历史记录）
        Integer oldScore = examResult.getScore();

        // 【v3.07 新增】检查分数修改是否需要审批
        if (approvalService.needsApproval(examResult, score)) {
            String reason = (String) body.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return Result.fail("分数变化较大，需要提供修改原因以提交审批");
            }

            // 创建审批记录
            BizGradingApproval approval = approvalService.createApproval(
                    examResult,
                    score,
                    examResult.getComment(), // 评语保持不变，使用comment字段
                    reason,
                    currentUser.getId(),
                    currentUser.getNickName() // 使用nickName字段
            );

            Map<String, Object> result = new HashMap<>();
            result.put("needsApproval", true);
            result.put("approvalId", approval.getId());
            result.put("message", "分数变化超过审批阈值，已提交审批，请等待审核");

            return Result.suc(result);
        }

        // 5. 更新成绩（使用乐观锁）
        examResult.setScore(score);
        examResult.setStatus(2);  // 设置为"已批改"状态
        examResult.setGradedBy(currentUser.getId());
        examResult.setGradedTime(LocalDateTime.now());

        boolean success = examResultService.updateById(examResult);
        if (!success) {
            return Result.fail("更新失败，数据可能已被其他用户修改，请刷新后重试");
        }

        // 6. 记录批阅历史
        String reason = (String) body.get("reason"); // 可选的修改原因
        gradingHistoryService.recordScoreUpdate(
                id,
                currentUser.getId(),
                currentUser.getNickName(),
                oldScore,
                score,
                reason
        );

        // 7. 发送通知给学生
        sendScoreUpdateNotification(examResult, oldScore, score, currentUser.getNickName());

        return Result.suc("分数修改成功");
    }

    /**
     * 更新成绩评语（增强版：添加权限校验、状态管理、历史记录）
     * TODO: 未来可在biz_paper表添加created_by字段，实现更细粒度的权限控制
     */
    @PutMapping("/{id}/comment")
    @Log(title = "成绩批阅-修改评语", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('sys:stats:list')") // 使用统计权限
    public Result updateComment(@PathVariable Long id,
                               @RequestBody Map<String, String> body,
                               Authentication authentication) {
        String comment = body.get("comment");

        // 1. 评语长度校验
        if (comment != null && comment.length() > 500) {
            return Result.fail("评语长度不能超过500字符");
        }

        // 2. 获取成绩记录
        BizExamResult examResult = examResultService.getById(id);
        if (examResult == null) {
            return Result.fail("成绩记录不存在");
        }

        // 3. 获取当前登录用户
        String username = authentication.getName();
        SysUser currentUser = userService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();

        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        // 4. 记录修改前的评语（用于历史记录）
        String oldComment = examResult.getComment();

        // 5. 更新评语（使用乐观锁）
        examResult.setComment(comment);
        examResult.setStatus(2);  // 设置为"已批改"状态
        examResult.setGradedBy(currentUser.getId());
        examResult.setGradedTime(LocalDateTime.now());

        boolean success = examResultService.updateById(examResult);
        if (!success) {
            return Result.fail("更新失败，数据可能已被其他用户修改，请刷新后重试");
        }

        // 6. 记录批阅历史
        String reason = body.get("reason"); // 可选的修改原因
        gradingHistoryService.recordCommentUpdate(
                id,
                currentUser.getId(),
                currentUser.getNickName(),
                oldComment,
                comment,
                reason
        );

        // 7. 发送通知给学生
        sendCommentUpdateNotification(examResult, comment, currentUser.getNickName());

        return Result.suc("评语修改成功");
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

    /**
     * 【批阅历史记录】获取某条成绩的批阅历史
     */
    @GetMapping("/{id}/grading-history")
    @PreAuthorize("hasAuthority('sys:stats:list')")
    public Result getGradingHistory(@PathVariable Long id) {
        // 1. 验证成绩记录是否存在
        BizExamResult examResult = examResultService.getById(id);
        if (examResult == null) {
            return Result.fail("成绩记录不存在");
        }

        // 2. 获取批阅历史
        List<BizGradingHistory> history = gradingHistoryService.getHistoryByExamResultId(id);
        return Result.suc(history);
    }

    /**
     * 【批阅历史记录】获取批阅人的操作历史
     */
    @GetMapping("/grader/{graderId}/history")
    @PreAuthorize("hasAuthority('sys:stats:list')")
    public Result getGraderHistory(@PathVariable Long graderId,
                                   @RequestParam(defaultValue = "50") Integer limit) {
        // 限制最大查询数量
        if (limit > 200) {
            limit = 200;
        }

        List<BizGradingHistory> history = gradingHistoryService.getHistoryByGraderId(graderId, limit);
        return Result.suc(history);
    }

    /**
     * 【批阅历史记录】统计批阅人的操作次数
     */
    @GetMapping("/grader/{graderId}/stats")
    @PreAuthorize("hasAuthority('sys:stats:list')")
    public Result getGraderStats(@PathVariable Long graderId) {
        Integer count = gradingHistoryService.countByGraderId(graderId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("graderId", graderId);
        stats.put("totalOperations", count);

        return Result.suc(stats);
    }

    /**
     * 【批阅历史记录】导出批阅历史Excel
     */
    @GetMapping("/{id}/grading-history/export")
    @PreAuthorize("hasAuthority('sys:stats:list')")
    public void exportGradingHistory(HttpServletResponse response,
                                      @PathVariable Long id) throws IOException {
        // 1. 验证成绩记录是否存在
        BizExamResult examResult = examResultService.getById(id);
        if (examResult == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"code\":404,\"msg\":\"成绩记录不存在\"}");
            return;
        }

        // 2. 获取批阅历史
        List<BizGradingHistory> historyList = gradingHistoryService.getHistoryByExamResultId(id);

        // 3. 转换为导出DTO
        List<GradingHistoryExportDTO> exportData = historyList.stream()
                .map(history -> {
                    GradingHistoryExportDTO dto = new GradingHistoryExportDTO();
                    dto.setExamResultId(history.getExamResultId());
                    dto.setGraderName(history.getGraderName());

                    // 转换操作类型为中文
                    String actionTypeText = "";
                    switch (history.getActionType()) {
                        case "UPDATE_SCORE":
                            actionTypeText = "修改分数";
                            break;
                        case "UPDATE_COMMENT":
                            actionTypeText = "修改评语";
                            break;
                        case "BATCH_UPDATE":
                            actionTypeText = "批量更新";
                            break;
                        default:
                            actionTypeText = history.getActionType();
                    }
                    dto.setActionType(actionTypeText);

                    dto.setOldScore(history.getOldScore());
                    dto.setNewScore(history.getNewScore());
                    dto.setOldComment(history.getOldComment());
                    dto.setNewComment(history.getNewComment());
                    dto.setReason(history.getReason());
                    dto.setCreateTime(history.getCreateTime());
                    return dto;
                })
                .collect(Collectors.toList());

        // 4. 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("批阅历史_成绩" + id + "_" + System.currentTimeMillis(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 5. 写入Excel
        EasyExcel.write(response.getOutputStream(), GradingHistoryExportDTO.class)
                .sheet("批阅历史")
                .doWrite(exportData);
    }

    /**
     * 【批量导出】批量导出批阅历史Excel
     */
    @PostMapping("/batch/grading-history/export")
    @PreAuthorize("hasAuthority('sys:stats:list')")
    public void batchExportGradingHistory(HttpServletResponse response,
                                          @RequestBody Map<String, Object> body) throws IOException {
        @SuppressWarnings("unchecked")
        List<Object> idsObj = (List<Object>) body.get("ids");

        if (idsObj == null || idsObj.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"code\":400,\"msg\":\"请选择要导出的记录\"}");
            return;
        }

        // 将 Integer/Long 转换为 Long
        List<Long> ids = idsObj.stream()
                .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
                .collect(Collectors.toList());

        // 获取所有批阅历史
        List<GradingHistoryExportDTO> exportData = new java.util.ArrayList<>();

        for (Long id : ids) {
            BizExamResult examResult = examResultService.getById(id);
            if (examResult == null) continue;

            List<BizGradingHistory> historyList = gradingHistoryService.getHistoryByExamResultId(id);

            historyList.stream()
                    .map(history -> {
                        GradingHistoryExportDTO dto = new GradingHistoryExportDTO();
                        dto.setExamResultId(history.getExamResultId());
                        dto.setGraderName(history.getGraderName());

                        // 转换操作类型为中文
                        String actionTypeText = "";
                        switch (history.getActionType()) {
                            case "UPDATE_SCORE":
                                actionTypeText = "修改分数";
                                break;
                            case "UPDATE_COMMENT":
                                actionTypeText = "修改评语";
                                break;
                            case "BATCH_UPDATE":
                                actionTypeText = "批量更新";
                                break;
                            default:
                                actionTypeText = history.getActionType();
                        }
                        dto.setActionType(actionTypeText);

                        dto.setOldScore(history.getOldScore());
                        dto.setNewScore(history.getNewScore());
                        dto.setOldComment(history.getOldComment());
                        dto.setNewComment(history.getNewComment());
                        dto.setReason(history.getReason());
                        dto.setCreateTime(history.getCreateTime());
                        return dto;
                    })
                    .forEach(exportData::add);
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("批阅历史_批量_" + System.currentTimeMillis(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入Excel
        EasyExcel.write(response.getOutputStream(), GradingHistoryExportDTO.class)
                .sheet("批阅历史")
                .doWrite(exportData);
    }

    /**
     * 【通知功能】发送分数更新通知
     */
    private void sendScoreUpdateNotification(BizExamResult examResult, Integer oldScore, Integer newScore, String graderName) {
        try {
            String title = "成绩已更新";
            String content = String.format("您的成绩已由 %s 更新：%d → %d",
                    graderName,
                    oldScore != null ? oldScore : 0,
                    newScore);

            // 1. 保存通知到数据库
            notificationService.createNotification(
                    examResult.getStudentId(),
                    title,
                    content,
                    "SCORE_UPDATE",
                    examResult.getId()
            );

            // 2. 发送WebSocket实时通知
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SCORE_UPDATE");
            notification.put("title", title);
            notification.put("message", content);
            notification.put("examResultId", examResult.getId());
            notification.put("studentId", examResult.getStudentId());
            notification.put("timestamp", System.currentTimeMillis());

            String message = com.alibaba.fastjson.JSON.toJSONString(notification);
            webSocketHandler.broadcast(message, "STUDENT");
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            System.err.println("发送分数更新通知失败: " + e.getMessage());
        }
    }

    /**
     * 【通知功能】发送评语更新通知
     */
    private void sendCommentUpdateNotification(BizExamResult examResult, String newComment, String graderName) {
        try {
            String title = "评语已更新";
            String content = String.format("您的成绩评语已由 %s 更新", graderName);

            // 1. 保存通知到数据库
            notificationService.createNotification(
                    examResult.getStudentId(),
                    title,
                    content,
                    "COMMENT_UPDATE",
                    examResult.getId()
            );

            // 2. 发送WebSocket实时通知
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "COMMENT_UPDATE");
            notification.put("title", title);
            notification.put("message", content);
            notification.put("examResultId", examResult.getId());
            notification.put("studentId", examResult.getStudentId());
            notification.put("comment", newComment);
            notification.put("timestamp", System.currentTimeMillis());

            String message = com.alibaba.fastjson.JSON.toJSONString(notification);
            webSocketHandler.broadcast(message, "STUDENT");
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            System.err.println("发送评语更新通知失败: " + e.getMessage());
        }
    }
}
