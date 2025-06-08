package com.inview.backend.repository;

import com.inview.backend.entity.SubComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCommentRepository extends JpaRepository<SubComment, Long> {
    List<SubComment> findByCommentCommentNum(Long commentNum);
}
