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
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index.html", "/static/**", "/assets/**", "/actuator/health", "/error").permitAll()

                        // ✅ 회원가입 전 공개 엔드포인트
                        .requestMatchers(HttpMethod.GET,
                                "/api/user/check-id",
                                "/api/user/check-email"          // 쓰면 같이 허용
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/user/register",
                                "/api/user/login",
                                "/api/user/email-code/send",     // 너 코드의 경로명과 일치하게
                                "/api/user/email-code/verify"    // 위와 동일
                        ).permitAll()

                        // 공개 조회
                        .requestMatchers(HttpMethod.GET, "/api/community/**", "/api/comments/**").permitAll()

                        // 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/user/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/community/**", "/api/comments", "/api/chat", "/api/chat/stt").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()

                        // 토큰 갱신 공개(중복 제거)
                        .requestMatchers("/api/auth/refresh").permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("text/plain;charset=UTF-8");
                            res.getWriter().write("Unauthorized");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("text/plain;charset=UTF-8");
                            res.getWriter().write("Forbidden");
                        })
                )
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://aiinviewer.co.kr",
                "https://www.aiinviewer.co.kr"
        ));
        c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}