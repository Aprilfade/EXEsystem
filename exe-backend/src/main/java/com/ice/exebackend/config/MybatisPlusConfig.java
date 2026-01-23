package com.ice.exebackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 *
 * 功能：
 * 1. 配置乐观锁插件
 * 2. 配置分页插件（含安全限制）
 * 3. 配置慢SQL监控拦截器
 * 4. 防止恶意分页参数导致内存溢出
 *
 * @author Ice
 * @date 2026-01-12
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 最大单页数量（防止恶意请求导致OOM）
     * 例如：防止请求 size=99999999
     */
    private static final Long MAX_PAGE_SIZE = 1000L;

    /**
     * 注入慢SQL监控拦截器
     */
    @Autowired
    private SlowSqlInterceptor slowSqlInterceptor;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 【插件1】乐观锁插件（必须放在分页插件之前）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 【插件2】分页插件（带安全限制）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        // 【安全配置】限制单页最大数量（防止 size=99999999 导致内存溢出）
        paginationInterceptor.setMaxLimit(MAX_PAGE_SIZE);

        // 【安全配置】分页溢出处理策略
        // false: 当请求页码超出总页数时，不执行查询，返回空结果（推荐，防止深分页）
        // true: 当请求页码超出总页数时，查询第一页
        paginationInterceptor.setOverflow(false);

        // 【性能优化】优化COUNT查询（可选）
        // paginationInterceptor.setOptimizeJoin(true); // 优化JOIN查询的COUNT语句

        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }

    /**
     * 注册慢SQL监控拦截器
     * （已通过@Component自动注册，这里只是显式声明）
     */
    @Bean
    public SlowSqlInterceptor getSlowSqlInterceptor() {
        return slowSqlInterceptor;
    }
}
