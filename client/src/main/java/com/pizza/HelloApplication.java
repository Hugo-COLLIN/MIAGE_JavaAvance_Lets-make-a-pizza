package com.pizza;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private Label welcomeText;
    private Button sendButton;
    private MQTTClient mqttClient;

    @Override
    public void start(Stage stage) {
        // Création des éléments d'interface
        welcomeText = new Label("Bienvenue à la Pizzeria!");
        sendButton = new Button("Envoyer un message");

        // Configuration du layout
        VBox root = new VBox(20);
        root.getChildren().addAll(welcomeText, sendButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        // Gestionnaire d'événements pour le bouton
        sendButton.setOnAction(event -> {
            welcomeText.setText("Message envoyé à la pizzeria!");
            mqttClient.sendMessage("HelloWorld");
        });

        // Initialisation du client MQTT
        mqttClient = new MQTTClient();
        try {
            mqttClient.connect();
        } catch (Exception e) {
            welcomeText.setText("Erreur de connexion MQTT: " + e.getMessage());
        }

        // Configuration de la scène
        Scene scene = new Scene(root, 320, 240);
        stage.setTitle("Pizza Client");
        stage.setScene(scene);
        stage.show();
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
