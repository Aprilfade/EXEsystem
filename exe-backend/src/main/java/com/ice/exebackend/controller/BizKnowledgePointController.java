package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.KnowledgePointStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizQuestionKnowledgePoint;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.ice.exebackend.service.BizKnowledgePointService;
import com.ice.exebackend.service.BizQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // 1. 导入 RedisTemplate
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/knowledge-points")
@PreAuthorize("hasAuthority('sys:kp:list')")
public class BizKnowledgePointController {

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Autowired
    private BizQuestionService questionService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 3. 定义缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @Log(title = "知识点管理", businessType = BusinessType.INSERT)
    public Result createKnowledgePoint(@RequestBody BizKnowledgePoint knowledgePoint) {
        boolean success = knowledgePointService.save(knowledgePoint);
        if (success) {
            // 4. 当成功创建一个新知识点后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

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

        Page<KnowledgePointStatsDTO> statsPage = knowledgePointService.getKnowledgePointStatsPage(page, queryWrapper);
        return Result.suc(statsPage.getRecords(), statsPage.getTotal());
    }

    @GetMapping("/{id}")
    public Result getKnowledgePointById(@PathVariable Long id) {
        BizKnowledgePoint knowledgePoint = knowledgePointService.getById(id);
        return Result.suc(knowledgePoint);
    }

    @PutMapping("/{id}")
    @Log(title = "知识点管理", businessType = BusinessType.UPDATE)
    public Result updateKnowledgePoint(@PathVariable Long id, @RequestBody BizKnowledgePoint knowledgePoint) {
        knowledgePoint.setId(id);
        boolean success = knowledgePointService.updateById(knowledgePoint);
        if (success) {
            // 4. 更新知识点后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    @Log(title = "知识点管理", businessType = BusinessType.DELETE)
    public Result deleteKnowledgePoint(@PathVariable Long id) {
        QueryWrapper<BizQuestionKnowledgePoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("knowledge_point_id", id);
        Long associatedQuestionCount = questionKnowledgePointMapper.selectCount(queryWrapper);

        if (associatedQuestionCount > 0) {
            return Result.fail("无法删除：该知识点已关联 " + associatedQuestionCount + " 个试题。");
        }

        boolean success = knowledgePointService.removeById(id);
        if (success) {
            // 4. 成功删除知识点后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @GetMapping("/{id}/questions")
    public Result getQuestionsForKnowledgePoint(@PathVariable Long id) {
        List<BizQuestionKnowledgePoint> relations = questionKnowledgePointMapper.selectList(
                new QueryWrapper<BizQuestionKnowledgePoint>().eq("knowledge_point_id", id)
        );

        if (relations.isEmpty()) {
            return Result.suc(Collections.emptyList());
        }

        List<Long> questionIds = relations.stream()
                .map(BizQuestionKnowledgePoint::getQuestionId)
                .collect(Collectors.toList());

        List<BizQuestion> questions = questionService.listByIds(questionIds);
        return Result.suc(questions);
    }
    // 获取图谱数据
    @GetMapping("/graph/{subjectId}")
    public Result getGraph(@PathVariable Long subjectId) {
        return Result.suc(knowledgePointService.getKnowledgeGraph(subjectId));
    }

    // 建立关联
    @PostMapping("/relation")
    @Log(title = "知识点管理", businessType = BusinessType.UPDATE)
    public Result addRelation(@RequestBody Map<String, Long> params) {
        Long parentId = params.get("parentId");
        Long childId = params.get("childId");
        boolean success = knowledgePointService.addRelation(parentId, childId);
        return success ? Result.suc() : Result.fail("关联失败或已存在");
    }

    // 删除关联
    @PostMapping("/relation/delete")
    @Log(title = "知识点管理", businessType = BusinessType.DELETE)
    public Result removeRelation(@RequestBody Map<String, Long> params) {
        Long parentId = params.get("parentId");
        Long childId = params.get("childId");
        knowledgePointService.removeRelation(parentId, childId);
        return Result.suc();
    }

    // 【给学生端用】获取某知识点的前置知识点建议
    @GetMapping("/{id}/prerequisites")
    public Result getPrerequisites(@PathVariable Long id) {
        return Result.suc(knowledgePointService.findPrerequisitePoints(id));
    }
}