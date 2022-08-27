package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.geom.Rectangle2D;
import javax.swing.JTextField;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test simple functioning of moveSelectionDialog
 *
 * @author George Warner Copyright (C) 2019
 */
@Timeout(10)
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class MoveSelectionDialogTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("layoutEditor exists", layoutEditor);
        Assert.assertNotNull("MoveSelectionDialog exists", moveSelectionDialog);
    }

    @Test
    public void testMoveSelectionCanceled() {
        moveSelectionDialog.moveSelection();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("TranslateSelection"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testMoveSelection() {

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

    private LayoutEditor layoutEditor = null;
    private MoveSelectionDialog moveSelectionDialog = null;

    /**
     * This is called before each test.
     */
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        layoutEditor = new LayoutEditor(this.getClass().getName());
        layoutEditor.setVisible(true);
        moveSelectionDialog = new MoveSelectionDialog(layoutEditor);
        layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
    }

    /**
     * This is called after each test.
     */
    @AfterEach
    public void tearDown() {
        // new jmri.jmrit.display.EditorFrameOperator(layoutEditor).closeFrameWithConfirmations();
        Assertions.assertNotNull(layoutEditor);
        layoutEditor.dispose();
        layoutEditor = null;
        moveSelectionDialog = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
