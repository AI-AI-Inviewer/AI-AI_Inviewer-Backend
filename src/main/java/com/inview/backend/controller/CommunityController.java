package com.inview.backend.controller;

import com.inview.backend.dto.CommunityRequestDto;
import com.inview.backend.dto.CommunityResponseDto;
import com.inview.backend.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // 목록 (DTO 반환)
    @GetMapping
    public Page<CommunityResponseDto> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "communityNum,desc") String sort) {
        String[] s = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(s[1]), s[0]));
        return communityService.list(pageable);
    }

    // 검색 (DTO 반환)
    @GetMapping("/search")
    public Page<CommunityResponseDto> search(@RequestParam String keyword,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "communityNum"));
        return communityService.search(keyword, pageable);
    }

    @GetMapping("/{id}")
    public CommunityResponseDto get(@PathVariable Long id) {
        return communityService.readAndIncrease(id);  // ✅ 조회수 증가 포함
    }

    // 생성 (DTO 반환)
    @PostMapping
    public CommunityResponseDto create(@RequestBody CommunityRequestDto dto, Authentication authentication) {
        String userId = authentication.getName(); // JwtAuthenticationFilter에서 세팅
        return communityService.create(userId, dto.getTitle(), dto.getContent(), dto.getResume());
    }

    // 수정 (DTO 반환)
    @PutMapping("/{id}")
    public CommunityResponseDto update(@PathVariable Long id,
                                       @RequestBody CommunityRequestDto dto,
                                       Authentication authentication) {
        String userId = authentication.getName();
        return communityService.update(id, userId, dto.getTitle(), dto.getContent(), dto.getResume());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        String userId = authentication.getName();
        communityService.delete(id, userId);
        return ResponseEntity.ok().build();
    }
}
