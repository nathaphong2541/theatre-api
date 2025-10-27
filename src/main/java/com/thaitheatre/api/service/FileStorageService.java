package com.thaitheatre.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${app.files.profile-dir}")
    private String profileDir;

    public String saveProfileImage(MultipartFile file, String oldFilename) throws IOException {
        if (file == null || file.isEmpty()) {
            return oldFilename;
        }

        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (ext == null) {
            ext = "";
        }
        ext = ext.toLowerCase();

        if (!ALLOWED.contains(ext)) {
            throw new IllegalArgumentException("รูปต้องเป็น jpg/jpeg/png/webp เท่านั้น");
        }

        // ตั้งชื่อใหม่กันชนกัน: yyyyMMddHHmmss_UUID.ext
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String newName = ts + "_" + UUID.randomUUID() + "." + ext;

        Path dir = Paths.get(profileDir);
        Files.createDirectories(dir);

        Path target = dir.resolve(newName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // ลบไฟล์เก่า (ถ้ามี และชื่อไม่ว่าง)
        if (oldFilename != null && !oldFilename.isBlank()) {
            try {
                Files.deleteIfExists(dir.resolve(oldFilename));
            } catch (Exception ignored) {
            }
        }
        return newName;
    }
}
