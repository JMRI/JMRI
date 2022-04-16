package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.geom.Rectangle2D;
import javax.swing.JTextField;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test simple functioning of scaleTrackDiagramDialog
 *
 * @author George Warner Copyright (C) 2019
 */
@Timeout(10)
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class ScaleTrackDiagramDialogTest {

    private LayoutEditor layoutEditor = null;
    private ScaleTrackDiagramDialog scaleTrackDiagramDialog = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        layoutEditor = new LayoutEditor();
        layoutEditor.setVisible(true);
        layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
        scaleTrackDiagramDialog = new ScaleTrackDiagramDialog(layoutEditor);
    }

    @AfterEach
    public void tearDown() {
        EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
        efo.closeFrameWithConfirmations();
        EditorFrameOperator.clearEditorFrameOperatorThreads();
        layoutEditor = null;
        scaleTrackDiagramDialog = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor() {
        Assertions.assertNotNull( layoutEditor, "layoutEditor exists");
        Assertions.assertNotNull( scaleTrackDiagramDialog, "scaleTrackDiagramDialog exists");
    }

    @Test
    public void testScaleTrackDiagramCanceled() {

        scaleTrackDiagramDialog.scaleTrackDiagram();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("ScaleTrackDiagram"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testScaleTrackDiagram() {

        scaleTrackDiagramDialog.scaleTrackDiagram();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("ScaleTrackDiagram"));

        // get ScaleTranslate button
        JButtonOperator scaleTranslateButtonOperator = new JButtonOperator(jFrameOperator,
                Bundle.getMessage("ScaleTranslate"));

        // try to enter an invalid value in horizontal (x) translation text field
        JLabelOperator xTranslateLabelOperator = new JLabelOperator(jFrameOperator, Bundle.getMessage("XTranslateLabel"));
        JTextFieldOperator horizontalTranslationTextFieldOperator = new JTextFieldOperator(
                (JTextField) xTranslateLabelOperator.getLabelFor());
        horizontalTranslationTextFieldOperator.setText("NumberFormatException string");

        Thread misc1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        scaleTranslateButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        // now set the horizontal (x) translation text field
        horizontalTranslationTextFieldOperator.setText("50");

        // try to enter an invalid value in vertical (y) translation text field
        JLabelOperator yTranslateLabelOperator = new JLabelOperator(jFrameOperator, Bundle.getMessage("YTranslateLabel"));
        JTextFieldOperator verticalTranslationTextFieldOperator = new JTextFieldOperator(
                (JTextField) yTranslateLabelOperator.getLabelFor());
        verticalTranslationTextFieldOperator.setText("NumberFormatException string");

        Thread misc2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        scaleTranslateButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc2.isAlive());
        }, "misc2 finished");

        // now set the vertical (y) translation text field
        verticalTranslationTextFieldOperator.setText("100");

        // try to enter an invalid value in the horizontal (x) scale factor text field
        JLabelOperator xFactorLabelLabelOperator = new JLabelOperator(jFrameOperator, Bundle.getMessage("XFactorLabel"));
        JTextFieldOperator horizontalScaleFactorTextFieldOperator = new JTextFieldOperator(
                (JTextField) xFactorLabelLabelOperator.getLabelFor());
        horizontalScaleFactorTextFieldOperator.setText("NumberFormatException string");

        Thread misc3 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        scaleTranslateButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc3.isAlive());
        }, "misc3 finished");

        // now set the horizontal (x) scale factor text field
        horizontalScaleFactorTextFieldOperator.setText("2");

        // try to enter an invalid value in vertical (y) scale factor text field
        JLabelOperator yFactorLabelLabelOperator = new JLabelOperator(jFrameOperator, Bundle.getMessage("YFactorLabel"));
        JTextFieldOperator verticalScaleFactorTextFieldOperator = new JTextFieldOperator(
                (JTextField) yFactorLabelLabelOperator.getLabelFor());
        verticalScaleFactorTextFieldOperator.setText("NumberFormatException string");

        Thread misc4 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        scaleTranslateButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc4.isAlive());
        }, "misc4 finished");

        // now set the Y  (vertical) translation text field
        verticalScaleFactorTextFieldOperator.setText("2");

        // and everything should work!
        scaleTranslateButtonOperator.doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }
}
