package com.ice.exebackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.ice.exebackend.dto.PaperDTO;
import com.ice.exebackend.entity.BizPaperImage;
import com.ice.exebackend.entity.BizPaperQuestion;
import com.ice.exebackend.entity.BizQuestion;
import com.ice.exebackend.service.BizPaperService;
import com.ice.exebackend.service.BizQuestionService;
import com.ice.exebackend.service.PdfService;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;


import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfServiceImpl implements PdfService {

    @Autowired
    private BizPaperService paperService;

    @Autowired
    private BizQuestionService questionService;

    // 【新增】注入文件上传路径，用于读取本地图片
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public ByteArrayOutputStream generatePaperPdf(Long paperId, boolean includeAnswers, String watermarkText) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // 1. 初始化 PDF 文档
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // 2. 加载中文字体
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
            document.setFont(font);

            // 3. 注册水印
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new WatermarkHandler(watermarkText, font));

            // 4. 获取试卷数据
            PaperDTO paper = paperService.getPaperWithQuestionsById(paperId);
            if (paper == null) throw new RuntimeException("试卷不存在");

            // 5. 写入公共标题信息
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

            // ==========================================
            // 【核心修复】根据 paperType 分别处理
            // ==========================================
            if (paper.getPaperType() != null && paper.getPaperType() == 2) {
                // --- 处理图片试卷 ---
                if (CollectionUtils.isEmpty(paper.getPaperImages())) {
                    document.add(new Paragraph("该试卷暂无图片内容。").setTextAlignment(TextAlignment.CENTER));
                } else {
                    for (BizPaperImage imgObj : paper.getPaperImages()) {
                        try {
                            // 1. 从 URL 提取文件名
                            String fileName = imgObj.getImageUrl().substring(imgObj.getImageUrl().lastIndexOf("/") + 1);
                            // 2. 构建本地绝对路径
                            Path imagePath = Paths.get(uploadDir, fileName);

                            if (Files.exists(imagePath)) {
                                // 3. 创建 ImageData
                                ImageData imageData = ImageDataFactory.create(imagePath.toString());
                                Image image = new Image(imageData);

                                // 4. 设置图片自动缩放以适应页面宽度 (保留边距)
                                image.setAutoScale(true);

                                document.add(image);
                                // 5. 每张图片后换行，或者换页
                                document.add(new Paragraph("\n"));
                            } else {
                                document.add(new Paragraph("[图片文件丢失: " + fileName + "]").setFontColor(ColorConstants.RED));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            document.add(new Paragraph("[图片加载失败]").setFontColor(ColorConstants.RED));
                        }
                    }
                }
            } else {
                // --- 处理传统题库试卷 (原有逻辑) ---

                // 预加载题目信息
                Map<Long, BizQuestion> questionMap = loadQuestionMap(paper);

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

                            // 选项
                            if ((q.getQuestionType() == 1 || q.getQuestionType() == 2) && q.getOptions() != null) {
                                try {
                                    List<Map> options = JSON.parseArray(q.getOptions(), Map.class);
                                    for (Map opt : options) {
                                        document.add(new Paragraph("    " + opt.get("key") + ". " + opt.get("value")));
                                    }
                                } catch (Exception e) { /* ignore option parse error */ }
                            }
                            document.add(new Paragraph("\n"));
                        }
                    }
                }

                // 写入答案
                if (includeAnswers) {
                    document.add(new AreaBreak());
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
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("PDF 生成失败: " + e.getMessage());
        }

        return out;
    }
    @Override
    public ByteArrayOutputStream generateAnswerSheetPdf(Long paperId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // 1. 初始化 PDF 文档
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // 2. 加载中文字体
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
            document.setFont(font);

            // 3. 获取试卷数据
            PaperDTO paper = paperService.getPaperWithQuestionsById(paperId);
            if (paper == null) throw new RuntimeException("试卷不存在");

            // --- 页面头部 ---
            document.add(new Paragraph(paper.getName() + " — 答题卡")
                    .setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            // 学生信息栏 (使用无边框表格布局)
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.addCell(createNoBorderCell("班级：_______________", font));
            headerTable.addCell(createNoBorderCell("姓名：_______________", font));
            headerTable.addCell(createNoBorderCell("学号：_______________", font));
            headerTable.addCell(createNoBorderCell("成绩：_______________", font));
            document.add(headerTable);

            // 分割线
            document.add(new Paragraph("\n----------------------------------------------------------------------------------------------------------------------------------\n"));
            document.add(new Paragraph("注意事项：\n1. 请在指定区域内作答，超出黑色矩形边框限定区域的答案无效。\n2. 选择题请使用2B铅笔填涂，非选择题请使用黑色签字笔书写。")
                    .setFontSize(9).setFontColor(ColorConstants.DARK_GRAY).setMarginBottom(10));


            // --- 区分试卷类型 ---
            if (paper.getPaperType() != null && paper.getPaperType() == 2) {
                // 图片试卷：生成通用答题卡 (20个填空位)
                document.add(new Paragraph("一、通用答题区").setFontSize(12).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
                Table genTable = new Table(UnitValue.createPercentArray(new float[]{1, 4, 1, 4})); // 两列展示
                genTable.setWidth(UnitValue.createPercentValue(100));

                for (int i = 1; i <= 20; i++) {
                    genTable.addCell(new Cell().add(new Paragraph(i + ".")).setBorder(Border.NO_BORDER));
                    genTable.addCell(new Cell().setBorderBottom(new SolidBorder(1)).setHeight(20)); // 下划线
                }
                document.add(genTable);

            } else {
                // 题库试卷：根据题目结构生成
                // 预加载题目信息
                Map<Long, BizQuestion> questionMap = loadQuestionMap(paper);

                int qIndex = 1;
                if (!CollectionUtils.isEmpty(paper.getGroups())) {
                    for (PaperDTO.PaperGroupDTO group : paper.getGroups()) {
                        // 检查该分组下的第一题类型，判断是客观题还是主观题
                        if (group.getQuestions().isEmpty()) continue;

                        BizQuestion firstQ = questionMap.get(group.getQuestions().get(0).getQuestionId());
                        if (firstQ == null) continue;

                        // 标题栏
                        document.add(new Paragraph(group.getName())
                                .setFontSize(11).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY).setMarginTop(5).setPaddingLeft(5));

                        // 判断题型布局
                        int type = firstQ.getQuestionType();
                        if (type == 1 || type == 2 || type == 4) {
                            // === 客观题 (单选/多选/判断) ===
                            // 使用 5列 布局
                            Table objTable = new Table(UnitValue.createPercentArray(5)).useAllAvailableWidth();

                            for (BizPaperQuestion pq : group.getQuestions()) {
                                // 构造单元格内容： "1. [A] [B] [C] [D]"
                                String optionsStr;
                                if (type == 4) {
                                    optionsStr = "[ T ]   [ F ]";
                                } else {
                                    optionsStr = "[ A ]   [ B ]   [ C ]   [ D ]";
                                }

                                Paragraph p = new Paragraph()
                                        .add(new com.itextpdf.layout.element.Text(qIndex++ + ".  ").setBold())
                                        .add(new com.itextpdf.layout.element.Text(optionsStr).setFontSize(10));

                                objTable.addCell(new Cell().add(p).setBorder(Border.NO_BORDER).setPadding(5));
                            }
                            document.add(objTable);

                        } else {
                            // === 主观题/填空题 ===
                            for (BizPaperQuestion pq : group.getQuestions()) {
                                Paragraph qTitle = new Paragraph()
                                        .add(new com.itextpdf.layout.element.Text(String.valueOf(qIndex++)).setBold().setFontSize(12))
                                        .add(new com.itextpdf.layout.element.Text(" (" + pq.getScore() + "分)").setFontSize(10));

                                // 创建一个大的书写框
                                Table boxTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
                                Cell boxCell = new Cell().add(qTitle).setHeight(100); // 高度100
                                boxTable.addCell(boxCell);

                                document.add(boxTable.setMarginBottom(5));
                            }
                        }
                    }
                }
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("答题卡生成失败: " + e.getMessage());
        }
        return out;
    }

    // 辅助：创建无边框单元格
    private Cell createNoBorderCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font)).setBorder(Border.NO_BORDER);
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

    // 辅助：去除HTML标签
    private String stripHtml(String html) {
        return html == null ? "" : html.replaceAll("<[^>]*>", "");
    }

    // 内部类：水印处理器
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

            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            Canvas canvas = new Canvas(pdfCanvas, pageSize);
            canvas.setFontColor(ColorConstants.LIGHT_GRAY);
            canvas.setFontSize(40);
            canvas.setFont(font);
            canvas.setOpacity(0.3f);

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
    }
}