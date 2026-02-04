package jmri.jmrit.entryexit;

import java.util.List;
import java.util.HashMap;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Sand Copyright (C) 2018
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class EntryExitPairsTest {

    private EntryExitTestTools tools;
    private HashMap<String, LayoutEditor> panels = new HashMap<>();

    private EntryExitPairs eep;
    private LayoutBlockManager lbm;
    private SensorManager sm;
    private TurnoutManager tm;

    @Test
    public void testCTor() {
        EntryExitPairs t = new EntryExitPairs();
        Assertions.assertNotNull( t, "exists");
        t.dispose();
    }

    @Test
    public void testAddNxSourcePoint() {
        Sensor sensor = sm.getSensor("NX-From-Alpha");  // NOI18N
        eep.addNXSourcePoint(sensor, panels.get("Beta"));  // NOI18N
    }

    @Test
    public void testGetSourceList() {
        List<Object> list = eep.getSourceList(panels.get("Alpha"));  // NOI18N
        Assertions.assertEquals( 4, list.size(), "test source list");
    }

    @Test
    public void testSetSingleSegmentRoute() {
        DestinationPoints dp = tools.getDestinationPoint(sm.getSensor("NX-AE"),  // NOI18N
                sm.getSensor("NX-AW-Main"), panels.get("Alpha"), eep);  // NOI18N
        Assertions.assertNotNull( dp, "single segment route");
        eep.setSingleSegmentRoute(dp.getUniqueId());
        new EventTool().waitNoEvent(1000);

        LayoutBlock lb = lbm.getLayoutBlock("B-Alpha-Main");
        Assertions.assertNotNull(lb);
        Turnout to = tm.getTurnout("T-AE");
        Assertions.assertNotNull(to);

        // Check the results
        JUnitUtil.waitFor(()->{return lb.getUseExtraColor();}, "Route active");  // NOI18N
        JUnitUtil.waitFor(()->{return to.getKnownState() == Turnout.CLOSED;}, "Turnout closed");  // NOI18N
    }

    @Test
    public void testDiscoverPairs() throws jmri.JmriException {
        eep.automaticallyDiscoverEntryExitPairs(panels.get("Alpha"), EntryExitPairs.FULLINTERLOCK);  // NOI18N
    }

    @Test
    public void testNxPairDelete() {
        Sensor sensor = sm.getSensor("NX-From-Alpha");  // NOI18N
        boolean chkDelete = eep.deleteNxPair(sensor);
        Assertions.assertTrue( chkDelete, "delete empty");

        createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonNo"));  // NOI18N
        sensor = sm.getSensor("NX-Alpha-EB");  // NOI18N
        chkDelete = eep.deleteNxPair(sensor);
        Assertions.assertFalse( chkDelete, "delete active denied");
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

    @Test
    public void testTypeHandledName(){
        Assertions.assertEquals("Entry Exit", eep.getBeanTypeHandled());
        Assertions.assertEquals("Entry Exit", eep.getBeanTypeHandled(false));
        Assertions.assertEquals("Entry Exits", eep.getBeanTypeHandled(true));
    }

    @BeforeEach
    public void before() throws jmri.JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        tools = new EntryExitTestTools();
        panels = EntryExitTestTools.getPanels();
        Assertions.assertNotNull( panels, "Get LE panels");
        eep = InstanceManager.getDefault(EntryExitPairs.class);
        lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        sm = InstanceManager.getDefault(SensorManager.class);
        tm = InstanceManager.getDefault(TurnoutManager.class);
    }

    @AfterEach
    public void after() {
        if (panels != null) {
            panels.forEach((name, panel) -> JUnitUtil.dispose(panel));
        }
        eep = null;
        lbm = null;
        sm = null;
        tm = null;
        panels = null;
        tools = null;

        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntryExitPairsTest.class);

}
