// src/main/java/com/inview/backend/service/PostScriptService.java
package com.inview.backend.service;

import com.inview.backend.dto.PostScriptResponseDto;
import com.inview.backend.entity.PostScript;
import com.inview.backend.entity.User;
import com.inview.backend.repository.PostScriptRepository;
import com.inview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PostScriptService {

    private final PostScriptRepository postScriptRepository;
    private final UserRepository userRepository;

    public PostScriptResponseDto create(String userId, String title, String content) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        PostScript p = new PostScript();
        p.setUser(user);
        p.setTitle(title);
        p.setContent(content);

        LocalDateTime now = LocalDateTime.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        PostScript saved = postScriptRepository.save(p);
        return PostScriptResponseDto.from(saved);
    }

    public PostScriptResponseDto update(Long id, String userId, String title, String content) {
        PostScript p = postScriptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("후기 글이 존재하지 않습니다."));

        if (!p.getUser().getUserId().equals(userId)) {
            throw new SecurityException("본인만 수정할 수 있습니다.");
        }

        p.setTitle(title);
        p.setContent(content);
        p.setUpdatedAt(LocalDateTime.now());

        PostScript updated = postScriptRepository.save(p);
        return PostScriptResponseDto.from(updated);
    }

    public void delete(Long id, String userId) {
        PostScript p = postScriptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("후기 글이 존재하지 않습니다."));
        if (!p.getUser().getUserId().equals(userId)) {
            throw new SecurityException("본인만 삭제할 수 있습니다.");
        }
        postScriptRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public Page<PostScriptResponseDto> list(Pageable pageable) {
        return postScriptRepository.findAll(pageable).map(PostScriptResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<PostScriptResponseDto> search(String keyword, Pageable pageable) {
        return postScriptRepository.findByTitleContaining(keyword, pageable)
                .map(PostScriptResponseDto::from);
    }

    @Transactional(readOnly = true)
    public PostScriptResponseDto get(Long id) {
        PostScript p = postScriptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("후기 글이 존재하지 않습니다."));
        return PostScriptResponseDto.from(p);
    }

    @Transactional
    public PostScriptResponseDto readAndIncrease(Long id) {
        postScriptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("후기 글이 존재하지 않습니다."));
        postScriptRepository.increaseViewCount(id);
        PostScript fresh = postScriptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("후기 글이 존재하지 않습니다."));
        return PostScriptResponseDto.from(fresh);
    }
}
