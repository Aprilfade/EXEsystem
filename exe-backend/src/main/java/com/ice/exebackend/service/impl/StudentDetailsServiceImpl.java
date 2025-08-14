package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 学生专用的UserDetailsService实现
 * 用于Spring Security在学生登录时进行身份认证
 */
@Service("studentDetailsServiceImpl") // 指定一个唯一的Bean名称，避免与管理后台的冲突
public class StudentDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private BizStudentService studentService;

    @Override
    public UserDetails loadUserByUsername(String studentNo) throws UsernameNotFoundException {
        // 这里的 "username" 实际上是学生的 "学号"
        BizStudent student = studentService.getOne(new QueryWrapper<BizStudent>().eq("student_no", studentNo));

        if (student == null) {
            throw new UsernameNotFoundException("学号或密码错误: " + studentNo);
        }

        // 为所有学生赋予一个固定的 "ROLE_STUDENT" 角色
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"));

        // 构建并返回Spring Security的UserDetails对象
        return User.builder()
                .username(student.getStudentNo()) // Principal (身份标识) 使用学号
                .password(student.getPassword())
                .authorities(authorities)
                // 这里可以添加更多账户状态检查，例如账户是否被禁用等
                .disabled(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .build();
    }
}