package jmri.jmrit.entryexit;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
public class SourceTest {

    static EntryExitTestTools tools;
    static HashMap<String, LayoutEditor> panels = new HashMap<>();

    static LayoutBlockManager lbm;
    static EntryExitPairs eep;
    static SensorManager sm;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutBlock facing = lbm.getLayoutBlock("B-Beta-East");  // NOI18N
        LayoutBlock protect1 = lbm.getLayoutBlock("B-BE");  // NOI18N
        LayoutBlock protect2 = lbm.getLayoutBlock("B-Beta-Main");  // NOI18N
        List<LayoutBlock> blockList = new ArrayList<>();
        blockList.add(protect1);
        blockList.add(protect2);
        PointDetails pd = new PointDetails(facing, blockList);
        Assert.assertNotNull("PointDetails create failed", pd);  // NOI18N
        Sensor nxSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("NX-BE");  // NOI18N
        pd.setPanel(panels.get("Beta"));  // NOI18N
        pd.setSensor(nxSensor);
        pd.setRefObject(nxSensor);
        Source src = new Source(pd);
        Assert.assertNotNull("Source CTor", src);  // NOI18N
    }

    @Test
    public void testDestinationPoints() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // Get a source object
        Source src = tools.getSourceInstance(sm.getSensor("NX-AW"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("Source fetch failed", src);  // NOI18N
        // Get a destination point
        PointDetails destPt = tools.getPoint(sm.getSensor("NX-AE-Main"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("Destination fetch failed", destPt);  // NOI18N
        Sensor destSensor = sm.getSensor("NX-AE-Main");  // NOI18N
        Assert.assertNotNull("Destination sensor", destSensor);  // NOI18N

        // Disable pair, delete, add, enable
        src.setEnabled(destSensor, panels.get("Alpha"), false);  // NOI18N
        boolean chkDisabled = src.isEnabled(destSensor, panels.get("Alpha"));  // NOI18N
        Assert.assertFalse("check disabled", chkDisabled);  // NOI18N
        src.removeDestination(destPt);
        ArrayList<PointDetails> dp1 = src.getDestinationPoints();
        Assert.assertEquals("one left", 1, dp1.size());  // NOI18N
        src.addDestination(destPt, null);
        ArrayList<PointDetails> dp2 = src.getDestinationPoints();
        Assert.assertEquals("now two", 2, dp2.size());  // NOI18N
        src.setEnabled(destSensor, panels.get("Alpha"), true);  // NOI18N
        boolean chkEnabled = src.isEnabled(destSensor, panels.get("Alpha"));  // NOI18N
        Assert.assertTrue("check enabled", chkEnabled);  // NOI18N
        boolean chkActive = src.isRouteActive(destPt);
        Assert.assertFalse("check active", chkActive);  // NOI18N
        boolean chkdest = src.isDestinationValid(destPt);
        Assert.assertTrue("check destination valid", chkdest);  // NOI18N
        String uuid = src.getUniqueId(destSensor, panels.get("Alpha"));  // NOI18N
        Assert.assertTrue("check uuid", uuid.startsWith("IN:"));  // NOI18N
    }

    @Test
    public void testSourceMethods() {
        // Get a source object
        Source src = tools.getSourceInstance(sm.getSensor("NX-AE"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("Source fetch failed", src);  // NOI18N

        PointDetails pdx = src.getPoint();
        Assert.assertNotNull("getPoint", pdx);  // NOI18N
        LayoutBlock lbx = src.getStart();
        Assert.assertNotNull("getStart", lbx);  // NOI18N
        List<LayoutBlock> lbprot = src.getSourceProtecting();
        Assert.assertEquals("getSourceProtecting", 1, lbprot.size());  // NOI18N
        NamedBean srcsig = src.getSourceSignal();
        Assert.assertNull("getSourceSignal", srcsig);  // NOI18N
        Object srcobj = src.getSourceObject();
        Assert.assertNotNull("getSourceObject", srcobj);  // NOI18N
    }

    @BeforeClass
    static public void setUp() throws Exception {
        JUnitUtil.setUp();

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JUnitUtil.resetProfileManager();

        tools = new EntryExitTestTools();
        panels = EntryExitTestTools.getPanels();
        Assert.assertEquals("Get LE panels", 2, panels.size());  // NOI18N
        eep = jmri.InstanceManager.getDefault(EntryExitPairs.class);
        lbm = jmri.InstanceManager.getDefault(LayoutBlockManager.class);
        sm = InstanceManager.getDefault(SensorManager.class);
    }

    @AfterClass
    static public void tearDown() {
        panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SourceTest.class);

}
