# Let's make pizza !
**Hugo COLLIN, Reika JACQUOT, Nathanaël MIESCH, Gaël BALLOIR**

Systeme distribué permettant de commander des pizzas, les préparer, les cuire, et suivre leur livraison.

## Fonctionnalités
- Interface client JavaFX permettant de consulter le menu de pizzas, passer commande et suivre l'état de la commande en temps réel
- Système de pizzeria capable de recevoir des commandes, les préparer et les cuire
- Communication asynchrone basée sur des événements via le protocole MQTT
- Gestion des erreurs et timeouts pour assurer une expérience utilisateur fluide
- Traitement parallèle de plusieurs commandes côté pizzeria

## Guide utilisateur
Pour utiliser ce système distribué :
0. Lancer un broker MQTT. Le cas échéant, il est possible d'utiliser l'image Docker de ce dépôt : `cd broker && docker-compose up -d && cd ..`
1. Télécharger les JARs Client et Pizzeria depuis la page [Releases](https://gitlab.univ-lorraine.fr/collin174u/lets-make-a-pizza/-/releases) du dépôt ;
2. Lancer le JAR Pizzeria : `java -jar pizzeria-1.0-SNAPSHOT-jar-with-dependencies.jar` ;
3. Lancer le JAR Client : `java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## Guide développeur
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


Pour compiler tout le projet, exécuter simplement :
```sh
mvn clean package
```

Les JAR exécutables se trouvent aux emplacements suivants :
- Pizzeria : `pizzeria/target/pizzeria-1.0-SNAPSHOT-jar-with-dependencies.jar`
- Client : `client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Déploiement
Pour déployer une nouvelle version :
1. Pousser un tag Git sur le dépôt. Cela lancera une pipeline GitLab qui génèrera et stockera les JARs compilés sur le dépôt (Package Registry - Registre de paquets).
2. Créer une Release manuellement, qui se base sur ce tag, et fournir pour liens ceux des JARs stockés dans le registre de paquets.
