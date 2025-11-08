// src/main/java/com/inview/backend/controller/PostScriptCommentController.java
package com.inview.backend.controller;

import com.inview.backend.dto.PostScriptCommentRequestDto;
import com.inview.backend.dto.PostScriptCommentResponseDto;
import com.inview.backend.service.PostScriptCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postscript-comments")
@RequiredArgsConstructor
public class PostScriptCommentController {

    private final PostScriptCommentService postCommentService;

    // 댓글 등록
    @PostMapping
    public PostScriptCommentResponseDto create(@RequestBody PostScriptCommentRequestDto dto,
                                               Authentication authentication) {
        String userId = authentication.getName();
        return postCommentService.create(userId, dto.getPostscriptNum(), dto.getContent());
    }

    // 목록: GET /api/postscript-comments/{postscriptNum}
    @GetMapping("/{postscriptNum}")
    public List<PostScriptCommentResponseDto> list(@PathVariable Long postscriptNum) {
        return postCommentService.listByPost(postscriptNum);
    }

    // 호환 경로: /api/postscript/{postscriptNum}/comments
    @GetMapping("/post/{postscriptNum}")
    public List<PostScriptCommentResponseDto> listCompat(@PathVariable Long postscriptNum) {
        return postCommentService.listByPost(postscriptNum);
    }

    // 삭제
    @DeleteMapping("/{pscommentNum}")
    public ResponseEntity<?> delete(@PathVariable Long pscommentNum, Authentication authentication) {
        String currentUserId = authentication.getName();
        postCommentService.delete(pscommentNum, currentUserId);
        return ResponseEntity.ok().build();
    }
}
