package jmri.jmrix.ecos.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EcosSimulatorConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class EcosSimulatorConnectionConfigTest {

    @Test
    public void constructorTest() {
        Assert.assertNotNull("ConnectionConfig constructor", new EcosSimulatorConnectionConfig());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
