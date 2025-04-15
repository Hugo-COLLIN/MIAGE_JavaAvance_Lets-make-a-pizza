package com.pizzeria;

import java.util.Arrays;
import java.util.List;

public class Pizza {
    private String nom;
    private List<String> ingredients;
    private int prix;

    public Pizza(String nom, List<String> ingredients, int prix) {
        this.nom = nom;
        this.ingredients = ingredients;
        this.prix = prix;
    }

    public String getNom() {
        return nom;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public int getPrix() {
        return prix;
    }

    // Méthode pour sérialiser une pizza
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(nom).append("|");

        // Joindre les ingrédients avec des virgules
        for (int i = 0; i < ingredients.size(); i++) {
            sb.append(ingredients.get(i));
            if (i < ingredients.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("|").append(prix);
        return sb.toString();
    }

    // Méthode pour désérialiser une pizza
    public static Pizza deserialize(String data) {
        String[] parts = data.split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Format de pizza invalide: " + data);
        }

        String nom = parts[0];
        List<String> ingredients = Arrays.asList(parts[1].split(","));
        int prix = Integer.parseInt(parts[2]);

        return new Pizza(nom, ingredients, prix);
    }

    @Override
    public String toString() {
        return "Pizza " + nom + ", ingrédients : " + ingredients + ", prix=" + prix + '}';
    }
}
