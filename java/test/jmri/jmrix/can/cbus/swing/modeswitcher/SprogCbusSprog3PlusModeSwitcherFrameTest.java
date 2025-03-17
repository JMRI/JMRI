package jmri.jmrix.can.cbus.swing.modeswitcher;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusDccProgrammerManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the ModeSwitcherPane class
 *
 * @author Andrew Crosland (C) 2020
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class SprogCbusSprog3PlusModeSwitcherFrameTest extends jmri.util.JmriJFrameTestBase {

    private CanSystemConnectionMemo memo = null;
    private jmri.jmrix.can.TrafficController tc = null;
    private CbusPreferences preferences = null;
    private CbusDccProgrammerManager pm;

    @Test
    public void testInitComponents () {
        Assertions.assertDoesNotThrow( () ->
            frame.initComponents());
    }

    @Test
    public void testPrefOff () {

        // Create global programer and matching preferences
        Assertions.assertNotNull(preferences);
        preferences.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_OFF_MODE);

        SprogCbusSprog3PlusModeSwitcherFrame f = ((SprogCbusSprog3PlusModeSwitcherFrame) frame);

        f.initComponents();
        Assertions.assertEquals( SprogCbusSprog3PlusModeSwitcherFrame.PROG_OFF_MODE, f.mode);
    }

    @Test
    public void testPrefOn () {

        // Create global programer and matching preferences
        Assertions.assertNotNull(preferences);
        preferences.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_ON_MODE);

        SprogCbusSprog3PlusModeSwitcherFrame f = ((SprogCbusSprog3PlusModeSwitcherFrame) frame);

        f.initComponents();
        Assertions.assertEquals( SprogCbusSprog3PlusModeSwitcherFrame.PROG_ON_MODE, f.mode);
    }

    @Test
    public void testPrefAr () {

        // Create global programer and matching preferences
        Assertions.assertNotNull(preferences);
        preferences.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_AR_MODE);

        SprogCbusSprog3PlusModeSwitcherFrame f = ((SprogCbusSprog3PlusModeSwitcherFrame) frame);

        f.initComponents();
        Assertions.assertEquals( SprogCbusSprog3PlusModeSwitcherFrame.PROG_AR_MODE, f.mode);
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

        preferences = memo.get(CbusPreferences.class);
        pm = (CbusDccProgrammerManager)InstanceManager.getNullableDefault(GlobalProgrammerManager.class);

        Assertions.assertNotNull(pm);
        pm.mySetGlobalProgrammerAvailable(true);
        pm.setAddressedModePossible(true);

        frame = new SprogCbusSprog3PlusModeSwitcherFrame(memo);
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
