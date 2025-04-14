package com.pizzeria;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import lets_make_a_pizza.serveur.Pizzaiolo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MQTTServer {
    private final String broker = "tcp://localhost:1883";
    private final String clientId = "PizzeriaServer";
    private MqttClient client;
    private List<Pizza> catalogue;
    private Pizzaiolo pizzaiolo;

    public MQTTServer() {
        // Initialisation du catalogue de pizzas
        initCatalogue();
        pizzaiolo = new Pizzaiolo(true);
    }

    private void initCatalogue() {
        catalogue = new ArrayList<>();
        catalogue.add(new Pizza("Margharita", Arrays.asList("Tomate", "Mozzarella", "Basilic"), 800));
        catalogue.add(new Pizza("Reine", Arrays.asList("Tomate", "Mozzarella", "Jambon", "Champignons"), 1000));
        catalogue.add(new Pizza("4 Fromages", Arrays.asList("Tomate", "Mozzarella", "Gorgonzola", "Parmesan", "Chèvre"), 1200));
        catalogue.add(new Pizza("Végétarienne", Arrays.asList("Tomate", "Mozzarella", "Poivrons", "Oignons", "Olives"), 1100));
    }

    public void start() {
        try {
            // Création du client MQTT
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            System.out.println("Connexion au broker: " + broker);
            client.connect(options);
            System.out.println("Connecté au broker MQTT");

            // Abonnement aux topics
            client.subscribe("pizza/messages", this::handleMessage);
            client.subscribe("bcast/i_am_ungry", this::handleMenuRequest);

            System.out.println("Serveur Pizzeria en attente de messages...");
        } catch (MqttException e) {
            System.err.println("Erreur lors de la connexion au broker MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("Message reçu: " + payload);
    }

    private void handleMenuRequest(String topic, MqttMessage message) {
        System.out.println("Demande de menu reçue");

        try {
            // Sérialisation du catalogue
            StringBuilder menuData = new StringBuilder();
            for (int i = 0; i < catalogue.size(); i++) {
                menuData.append(catalogue.get(i).serialize());
                if (i < catalogue.size() - 1) {
                    menuData.append(";");
                }
            }

            // Envoi du menu
            MqttMessage menuMessage = new MqttMessage(menuData.toString().getBytes());
            menuMessage.setQos(1);
            client.publish("bcast/menu", menuMessage);
            System.out.println("Menu envoyé: " + menuData);
        } catch (MqttException e) {
            System.err.println("Erreur lors de l'envoi du menu: " + e.getMessage());
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
