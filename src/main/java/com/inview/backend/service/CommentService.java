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

    public Comment createComment(String userId, Long communityNum, String content) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Community community = communityRepository.findById(communityNum).orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        Comment c = new Comment();
        c.setUser(user);
        c.setCommunity(community);
        c.setContent(content);
        c.setCommentDate(new Date());
        return commentRepository.save(c);
    }

    public List<Comment> listByCommunity(Long communityNum) {
        return commentRepository.findByCommunityCommunityNum(communityNum);
    }

    public void deleteComment(Long commentNum, String currentUserId) {
        Comment comment = commentRepository.findById(commentNum)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        String writerId = comment.getUser().getUserId();
        if (!writerId.equals(currentUserId)) throw new SecurityException("본인만 삭제할 수 있습니다.");
        commentRepository.deleteById(commentNum);
    }
}
