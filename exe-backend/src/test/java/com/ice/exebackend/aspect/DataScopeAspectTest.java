package com.ice.exebackend.aspect;

import com.ice.exebackend.annotation.DataScope;
import com.ice.exebackend.annotation.DataScopeType;
import com.ice.exebackend.context.DataScopeContext;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.service.SysUserService;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据权限切面测试
 * 测试基于角色的数据过滤逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据权限切面测试")
class DataScopeAspectTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DataScopeAspect dataScopeAspect;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        DataScopeContext.clear();
    }

    // ==================== 管理员权限测试 ====================

    @Test
    @DisplayName("管理员应该不受数据权限限制")
    void shouldNotApplyDataScopeForAdmin() {
        // Given
        when(authentication.getName()).thenReturn("admin");

        SysUser adminUser = createUser(1L, "admin");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(adminUser);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertNotNull(info);
        assertTrue(info.isAdmin());
        assertNull(info.getSqlCondition()); // 管理员不应有SQL过滤条件
    }

    @Test
    @DisplayName("超级管理员应该不受数据权限限制")
    void shouldNotApplyDataScopeForSuperAdmin() {
        // Given
        when(authentication.getName()).thenReturn("superadmin");

        SysUser superAdmin = createUser(1L, "superadmin");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(superAdmin);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertTrue(info.isAdmin());
        assertNull(info.getSqlCondition());
    }

    // ==================== 教师权限测试 ====================

    @Test
    @DisplayName("教师应该只能查看自己的班级")
    void shouldApplyTeacherClassDataScope() {
        // Given
        Long teacherId = 100L;
        when(authentication.getName()).thenReturn("teacher001");

        SysUser teacher = createUser(teacherId, "teacher001");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(teacher);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_TEACHER")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertNotNull(info);
        assertFalse(info.isAdmin());
        assertEquals("teacher_id = 100", info.getSqlCondition());
    }

    @Test
    @DisplayName("教师应该只能查看自己班级的学生")
    void shouldApplyTeacherStudentDataScope() {
        // Given
        Long teacherId = 100L;
        when(authentication.getName()).thenReturn("teacher001");

        SysUser teacher = createUser(teacherId, "teacher001");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(teacher);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_TEACHER")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_STUDENT, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertNotNull(info);
        String expectedSql = "id IN (" +
                "SELECT cs.student_id FROM biz_class_student cs " +
                "INNER JOIN biz_class c ON cs.class_id = c.id " +
                "WHERE c.teacher_id = 100)";
        assertEquals(expectedSql, info.getSqlCondition());
    }

    @Test
    @DisplayName("教师应该只能查看自己班级学生的考试成绩")
    void shouldApplyTeacherExamDataScope() {
        // Given
        Long teacherId = 100L;
        when(authentication.getName()).thenReturn("teacher001");

        SysUser teacher = createUser(teacherId, "teacher001");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(teacher);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_TEACHER")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_EXAM, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        String expectedSql = "student_id IN (" +
                "SELECT cs.student_id FROM biz_class_student cs " +
                "INNER JOIN biz_class c ON cs.class_id = c.id " +
                "WHERE c.teacher_id = 100)";
        assertEquals(expectedSql, info.getSqlCondition());
    }

    @Test
    @DisplayName("教师应该只能查看自己创建的课程")
    void shouldApplyTeacherCourseDataScope() {
        // Given
        Long teacherId = 100L;
        when(authentication.getName()).thenReturn("teacher001");

        SysUser teacher = createUser(teacherId, "teacher001");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(teacher);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_TEACHER")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_COURSE, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertEquals("teacher_id = 100", info.getSqlCondition());
    }

    // ==================== 表别名测试 ====================

    @Test
    @DisplayName("应该正确添加表别名到SQL条件")
    void shouldApplyTableAliasToSqlCondition() {
        // Given
        Long teacherId = 100L;
        when(authentication.getName()).thenReturn("teacher001");

        SysUser teacher = createUser(teacherId, "teacher001");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(teacher);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_TEACHER")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "c");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertEquals("c.teacher_id = 100", info.getSqlCondition());
    }

    // ==================== 特殊情况测试 ====================

    @Test
    @DisplayName("应该处理无认证信息的情况")
    void shouldHandleNoAuthenticationCase() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertNull(info); // 无认证信息时不设置数据权限
    }

    @Test
    @DisplayName("应该处理用户不存在的情况")
    void shouldHandleUserNotFoundCase() {
        // Given
        when(authentication.getName()).thenReturn("nonexistent");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(null);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertNull(info);
    }

    @Test
    @DisplayName("应该处理ALL类型数据权限")
    void shouldHandleAllDataScopeType() {
        // Given
        Long teacherId = 100L;
        when(authentication.getName()).thenReturn("teacher001");

        SysUser teacher = createUser(teacherId, "teacher001");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(teacher);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_TEACHER")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.ALL, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertNotNull(info);
        assertEquals("", info.getSqlCondition()); // ALL类型不添加过滤条件
    }

    @Test
    @DisplayName("方法执行后应该清除ThreadLocal")
    void shouldClearThreadLocalAfterMethodExecution() {
        // Given
        Long teacherId = 100L;
        when(authentication.getName()).thenReturn("teacher001");

        SysUser teacher = createUser(teacherId, "teacher001");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(teacher);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_TEACHER")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);
        assertNotNull(DataScopeContext.get()); // Before执行后有数据

        dataScopeAspect.doAfter(joinPoint, dataScope);

        // Then
        assertNull(DataScopeContext.get()); // After执行后清除
    }

    @Test
    @DisplayName("应该正确设置用户ID到数据权限信息")
    void shouldSetUserIdToDataScopeInfo() {
        // Given
        Long teacherId = 999L;
        when(authentication.getName()).thenReturn("teacher999");

        SysUser teacher = createUser(teacherId, "teacher999");
        when(sysUserService.lambdaQuery()).thenReturn(mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryChainWrapper.class));
        when(sysUserService.lambdaQuery().eq(any(), any()).one()).thenReturn(teacher);

        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_TEACHER")
        );
        when(authentication.getAuthorities()).thenReturn((Collection<GrantedAuthority>) authorities);

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "");

        // When
        dataScopeAspect.doBefore(joinPoint, dataScope);

        // Then
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertNotNull(info);
        assertEquals(999L, info.getUserId());
    }

    @Test
    @DisplayName("应该处理异常情况而不中断程序")
    void shouldHandleExceptionGracefully() {
        // Given
        when(authentication.getName()).thenReturn("test");
        when(sysUserService.lambdaQuery()).thenThrow(new RuntimeException("数据库异常"));

        DataScope dataScope = mockDataScope(DataScopeType.TEACHER_CLASS, "");

        // When & Then - 不应抛出异常
        assertDoesNotThrow(() -> {
            dataScopeAspect.doBefore(joinPoint, dataScope);
        });

        // 异常情况下不应设置数据权限
        DataScopeContext.DataScopeInfo info = DataScopeContext.get();
        assertNull(info);
    }

    // ==================== 辅助方法 ====================

    private SysUser createUser(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private DataScope mockDataScope(DataScopeType type, String tableAlias) {
        return new DataScope() {
            @Override
            public DataScopeType value() {
                return type;
            }

            @Override
            public String tableAlias() {
                return tableAlias;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DataScope.class;
            }
        };
    }
}
