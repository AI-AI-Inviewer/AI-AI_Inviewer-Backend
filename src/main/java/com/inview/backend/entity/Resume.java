// src/main/java/com/inview/backend/entity/Resume.java
package com.inview.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RESUME_TABLE")
@SequenceGenerator(
        name = "RESUME_SEQ_GEN",
        sequenceName = "RESUME_SEQ",
        allocationSize = 1
)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RESUME_SEQ_GEN")
    @Column(name = "RESUME_ID")
    private Long id;

    // 로그인 아이디(문자열) 저장
    @Column(name = "USER_ID", length = 100)
    private String userId;

    @Column(name = "FILE_NAME", nullable = false, length = 255)
    private String fileName;

    @Column(name = "CONTENT_TYPE", nullable = false, length = 150)
    private String contentType;

    @Column(name = "FILE_SIZE", nullable = false)
    private Long fileSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "FILE_DATA", nullable = false)
    private byte[] fileData;

    // TIMESTAMP(6) ↔ LocalDateTime
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // getters & setters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public byte[] getFileData() { return fileData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setId(Long id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
