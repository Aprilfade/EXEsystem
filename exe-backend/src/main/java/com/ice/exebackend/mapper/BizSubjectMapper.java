package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.BizSubject;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizSubjectMapper extends BaseMapper<BizSubject> {
    // 基础的 CRUD 方法已由 BaseMapper 提供
}