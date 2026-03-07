package com.ice.exebackend.aspect;

import com.ice.exebackend.annotation.DataScope;
import com.ice.exebackend.annotation.DataScopeType;
import com.ice.exebackend.context.DataScopeContext;
import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.service.SysUserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 数据权限 AOP 拦截器
 * 拦截带有 @DataScope 注解的方法，自动添加数据权限过滤条件
 *
 * 工作原理:
 * 1. @Before: 方法执行前，从SecurityContext获取当前用户信息
 * 2. 判断用户角色，如果是管理员则不添加过滤条件
 * 3. 如果是普通教师，根据DataScopeType生成SQL过滤条件
 * 4. 将过滤条件存入ThreadLocal，供Service层使用
 * 5. @After: 方法执行后，清除ThreadLocal，防止内存泄漏
 */
@Aspect
@Component
public class DataScopeAspect {

    private static final Logger logger = LoggerFactory.getLogger(DataScopeAspect.class);

    @Autowired
    private SysUserService sysUserService;

    /**
     * 方法执行前：设置数据权限过滤条件
     */
    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint point, DataScope dataScope) {
        try {
            // 1. 获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                logger.warn("数据权限拦截：未获取到认证信息");
                return;
            }

            // 2. 获取用户信息
            String username = authentication.getName();
            SysUser user = sysUserService.lambdaQuery()
                    .eq(SysUser::getUsername, username)
                    .one();

            if (user == null) {
                logger.warn("数据权限拦截：未找到用户 {}", username);
                return;
            }

            // 3. 判断是否为管理员（ADMIN或SUPER_ADMIN）
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority())
                            || "ROLE_SUPER_ADMIN".equals(auth.getAuthority()));

            // 4. 创建数据权限信息对象
            DataScopeContext.DataScopeInfo info = new DataScopeContext.DataScopeInfo();
            info.setUserId(user.getId());
            info.setAdmin(isAdmin);

            // 5. 如果不是管理员，添加数据权限过滤条件
            if (!isAdmin) {
                String sqlCondition = buildSqlCondition(dataScope, user.getId());
                info.setSqlCondition(sqlCondition);
                logger.debug("数据权限拦截：用户 {} (ID: {}) 应用数据权限过滤: {}",
                        username, user.getId(), sqlCondition);
            } else {
                logger.debug("数据权限拦截：用户 {} (ID: {}) 是管理员，不应用数据权限过滤",
                        username, user.getId());
            }

            // 6. 存入ThreadLocal
            DataScopeContext.set(info);

        } catch (Exception e) {
            logger.error("数据权限拦截异常", e);
        }
    }

    /**
     * 方法执行后：清除ThreadLocal，防止内存泄漏
     */
    @After("@annotation(dataScope)")
    public void doAfter(JoinPoint point, DataScope dataScope) {
        DataScopeContext.clear();
    }

    /**
     * 根据数据权限类型构建SQL过滤条件
     *
     * @param dataScope 数据权限注解
     * @param userId    用户ID
     * @return SQL WHERE条件
     */
    private String buildSqlCondition(DataScope dataScope, Long userId) {
        DataScopeType type = dataScope.value();
        String tableAlias = dataScope.tableAlias();
        String prefix = tableAlias.isEmpty() ? "" : tableAlias + ".";

        switch (type) {
            case TEACHER_CLASS:
                // 教师班级权限：只能查看自己负责的班级
                return prefix + "teacher_id = " + userId;

            case TEACHER_STUDENT:
                // 教师学生权限：只能查看自己班级的学生
                return prefix + "id IN (" +
                        "SELECT cs.student_id FROM biz_class_student cs " +
                        "INNER JOIN biz_class c ON cs.class_id = c.id " +
                        "WHERE c.teacher_id = " + userId + ")";

            case TEACHER_EXAM:
                // 教师考试权限：只能查看自己班级学生的考试成绩
                return prefix + "student_id IN (" +
                        "SELECT cs.student_id FROM biz_class_student cs " +
                        "INNER JOIN biz_class c ON cs.class_id = c.id " +
                        "WHERE c.teacher_id = " + userId + ")";

            case TEACHER_COURSE:
                // 教师课程权限：只能查看自己创建的课程
                // 根据实际表结构调整字段名（可能是teacher_id或creator_id）
                return prefix + "teacher_id = " + userId;

            case ALL:
                // 全部权限：无过滤条件
                return "";

            default:
                logger.warn("未知的数据权限类型: {}", type);
                return "";
        }
    }
}
