package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.geom.Point2D;

import javax.swing.*;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of LayoutSingleSlipEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutSingleSlipEditorTest extends LayoutSlipEditorTest {

    @Test
    @Override
    public void testCtor() {
        LayoutSingleSlipEditor t = new LayoutSingleSlipEditor(layoutEditor);
        Assertions.assertNotNull(t);
        
    }

    @Test
    public void testEditSingleSlipDone() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();
        createBlocks();

        LayoutSingleSlipEditor editor = new LayoutSingleSlipEditor(layoutEditor);

        // Edit the single Slip
        editor.editLayoutTrack(singleLayoutSlipView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditSlip"));

        // Select turnout A
        JLabelOperator firstTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BeanNameTurnout") + " A");
        JComboBoxOperator firstTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox<?>) firstTurnoutLabelOperator.getLabelFor());
        firstTurnoutComboBoxOperator.selectItem(1); //TODO: fix hardcoded index

        // Select turnout B
        JLabelOperator secondTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("BeanNameTurnout") + " B");
        JComboBoxOperator secondTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox<?>) secondTurnoutLabelOperator.getLabelFor());
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

    private LayoutSingleSlip singleLayoutSlip = null;
    private LayoutSingleSlipView singleLayoutSlipView = null;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        
        Point2D point = new Point2D.Double(150.0, 100.0);
        Point2D delta = new Point2D.Double(50.0, 10.0);

        // Single doubleLayoutSlip
        point = MathUtil.add(point, delta);
        singleLayoutSlip = new LayoutSingleSlip("Single Slip",  // point, 0.0,
                layoutEditor);
        singleLayoutSlipView = new LayoutSingleSlipView(singleLayoutSlip,
                point, 0.0,
                layoutEditor);
        layoutEditor.addLayoutTrack(singleLayoutSlip, singleLayoutSlipView);

    }

    @AfterEach
    @Override
    public void tearDown() {
        if (singleLayoutSlip != null) {
            singleLayoutSlip.remove();
        }

        singleLayoutSlip = null;

        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSingleSlipEditorTest.class);
}
