package jmri.jmrix.roco.z21.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Z21SensorManagerXml.java
 *
 * Description: tests for the Z21SensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Z21SensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Z21SensorManagerXml constructor",new Z21SensorManagerXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

