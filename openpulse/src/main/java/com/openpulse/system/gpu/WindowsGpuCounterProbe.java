
package com.openpulse.system.gpu;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WindowsGpuCounterProbe {

    private static final String POWERSHELL = """
            $sample = (Get-Counter '\\GPU Engine(*)\\Utilization Percentage' `
                -SampleInterval 1 -MaxSamples 2)[-1]

            $rows = foreach ($counter in $sample.CounterSamples) {
                if ($counter.InstanceName -match
                    'luid_(?<luid>0x[0-9a-f]+_0x[0-9a-f]+)_phys_(?<phys>\\d+)_eng_(?<engine>\\d+)_engtype_3d') {

                    [pscustomobject]@{
                        Adapter = $matches['luid']
                        Engine = '{0}_{1}' -f $matches['phys'], $matches['engine']
                        Usage = [double]$counter.CookedValue
                    }
                }
            }

            $engineTotals = $rows |
                Group-Object Adapter, Engine |
                ForEach-Object {
                    [pscustomobject]@{
                        Adapter = $_.Group[0].Adapter
                        Engine = $_.Group[0].Engine
                        Usage = ($_.Group | Measure-Object Usage -Sum).Sum
                    }
                }

            $engineTotals |
                Group-Object Adapter |
                ForEach-Object {
                    $busy = ($_.Group | Measure-Object Usage -Maximum).Maximum
                    '{0}={1}' -f $_.Group[0].Adapter, [math]::Round($busy, 1)
                }
            """;

    public Map<String, Double> readBusiest3DEngine() {
        Map<String, Double> results = new HashMap<>();

        Process process = null;

        try {
            process = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    "-Command",
                    POWERSHELL)
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);

            if (!finished) {
                System.out.println("GPU probe: PowerShell timed out.");
                process.destroyForcibly();
                return results;
            }

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);

            System.out.println("GPU probe exit code: " + process.exitValue());
            System.out.println("GPU probe raw output:");
            System.out.println(output);

            if (process.exitValue() != 0) {
                return results;
            }

            for (String line : output.split("\\R")) {
                String[] parts = line.trim().split("=", 2);

                if (parts.length != 2) {
                    continue;
                }

                try {
                    results.put(
                            parts[0].trim().toLowerCase(Locale.ROOT),
                            Double.parseDouble(parts[1].trim()));
                } catch (NumberFormatException ignored) {
                    // Ignore output that is not an adapter reading.
                }
            }

        } catch (IOException e) {
            System.out.println("GPU probe: Could not start PowerShell: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("GPU probe: Interrupted while waiting for PowerShell.");
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }

        return results;
    }

    public static void main(String[] args) {
        WindowsGpuCounterProbe probe = new WindowsGpuCounterProbe();

        System.out.println("=== OpenPulse Windows GPU Counter Test ===");

        Map<String, Double> readings = probe.readBusiest3DEngine();

        if (readings.isEmpty()) {
            System.out.println("No readings returned.");
            return;
        }

        readings.forEach((adapter, usage) ->
                System.out.printf("%s: %.1f%%%n", adapter, usage));
    }
}
