package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizGradingApproval;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.BizGradingApprovalService;
import com.ice.exebackend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 批阅审批控制器
 *
 * @author Claude Code Assistant
 * @since v3.07
 */
@RestController
@RequestMapping("/api/v1/grading-approvals")
public class BizGradingApprovalController {

    @Autowired
    private BizGradingApprovalService approvalService;

    @Autowired
    private SysUserService userService;

    /**
     * 分页查询审批记录
     */
    @GetMapping
    public Result getApprovalList(@RequestParam(defaultValue = "1") Integer current,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) String studentName,
                                  @RequestParam(required = false) Long paperId,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("studentId", studentId);
        params.put("studentName", studentName);
        params.put("paperId", paperId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Page<BizGradingApproval> page = approvalService.getApprovalPage(current, size, params);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 查询我的审批记录（我提交的）
     */
    @GetMapping("/my-submissions")
    public Result getMySubmissions(@RequestParam(defaultValue = "1") Integer current,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String status,
                                   Authentication authentication) {
        // 获取当前用户
        SysUser currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("graderId", currentUser.getId());
        params.put("status", status);

        Page<BizGradingApproval> page = approvalService.getApprovalPage(current, size, params);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 获取审批详情
     */
    @GetMapping("/{id}")
    public Result getApprovalDetail(@PathVariable Long id) {
        BizGradingApproval approval = approvalService.getById(id);
        if (approval == null) {
            return Result.fail("审批记录不存在");
        }
        return Result.suc(approval);
    }

    /**
     * 审批通过
     */
    @PutMapping("/{id}/approve")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    public Result approve(@PathVariable Long id,
                         @RequestBody Map<String, String> body,
                         Authentication authentication) {
        // 获取当前用户
        SysUser currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        String approvalComment = body.get("approvalComment");
        if (approvalComment == null || approvalComment.trim().isEmpty()) {
            return Result.fail("请填写审批意见");
        }

        boolean success = approvalService.approve(id, approvalComment, currentUser.getId(), currentUser.getNickName());
        return success ? Result.suc("审批通过") : Result.fail("审批失败");
    }

    /**
     * 审批驳回
     */
    @PutMapping("/{id}/reject")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    public Result reject(@PathVariable Long id,
                        @RequestBody Map<String, String> body,
                        Authentication authentication) {
        // 获取当前用户
        SysUser currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        String approvalComment = body.get("approvalComment");
        if (approvalComment == null || approvalComment.trim().isEmpty()) {
            return Result.fail("请填写驳回原因");
        }

        boolean success = approvalService.reject(id, approvalComment, currentUser.getId(), currentUser.getNickName());
        return success ? Result.suc("已驳回审批") : Result.fail("驳回失败");
    }

    /**
     * 获取待审批数量
     */
    @GetMapping("/pending-count")
    public Result getPendingCount() {
        Integer count = approvalService.getPendingCount();
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.suc(data);
    }

    /**
     * 获取审批统计
     */
    @GetMapping("/statistics")
    public Result getStatistics() {
        Map<String, Integer> stats = approvalService.getStatusStatistics();
        return Result.suc(stats);
    }

    /**
     * 撤销审批（仅限申请人撤销待审批状态的记录）
     */
    @DeleteMapping("/{id}")
    @Log(title = "审批管理", businessType = BusinessType.DELETE)
    public Result cancelApproval(@PathVariable Long id, Authentication authentication) {
        // 获取当前用户
        SysUser currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return Result.fail("无法获取当前用户信息");
        }

        BizGradingApproval approval = approvalService.getById(id);
        if (approval == null) {
            return Result.fail("审批记录不存在");
        }

        // 检查是否是申请人
        if (!approval.getGraderId().equals(currentUser.getId())) {
            return Result.fail("只能撤销自己提交的审批");
        }

        // 只能撤销待审批状态的记录
        if (!BizGradingApproval.STATUS_PENDING.equals(approval.getStatus())) {
            return Result.fail("只能撤销待审批状态的记录");
        }

        boolean success = approvalService.removeById(id);
        return success ? Result.suc("撤销成功") : Result.fail("撤销失败");
    }

    /**
     * 获取当前登录用户
     */
    private SysUser getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();
    }
}
