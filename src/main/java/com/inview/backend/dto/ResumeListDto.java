// src/main/java/com/inview/backend/dto/ResumeListDto.java
package com.inview.backend.dto;

import java.time.LocalDateTime;

public class ResumeListDto {
    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;

    public ResumeListDto(Long id, String fileName, String contentType, Long fileSize, LocalDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
