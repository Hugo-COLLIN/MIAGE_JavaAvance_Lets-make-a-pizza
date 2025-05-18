package com.pizza.model;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

public class PizzaTest extends TestCase {
    private Pizza pizza;
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PizzaTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PizzaTest.class );
    }

    public void setUp() throws Exception {
        super.setUp();
        pizza = new Pizza("37 fromages", List.of("un fromage", "mélange douteux"), 13);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        pizza = null;
    }

    public void testGetNom() {
        assertEquals("37 fromages", pizza.getNom());
    }

    public void testGetPrix() {
        assertEquals(13, pizza.getPrix());
    }

    public void testSerialize() {
        String serializedStr = "37 fromages|un fromage,mélange douteux|13";
        assertEquals(serializedStr, pizza.serialize());
    }

    public void testDeserialize() {
        Pizza pizzaTested = Pizza.deserialize("37 fromages|un fromage,mélange douteux|13");
        assertEquals(pizza.getNom(), pizzaTested.getNom());
        assertEquals(pizza.getPrix(), pizzaTested.getPrix());

        // Les tests en JUnit 3 sont comme ça :
        try {
            Pizza.deserialize("");
            fail("Missing exception");
        } catch (IllegalArgumentException ignored) {
            // Test passé
        }

        try {
            Pizza.deserialize("a|09");
            fail("Missing exception");
        } catch (IllegalArgumentException ignored) {
            // Test passé
        }

        try {
            Pizza.deserialize("a|09a,testquiteste|09a|09");
            fail("Missing exception");
        } catch (IllegalArgumentException ignored) {
            // Test passé
        }
    }

    /**
     * Test pour montrer que la désérialisation est l'inverse de la sérialisation,
     * et permet qu'on s'assure que ces opérations ne modifient pas l'objet
     */
    public void testSerializedAndDeserialized() {
        String serializedStr = "37 fromages|un fromage,mélange douteux|13";
        assertEquals(serializedStr, pizza.serialize());
        assertEquals(pizza.getNom(), Pizza.deserialize(pizza.serialize()).getNom());
    }

    public void testToString() {
        assertEquals("37 fromages - 13€", pizza.toString());
    }
}