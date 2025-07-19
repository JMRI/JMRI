package jmri.jmrix.can.cbus.swing.modeswitcher;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the ModeSwitcherPane class
 *
 * @author Andrew Crosland (C) 2020
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class SprogCbusModeSwitcherFrameTest extends jmri.util.JmriJFrameTestBase {

    private CanSystemConnectionMemo memo = null;
    private jmri.jmrix.can.TrafficController tc = null;

    @Test
    public void testInitSetup () {
        Assertions.assertTrue(((SprogCbusModeSwitcherFrame) frame).initSetup(),
            "frame setup, GlobalProgrammerManager found");
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
