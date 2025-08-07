package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
// import lombok.Data; // 不再需要导入 @Data

// @Data // 我们不再使用 @Data 注解
@TableName("sys_role")
public class SysRole {
    private Long id;
    private String name;
    private String code;
    private String description;

    // --- 手动添加所有字段的 Getter 和 Setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}