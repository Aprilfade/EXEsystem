package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.entity.BizGradingApproval;
import com.ice.exebackend.mapper.BizGradingApprovalMapper;
import com.ice.exebackend.service.BizExamResultService;
import com.ice.exebackend.service.BizGradingApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批阅审批服务实现类
 *
 * @author Claude Code Assistant
 * @since v3.07
 */
@Slf4j
@Service
public class BizGradingApprovalServiceImpl extends ServiceImpl<BizGradingApprovalMapper, BizGradingApproval>
        implements BizGradingApprovalService {

    @Autowired
    private BizGradingApprovalMapper approvalMapper;

    @Autowired
    private BizExamResultService examResultService;

    /**
     * 审批配置：是否启用审批功能
     */
    @Value("${grading.approval.enabled:true}")
    private Boolean approvalEnabled;

    /**
     * 审批配置：分数变化绝对值阈值
     */
    @Value("${grading.approval.score-threshold:10}")
    private Integer scoreThreshold;

    /**
     * 审批配置：分数变化百分比阈值
     */
    @Value("${grading.approval.percentage-threshold:20}")
    private Integer percentageThreshold;

    /**
     * 默认审批阈值配置（如果配置文件中未指定）
     */
    private static final int DEFAULT_SCORE_THRESHOLD = 10;
    private static final int DEFAULT_PERCENTAGE_THRESHOLD = 20;

    @Override
    public boolean needsApproval(BizExamResult examResult, Integer newScore) {
        // 检查是否启用审批功能
        if (!approvalEnabled) {
            return false;
        }

        Integer oldScore = examResult.getScore();
        if (oldScore == null || newScore == null) {
            return false;
        }

        // 分数变化量
        int scoreChange = Math.abs(newScore - oldScore);

        // 检查绝对值阈值
        if (scoreChange >= scoreThreshold) {
            return true;
        }

        // 检查百分比阈值（如果原分数不为0）
        if (oldScore != 0) {
            BigDecimal changePercentage = new BigDecimal(scoreChange)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(Math.abs(oldScore)), 2, RoundingMode.HALF_UP);

            if (changePercentage.compareTo(new BigDecimal(percentageThreshold)) >= 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizGradingApproval createApproval(BizExamResult examResult, Integer newScore, String newComment,
                                             String reason, Long graderId, String graderName) {
        BizGradingApproval approval = new BizGradingApproval();

        // 基本信息
        approval.setExamResultId(examResult.getId());
        approval.setStudentId(examResult.getStudentId());
        approval.setStudentName("Student-" + examResult.getStudentId()); // 使用学生ID构造名称
        approval.setPaperId(examResult.getPaperId());
        approval.setPaperTitle(examResult.getPaperName()); // 使用paperName字段

        // 分数变更信息
        Integer oldScore = examResult.getScore();
        approval.setOldScore(oldScore);
        approval.setNewScore(newScore);
        approval.setScoreChange(newScore - (oldScore != null ? oldScore : 0));

        // 计算变化百分比
        if (oldScore != null && oldScore != 0) {
            BigDecimal changePercentage = new BigDecimal(Math.abs(approval.getScoreChange()))
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(Math.abs(oldScore)), 2, RoundingMode.HALF_UP);
            approval.setChangePercentage(changePercentage);
        }

        // 评语变更信息
        approval.setOldComment(examResult.getComment()); // 使用comment字段
        approval.setNewComment(newComment);

        // 申请人信息
        approval.setGraderId(graderId);
        approval.setGraderName(graderName);
        approval.setReason(reason);

        // 审批状态
        approval.setStatus(BizGradingApproval.STATUS_PENDING);
        approval.setSubmitTime(LocalDateTime.now());

        // 保存审批记录
        this.save(approval);

        log.info("创建审批记录成功: ID={}, 学生ID={}, 分数变化={}->{}",
                approval.getId(), examResult.getStudentId(), oldScore, newScore);

        return approval;
    }

    @Override
    public Page<BizGradingApproval> getApprovalPage(Integer current, Integer size, Map<String, Object> params) {
        Page<BizGradingApproval> page = new Page<>(current, size);
        return approvalMapper.selectApprovalPage(page, params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long approvalId, String approvalComment, Long approverId, String approverName) {
        BizGradingApproval approval = this.getById(approvalId);
        if (approval == null) {
            log.error("审批记录不存在: ID={}", approvalId);
            return false;
        }

        if (!BizGradingApproval.STATUS_PENDING.equals(approval.getStatus())) {
            log.error("审批记录状态不是待审批: ID={}, Status={}", approvalId, approval.getStatus());
            return false;
        }

        // 更新审批记录状态
        approval.setStatus(BizGradingApproval.STATUS_APPROVED);
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setApprovalComment(approvalComment);
        approval.setApprovalTime(LocalDateTime.now());
        this.updateById(approval);

        // 更新考试结果
        BizExamResult examResult = examResultService.getById(approval.getExamResultId());
        if (examResult != null) {
            examResult.setScore(approval.getNewScore());
            if (approval.getNewComment() != null) {
                examResult.setComment(approval.getNewComment()); // 使用comment字段
            }
            examResultService.updateById(examResult);

            log.info("审批通过，已更新成绩: 学生ID={}, 分数={}->{}",
                    approval.getStudentId(), approval.getOldScore(), approval.getNewScore());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(Long approvalId, String approvalComment, Long approverId, String approverName) {
        BizGradingApproval approval = this.getById(approvalId);
        if (approval == null) {
            log.error("审批记录不存在: ID={}", approvalId);
            return false;
        }

        if (!BizGradingApproval.STATUS_PENDING.equals(approval.getStatus())) {
            log.error("审批记录状态不是待审批: ID={}, Status={}", approvalId, approval.getStatus());
            return false;
        }

        // 更新审批记录状态
        approval.setStatus(BizGradingApproval.STATUS_REJECTED);
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setApprovalComment(approvalComment);
        approval.setApprovalTime(LocalDateTime.now());
        this.updateById(approval);

        log.info("审批驳回: 学生ID={}, 分数变化={}->{}",
                approval.getStudentId(), approval.getOldScore(), approval.getNewScore());

        return true;
    }

    @Override
    public Integer getPendingCount() {
        return approvalMapper.countPending();
    }

    @Override
    public Map<String, Integer> getStatusStatistics() {
        List<Map<String, Object>> stats = approvalMapper.countByStatus();
        Map<String, Integer> result = new HashMap<>();

        result.put("PENDING", 0);
        result.put("APPROVED", 0);
        result.put("REJECTED", 0);

        for (Map<String, Object> stat : stats) {
            String status = (String) stat.get("status");
            Long count = (Long) stat.get("count");
            result.put(status, count.intValue());
        }

        result.put("TOTAL", result.get("PENDING") + result.get("APPROVED") + result.get("REJECTED"));

        return result;
    }
}
