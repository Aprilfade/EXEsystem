package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizAiTutorStudyRecord;
import com.ice.exebackend.mapper.BizAiTutorStudyRecordMapper;
import com.ice.exebackend.service.BizAiTutorStudyRecordService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class BizAiTutorStudyRecordServiceImpl extends ServiceImpl<BizAiTutorStudyRecordMapper, BizAiTutorStudyRecord> implements BizAiTutorStudyRecordService {

    @Resource
    private BizAiTutorStudyRecordMapper studyRecordMapper;

    @Override
    public Map<String, Object> getStudyStats(Long studentId, String subject) {
        Map<String, Object> stats = studyRecordMapper.getStudyStats(studentId, subject);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("totalStudyTime", 0);
            stats.put("totalExercises", 0);
            stats.put("totalCorrect", 0);
            stats.put("completedChapters", 0);
        }
        return stats;
    }
}
