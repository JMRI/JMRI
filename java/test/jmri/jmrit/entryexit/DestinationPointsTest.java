package jmri.jmrit.entryexit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
@DisabledIfHeadless
public class DestinationPointsTest {

    private EntryExitTestTools tools;
    private HashMap<String, LayoutEditor> panels = new HashMap<>();

    private EntryExitPairs eep;
    private LayoutBlockManager lbm;
    private SensorManager sm;
    private TurnoutManager tm;

    @Test
    public void testCTor() {
        PointDetails pdSrc = ThreadingUtil.runOnGUIwithReturn(() -> {
            return tools.getPoint(sm.getSensor("NX-From-Beta"), panels.get("Alpha") ,eep);  // NOI18N
        });
        assertNotNull(pdSrc, "testCTor - source point");
        Source src = new Source(pdSrc);
        assertNotNull(src, "testCTor - source");

        PointDetails pdDest = ThreadingUtil.runOnGUIwithReturn(() -> {
            return tools.getPoint(sm.getSensor("NX-AE"), panels.get("Alpha") ,eep);  // NOI18N
        });
        assertNotNull(pdDest, "testCTor - destination point");

        DestinationPoints dp = new DestinationPoints(pdDest, null, src);
        assertNotNull(dp, "testCTor");
        String uuid = dp.getUniqueId();
        assertTrue(uuid.startsWith("IN:"), "check uuid");

        assertNotNull(lbm);
        assertNotNull(tm);
    }

    @Test
    public void testSetRoute() {
        DestinationPoints dp2 = ThreadingUtil.runOnGUIwithReturn(() -> {
            // Create a route
            DestinationPoints dp = tools.getDestinationPoint(sm.getSensor("NX-AW-Side"),  // NOI18N
                    sm.getSensor("NX-Alpha-EB"), panels.get("Alpha"), eep);  // NOI18N
            dp.activeBean(false, false);
            dp.setRoute(true);
            return dp;
        });
        JUnitUtil.waitFor(()->{return dp2.getState() == 2;}, "Route active");  // NOI18N

        // Cancel the route
        ThreadingUtil.runOnGUI(() -> dp2.cancelClearInterlock(EntryExitPairs.CANCELROUTE));
        JUnitUtil.waitFor(()->{return dp2.getState() == 4;}, "Route inactive");  // NOI18N
    }

    @Test
    public void testEnabled() {
        DestinationPoints dp2 = ThreadingUtil.runOnGUIwithReturn(() -> {
            DestinationPoints dp = tools.getDestinationPoint(sm.getSensor("NX-AE"),  // NOI18N
                    sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);  // NOI18N
            return dp;
        });
        assertNotNull(dp2, "test enabled");
        boolean chkEnabled = dp2.isEnabled();
        assertTrue(chkEnabled, "test enabled true");
        dp2.setEnabled(false);
        chkEnabled = dp2.isEnabled();
        assertFalse(chkEnabled, "test enabled false");
    }

    @Test
    public void testState() {
        DestinationPoints dp = ThreadingUtil.runOnGUIwithReturn(() -> {
            return tools.getDestinationPoint(sm.getSensor("NX-AE"),  // NOI18N
                    sm.getSensor("NX-AW-Side"), panels.get("Alpha"), eep);  // NOI18N
        });
        assertNotNull(dp, "test state");
        int state = dp.getState();
        assertEquals(4, state, "test state inactive");
        dp.setActiveEntryExit(true);
        state = dp.getState();
        assertEquals(2, state, "test state active");
    }

    @Test
    public void testNoCurrentRoute() {
        DestinationPoints dp = ThreadingUtil.runOnGUIwithReturn(() -> {
            return tools.getDestinationPoint(sm.getSensor("NX-AE"),  // NOI18N
                    sm.getSensor("NX-AW-Side"), panels.get("Alpha"), eep);  // NOI18N
        });
        assertNotNull(dp, "test state");

        // Setup memory variable
        InstanceManager.getDefault(MemoryManager.class).provideMemory("testMemory");
        eep.setMemoryOption("IMtestMemory");

        Thread routeError = createModalDialogOperatorThread(Bundle.getMessage("RouteNotClear"), Bundle.getMessage("ButtonNo"), "routeError");  // NOI18N
        dp.handleNoCurrentRoute(false, "Allocation Error");
        JUnitUtil.waitFor(()->{return !(routeError.isAlive());}, "routeError finished");
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread: " + threadName);  // NOI18N
        t.start();
        return t;
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        tools = new EntryExitTestTools();
        panels = EntryExitTestTools.getPanels();
        assertEquals(2, panels.size(), "Get LE panels");
        eep = InstanceManager.getDefault(EntryExitPairs.class);
        lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        sm = InstanceManager.getDefault(SensorManager.class);
        tm = InstanceManager.getDefault(TurnoutManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        tm = null;
        sm = null;
        lbm = null;
        eep = null;
        panels = null;
        tools = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DestinationPointsTest.class);

}
