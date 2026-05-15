package com.profit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "app.auth.session-store=memory")
@ActiveProfiles("dev")
class ProfitDecisionSystemApplicationTests {

    @Test
    void contextLoads() {
    }
}
