package com.inview.backend.controller;

import com.inview.backend.entity.Community;
import com.inview.backend.service.CommunityService;
import com.inview.backend.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final JwtTokenProvider jwtTokenProvider;

    // 게시글 등록
    @PostMapping
    public ResponseEntity<?> createCommunity(@RequestHeader("Authorization") String token,
                                             @RequestBody Community community) {
        String userId = extractUserId(token);
        Community created = communityService.createCommunity(userId, community);
        return ResponseEntity.ok(created);
    }

    // 전체 게시글 조회
    @GetMapping
    public ResponseEntity<List<Community>> getAllCommunities() {
        return ResponseEntity.ok(communityService.getAllCommunities());
    }

    // 특정 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<Community> getCommunityById(@PathVariable Long id) {
        return communityService.getCommunityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCommunity(@PathVariable Long id, @RequestBody Community community) {
        Community updated = communityService.updateCommunity(id, community);
        return ResponseEntity.ok(updated);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommunity(@PathVariable Long id) {
        communityService.deleteCommunity(id);
        return ResponseEntity.ok("삭제 완료");
    }

    // JWT 토큰에서 userId 추출
    private String extractUserId(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtTokenProvider.getUserId(token);
    }
}
