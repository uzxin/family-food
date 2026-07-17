package com.familyfood.controller;

import com.familyfood.common.BusinessException;
import com.familyfood.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    @Value("${file.upload-dir:/data/family-food/uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<String> uploadImage(
            Authentication auth,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("仅支持 JPG/PNG/GIF/WEBP 格式的图片");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("图片大小不能超过 10MB");
        }

        Long userId = (Long) auth.getPrincipal();

        // 生成文件路径：年/月/日/userId/UUID.ext
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String subDir = datePath + "/" + userId;

        // 获取扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (ext.isEmpty()) ext = ".jpg";

        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 确保目录存在
        File targetDir = new File(uploadDir + "/" + subDir);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            log.error("创建上传目录失败: {}", targetDir.getAbsolutePath());
            throw new BusinessException("文件上传失败，请稍后重试");
        }

        // 写入文件
        File targetFile = new File(targetDir, newFileName);
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            log.error("文件上传写入失败", e);
            throw new BusinessException("文件上传失败，请稍后重试");
        }

        // 返回相对路径（供 nginx 访问）
        String relativePath = "/images/" + subDir + "/" + newFileName;
        log.info("用户 {} 上传图片成功: {}", userId, relativePath);
        return Result.ok(relativePath);
    }
}
