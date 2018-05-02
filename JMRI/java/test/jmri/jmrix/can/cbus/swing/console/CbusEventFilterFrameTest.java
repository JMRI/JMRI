package jmri.jmrix.can.cbus.swing.console;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusEventFilterFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventFilterFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusEventFilterFrame pane = new CbusEventFilterFrame();
        Assert.assertNotNull("exists", pane);
    }

    @Test
    public void testPaneCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusConsolePane pane = new CbusConsolePane();
        CbusEventFilterFrame frame = new CbusEventFilterFrame(pane);
        Assert.assertNotNull("exists", frame);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
