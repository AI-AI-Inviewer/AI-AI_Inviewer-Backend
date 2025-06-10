package com.inview.backend.dto;

import lombok.Data;

@Data
public class CommunityRequestDto {
    private String title;
    private String content;
    private String resume;  // 필요 시 사용
}