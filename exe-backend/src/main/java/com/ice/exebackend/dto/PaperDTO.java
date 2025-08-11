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

    // 【修改】将 questions 替换为 groups
    private List<PaperGroupDTO> groups;
    private List<BizPaperImage> paperImages; // 【新增】

    @Data
    public static class PaperGroupDTO {
        private Long id;
        private String name;
        private Integer sortOrder;
        private List<BizPaperQuestion> questions;


        public List<BizPaperQuestion> getQuestions() {
            return questions;
        }
        public void setQuestions(List<BizPaperQuestion> questions) {
            this.questions = questions;
        }
    }

    public List<BizPaperImage> getPaperImages() {
        return paperImages;
    }

    public void setPaperImages(List<BizPaperImage> paperImages) {
        this.paperImages = paperImages;
    }

}