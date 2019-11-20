package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.SingleTurnoutSignalHead;
import jmri.implementation.VirtualSignalHead;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 * Test simple functioning of LayoutEditorTools
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	George Warner Copyright (C) 2019
 */
public class LayoutEditorToolsTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 5 second timeout for methods in this test class.

    // allow 2 retries of intermittent tests
    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    private LayoutEditor le = null;
    private LayoutEditorTools let = null;
    private LayoutBlock layoutBlocks[] = {null, null, null, null, null};
    private Turnout turnouts[] = {null, null, null, null, null};
    private SignalHead signalHeads[] = {null, null, null, null, null};
    private Sensor sensors[] = {null, null, null, null, null};

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", let);
    }

    @Test
    public void testHitEndBumper() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // we haven't done anything, so reachedEndBumper should return false.
        Assert.assertFalse("reached end bumper", let.reachedEndBumper());
    }

    @Test
    public void testSetSignalsAtTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            // this causes a "set Signal Heads at Turnout" dialog to be displayed.
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });
        // the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        // then closes it.
        jFrameOperator.requestClose();
    }

    @Test
    public void testSetSignalsAtTurnoutWithDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            // create a new Layout Turnout here
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutTurnout to = new LayoutTurnout("Right Hand",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, le);
            Assert.assertNotNull("RH turnout for testSetSignalsAtTurnoutWithDone", to);
            to.setTurnout(turnouts[0].getSystemName());
            le.getLayoutTracks().add(to);

            // this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });

        // the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));

        // select the turnout from the popup menu
        JLabelOperator JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(0);  //TODO:fix hardcoded index

        JButtonOperator jButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("GetSaved"));
        jButtonOperator.doClick();

        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("PlaceAllHeads"));
        jCheckBoxOperator.doClick();

//        jCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SetAllLogic"));
//        jCheckBoxOperator.doClick();

        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //pressing "Done" should throw up an error dialog... dismiss it
        Thread modalDialogOperatorThread1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread1.isAlive());
        }, "modalDialogOperatorThread1 finished");

        JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("ThroatContinuing")));
        jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        //pressing "Done" should throw up an error dialog... dismiss it
        Thread modalDialogOperatorThread2 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread2.isAlive());
        }, "modalDialogOperatorThread2 finished");

        JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("ThroatDiverging")));
        jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index

        //pressing "Done" should throw up an error dialog... dismiss it
        Thread modalDialogOperatorThread3 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread3.isAlive());
        }, "modalDialogOperatorThread3 finished");

        //NOTE: index used here because "Continuing" match against "ThroadContinuing"
        JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("Continuing")), 1);
        jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(3);  //TODO:fix hardcoded index

        //pressing "Done" should throw up an error dialog... dismiss it
        Thread modalDialogOperatorThread4 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread4.isAlive());
        }, "modalDialogOperatorThread4 finished");

        //NOTE: index used here because "Diverging" match against "ThroadDiverging"
        JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("Diverging")), 1);
        jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(4);  //TODO:fix hardcoded index

        //dumpToXML();  // dump jemmy GUI info to xml file
        //captureScreenshot();
        
        //TODO: Add failure case for SSL logic
        //pressing "Done" should throw up an error dialog... dismiss it
//        Thread modalDialogOperatorThread5 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
//                Bundle.getMessage("ErrorTitle"),
//                Bundle.getMessage("ButtonOK"));  // NOI18N
//        doneButtonOperator.doClick();
//        JUnitUtil.waitFor(() -> {
//            return !(modalDialogOperatorThread5.isAlive());
//        }, "modalDialogOperatorThread5 finished");
        doneButtonOperator.doClick();

        // deselect the "SetAllLogic" checkbox
//        jCheckBoxOperator.doClick();

        doneButtonOperator.doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }   // testSetSignalsAtTurnoutWithDone

    @Test
    public void testSetSignalsAtTurnoutWithCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutTurnout to = new LayoutTurnout("Right Hand",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, le);
            to.setTurnout(turnouts[0].getSystemName());
            le.getLayoutTracks().add(to);

            // this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, le.signalIconEditor, le.getTargetFrame());
        });
        // the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        // then we find and press the "Done" button.
        JButtonOperator jbo = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel"));
        jbo.press();
        jFrameOperator.requestClose();
    }

    @Test
    public void testSetSignalsAtTurnoutFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutTurnout to = new LayoutTurnout("Right Hand",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, le);
            to.setTurnout(turnouts[0].getSystemName());
            le.getLayoutTracks().add(to);
            // this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, le.signalIconEditor, le.getTargetFrame());
        });
        // the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        // then closes it.
        jFrameOperator.requestClose();
    }

    @Test
    //@Ignore("NPE during execution due to missing frame")
    public void testSetSignalsAtLevelXing() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            // this causes a "set Signal Heads Level Crossing" dialog to be displayed.
            let.setSignalsAtLevelXing(le.signalIconEditor, le.getTargetFrame());
        });
        // the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtLevelXing"));
        // then closes it.
        jFrameOperator.requestClose();
    }

    @Test
    public void testSetSignalsAtLevelXingFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LevelXing lx = new LevelXing("LevelCrossing", point, le);
            lx.setLayoutBlockAC(layoutBlocks[0]);
            lx.setLayoutBlockBD(layoutBlocks[1]);

            // this causes a "set Signal Heads Level Crossing" dialog to be displayed.
            let.setSignalsAtLevelXingFromMenu(lx, le.signalIconEditor, le.getTargetFrame());
        });
        // the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtLevelXing"));
        // then closes it.
        jFrameOperator.requestClose();
    }

    @Test
    public void testGetHeadFromNameNullName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull("null signal head for null name", let.getHeadFromName(null));
    }

    @Test
    public void testGetHeadFromNameEmptyName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull("null signal head for empty name", let.getHeadFromName(""));
    }

    @Test
    public void testGetHeadFromNameValid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);

        Assert.assertEquals("signal head for valid name", h, let.getHeadFromName("IH1"));
    }

    @Test
    public void testRemoveSignalHeadFromPanelNameNullName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // this test verifies there is no exception
        let.removeSignalHeadFromPanel(null);
    }

    @Test
    public void testRemoveSignalHeadFromPanelEmptyName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // this test verifies there is no exception
        let.removeSignalHeadFromPanel("");
    }

    @Test
    public void testFinalizeBlockBossLogicNullInput() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // this test verifies there is no exception
        let.finalizeBlockBossLogic();
    }

    @Test
    public void testSetSignalHeadOnPanelAtXYIntAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(h));
        let.setSignalHeadOnPanel(0, "IH1", 0, 0);
        // setSignalHeadOnPanel performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        // removeSignalHeadFromPanel performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(h));
    }

    @Test
    public void testSetSignalHeadOnPanelAtPointAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(h));
        Point2D point = new Point2D.Double(150.0, 100.0);
        let.setSignalHeadOnPanel(0.D, "IH1", point);
        // setSignalHeadOnPanel performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        // removeSignalHeadFromPanel performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(h));
    }

    @Test
    public void testSetSignalHeadOnPanelAtXYDoubleAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(h));
        let.setSignalHeadOnPanel(0.D, "IH1", 0, 0);
        // setSignalHeadOnPanel performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        // removeSignalHeadFromPanel performs some GUI actions, so give
        // the AWT queue some time to clear.
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(h));
    }

    @Test
    public void testGetSignalHeadIcon() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertNotNull("Signal head icon for panel", let.getSignalHeadIcon("IH1"));
    }

    @Test
    public void testIsHeadOnPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel", let.isHeadOnPanel(h));
    }

    @Test
    public void testIsHeadAssignedAnywhere() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel", let.isHeadAssignedAnywhere(h));
    }

    @Test
    public void testRemoveSignalHeadAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        // just verify this doesn't thrown an error.
        let.removeAssignment(h);
    }

    @Test
    public void testInitializeBlockBossLogic() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        Assert.assertTrue("Signal head block boss logic started", let.initializeBlockBossLogic("IH1"));
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            jmri.util.JUnitUtil.resetProfileManager();
            le = new LayoutEditor();
            le.setVisible(true);
            let = le.getLETools();

            for (int i = 0; i < 4; i++) {
                String sBlockName = "IB" + i;
                String uBlockName = "Block " + i;
                layoutBlocks[i] = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(sBlockName, uBlockName);

                String toName = "TO" + i;
                turnouts[i] = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout(toName);

                String sName = "SH" + i;
                String uName = "signal head " + i;
                NamedBeanHandle<Turnout> nbh = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(toName, turnouts[i]);
                if (nbh != null) {
                    signalHeads[i] = new SingleTurnoutSignalHead(sName, uName, nbh, SignalHead.GREEN, SignalHead.RED);
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(signalHeads[i]);
                }

//                sName = "IS" + i;
//                uName = "sensor " + i;
//                sensors[i] = InstanceManager.getDefault(SensorManager.class).newSensor(sName, uName);
                //layoutBlocks[i].setOccupancySensorName(uName);
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.dispose(le);
            le = null;
            let = null;
            for (int i = 0; i < 4; i++) {
                layoutBlocks[i] = null;
                turnouts[i] = null;
                signalHeads[i] = null;
            }
        }
        JUnitUtil.tearDown();
    }
//

//    private void waitSeconds(int s) {
//        try {
//            //new QueueTool().waitEmpty(s * 1000);
//
//            //new EventTool().waitNoEvent(s * 1000);
//
//            //EventTool eventTool = new EventTool();
//            //eventTool.getTimeouts().setTimeout("EventTool.WaitNoEventTimeout", Math.max(600, s * 1000));
//            //eventTool.waitNoEvent(s * 1000);
//
//            //Thread.sleep(s * 1000);
//        } catch (TimeoutExpiredException tee) {
//            // Ignore
//        }
//    }
    // save screenshot of GUI
//    private void captureScreenshot() {
//        //grab image
//        PNGEncoder.captureScreen(System.getProperty("user.home")
//                + System.getProperty("file.separator")
//                + "screen.png");
//    }
//
//    // dump jemmy GUI info to xml file
//    private void dumpToXML() {
//        //grab component state
//        try {
//            Dumper.dumpAll(System.getProperty("user.home")
//                    + System.getProperty("file.separator")
//                    + "dump.xml");
//
//        } catch (FileNotFoundException e) {
//        }
//    }
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorToolsTest.class);
}   // class LayoutEditorToolsTest
