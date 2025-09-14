package com.inview.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CommunityRequestDto {
    private String title;   // COMMUNITY_TITLE
    private String content; // COMMUNITY_CONTENT
    private String resume;  // COMMUNITY_RESUME (선택)
}
