package com.inview.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "POSTSCRIPT_TABLE")
@SequenceGenerator(
        name = "postscript_seq",
        sequenceName = "POSTSCRIPT_SEQ",
        allocationSize = 1
)
public class PostScript {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "postscript_seq")
    @Column(name = "POSTSCRIPT_NUM")
    private Long postscriptNum;

    @Column(name = "POSTSCRIPT_TITLE", nullable = false, length = 100)
    private String title;

    @Column(name = "POSTSCRIPT_CONTENT", nullable = false, length = 2000)
    private String content;

    @Column(name = "POSTSCRIPT_DATE", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "POSTSCRIPT_UPDATE", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "POSTSCRIPT_VIEWCOUNT", nullable = false)
    private Long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    public PostScript() {}

    // getters / setters
    public Long getPostscriptNum() { return postscriptNum; }
    public void setPostscriptNum(Long postscriptNum) { this.postscriptNum = postscriptNum; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
