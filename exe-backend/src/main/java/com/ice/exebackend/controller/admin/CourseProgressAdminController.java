package com.ice.exebackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizCourseProgress;
import com.ice.exebackend.entity.BizCourseResource;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.CourseProgressService;
import com.ice.exebackend.mapper.BizCourseResourceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端课程学习进度控制器
 * 教师/管理员查看学生学习数据
 */
@RestController
@RequestMapping("/api/v1/admin/course-progress")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
public class CourseProgressAdminController {

    private static final Logger logger = LoggerFactory.getLogger(CourseProgressAdminController.class);

    @Autowired
    private CourseProgressService progressService;

    @Autowired
    private BizStudentService studentService;

    @Autowired
    private BizCourseResourceMapper resourceMapper;

    /**
     * 获取课程学习概览（班级维度）
     *
     * @param courseId 课程ID
     * @param grade    年级（可选）
     * @param classNo  班级（可选）
     * @return 学习概览数据
     */
    @GetMapping("/course/{courseId}/overview")
    public Result getCourseProgressOverview(
        @PathVariable Long courseId,
        @RequestParam(required = false) String grade,
        @RequestParam(required = false) String classNo
    ) {
        logger.info("获取课程学习概览: courseId={}, grade={}, classNo={}",
            courseId, grade, classNo);

        try {
            // 1. 获取筛选条件下的学生列表
            QueryWrapper<BizStudent> studentQuery = new QueryWrapper<>();
            if (grade != null) {
                studentQuery.eq("grade", grade);
            }
            if (classNo != null) {
                studentQuery.eq("class", classNo);
            }
            List<BizStudent> students = studentService.list(studentQuery);
            int totalStudents = students.size();

            if (totalStudents == 0) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("totalStudents", 0);
                emptyResult.put("completedStudents", 0);
                emptyResult.put("averageProgress", 0);
                emptyResult.put("averageStudyTime", 0);
                return Result.suc(emptyResult);
            }

            // 2. 获取课程总资源数
            Long totalResources = resourceMapper.selectCount(
                new QueryWrapper<BizCourseResource>().eq("course_id", courseId)
            );

            if (totalResources == 0) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("totalStudents", totalStudents);
                emptyResult.put("completedStudents", 0);
                emptyResult.put("averageProgress", 0);
                emptyResult.put("averageStudyTime", 0);
                return Result.suc(emptyResult);
            }

            // 3. 统计每个学生的学习情况
            int completedCount = 0;
            double totalProgress = 0;
            long totalStudyTime = 0;

            for (BizStudent student : students) {
                double completion = progressService.calculateCourseCompletion(student.getId(), courseId);
                List<BizCourseProgress> progressList = progressService.getCourseProgress(student.getId(), courseId);

                // 统计学习时长
                int studentStudyTime = progressList.stream()
                    .mapToInt(p -> p.getStudyDuration() != null ? p.getStudyDuration() : 0)
                    .sum();

                totalProgress += completion;
                totalStudyTime += studentStudyTime;

                // 完成率>=95%算完成
                if (completion >= 95) {
                    completedCount++;
                }
            }

            // 4. 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("totalStudents", totalStudents);
            result.put("completedStudents", completedCount);
            result.put("averageProgress", Math.round(totalProgress / totalStudents * 100.0) / 100.0);
            result.put("averageStudyTime", totalStudyTime / totalStudents);

            return Result.suc(result);

        } catch (Exception e) {
            logger.error("获取课程学习概览失败", e);
            return Result.fail("获取学习概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程学习详情列表（学生维度）
     *
     * @param courseId 课程ID
     * @param current  当前页
     * @param size     每页大小
     * @param grade    年级（可选）
     * @param classNo  班级（可选）
     * @return 学生学习进度分页列表
     */
    @GetMapping("/course/{courseId}/students")
    public Result getCourseStudentProgress(
        @PathVariable Long courseId,
        @RequestParam(defaultValue = "1") int current,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String grade,
        @RequestParam(required = false) String classNo
    ) {
        logger.info("获取课程学生学习进度列表: courseId={}, current={}, size={}",
            courseId, current, size);

        try {
            // 1. 获取筛选条件下的学生列表（分页）
            Page<BizStudent> studentPage = new Page<>(current, size);
            QueryWrapper<BizStudent> studentQuery = new QueryWrapper<>();
            if (grade != null) {
                studentQuery.eq("grade", grade);
            }
            if (classNo != null) {
                studentQuery.eq("class", classNo);
            }
            studentService.page(studentPage, studentQuery);

            // 2. 为每个学生构建学习数据
            List<Map<String, Object>> studentProgressList = studentPage.getRecords().stream()
                .map(student -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("studentId", student.getId());
                    item.put("studentName", student.getName());
                    item.put("studentNo", student.getStudentNo());
                    item.put("grade", student.getGrade());
                    item.put("className", student.getClassName());

                    // 计算完成率
                    double completion = progressService.calculateCourseCompletion(student.getId(), courseId);
                    item.put("completionRate", Math.round(completion * 100.0) / 100.0);

                    // 获取学习进度
                    List<BizCourseProgress> progressList = progressService.getCourseProgress(student.getId(), courseId);

                    // 统计学习时长
                    int totalStudyTime = progressList.stream()
                        .mapToInt(p -> p.getStudyDuration() != null ? p.getStudyDuration() : 0)
                        .sum();
                    item.put("totalStudyTime", totalStudyTime);

                    // 最后学习时间
                    Optional<BizCourseProgress> lastProgress = progressList.stream()
                        .max(Comparator.comparing(BizCourseProgress::getUpdatedTime));
                    item.put("lastStudyTime", lastProgress.map(BizCourseProgress::getUpdatedTime).orElse(null));

                    return item;
                })
                .collect(Collectors.toList());

            // 3. 构建分页结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", studentProgressList);
            result.put("total", studentPage.getTotal());
            result.put("current", studentPage.getCurrent());
            result.put("size", studentPage.getSize());
            result.put("pages", studentPage.getPages());

            return Result.suc(result);

        } catch (Exception e) {
            logger.error("获取课程学生学习进度失败", e);
            return Result.fail("获取学生学习进度失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生详细学习进度（资源维度）
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 学生每个资源的学习进度
     */
    @GetMapping("/student/{studentId}/course/{courseId}")
    public Result getStudentDetailProgress(
        @PathVariable Long studentId,
        @PathVariable Long courseId
    ) {
        logger.info("获取学生详细学习进度: studentId={}, courseId={}", studentId, courseId);

        try {
            // 1. 获取学生基本信息
            BizStudent student = studentService.getById(studentId);
            if (student == null) {
                return Result.fail("学生不存在");
            }

            // 2. 获取学生学习进度
            List<BizCourseProgress> progressList = progressService.getCourseProgress(studentId, courseId);

            // 3. 获取课程所有资源
            List<BizCourseResource> resources = resourceMapper.selectList(
                new QueryWrapper<BizCourseResource>().eq("course_id", courseId)
            );

            // 4. 构建完整的资源学习数据（包括未学习的）
            Map<Long, BizCourseProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(BizCourseProgress::getResourceId, p -> p));

            List<Map<String, Object>> resourceProgressList = resources.stream()
                .map(resource -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("resourceId", resource.getId());
                    item.put("resourceName", resource.getName());
                    item.put("resourceType", resource.getResourceType());

                    BizCourseProgress progress = progressMap.get(resource.getId());
                    if (progress != null) {
                        item.put("progressPercent", progress.getProgressPercent());
                        item.put("studyDuration", progress.getStudyDuration());
                        item.put("isCompleted", progress.getIsCompleted());
                        item.put("lastPosition", progress.getLastPosition());
                        item.put("updatedTime", progress.getUpdatedTime());
                    } else {
                        item.put("progressPercent", 0);
                        item.put("studyDuration", 0);
                        item.put("isCompleted", 0);
                        item.put("lastPosition", null);
                        item.put("updatedTime", null);
                    }

                    return item;
                })
                .collect(Collectors.toList());

            // 5. 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("student", student);
            result.put("resourceProgress", resourceProgressList);
            result.put("completionRate", progressService.calculateCourseCompletion(studentId, courseId));

            return Result.suc(result);

        } catch (Exception e) {
            logger.error("获取学生详细学习进度失败", e);
            return Result.fail("获取学生详细学习进度失败: " + e.getMessage());
        }
    }
}
