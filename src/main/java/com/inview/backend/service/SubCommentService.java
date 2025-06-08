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

    public SubComment addSubComment(String userId, Long commentNum, String content) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentNum)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        SubComment subComment = new SubComment();
        subComment.setUser(user);
        subComment.setComment(comment);
        subComment.setContent(content);
        subComment.setSubCommentDate(new Date());

        return subCommentRepository.save(subComment);
    }

    public List<SubComment> getSubCommentsByComment(Long commentNum) {
        return subCommentRepository.findByCommentCommentNum(commentNum);
    }

    public void deleteSubComment(Long subCommentNum) {
        subCommentRepository.deleteById(subCommentNum);
    }
}
