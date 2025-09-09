package com.inview.backend.service;

import com.inview.backend.entity.Community;
import com.inview.backend.entity.User;
import com.inview.backend.repository.CommunityRepository;
import com.inview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public Community create(String userId, String title, String content, String resume) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Community c = new Community();
        c.setUser(user);
        c.setCommunityTitle(title);
        c.setCommunityContent(content);
        c.setCommunityResume(resume);
        c.setCommunityWriteDate(new Date());
        c.setCommunityUpdate(new Date());
        return communityRepository.save(c);
    }

    public Community update(Long id, String userId, String title, String content, String resume) {
        Community c = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        if (!c.getUser().getUserId().equals(userId)) throw new SecurityException("본인만 수정할 수 있습니다.");
        c.setCommunityTitle(title);
        c.setCommunityContent(content);
        c.setCommunityResume(resume);
        c.setCommunityUpdate(new Date());
        return communityRepository.save(c);
    }

    public void delete(Long id, String userId) {
        Community c = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        if (!c.getUser().getUserId().equals(userId)) throw new SecurityException("본인만 삭제할 수 있습니다.");
        communityRepository.deleteById(id);
    }

    public Page<Community> list(Pageable pageable) {
        return communityRepository.findAll(pageable);
    }

    public Page<Community> search(String keyword, Pageable pageable) {
        return communityRepository.findByCommunityTitleContaining(keyword, pageable);
    }

    public Community get(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    }
}
