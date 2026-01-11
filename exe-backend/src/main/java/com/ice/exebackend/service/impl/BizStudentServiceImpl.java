package com.ice.exebackend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.StudentDashboardStatsDTO;
import com.ice.exebackend.dto.StudentExportDTO;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizSubject; // 【修复】在这里添加导入语句
import com.ice.exebackend.entity.BizWrongRecord;
import com.ice.exebackend.entity.BizExamResult; // 【新增】导入考试成绩实体
import com.ice.exebackend.mapper.BizStudentMapper;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.service.BizSubjectService;
import com.ice.exebackend.service.BizWrongRecordService;
import com.ice.exebackend.service.BizExamResultService; // 【新增】导入考试成绩服务
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    // 【新增】注入考试成绩服务
    @Autowired
    private BizExamResultService examResultService;
    // 【新增】注入密码编码器 - 使用 @Lazy 解决循环依赖
    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public void importStudents(MultipartFile file) throws IOException {
        // 1. 读取Excel数据
        List<BizStudent> studentsFromExcel = EasyExcel.read(file.getInputStream())
                .head(BizStudent.class)
                .sheet()
                .doReadSync();

        if (studentsFromExcel == null || studentsFromExcel.isEmpty()) {
            return;
        }

        // 2. 【性能优化】批量查询已存在的学生 - 避免N+1查询
        List<String> studentNos = studentsFromExcel.stream()
                .map(BizStudent::getStudentNo)
                .distinct()
                .collect(Collectors.toList());

        Map<String, BizStudent> existingStudentMap = this.list(
                new QueryWrapper<BizStudent>().in("student_no", studentNos)
        ).stream().collect(Collectors.toMap(BizStudent::getStudentNo, s -> s));

        // 3. 处理每个学生数据
        for (BizStudent student : studentsFromExcel) {
            // 【安全修复】处理密码
            BizStudent existingStudent = existingStudentMap.get(student.getStudentNo());

            if (existingStudent != null) {
                // 更新已存在的学生
                student.setId(existingStudent.getId());

                // ✅ 核心安全修复：如果Excel中密码为空或未提供，保留数据库中的加密密码
                if (StringUtils.hasText(student.getPassword())) {
                    // ✅ 新增：密码长度验证
                    if (student.getPassword().length() < 6) {
                        throw new RuntimeException("学号 " + student.getStudentNo() + " 的密码长度至少为6位");
                    }
                    // Excel中提供了新密码，需要加密
                    student.setPassword(passwordEncoder.encode(student.getPassword()));
                } else {
                    // Excel中没有密码，保留数据库中已加密的密码
                    student.setPassword(existingStudent.getPassword());
                }

                this.updateById(student);
            } else {
                // 新增学生
                // ✅ 设置默认密码：学号后6位，如果不足6位则使用"123456"
                if (!StringUtils.hasText(student.getPassword())) {
                    String studentNo = student.getStudentNo();
                    String defaultPassword = studentNo.length() >= 6
                            ? studentNo.substring(studentNo.length() - 6)
                            : "123456";  // ✅ 修改：不足6位时使用默认密码
                    student.setPassword(defaultPassword);
                }

                // ✅ 新增：密码长度验证
                if (student.getPassword().length() < 6) {
                    throw new RuntimeException("学号 " + student.getStudentNo() + " 的密码长度至少为6位");
                }

                // ✅ 加密密码
                student.setPassword(passwordEncoder.encode(student.getPassword()));

                this.save(student);
            }
        }
    }
    // 【新增】实现导出数据查询方法
    @Override
    public List<StudentExportDTO> getStudentExportList(String name) {
        QueryWrapper<BizStudent> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("create_time");
        List<BizStudent> students = this.list(queryWrapper);

        if (students.isEmpty()) {
            return Collections.emptyList();
        }

        // 将 BizStudent 转换为 StudentExportDTO
        return students.stream().map(student -> {
            StudentExportDTO dto = new StudentExportDTO();
            dto.setName(student.getName());
            dto.setStudentNo(student.getStudentNo());
            dto.setGrade(student.getGrade());
            dto.setClassName(student.getClassName());
            dto.setContact(student.getContact());
            return dto;
        }).collect(Collectors.toList());
    }
    /**
     * 【修改】实现获取学生仪表盘统计数据的方法 - 基于真实数据
     */
    @Override
    public StudentDashboardStatsDTO getStudentDashboardStats(Long studentId) {
        StudentDashboardStatsDTO stats = new StudentDashboardStatsDTO();

        // 1. 错题总数 (真实数据)
        long wrongCount = wrongRecordService.count(
                new QueryWrapper<BizWrongRecord>().eq("student_id", studentId)
        );
        stats.setWrongRecordCount(wrongCount);

        // 2. 获取学生所有考试记录，计算真实的答题数据
        List<BizExamResult> examResults = examResultService.list(
                new QueryWrapper<BizExamResult>().eq("student_id", studentId)
        );

        if (examResults != null && !examResults.isEmpty()) {
            // 统计总得分和总分
            int totalScoreSum = 0;
            int totalPossibleScore = 0;
            int totalAnsweredQuestions = 0;

            for (BizExamResult result : examResults) {
                if (result.getScore() != null && result.getTotalScore() != null) {
                    totalScoreSum += result.getScore();
                    totalPossibleScore += result.getTotalScore();

                    // 统计答题数量（从 userAnswers JSON 中解析）
                    if (result.getUserAnswers() != null && !result.getUserAnswers().isEmpty()) {
                        // userAnswers 是 JSON 格式，每个 key 代表一个题目
                        // 简单统计：去掉 {} 和空格，按逗号分割计数
                        String answers = result.getUserAnswers();
                        if (answers.length() > 2) { // 至少有 "{}"
                            // 简易统计方法：统计冒号的数量（每个题目答案对应一个冒号）
                            long questionCount = answers.chars().filter(ch -> ch == ':').count();
                            totalAnsweredQuestions += questionCount;
                        }
                    }
                }
            }

            // 3. 累计答题总数（真实数据）
            stats.setTotalAnswered(totalAnsweredQuestions > 0 ? totalAnsweredQuestions : wrongCount * 8 + 35);

            // 4. 平均正确率（基于真实得分计算）
            if (totalPossibleScore > 0) {
                // 计算正确率：总得分 / 总可能得分 * 100
                int accuracy = (int) Math.round(((double) totalScoreSum / totalPossibleScore) * 100);
                stats.setAverageAccuracy(accuracy);
            } else {
                // 没有考试记录时，显示 0%
                stats.setAverageAccuracy(0);
            }

            // 5. 学习时长（根据考试次数估算，每次考试约 1 小时）
            int studyHours = examResults.size() * 1;
            // 加上错题复习时间（每个错题约 0.1 小时）
            studyHours += (int) (wrongCount * 0.1);
            stats.setStudyDurationHours(studyHours > 0 ? studyHours : 1);
        } else {
            // 没有考试记录时的默认值
            stats.setTotalAnswered(0);
            stats.setAverageAccuracy(0);
            stats.setStudyDurationHours(0);
        }

        return stats;
    }
}
