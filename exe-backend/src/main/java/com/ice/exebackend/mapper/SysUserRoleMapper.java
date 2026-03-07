// exe-backend/src/main/java/com/ice/exebackend/mapper/SysUserRoleMapper.java
package com.ice.exebackend.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    /**
     * 根据角色ID查询所有用户ID
     * 用于角色权限修改时批量清除用户权限缓存
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}