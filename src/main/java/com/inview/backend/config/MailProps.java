// MailProps.java
package com.inview.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.mail")
public class MailProps {
    private String host;
    private int port;
    private String username;
    private String password;
    private String from;
    private String fromName;
}
