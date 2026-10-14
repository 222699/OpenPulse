
package com.openpulse.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import oshi.hardware.GpuTicks;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.GpuStats;

import com.openpulse.system.gpu.GpuUtilizationProvider;
import com.openpulse.system.gpu.GpuUtilizationProviders;

public class GpuProbe implements AutoCloseable {

    private final List<GpuDevice> devices = new ArrayList<>();
    private final GpuUtilizationProvider utilizationProvider = GpuUtilizationProviders.create();

    public GpuProbe() {
        SystemInfo systemInfo = new SystemInfo();

        for (GraphicsCard card : systemInfo.getHardware().getGraphicsCards()) {

            GpuStats stats = card.createStatsSession();

            // Prime metrics that need a previous sample.
            stats.getGpuUtilization();
            stats.getPowerDraw();

            devices.add(new GpuDevice(card, stats));
        }
    }

    public synchronized List<GpuReading> read() {
        List<GpuReading> readings = new ArrayList<>();

        for (GpuDevice device : devices) {
            GpuStats stats = device.stats;

            double utilization = utilizationProvider.getUtilization(device.card, stats);

            readings.add(new GpuReading(
                    device.card.getName(),
                    device.card.getVendor(),
                    device.card.getVRam(),
                    stats.getVramUsed(),
                    utilization,
                    stats.getTemperature(),
                    stats.getPowerDraw(),
                    stats.getCoreClockMhz()));
        }

        return Collections.unmodifiableList(readings);
    }

    @Override
    public synchronized void close() {
        for (GpuDevice device : devices) {
            device.stats.close();
        }

        devices.clear();
    }

    private static class GpuDevice {
        private final GraphicsCard card;
        private final GpuStats stats;

        private GpuDevice(GraphicsCard card, GpuStats stats) {
            this.card = card;
            this.stats = stats;
        }
    }

    public record GpuReading(
            String name,
            String vendor,
            long totalVramBytes,
            long usedVramBytes,
            double utilization,
            double temperatureCelsius,
            double powerWatts,
            long coreClockMhz) {
    }
}
