package com.inview.backend.controller;

import com.inview.backend.entity.Comment;
import com.inview.backend.service.CommentService;
import com.inview.backend.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/{communityNum}")
    public ResponseEntity<Comment> addComment(@RequestHeader("Authorization") String token,
                                              @PathVariable Long communityNum,
                                              @RequestBody String content) {
        String userId = extractUserId(token);
        Comment comment = commentService.addComment(userId, communityNum, content);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{communityNum}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long communityNum) {
        List<Comment> comments = commentService.getCommentsByCommunity(communityNum);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentNum}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentNum) {
        commentService.deleteComment(commentNum);
        return ResponseEntity.ok("댓글 삭제 완료");
    }

    private String extractUserId(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtTokenProvider.getUserId(token);
    }
}
