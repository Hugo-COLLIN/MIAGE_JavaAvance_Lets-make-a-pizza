package com.pizza;

import java.io.IOException;

import com.pizza.controllers.OrderController;
import com.pizza.controllers.WaitingController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    private static Stage primaryStage;
    private OrderController controller;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        loadWelcomeScreen();

        stage.setTitle("Pizza Client");
        stage.show();
    }

    public static void loadWelcomeScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("views/welcome-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pizzeria - Accueil");
    }

    public static void loadOrderScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("views/order-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        OrderController controller = fxmlLoader.getController();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pizzeria - Commander");
    }

    public static void loadWaitingScreen(MQTTClient mqttc) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("views/waiting-view.fxml"));
        WaitingController controller = new WaitingController(mqttc);
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pizzeria - Suivi de commande");
    }

    @Override
    public void stop() {
        // Fermer toutes les connexions MQTT
        try {
            if (controller != null) {
                controller.shutdown();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
