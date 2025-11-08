package com.inview.backend.repository;

import com.inview.backend.entity.PostScriptComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostScriptCommentRepository extends JpaRepository<PostScriptComment, Long> {

    @EntityGraph(attributePaths = {"user", "postscript"})
    List<PostScriptComment> findByPostscriptPostscriptNumOrderByPscommentNumAsc(Long postscriptNum);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PostScriptComment c where c.postscript.postscriptNum = :postscriptNum")
    int deleteByPostscript(@Param("postscriptNum") Long postscriptNum);
}
