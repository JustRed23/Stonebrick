package dev.JustRed23.stonebrick.log;

import dev.JustRed23.abcm.ConfigField;
import dev.JustRed23.abcm.Configurable;

@Configurable
public final class FallbackConfig {

    @ConfigField(defaultValue = "true")
    public static boolean SHOW_LOG_NAME;

    @ConfigField(defaultValue = "true")
    public static boolean SHOW_DATE_TIME;

    @ConfigField(defaultValue = "false")
    public static boolean SHOW_THREAD_NAME;

    @ConfigField(defaultValue = "hh:mm:ss.sss")
    public static String DATE_TIME_FORMAT;

    @ConfigField(defaultValue = "System.out")
    public static String LOG_LOCATION;

    @ConfigField(defaultValue = "INFO", optional = true)
    public static LogLevel DEFAULT_LOG_LEVEL;
}