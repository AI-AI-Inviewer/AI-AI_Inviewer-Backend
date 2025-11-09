// src/main/java/com/inview/backend/dto/ResumeTextDto.java
package com.inview.backend.dto;

import java.time.LocalDateTime;

public class ResumeTextDto {
    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;
    private String text;

    public ResumeTextDto(Long id, String fileName, String contentType,
                         Long fileSize, LocalDateTime createdAt, String text) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
        this.text = text;
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getText() { return text; }
}
