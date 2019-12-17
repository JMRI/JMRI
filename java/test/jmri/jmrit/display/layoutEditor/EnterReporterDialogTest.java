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
 * Test simple functioning of enterReporterDialog
 *
 * @author	George Warner Copyright (C) 2019
 */
public class EnterReporterDialogTest {

    private static LayoutEditor layoutEditor = null;
    private static EnterReporterDialog enterReporterDialog = null;

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
            enterReporterDialog = new EnterReporterDialog(layoutEditor);
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
            enterReporterDialog = null;
        }
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor exists", layoutEditor);
        Assert.assertNotNull("EnterReporterDialog exists", enterReporterDialog);
    }

    @Test
    public void testEnterReporterCanceled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        enterReporterDialog.enterReporter(150, 200);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("AddReporter"));

        // cancel the dialog
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEnterReporter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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

        Thread misc1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        addNewLabelButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        // ok, now set the reporter name to an invalid (doesn't start with IB) value
        reporterNameTextFieldOperator.setText("ClarkKent");
        addNewLabelButtonOperator.doClick();
        jmri.util.JUnitAppender.assertErrorMessage("Invalid system name for Reporter: System name must start with \"IR\".");
        //TODO: any way to verify that the dialog is closed?

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

        Thread misc2 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
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

        Thread misc3 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
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
}
