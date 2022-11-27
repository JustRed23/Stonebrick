import dev.JustRed23.abcm.Config;
import dev.JustRed23.stonebrick.app.Application;

public class ApplicationTest$TestApp extends Application {


    protected void init() throws Exception {
        System.out.println("App init");
        Config.addScannable("net.packagethatdoesnotexist.test");
    }

    protected void start() throws Exception {
        System.out.println("App start");
        getServicePool().addService(TestService.class);
    }

    protected void stop() throws Exception {
        System.out.println("App stop");
    }
}
