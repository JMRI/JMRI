package jmri.jmrix.can.cbus.swing.console;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusConsolePane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusConsolePaneTest extends jmri.util.swing.JmriPanelTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    jmri.jmrix.can.TrafficController tc = null;

    @Override 
    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes ure there isn't an exception.
        ((CbusConsolePane) panel).initComponents(memo);
    }

    @Test
    public void testInitComponentsNoArgs() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // for now, just makes ure there isn't an exception.
        ((CbusConsolePane) panel).initComponents();
    }

    @Test
    public void testInitContext() throws Exception {
        // for now, just makes ure there isn't an exception.
        ((CbusConsolePane) panel).initContext(memo);
    }


    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.TrafficControllerScaffold();
        memo.setTrafficController(tc);
        panel = new CbusConsolePane();
        helpTarget="package.jmri.jmrix.can.cbus.swing.console.CbusConsoleFrame";
        title="CBUS Console";
    }

    @After
    @Override
    public void tearDown() {        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


}
