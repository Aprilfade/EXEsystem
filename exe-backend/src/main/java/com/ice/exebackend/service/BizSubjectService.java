package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.SubjectStatsDTO;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.entity.BizSubject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 导入

import java.util.List;

public interface BizSubjectService extends IService<BizSubject> {
    // 暂时不需要额外方法，但为了结构清晰，我们创建它
    // 未来复杂的业务逻辑可以加在这里
    // 【新增】获取包含统计数据的科目分页列表
    Page<SubjectStatsDTO> getSubjectStatsPage(Page<BizSubject> page, QueryWrapper<BizSubject> queryWrapper);
    // 【新增】获取包含统计数据的科目列表（不分页）- 用于学生端
    List<SubjectStatsDTO> getSubjectStatsList(QueryWrapper<BizSubject> queryWrapper);
    // 【新增】专门用于获取科目下试题列表的新方法
    List<BizQuestion> getQuestionsForSubject(Long subjectId);
}