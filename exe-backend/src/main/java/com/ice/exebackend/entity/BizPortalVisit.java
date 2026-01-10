package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Portal访问记录实体
 *
 * @author System
 * @version v3.05
 */
@Data
@TableName("biz_portal_visit")
public class BizPortalVisit {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 系统ID
     */
    private String systemId;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型: admin/teacher/student
     */
    private String userType;

    /**
     * 访问时间
     */
    private LocalDateTime visitTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
