package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizAiTutorStudyRecord;
import java.util.Map;

public interface BizAiTutorStudyRecordService extends IService<BizAiTutorStudyRecord> {

    /**
     * 获取学生的学习统计数据
     *
     * @param studentId 学生ID
     * @param subject 科目
     * @return 统计数据
     */
    Map<String, Object> getStudyStats(Long studentId, String subject);
}
