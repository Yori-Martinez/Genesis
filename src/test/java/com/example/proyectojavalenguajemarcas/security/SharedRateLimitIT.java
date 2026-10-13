package com.example.proyectojavalenguajemarcas.security;

import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SharedRateLimitIT {
    @BeforeEach void reset() throws Exception { DatabaseFixture.reset(); }

    @Test void concurrentConnectionsCannotExceedSharedLimit() throws Exception {
        String key = "a".repeat(64);
        try (ExecutorService workers = Executors.newFixedThreadPool(8)) {
            List<Callable<Boolean>> requests = new ArrayList<>();
            for (int i = 0; i < 40; i++) requests.add(() -> {
                try (var con = DatabaseFixture.connection()) {
                    return SharedRateLimit.allow(con,key,100,20);
                }
            });
            long accepted = 0;
            for (Future<Boolean> result : workers.invokeAll(requests)) if (result.get()) accepted++;
            assertEquals(20,accepted);
        }
        assertEquals("40",DatabaseFixture.scalar("SELECT attempts FROM genesis_rate_limits WHERE key_hash='"+key+"' AND bucket_id=100"));
        try (var con = DatabaseFixture.connection()) {
            assertFalse(SharedRateLimit.allow(con,key,100,20));
            assertTrue(SharedRateLimit.allow(con,key,101,20));
            assertTrue(SharedRateLimit.allow(con,"b".repeat(64),100,5));
        }
    }
}
