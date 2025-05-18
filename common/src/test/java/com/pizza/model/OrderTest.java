package com.pizza.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Map;
import java.util.Optional;

public class OrderTest extends TestCase {
    private Order order;
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public OrderTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( OrderTest.class );
    }

    public void setUp() throws Exception {
        super.setUp();
        order = new Order();
        order.addPizza("pizzaTime", 5);
    }

    public void tearDown() throws Exception {
        order = null;
        super.tearDown();
    }

    public void testGetPizzaQuantities() {
        assertEquals(5, (int)order.getPizzaQuantities().get("pizzaTime"));
    }

    // Tester le getter d'ID ne semble pas pertinent : il est généré par UUID.randomUUID et non modifiable
    public void testAddPizza() {
        // Méthode déjà utilisée dans setUp
        assertEquals(5, (int) order.getPizzaQuantities().get("pizzaTime"));

        try {
            order.addPizza("bonjourjefais300000commandesetquelquesjesuisrigolovousavezvu", 476237);
            fail("Missing exception");
        } catch (IllegalArgumentException ignored) {
            // Test passé
        }
    }

    public void testSerialize() {

    }

    public void testDeserialize() {

    }

    public void testSerializeAndDeserialize() {

    }

    public void testToString() {

    }
}