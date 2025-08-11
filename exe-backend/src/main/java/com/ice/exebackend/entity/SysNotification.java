package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class SysNotification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Boolean isPublished;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    public  String getTitle(){
        return title;
    }
    public  String getContent(){
        return content;
    }
    public  Boolean getIsPublished(){
        return isPublished;
    }
    public  LocalDateTime getPublishTime(){
        return publishTime;
    }
    public  LocalDateTime getCreateTime(){
        return createTime;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public void setContent(String content){
        this.content = content;
    }
    public void setIsPublished(Boolean isPublished){
        this.isPublished = isPublished;
    }
    public void setPublishTime(LocalDateTime publishTime){
        this.publishTime = publishTime;
    }
    public void setCreateTime(LocalDateTime createTime){
        this.createTime = createTime;
    }
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

}