package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import javax.swing.JTextField;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test simple functioning of scaleTrackDiagramDialog
 *
 * @author	George Warner Copyright (C) 2019
 */
public class ScaleTrackDiagramDialogTest {

    private static LayoutEditor layoutEditor = null;
    private static ScaleTrackDiagramDialog scaleTrackDiagramDialog = null;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule    // allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    /*
     * This is called before each tests
     */
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
            scaleTrackDiagramDialog = new ScaleTrackDiagramDialog(layoutEditor);
        }
    }

    /*
     * This is called after each tests
     */
    @After
    public void tearDown() {
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
            scaleTrackDiagramDialog = null;
        }
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor exists", layoutEditor);
        Assert.assertNotNull("scaleTrackDiagramDialog exists", scaleTrackDiagramDialog);
    }

    @Test
    public void testScaleTrackDiagramCanceled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        scaleTrackDiagramDialog.scaleTrackDiagram();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("ScaleTrackDiagram"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testScaleTrackDiagram() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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

        Thread misc1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
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

        Thread misc2 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ErrorTitle"),
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

        Thread misc3 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
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

        Thread misc4 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
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
