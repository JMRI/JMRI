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
public class PointDetailsTest {

    static EntryExitTestTools tools;
    static HashMap<String, LayoutEditor> panels = new HashMap<>();

    static EntryExitPairs eep;
    static LayoutBlockManager lbm;
    static SensorManager sm;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutBlock f = lbm.getLayoutBlock("B-Alpha-East");  // NOI18N
        LayoutBlock p = lbm.getLayoutBlock("B-Alpha-Beta");  // NOI18N
        List<LayoutBlock> blockList = new ArrayList<>();
        blockList.add(p);
        PointDetails pd = new PointDetails(f,blockList);
        Assert.assertNotNull("create failed", pd);  // NOI18N
    }

    @Test
    public void testGetters() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PointDetails pd = tools.getPoint(sm.getSensor("NX-AW"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("fetch failed", pd);  // NOI18N
        LayoutBlock fblk = pd.getFacing();
        Assert.assertNotNull("getFacing", fblk);  // NOI18N
        List<LayoutBlock> pblk = pd.getProtecting();
        Assert.assertEquals("getProtecting", 1, pblk.size());  // NOI18N

        Sensor sensor = pd.getSensor();
        Assert.assertNotNull("getSensor", sensor);  // NOI18N
        NamedBean signal = pd.getSignal();
        Assert.assertNull("getSignal", signal);  // NOI18N
    }

    @Test
    public void testNxButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PointDetails pd = tools.getPoint(sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("PointDetails fetch failed", pd);  // NOI18N
        pd.setButtonState(EntryExitPairs.NXBUTTONACTIVE);
        pd.setNXState(EntryExitPairs.NXBUTTONINACTIVE);
        int nxState = pd.getNXState();
        Assert.assertEquals("Button is active", nxState, EntryExitPairs.NXBUTTONINACTIVE);  // NOI18N
    }

    @BeforeClass
    public static void setUp() throws Exception {
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
    public static void tearDown() {
        panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PointDetailsTest.class);

}
