package jmri.jmrix.can.cbus.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * CbusLightManagerXmlTest.java
 *
 * Test for the CbusLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class CbusLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("CbusLightManagerXml constructor",new CbusLightManagerXml());
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

