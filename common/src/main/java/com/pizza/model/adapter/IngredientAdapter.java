package com.pizza.model.adapter;

import java.util.List;

/**
 * Interface pour adapter les ingrédients entre différentes implémentations
 */
public interface IngredientAdapter<T> {
    /**
     * Convertit une liste de chaînes en liste d'ingrédients spécifiques
     */
    List<T> stringToIngredients(List<String> stringIngredients);

    /**
     * Convertit une liste d'ingrédients spécifiques en liste de chaînes
     */
    List<String> ingredientsToString(List<T> ingredients);
}
