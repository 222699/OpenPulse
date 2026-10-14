
package com.openpulse.system.gpu;

import java.util.Locale;

public final class GpuUtilizationProviders {

    private GpuUtilizationProviders() {
    }

    public static GpuUtilizationProvider create() {
        String os = System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT);

        if (os.startsWith("windows")) {
            return new WindowsNvidiaUtilizationProvider();
        }

        return new OshiGpuUtilizationProvider();
    }
}
