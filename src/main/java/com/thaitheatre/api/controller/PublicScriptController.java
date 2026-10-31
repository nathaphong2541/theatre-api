package com.thaitheatre.api.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.model.dto.script.ScriptResponse;
import com.thaitheatre.api.service.ScriptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/scripts")
@RequiredArgsConstructor
public class PublicScriptController {

    private final ScriptService scriptService;

    /**
     * ✅ GET ทั้งหมด (Public) ตัวอย่าง: GET /api/public/scripts
     */
    @GetMapping
    public ResponseEntity<List<ScriptResponse>> getAllPublicScripts() {
        // Only admin-approved scripts appear in the public Play Library.
        List<ScriptResponse> list = scriptService.getPublicScripts();
        return ResponseEntity.ok(list);
    }

    /**
     * ✅ GET ตาม id (Public) ตัวอย่าง: GET /api/public/scripts/2
     * คืนเฉพาะบทที่อนุมัติแล้วเท่านั้น (บทที่ยังไม่อนุมัติจะได้ 404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScriptResponse> getPublicScriptById(@PathVariable Long id) {
        ScriptResponse res = scriptService.getPublicScript(id);
        return ResponseEntity.ok(res);
    }
}
