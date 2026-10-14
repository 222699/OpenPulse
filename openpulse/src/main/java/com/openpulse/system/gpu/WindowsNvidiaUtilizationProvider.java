
package com.openpulse.system.gpu;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import oshi.hardware.GraphicsCard;
import oshi.hardware.GpuStats;

public class WindowsNvidiaUtilizationProvider
        implements GpuUtilizationProvider {

    private final OshiGpuUtilizationProvider fallback =
            new OshiGpuUtilizationProvider();

    @Override
    public double getUtilization(GraphicsCard card, GpuStats stats) {
        if (!isNvidia(card)) {
            return -1;
        }

        double nvidiaUsage = readNvidiaSmiUtilization(card.getName());

        if (nvidiaUsage >= 0) {
            return nvidiaUsage;
        }

        return fallback.getUtilization(card, stats);
    }

    private boolean isNvidia(GraphicsCard card) {
        String vendor = card.getVendor();
        String name = card.getName();

        return (vendor != null
                    && vendor.toLowerCase(Locale.ROOT).contains("nvidia"))
                || (name != null
                    && name.toLowerCase(Locale.ROOT).contains("nvidia"));
    }

    private double readNvidiaSmiUtilization(String gpuName) {
        Process process = null;

        try {
            process = new ProcessBuilder(
                    "nvidia-smi",
                    "--query-gpu=name,utilization.gpu",
                    "--format=csv,noheader,nounits")
                    .redirectErrorStream(true)
                    .start();

            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return -1;
            }

            if (process.exitValue() != 0) {
                return -1;
            }

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);

            for (String line : output.split("\\R")) {
                String[] columns = line.split(",", 2);

                if (columns.length != 2) {
                    continue;
                }

                String name = columns[0].trim();
                if (!name.equalsIgnoreCase(gpuName)) {
                    continue;
                }

                try {
                    double usage = Double.parseDouble(columns[1].trim());

                    if (Double.isFinite(usage) && usage >= 0 && usage <= 100) {
                        return usage;
                    }
                } catch (NumberFormatException ignored) {
                    // Try another matching line, if present.
                }
            }
        } catch (IOException e) {
            // nvidia-smi is unavailable; use the fallback provider.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }

        return -1;
    }
}
