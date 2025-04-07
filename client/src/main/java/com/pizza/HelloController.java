package com.pizza;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

public class HelloController {
    @FXML
    private Label statusLabel;

    @FXML
    private Button requestMenuButton;

    @FXML
    private ListView<Pizza> menuListView;

    private MQTTClient mqttClient;

    public void initialize() {
        // Initialisation du client MQTT
        mqttClient = new MQTTClient();
        try {
            mqttClient.connect();
            mqttClient.setMenuCallback(this::updateMenuUI);
        } catch (Exception e) {
            showError("Erreur de connexion MQTT", e.getMessage());
        }
    }

    @FXML
    protected void onRequestMenuButtonClick() {
        statusLabel.setText("Demande du menu en cours...");
        requestMenuButton.setDisable(true);

        mqttClient.requestMenu()
                .thenAccept(this::updateMenuUI)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showError("Erreur de récupération du menu", ex.getMessage());
                        statusLabel.setText("Erreur lors de la récupération du menu");
                        requestMenuButton.setDisable(false);
                    });
                    return null;
                });
    }

    @FXML
    protected void onSendMessageButtonClick() {
        statusLabel.setText("Message envoyé à la pizzeria!");
        mqttClient.sendMessage("HelloWorld");
    }

    private void updateMenuUI(List<Pizza> menu) {
        Platform.runLater(() -> {
            menuListView.getItems().clear();
            menuListView.getItems().addAll(menu);
            statusLabel.setText("Menu récupéré avec succès - " + menu.size() + " pizzas disponibles");
            requestMenuButton.setDisable(false);
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void shutdown() {
        if (mqttClient != null) {
            mqttClient.disconnect();
        }
    }
}
