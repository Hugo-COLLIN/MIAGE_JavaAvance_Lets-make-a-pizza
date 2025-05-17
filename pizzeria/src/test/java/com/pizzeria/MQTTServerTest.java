package com.pizzeria;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MQTTServerTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MQTTServerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MQTTServerTest.class );
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testTrouverDansCatalogue() {
    }
}