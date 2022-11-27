import dev.JustRed23.abcm.Config;
import dev.JustRed23.abcm.exception.ConfigInitException;
import dev.JustRed23.stonebrick.cache.types.ExpiringCache;
import dev.JustRed23.stonebrick.cache.types.LRUCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CacheTest {

    @Test
    void testLRUCache() throws ConfigInitException {
        Config.init();
        LRUCache<String, String> cache = new LRUCache<>(1, TimeUnit.MINUTES, 10);

        for (int i = 0; i < 20; i++)
            cache.put(String.valueOf(i), "hello world x" + i);

        assertEquals(10, cache.size());
    }

    @Test
    void testExpiringCache() throws ConfigInitException, InterruptedException {
        Config.init();
        ExpiringCache<String, String> cache = new ExpiringCache<>(10, TimeUnit.SECONDS);

        cache.put("Hello", "World");
        assertEquals("World", cache.get("Hello").orElseThrow());

        TimeUnit.SECONDS.sleep(10);

        assertFalse(cache.get("Hello").isPresent());
    }
}