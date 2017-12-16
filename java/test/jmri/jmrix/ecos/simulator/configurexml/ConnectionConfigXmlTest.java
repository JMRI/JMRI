package jmri.jmrix.ecos.simulator.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the EcosSimulatorConnectionConfigXml class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConnectionConfigXmlTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("ConnectionConfigXml constructor", new EcosSimulatorConnectionConfigXml());
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
