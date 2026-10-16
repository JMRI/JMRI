package jmri.jmrit.entryexit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for NX routes made of more than one entry/exit pair.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class EntryExitMultiPointRouteTest {

    private HashMap<String, LayoutEditor> panels = new HashMap<>();
    private EntryExitPairs eep;
    private SensorManager sm;
    private Turnout tAW;
    private Turnout tAE;

    private void setPairsType(int type) {
        for (DestinationPoints dp : eep.getNamedBeanSet()) {
            dp.setEntryExitType(type);
        }
    }

    /**
     * Click an NX button: its sensor changes to the other state.
     *
     * @param name the name of the sensor
     */
    private void pressButton(String name) {
        Sensor sensor = sm.getSensor(name);
        assertNotNull(sensor, name);
        ThreadingUtil.runOnLayout(() -> sensor.setCommandedState(
                sensor.getKnownState() == Sensor.ACTIVE ? Sensor.INACTIVE : Sensor.ACTIVE));
    }

    private static boolean routeSettingInProgress() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isAlive() && ("NX set route thread".equals(t.getName()) || "Entry Exit Set Route".equals(t.getName()))) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testAllSegmentsSetWhenNotFullInterlock() {
        setPairsType(EntryExitPairs.SETUPTURNOUTSONLY);

        // From the east end over the main line to the west end, which takes two NX pairs
        pressButton("NX-AE");
        pressButton("NX-Alpha-EB");

        JUnitUtil.waitFor(() -> tAE.getKnownState() == Turnout.CLOSED, "first segment set");
        JUnitUtil.waitFor(() -> tAW.getKnownState() == Turnout.CLOSED, "second segment set");
        JUnitUtil.waitFor(() -> !routeSettingInProgress(), "route setting finished");
    }

    @Test
    public void testNoLeftoverSegmentsForTheNextRoute() {
        setPairsType(EntryExitPairs.SETUPTURNOUTSONLY);

        pressButton("NX-AE");
        pressButton("NX-Alpha-EB");
        JUnitUtil.waitFor(() -> tAE.getKnownState() == Turnout.CLOSED, "first segment set");
        JUnitUtil.waitFor(() -> tAW.getKnownState() == Turnout.CLOSED, "second segment set");
        JUnitUtil.waitFor(() -> !routeSettingInProgress(), "route setting finished");

        // A later, unrelated route must set its own turnouts and nothing else
        tAE.setCommandedState(Turnout.THROWN);
        eep.setSingleSegmentRoute(tools().getDestinationPoint(sm.getSensor("NX-AW"),
                sm.getSensor("NX-AE-Side"), panels.get("Alpha"), eep).getSystemName());
        JUnitUtil.waitFor(() -> tAW.getKnownState() == Turnout.THROWN, "new route set");
        JUnitUtil.waitFor(() -> !routeSettingInProgress(), "route setting finished");
        assertEquals(Turnout.THROWN, tAW.getKnownState(), "turnout of the new route");
        assertEquals(Turnout.THROWN, tAE.getKnownState(), "turnout not in the new route is unchanged");
    }

    @Test
    public void testFullInterlockSegmentsSetInOrder() {
        pressButton("NX-AE");
        pressButton("NX-Alpha-EB");

        DestinationPoints first = tools().getDestinationPoint(sm.getSensor("NX-AE"),
                sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);
        DestinationPoints second = tools().getDestinationPoint(sm.getSensor("NX-AW-Main"),
                sm.getSensor("NX-Alpha-EB"), panels.get("Alpha"), eep);
        assertNotNull(first);
        assertNotNull(second);
        JUnitUtil.waitFor(() -> first.isActive() && second.isActive(), "both segments active");
        JUnitUtil.waitFor(() -> tAE.getKnownState() == Turnout.CLOSED, "turnout of the first segment");
        JUnitUtil.waitFor(() -> tAW.getKnownState() == Turnout.CLOSED, "turnout of the second segment");
        JUnitUtil.waitFor(() -> !routeSettingInProgress(), "route setting finished");
    }

    @Test
    public void testFullInterlockRouteClearedDownOverAllSegments() {
        pressButton("NX-AE");
        pressButton("NX-Alpha-EB");

        DestinationPoints first = tools().getDestinationPoint(sm.getSensor("NX-AE"),
                sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);
        DestinationPoints second = tools().getDestinationPoint(sm.getSensor("NX-AW-Main"),
                sm.getSensor("NX-Alpha-EB"), panels.get("Alpha"), eep);
        assertNotNull(first);
        assertNotNull(second);
        JUnitUtil.waitFor(() -> first.isActive() && second.isActive(), "both segments active");
        JUnitUtil.waitFor(() -> tAW.getKnownState() == Turnout.CLOSED, "turnout of the second segment");
        JUnitUtil.waitFor(() -> !routeSettingInProgress(), "route setting finished");

        // Clicking the same buttons again clears the whole route down
        eep.setClearDownOption(EntryExitPairs.AUTOCANCEL);
        pressButton("NX-AE");
        pressButton("NX-Alpha-EB");
        JUnitUtil.waitFor(() -> !first.isActive(), "first segment cleared down");
        JUnitUtil.waitFor(() -> !second.isActive(), "second segment cleared down");
    }

    private EntryExitTestTools tools() {
        return new EntryExitTestTools();
    }

    /**
     * Wait for the routes of the test panel that run when it is loaded: they close the turnouts
     * and set the NX and block detection sensors inactive.
     */
    static void waitForPanelReset() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        JUnitUtil.waitFor(() -> {
            for (Sensor s : sm.getNamedBeanSet()) {
                String name = s.getUserName();
                if (name != null && (name.startsWith("NX-") || name.startsWith("BD-") || name.equals("Reset"))
                        && s.getKnownState() != Sensor.INACTIVE) {
                    return false;
                }
            }
            for (Turnout t : tm.getNamedBeanSet()) {
                if (t.getKnownState() != Turnout.CLOSED) {
                    return false;
                }
            }
            return true;
        }, "test panel reset");
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        panels = EntryExitTestTools.getPanels();
        assertTrue(panels.containsKey("Alpha"));
        eep = InstanceManager.getDefault(EntryExitPairs.class);
        sm = InstanceManager.getDefault(SensorManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        tAW = tm.getTurnout("T-AW");
        tAE = tm.getTurnout("T-AE");
        assertNotNull(tAW);
        assertNotNull(tAE);
        waitForPanelReset();
        tAW.setCommandedState(Turnout.THROWN);
        tAE.setCommandedState(Turnout.THROWN);
        JUnitUtil.waitFor(() -> InstanceManager.getDefault(
                jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).routingStablised(), "routing stable");
    }

    @AfterEach
    public void tearDown() {
        if (panels != null) {
            panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        }
        panels = null;
        eep = null;
        sm = null;
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
