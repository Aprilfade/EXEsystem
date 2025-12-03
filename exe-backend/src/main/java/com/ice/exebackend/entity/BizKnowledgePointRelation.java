package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_knowledge_point_relation")
public class BizKnowledgePointRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private Long childId;

    // 手动 Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
}