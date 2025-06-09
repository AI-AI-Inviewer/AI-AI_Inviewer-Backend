package com.inview.backend.controller;

import com.inview.backend.dto.CommunityRequestDto;
import com.inview.backend.entity.Community;
import com.inview.backend.entity.User;
import com.inview.backend.repository.CommunityRepository;
import com.inview.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    // 🔥 GET: 게시글 목록
    @GetMapping
    public ResponseEntity<List<Community>> getAllCommunity() {
        List<Community> list = communityRepository.findAll();
        return ResponseEntity.ok(list);
    }

    // 🔥 POST: 게시글 작성
    @PostMapping
    @Transactional
    public ResponseEntity<Community> createCommunity(
            @RequestBody CommunityRequestDto dto,
            Authentication authentication
    ) {
        String userId = (String) authentication.getPrincipal();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Community community = new Community();
        community.setCommunityTitle(dto.getTitle());
        community.setCommunityContent(dto.getContent());
        community.setUser(user);

        Community saved = communityRepository.save(community);
        return ResponseEntity.ok(saved);
    }
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<Community> getCommunityById(@PathVariable Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 조회수 증가
        community.setCommunityViewCount(community.getCommunityViewCount() + 1);
        // JPA가 영속 상태라서 트랜잭션 커밋 시 자동 업데이트됨

        return ResponseEntity.ok(community);
    }

}
