package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "SUB_COMMENT_TABLE")
@Getter
@Setter
@NoArgsConstructor
public class SubComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUB_COMMENT_NUM")
    private Long subCommentNum;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SUB_COMMENT_DATE")
    private Date subCommentDate = new Date();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    private User user;

    @ManyToOne
    @JoinColumn(name = "COMMENT_NUM")
    private Comment comment;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;
}
