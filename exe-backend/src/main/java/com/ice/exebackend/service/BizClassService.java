package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizClass;

import java.util.List;

/**
 * 班级管理Service接口
 */
public interface BizClassService extends IService<BizClass> {

    /**
     * 创建班级并生成6位邀请码
     * @param bizClass 班级信息
     * @return 创建的班级
     */
    BizClass createClass(BizClass bizClass);

    /**
     * 更新班级信息
     * @param id 班级ID
     * @param bizClass 班级信息
     * @return 是否成功
     */
    boolean updateClass(Long id, BizClass bizClass);

    /**
     * 删除班级（检查关联数据）
     * @param id 班级ID
     * @return 删除结果消息
     */
    String deleteClass(Long id);

    /**
     * 学生加入班级（通过邀请码）
     * @param studentId 学生ID
     * @param code 邀请码
     * @return 是否成功
     */
    boolean joinClass(Long studentId, String code);

    /**
     * 从班级移除学生
     * @param classId 班级ID
     * @param studentId 学生ID
     * @return 是否成功
     */
    boolean removeStudent(Long classId, Long studentId);

    /**
     * 获取班级的学生列表
     * @param classId 班级ID
     * @return 学生列表
     */
    List<Long> getClassStudentIds(Long classId);

    /**
     * 获取学生所属的班级列表
     * @param studentId 学生ID
     * @return 班级列表
     */
    List<BizClass> getStudentClasses(Long studentId);

    /**
     * 重新生成邀请码
     * @param classId 班级ID
     * @return 新邀请码
     */
    String regenerateCode(Long classId);
}
