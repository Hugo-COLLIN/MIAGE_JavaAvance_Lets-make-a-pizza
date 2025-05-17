package com.pizza.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.Objects;

import com.pizza.ClientApplication;

public class WelcomeController {
    @FXML
    private ImageView logoImage;

    public void initialize() {
        try {
            Image logo = new Image(Objects.requireNonNull(
                    ClientApplication.class.getResourceAsStream("assets/pizza.png")
            ));
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image du logo: " + e.getMessage());
        }
    }

    @FXML
    protected void onStartOrderButtonClick() {
        try {
            ClientApplication.loadOrderScreen();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'écran de commande: " + e.getMessage());
        }
    }
}
