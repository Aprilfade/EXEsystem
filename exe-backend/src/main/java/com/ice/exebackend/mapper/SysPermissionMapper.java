package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}