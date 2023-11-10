package jmri.jmrit.entryexit;

import java.util.HashMap;

import jmri.InstanceManager;
import jmri.MemoryManager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DestinationPointsTest {

    private EntryExitTestTools tools;
    private HashMap<String, LayoutEditor> panels = new HashMap<>();

    private EntryExitPairs eep;
    private LayoutBlockManager lbm;
    private SensorManager sm;
    private TurnoutManager tm;

    @Test
    public void testCTor() {
        PointDetails pdSrc = tools.getPoint(sm.getSensor("NX-From-Beta"), panels.get("Alpha") ,eep);  // NOI18N
        Assert.assertNotNull("testCTor - source point", pdSrc);  // NOI18N
        Source src = new Source(pdSrc);
        Assert.assertNotNull("testCTor - source", src);  // NOI18N

        PointDetails pdDest = tools.getPoint(sm.getSensor("NX-AE"), panels.get("Alpha") ,eep);  // NOI18N
        Assert.assertNotNull("testCTor - destination point", pdDest);  // NOI18N

        DestinationPoints dp = new DestinationPoints(pdDest, null, src);
        Assert.assertNotNull("testCTor", dp);  // NOI18N
        String uuid = dp.getUniqueId();
        Assert.assertTrue("check uuid", uuid.startsWith("IN:"));  // NOI18N

        Assertions.assertNotNull(lbm);
        Assertions.assertNotNull(tm);
    }

    @Test
    public void testSetRoute() throws Exception {
        // Create a route
        DestinationPoints dp = tools.getDestinationPoint(sm.getSensor("NX-AW-Side"),  // NOI18N
                sm.getSensor("NX-Alpha-EB"), panels.get("Alpha"), eep);  // NOI18N
        dp.activeBean(false, false);
        dp.setRoute(true);
        JUnitUtil.waitFor(()->{return dp.getState() == 2;}, "Route active");  // NOI18N

        // Cancel the route
        dp.cancelClearInterlock(EntryExitPairs.CANCELROUTE);
        JUnitUtil.waitFor(()->{return dp.getState() == 4;}, "Route inactive");  // NOI18N
    }

    @Test
    public void testEnabled() {
        DestinationPoints dp = tools.getDestinationPoint(sm.getSensor("NX-AE"),  // NOI18N
                sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("test enabled", dp);  // NOI18N
        boolean chkEnabled = dp.isEnabled();
        Assert.assertTrue("test enabled true", chkEnabled);  // NOI18N
        dp.setEnabled(false);
        chkEnabled = dp.isEnabled();
        Assert.assertFalse("test enabled false", chkEnabled);  // NOI18N
    }

    @Test
    public void testState() {
        DestinationPoints dp = tools.getDestinationPoint(sm.getSensor("NX-AE"),  // NOI18N
                sm.getSensor("NX-AW-Side"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("test state", dp);
        int state = dp.getState();
        Assert.assertEquals("test state inactive", 4, state);  // NOI18N
        dp.setActiveEntryExit(true);
        state = dp.getState();
        Assert.assertEquals("test state active", 2, state);  // NOI18N
    }

    @Test
    public void testNoCurrentRoute() {
        DestinationPoints dp = tools.getDestinationPoint(sm.getSensor("NX-AE"),  // NOI18N
                sm.getSensor("NX-AW-Side"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("test state", dp);

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
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        tools = new EntryExitTestTools();
        panels = EntryExitTestTools.getPanels();
        Assert.assertEquals("Get LE panels", 2, panels.size());  // NOI18N
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
