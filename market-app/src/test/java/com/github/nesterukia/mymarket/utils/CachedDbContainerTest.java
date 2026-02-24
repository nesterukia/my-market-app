package com.github.nesterukia.mymarket.utils;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class CachedDbContainerTest {

    protected static PostgreSQLContainer<?> postgres;
    protected static RedisContainer redisContainer;
    private static final String REDIS_PASSWORD = "secret1234";

    @BeforeAll
    protected static void init() {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("test-market-db")
                .withUsername("postgres")
                .withPassword("postgres")
                .withReuse(true);
        postgres.start();

        redisContainer = new RedisContainer(DockerImageName.parse("redis:7.4.2-bookworm"))
                .withEnv("REDIS_PASSWORD", REDIS_PASSWORD);
        redisContainer.start();
    }

    @DynamicPropertySource
    protected static void registerR2dbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://%s:%d/%s".formatted(
                        postgres.getHost(),
                        postgres.getMappedPort(5432),
                        postgres.getDatabaseName()
                )
        );
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.r2dbc.pool.enabled", () -> "false");
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @DynamicPropertySource
    protected static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getRedisHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getRedisPort());
        registry.add("spring.data.redis.password", () -> REDIS_PASSWORD);
    }

    @DynamicPropertySource
    protected static void registerMandatoryProperties(DynamicPropertyRegistry registry) {
        registry.add("paymentService.baseUrl", () -> "http://payment-service:8081");
    }
}
