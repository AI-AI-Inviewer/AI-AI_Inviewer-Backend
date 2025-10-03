package com.inview.backend.repository;

import com.inview.backend.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    @Override
    @EntityGraph(attributePaths = "user") // 목록에서 작성자 함께 로딩
    Page<Community> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "user") // 검색도 작성자 함께 로딩
    Page<Community> findByCommunityTitleContaining(String keyword, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user") // 상세도 작성자 함께 로딩
    Optional<Community> findById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Community c " +
            "set c.communityViewCount = c.communityViewCount + 1 " +
            "where c.communityNum = :id")
    int increaseViewCount(@Param("id") Long id);
}
