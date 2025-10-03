// src/main/java/com/inview/backend/repository/CommentRepository.java
package com.inview.backend.repository;

import com.inview.backend.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"user", "community"}) // 작성자/커뮤니티 함께 로딩
    List<Comment> findByCommunityCommunityNumOrderByCommentNumAsc(Long communityNum);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Comment c where c.community.communityNum = :communityNum")
    int deleteByCommunity(@Param("communityNum") Long communityNum);
}
