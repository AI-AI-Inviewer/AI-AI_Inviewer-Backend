package com.inview.backend.service;

import com.inview.backend.entity.Community;
import com.inview.backend.entity.User;
import com.inview.backend.repository.CommunityRepository;
import com.inview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    // 게시글 등록
    public Community createCommunity(String userId, Community community) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        community.setUser(user);
        community.setCommunityDate(new Date());
        community.setCommunityUpdate(new Date());
        return communityRepository.save(community);
    }

    // 전체 게시글 조회
    public List<Community> getAllCommunities() {
        return communityRepository.findAll();
    }

    // 특정 게시글 조회
    public Optional<Community> getCommunityById(Long id) {
        return communityRepository.findById(id);
    }

    // 게시글 수정
    public Community updateCommunity(Long id, Community updatedCommunity) {
        Community existing = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        existing.setCommunityTitle(updatedCommunity.getCommunityTitle());
        existing.setCommunityContent(updatedCommunity.getCommunityContent());
        existing.setCommunityUpdate(new Date());
        return communityRepository.save(existing);
    }

    // 게시글 삭제
    public void deleteCommunity(Long id) {
        communityRepository.deleteById(id);
    }
}
