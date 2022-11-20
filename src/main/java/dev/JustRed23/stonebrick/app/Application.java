package dev.JustRed23.stonebrick.app;

import dev.JustRed23.stonebrick.service.ServicePool;
import dev.JustRed23.stonebrick.util.Args;
import dev.JustRed23.stonebrick.util.CommonThreads;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

/**
 * The entry point of every application made by Stonebrick. This abstract class provides three easy to use methods to start developing your application.
 * <p>
 * {@link #init()} is generally used to initialize parts of your application. Like database connections, services, or other things that need to be initialized.
 * <p>
 * {@link #start()} is called when the initialization is done. This is where you start your application.
 * <p>
 * {@link #stop()} is called when you call {@link #exit()}. This is where you close your database connections, or other things that need to be closed before exiting cleanly.
 * <p>
 * NOTE: you need to call {@link #launch(String[])} or {@link #launch(Class, String[])} to start your application.
 */
public abstract class Application {

    private Args args;
    private final ServicePool servicePool = new ServicePool(this);

    protected abstract void init() throws Exception;
    protected abstract void start() throws Exception;
    protected abstract void stop() throws Exception;

    /**
     * Launches the application.
     * @param appClass The class of the application.
     * @param args The arguments that are passed to the application. Usually the args from the main method.
     * @throws RuntimeException If the application is already running.
     */
    public static void launch(Class<? extends Application> appClass, String[] args) {
        Launcher.launchApplication(appClass, args);
    }

    /**
     * Launches the application.
     * @param args The arguments that are passed to the application. Usually the args from the main method.
     * @throws RuntimeException If the application is already running.
     */
    public static void launch(String[] args) {
        StackTraceElement[] tree = Thread.currentThread().getStackTrace();
        boolean found = false;
        String callingClassName = null;

        for (StackTraceElement se : tree) {
            String className = se.getClassName();
            String methodName = se.getMethodName();
            if (found) {
                callingClassName = className;
                break;
            } else if (Application.class.getName().equals(className) && "launch".equals(methodName))
                found = true;
        }

        if (callingClassName == null)
            throw new RuntimeException("Error: unable to determine Application class");

        try {
            Class<?> theClass = Class.forName(callingClassName, false, Thread.currentThread().getContextClassLoader());
            if (Application.class.isAssignableFrom(theClass))
                launch((Class<? extends Application>) theClass, args);
            else
                throw new RuntimeException("Error: " + theClass + " does not extend " + Application.class.getCanonicalName());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This method will release the shutdown latch and call {@link #stop()} if no errors occurred beforehand.
     */
    public static void exit() {
        Launcher.stop();
    }

    //Runnables
    private static final Object runLock = new Object();

    /**
     * Runs the given runnable on the application thread and waits for it to finish.
     * @param runnable The runnable to run.
     */
    public static void runAndWait(@NotNull Runnable runnable) {
        final CountDownLatch doneLatch = new CountDownLatch(1);
        runLater(() -> {
            try {
                runnable.run();
            } finally {
                doneLatch.countDown();
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Submits the given runnable to the application thread to run after all tasks before it are done.
     * @param runnable The runnable to run.
     */
    public static void runLater(@NotNull Runnable runnable) {
        synchronized (runLock) {
            CommonThreads.appThread.submit(runnable::run);
        }
    }

    //GETTERS
    void setArgs(Args args) {
        this.args = args;
    }

    public Args getArgs() {
        return args;
    }

    public ServicePool getServicePool() {
        return servicePool;
    }
}