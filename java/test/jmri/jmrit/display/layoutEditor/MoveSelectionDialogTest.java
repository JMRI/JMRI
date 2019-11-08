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
 * Test simple functioning of moveSelectionDialog
 *
 * @author	George Warner Copyright (C) 2019
 */
public class MoveSelectionDialogTest {

    private static LayoutEditor layoutEditor = null;
    private static MoveSelectionDialog moveSelectionDialog = null;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule    // allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    /*
     * This is called before each test
     */
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();
            moveSelectionDialog = new MoveSelectionDialog(layoutEditor);
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
        }
    }

    /*
     * This is called after each test
     */
    @After
    public void tearDown() {
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
            moveSelectionDialog = null;
        }
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor exists", layoutEditor);
        Assert.assertNotNull("MoveSelectionDialog exists", moveSelectionDialog);
    }

    @Test
    public void testMoveSelectionCanceled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        moveSelectionDialog.moveSelection();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("TranslateSelection"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testMoveSelection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        moveSelectionDialog.moveSelection();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("TranslateSelection"));

        // get MoveSelection button
        JButtonOperator moveSelectionButtonOperator = new JButtonOperator(jFrameOperator,
                Bundle.getMessage("MoveSelection"));

        // try to enter an invalid value in horizontal (x) translation text field
        JLabelOperator xTranslateLabelOperator = new JLabelOperator(jFrameOperator, Bundle.getMessage("XTranslateLabel"));
        JTextFieldOperator horizontalTranslationTextFieldOperator = new JTextFieldOperator(
                (JTextField) xTranslateLabelOperator.getLabelFor());
        horizontalTranslationTextFieldOperator.setText("NumberFormatException string");

        Thread misc1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        moveSelectionButtonOperator.doClick();
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

        Thread misc2 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        moveSelectionButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc2.isAlive());
        }, "misc2 finished");

        // now set the vertical (y) translation text field
        verticalTranslationTextFieldOperator.setText("100");

        // and everything should work!
        moveSelectionButtonOperator.doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }
}
