package com.ice.exebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.exebackend.utils.AiHttpClient;
import com.ice.exebackend.mapper.BizQuestionMapper;
import com.ice.exebackend.entity.BizQuestion;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 自然语言搜索服务
 * 实现AI意图识别和语义搜索
 *
 * @author AI功能组
 * @version v3.03
 */
@Service
public class NaturalLanguageSearchService {

    private static final Logger log = LoggerFactory.getLogger(NaturalLanguageSearchService.class);

    @Autowired
    private AiHttpClient aiHttpClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BizQuestionMapper questionMapper;

    /**
     * 用户意图枚举
     */
    public enum Intent {
        FIND_QUESTION("搜索题目"),
        LEARN_CONCEPT("学习知识点"),
        ASK_QUESTION("提问"),
        FIND_COURSE("查找课程"),
        CHECK_PROGRESS("查看进度"),
        UNKNOWN("未知");

        private final String description;

        Intent(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 意图识别结果
     */
    public static class IntentAnalysis {
        private Intent intent;
        private List<String> keywords;
        private String concept;
        private Map<String, Object> entities;

        public IntentAnalysis() {
            this.entities = new HashMap<>();
            this.keywords = new ArrayList<>();
        }

        // Getters and Setters
        public Intent getIntent() { return intent; }
        public void setIntent(Intent intent) { this.intent = intent; }
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
        public String getConcept() { return concept; }
        public void setConcept(String concept) { this.concept = concept; }
        public Map<String, Object> getEntities() { return entities; }
        public void setEntities(Map<String, Object> entities) { this.entities = entities; }
    }

    /**
     * 搜索结果
     */
    public static class SearchResult {
        private Intent intent;
        private List<QuestionResult> questions;
        private List<CourseResult> courses;
        private String answer;

        public SearchResult() {
            this.questions = new ArrayList<>();
            this.courses = new ArrayList<>();
        }

        // Getters and Setters
        public Intent getIntent() { return intent; }
        public void setIntent(Intent intent) { this.intent = intent; }
        public List<QuestionResult> getQuestions() { return questions; }
        public void setQuestions(List<QuestionResult> questions) { this.questions = questions; }
        public List<CourseResult> getCourses() { return courses; }
        public void setCourses(List<CourseResult> courses) { this.courses = courses; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    /**
     * 题目搜索结果
     */
    public static class QuestionResult {
        private Long id;
        private String content;
        private double relevance;
        private Integer questionType;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public double getRelevance() { return relevance; }
        public void setRelevance(double relevance) { this.relevance = relevance; }
        public Integer getQuestionType() { return questionType; }
        public void setQuestionType(Integer questionType) { this.questionType = questionType; }
    }

    /**
     * 课程搜索结果
     */
    public static class CourseResult {
        private Long id;
        private String name;
        private double relevance;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getRelevance() { return relevance; }
        public void setRelevance(double relevance) { this.relevance = relevance; }
    }

    /**
     * 自然语言搜索
     */
    public SearchResult search(String apiKey, String provider, String query) throws Exception {
        log.info("执行自然语言搜索: {}", query);

        // 1. 意图识别
        IntentAnalysis analysis = analyzeIntent(apiKey, provider, query);

        log.info("识别意图: {}, 关键词: {}", analysis.getIntent(), analysis.getKeywords());

        // 2. 根据意图执行对应操作
        SearchResult result = new SearchResult();
        result.setIntent(analysis.getIntent());

        switch (analysis.getIntent()) {
            case FIND_QUESTION:
                result.setQuestions(searchQuestions(analysis.getKeywords()));
                break;

            case LEARN_CONCEPT:
                result.setCourses(searchCourses(analysis.getConcept()));
                break;

            case ASK_QUESTION:
                result.setAnswer(askAI(apiKey, provider, query));
                break;

            case FIND_COURSE:
                if (!analysis.getKeywords().isEmpty()) {
                    result.setCourses(searchCourses(analysis.getKeywords().get(0)));
                }
                break;

            case CHECK_PROGRESS:
                result.setAnswer("学习进度查看功能正在开发中，敬请期待！");
                break;

            default:
                result.setAnswer("抱歉，我没有理解您的问题，请换一种方式表达。\n\n您可以尝试：\n- 我想练习数学题\n- 什么是函数\n- 查看我的学习进度");
        }

        return result;
    }

    /**
     * AI意图识别
     */
    private IntentAnalysis analyzeIntent(String apiKey, String provider, String query) throws Exception {
        String prompt = String.format("""
                你是一个教育平台的智能助手。请分析用户的搜索意图并提取关键信息。

                用户输入：%s

                请以JSON格式返回：
                {
                  "intent": "FIND_QUESTION | LEARN_CONCEPT | ASK_QUESTION | FIND_COURSE | CHECK_PROGRESS",
                  "keywords": ["关键词1", "关键词2"],
                  "concept": "核心知识点"
                }

                意图定义：
                - FIND_QUESTION: 搜索题目（如"找一些数学题"、"一元二次方程的题"）
                - LEARN_CONCEPT: 学习知识点（如"学习函数"、"什么是导数"）
                - ASK_QUESTION: 提问求助（如"这道题怎么做"、"为什么选C"）
                - FIND_COURSE: 查找课程（如"有什么物理课程"）
                - CHECK_PROGRESS: 查看进度（如"我的学习情况"、"正确率"）

                只返回JSON，不要其他内容。
                """, query);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));

        try {
            String response = aiHttpClient.sendRequest(apiKey, provider, messages, 0.3, 30);

            // 解析JSON响应
            return parseIntentFromJson(response);

        } catch (Exception e) {
            log.error("AI意图识别失败，使用规则降级", e);
            // 使用规则兜底
            return ruleBasedIntentAnalysis(query);
        }
    }

    /**
     * 解析AI返回的JSON
     */
    private IntentAnalysis parseIntentFromJson(String response) {
        IntentAnalysis analysis = new IntentAnalysis();

        try {
            // 提取JSON部分（可能包含```json标记）
            String jsonStr = response;
            if (response.contains("```json")) {
                int start = response.indexOf("```json") + 7;
                int end = response.lastIndexOf("```");
                jsonStr = response.substring(start, end).trim();
            } else if (response.contains("```")) {
                int start = response.indexOf("```") + 3;
                int end = response.lastIndexOf("```");
                jsonStr = response.substring(start, end).trim();
            }

            JsonNode jsonNode = objectMapper.readTree(jsonStr);

            String intentStr = jsonNode.path("intent").asText();
            try {
                analysis.setIntent(Intent.valueOf(intentStr));
            } catch (IllegalArgumentException e) {
                analysis.setIntent(Intent.UNKNOWN);
            }

            List<String> keywords = new ArrayList<>();
            jsonNode.path("keywords").forEach(node -> keywords.add(node.asText()));
            analysis.setKeywords(keywords);

            analysis.setConcept(jsonNode.path("concept").asText());

        } catch (Exception e) {
            log.error("JSON解析失败: {}", e.getMessage());
            analysis.setIntent(Intent.UNKNOWN);
        }

        return analysis;
    }

    /**
     * 基于规则的意图识别（降级方案）
     */
    private IntentAnalysis ruleBasedIntentAnalysis(String query) {
        IntentAnalysis analysis = new IntentAnalysis();

        query = query.toLowerCase();

        if (query.contains("题") || query.contains("练习")) {
            analysis.setIntent(Intent.FIND_QUESTION);
            // 提取科目关键词
            List<String> keywords = new ArrayList<>();
            if (query.contains("数学")) keywords.add("数学");
            if (query.contains("语文")) keywords.add("语文");
            if (query.contains("英语")) keywords.add("英语");
            if (query.contains("物理")) keywords.add("物理");
            if (query.contains("化学")) keywords.add("化学");
            analysis.setKeywords(keywords);

        } else if (query.contains("学习") || query.contains("什么是") || query.contains("如何")) {
            analysis.setIntent(Intent.LEARN_CONCEPT);
            analysis.setConcept(query);

        } else if (query.contains("课程") || query.contains("视频")) {
            analysis.setIntent(Intent.FIND_COURSE);
            analysis.getKeywords().add(query);

        } else if (query.contains("进度") || query.contains("正确率") || query.contains("统计")) {
            analysis.setIntent(Intent.CHECK_PROGRESS);

        } else {
            analysis.setIntent(Intent.ASK_QUESTION);
        }

        return analysis;
    }

    /**
     * 搜索题目
     */
    private List<QuestionResult> searchQuestions(List<String> keywords) {
        List<QuestionResult> results = new ArrayList<>();

        try {
            LambdaQueryWrapper<BizQuestion> wrapper = new LambdaQueryWrapper<>();

            // 关键词搜索
            if (!keywords.isEmpty()) {
                wrapper.and(w -> {
                    for (String keyword : keywords) {
                        w.or().like(BizQuestion::getContent, keyword);
                    }
                });
            }

            wrapper.orderByDesc(BizQuestion::getId);
            wrapper.last("LIMIT 20");

            List<BizQuestion> questions = questionMapper.selectList(wrapper);

            for (BizQuestion question : questions) {
                QuestionResult result = new QuestionResult();
                result.setId(question.getId());
                result.setContent(question.getContent());
                result.setQuestionType(question.getQuestionType());
                result.setRelevance(0.85); // 简化的相关度
                results.add(result);
            }

            log.info("搜索到 {} 道题目", results.size());

        } catch (Exception e) {
            log.error("搜索题目失败", e);
        }

        return results;
    }

    /**
     * 搜索课程
     */
    private List<CourseResult> searchCourses(String concept) {
        List<CourseResult> results = new ArrayList<>();

        // TODO: 实现课程搜索
        log.info("搜索课程: {}", concept);

        return results;
    }

    /**
     * AI问答
     */
    private String askAI(String apiKey, String provider, String question) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                "你是一位经验丰富的中学教师，擅长用简洁易懂的语言回答学生的问题。" +
                "回答要有条理，如果涉及步骤，请分点说明。"));
        messages.add(Map.of("role", "user", "content", question));

        return aiHttpClient.sendRequest(apiKey, provider, messages, 0.7, 30);
    }
}
