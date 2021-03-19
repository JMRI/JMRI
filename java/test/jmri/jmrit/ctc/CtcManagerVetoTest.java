package jmri.jmrit.ctc;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/*
* Test for the CtcManager delete veto actions.
* @author  Dave Sand   Copyright (C) 2020
*/
public class CtcManagerVetoTest {

//     @Rule
//     public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testDeleteVetos() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Masts-SML.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        JUnitUtil.waitFor(5000);     // Wait for block routing and SML initialization

        String msg = "";
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Sensor sensor = sm.getSensor("S-Alpha-Main");
        try {
            sm.deleteBean(sensor, "CanDelete");
        } catch (java.beans.PropertyVetoException ex) {
            msg = ex.getMessage();
        }
        Assert.assertTrue(msg.contains("Sensor is in use by CTC"));

        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        SignalMast mast = smm.getSignalMast("SM-Alpha-Left-A");
        try {
            smm.deleteBean(mast, "CanDelete");
        } catch (java.beans.PropertyVetoException ex) {
            msg = ex.getMessage();
        }
        Assert.assertTrue(msg.contains("Signal Mast is in use by CTC"));

        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout turnout = tm.getTurnout("T-Alpha-Left");
        try {
            tm.deleteBean(turnout, "CanDelete");
        } catch (java.beans.PropertyVetoException ex) {
            msg = ex.getMessage();
        }
        Assert.assertTrue(msg.contains("Turnout is in use by CTC"));

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        Block block = bm.getBlock("B-Alpha-Main");
        try {
            bm.deleteBean(block, "CanDelete");
        } catch (java.beans.PropertyVetoException ex) {
            msg = ex.getMessage();
        }
        Assert.assertTrue(msg.contains("Block is in use by CTC"));
    }

    @Test
    public void testDeleteSignalHeadVeto() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Heads-SSL.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();

        String msg = "";

        SignalHeadManager shm = InstanceManager.getDefault(SignalHeadManager.class);
        SignalHead head = shm.getSignalHead("SH-Alpha-Left-AU");
        try {
            shm.deleteBean(head, "CanDelete");
        } catch (java.beans.PropertyVetoException ex) {
            msg = ex.getMessage();
        }
        Assert.assertTrue(msg.contains("Signal Head is in use by CTC"));
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws Exception {
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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        jmri.util.JUnitUtil.tearDown();
    }
//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcManagerVetoTest.class);
}
