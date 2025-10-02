// MailConfig.java
package com.inview.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProps.class)
@RequiredArgsConstructor
public class MailConfig {

    private final MailProps props;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl s = new JavaMailSenderImpl();
        s.setHost(props.getHost());
        s.setPort(props.getPort());
        s.setUsername(props.getUsername());
        s.setPassword(props.getPassword());
        s.setDefaultEncoding(StandardCharsets.UTF_8.name());
        Properties p = s.getJavaMailProperties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.ssl.trust", props.getHost()); // 예: mail
        p.put("mail.smtp.connectiontimeout", "5000");
        p.put("mail.smtp.timeout", "5000");
        p.put("mail.smtp.writetimeout", "5000");
        return s;
    }
}
