package com.ice.exebackend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 批阅历史导出 DTO
 *
 * @author Claude Code Assistant
 * @since v3.05
 */
@Data
public class GradingHistoryExportDTO {

    @ExcelProperty("成绩ID")
    private Long examResultId;

    @ExcelProperty("批阅人")
    private String graderName;

    @ExcelProperty("操作类型")
    private String actionType;

    @ExcelProperty("旧分数")
    private Integer oldScore;

    @ExcelProperty("新分数")
    private Integer newScore;

    @ExcelProperty("旧评语")
    private String oldComment;

    @ExcelProperty("新评语")
    private String newComment;

    @ExcelProperty("修改原因")
    private String reason;

    @ExcelProperty("操作时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
