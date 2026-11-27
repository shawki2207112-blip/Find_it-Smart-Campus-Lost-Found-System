package com.findit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables @Scheduled jobs (blacklist purge). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
