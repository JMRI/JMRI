package jmri.jmrix.can.cbus.swing.simulator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusSlotMonitorPane.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CsPaneTest  {

    @Test
    public void testCTor() {
        CsPane t = new CsPane(null);
        Assert.assertNotNull("exists",t);
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
