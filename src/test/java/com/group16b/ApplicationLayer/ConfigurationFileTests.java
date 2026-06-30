package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;

class ConfigurationFileTests {

    @Test
    void givenValidSystemConfig_whenContextStarts_thenConfigValuesAreLoaded() {
        assertDoesNotThrow(() -> {
            try (AnnotationConfigApplicationContext context = createContextWith("system-config.properties")) {
                ConfigProbe probe = context.getBean(ConfigProbe.class);
                probe.validate();
            }
        });
    }

    @Test
    void givenInvalidSystemConfig_whenContextStarts_thenContextFailsToStart() {
        assertThrows(Exception.class, () -> {
            try (AnnotationConfigApplicationContext ignored = createContextWith("invalid-system-config.properties")) {
                // Context startup should fail before this point.
            }
        });
    }

    private AnnotationConfigApplicationContext createContextWith(String configFileName) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(
                new ResourcePropertySource(new ClassPathResource(configFileName))
        );
        context.register(TestConfig.class);
        context.refresh();
        return context;
    }

    @Configuration
    static class TestConfig {
        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        ConfigProbe configProbe(
                @Value("${external.wsep.base-url}") String wsepBaseUrl,
                @Value("${startup.validate-external-systems}") boolean validateExternalSystems,
                @Value("${system.default-admin.username}") String defaultAdminUsername,
                @Value("${system.default-admin.password}") String defaultAdminPassword,
                @Value("${system.default-admin.email}") String defaultAdminEmail,
                @Value("${virtual-queue.default-pass-num}") int defaultQueuePassNum,
                @Value("${virtual-queue.pass-timeout-ms}") int queuePassTimeoutMs,
                @Value("${jwt.admin-secret}") String jwtAdminSecret,
                @Value("${jwt.user-expiration-ms}") long jwtUserExpirationMs,
                @Value("${jwt.admin-expiration-ms}") long jwtAdminExpirationMs) {
            return new ConfigProbe(
                    wsepBaseUrl,
                    validateExternalSystems,
                    defaultAdminUsername,
                    defaultAdminPassword,
                    defaultAdminEmail,
                    defaultQueuePassNum,
                    queuePassTimeoutMs,
                    jwtAdminSecret,
                    jwtUserExpirationMs,
                    jwtAdminExpirationMs
            );
        }
    }

    record ConfigProbe(
            String wsepBaseUrl,
            boolean validateExternalSystems,
            String defaultAdminUsername,
            String defaultAdminPassword,
            String defaultAdminEmail,
            int defaultQueuePassNum,
            int queuePassTimeoutMs,
            String jwtAdminSecret,
            long jwtUserExpirationMs,
            long jwtAdminExpirationMs
    ) {
        void validate() {
            if (wsepBaseUrl == null || wsepBaseUrl.isBlank()) {
                throw new IllegalStateException("external.wsep.base-url is missing");
            }
            if (defaultAdminUsername == null || defaultAdminUsername.isBlank()) {
                throw new IllegalStateException("system.default-admin.username is missing");
            }
            if (defaultAdminPassword == null || defaultAdminPassword.isBlank()) {
                throw new IllegalStateException("system.default-admin.password is missing");
            }
            if (defaultAdminEmail == null || defaultAdminEmail.isBlank()) {
                throw new IllegalStateException("system.default-admin.email is missing");
            }
            if (defaultQueuePassNum <= 0) {
                throw new IllegalStateException("virtual-queue.default-pass-num must be positive");
            }
            if (queuePassTimeoutMs <= 0) {
                throw new IllegalStateException("virtual-queue.pass-timeout-ms must be positive");
            }
            if (jwtAdminSecret == null || jwtAdminSecret.length() < 32) {
                throw new IllegalStateException("jwt.admin-secret must be at least 32 characters");
            }
            if (jwtUserExpirationMs <= 0) {
                throw new IllegalStateException("jwt.user-expiration-ms must be positive");
            }
            if (jwtAdminExpirationMs <= 0) {
                throw new IllegalStateException("jwt.admin-expiration-ms must be positive");
            }
        }
    }
}