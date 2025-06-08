package com.inview.backend.controller;

import com.inview.backend.config.JwtTokenProvider;
import com.inview.backend.entity.User;
import com.inview.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider; // 추가!

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            // User 객체 생성
            User user = new User();
            user.setUserId(userId);
            user.setUserPassword(password);
            user.setUserEmail(email);
            user.setUserName(name);
            user.setUserNickname(nickname);

            // TODO: profileImage 처리 (선택)
            if (profileImage != null && !profileImage.isEmpty()) {
                user.setUserImage(profileImage.getBytes());
            }
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User authenticatedUser = userService.authenticate(user.getUserId(), user.getUserPassword());
        String token = jwtTokenProvider.createToken(authenticatedUser.getUserId());
        return ResponseEntity.ok(token);
    }
}
