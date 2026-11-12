package jmri.jmrit.entryexit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
@DisabledIfHeadless
public class PointDetailsTest {

    private static EntryExitTestTools tools;
    private static HashMap<String, LayoutEditor> panels = new HashMap<>();

    private static EntryExitPairs eep;
    private static LayoutBlockManager lbm;
    private static SensorManager sm;

    @Test
    public void testCTor() {
        LayoutBlock f = lbm.getLayoutBlock("B-Alpha-East");  // NOI18N
        LayoutBlock p = lbm.getLayoutBlock("B-Alpha-Beta");  // NOI18N
        List<LayoutBlock> blockList = new ArrayList<>();
        blockList.add(p);
        PointDetails pd = new PointDetails(f,blockList);
        assertNotNull(pd, "create failed");
    }

    @Test
    public void testGetters() {
        PointDetails pd = tools.getPoint(sm.getSensor("NX-AW"), panels.get("Alpha"), eep);  // NOI18N
        assertNotNull(pd, "fetch failed");
        LayoutBlock fblk = pd.getFacing();
        assertNotNull(fblk, "getFacing");
        List<LayoutBlock> pblk = pd.getProtecting();
        assertEquals(1, pblk.size(), "getProtecting");

        Sensor sensor = pd.getSensor();
        assertNotNull(sensor, "getSensor");
        NamedBean signal = pd.getSignal();
        assertNull(signal, "getSignal");
    }

    @Test
    public void testNxButton() {
        PointDetails pd = tools.getPoint(sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);  // NOI18N
        assertNotNull(pd, "PointDetails fetch failed");
        pd.setButtonState(EntryExitPairs.NXBUTTONACTIVE);
        pd.setNXState(EntryExitPairs.NXBUTTONINACTIVE);
        int nxState = pd.getNXState();
        assertEquals(EntryExitPairs.NXBUTTONINACTIVE, nxState, "Button is active");
    }

    @Test
    public void testButtonTimeOutSetsSensorOnLayoutThread() {
        PointDetails pd = tools.getPoint(sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);  // NOI18N
        assertNotNull(pd, "PointDetails fetch failed");
        Sensor sensor = pd.getSensor();
        // set the button, and its sensor, active as NX does, not as a click on the button
        pd.setNXButtonState(EntryExitPairs.NXBUTTONACTIVE);
        assertEquals(Sensor.ACTIVE, sensor.getKnownState(), "button sensor active");
        List<Boolean> setOnLayoutThread = java.util.Collections.synchronizedList(new ArrayList<>());
        java.beans.PropertyChangeListener l = e -> setOnLayoutThread.add(jmri.util.ThreadingUtil.isLayoutThread());
        sensor.addPropertyChangeListener("KnownState", l);

        // the button is pressed, starting its time out, which ends when cancelled
        pd.setNXState(EntryExitPairs.NXBUTTONSELECTED);
        pd.cancelNXButtonTimeOut();

        JUnitUtil.waitFor(() -> sensor.getKnownState() == Sensor.INACTIVE, "button set inactive");
        sensor.removePropertyChangeListener("KnownState", l);
        // other NX activity on the test panels may also change the sensor; all of it on the layout thread
        assertTrue(setOnLayoutThread.contains(true), "button sensor set");
        assertFalse(setOnLayoutThread.contains(false), "button sensor set on the layout thread");
        assertEquals(EntryExitPairs.NXBUTTONINACTIVE, pd.getNXState(), "button inactive");
    }

    @BeforeAll
    public static void setUp() throws JmriException {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        tools = new EntryExitTestTools();
        panels = EntryExitTestTools.getPanels();
        assertEquals(2, panels.size(), "Get LE panels");
        eep = InstanceManager.getDefault(EntryExitPairs.class);
        lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        sm = InstanceManager.getDefault(SensorManager.class);
    }

    @AfterAll
    public static void tearDown() {
        panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PointDetailsTest.class);

}
