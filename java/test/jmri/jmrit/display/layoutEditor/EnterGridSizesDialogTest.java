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
 * Test simple functioning of enterGridSizesDialog
 *
 * @author	George Warner Copyright (C) 2019
 */
public class EnterGridSizesDialogTest {

    private static LayoutEditor layoutEditor = null;
    private static EnterGridSizesDialog enterGridSizesDialog = null;

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
            enterGridSizesDialog = new EnterGridSizesDialog(layoutEditor);
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
            enterGridSizesDialog = null;
        }
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor exists", layoutEditor);
        Assert.assertNotNull("EnterGridSizesDialog exists", enterGridSizesDialog);
    }

    @Test
    public void testEnterGridSizesCanceled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        enterGridSizesDialog.enterGridSizes();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SetGridSizes"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEnterGridSizes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        enterGridSizesDialog.enterGridSizes();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SetGridSizes"));

        JLabelOperator primaryGridSizeLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("PrimaryGridSize"));
        JTextFieldOperator primaryGridSizeTextFieldOperator = new JTextFieldOperator(
                (JTextField) primaryGridSizeLabelOperator.getLabelFor());

        JButtonOperator doneButtonOperator = new JButtonOperator(jFrameOperator,
                Bundle.getMessage("ButtonDone"));

        // try to enter an invalid value in the primary grid size text field
        primaryGridSizeTextFieldOperator.setText("NumberFormatException string");

        Thread misc1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        //restore valid value
        int oldGridSize1st = layoutEditor.getGridSize();
        primaryGridSizeTextFieldOperator.setText(Integer.toString(oldGridSize1st));

        // try to enter an invalid value in the secondary grid size text field
        JLabelOperator secondaryGridSizeLabelOperator = new JLabelOperator(
                jFrameOperator, Bundle.getMessage("SecondaryGridSize"));
        JTextFieldOperator secondaryGridSizeTextFieldOperator = new JTextFieldOperator(
                (JTextField) secondaryGridSizeLabelOperator.getLabelFor());

        secondaryGridSizeTextFieldOperator.setText("NumberFormatException string");

        Thread misc2 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        doneButtonOperator.doClick();
        JUnitUtil.waitFor(() -> {
            return !(misc2.isAlive());
        }, "misc2 finished");

        //put in new (valid) values
        primaryGridSizeTextFieldOperator.setText(Integer.toString(oldGridSize1st ^ 1));
        int oldGridSize2nd = layoutEditor.getGridSize2nd();
        secondaryGridSizeTextFieldOperator.setText(Integer.toString(oldGridSize2nd ^ 1));

        doneButtonOperator.doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

        Assert.assertEquals("new grid size 1st", oldGridSize1st ^ 1, layoutEditor.getGridSize());
        Assert.assertEquals("new grid size 2nd", oldGridSize2nd ^ 1, layoutEditor.getGridSize2nd());

        layoutEditor.setGridSize(oldGridSize1st);
        Assert.assertEquals("old grid size 1st", oldGridSize1st, layoutEditor.getGridSize());

        layoutEditor.setGridSize2nd(oldGridSize2nd);
        Assert.assertEquals("old grid size 2nd", oldGridSize2nd, layoutEditor.getGridSize2nd());
    }
}
