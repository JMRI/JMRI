package jmri.jmrix.can.cbus.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * CbusSensorManagerXmlTest.java
 *
 * Test for the CbusSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class CbusSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("CbusSensorManagerXml constructor",new CbusSensorManagerXml());
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

