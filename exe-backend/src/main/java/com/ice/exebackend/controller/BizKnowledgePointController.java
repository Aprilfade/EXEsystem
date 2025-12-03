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
import com.ice.exebackend.service.AiService; // 【新增】导入 AiService
import jakarta.servlet.http.HttpServletRequest; // 【新增】导入 HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 【新增】注入 AiService
    @Autowired
    private AiService aiService;

    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @Log(title = "知识点管理", businessType = BusinessType.INSERT)
    public Result createKnowledgePoint(@RequestBody BizKnowledgePoint knowledgePoint) {
        boolean success = knowledgePointService.save(knowledgePoint);
        if (success) {
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

        // 【核心修改】支持名称、编码、标签的综合模糊搜索
        if (StringUtils.hasText(name)) {
            queryWrapper.and(w -> w.like("name", name)
                    .or().like("code", name)
                    .or().like("tags", name));
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

    @GetMapping("/graph/{subjectId}")
    public Result getGraph(@PathVariable Long subjectId) {
        return Result.suc(knowledgePointService.getKnowledgeGraph(subjectId));
    }

    @PostMapping("/relation")
    @Log(title = "知识点管理", businessType = BusinessType.UPDATE)
    public Result addRelation(@RequestBody Map<String, Long> params) {
        Long parentId = params.get("parentId");
        Long childId = params.get("childId");
        boolean success = knowledgePointService.addRelation(parentId, childId);
        return success ? Result.suc() : Result.fail("关联失败或已存在");
    }

    @PostMapping("/relation/delete")
    @Log(title = "知识点管理", businessType = BusinessType.DELETE)
    public Result removeRelation(@RequestBody Map<String, Long> params) {
        Long parentId = params.get("parentId");
        Long childId = params.get("childId");
        knowledgePointService.removeRelation(parentId, childId);
        return Result.suc();
    }

    @GetMapping("/{id}/prerequisites")
    public Result getPrerequisites(@PathVariable Long id) {
        return Result.suc(knowledgePointService.findPrerequisitePoints(id));
    }

    // 【新增】AI 智能生成知识点 (注意：这段代码必须在类的最后一个大括号之前)
    @PostMapping("/ai-generate")
    @Log(title = "知识点管理", businessType = BusinessType.OTHER)
    public Result generateKnowledgePoints(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String text = (String) params.get("text");
        Integer count = (Integer) params.get("count");

        // 从 Header 获取 Key
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            return Result.fail("请在设置中配置 AI API Key");
        }

        try {
            List<Map<String, String>> points = aiService.generateKnowledgePointsFromText(
                    apiKey, provider, text,
                    count == null ? 5 : count
            );
            return Result.suc(points);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("生成失败: " + e.getMessage());
        }
    }
}