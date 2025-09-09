package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "COMMUNITY_TABLE")
@Getter
@Setter
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMUNITY_NUM")
    private Long communityNum;

    @Column(name = "COMMUNITY_TITLE", nullable = false, length = 200)
    private String communityTitle;

    @Column(name = "COMMUNITY_CONTENT", nullable = false, length = 4000)
    private String communityContent;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COMMUNITY_WRITE_DATE")
    private Date communityWriteDate = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COMMUNITY_UPDATE")
    private Date communityUpdate = new Date();

    @Column(name = "COMMUNITY_VIEWCOUNT")
    private Long communityViewCount = 0L;

    @Lob
    @Column(name = "COMMUNITY_RESUME")
    private String communityResume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    private User user;
}
