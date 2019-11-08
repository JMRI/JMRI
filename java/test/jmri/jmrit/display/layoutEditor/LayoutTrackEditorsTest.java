package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import javax.swing.JComboBox;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutTrackEditorsTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule    // allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(0); // allow 2 retries

    private LayoutEditor layoutEditor = null;

    private Turnout to0 = null;
    private Turnout to1 = null;

    private LayoutTurnout dxo = null;
    private LayoutTurnout rhto = null;
    private LayoutSlip slip = null;
    private LayoutSlip sslip = null;
    private LevelXing xing = null;
    private TrackSegment segment = null;
    private LayoutTurntable turntable = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditors t = new LayoutTrackEditors(layoutEditor);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testHasNxSensorPairsNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditors t = new LayoutTrackEditors(layoutEditor);
        Assert.assertFalse("null block NxSensorPairs", t.hasNxSensorPairs(null));
    }

    @Test
    public void testHasNxSensorPairsDisconnectedBlock() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditors t = new LayoutTrackEditors(layoutEditor);
        LayoutBlock b = new LayoutBlock("test", "test");
        Assert.assertFalse("disconnected block NxSensorPairs", t.hasNxSensorPairs(b));
    }

    @Test
    public void testShowSensorMessage() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditors t = new LayoutTrackEditors(layoutEditor);
        t.sensorList.add("Test");
        Assert.assertFalse(t.sensorList.isEmpty());
        t.showSensorMessage();
    }

    @Test
    public void testEditTrackSegmentDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        segment.setArc(true);
        segment.setCircle(true);
        createBlocks();

        // Edit the track segment
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(segment);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        // Select dashed
        JLabelOperator styleLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("Style")));
        JComboBoxOperator styleComboBoxOperator = new JComboBoxOperator(
                (JComboBox) styleLabelOperator.getLabelFor());
        styleComboBoxOperator.selectItem(Bundle.getMessage("Dashed"));

        // Select mainline
        JComboBoxOperator mainlineComboboxOperator = new JComboBoxOperator(
                jFrameOperator, new NameComponentChooser(Bundle.getMessage("Mainline")));
        mainlineComboboxOperator.selectItem(Bundle.getMessage("Mainline"));

        // Enable Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideTrack")).doClick();

        // Select block
        JLabelOperator blockNameLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BlockID"));
        JComboBoxOperator blockComboBoxOperator = new JComboBoxOperator(
                (JComboBox) blockNameLabelOperator.getLabelFor());
        blockComboBoxOperator.selectItem(2); //TODO: fix hardcoded index
        // Use editable combo instead of select
        JTextFieldOperator jblktxt = blockComboBoxOperator.getTextField();
        jblktxt.setText("XYZ Block");

        // Set arc angle
        JTextFieldOperator jtxt = new JTextFieldOperator(jFrameOperator, 1);
        jtxt.setText("35");

        // Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();
        //TODO: frame (dialog) titles hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "DX Blk A"));
        // but that isn't working...
        JFrameOperator blkFO = new JFrameOperator("Edit Block Blk 2");
        new JButtonOperator(blkFO, Bundle.getMessage("ButtonOK")).doClick();

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditTrackSegmentCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        segment.setArc(false);
        segment.setCircle(false);

        // Edit the track segment
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(segment);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        // Create empty block edit dialog
        Thread segmentBlockError = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "segmentBlockError");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(segmentBlockError.isAlive());
        }, "segmentBlockError finished");

        new JButtonOperator(jFrameOperator, "Cancel").doClick();
    }

    @Test
    public void testEditTrackSegmentClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the track segment
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(segment);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        jFrameOperator.close();
    }

    @Test
    public void testEditTrackSegmentError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        segment.setArc(true);
        segment.setCircle(true);

        // Edit the track segment
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(segment);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTrackSegment"));

        // Set arc angle
        JTextFieldOperator jtxt = new JTextFieldOperator(jFrameOperator, 1);
        jtxt.setText("abc");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditTurnoutDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createTurnouts();

        // Edit the double crossover
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(dxo);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXover"));

        // Select main turnout
        JLabelOperator mainTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator mainTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) mainTurnoutLabelOperator.getLabelFor());
        mainTurnoutComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        // Enable second turnout and select it
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SupportingTurnout")).doClick();

        JLabelOperator supportingTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("Supporting", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator supportingTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) supportingTurnoutLabelOperator.getLabelFor());
        supportingTurnoutComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index

        // Enable Invert and Hide
        new JCheckBoxOperator(jFrameOperator, 1).doClick();
        new JCheckBoxOperator(jFrameOperator, 2).doClick();

        // Ener new names for each block position
        //TODO: fix hardcoded index
        JTextFieldOperator jblktxt = new JTextFieldOperator(jFrameOperator, 0);
        jblktxt.setText("DX Blk A");
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 0).doClick();
        //TODO: frame (dialog) titles hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "DX Blk A"));
        // but that isn't working...
        //JFrameOperator blkFOa = new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk A"));
        JFrameOperator blkFOa = new JFrameOperator("Edit Block DX Blk A");
        new JButtonOperator(blkFOa, Bundle.getMessage("ButtonOK")).doClick();

        jblktxt = new JTextFieldOperator(jFrameOperator, 1);
        jblktxt.setText("DX Blk B");
        //JFrameOperator blkFOa = new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk B"));
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 1).doClick();
        //TODO: frame (dialog) titles hard coded here...
        JFrameOperator blkFOb = new JFrameOperator("Edit Block DX Blk B");
        new JButtonOperator(blkFOb, Bundle.getMessage("ButtonOK")).doClick();

        jblktxt = new JTextFieldOperator(jFrameOperator, 2);
        jblktxt.setText("DX Blk C");
        //JFrameOperator blkFOa = new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk C"));
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 2).doClick();
        //TODO: frame (dialog) titles hard coded here...
        JFrameOperator blkFOc = new JFrameOperator("Edit Block DX Blk C");
        new JButtonOperator(blkFOc, Bundle.getMessage("ButtonOK")).doClick();

        jblktxt = new JTextFieldOperator(jFrameOperator, 3);
        jblktxt.setText("DX Blk D");
        //JFrameOperator blkFOa = new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk D"));
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 3).doClick();
        //TODO: frame (dialog) titles hard coded here...
        JFrameOperator blkFOd = new JFrameOperator("Edit Block DX Blk D");
        new JButtonOperator(blkFOd, Bundle.getMessage("ButtonOK")).doClick();

        /* The previous block editor sections create new layout blocks so
           the following force tests of the normal create process handled by done. */
        jblktxt = new JTextFieldOperator(jFrameOperator, 1);
        jblktxt.setText("DX New B");

        jblktxt = new JTextFieldOperator(jFrameOperator, 2);
        jblktxt.setText("DX New C");

        jblktxt = new JTextFieldOperator(jFrameOperator, 3);
        jblktxt.setText("DX New D");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditRHTurnoutDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createTurnouts();
        createBlocks();

        // Edit the rh turnout
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(rhto);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurnout"));

        // Select main turnout
        //TODO: fix hardcoded index
        JComboBoxOperator tcbo = new JComboBoxOperator(jFrameOperator, 0);
        tcbo.selectItem(1); //TODO: fix hardcoded index

        // Enable second turnout and select it
        //TODO: fix hardcoded index
        new JCheckBoxOperator(jFrameOperator, 0).doClick();
        JComboBoxOperator tcbo2 = new JComboBoxOperator(jFrameOperator, 1);
        tcbo2.selectItem(2); //TODO: fix hardcoded index

        // Enable Invert and Hide
        new JCheckBoxOperator(jFrameOperator, 1).doClick();
        new JCheckBoxOperator(jFrameOperator, 2).doClick();

        // Continuing route option
        JComboBoxOperator contOption = new JComboBoxOperator(jFrameOperator, 2);
        contOption.selectItem(1); //TODO: fix hardcoded index

        // Use editable combo instead of select
        //TODO: fix hardcoded index
        JTextFieldOperator jblktxt = new JTextFieldOperator(jFrameOperator, 0);
        jblktxt.setText("QRS Block");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditTurnoutCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the double crossover
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(dxo);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXover"));

        // Invoke layout block editor with no block assigned
        Thread turnoutBlockAError = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "turnoutBlockAError");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit", "0")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockAError.isAlive());
        }, "turnoutBlockAError finished");

        Thread turnoutBlockBError = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "turnoutBlockBError");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit", "1")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockBError.isAlive());
        }, "turnoutBlockBError finished");

        Thread turnoutBlockCError = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "turnoutBlockCError");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit", "2")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockCError.isAlive());
        }, "turnoutBlockCError finished");

        Thread turnoutBlockDError = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "turnoutBlockDError");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit", "3")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockDError.isAlive());
        }, "turnoutBlockDError finished");

        new JButtonOperator(jFrameOperator, "Cancel").doClick();
    }

    @Test
    public void testEditTurnoutClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the double crossover
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(dxo);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXover"));

        jFrameOperator.close();
    }

    @Test
    public void testEditSlipDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createTurnouts();
        createBlocks();

        // Edit the double slip
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(slip);

        // Select turnout A
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        //TODO: fix hardcoded index
        JComboBoxOperator tcbA = new JComboBoxOperator(jFrameOperator, 0);
        tcbA.selectItem(1); //TODO: fix hardcoded index

        // Select turnout B
        //TODO: fix hardcoded index
        JComboBoxOperator tcbB = new JComboBoxOperator(jFrameOperator, 1);
        tcbB.selectItem(2); //TODO: fix hardcoded index

        // Create a block
        //TODO: fix hardcoded index
        JTextFieldOperator jblktxt = new JTextFieldOperator(jFrameOperator, 0);
        jblktxt.setText("Slip Block");

        // Enable Hide
        //TODO: fix hardcoded index
        new JCheckBoxOperator(jFrameOperator, 0).doClick();

        // Trigger Test button
        new JButtonOperator(jFrameOperator, "Test").doClick();
        new JButtonOperator(jFrameOperator, "Test").doClick();
        new JButtonOperator(jFrameOperator, "Test").doClick();
        new JButtonOperator(jFrameOperator, "Test").doClick();

        // Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();

        // Close the block editor dialog
        //TODO: frame (dialog) title hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "Slip Block"));
        // but that isn't working...
        JFrameOperator blkFO = new JFrameOperator("Edit Block Slip Block");
        new JButtonOperator(blkFO, Bundle.getMessage("ButtonOK")).doClick();

        /* The previous block editor sections create new layout blocks so
           the following force tests of the normal create process handled by done. */
        //TODO: fix hardcoded index
        jblktxt = new JTextFieldOperator(jFrameOperator, 0);
        jblktxt.setText("New Slip Block");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }   // testEditSlipDone()

    @Test
    public void testEditSingleSlipDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createTurnouts();
        createBlocks();

        // Edit the single slip
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(sslip);

        // Select turnout A
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        //TODO: fix hardcoded index
        JComboBoxOperator tcbA = new JComboBoxOperator(jFrameOperator, 0);
        tcbA.selectItem(1); //TODO: fix hardcoded index

        // Select turnout B
        JComboBoxOperator tcbB = new JComboBoxOperator(jFrameOperator, 1);
        tcbB.selectItem(2); //TODO: fix hardcoded index

        // Select a block
        JComboBoxOperator blk_cbo_slip = new JComboBoxOperator(jFrameOperator, 8);
        blk_cbo_slip.selectItem(1); //TODO: fix hardcoded index

        // Enable Hide
        //TODO: fix hardcoded index
        new JCheckBoxOperator(jFrameOperator, 0).doClick();

        // Trigger Test button
        new JButtonOperator(jFrameOperator, "Test").doClick();
        new JButtonOperator(jFrameOperator, "Test").doClick();
        new JButtonOperator(jFrameOperator, "Test").doClick();

        // Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();

        //TODO: frame (dialog) titles hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "DX Blk A"));
        // but that isn't working...
        JFrameOperator blkFO = new JFrameOperator("Edit Block Blk 1");
        new JButtonOperator(blkFO, Bundle.getMessage("ButtonOK")).doClick();

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditSlipCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the double slip
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(slip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        // Invoke layout block editor with no block assigned
        Thread slipBlockError = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "slipBlockError");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(slipBlockError.isAlive());
        }, "slipBlockError finished");

        new JButtonOperator(jFrameOperator, "Cancel").doClick();
    }

    @Test
    public void testEditSlipClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the double slip
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(slip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        jFrameOperator.close();
    }

    @Test
    public void testEditXingDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createBlocks();

        // Edit the level crossing
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(xing);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXing"));

        // Select AC block
        JLabelOperator acBlockLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("Block_ID", "AC"));
        JComboBoxOperator acBlockComboBoxOperator = new JComboBoxOperator(
                (JComboBox) acBlockLabelOperator.getLabelFor());
        acBlockComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        // Select BD block
        JLabelOperator bdBlockLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("Block_ID", "BD"));
        JComboBoxOperator bdBlockComboBoxOperator = new JComboBoxOperator(
                (JComboBox) bdBlockLabelOperator.getLabelFor());
        bdBlockComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index

        // Enable Hide
        //TODO: fix hardcoded index
        new JCheckBoxOperator(jFrameOperator, 0).doClick();  //TODO:fix hardcoded index

        // Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "AC")).doClick();
        //new JButtonOperator(jFrameOperator, "Edit Block 1").doClick();

        // find and dismiss the "Edit Block Xing Blk AC" dialog
        //TODO: frame (dialog) titles hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "Blk 2"));
        // but that isn't working...
        JFrameOperator blkFOac = new JFrameOperator("Edit Block Blk 1");
        new JButtonOperator(blkFOac, Bundle.getMessage("ButtonOK")).doClick();

        // Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "BD")).doClick();

        // find and dismiss the "Edit Block Xing Blk BD" dialog
        //TODO: frame (dialog) titles hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "Blk 2"));
        // but that isn't working...
        JFrameOperator blkFObd = new JFrameOperator("Edit Block Blk 2");
        new JButtonOperator(blkFObd, Bundle.getMessage("ButtonOK")).doClick();

        /* The previous block editor sections create new layout blocks so
           the following force tests of the normal create process handled by done. */
        acBlockComboBoxOperator.getTextField().setText("Xing New AC");
        bdBlockComboBoxOperator.getTextField().setText("Xing New BD");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditXingCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the level crossing
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(xing);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXing"));

        // Invoke layout block editor with no block assigned
        Thread xingBlock1Error = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "xingBlock1Error");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "AC")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(xingBlock1Error.isAlive());
        }, "xingBlock1Error finished");

        Thread xingBlock2Error = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "xingBlock2Error");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "BD")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(xingBlock2Error.isAlive());
        }, "xingBlock2Error finished");

        new JButtonOperator(jFrameOperator, "Cancel").doClick();
    }

    @Test
    public void testEditXingClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the level crossing
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(xing);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXing"));

        jFrameOperator.close();
    }

    @Test
    public void testEditTurntableDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createTurnouts();

        // Edit the turntable
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(turntable);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        // Set good radius
        //TODO: fix hardcoded index
        JTextFieldOperator jtxt = new JTextFieldOperator(jFrameOperator, 0);
        jtxt.setText("30");

        // Add 5 rays
        new JButtonOperator(jFrameOperator, "New Ray Track").doClick();
        jtxt = new JTextFieldOperator(jFrameOperator, 1);
        jtxt.setText("90");
        new JButtonOperator(jFrameOperator, "New Ray Track").doClick();
        jtxt.setText("180");
        new JButtonOperator(jFrameOperator, "New Ray Track").doClick();
        jtxt.setText("270");
        new JButtonOperator(jFrameOperator, "New Ray Track").doClick();
        jtxt.setText("315");
        new JButtonOperator(jFrameOperator, "New Ray Track").doClick();

        // Delete the 5th ray
        Thread deleteRay = createModalDialogOperatorThread("Warning", "Yes", "deleteRay");  // NOI18N
        new JButtonOperator(jFrameOperator, "Delete", 4).doClick();
        JUnitUtil.waitFor(() -> {
            return !(deleteRay.isAlive());
        }, "deleteRay finished");

        // Enable DCC control
        //TODO: fix hardcoded index
        new JCheckBoxOperator(jFrameOperator, 0).doClick();

        // Change the first ray to 30 degrees
        jtxt = new JTextFieldOperator(jFrameOperator, 2);
        jtxt.setText("30");

        // Set ray turnouts
        //TODO: fix hardcoded index
        JComboBoxOperator turnout_cbo = new JComboBoxOperator(jFrameOperator, 0);
        //TODO: fix hardcoded index
        JComboBoxOperator state_cbo = new JComboBoxOperator(jFrameOperator, 1);
        turnout_cbo.selectItem(1); //TODO: fix hardcoded index
        state_cbo.selectItem(0); //TODO: fix hardcoded index

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 2);
        state_cbo = new JComboBoxOperator(jFrameOperator, 3);
        turnout_cbo.selectItem(1); //TODO: fix hardcoded index
        state_cbo.selectItem(1); //TODO: fix hardcoded index

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 4);
        state_cbo = new JComboBoxOperator(jFrameOperator, 5);
        turnout_cbo.selectItem(2); //TODO: fix hardcoded index
        state_cbo.selectItem(0); //TODO: fix hardcoded index

        turnout_cbo = new JComboBoxOperator(jFrameOperator, 6);
        state_cbo = new JComboBoxOperator(jFrameOperator, 7);
        turnout_cbo.selectItem(2); //TODO: fix hardcoded index
        state_cbo.selectItem(1); //TODO: fix hardcoded index

        // Add a valid ray and then change the angle to an invalid value
        jtxt = new JTextFieldOperator(jFrameOperator, 2);
        jtxt.clickMouse();
        jtxt.setText("qqq");

        // Move focus
        Thread badRayAngleModalDialogOperatorThread = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "badRayAngle");  // NOI18N
        jtxt = new JTextFieldOperator(jFrameOperator, 3);
        jtxt.clickMouse();
        JUnitUtil.waitFor(() -> {
            return !(badRayAngleModalDialogOperatorThread.isAlive());
        }, "badRayAngle finished");

        // Put a good value back in
        jtxt = new JTextFieldOperator(jFrameOperator, 2);
        jtxt.setText("30");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditTurntableCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the Turntable
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(turntable);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        new JButtonOperator(jFrameOperator, "Cancel").doClick();
    }

    @Test
    public void testEditTurntableClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the Turntable
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(turntable);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        jFrameOperator.close();
    }

    @Test
    public void testEditTurntableErrors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the turntable
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(turntable);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurntable"));

        // Ray angle
        JTextFieldOperator jtxt = new JTextFieldOperator(jFrameOperator, 1);
        jtxt.setText("xyz");
        Thread badAngle = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "badAngle");  // NOI18N
        new JButtonOperator(jFrameOperator, "New Ray Track").doClick();
        JUnitUtil.waitFor(() -> {
            return !(badAngle.isAlive());
        }, "badAngle finished");

        // Set radius
        //TODO: fix hardcoded index
        jtxt = new JTextFieldOperator(jFrameOperator, 0);
        jtxt.setText("abc");

        Thread badRadius = createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"), "badRadius");  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(badRadius.isAlive());
        }, "badRadius finished");
    }

// new EventTool().waitNoEvent(10000);
    // from here down is testing infrastructure
    public void createTurnouts() {
        Turnout turnout1 = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT101");
        turnout1.setUserName("Turnout 101");
        turnout1.setCommandedState(Turnout.CLOSED);

        Turnout turnout2 = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IT102");
        turnout2.setUserName("Turnout 102");
        turnout2.setCommandedState(Turnout.CLOSED);
    }

    public void createBlocks() {
        Block block1 = InstanceManager.getDefault(jmri.BlockManager.class).provideBlock("IB1");
        block1.setUserName("Blk 1");
        Block block2 = InstanceManager.getDefault(jmri.BlockManager.class).provideBlock("IB2");
        block2.setUserName("Blk 2");
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String threadName) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread: " + threadName);
        t.start();
        return t;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        InstanceManager.setDefault(BlockManager.class, new BlockManager());
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            jmri.util.JUnitUtil.resetInstanceManager();
            jmri.util.JUnitUtil.initInternalTurnoutManager();
            jmri.util.JUnitUtil.initInternalSensorManager();

            layoutEditor = new LayoutEditor();

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 75.0);

            // Double crossover
            dxo = new LayoutTurnout("Double Xover",
                    LayoutTurnout.DOUBLE_XOVER, point, 33.0, 1.1, 1.2, layoutEditor);

            // RH Turnout
            point = MathUtil.add(point, delta);
            rhto = new LayoutTurnout("RH Turnout",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, layoutEditor);

            // Double slip
            point = MathUtil.add(point, delta);
            slip = new LayoutSlip("Double Slip",
                    point, 0.0, layoutEditor, LayoutTurnout.DOUBLE_SLIP);

            // Single slip
            point = MathUtil.add(point, delta);
            sslip = new LayoutSlip("Single Slip",
                    point, 0.0, layoutEditor, LayoutTurnout.SINGLE_SLIP);

            // Level crossing
            point = MathUtil.add(point, delta);
            xing = new LevelXing("Level Xing",
                    point, layoutEditor);

            // Turntable
            point = MathUtil.add(point, delta);
            turntable = new LayoutTurntable("Turntable",
                    point, layoutEditor);

            // Track Segment
            PositionablePoint p1 = new PositionablePoint("a", PositionablePoint.ANCHOR, new Point2D.Double(0.0, 0.0), layoutEditor);
            PositionablePoint p2 = new PositionablePoint("b", PositionablePoint.ANCHOR, new Point2D.Double(1.0, 1.0), layoutEditor);
            segment = new TrackSegment("Segment", p1, LayoutTrack.POS_POINT, p2, LayoutTrack.POS_POINT, false, false, layoutEditor);
        }
    }

    @After
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        if (dxo != null) {
            dxo.remove();
            dxo.dispose();
            dxo = null;
        }

        if (rhto != null) {
            rhto.remove();
            rhto.dispose();
            rhto = null;
        }

        if (slip != null) {
            slip.remove();
            slip.dispose();
            slip = null;
        }

        if (sslip != null) {
            sslip.remove();
            sslip.dispose();
            sslip = null;
        }

        if (xing != null) {
            xing.remove();
            xing.dispose();
            xing = null;
        }

        if (segment != null) {
            segment.remove();
            segment.dispose();
            segment = null;
        }

        if (turntable != null) {
            turntable.remove();
            turntable.dispose();
            turntable = null;
        }

        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

//    try {
//        Dumper.dumpAll("Dumper.xml");
//    } catch (Exception e2) {
//    }
    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditorsTest.class);
}
