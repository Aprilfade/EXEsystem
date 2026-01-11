package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizCourseProgress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程学习进度Mapper
 */
@Mapper
public interface BizCourseProgressMapper extends BaseMapper<BizCourseProgress> {
}
