package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI家教笔记实体
 * 存储学生在学习过程中记录的笔记
 */
@Data
@TableName("biz_ai_tutor_note")
public class BizAiTutorNote {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 笔记标题 */
    private String title;

    /** 笔记内容 */
    private String content;

    /** 标签 */
    private String tag;

    /** 所属章节ID */
    private Integer chapterId;

    /** 所属小节ID */
    private Integer sectionId;

    /** 科目 */
    private String subject;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
