package com.pizza;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class HelloApplication extends Application {
    private Label statusLabel;
    private Button requestMenuButton;
    private ListView<Pizza> menuListView;
    private MQTTClient mqttClient;

    @Override
    public void start(Stage stage) {
        // Initialisation du client MQTT
        mqttClient = new MQTTClient();
        try {
            mqttClient.connect();
            mqttClient.setMenuCallback(this::updateMenuUI);
        } catch (Exception e) {
            showError("Erreur de connexion MQTT", e.getMessage());
        }

        // Création des éléments d'interface
        BorderPane root = new BorderPane();

        // En-tête
        Label titleLabel = new Label("Bienvenue à la Pizzeria!");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        statusLabel = new Label("Prêt à commander");

        VBox headerBox = new VBox(10, titleLabel, statusLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));
        root.setTop(headerBox);

        // Centre - Liste des pizzas
        menuListView = new ListView<>();
        menuListView.setPrefHeight(300);
        root.setCenter(menuListView);

        // Bas - Boutons
        requestMenuButton = new Button("Voir le menu");
        requestMenuButton.setOnAction(event -> requestMenu());

        Button sendButton = new Button("Envoyer un message");
        sendButton.setOnAction(event -> {
            statusLabel.setText("Message envoyé à la pizzeria!");
            mqttClient.sendMessage("HelloWorld");
        });

        HBox buttonBox = new HBox(20, requestMenuButton, sendButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));
        root.setBottom(buttonBox);

        // Configuration de la scène
        Scene scene = new Scene(root, 500, 500);
        stage.setTitle("Pizza Client");
        stage.setScene(scene);
        stage.show();
    }

    private void requestMenu() {
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

    @Override
    public void stop() {
        if (mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
