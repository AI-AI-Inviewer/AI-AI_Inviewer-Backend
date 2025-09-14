package com.inview.backend.dto;

import com.inview.backend.entity.Comment;
import lombok.Builder;

import java.util.Date;

@Builder
public record CommentResponseDto(
        Long commentNum,
        Long communityNum,
        String content,
        Date commentDate,
        String userId,
        String userName,
        String userNickname
) {
    public static CommentResponseDto from(Comment c) {
        return CommentResponseDto.builder()
                .commentNum(c.getCommentNum())
                .communityNum(c.getCommunity().getCommunityNum())
                .content(c.getContent())
                .commentDate(c.getCommentDate())
                .userId(c.getUser() != null ? c.getUser().getUserId() : null)
                .userName(c.getUser() != null ? c.getUser().getUserName() : null)
                .userNickname(c.getUser() != null ? c.getUser().getUserNickname() : null)
                .build();
    }
}
