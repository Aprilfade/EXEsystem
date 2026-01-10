package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.KnowledgePointBatchUpdateDTO;
import com.ice.exebackend.dto.KnowledgePointStatsDTO;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizQuestionKnowledgePoint;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.ice.exebackend.service.BizKnowledgePointService;
import com.ice.exebackend.service.BizQuestionService;
import com.ice.exebackend.service.AiServiceV3; // 【修改】使用 AiServiceV3
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest; // 【新增】导入 HttpServletRequest
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/knowledge-points")
// 移除类级别权限注解，改为在各个方法上单独添加
public class BizKnowledgePointController {

    private static final Logger logger = LoggerFactory.getLogger(BizKnowledgePointController.class);

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Autowired
    private BizQuestionService questionService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 【修改】注入 AiServiceV3
    @Autowired
    private AiServiceV3 aiServiceV3;

    // 【新增】注入 SysUserService 用于获取用户信息
    @Autowired
    private SysUserService sysUserService;

    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @PreAuthorize("hasAuthority('sys:kp:create')") // ✅ 添加细粒度权限
    @Log(title = "知识点管理", businessType = BusinessType.INSERT)
    public Result createKnowledgePoint(@RequestBody BizKnowledgePoint knowledgePoint) {
        boolean success = knowledgePointService.save(knowledgePoint);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('sys:kp:list', 'sys:paper:list', 'sys:question:list')")
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

    /**
     * 【知识点功能增强】获取知识点全局统计数据
     */
    @GetMapping("/global-stats")
    @PreAuthorize("hasAuthority('sys:kp:list')")
    public Result<Map<String, Object>> getGlobalStats() {
        Map<String, Object> stats = knowledgePointService.getGlobalStats();
        return Result.ok(stats);
    }

    /**
     * 【新增】获取所有知识点（不分页）
     * 允许有知识点权限或试卷权限的用户访问
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('sys:kp:list', 'sys:paper:list', 'sys:paper:create')")
    public Result getAllKnowledgePoints(@RequestParam(required = false) Long subjectId) {
        QueryWrapper<BizKnowledgePoint> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        queryWrapper.orderByDesc("create_time");
        List<BizKnowledgePoint> list = knowledgePointService.list(queryWrapper);
        return Result.suc(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('sys:kp:list', 'sys:paper:list', 'sys:question:list')")
    public Result getKnowledgePointById(@PathVariable Long id) {
        BizKnowledgePoint knowledgePoint = knowledgePointService.getById(id);
        return Result.suc(knowledgePoint);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:kp:update')") // ✅ 添加细粒度权限
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
    @PreAuthorize("hasAuthority('sys:kp:delete')") // ✅ 添加细粒度权限
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
    @PreAuthorize("hasAnyAuthority('sys:kp:list', 'sys:question:list')")
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
    @PreAuthorize("hasAnyAuthority('sys:kp:list', 'sys:paper:list')")
    public Result getGraph(@PathVariable Long subjectId) {
        return Result.suc(knowledgePointService.getKnowledgeGraph(subjectId));
    }

    @PostMapping("/relation")
    @PreAuthorize("hasAuthority('sys:kp:update')")
    @Log(title = "知识点管理", businessType = BusinessType.UPDATE)
    public Result addRelation(@RequestBody Map<String, Long> params) {
        Long parentId = params.get("parentId");
        Long childId = params.get("childId");
        boolean success = knowledgePointService.addRelation(parentId, childId);
        return success ? Result.suc() : Result.fail("关联失败或已存在");
    }

    @PostMapping("/relation/delete")
    @PreAuthorize("hasAuthority('sys:kp:update')")
    @Log(title = "知识点管理", businessType = BusinessType.DELETE)
    public Result removeRelation(@RequestBody Map<String, Long> params) {
        Long parentId = params.get("parentId");
        Long childId = params.get("childId");
        knowledgePointService.removeRelation(parentId, childId);
        return Result.suc();
    }

    @GetMapping("/{id}/prerequisites")
    @PreAuthorize("hasAnyAuthority('sys:kp:list', 'sys:paper:list')")
    public Result getPrerequisites(@PathVariable Long id) {
        return Result.suc(knowledgePointService.findPrerequisitePoints(id));
    }

    /**
     * 【批量编辑】批量更新知识点
     */
    @PutMapping("/batch-update")
    @PreAuthorize("hasAuthority('sys:kp:update')")
    @Log(title = "知识点管理", businessType = BusinessType.UPDATE)
    public Result batchUpdateKnowledgePoints(@RequestBody KnowledgePointBatchUpdateDTO dto) {
        boolean success = knowledgePointService.batchUpdateKnowledgePoints(dto);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc("批量更新成功") : Result.fail("批量更新失败");
    }

    // 【修改】AI 智能生成知识点 - 使用 AiServiceV3
    @PostMapping("/ai-generate")
    @PreAuthorize("hasAnyAuthority('sys:kp:create', 'sys:paper:create')")
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

        // 获取当前登录用户ID
        Long userId = null;
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
            if (user != null) {
                userId = user.getId();
            }
        } catch (Exception e) {
            logger.warn("获取用户ID失败，将使用匿名模式", e);
        }

        try {
            // 使用 AiServiceV3，支持大文本分块、缓存、限流和日志记录
            List<Map<String, String>> points = aiServiceV3.generateKnowledgePointsFromText(
                    apiKey, provider, text,
                    count == null ? 5 : count,
                    userId
            );
            return Result.suc(points);
        } catch (Exception e) {
            logger.error("AI生成知识点失败", e);
            return Result.fail("生成失败: " + e.getMessage());
        }
    }

    // 【新增】AI 智能生成知识点（流式版本）
    @PostMapping("/ai-generate-stream")
    @PreAuthorize("hasAnyAuthority('sys:kp:create', 'sys:paper:create')")
    @Log(title = "知识点管理", businessType = BusinessType.OTHER)
    public SseEmitter generateKnowledgePointsStream(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String text = (String) params.get("text");
        Integer count = (Integer) params.get("count");

        // 从 Header 获取 Key
        String apiKey = request.getHeader("X-Ai-Api-Key");
        String provider = request.getHeader("X-Ai-Provider");

        if (!StringUtils.hasText(apiKey)) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("请在设置中配置 AI API Key"));
            return emitter;
        }

        // 获取当前登录用户ID
        Long userId = null;
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            SysUser user = sysUserService.lambdaQuery().eq(SysUser::getUsername, username).one();
            if (user != null) {
                userId = user.getId();
            }
        } catch (Exception e) {
            logger.warn("获取用户ID失败，将使用匿名模式", e);
        }

        try {
            // 使用 AiServiceV3 的流式方法
            return aiServiceV3.generateKnowledgePointsFromTextStream(
                    apiKey, provider, text,
                    count == null ? 5 : count,
                    userId
            );
        } catch (Exception e) {
            logger.error("AI生成知识点（流式）失败", e);
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(e);
            return emitter;
        }
    }
}