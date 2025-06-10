package com.inview.backend.repository;

import com.inview.backend.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    Page<Community> findByCommunityTitleContaining(String keyword, Pageable pageable);
}

