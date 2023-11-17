package jmri.jmrix.bidib.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBSensorManagerXml class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
        BiDiBTurnoutManagerXml t = new BiDiBTurnoutManagerXml();
        Assertions.assertNotNull(t, "BiDiBTurnoutManagerXml constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
