package com.pizzeria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService executorService;
    private Random random;

    public MQTTServer() {
        // Initialisation du catalogue de pizzas
        pizzaiolo = new Pizzaiolo(false);
        initCatalogue();
        executorService = Executors.newFixedThreadPool(6);
        random = new Random();
    }

    private void initCatalogue() {
        catalogue = new ArrayList<>();
        for (Pizzaiolo.DetailsPizza detailsPizza : pizzaiolo.getListePizzas()) {
            //le pizzaiolo déteste la pizza : gestion de sa haine avec cette condition
            if(!detailsPizza.ingredients().contains(Pizzaiolo.Ingredient.ANANAS))
                catalogue.add(new Pizza(
                    satanize(detailsPizza.nom()),
                    detailsPizza.ingredients().stream()
                            .map(Pizzaiolo.Ingredient::toString)
                            .map(this::satanize)
                            .toList(),
                    detailsPizza.prix()));
        }
    }


    /**
     * Méthode pour démarrer le serveur MQTT
     */
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

    /**
     * Méthode pour gérer les messages reçus
     * @param topic Le topic du message
     * @param message Le message reçu
     */
    private void handleMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("Message reçu: " + payload);
    }

    /**
     * Méthode pour gérer les demandes de menu
     * @param topic Le topic de la demande
     * @param message Le message de demande
     */
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

    /**
     * Méthode pour arrêter le serveur MQTT
     */
    public void stop() {
        try {
            executorService.shutdown();
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("Déconnecté du broker MQTT");
            }
        } catch (MqttException e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    /**
     * Méthode pour gérer les commandes reçues
     * @param topic Le topic de la commande
     * @param commande Le message de commande
     */
    private void handleCommande(String topic, MqttMessage commande) {
        // Extraire l'ID de la commande du topic (orders/xxx)
        String orderId = topic.substring(topic.lastIndexOf('/') + 1);
        String payload = new String(commande.getPayload());
        System.out.println("Commande reçue [" + orderId + "]: " + payload);

        // Désérialiser la commande
        Order order = Order.deserialize(orderId, payload);

        // Traiter la commande dans un thread séparé
        executorService.submit(() -> processOrder(order));
    }

    /**
     * Méthode pour traiter une commande
     * @param order La commande à traiter
     */
    private void processOrder(Order order) {
        String orderId = order.getId();
        int totalPizzas = 0;

        try {
            // Étape 1: Commande en validation
            sendOrderStatus(orderId, "validating");

            if (!validateOrder(order)) {
                sendOrderCancelled(orderId);
                return;
            }

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

                    // Étape 3 : Cuisson
                    synchronized (this){
                        sendOrderStatus(orderId, "cooking");
                        System.out.println("Cuisson des pizzas : " + pizzaName);
                        List<Pizzaiolo.Pizza> pizzasCuites = pizzaiolo.cuire(pizzasPreparees);
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors de la préparation : " + e.getMessage());
                }
            }

            // Étape 4: Livraison
            sendOrderStatus(orderId, "delivering");

            // Temps de livraison variable (1500ms + 0-500ms aléatoire)
            long deliveryTime = 1500 + random.nextInt(501);
            Thread.sleep(deliveryTime);

            // Notification de livraison avec le nombre de pizzas
            envoyerNotificationLivraison(orderId, totalPizzas);

        } catch (InterruptedException e) {
            System.out.println("Interruption lors du traitement de la commande: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement de la commande: " + e.getMessage());
        }
    }

    /**
     * Méthode pour envoyer une notification de statut de commande
     * @param orderId L'ID de la commande
     * @param status Le statut de la commande
     */
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
     * Méthode pour valider une commande
     * @param order La commande à valider
     */
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

    /**
     * Méthode pour envoyer une notification d'annulation de commande
     * @param orderId L'ID de la commande annulée
     */
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


    /**
     * Méthode pour envoyer une notification de livraison
     * @param orderId L'ID de la commande livrée
     * @param pizzaCount Le nombre de pizzas livrées
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

    /**
     * Méthode pour trouver une pizza dans le catalogue
     * @param nom Le nom de la pizza à rechercher
     * @throws Exception si la pizza n'est pas trouvée
     */
    public Pizza trouverDansCatalogue(String nom) throws Exception {
        for(int i = 0; i < catalogue.size();i++){
            if(catalogue.get(i).serialize().split("\\|")[0].equals(nom)){return catalogue.get(i);}
        }
        throw new Exception("Nom de pizza introuvable dans le catalogue");
    }

    /**
     * Méthode qui permet d'enlever des caractères exotiques dans les pizzas
     * @param str string à nettoyer
     */
    private String satanize(String str){
        return str.replace("|", "")
                .replace(",", "")
                .replace(";", "")
                .replace("]", "")
                .replace("[", "");
    }
}
