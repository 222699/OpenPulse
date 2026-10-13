
package com.openpulse.app;

import javafx.application.Application;
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
                footer.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 10px;");

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
                subtitle.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 13px;");

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

                // Placeholder cards — real readings come next
                HBox metrics = new HBox(16,
                                metricCard("PROCESSOR", "Awaiting data", "CPU utilization"),
                                metricCard("MEMORY", "Awaiting data", "Physical RAM usage"),
                                metricCard("SYSTEM", "Windows 10", "Operating system"));

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

        private VBox metricCard(String heading, String value, String description) {
                Label title = new Label(heading);
                title.setStyle(
                                "-fx-text-fill: " + MUTED + ";" +
                                                "-fx-font-size: 11px;" +
                                                "-fx-font-weight: bold;");

                Label reading = new Label(value);
                reading.setWrapText(true);
                reading.setStyle(
                                "-fx-text-fill: " + TEXT + ";" +
                                                "-fx-font-size: 20px;" +
                                                "-fx-font-weight: bold;");

                Label detail = new Label(description);
                detail.setWrapText(true);
                detail.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 11px;");

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

        public static void main(String[] args) {
                launch(args);
        }
}
