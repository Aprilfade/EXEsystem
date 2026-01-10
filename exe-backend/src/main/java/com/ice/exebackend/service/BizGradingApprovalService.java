package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.entity.BizGradingApproval;

import java.util.Map;

/**
 * 批阅审批服务接口
 *
 * @author Claude Code Assistant
 * @since v3.07
 */
public interface BizGradingApprovalService extends IService<BizGradingApproval> {

    /**
     * 检查分数修改是否需要审批
     *
     * @param examResult 考试结果
     * @param newScore   新分数
     * @return true-需要审批, false-不需要审批
     */
    boolean needsApproval(BizExamResult examResult, Integer newScore);

    /**
     * 创建审批记录
     *
     * @param examResult 考试结果
     * @param newScore   新分数
     * @param newComment 新评语
     * @param reason     申请原因
     * @param graderId   批阅人ID
     * @param graderName 批阅人姓名
     * @return 审批记录
     */
    BizGradingApproval createApproval(BizExamResult examResult, Integer newScore, String newComment,
                                      String reason, Long graderId, String graderName);

    /**
     * 分页查询审批记录
     *
     * @param current 当前页
     * @param size    每页大小
     * @param params  查询参数
     * @return 分页结果
     */
    Page<BizGradingApproval> getApprovalPage(Integer current, Integer size, Map<String, Object> params);

    /**
     * 审批通过
     *
     * @param approvalId      审批ID
     * @param approvalComment 审批意见
     * @param approverId      审批人ID
     * @param approverName    审批人姓名
     * @return 是否成功
     */
    boolean approve(Long approvalId, String approvalComment, Long approverId, String approverName);

    /**
     * 审批驳回
     *
     * @param approvalId      审批ID
     * @param approvalComment 审批意见
     * @param approverId      审批人ID
     * @param approverName    审批人姓名
     * @return 是否成功
     */
    boolean reject(Long approvalId, String approvalComment, Long approverId, String approverName);

    /**
     * 获取待审批数量
     *
     * @return 待审批数量
     */
    Integer getPendingCount();

    /**
     * 统计各状态审批数量
     *
     * @return 状态统计
     */
    Map<String, Integer> getStatusStatistics();
}
