package com.pizzeria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lets_make_a_pizza.serveur.Pizzaiolo.Ingredient;

public class Pizza {
    private String nom;
    private List<Ingredient> ingredients;
    private int prix;

    public Pizza(String nom, List<String> ingredients, int prix) {
        this.nom = nom;
        this.ingredients = toListIngredients(ingredients);
        this.prix = prix;
    }

    public String getNom() {
        return nom;
    }

    public List<Ingredient> getIngredients() {
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

    //fonction pour transformer une liste de String en listIngredient
    public List<Ingredient> toListIngredients(List<String> list){
        List<Ingredient> ingredients = new ArrayList<>();
        list.forEach(ing -> {
            ingredients.add(toIngredient(ing));
        });
        return ingredients;
    }

    public Ingredient toIngredient(String s){
        System.out.println(s);
        switch(s){
            case "sauce tomate" : return Ingredient.SAUCE_TOMATE;
            case "tomates cerise" : return Ingredient.TOMATES_CERISES;
            case "mozarella" : return Ingredient.MOZARELLA;
            case "basilic" : return Ingredient.BASILIC;
            case "anchois" : return Ingredient.SAUCE_TOMATE;
            case "olives" : return Ingredient.OLIVES;
            case "fromage" : return Ingredient.FROMAGE;
            case "jambon" : return Ingredient.JAMBON;
            case "champignons" : return Ingredient.CHAMPIGNONS;
            case "poivron" : return Ingredient.POIVRON;
            case "artichaut" : return Ingredient.ARTICHAUT;
            default : return Ingredient.ANANAS;
        }
    }

    //SAUCE_TOMATE, TOMATES_CERISES, MOZARELLA, BASILIC, ANCHOIS, OLIVES, FROMAGE, JAMBON, CHAMPIGNONS, POIVRON, ARTICHAUT, ANANAS
}
