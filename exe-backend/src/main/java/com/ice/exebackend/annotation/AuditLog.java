package com.ice.exebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 *
 * 用于标记需要记录审计日志的方法
 * 通过AOP拦截并记录操作信息
 *
 * @author Ice
 * @date 2026-01-12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 操作模块
     */
    String module();

    /**
     * 操作类型
     */
    OperationType operationType();

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 查询
         */
        SELECT,

        /**
         * 新增
         */
        INSERT,

        /**
         * 修改
         */
        UPDATE,

        /**
         * 删除
         */
        DELETE,

        /**
         * 登录
         */
        LOGIN,

        /**
         * 登出
         */
        LOGOUT,

        /**
         * 导出
         */
        EXPORT,

        /**
         * 导入
         */
        IMPORT,

        /**
         * 审批
         */
        APPROVE,

        /**
         * 发布
         */
        PUBLISH,

        /**
         * 其他
         */
        OTHER
    }
}
