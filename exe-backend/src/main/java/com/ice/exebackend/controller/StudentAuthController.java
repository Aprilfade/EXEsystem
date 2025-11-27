package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;
import com.ice.exebackend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student/auth") // 注意，是/student/auth
public class StudentAuthController {

    @Autowired
    private BizStudentService studentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 学生登录接口
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginRequest) {
        String studentNo = loginRequest.get("studentNo");
        String password = loginRequest.get("password");

        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        if (student == null || student.getPassword() == null || !passwordEncoder.matches(password, student.getPassword())) {
            return Result.fail("学号或密码错误");
        }
// 【新增】简单的每日登录加分逻辑 (这里未做每日限制，生产环境建议配合 Redis 做每日一次限制)
        student.setPoints(student.getPoints() == null ? 1 : student.getPoints() + 1);
        studentService.updateById(student);

        String token = jwtUtil.generateTokenForStudent(student.getStudentNo());
        return Result.suc(Map.of("token", token));
    }

    /**
     * 获取当前登录学生的信息
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result getStudentInfo(Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        // 关键：出于安全考虑，返回给前端前一定要擦除密码
        if (student != null) {
            student.setPassword(null);
        }

        return Result.suc(student);
    }

    /**
     * 学生修改自己的密码
     */
    @PutMapping("/update-password")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result updatePassword(@RequestBody Map<String, String> passwordRequest, Authentication authentication) {
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");
        String studentNo = authentication.getName();

        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        if (student == null || !passwordEncoder.matches(oldPassword, student.getPassword())) {
            return Result.fail("旧密码不正确");
        }

        student.setPassword(passwordEncoder.encode(newPassword));
        studentService.updateById(student);

        return Result.suc("密码修改成功");
    }
    /**
     * 【新增】学生更新自己的个人资料
     */
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public Result updateProfile(@RequestBody BizStudent studentUpdateData, Authentication authentication) {
        String studentNo = authentication.getName();
        BizStudent currentStudent = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        if (currentStudent == null) {
            return Result.fail("学生不存在");
        }

        // 创建一个新对象，只设置允许学生修改的字段，防止恶意修改学号等信息
        BizStudent studentToUpdate = new BizStudent();
        studentToUpdate.setId(currentStudent.getId()); // 必须设置ID，用于定位要更新的记录

        // 设置允许更新的字段
        studentToUpdate.setName(studentUpdateData.getName());
        studentToUpdate.setContact(studentUpdateData.getContact());
        studentToUpdate.setAvatar(studentUpdateData.getAvatar());

        // 只有当用户输入了新密码时，才进行加密和更新
        if (studentUpdateData.getPassword() != null && !studentUpdateData.getPassword().isEmpty()) {
            studentToUpdate.setPassword(passwordEncoder.encode(studentUpdateData.getPassword()));
        }

        boolean success = studentService.updateById(studentToUpdate);
        return success ? Result.suc("更新成功") : Result.fail("更新失败");
    }
}