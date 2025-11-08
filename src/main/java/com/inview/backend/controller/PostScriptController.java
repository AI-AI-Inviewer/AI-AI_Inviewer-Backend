// src/main/java/com/inview/backend/controller/PostScriptController.java
package com.inview.backend.controller;

import com.inview.backend.dto.PostScriptRequestDto;
import com.inview.backend.dto.PostScriptResponseDto;
import com.inview.backend.service.PostScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/postscript")
@RequiredArgsConstructor
public class PostScriptController {

    private final PostScriptService postScriptService;

    // 목록
    @GetMapping
    public Page<PostScriptResponseDto> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "postscriptNum,desc") String sort) {
        String[] s = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(s[1]), s[0]));
        return postScriptService.list(pageable);
    }

    // 검색
    @GetMapping("/search")
    public Page<PostScriptResponseDto> search(@RequestParam String keyword,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postscriptNum"));
        return postScriptService.search(keyword, pageable);
    }

    // 상세 (조회수 증가)
    @GetMapping("/{id}")
    public PostScriptResponseDto get(@PathVariable Long id) {
        return postScriptService.readAndIncrease(id);
    }

    // 작성
    @PostMapping
    public PostScriptResponseDto create(@RequestBody PostScriptRequestDto dto, Authentication authentication) {
        String userId = authentication.getName();
        return postScriptService.create(userId, dto.getTitle(), dto.getContent());
    }

    // 수정
    @PutMapping("/{id}")
    public PostScriptResponseDto update(@PathVariable Long id,
                                        @RequestBody PostScriptRequestDto dto,
                                        Authentication authentication) {
        String userId = authentication.getName();
        return postScriptService.update(id, userId, dto.getTitle(), dto.getContent());
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        String userId = authentication.getName();
        postScriptService.delete(id, userId);
        return ResponseEntity.ok().build();
    }
}
