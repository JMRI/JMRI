package jmri.jmrix.can.cbus.swing.modeswitcher;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusDccProgrammerManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the ModeSwitcherPane class
 *
 * @author Andrew Crosland (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class SprogCbusSimpleModeSwitcherFrameTest extends jmri.util.JmriJFrameTestBase {

    private CanSystemConnectionMemo memo = null;
    private jmri.jmrix.can.TrafficController tc = null;
    private CbusPreferences preferences = null;
    private CbusDccProgrammerManager pm;

    @Test
    public void testInitComponents () throws Exception{
        // for now, just makes ure there isn't an exception.
        ((SprogCbusSimpleModeSwitcherFrame) frame).initComponents();
    }

    @Test
    public void testPrefProg () throws Exception {

        // Create global programer and matching preferences
        pm = (CbusDccProgrammerManager)InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        Assertions.assertNotNull(pm);
        pm.setGlobalProgrammerAvailable(true);
        pm.setAddressedModePossible(false);
        Assertions.assertNotNull(preferences);
        preferences.setProgrammersAvailable(true, false);

        SprogCbusSimpleModeSwitcherFrame f = ((SprogCbusSimpleModeSwitcherFrame) frame);

        f.initComponents();
        Assert.assertEquals(SprogCbusSimpleModeSwitcherFrame.PROG_MODE, f.mode);
    }

    @Test
    public void testPrefCmd () throws Exception {

        // Create addressed programer and matching preferences
        pm = (CbusDccProgrammerManager)InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        Assertions.assertNotNull(pm);
        pm.setGlobalProgrammerAvailable(false);
        pm.setAddressedModePossible(true);
        Assertions.assertNotNull(preferences);
        preferences.setProgrammersAvailable(false, true);

        SprogCbusSimpleModeSwitcherFrame f = ((SprogCbusSimpleModeSwitcherFrame) frame);

        f.initComponents();
        Assert.assertEquals( SprogCbusSimpleModeSwitcherFrame.CMD_MODE, f.mode);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);
        memo.configureManagers();

        preferences = memo.get(jmri.jmrix.can.cbus.CbusPreferences.class);

        frame = new SprogCbusSimpleModeSwitcherFrame(memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        pm = null;
        preferences = null;
        Assertions.assertNotNull(tc);
        tc.terminateThreads();
        tc = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.clearShutDownManager();
        super.tearDown();
    }
}
