package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_goods")
public class BizGoods {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private String imageUrl;
    private String type; // AVATAR_FRAME, BACKGROUND
    private String resourceValue;
    private Integer stock;
    private Integer isEnabled;
    private LocalDateTime createTime;

    // 这是一个辅助字段，用于前端判断是否已拥有，不映射数据库
    @TableField(exist = false)
    private Boolean isOwned;

    // === 手动添加 Getter 和 Setter ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getResourceValue() { return resourceValue; }
    public void setResourceValue(String resourceValue) { this.resourceValue = resourceValue; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Integer isEnabled) { this.isEnabled = isEnabled; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Boolean getIsOwned() { return isOwned; }
    public void setIsOwned(Boolean isOwned) { this.isOwned = isOwned; }
}