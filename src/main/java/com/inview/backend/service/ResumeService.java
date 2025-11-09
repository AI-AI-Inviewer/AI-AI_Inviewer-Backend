// src/main/java/com/inview/backend/service/ResumeService.java
package com.inview.backend.service;

import com.inview.backend.entity.Resume;
import com.inview.backend.dto.ResumeListDto;
import com.inview.backend.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ResumeService {

    private static final Set<String> ALLOWED_EXT =
            Set.of("pdf","doc","docx","jpg","jpeg","png");
    private static final long MAX_BYTES = 20L * 1024 * 1024; // 20MB

    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public Long upload(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("파일이 20MB를 초과합니다.");
        }
        if (userId == null || userId.isBlank()) {
            throw new SecurityException("로그인 정보가 없습니다.");
        }

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("unnamed");
        String safeName = sanitize(originalName);
        String ext = getExt(safeName);
        if (!ALLOWED_EXT.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("허용되지 않은 파일 형식입니다. (pdf, doc, docx, jpg, jpeg, png)");
        }

        try {
            Resume r = new Resume();
            r.setUserId(userId);
            r.setFileName(safeName);
            r.setContentType(Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"));
            r.setFileSize(file.getSize());
            r.setFileData(file.getBytes());
            return resumeRepository.save(r).getId();
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ResumeListDto> listByUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new SecurityException("로그인 정보가 없습니다.");
        }
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

    private String sanitize(String name) {
        String s = name.replaceAll("[\\\\/]+", "_");
        return s.replaceAll("\\p{Cntrl}", "");
    }

    private String getExt(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1) : "";
    }
}
