package com.daisy.health.controller;

import com.daisy.health.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UploadController {
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final List<String> REPORT_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "pdf");
    private static final long AVATAR_MAX_SIZE = 2L * 1024L * 1024L;
    private static final long BANNER_MAX_SIZE = 5L * 1024L * 1024L;
    private static final long REPORT_MAX_SIZE = 20L * 1024L * 1024L;
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final Path uploadRoot;

    public UploadController(@Value("${daisy.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "avatar") String category
    ) throws IOException {
        String normalizedCategory = normalizeCategory(category);
        validateFile(file, normalizedCategory);

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = extensionOf(originalName);
        String day = LocalDate.now().toString();
        Path targetDir = uploadRoot.resolve(normalizedCategory).resolve(day).normalize();
        Files.createDirectories(targetDir);
        String fileName = uniqueFileName(targetDir, normalizedCategory, originalName, extension);
        Path target = targetDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("文件路径非法");
        }
        file.transferTo(target.toFile());

        String url = "/uploads/" + normalizedCategory + "/" + day + "/" + fileName;
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("url", url);
        data.put("originalName", originalName);
        data.put("contentType", file.getContentType());
        data.put("size", file.getSize());
        return ApiResponse.success(data);
    }

    private void validateFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extensionOf(originalName);
        List<String> allowed = "report".equals(category) ? REPORT_EXTENSIONS : IMAGE_EXTENSIONS;
        if (!allowed.contains(extension)) {
            throw new IllegalArgumentException("文件格式不支持");
        }
        long maxSize = maxSize(category);
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制");
        }
    }

    private String normalizeCategory(String category) {
        String value = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
        if ("avatar".equals(value) || "report".equals(value) || "banner".equals(value)) {
            return value;
        }
        throw new IllegalArgumentException("上传分类不支持");
    }

    private long maxSize(String category) {
        if ("report".equals(category)) return REPORT_MAX_SIZE;
        if ("banner".equals(category)) return BANNER_MAX_SIZE;
        return AVATAR_MAX_SIZE;
    }

    private String extensionOf(String fileName) {
        String cleanName = StringUtils.cleanPath(fileName == null ? "" : fileName);
        int index = cleanName.lastIndexOf('.');
        return index < 0 ? "" : cleanName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String uniqueFileName(Path targetDir, String category, String originalName, String extension) {
        String baseName = baseNameOf(originalName);
        String timestamp = LocalDateTime.now().format(FILE_TIME_FORMATTER);
        String prefix = category + "_" + timestamp + "_" + baseName;
        String fileName = prefix + "." + extension;
        int index = 1;
        while (Files.exists(targetDir.resolve(fileName))) {
            fileName = prefix + "_" + index++ + "." + extension;
        }
        return fileName;
    }

    private String baseNameOf(String fileName) {
        String cleanName = StringUtils.cleanPath(fileName == null ? "" : fileName);
        int index = cleanName.lastIndexOf('.');
        String baseName = index < 0 ? cleanName : cleanName.substring(0, index);
        baseName = baseName.trim().replaceAll("[^\\p{IsHan}a-zA-Z0-9_-]+", "_");
        baseName = baseName.replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        if (baseName.length() == 0) {
            return "file";
        }
        return baseName.length() > 40 ? baseName.substring(0, 40) : baseName;
    }
}
