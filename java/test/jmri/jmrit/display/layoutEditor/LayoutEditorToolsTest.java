package jmri.jmrit.display.layoutEditor;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.util.logging.*;
import javax.annotation.*;
import jmri.*;
import jmri.implementation.*;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.*;

/**
 * Test simple functioning of LayoutEditorTools
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	George Warner Copyright (C) 2019
 */
public class LayoutEditorToolsTest {

    @Rule   //10 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(10);

    @Rule   //allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(0);

    private static LayoutEditor layoutEditor = null;
    private static LayoutEditorTools let = null;

    //these all have to contain the same number of elements
    private static LayoutBlock layoutBlocks[] = new LayoutBlock[8];
    private static Turnout turnouts[] = new Turnout[8];
    private static SignalHead signalHeads[] = new SignalHead[8];
    private static Sensor sensors[] = new Sensor[8];

    private static LayoutTurnout layoutTurnout = null;
    private static LayoutTurnout layoutTurnout2 = null;
    private static PositionablePoint positionablePoint1 = null;
    private static PositionablePoint positionablePoint2 = null;
    private static PositionablePoint positionablePoint3 = null;
    private static TrackSegment trackSegment = null;

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
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
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
                33.0, 1.1, 1.2, layoutEditor);
        Assert.assertNotNull("RH turnout for testSetSignalsAtTurnoutWithDone", layoutTurnout);
        layoutEditor.getLayoutTracks().add(layoutTurnout);

        positionablePoint1 = new PositionablePoint("A1", PositionablePoint.ANCHOR, new Point2D.Double(250.0, 100.0), layoutEditor);
        Assert.assertNotNull("positionablePoint1 for testSetSignalsAtTurnoutWithDone", positionablePoint1);
        layoutEditor.getLayoutTracks().add(positionablePoint1);

        positionablePoint2 = new PositionablePoint("A2", PositionablePoint.ANCHOR, new Point2D.Double(50.0, 100.0), layoutEditor);
        layoutEditor.getLayoutTracks().add(positionablePoint2);
        Assert.assertNotNull("positionablePoint2 for testSetSignalsAtTurnoutWithDone", positionablePoint2);

        positionablePoint3 = new PositionablePoint("A3", PositionablePoint.ANCHOR, new Point2D.Double(250.0, 150.0), layoutEditor);
        layoutEditor.getLayoutTracks().add(positionablePoint3);
        Assert.assertNotNull("positionablePoint3 for testSetSignalsAtTurnoutWithDone", positionablePoint3);

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //pressing "Done" should display a dialog
        //SignalsError1 = Error - No turnout name was entered. Please enter a turnout name or cancel.
        Thread modalDialogOperatorThread0 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError1"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread0.isAlive());
        }, "modalDialogOperatorThread0 finished");

        //select the turnout from the popup menu
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("turnoutComboBox"));
        jComboBoxOperator.selectItem(0);  //TODO:fix hardcoded index

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread0a = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError3", turnouts[0].getSystemName()),
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
        //pressing "Done" should display a dialog
        //SignalsError5 = Error - Signal head name was not entered. Please enter\na signal head name for required positions or cancel.
        Thread modalDialogOperatorThread1 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread1.isAlive());
        }, "modalDialogOperatorThread1 finished");

        //select signal head for this combobox
        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("throatContinuingSignalHeadComboBox"));
        jComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread2 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread2.isAlive());
        }, "modalDialogOperatorThread2 finished");

        //select signal head for this combobox
        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("throatDivergingSignalHeadComboBox"));
        jComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread3 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread3.isAlive());
        }, "modalDialogOperatorThread3 finished");

        //select signal head for this combobox
        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("continuingSignalHeadComboBox"));
        jComboBoxOperator.selectItem(3);  //TODO:fix hardcoded index

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread4 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread4.isAlive());
        }, "modalDialogOperatorThread4 finished");

        //select signal head for this combobox
        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("divergingSignalHeadComboBox"));
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

        //pressing "Done" should display a dialog
        //InfoMessage6 = Cannot set up logic because blocks have\nnot been defined around this item.
        //InfoMessage7 = Cannot set up logic because all connections\nhave not been defined around this item.
        Thread modalDialogOperatorThread1 = createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                //Bundle.getMessage("InfoMessage6"),
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
        int types[] = {LayoutTrack.TURNOUT_B, LayoutTrack.TURNOUT_C, LayoutTrack.TURNOUT_A, LayoutTrack.TURNOUT_A};
        PositionablePoint[] positionablePoints = {positionablePoint2, positionablePoint3, positionablePoint1, positionablePoint1};
        TrackSegment trackSegment = new TrackSegment(uName,
                layoutTurnout, types[idx],
                positionablePoints[idx], LayoutTrack.POS_POINT,
                false, false, layoutEditor);
        Assert.assertNotNull("trackSegment not null", trackSegment);
        layoutEditor.getLayoutTracks().add(trackSegment);
        try {
            layoutTurnout.setConnection(types[idx], trackSegment, LayoutTrack.TRACK);
        } catch (JmriException ex) {
            Logger.getLogger(LayoutEditorToolsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread2 = createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                //Bundle.getMessage("InfoMessage5"),
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
        positionablePoints[idx].setType(PositionablePoint.END_BUMPER);

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread3 = createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage6"),
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
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread4 = createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage4", layoutBlocks[lbIndex[idx]].getUserName()),
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
        layoutBlocks[lbIndex[idx]].setOccupancySensorName(null);
        //le.removeTrackSegment(trackSegment);
        positionablePoint1.setType(PositionablePoint.ANCHOR);
        positionablePoint2.setType(PositionablePoint.ANCHOR);
        positionablePoint3.setType(PositionablePoint.ANCHOR);
    }

    @Test
    public void testSetSignalsAtTurnoutWithCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutTurnout to = new LayoutTurnout("Right Hand",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, layoutEditor);
            to.setTurnout(turnouts[0].getSystemName());
            layoutEditor.getLayoutTracks().add(to);

            //this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        //then we find and press the "Cancel" button.
        JButtonOperator jbo = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel"));
        jbo.doClick();
        ///jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
    }

    @Test
    public void testSetSignalsAtTurnoutFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutTurnout to = new LayoutTurnout("Right Hand",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, layoutEditor);
            to.setTurnout(turnouts[0].getSystemName());
            layoutEditor.getLayoutTracks().add(to);
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
    //@Ignore("NPE during execution due to missing frame")
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
    public void testSetSignalsAtLevelXingFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LevelXing lx = new LevelXing("LevelCrossing", point, layoutEditor);
            lx.setLayoutBlockAC(layoutBlocks[0]);
            lx.setLayoutBlockBD(layoutBlocks[1]);

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
    public void testSetSignalsAtThroatToThroatTurnouts() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        //then closes it.
        jFrameOperator.requestClose();
        jFrameOperator.waitClosed();    // make sure the dialog closed
    }

    @Test
    public void testSetSignalsAtThroatToThroatTurnoutsWithDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //pressing "Done" should display a dialog
        //SignalsError1 = Error - No turnout name was entered. Please enter a turnout name or cancel.
        Thread modalDialogOperatorThread0 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError1"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread0.isAlive());
        }, "modalDialogOperatorThread0 finished");

        //so lets create a turnout
        layoutTurnout = new LayoutTurnout("Right Hand",
                LayoutTurnout.RH_TURNOUT, new Point2D.Double(100.0, 100.0),
                180.0, 1.1, 1.2, layoutEditor);
        Assert.assertNotNull("RH turnout for testSetSignalsAtThroatToThroatTurnoutsWithDone", layoutTurnout);
        layoutEditor.getLayoutTracks().add(layoutTurnout);

        //select the turnout from the popup menu
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("turnout1ComboBox"));
        jComboBoxOperator.selectItem(0);  //TODO:fix hardcoded index

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread1 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError3", turnouts[0].getSystemName()),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread1.isAlive());
        }, "modalDialogOperatorThread1 finished");

        layoutTurnout.setTurnout(turnouts[0].getSystemName()); //this should fix the "is not drawn on the panel" error

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread2 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError18"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread2.isAlive());
        }, "modalDialogOperatorThread2 finished");

        //so lets create a second turnout
        layoutTurnout2 = new LayoutTurnout("Left Hand",
                LayoutTurnout.LH_TURNOUT, new Point2D.Double(200.0, 100.0),
                0.0, 1.1, 1.2, layoutEditor);
        Assert.assertNotNull("LH turnout for testSetSignalsAtThroatToThroatTurnoutsWithDone", layoutTurnout2);
        layoutEditor.getLayoutTracks().add(layoutTurnout2);
        layoutTurnout2.setTurnout(turnouts[1].getSystemName()); //this should fix the "is not drawn on the panel" error

        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("turnout2ComboBox"));
        jComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        trackSegment = addNewTrackSegment(layoutTurnout, LayoutTrack.TURNOUT_A,
                layoutTurnout2, LayoutTrack.TURNOUT_A, 1);

        JButtonOperator jButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("GetSaved"));
        jButtonOperator.doClick();

        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("PlaceAllHeads"));
        jCheckBoxOperator.doClick();

        //select the "SetAllLogic" checkbox
        JCheckBoxOperator allLogicCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SetAllLogic"));
        allLogicCheckBoxOperator.doClick(); //click all on
        allLogicCheckBoxOperator.doClick(); //click all off

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread3 = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread3.isAlive());
        }, "modalDialogOperatorThread3 finished");

        //select the turnouts from the popup menus
        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("a1TToTSignalHeadComboBox"));
        jComboBoxOperator.selectItem(0);  //TODO:fix hardcoded index

        //select the turnout from the popup menu
        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("a2TToTSignalHeadComboBox"));
        jComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("b1TToTSignalHeadComboBox"));
        jComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index

        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("b2TToTSignalHeadComboBox"));
        jComboBoxOperator.selectItem(3);  //TODO:fix hardcoded index

        //select the turnout from the popup menu
        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("c1TToTSignalHeadComboBox"));
        jComboBoxOperator.selectItem(4);  //TODO:fix hardcoded index

        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("c2TToTSignalHeadComboBox"));
        jComboBoxOperator.selectItem(5);  //TODO:fix hardcoded index

        //select the turnout from the popup menu
        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("d1TToTSignalHeadComboBox"));
        jComboBoxOperator.selectItem(6);  //TODO:fix hardcoded index

        jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new ByNameComponentChooser("d2TToTSignalHeadComboBox"));
        jComboBoxOperator.selectItem(7);  //TODO:fix hardcoded index

        //this time everything should work
        doneButtonOperator.doClick();
        jFrameOperator.waitClosed();    //make sure the dialog closed
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear
        jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));
        //allLogicCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SetAllLogic"));
        allLogicCheckBoxOperator.doClick(); //click all on

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread4 = createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage6"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.pushNoBlock();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread4.isAlive());
        }, "modalDialogOperatorThread4 finished");

        for (int idx = 0; idx < 3; idx++) {
            waitAndCloseDialog(Bundle.getMessage("MessageTitle"), Bundle.getMessage("InfoMessage6"), Bundle.getMessage("ButtonOK"));
        }

        layoutTurnout.setLayoutBlock(layoutBlocks[0]);
        layoutTurnout2.setLayoutBlock(layoutBlocks[1]);
        trackSegment.setLayoutBlock(layoutBlocks[2]);

        //pressing "Done" should display a dialog
        Thread modalDialogOperatorThread5 = createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage4", layoutBlocks[2].getUserName()),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.pushNoBlock();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread5.isAlive());
        }, "modalDialogOperatorThread5 finished");

        //close three InfoMessage4 dialogs
        for (int idx = 0; idx < 3; idx++) {
            waitAndCloseDialog(Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage4", layoutBlocks[2].getUserName()),
                    Bundle.getMessage("ButtonOK"));
        }

        //assign Occupancy Sensor to block
        layoutBlocks[2].setOccupancySensorName(sensors[2].getUserName());

        if (false) {
            //pressing "Done" should display a dialog
            //InfoMessage7 = Cannot set up logic because all connections\nhave not been defined around this item.
            Thread modalDialogOperatorThread6 = createModalDialogOperatorThread(
                    Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("ButtonOK"), true);  // NOI18N
            doneButtonOperator.doClick();
            JUnitUtil.waitFor(() -> {
                return !(modalDialogOperatorThread6.isAlive());
            }, "modalDialogOperatorThread1 finished");

            waitAndCloseDialog(Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("ButtonOK"));

            waitAndCloseDialog(Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("ButtonOK"));

            waitAndCloseDialog(Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("ButtonOK"));

            waitAndCloseDialog(Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("ButtonOK"));
        }
//
//        captureScreenshot();
//        new JFrameOperator("PLOVER");   //delay for observation
        //this time everything should work
//        doneButtonOperator.doClick();
        jFrameOperator.waitClosed();    //make sure the dialog closed
    }

    @Test
    public void testSetSignalsAtThroatToThroatTurnoutsWithCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        //then we find and press the "Cancel" button.
        JButtonOperator jbo = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel"));
        jbo.doClick();
        ///jFrameOperator.requestClose();
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

    /**
     * convenience method for creating a track segment to connect two
     * LayoutTracks
     *
     * @return the layout editor's toolbar panel
     */
    @Nonnull
    private static TrackSegment addNewTrackSegment(
            @CheckForNull LayoutTrack c1, int t1,
            @CheckForNull LayoutTrack c2, int t2,
            int idx) {
        TrackSegment result = null;
        if ((c1 != null) && (c2 != null)) {
            //create new track segment
            String name = layoutEditor.getFinder().uniqueName("T", idx);
            result = new TrackSegment(name, c1, t1, c2, t2,
                    false, true, layoutEditor);
            Assert.assertNotNull("new TrackSegment is null", result);
            layoutEditor.getLayoutTracks().add(result);
            //link to connected objects
            layoutEditor.setLink(c1, t1, result, LayoutTrack.TRACK);
            layoutEditor.setLink(c2, t2, result, LayoutTrack.TRACK);
        }
        return result;
    }

    public static void waitAndCloseDialog(String dialogTitle, String messageText, String buttonText) {
        JDialogOperator jdo = new JDialogOperator(dialogTitle);
        waitForLables(jdo, messageText);
        JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
        jbo.pushNoBlock();
        jdo.waitClosed();    //make sure the dialog closed
    }

    public static void waitForLables(JDialogOperator jdo, String labelText) {
        waitForLables(jdo, labelText, false);
    }

    public static void waitForLables(JDialogOperator jdo, String labelText, boolean debugFlag) {
        if ((labelText != null) && !labelText.isEmpty()) {
            String[] lines = labelText.split("\\n");
            for (String line : lines) {
                if (debugFlag) {
                    System.out.println("line: " + line);
                }
                //wait for this label
                new JLabelOperator(jdo, line);
            }
        }
    }

    /**
     * createModalDialogOperatorThread (wrapper for
     * JemmyUtil.createModalDialogOperatorThread)
     *
     * @param dialogTitle the title for the dialog to wait for
     * @param buttonText  the name of the button to push to dismiss this dialog
     * @return the thread that's waiting for this dialog
     */
    public static Thread createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        return JemmyUtil.createModalDialogOperatorThread(dialogTitle, buttonText);
    }

    /**
     * create a modal dialog operator thread
     *
     * @param dialogTitle the title for the dialog
     * @param messageText the message for the dialog
     * @param buttonText  the name of the button to press to dismiss
     * @return the thread that is waiting to dismiss this dialog
     */
    public static Thread createModalDialogOperatorThread(String dialogTitle, String messageText, String buttonText) {
        return createModalDialogOperatorThread(dialogTitle, messageText, buttonText, false);
    }

    /**
     * create a modal dialog operator thread
     *
     * @param dialogTitle the title for the dialog
     * @param messageText the message for the dialog
     * @param buttonText  the name of the button to press to dismiss
     * @param debugFlag   set true to dumpToXML, captureScreenshot and log
     *                    message labels
     * @return the thread that is waiting to dismiss this dialog
     */
    public static Thread createModalDialogOperatorThread(String dialogTitle, String messageText, String buttonText, boolean debugFlag) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            if (debugFlag) {
                dumpToXML();
                captureScreenshot();
            }
            waitForLables(jdo, messageText, debugFlag);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }

    public class ByNameComponentChooser implements ComponentChooser {

        private final String componentName;

        public ByNameComponentChooser(String componentName) {
            this.componentName = componentName;
        }

        @Override
        public boolean checkComponent(Component comp) {
            if (comp == null) {
                return false;
            }
            return componentName.equals(comp.getName());
        }

        @Override
        public String getDescription() {
            return componentName;
        }
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
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
            layoutEditor.setVisible(true);

            let = layoutEditor.getLETools();

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
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
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

    //save screenshot of GUI
    private static void captureScreenshot() {
        //grab image
        PNGEncoder.captureScreen(System.getProperty("user.home")
                + System.getProperty("file.separator")
                + "screen.png");
    }

    //dump jemmy GUI info to xml file
    private static void dumpToXML() {
        //grab component state
        try {
            Dumper.dumpAll(System.getProperty("user.home")
                    + System.getProperty("file.separator")
                    + "dump.xml");

        } catch (FileNotFoundException e) {
        }
    }
//
    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorToolsTest.class);
}   //class LayoutEditorToolsTest
