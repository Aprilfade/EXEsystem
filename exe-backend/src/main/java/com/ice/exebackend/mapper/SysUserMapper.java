package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 声明这是一个MyBatis的Mapper接口
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 基础的增、删、改、查方法已由BaseMapper提供。
    // 如果有复杂的多表查询，可以在此定义额外的方法。
    // 例如，根据用户名查询用户（虽然BaseMapper的Wrapper也能实现）。
}