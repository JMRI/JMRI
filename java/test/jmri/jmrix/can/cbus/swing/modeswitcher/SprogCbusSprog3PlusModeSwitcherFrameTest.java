package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.GraphicsEnvironment;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusDccProgrammer;
import jmri.jmrix.can.cbus.CbusDccProgrammerManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Tests for the ModeSwitcherPane class
 *
 * @author Andrew Crosland (C) 2020
 */
public class SprogCbusSprog3PlusModeSwitcherFrameTest extends jmri.util.JmriJFrameTestBase {

    CanSystemConnectionMemo memo;
    CbusDccProgrammer prog;
    jmri.jmrix.can.TrafficController tc;
    CbusPreferences preferences;
    CbusDccProgrammerManager pm;
    
    @Test
    public void testInitComponents () throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // for now, just makes ure there isn't an exception.
        ((SprogCbusSprog3PlusModeSwitcherFrame) frame).initComponents();
    }
    
    @Test
    public void testPrefOff () throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Create global programer and matching preferences
        preferences.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_OFF_MODE);
        
        SprogCbusSprog3PlusModeSwitcherFrame f = ((SprogCbusSprog3PlusModeSwitcherFrame) frame);

        f.initComponents();
        Assert.assertEquals(f.mode, SprogCbusSprog3PlusModeSwitcherFrame.PROG_OFF_MODE);
    }
    
    @Test
    public void testPrefOn () throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Create global programer and matching preferences
        preferences.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_ON_MODE);
        
        SprogCbusSprog3PlusModeSwitcherFrame f = ((SprogCbusSprog3PlusModeSwitcherFrame) frame);

        f.initComponents();
        Assert.assertEquals(f.mode, SprogCbusSprog3PlusModeSwitcherFrame.PROG_ON_MODE);
    }
    
    @Test
    public void testPrefAr () throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Create global programer and matching preferences
        preferences.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_AR_MODE);
        
        SprogCbusSprog3PlusModeSwitcherFrame f = ((SprogCbusSprog3PlusModeSwitcherFrame) frame);

        f.initComponents();
        Assert.assertEquals(f.mode, SprogCbusSprog3PlusModeSwitcherFrame.PROG_AR_MODE);
    }
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );

        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        prog = new CbusDccProgrammer(tc);
         
        preferences = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
         
        jmri.InstanceManager.setDefault(GlobalProgrammerManager.class,new CbusDccProgrammerManager(prog, memo) );
        jmri.InstanceManager.setDefault(AddressedProgrammerManager.class,new CbusDccProgrammerManager(prog, memo) );
        pm = (CbusDccProgrammerManager)InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
        pm.mySetGlobalProgrammerAvailable(true);
        pm.setAddressedModePossible(true);
        
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new SprogCbusSprog3PlusModeSwitcherFrame(memo);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        pm = null;
        preferences = null;
        prog = null;
        tc.terminateThreads();
        memo.dispose();
        tc = null;
        memo = null;
        super.tearDown();
    }
}
