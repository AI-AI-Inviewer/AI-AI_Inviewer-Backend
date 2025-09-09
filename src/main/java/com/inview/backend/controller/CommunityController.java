package com.inview.backend.controller;

import com.inview.backend.dto.CommunityRequestDto;
import com.inview.backend.entity.Community;
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

    @GetMapping
    public Page<Community> list(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "communityNum,desc") String sort) {
        String[] s = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(s[1]), s[0]));
        return communityService.list(pageable);
    }

    @GetMapping("/search")
    public Page<Community> search(@RequestParam String keyword,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "communityNum"));
        return communityService.search(keyword, pageable);
    }

    @GetMapping("/{id}")
    public Community get(@PathVariable Long id) {
        return communityService.get(id);
    }

    @PostMapping
    public Community create(@RequestBody CommunityRequestDto dto, Authentication authentication) {
        String userId = authentication.getName();
        return communityService.create(userId, dto.getTitle(), dto.getContent(), dto.getResume());
    }

    @PutMapping("/{id}")
    public Community update(@PathVariable Long id, @RequestBody CommunityRequestDto dto, Authentication authentication) {
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
