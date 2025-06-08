package com.inview.backend.controller;

import com.inview.backend.entity.SubComment;
import com.inview.backend.service.SubCommentService;
import com.inview.backend.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcomments")
@RequiredArgsConstructor
public class SubCommentController {

    private final SubCommentService subCommentService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/{commentNum}")
    public ResponseEntity<SubComment> addSubComment(@RequestHeader("Authorization") String token,
                                                    @PathVariable Long commentNum,
                                                    @RequestBody String content) {
        String userId = extractUserId(token);
        SubComment subComment = subCommentService.addSubComment(userId, commentNum, content);
        return ResponseEntity.ok(subComment);
    }

    @GetMapping("/{commentNum}")
    public ResponseEntity<List<SubComment>> getSubComments(@PathVariable Long commentNum) {
        List<SubComment> subComments = subCommentService.getSubCommentsByComment(commentNum);
        return ResponseEntity.ok(subComments);
    }

    @DeleteMapping("/{subCommentNum}")
    public ResponseEntity<String> deleteSubComment(@PathVariable Long subCommentNum) {
        subCommentService.deleteSubComment(subCommentNum);
        return ResponseEntity.ok("대댓글 삭제 완료");
    }

    private String extractUserId(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtTokenProvider.getUserId(token);
    }
}
