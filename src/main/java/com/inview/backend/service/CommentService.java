package com.inview.backend.service;

import com.inview.backend.dto.CommentResponseDto;
import com.inview.backend.entity.Comment;
import com.inview.backend.entity.Community;
import com.inview.backend.entity.User;
import com.inview.backend.repository.CommentRepository;
import com.inview.backend.repository.CommunityRepository;
import com.inview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public CommentResponseDto createComment(String userId, Long communityNum, String content) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Community community = communityRepository.findById(communityNum)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Comment c = new Comment();
        c.setUser(user);
        c.setCommunity(community);
        c.setContent(content);
        c.setCommentDate(new Date());

        Comment saved = commentRepository.save(c);
        return CommentResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> listByCommunity(Long communityNum) {
        return commentRepository
                .findByCommunityCommunityNumOrderByCommentNumAsc(communityNum)
                .stream().map(CommentResponseDto::from).toList();
    }

    public void deleteComment(Long commentNum, String currentUserId) {
        Comment comment = commentRepository.findById(commentNum)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        String writerId = comment.getUser().getUserId();
        if (!writerId.equals(currentUserId)) {
            throw new SecurityException("본인만 삭제할 수 있습니다.");
        }
        commentRepository.deleteById(commentNum);
    }
}
