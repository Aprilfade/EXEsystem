package com.ice.exebackend.controller;

import com.alibaba.excel.EasyExcel; // 导入 EasyExcel
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.annotation.Log;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.StudentExportDTO;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.enums.BusinessType;
import com.ice.exebackend.service.BizStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse; // 导入 HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // 1. 导入 RedisTemplate
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ice.exebackend.entity.BizWrongRecord; // 【新增】 导入错题记录实体
import com.ice.exebackend.service.BizWrongRecordService; // 【新增】 导入错题记录服务
import com.ice.exebackend.entity.BizExamResult; // 【新增】 导入考试成绩实体
import com.ice.exebackend.service.BizExamResultService; // 【新增】 导入考试成绩服务
import com.ice.exebackend.entity.BizLearningActivity; // 【新增】
import com.ice.exebackend.service.BizLearningActivityService; // 【新增】
import java.time.LocalDateTime; // 【新增】
import java.util.Map; // 【新增】
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder; // 导入 URLEncoder
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "学生管理", description = "学生管理相关接口，包括增删改查、批量导入导出、积分管理等")
@RestController
@RequestMapping("/api/v1/students")
@PreAuthorize("hasAuthority('sys:student:list')")
public class BizStudentController {

    private static final Logger logger = LoggerFactory.getLogger(BizStudentController.class);

    @Autowired
    private BizStudentService studentService;

    // 【新增】 注入错题记录服务
    @Autowired
    private BizWrongRecordService wrongRecordService;

    // 【新增】注入考试成绩服务
    @Autowired
    private BizExamResultService examResultService;

    // 2. 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 【新增】注入学习活动服务，用于记录日志
    @Autowired
    private BizLearningActivityService learningActivityService;

    // 2. 【新增】注入 PasswordEncoder
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 3. 定义缓存键常量
    private static final String DASHBOARD_CACHE_KEY = "dashboard:stats:all";

    @Operation(summary = "创建学生", description = "新增一个学生，默认密码为学号后6位（BCrypt加密）")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:student:create')") // ✅ 添加细粒度权限
    @Log(title = "学生管理", businessType = BusinessType.INSERT)
    public Result createStudent(@RequestBody BizStudent student) {
        // 3. 【核心修复】如果密码不为空，进行加密处理
        // ✅ 优化：默认密码使用学号后6位（如果学号不足6位则使用全部）
        if (student.getPassword() == null || student.getPassword().isEmpty()) {
            String studentNo = student.getStudentNo();
            String defaultPassword = studentNo != null && studentNo.length() >= 6
                    ? studentNo.substring(studentNo.length() - 6)
                    : (studentNo != null ? studentNo : "123456");
            student.setPassword(defaultPassword);
        }

        // ✅ 新增：密码长度验证（至少6位）
        if (student.getPassword().length() < 6) {
            return Result.fail("密码长度至少为6位");
        }

        // 对密码进行 BCrypt 加密
        student.setPassword(passwordEncoder.encode(student.getPassword()));

        boolean success = studentService.save(student);
        if (success) {
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail("学号已存在");
    }

    @Operation(summary = "获取学生列表", description = "分页查询学生列表，支持按科目ID和姓名筛选")
    @GetMapping
    public Result getStudentList(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "科目ID（可选）") @RequestParam(required = false) Long subjectId,
            @Parameter(description = "学生姓名（模糊搜索，可选）") @RequestParam(required = false) String name) {
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

    /**
     * 【新增】获取学生管理统计数据
     */
    @Operation(summary = "获取学生统计数据", description = "获取学生总数、信息完整度、账号激活率等统计数据")
    @GetMapping("/stats")
    public Result getStudentStats() {
        // 1. 总学生数
        long totalCount = studentService.count();

        // 2. 计算信息完整度（检查姓名、学号、年级、联系方式是否都填写）
        long completeInfoCount = studentService.count(
                new QueryWrapper<BizStudent>()
                        .isNotNull("name")
                        .isNotNull("student_no")
                        .isNotNull("grade")
                        .isNotNull("contact")
                        .ne("name", "")
                        .ne("student_no", "")
                        .ne("grade", "")
                        .ne("contact", "")
        );
        int completenessRate = totalCount > 0 ? (int) Math.round((double) completeInfoCount / totalCount * 100) : 0;

        // 3. 计算账号激活率（假设：有考试记录或学习活动记录的学生视为已激活）
        // 获取所有有考试记录的学生ID
        List<Long> activeStudentIds = examResultService.list()
                .stream()
                .map(BizExamResult::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        // 获取所有有学习活动的学生ID
        List<Long> activeByActivityIds = learningActivityService.list()
                .stream()
                .map(BizLearningActivity::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        // 合并去重
        activeStudentIds.addAll(activeByActivityIds);
        long activatedCount = activeStudentIds.stream().distinct().count();
        int activationRate = totalCount > 0 ? (int) Math.round((double) activatedCount / totalCount * 100) : 0;

        // 4. 返回统计数据
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("completenessRate", completenessRate + "%");
        stats.put("activationRate", activationRate + "%");

        return Result.suc(stats);
    }

    @Operation(summary = "更新学生信息", description = "根据学生ID更新学生信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:student:update')") // ✅ 添加细粒度权限
    @Log(title = "学生管理", businessType = BusinessType.UPDATE) // 修改
    public Result updateStudent(
            @Parameter(description = "学生ID", required = true) @PathVariable Long id,
            @RequestBody BizStudent student) {
        student.setId(id);
        boolean success = studentService.updateById(student);
        if (success) {
            // 4. 数据变更成功后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 删除学生 - 【已优化】增强级联检查
     */
    @Operation(summary = "删除学生", description = "根据ID删除学生，会先检查是否存在关联数据（错题、成绩、学习活动）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:student:delete')") // ✅ 添加细粒度权限
    @Log(title = "学生管理", businessType = BusinessType.DELETE) // 删除
    public Result deleteStudent(@Parameter(description = "学生ID", required = true) @PathVariable Long id) {
        // 【优化】完善的安全删除检查
        StringBuilder errorMsg = new StringBuilder();

        // 1. 检查该学生是否存在错题记录
        long wrongRecordCount = wrongRecordService.count(
                new QueryWrapper<BizWrongRecord>().eq("student_id", id)
        );
        if (wrongRecordCount > 0) {
            errorMsg.append("存在 ").append(wrongRecordCount).append(" 条错题记录；");
        }

        // 2. 检查该学生是否存在考试成绩
        long examResultCount = examResultService.count(
                new QueryWrapper<BizExamResult>().eq("student_id", id)
        );
        if (examResultCount > 0) {
            errorMsg.append("存在 ").append(examResultCount).append(" 条考试成绩记录；");
        }

        // 3. 检查该学生是否存在学习活动记录
        long activityCount = learningActivityService.count(
                new QueryWrapper<BizLearningActivity>().eq("student_id", id)
        );
        if (activityCount > 0) {
            errorMsg.append("存在 ").append(activityCount).append(" 条学习活动记录；");
        }

        // 4. 如果存在任何关联数据，则阻止删除
        if (errorMsg.length() > 0) {
            return Result.fail("无法删除：该学生" + errorMsg.toString() + " 请先清理相关数据。");
        }

        // 5. 如果没有关联数据，则执行删除
        boolean success = studentService.removeById(id);
        if (success) {
            // 6. 数据变更成功后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }
        return success ? Result.suc() : Result.fail();
    }

    /**
     * 【新增】批量删除学生
     */
    @Operation(summary = "批量删除学生", description = "批量删除学生，自动跳过有关联数据的学生")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('sys:student:delete')")
    @Log(title = "学生管理", businessType = BusinessType.DELETE)
    public Result batchDeleteStudents(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("请选择要删除的学生");
        }

        // 检查每个学生的关联数据
        StringBuilder blockedStudents = new StringBuilder();
        List<Long> canDeleteIds = new java.util.ArrayList<>();

        for (Long id : ids) {
            // 检查关联数据
            long wrongCount = wrongRecordService.count(new QueryWrapper<BizWrongRecord>().eq("student_id", id));
            long examCount = examResultService.count(new QueryWrapper<BizExamResult>().eq("student_id", id));
            long activityCount = learningActivityService.count(new QueryWrapper<BizLearningActivity>().eq("student_id", id));

            if (wrongCount > 0 || examCount > 0 || activityCount > 0) {
                BizStudent student = studentService.getById(id);
                if (student != null) {
                    blockedStudents.append(student.getName()).append("(").append(student.getStudentNo()).append(")、");
                }
            } else {
                canDeleteIds.add(id);
            }
        }

        // 删除可以删除的学生
        if (!canDeleteIds.isEmpty()) {
            studentService.removeByIds(canDeleteIds);
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
        }

        // 返回结果
        if (blockedStudents.length() > 0) {
            String msg = String.format("成功删除 %d 个学生。以下学生存在关联数据无法删除：%s",
                    canDeleteIds.size(),
                    blockedStudents.substring(0, blockedStudents.length() - 1));
            return Result.suc(msg);
        } else {
            return Result.suc("成功删除 " + canDeleteIds.size() + " 个学生");
        }
    }

    @Operation(summary = "批量导入学生", description = "通过Excel文件批量导入学生，自动加密密码，支持更新已有学生")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('sys:student:create')") // ✅ 添加细粒度权限（导入视为创建）
    @Log(title = "学生管理", businessType = BusinessType.IMPORT) // 导入
    public Result importStudents(
            @Parameter(description = "Excel文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            studentService.importStudents(file);
            // 4. 数据变更成功后，删除缓存
            redisTemplate.delete(DASHBOARD_CACHE_KEY);
            return Result.suc();
        } catch (IOException e) {
            logger.error("学生导入失败", e);
            return Result.fail("导入失败：" + e.getMessage());
        }
    }
    /**
     * 【新增】导出学生列表为Excel
     */
    @Operation(summary = "导出学生列表", description = "将学生列表导出为Excel文件，支持按姓名筛选")
    @GetMapping("/export")
    @Log(title = "学生管理", businessType = BusinessType.EXPORT) // 导出
    public void exportStudents(HttpServletResponse response,
                               @Parameter(description = "学生姓名（筛选条件，可选）") @RequestParam(required = false) String name) throws IOException {

        List<StudentExportDTO> list = studentService.getStudentExportList(name);

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
     * 【新增】下载学生导入模板
     */
    @Operation(summary = "下载学生导入模板", description = "下载学生批量导入的Excel模板文件")
    @GetMapping("/template")
    @Log(title = "学生管理", businessType = BusinessType.EXPORT)
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("学生导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 创建模板数据（包含一行示例）
        List<BizStudent> templateData = new java.util.ArrayList<>();
        BizStudent example = new BizStudent();
        example.setStudentNo("2024001");
        example.setName("张三");
        example.setGrade("高一"); // 使用标准年级格式
        example.setClassName("1班");
        example.setContact("13800138000");
        templateData.add(example);

        // 写入Excel文件
        EasyExcel.write(response.getOutputStream(), BizStudent.class)
                .sheet("学生信息")
                .doWrite(templateData);
    }
    /**
     * 【新增】教师手动给学生增加/扣除积分
     */
    @Operation(summary = "手动增减学生积分", description = "教师手动给学生增加或扣除积分，支持备注说明")
    @PostMapping("/{id}/points")
    @PreAuthorize("hasAuthority('sys:student:update')") // ✅ 添加细粒度权限
    @Log(title = "学生管理", businessType = BusinessType.UPDATE) // 积分奖惩
    public Result addPoints(
            @Parameter(description = "学生ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
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

