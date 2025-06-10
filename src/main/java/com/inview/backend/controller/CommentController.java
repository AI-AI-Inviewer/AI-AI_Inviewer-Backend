package com.inview.backend.controller;

import com.inview.backend.dto.CommentRequestDto;
import com.inview.backend.entity.Comment;
import com.inview.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> addComment(
            @RequestBody CommentRequestDto requestDto,
            Authentication authentication
    ) {
        String userId = (String) authentication.getPrincipal();
        Comment comment = commentService.addComment(userId, requestDto.getCommunityNum(), requestDto.getContent());
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{communityNum}")
    public ResponseEntity<List<Comment>> getCommentsByCommunity(@PathVariable Long communityNum) {
        List<Comment> comments = commentService.getCommentsByCommunity(communityNum);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentNum}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentNum) {
        commentService.deleteComment(commentNum);
        return ResponseEntity.noContent().build();
    }
}
