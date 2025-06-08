package com.inview.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "USER_TABLE")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NUM")
    private Long userNum;

    @Column(name = "USER_ID", nullable = false, unique = true)
    private String userId;

    @Column(name = "USER_PASSWORD", nullable = false)
    private String userPassword;

    @Column(name = "USER_EMAIL", nullable = false)
    private String userEmail;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @Column(name = "USER_NICKNAME", nullable = false)
    private String userNickname;

    @Lob
    @Column(name = "USER_TABLE_IMAGE")
    private byte[] userImage;

    // (추가) 생성자
    public User(String userId, String userPassword, String userEmail, String userName, String userNickname, byte[] userImage) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userNickname = userNickname;
        this.userImage = userImage;
    }
}
