package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import jmri.*;
import jmri.implementation.*;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of LayoutEditorTools
 *
 * @author Paul Bender Copyright (C) 2016
 * @author George Warner Copyright (C) 2019
 */
public class LayoutEditorToolsTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); //10 second timeout for methods in this test class.

    //allow 2 retries of intermittent tests
    @Rule
    public RetryRule retryRule = new RetryRule(2); //allow 2 retries

    private LayoutEditor layoutEditor = null;
    private LayoutEditorTools let = null;

    //these all have to contain the same number of elements
    private List<LayoutBlock> layoutBlocks;
    private List<Turnout> turnouts;
    private List<SignalHead> signalHeads;
    private List<Sensor> sensors;

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
    @Disabled("causes error on jenkins; exhausts failure retries")
    public void testSetSignalsAtTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        //then closes it.
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
    }

    @Test
    @Disabled("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //create a new Layout Turnout
        layoutTurnout = new LayoutRHTurnout("Right Hand",
                // new Point2D.Double(150.0, 100.0),
                // 33.0, 1.1, 1.2,
                layoutEditor);
        LayoutTurnoutView ltv = new LayoutTurnoutView(layoutTurnout,
                new Point2D.Double(150.0, 100.0),
                33.0, 1.1, 1.2,
                layoutEditor);
        Assert.assertNotNull("RH turnout for testSetSignalsAtTurnoutWithDone", layoutTurnout);
        layoutEditor.addLayoutTrack(layoutTurnout, ltv);

        positionablePoint1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, layoutEditor); // new Point2D.Double(250.0, 100.0),
        PositionablePointView pp1v = new PositionablePointView(positionablePoint1, new Point2D.Double(250.0, 100.0), layoutEditor);
        Assert.assertNotNull("positionablePoint1 for testSetSignalsAtTurnoutWithDone", positionablePoint1);
        layoutEditor.addLayoutTrack(positionablePoint1, pp1v);

        positionablePoint2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, layoutEditor); // new Point2D.Double(50.0, 100.0),
        PositionablePointView pp2v = new PositionablePointView(positionablePoint2, new Point2D.Double(250.0, 100.0), layoutEditor);
        layoutEditor.addLayoutTrack(positionablePoint2, pp2v);
        Assert.assertNotNull("positionablePoint2 for testSetSignalsAtTurnoutWithDone", positionablePoint2);

        positionablePoint3 = new PositionablePoint("A3", PositionablePoint.PointType.ANCHOR, layoutEditor);
        PositionablePointView pp3v = new PositionablePointView(positionablePoint3, new Point2D.Double(250.0, 150.0), layoutEditor);
        layoutEditor.addLayoutTrack(positionablePoint3, pp3v);
        Assert.assertNotNull("positionablePoint3 for testSetSignalsAtTurnoutWithDone", positionablePoint3);

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
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

        layoutTurnout.setTurnout(turnouts.get(0).getSystemName()); //this should fix the "is not drawn on the panel" error

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
        //layoutBlocks.get(i).setOccupancySensorName(uName);
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
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //define connection
        String uName = "T" + (idx + 1);
        HitPointType types[] = {HitPointType.TURNOUT_B, HitPointType.TURNOUT_C, HitPointType.TURNOUT_A, HitPointType.TURNOUT_A};
        PositionablePoint[] positionablePoints = {positionablePoint2, positionablePoint3, positionablePoint1, positionablePoint1};
        TrackSegment trackSegment = new TrackSegment(uName,
                layoutTurnout, types[idx],
                positionablePoints[idx], HitPointType.POS_POINT,
                false, layoutEditor);
        TrackSegmentView trackSegmentView = new TrackSegmentView(trackSegment, layoutEditor);
        Assert.assertNotNull("trackSegment not null", trackSegment);
        Assert.assertNotNull("trackSegmentView not null", trackSegmentView);
        layoutEditor.addLayoutTrack(trackSegment, trackSegmentView);
        try {
            layoutTurnout.setConnection(types[idx], trackSegment, HitPointType.TRACK);
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
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //change anchor to end bumper
        positionablePoints[idx].setType(PositionablePoint.PointType.END_BUMPER);

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
        trackSegment.setLayoutBlock(layoutBlocks.get(lbIndex[idx]));

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
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
        layoutBlocks.get(lbIndex[idx]).setOccupancySensorName(sensors.get(lbIndex[idx]).getUserName());

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        doneButtonOperator.doClick();
        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to (re)appear
        jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        //doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        cboSetLogic = new JCheckBoxOperator(jfoSignalsAtTurnout, Bundle.getMessage("SetLogic"), idx);
        cboSetLogic.doClick(); //click off

        //reset these
        trackSegment.setLayoutBlock(null);
        layoutBlocks.get(lbIndex[idx]).setOccupancySensorName(null);
        //le.removeTrackSegment(trackSegment);
        positionablePoint1.setType(PositionablePoint.PointType.ANCHOR);
        positionablePoint2.setType(PositionablePoint.PointType.ANCHOR);
        positionablePoint3.setType(PositionablePoint.PointType.ANCHOR);
    }

    @Test
    public void testSetSignalsAtTurnoutWithCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutRHTurnout to = new LayoutRHTurnout("Right Hand", layoutEditor);
            LayoutRHTurnoutView tov = new LayoutRHTurnoutView(to,
                    point, 33.0, 1.1, 1.2, layoutEditor);
            to.setTurnout(turnouts.get(0).getSystemName());
            layoutEditor.addLayoutTrack(to, tov);

            //this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
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
    @Disabled("causes error on jenkins; exhausts failure retries")
    public void testSetSignalsAtTurnoutFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutRHTurnout to = new LayoutRHTurnout("Right Hand", layoutEditor);
            LayoutRHTurnoutView tov = new LayoutRHTurnoutView(to,
                    point, 33.0, 1.1, 1.2, layoutEditor);
            to.setTurnout(turnouts.get(0).getSystemName());
            layoutEditor.addLayoutTrack(to, tov);
            //this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        //then closes it.
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
    }

    @Test
    @Disabled("causes error on jenkins; exhausts failure retries")
    public void testSetSignalsAtLevelXing() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads Level Crossing" dialog to be displayed.
            let.setSignalsAtLevelXing(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtLevelXing"));
        //then closes it.
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
    }

    @Test
    @Disabled("causes error on jenkins; exhausts failure retries")
    public void testSetSignalsAtLevelXingFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            // Point2D point = new Point2D.Double(150.0, 100.0);
            LevelXing lx = new LevelXing("LevelCrossing", layoutEditor);
            // LevelXingView lxv = new LevelXingView(lx, point, layoutEditor);

            lx.setLayoutBlockAC(layoutBlocks.get(0));
            lx.setLayoutBlockBD(layoutBlocks.get(1));

            //this causes a "set Signal Heads Level Crossing" dialog to be displayed.
            let.setSignalsAtLevelXingFromMenu(lx, getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
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
        Assert.assertEquals("signal head for valid name", signalHeads.get(1), let.getHeadFromName("IH1"));
    }

    @Test
    @Disabled("causes error on jenkins; exhausts failure retries")
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
    @Disabled("Consistently fails on AppVeyor and Windows 12/20/2019")
    public void testSetSignalHeadOnPanelAtXYIntAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(signalHeads.get(1)));
        let.setSignalHeadOnPanel(0.D, "IH1", 0, 0);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(signalHeads.get(1)));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(signalHeads.get(1)));
    }

    @Test
    @Disabled("Consistently fails on AppVeyor and Windows 12/20/2019")
    public void testSetSignalHeadOnPanelAtPointAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(signalHeads.get(1)));
        Point2D point = new Point2D.Double(150.0, 100.0);
        let.setSignalHeadOnPanel(0.D, "IH1", point);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(signalHeads.get(1)));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(signalHeads.get(1)));
    }

    @Test
    @Disabled("Consistently fails on AppVeyor and Windows 12/20/2019")
    public void testSetSignalHeadOnPanelAtXYDoubleAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(signalHeads.get(1)));
        let.setSignalHeadOnPanel(0.D, "IH1", 0, 0);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(signalHeads.get(1)));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(signalHeads.get(1)));
    }

    @Test
    @Disabled("causes error on jenkins; exhausts failure retries")
    public void testGetSignalHeadIcon() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("Signal head icon for panel", let.getSignalHeadIcon("IH1"));
    }

    @Test
    public void testIsHeadOnPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Signal head not on panel", let.isHeadOnPanel(signalHeads.get(1)));
    }

    @Test
    public void testIsHeadAssignedAnywhere() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Signal head not on panel", let.isHeadAssignedAnywhere(signalHeads.get(1)));
    }

    @Test
    public void testRemoveSignalHeadAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //just verify this doesn't thrown an error.
        let.removeAssignment(signalHeads.get(1));
    }

    @Test
    @Disabled("causes error on jenkins; exhausts failure retries")
    public void testInitializeBlockBossLogic() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("Signal head block boss logic started", let.initializeBlockBossLogic("IH1"));
    }

    /**
     * convenience method for accessing...
     *
     * @return the layout editor's toolbar panel
     */
    @Nonnull
    public LayoutEditorToolBarPanel getLayoutEditorToolBarPanel() {
        return layoutEditor.getLayoutEditorToolBarPanel();
    }

    //from here down is testing infrastructure
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();

        if (!GraphicsEnvironment.isHeadless()) {

            layoutEditor = new LayoutEditor();
            layoutEditor.setVisible(true);

            let = layoutEditor.getLETools();

            for (int i = 0; i < 5; i++) {
                String sBlockName = "IB" + i;
                String uBlockName = "Block " + i;
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(sBlockName, uBlockName);
            }
            layoutBlocks = InstanceManager.getDefault(LayoutBlockManager.class).getNamedBeanSet().stream().collect(Collectors.toList());

            for (int i = 0; i < 5; i++) {
                String toName = "IT" + i;
                InstanceManager.getDefault(TurnoutManager.class).provideTurnout(toName);
            }
            turnouts = InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().stream().collect(Collectors.toList());

            for (int i = 0; i < 5; i++) {
                String sName = "IS" + i;
                String uName = "sensor " + i;
                InstanceManager.getDefault(SensorManager.class).provideSensor(sName).setUserName(uName);
            }
            sensors = InstanceManager.getDefault(SensorManager.class).getNamedBeanSet().stream().collect(Collectors.toList());

            for (int i = 0; i < 5; i++) {
                String sName = "IH" + i;
                String uName = "signal head " + i;
                VirtualSignalHead signalHead = new VirtualSignalHead(sName, uName);
                InstanceManager.getDefault(SignalHeadManager.class).register(signalHead);
            }
            signalHeads = InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().stream().collect(Collectors.toList());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            layoutBlocks.stream().forEach(LayoutBlock::dispose);
            turnouts.stream().forEach(Turnout::dispose);
            signalHeads.stream().forEach(SignalHead::dispose);
            sensors.stream().forEach(Sensor::dispose);
            EditorFrameOperator operator = new EditorFrameOperator(layoutEditor);
            operator.closeFrameWithConfirmations();
            JUnitUtil.dispose(layoutEditor);
            EditorFrameOperator.clearEditorFrameOperatorThreads();
        }

        InstanceManager.getDefault(LayoutBlockManager.class).dispose();
        InstanceManager.getDefault(SignalHeadManager.class).dispose();
        InstanceManager.getDefault(TurnoutManager.class).dispose();
        InstanceManager.getDefault(SensorManager.class).dispose();

        let = null;
        layoutEditor = null;
        layoutBlocks = null;
        turnouts = null;
        signalHeads = null;
        sensors = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorToolsTest.class);
}   //class LayoutEditorToolsTest
