package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.CourseCommentVO;
import com.ice.exebackend.entity.BizCourseComment;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizCourseCommentService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student/comments")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentCourseCommentController {

    @Autowired
    private BizCourseCommentService commentService;

    @Autowired
    private BizStudentService studentService;

    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return s != null ? s.getId() : null;
    }

    @GetMapping("/{courseId}")
    public Result getComments(@PathVariable Long courseId, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        List<CourseCommentVO> list = commentService.getCommentsByCourseId(courseId, studentId);
        return Result.suc(list);
    }

    @PostMapping
    public Result addComment(@RequestBody Map<String, Object> body, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        Long courseId = Long.valueOf(body.get("courseId").toString());
        String content = (String) body.get("content");

        if (content == null || content.trim().isEmpty()) {
            return Result.fail("评论内容不能为空");
        }

        BizCourseComment comment = new BizCourseComment();
        comment.setCourseId(courseId);
        comment.setStudentId(studentId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());

        commentService.save(comment);
        return Result.suc("发布成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Long id, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        boolean success = commentService.deleteComment(id, studentId);
        return success ? Result.suc("删除成功") : Result.fail("删除失败或无权限");
    }
}