package com.pizzeria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            client.subscribe("orders/+", this::handleCommande);

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

    private void handleCommande(String topic, MqttMessage commande) {
        // Extraire l'ID de la commande du topic (orders/xxx)
        String orderId = topic.substring(topic.lastIndexOf('/') + 1);
        String payload = new String(commande.getPayload());
        System.out.println("Commande reçue [" + orderId + "]: " + payload);

        // Désérialiser la commande
        Order order = Order.deserialize(orderId, payload);

        // Valider la commande
        if (!validateOrder(order)) {
            sendOrderCancelled(orderId);
            return;
        }

        // Traiter la commande
        // TODO dans un thread séparé
        processOrder(order);
    }

    private boolean validateOrder(Order order) {
        for (Map.Entry<String, Integer> entry : order.getPizzaQuantities().entrySet()) {
            String pizzaName = entry.getKey();
            int quantity = entry.getValue();

            // Vérifier que la pizza existe
            boolean pizzaExists = false;
            for (Pizza pizza : catalogue) {
                if (pizza.getNom().equals(pizzaName)) {
                    pizzaExists = true;
                    break;
                }
            }

            // Vérifier la quantité
            if (!pizzaExists || quantity <= 0 || quantity >= 10) {
                return false;
            }
        }
        return true;
    }

    private void sendOrderCancelled(String orderId) {
        try {
            MqttMessage cancelMessage = new MqttMessage();
            cancelMessage.setQos(1);
            client.publish("orders/" + orderId + "/cancelled", cancelMessage);
            System.out.println("Commande " + orderId + " annulée");
        } catch (MqttException e) {
            System.err.println("Erreur lors de l'envoi de l'annulation: " + e.getMessage());
        }
    }

    private void processOrder(Order order) {
        String orderId = order.getId();
        int totalPizzas = 0;

        try {
            // Étape 1: Commande validée
            sendOrderStatus(orderId, "validated");

            // Étape 2: Commande en préparation
            sendOrderStatus(orderId, "preparing");

            // Pour chaque type de pizza commandée
            for (Map.Entry<String, Integer> entry : order.getPizzaQuantities().entrySet()) {
                String pizzaName = entry.getKey();
                int quantite = entry.getValue();
                totalPizzas += quantite;

                try {
                    // Récupérer les détails de la pizza dans le catalogue
                    Pizza pizzaInfo = trouverDansCatalogue(pizzaName);

                    // Créer un objet DetailsPizza pour la préparation
                    Pizzaiolo.DetailsPizza detailsPizza = pizzaiolo.getListePizzas()
                            .stream()
                            .filter(p -> p.nom().equals(pizzaName))
                            .findFirst()
                            .orElseThrow();

                    // Préparer les pizzas demandées
                    List<Pizzaiolo.Pizza> pizzasPreparees = new ArrayList<>();
                    for (int i = 0; i < quantite; i++) {
                        System.out.println("Préparation de la pizza : " + pizzaName);
                        // Préparer la pizza - cette méthode renvoie un objet Pizzaiolo.Pizza
                        Pizzaiolo.Pizza pizzaPreparee = pizzaiolo.preparer(detailsPizza);
                        // Ajouter à la liste des pizzas préparées
                        pizzasPreparees.add(pizzaPreparee);
                    }

                    // Étape 3: Cuisson
                    sendOrderStatus(orderId, "cooking");
                    System.out.println("Cuisson des pizzas : " + pizzaName);
                    List<Pizzaiolo.Pizza> pizzasCuites = pizzaiolo.cuire(pizzasPreparees);

                } catch (Exception e) {
                    System.out.println("Erreur lors de la préparation : " + e.getMessage());
                }
            }

            // Étape 4: Livraison
            sendOrderStatus(orderId, "delivering");

            // Notification de livraison avec le nombre de pizzas
            envoyerNotificationLivraison(orderId, totalPizzas);

        } catch (Exception e) {
            System.out.println("Erreur lors du traitement de la commande: " + e.getMessage());
        }
    }

    // Méthode pour envoyer une mise à jour de statut
    private void sendOrderStatus(String orderId, String status) {
        try {
            MqttMessage statusMessage = new MqttMessage();
            statusMessage.setQos(1);
            client.publish("orders/" + orderId + "/status/" + status, statusMessage);
            System.out.println("Statut de la commande " + orderId + " mis à jour: " + status);
        } catch (MqttException e) {
            System.err.println("Erreur lors de l'envoi du statut: " + e.getMessage());
        }
    }

    /**
     * Méthode pour envoyer une notification de livraison
     */
    private void envoyerNotificationLivraison(String orderId, int pizzaCount) {
        try {
            MqttMessage deliveryMessage = new MqttMessage(String.valueOf(pizzaCount).getBytes());
            deliveryMessage.setQos(1);
            client.publish("orders/" + orderId + "/delivery", deliveryMessage);
            System.out.println("Livraison de la commande " + orderId + " terminée: " + pizzaCount + " pizzas");
        } catch (MqttException e) {
            System.err.println("Erreur lors de l'envoi de la notification de livraison: " + e.getMessage());
        }
    }

    public Pizza trouverDansCatalogue(String nom) throws Exception{
        for(int i = 0; i < catalogue.size();i++){
            if(catalogue.get(i).serialize().split("\\|")[0].equals(nom)){return catalogue.get(i);}
        }
        throw new Exception("Nom de pizza introuvable dans le catalogue");
    }
}
