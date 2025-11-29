package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.CourseCommentVO;
import com.ice.exebackend.entity.BizCourseComment;
import java.util.List;

public interface BizCourseCommentService extends IService<BizCourseComment> {
    List<CourseCommentVO> getCommentsByCourseId(Long courseId, Long currentStudentId);
    boolean deleteComment(Long commentId, Long currentStudentId);
}