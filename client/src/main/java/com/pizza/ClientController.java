package com.pizza;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientController {
    @FXML
    private Label statusLabel;

    @FXML
    private Button requestMenuButton;

    @FXML
    private VBox orderPane;

    private MQTTClient mqttClient;
    private Map<String, Spinner<Integer>> pizzaQuantities = new HashMap<>();

    public void initialize() {
        // Initialisation du client MQTT
        mqttClient = new MQTTClient();
        try {
            mqttClient.connect();
            mqttClient.setMenuCallback(this::updateMenuUI);
            this.onRequestMenu();
        } catch (Exception e) {
            showError("Erreur de connexion MQTT", e.getMessage());
        }
    }

    @FXML
    protected void onRequestMenu() {
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
            statusLabel.setText("Menu récupéré avec succès - " + menu.size() + " pizzas disponibles");
            requestMenuButton.setDisable(false);

            // Mettre à jour le panneau de commande
            updateOrderPane(menu);
        });
    }

    private void updateOrderPane(List<Pizza> pizzas) {
        // Nettoyer les anciens éléments
        orderPane.getChildren().clear();
        pizzaQuantities.clear();

        Label orderLabel = new Label("Sélectionnez vos pizzas:");
        orderLabel.setStyle("-fx-font-weight: bold;");
        orderPane.getChildren().add(orderLabel);

        // Créer un contrôle pour chaque pizza
        for (Pizza pizza : pizzas) {
            HBox pizzaBox = new HBox(10);
            pizzaBox.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(pizza.getNom());
            nameLabel.setPrefWidth(150);

            Label priceLabel = new Label(String.format("%.2f €", pizza.getPrix() / 100.0));
            priceLabel.setPrefWidth(80);

            Spinner<Integer> quantitySpinner = new Spinner<>(0, 9, 0);
            quantitySpinner.setPrefWidth(70);
            quantitySpinner.setEditable(true);

            pizzaQuantities.put(pizza.getNom(), quantitySpinner);

            pizzaBox.getChildren().addAll(nameLabel, priceLabel, quantitySpinner);
            orderPane.getChildren().add(pizzaBox);
        }
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
