package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NodeConfigToolPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventRequestTablePaneTest {

    @Test
    public void testCtor() {
        // Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusEventRequestTablePane t = new CbusEventRequestTablePane();
        Assert.assertNotNull("exists", t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
