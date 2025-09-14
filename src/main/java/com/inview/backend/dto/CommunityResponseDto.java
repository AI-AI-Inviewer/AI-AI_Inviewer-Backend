package com.inview.backend.dto;

import com.inview.backend.entity.Community;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommunityResponseDto(
        Long communityNum,
        String title,
        String content,
        String resume,
        String userId,
        String userName,
        String userNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long viewCount
) {
    public static CommunityResponseDto from(Community c) {
        return CommunityResponseDto.builder()
                .communityNum(c.getCommunityNum())
                .title(c.getCommunityTitle())
                .content(c.getCommunityContent())
                .resume(c.getCommunityResume())
                .userId(c.getUser() != null ? c.getUser().getUserId() : null)
                .userName(c.getUser() != null ? c.getUser().getUserName() : null)
                .userNickname(c.getUser() != null ? c.getUser().getUserNickname() : null)
                .createdAt(c.getCommunityDate())
                .updatedAt(c.getCommunityUpdate())
                .viewCount(c.getCommunityViewCount())
                .build();
    }
}
