package jmri.jmrix.can.cbus.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
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
public class CbusEventHighlightFrameTest extends jmri.util.JmriJFrameTestBase{

    @Test
    public void testPaneCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusConsolePane pane = new CbusConsolePane();
        CbusEventHighlightFrame frame = new CbusEventHighlightFrame(pane,null);
        Assert.assertNotNull("exists", frame);
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new CbusEventHighlightFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();    
    }


}
