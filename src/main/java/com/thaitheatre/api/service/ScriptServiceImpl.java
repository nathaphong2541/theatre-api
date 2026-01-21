package com.thaitheatre.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.thaitheatre.api.model.dto.script.ScriptCreateRequest;
import com.thaitheatre.api.model.dto.script.ScriptImageResponse;
import com.thaitheatre.api.model.dto.script.ScriptResponse;
import com.thaitheatre.api.model.dto.script.ScriptUpdateRequest;
import com.thaitheatre.api.model.entity.Profile;
import com.thaitheatre.api.model.entity.Script;
import com.thaitheatre.api.model.entity.ScriptImage;
import com.thaitheatre.api.model.entity.ScriptPdf;
import com.thaitheatre.api.repository.ScriptImageRepository;
import com.thaitheatre.api.repository.ScriptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScriptServiceImpl implements ScriptService {

    private final ScriptRepository scriptRepository;
    private final ScriptImageRepository scriptImageRepository;

    @Value("${upload.script.dir:uploads/scripts}")
    private String uploadDir;

    // ✅ โฟลเดอร์เก็บ pdf แยกออกมา
    @Value("${upload.script.pdf.dir:uploads/scripts/pdf}")
    private String uploadPdfDir;

    @Override
    public ScriptResponse createScript(ScriptCreateRequest request,
            List<MultipartFile> images,
            MultipartFile pdfFile,
            Long currentUserId) {

        // สร้าง entity เปล่า ๆ ก่อนเพื่อให้ได้ id (ใช้ในชื่อไฟล์)
        Script script = new Script();
        script.setTitle(request.getTitle());
        script.setDescription(request.getDescription());
        script.setTags(request.getTags());
        script.setCreatedBy(currentUserId);
        script.setUpdatedBy(currentUserId);

        script = scriptRepository.save(script);

        // ✅ บังคับว่าต้องมี pdf เสมอ
        if (pdfFile == null || pdfFile.isEmpty()) {
            throw new IllegalArgumentException("ต้องอัปโหลดไฟล์ PDF อย่างน้อย 1 ไฟล์");
        }

        // --- main PDF + version แรก ---
        String pdfPath = savePdfFile(script.getId(), pdfFile);
        script.setPdfPath(pdfPath); // ไฟล์หลักล่าสุด

        // เก็บเป็น version 1 ในตาราง script_pdfs ด้วย
        ScriptPdf firstVersion = ScriptPdf.builder()
                .script(script)
                .filePath(pdfPath)
                .versionNo(1)
                .versionName("เวอร์ชันแรก")
                .createdBy(currentUserId)
                .build();
        script.getPdfs().add(firstVersion);

        // --- images ---
        List<ScriptImage> imageEntities = saveImages(script, images);
        // ❗ใช้ getImages().addAll แทน setImages เพื่อไม่เปลี่ยน reference
        script.getImages().addAll(imageEntities);

        return mapToResponse(scriptRepository.save(script));
    }

    @Override
    public ScriptResponse addPdfVersion(Long scriptId,
            MultipartFile pdfFile,
            String versionName,
            Long currentUserId) {

        if (pdfFile == null || pdfFile.isEmpty()) {
            throw new IllegalArgumentException("ต้องอัปโหลดไฟล์ PDF");
        }

        Script script = scriptRepository.findById(scriptId)
                .orElseThrow(() -> new RuntimeException("Script not found"));

        // ใช้ helper เดิมสร้าง version ใหม่
        ScriptPdf pdf = savePdfVersionInternal(script, pdfFile, versionName, currentUserId);

        // อัปเดต pdf หลักให้เป็นไฟล์เวอร์ชันล่าสุด
        script.setPdfPath(pdf.getFilePath());

        return mapToResponse(scriptRepository.save(script));
    }

    @Override
    public ScriptResponse updateScript(Long id,
            ScriptUpdateRequest request,
            List<MultipartFile> images,
            MultipartFile pdfFile,
            Long currentUserId) {

        Script script = scriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Script not found"));

        script.setTitle(request.getTitle());
        script.setDescription(request.getDescription());
        script.setTags(request.getTags());
        script.setUpdatedBy(currentUserId);

        // ✅ ถ้ามี PDF ใหม่ → ลบไฟล์เก่า + เซฟใหม่ + เพิ่ม version ใหม่
        if (pdfFile != null && !pdfFile.isEmpty()) {
            // ลบไฟล์ pdf หลักเก่า (ถ้ามี)
            if (script.getPdfPath() != null) {
                try {
                    Files.deleteIfExists(Path.of(script.getPdfPath()));
                } catch (IOException ignored) {
                }
            }

            ScriptPdf newVersion = savePdfVersionInternal(
                    script,
                    pdfFile,
                    "อัปเดตเวอร์ชัน", // หรือส่งจาก request ก็ได้
                    currentUserId);

            script.setPdfPath(newVersion.getFilePath());
        }

        // --- จัดการรูป ---
        if (images != null) {
            // ❗ใช้ clear() แทน deleteAll + setImages()
            script.getImages().clear(); // orphanRemoval จะลบรูปเก่าใน DB ให้เอง

            List<ScriptImage> newImages = saveImages(script, images);
            script.getImages().addAll(newImages);
        }

        return mapToResponse(scriptRepository.save(script));
    }

    // ───────────── helper สำหรับ PDF ─────────────
    /**
     * สร้าง ScriptPdf version ใหม่ + เซฟไฟล์ และคืน ScriptPdf ที่สร้าง
     * (เผื่อใช้ set pdfPath)
     */
    private ScriptPdf savePdfVersionInternal(Script script,
            MultipartFile pdfFile,
            String versionName,
            Long currentUserId) {

        try {
            String contentType = pdfFile.getContentType();
            if (contentType != null && !contentType.equalsIgnoreCase("application/pdf")) {
                throw new IllegalArgumentException("รองรับเฉพาะไฟล์ PDF เท่านั้น");
            }

            Path uploadPath = Path.of(uploadPdfDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = pdfFile.getOriginalFilename();
            String cleanName = originalName != null ? originalName.replaceAll("\\s+", "_") : "script.pdf";

            // หา versionNo ล่าสุด +1
            int nextVersion = script.getPdfs().stream()
                    .map(ScriptPdf::getVersionNo)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;

            String filename = "script_" + script.getId() + "_v" + nextVersion + "_"
                    + System.currentTimeMillis() + "_" + cleanName;

            Path target = uploadPath.resolve(filename);
            Files.copy(pdfFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            ScriptPdf pdf = ScriptPdf.builder()
                    .script(script)
                    .filePath(target.toString())
                    .versionNo(nextVersion)
                    .versionName(versionName)
                    .createdBy(currentUserId)
                    .build();

            script.getPdfs().add(pdf);
            return pdf;
        } catch (IOException e) {
            throw new RuntimeException("ไม่สามารถบันทึกไฟล์ PDF ได้", e);
        }
    }

    @Override
    public ScriptResponse getScript(Long id) {
        Script script = scriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Script not found"));
        return mapToResponse(script);
    }

    @Override
    public List<ScriptResponse> getAllScripts() {
        return scriptRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteScript(Long id) {
        Script script = scriptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Script not found"));

        // ลบไฟล์จริงด้วย (optional)
        if (script.getImages() != null) {
            script.getImages().forEach(img -> {
                try {
                    Files.deleteIfExists(Path.of(img.getFilePath()));
                } catch (IOException ignored) {
                }
            });
        }
        if (script.getPdfs() != null) {
            script.getPdfs().forEach(pdf -> {
                try {
                    Files.deleteIfExists(Path.of(pdf.getFilePath()));
                } catch (IOException ignored) {
                }
            });
        }

        scriptRepository.delete(script);
    }

    // ───────────────── helper ─────────────────
    private List<ScriptImage> saveImages(Script script, List<MultipartFile> images) {
        List<ScriptImage> result = new ArrayList<>();
        if (images == null || images.isEmpty()) {
            return result;
        }

        if (images.size() > 5) {
            throw new IllegalArgumentException("อัปโหลดรูปได้ไม่เกิน 5 รูป");
        }

        try {
            Path uploadPath = Path.of(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            int sortOrder = 1;
            for (MultipartFile file : images) {
                if (file.isEmpty()) {
                    continue;
                }

                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path target = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                ScriptImage image = ScriptImage.builder()
                        .script(script)
                        .filePath(target.toString())
                        .sortOrder(sortOrder++)
                        .build();

                result.add(image);
            }
        } catch (IOException e) {
            throw new RuntimeException("ไม่สามารถบันทึกรูปภาพได้", e);
        }

        return result;
    }

    private String savePdfFile(Long scriptId, MultipartFile pdfFile) {
        try {
            if (pdfFile == null || pdfFile.isEmpty()) {
                return null;
            }

            String contentType = pdfFile.getContentType();
            if (contentType != null && !contentType.equalsIgnoreCase("application/pdf")) {
                throw new IllegalArgumentException("รองรับเฉพาะไฟล์ PDF เท่านั้น");
            }

            Path uploadPath = Path.of(uploadPdfDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = pdfFile.getOriginalFilename();
            String cleanName = originalName != null ? originalName.replaceAll("\\s+", "_") : "script.pdf";

            String filename = "script_" + scriptId + "_" + System.currentTimeMillis() + "_" + cleanName;
            Path target = uploadPath.resolve(filename);

            Files.copy(pdfFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("ไม่สามารถบันทึกไฟล์ PDF ได้", e);
        }
    }

    private ScriptResponse mapToResponse(Script script) {
        List<ScriptImageResponse> imageResponses = script.getImages() == null
                ? List.of()
                : script.getImages().stream()
                        .map(img -> ScriptImageResponse.builder()
                                .id(img.getId())
                                .filePath(img.getFilePath())
                                .sortOrder(img.getSortOrder())
                                .build())
                        .toList();

        // 👇 ดึง Profile ผู้สร้างจาก Script (ถ้าคุณผูก relation ไว้แล้ว)
        Profile profile = script.getCreatedProfile(); // type ต้องเป็น entity.Profile

        String createdByName = null;
        if (profile != null) {
            createdByName = ((profile.getFirstName() != null ? profile.getFirstName() : "") + " "
                    + (profile.getLastName() != null ? profile.getLastName() : "")).trim();
        }

        return ScriptResponse.builder()
                .id(script.getId())
                .title(script.getTitle())
                .description(script.getDescription())
                .tags(script.getTags())
                .images(imageResponses)
                .pdfPath(script.getPdfPath())
                .createdAt(script.getCreatedAt())
                .createdBy(script.getCreatedBy())
                .createdByName(createdByName) // 👈 อันนี้ไว้โชว์ชื่อบน frontend
                .updatedAt(script.getUpdatedAt())
                .updatedBy(script.getUpdatedBy())
                .build();
    }

    @Override
    public List<ScriptResponse> getMyScripts(Long userId) {
        return scriptRepository.findAllByCreatedBy(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ScriptResponse getMyScript(Long scriptId, Long userId) {
        Script script = scriptRepository.findByIdAndCreatedBy(scriptId, userId)
                .orElseThrow(() -> new RuntimeException("Script not found or you have no permission"));
        return mapToResponse(script);
    }
}
