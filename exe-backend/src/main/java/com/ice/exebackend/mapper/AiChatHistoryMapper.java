package com.ice.exebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.exebackend.entity.AiChatHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话历史Mapper
 *
 * @author AI功能组
 * @version v3.05
 */
@Mapper
public interface AiChatHistoryMapper extends BaseMapper<AiChatHistory> {
}
