# Pizzeria

La pizzeria est une application qui permet de gérer les commandes de pizzas.

## Prérequis
- Maven
- Java 21

## Démarrage

```bash

mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=libs/pizzaiolo.jar -DgroupId="lets-make-a-pizza" -DartifactId="pizzaiolo" -Dversion="0.0.1" -Dpackaging="jar" \
 && mvn clean package && java -jar target/pizza-mqtt-1.0-SNAPSHOT.jar
```
