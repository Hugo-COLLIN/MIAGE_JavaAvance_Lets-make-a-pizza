package com.pizza;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTClient {
    private final String broker = "tcp://localhost:1883";
    private final String clientId = "PizzaClient-" + System.currentTimeMillis();
    private MqttClient client;

    public void connect() throws MqttException {
        client = new MqttClient(broker, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        System.out.println("Connexion au broker: " + broker);
        client.connect(options);
        System.out.println("Connecté au broker MQTT");
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
}
