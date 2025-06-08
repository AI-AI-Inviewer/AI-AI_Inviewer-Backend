package com.inview.backend.service;

import com.inview.backend.entity.Comment;
import com.inview.backend.entity.Community;
import com.inview.backend.entity.User;
import com.inview.backend.repository.CommentRepository;
import com.inview.backend.repository.CommunityRepository;
import com.inview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public Comment addComment(String userId, Long communityNum, String content) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Community community = communityRepository.findById(communityNum)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setCommunity(community);
        comment.setContent(content);
        comment.setCommentDate(new Date());

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByCommunity(Long communityNum) {
        return commentRepository.findByCommunityCommunityNum(communityNum);
    }

    public void deleteComment(Long commentNum) {
        commentRepository.deleteById(commentNum);
    }
}
