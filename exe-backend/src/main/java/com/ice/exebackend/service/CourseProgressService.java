package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.ProgressUpdateDTO;
import com.ice.exebackend.entity.BizCourseProgress;

import java.util.List;

/**
 * 课程学习进度服务接口
 */
public interface CourseProgressService extends IService<BizCourseProgress> {

    /**
     * 更新学习进度
     * @param studentId 学生ID
     * @param dto 进度更新数据
     * @return 是否成功
     */
    boolean updateProgress(Long studentId, ProgressUpdateDTO dto);

    /**
     * 获取学生的课程学习进度列表
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 进度列表
     */
    List<BizCourseProgress> getCourseProgress(Long studentId, Long courseId);

    /**
     * 获取资源学习进度
     * @param studentId 学生ID
     * @param resourceId 资源ID
     * @return 学习进度
     */
    BizCourseProgress getResourceProgress(Long studentId, Long resourceId);

    /**
     * 计算课程完成率
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 完成率（0-100）
     */
    double calculateCourseCompletion(Long studentId, Long courseId);

    /**
     * 开始学习会话
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param resourceId 资源ID
     * @return 会话ID
     */
    Long startSession(Long studentId, Long courseId, Long resourceId);

    /**
     * 结束学习会话
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean endSession(Long sessionId);
}
