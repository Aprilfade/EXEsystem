package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.ProgressUpdateDTO;
import com.ice.exebackend.entity.BizCourseProgress;
import com.ice.exebackend.entity.BizCourseResource;
import com.ice.exebackend.entity.BizStudySession;
import com.ice.exebackend.mapper.BizCourseProgressMapper;
import com.ice.exebackend.mapper.BizCourseResourceMapper;
import com.ice.exebackend.mapper.BizStudySessionMapper;
import com.ice.exebackend.service.CourseProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程学习进度服务实现
 */
@Service
public class CourseProgressServiceImpl extends ServiceImpl<BizCourseProgressMapper, BizCourseProgress> implements CourseProgressService {

    private static final Logger logger = LoggerFactory.getLogger(CourseProgressServiceImpl.class);

    @Autowired
    private BizCourseResourceMapper resourceMapper;

    @Autowired
    private BizStudySessionMapper sessionMapper;

    @Override
    @Transactional
    public boolean updateProgress(Long studentId, ProgressUpdateDTO dto) {
        logger.info("更新学习进度: studentId={}, resourceId={}, percent={}",
            studentId, dto.getResourceId(), dto.getProgressPercent());

        // 1. 查询是否已有进度记录
        BizCourseProgress progress = this.getOne(
            new QueryWrapper<BizCourseProgress>()
                .eq("student_id", studentId)
                .eq("resource_id", dto.getResourceId())
        );

        LocalDateTime now = LocalDateTime.now();

        if (progress == null) {
            // 新建进度记录
            progress = new BizCourseProgress();
            progress.setStudentId(studentId);
            progress.setResourceId(dto.getResourceId());

            // 获取资源的课程ID和类型
            var resource = resourceMapper.selectById(dto.getResourceId());
            if (resource != null) {
                progress.setCourseId(resource.getCourseId());
                progress.setResourceType(resource.getResourceType());
            }

            progress.setProgressPercent(dto.getProgressPercent());
            progress.setLastPosition(dto.getLastPosition());
            progress.setStudyDuration(dto.getStudyDuration());

            // 判断是否完成（进度>=95%）
            if (dto.getProgressPercent() >= 95) {
                progress.setIsCompleted(1);
                progress.setCompletedTime(now);
            } else {
                progress.setIsCompleted(0);
            }

            return this.save(progress);
        } else {
            // 更新进度记录
            progress.setProgressPercent(dto.getProgressPercent());
            progress.setLastPosition(dto.getLastPosition());

            // 累加学习时长
            if (dto.getStudyDuration() != null) {
                int currentDuration = progress.getStudyDuration() == null ? 0 : progress.getStudyDuration();
                progress.setStudyDuration(currentDuration + dto.getStudyDuration());
            }

            // 更新完成状态
            if (dto.getProgressPercent() >= 95 && progress.getIsCompleted() == 0) {
                progress.setIsCompleted(1);
                progress.setCompletedTime(now);
                logger.info("资源学习完成: studentId={}, resourceId={}", studentId, dto.getResourceId());
            }

            return this.updateById(progress);
        }
    }

    @Override
    public List<BizCourseProgress> getCourseProgress(Long studentId, Long courseId) {
        return this.list(
            new QueryWrapper<BizCourseProgress>()
                .eq("student_id", studentId)
                .eq("course_id", courseId)
        );
    }

    @Override
    public BizCourseProgress getResourceProgress(Long studentId, Long resourceId) {
        return this.getOne(
            new QueryWrapper<BizCourseProgress>()
                .eq("student_id", studentId)
                .eq("resource_id", resourceId)
        );
    }

    @Override
    public double calculateCourseCompletion(Long studentId, Long courseId) {
        // 1. 获取课程总资源数
        Long totalResources = resourceMapper.selectCount(
            new QueryWrapper<BizCourseResource>().eq("course_id", courseId)
        );

        if (totalResources == 0) {
            return 0.0;
        }

        // 2. 获取已完成资源数
        Long completedResources = this.count(
            new QueryWrapper<BizCourseProgress>()
                .eq("student_id", studentId)
                .eq("course_id", courseId)
                .eq("is_completed", 1)
        );

        // 3. 计算完成率
        double completion = (completedResources.doubleValue() / totalResources.doubleValue()) * 100;
        return Math.round(completion * 100.0) / 100.0; // 保留两位小数
    }

    @Override
    @Transactional
    public Long startSession(Long studentId, Long courseId, Long resourceId) {
        logger.info("开始学习会话: studentId={}, courseId={}, resourceId={}",
            studentId, courseId, resourceId);

        BizStudySession session = new BizStudySession();
        session.setStudentId(studentId);
        session.setCourseId(courseId);
        session.setResourceId(resourceId);
        session.setSessionStart(LocalDateTime.now());

        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    @Transactional
    public boolean endSession(Long sessionId) {
        logger.info("结束学习会话: sessionId={}", sessionId);

        BizStudySession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            logger.warn("会话不存在: sessionId={}", sessionId);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        session.setSessionEnd(now);

        // 计算会话时长（秒）
        if (session.getSessionStart() != null) {
            Duration duration = Duration.between(session.getSessionStart(), now);
            session.setDuration((int) duration.getSeconds());
        }

        return sessionMapper.updateById(session) > 0;
    }
}
