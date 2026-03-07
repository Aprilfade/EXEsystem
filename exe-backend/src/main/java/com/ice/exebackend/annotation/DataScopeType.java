package com.ice.exebackend.annotation;

/**
 * 数据权限类型枚举
 * 定义不同的数据权限范围，用于实现数据级别的访问控制
 */
public enum DataScopeType {
    /**
     * 教师班级权限：只能查看自己负责的班级
     * SQL条件示例: teacher_id = #{userId}
     */
    TEACHER_CLASS,

    /**
     * 教师学生权限：只能查看自己班级的学生
     * SQL条件示例: id IN (SELECT student_id FROM biz_class_student
     *              WHERE class_id IN (SELECT id FROM biz_class WHERE teacher_id = #{userId}))
     */
    TEACHER_STUDENT,

    /**
     * 教师考试权限：只能查看自己班级学生的考试成绩
     * SQL条件示例: student_id IN (SELECT cs.student_id FROM biz_class_student cs
     *              INNER JOIN biz_class c ON cs.class_id = c.id WHERE c.teacher_id = #{userId})
     */
    TEACHER_EXAM,

    /**
     * 教师课程权限：只能查看自己创建的课程
     * SQL条件示例: teacher_id = #{userId} 或 creator_id = #{userId}
     */
    TEACHER_COURSE,

    /**
     * 全部数据权限（管理员或超级管理员）
     * 无SQL过滤条件
     */
    ALL
}
