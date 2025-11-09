// src/main/java/com/inview/backend/controller/ResumeParseController.java
package com.inview.backend.controller;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController("resumeParseController")
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeParseController {

    private static final int MAX = 20000;

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> parse(@RequestPart("file") MultipartFile file) throws Exception {
        String name = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "resume";
        String lower = name.toLowerCase();

        String text;
        byte[] bytes = file.getBytes();

        if (lower.endsWith(".txt") || lower.endsWith(".md")) {
            text = new String(bytes, StandardCharsets.UTF_8);
        } else if (lower.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes));
                 XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
                text = ex.getText();
            }
        } else if (lower.endsWith(".pdf")) {
            try (PDDocument pdf = PDDocument.load(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                text = stripper.getText(pdf);
            }
        } else {
            throw new IllegalArgumentException("지원하지 않는 형식입니다. (.txt, .md, .docx, .pdf)");
        }

        if (text.length() > MAX) {
            text = text.substring(0, MAX) + "\n\n(요약: 길이 제한으로 일부가 생략되었습니다)";
        }
        return Map.of("text", text);
    }
}
