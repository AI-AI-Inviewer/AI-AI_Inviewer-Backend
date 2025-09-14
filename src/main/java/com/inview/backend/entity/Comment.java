package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "COMMENT_TABLE")
@Getter @Setter
@NoArgsConstructor
@SequenceGenerator(name = "comment_seq", sequenceName = "COMMENT_SEQ", allocationSize = 1)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq")
    @Column(name = "COMMENT_NUM")
    private Long commentNum;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COMMENT_DATE", nullable = false)
    private Date commentDate = new Date();

    @Column(name = "COMMENT_LIKE", nullable = false)
    private int commentLike = 0;

    @Column(name = "COMMENT_DISLIKE", nullable = false)
    private int commentDislike = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    @com.fasterxml.jackson.annotation.JsonIgnore    // 엔티티를 실수로 반환해도 순환 막기
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMMUNITY_NUM")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Community community;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;
}
