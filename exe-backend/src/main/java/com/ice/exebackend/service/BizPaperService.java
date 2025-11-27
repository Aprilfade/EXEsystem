package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.dto.SmartPaperReq;
import com.ice.exebackend.entity.BizPaper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.List;

public interface BizPaperService extends IService<BizPaper> {

    /**
     * 创建试卷及其关联的题目
     * @param paperDTO 包含试卷信息和题目列表的DTO
     * @return 是否创建成功
     */
    boolean createPaperWithQuestions(PaperDTO paperDTO);

    /**
     * 更新试卷及其关联的题目
     * @param paperDTO 包含试卷信息和题目列表的DTO
     * @return 是否更新成功
     */
    boolean updatePaperWithQuestions(PaperDTO paperDTO);

    /**
     * 根据ID获取试卷详情，包含关联的试题
     * @param id 试卷ID
     * @return PaperDTO
     */
    PaperDTO getPaperWithQuestionsById(Long id);

    /**
     * 导出试卷为 Word 文档
     * @param id 试卷ID
     * @param includeAnswers 是否包含答案
     * @return XWPFDocument Word文档对象
     */
    XWPFDocument exportPaperToWord(Long id, boolean includeAnswers);

    /**
     * 智能生成试卷结构
     */
    List<PaperDTO.PaperGroupDTO> generateSmartPaper(SmartPaperReq req);
}