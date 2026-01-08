package com.ice.exebackend.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 工作台数据导出服务接口
 *
 * @author ice
 */
public interface DashboardExportService {

    /**
     * 导出工作台数据为 Excel
     * @param response HTTP响应
     */
    void exportToExcel(HttpServletResponse response) throws Exception;
}
