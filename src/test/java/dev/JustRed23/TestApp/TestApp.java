package dev.JustRed23.TestApp;

import dev.JustRed23.abcm.Config;
import dev.JustRed23.stonebrick.app.Application;
import dev.JustRed23.stonebrick.data.FileStructure;

import java.util.concurrent.TimeUnit;

public class TestApp extends Application {

    protected void init() throws Exception {
        System.out.println("Init");
        FileStructure.discover(TestFileStructure.class);
    }

    protected void start() throws Exception {
        System.out.println("Started");
        TimeUnit.SECONDS.sleep(10);
        TestFileStructure.fruits.delete();
        exit();
    }

    protected void stop() throws Exception {
        System.out.println("Stopping");
    }

    public static void main(String[] args) {
        Config.setDebug(true);
        launch(args);
    }
}
