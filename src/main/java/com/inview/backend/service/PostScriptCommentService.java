// src/main/java/com/inview/backend/service/PostScriptCommentService.java
package com.inview.backend.service;

import com.inview.backend.dto.PostScriptCommentResponseDto;
import com.inview.backend.entity.PostScript;
import com.inview.backend.entity.PostScriptComment;
import com.inview.backend.entity.User;
import com.inview.backend.repository.PostScriptCommentRepository;
import com.inview.backend.repository.PostScriptRepository;
import com.inview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostScriptCommentService {

    private final PostScriptCommentRepository commentRepository;
    private final PostScriptRepository postScriptRepository;
    private final UserRepository userRepository;

    public PostScriptCommentResponseDto create(String userId, Long postscriptNum, String content) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        PostScript post = postScriptRepository.findById(postscriptNum)
                .orElseThrow(() -> new IllegalArgumentException("면접후기 글 없음"));

        PostScriptComment c = new PostScriptComment();
        c.setUser(user);
        c.setPostscript(post);
        c.setContent(content);
        c.setPscommentDate(new Date());

        PostScriptComment saved = commentRepository.save(c);
        return PostScriptCommentResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PostScriptCommentResponseDto> listByPost(Long postscriptNum) {
        return commentRepository
                .findByPostscriptPostscriptNumOrderByPscommentNumAsc(postscriptNum)
                .stream()
                .map(PostScriptCommentResponseDto::from)
                .toList();
    }

    public void delete(Long pscommentNum, String currentUserId) {
        PostScriptComment c = commentRepository.findById(pscommentNum)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        String writerId = c.getUser().getUserId();
        if (!writerId.equals(currentUserId)) {
            throw new SecurityException("본인만 삭제할 수 있습니다.");
        }
        commentRepository.deleteById(pscommentNum);
    }
}
