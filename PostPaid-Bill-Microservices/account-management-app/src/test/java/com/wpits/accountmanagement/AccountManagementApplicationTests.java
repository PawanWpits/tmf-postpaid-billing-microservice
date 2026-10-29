package com.wpits.accountmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountManagementApplicationTests {

    @Test
    void contextLoads() {
        // Requires a reachable MySQL instance (see application-dev.properties);
        // Kafka's producer factory is lazy and does not need a live broker to start.
    }
}
