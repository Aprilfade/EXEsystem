package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.entity.BizPaper;
import com.ice.exebackend.service.BizPaperService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/papers")
// 【重要修改】将 hasAnyRole 修改为 hasAuthority
@PreAuthorize("hasAuthority('sys:paper:list')")
public class BizPaperController {

    @Autowired
    private BizPaperService paperService;

    @PostMapping
    @PreAuthorize("hasAuthority('sys:paper:create')") // 【新增】应用创建权限
    public Result createPaper(@RequestBody PaperDTO paperDTO) {
        boolean success = paperService.createPaperWithQuestions(paperDTO);
        return success ? Result.suc() : Result.fail();
    }

    @GetMapping
    public Result getPaperList(@RequestParam(defaultValue = "1") int current,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Long subjectId,
                               // 【新增此行】
                               @RequestParam(required = false) String grade) {
        Page<BizPaper> page = new Page<>(current, size);
        QueryWrapper<BizPaper> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        // 【新增代码块】
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:paper:update')") // 【新增】应用更新权限
    public Result updatePaper(@PathVariable Long id, @RequestBody PaperDTO paperDTO) {
        paperDTO.setId(id);
        boolean success = paperService.updatePaperWithQuestions(paperDTO);
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:paper:delete')") // 【新增】应用删除权限
    public Result deletePaper(@PathVariable Long id) {
        boolean success = paperService.removeById(id);
        return success ? Result.suc() : Result.fail();
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
            // Log the exception
            return ResponseEntity.status(500).build();
        }
    }
}