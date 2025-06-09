package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "COMMUNITY_TABLE")
@Getter
@Setter
@NoArgsConstructor
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMUNITY_NUM")
    private Long communityNum;

    @Column(name = "COMMUNITY_TITLE", nullable = false)
    private String communityTitle;

    @Column(name = "COMMUNITY_CONTENT", nullable = false, length = 1000)
    private String communityContent;

    @CreationTimestamp  // 🔥 INSERT 시 자동
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COMMUNITY_DATE", nullable = false, updatable = false)
    private Date communityDate;

    @UpdateTimestamp  // 🔥 UPDATE 시 자동
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COMMUNITY_UPDATE", nullable = false)
    private Date communityUpdate;

    @Column(name = "COMMUNITY_VIEWCOUNT", nullable = false)
    private int communityViewCount = 0;

    @Lob
    @Column(name = "COMMUNITY_RESUME")
    private String communityResume;

    @ManyToOne
    @JoinColumn(name = "USER_NUM")
    private User user;
}
