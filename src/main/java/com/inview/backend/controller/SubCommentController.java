package com.inview.backend.controller;

import com.inview.backend.entity.SubComment;
import com.inview.backend.service.SubCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subcomments")
@RequiredArgsConstructor
public class SubCommentController {

    private final SubCommentService subCommentService;

    @PostMapping
    public SubComment create(@RequestBody Map<String, Object> body, Authentication authentication) {
        String userId = authentication.getName();
        Long commentNum = ((Number) body.get("commentNum")).longValue();
        String content = (String) body.get("content");
        return subCommentService.create(userId, commentNum, content);
    }

    // ✅ 프론트가 읽기 쉬운 경로: GET /api/subcomments/{commentNum}
    @GetMapping("/{commentNum}")
    public List<SubComment> list(@PathVariable Long commentNum) {
        return subCommentService.listByComment(commentNum);
    }

    @DeleteMapping("/{subCommentNum}")
    public ResponseEntity<?> delete(@PathVariable Long subCommentNum, Authentication authentication) {
        String userId = authentication.getName();
        subCommentService.delete(subCommentNum, userId);
        return ResponseEntity.ok().build();
    }
}
