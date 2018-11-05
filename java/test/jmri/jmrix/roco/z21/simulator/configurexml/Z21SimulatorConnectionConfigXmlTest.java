package jmri.jmrix.roco.z21.simulator.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.roco.z21.simulator.Z21SimulatorConnectionConfig;

/**
 * Z21SimulatorConnectionConfigXmlTest.java

 Description: tests for the Z21SimulatorConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Z21SimulatorConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractConnectionConfigXmlTestBase {

    @Test
    public void testCtor(){
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new Z21SimulatorConnectionConfigXml();
        cc = new Z21SimulatorConnectionConfig();
    }

    @After
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }

}

