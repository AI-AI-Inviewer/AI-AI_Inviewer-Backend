// com/inview/backend/service/EmailService.java
package com.inview.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendVerificationEmail(String to, String verifyLink) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(from);
            h.setTo(to);
            h.setSubject("[AI-Inviewer] 이메일 인증을 완료해 주세요");
            String html = """
                <div style="font-family:sans-serif">
                  <h2>이메일 인증</h2>
                  <p>아래 버튼을 눌러 인증을 완료해 주세요 (유효기간 15분):</p>
                  <p><a href="%s" style="background:#4f46e5;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px">이메일 인증하기</a></p>
                  <p>버튼이 동작하지 않으면 이 링크를 브라우저 주소창에 붙여넣기:<br>%s</p>
                </div>
                """.formatted(verifyLink, verifyLink);
            h.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    public void sendEmailCode(String to, String code) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(from);
            h.setTo(to);
            h.setSubject("[AI-Inviewer] 이메일 인증 코드");
            String html = """
                <div style="font-family:sans-serif">
                  <h2>이메일 인증 코드</h2>
                  <p>아래 인증 코드를 회원가입 화면에 입력해 주세요.</p>
                  <p style="font-size:20px;letter-spacing:2px;"><b>%s</b></p>
                  <p>유효기간: 10분</p>
                </div>
                """.formatted(code);
            h.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 전송 실패", e);
        }
    }
}
