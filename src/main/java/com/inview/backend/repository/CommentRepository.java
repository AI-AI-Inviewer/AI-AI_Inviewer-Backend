package com.inview.backend.repository;

import com.inview.backend.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"user", "community"}) // 작성자/커뮤니티 함께 로딩
    List<Comment> findByCommunityCommunityNumOrderByCommentNumAsc(Long communityNum);
}
