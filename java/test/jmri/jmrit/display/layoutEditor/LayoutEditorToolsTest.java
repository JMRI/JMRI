package jmri.jmrit.display.layoutEditor;

import static jmri.jmrit.display.layoutEditor.LayoutTrack.POS_POINT;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.TURNOUT_A;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.TURNOUT_B;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.TURNOUT_C;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.ANCHOR;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.END_BUMPER;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.Turnout;
import jmri.implementation.SingleTurnoutSignalHead;
import jmri.implementation.VirtualSignalHead;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
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
    public Timeout globalTimeout = Timeout.seconds(10); //10 second timeout for methods in this test class.

    //allow 2 retries of intermittent tests
    @Rule
    public RetryRule retryRule = new RetryRule(2); //allow 2 retries

    private LayoutEditor le = null;
    private LayoutEditorTools let = null;

    //these all have to contain the same number of elements
    private LayoutBlock layoutBlocks[] = new LayoutBlock[5];
    private Turnout turnouts[] = new Turnout[5];
    private SignalHead signalHeads[] = new SignalHead[5];
    private Sensor sensors[] = new Sensor[5];

    private LayoutTurnout layoutTurnout = null;
    private PositionablePoint positionablePoint1 = null;
    private PositionablePoint positionablePoint2 = null;
    private PositionablePoint positionablePoint3 = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", let);
    }

    @Test
    public void testHitEndBumper() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //we haven't done anything, so reachedEndBumper should return false.
        Assert.assertFalse("reached end bumper", let.reachedEndBumper());
    }

    @Test
    public void testSetSignalsAtTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        //then closes it.
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
    }

    @Test
    public void testSetSignalsAtTurnoutWithDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //create a new Layout Turnout
        layoutTurnout = new LayoutTurnout("Right Hand",
                LayoutTurnout.RH_TURNOUT, new Point2D.Double(150.0, 100.0),
                33.0, 1.1, 1.2, le);
        Assert.assertNotNull("RH turnout for testSetSignalsAtTurnoutWithDone", layoutTurnout);
        le.getLayoutTracks().add(layoutTurnout);

        positionablePoint1 = new PositionablePoint("A1", ANCHOR, new Point2D.Double(250.0, 100.0), le);
        Assert.assertNotNull("positionablePoint1 for testSetSignalsAtTurnoutWithDone", positionablePoint1);
        le.getLayoutTracks().add(positionablePoint1);

        positionablePoint2 = new PositionablePoint("A2", ANCHOR, new Point2D.Double(50.0, 100.0), le);
        le.getLayoutTracks().add(positionablePoint2);
        Assert.assertNotNull("positionablePoint2 for testSetSignalsAtTurnoutWithDone", positionablePoint2);

        positionablePoint3 = new PositionablePoint("A3", ANCHOR, new Point2D.Double(250.0, 150.0), le);
        le.getLayoutTracks().add(positionablePoint3);
        Assert.assertNotNull("positionablePoint3 for testSetSignalsAtTurnoutWithDone", positionablePoint3);

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //pressing "Done" should throw up a "no turnout name was entered" (SignalsError1)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread0 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread0.isAlive());
        }, "modalDialogOperatorThread0 finished");

        //select the turnout from the popup menu
        JLabelOperator JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(0);  //TODO:fix hardcoded index

        //pressing "Done" should throw up a "Turnout XXX is not drawn on the panel" (SignalsError3)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread0a = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread0a.isAlive());
        }, "modalDialogOperatorThread0a finished");

        layoutTurnout.setTurnout(turnouts[0].getSystemName()); //this should fix the "is not drawn on the panel" error

        JButtonOperator jButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("GetSaved"));
        jButtonOperator.doClick();

        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("PlaceAllHeads"));
        jCheckBoxOperator.doClick();

        //select the "SetAllLogic" checkbox
        JCheckBoxOperator allLogicCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SetAllLogic"));
        allLogicCheckBoxOperator.doClick(); //click all on
        allLogicCheckBoxOperator.doClick(); //click all off

        /*
        * test all four comboboxes for "Signal head name was not entered"  (SignalsError5)
         */
        //pressing "Done" should throw up a "Signal head name was not entered"  (SignalsError5)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread1.isAlive());
        }, "modalDialogOperatorThread1 finished");

        //select signal head for this combobox
        JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("ThroatContinuing")));
        jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        //pressing "Done" should throw up a "Signal head name was not entered"  (SignalsError5)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread2 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread2.isAlive());
        }, "modalDialogOperatorThread2 finished");

        //select signal head for this combobox
        JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("ThroatDiverging")));
        jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index

        //pressing "Done" should throw up a "Signal head name was not entered"  (SignalsError5)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread3 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread3.isAlive());
        }, "modalDialogOperatorThread3 finished");

        //select signal head for this combobox
        //NOTE: index used here because "Continuing" matches against "ThroadContinuing" above
        JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("Continuing")), 1);
        jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(3);  //TODO:fix hardcoded index

        //pressing "Done" should throw up a "Signal head name was not entered"  (SignalsError5)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread4 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread4.isAlive());
        }, "modalDialogOperatorThread4 finished");

        //select signal head for this combobox
        //NOTE: index used here because "Diverging" matches against "ThroadDiverging" above
        JLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("Diverging")), 1);
        jComboBoxOperator = new JComboBoxOperator(
                (JComboBox) JLabelOperator.getLabelFor());
        jComboBoxOperator.selectItem(4); //TODO:fix hardcoded index

        testSetupSSL(0);    //test Throat Continuing SSL logic setup
        testSetupSSL(1);    //test Throat Diverging SSL logic setup
        testSetupSSL(2);    //test Continuing SSL logic setup
        testSetupSSL(3);    //test Diverging SSL logic setup

        //TODO: fix the other failure conditions (testing each one)
        //layoutBlocks[i].setOccupancySensorName(uName);
//
        //this time everything should work
        doneButtonOperator.doClick();
        jFrameOperator.waitClosed();    //make sure the dialog closed
    }   //testSetSignalsAtTurnoutWithDone

    /*
     * test SSL logic setup
     */
    private void testSetupSSL(int idx) {
        JFrameOperator jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        //NOTE: index used here because there are four identical buttons
        JCheckBoxOperator cboSetLogic = new JCheckBoxOperator(jfoSignalsAtTurnout, Bundle.getMessage("SetLogic"), idx);
        cboSetLogic.doClick(); //click on

        //pressing "Done" should throw up a "all connections have not been defined"  (InfoMessage7)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread1.isAlive());
        }, "modalDialogOperatorThread1 finished");

        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });

        //define connection
        String uName = "T" + (idx + 1);
        int types[] = {TURNOUT_B, TURNOUT_C, TURNOUT_A, TURNOUT_A};
        PositionablePoint[] positionablePoints = {positionablePoint2, positionablePoint3, positionablePoint1, positionablePoint1};
        TrackSegment trackSegment = new TrackSegment(uName,
                layoutTurnout, types[idx],
                positionablePoints[idx], POS_POINT,
                false, false, le);
        Assert.assertNotNull("trackSegment not null", trackSegment);
        le.getLayoutTracks().add(trackSegment);
        try {
            layoutTurnout.setConnection(types[idx], trackSegment, LayoutTrack.TRACK);
        } catch (JmriException ex) {
            Logger.getLogger(LayoutEditorToolsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        //pressing "Done" should throw up a "the next signal... apparently is not yet defined."  (InfoMessage5)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread2 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread2.isAlive());
        }, "modalDialogOperatorThread2 finished");

        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });

        //change anchor to end bumper
        positionablePoints[idx].setType(END_BUMPER);

        //pressing "Done" should throw up a "blocks have not been defined around this item."  (InfoMessage6)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread3 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread3.isAlive());
        }, "modalDialogOperatorThread3 finished");

        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //assign block to track segment
        int lbIndex[] = {2, 3, 1, 1};
        trackSegment.setLayoutBlock(layoutBlocks[lbIndex[idx]]);

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });

        //pressing "Done" should throw up a "block XXX doesn''t have an occupancy sensor"  (InfoMessage4)
        //error dialog... dismiss it
        Thread modalDialogOperatorThread4 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread4.isAlive());
        }, "modalDialogOperatorThread4 finished");

        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //assign Occupancy Sensor to block
        layoutBlocks[lbIndex[idx]].setOccupancySensorName(sensors[lbIndex[idx]].getUserName());

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });

        doneButtonOperator.doClick();
        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(le.signalIconEditor, le.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to (re)appear
        jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        //doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        cboSetLogic = new JCheckBoxOperator(jfoSignalsAtTurnout, Bundle.getMessage("SetLogic"), idx);
        cboSetLogic.doClick(); //click off

        //reset these
        trackSegment.setLayoutBlock(null);
        layoutBlocks[lbIndex[idx]].setOccupancySensorName(null);
        //le.removeTrackSegment(trackSegment);
        positionablePoint1.setType(ANCHOR);
        positionablePoint2.setType(ANCHOR);
        positionablePoint3.setType(ANCHOR);
    }

    @Test
    public void testSetSignalsAtTurnoutWithCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutTurnout to = new LayoutTurnout("Right Hand",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, le);
            to.setTurnout(turnouts[0].getSystemName());
            le.getLayoutTracks().add(to);

            //this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, le.signalIconEditor, le.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        //then we find and press the "Done" button.
        JButtonOperator jbo = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel"));
        jbo.press();
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
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
            //this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, le.signalIconEditor, le.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        //then closes it.
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
    }

    @Test
    //@Ignore("NPE during execution due to missing frame")
    public void testSetSignalsAtLevelXing() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads Level Crossing" dialog to be displayed.
            let.setSignalsAtLevelXing(le.signalIconEditor, le.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtLevelXing"));
        //then closes it.
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
    }

    @Test
    public void testSetSignalsAtLevelXingFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LevelXing lx = new LevelXing("LevelCrossing", point, le);
            lx.setLayoutBlockAC(layoutBlocks[0]);
            lx.setLayoutBlockBD(layoutBlocks[1]);

            //this causes a "set Signal Heads Level Crossing" dialog to be displayed.
            let.setSignalsAtLevelXingFromMenu(lx, le.signalIconEditor, le.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtLevelXing"));
        //then closes it.
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
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
        InstanceManager.getDefault(SignalHeadManager.class).register(h);

        Assert.assertEquals("signal head for valid name", h, let.getHeadFromName("IH1"));
    }

    @Test
    public void testRemoveSignalHeadFromPanelNameNullName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //this test verifies there is no exception
        let.removeSignalHeadFromPanel(null);
    }

    @Test
    public void testRemoveSignalHeadFromPanelEmptyName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //this test verifies there is no exception
        let.removeSignalHeadFromPanel("");
    }

    @Test
    public void testFinalizeBlockBossLogicNullInput() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //this test verifies there is no exception
        let.finalizeBlockBossLogic();
    }

    @Test
    public void testSetSignalHeadOnPanelAtXYIntAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(h));
        let.setSignalHeadOnPanel(0.D, "IH1", 0, 0);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(h));
    }

    @Test
    public void testSetSignalHeadOnPanelAtPointAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(h));
        Point2D point = new Point2D.Double(150.0, 100.0);
        let.setSignalHeadOnPanel(0.D, "IH1", point);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(h));
    }

    @Test
    public void testSetSignalHeadOnPanelAtXYDoubleAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(h));
        let.setSignalHeadOnPanel(0.D, "IH1", 0, 0);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(h));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(h));
    }

    @Test
    public void testGetSignalHeadIcon() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        Assert.assertNotNull("Signal head icon for panel", let.getSignalHeadIcon("IH1"));
    }

    @Test
    public void testIsHeadOnPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel", let.isHeadOnPanel(h));
    }

    @Test
    public void testIsHeadAssignedAnywhere() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        Assert.assertFalse("Signal head not on panel", let.isHeadAssignedAnywhere(h));
    }

    @Test
    public void testRemoveSignalHeadAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        //just verify this doesn't thrown an error.
        let.removeAssignment(h);
    }

    @Test
    public void testInitializeBlockBossLogic() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        Assert.assertTrue("Signal head block boss logic started", let.initializeBlockBossLogic("IH1"));
    }

    //from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            le = new LayoutEditor();
            le.setVisible(true);

            let = le.getLETools();

            for (int i = 0; i < layoutBlocks.length; i++) {
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

                sName = "IS" + i;
                uName = "sensor " + i;
                sensors[i] = InstanceManager.getDefault(SensorManager.class).newSensor(sName, uName);
                //TODO: don't do this here because he have to test the failure cases 
                //(no sensor assigned to block) first
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
            for (int i = 0; i < layoutBlocks.length; i++) {
                layoutBlocks[i] = null;
                turnouts[i] = null;
                signalHeads[i] = null;
                sensors[i] = null;
            }
        }
        JUnitUtil.tearDown();
    }
//
//
//    private void waitSeconds(int s) {
//        //waits until queue has been empty for X milliseconds
//        //new QueueTool().waitEmpty(s * 1000);
//
//        //wait until no event is registered for a given number of milliseconds
//        new EventTool().waitNoEvent(s * 1000);
//    }
//
//    //save screenshot of GUI
//    private void captureScreenshot() {
//        //grab image
//        PNGEncoder.captureScreen(System.getProperty("user.home")
//                + System.getProperty("file.separator")
//                + "screen.png");
//    }
//
//   //dump jemmy GUI info to xml file
//   private void dumpToXML() {
//        //grab component state
//        try {
//            Dumper.dumpAll(System.getProperty("user.home")
//                    + System.getProperty("file.separator")
//                    + "dump.xml");
//
//        } catch (FileNotFoundException e) {
//        }
//    }
//
    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorToolsTest.class);
}   //class LayoutEditorToolsTest
