package com.pizzeria;

public class App {
    public static void main(String[] args) {
        System.out.println("Démarrage du serveur Pizzeria...");
        MQTTServer server = new MQTTServer();
        server.start();

        // Ajout d'un hook d'arrêt pour fermer proprement la connexion
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Arrêt du serveur Pizzeria...");
            server.stop();
        }));
    }
}
