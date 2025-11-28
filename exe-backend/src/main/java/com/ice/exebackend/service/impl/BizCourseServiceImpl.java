package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizCourse;
import com.ice.exebackend.entity.BizCourseResource;
import com.ice.exebackend.mapper.BizCourseMapper;
import com.ice.exebackend.mapper.BizCourseResourceMapper;
import com.ice.exebackend.service.BizCourseService; // 需创建此接口继承 IService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BizCourseServiceImpl extends ServiceImpl<BizCourseMapper, BizCourse> implements BizCourseService {

    @Autowired
    private BizCourseResourceMapper resourceMapper;

    public BizCourse getCourseWithResources(Long id) {
        BizCourse course = this.getById(id);
        if (course != null) {
            List<BizCourseResource> resources = resourceMapper.selectList(
                    new QueryWrapper<BizCourseResource>().eq("course_id", id).orderByAsc("sort_order")
            );
            course.setResources(resources);
        }
        return course;
    }

    @Transactional
    public void saveResource(BizCourseResource resource) {
        if (resource.getId() == null) {
            // 新增时自动设置排序为最后一个
            Long count = resourceMapper.selectCount(new QueryWrapper<BizCourseResource>().eq("course_id", resource.getCourseId()));
            resource.setSortOrder(count.intValue());
            resourceMapper.insert(resource);
        } else {
            resourceMapper.updateById(resource);
        }
    }
}