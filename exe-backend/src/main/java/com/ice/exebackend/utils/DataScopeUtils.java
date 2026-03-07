package com.ice.exebackend.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.exebackend.context.DataScopeContext;

/**
 * 数据权限工具类
 * 提供便捷的方法在Service层应用数据权限过滤
 */
public class DataScopeUtils {

    /**
     * 将数据权限过滤条件应用到QueryWrapper
     *
     * 使用示例:
     * QueryWrapper<BizClass> wrapper = new QueryWrapper<>();
     * DataScopeUtils.applyDataScope(wrapper);
     * List<BizClass> classes = baseMapper.selectList(wrapper);
     *
     * @param wrapper MyBatis-Plus的QueryWrapper
     * @param <T>     实体类型
     * @return 应用了数据权限的QueryWrapper
     */
    public static <T> QueryWrapper<T> applyDataScope(QueryWrapper<T> wrapper) {
        DataScopeContext.DataScopeInfo scopeInfo = DataScopeContext.get();

        // 如果有数据权限信息，且不是管理员，且有SQL过滤条件
        if (scopeInfo != null && !scopeInfo.isAdmin() && scopeInfo.getSqlCondition() != null) {
            // 使用apply方法直接添加SQL条件
            wrapper.apply(scopeInfo.getSqlCondition());
        }

        return wrapper;
    }

    /**
     * 检查当前用户是否为管理员
     *
     * @return true-管理员，false-非管理员
     */
    public static boolean isAdmin() {
        DataScopeContext.DataScopeInfo scopeInfo = DataScopeContext.get();
        return scopeInfo != null && scopeInfo.isAdmin();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未获取到则返回null
     */
    public static Long getCurrentUserId() {
        DataScopeContext.DataScopeInfo scopeInfo = DataScopeContext.get();
        return scopeInfo != null ? scopeInfo.getUserId() : null;
    }

    /**
     * 获取SQL过滤条件
     *
     * @return SQL过滤条件字符串，如果未设置则返回null
     */
    public static String getSqlCondition() {
        DataScopeContext.DataScopeInfo scopeInfo = DataScopeContext.get();
        return scopeInfo != null ? scopeInfo.getSqlCondition() : null;
    }
}
