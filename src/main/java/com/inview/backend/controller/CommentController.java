package com.inview.backend.controller;

import com.inview.backend.dto.CommentRequestDto;
import com.inview.backend.dto.CommentResponseDto;
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

    // 댓글 등록 (로그인 필요)
    @PostMapping
    public CommentResponseDto create(@RequestBody CommentRequestDto dto, Authentication authentication) {
        String userId = authentication.getName();
        return commentService.createComment(userId, dto.getCommunityNum(), dto.getContent());
    }

    // ✅ 프론트가 호출하는 형태: GET /api/comments/{communityNum}
    @GetMapping("/{communityNum}")
    public List<CommentResponseDto> list(@PathVariable Long communityNum) {
        return commentService.listByCommunity(communityNum);
    }

    // (선택) 과거 경로 호환: /api/comments/community/{communityNum}
    @GetMapping("/community/{communityNum}")
    public List<CommentResponseDto> listCompat(@PathVariable Long communityNum) {
        return commentService.listByCommunity(communityNum);
    }

    // 댓글 삭제 (로그인 필요)
    @DeleteMapping("/{commentNum}")
    public ResponseEntity<?> delete(@PathVariable Long commentNum, Authentication authentication) {
        String currentUserId = authentication.getName();
        commentService.deleteComment(commentNum, currentUserId);
        return ResponseEntity.ok().build();
    }
}
