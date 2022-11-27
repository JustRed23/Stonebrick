package dev.JustRed23.stonebrick.net;

import dev.JustRed23.abcm.ConfigField;
import dev.JustRed23.abcm.Configurable;

@Configurable
public final class NetworkConfig {

    @ConfigField(defaultValue = "JustRed23/StoneBrick")
    public static String USER_AGENT;

    @ConfigField(defaultValue = "10000", optional = true)
    public static int MAX_CONNECT_TIMEOUT;

    @ConfigField(defaultValue = "10000", optional = true)
    public static int MAX_READ_TIMEOUT;

    @ConfigField(defaultValue = "4096", optional = true)
    public static int BUFFER_SIZE;
}