package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;

import javax.swing.*;

import jmri.jmrit.display.layoutEditor.*;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of LayoutTurnoutEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTurnoutEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutTurnoutEditor(null);
    }
 
    protected void turnoutTestSequence(LayoutTurnoutEditor editor, LayoutTurnoutView turnoutView) {
        createTurnouts();
        createBlocks();

        // Edit the rh turnout
        editor.editLayoutTrack(turnoutView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTurnout"));

        // Select main turnout
        JLabelOperator mainTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator mainTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) mainTurnoutLabelOperator.getLabelFor());
        mainTurnoutComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        // Enable second turnout and select it
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("ThrowTwoTurnouts")).doClick();

        JLabelOperator supportingTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("Supporting", Bundle.getMessage("BeanNameTurnout")));
        JComboBoxOperator supportingTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) supportingTurnoutLabelOperator.getLabelFor());
        supportingTurnoutComboBoxOperator.selectItem(2);  //TODO:fix hardcoded index

        // Enable Invert and Hide
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("SecondTurnoutInvert")).doClick();
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("HideTurnout")).doClick();

        // Continuing route option
        JLabelOperator continuingTurnoutLabelOperator = new JLabelOperator(jFrameOperator,
                Bundle.getMessage("ContinuingState"));
        JComboBoxOperator continuingTurnoutComboBoxOperator = new JComboBoxOperator(
                (JComboBox) continuingTurnoutLabelOperator.getLabelFor());
        continuingTurnoutComboBoxOperator.selectItem(1);  //TODO:fix hardcoded index

        // put a new block name in the block combobox's textfield
        JTextFieldOperator blockTextFieldOperator = new JTextFieldOperator(jFrameOperator,
                new ToolTipComponentChooser(Bundle.getMessage("EditBlockNameHint")));
        blockTextFieldOperator.setText("QRS Block");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutEditorTest.class);
}
