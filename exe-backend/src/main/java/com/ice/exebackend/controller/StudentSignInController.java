package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizSignInService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/student/sign-in")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentSignInController {

    @Autowired
    private BizSignInService signInService;
    @Autowired
    private BizStudentService studentService;

    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return s != null ? s.getId() : null;
    }

    @PostMapping
    public Result doSignIn(Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        try {
            Map<String, Object> result = signInService.doSignIn(studentId);
            return Result.suc(result);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/status")
    public Result getStatus(@RequestParam String month, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        return Result.suc(signInService.getSignInStatus(studentId, month));
    }
}