package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizAiTutorStudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.Map;

@Mapper
public interface BizAiTutorStudyRecordMapper extends BaseMapper<BizAiTutorStudyRecord> {

    /**
     * 获取学生的学习统计数据
     */
    @Select("SELECT " +
            "subject, " +
            "SUM(study_time) as totalStudyTime, " +
            "SUM(exercise_count) as totalExercises, " +
            "SUM(correct_count) as totalCorrect, " +
            "COUNT(DISTINCT chapter_id) as completedChapters " +
            "FROM biz_ai_tutor_study_record " +
            "WHERE student_id = #{studentId} " +
            "AND subject = #{subject} " +
            "GROUP BY subject")
    Map<String, Object> getStudyStats(@Param("studentId") Long studentId, @Param("subject") String subject);
}
