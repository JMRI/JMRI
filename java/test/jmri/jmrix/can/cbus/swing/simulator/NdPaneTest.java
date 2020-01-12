package jmri.jmrix.can.cbus.swing.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NdPane
 *
 * @author Steve Young Copyright (C) 2019
 */
public class NdPaneTest  {

    @Test
    public void testCTor() {
        NdPane t = new NdPane();
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
