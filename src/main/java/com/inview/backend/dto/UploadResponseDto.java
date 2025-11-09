// src/main/java/com/inview/backend/dto/UploadResponseDto.java
package com.inview.backend.dto;

public class UploadResponseDto {
    private Long resumeId;
    private String message;

    public UploadResponseDto(Long resumeId, String message) {
        this.resumeId = resumeId;
        this.message = message;
    }

    public Long getResumeId() { return resumeId; }
    public String getMessage() { return message; }
}
