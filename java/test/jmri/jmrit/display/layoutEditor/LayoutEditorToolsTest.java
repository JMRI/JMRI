package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;
import javax.annotation.*;
import jmri.*;
import jmri.implementation.*;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Test simple functioning of LayoutEditorTools
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	George Warner Copyright (C) 2019
 */
public class LayoutEditorToolsTest {

    @Rule   //5 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(5);

    @Rule   //allow 3 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(3);

    private static Operator.StringComparator stringComparator;

    private static LayoutEditor layoutEditor = null;
    private static LayoutEditorTools let = null;

    //these all have to contain the same number of elements
    private List<LayoutBlock> layoutBlocks = null;
    private List<Turnout> turnouts = null;
    private List<SignalHead> signalHeads = null;
    private List<Sensor> sensors = null;

    private static LayoutTurnout layoutTurnout = null;
    private static LayoutTurnout layoutTurnout2 = null;
    private static PositionablePoint positionablePoint1 = null;
    private static PositionablePoint positionablePoint2 = null;
    private static PositionablePoint positionablePoint3 = null;
    private static TrackSegment trackSegment = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("let null", let);
    }

    @Test
    public void testHitEndBumper() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("let null", let);
        //we haven't done anything, so reachedEndBumper should return false.
        Assert.assertFalse("reached end bumper", let.reachedEndBumper());
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        JemmyUtil.waitAndCloseFrame(Bundle.getMessage("SignalsAtTurnout"));
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart1() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //pressing "Done" should display a dialog
        //SignalsError1 = Error - No turnout name was entered. Please enter a turnout name or cancel.
        Thread modalDialogOperatorThread1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError1"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread1.isAlive());
        }, "modalDialogOperatorThread1 finished");

        //now close this dialog
        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtTurnoutWithDonePart1

    private void setupSetSignalsAtTurnoutWithDonePart1() {
        List<LayoutTrack> layoutTracks = layoutEditor.getLayoutTracks();
        //create a new Layout Turnout
        layoutTurnout = new LayoutTurnout("Right Hand",
                LayoutTurnout.TurnoutType.RH_TURNOUT, new Point2D.Double(150.0, 100.0),
                33.0, 1.1, 1.2, layoutEditor);
        Assert.assertNotNull("RH turnout for testSetSignalsAtTurnoutWithDone", layoutTurnout);
        layoutTracks.add(layoutTurnout);

        positionablePoint1 = new PositionablePoint("A1", PositionablePoint.ANCHOR, new Point2D.Double(250.0, 100.0), layoutEditor);
        Assert.assertNotNull("positionablePoint1 for testSetSignalsAtTurnoutWithDone", positionablePoint1);
        layoutTracks.add(positionablePoint1);

        positionablePoint2 = new PositionablePoint("A2", PositionablePoint.ANCHOR, new Point2D.Double(50.0, 100.0), layoutEditor);
        layoutTracks.add(positionablePoint2);
        Assert.assertNotNull("positionablePoint2 for testSetSignalsAtTurnoutWithDone", positionablePoint2);

        positionablePoint3 = new PositionablePoint("A3", PositionablePoint.ANCHOR, new Point2D.Double(250.0, 150.0), layoutEditor);
        layoutTracks.add(positionablePoint3);
        Assert.assertNotNull("positionablePoint3 for testSetSignalsAtTurnoutWithDone", positionablePoint3);
    }   //setupSetSignalsAtTurnoutWithDone

    @Test
    ///@Ignore("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        new EventTool().waitNoEvent(0);

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart2(jFrameOperator);

        //pressing "Done" should display a dialog
        //SignalsError3 = Error - Turnout "{0}" is not drawn on the panel.\nPlease enter the name of a drawn turnout.
        Thread modalDialogOperatorThread2 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError3", turnouts.get(0).getSystemName()),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread2.isAlive());
        }, "modalDialogOperatorThread2 finished");

        //now close this dialog
        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtTurnoutWithDonePart2

    private void setupSetSignalsAtTurnoutWithDonePart2(JFrameOperator jFrameOperator) {
        setupSetSignalsAtTurnoutWithDonePart1();

        //select the turnout from the popup menu
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser("turnoutComboBox"));

        new EventTool().waitNoEvent(0);

        jComboBoxOperator.selectItem(turnouts.get(0).getSystemName());
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart3() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart3(jFrameOperator);

        JButtonOperator jButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("GetSaved"));
        jButtonOperator.push();

        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("PlaceAllHeads"));
        jCheckBoxOperator.push();

        //select the "SetAllLogic" checkbox
        JCheckBoxOperator allLogicCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SetAllLogic"));
        do {
            allLogicCheckBoxOperator.push(); //toggle all on/off
        } while (allLogicCheckBoxOperator.isSelected());

        /*
        * test all four comboboxes for "Signal head name was not entered"  (SignalsError5)
         */
        //pressing "Done" should display a dialog
        //SignalsError5 = Error - Signal head name was not entered. Please enter\na signal head name for required positions or cancel.
        Thread modalDialogOperatorThread3 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread3.isAlive());
        }, "modalDialogOperatorThread3 finished");

        //now close this dialog
        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtTurnoutWithDonePart3

    private void setupSetSignalsAtTurnoutWithDonePart3(JFrameOperator jFrameOperator) {
        setupSetSignalsAtTurnoutWithDonePart2(jFrameOperator);
        //this should fix the "is not drawn on the panel" error
        layoutTurnout.setTurnout(turnouts.get(0).getSystemName());
    }

    @Test
    ///@Ignore("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart4() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart4(jFrameOperator);

        //pressing "Done" should display a dialog
        //SignalsError5 = Error - Signal head name was not entered. Please enter\na signal head name for required positions or cancel.
        Thread modalDialogOperatorThread4 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread4.isAlive());
        }, "modalDialogOperatorThread4 finished");

        new EventTool().waitNoEvent(0);

        //now close this dialog
        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtTurnoutWithDonePart4

    private void setupSetSignalsAtTurnoutWithDonePart4(JFrameOperator jFrameOperator) {
        setupSetSignalsAtTurnoutWithDonePart3(jFrameOperator);

        //select signal head for this combobox
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser("throatContinuingSignalHeadComboBox"));

        new EventTool().waitNoEvent(0);

        jComboBoxOperator.selectItem(signalHeads.get(0).getSystemName());
    }

    @Test
    ///@Ignore("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart5() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        new EventTool().waitNoEvent(0);

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart5(jFrameOperator);

        //pressing "Done" should display a dialog
        //SignalsError5 = Error - Signal head name was not entered. Please enter\na signal head name for required positions or cancel.
        Thread modalDialogOperatorThread5 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread5.isAlive());
        }, "modalDialogOperatorThread5 finished");

        //now close this dialog
        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtTurnoutWithDonePart5

    private void setupSetSignalsAtTurnoutWithDonePart5(JFrameOperator jFrameOperator) {
        setupSetSignalsAtTurnoutWithDonePart4(jFrameOperator);

        //select signal head for this combobox
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser("throatDivergingSignalHeadComboBox"));

        new EventTool().waitNoEvent(0);

        jComboBoxOperator.selectItem(signalHeads.get(1).getSystemName());
    }

    @Test
    ///@Ignore("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart6() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart6(jFrameOperator);

        //pressing "Done" should display a dialog
        //SignalsError5 = Error - Signal head name was not entered. Please enter\na signal head name for required positions or cancel.
        Thread modalDialogOperatorThread6 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread6.isAlive());
        }, "modalDialogOperatorThread6 finished");

        new EventTool().waitNoEvent(0);

        //now close this dialog
        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtTurnoutWithDonePart6

    private void setupSetSignalsAtTurnoutWithDonePart6(JFrameOperator jFrameOperator) {
        setupSetSignalsAtTurnoutWithDonePart5(jFrameOperator);

        //select signal head for this combobox
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser("continuingSignalHeadComboBox"));

        new EventTool().waitNoEvent(0);

        jComboBoxOperator.selectItem(signalHeads.get(2).getSystemName());
    }

    @Test
    ///@Ignore("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart7a() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart7(jFrameOperator);

        testSetupSSL(0);    //test Throat Continuing SSL logic setup

        //TODO: fix the other failure conditions (testing each one)
        //layoutBlocks[i].setOccupancySensorName(uName);
//
        //this time everything should work
        doneButtonOperator.push();
        jFrameOperator.waitClosed();    //make sure the dialog closed
    }   //testSetSignalsAtTurnoutWithDonePart7a

    @Test
    ///@Ignore("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart7b() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart7(jFrameOperator);

        testSetupSSL(1);    //test Throat Diverging SSL logic setup

        //TODO: fix the other failure conditions (testing each one)
        //layoutBlocks[i].setOccupancySensorName(uName);
//
        //this time everything should work
        doneButtonOperator.push();
        jFrameOperator.waitClosed();    //make sure the dialog closed
    }   //testSetSignalsAtTurnoutWithDonePart7b

    @Test
    ///@Ignore("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart7c() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart7(jFrameOperator);

        testSetupSSL(2);    //test Continuing SSL logic setup

        //TODO: fix the other failure conditions (testing each one)
        //layoutBlocks[i].setOccupancySensorName(uName);
//
        //this time everything should work
        doneButtonOperator.push();
        jFrameOperator.waitClosed();    //make sure the dialog closed
    }   //testSetSignalsAtTurnoutWithDonePart7c

    @Test
    ///@Ignore("Consistently fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutWithDonePart7d() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //this causes a "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        setupSetSignalsAtTurnoutWithDonePart7(jFrameOperator);

        testSetupSSL(3);    //test Diverging SSL logic setup

        //TODO: fix the other failure conditions (testing each one)
        //layoutBlocks.get(i).setOccupancySensorName(uName);
//
        //this time everything should work
        doneButtonOperator.push();
        jFrameOperator.waitClosed();    //make sure the dialog closed
    }   //testSetSignalsAtTurnoutWithDonePart7d

    private void setupSetSignalsAtTurnoutWithDonePart7(JFrameOperator jFrameOperator) {
        setupSetSignalsAtTurnoutWithDonePart6(jFrameOperator);

        //select signal head for this combobox
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser("divergingSignalHeadComboBox"));

        new EventTool().waitNoEvent(0);

        jComboBoxOperator.selectItem(signalHeads.get(3).getSystemName());
    }

    /*
     * test SSL logic setup
     */
    private void testSetupSSL(int idx) {
        JFrameOperator jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        //NOTE: index used here because there are four identical buttons
        JCheckBoxOperator cboSetLogic = new JCheckBoxOperator(jfoSignalsAtTurnout, Bundle.getMessage("SetLogic"), idx);
        cboSetLogic.push(); //turn on

        //pressing "Done" should display a dialog
        //InfoMessage6 = Cannot set up logic because blocks have\nnot been defined around this item.
        //InfoMessage7 = Cannot set up logic because all connections\nhave not been defined around this item.
        Thread modalDialogOperatorThread1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                //Bundle.getMessage("InfoMessage7"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread1.isAlive());
        }, "modalDialogOperatorThread1 finished");

        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        //define connection
        String uName = "T" + (idx + 1);
        LayoutEditor.HitPointType types[] = {LayoutEditor.HitPointType.TURNOUT_B, LayoutEditor.HitPointType.TURNOUT_C, LayoutEditor.HitPointType.TURNOUT_A, LayoutEditor.HitPointType.TURNOUT_A};
        PositionablePoint[] positionablePoints = {positionablePoint2, positionablePoint3, positionablePoint1, positionablePoint1};
        TrackSegment trackSegment = new TrackSegment(uName,
                layoutTurnout, types[idx],
                positionablePoints[idx], LayoutEditor.HitPointType.POS_POINT,
                false, false, layoutEditor);
        Assert.assertNotNull("trackSegment not null", trackSegment);
        layoutEditor.getLayoutTracks().add(trackSegment);
        try {
            layoutTurnout.setConnection(types[idx], trackSegment, LayoutEditor.HitPointType.TRACK);
        }
        catch (JmriException ex) {
            Logger.getLogger(LayoutEditorToolsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        //pressing "Done" should display a dialog
        //InfoMessage5 = Cannot set up logic because the next signal (in or \nat the end of block "{0}") apparently is not yet defined.
        //InfoMessage6 = Cannot set up logic because blocks have\nnot been defined around this item.
        Thread modalDialogOperatorThread2 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                //Bundle.getMessage("InfoMessage6"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread2.isAlive());
        }, "modalDialogOperatorThread2 finished");

        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to appear
        jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        //change anchor to end bumper
        positionablePoints[idx].setType(PositionablePoint.END_BUMPER);

        //pressing "Done" should display a dialog
        //InfoMessage6 = Cannot set up logic because blocks have\nnot been defined around this item.
        Thread modalDialogOperatorThread3 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage6"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
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

        //the JFrameOperator waits for the set signal frame to appear
        jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        //pressing "Done" should display a dialog
        //InfoMessage4 = Cannot set up logic because block "{0}"\ndoesn''t have an occupancy sensor.
        Thread modalDialogOperatorThread4 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage4", layoutBlocks.get(lbIndex[idx]).getUserName()),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
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

        //the JFrameOperator waits for the set signal frame to appear
        jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        doneButtonOperator.push();
        // make sure the dialog closed
        jfoSignalsAtTurnout.waitClosed();

        //this causes the "set Signal Heads Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtTurnout(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        //the JFrameOperator waits for the set signal frame to (re)appear
        jfoSignalsAtTurnout = new JFrameOperator(Bundle.getMessage("SignalsAtTurnout"));
        doneButtonOperator = new JButtonOperator(jfoSignalsAtTurnout, Bundle.getMessage("ButtonDone"));

        cboSetLogic = new JCheckBoxOperator(jfoSignalsAtTurnout, Bundle.getMessage("SetLogic"), idx);
        cboSetLogic.push(); //turn off

        //reset these
        trackSegment.setLayoutBlock(null);
        layoutBlocks.get(lbIndex[idx]).setOccupancySensorName(null);
        //le.removeTrackSegment(trackSegment);
        positionablePoint1.setType(PositionablePoint.ANCHOR);
        positionablePoint2.setType(PositionablePoint.ANCHOR);
        positionablePoint3.setType(PositionablePoint.ANCHOR);
    }   //testSetupSSL

    @Test
    public void testSetSignalsAtTurnoutWithCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutTurnout to = new LayoutTurnout("Right Hand",
                    LayoutTurnout.TurnoutType.RH_TURNOUT, point, 33.0, 1.1, 1.2, layoutEditor);
            to.setTurnout(turnouts.get(0).getSystemName());
            layoutEditor.getLayoutTracks().add(to);

            //this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        JemmyUtil.waitAndCloseFrame(Bundle.getMessage("SignalsAtTurnout"), Bundle.getMessage("ButtonCancel"));
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtTurnoutFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LayoutTurnout to = new LayoutTurnout("Right Hand",
                    LayoutTurnout.TurnoutType.RH_TURNOUT, point, 33.0, 1.1, 1.2, layoutEditor);
            to.setTurnout(turnouts.get(0).getSystemName());
            layoutEditor.getLayoutTracks().add(to);
            //this causes a "set Signal Heads Turnout" dialog to be displayed.
            let.setSignalsAtTurnoutFromMenu(to, getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        JemmyUtil.waitAndCloseFrame(Bundle.getMessage("SignalsAtTurnout"));
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtLevelXing() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads Level Crossing" dialog to be displayed.
            let.setSignalsAtLevelXing(getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        JemmyUtil.waitAndCloseFrame(Bundle.getMessage("SignalsAtLevelXing"));
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtLevelXingFromMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            Point2D point = new Point2D.Double(150.0, 100.0);
            LevelXing lx = new LevelXing("LevelCrossing", point, layoutEditor);
            lx.setLayoutBlockAC(layoutBlocks.get(0));
            lx.setLayoutBlockBD(layoutBlocks.get(1));

            //this causes a "set Signal Heads Level Crossing" dialog to be displayed.
            let.setSignalsAtLevelXingFromMenu(lx, getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        JemmyUtil.waitAndCloseFrame(Bundle.getMessage("SignalsAtLevelXing"));
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnouts() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
        ThreadingUtil.runOnLayoutEventually(() -> {
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        JemmyUtil.waitAndCloseFrame(Bundle.getMessage("SignalsAtTToTTurnout"), Bundle.getMessage("ButtonCancel"));
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnoutsWithDonePart1() {
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
        Thread modalDialogOperatorThread1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError1"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread1.isAlive());
        }, "modalDialogOperatorThread1 finished");

        new EventTool().waitNoEvent(0);

        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtThroatToThroatTurnoutsWithDonePart1

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnoutsWithDonePart2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //fixes SignalsError1 = Error - No turnout name was entered. Please enter a turnout name or cancel.
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart2(jFrameOperator);

        //pressing "Done" should display a dialog
        //SignalsError3 = Error - Turnout "{0}" is not drawn on the panel.\nPlease enter the name of a drawn turnout.
        Thread modalDialogOperatorThread2 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError3", turnouts.get(0).getSystemName()),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread2.isAlive());
        }, "modalDialogOperatorThread2 finished");

        new EventTool().waitNoEvent(0);

        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtThroatToThroatTurnoutsWithDonePart2

    private void setupSetSignalsAtThroatToThroatTurnoutsWithDonePart2(JFrameOperator jFrameOperator) {
        //fixes SignalsError1 = Error - No turnout name was entered. Please enter a turnout name or cancel.
        //so lets create a turnout
        layoutTurnout = new LayoutTurnout("Right Hand",
                LayoutTurnout.TurnoutType.RH_TURNOUT, new Point2D.Double(100.0, 100.0),
                180.0, 1.1, 1.2, layoutEditor);
        Assert.assertNotNull("RH turnout for testSetSignalsAtThroatToThroatTurnoutsWithDone", layoutTurnout);
        layoutEditor.getLayoutTracks().add(layoutTurnout);

        //select the turnout from the popup menu
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser("turnout1ComboBox"));

        new EventTool().waitNoEvent(0);

        jComboBoxOperator.selectItem(turnouts.get(0).getSystemName());
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnoutsWithDonePart3() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //fixes SignalsError3 = Error - Turnout "{0}" is not drawn on the panel.\nPlease enter the name of a drawn turnout.
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart3(jFrameOperator);

        //pressing "Done" should display a dialog
        //SignalsError18 = Error - This tool requires two turnouts (RH, LH, or WYE) \nconnected throat-to-throat by a single track segment.
        Thread modalDialogOperatorThread3 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError18"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread3.isAlive());
        }, "modalDialogOperatorThread3 finished");

        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtThroatToThroatTurnoutsWithDonePart3

    private void setupSetSignalsAtThroatToThroatTurnoutsWithDonePart3(JFrameOperator jFrameOperator) {
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart2(jFrameOperator);
        //fixes SignalsError3 = Error - Turnout "{0}" is not drawn on the panel.\nPlease enter the name of a drawn turnout.
        layoutTurnout.setTurnout(turnouts.get(0).getSystemName());
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnoutsWithDonePart4() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //fixes SignalsError18 = Error - This tool requires two turnouts (RH, LH, or WYE) \nconnected throat-to-throat by a single track segment.
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart4(jFrameOperator);

        JButtonOperator jButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("GetSaved"));
        jButtonOperator.push();

        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("PlaceAllHeads"));
        jCheckBoxOperator.push();

        //select the "SetAllLogic" checkbox
        JCheckBoxOperator allLogicCheckBoxOperator = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SetAllLogic"));
        do {
            allLogicCheckBoxOperator.push(); //toggle all on/off
        } while (allLogicCheckBoxOperator.isSelected());

        //pressing "Done" should display a dialog
        //SignalsError5 = Error - Signal head name was not entered. Please enter\na signal head name for required positions or cancel.
        Thread modalDialogOperatorThread4 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("SignalsError5"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread4.isAlive());
        }, "modalDialogOperatorThread4 finished");

        new EventTool().waitNoEvent(0);

        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtThroatToThroatTurnoutsWithDonePart4

    private void setupSetSignalsAtThroatToThroatTurnoutsWithDonePart4(JFrameOperator jFrameOperator) {
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart3(jFrameOperator);
        //fixes SignalsError18 = Error - This tool requires two turnouts (RH, LH, or WYE) \nconnected throat-to-throat by a single track segment.
        //so lets create a second turnout
        layoutTurnout2 = new LayoutTurnout("Left Hand",
                LayoutTurnout.TurnoutType.LH_TURNOUT, new Point2D.Double(200.0, 100.0),
                0.0, 1.1, 1.2, layoutEditor);
        Assert.assertNotNull("LH turnout for testSetSignalsAtThroatToThroatTurnoutsWithDone", layoutTurnout2);
        layoutEditor.getLayoutTracks().add(layoutTurnout2);
        layoutTurnout2.setTurnout(turnouts.get(1).getSystemName()); //this should fix the "is not drawn on the panel" error

        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser("turnout2ComboBox"));

        new EventTool().waitNoEvent(0);

        jComboBoxOperator.selectItem(turnouts.get(1).getSystemName());

        trackSegment = addNewTrackSegment(layoutTurnout, LayoutEditor.HitPointType.TURNOUT_A,
                layoutTurnout2, LayoutEditor.HitPointType.TURNOUT_A, 1);
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnoutsWithDonePart5() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //fixes SignalsError5 = Error - Signal head name was not entered. Please enter\na signal head name for required positions or cancel.
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart5(jFrameOperator);

        //this time everything should work
        doneButtonOperator.push();
        jFrameOperator.waitClosed();    //make sure the dialog closed
    }   //testSetSignalsAtThroatToThroatTurnoutsWithDonePart5

    private void setupSetSignalsAtThroatToThroatTurnoutsWithDonePart5(JFrameOperator jFrameOperator) {
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart4(jFrameOperator);
        //fixes SignalsError5 = Error - Signal head name was not entered. Please enter\na signal head name for required positions or cancel.
        //select the turnouts from the popup menus
        List<String> names = new ArrayList<>(Arrays.asList(
                "a1TToTSignalHeadComboBox",
                "a2TToTSignalHeadComboBox",
                "b1TToTSignalHeadComboBox",
                "b2TToTSignalHeadComboBox",
                "c1TToTSignalHeadComboBox",
                "c2TToTSignalHeadComboBox",
                "d1TToTSignalHeadComboBox",
                "d2TToTSignalHeadComboBox"));
        int idx = 0;
        for (String name : names) {
            JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(
                    jFrameOperator, new NameComponentChooser(name));

            new EventTool().waitNoEvent(0);

            jComboBoxOperator.selectItem(signalHeads.get(idx++).getSystemName());
        }
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnoutsWithDonePart6() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear
        jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //turn on all logic check boxes
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart6(jFrameOperator);

        //pressing "Done" should display a dialog
        //InfoMessage6 = Cannot set up logic because blocks have\nnot been defined around this item.
        Thread modalDialogOperatorThread6 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage6"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.pushNoBlock();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread6.isAlive());
        }, "modalDialogOperatorThread6 finished");

        for (int idx = 0; idx < 3; idx++) {
            new EventTool().waitNoEvent(0);

            //InfoMessage6 = Cannot set up logic because blocks have\nnot been defined around this item.
            JemmyUtil.waitAndCloseDialog(Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("ButtonOK"));  // NOI18N
        }

        JemmyUtil.waitAndCloseFrame(jFrameOperator);
        //jFrameOperator.waitClosed();    //make sure the frame closed

    }   //testSetSignalsAtThroatToThroatTurnoutsWithDonePart6

    private void setupSetSignalsAtThroatToThroatTurnoutsWithDonePart6(JFrameOperator jFrameOperator) {
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart5(jFrameOperator);
        //turn on all logic check boxes
        JCheckBoxOperator allLogicCheckBoxOperator = new JCheckBoxOperator(
                jFrameOperator, Bundle.getMessage("SetAllLogic"));
        do {
            allLogicCheckBoxOperator.push(); //toggle all on/off
        } while (!allLogicCheckBoxOperator.isSelected());
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnoutsWithDonePart7() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //fixes InfoMessage6 = Cannot set up logic because blocks have\nnot been defined around this item.
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart7(jFrameOperator);

        //pressing "Done" should display a dialog
        //InfoMessage4 = Cannot set up logic because block "{0}"\ndoesn''t have an occupancy sensor.
        Thread modalDialogOperatorThread7 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage4", layoutBlocks.get(2).getUserName()),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.pushNoBlock();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread7.isAlive());
        }, "modalDialogOperatorThread7 finished");

        //close three InfoMessage4 dialogs
        for (int idx = 0; idx < 3; idx++) {
            new EventTool().waitNoEvent(0);

            JemmyUtil.waitAndCloseDialog(Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage4", layoutBlocks.get(2).getUserName()),
                    Bundle.getMessage("ButtonOK"));  // NOI18N
        }

        JemmyUtil.waitAndCloseFrame(jFrameOperator);
    }   //testSetSignalsAtThroatToThroatTurnoutsWithDonePart7

    private void setupSetSignalsAtThroatToThroatTurnoutsWithDonePart7(JFrameOperator jFrameOperator) {
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart6(jFrameOperator);
        //fixes InfoMessage6 = Cannot set up logic because blocks have\nnot been defined around this item.
        layoutTurnout.setLayoutBlock(layoutBlocks.get(0));
        layoutTurnout2.setLayoutBlock(layoutBlocks.get(1));
        trackSegment.setLayoutBlock(layoutBlocks.get(2));
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testSetSignalsAtThroatToThroatTurnoutsWithDonePart8() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });
        //the JFrameOperator waits for the set signal frame to appear,
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SignalsAtTToTTurnout"));
        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone"));

        //fixes InfoMessage4 = Cannot set up logic because block "{0}"\ndoesn''t have an occupancy sensor.
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart8(jFrameOperator);

        //pressing "Done" should display a dialog
        //InfoMessage7 = Cannot set up logic because all connections\nhave not been defined around this item.
        Thread modalDialogOperatorThread8 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("MessageTitle"),
                Bundle.getMessage("InfoMessage7"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.push();
        JUnitUtil.waitFor(() -> {
            return !(modalDialogOperatorThread8.isAlive());
        }, "modalDialogOperatorThread8 finished");

        //three more times
        for (int idx = 0; idx < 3; idx++) {
            new EventTool().waitNoEvent(0);

            JemmyUtil.waitAndCloseDialog(Bundle.getMessage("MessageTitle"),
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("ButtonOK"));  // NOI18N
        }

        jFrameOperator.waitClosed();    //make sure the dialog closed
    }   //testSetSignalsAtThroatToThroatTurnoutsWithDonePart8

    private void setupSetSignalsAtThroatToThroatTurnoutsWithDonePart8(JFrameOperator jFrameOperator) {
        setupSetSignalsAtThroatToThroatTurnoutsWithDonePart7(jFrameOperator);
        //fixes InfoMessage4 = Cannot set up logic because block "{0}"\ndoesn''t have an occupancy sensor.
        //assign Occupancy Sensor to block
        layoutBlocks.get(2).setOccupancySensorName(sensors.get(2).getUserName());
    }

    @Test
    public void testSetSignalsAtThroatToThroatTurnoutsWithCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreadingUtil.runOnLayoutEventually(() -> {
            //this causes a "set Signal Heads at throat to throat Turnout" dialog to be (re)displayed.
            let.setSignalsAtThroatToThroatTurnouts(
                    getLayoutEditorToolBarPanel().signalIconEditor, layoutEditor.getTargetFrame());
        });

        JemmyUtil.waitAndCloseFrame(Bundle.getMessage("SignalsAtTToTTurnout"), Bundle.getMessage("ButtonCancel"));
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
        signalHeads.forEach((sh) -> {
            Assert.assertEquals("signal head for valid name", sh, let.getHeadFromName(sh.getSystemName()));
        });
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
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
    ///@Ignore("Fails on AppVeyor and Windows 12/20/2019")
    public void testSetSignalHeadOnPanelAtXYIntAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(signalHeads.get(1)));
        let.setSignalHeadOnPanel(0.D, "IH1", 0, 0);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new EventTool().waitNoEvent(100);

        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(signalHeads.get(1)));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new EventTool().waitNoEvent(100);

        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(signalHeads.get(1)));
    }

    @Test
    ///@Ignore("Fails on AppVeyor and Windows 12/20/2019")
    public void testSetSignalHeadOnPanelAtPointAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(signalHeads.get(1)));
        Point2D point = new Point2D.Double(150.0, 100.0);
        let.setSignalHeadOnPanel(0.D, "IH1", point);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new EventTool().waitNoEvent(100);

        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(signalHeads.get(1)));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new EventTool().waitNoEvent(100);

        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(signalHeads.get(1)));
    }

    @Test
    ///@Ignore("Fails on AppVeyor and Windows 12/20/2019")
    public void testSetSignalHeadOnPanelAtXYDoubleAndRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Signal head not on panel before set", let.isHeadOnPanel(signalHeads.get(1)));
        let.setSignalHeadOnPanel(0.D, "IH1", 0, 0);
        //setSignalHeadOnPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new EventTool().waitNoEvent(100);

        Assert.assertTrue("Signal head on panel after set", let.isHeadOnPanel(signalHeads.get(1)));
        let.removeSignalHeadFromPanel("IH1");
        //removeSignalHeadFromPanel performs some GUI actions, so give
        //the AWT queue some time to clear.
        new EventTool().waitNoEvent(100);

        Assert.assertFalse("Signal head not on panel after remove", let.isHeadOnPanel(signalHeads.get(1)));
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testGetSignalHeadIcon() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("let null", let);
        signalHeads.forEach((sh) -> {
            Assert.assertNotNull("Signal head icon for panel", let.getSignalHeadIcon(sh.getSystemName()));
        });
    }

    @Test
    public void testIsHeadOnPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        signalHeads.forEach((sh) -> {
            Assert.assertFalse("Signal head not on panel", let.isHeadOnPanel(sh));
        });
    }

    @Test
    public void testIsHeadAssignedAnywhere() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        signalHeads.forEach((sh) -> {
            Assert.assertFalse("Signal head not on panel", let.isHeadAssignedAnywhere(sh));
        });
    }

    @Test
    public void testRemoveSignalHeadAssignment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        //just verify this doesn't thrown an error.
        signalHeads.forEach((sh) -> {
            let.removeAssignment(sh);
        });
    }

    @Test
    ///@Ignore("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testInitializeBlockBossLogic() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
            @CheckForNull LayoutTrack c1, LayoutEditor.HitPointType t1,
            @CheckForNull LayoutTrack c2, LayoutEditor.HitPointType t2,
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
            layoutEditor.setLink(c1, t1, result, LayoutEditor.HitPointType.TRACK);
            layoutEditor.setLink(c2, t2, result, LayoutEditor.HitPointType.TRACK);
        }
        return result;
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
    @BeforeClass
    public static void setUpClass() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            //save the old string comparator
            stringComparator = Operator.getDefaultStringComparator();
            //set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            //restore the default string matching comparator
            Operator.setDefaultStringComparator(stringComparator);
        }
    }

    @Before
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
                InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout(toName);
            }
            turnouts = InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().stream().collect(Collectors.toList());

            for (int i = 0; i < 5; i++) {
                String sName = "IS" + i;
                String uName = "sensor " + i;
                InstanceManager.getDefault(SensorManager.class).provideSensor(sName).setUserName(uName);
            }
            sensors = InstanceManager.getDefault(SensorManager.class).getNamedBeanSet().stream().collect(Collectors.toList());

            for (int i = 0; i < 8; i++) {
                String sName = "IH" + i;
                String uName = "signal head " + i;
                VirtualSignalHead signalHead = new VirtualSignalHead(sName, uName);
                InstanceManager.getDefault(SignalHeadManager.class).register(signalHead);
            }
            signalHeads = InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().stream().collect(Collectors.toList());
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            layoutBlocks.stream().forEach(LayoutBlock::dispose);
            turnouts.stream().forEach(Turnout::dispose);
            signalHeads.stream().forEach(SignalHead::dispose);
            sensors.stream().forEach(Sensor::dispose);

            layoutBlocks = null;
            turnouts = null;
            signalHeads = null;
            sensors = null;

            EditorFrameOperator operator = new EditorFrameOperator(layoutEditor);
            operator.closeFrameWithConfirmations();
            JUnitUtil.dispose(layoutEditor);

            let = null;
            layoutEditor = null;
        }

        JUnitUtil.deregisterBlockManagerShutdownTask();

        InstanceManager.getDefault(ShutDownManager.class).deregister(InstanceManager.getDefault(BlockManager.class).shutDownTask);
        JUnitUtil.tearDown();
    }
//
    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorToolsTest.class);
}   //class LayoutEditorToolsTest
