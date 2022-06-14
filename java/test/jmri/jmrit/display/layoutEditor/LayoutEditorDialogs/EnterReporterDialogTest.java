package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.geom.Rectangle2D;
import javax.swing.JTextField;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test simple functioning of enterReporterDialog
 *
 * @author George Warner Copyright (C) 2019
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class EnterReporterDialogTest {

    @Test
    public void testCtor() {

        Assertions.assertNotNull(layoutEditor, "layoutEditor exists");
        Assertions.assertNotNull(enterReporterDialog, "EnterReporterDialog exists");
    }

    @Test
    public void testEnterReporterCanceled() {

        enterReporterDialog.enterReporter(150, 200);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("AddReporter"));

        // cancel the dialog
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEnterReporter() {

        enterReporterDialog.enterReporter(150, 200);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("AddReporter"));

        // Try to press Add New Label button with reporter name blank... should get an error dialog
        JLabelOperator reporterNameLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("ReporterName"));
        JTextFieldOperator reporterNameTextFieldOperator = new JTextFieldOperator(
                (JTextField) reporterNameLabelOperator.getLabelFor());
        reporterNameTextFieldOperator.clearText();

        JButtonOperator addNewLabelButtonOperator = new JButtonOperator(jFrameOperator,
                Bundle.getMessage("AddNewLabel"));

        Thread misc1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        addNewLabelButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        // ok, now set the reporter name to an invalid (doesn't start with IB) value
        reporterNameTextFieldOperator.setText("ClarkKent");
        addNewLabelButtonOperator.doClick();
        JUnitAppender.assertErrorMessage("Invalid system name for Reporter: System name must start with \"IR\".");
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

        enterReporterDialog.enterReporter(150, 200);
        jFrameOperator = new JFrameOperator(Bundle.getMessage("AddReporter"));

        // ok, now set the reporter name to an valid (starts with IB) value
        reporterNameLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("ReporterName"));
        reporterNameTextFieldOperator = new JTextFieldOperator(
                (JTextField) reporterNameLabelOperator.getLabelFor());

        reporterNameTextFieldOperator.setText("IBClarkKent");

        // try to enter an invalid value in X location text field
        JLabelOperator reporterLocationX = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("ReporterLocationX"));
        JTextFieldOperator xLocationTextFieldOperator = new JTextFieldOperator(
                (JTextField) reporterLocationX.getLabelFor());
        xLocationTextFieldOperator.setText("NumberFormatException string");

        Thread misc2 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        addNewLabelButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc2.isAlive());
        }, "misc2 finished");

        // now set the X location text field to a valid value
        xLocationTextFieldOperator.setText("50");

        // try to enter an invalid value in Y location text field
        JLabelOperator reporterLocationY = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("ReporterLocationY"));
        JTextFieldOperator yLocationTextFieldOperator = new JTextFieldOperator(
                (JTextField) reporterLocationY.getLabelFor());
        yLocationTextFieldOperator.setText("NumberFormatException string");

        Thread misc3 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        addNewLabelButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc3.isAlive());
        }, "misc3 finished");

        // now set the Y location text field to a valid value
        yLocationTextFieldOperator.setText("100");

        // and everything should work!
        addNewLabelButtonOperator.doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    private LayoutEditor layoutEditor = null;
    private EnterReporterDialog enterReporterDialog = null;

    /*
     * This is called before each test
     */
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        layoutEditor = new LayoutEditor();
        enterReporterDialog = new EnterReporterDialog(layoutEditor);
        layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
        layoutEditor.setVisible(true);

    }

    /*
     * This is called after each test
     */
    @AfterEach
    public void tearDown() {

        EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
        efo.closeFrameWithConfirmations();
        layoutEditor = null;
        enterReporterDialog = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.tearDown();
    }

}
