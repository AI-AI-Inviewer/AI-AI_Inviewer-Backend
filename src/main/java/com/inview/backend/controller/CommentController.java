package com.inview.backend.controller;

import com.inview.backend.entity.Comment;
import com.inview.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 등록
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @RequestParam String userId,
            @RequestParam Long communityNum,
            @RequestParam String content) {
        Comment comment = commentService.addComment(userId, communityNum, content);
        return ResponseEntity.ok(comment);
    }

    // 특정 커뮤니티의 댓글 목록 조회
    @GetMapping("/{communityNum}")
    public ResponseEntity<List<Comment>> getCommentsByCommunity(@PathVariable Long communityNum) {
        List<Comment> comments = commentService.getCommentsByCommunity(communityNum);
        return ResponseEntity.ok(comments);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentNum}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentNum) {
        commentService.deleteComment(commentNum);
        return ResponseEntity.noContent().build();
    }
}
