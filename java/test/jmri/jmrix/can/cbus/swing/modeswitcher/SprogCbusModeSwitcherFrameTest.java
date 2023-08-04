package jmri.jmrix.can.cbus.swing.modeswitcher;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the ModeSwitcherPane class
 *
 * @author Andrew Crosland (C) 2020
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class SprogCbusModeSwitcherFrameTest extends jmri.util.JmriJFrameTestBase {

    private CanSystemConnectionMemo memo = null;
    private jmri.jmrix.can.TrafficController tc = null;
    
    @Test
    public void testInitSetup () throws Exception{
        // for now, just makes ure there isn't an exception.
        ((SprogCbusModeSwitcherFrame) frame).initSetup();
    }
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        
        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.SPROGCBUS);
        memo.setTrafficController(tc);
        memo.configureManagers();
        frame = new SprogCbusModeSwitcherFrame(memo, "SPROG CBUS Mode Switcher Frame test");
    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(tc);
        tc.terminateThreads();
        tc = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }
}
