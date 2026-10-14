
package com.openpulse.system.gpu;

import oshi.hardware.GraphicsCard;
import oshi.hardware.GpuStats;

public interface GpuUtilizationProvider {

    double getUtilization(GraphicsCard card, GpuStats stats);
}
