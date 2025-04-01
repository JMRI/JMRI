package jmri.jmrit.entryexit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class SourceTest {

    private static EntryExitTestTools tools;
    private static HashMap<String, LayoutEditor> panels = new HashMap<>();

    private static LayoutBlockManager lbm;
    private static EntryExitPairs eep;
    private static SensorManager sm;

    @Test
    public void testCTor() {

        LayoutBlock facing = lbm.getLayoutBlock("B-Beta-East");
        LayoutBlock protect1 = lbm.getLayoutBlock("B-BE");
        LayoutBlock protect2 = lbm.getLayoutBlock("B-Beta-Main");
        List<LayoutBlock> blockList = new ArrayList<>();
        blockList.add(protect1);
        blockList.add(protect2);
        PointDetails pd = new PointDetails(facing, blockList);
        assertNotNull( pd, "PointDetails create failed");
        Sensor nxSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("NX-BE");
        pd.setPanel(panels.get("Beta"));
        pd.setSensor(nxSensor);
        pd.setRefObject(nxSensor);
        Source src = new Source(pd);
        assertNotNull( src, "Source CTor");
    }

    @Test
    public void testDestinationPoints() {

        // Get a source object
        Source src = tools.getSourceInstance(sm.getSensor("NX-AW"), panels.get("Alpha"), eep);
        assertNotNull( src, "Source fetch failed");
        // Get a destination point
        PointDetails destPt = tools.getPoint(sm.getSensor("NX-AE-Main"), panels.get("Alpha"), eep);
        assertNotNull( destPt, "Destination fetch failed");
        Sensor destSensor = sm.getSensor("NX-AE-Main");
        assertNotNull( destSensor, "Destination sensor");

        // Disable pair, delete, add, enable
        src.setEnabled(destSensor, panels.get("Alpha"), false);
        boolean chkDisabled = src.isEnabled(destSensor, panels.get("Alpha"));
        assertFalse( chkDisabled, "check disabled");
        src.removeDestination(destPt);
        ArrayList<PointDetails> dp1 = src.getDestinationPoints();
        assertEquals( 1, dp1.size(), "one left");
        src.addDestination(destPt, null);
        ArrayList<PointDetails> dp2 = src.getDestinationPoints();
        assertEquals( 2, dp2.size(), "now two");
        src.setEnabled(destSensor, panels.get("Alpha"), true);
        boolean chkEnabled = src.isEnabled(destSensor, panels.get("Alpha"));
        assertTrue( chkEnabled, "check enabled");
        boolean chkActive = src.isRouteActive(destPt);
        assertFalse( chkActive, "check active");
        boolean chkdest = src.isDestinationValid(destPt);
        assertTrue( chkdest, "check destination valid");
        String uuid = src.getUniqueId(destSensor, panels.get("Alpha"));
        assertNotNull(uuid);
        assertTrue( uuid.startsWith("IN:"), "check uuid");
    }

    @Test
    public void testSourceMethods() {
        // Get a source object
        Source src = tools.getSourceInstance(sm.getSensor("NX-AE"), panels.get("Alpha"), eep);
        assertNotNull( src, "Source fetch failed");

        PointDetails pdx = src.getPoint();
        assertNotNull( pdx, "getPoint");
        LayoutBlock lbx = src.getStart();
        assertNotNull( lbx, "getStart");
        List<LayoutBlock> lbprot = src.getSourceProtecting();
        assertEquals( 1, lbprot.size(), "getSourceProtecting");
        NamedBean srcsig = src.getSourceSignal();
        assertNull( srcsig, "getSourceSignal");
        Object srcobj = src.getSourceObject();
        assertNotNull(srcobj, "getSourceObject");
    }

    @BeforeAll
    static public void setUp() throws JmriException {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        tools = new EntryExitTestTools();
        panels = EntryExitTestTools.getPanels();
        assertEquals( 2, panels.size(), "Get LE panels");
        eep = jmri.InstanceManager.getDefault(EntryExitPairs.class);
        lbm = jmri.InstanceManager.getDefault(LayoutBlockManager.class);
        sm = InstanceManager.getDefault(SensorManager.class);
    }

    @AfterAll
    static public void tearDown() {
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SourceTest.class);

}
