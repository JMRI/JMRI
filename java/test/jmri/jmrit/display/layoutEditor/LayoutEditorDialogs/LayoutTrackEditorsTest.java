package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import javax.swing.*;
import jmri.*;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author George Warner Copyright (C) 2019
 */
public class LayoutTrackEditorsTest {

    @Rule   //10 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(10);

    @Rule  //allow 3 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(3);

    private static Operator.StringComparator stringComparator = null;

    private String closedString = Bundle.getMessage("BeanStateClosed");
    private String thrownString = Bundle.getMessage("BeanStateThrown");

    private LayoutEditor layoutEditor = null;
    private LayoutTrackEditors layoutTrackEditors = null;

    private Turnout turnout0 = null;
    private Turnout turnout1 = null;

    private LayoutTurnout doubleXoverLayoutTurnout = null;
    private LayoutTurnout rightHandLayoutTurnout = null;
    private LayoutSlip singleLayoutSlip = null;
    private LayoutSlip doubleLayoutSlip = null;
    private LevelXing levelXing = null;
    private TrackSegment trackSegment = null;
    private LayoutTurntable layoutTurntable = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", layoutTrackEditors);
    }

    @Test
    public void testHasNxSensorPairsNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("null block NxSensorPairs", layoutTrackEditors.hasNxSensorPairs(null));
    }

    @Test
    public void testHasNxSensorPairsDisconnectedBlock() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutBlock b = new LayoutBlock("test", "test");
        Assert.assertFalse("disconnected block NxSensorPairs", layoutTrackEditors.hasNxSensorPairs(b));
    }

    @Test
    public void testShowSensorMessage() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        layoutTrackEditors.sensorList.add("Test");
        Assert.assertFalse(layoutTrackEditors.sensorList.isEmpty());
        layoutTrackEditors.showSensorMessage();
    }

    @Test
    public void testEditTrackSegmentDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        trackSegment.setArc(true);
        trackSegment.setCircle(true);
        createBlocks();

        //Edit the track trackSegment
        layoutTrackEditors.editLayoutTrack(trackSegment);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        //Select dashed
        JLabelOperator styleLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("Style")));
        JComboBoxOperator styleComboBoxOperator = new JComboBoxOperator(
                (JComboBox) styleLabelOperator.getLabelFor());
        styleComboBoxOperator.selectItem(Bundle.getMessage("Dashed"));

        //Select mainline
        JComboBoxOperator mainlineComboboxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser(Bundle.getMessage("Mainline")));
        mainlineComboboxOperator.selectItem(Bundle.getMessage("Mainline"));

        //Enable Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideTrack")).doClick();

        //Select block
        JLabelOperator blockNameLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BlockID"));
        JComboBoxOperator blockComboBoxOperator = new JComboBoxOperator(
                (JComboBox) blockNameLabelOperator.getLabelFor());
        blockComboBoxOperator.getTextField().setText("Blk 2");

        //Set arc angle
        JLabelOperator setArcAngleLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("SetArcAngle"));
        JTextFieldOperator jtxt = new JTextFieldOperator(
                (JTextField) setArcAngleLabelOperator.getLabelFor());
        jtxt.setText("35");

        //Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();

        //TODO: frame (dialog) titles hard coded here...
        //it should be based on Bundle.getMessage("EditBean", "Block", "Blk 2"));
        //but that isn't working...
        JFrameOperator blkFO = new JFrameOperator("Edit Block Blk 2");
        new JButtonOperator(blkFO, Bundle.getMessage("ButtonOK")).doClick();

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTrackSegmentCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        trackSegment.setArc(false);
        trackSegment.setCircle(false);

        //Edit the track trackSegment
        layoutTrackEditors.editLayoutTrack(trackSegment);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        //Create empty block edit dialog
        Thread segmentBlockError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(segmentBlockError.isAlive());
        }, "segmentBlockError finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTrackSegmentClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the track trackSegment
        layoutTrackEditors.editLayoutTrack(trackSegment);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTrackSegmentError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        trackSegment.setArc(true);
        trackSegment.setCircle(true);

        //Edit the track trackSegment
        layoutTrackEditors.editLayoutTrack(trackSegment);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        //Set arc angle
        JTextFieldOperator jtxt = new JTextFieldOperator(jFrameOperator, 1);
        jtxt.setText("abc");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTurnoutDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();

        //Edit the double crossover
        layoutTrackEditors.editLayoutTrack(doubleXoverLayoutTurnout);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXover"));

        //Select main turnout
        JLabelOperator mainTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator mainTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) mainTurnoutLabelOperator.getLabelFor());
        mainTurnoutComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index
        Assert.assertEquals("main selected turnout name",
                turnout0.getSystemName(),
                mainTurnoutComboBoxOperator.getSelectedItem().toString());

        //Enable second turnout and select it
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SupportingTurnout")).doClick();

        JLabelOperator supportingTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("Supporting", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator supportingTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) supportingTurnoutLabelOperator.getLabelFor());
        supportingTurnoutComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index
        Assert.assertEquals("supporting selected turnout name",
                turnout1.getSystemName(),
                supportingTurnoutComboBoxOperator.getSelectedItem().toString());

        //Enable Invert and Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SecondTurnoutInvert")).doClick();
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideXover")).doClick();

        //Ener new names for each block position
        JTextFieldOperator blockATextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockNameHint")));
        blockATextFieldOperator.setText("DX Blk A");
        JButtonOperator editBlockButtonOperator = new JButtonOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockHint", "")));
        editBlockButtonOperator.doClick();

        //TODO: frame (dialog) titles hard coded here...
        //should be: new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk A"));
        //but that's not working...
        JFrameOperator blkFOa = new JFrameOperator("Edit Block DX Blk A");
        new JButtonOperator(blkFOa, Bundle.getMessage("ButtonOK")).doClick();

        JTextFieldOperator blockBTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockBNameHint")));
        blockBTextFieldOperator.setText("DX Blk B");

        editBlockButtonOperator = new JButtonOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockHint", "2")));
        editBlockButtonOperator.doClick();

        //TODO: frame (dialog) titles hard coded here...
        //should be: new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk B"));
        JFrameOperator blkFOb = new JFrameOperator("Edit Block DX Blk B");
        new JButtonOperator(blkFOb, Bundle.getMessage("ButtonOK")).doClick();

        JTextFieldOperator blockCTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockCNameHint")));
        blockCTextFieldOperator.setText("DX Blk C");
        editBlockButtonOperator = new JButtonOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockHint", "3")));
        editBlockButtonOperator.doClick();

        //TODO: frame (dialog) titles hard coded here...
        //should be: new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk C"));
        JFrameOperator blkFOc = new JFrameOperator("Edit Block DX Blk C");
        new JButtonOperator(blkFOc, Bundle.getMessage("ButtonOK")).doClick();

        JTextFieldOperator blockDTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockDNameHint")));
        blockDTextFieldOperator.setText("DX Blk D");
        editBlockButtonOperator = new JButtonOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockHint", "4")));
        editBlockButtonOperator.doClick();

        //TODO: frame (dialog) titles hard coded here...
        //should be: new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk D"));
        JFrameOperator blkFOd = new JFrameOperator("Edit Block DX Blk D");
        new JButtonOperator(blkFOd, Bundle.getMessage("ButtonOK")).doClick();

        /* The previous block editor sections create new layout blocks so
           the following force tests of the normal create process handled by done. */
        blockATextFieldOperator.setText("DX New A");
        blockBTextFieldOperator.setText("DX New B");
        blockCTextFieldOperator.setText("DX New C");
        blockDTextFieldOperator.setText("DX New D");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditRHTurnoutDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();
        createBlocks();

        //Edit the rh turnout
        layoutTrackEditors.editLayoutTrack(rightHandLayoutTurnout);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurnout"));

        //Select main turnout
        JLabelOperator mainTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator mainTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) mainTurnoutLabelOperator.getLabelFor());
        mainTurnoutComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index
        Assert.assertEquals("main selected turnout name",
                turnout0.getSystemName(),
                mainTurnoutComboBoxOperator.getSelectedItem().toString());

        //Enable second turnout and select it
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("ThrowTwoTurnouts")).doClick();

        JLabelOperator supportingTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("Supporting", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator supportingTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) supportingTurnoutLabelOperator.getLabelFor());
        supportingTurnoutComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index
        Assert.assertEquals("supporting selected turnout name",
                turnout1.getSystemName(),
                supportingTurnoutComboBoxOperator.getSelectedItem().toString());

        //Enable Invert and Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SecondTurnoutInvert")).doClick();
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideTurnout")).doClick();

        //Continuing route option
        JLabelOperator continuingTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("ContinuingState"));
        JComboBoxOperator continuingTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) continuingTurnoutLabelOperator.getLabelFor());
        continuingTurnoutComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index
        Assert.assertEquals("continuing selected turnout state",
                thrownString,
                continuingTurnoutComboBoxOperator.getSelectedItem().toString());

        //put a new block name in the block combobox's textfield
        JTextFieldOperator blockTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockNameHint")));
        blockTextFieldOperator.setText("QRS Block");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTurnoutCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the double crossover
        layoutTrackEditors.editLayoutTrack(doubleXoverLayoutTurnout);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXover"));

        //Invoke layout block editor with no block assigned
        Thread turnoutBlockAError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 0).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockAError.isAlive());
        }, "turnoutBlockAError finished");

        Thread turnoutBlockBError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 1).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockBError.isAlive());
        }, "turnoutBlockBError finished");

        Thread turnoutBlockCError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 2).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockCError.isAlive());
        }, "turnoutBlockCError finished");

        Thread turnoutBlockDError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 3).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockDError.isAlive());
        }, "turnoutBlockDError finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTurnoutClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the double crossover
        layoutTrackEditors.editLayoutTrack(doubleXoverLayoutTurnout);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXover"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditDoubleSlipDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();
        createBlocks();

        //Edit the double Slip
        layoutTrackEditors.editLayoutTrack(doubleLayoutSlip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        //Select turnout A
        JLabelOperator firstTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BeanNameTurnout") + " A");
        JComboBoxOperator firstTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) firstTurnoutLabelOperator.getLabelFor());
        firstTurnoutComboBoxOperator.selectItem(1); //TODO: fix hardcoded index
        Assert.assertEquals("first selected turnout name",
                turnout0.getSystemName(),
                firstTurnoutComboBoxOperator.getSelectedItem().toString());

        //Select turnout B
        JLabelOperator secondTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BeanNameTurnout") + " B");
        JComboBoxOperator secondTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) secondTurnoutLabelOperator.getLabelFor());
        secondTurnoutComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index
        Assert.assertEquals("second selected turnout name",
                turnout1.getSystemName(),
                secondTurnoutComboBoxOperator.getSelectedItem().toString());

        //Create a (new) block
        JTextFieldOperator blockTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockNameHint")));
        blockTextFieldOperator.setText("Slip Block");

        //Enable Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideSlip")).doClick();

        //click Test button four times
        JButtonOperator testButtonOperator = new JButtonOperator(jFrameOperator, "Test");
        testButtonOperator.doClick();
        testButtonOperator.doClick();
        testButtonOperator.doClick();
        testButtonOperator.doClick();

        //Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();

        //Close the block editor dialog
        //TODO: frame (dialog) title hard coded here...
        //it should be based on Bundle.getMessage("EditBean", "Block", "Slip Block"));
        //but that isn't working...
        JFrameOperator blkFO = new JFrameOperator("Edit Block Slip Block");
        new JButtonOperator(blkFO, Bundle.getMessage("ButtonOK")).doClick();

        /* The previous block editor sections create new layout blocks so
           the following force tests of the normal create process handled by done. */
        blockTextFieldOperator.setText("New Slip Block");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }   //testEditDoubleSlipDone()

    @Test
    public void testEditSingleSlipDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();
        createBlocks();

        //Edit the single Slip
        layoutTrackEditors.editLayoutTrack(singleLayoutSlip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        //Select turnout A
        JLabelOperator firstTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BeanNameTurnout") + " A");
        JComboBoxOperator firstTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) firstTurnoutLabelOperator.getLabelFor());
        firstTurnoutComboBoxOperator.selectItem(1); //TODO: fix hardcoded index
        Assert.assertEquals("first selected turnout name",
                turnout0.getSystemName(),
                firstTurnoutComboBoxOperator.getSelectedItem().toString());

        //Select turnout B
        JLabelOperator secondTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BeanNameTurnout") + " B");
        JComboBoxOperator secondTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) secondTurnoutLabelOperator.getLabelFor());
        secondTurnoutComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index
        Assert.assertEquals("second selected turnout name",
                turnout1.getSystemName(),
                secondTurnoutComboBoxOperator.getSelectedItem().toString());

        //Create a (new) block
        JTextFieldOperator blockTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new JemmyUtil.ToolTipComponentChooser(Bundle.getMessage("EditBlockNameHint")));
        blockTextFieldOperator.setText("Slip Block");

        //Enable Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideSlip")).doClick();

        //click Test button three times
        JButtonOperator testButtonOperator = new JButtonOperator(jFrameOperator, "Test");
        testButtonOperator.doClick();
        testButtonOperator.doClick();
        testButtonOperator.doClick();

        //Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();

        //Close the block editor dialog
        //TODO: frame (dialog) title hard coded here...
        //it should be based on Bundle.getMessage("EditBean", "Block", "Slip Block"));
        //but that isn't working...
        JFrameOperator blkFO = new JFrameOperator("Edit Block Slip Block");
        new JButtonOperator(blkFO, Bundle.getMessage("ButtonOK")).doClick();

        /* The previous block editor sections create new layout blocks so
           the following force tests of the normal create process handled by done. */
        blockTextFieldOperator.setText("New Slip Block");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditSlipCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the double doubleLayoutSlip
        layoutTrackEditors.editLayoutTrack(doubleLayoutSlip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        //Invoke layout block editor with no block assigned
        Thread slipBlockError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(slipBlockError.isAlive());
        }, "slipBlockError finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditSlipClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the double doubleLayoutSlip
        layoutTrackEditors.editLayoutTrack(doubleLayoutSlip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditXingDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createBlocks();

        //Edit the level crossing
        layoutTrackEditors.editLayoutTrack(levelXing);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXing"));

        //Select AC block
        JLabelOperator acBlockLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("Block_ID", "AC"));
        JComboBoxOperator acBlockComboBoxOperator = new JComboBoxOperator(
                (JComboBox) acBlockLabelOperator.getLabelFor());
        acBlockComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index
        Assert.assertEquals("ac selected block name",
                "IB1",
                acBlockComboBoxOperator.getSelectedItem().toString());

        //Select BD block
        JLabelOperator bdBlockLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("Block_ID", "BD"));
        JComboBoxOperator bdBlockComboBoxOperator = new JComboBoxOperator(
                (JComboBox) bdBlockLabelOperator.getLabelFor());
        bdBlockComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index
        Assert.assertEquals("BD selected block name",
                "IB2",
                bdBlockComboBoxOperator.getSelectedItem().toString());

        //Enable Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideCrossing")).doClick();

        //Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "AC")).doClick();

        //find and dismiss the "Edit Block Xing Blk AC" dialog
        //TODO: frame (dialog) titles hard coded here...
        //it should be based on Bundle.getMessage("EditBean", "Block", "Blk 1"));
        //but that isn't working...
        JFrameOperator blkFOac = new JFrameOperator("Edit Block Blk 1");
        new JButtonOperator(blkFOac, Bundle.getMessage("ButtonOK")).doClick();

        //Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "BD")).doClick();

        //find and dismiss the "Edit Block Xing Blk BD" dialog
        //TODO: frame (dialog) titles hard coded here...
        //it should be based on Bundle.getMessage("EditBean", "Block", "Blk 2"));
        //but that isn't working...
        JFrameOperator blkFObd = new JFrameOperator("Edit Block Blk 2");
        new JButtonOperator(blkFObd, Bundle.getMessage("ButtonOK")).doClick();

        /* The previous block editor sections create new layout blocks so
           the following force tests of the normal create process handled by done. */
        acBlockComboBoxOperator.getTextField().setText("Xing New AC");
        bdBlockComboBoxOperator.getTextField().setText("Xing New BD");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditXingCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the level crossing
        layoutTrackEditors.editLayoutTrack(levelXing);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXing"));

        //Invoke layout block editor with no block assigned
        Thread xingBlock1Error = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "AC")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(xingBlock1Error.isAlive());
        }, "xingBlock1Error finished");

        Thread xingBlock2Error = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "BD")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(xingBlock2Error.isAlive());
        }, "xingBlock2Error finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditXingClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the level crossing
        layoutTrackEditors.editLayoutTrack(levelXing);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXing"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    ///@Ignore("giving up after X failures")
    public void testEditTurntableDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();

        //Edit the layoutTurntable
        layoutTrackEditors.editLayoutTrack(layoutTurntable);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        //Set good radius
        JLabelOperator jLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("TurntableRadius"));
        JTextFieldOperator jtxt = new JTextFieldOperator(
                (JTextField) jLabelOperator.getLabelFor());
        jtxt.setText("30");

        //Add 5 rays
        JButtonOperator addRayTrackJButtonOperator = new JButtonOperator(
                jFrameOperator, Bundle.getMessage("AddRayTrack"));
        jLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("RayAngle"));
        jtxt = new JTextFieldOperator(
                (JTextField) jLabelOperator.getLabelFor());

        addRayTrackJButtonOperator.doClick();
        jtxt.setText("90");
        addRayTrackJButtonOperator.doClick();
        jtxt.setText("180");
        addRayTrackJButtonOperator.doClick();
        jtxt.setText("270");
        addRayTrackJButtonOperator.doClick();
        jtxt.setText("315");
        addRayTrackJButtonOperator.doClick();

        //Delete the 5th ray
        Thread deleteRay = JemmyUtil.createModalDialogOperatorThread(
                "Warning", "Yes");  //NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDelete"), 4).doClick();
        JUnitUtil.waitFor(() -> {
            return !(deleteRay.isAlive());
        }, "deleteRay finished");

        //Enable DCC control
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("TurntableDCCControlled")).doClick();

        //Change the first ray to 30 degrees
        JLabelOperator rayAngleLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("MakeLabel", Bundle.getMessage("RayAngle")), 0);
        jtxt = new JTextFieldOperator((JTextField) rayAngleLabelOperator.getLabelFor());
        jtxt.setText("30");

        //Set ray turnouts
        //TODO: fix hardcoded index
        JComboBoxOperator turnout_cbo = new JComboBoxOperator(jFrameOperator, 0);
        JComboBoxOperator state_cbo = new JComboBoxOperator(jFrameOperator, 1);

        turnout_cbo.selectItem(1); //TODO: fix hardcoded index
        Assert.assertEquals("turnout name",
                turnout0.getSystemName(),
                turnout_cbo.getSelectedItem().toString());
        state_cbo.selectItem(closedString);
        Assert.assertEquals("turnout state",
                closedString,
                state_cbo.getSelectedItem().toString());

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 2);
        state_cbo = new JComboBoxOperator(jFrameOperator, 3);
        turnout_cbo.selectItem(1); //TODO: fix hardcoded index
        Assert.assertEquals("turnout name",
                turnout0.getSystemName(),
                turnout_cbo.getSelectedItem().toString());
        state_cbo.selectItem(thrownString);
        Assert.assertEquals("turnout state",
                thrownString,
                state_cbo.getSelectedItem().toString());

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 4);
        state_cbo = new JComboBoxOperator(jFrameOperator, 5);
        turnout_cbo.selectItem(2); //TODO: fix hardcoded index
        Assert.assertEquals("turnout name",
                turnout1.getSystemName(),
                turnout_cbo.getSelectedItem().toString());
        state_cbo.selectItem(closedString);
        Assert.assertEquals("turnout state",
                closedString,
                state_cbo.getSelectedItem().toString());

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 6);
        state_cbo = new JComboBoxOperator(jFrameOperator, 7);
        turnout_cbo.selectItem(2); //TODO: fix hardcoded index
        Assert.assertEquals("turnout name",
                turnout1.getSystemName(),
                turnout_cbo.getSelectedItem().toString());
        state_cbo.selectItem(thrownString);
        Assert.assertEquals("turnout state",
                thrownString,
                state_cbo.getSelectedItem().toString());

        //Add a valid ray and then change the angle to an invalid value
        jtxt = new JTextFieldOperator(jFrameOperator, 2);
        jtxt.clickMouse();
        jtxt.setText("qqq");

        //Move focus
        Thread badRayAngleModalDialogOperatorThread = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N
        jtxt = new JTextFieldOperator(jFrameOperator, 3);
        jtxt.clickMouse();
        JUnitUtil.waitFor(() -> {
            return !(badRayAngleModalDialogOperatorThread.isAlive());
        }, "badRayAngle finished");

        //Put a good value back in
        jtxt = new JTextFieldOperator(jFrameOperator, 2);
        jtxt.setText("30");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTurntableCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the Turntable
        layoutTrackEditors.editLayoutTrack(layoutTurntable);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTurntableClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the Turntable
        layoutTrackEditors.editLayoutTrack(layoutTurntable);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    @Test
    public void testEditTurntableErrors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        //Edit the layoutTurntable
        layoutTrackEditors.editLayoutTrack(layoutTurntable);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        //Ray angle
        JLabelOperator jLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("RayAngle"));
        JTextFieldOperator jtxt = new JTextFieldOperator(
                (JTextField) jLabelOperator.getLabelFor());
        jtxt.setText("xyz");

        Thread badAngle = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N

        JButtonOperator addRayTrackJButtonOperator = new JButtonOperator(
                jFrameOperator, Bundle.getMessage("AddRayTrack"));
        addRayTrackJButtonOperator.doClick();

        JUnitUtil.waitFor(() -> {
            return !(badAngle.isAlive());
        }, "badAngle finished");

        //Set radius
        jLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("TurntableRadius"));
        jtxt = new JTextFieldOperator(
                (JTextField) jLabelOperator.getLabelFor());
        jtxt.setText("abc");

        Thread badRadius = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  //NOI18N

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();

        JUnitUtil.waitFor(() -> {
            return !(badRadius.isAlive());
        }, "badRadius finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    //make sure the dialog actually closed
    }

    public void createTurnouts() {
        turnout0 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT101");
        turnout0.setUserName("Turnout 101");
        turnout0.setCommandedState(Turnout.CLOSED);

        turnout1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT102");
        turnout1.setUserName("Turnout 102");
        turnout1.setCommandedState(Turnout.CLOSED);
    }

    public void createBlocks() {
        Block block1 = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        block1.setUserName("Blk 1");
        Block block2 = InstanceManager.getDefault(BlockManager.class).provideBlock("IB2");
        block2.setUserName("Blk 2");
    }

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
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetInstanceManager();
            JUnitUtil.initInternalTurnoutManager();
            JUnitUtil.initInternalSensorManager();
            InstanceManager.setDefault(BlockManager.class, new BlockManager());

            layoutEditor = new LayoutEditor();
            layoutEditor.setVisible(true);
            layoutTrackEditors = layoutEditor.getLayoutTrackEditors();

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 10.0);

            //Track Segment
            PositionablePoint pp1 = new PositionablePoint("a", PositionablePoint.ANCHOR, point, layoutEditor);
            point = MathUtil.add(point, delta);
            PositionablePoint pp2 = new PositionablePoint("b", PositionablePoint.ANCHOR, point, layoutEditor);
            trackSegment = new TrackSegment("Segment", pp1, LayoutTrack.POS_POINT, pp2, LayoutTrack.POS_POINT, false, false, layoutEditor);

            //RH Turnout
            point = MathUtil.add(point, delta);
            rightHandLayoutTurnout = new LayoutTurnout("RH Turnout",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, layoutEditor);

            //Double crossover
            doubleXoverLayoutTurnout = new LayoutTurnout("Double Xover",
                    LayoutTurnout.DOUBLE_XOVER, point, 33.0, 1.1, 1.2, layoutEditor);

            //Single doubleLayoutSlip
            point = MathUtil.add(point, delta);
            singleLayoutSlip = new LayoutSlip("Single Slip",
                    point, 0.0, layoutEditor, LayoutTurnout.SINGLE_SLIP);

            //Double doubleLayoutSlip
            point = MathUtil.add(point, delta);
            doubleLayoutSlip = new LayoutSlip("Double Slip",
                    point, 0.0, layoutEditor, LayoutTurnout.DOUBLE_SLIP);

            //Level crossing
            point = MathUtil.add(point, delta);
            levelXing = new LevelXing("Level Xing",
                    point, layoutEditor);

            //Turntable
            point = MathUtil.add(point, delta);
            layoutTurntable = new LayoutTurntable("Turntable",
                    point, layoutEditor);
        }
    }

    @After
    public void tearDown() {
        if (doubleXoverLayoutTurnout != null) {
            doubleXoverLayoutTurnout.remove();
            doubleXoverLayoutTurnout.dispose();
        }

        if (rightHandLayoutTurnout != null) {
            rightHandLayoutTurnout.remove();
            rightHandLayoutTurnout.dispose();
        }

        if (doubleLayoutSlip != null) {
            doubleLayoutSlip.remove();
            doubleLayoutSlip.dispose();
        }

        if (singleLayoutSlip != null) {
            singleLayoutSlip.remove();
            singleLayoutSlip.dispose();
        }

        if (levelXing != null) {
            levelXing.remove();
            levelXing.dispose();
        }

        if (trackSegment != null) {
            trackSegment.remove();
            trackSegment.dispose();
        }

        if (layoutTurntable != null) {
            layoutTurntable.remove();
            layoutTurntable.dispose();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }
        doubleXoverLayoutTurnout = null;
        rightHandLayoutTurnout = null;
        doubleLayoutSlip = null;
        singleLayoutSlip = null;
        levelXing = null;
        trackSegment = null;
        layoutTurntable = null;
        layoutEditor = null;
        layoutTrackEditors = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditorsTest.class);
}
