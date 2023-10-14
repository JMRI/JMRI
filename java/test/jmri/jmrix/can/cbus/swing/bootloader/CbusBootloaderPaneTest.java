package jmri.jmrix.can.cbus.swing.bootloader;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the BootloaderPane class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
@DisabledIfSystemProperty( named = "java.awt.headless", matches = "true")
public class CbusBootloaderPaneTest extends jmri.util.swing.JmriPanelTest {

    private jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    private jmri.jmrix.can.TrafficController tcis = null;

    @Override 
    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes ure there isn't an exception.
        ((CbusBootloaderPane) panel).initComponents(memo);
    }

    @Test
    public void testInitComponentsNoArgs() throws Exception{
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
        
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        // memo.configureManagers();
        
        panel = new CbusBootloaderPane();
        helpTarget="package.jmri.jmrix.can.cbus.swing.bootloader.CbusBootloaderPane";
        title="CBUS Firmware Update";
    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        tcis = null;
        memo = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }


    
}
