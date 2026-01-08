package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.SysUserPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户个性化配置 Mapper
 *
 * @author ice
 */
@Mapper
public interface SysUserPreferenceMapper extends BaseMapper<SysUserPreference> {
}
