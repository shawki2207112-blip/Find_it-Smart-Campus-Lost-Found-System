package com.findit.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stand-in for a third-party mail library with its own, different API (to / subject / body instead of our
 * Notification). It only logs, so no mail server is needed for the lab.
 */
@Component
public class EmailClient {

    private static final Logger log = LoggerFactory.getLogger(EmailClient.class);

    public void sendMail(String toAddress, String subject, String body) {
        log.info("[fake e-mail] to={} subject={} body={}", toAddress, subject, body);
    }
}
