package jmri.jmrix.cmri.serial.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SerialLightManagerXmlTest.java
 *
 * Test for the SerialLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialLightManagerXml constructor",new SerialLightManagerXml());
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

