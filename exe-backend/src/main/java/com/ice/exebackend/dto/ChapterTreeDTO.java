package com.ice.exebackend.dto;

import lombok.Data;
import com.ice.exebackend.entity.BizCourseResource;
import java.util.List;

/**
 * 章节树DTO
 * 用于前端展示章节树结构
 */
@Data
public class ChapterTreeDTO {

    /** 章节ID */
    private Long id;

    /** 章节名称 */
    private String name;

    /** 章节描述 */
    private String description;

    /** 节点类型：chapter或resource */
    private String type;

    /** 子章节列表 */
    private List<ChapterTreeDTO> children;

    /** 章节下的资源列表 */
    private List<BizCourseResource> resources;

    // === 手动添加 Getter 和 Setter ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<ChapterTreeDTO> getChildren() { return children; }
    public void setChildren(List<ChapterTreeDTO> children) { this.children = children; }

    public List<BizCourseResource> getResources() { return resources; }
    public void setResources(List<BizCourseResource> resources) { this.resources = resources; }
}
