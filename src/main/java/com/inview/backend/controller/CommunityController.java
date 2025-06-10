package com.inview.backend.controller;

import com.inview.backend.dto.CommunityRequestDto;
import com.inview.backend.entity.Community;
import com.inview.backend.entity.User;
import com.inview.backend.repository.CommunityRepository;
import com.inview.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    // 🔥 GET: 게시글 목록
    @GetMapping
    public ResponseEntity<Page<Community>> getAllCommunity(
            @RequestParam(defaultValue = "1") int page,  // 프론트는 1부터 보냄
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKey
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "communityNum"));

        Page<Community> communityPage;
        if (searchKey != null && !searchKey.isBlank()) {
            communityPage = communityRepository.findByCommunityTitleContaining(searchKey, pageable);
        } else {
            communityPage = communityRepository.findAll(pageable);
        }

        return ResponseEntity.ok(communityPage);
    }



    // 🔥 POST: 게시글 작성
// CommunityController.java
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
        community.setCommunityResume(dto.getResume());  // dto에 resume가 있다면
        community.setCommunityDate(new Date());
        community.setCommunityUpdate(new Date());
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
