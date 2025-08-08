package com.ice.exebackend.dto;

import com.ice.exebackend.entity.BizPaper;
import com.ice.exebackend.entity.BizPaperImage;
import com.ice.exebackend.entity.BizPaperQuestion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaperDTO extends BizPaper {

    // 包含的试题列表 (包含分数和排序信息)
    private List<BizPaperQuestion> questions;
    private List<BizPaperImage> paperImages; // 【新增】

    public List<BizPaperImage> getPaperImages() {
        return paperImages;
    }

    public void setPaperImages(List<BizPaperImage> paperImages) {
        this.paperImages = paperImages;
    }

    public List<BizPaperQuestion> getQuestions() {
        return questions;
    }
    public void setQuestions(List<BizPaperQuestion> questions) {
        this.questions = questions;
    }
}