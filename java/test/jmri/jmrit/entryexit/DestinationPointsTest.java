package jmri.jmrit.entryexit;

import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.util.JUnitUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.jemmy.EventTool;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
public class DestinationPointsTest {

    static EntryExitTestTools tools;
    static HashMap<String, LayoutEditor> panels = new HashMap<>();

    static EntryExitPairs eep;
    static LayoutBlockManager lbm;
    static SensorManager sm;
    static TurnoutManager tm;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
    }

    @Test
    public void testSetRoute() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // Set clear down option
        eep.setClearDownOption(EntryExitPairs.AUTOCLEAR);

        // Create the route
        Sensor srcSensor = sm.getSensor("NX-AW-Side");  // NOI18N
        Sensor destSensor = sm.getSensor("NX-Alpha-EB");  // NOI18N

        JUnitUtil.setBeanStateAndWait(srcSensor, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(destSensor, Sensor.INACTIVE);

        JUnitUtil.setBeanStateAndWait(srcSensor, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(destSensor, Sensor.ACTIVE);

        // Check the results
        JUnitUtil.waitFor(()->{return tm.getTurnout("T-AW").getKnownState() == Turnout.THROWN;}, "Turnout thrown");  // NOI18N

        // Simulate route movement using clear down.
        JUnitUtil.setBeanState(srcSensor, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(srcSensor, Sensor.ACTIVE);
        new EventTool().waitNoEvent(500);
        JUnitUtil.setBeanState(destSensor, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(destSensor, Sensor.ACTIVE);

        // Check the results
        JUnitUtil.waitFor(()->{return lbm.getLayoutBlock("B-Alpha-West").getOccupancy() == jmri.Block.OCCUPIED;}, "Block occupied");  // NOI18N
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

    @BeforeClass
    public static void setUp() throws Exception {
        JUnitUtil.setUp();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        tools = new EntryExitTestTools();
        panels = tools.getPanels();
        Assert.assertEquals("Get LE panels", 2, panels.size());  // NOI18N
        eep = InstanceManager.getDefault(EntryExitPairs.class);
        lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        sm = InstanceManager.getDefault(SensorManager.class);
        tm = InstanceManager.getDefault(TurnoutManager.class);
    }

    @AfterClass
    public static void tearDown() {
        panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DestinationPointsTest.class);

}
