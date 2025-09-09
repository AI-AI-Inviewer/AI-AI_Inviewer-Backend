package com.inview.backend.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "USER_TABLE")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_NUM")
    private Long userNum;

    @Column(name = "USER_ID", unique = true, nullable = false, length = 50)
    private String userId;

    @Column(name = "USER_PASSWORD", nullable = false)
    private String userPassword;

    @Column(name = "USER_EMAIL", nullable = false, length = 100)
    private String userEmail;

    @Column(name = "USER_NAME", nullable = false, length = 50)
    private String userName;

    @Column(name = "USER_NICKNAME", unique = true, nullable = false, length = 50)
    private String userNickname;

    @Lob
    @Basic(fetch = FetchType.LAZY)   // (선택) BLOB 지연로딩 추천
    @Column(name = "USER_TABLE_IMAGE")
    private byte[] userImage;

    public User() {}
    public User(String userId, String userPassword, String userEmail, String userName, String userNickname, byte[] userImage) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userNickname = userNickname;
        this.userImage = userImage;
    }

    public Long getUserNum() { return userNum; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public byte[] getUserImage() { return userImage; }
    public void setUserImage(byte[] userImage) { this.userImage = userImage; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
    @Override public String getPassword() { return userPassword; }
    @Override public String getUsername() { return userId; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
