package com.ice.exebackend.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于标记需要进行数据权限过滤的方法
 *
 * 使用示例:
 * @DataScope(DataScopeType.TEACHER_CLASS)
 * public Result listClasses() {
 *     // 只能查询当前教师负责的班级
 * }
 *
 * @DataScope(value = DataScopeType.TEACHER_STUDENT, tableAlias = "s")
 * public Result listStudents() {
 *     // 只能查询当前教师班级的学生，表别名为"s"
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 数据权限类型
     * 默认为教师班级权限
     */
    DataScopeType value() default DataScopeType.TEACHER_CLASS;

    /**
     * 表别名（用于多表关联时指定过滤的表）
     * 例如: "SELECT * FROM biz_class c WHERE c.teacher_id = ?"
     * 此时 tableAlias = "c"
     *
     * 如果不指定，则默认不使用表别名
     */
    String tableAlias() default "";
}
