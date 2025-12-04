package com.ice.exebackend.service;

import java.io.ByteArrayOutputStream;

public interface PdfService {
    /**
     * 将试卷导出为 PDF 字节流
     * @param paperId 试卷ID
     * @param includeAnswers 是否包含答案
     * @param watermarkText 水印文字
     * @return PDF 文件字节流
     */
    ByteArrayOutputStream generatePaperPdf(Long paperId, boolean includeAnswers, String watermarkText);
    /**
     * 【新增】生成 PDF 答题卡
     * @param paperId 试卷ID
     * @return PDF 字节流
     */
    ByteArrayOutputStream generateAnswerSheetPdf(Long paperId);
}