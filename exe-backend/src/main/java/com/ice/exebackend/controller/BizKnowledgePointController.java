package com.ice.exebackend.controller;


import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizQuestionKnowledgePoint; // 【新增】 导入关联表实体
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.KnowledgePointStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.service.BizKnowledgePointService;
import com.ice.exebackend.service.BizQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/knowledge-points")
@PreAuthorize("hasAuthority('sys:kp:list')") // 类级别权限
public class BizKnowledgePointController {

    @Autowired
    private BizKnowledgePointService knowledgePointService;


    // 【新增】 注入关联表的Mapper
    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;
    // 【新增】注入 BizQuestionService
    @Autowired
    private BizQuestionService questionService;

    /**
     * 新增知识点
     */
    @PostMapping
    public Result createKnowledgePoint(@RequestBody BizKnowledgePoint knowledgePoint) {
        boolean success = knowledgePointService.save(knowledgePoint);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 分页获取知识点列表
     */
    /**
     * 分页获取知识点列表 (已更新为包含统计数据)
     */
    @GetMapping
    public Result getKnowledgePointList(@RequestParam(defaultValue = "1") int current,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) Long subjectId,
                                        @RequestParam(required = false) String name) {
        Page<BizKnowledgePoint> page = new Page<>(current, size);
        QueryWrapper<BizKnowledgePoint> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("create_time");

        // 2. 调用新的 service 方法
        Page<KnowledgePointStatsDTO> statsPage = knowledgePointService.getKnowledgePointStatsPage(page, queryWrapper);
        return Result.suc(statsPage.getRecords(), statsPage.getTotal());
    }

    /**
     * 获取单个知识点详情
     */
    @GetMapping("/{id}")
    public Result getKnowledgePointById(@PathVariable Long id) {
        BizKnowledgePoint knowledgePoint = knowledgePointService.getById(id);
        return Result.suc(knowledgePoint);
    }


    /**
     * 更新知识点信息
     */
    @PutMapping("/{id}")
    public Result updateKnowledgePoint(@PathVariable Long id, @RequestBody BizKnowledgePoint knowledgePoint) {
        knowledgePoint.setId(id);
        boolean success = knowledgePointService.updateById(knowledgePoint);
        return success ? Result.suc() : Result.fail();
    }


    /**
     * 删除知识点 - 【已优化】
     */
    @DeleteMapping("/{id}")
    public Result deleteKnowledgePoint(@PathVariable Long id) {
        // 【新增】 安全删除检查
        // 1. 查询该知识点是否已关联任何试题
        QueryWrapper<BizQuestionKnowledgePoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("knowledge_point_id", id);
        Long associatedQuestionCount = questionKnowledgePointMapper.selectCount(queryWrapper);

        // 2. 如果有关联的试题，则阻止删除并返回错误信息
        if (associatedQuestionCount > 0) {
            return Result.fail("无法删除：该知识点已关联 " + associatedQuestionCount + " 个试题。");
        }

        // 3. 如果没有关联，则执行删除
        boolean success = knowledgePointService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }
    // ... 在 deleteKnowledgePoint 方法之后，添加以下新方法
    /**
     * 【新增】根据知识点ID获取其关联的所有试题
     */
    @GetMapping("/{id}/questions")
    public Result getQuestionsForKnowledgePoint(@PathVariable Long id) {
        // 1. 从关联表中找出所有与该知识点ID相关的记录
        List<BizQuestionKnowledgePoint> relations = questionKnowledgePointMapper.selectList(
                new QueryWrapper<BizQuestionKnowledgePoint>().eq("knowledge_point_id", id)
        );

        // 如果没有关联的试题，直接返回空列表
        if (relations.isEmpty()) {
            return Result.suc(Collections.emptyList());
        }

        // 2. 提取所有试题的ID
        List<Long> questionIds = relations.stream()
                .map(BizQuestionKnowledgePoint::getQuestionId)
                .collect(Collectors.toList());

        // 3. 根据试题ID列表，一次性查询出所有试题详情
        List<BizQuestion> questions = questionService.listByIds(questionIds);
        return Result.suc(questions);
    }
}
