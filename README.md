# Let's make pizza !
**Hugo COLLIN, Reika JACQUOT, Natanaël MIESCH, Gaël BALLOIR**

Systeme distribué permettant de commander des pizzas, les préparer, les cuire, et suivre leur livraison.

## Prérequis
- Docker et docker-compose
- Maven
- Java 21
- JavaFX

## Fonctionnalités
La base actuelle permet à un client d'envoyer des messages à la pizzeria via le clic sur un bouton d'IHM. La pizzeria se contente pour l'instant de les afficher dans le terminal.

## Guide de démarrage
1. Démarrer le broker MQTT
```bash
cd broker && docker-compose up -d
```

2. Démarrer la pizzeria
```bash
cd pizzeria && mvn clean package && java -jar target/pizza-mqtt-1.0-SNAPSHOT.jar
```

3. Démarrer le client
```bash
cd client && mvn clean javafx:run
```

Le terminal de la pizzeria doit normalement afficher un message lors du clic sur le bouton dans l'IHM client.
