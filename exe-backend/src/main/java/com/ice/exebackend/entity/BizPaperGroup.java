package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_paper_group")
public class BizPaperGroup {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private String name;
    private Integer sortOrder;
}