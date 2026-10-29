package com.thaitheatre.api.controller.publice;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thaitheatre.api.service.OtherSuggestionService;

import lombok.RequiredArgsConstructor;

/**
 * Read-only suggestions for the "Other / อื่น ๆ" free-text fields: the distinct
 * values other members have already entered for a given field, so the form can
 * offer them for selection. Public GET (same tier as the master-data lists).
 */
@RestController
@RequestMapping("/api/public/other-suggestions")
@RequiredArgsConstructor
public class OtherSuggestionPublicController {
    private final OtherSuggestionService svc;

    @GetMapping
    public List<String> list(@RequestParam String field) {
        return svc.suggestions(field);
    }
}
