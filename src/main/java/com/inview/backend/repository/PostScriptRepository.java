// src/main/java/com/inview/backend/repository/PostScriptRepository.java
package com.inview.backend.repository;

import com.inview.backend.entity.PostScript;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostScriptRepository extends JpaRepository<PostScript, Long> {

    @Override
    @EntityGraph(attributePaths = "user")
    Page<PostScript> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<PostScript> findByTitleContaining(String keyword, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<PostScript> findById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PostScript p set p.viewCount = p.viewCount + 1 where p.postscriptNum = :id")
    int increaseViewCount(@Param("id") Long id);
}
