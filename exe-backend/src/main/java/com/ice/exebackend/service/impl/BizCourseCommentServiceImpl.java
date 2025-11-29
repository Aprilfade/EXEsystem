package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.CourseCommentVO;
import com.ice.exebackend.entity.BizCourseComment;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.mapper.BizCourseCommentMapper;
import com.ice.exebackend.mapper.BizStudentMapper;
import com.ice.exebackend.service.BizCourseCommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BizCourseCommentServiceImpl extends ServiceImpl<BizCourseCommentMapper, BizCourseComment> implements BizCourseCommentService {

    @Autowired
    private BizStudentMapper studentMapper;

    @Override
    public List<CourseCommentVO> getCommentsByCourseId(Long courseId, Long currentStudentId) {
        // 1. 查询该课程下的所有评论，按时间倒序
        List<BizCourseComment> comments = this.list(
                new QueryWrapper<BizCourseComment>()
                        .eq("course_id", courseId)
                        .orderByDesc("create_time")
        );

        if (comments.isEmpty()) return List.of();

        // 2. 收集所有发帖学生的ID
        List<Long> studentIds = comments.stream().map(BizCourseComment::getStudentId).distinct().collect(Collectors.toList());

        // 3. 【修改点】使用 selectList 替代 selectBatchIds
        List<BizStudent> students = studentMapper.selectList(
                new QueryWrapper<BizStudent>().in("id", studentIds)
        );
        Map<Long, BizStudent> studentMap = students.stream().collect(Collectors.toMap(BizStudent::getId, s -> s));

        // 4. 组装 VO
        return comments.stream().map(c -> {
            CourseCommentVO vo = new CourseCommentVO();
            BeanUtils.copyProperties(c, vo);

            BizStudent s = studentMap.get(c.getStudentId());
            if (s != null) {
                vo.setStudentName(s.getName());
                vo.setStudentAvatar(s.getAvatar());
                vo.setAvatarFrameStyle(s.getAvatarFrameStyle());
            } else {
                vo.setStudentName("未知用户");
            }

            // 标记是否是当前用户发的
            vo.setIsSelf(c.getStudentId().equals(currentStudentId));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean deleteComment(Long commentId, Long currentStudentId) {
        // 只能删除自己的评论
        return this.remove(new QueryWrapper<BizCourseComment>()
                .eq("id", commentId)
                .eq("student_id", currentStudentId));
    }
}