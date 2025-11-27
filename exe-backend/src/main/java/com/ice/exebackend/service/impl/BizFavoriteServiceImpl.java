package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizFavorite;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.mapper.BizFavoriteMapper;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.service.BizFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizFavoriteServiceImpl extends ServiceImpl<BizFavoriteMapper, BizFavorite> implements BizFavoriteService {

    @Autowired
    private BizQuestionMapper questionMapper;

    @Override
    public boolean toggleFavorite(Long studentId, Long questionId) {
        QueryWrapper<BizFavorite> query = new QueryWrapper<>();
        query.eq("student_id", studentId).eq("question_id", questionId);
        BizFavorite existing = this.getOne(query);

        if (existing != null) {
            // 已存在，则取消收藏
            return this.removeById(existing.getId());
        } else {
            // 不存在，则添加收藏
            BizFavorite fav = new BizFavorite();
            fav.setStudentId(studentId);
            fav.setQuestionId(questionId);
            return this.save(fav);
        }
    }

    @Override
    public boolean isFavorited(Long studentId, Long questionId) {
        return this.count(new QueryWrapper<BizFavorite>()
                .eq("student_id", studentId)
                .eq("question_id", questionId)) > 0;
    }

    @Override
    public Page<BizQuestion> getMyFavorites(Page<BizQuestion> page, Long studentId) {
        // 1. 先分页查询收藏表里的 question_id，按收藏时间倒序
        Page<BizFavorite> favPage = this.page(new Page<>(page.getCurrent(), page.getSize()),
                new QueryWrapper<BizFavorite>().eq("student_id", studentId).orderByDesc("create_time"));

        // 2. 如果没有收藏，直接返回空页
        if (favPage.getRecords().isEmpty()) {
            Page<BizQuestion> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
            return emptyPage;
        }

        // 3. 提取所有题目ID
        List<Long> qIds = favPage.getRecords().stream()
                .map(BizFavorite::getQuestionId)
                .collect(Collectors.toList());

        // 4. 批量查询题目详情
        List<BizQuestion> questionsFromDb = questionMapper.selectBatchIds(qIds);

        // 【关键修复】将查询结果转为 Map，防止 selectBatchIds 导致的乱序问题
        Map<Long, BizQuestion> questionMap = questionsFromDb.stream()
                .collect(Collectors.toMap(BizQuestion::getId, q -> q));

        // 5. 【关键修复】按照收藏记录的顺序（qIds的顺序）重新组装题目列表
        // 同时过滤掉可能已经被物理删除的题目，防止空指针
        List<BizQuestion> sortedQuestions = new java.util.ArrayList<>();
        for (Long qId : qIds) {
            if (questionMap.containsKey(qId)) {
                sortedQuestions.add(questionMap.get(qId));
            }
        }

        // 6. 组装返回
        Page<BizQuestion> resultPage = new Page<>(page.getCurrent(), page.getSize(), favPage.getTotal());
        resultPage.setRecords(sortedQuestions);
        return resultPage;
    }
}