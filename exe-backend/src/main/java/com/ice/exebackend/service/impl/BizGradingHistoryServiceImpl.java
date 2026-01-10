package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizExamResult;
import com.ice.exebackend.entity.BizGradingHistory;
import com.ice.exebackend.mapper.BizGradingHistoryMapper;
import com.ice.exebackend.service.BizGradingHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 批阅历史记录服务实现类
 *
 * @author Claude Code Assistant
 * @since v3.04
 */
@Service
public class BizGradingHistoryServiceImpl extends ServiceImpl<BizGradingHistoryMapper, BizGradingHistory>
        implements BizGradingHistoryService {

    @Override
    public void recordScoreUpdate(Long examResultId, Long graderId, String graderName,
                                  Integer oldScore, Integer newScore, String reason) {
        BizGradingHistory history = new BizGradingHistory();
        history.setExamResultId(examResultId);
        history.setGraderId(graderId);
        history.setGraderName(graderName);
        history.setActionType(BizGradingHistory.ActionType.UPDATE_SCORE);
        history.setOldScore(oldScore);
        history.setNewScore(newScore);
        history.setReason(reason);
        history.setCreateTime(LocalDateTime.now());

        this.save(history);
    }

    @Override
    public void recordCommentUpdate(Long examResultId, Long graderId, String graderName,
                                   String oldComment, String newComment, String reason) {
        BizGradingHistory history = new BizGradingHistory();
        history.setExamResultId(examResultId);
        history.setGraderId(graderId);
        history.setGraderName(graderName);
        history.setActionType(BizGradingHistory.ActionType.UPDATE_COMMENT);
        history.setOldComment(oldComment);
        history.setNewComment(newComment);
        history.setReason(reason);
        history.setCreateTime(LocalDateTime.now());

        this.save(history);
    }

    @Override
    public void recordGradingOperation(BizExamResult oldResult, BizExamResult newResult,
                                      Long graderId, String graderName, String reason) {
        // 检查分数是否变化
        boolean scoreChanged = !Objects.equals(oldResult.getScore(), newResult.getScore());

        // 检查评语是否变化
        boolean commentChanged = !Objects.equals(oldResult.getComment(), newResult.getComment());

        // 如果分数变化，记录分数修改历史
        if (scoreChanged) {
            recordScoreUpdate(
                    oldResult.getId(),
                    graderId,
                    graderName,
                    oldResult.getScore(),
                    newResult.getScore(),
                    reason
            );
        }

        // 如果评语变化，记录评语修改历史
        if (commentChanged) {
            recordCommentUpdate(
                    oldResult.getId(),
                    graderId,
                    graderName,
                    oldResult.getComment(),
                    newResult.getComment(),
                    reason
            );
        }
    }

    @Override
    public List<BizGradingHistory> getHistoryByExamResultId(Long examResultId) {
        return baseMapper.selectByExamResultId(examResultId);
    }

    @Override
    public List<BizGradingHistory> getHistoryByGraderId(Long graderId, Integer limit) {
        return baseMapper.selectByGraderId(graderId, limit);
    }

    @Override
    public Integer countByGraderId(Long graderId) {
        return baseMapper.countByGraderId(graderId);
    }
}
