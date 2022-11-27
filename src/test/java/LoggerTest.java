import dev.JustRed23.abcm.Config;
import dev.JustRed23.abcm.exception.ConfigInitException;
import dev.JustRed23.stonebrick.log.SBLogger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoggerTest {

    @Test
    void testLogger() throws ConfigInitException {
        Config.init();
        final Logger logger = SBLogger.getLogger(getClass());
        logger.info("Hello world!");
        assertNotNull(logger);
    }
}