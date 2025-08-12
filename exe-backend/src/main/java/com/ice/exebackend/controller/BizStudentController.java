package com.ice.exebackend.controller;

import com.alibaba.excel.EasyExcel; // 导入 EasyExcel
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.StudentExportDTO;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;
import jakarta.servlet.http.HttpServletResponse; // 导入 HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ice.exebackend.entity.BizWrongRecord; // 【新增】 导入错题记录实体
import com.ice.exebackend.service.BizWrongRecordService; // 【新增】 导入错题记录服务

import java.io.IOException;
import java.net.URLEncoder; // 导入 URLEncoder
import java.util.List;
@RestController
@RequestMapping("/api/v1/students")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class BizStudentController {

    @Autowired
    private BizStudentService studentService;

    // 【新增】 注入错题记录服务
    @Autowired
    private BizWrongRecordService wrongRecordService;

    @PostMapping
    //@CacheEvict(value = "dashboardStats", allEntries = true) // 【新增】清除缓存
    public Result createStudent(@RequestBody BizStudent student) {
        boolean success = studentService.save(student);
        return success ? Result.suc() : Result.fail("学号已存在");
    }

    @GetMapping
    public Result getStudentList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) Long subjectId,
                                 @RequestParam(required = false) String name) {
        Page<BizStudent> page = new Page<>(current, size);
        QueryWrapper<BizStudent> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("create_time");

        studentService.page(page, queryWrapper);
        return Result.suc(page.getRecords(), page.getTotal());
    }

    @PutMapping("/{id}")
    //@CacheEvict(value = "dashboardStats", allEntries = true) // 【新增】清除缓存
    public Result updateStudent(@PathVariable Long id, @RequestBody BizStudent student) {
        student.setId(id);
        boolean success = studentService.updateById(student);
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 删除学生 - 【已优化】
     */
    @DeleteMapping("/{id}")
   // @CacheEvict(value = "dashboardStats", allEntries = true) // 【新增】清除缓存
    public Result deleteStudent(@PathVariable Long id) {
        // 【新增】 安全删除检查
        // 1. 检查该学生是否已存在错题记录
        long wrongRecordCount = wrongRecordService.count(new QueryWrapper<BizWrongRecord>().eq("student_id", id));

        // 2. 如果存在错题记录，则阻止删除
        if (wrongRecordCount > 0) {
            return Result.fail("无法删除：该学生名下存在 " + wrongRecordCount + " 条错题记录。");
        }

        // 3. 如果没有，则执行删除
        boolean success = studentService.removeById(id);
        return success ? Result.suc() : Result.fail();
    }

    @PostMapping("/import")
   // @CacheEvict(value = "dashboardStats", allEntries = true) // 【新增】清除缓存
    public Result importStudents(@RequestParam("file") MultipartFile file,
                                 @RequestParam("subjectId") Long subjectId) {
        try {
            studentService.importStudents(file, subjectId);
            return Result.suc();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("导入失败：" + e.getMessage());
        }
    }
    /**
     * 【新增】导出学生列表为Excel
     */
    @GetMapping("/export")
    public void exportStudents(HttpServletResponse response,
                               @RequestParam(required = false) Long subjectId,
                               @RequestParam(required = false) String name) throws IOException {

        List<StudentExportDTO> list = studentService.getStudentExportList(subjectId, name);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 设置文件名
        String fileName = URLEncoder.encode("学生列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入Excel文件
        EasyExcel.write(response.getOutputStream(), StudentExportDTO.class)
                .sheet("学生数据")
                .doWrite(list);
    }

}