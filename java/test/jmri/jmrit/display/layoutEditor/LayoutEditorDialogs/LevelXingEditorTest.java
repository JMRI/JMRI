package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import javax.swing.*;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of LevelXing.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LevelXingEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LevelXingEditor(null);
    }

    @Test
    public void testEditXingDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createBlocks();

        LevelXingEditor editor = new LevelXingEditor(layoutEditor);

        // Edit the level crossing
        editor.editLayoutTrack(levelXingView);
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
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideCrossing")).doClick();

        // Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "AC")).doClick();

        // find and dismiss the "Edit Block Xing Blk AC" dialog
        //TODO: frame (dialog) titles hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "Blk 1"));
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

        try {
            new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
            jFrameOperator.waitClosed();    // make sure the dialog actually closed
        } catch (Exception ex) {
            log.error("Jemmy temporary", ex);
        }
    }

    @Test
    public void testEditXingCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LevelXingEditor editor = new LevelXingEditor(layoutEditor);

        // Edit the level crossing
        editor.editLayoutTrack(levelXingView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXing"));

        // Invoke layout block editor with no block assigned
        Thread xingBlock1Error = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "AC")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(xingBlock1Error.isAlive());
        }, "xingBlock1Error finished");

        Thread xingBlock2Error = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "BD")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(xingBlock2Error.isAlive());
        }, "xingBlock2Error finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditXingClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LevelXingEditor editor = new LevelXingEditor(layoutEditor);

        // Edit the level crossing
        editor.editLayoutTrack(levelXingView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXing"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }


    private LayoutEditor layoutEditor = null;
    private LevelXing levelXing = null;
    private LevelXingView levelXingView = null;

    @BeforeEach
    public void setUp() {
        super.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        if (!GraphicsEnvironment.isHeadless()) {

            layoutEditor = new LayoutEditor();
            layoutEditor.setVisible(true);

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 10.0);

            // Level crossing
            point = MathUtil.add(point, delta);
            levelXing = new LevelXing("Level Xing", layoutEditor); // point
            levelXingView = new LevelXingView(levelXing, point, layoutEditor);
            layoutEditor.addLayoutTrack(levelXing, levelXingView);
        }
    }

    @AfterEach
    public void tearDown() {
        if (levelXing != null) {
            levelXing.remove();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }

        layoutEditor = null;
        levelXing = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditorTest.class);
}
