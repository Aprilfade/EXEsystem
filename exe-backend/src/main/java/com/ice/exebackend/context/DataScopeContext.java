package com.ice.exebackend.context;

/**
 * 数据权限上下文（基于 ThreadLocal）
 * 用于在请求处理过程中传递数据权限信息
 *
 * 工作流程:
 * 1. AOP拦截器在方法执行前将权限信息存入ThreadLocal
 * 2. Service层从ThreadLocal获取权限信息，构建SQL过滤条件
 * 3. AOP拦截器在方法执行后清除ThreadLocal，防止内存泄漏
 */
public class DataScopeContext {

    private static final ThreadLocal<DataScopeInfo> CONTEXT = new ThreadLocal<>();

    /**
     * 设置数据权限信息
     */
    public static void set(DataScopeInfo info) {
        CONTEXT.set(info);
    }

    /**
     * 获取数据权限信息
     */
    public static DataScopeInfo get() {
        return CONTEXT.get();
    }

    /**
     * 清除数据权限信息（必须在finally块中调用，防止内存泄漏）
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 数据权限信息类
     */
    public static class DataScopeInfo {
        /**
         * 当前用户ID
         */
        private Long userId;

        /**
         * 是否为管理员（管理员可以查看所有数据）
         */
        private boolean isAdmin;

        /**
         * SQL过滤条件（用于WHERE子句）
         * 例如: "teacher_id = 123" 或 "id IN (SELECT ...)"
         */
        private String sqlCondition;

        public DataScopeInfo() {
        }

        public DataScopeInfo(Long userId, boolean isAdmin, String sqlCondition) {
            this.userId = userId;
            this.isAdmin = isAdmin;
            this.sqlCondition = sqlCondition;
        }

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }

        public String getSqlCondition() {
            return sqlCondition;
        }

        public void setSqlCondition(String sqlCondition) {
            this.sqlCondition = sqlCondition;
        }
    }
}
