package com.inview.backend.service;

import com.inview.backend.entity.Comment;
import com.inview.backend.entity.SubComment;
import com.inview.backend.entity.User;
import com.inview.backend.repository.CommentRepository;
import com.inview.backend.repository.SubCommentRepository;
import com.inview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubCommentService {

    private final SubCommentRepository subCommentRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public SubComment create(String userId, Long commentNum, String content) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Comment comment = commentRepository.findById(commentNum).orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        SubComment sc = new SubComment();
        sc.setUser(user);
        sc.setComment(comment);
        sc.setContent(content);
        sc.setSubCommentDate(new Date());
        return subCommentRepository.save(sc);
    }

    public List<SubComment> listByComment(Long commentNum) {
        return subCommentRepository.findByCommentCommentNum(commentNum);
    }

    public void delete(Long subCommentNum, String userId) {
        subCommentRepository.deleteById(subCommentNum);
    }
}
