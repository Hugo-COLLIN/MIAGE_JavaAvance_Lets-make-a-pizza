package com.pizza;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTClient {
    private final String broker = "tcp://localhost:1883";
    private final String clientId = "PizzaClient-" + System.currentTimeMillis();
    private MqttClient client;
    private CompletableFuture<List<Pizza>> menuFuture;
    private Consumer<List<Pizza>> menuCallback;

    public void connect() throws MqttException {
        client = new MqttClient(broker, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        System.out.println("Connexion au broker: " + broker);
        client.connect(options);
        System.out.println("Connecté au broker MQTT");

        // S'abonner au topic du menu
        client.subscribe("bcast/menu", this::handleMenuResponse);
    }

    public void sendMessage(String message) {
        try {
            if (client == null || !client.isConnected()) {
                connect();
            }

            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(1);
            client.publish("pizza/messages", mqttMessage);
            System.out.println("Message envoyé: " + message);
        } catch (MqttException e) {
            System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
        }
    }

    public CompletableFuture<List<Pizza>> requestMenu() {
        try {
            if (client == null || !client.isConnected()) {
                connect();
            }
            menuFuture = new CompletableFuture<>();

            // Envoyer la demande de menu
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(1);
            client.publish("bcast/i_am_ungry", mqttMessage);
            System.out.println("Demande de menu envoyée");
            return menuFuture;
        } catch (MqttException e) {
            System.err.println("Erreur lors de la demande de menu: " + e.getMessage());
            CompletableFuture<List<Pizza>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private void handleMenuResponse(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            System.out.println("Menu reçu: " + payload);

            // Désérialiser le menu
            List<Pizza> menu = new ArrayList<>();
            if (!payload.isEmpty()) {
                String[] pizzaStrings = payload.split(";");
                for (String pizzaString : pizzaStrings) {
                    menu.add(Pizza.deserialize(pizzaString));
                }
            }

            // Compléter le future avec le menu
            if (menuFuture != null && !menuFuture.isDone()) {
                menuFuture.complete(menu);
            }

            // Appeler le callback si défini
            if (menuCallback != null) {
                menuCallback.accept(menu);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du menu: " + e.getMessage());
            if (menuFuture != null && !menuFuture.isDone()) {
                menuFuture.completeExceptionally(e);
            }
        }
    }

    public void setMenuCallback(Consumer<List<Pizza>> callback) {
        this.menuCallback = callback;
    }

    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("Déconnecté du broker MQTT");
            }
        } catch (MqttException e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    //Work In Progress
    //fonction pour envoyer une commande vers la pizzeria
    public void sendOrder(Order order) {
        try {
            if (client == null || !client.isConnected()) {
                connect();
            }

            MqttMessage mqttMessage = new MqttMessage(order.serialize().getBytes());
            mqttMessage.setQos(1);
            client.publish("pizza/commande", mqttMessage);
            System.out.println("Commande envoyé: " + order.serialize());
        } catch (MqttException e) {
            System.err.println("Erreur lors de l'envoi de la commande: " + e.getMessage());
        }
    }
}
