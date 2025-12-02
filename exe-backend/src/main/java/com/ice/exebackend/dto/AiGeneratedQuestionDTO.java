package com.ice.exebackend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiGeneratedQuestionDTO {
    private String content;       // 题干
    private Integer questionType; // 1:单选, 2:多选, 3:填空, 4:判断
    // 选项结构: [{"key":"A", "value":"选项内容"}, {"key":"B", "value":"..."}]
    private List<Map<String, String>> options;
    private String answer;        // 参考答案
    private String description;   // 解析

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Integer getQuestionType() {
        return questionType;
    }
    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }
    public List<Map<String, String>> getOptions() {
        return options;
    }
    public void setOptions(List<Map<String, String>> options) {
        this.options = options;
    }
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}