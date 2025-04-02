package com.pizza;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    private final MQTTClient mqttClient = new MQTTClient();

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Message envoyé à la pizzeria!");
        mqttClient.sendMessage("HelloWorld");
    }

    public void initialize() {
        try {
            mqttClient.connect();
        } catch (Exception e) {
            welcomeText.setText("Erreur de connexion MQTT: " + e.getMessage());
        }
    }

    public void shutdown() {
        mqttClient.disconnect();
    }
}
