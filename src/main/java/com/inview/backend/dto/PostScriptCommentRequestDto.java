package com.inview.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PostScriptCommentRequestDto {
    private Long postscriptNum;
    private String content;
}
