
package com.openpulse.app;

import com.openpulse.system.SystemProbe;
import java.util.List;
import com.openpulse.system.GpuProbe;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OpenPulseApp extends Application {

        private static final String BACKGROUND = "#101216";
        private static final String PANEL = "#191c23";
        private static final String TEXT = "#f0f2f5";
        private static final String MUTED = "#9299a8";
        private static final String ACCENT = "#65e6a5";

        // These labels stay accessible so we can update their readings.
        private final Label cpuReading = new Label("Loading...");
        private final Label ramReading = new Label("Loading...");
        private final Label systemReading = new Label("Loading...");
        private final Label gpuReading = new Label("Loading...");
        private final Label gpuDetail = new Label("Detecting graphics...");
        private GpuProbe gpuProbe;

        // Collects hardware readings away from the JavaFX UI thread.
        private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "OpenPulse-Monitor");
                thread.setDaemon(true);
                return thread;
        });

        @Override
        public void start(Stage stage) {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: " + BACKGROUND + ";");

                // Sidebar
                VBox sidebar = new VBox(12);
                sidebar.setPadding(new Insets(28, 16, 20, 16));
                sidebar.setPrefWidth(205);
                sidebar.setStyle("-fx-background-color: " + PANEL + ";");

                Label logo = new Label("◉  OpenPulse");
                logo.setStyle(
                                "-fx-text-fill: " + ACCENT + ";" +
                                                "-fx-font-size: 21px;" +
                                                "-fx-font-weight: bold;");

                Label navHeading = new Label("MONITOR");
                navHeading.setStyle(
                                "-fx-text-fill: " + MUTED + ";" +
                                                "-fx-font-size: 10px;" +
                                                "-fx-font-weight: bold;");

                Button dashboardButton = navButton("Dashboard", true);
                Button hardwareButton = navButton("Hardware", false);
                Button diagnosticsButton = navButton("Diagnostics", false);
                Button settingsButton = navButton("Settings", false);

                Region spacer = new Region();
                VBox.setVgrow(spacer, Priority.ALWAYS);

                Label footer = new Label("Open-source hardware monitor");
                footer.setWrapText(true);
                footer.setStyle(
                                "-fx-text-fill: " + MUTED + "; -fx-font-size: 10px;");

                sidebar.getChildren().addAll(
                                logo,
                                new Region(),
                                navHeading,
                                dashboardButton,
                                hardwareButton,
                                diagnosticsButton,
                                spacer,
                                settingsButton,
                                footer);

                root.setLeft(sidebar);

                // Main dashboard
                VBox content = new VBox(24);
                content.setPadding(new Insets(32));
                content.setFillWidth(true);

                Label pageTitle = new Label("Dashboard");
                pageTitle.setStyle(
                                "-fx-text-fill: " + TEXT + ";" +
                                                "-fx-font-size: 28px;" +
                                                "-fx-font-weight: bold;");

                Label subtitle = new Label("Know your machine.");
                subtitle.setStyle(
                                "-fx-text-fill: " + MUTED + "; -fx-font-size: 13px;");

                VBox heading = new VBox(5, pageTitle, subtitle);

                Label status = new Label("●  SYSTEM ONLINE");
                status.setStyle(
                                "-fx-text-fill: " + ACCENT + ";" +
                                                "-fx-font-size: 11px;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-background-color: #20372d;" +
                                                "-fx-background-radius: 6px;" +
                                                "-fx-padding: 10px;");

                Region headingSpacer = new Region();
                HBox.setHgrow(headingSpacer, Priority.ALWAYS);

                HBox topBar = new HBox(16, heading, headingSpacer, status);
                topBar.setAlignment(Pos.CENTER_LEFT);

                // Live metric cards
                HBox metrics = new HBox(16,
                                metricCard("PROCESSOR", cpuReading, "CPU Usage"),
                                metricCard("MEMORY", ramReading, "Physical RAM usage"),
                                metricCard("GRAPHICS", gpuReading, gpuDetail),
                                metricCard("SYSTEM", systemReading, "Operating system"));

                for (javafx.scene.Node node : metrics.getChildren()) {
                        HBox.setHgrow(node, Priority.ALWAYS);
                }

                VBox overview = new VBox(12);

                Label overviewTitle = sectionTitle("System overview");

                Label overviewText = new Label(
                                "Your hardware information and live performance readings " +
                                                "will appear here as we connect the monitoring engine.");
                overviewText.setWrapText(true);
                overviewText.setStyle(
                                "-fx-text-fill: " + MUTED + ";" +
                                                "-fx-font-size: 13px;");

                overview.getChildren().addAll(overviewTitle, overviewText);
                overview.setPadding(new Insets(22));
                overview.setStyle(
                                "-fx-background-color: " + PANEL + ";" +
                                                "-fx-background-radius: 12px;");

                content.getChildren().addAll(topBar, metrics, overview);
                root.setCenter(content);

                Scene scene = new Scene(root, 1100, 680);

                stage.setTitle("OpenPulse");
                stage.setMinWidth(850);
                stage.setMinHeight(550);
                stage.setScene(scene);
                stage.show();

                // Begin collecting readings after the UI has been created.
                startMonitoring();
        }

        private void startMonitoring() {
                gpuProbe = new GpuProbe();

                monitor.scheduleAtFixedRate(() -> {
                        try {
                                double cpuUsage = SystemProbe.getCpuUsagePercentage();
                                double ramUsage = SystemProbe.getRamUsagePercentage();
                                String operatingSystem = SystemProbe.getOperatingSystem();

                                List<GpuProbe.GpuReading> gpus = gpuProbe.read();

                                Platform.runLater(() -> {
                                        cpuReading.setText(formatPercentage(cpuUsage));
                                        ramReading.setText(formatPercentage(ramUsage));
                                        systemReading.setText(operatingSystem);

                                        updateGpuDisplay(gpus);
                                });

                        } catch (Exception exception) {
                                exception.printStackTrace();
                        }
                }, 0, 1, TimeUnit.SECONDS);
        }

        private String formatPercentage(double value) {
                if (value < 0 || !Double.isFinite(value)) {
                        return "N/A";
                }

                return String.format(Locale.ROOT, "%.1f%%", value);
        }

        private void updateGpuDisplay(List<GpuProbe.GpuReading> gpus) {
                if (gpus.isEmpty()) {
                        gpuReading.setText("Not detected");
                        gpuDetail.setText("No graphics card found");
                        return;
                }

                StringBuilder readings = new StringBuilder();
                StringBuilder details = new StringBuilder();

                for (GpuProbe.GpuReading gpu : gpus) {
                        if (!readings.isEmpty()) {
                                readings.append("\n");
                                details.append("\n");
                        }

                        readings.append(gpu.name())
                                        .append(": ")
                                        .append(formatPercentage(gpu.utilization()));

                        details.append(gpu.name());

                        if (gpu.usedVramBytes() >= 0 && gpu.totalVramBytes() > 0) {
                                double usedGb = gpu.usedVramBytes() / 1_073_741_824.0;
                                double totalGb = gpu.totalVramBytes() / 1_073_741_824.0;

                                details.append(String.format(
                                                Locale.ROOT, " · VRAM %.1f / %.1f GB",
                                                usedGb, totalGb));
                        }

                        if (gpu.temperatureCelsius() >= 0) {
                                details.append(String.format(
                                                Locale.ROOT, " · %.0f°C",
                                                gpu.temperatureCelsius()));
                        }
                }

                gpuReading.setText(readings.toString());
                gpuDetail.setText(details.toString());
        }

        private Button navButton(String text, boolean selected) {
                Button button = new Button(text);
                button.setMaxWidth(Double.MAX_VALUE);
                button.setAlignment(Pos.CENTER_LEFT);
                button.setPadding(new Insets(11, 12, 11, 12));

                String background = selected ? "#293b34" : "transparent";
                String foreground = selected ? ACCENT : "#b8bfcb";

                button.setStyle(
                                "-fx-background-color: " + background + ";" +
                                                "-fx-text-fill: " + foreground + ";" +
                                                "-fx-font-size: 13px;" +
                                                "-fx-background-radius: 7px;" +
                                                "-fx-cursor: hand;");

                return button;
        }

        private VBox metricCard(
                        String heading, Label reading, String description) {

                Label detail = new Label(description);
                return metricCard(heading, reading, detail);
        }

        private VBox metricCard(
                        String heading, Label reading, Label detail) {

                Label title = new Label(heading);
                title.setStyle(
                                "-fx-text-fill: " + MUTED + ";" +
                                                "-fx-font-size: 11px;" +
                                                "-fx-font-weight: bold;");

                reading.setWrapText(true);
                reading.setStyle(
                                "-fx-text-fill: " + TEXT + ";" +
                                                "-fx-font-size: 20px;" +
                                                "-fx-font-weight: bold;");

                detail.setWrapText(true);
                detail.setStyle(
                                "-fx-text-fill: " + MUTED + "; -fx-font-size: 11px;");

                VBox card = new VBox(12, title, reading, detail);
                card.setPadding(new Insets(20));
                card.setMinWidth(0);
                card.setStyle(
                                "-fx-background-color: " + PANEL + ";" +
                                                "-fx-background-radius: 12px;");

                return card;
        }

        private Label sectionTitle(String text) {
                Label label = new Label(text);
                label.setStyle(
                                "-fx-text-fill: " + TEXT + ";" +
                                                "-fx-font-size: 16px;" +
                                                "-fx-font-weight: bold;");
                return label;
        }

        @Override
        public void stop() {
                monitor.shutdownNow();

                if (gpuProbe != null) {
                        gpuProbe.close();
                }
        }

        public static void main(String[] args) {
                launch(args);
        }
}
