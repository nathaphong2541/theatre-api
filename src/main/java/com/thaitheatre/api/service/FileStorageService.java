package com.thaitheatre.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    // allowed: JPG, PNG, GIF, WEBP, PDF / 10MB
    // allowed: PDF, JPG, PNG, WEBP (resume)
    public String saveResume(MultipartFile file, String oldFilename) throws IOException {
        validateFileSize(file, 50 * 1024 * 1024L); // หรือ 10MB ตามต้องการ
        validateExtension(file, List.of("pdf", "jpg", "jpeg", "png", "webp"));

        String orig = file.getOriginalFilename();
        if (orig == null || orig.isBlank()) {
            orig = "resume"; // กันกรณี browser ไม่ส่งชื่อไฟล์มา
        }

        String newName = generateFileName(orig);

        // เก็บไว้ในโฟลเดอร์ย่อย resume ของ profileDir
        Path dir = Paths.get(profileDir, "resume");
        Files.createDirectories(dir);

        Path target = dir.resolve(newName);
        file.transferTo(target);

        // ลบไฟล์เก่าถ้ามี
        if (oldFilename != null && !oldFilename.isBlank()) {
            try {
                Files.deleteIfExists(dir.resolve(oldFilename));
            } catch (Exception ignored) {
            }
        }

        // ถ้าฝั่ง ProfileService เก็บใน DB แค่ชื่อไฟล์ ให้ return เฉพาะชื่อ
        // แล้วค่อยไปเติม "resume/" ใน toResponse()
        return newName;
    }

    // allowed: JPG, PNG, GIF, WEBP (performance)
    public String savePerformanceImage(MultipartFile file) throws IOException {
        validateFileSize(file, 10 * 1024 * 1024L); // จะลดเหลือ 2MB ก็ได้
        validateExtension(file, List.of("jpg", "jpeg", "png", "gif", "webp"));

        String newName = generateFileName(file.getOriginalFilename());
        Path target = Paths.get(profileDir, "performance").resolve(newName);

        Files.createDirectories(target.getParent());
        file.transferTo(target);
        return newName;
    }

    private void validateFileSize(MultipartFile file, long maxSizeBytes) {
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("ไฟล์มีขนาดเกิน " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }
    }

    private void validateExtension(MultipartFile file, List<String> allowedExtensions) {
        String original = file.getOriginalFilename();
        if (original == null) {
            throw new IllegalArgumentException("ไม่พบชื่อไฟล์");
        }

        String ext = original.substring(original.lastIndexOf(".") + 1).toLowerCase();

        if (!allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException("ประเภทไฟล์ไม่ถูกต้อง: ." + ext);
        }
    }

    private String generateFileName(String originalName) {
        String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        return System.currentTimeMillis() + "_" + java.util.UUID.randomUUID() + ext;
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }

}
