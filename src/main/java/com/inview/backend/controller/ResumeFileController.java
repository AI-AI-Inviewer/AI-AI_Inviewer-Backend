package com.inview.backend.controller;

import com.inview.backend.dto.ResumeListDto;
import com.inview.backend.dto.ResumeTextDto;
import com.inview.backend.dto.UploadResponseDto;
import com.inview.backend.entity.Resume;
import com.inview.backend.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController("resumeFileController")
@RequestMapping("/api")
public class ResumeFileController {

    private final ResumeService resumeService;

    public ResumeFileController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDto> upload(@RequestPart("resume") MultipartFile file,
                                                    HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        Long id = resumeService.upload(file, userId);
        return ResponseEntity.ok(new UploadResponseDto(id, "업로드 성공"));
    }

    @GetMapping("/resumes")
    public ResponseEntity<List<ResumeListDto>> list(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        return ResponseEntity.ok(resumeService.listByUser(userId));
    }

    @GetMapping("/resumes/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        Resume r = resumeService.getOne(id, userId);

        String encodedName = contentDispositionFilename(r.getFileName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName);
        headers.setContentLength(r.getFileSize());

        return new ResponseEntity<>(
                new InputStreamResource(new ByteArrayInputStream(r.getFileData())),
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping("/resumes/{id}/preview")
    public ResponseEntity<InputStreamResource> preview(@PathVariable Long id, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        Resume r = resumeService.getOne(id, userId);

        MediaType mt = safeMediaType(r.getContentType());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mt);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename*=UTF-8''" + contentDispositionFilename(r.getFileName()));
        headers.setContentLength(r.getFileSize());

        return new ResponseEntity<>(
                new InputStreamResource(new ByteArrayInputStream(r.getFileData())),
                headers,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        resumeService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ★ 본문 텍스트 추출
    @GetMapping("/resumes/{id}/text")
    public ResponseEntity<ResumeTextDto> text(@PathVariable Long id,
                                              @RequestParam(name = "limit", required = false) Integer limit,
                                              HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        ResumeTextDto dto = resumeService.getResumeText(id, userId, limit);
        return ResponseEntity.ok(dto);
    }

    private String contentDispositionFilename(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }

    private MediaType safeMediaType(String contentType) {
        try { return MediaType.parseMediaType(contentType); }
        catch (Exception e) { return MediaType.APPLICATION_OCTET_STREAM; }
    }

    // JWT/시큐리티 컨텍스트에서 로그인 아이디(String) 추출
    @SuppressWarnings("unchecked")
    private String getCurrentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        if (attr instanceof String s && !s.isBlank()) return s;
        if (attr != null) return String.valueOf(attr);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) return name;

            Object principal = auth.getPrincipal();
            if (principal instanceof Map<?,?> map) {
                Object id = map.get("userId");
                if (id != null) return String.valueOf(id);
            }
            if (principal instanceof Principal p) {
                if (p.getName() != null && !p.getName().isBlank()) return p.getName();
            }
        }
        throw new SecurityException("로그인이 필요합니다.");
    }
}
