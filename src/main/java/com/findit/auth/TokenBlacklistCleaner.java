package com.findit.auth;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Scheduled job that purges blacklist rows whose tokens have expired anyway. */
@Component
public class TokenBlacklistCleaner {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistCleaner.class);

    private final TokenBlacklistRepository repository;

    public TokenBlacklistCleaner(TokenBlacklistRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "${findit.jwt.blacklist-cleanup-ms:3600000}")
    @Transactional
    public void purgeExpired() {
        int removed = repository.deleteExpired(Instant.now());
        if (removed > 0) {
            log.info("Purged {} expired blacklisted tokens", removed);
        }
    }
}
