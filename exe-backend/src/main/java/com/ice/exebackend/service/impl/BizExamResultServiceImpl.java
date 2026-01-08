package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.ExamResultDetailDTO;
import com.ice.exebackend.dto.KnowledgePointScoreAnalysisDTO;
import com.ice.exebackend.dto.ScoreExportDTO;
import com.ice.exebackend.dto.ScoreStatsDTO;
import com.ice.exebackend.entity.*;
import com.ice.exebackend.mapper.BizExamResultMapper;
import com.ice.exebackend.mapper.BizPaperQuestionMapper;
import com.ice.exebackend.mapper.BizQuestionKnowledgePointMapper;
import com.ice.exebackend.mapper.BizWrongRecordMapper;
import com.ice.exebackend.service.*;
import com.ice.exebackend.service.BizStudentKnowledgeMasteryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BizExamResultServiceImpl extends ServiceImpl<BizExamResultMapper, BizExamResult> implements BizExamResultService {

    @Lazy
    @Autowired
    private BizStudentService studentService;

    @Autowired
    private BizPaperService paperService;

    @Autowired
    private BizClassService classService;

    @Autowired
    private BizSubjectService subjectService;

    // 【知识点功能增强】注入知识点掌握度服务
    @Autowired
    private BizStudentKnowledgeMasteryService studentKnowledgeMasteryService;

    // 【知识点功能增强】注入必要的Mapper
    @Autowired
    private BizPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private BizQuestionKnowledgePointMapper questionKnowledgePointMapper;

    @Autowired
    private BizWrongRecordMapper wrongRecordMapper;

    @Autowired
    private BizKnowledgePointService knowledgePointService;

    @Override
    public Page<ExamResultDetailDTO> getExamResultsWithDetails(int current, int size, Map<String, Object> params) {
        Page<BizExamResult> page = new Page<>(current, size);
        QueryWrapper<BizExamResult> query = buildQueryWrapper(params);

        page(page, query);

        // 如果当前页没有数据，直接返回空页面
        if (page.getRecords().isEmpty()) {
            Page<ExamResultDetailDTO> detailPage = new Page<>(current, size, 0);
            detailPage.setRecords(Collections.emptyList());
            return detailPage;
        }

        // ✅ 优化：批量查询关联数据，避免N+1问题
        List<BizExamResult> results = page.getRecords();

        // 1. 批量查询学生信息
        List<Long> studentIds = results.stream()
                .map(BizExamResult::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, BizStudent> studentMap = studentService.listByIds(studentIds).stream()
                .collect(Collectors.toMap(BizStudent::getId, student -> student));

        // 2. 批量查询试卷信息
        List<Long> paperIds = results.stream()
                .map(BizExamResult::getPaperId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, BizPaper> paperMap = paperService.listByIds(paperIds).stream()
                .collect(Collectors.toMap(BizPaper::getId, paper -> paper));

        // 3. 批量查询科目信息
        List<Long> subjectIds = paperMap.values().stream()
                .map(BizPaper::getSubjectId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, BizSubject> subjectMap = subjectIds.isEmpty() ? Collections.emptyMap() :
                subjectService.listByIds(subjectIds).stream()
                        .collect(Collectors.toMap(BizSubject::getId, subject -> subject));

        // 转换为 DTO 并补充关联信息
        List<ExamResultDetailDTO> detailList = results.stream().map(result -> {
            ExamResultDetailDTO dto = new ExamResultDetailDTO();
            BeanUtils.copyProperties(result, dto);

            // 补充学生信息（从Map中获取）
            BizStudent student = studentMap.get(result.getStudentId());
            if (student != null) {
                dto.setStudentName(student.getName());
                dto.setStudentNo(student.getStudentNo());
                dto.setClassName(student.getClassName());
            }

            // 补充试卷科目信息（从Map中获取）
            BizPaper paper = paperMap.get(result.getPaperId());
            if (paper != null && paper.getSubjectId() != null) {
                dto.setSubjectId(paper.getSubjectId());
                BizSubject subject = subjectMap.get(paper.getSubjectId());
                if (subject != null) {
                    dto.setSubjectName(subject.getName());
                }
            }

            return dto;
        }).collect(Collectors.toList());

        Page<ExamResultDetailDTO> detailPage = new Page<>(current, size, page.getTotal());
        detailPage.setRecords(detailList);

        return detailPage;
    }

    @Override
    public ScoreStatsDTO getScoreStats(Map<String, Object> params) {
        QueryWrapper<BizExamResult> query = buildQueryWrapper(params);
        List<BizExamResult> results = list(query);

        ScoreStatsDTO stats = new ScoreStatsDTO();

        if (results.isEmpty()) {
            stats.setTotalCount(0);
            stats.setAverageScore(0.0);
            stats.setMaxScore(0);
            stats.setMinScore(0);
            stats.setPassCount(0);
            stats.setPassRate(0.0);
            stats.setExcellentCount(0);
            stats.setExcellentRate(0.0);
            stats.setScoreDistribution(new HashMap<>());
            return stats;
        }

        // 总人数
        int totalCount = results.size();
        stats.setTotalCount(totalCount);

        // 平均分
        double avgScore = results.stream()
                .mapToInt(BizExamResult::getScore)
                .average()
                .orElse(0.0);
        stats.setAverageScore(Math.round(avgScore * 100.0) / 100.0);

        // 最高分、最低分
        IntSummaryStatistics scoreStats = results.stream()
                .mapToInt(BizExamResult::getScore)
                .summaryStatistics();
        stats.setMaxScore(scoreStats.getMax());
        stats.setMinScore(scoreStats.getMin());

        // 及格人数和及格率（>=60分）
        long passCount = results.stream()
                .filter(r -> r.getScore() >= 60)
                .count();
        stats.setPassCount((int) passCount);
        stats.setPassRate(Math.round((passCount * 100.0 / totalCount) * 100.0) / 100.0);

        // 优秀人数和优秀率（>=90分）
        long excellentCount = results.stream()
                .filter(r -> r.getScore() >= 90)
                .count();
        stats.setExcellentCount((int) excellentCount);
        stats.setExcellentRate(Math.round((excellentCount * 100.0 / totalCount) * 100.0) / 100.0);

        // 成绩分段统计
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0-59", (int) results.stream().filter(r -> r.getScore() < 60).count());
        distribution.put("60-69", (int) results.stream().filter(r -> r.getScore() >= 60 && r.getScore() < 70).count());
        distribution.put("70-79", (int) results.stream().filter(r -> r.getScore() >= 70 && r.getScore() < 80).count());
        distribution.put("80-89", (int) results.stream().filter(r -> r.getScore() >= 80 && r.getScore() < 90).count());
        distribution.put("90-100", (int) results.stream().filter(r -> r.getScore() >= 90).count());
        stats.setScoreDistribution(distribution);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getStudentScoreTrend(Long studentId, Long subjectId) {
        QueryWrapper<BizExamResult> query = new QueryWrapper<>();
        query.eq("student_id", studentId);
        query.orderByAsc("create_time");

        List<BizExamResult> results = list(query);

        // 如果指定了科目，过滤科目
        if (subjectId != null) {
            results = results.stream().filter(result -> {
                BizPaper paper = paperService.getById(result.getPaperId());
                return paper != null && subjectId.equals(paper.getSubjectId());
            }).collect(Collectors.toList());
        }

        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("paperName", result.getPaperName());
            map.put("score", result.getScore());
            map.put("totalScore", result.getTotalScore());
            map.put("createTime", result.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ScoreExportDTO> getExportData(Map<String, Object> params) {
        QueryWrapper<BizExamResult> query = buildQueryWrapper(params);
        List<BizExamResult> results = list(query);

        return results.stream().map(result -> {
            ScoreExportDTO dto = new ScoreExportDTO();
            dto.setPaperName(result.getPaperName());
            dto.setScore(result.getScore());
            dto.setTotalScore(result.getTotalScore());
            dto.setViolationCount(result.getViolationCount());
            dto.setCreateTime(java.sql.Timestamp.valueOf(result.getCreateTime()));
            dto.setComment(result.getComment());
            dto.setStatus(Boolean.TRUE.equals(result.getPublished()) ? "已发布" : "未发布");

            // 补充学生信息
            BizStudent student = studentService.getById(result.getStudentId());
            if (student != null) {
                dto.setStudentName(student.getName());
                dto.setStudentNo(student.getStudentNo());
                dto.setClassName(student.getClassName());
            }

            // 补充科目信息
            BizPaper paper = paperService.getById(result.getPaperId());
            if (paper != null && paper.getSubjectId() != null) {
                BizSubject subject = subjectService.getById(paper.getSubjectId());
                if (subject != null) {
                    dto.setSubjectName(subject.getName());
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public boolean batchPublish(List<Long> ids, Boolean published) {
        List<BizExamResult> updateList = ids.stream().map(id -> {
            BizExamResult result = new BizExamResult();
            result.setId(id);
            result.setPublished(published);
            return result;
        }).collect(Collectors.toList());

        boolean success = updateBatchById(updateList);

        // 【知识点功能增强】如果是发布成绩（published=true），则更新知识点掌握度
        if (success && Boolean.TRUE.equals(published)) {
            for (Long examResultId : ids) {
                try {
                    BizExamResult examResult = this.getById(examResultId);
                    if (examResult != null) {
                        studentKnowledgeMasteryService.updateMasteryAfterExam(
                                examResult.getStudentId(),
                                examResultId
                        );
                    }
                } catch (Exception e) {
                    // 记录错误但不影响成绩发布
                    // logger.error("更新知识点掌握度失败: examResultId=" + examResultId, e);
                }
            }
        }

        return success;
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<BizExamResult> buildQueryWrapper(Map<String, Object> params) {
        QueryWrapper<BizExamResult> query = new QueryWrapper<>();

        // 试卷名称模糊查询
        if (params.containsKey("paperName") && StringUtils.hasText((String) params.get("paperName"))) {
            query.like("paper_name", params.get("paperName"));
        }

        // 学生姓名查询（需要先查询学生ID）
        if (params.containsKey("studentName") && StringUtils.hasText((String) params.get("studentName"))) {
            QueryWrapper<BizStudent> studentQuery = new QueryWrapper<>();
            studentQuery.like("name", params.get("studentName"));
            List<Long> studentIds = studentService.list(studentQuery).stream()
                    .map(BizStudent::getId)
                    .collect(Collectors.toList());
            if (!studentIds.isEmpty()) {
                query.in("student_id", studentIds);
            } else {
                query.eq("student_id", -1); // 没有匹配的学生，返回空结果
            }
        }

        // 班级筛选
        if (params.containsKey("classId") && params.get("classId") != null) {
            Long classId = Long.valueOf(params.get("classId").toString());
            List<Long> studentIds = studentService.list(new QueryWrapper<BizStudent>().eq("class_id", classId))
                    .stream().map(BizStudent::getId).collect(Collectors.toList());
            if (!studentIds.isEmpty()) {
                query.in("student_id", studentIds);
            } else {
                query.eq("student_id", -1);
            }
        }

        // 科目筛选
        if (params.containsKey("subjectId") && params.get("subjectId") != null) {
            Long subjectId = Long.valueOf(params.get("subjectId").toString());
            List<Long> paperIds = paperService.list(new QueryWrapper<BizPaper>().eq("subject_id", subjectId))
                    .stream().map(BizPaper::getId).collect(Collectors.toList());
            if (!paperIds.isEmpty()) {
                query.in("paper_id", paperIds);
            } else {
                query.eq("paper_id", -1);
            }
        }

        // 试卷ID筛选
        if (params.containsKey("paperId") && params.get("paperId") != null) {
            query.eq("paper_id", params.get("paperId"));
        }

        // 分数范围筛选
        if (params.containsKey("minScore") && params.get("minScore") != null) {
            query.ge("score", params.get("minScore"));
        }
        if (params.containsKey("maxScore") && params.get("maxScore") != null) {
            query.le("score", params.get("maxScore"));
        }

        // 时间范围筛选
        if (params.containsKey("startTime") && StringUtils.hasText((String) params.get("startTime"))) {
            query.ge("create_time", params.get("startTime"));
        }
        if (params.containsKey("endTime") && StringUtils.hasText((String) params.get("endTime"))) {
            query.le("create_time", params.get("endTime"));
        }

        // 发布状态筛选
        if (params.containsKey("published") && params.get("published") != null) {
            query.eq("published", params.get("published"));
        }

        // 排序
        String sortBy = (String) params.getOrDefault("sortBy", "createTime");
        String sortOrder = (String) params.getOrDefault("sortOrder", "desc");

        switch (sortBy) {
            case "score":
                if ("asc".equals(sortOrder)) {
                    query.orderByAsc("score");
                } else {
                    query.orderByDesc("score");
                }
                break;
            case "violationCount":
                if ("asc".equals(sortOrder)) {
                    query.orderByAsc("violation_count");
                } else {
                    query.orderByDesc("violation_count");
                }
                break;
            default:
                if ("asc".equals(sortOrder)) {
                    query.orderByAsc("create_time");
                } else {
                    query.orderByDesc("create_time");
                }
                break;
        }

        return query;
    }

    @Override
    public List<KnowledgePointScoreAnalysisDTO> getKnowledgePointAnalysis(Long examResultId) {
        // 1. 获取考试成绩记录
        BizExamResult examResult = this.getById(examResultId);
        if (examResult == null) {
            return new ArrayList<>();
        }

        Long paperId = examResult.getPaperId();
        Long studentId = examResult.getStudentId();

        // 2. 获取试卷的所有题目
        List<BizPaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new QueryWrapper<BizPaperQuestion>().eq("paper_id", paperId)
        );

        if (paperQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 获取学生的错题记录
        Set<Long> wrongQuestionIds = wrongRecordMapper.selectList(
                new QueryWrapper<BizWrongRecord>()
                        .eq("student_id", studentId)
                        .eq("exam_result_id", examResultId)
        ).stream().map(BizWrongRecord::getQuestionId).collect(Collectors.toSet());

        // 4. 按知识点统计得分
        Map<Long, KnowledgePointScoreAnalysisDTO> kpStatsMap = new HashMap<>();

        for (BizPaperQuestion pq : paperQuestions) {
            Long questionId = pq.getQuestionId();
            Integer questionScore = pq.getScore();

            // 获取该题目关联的所有知识点
            List<BizQuestionKnowledgePoint> qkpList = questionKnowledgePointMapper.selectList(
                    new QueryWrapper<BizQuestionKnowledgePoint>().eq("question_id", questionId)
            );

            for (BizQuestionKnowledgePoint qkp : qkpList) {
                Long kpId = qkp.getKnowledgePointId();

                // 如果该知识点还没有统计记录，创建一个
                KnowledgePointScoreAnalysisDTO dto = kpStatsMap.computeIfAbsent(kpId, k -> {
                    KnowledgePointScoreAnalysisDTO newDto = new KnowledgePointScoreAnalysisDTO();
                    newDto.setKnowledgePointId(kpId);
                    newDto.setScore(java.math.BigDecimal.ZERO);
                    newDto.setMaxScore(0);
                    newDto.setQuestionCount(0);
                    return newDto;
                });

                // 累加满分和题目数
                dto.setMaxScore(dto.getMaxScore() + questionScore);
                dto.setQuestionCount(dto.getQuestionCount() + 1);

                // 如果这道题没有答错，累加得分
                if (!wrongQuestionIds.contains(questionId)) {
                    dto.setScore(dto.getScore().add(java.math.BigDecimal.valueOf(questionScore)));
                }
            }
        }

        // 5. 批量获取知识点名称
        if (!kpStatsMap.isEmpty()) {
            List<Long> kpIds = new ArrayList<>(kpStatsMap.keySet());
            Map<Long, String> kpNameMap = knowledgePointService.listByIds(kpIds).stream()
                    .collect(Collectors.toMap(BizKnowledgePoint::getId, BizKnowledgePoint::getName));

            // 补充知识点名称并计算得分率
            kpStatsMap.forEach((kpId, dto) -> {
                dto.setKnowledgePointName(kpNameMap.get(kpId));
                if (dto.getMaxScore() > 0) {
                    double rate = dto.getScore().doubleValue() * 100.0 / dto.getMaxScore();
                    dto.setScoreRate(java.math.BigDecimal.valueOf(rate).setScale(2, java.math.RoundingMode.HALF_UP));
                } else {
                    dto.setScoreRate(java.math.BigDecimal.ZERO);
                }
            });
        }

        // 6. 转换为List并返回
        return new ArrayList<>(kpStatsMap.values());
    }
}