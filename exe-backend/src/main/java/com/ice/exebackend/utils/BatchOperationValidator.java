package com.ice.exebackend.utils;

import java.util.Collection;

/**
 * 批量操作验证工具类
 *
 * 功能：
 * 1. 统一验证批量操作的数量限制
 * 2. 防止单次操作过多数据导致性能问题
 * 3. 提供标准化的验证方法
 *
 * @author Ice
 * @date 2026-01-12
 */
public class BatchOperationValidator {

    /**
     * 批量删除最大数量
     */
    public static final int MAX_BATCH_DELETE = 500;

    /**
     * 批量更新最大数量
     */
    public static final int MAX_BATCH_UPDATE = 1000;

    /**
     * 批量查询最大数量
     */
    public static final int MAX_BATCH_QUERY = 1000;

    /**
     * 批量导出最大数量
     */
    public static final int MAX_BATCH_EXPORT = 5000;

    /**
     * 通用批量操作最大数量（默认值）
     */
    public static final int MAX_BATCH_DEFAULT = 500;

    /**
     * 验证批量删除操作
     *
     * @param collection 要删除的ID集合
     * @param <T>        集合元素类型
     * @return 验证结果消息，null表示验证通过
     */
    public static <T> String validateBatchDelete(Collection<T> collection) {
        return validateBatchOperation(collection, MAX_BATCH_DELETE, "删除");
    }

    /**
     * 验证批量更新操作
     *
     * @param collection 要更新的ID集合
     * @param <T>        集合元素类型
     * @return 验证结果消息，null表示验证通过
     */
    public static <T> String validateBatchUpdate(Collection<T> collection) {
        return validateBatchOperation(collection, MAX_BATCH_UPDATE, "更新");
    }

    /**
     * 验证批量查询操作
     *
     * @param collection 要查询的ID集合
     * @param <T>        集合元素类型
     * @return 验证结果消息，null表示验证通过
     */
    public static <T> String validateBatchQuery(Collection<T> collection) {
        return validateBatchOperation(collection, MAX_BATCH_QUERY, "查询");
    }

    /**
     * 验证批量导出操作
     *
     * @param collection 要导出的ID集合
     * @param <T>        集合元素类型
     * @return 验证结果消息，null表示验证通过
     */
    public static <T> String validateBatchExport(Collection<T> collection) {
        return validateBatchOperation(collection, MAX_BATCH_EXPORT, "导出");
    }

    /**
     * 通用批量操作验证
     *
     * @param collection 要操作的集合
     * @param maxSize    最大数量
     * @param operation  操作名称（用于错误提示）
     * @param <T>        集合元素类型
     * @return 验证结果消息，null表示验证通过
     */
    public static <T> String validateBatchOperation(Collection<T> collection, int maxSize, String operation) {
        if (collection == null || collection.isEmpty()) {
            return "请选择要" + operation + "的记录";
        }

        if (collection.size() > maxSize) {
            return String.format("单次%s数量不能超过 %d 条，当前选择了 %d 条",
                    operation, maxSize, collection.size());
        }

        return null; // 验证通过
    }

    /**
     * 验证并抛出异常（用于不返回Result的场景）
     *
     * @param collection 要操作的集合
     * @param maxSize    最大数量
     * @param operation  操作名称
     * @param <T>        集合元素类型
     * @throws IllegalArgumentException 验证失败时抛出
     */
    public static <T> void validateBatchOperationOrThrow(Collection<T> collection, int maxSize, String operation) {
        String errorMsg = validateBatchOperation(collection, maxSize, operation);
        if (errorMsg != null) {
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
