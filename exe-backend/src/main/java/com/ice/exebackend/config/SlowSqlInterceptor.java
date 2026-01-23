package com.ice.exebackend.config;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.Properties;

/**
 * 慢SQL监控拦截器
 *
 * 功能：
 * 1. 记录执行时间超过阈值的SQL语句
 * 2. 记录SQL语句和参数（便于排查性能问题）
 * 3. 统计慢SQL次数
 *
 * @author Ice
 * @date 2026-01-12
 */
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})
public class SlowSqlInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SlowSqlInterceptor.class);

    /**
     * 慢SQL阈值（毫秒）
     * 超过此时间的SQL会被记录
     */
    private static final long SLOW_SQL_THRESHOLD_MS = 1000; // 1秒

    /**
     * 超慢SQL阈值（毫秒）
     * 超过此时间的SQL会记录WARNING级别日志
     */
    private static final long VERY_SLOW_SQL_THRESHOLD_MS = 3000; // 3秒

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        Object result = null;
        Throwable exception = null;

        try {
            // 执行SQL
            result = invocation.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            // 计算执行时间
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // 如果执行时间超过阈值，记录慢SQL
            if (executionTime >= SLOW_SQL_THRESHOLD_MS) {
                logSlowSql(invocation, executionTime, exception);
            }
        }
    }

    /**
     * 记录慢SQL日志
     *
     * @param invocation    拦截的方法调用
     * @param executionTime 执行时间（毫秒）
     * @param exception     执行过程中的异常（如果有）
     */
    private void logSlowSql(Invocation invocation, long executionTime, Throwable exception) {
        try {
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

            // 获取MappedStatement
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

            // 获取SQL语句
            String sql = statementHandler.getBoundSql().getSql();

            // 简化SQL（去除多余空格和换行）
            sql = sql.replaceAll("\\s+", " ").trim();

            // 获取SQL ID（mapper方法）
            String sqlId = mappedStatement.getId();

            // 获取参数对象
            Object parameterObject = statementHandler.getBoundSql().getParameterObject();

            // 根据执行时间选择日志级别
            if (executionTime >= VERY_SLOW_SQL_THRESHOLD_MS) {
                // 超慢SQL：WARNING级别
                logger.warn("⚠️ 超慢SQL检测 - 执行时间: {}ms | SQL ID: {} | SQL: {} | 参数: {} | 状态: {}",
                        executionTime,
                        sqlId,
                        sql.length() > 500 ? sql.substring(0, 500) + "..." : sql,
                        parameterObject,
                        exception != null ? "失败" : "成功");
            } else {
                // 慢SQL：INFO级别
                logger.info("🐌 慢SQL检测 - 执行时间: {}ms | SQL ID: {} | SQL: {} | 参数: {} | 状态: {}",
                        executionTime,
                        sqlId,
                        sql.length() > 500 ? sql.substring(0, 500) + "..." : sql,
                        parameterObject,
                        exception != null ? "失败" : "成功");
            }

            // 【可选】将慢SQL记录到数据库或监控系统
            // recordSlowSqlToDatabase(sqlId, sql, executionTime, parameterObject);

        } catch (Exception e) {
            // 日志记录失败不应影响正常SQL执行
            logger.error("记录慢SQL时发生异常", e);
        }
    }

    @Override
    public Object plugin(Object target) {
        // 使用Plugin.wrap生成代理对象
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件读取阈值
        // String threshold = properties.getProperty("slowSqlThreshold");
        // if (threshold != null) {
        //     SLOW_SQL_THRESHOLD_MS = Long.parseLong(threshold);
        // }
    }
}
