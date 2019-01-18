package jmri.jmrix.can.cbus.swing.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusSlotMonitorPane.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DirectionPaneTest  {

    @Test
    public void testCTor() {
        DirectionPane t = new DirectionPane();
        Assert.assertNotNull("exists",t);
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
