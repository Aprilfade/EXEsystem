package com.ice.exebackend.service;

import com.ice.exebackend.dto.AiAnalysisReq;
import com.ice.exebackend.dto.AiGeneratedQuestionDTO;
import com.ice.exebackend.dto.AiGradingResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基础分析器（AI降级方案）
 * 当AI服务不可用时，提供基础的分析功能
 */
@Service
public class BasicAnalyzer {

    /**
     * 基础错题分析（不使用AI）
     */
    public String analyzeWrongQuestion(AiAnalysisReq req) {
        StringBuilder analysis = new StringBuilder();

        analysis.append("## 📝 错题分析\n\n");
        analysis.append("**题目：**\n").append(req.getQuestionContent()).append("\n\n");

        analysis.append("**您的答案：**\n").append(req.getStudentAnswer()).append("\n\n");

        analysis.append("**正确答案：**\n").append(req.getCorrectAnswer()).append("\n\n");

        // 比较答案差异
        if (!req.getStudentAnswer().equals(req.getCorrectAnswer())) {
            analysis.append("### ❌ 答案分析\n\n");
            analysis.append("您的答案与正确答案不同，请仔细对比两者的差异。\n\n");
        }

        // 如果有原解析，显示出来
        if (req.getAnalysis() != null && !req.getAnalysis().isEmpty()) {
            analysis.append("### 💡 题目解析\n\n");
            analysis.append(req.getAnalysis()).append("\n\n");
        }

        // 给出通用建议
        analysis.append("### 📚 学习建议\n\n");
        analysis.append("1. **理解知识点**：回顾相关章节，确保掌握核心概念\n");
        analysis.append("2. **分析错误原因**：思考为什么会选择这个答案\n");
        analysis.append("3. **对比正确答案**：理解正确答案的思路和方法\n");
        analysis.append("4. **举一反三**：尝试做类似的题目加深理解\n\n");

        analysis.append("---\n");
        analysis.append("*注：当前为基础分析模式。如需AI智能分析，请稍后重试。*");

        return analysis.toString();
    }

    /**
     * 基础主观题批改（不使用AI）
     */
    public AiGradingResult gradeSubjectiveQuestion(
            String questionContent, String referenceAnswer,
            String studentAnswer, int maxScore) {

        AiGradingResult result = new AiGradingResult();

        // 简单的评分逻辑：基于文本相似度
        int score = calculateSimpleScore(referenceAnswer, studentAnswer, maxScore);
        result.setScore(score);

        // 生成反馈
        StringBuilder feedback = new StringBuilder();

        if (score >= maxScore * 0.9) {
            feedback.append("回答很好！");
        } else if (score >= maxScore * 0.7) {
            feedback.append("回答较好，但还有提升空间。");
        } else if (score >= maxScore * 0.5) {
            feedback.append("回答基本正确，但需要更完整。");
        } else {
            feedback.append("回答不够准确，请参考标准答案。");
        }

        feedback.append("建议对照参考答案，补充遗漏的要点。");
        result.setFeedback(feedback.toString());

        return result;
    }

    /**
     * 计算简单评分（基于关键词匹配）
     */
    private int calculateSimpleScore(String reference, String student, int maxScore) {
        if (student == null || student.trim().isEmpty()) {
            return 0;
        }

        if (reference == null || reference.trim().isEmpty()) {
            return maxScore / 2; // 无参考答案时给一半分
        }

        // 将参考答案分词（简单按空格和标点分割）
        String[] referenceWords = reference.split("[\\s\\p{Punct}]+");
        String[] studentWords = student.split("[\\s\\p{Punct}]+");

        // 计算关键词覆盖率
        int matchCount = 0;
        for (String refWord : referenceWords) {
            if (refWord.length() > 1) { // 忽略单字
                for (String stuWord : studentWords) {
                    if (stuWord.equals(refWord)) {
                        matchCount++;
                        break;
                    }
                }
            }
        }

        // 计算覆盖率
        double coverage = referenceWords.length > 0 ?
            (double) matchCount / referenceWords.length : 0.5;

        // 长度因子（避免答案过短）
        double lengthFactor = Math.min(1.0, (double) student.length() / reference.length());

        // 综合评分
        double finalScore = maxScore * ((coverage * 0.7) + (lengthFactor * 0.3));

        return Math.max(0, Math.min(maxScore, (int) Math.round(finalScore)));
    }

    /**
     * 基础智能出题（不使用AI）
     */
    public List<AiGeneratedQuestionDTO> generateQuestions(
            String text, int count, int type) {

        List<AiGeneratedQuestionDTO> questions = new ArrayList<>();

        // 生成提示信息（告知用户AI不可用）
        AiGeneratedQuestionDTO notice = new AiGeneratedQuestionDTO();
        notice.setContent("AI智能出题服务暂时不可用，请稍后重试。\n\n建议：\n1. 手动从文本中提取关键知识点\n2. 参考教材例题进行出题\n3. 使用题库中的相似题目");
        notice.setQuestionType(type);
        notice.setAnswer("无");
        notice.setDescription("当前为降级模式，无法自动生成题目");

        questions.add(notice);

        return questions;
    }

    /**
     * 基础知识点提取（不使用AI）
     */
    public List<Map<String, String>> extractKnowledgePoints(String text, int count) {
        List<Map<String, String>> points = new ArrayList<>();

        // 简单的知识点提取：按句子分割
        String[] sentences = text.split("[。！？\\n]+");

        int extracted = 0;
        for (String sentence : sentences) {
            if (extracted >= count) break;

            sentence = sentence.trim();
            if (sentence.length() > 10 && sentence.length() < 200) {
                // 提取句子作为知识点
                String name = sentence.length() > 30 ?
                    sentence.substring(0, 30) + "..." : sentence;

                points.add(Map.of(
                    "name", "知识点 " + (extracted + 1),
                    "description", sentence
                ));

                extracted++;
            }
        }

        // 如果没有提取到足够的知识点，添加提示
        if (points.isEmpty()) {
            points.add(Map.of(
                "name", "提示",
                "description", "AI知识点提取服务暂时不可用。建议手动整理文本中的核心概念和要点。"
            ));
        }

        return points;
    }

    /**
     * 检查服务是否可用（基础分析器始终可用）
     */
    public boolean isAvailable() {
        return true;
    }
}
