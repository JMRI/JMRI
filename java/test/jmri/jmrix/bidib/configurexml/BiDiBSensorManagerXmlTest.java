package jmri.jmrix.bidib.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBSensorManagerXml class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSensorManagerXmlTest {

    @Test
    public void testCtor(){
        BiDiBSensorManagerXml t = new BiDiBSensorManagerXml();
        Assertions.assertNotNull(t, "BiDiBSensorManagerXml constructor");
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
