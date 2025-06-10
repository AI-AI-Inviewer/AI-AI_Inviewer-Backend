package com.inview.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @Column(name = "SUB_COMMENT_DATE", nullable = false)
    private Date subCommentDate;

    @Column(name = "SUB_COMMENT_LIKE", nullable = false)
    private int subCommentLike = 0;

    @Column(name = "SUB_COMMENT_DISLIKE", nullable = false)
    private int subCommentDislike = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;


    @ManyToOne
    @JoinColumn(name = "COMMENT_NUM")
    private Comment comment;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;
}
