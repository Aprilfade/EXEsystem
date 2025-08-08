package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_paper_image")
public class BizPaperImage {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private String imageUrl;
    private Integer sortOrder;


    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public Long getPaperId(){
        return paperId;
    }
    public void setPaperId(Long paperId){
        this.paperId = paperId;
    }
    public String getImageUrl(){
        return imageUrl;
    }
    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
    public Integer getSortOrder(){
        return sortOrder;
    }
    public void setSortOrder(Integer sortOrder){
        this.sortOrder = sortOrder;
    }
}