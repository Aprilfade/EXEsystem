package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.entity.BizPaper;
import com.ice.exebackend.service.BizPaperService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // 1. 导入 RedisTemplate
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/papers")
@PreAuthorize("hasAuthority('sys:paper:list')")
public class BizPaperController {

    @Autowired
    private BizPaperService paperService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 3. 定义缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @PreAuthorize("hasAuthority('sys:paper:create')")
    public Result createPaper(@RequestBody PaperDTO paperDTO) {
        boolean success = paperService.createPaperWithQuestions(paperDTO);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    // ... [此处省略所有 GET 查询和导出方法，它们不需要修改] ...
    @GetMapping
    public Result getPaperList(@RequestParam(defaultValue = "1") int current,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Long subjectId,
                               @RequestParam(required = false) String grade) {
        Page<BizPaper> page = new Page<>(current, size);
        QueryWrapper<BizPaper> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        if (StringUtils.hasText(grade)) {
            queryWrapper.eq("grade", grade);
        }
        queryWrapper.orderByDesc("create_time");
        paperService.page(page, queryWrapper);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    @GetMapping("/{id}")
    public Result getPaperById(@PathVariable Long id) {
        PaperDTO paperDTO = paperService.getPaperWithQuestionsById(id);
        return Result.suc(paperDTO);
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportPaper(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean includeAnswers) {
        try (XWPFDocument document = paperService.exportPaperToWord(id, includeAnswers);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PaperDTO paper = paperService.getPaperWithQuestionsById(id);
            String fileName = URLEncoder.encode(paper.getName() + ".docx", StandardCharsets.UTF_8.toString());

            document.write(out);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:paper:update')")
    public Result updatePaper(@PathVariable Long id, @RequestBody PaperDTO paperDTO) {
        paperDTO.setId(id);
        boolean success = paperService.updatePaperWithQuestions(paperDTO);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:paper:delete')")
    public Result deletePaper(@PathVariable Long id) {
        boolean success = paperService.removeById(id);
        if (success) {
            // 4. 操作成功后，清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }
    /**
     * 【新增】修改试卷状态（发布/下架）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:paper:update')")
    public Result updatePaperStatus(@PathVariable Long id, @RequestParam Integer status) {
        BizPaper paper = new BizPaper();
        paper.setId(id);
        paper.setStatus(status);
        boolean success = paperService.updateById(paper);

        // 清除缓存
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }

        return success ? Result.suc() : Result.fail();
    }
}