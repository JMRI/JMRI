package jmri.jmrix.can.cbus.swing.console;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusEventFilterPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventFilterPanelTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusEventFilterFrame frame = new CbusEventFilterFrame();
        CbusEventFilterPanel panel = new CbusEventFilterPanel(frame,1);
        Assert.assertNotNull("exists", panel);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
