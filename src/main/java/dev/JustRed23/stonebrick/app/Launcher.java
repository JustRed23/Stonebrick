package dev.JustRed23.stonebrick.app;

import dev.JustRed23.abcm.Config;
import dev.JustRed23.stonebrick.cfg.LogLevelParser;
import dev.JustRed23.stonebrick.data.FileStructure;
import dev.JustRed23.stonebrick.log.SBLogger;
import dev.JustRed23.stonebrick.net.NetworkManager;
import dev.JustRed23.stonebrick.util.Args;
import dev.JustRed23.stonebrick.util.CommonThreads;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class Launcher {

    private static Logger LOGGER;
    private static final AtomicBoolean started = new AtomicBoolean(false);
    private static final CountDownLatch launchLatch = new CountDownLatch(1);
    private static volatile RuntimeException launchException = null;

    //Inspired by com.sun.javafx.application.Application.launch()
    static void launchApplication(Class<? extends Application> appClass, String[] args) {
        if (started.getAndSet(true))
            throw new RuntimeException("Error: Application already started");

        Thread launchThread = new Thread(() -> {
            try {
                launch(appClass, args);
            } catch (RuntimeException rte) {
                launchException = rte;
            } catch (Exception ex) {
                launchException = new RuntimeException("Application launch exception", ex);
            } catch (Error err) {
                launchException = new RuntimeException("Application launch error", err);
            } finally {
                launchLatch.countDown();
            }
        });
        launchThread.setName("Application Launcher");
        launchThread.start();

        try {
            launchLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Application launch interrupted", e);
        }

        if (launchException != null)
            throw launchException;
    }

    private static volatile boolean error = false;
    private static volatile Throwable
            constructorError = null,
            initError = null,
            startError = null,
            stopError = null;
    private static volatile CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static volatile AtomicBoolean exitCalled = new AtomicBoolean(false);

    private static void launch(Class<? extends Application> appClass, String[] args) throws Exception {
        Config.addParser(LogLevelParser.class);
        Config.addScannable("dev.JustRed23");
        Config.init();
        LOGGER = SBLogger.getLogger(appClass);
        LOGGER.info("Launching application {}", appClass.getName());

        AtomicBoolean startCalled = new AtomicBoolean(false);

        Runtime.getRuntime().addShutdownHook(new Thread(Application::exit));

        try {
            AtomicReference<Application> app = new AtomicReference<>();
            if (!exitCalled.get()) {
                Application.runAndWait(() -> {
                    try {
                        Constructor<? extends Application> c = appClass.getConstructor();
                        app.set(c.newInstance());
                        app.get().setArgs(Args.from(args));
                    } catch (Throwable t) {
                        LOGGER.warn("Exception in Application constructor");
                        constructorError = t;
                        error = true;
                    }
                });
            }

            Application application = app.get();

            if (!error && !exitCalled.get()) {
                try {
                    application.init();
                    Config.rescan(false);
                } catch (Throwable t) {
                    LOGGER.warn("Exception in Application init method");
                    initError = t;
                    error = true;
                }
            }

            FileStructure.init();

            if (!error && !exitCalled.get()) {
                try {
                    startCalled.set(true);
                    application.start();
                    application.getServicePool().start();
                } catch (Throwable t) {
                    LOGGER.warn("Exception in Application start method");
                    startError = t;
                    error = true;
                }
            }

            //Wait for the app to shut down successfully
            if (!error)
                shutdownLatch.await();

            LOGGER.info("Application {} shutting down", appClass.getName());

            //If the application actually started, stop it
            if (startCalled.get()) {
                Application.runAndWait(() -> {
                    try {
                        application.stop();
                        application.getServicePool().shutdown();
                        NetworkManager.cancelAll();
                    } catch (Throwable t) {
                        LOGGER.warn("Exception in Application stop method");
                        stopError = t;
                        error = true;
                    }
                });
            }

            if (error) {
                if (constructorError != null) {
                    String msg = "Unable to construct Application instance: " + appClass;
                    throw new RuntimeException(msg, constructorError);
                } else if (initError != null) {
                    String msg = "Exception in Application init method";
                    throw new RuntimeException(msg, initError);
                } else if(startError != null) {
                    String msg = "Exception in Application start method";
                    throw new RuntimeException(msg, startError);
                } else if (stopError != null) {
                    String msg = "Exception in Application stop method";
                    throw new RuntimeException(msg, stopError);
                }
            }
        } finally {
            CommonThreads.shutdown();
            Config.destroy();
        }
    }

    static void stop() {
        exitCalled.set(true);
        shutdownLatch.countDown();
    }
}