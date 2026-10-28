package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.implementation.VirtualSignalHead;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Check that a signal controlled warrant held for its next block listens to
 * that block once, and stops listening when it may move on.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SCWarrantBlockListenerTest {

    private SCWarrant warrant;
    private OBlock bNorth;
    private OBlock bWest;
    private OBlock bSouth;

    private int warrantListenersOn(OBlock block) {
        int n = 0;
        for (java.beans.PropertyChangeListener l : block.getPropertyChangeListeners()) {
            if (l == warrant) {
                n++;
            }
        }
        return n;
    }

    // wake the train runner so it makes another pass of its loop
    private void wakeRunner() {
        synchronized (warrant) {
            warrant.notifyAll();
        }
    }

    @Test
    public void testHeldTrainListensOnceToNextBlock() throws JmriException {
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(bNorth, "NorthToWest", "", "NorthWest"));
        orders.add(new BlockOrder(bWest, "SouthToNorth", "NorthWest", "SouthWest"));
        orders.add(new BlockOrder(bSouth, "SouthToWest", "SouthWest", null));
        warrant.getSpeedUtil().setAddress("999(L)");
        warrant.setBlockOrders(orders);
        warrant.setRoute(false, orders);
        warrant.setTrainName("TestTrain");

        assertNull(warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false));
        JUnitUtil.waitFor(() -> {
            wakeRunner();
            return warrant.getRunningMessage().endsWith("IH1 showing appearance 16");
        }, "train starts to move");

        // the next block can no longer be entered: the train is held for it
        ThreadingUtil.runOnLayout(() -> bWest.setOutOfService(true));
        JUnitUtil.waitFor(() -> {
            wakeRunner();
            return warrantListenersOn(bWest) > 0;
        }, "warrant listens to the block it is held for");
        for (int i = 0; i < 5; i++) {
            wakeRunner();
            JUnitUtil.waitFor(50);
        }
        assertEquals(1, warrantListenersOn(bWest), "one listener however long the train is held");

        // block usable again: the train moves on and stops listening
        ThreadingUtil.runOnLayout(() -> bWest.setOutOfService(false));
        JUnitUtil.waitFor(() -> {
            wakeRunner();
            return warrantListenersOn(bWest) == 0;
        }, "listener removed when the train may move on");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        WarrantPreferences.getDefault().setSpeedAssistance(0);
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        OBlockManager blkMgr = InstanceManager.getDefault(OBlockManager.class);
        bWest = blkMgr.createNewOBlock("OB1", "West");
        bNorth = blkMgr.createNewOBlock("OB3", "North");
        bSouth = blkMgr.createNewOBlock("OB4", "South");

        SignalHeadManager shMgr = InstanceManager.getDefault(SignalHeadManager.class);
        SignalHead shNW = new VirtualSignalHead("IH1");
        shNW.setAppearance(SignalHead.GREEN);
        shMgr.register(shNW);
        SignalHead shSW = new VirtualSignalHead("IH2");
        shSW.setAppearance(SignalHead.GREEN);
        shMgr.register(shSW);

        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal pNorthWest = portalMgr.createNewPortal("NorthWest");
        pNorthWest.setToBlock(bWest, false);
        pNorthWest.setFromBlock(bNorth, false);
        pNorthWest.setProtectSignal(shNW, 20, bWest);
        Portal pSouthWest = portalMgr.createNewPortal("SouthWest");
        pSouthWest.setToBlock(bWest, false);
        pSouthWest.setFromBlock(bSouth, false);
        pSouthWest.setProtectSignal(shSW, 20, bWest);

        bNorth.addPath(new OPath("NorthToWest", bNorth, null, pNorthWest, new ArrayList<>()));
        OPath path = new OPath("SouthToNorth", bWest, pNorthWest, pSouthWest, new ArrayList<>());
        path.setLength(200);
        bWest.addPath(path);
        bSouth.addPath(new OPath("SouthToWest", bSouth, null, pSouthWest, new ArrayList<>()));
        bSouth.setLength(100);

        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor sWest = sensorMgr.newSensor("IS1", "WestSensor");
        Sensor sNorth = sensorMgr.newSensor("IS3", "NorthSensor");
        Sensor sSouth = sensorMgr.newSensor("IS4", "SouthSensor");
        bWest.setSensor("IS1");
        bNorth.setSensor("IS3");
        bSouth.setSensor("IS4");
        try {
            sWest.setState(Sensor.INACTIVE);
            sSouth.setState(Sensor.INACTIVE);
            sNorth.setState(Sensor.ACTIVE);     // train in the start block
        } catch (JmriException e) {
            fail("sensor state", e);
        }
        warrant = new SCWarrant("IW1", "SCWarrant listener test", 5);
    }

    @AfterEach
    public void tearDown() {
        // stop on the GUI thread, where block events to the warrant are delivered
        ThreadingUtil.runOnGUI(() -> warrant.stopWarrant(true, true));
        wakeRunner();
        JUnitUtil.waitFor(() -> warrant.getRunMode() == Warrant.MODE_NONE, "warrant stopped");
        warrant = null;
        JUnitUtil.removeMatchingThreads("SCTrainRunner");
        JUnitUtil.tearDown();
    }

}
