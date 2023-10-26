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
 * Test simple functioning of enterGridSizesDialog
 *
 * @author George Warner Copyright (C) 2019
 */
@Timeout(10)
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class EnterGridSizesDialogTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("layoutEditor exists", layoutEditor);
        Assert.assertNotNull("EnterGridSizesDialog exists", enterGridSizesDialog);
    }

    @Test
    public void testEnterGridSizesCanceled() {

        enterGridSizesDialog.enterGridSizes();
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("SetGridSizes"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();  // NOI18N
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEnterGridSizes() {

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
        int oldGridSize1st = layoutEditor.gContext.getGridSize();
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
        int oldGridSize2nd = layoutEditor.gContext.getGridSize2nd();
        secondaryGridSizeTextFieldOperator.setText(Integer.toString(oldGridSize2nd ^ 1));

        doneButtonOperator.doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

        Assert.assertEquals("new grid size 1st", oldGridSize1st ^ 1, layoutEditor.gContext.getGridSize());
        Assert.assertEquals("new grid size 2nd", oldGridSize2nd ^ 1, layoutEditor.gContext.getGridSize2nd());

        layoutEditor.gContext.setGridSize(oldGridSize1st);
        Assert.assertEquals("old grid size 1st", oldGridSize1st, layoutEditor.gContext.getGridSize());

        layoutEditor.gContext.setGridSize2nd(oldGridSize2nd);
        Assert.assertEquals("old grid size 2nd", oldGridSize2nd, layoutEditor.gContext.getGridSize2nd());
    }

    private LayoutEditor layoutEditor = null;
    private EnterGridSizesDialog enterGridSizesDialog = null;

    /*
     * This is called before each test
     */
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        layoutEditor = new LayoutEditor(this.getClass().getName());
        enterGridSizesDialog = new EnterGridSizesDialog(layoutEditor);
        layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
        layoutEditor.setVisible(true);

    }

    /*
     * This is called after each test
     */
    @AfterEach
    public void tearDown() {

        // new EditorFrameOperator(layoutEditor).closeFrameWithConfirmations();
        layoutEditor.dispose();
        layoutEditor = null;
        enterGridSizesDialog = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
