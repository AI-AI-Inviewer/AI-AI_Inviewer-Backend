package com.inview.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @Column(name = "COMMUNITY_TITLE", nullable = false)
    private String communityTitle;

    @Column(name = "COMMUNITY_CONTENT", nullable = false)
    private String communityContent;

    @Column(name = "COMMUNITY_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date communityDate;

    @Column(name = "COMMUNITY_UPDATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date communityUpdate;

    @Column(name = "COMMUNITY_VIEWCOUNT")
    private Long communityViewCount = 0L;

    @Lob
    @Column(name = "COMMUNITY_RESUME")
    private String communityResume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

}
