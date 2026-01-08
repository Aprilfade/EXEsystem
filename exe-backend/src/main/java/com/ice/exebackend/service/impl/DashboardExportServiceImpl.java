package com.ice.exebackend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.ice.exebackend.dto.DashboardStatsDTO;
import com.ice.exebackend.dto.export.DashboardExportDTO;
import com.ice.exebackend.service.DashboardExportService;
import com.ice.exebackend.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作台数据导出服务实现
 *
 * @author ice
 */
@Service
public class DashboardExportServiceImpl implements DashboardExportService {

    @Autowired
    private DashboardService dashboardService;

    @Override
    public void exportToExcel(HttpServletResponse response) throws Exception {
        // 获取统计数据
        DashboardStatsDTO stats = dashboardService.getDashboardStats(null);

        // 构建导出数据
        List<DashboardExportDTO> exportData = buildExportData(stats);

        // 设置响应头
        setExcelResponseHeader(response);

        // 写入 Excel
        EasyExcel.write(response.getOutputStream(), DashboardExportDTO.class)
                .sheet("工作台统计数据")
                .doWrite(exportData);
    }

    /**
     * 构建导出数据
     */
    private List<DashboardExportDTO> buildExportData(DashboardStatsDTO stats) {
        List<DashboardExportDTO> data = new ArrayList<>();

        // 添加基础统计数据
        if (stats.getTrends() != null) {
            data.add(new DashboardExportDTO(
                    "学生总数",
                    stats.getStudentCount(),
                    stats.getTrends().getStudentCountTrend(),
                    "系统注册学生总数"
            ));

            data.add(new DashboardExportDTO(
                    "科目数量",
                    stats.getSubjectCount(),
                    stats.getTrends().getSubjectCountTrend(),
                    "开设科目总数"
            ));

            data.add(new DashboardExportDTO(
                    "知识点总数",
                    stats.getKnowledgePointCount(),
                    stats.getTrends().getKnowledgePointCountTrend(),
                    "题库知识点总数"
            ));

            data.add(new DashboardExportDTO(
                    "题目总数",
                    stats.getQuestionCount(),
                    stats.getTrends().getQuestionCountTrend(),
                    "题库题目总数"
            ));

            data.add(new DashboardExportDTO(
                    "试卷总数",
                    stats.getPaperCount(),
                    stats.getTrends().getPaperCountTrend(),
                    "已创建试卷总数"
            ));
        } else {
            // 如果没有趋势数据，只导出基础数据
            data.add(new DashboardExportDTO("学生总数", stats.getStudentCount(), null, "系统注册学生总数"));
            data.add(new DashboardExportDTO("科目数量", stats.getSubjectCount(), null, "开设科目总数"));
            data.add(new DashboardExportDTO("知识点总数", stats.getKnowledgePointCount(), null, "题库知识点总数"));
            data.add(new DashboardExportDTO("题目总数", stats.getQuestionCount(), null, "题库题目总数"));
            data.add(new DashboardExportDTO("试卷总数", stats.getPaperCount(), null, "已创建试卷总数"));
        }

        return data;
    }

    /**
     * 设置 Excel 响应头
     */
    private void setExcelResponseHeader(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        String fileName = "工作台数据_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }
}
