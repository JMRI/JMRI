package jmri.jmrix.roco.z21.simulator.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Z21SimulatorConnectionConfigXmlTest.java

 Description: tests for the Z21SimulatorZ21SimulatorConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Z21SimulatorConnectionConfigXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Z21SimulatorConnectionConfigXml constructor",new Z21SimulatorConnectionConfigXml());
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

