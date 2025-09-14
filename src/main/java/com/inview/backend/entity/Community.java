package com.inview.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "COMMUNITY_TABLE")
@SequenceGenerator(
        name = "community_seq",
        sequenceName = "COMMUNITY_SEQ",
        allocationSize = 1
)
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "community_seq")
    @Column(name = "COMMUNITY_NUM")
    private Long communityNum;

    @Column(name = "COMMUNITY_TITLE", nullable = false, length = 100)
    private String communityTitle;

    @Column(name = "COMMUNITY_CONTENT", nullable = false, length = 1000)
    private String communityContent;

    @Column(name = "COMMUNITY_DATE", nullable = false)
    private LocalDateTime communityDate;   // Oracle DATE/TIMESTAMP 매핑 OK

    @Column(name = "COMMUNITY_UPDATE", nullable = false)
    private LocalDateTime communityUpdate;

    @Column(name = "COMMUNITY_VIEWCOUNT", nullable = false)
    private Long communityViewCount = 0L;

    @Lob
    @Column(name = "COMMUNITY_RESUME")
    private String communityResume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    public Community() {}

    // getters / setters
    public Long getCommunityNum() { return communityNum; }
    public void setCommunityNum(Long communityNum) { this.communityNum = communityNum; }
    public String getCommunityTitle() { return communityTitle; }
    public void setCommunityTitle(String communityTitle) { this.communityTitle = communityTitle; }
    public String getCommunityContent() { return communityContent; }
    public void setCommunityContent(String communityContent) { this.communityContent = communityContent; }
    public LocalDateTime getCommunityDate() { return communityDate; }
    public void setCommunityDate(LocalDateTime communityDate) { this.communityDate = communityDate; }
    public LocalDateTime getCommunityUpdate() { return communityUpdate; }
    public void setCommunityUpdate(LocalDateTime communityUpdate) { this.communityUpdate = communityUpdate; }
    public Long getCommunityViewCount() { return communityViewCount; }
    public void setCommunityViewCount(Long communityViewCount) { this.communityViewCount = communityViewCount; }
    public String getCommunityResume() { return communityResume; }
    public void setCommunityResume(String communityResume) { this.communityResume = communityResume; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
