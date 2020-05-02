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
 * Test simple functioning of LayoutSingleSlipEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutSingleSlipEditorTest extends LayoutSlipEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutSingleSlipEditor(null);
    }

    @Test
    public void testEditSingleSlipDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();
        createBlocks();

        LayoutSingleSlipEditor editor = new LayoutSingleSlipEditor(layoutEditor);

        // Edit the single Slip
        editor.editLayoutSlip(singleLayoutSlip);
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

        // click Test button three times
        JButtonOperator testButtonOperator = new JButtonOperator(jFrameOperator, "Test");
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

    private LayoutEditor layoutEditor = null;
    private LayoutSlip singleLayoutSlip = null;

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

            // Single doubleLayoutSlip
            point = MathUtil.add(point, delta);
            singleLayoutSlip = new LayoutSingleSlip("Single Slip",
                    point, 0.0, layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (singleLayoutSlip != null) {
            singleLayoutSlip.remove();
            singleLayoutSlip.dispose();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }

        singleLayoutSlip = null;
        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

    /*
     * This is used to find a component by matching against its tooltip
     */
    protected static class ToolTipComponentChooser implements ComponentChooser {

        private String buttonTooltip;
        private StringComparator comparator = Operator.getDefaultStringComparator();

        public ToolTipComponentChooser(String buttonTooltip) {
            this.buttonTooltip = buttonTooltip;
        }

        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent) comp).getToolTipText(), buttonTooltip);
        }

        public String getDescription() {
            return "Component with tooltip \"" + buttonTooltip + "\".";
        }
    }


    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSingleSlipEditorTest.class);
}
