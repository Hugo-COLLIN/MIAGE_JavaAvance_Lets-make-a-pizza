package com.pizza;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    private static Stage primaryStage;
    private ClientController controller;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        loadWelcomeScreen();

        stage.setTitle("Pizza Client");
        stage.show();
    }

    public static void loadWelcomeScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("welcome-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        primaryStage.setScene(scene);
    }

    public static void loadOrderScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        ClientController controller = fxmlLoader.getController();
        primaryStage.setScene(scene);
    }

    public static void loadOrderViewScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("order-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        OrderController controller = fxmlLoader.getController();
        primaryStage.setScene(scene);
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
