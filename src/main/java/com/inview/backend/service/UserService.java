// com/inview/backend/service/UserService.java
package com.inview.backend.service;

import com.inview.backend.entity.User;
import com.inview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    public User register(User user) {
        if (userRepository.existsByUserId(user.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByUserNickname(user.getUserNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        return userRepository.save(user);
    }

    /**
     * 로그인 인증
     */
    public User authenticate(String userId, String rawPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        if (!passwordEncoder.matches(rawPassword, user.getUserPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    /**
     * 비밀번호 변경
     * @param userNum           로그인한 사용자 PK
     * @param currentPassword   현재 비밀번호(평문)
     * @param newPassword       새 비밀번호(평문)
     */
    @Transactional
    public void changePassword(Long userNum, String currentPassword, String newPassword) {
        User user = userRepository.findById(userNum)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        // (안전장치) 동일 비밀번호 방지
        if (passwordEncoder.matches(newPassword, user.getUserPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 기존 비밀번호와 동일합니다.");
        }

        // 새 비밀번호 정책(최소 8자)은 컨트롤러에서도 체크하지만, 방어적으로 한 번 더 점검 가능
        if (newPassword == null || newPassword.trim().length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }

        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
