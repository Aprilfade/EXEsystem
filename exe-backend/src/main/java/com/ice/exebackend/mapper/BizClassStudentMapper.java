package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizClassStudent;
import org.apache.ibatis.annotations.Mapper;

// 必须是 interface，且继承 BaseMapper，加上 @Mapper 注解
@Mapper
public interface BizClassStudentMapper extends BaseMapper<BizClassStudent> {
}