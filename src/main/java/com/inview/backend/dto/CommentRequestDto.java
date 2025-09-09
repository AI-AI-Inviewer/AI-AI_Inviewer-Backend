package com.inview.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentRequestDto {
    private Long communityNum;
    private String content;
}
