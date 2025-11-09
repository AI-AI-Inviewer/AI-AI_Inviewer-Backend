package com.inview.backend.service;

import com.inview.backend.dto.ResumeListDto;
import com.inview.backend.dto.ResumeTextDto;
import com.inview.backend.entity.Resume;
import com.inview.backend.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class ResumeService {

    private static final Set<String> ALLOWED_EXT =
            Set.of("pdf", "doc", "docx", "jpg", "jpeg", "png");

    private static final long MAX_BYTES = 20L * 1024 * 1024; // 20MB
    private static final int DEFAULT_MAX_TEXT_CHARS = 8000;

    private static final Map<String, List<String>> EXT_TO_CT = Map.of(
            "pdf", List.of("application/pdf"),
            "doc", List.of("application/msword", "application/x-tika-msoffice"),
            "docx", List.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            "jpg", List.of("image/jpeg"),
            "jpeg", List.of("image/jpeg"),
            "png", List.of("image/png")
    );

    private final ResumeRepository resumeRepository;
    private final Tika tika = new Tika();

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    /* -------------------- CRUD -------------------- */

    @Transactional
    public Long upload(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("파일이 20MB를 초과합니다.");
        if (userId == null || userId.isBlank()) throw new SecurityException("로그인 정보가 없습니다.");

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("unnamed");
        String safeName = sanitize(originalName);
        String ext = getExt(safeName);
        if (!ALLOWED_EXT.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("허용되지 않은 파일 형식입니다. (pdf, doc, docx, jpg, jpeg, png)");
        }

        try {
            byte[] data = file.getBytes();

            String detected = detectContentType(data, safeName);
            if (!isContentTypeAllowed(ext, detected)) {
                throw new IllegalArgumentException("파일 내용과 확장자가 일치하지 않습니다. (" + detected + ")");
            }

            Resume r = new Resume();
            r.setUserId(userId);
            r.setFileName(safeName);
            r.setContentType(Optional.ofNullable(file.getContentType()).orElse(detected != null ? detected : "application/octet-stream"));
            r.setFileSize((long) data.length);
            r.setFileData(data);

            return resumeRepository.save(r).getId();
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ResumeListDto> listByUser(String userId) {
        if (userId == null || userId.isBlank()) throw new SecurityException("로그인 정보가 없습니다.");
        return resumeRepository.findListByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Resume getOne(Long id, String userId) {
        Resume r = resumeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 파일이 존재하지 않습니다."));
        if (userId != null && r.getUserId() != null && !Objects.equals(r.getUserId(), userId)) {
            throw new SecurityException("권한이 없습니다.");
        }
        return r;
    }

    @Transactional
    public void delete(Long id, String userId) {
        Resume r = getOne(id, userId);
        resumeRepository.delete(r);
    }

    /* -------------------- 텍스트 추출 API -------------------- */

    @Transactional(readOnly = true)
    public String extractPlainText(Long id, String userId) {
        return extractPlainText(id, userId, DEFAULT_MAX_TEXT_CHARS);
    }

    @Transactional(readOnly = true)
    public String extractPlainText(Long id, String userId, int maxChars) {
        Resume r = getOne(id, userId);
        String text = safeExtractText(r.getFileData(), r.getContentType(), r.getFileName());
        text = normalizeText(text);
        if (maxChars > 0 && text.length() > maxChars) {
            text = text.substring(0, maxChars) + "\n...(생략)";
        }
        return text;
    }

    @Transactional(readOnly = true)
    public ResumeTextDto getResumeText(Long id, String userId, Integer limit) {
        Resume r = getOne(id, userId);
        int max = (limit != null && limit > 0) ? limit : DEFAULT_MAX_TEXT_CHARS;
        String text = extractPlainText(id, userId, max);

        // ★ 6개 인자 형태로 반환 (id, fileName, contentType, fileSize, createdAt, text)
        return new ResumeTextDto(
                r.getId(),
                r.getFileName(),
                r.getContentType(),
                r.getFileSize(),
                r.getCreatedAt(),
                text
        );
    }

    /* -------------------- 내부 유틸 -------------------- */

    private String sanitize(String name) {
        String s = name.replaceAll("[\\\\/]+", "_").replaceAll("\\p{Cntrl}", "");
        return (s.length() > 255) ? s.substring(0, 255) : s;
    }

    private String getExt(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1) : "";
    }

    private String detectContentType(byte[] bytes, String fileName) {
        try {
            if (fileName != null && !fileName.isBlank()) {
                return tika.detect(bytes, fileName);
            }
            return tika.detect(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isContentTypeAllowed(String ext, String detected) {
        if (detected == null || ext == null) return true;
        List<String> allow = EXT_TO_CT.getOrDefault(ext.toLowerCase(Locale.ROOT), List.of());
        if (allow.isEmpty()) return true;
        for (String ct : allow) {
            if (detected.equalsIgnoreCase(ct)) return true;
            if (detected.toLowerCase(Locale.ROOT).startsWith(ct.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private String safeExtractText(byte[] data, String contentType, String fileName) {
        if (data == null || data.length == 0) return "";
        try (InputStream is = new ByteArrayInputStream(data)) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata md = new Metadata();
            if (contentType != null && !contentType.isBlank()) {
                md.set(Metadata.CONTENT_TYPE, contentType);
            }
            if (fileName != null && !fileName.isBlank()) {
                // Tika 2.x: 파일명 힌트
                md.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
            }
            ParseContext ctx = new ParseContext();
            parser.parse(is, handler, md, ctx);
            return handler.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String normalizeText(String s) {
        if (s == null) return "";
        String t = s.replace("\u0000", "")
                .replaceAll("[ \\t\\f\\r]+\\n", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
        if (t.startsWith("\uFEFF")) t = t.substring(1);
        return t;
    }
}
