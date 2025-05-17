package com.pizza.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.pizza.ClientApplication;
import com.pizza.MQTTClient;
import com.pizza.model.Order;
import com.pizza.model.Pizza;

public class OrderController {
    @FXML
    private Label statusLabel;

    @FXML
    private Button requestMenuButton;

    @FXML
    private Button orderButton;

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
            mqttClient.setChangerVisuel(this::switchScene);
            mqttClient.setErrorCallback(this::showError);
            this.onRequestMenu();
        } catch (Exception e) {
            showError("Erreur de connexion MQTT", e.getMessage());
        }
    }

    @FXML
    protected void onRequestMenu() {
        statusLabel.setText("Demande du menu en cours...");
        requestMenuButton.setDisable(true);
        orderButton.setDisable(true);

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
    protected void onPlaceOrderButtonClick() {
        // Vérifier si au moins une pizza est sélectionnée
        boolean hasSelection = false;
        for (Spinner<Integer> spinner : pizzaQuantities.values()) {
            if (spinner.getValue() > 0) {
                hasSelection = true;
                break;
            }
        }

        if (!hasSelection) {
            showError("Commande vide", "Veuillez sélectionner au moins une pizza.");
            return;
        }

        // Créer la commande
        Order order = new Order();
        for (Map.Entry<String, Spinner<Integer>> entry : pizzaQuantities.entrySet()) {
            int quantity = entry.getValue().getValue();
            if (quantity > 0) {
                order.addPizza(entry.getKey(), quantity);
            }
        }

        // Désactiver le bouton pour éviter les doubles commandes
        orderButton.setDisable(true);
        statusLabel.setText("Envoi de votre commande...");

        // Envoyer la commande
        mqttClient.sendOrder(order);
    }

    private void updateMenuUI(Map<Pizza, String> menu) {
        Platform.runLater(() -> {
            statusLabel.setText("Menu récupéré avec succès - " + menu.size() + " pizzas disponibles");
            requestMenuButton.setDisable(false);
            orderButton.setDisable(false);

            // Mettre à jour le panneau de commande
            updateOrderPane(menu);
        });
    }

    private void updateOrderPane(Map<Pizza, String> pizzas) {
        // Nettoyer les anciens éléments
        orderPane.getChildren().clear();
        pizzaQuantities.clear();

        Label orderLabel = new Label("Sélectionnez vos pizzas:");
        orderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        orderPane.getChildren().add(orderLabel);

        // Créer un contrôle pour chaque pizza
        for (Pizza pizza : pizzas.keySet()) {
            String pizzaName = pizza.getNom();
            pizzaName = ("" + pizzaName.charAt(0)).toUpperCase() + pizzaName.substring(1);
            //System.out.println(pizza.ingredient());
            HBox pizzaBox = new HBox(10);
            pizzaBox.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(pizzaName);
            nameLabel.setPrefWidth(150);

            Label priceLabel = new Label(String.format("%d.00 €", pizza.getPrix()));
            priceLabel.setPrefWidth(80);

            Label ingredientsLabel = new Label("Ingredients : " + pizzas.get(pizza).replaceAll(",", ", "));

            Spinner<Integer> quantitySpinner = new Spinner<>(0, 9, 0);
            quantitySpinner.setPrefWidth(70);
            quantitySpinner.setEditable(true);
            pizzaQuantities.put(pizza.getNom(), quantitySpinner);

            VBox container = new VBox(0);
            pizzaBox.getChildren().addAll(nameLabel, priceLabel, quantitySpinner);
            container.getChildren().add(pizzaBox);
            container.getChildren().add(ingredientsLabel);
            orderPane.getChildren().add(container);
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

    public void switchScene(String scene){
        try{
            ClientApplication.loadWaitingScreen(mqttClient);
        }
        catch(IOException e){
            System.out.print(e.fillInStackTrace());
            showError("Erreur de navigation", "Impossible de charger l'écran d'attente: " + e.getMessage());
        }
    }

    public void showError(String[] error) {
        Platform.runLater(() -> {
            this.requestMenuButton.setDisable(false);
            showError(error[0], error[1]);
        });
    }
}
