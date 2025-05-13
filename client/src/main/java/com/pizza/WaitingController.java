package com.pizza;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WaitingController {
    @FXML
    private Label infoStatus;
    @FXML
    private Button boutonFin;

    private MQTTClient mqttClient;

    public WaitingController(MQTTClient mqttc){
        mqttClient = mqttc;
        try{
            mqttClient.setNotificationCallback(this::showNotification);
            mqttClient.setFonctionBoutonLivraison(this::updateLivraison);
        }catch(Exception e){
            showError("Erreur de connexion MQTT", e.getMessage());
        }
    }


    public void showNotification(String message) {
        System.out.println("Notification: " + message);
        String status = message.split("/")[1];
        updateStatus(status);
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> infoStatus.setText("Your order is "+status));
    }

    private void updateLivraison() {
        Platform.runLater(() -> {infoStatus.setText("Livraison terminée");
                                boutonFin.setVisible(true);
                                });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //TODO
    @FXML
    private void onBoutonFinClick(){
        try {
            ClientApplication.loadOrderScreen();
        } catch (Exception e) {
            System.out.println("Probleme lors du reload");
            showError("Erreur", e.getMessage());
        }
    }
}
