package jmri.jmrix.can.cbus.swing.bootloader;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Tests for the BootloaderPane class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class CbusBootloaderPaneTest extends jmri.util.swing.JmriPanelTest {

    private jmri.jmrix.can.CanSystemConnectionMemo memo;
    private jmri.jmrix.can.TrafficController tcis;

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


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tcis = new jmri.jmrix.can.TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );
        
        panel = new CbusBootloaderPane();
        helpTarget="package.jmri.jmrix.can.cbus.swing.bootloader.CbusBootloaderPane";
        title="CBUS Firmware Update";
    }

    @AfterEach
    @Override
    public void tearDown() {
        
        tcis.terminateThreads();
        memo.dispose();
        tcis = null;
        memo = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }


    
}
