# Pizzeria

La pizzeria est une application qui permet de gérer les commandes de pizzas.

## Fonctionnalités
- Réception des commandes via MQTT 
- Envoi du menu des pizzas disponibles 
- Traitement parallèle de plusieurs commandes 
- Interaction avec le Pizzaiolo pour la préparation et la cuisson des pizzas 
- Notification en temps réel de l'état des commandes 
- Gestion des erreurs et des cas particuliers (ingrédients indisponibles, etc.)

## Prérequis
- Java 21
- Maven

## Démarrage

```bash
mvn clean package && java -jar target/pizza-mqtt-1.0-SNAPSHOT-jar-with-dependencies.jar
```
