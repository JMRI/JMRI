package jmri.jmrix.can.cbus.swing.bootloader;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the BootloaderPane class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class CbusBootloaderPaneTest extends jmri.util.swing.JmriPanelTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    jmri.jmrix.can.TrafficController tc = null;

    @Override 
    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes ure there isn't an exception.
        ((CbusBootloaderPane) panel).initComponents(memo);
    }

    @Test
    public void testInitComponentsNoArgs() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // for now, just makes ure there isn't an exception.
        ((CbusBootloaderPane) panel).initComponents();
    }

    @Test
    public void testInitContext() throws Exception {
        // for now, just makes ure there isn't an exception.
        ((CbusBootloaderPane) panel).initContext(memo);
    }


    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.TrafficControllerScaffold();
        memo.setTrafficController(tc);
        panel = new CbusBootloaderPane();
        helpTarget="package.jmri.jmrix.can.cbus.swing.bootloader.CbusBootloaderPane";
        title="CBUS Firmware Update";
    }

    @After
    @Override
    public void tearDown() {        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


    
}
