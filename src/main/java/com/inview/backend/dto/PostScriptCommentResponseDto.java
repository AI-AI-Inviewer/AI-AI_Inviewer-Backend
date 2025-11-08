// src/main/java/com/inview/backend/dto/PostScriptCommentResponseDto.java
package com.inview.backend.dto;

import com.inview.backend.entity.PostScriptComment;
import lombok.Builder;

import java.util.Date;

@Builder
public record PostScriptCommentResponseDto(
        Long pscommentNum,
        Long postscriptNum,
        String content,
        Date pscommentDate,
        String userId,
        String userName,
        String userNickname
) {
    public static PostScriptCommentResponseDto from(PostScriptComment c) {
        return PostScriptCommentResponseDto.builder()
                .pscommentNum(c.getPscommentNum())
                .postscriptNum(c.getPostscript().getPostscriptNum())
                .content(c.getContent())
                .pscommentDate(c.getPscommentDate())
                .userId(c.getUser() != null ? c.getUser().getUserId() : null)
                .userName(c.getUser() != null ? c.getUser().getUserName() : null)
                .userNickname(c.getUser() != null ? c.getUser().getUserNickname() : null)
                .build();
    }
}
