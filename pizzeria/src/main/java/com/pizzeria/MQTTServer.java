package com.pizzeria;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import lets_make_a_pizza.serveur.Pizzaiolo;

public class MQTTServer {
    private final String broker = "tcp://localhost:1883";
    private final String clientId = "PizzeriaServer";
    private MqttClient client;
    private List<Pizza> catalogue;
    private Pizzaiolo pizzaiolo;

    public MQTTServer() {
        // Initialisation du catalogue de pizzas
        pizzaiolo = new Pizzaiolo(true);
        initCatalogue();
    }

    private void initCatalogue() {
        catalogue = new ArrayList<>();
        for (Pizzaiolo.DetailsPizza detailsPizza : pizzaiolo.getListePizzas()) {
            catalogue.add(new Pizza(
                    detailsPizza.nom(),
                    detailsPizza.ingredients().stream().map(Pizzaiolo.Ingredient::toString).toList(),
                    detailsPizza.prix()));
        }
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
            client.subscribe("pizza/commande", this::handleCommande);

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

    //TODO
    private void handleCommande(String topic, MqttMessage commande) {
        String payload = new String(commande.getPayload());
        System.out.println("Commande reçue: " + payload);
        Order order = Order.deserialize("1",payload);
        order.getPizzaQuantities().forEach((pizzanom,quantite) -> {
            List<Pizzaiolo.Pizza> pizzaspreparees = new ArrayList<>();
            for(int i = 0; i < quantite; i++){
                System.out.println("Gestion de la pizza : "+pizzanom);
                try {
                    Pizza pizza = trouverDansCatalogue(pizzanom);
                    Pizzaiolo.DetailsPizza detail = new Pizzaiolo.DetailsPizza(pizza.getNom(), pizza.getIngredients() , pizza.getPrix());
                    pizzaiolo.preparer(detail);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }   
            }
            pizzaiolo.cuire(pizzaspreparees);
        }); 
    }

    public Pizza trouverDansCatalogue(String nom) throws Exception{
        for(int i = 0; i < catalogue.size();i++){
            if(catalogue.get(i).serialize().split("\\|")[0].equals(nom)){return catalogue.get(i);}
        }
        throw new Exception("Nom de pizza introuvable dans le catalogue");
    }
}
