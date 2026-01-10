package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.entity.BizGradingHistory;

import java.util.List;

/**
 * 批阅历史记录服务接口
 *
 * @author Claude Code Assistant
 * @since v3.04
 */
public interface BizGradingHistoryService extends IService<BizGradingHistory> {

    /**
     * 记录分数修改历史
     *
     * @param examResultId 成绩ID
     * @param graderId 批阅人ID
     * @param graderName 批阅人姓名
     * @param oldScore 旧分数
     * @param newScore 新分数
     * @param reason 修改原因（可选）
     */
    void recordScoreUpdate(Long examResultId, Long graderId, String graderName,
                          Integer oldScore, Integer newScore, String reason);

    /**
     * 记录评语修改历史
     *
     * @param examResultId 成绩ID
     * @param graderId 批阅人ID
     * @param graderName 批阅人姓名
     * @param oldComment 旧评语
     * @param newComment 新评语
     * @param reason 修改原因（可选）
     */
    void recordCommentUpdate(Long examResultId, Long graderId, String graderName,
                            String oldComment, String newComment, String reason);

    /**
     * 记录批阅操作（同时修改分数和评语）
     *
     * @param oldResult 修改前的成绩记录
     * @param newResult 修改后的成绩记录
     * @param graderId 批阅人ID
     * @param graderName 批阅人姓名
     * @param reason 修改原因（可选）
     */
    void recordGradingOperation(BizExamResult oldResult, BizExamResult newResult,
                               Long graderId, String graderName, String reason);

    /**
     * 根据成绩ID查询批阅历史
     *
     * @param examResultId 成绩ID
     * @return 批阅历史列表
     */
    List<BizGradingHistory> getHistoryByExamResultId(Long examResultId);

    /**
     * 根据批阅人ID查询批阅历史
     *
     * @param graderId 批阅人ID
     * @param limit 限制数量
     * @return 批阅历史列表
     */
    List<BizGradingHistory> getHistoryByGraderId(Long graderId, Integer limit);

    /**
     * 统计批阅人的操作次数
     *
     * @param graderId 批阅人ID
     * @return 操作次数
     */
    Integer countByGraderId(Long graderId);
}
