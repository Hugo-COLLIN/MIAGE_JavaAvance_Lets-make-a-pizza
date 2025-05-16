package com.pizzeria;

import com.pizza.model.adapter.IngredientAdapter;
import lets_make_a_pizza.serveur.Pizzaiolo.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PizzaioloIngredientAdapter implements IngredientAdapter<Ingredient> {

    @Override
    public List<Ingredient> stringToIngredients(List<String> stringIngredients) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (String ing : stringIngredients) {
            ingredients.add(toIngredient(ing));
        }
        return ingredients;
    }

    @Override
    public List<String> ingredientsToString(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(Ingredient::toString)
                .map(this::sanitize)
                .collect(Collectors.toList());
    }

    private Ingredient toIngredient(String s) {
        switch(s) {
            case "sauce tomate": return Ingredient.SAUCE_TOMATE;
            case "tomates cerise": return Ingredient.TOMATES_CERISES;
            case "mozarella": return Ingredient.MOZARELLA;
            case "basilic": return Ingredient.BASILIC;
            case "anchois": return Ingredient.ANCHOIS;
            case "olives": return Ingredient.OLIVES;
            case "fromage": return Ingredient.FROMAGE;
            case "jambon": return Ingredient.JAMBON;
            case "champignons": return Ingredient.CHAMPIGNONS;
            case "poivron": return Ingredient.POIVRON;
            case "artichaut": return Ingredient.ARTICHAUT;
            default: return Ingredient.ANANAS;
        }
    }

    private String sanitize(String str) {
        return str.replace("|", "")
                .replace(",", "")
                .replace(";", "")
                .replace("]", "")
                .replace("[", "");
    }
}
