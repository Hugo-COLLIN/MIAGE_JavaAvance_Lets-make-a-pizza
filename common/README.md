# Common
Ce module a été créé pour éviter la duplication de code entre le client et la pizzeria. Il contient les classes de modèle communes entre les deux composants.

## Utilisation
Ce module est importé comme dépendance dans les modules client et pizzeria. Il n'a pas vocation a être lancé séparément.

Il est important de garder à l'esprit que les modifications impacteront à la fois le client et la pizzeria. Après modification, il faut recompiler les deux modules qui en dépendent.

## Prérequis
- Java 21
