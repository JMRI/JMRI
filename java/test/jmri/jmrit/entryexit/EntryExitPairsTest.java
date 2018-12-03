package jmri.jmrit.entryexit;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.HashMap;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
+ * @author Dave Sand Copyright (C) 2018
 */
public class EntryExitPairsTest {

    static EntryExitTestTools tools;
    static HashMap<String, LayoutEditor> panels = new HashMap<>();

    static EntryExitPairs eep;
    static LayoutBlockManager lbm;
    static SensorManager sm;
    static TurnoutManager tm;

    @Test
    public void testCTor() {
        EntryExitPairs t = new EntryExitPairs();
        Assert.assertNotNull("exists", t);  // NOI18N
    }

    @Test
    public void testAddNxSourcePoint() {
        Sensor sensor = sm.getSensor("NX-From-Alpha");  // NOI18N
        eep.addNXSourcePoint(sensor, panels.get("Beta"));  // NOI18N
    }

    @Test
    public void testGetSourceList() {
        List<Object> list = eep.getSourceList(panels.get("Alpha"));  // NOI18N
        Assert.assertEquals("test source list", 4, list.size());  // NOI18N
    }

    @Test
    public void testSetSingleSegmentRoute() {
        DestinationPoints dp = tools.getDestinationPoint(sm.getSensor("NX-AE"),  // NOI18N
                sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);  // NOI18N
        Assert.assertNotNull("single segment route", dp);  // NOI18N
        eep.setSingleSegmentRoute(dp.getUniqueId());
        new EventTool().waitNoEvent(1000);

        // Check the results
        JUnitUtil.waitFor(()->{return lbm.getLayoutBlock("B-Alpha-Main").getUseExtraColor();}, "Route active");  // NOI18N
        JUnitUtil.waitFor(()->{return tm.getTurnout("T-AE").getKnownState() == Turnout.CLOSED;}, "Turnout closed");  // NOI18N
    }

    @Test
    public void testDiscoverPairs() throws Exception {
        eep.automaticallyDiscoverEntryExitPairs(panels.get("Alpha"), EntryExitPairs.FULLINTERLOCK);  // NOI18N
    }

    @Test
    public void testNxPairDelete() {
        Sensor sensor = sm.getSensor("NX-From-Alpha");  // NOI18N
        boolean chkDelete = eep.deleteNxPair(sensor);
        Assert.assertTrue("delete empty", chkDelete);  // NOI18N

        createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonNo"));  // NOI18N
        sensor = sm.getSensor("NX-Alpha-EB");  // NOI18N
        chkDelete = eep.deleteNxPair(sensor);
        Assert.assertFalse("delete active denied", chkDelete);  // NOI18N
    }

    void createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            new EventTool().waitNoEvent(1000);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");  // NOI18N
        t.start();
    }

    @BeforeClass
    public static void before() throws Exception {
        JUnitUtil.setUp();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JUnitUtil.resetProfileManager();

        tools = new EntryExitTestTools();
        panels = EntryExitTestTools.getPanels();
        Assert.assertNotNull("Get LE panels", panels);  // NOI18N
        eep = InstanceManager.getDefault(EntryExitPairs.class);
        lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        sm = InstanceManager.getDefault(SensorManager.class);
        tm = InstanceManager.getDefault(TurnoutManager.class);
    }

    @AfterClass
    public static void after() {
        panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        eep = null;
        lbm = null;
        sm = null;
        tm = null;
        panels = null;
        tools = null;
        
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntryExitPairsTest.class);

}
