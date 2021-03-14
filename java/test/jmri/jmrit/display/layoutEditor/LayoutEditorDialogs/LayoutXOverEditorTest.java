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
 * Test simple functioning of LayoutXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutXOverEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutXOverEditor(null);
    }

    @Test
    public void testEditXOverDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();

        LayoutDoubleXOverEditor editor = new LayoutDoubleXOverEditor(layoutEditor);

        // Edit the double crossover
        editor.editLayoutTrack(doubleXoverLayoutTurnoutView);
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
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SecondTurnoutInvert")).doClick();
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideXover")).doClick();

        // Ener new names for each block position
        JTextFieldOperator blockATextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockNameHint")));
        blockATextFieldOperator.setText("DX Blk A");
        JButtonOperator editBlockButtonOperator = new JButtonOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockHint", "")));
        editBlockButtonOperator.doClick();

        //TODO: frame (dialog) titles hard coded here...
        // should be: new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk A"));
        // but that's not working...
        JFrameOperator blkFOa = new JFrameOperator("Edit Block DX Blk A");
        new JButtonOperator(blkFOa, Bundle.getMessage("ButtonOK")).doClick();

        JTextFieldOperator blockBTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockBNameHint")));
        blockBTextFieldOperator.setText("DX Blk B");

        editBlockButtonOperator = new JButtonOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockHint", "2")));
        editBlockButtonOperator.doClick();

        //TODO: frame (dialog) titles hard coded here...
        //should be: new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk B"));
        JFrameOperator blkFOb = new JFrameOperator("Edit Block DX Blk B");
        new JButtonOperator(blkFOb, Bundle.getMessage("ButtonOK")).doClick();

        JTextFieldOperator blockCTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockCNameHint")));
        blockCTextFieldOperator.setText("DX Blk C");
        editBlockButtonOperator = new JButtonOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockHint", "3")));
        editBlockButtonOperator.doClick();

        //TODO: frame (dialog) titles hard coded here...
        //should be: new JFrameOperator(Bundle.getMessage("EditBean", "Block", "DX Blk C"));
        JFrameOperator blkFOc = new JFrameOperator("Edit Block DX Blk C");
        new JButtonOperator(blkFOc, Bundle.getMessage("ButtonOK")).doClick();

        JTextFieldOperator blockDTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockDNameHint")));
        blockDTextFieldOperator.setText("DX Blk D");
        editBlockButtonOperator = new JButtonOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockHint", "4")));
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
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }


   @Test
    public void testEditTurnoutCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutDoubleXOverEditor editor = new LayoutDoubleXOverEditor(layoutEditor);

        // Edit the double crossover
        editor.editLayoutTrack(doubleXoverLayoutTurnoutView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXover"));

        // Invoke layout block editor with no block assigned
        Thread turnoutBlockAError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 0).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockAError.isAlive());
        }, "turnoutBlockAError finished");

        Thread turnoutBlockBError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 1).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockBError.isAlive());
        }, "turnoutBlockBError finished");

        Thread turnoutBlockCError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 2).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockCError.isAlive());
        }, "turnoutBlockCError finished");

        Thread turnoutBlockDError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("CreateEdit"), 3).doClick();
        JUnitUtil.waitFor(() -> {
            return !(turnoutBlockDError.isAlive());
        }, "turnoutBlockDError finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditDoubleXoverClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutDoubleXOverEditor editor = new LayoutDoubleXOverEditor(layoutEditor);

        // Edit the double crossover
        editor.editLayoutTrack(doubleXoverLayoutTurnoutView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditXover"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }


    private LayoutEditor layoutEditor = null;
    private LayoutDoubleXOver doubleXoverLayoutTurnout = null;
    private LayoutDoubleXOverView doubleXoverLayoutTurnoutView = null;

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

            point = MathUtil.add(point, delta);

            // Double crossover
            doubleXoverLayoutTurnout = new LayoutDoubleXOver("Double Xover", layoutEditor); // point, 33.0, 1.1, 1.2,
            doubleXoverLayoutTurnoutView = new LayoutDoubleXOverView(doubleXoverLayoutTurnout,
                                                    point, 33.0, 1.1, 1.2,
                                                    layoutEditor);
            layoutEditor.addLayoutTrack(doubleXoverLayoutTurnout, doubleXoverLayoutTurnoutView);

        }
    }

    @AfterEach
    public void tearDown() {
        if (doubleXoverLayoutTurnout != null) {
            doubleXoverLayoutTurnout.remove();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }

        doubleXoverLayoutTurnout = null;
        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutXOverEditorTest.class);
}
