
package com.openpulse.system;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.util.GlobalConfig;

public class SystemProbe {
        static {
                GlobalConfig.set("oshi.os.windows.cpu.utility", true);
        }
        private static final SystemInfo SYSTEM_INFO = new SystemInfo();
        private static final CentralProcessor PROCESSOR = SYSTEM_INFO.getHardware().getProcessor();

        private static long[] previousCpuTicks = PROCESSOR.getSystemCpuLoadTicks();

        public static String getOperatingSystem() {
                return System.getProperty("os.name");
        }

        public static String getOSVersion() {
                return System.getProperty("os.version");
        }

        public static String getJavaVersion() {
                return System.getProperty("java.version");
        }

        public static int getAvailableProcessors() {
                return Runtime.getRuntime().availableProcessors();
        }

        public static long getTotalPhysicalMemory() {
                return SYSTEM_INFO.getHardware().getMemory().getTotal();
        }

        public static long getAvailablePhysicalMemory() {
                return SYSTEM_INFO.getHardware().getMemory().getAvailable();
        }

        public static double getRamUsagePercentage() {
                long total = getTotalPhysicalMemory();
                long available = getAvailablePhysicalMemory();

                if (total <= 0) {
                        return -1;
                }

                double used = total - available;
                return Math.round(used / total * 1000) / 10.0;
        }

        public static synchronized double getCpuUsagePercentage() {
                double load = PROCESSOR.getSystemCpuLoadBetweenTicks(
                                previousCpuTicks);

                previousCpuTicks = PROCESSOR.getSystemCpuLoadTicks();

                if (load < 0) {
                        return -1;
                }

                return Math.round(load * 1000) / 10.0;
        }

        public static void main(String[] args) {
                System.out.println("=== OpenPulse System Probe ===");
                System.out.println("Operating System: " + getOperatingSystem());
                System.out.println("OS Version: " + getOSVersion());
                System.out.println("Java Version: " + getJavaVersion());
                System.out.println("Available Processors: " + getAvailableProcessors());

                System.out.println("Physical RAM: "
                                + getTotalPhysicalMemory() / (1024 * 1024) + " MB");

                System.out.println("Available Physical RAM: "
                                + getAvailablePhysicalMemory() / (1024 * 1024) + " MB");

                System.out.printf("RAM Usage: %.1f%%%n", getRamUsagePercentage());
                System.out.printf("CPU Usage: %.1f%%%n", getCpuUsagePercentage());
        }
}
