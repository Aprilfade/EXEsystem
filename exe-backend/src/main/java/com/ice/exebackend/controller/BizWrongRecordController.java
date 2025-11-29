package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.PaperStatsVO;
import com.ice.exebackend.dto.WrongRecordDTO;
import com.ice.exebackend.dto.WrongRecordVO;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizWrongRecord;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.BizWrongRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication; // 确保导入
import com.ice.exebackend.entity.BizStudent; // 确保导入
import com.ice.exebackend.service.BizStudentService; // 确保导入

import java.util.List;

@RestController
@RequestMapping("/api/v1/wrong-records")
// 【重要修改】更新权限注解
@PreAuthorize("hasAuthority('sys:wrong:list')")
public class BizWrongRecordController {

    @Autowired
    private BizWrongRecordService wrongRecordService;

    @Autowired
    private BizStudentService studentService;

    @PostMapping
    @Log(title = "错题管理", businessType = BusinessType.INSERT)
    public Result createRecord(@RequestBody WrongRecordDTO dto) {
        boolean success = wrongRecordService.createWrongRecord(dto);
        return success ? Result.suc() : Result.fail("创建失败，参数不完整");
    }

    @GetMapping
    public Result getRecordList(@RequestParam(defaultValue = "1") int current,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) Long studentId,
                                @RequestParam(required = false) Long questionId) {
        Page<WrongRecordVO> page = new Page<>(current, size);
        wrongRecordService.getWrongRecordPage(page, studentId, questionId);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    @PutMapping("/{id}")
    @Log(title = "错题管理", businessType = BusinessType.UPDATE)
    public Result updateRecord(@PathVariable Long id, @RequestBody BizWrongRecord record) {
        record.setId(id);
        boolean success = wrongRecordService.updateById(record);
        return success ? Result.suc() : Result.fail();
    }

    @DeleteMapping("/{id}")
    @Log(title = "错题管理", businessType = BusinessType.DELETE)
    public Result deleteRecord(@PathVariable Long id) {
        boolean success = wrongRecordService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }

    // --- 统计API ---
    @GetMapping("/stats/by-student")
    public Result getRecordsByStudent(@RequestParam Long studentId) {
        Page<WrongRecordVO> page = wrongRecordService.getWrongRecordPage(new Page<>(1, 999), studentId, null);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    @GetMapping("/stats/by-question")
    public Result getStudentsByQuestion(@RequestParam Long questionId) {
        Page<WrongRecordVO> page = wrongRecordService.getWrongRecordPage(new Page<>(1, 999), null, questionId);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    @GetMapping("/stats/by-paper")
    public Result getStatsByPaper(@RequestParam Long paperId) {
        List<PaperStatsVO> stats = wrongRecordService.getPaperErrorStats(paperId);
        return Result.suc(stats);
    }
}

