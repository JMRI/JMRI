package jmri.jmrit.ctc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/*
* Test for the CtcManager delete veto actions.
* @author  Dave Sand   Copyright (C) 2020
*/
public class CtcManagerVetoTest {

    @Test
    @DisabledIfHeadless
    public void testDeleteVetos() throws JmriException {

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Masts-SML.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        JUnitUtil.waitFor(5000);     // Wait for block routing and SML initialization

        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor = sm.provideSensor("S-Alpha-Main");
        PropertyVetoException ex = assertThrows(PropertyVetoException.class, () ->
            sm.deleteBean(sensor, "CanDelete"));
        assertTrue(ex.getMessage().contains("Sensor is in use by CTC"));

        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        SignalMast mast = smm.getSignalMast("SM-Alpha-Left-A");
        assertNotNull(mast);
        ex = assertThrows(PropertyVetoException.class, () ->
            smm.deleteBean(mast, "CanDelete"));
        assertTrue(ex.getMessage().contains("Signal Mast is in use by CTC"));

        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout = tm.provideTurnout("T-Alpha-Left");
        ex = assertThrows(PropertyVetoException.class, () ->
            tm.deleteBean(turnout, "CanDelete"));
        assertTrue(ex.getMessage().contains("Turnout is in use by CTC"));

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        Block block = bm.getBlock("B-Alpha-Main");
        assertNotNull(block);
        ex = assertThrows(PropertyVetoException.class, () ->
            bm.deleteBean(block, "CanDelete"));
        assertTrue(ex.getMessage().contains("Block is in use by CTC"));
    }

    @Test
    public void testDeleteSignalHeadVeto() throws JmriException {

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Heads-SSL.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();

        SignalHeadManager shm = InstanceManager.getDefault(SignalHeadManager.class);
        SignalHead head = shm.getSignalHead("SH-Alpha-Left-AU");
        assertNotNull(head);
        PropertyVetoException ex = assertThrows(PropertyVetoException.class, () ->
            shm.deleteBean(head, "CanDelete"));
        assertTrue(ex.getMessage().contains("Signal Head is in use by CTC"));
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearBlockBossLogicThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.util.JUnitUtil.tearDown();
    }
//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcManagerVetoTest.class);
}
