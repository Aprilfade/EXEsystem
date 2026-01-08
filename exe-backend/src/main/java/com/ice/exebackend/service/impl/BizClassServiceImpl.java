package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizClass;
import com.ice.exebackend.entity.BizClassStudent;
import com.ice.exebackend.entity.BizHomework;
import com.ice.exebackend.mapper.BizClassMapper;
import com.ice.exebackend.mapper.BizClassStudentMapper;
import com.ice.exebackend.mapper.BizHomeworkMapper;
import com.ice.exebackend.service.BizClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 班级管理Service实现
 */
@Service
public class BizClassServiceImpl extends ServiceImpl<BizClassMapper, BizClass> implements BizClassService {

    @Autowired
    private BizClassStudentMapper classStudentMapper;

    @Autowired
    private BizHomeworkMapper homeworkMapper;

    @Override
    @Transactional
    public BizClass createClass(BizClass bizClass) {
        // 生成6位随机邀请码
        bizClass.setCode(generateInviteCode());
        bizClass.setCreateTime(LocalDateTime.now());
        this.save(bizClass);
        return bizClass;
    }

    @Override
    public boolean updateClass(Long id, BizClass bizClass) {
        BizClass existing = this.getById(id);
        if (existing == null) {
            return false;
        }
        bizClass.setId(id);
        // 保留原有的邀请码和创建时间
        bizClass.setCode(existing.getCode());
        bizClass.setCreateTime(existing.getCreateTime());
        return this.updateById(bizClass);
    }

    @Override
    @Transactional
    public String deleteClass(Long id) {
        // 检查是否有学生
        long studentCount = classStudentMapper.selectCount(
                new QueryWrapper<BizClassStudent>().eq("class_id", id)
        );

        // 检查是否有作业
        long homeworkCount = homeworkMapper.selectCount(
                new QueryWrapper<BizHomework>().eq("class_id", id)
        );

        if (studentCount > 0 || homeworkCount > 0) {
            StringBuilder msg = new StringBuilder("无法删除：该班级下还存在 ");
            if (studentCount > 0) {
                msg.append(studentCount).append(" 名学生");
            }
            if (homeworkCount > 0) {
                if (studentCount > 0) msg.append("、");
                msg.append(homeworkCount).append(" 份作业");
            }
            msg.append("。请先清理关联数据后再删除班级。");
            return msg.toString();
        }

        // 删除班级
        this.removeById(id);
        return "删除成功";
    }

    @Override
    @Transactional
    public boolean joinClass(Long studentId, String code) {
        // 查找班级
        BizClass bizClass = this.getOne(
                new QueryWrapper<BizClass>().eq("code", code)
        );

        if (bizClass == null) {
            throw new RuntimeException("邀请码无效");
        }

        // 检查是否已加入
        long count = classStudentMapper.selectCount(
                new QueryWrapper<BizClassStudent>()
                        .eq("class_id", bizClass.getId())
                        .eq("student_id", studentId)
        );

        if (count > 0) {
            throw new RuntimeException("您已经是该班级成员");
        }

        // 加入班级
        BizClassStudent relation = new BizClassStudent();
        relation.setClassId(bizClass.getId());
        relation.setStudentId(studentId);
        relation.setCreateTime(LocalDateTime.now());
        return classStudentMapper.insert(relation) > 0;
    }

    @Override
    @Transactional
    public boolean removeStudent(Long classId, Long studentId) {
        return classStudentMapper.delete(
                new QueryWrapper<BizClassStudent>()
                        .eq("class_id", classId)
                        .eq("student_id", studentId)
        ) > 0;
    }

    @Override
    public List<Long> getClassStudentIds(Long classId) {
        List<BizClassStudent> relations = classStudentMapper.selectList(
                new QueryWrapper<BizClassStudent>().eq("class_id", classId)
        );
        return relations.stream()
                .map(BizClassStudent::getStudentId)
                .collect(Collectors.toList());
    }

    @Override
    public List<BizClass> getStudentClasses(Long studentId) {
        // 查询学生关联的班级ID列表
        List<BizClassStudent> relations = classStudentMapper.selectList(
                new QueryWrapper<BizClassStudent>().eq("student_id", studentId)
        );

        List<Long> classIds = relations.stream()
                .map(BizClassStudent::getClassId)
                .collect(Collectors.toList());

        if (classIds.isEmpty()) {
            return List.of();
        }

        // 查询班级详情
        return this.listByIds(classIds);
    }

    @Override
    @Transactional
    public String regenerateCode(Long classId) {
        BizClass bizClass = this.getById(classId);
        if (bizClass == null) {
            throw new RuntimeException("班级不存在");
        }

        String newCode = generateInviteCode();
        bizClass.setCode(newCode);
        this.updateById(bizClass);
        return newCode;
    }

    /**
     * 生成6位随机邀请码
     */
    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去掉易混淆的字符
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        // 检查是否重复
        long count = this.count(new QueryWrapper<BizClass>().eq("code", code.toString()));
        if (count > 0) {
            // 重复则递归重新生成
            return generateInviteCode();
        }

        return code.toString();
    }
}
