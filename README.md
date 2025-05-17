# Let's make pizza !
**Hugo COLLIN, Reika JACQUOT, Nathanaël MIESCH, Gaël BALLOIR**

Systeme distribué permettant de commander des pizzas, les préparer, les cuire, et suivre leur livraison.

## Fonctionnalités
La base actuelle permet à un client d'envoyer des messages à la pizzeria via le clic sur un bouton d'IHM. La pizzeria se contente pour l'instant de les afficher dans le terminal.

## Guide de démarrage
### Prérequis pour le développement
- Docker et docker-compose
- Maven
- Java 21
- JavaFX

### Développement
Effectuer chaque étape dans un terminal différent :

1. Démarrer le broker MQTT
```bash
cd broker && docker-compose up -d && cd ..
```

2. Recompiler et démarrer la pizzeria
```bash
mvn clean compile package -am -pl pizzeria && java -jar pizzeria/target/pizzeria-1.0-SNAPSHOT-jar-with-dependencies.jar
```

3. Recompiler et démarrer le client
```bash
mvn clean compile package -am -pl client && java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Lors de la modification du code du client ou de la pizzeria, l'étape correspondante doit être réexécutée.

### Déploiement
Pour compiler tout le projet, exécuter simplement :
```sh
mvn clean package
```

Les JAR exécutables se trouvent aux emplacements suivants :
- Pizzeria : `pizzeria/target/pizzeria-1.0-SNAPSHOT-jar-with-dependencies.jar`
- Client : `client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar`
