package com.ice.exebackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.entity.BizPaperQuestion;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.service.BizPaperService;
import com.ice.exebackend.service.BizQuestionService;
import com.ice.exebackend.service.PdfService;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfServiceImpl implements PdfService {

    @Autowired
    private BizPaperService paperService;

    @Autowired
    private BizQuestionService questionService;

    @Override
    public ByteArrayOutputStream generatePaperPdf(Long paperId, boolean includeAnswers, String watermarkText) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // 1. 初始化 PDF 文档
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // 2. 加载中文字体 (STSong-Light 是 font-asian 包自带的)
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
            document.setFont(font);

            // 3. 注册水印事件监听器
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new WatermarkHandler(watermarkText, font));

            // 4. 获取试卷数据
            PaperDTO paper = paperService.getPaperWithQuestionsById(paperId);
            if (paper == null) throw new RuntimeException("试卷不存在");

            // 预加载题目信息
            Map<Long, BizQuestion> questionMap = loadQuestionMap(paper);

            // 5. 写入标题
            Paragraph title = new Paragraph(paper.getName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            Paragraph info = new Paragraph(String.format("年级：%s   总分：%d 分   时长：120分钟",
                    paper.getGrade() == null ? "通用" : paper.getGrade(),
                    paper.getTotalScore()))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(info);

            // 6. 遍历分组写入题目
            if (!CollectionUtils.isEmpty(paper.getGroups())) {
                int qIndex = 1;
                for (PaperDTO.PaperGroupDTO group : paper.getGroups()) {
                    // 分组标题
                    document.add(new Paragraph(group.getName()).setFontSize(14).setBold().setMarginTop(10));

                    for (BizPaperQuestion pq : group.getQuestions()) {
                        BizQuestion q = questionMap.get(pq.getQuestionId());
                        if (q == null) continue;

                        // 题干
                        String contentText = String.format("%d. (%d分) %s", qIndex++, pq.getScore(), stripHtml(q.getContent()));
                        document.add(new Paragraph(contentText).setMarginBottom(5));

                        // 选项 (如果是选择题)
                        if ((q.getQuestionType() == 1 || q.getQuestionType() == 2) && q.getOptions() != null) {
                            List<Map> options = JSON.parseArray(q.getOptions(), Map.class);
                            for (Map opt : options) {
                                document.add(new Paragraph("    " + opt.get("key") + ". " + opt.get("value")));
                            }
                        }
                        document.add(new Paragraph("\n")); // 空行
                    }
                }
            }

            // 7. 写入答案 (如果需要)
            if (includeAnswers) {
                document.add(new com.itextpdf.layout.element.AreaBreak()); // 换页
                document.add(new Paragraph("参考答案").setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));

                if (!CollectionUtils.isEmpty(paper.getGroups())) {
                    int aIndex = 1;
                    for (PaperDTO.PaperGroupDTO group : paper.getGroups()) {
                        document.add(new Paragraph(group.getName()).setBold().setMarginTop(10));
                        for (BizPaperQuestion pq : group.getQuestions()) {
                            BizQuestion q = questionMap.get(pq.getQuestionId());
                            if (q == null) continue;
                            String ansText = String.format("%d. %s", aIndex++, q.getAnswer());
                            document.add(new Paragraph(ansText));
                        }
                    }
                }
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("PDF 生成失败: " + e.getMessage());
        }

        return out;
    }

    // 辅助：加载题目Map
    private Map<Long, BizQuestion> loadQuestionMap(PaperDTO paper) {
        if (paper.getGroups() == null) return Map.of();
        List<Long> ids = paper.getGroups().stream()
                .flatMap(g -> g.getQuestions().stream())
                .map(BizPaperQuestion::getQuestionId)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return Map.of();
        return questionService.listByIds(ids).stream().collect(Collectors.toMap(BizQuestion::getId, q -> q));
    }

    // 辅助：去除HTML标签 (简单版)
    private String stripHtml(String html) {
        return html == null ? "" : html.replaceAll("<[^>]*>", "");
    }

    // --- 内部类：水印处理器 ---
    class WatermarkHandler implements IEventHandler {
        private final String watermarkText;
        private final PdfFont font;

        public WatermarkHandler(String watermarkText, PdfFont font) {
            this.watermarkText = watermarkText;
            this.font = font;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();

            // 在内容之下绘制 (newContentStreamBefore)
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            // 画布设置
            Canvas canvas = new Canvas(pdfCanvas, pageSize);
            canvas.setFontColor(ColorConstants.LIGHT_GRAY);
            canvas.setFontSize(40);
            canvas.setFont(font);
            canvas.setOpacity(0.3f); // 设置透明度

            // 绘制旋转水印
            for (int i = 1; i < 5; i++) {
                for (int j = 1; j < 5; j++) {
                    canvas.showTextAligned(
                            new Paragraph(watermarkText),
                            (pageSize.getWidth() / 4) * i,
                            (pageSize.getHeight() / 4) * j,
                            pdfDoc.getPageNumber(page),
                            TextAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            (float) Math.toRadians(45));
                }
            }
            canvas.close();
        }

        // 这里的 pdfDocument 需要通过外部上下文获取，或者在构造时传入 PdfDocument 对象，
        // 为了简化，上面的 handleEvent 参数中通常可以直接拿到 docEvent.getDocument()
        private PdfDocument pdfDocument;
        // 修正：实际上 handleEvent 里 docEvent.getDocument() 即可
    }

    // 修正后的 WatermarkHandler 逻辑（上面代码片段只是示意，这里是修正版）：
    // 为了不让代码太长，你可以把这个类单独提取出来，或者像下面这样写：
}