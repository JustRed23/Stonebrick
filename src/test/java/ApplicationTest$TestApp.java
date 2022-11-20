import dev.JustRed23.stonebrick.app.Application;
import dev.JustRed23.stonebrick.cfg.Config;

public class ApplicationTest$TestApp extends Application {


    protected void init() throws Exception {
        System.out.println("App init");
        Config.addScannablePackage("net.packagethatdoesnotexist.test");
    }

    protected void start() throws Exception {
        System.out.println("App start");
        getServicePool().addService(TestService.class);
    }

    protected void stop() throws Exception {
        System.out.println("App stop");
    }
}
