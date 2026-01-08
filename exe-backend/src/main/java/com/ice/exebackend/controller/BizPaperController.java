package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.dto.PaperKnowledgePointDTO;
import com.ice.exebackend.dto.SmartPaperReq;
import com.ice.exebackend.entity.BizPaper;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.BizPaperService;
import com.ice.exebackend.service.PdfService; // 【1. 新增导入】
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/papers")
@PreAuthorize("hasAuthority('sys:paper:list')")
public class BizPaperController {

    private static final Logger logger = LoggerFactory.getLogger(BizPaperController.class);

    @Autowired
    private BizPaperService paperService;
    // 【2. 新增注入 PdfService】 解决 "找不到符号 pdfService" 的错误
    @Autowired
    private PdfService pdfService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 3. 定义缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    @PreAuthorize("hasAuthority('sys:paper:create')")
    @Log(title = "试卷管理", businessType = BusinessType.INSERT) // 新增
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
                               @RequestParam(required = false) String grade,
                               @RequestParam(required = false) String keyword) {  // ✅ 新增keyword参数
        Page<BizPaper> page = new Page<>(current, size);
        QueryWrapper<BizPaper> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        if (StringUtils.hasText(grade)) {
            queryWrapper.eq("grade", grade);
        }
        // ✅ 新增：支持按试卷名称或编码搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like("name", keyword)
                .or()
                .like("code", keyword)
            );
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

    /**
     * 【知识点功能增强】获取试卷的知识点分布
     */
    @GetMapping("/{id}/knowledge-points")
    @PreAuthorize("hasAuthority('sys:paper:list')")
    public Result<List<PaperKnowledgePointDTO>> getPaperKnowledgePoints(@PathVariable Long id) {
        List<PaperKnowledgePointDTO> knowledgePoints = paperService.getPaperKnowledgePoints(id);
        return Result.ok(knowledgePoints);
    }

    @GetMapping("/export/{id}")
    @Log(title = "试卷管理", businessType = BusinessType.EXPORT) // 导出Word
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
    @Log(title = "试卷管理", businessType = BusinessType.UPDATE) // 修改
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
    @Log(title = "试卷管理", businessType = BusinessType.DELETE) // 删除
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
    @Log(title = "试卷管理", businessType = BusinessType.UPDATE) // 状态变更
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
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('sys:paper:create')")
    @Log(title = "试卷管理", businessType = BusinessType.OTHER) // 智能组卷 (属于操作但不是简单的增删改)
    public Result generateSmartPaper(@RequestBody SmartPaperReq req) {
        if (req.getSubjectId() == null) {
            return Result.fail("请选择科目");
        }
        List<PaperDTO.PaperGroupDTO> groups = paperService.generateSmartPaper(req);
        if (groups.isEmpty()) {
            return Result.fail("未找到符合条件的题目，请检查题库或调整条件");
        }
        return Result.suc(groups);
    }
    /**
     * 【新增】导出 PDF 试卷
     */
    @GetMapping("/export/pdf/{id}")
    @Log(title = "试卷管理", businessType = BusinessType.EXPORT) // 导出PDF
    public ResponseEntity<byte[]> exportPaperPdf(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean includeAnswers) {
        try (ByteArrayOutputStream out = pdfService.generatePaperPdf(id, includeAnswers, "在线学习系统专用")) {

            // 获取文件名
            PaperDTO paper = paperService.getPaperWithQuestionsById(id);
            String fileName = URLEncoder.encode(paper.getName() + ".pdf", StandardCharsets.UTF_8.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());

        } catch (Exception e) {
            logger.error("导出PDF试卷失败, paperId: {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }
    /**
     * 【新增】导出答题卡 PDF
     */
    @GetMapping("/export/answer-sheet/{id}")
    @Log(title = "试卷管理", businessType = BusinessType.EXPORT)
    public ResponseEntity<byte[]> exportAnswerSheet(@PathVariable Long id) {
        try (ByteArrayOutputStream out = pdfService.generateAnswerSheetPdf(id)) {

            PaperDTO paper = paperService.getPaperWithQuestionsById(id);
            String fileName = URLEncoder.encode(paper.getName() + "_答题卡.pdf", StandardCharsets.UTF_8.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());

        } catch (Exception e) {
            logger.error("导出答题卡PDF失败, paperId: {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

}