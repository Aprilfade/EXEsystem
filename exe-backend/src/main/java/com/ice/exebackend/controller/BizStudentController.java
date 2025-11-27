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
import org.springframework.data.redis.core.RedisTemplate; // 1. 导入 RedisTemplate
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ice.exebackend.entity.BizWrongRecord; // 【新增】 导入错题记录实体
import com.ice.exebackend.service.BizWrongRecordService; // 【新增】 导入错题记录服务
import com.ice.exebackend.entity.BizLearningActivity; // 【新增】
import com.ice.exebackend.service.BizLearningActivityService; // 【新增】
import java.time.LocalDateTime; // 【新增】
import java.util.Map; // 【新增】

import java.io.IOException;
import java.net.URLEncoder; // 导入 URLEncoder
import java.util.List;
@RestController
@RequestMapping("/api/v1/students")
@PreAuthorize("hasAuthority('sys:student:list')")
public class BizStudentController {

    @Autowired
    private BizStudentService studentService;

    // 【新增】 注入错题记录服务
    @Autowired
    private BizWrongRecordService wrongRecordService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 【新增】注入学习活动服务，用于记录日志
    @Autowired
    private BizLearningActivityService learningActivityService;

    // 3. 定义缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @PostMapping
    public Result createStudent(@RequestBody BizStudent student) {
        boolean success = studentService.save(student);
        if (success) {
            // 4. 数据变更成功后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
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
    public Result updateStudent(@PathVariable Long id, @RequestBody BizStudent student) {
        student.setId(id);
        boolean success = studentService.updateById(student);
        if (success) {
            // 4. 数据变更成功后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 删除学生 - 【已优化】
     */
    @DeleteMapping("/{id}")
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
        if (success) {
            // 4. 数据变更成功后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    @PostMapping("/import")
    public Result importStudents(@RequestParam("file") MultipartFile file,
                                 @RequestParam("subjectId") Long subjectId) {
        try {
            studentService.importStudents(file, subjectId);
            // 4. 数据变更成功后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
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
    /**
     * 【新增】教师手动给学生增加/扣除积分
     */
    @PostMapping("/{id}/points")
    public Result addPoints(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 从请求体获取积分值和备注
        Integer points = (Integer) body.get("points");
        String remark = (String) body.get("remark");

        if (points == null || points == 0) {
            return Result.fail("积分数值无效");
        }

        BizStudent student = studentService.getById(id);
        if (student == null) {
            return Result.fail("学生不存在");
        }

        // 更新积分
        int currentPoints = student.getPoints() == null ? 0 : student.getPoints();
        student.setPoints(currentPoints + points);
        boolean success = studentService.updateById(student);

        if (success) {
            // 记录日志，这样学生在个人中心能看到
            BizLearningActivity log = new BizLearningActivity();
            log.setStudentId(id);
            log.setActivityType("TEACHER_REWARD"); // 设置特定类型

            // 构建描述信息
            String action = points > 0 ? "奖励" : "扣除";
            String desc = String.format("教师手动%s积分: %d", action, Math.abs(points));
            if (remark != null && !remark.isEmpty()) {
                desc += "，原因：" + remark;
            }
            log.setDescription(desc);

            log.setCreateTime(LocalDateTime.now());
            learningActivityService.save(log);

            // 清除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }

        return success ? Result.suc() : Result.fail("操作失败");
    }
}

