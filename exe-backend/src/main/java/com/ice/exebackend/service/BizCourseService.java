package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizCourse;
import com.ice.exebackend.entity.BizCourseResource;

// 关键：这里必须是 interface
public interface BizCourseService extends IService<BizCourse> {
    /**
     * 获取课程详情及其资源
     */
    BizCourse getCourseWithResources(Long id);

    /**
     * 保存或更新课程资源
     */
    void saveResource(BizCourseResource resource);
}