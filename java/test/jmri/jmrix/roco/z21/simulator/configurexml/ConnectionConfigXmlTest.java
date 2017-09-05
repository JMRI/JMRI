package jmri.jmrix.roco.z21.simulator.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConnectionConfigXmlTest.java

 Description: tests for the Z21SimulatorConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ConnectionConfigXml constructor",new Z21SimulatorConnectionConfigXml());
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

