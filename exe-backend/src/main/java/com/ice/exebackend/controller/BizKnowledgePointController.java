package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizKnowledgePoint;
import com.ice.exebackend.service.BizKnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge-points")
public class BizKnowledgePointController {

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    /**
     * 新增知识点
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result createKnowledgePoint(@RequestBody BizKnowledgePoint knowledgePoint) {
        boolean success = knowledgePointService.save(knowledgePoint);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 分页获取知识点列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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

        knowledgePointService.page(page, queryWrapper);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    /**
     * 获取单个知识点详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result getKnowledgePointById(@PathVariable Long id) {
        BizKnowledgePoint knowledgePoint = knowledgePointService.getById(id);
        return Result.suc(knowledgePoint);
    }


    /**
     * 更新知识点信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result updateKnowledgePoint(@PathVariable Long id, @RequestBody BizKnowledgePoint knowledgePoint) {
        knowledgePoint.setId(id);
        boolean success = knowledgePointService.updateById(knowledgePoint);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 删除知识点
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result deleteKnowledgePoint(@PathVariable Long id) {
        // TODO: 在删除前可以增加检查，例如该知识点是否已关联试题
        boolean success = knowledgePointService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }
}