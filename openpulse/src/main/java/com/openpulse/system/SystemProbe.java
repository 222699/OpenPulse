
package com.openpulse.system;

public class SystemProbe {

    public static void main(String[] args) {
        System.out.println("=== OpenPulse System Probe ===");

        System.out.println("Operating System: "
                + System.getProperty("os.name"));

        System.out.println("OS Version: "
                + System.getProperty("os.version"));

        System.out.println("Java Version: "
                + System.getProperty("java.version"));

        Runtime runtime = Runtime.getRuntime();

        System.out.println("Available Processors: "
                + runtime.availableProcessors());

        System.out.println("Maximum JVM Memory: "
                + runtime.maxMemory() / (1024 * 1024) + " MB");

        System.out.println("Total JVM Memory: "
                + runtime.totalMemory() / (1024 * 1024) + " MB");

        System.out.println("Free JVM Memory: "
                + runtime.freeMemory() / (1024 * 1024) + " MB");
    }
}
