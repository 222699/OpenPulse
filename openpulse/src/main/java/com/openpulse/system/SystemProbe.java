
package com.openpulse.system;

import oshi.SystemInfo;

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
                                + getAvailableProcessors());

                System.out.println("Maximum JVM Memory: "
                                + runtime.maxMemory() / (1024 * 1024) + " MB");

                System.out.println("Total JVM Memory: "
                                + runtime.totalMemory() / (1024 * 1024) + " MB");

                System.out.println("Free JVM Memory: "
                                + runtime.freeMemory() / (1024 * 1024) + " MB");

                System.out.println("Physical RAM: "
                                + getTotalPhysicalMemory() / (1024 * 1024) + " MB");

                System.out.println("Available Physical RAM: "
                                + getAvailablePhysicalMemory() / (1024 * 1024) + " MB");

                System.out.printf("RAM Usage: %.1f%%%n", getRamUsagePercentage());

                System.out.printf("CPU Usage: %.1f%%%n", getCpuUsagePercentage());
        }

        public static String getOperatingSystem() {
                return System.getProperty("os.name");
        }

        public static int getAvailableProcessors() {
                return Runtime.getRuntime().availableProcessors();
        }

        public static long getTotalPhysicalMemory() {
                SystemInfo systemInfo = new SystemInfo();

                return systemInfo.getHardware()
                                .getMemory()
                                .getTotal();
        }

        public static long getAvailablePhysicalMemory() {
                SystemInfo systemInfo = new SystemInfo();

                return systemInfo.getHardware()
                                .getMemory()
                                .getAvailable();
        }

        public static double getRamUsagePercentage() {
                long total = getTotalPhysicalMemory();
                long available = getAvailablePhysicalMemory();
                long used = total - available;

                return (double) used / total * 100;
        }

        public static double getCpuUsagePercentage() {
                SystemInfo systemInfo = new SystemInfo();

                var processor = systemInfo.getHardware().getProcessor();

                long[] previousTicks = processor.getSystemCpuLoadTicks();

                try {
                        Thread.sleep(1000);
                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return -1;
                }

                return processor.getSystemCpuLoadBetweenTicks(previousTicks) * 100;
        }
}
