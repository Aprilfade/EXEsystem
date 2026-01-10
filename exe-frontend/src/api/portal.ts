import request from '@/utils/request';
import type { ApiResult } from './user';

// ========== DTO 接口定义 ==========

/**
 * 系统访问统计数据
 */
export interface SystemVisitStats {
  systemId: string;
  systemName: string;
  visitCount: number;
  lastVisitTime?: string;
}

/**
 * 访问趋势数据点
 */
export interface VisitTrendDataPoint {
  date: string;        // 日期，格式：YYYY-MM-DD
  count: number;       // 访问次数
  systems?: {          // 各系统的访问次数详情
    [systemId: string]: number;
  };
}

/**
 * 系统使用分布数据
 */
export interface SystemUsageDistribution {
  systemId: string;
  systemName: string;
  count: number;
  percentage: number;
  color: string;
}

/**
 * 热力图单元格数据
 */
export interface HeatmapCell {
  date: string;        // 日期，格式：YYYY-MM-DD
  day: number;         // 星期几，0-6 (周日-周六)
  week: number;        // 第几周
  count: number;       // 访问次数
  level: number;       // 热度等级，0-4
}

/**
 * 访问记录提交数据
 */
export interface VisitRecordData {
  systemId: string;
  systemName: string;
  visitTime: string;   // ISO 8601 格式
  userId?: number;
  userType?: 'admin' | 'teacher' | 'student';
}

/**
 * 最近访问记录
 */
export interface RecentAccessRecord {
  id: string;
  name: string;
  icon: string;
  time: string;        // 相对时间，如 "5分钟前"
  path: string;
  gradient?: string;
}

/**
 * 访问统计汇总
 */
export interface VisitStatsSummary {
  totalVisits: number;      // 总访问次数
  todayVisits: number;      // 今日访问次数
  weekVisits: number;       // 本周访问次数
  monthVisits: number;      // 本月访问次数
  maxDailyVisits: number;   // 最高单日访问
  avgDailyVisits: number;   // 日均访问
}

// ========== API 函数 ==========

/**
 * 获取系统访问统计数据
 * @param days 统计天数，默认 30 天
 */
export function fetchSystemVisitStats(days: number = 30): Promise<ApiResult<SystemVisitStats[]>> {
  return request({
    url: '/api/v1/portal/visit-stats',
    method: 'get',
    params: { days }
  });
}

/**
 * 获取访问趋势数据
 * @param days 统计天数，默认 7 天
 */
export function fetchVisitTrend(days: number = 7): Promise<ApiResult<VisitTrendDataPoint[]>> {
  return request({
    url: '/api/v1/portal/visit-trend',
    method: 'get',
    params: { days }
  });
}

/**
 * 获取系统使用分布数据
 */
export function fetchSystemUsageDistribution(): Promise<ApiResult<SystemUsageDistribution[]>> {
  return request({
    url: '/api/v1/portal/usage-distribution',
    method: 'get'
  });
}

/**
 * 获取热力图数据
 * @param days 统计天数，默认 30 天
 */
export function fetchHeatmapData(days: number = 30): Promise<ApiResult<HeatmapCell[]>> {
  return request({
    url: '/api/v1/portal/heatmap',
    method: 'get',
    params: { days }
  });
}

/**
 * 记录系统访问
 * @param data 访问记录数据
 */
export function recordVisit(data: VisitRecordData): Promise<ApiResult<void>> {
  return request({
    url: '/api/v1/portal/record-visit',
    method: 'post',
    data
  });
}

/**
 * 获取最近访问记录
 * @param limit 返回记录数量，默认 5 条
 */
export function fetchRecentAccess(limit: number = 5): Promise<ApiResult<RecentAccessRecord[]>> {
  return request({
    url: '/api/v1/portal/recent-access',
    method: 'get',
    params: { limit }
  });
}

/**
 * 获取访问统计汇总数据
 */
export function fetchVisitSummary(): Promise<ApiResult<VisitStatsSummary>> {
  return request({
    url: '/api/v1/portal/visit-summary',
    method: 'get'
  });
}

/**
 * 批量记录访问（用于离线同步）
 * @param records 访问记录数组
 */
export function batchRecordVisits(records: VisitRecordData[]): Promise<ApiResult<void>> {
  return request({
    url: '/api/v1/portal/batch-record-visits',
    method: 'post',
    data: { records }
  });
}
