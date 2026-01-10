package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.*;
import com.ice.exebackend.entity.BizPortalVisit;
import com.ice.exebackend.mapper.BizPortalVisitMapper;
import com.ice.exebackend.service.PortalService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Portal访问记录服务实现
 *
 * @author System
 * @version v3.05
 */
@Service
public class PortalServiceImpl extends ServiceImpl<BizPortalVisitMapper, BizPortalVisit> implements PortalService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-DD");

    // 系统配置映射（与前端保持一致）
    private static final Map<String, Map<String, String>> SYSTEM_CONFIG = new HashMap<>();
    static {
        Map<String, String> admin = new HashMap<>();
        admin.put("icon", "Management");
        admin.put("path", "/home");
        admin.put("gradient", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        SYSTEM_CONFIG.put("admin", admin);

        Map<String, String> student = new HashMap<>();
        student.put("icon", "School");
        student.put("path", "/student/dashboard");
        student.put("gradient", "linear-gradient(135deg, #11998e 0%, #38ef7d 100%)");
        SYSTEM_CONFIG.put("student", student);

        Map<String, String> datascreen = new HashMap<>();
        datascreen.put("icon", "DataBoard");
        datascreen.put("path", "/data-screen");
        datascreen.put("gradient", "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)");
        SYSTEM_CONFIG.put("datascreen", datascreen);
    }

    @Override
    public List<SystemVisitStatsDTO> getSystemVisitStats(Integer days, Long userId) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days != null ? days : 30);
        return baseMapper.getSystemVisitStats(startTime, userId);
    }

    @Override
    public List<VisitTrendDataPointDTO> getVisitTrendData(Integer days, Long userId) {
        int queryDays = days != null ? days : 7;
        LocalDateTime startTime = LocalDateTime.now().minusDays(queryDays);

        // 获取原始数据
        List<Map<String, Object>> rawData = baseMapper.getVisitTrendData(startTime, userId);

        // 按日期分组
        Map<String, Map<String, Integer>> groupedData = new HashMap<>();
        for (Map<String, Object> row : rawData) {
            String date = row.get("date").toString();
            String systemId = row.get("systemId").toString();
            Integer count = Integer.parseInt(row.get("count").toString());

            groupedData.putIfAbsent(date, new HashMap<>());
            groupedData.get(date).put(systemId, count);
        }

        // 生成完整日期序列（包括0访问的日期）
        List<VisitTrendDataPointDTO> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate currentDate = endDate.minusDays(queryDays - 1);

        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            VisitTrendDataPointDTO dto = new VisitTrendDataPointDTO();
            dto.setDate(dateStr);

            Map<String, Integer> systems = groupedData.getOrDefault(dateStr, new HashMap<>());
            int totalCount = systems.values().stream().mapToInt(Integer::intValue).sum();

            dto.setCount(totalCount);
            dto.setSystems(systems);

            result.add(dto);
            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getSystemUsageDistribution(Long userId) {
        // 基于最近30天的访问统计
        List<SystemVisitStatsDTO> stats = getSystemVisitStats(30, userId);

        int total = stats.stream().mapToInt(SystemVisitStatsDTO::getVisitCount).sum();

        return stats.stream().map(stat -> {
            Map<String, Object> item = new HashMap<>();
            item.put("systemId", stat.getSystemId());
            item.put("systemName", stat.getSystemName());
            item.put("count", stat.getVisitCount());
            item.put("percentage", total > 0 ? Math.round((stat.getVisitCount() * 100.0) / total) : 0);
            item.put("color", getSystemColor(stat.getSystemId()));
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<HeatmapCellDTO> getHeatmapData(Integer days, Long userId) {
        int queryDays = days != null ? days : 30;
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(queryDays);

        List<Map<String, Object>> rawData = baseMapper.getHeatmapData(startTime, endTime, userId);

        // 计算最大访问次数，用于热度等级计算
        int maxCount = rawData.stream()
                .mapToInt(row -> Integer.parseInt(row.get("count").toString()))
                .max()
                .orElse(1);

        return rawData.stream().map(row -> {
            HeatmapCellDTO dto = new HeatmapCellDTO();
            String dateStr = row.get("date").toString();
            int count = Integer.parseInt(row.get("count").toString());
            int day = Integer.parseInt(row.get("day").toString());

            // 计算第几周
            LocalDate date = LocalDate.parse(dateStr);
            LocalDate startDate = startTime.toLocalDate();
            int week = (int) ChronoUnit.WEEKS.between(startDate, date);

            dto.setDate(dateStr);
            dto.setDay(day);
            dto.setWeek(week);
            dto.setCount(count);
            dto.setLevel(calculateHeatLevel(count, maxCount));

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void recordVisit(VisitRecordDTO visitRecord) {
        BizPortalVisit visit = new BizPortalVisit();
        visit.setSystemId(visitRecord.getSystemId());
        visit.setSystemName(visitRecord.getSystemName());
        visit.setUserId(visitRecord.getUserId());
        visit.setUserType(visitRecord.getUserType());

        // 解析ISO 8601格式时间
        visit.setVisitTime(LocalDateTime.parse(visitRecord.getVisitTime().substring(0, 19)));
        visit.setCreateTime(LocalDateTime.now());

        this.save(visit);
    }

    @Override
    public void batchRecordVisits(List<VisitRecordDTO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        List<BizPortalVisit> visits = records.stream().map(record -> {
            BizPortalVisit visit = new BizPortalVisit();
            visit.setSystemId(record.getSystemId());
            visit.setSystemName(record.getSystemName());
            visit.setUserId(record.getUserId());
            visit.setUserType(record.getUserType());
            visit.setVisitTime(LocalDateTime.parse(record.getVisitTime().substring(0, 19)));
            visit.setCreateTime(LocalDateTime.now());
            return visit;
        }).collect(Collectors.toList());

        this.saveBatch(visits);
    }

    @Override
    public List<RecentAccessRecordDTO> getRecentAccess(Long userId, Integer limit) {
        List<Map<String, Object>> rawData = baseMapper.getRecentAccess(userId, limit != null ? limit : 5);

        return rawData.stream().map(row -> {
            RecentAccessRecordDTO dto = new RecentAccessRecordDTO();
            String systemId = row.get("systemId").toString();
            String systemName = row.get("systemName").toString();
            LocalDateTime visitTime = (LocalDateTime) row.get("visitTime");

            dto.setId(systemId);
            dto.setName(systemName);
            dto.setIcon(getSystemIcon(systemId));
            dto.setTime(formatRelativeTime(visitTime));
            dto.setPath(getSystemPath(systemId));
            dto.setGradient(getSystemGradient(systemId));

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getVisitSummary(Long userId) {
        Map<String, Object> summary = baseMapper.getVisitSummary(userId);

        // 计算最高单日访问和日均访问
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        List<VisitTrendDataPointDTO> trendData = getVisitTrendData(30, userId);

        int maxDailyVisits = trendData.stream()
                .mapToInt(VisitTrendDataPointDTO::getCount)
                .max()
                .orElse(0);

        int totalVisits = Integer.parseInt(summary.get("totalVisits").toString());
        int avgDailyVisits = totalVisits > 0 ? Math.round(totalVisits / 30.0f) : 0;

        summary.put("maxDailyVisits", maxDailyVisits);
        summary.put("avgDailyVisits", avgDailyVisits);

        return summary;
    }

    /**
     * 计算热度等级（0-4）
     */
    private int calculateHeatLevel(int count, int maxCount) {
        if (count == 0) return 0;
        double ratio = (double) count / maxCount;
        if (ratio >= 0.75) return 4;
        if (ratio >= 0.50) return 3;
        if (ratio >= 0.25) return 2;
        return 1;
    }

    /**
     * 格式化相对时间
     */
    private String formatRelativeTime(LocalDateTime visitTime) {
        Duration duration = Duration.between(visitTime, LocalDateTime.now());

        long minutes = duration.toMinutes();
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";

        long hours = duration.toHours();
        if (hours < 24) return hours + "小时前";

        long days = duration.toDays();
        if (days < 7) return days + "天前";
        if (days < 30) return (days / 7) + "周前";

        return visitTime.format(DateTimeFormatter.ofPattern("MM-dd"));
    }

    /**
     * 获取系统图标
     */
    private String getSystemIcon(String systemId) {
        Map<String, String> config = SYSTEM_CONFIG.get(systemId);
        return config != null ? config.get("icon") : "Document";
    }

    /**
     * 获取系统路径
     */
    private String getSystemPath(String systemId) {
        Map<String, String> config = SYSTEM_CONFIG.get(systemId);
        return config != null ? config.get("path") : "#";
    }

    /**
     * 获取系统渐变色
     */
    private String getSystemGradient(String systemId) {
        Map<String, String> config = SYSTEM_CONFIG.get(systemId);
        return config != null ? config.get("gradient") : "";
    }

    /**
     * 获取系统颜色
     */
    private String getSystemColor(String systemId) {
        String[] colors = {"#409eff", "#67c23a", "#e6a23c", "#f56c6c", "#909399"};
        int index = Math.abs(systemId.hashCode()) % colors.length;
        return colors[index];
    }
}
