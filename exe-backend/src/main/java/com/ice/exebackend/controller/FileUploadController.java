package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import org.apache.tika.Tika; // 需要引入 Tika
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 1. 定义允许上传的文件后缀白名单 (根据业务需求调整)
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", // 图片
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx" // 文档
    );

    // Tika 实例，用于检测文件真实类型
    private final Tika tika = new Tika();

    @PostMapping("/upload")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("上传失败，请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.fail("文件名非法");
        }

        try {
            // 2. 校验文件后缀 (Case Insensitive)
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                return Result.fail("不支持的文件类型: " + fileExtension);
            }

            // 3. 校验文件真实内容类型 (Magic Number Check)
            // 读取文件流的前几个字节来判断真实类型，防止 "1.jsp" 改名为 "1.jpg" 上传
            String detectedMimeType = tika.detect(file.getInputStream());

            // 简单的 MIME 类型校验逻辑
            if (!isValidType(detectedMimeType, fileExtension)) {
                return Result.fail("文件内容异常，请勿修改文件后缀上传");
            }

            // 4. 生成安全的文件名 (保持原有逻辑，使用 UUID)
            String newFileName = UUID.randomUUID().toString() + "." + fileExtension;

            // 确保上传目录存在
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(uploadDir, newFileName);
            Files.copy(file.getInputStream(), filePath);

            // 返回 URL
            String fileUrl = "/api/v1/files/" + newFileName;
            return Result.suc(fileUrl);

        } catch (IOException e) {
            logger.error("文件上传失败", e);
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    /**
     * 辅助方法：校验 MIME 类型是否与后缀匹配
     */
    private boolean isValidType(String mimeType, String extension) {
        // 图片校验
        if (Arrays.asList("jpg", "jpeg", "png", "gif").contains(extension)) {
            return mimeType.startsWith("image/");
        }
        // PDF 校验
        if ("pdf".equals(extension)) {
            return "application/pdf".equals(mimeType);
        }
        // Office 文档校验 (MIME 类型较多，这里做宽泛检查，或者信任 Tika 解析出的 office 类型)
        if (Arrays.asList("doc", "docx", "xls", "xlsx", "ppt", "pptx").contains(extension)) {
            return mimeType.contains("word") || mimeType.contains("excel") ||
                    mimeType.contains("spreadsheet") || mimeType.contains("presentation") ||
                    mimeType.contains("office") || mimeType.contains("ole");
        }
        return false;
    }

    /**
     * 文件下载接口 - 安全加固版
     * 防止路径遍历攻击（如 ../../etc/passwd）
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // 1. 文件名安全验证 - 仅允许UUID格式的文件名
            if (!filename.matches("^[a-f0-9\\-]{36}\\.[a-z0-9]{2,5}$")) {
                logger.warn("非法文件名访问尝试: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            // 2. 获取规范化的上传目录路径
            Path uploadPath = Paths.get(uploadDir).toRealPath();

            // 3. 解析请求的文件路径
            Path file = uploadPath.resolve(filename).normalize();

            // 4. 路径验证 - 确保文件在上传目录内（防止路径遍历）
            if (!file.startsWith(uploadPath)) {
                logger.warn("路径遍历攻击尝试: {}", filename);
                return ResponseEntity.status(403).build();
            }

            // 5. 加载文件资源
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 6. 获取Content-Type
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                // 7. 返回文件
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (IOException e) {
            logger.error("文件访问错误: {}", filename, e);
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            logger.error("文件服务异常: {}", filename, e);
            return ResponseEntity.status(500).build();
        }
    }
}