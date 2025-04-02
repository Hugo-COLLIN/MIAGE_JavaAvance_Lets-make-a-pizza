package com.pizzeria;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTServer {
    private final String broker = "tcp://localhost:1883";
    private final String clientId = "PizzeriaServer";
    private MqttClient client;

    public void start() {
        try {
            // Création du client MQTT
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            System.out.println("Connexion au broker: " + broker);
            client.connect(options);
            System.out.println("Connecté au broker MQTT");

            // Abonnement au topic pour recevoir les messages des clients
            client.subscribe("pizza/messages", (topic, message) -> {
                String payload = new String(message.getPayload());
                System.out.println("Message reçu: " + payload);
            });

            System.out.println("Serveur Pizzeria en attente de messages...");
        } catch (MqttException e) {
            System.err.println("Erreur lors de la connexion au broker MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
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
