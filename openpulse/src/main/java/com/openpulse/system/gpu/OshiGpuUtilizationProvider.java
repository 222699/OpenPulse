
package com.openpulse.system.gpu;

import oshi.hardware.GraphicsCard;
import oshi.hardware.GpuStats;

public class OshiGpuUtilizationProvider
        implements GpuUtilizationProvider {

    @Override
    public double getUtilization(GraphicsCard card, GpuStats stats) {
        return stats.getGpuUtilization();
    }
}
