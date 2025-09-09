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
    public Comment create(@RequestBody CommentRequestDto dto, Authentication authentication) {
        String userId = authentication.getName();
        return commentService.createComment(userId, dto.getCommunityNum(), dto.getContent());
    }

    @GetMapping("/community/{communityNum}")
    public List<Comment> list(@PathVariable Long communityNum) {
        return commentService.listByCommunity(communityNum);
    }

    @DeleteMapping("/{commentNum}")
    public ResponseEntity<?> delete(@PathVariable Long commentNum, Authentication authentication) {
        String currentUserId = authentication.getName();
        commentService.deleteComment(commentNum, currentUserId);
        return ResponseEntity.ok().build();
    }
}
