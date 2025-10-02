// com/inview/backend/service/EmailService.java
package com.inview.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@aiinviewer.co.kr}")
    private String from;

    @Value("${app.mail.from-name:AI Interviewer}")
    private String fromName;

    public void sendEmailCode(String to, String code) {
        try {
            var msg = mailSender.createMimeMessage();
            var h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(from, fromName);
            h.setTo(to);
            h.setSubject("[AI Interviewer] 이메일 인증 코드");
            h.setText(buildHtml(code), true);
            mailSender.send(msg);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("메일 전송 실패", e);
        }
    }

    private String buildHtml(String code) {
        return """
            <div style="font-family:pretendard,Arial,sans-serif">
              <h2>이메일 인증</h2>
              <p>아래 인증 코드를 10분 이내에 입력하세요.</p>
              <div style="font-size:28px;font-weight:700;letter-spacing:2px">%s</div>
            </div>
        """.formatted(code);
    }
}
