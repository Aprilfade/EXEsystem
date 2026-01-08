package com.ice.exebackend.dto.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作台数据导出 DTO
 *
 * @author ice
 */
@Data
public class DashboardExportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "统计项目", index = 0)
    private String itemName;

    @ExcelProperty(value = "数量", index = 1)
    private Long count;

    @ExcelProperty(value = "周同比趋势(%)", index = 2)
    private String trend;

    @ExcelProperty(value = "备注", index = 3)
    private String remark;

    public DashboardExportDTO() {
    }

    public DashboardExportDTO(String itemName, Long count, Double trend, String remark) {
        this.itemName = itemName;
        this.count = count;
        this.trend = trend != null ? String.format("%.1f%%", trend) : "-";
        this.remark = remark;
    }
}
