package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import javax.swing.*;
import jmri.*;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Test simple functioning of LayoutDoubleSlipEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutDoubleSlipEditorTest extends LayoutSlipEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutDoubleSlipEditor(null);
    }

    @Test
    public void testEditDoubleSlipDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();
        createBlocks();

        LayoutDoubleSlipEditor editor = new LayoutDoubleSlipEditor(layoutEditor);

        // Edit the double Slip
        editor.editLayoutTrack(doubleLayoutSlip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        // Select turnout A
        JLabelOperator firstTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BeanNameTurnout") + " A");
        JComboBoxOperator firstTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) firstTurnoutLabelOperator.getLabelFor());
        firstTurnoutComboBoxOperator.selectItem(1); //TODO: fix hardcoded index

        // Select turnout B
        JLabelOperator secondTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BeanNameTurnout") + " B");
        JComboBoxOperator secondTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) secondTurnoutLabelOperator.getLabelFor());
        secondTurnoutComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index

        // Create a (new) block
        JTextFieldOperator blockTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockNameHint")));
        blockTextFieldOperator.setText("Slip Block");

        // Enable Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideSlip")).doClick();

        // click Test button four times
        JButtonOperator testButtonOperator = new JButtonOperator(jFrameOperator, "Test");
        testButtonOperator.doClick();
        testButtonOperator.doClick();
        testButtonOperator.doClick();
        testButtonOperator.doClick();

        // Invoke layout block editor
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();

        // Close the block editor dialog
        //TODO: frame (dialog) title hard coded here...
        // it should be based on Bundle.getMessage("EditBean", "Block", "Slip Block"));
        // but that isn't working...
        JFrameOperator blkFO = new JFrameOperator("Edit Block Slip Block");
        new JButtonOperator(blkFO, Bundle.getMessage("ButtonOK")).doClick();

        /* The previous block editor sections create new layout blocks so
           the following force tests of the normal create process handled by done. */
        blockTextFieldOperator.setText("New Slip Block");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }


    @Test
    public void testEditSlipCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutDoubleSlipEditor editor = new LayoutDoubleSlipEditor(layoutEditor);

        // Edit the double doubleLayoutSlip
        editor.editLayoutTrack(doubleLayoutSlip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        // Invoke layout block editor with no block assigned
        Thread slipBlockError = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        new JButtonOperator(jFrameOperator, Bundle.getMessage("EditBlock", "")).doClick();
        JUnitUtil.waitFor(() -> {
            return !(slipBlockError.isAlive());
        }, "slipBlockError finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    @Test
    public void testEditSlipClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutDoubleSlipEditor editor = new LayoutDoubleSlipEditor(layoutEditor);

        // Edit the double doubleLayoutSlip
        editor.editLayoutTrack(doubleLayoutSlip);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }



    private LayoutEditor layoutEditor = null;
    private LayoutSlip doubleLayoutSlip = null;

    @Before
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

            // doubleLayoutSlip
            point = MathUtil.add(point, delta);
            doubleLayoutSlip = new LayoutDoubleSlip("Double Slip",
                    point, 0.0, layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (doubleLayoutSlip != null) {
            doubleLayoutSlip.remove();
            doubleLayoutSlip.dispose();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }

        doubleLayoutSlip = null;
        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleSlipEditorTest.class);
}
