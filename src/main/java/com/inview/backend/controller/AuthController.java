package com.inview.backend.controller;
import com.inview.backend.config.JwtTokenProvider;
import com.inview.backend.entity.User;
import com.inview.backend.repository.UserRepository;
import com.inview.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PutMapping("/me")
    public ResponseEntity<User> updateMyInfo(
            Authentication authentication,
            @RequestBody User updatedUser
    ) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String userId = (String) authentication.getPrincipal();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setUserName(updatedUser.getUserName());
        user.setUserNickname(updatedUser.getUserNickname());
        user.setUserEmail(updatedUser.getUserEmail());
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

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
            User user = new User();
            user.setUserId(userId);
            user.setUserPassword(password);
            user.setUserEmail(email);
            user.setUserName(name);
            user.setUserNickname(nickname);

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

    @GetMapping("/me")
    public ResponseEntity<User> getMyInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String userId = (String) authentication.getPrincipal();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(user);
    }
}
