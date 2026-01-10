package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.SysUserNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户通知记录Mapper
 *
 * @author Claude Code Assistant
 * @since v3.06
 */
@Mapper
public interface SysUserNotificationMapper extends BaseMapper<SysUserNotification> {

    /**
     * 统计用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer countUnread(@Param("userId") Long userId);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 更新条数
     */
    Integer markAllAsRead(@Param("userId") Long userId);
}
