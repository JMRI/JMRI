package jmri.jmrix.roco.z21.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Z21SensorManagerXml.java
 *
 * Test for the Z21SensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Z21SensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Z21SensorManagerXml constructor",new Z21SensorManagerXml());
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

