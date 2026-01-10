package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI家教学习记录实体
 * 记录学生在AI家教模块的学习行为和进度
 */
@Data
@TableName("biz_ai_tutor_study_record")
public class BizAiTutorStudyRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 科目（math/physics/chemistry等） */
    private String subject;

    /** 章节ID */
    private Integer chapterId;

    /** 章节名称 */
    private String chapterName;

    /** 小节ID */
    private Integer sectionId;

    /** 小节标题 */
    private String sectionTitle;

    /** 学习时长（分钟） */
    private Integer studyTime;

    /** 练习题数量 */
    private Integer exerciseCount;

    /** 正确题目数量 */
    private Integer correctCount;

    /** 是否完成 */
    private Boolean completed;

    /** 学习进度（0-100） */
    private Integer progress;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
