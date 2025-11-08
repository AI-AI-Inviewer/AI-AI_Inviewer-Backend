// src/main/java/com/inview/backend/entity/PostScriptComment.java
package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "POSTSCRIPT_COMMENT_TABLE")
@Getter @Setter
@NoArgsConstructor
@SequenceGenerator(name = "pscomment_seq", sequenceName = "POSTSCRIPT_COMMENT_SEQ", allocationSize = 1)
public class PostScriptComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pscomment_seq")
    @Column(name = "PSCOMMENT_NUM")
    private Long pscommentNum;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PSCOMMENT_DATE", nullable = false)
    private Date pscommentDate = new Date();

    @Column(name = "PSCOMMENT_LIKE", nullable = false)
    private int pscommentLike = 0;

    @Column(name = "PSCOMMENT_DISLIKE", nullable = false)
    private int pscommentDislike = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POSTSCRIPT_NUM")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PostScript postscript;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;
}
