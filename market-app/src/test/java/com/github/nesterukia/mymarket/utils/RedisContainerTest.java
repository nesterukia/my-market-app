package com.github.nesterukia.mymarket.utils;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.utility.DockerImageName;

public class RedisContainerTest {

    protected static RedisContainer redisContainer;
    private static final String REDIS_PASSWORD = "secret1234";

    @BeforeAll
    protected static void init() {
        redisContainer = new RedisContainer(DockerImageName.parse("redis:7.4.2-bookworm"))
                .withEnv("REDIS_PASSWORD", REDIS_PASSWORD);
        redisContainer.start();
    }

    @DynamicPropertySource
    protected static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getRedisHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getRedisPort());
        registry.add("spring.data.redis.password", () -> REDIS_PASSWORD);

        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://%s:%d/%s".formatted(
                        "localhost",
                        5432,
                        "mock-db-name"
                )
        );
        registry.add("spring.r2dbc.username", () -> "postgres");
        registry.add("spring.r2dbc.password", () -> "postgres");
        registry.add("spring.r2dbc.pool.enabled", () -> "false");
        registry.add("spring.sql.init.mode", () -> "always");
    }
}
