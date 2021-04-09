package jmri.jmrix.can.cbus;

import java.io.IOException;
import java.nio.file.Path;

import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.swing.modeswitcher.SprogCbusSprog3PlusModeSwitcherFrame;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusDccProgrammerManagerTest {

    @Test
    public void testCTor() {
        CbusDccProgrammerManager t = new CbusDccProgrammerManager(new CbusDccProgrammer(tc),memo);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTor2() {
        CbusDccProgrammerManager t = new CbusDccProgrammerManager(new CbusDccProgrammer(memo),memo);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testInitialPrefs() {
        memo.setSubProtocol(ConfigurationManager.SubProtocol.NONE);
        memo.setProgModeSwitch(ConfigurationManager.ProgModeSwitch.EITHER);
        prefs.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_OFF_MODE);
        Assert.assertEquals(true, prefs.isGlobalProgrammerAvailable());
        Assert.assertEquals(true, prefs.isAddressedModePossible());
        CbusDccProgrammerManager t = new CbusDccProgrammerManager(new CbusDccProgrammer(tc),memo);
        Assert.assertNotNull("exists",t);
        Assert.assertEquals(true , prefs.isGlobalProgrammerAvailable());
        Assert.assertEquals(false , prefs.isAddressedModePossible());
    }
    
    @Test
    public void test3PlusPrefsOff() {
        memo.setSubProtocol(ConfigurationManager.SubProtocol.NONE);
        memo.setProgModeSwitch(ConfigurationManager.ProgModeSwitch.SPROG3PLUS);
        prefs.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_OFF_MODE);
        CbusDccProgrammerManager t = new CbusDccProgrammerManager(new CbusDccProgrammer(tc),memo);
        Assert.assertNotNull("exists",t);
        Assert.assertEquals(true , prefs.isGlobalProgrammerAvailable());
        Assert.assertEquals(true , prefs.isAddressedModePossible());
    }
    
    @Test
    public void test3PlusPrefsAr() {
        memo.setSubProtocol(ConfigurationManager.SubProtocol.NONE);
        memo.setProgModeSwitch(ConfigurationManager.ProgModeSwitch.SPROG3PLUS);
        prefs.setProgTrackMode(SprogCbusSprog3PlusModeSwitcherFrame.PROG_AR_MODE);
        CbusDccProgrammerManager t = new CbusDccProgrammerManager(new CbusDccProgrammer(tc),memo);
        Assert.assertNotNull("exists",t);
        Assert.assertEquals(false , prefs.isGlobalProgrammerAvailable());
        Assert.assertEquals(true , prefs.isAddressedModePossible());
    }
    
    private TrafficControllerScaffold tc;
    private CanSystemConnectionMemo memo;
    private CbusPreferences prefs;

    @TempDir
    protected Path tempDir;
    
    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        
        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        prefs = new CbusPreferences();
        jmri.InstanceManager.store(prefs,CbusPreferences.class );
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDccProgrammerManagerTest.class);

}
