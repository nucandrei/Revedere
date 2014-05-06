package org.nuc.revedere.client.util;

public class NetworkUtils {
    public static final int MINIMUM_PORT_VALUE = 1025;
    public static final int MAXIMUM_PORT_VALUE = 65535;

    public static boolean validAddress(String address) {
        if (address == null) {
            return false;
        }

        if (address.matches("(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}:[1-9]([0-9]){0,4}")) {
            return true;
        } else {
            return false;
        }
    }

    public static String extractIP(String address) throws Exception {
        if (!validAddress(address)) {
            throw new Exception("Invalid address");
        } else {
            return address.split(":")[0];
        }
    }

    public static int extractPort(String address) throws Exception {
        if (!validAddress(address)) {
            throw new Exception("Invalid address");
        } else {
            Integer port = Integer.parseInt(address.split(":")[1]);
            if (port < MINIMUM_PORT_VALUE || port > MAXIMUM_PORT_VALUE) {
                throw new Exception("Port out of range");
            }
            return port;
        }
    }

    private NetworkUtils() {
        // empty constructor. It protects from creating instances of NetworkUtils
    }
}
