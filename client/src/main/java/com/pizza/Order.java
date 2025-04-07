package com.pizza;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Order {
    private final String id;
    private final Map<String, Integer> pizzaQuantities;
    private OrderStatus status;

    public enum OrderStatus {
        PENDING,
        VALIDATED,
        PREPARING,
        COOKING,
        DELIVERING,
        DELIVERED,
        CANCELLED
    }

    public Order() {
        this.id = UUID.randomUUID().toString();
        this.pizzaQuantities = new HashMap<>();
        this.status = OrderStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public void addPizza(String pizzaName, int quantity) {
        if (quantity > 0 && quantity < 10) {
            pizzaQuantities.put(pizzaName, quantity);
        } else {
            throw new IllegalArgumentException("La quantité doit être entre 1 et 9");
        }
    }

    public void removePizza(String pizzaName) {
        pizzaQuantities.remove(pizzaName);
    }

    public Map<String, Integer> getPizzaQuantities() {
        return new HashMap<>(pizzaQuantities);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> entry : pizzaQuantities.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }

    public static Order deserialize(String id, String data) {
        Order order = new Order();
        // Remplacer l'ID généré automatiquement par celui fourni
        try {
            java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);
        } catch (Exception e) {
            System.err.println("Erreur lors de la définition de l'ID: " + e.getMessage());
        }

        if (data != null && !data.isEmpty()) {
            String[] items = data.split(",");
            for (String item : items) {
                String[] parts = item.split(":");
                if (parts.length == 2) {
                    String pizzaName = parts[0];
                    int quantity = Integer.parseInt(parts[1]);
                    order.addPizza(pizzaName, quantity);
                }
            }
        }
        return order;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Commande #").append(id.substring(0, 8)).append("\n");
        sb.append("Statut: ").append(status).append("\n");
        sb.append("Pizzas:\n");
        for (Map.Entry<String, Integer> entry : pizzaQuantities.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(" x").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
