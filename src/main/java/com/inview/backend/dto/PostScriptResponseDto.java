// src/main/java/com/inview/backend/dto/PostScriptResponseDto.java
package com.inview.backend.dto;

import com.inview.backend.entity.PostScript;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PostScriptResponseDto(
        Long postscriptNum,
        String title,
        String content,
        String userId,
        String userName,
        String userNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long viewCount
) {
    public static PostScriptResponseDto from(PostScript p) {
        return PostScriptResponseDto.builder()
                .postscriptNum(p.getPostscriptNum())
                .title(p.getTitle())
                .content(p.getContent())
                .userId(p.getUser() != null ? p.getUser().getUserId() : null)
                .userName(p.getUser() != null ? p.getUser().getUserName() : null)
                .userNickname(p.getUser() != null ? p.getUser().getUserNickname() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .viewCount(p.getViewCount())
                .build();
    }
}
