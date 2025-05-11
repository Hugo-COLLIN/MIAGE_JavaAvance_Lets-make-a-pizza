package com.pizza;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class OrderController {
    @FXML
    private Label infoStatus;

    private MQTTClient mqttClient;

    public void initialize(){
        mqttClient = new MQTTClient();
        try{
            mqttClient.setNotificationCallback(this::showNotification);
        }catch(Exception e){
            showError("Erreur de connexion MQTT", e.getMessage());
        }
    }

    public void showNotification(String message) {
        System.out.println("Notification: " + message);
        updateStatus(message);
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> infoStatus.setText(status));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
