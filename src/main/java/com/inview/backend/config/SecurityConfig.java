package com.inview.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS & CSRF
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // 세션 미사용 (JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증/인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // --- CORS Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- 정적/헬스체크(필요 시)
                        .requestMatchers("/", "/index.html", "/static/**", "/assets/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // --- ElevenLabs: 프론트에서 미리듣기/테스트 용도로 공개
                        .requestMatchers(HttpMethod.GET,
                                "/api/chat/elevenlabs/whoami",
                                "/api/chat/elevenlabs/tts-test",
                                "/api/chat/voices"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/chat/tts").permitAll()

                        // --- 공개 인증 엔드포인트
                        .requestMatchers("/api/auth/**", "/auth/**", "/api/user/**").permitAll()

                        // --- 커뮤니티 공개 조회
                        .requestMatchers(HttpMethod.GET,
                                "/api/community", "/api/community/**", "/api/community/search"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()

                        // --- 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/comments").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/chat").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/chat/stt").authenticated()

                        // --- 그 외 전부 인증
                        .anyRequest().authenticated()
                )

                // 예외 처리: 인증 실패는 반드시 401로, 권한 거부는 403
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            res.setContentType("text/plain;charset=UTF-8");
                            res.getWriter().write("Unauthorized");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                            res.setContentType("text/plain;charset=UTF-8");
                            res.getWriter().write("Forbidden");
                        })
                )

                // 기본 폼/베이식 인증 비활성화 (JWT만 사용)
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())

                // JWT 필터 삽입
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 프론트 도메인만 명시 (credentials 사용 시 * 불가)
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "https://kwungjin.site",
                "https://*.kwungjin.site"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        // Authorization, Content-Type 은 반드시 허용
        config.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // preflight 캐시

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
