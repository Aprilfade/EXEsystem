package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.SubjectStatsDTO;
import com.ice.exebackend.entity.BizSubject;

public interface BizSubjectService extends IService<BizSubject> {
    // 暂时不需要额外方法，但为了结构清晰，我们创建它
    // 未来复杂的业务逻辑可以加在这里
    // 【新增】获取包含统计数据的科目分页列表
    Page<SubjectStatsDTO> getSubjectStatsPage(Page<BizSubject> page);
}