package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "COMMENT_TABLE")
@Getter
@Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_NUM")
    private Long commentNum;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COMMENT_DATE")
    private Date commentDate = new Date();

    @Column(name = "COMMENT_LIKE")
    private int commentLike = 0;

    @Column(name = "COMMENT_DISLIKE")
    private int commentDislike = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    private User user;

    @ManyToOne
    @JoinColumn(name = "COMMUNITY_NUM")
    private Community community;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;
}
