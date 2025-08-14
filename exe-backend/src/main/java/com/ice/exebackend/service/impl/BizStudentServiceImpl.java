package com.ice.exebackend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.StudentDashboardStatsDTO;
import com.ice.exebackend.dto.StudentExportDTO;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizSubject; // 【修复】在这里添加导入语句
import com.ice.exebackend.entity.BizWrongRecord;
import com.ice.exebackend.mapper.BizStudentMapper;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.BizSubjectService;
import com.ice.exebackend.service.BizWrongRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizStudentServiceImpl extends ServiceImpl<BizStudentMapper, BizStudent> implements BizStudentService {

    // 【新增】注入科目服务
    @Autowired
    private BizSubjectService subjectService;
    // 【新增】注入错题记录服务
    @Autowired
    private BizWrongRecordService wrongRecordService;
    @Override
    @Transactional
    public void importStudents(MultipartFile file, Long subjectId) throws IOException {
        List<BizStudent> studentsFromExcel = EasyExcel.read(file.getInputStream())
                .head(BizStudent.class)
                .sheet()
                .doReadSync();

        for (BizStudent student : studentsFromExcel) {
            student.setSubjectId(subjectId);

            // 【修复点】核心修改逻辑
            // 1. 根据学号查询数据库中是否已存在该学生
            BizStudent existingStudent = this.getOne(
                    new QueryWrapper<BizStudent>().eq("student_no", student.getStudentNo())
            );

            if (existingStudent != null) {
                // 2. 如果存在，将数据库中的ID设置到当前学生对象上，然后执行更新
                student.setId(existingStudent.getId());
                this.updateById(student);
            } else {
                // 3. 如果不存在，直接执行插入
                this.save(student);
            }
        }
    }
    // 【新增】实现导出数据查询方法
    @Override
    public List<StudentExportDTO> getStudentExportList(Long subjectId, String name) {
        QueryWrapper<BizStudent> queryWrapper = new QueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq("subject_id", subjectId);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("create_time");
        List<BizStudent> students = this.list(queryWrapper);

        if (students.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询所有涉及的科目，并转为Map以提高效率
        List<Long> subjectIds = students.stream().map(BizStudent::getSubjectId).distinct().collect(Collectors.toList());
        Map<Long, String> subjectMap = subjectService.listByIds(subjectIds).stream()
                .collect(Collectors.toMap(BizSubject::getId, BizSubject::getName));

        // 将 BizStudent 转换为 StudentExportDTO
        return students.stream().map(student -> {
            StudentExportDTO dto = new StudentExportDTO();
            dto.setName(student.getName());
            dto.setStudentNo(student.getStudentNo());
            dto.setSubjectName(subjectMap.getOrDefault(student.getSubjectId(), "未知科目"));
            dto.setGrade(student.getGrade());
            dto.setContact(student.getContact());
            return dto;
        }).collect(Collectors.toList());
    }
    /**
     * 【新增】实现获取学生仪表盘统计数据的方法
     */
    @Override
    public StudentDashboardStatsDTO getStudentDashboardStats(Long studentId) {
        StudentDashboardStatsDTO stats = new StudentDashboardStatsDTO();

        // 1. 错题总数 (真实数据)
        long wrongCount = wrongRecordService.count(
                new QueryWrapper<BizWrongRecord>().eq("student_id", studentId)
        );
        stats.setWrongRecordCount(wrongCount);

        // --- 以下为模拟数据，因为当前数据模型无法精确追踪 ---
        // TODO: 未来可通过记录学生每一次练习来精确计算以下数据

        // 2. 累计答题总数 (模拟)
        // 模拟一个看起来合理的值，例如错题数的8倍加上一些基础题数
        stats.setTotalAnswered(wrongCount * 8 + 35);

        // 3. 平均正确率 (模拟)
        if (stats.getTotalAnswered() > 0) {
            stats.setAverageAccuracy((int) (((double)(stats.getTotalAnswered() - wrongCount) / stats.getTotalAnswered()) * 100));
        } else {
            stats.setAverageAccuracy(100);
        }

        // 4. 学习时长 (模拟)
        stats.setStudyDurationHours(28);

        return stats;
    }
}
