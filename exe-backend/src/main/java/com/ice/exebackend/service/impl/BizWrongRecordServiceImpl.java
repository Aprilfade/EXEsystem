package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.PaperStatsVO;
import com.ice.exebackend.dto.WrongRecordDTO;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizWrongRecord;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.mapper.BizWrongRecordMapper;
import com.ice.exebackend.service.BizWrongRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizWrongRecordServiceImpl extends ServiceImpl<BizWrongRecordMapper, BizWrongRecord> implements BizWrongRecordService {


    @Autowired
    private BizQuestionMapper questionMapper; // 确保注入 QuestionMapper 用于查询题目详情

    // 复习间隔算法 (天)：1天, 3天, 7天, 15天, 30天
    private static final int[] REVIEW_INTERVALS = {1, 3, 7, 15, 30};
    @Override
    @Transactional
    public boolean createWrongRecord(WrongRecordDTO dto) {
        if (dto == null || dto.getQuestionId() == null || CollectionUtils.isEmpty(dto.getStudentIds())) {
            return false;
        }
        for (Long studentId : dto.getStudentIds()) {
            // 检查是否已存在未掌握的记录，避免重复
            long count = this.lambdaQuery()
                    .eq(BizWrongRecord::getStudentId, studentId)
                    .eq(BizWrongRecord::getQuestionId, dto.getQuestionId())
                    .eq(BizWrongRecord::getIsMastered, 0)
                    .count();

            if (count > 0) continue; // 已存在则跳过

            BizWrongRecord record = new BizWrongRecord();
            record.setStudentId(studentId);
            record.setQuestionId(dto.getQuestionId());
            record.setPaperId(dto.getPaperId());
            record.setWrongReason(dto.getWrongReason());
            record.setCreateTime(LocalDateTime.now());
            record.setIsMastered(0);

            // 【新增】初始化复习计划：第0次复习，下次复习时间为明天
            record.setReviewCount(0);
            record.setNextReviewTime(LocalDateTime.now().plusDays(1));

            this.save(record);
        }
        return true;
    }

    // 【修改】 getWrongRecordPage 方法，增加过滤is_mastered的条件
    @Override
    public Page<WrongRecordVO> getWrongRecordPage(Page<WrongRecordVO> page, Long studentId, Long questionId) {
        return this.baseMapper.selectWrongRecordPage(page, studentId, questionId);
    }

    @Override
    public List<PaperStatsVO> getPaperErrorStats(Long paperId) {
        return this.baseMapper.countErrorsByPaper(paperId);
    }

    // 【新增】 获取错题详情
    @Override
    public WrongRecordVO getWrongRecordDetail(Long recordId, Long studentId) {
        return this.baseMapper.selectWrongRecordDetail(recordId, studentId);
    }

    // 【新增】 标记错题为已掌握
    @Override
    public boolean markAsMastered(Long recordId, Long studentId) {
        return this.lambdaUpdate()
                .eq(BizWrongRecord::getId, recordId)
                .eq(BizWrongRecord::getStudentId, studentId)
                .set(BizWrongRecord::getIsMastered, 1)
                .update();
    }
    @Override
    public List<Long> getTopErrorKnowledgePointIds(Long studentId, Long subjectId, int limit) {
        // 确保你已经注入了 BizQuestionKnowledgePointMapper 和 BizKnowledgePointMapper
        // 这里只是一个简化的逻辑，在实际项目中需要更复杂的SQL联表查询
        return Collections.emptyList(); // 这里暂时返回空列表，实际逻辑需要实现
    }
    /**
     * 【新增】获取今日待复习的题目列表
     */
    public List<WrongRecordVO> getDailyReviewRecords(Long studentId) {
        // 查询条件：is_mastered = 0 且 next_review_time <= 当前时间
        List<BizWrongRecord> records = this.lambdaQuery()
                .eq(BizWrongRecord::getStudentId, studentId)
                .eq(BizWrongRecord::getIsMastered, 0)
                .le(BizWrongRecord::getNextReviewTime, LocalDateTime.now())
                .orderByDesc(BizWrongRecord::getNextReviewTime) // 逾期最久的排前面
                .list();

        if (records.isEmpty()) return Collections.emptyList();

        // 填充题目详细信息 (这里为了简单直接循环查，生产环境建议用 IN 查询优化)
        List<WrongRecordVO> voList = new ArrayList<>();
        List<Long> questionIds = records.stream().map(BizWrongRecord::getQuestionId).collect(Collectors.toList());
        Map<Long, BizQuestion> questionMap = questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        for (BizWrongRecord record : records) {
            BizQuestion q = questionMap.get(record.getQuestionId());
            if (q != null) {
                WrongRecordVO vo = new WrongRecordVO();
                // 复制错题记录信息
                vo.setId(record.getId());
                vo.setStudentId(record.getStudentId());
                vo.setQuestionId(record.getQuestionId());
                vo.setWrongReason(record.getWrongReason());
                vo.setCreateTime(record.getCreateTime());
                // 复制题目信息 (用于前端展示做题)
                vo.setQuestionContent(q.getContent());
                vo.setContent(q.getContent());
                vo.setOptions(q.getOptions());
                vo.setImageUrl(q.getImageUrl());
                // 关键：前端做题时需要题型来决定显示单选还是填空
                // 借用 vo 的 description 字段或者扩展 VO 传题型，这里为了不改 VO 结构
                // 我们假设前端会单独拉取题目详情，或者我们扩展 WrongRecordVO 增加 questionType
                // 这里简单起见，我们把 questionType 拼在 content 里或者扩展 VO
                // *建议在 WrongRecordVO 中增加 questionType 字段，这里暂略*
                voList.add(vo);
            }
        }
        return voList;
    }

    /**
     * 【新增】提交复习结果
     * @param recordId 错题记录ID
     * @param isCorrect 是否回答正确
     * @return 增加的复习天数 (用于前端提示)
     */
    @Transactional
    public int submitReviewResult(Long recordId, boolean isCorrect) {
        BizWrongRecord record = this.getById(recordId);
        if (record == null) return 0;

        int daysToAdd;
        if (isCorrect) {
            // 回答正确：进入下一个复习阶段
            int currentCount = record.getReviewCount() == null ? 0 : record.getReviewCount();

            // 如果已经完成了所有复习阶段，标记为已掌握
            if (currentCount >= REVIEW_INTERVALS.length - 1) {
                record.setIsMastered(1);
                daysToAdd = 999; // 标记为已掌握
            } else {
                daysToAdd = REVIEW_INTERVALS[currentCount];
                record.setReviewCount(currentCount + 1);
                record.setNextReviewTime(LocalDateTime.now().plusDays(daysToAdd));
            }
        } else {
            // 回答错误：重置复习进度
            daysToAdd = 1;
            record.setReviewCount(0); // 打回原形
            record.setNextReviewTime(LocalDateTime.now().plusDays(1)); // 明天再来
        }

        this.updateById(record);
        return daysToAdd;
    }
}


