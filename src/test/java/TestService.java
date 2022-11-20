import dev.JustRed23.stonebrick.app.Application;
import dev.JustRed23.stonebrick.service.Service;

import java.util.concurrent.TimeUnit;

public class TestService extends Service {

    private int count = 0;
    public boolean shouldRun() {
        return true;
    }

    public long delayBetweenRuns() {
        return TimeUnit.SECONDS.toMillis(10);
    }

    public void run() throws Exception {
        System.out.println("Test service ran " + ++count + " time(s)");
        if (count > 2)
            throw new Exception("Test exception");
    }

    public void onError(Exception e) {
        System.out.println("Test service error: " + e.getMessage());
        Application.exit();
    }
}
