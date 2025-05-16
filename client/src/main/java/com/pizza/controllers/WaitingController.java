package com.pizza.controllers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import com.pizza.ClientApplication;
import com.pizza.MQTTClient;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;

public class WaitingController {
    @FXML
    private Label mainTitle;
    @FXML
    private Label infoStatus;
    @FXML
    private Button boutonFin;
    @FXML
    private ImageView logoImage;
    private FadeTransition logoFadeTransition;

    private MQTTClient mqttClient;

    public WaitingController(MQTTClient mqttc) {
        mqttClient = mqttc;
        try {
            mqttClient.setNotificationCallback(this::showNotification);
            mqttClient.setFonctionBoutonLivraison(this::updateLivraison);
        } catch (Exception e) {
            showError("Erreur de connexion MQTT", e.getMessage());
        }
    }

    public void initialize() {
        // Initialiser le texte de statut
        if (infoStatus != null) {
            infoStatus.setText("Validation de votre commande...");
        }

        // Charger l'image
        try {
            Image logo = new Image(Objects.requireNonNull(
                    ClientApplication.class.getResourceAsStream("assets/pizza.png")
            ));
            logoImage.setImage(logo);

            // Appliquer un effet de fondu clignotant
            logoFadeTransition = new FadeTransition(Duration.seconds(1), logoImage);
            logoFadeTransition.setFromValue(1.0);
            logoFadeTransition.setToValue(0.0);
            logoFadeTransition.setCycleCount(FadeTransition.INDEFINITE);
            logoFadeTransition.setAutoReverse(true);
            logoFadeTransition.play();
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image du logo: " + e.getMessage());
        }
    }

    public void showNotification(String message) {
        System.out.println("Notification: " + message);
        String status = message.split("/")[1];
        updateStatus(status);
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> {
            // Mettre à jour le texte de statut
            switch (status) {
                case "validating":
                    infoStatus.setText("Validation de votre commande...");
                    break;
                case "preparing":
                    infoStatus.setText("Préparation de vos pizzas...");
                    break;
                case "cooking":
                    infoStatus.setText("Cuisson en cours...");
                    break;
                case "delivering":
                    infoStatus.setText("Livraison en cours...");
                    break;
                default:
                    infoStatus.setText("Statut: " + status);
            }
        });
    }

    private void updateLivraison() {
        Platform.runLater(() -> {
            infoStatus.setText("Livraison terminée");
            mainTitle.setText("Commande livrée !!!");
            boutonFin.setVisible(true);
            
            if (logoFadeTransition != null) {
                logoFadeTransition.stop();
                logoImage.setOpacity(1.0);
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onBoutonFinClick() {
        try {
            ClientApplication.loadWelcomeScreen();
        } catch (Exception e) {
            System.out.println("Probleme lors du reload");
            showError("Erreur", e.getMessage());
        }
    }
}
