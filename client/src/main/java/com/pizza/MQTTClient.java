package com.pizza;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.pizza.model.Order;
import com.pizza.model.Pizza;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTClient {
    private final String broker = "tcp://localhost:1883";
    private final String clientId = "PizzaClient-" + System.currentTimeMillis();
    private MqttClient client;
    private CompletableFuture<Map<Pizza,String>> menuFuture;
    private Consumer<Map<Pizza,String>> menuCallback;
    private Consumer<String> notificationCallback;
    private Consumer<String> changerVisuel;
    private Runnable fonctionBoutonLivraison;

    private Runnable getFonctionBoutonCanceled;

    private Consumer<String[]> errorCallback;

    private TimeoutTimer timeoutTimer;

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

    public CompletableFuture<Map<Pizza,String>> requestMenu() {
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
            // lancer le timer de timout
            timeoutTimer = new TimeoutTimer(5000, this::handleError, "Le menu n'a pas pu être récupéré");
            timeoutTimer.start();
            return menuFuture;
        } catch (MqttException e) {
            System.err.println("Erreur lors de la demande de menu: " + e.getMessage());
            CompletableFuture<Map<Pizza,String>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private void handleMenuResponse(String topic, MqttMessage message) {
        try {
            // Arrêter le timer de timeout
            if (timeoutTimer != null) {
                timeoutTimer.interrupt();
            }
            String payload = new String(message.getPayload());
            System.out.println("Menu reçu: " + payload);

            // Désérialiser le menu
            HashMap<Pizza, String> menu = new HashMap<>();
            if (!payload.isEmpty()) {
                String[] pizzaStrings = payload.split(";");
                for (String pizzaString : pizzaStrings) {
                    String[] parts = pizzaString.split("\\|");
                    Pizza pizza = Pizza.deserialize(pizzaString);
                    menu.put(pizza, parts[1]);
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

    public void setMenuCallback(Consumer<Map<Pizza,String>> callback) {
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

    /**
     * Fonction pour envoyer une commande vers la pizzeria
     */
    public void sendOrder(Order order) {
        try {
            if (client == null || !client.isConnected()) {
                connect();
            }
            String[] topics = {"validating", "preparing", "cooking", "delivering"};

            for (String topic : topics) {
                client.subscribe("orders/" + order.getId() + "/status/" + topic, this::handleNotification);
            }
            client.subscribe("orders/" + order.getId() + "/delivery", this::handleDelivery);
            client.subscribe("orders/" + order.getId() + "/cancelled", this::handleCanceled);
            // Envoyer la commande sur le bon topic (orders/xxx)
            MqttMessage mqttMessage = new MqttMessage(order.serialize().getBytes());
            mqttMessage.setQos(1);
            client.publish("orders/" + order.getId(), mqttMessage);
            System.out.println("Commande envoyée: " + order.serialize());
            changerVisuel.accept("changement");
        } catch (MqttException e) {
            System.err.println("Erreur lors de l'envoi de la commande: " + e.getMessage());
        }
    }

    public void setNotificationCallback(Consumer<String> callback) {
        this.notificationCallback = callback;
    }

    public void setErrorCallback(Consumer<String[]> callback) {
        this.errorCallback = callback;
    }
    public void handleNotification(String topic, MqttMessage message) {
        String id = topic.split("/")[1];
        String status = topic.split("/")[3];
        System.out.println("Notification reçue [" + topic + "]");
        if (notificationCallback != null) {
            notificationCallback.accept(id + "/" + status);
        }
    }

    public void handleDelivery(String topic, MqttMessage message) {
        String id = topic.split("/")[1];
        System.out.println("Notification de livraison reçue [" + topic + "]");
        String payload = new String(message.getPayload());
        System.out.println("Payload de livraison: " + payload);
        fonctionBoutonLivraison.run();
        if (notificationCallback != null) {
            notificationCallback.accept(payload);
        }
    }


    public void setChangerVisuel(Consumer<String> consumer)
    {
        this.changerVisuel = consumer;
    }

    public void setFonctionBoutonLivraison(Runnable runnable){
        fonctionBoutonLivraison = runnable;
    }

    public void setFonctionBoutonCanceled(Runnable runnable){
        getFonctionBoutonCanceled = runnable;
    }

    public void handleCanceled(String topic, MqttMessage message) {
        String id = topic.split("/")[1];
        System.out.println("Notification d'annulation reçue [" + topic + "]");
        getFonctionBoutonCanceled.run();
        if (notificationCallback != null) {
            notificationCallback.accept("La commande " + id + " a été annulée.");
        }
    }

    public void handleError(String [] message) {
        System.out.println("error [" + message[1] + "]");
        if (errorCallback != null) {
            errorCallback.accept(message);
        }
    }
}
