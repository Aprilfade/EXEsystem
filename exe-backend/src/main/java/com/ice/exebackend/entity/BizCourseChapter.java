package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程章节实体类
 * 支持无限层级的章节嵌套结构
 */
@Data
@TableName("biz_course_chapter")
public class BizCourseChapter {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 父章节ID，0表示根章节 */
    private Long parentId;

    /** 章节名称 */
    private String name;

    /** 章节描述 */
    private String description;

    /** 排序顺序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    // 非数据库字段 - 子章节列表
    @TableField(exist = false)
    private List<BizCourseChapter> children;

    // 非数据库字段 - 章节下的资源列表
    @TableField(exist = false)
    private List<BizCourseResource> resources;

    // === 手动添加 Getter 和 Setter ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public List<BizCourseChapter> getChildren() { return children; }
    public void setChildren(List<BizCourseChapter> children) { this.children = children; }

    public List<BizCourseResource> getResources() { return resources; }
    public void setResources(List<BizCourseResource> resources) { this.resources = resources; }
}
