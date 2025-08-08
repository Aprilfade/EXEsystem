package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    // 从 application.yml 文件中读取文件存储路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 文件上传接口
     * @param file 前端上传的文件
     * @return 包含文件访问URL的Result对象
     */
    @PostMapping("/upload")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("上传失败，请选择文件");
        }

        try {
            // 生成唯一的文件名以避免冲突
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // 确保上传目录存在
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(uploadDir, newFileName);
            Files.copy(file.getInputStream(), filePath);

            // 返回可供前端访问的文件URL
            String fileUrl = "/api/v1/files/" + newFileName;
            return Result.suc(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    /**
     * 文件访问接口
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("无法读取文件!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("无法读取文件!", e);
        }
    }
}