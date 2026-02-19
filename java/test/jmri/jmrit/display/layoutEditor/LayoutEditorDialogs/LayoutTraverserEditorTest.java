package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Component;
import java.awt.geom.Point2D;

import javax.swing.*;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of LayoutTraverserEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 * @author Dave Sand Copyright (c) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class LayoutTraverserEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        LayoutTraverserEditor t = new LayoutTraverserEditor(layoutEditor);
        Assertions.assertNotNull(t);
    }

    @Test
    public void testEditTraverserDone() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        createTurnouts();

        LayoutTraverserEditor editor = new LayoutTraverserEditor(layoutEditor);

        // Edit the layoutTraverser
        editor.editLayoutTrack(layoutTraverserView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTraverser"));

        // Set width
        // Use a ComponentChooser for more robust JLabel lookup
        JLabelOperator jLabelOperator = new JLabelOperator(jFrameOperator, new org.netbeans.jemmy.ComponentChooser() {
            @Override
            public boolean checkComponent(java.awt.Component comp) {
                return comp instanceof JLabel && ((JLabel) comp).getText().equals(Bundle.getMessage("Width"));
            }
            @Override
            public String getDescription() {
                return "JLabel with text '" + Bundle.getMessage("Width") + "'";
            }
        });
        // Find the JTextField for width directly by index (1), as getLabelFor() is null
        JTextFieldOperator jtxt = new JTextFieldOperator(jFrameOperator, 1);
        jtxt.setText("40");

        // Add a slot pair
        JButtonOperator addSlotButtonOperator = new JButtonOperator(
                jFrameOperator, Bundle.getMessage("AddSlotPair"));
        addSlotButtonOperator.doClick();

        // Enable DCC control
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("TraverserDCCControlled")).doClick();

        // Set slot turnouts
        // Index 0: Orientation
        // Index 1: Block Name
        // Index 2: Mainline
        // Index 3: First turnout (Slot 1A)
        // Index 4: Turnout state (Slot 1A)
        // Index 5: Second turnout (Slot 1B)
        // Index 6: Turnout state (Slot 1B)
        
        // Note: The TraverserPairPanel adds components dynamically.
        // We need to be careful about indices.
        
        JComboBoxOperator turnout_cbo = new JComboBoxOperator(jFrameOperator, 3);
        JComboBoxOperator state_cbo = new JComboBoxOperator(jFrameOperator, 4);

        turnout_cbo.selectItem(1); 
        state_cbo.selectItem(0); 

        // Add another slot pair
        addSlotButtonOperator.doClick();
        
        // Set turnouts for second pair (indices shift)
        // Previous pair: 3, 4, 5, 6
        // New pair: 7, 8, 9, 10
        
        turnout_cbo = new JComboBoxOperator(jFrameOperator, 7);
        state_cbo = new JComboBoxOperator(jFrameOperator, 8);
        turnout_cbo.selectItem(2); 
        state_cbo.selectItem(1); 

        // NEW TEST: Disable one side of a slot
        // Checkbox indices for text "Disabled":
        // 0: Pair 0, Side A (Slot 0)
        // 1: Pair 0, Side B (Slot 1)
        // 2: Pair 1, Side A (Slot 2)
        // 3: Pair 1, Side B (Slot 3)
        JCheckBoxOperator disabledCheckBox = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("Disabled"), 0);
        disabledCheckBox.doClick();

        // Test invalid width
        // Removed jtxt.clickMouse() as it's not needed for setText and validation is on Done button.
        jtxt.setText("qqq");

        // Move focus to trigger validation
        Thread badWidthModalDialogOperatorThread = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));  // NOI18N
        
        // Click somewhere else (e.g., Slot Offset field)
        // Using JLabelOperator to find the SlotOffset field more robustly
        JLabelOperator slotOffsetLabel = new JLabelOperator(jFrameOperator, new org.netbeans.jemmy.ComponentChooser() {
            @Override
            public boolean checkComponent(java.awt.Component comp) {
                return comp instanceof JLabel && ((JLabel) comp).getText().equals(Bundle.getMessage("SlotOffset"));
            }
            @Override
            public String getDescription() {
                return "JLabel with text '" + Bundle.getMessage("SlotOffset") + "'";
            }
        });
        // Find the JTextField for slot offset directly by index (2)
        JTextFieldOperator slotOffsetTxt = new JTextFieldOperator(jFrameOperator, 2);
        slotOffsetTxt.clickMouse();
        
        // Wait for error dialog? 
        // Actually, the validation in LayoutTraverserEditor happens on Done pressed for width/length usually, 
        // or focus lost if a listener is added. 
        // Looking at code: deckWidthTextField doesn't have a focus listener for validation. 
        // It's validated in editLayoutTraverserDonePressed.
        
        // So let's try to click Done with invalid width
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        
        JUnitUtil.waitFor(() -> {
            return !(badWidthModalDialogOperatorThread.isAlive());
        }, "badWidth finished");

        // Put a good value back in
        jtxt.clickMouse();
        jtxt.setText("50");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

        // VERIFY NEW TEST: Assert that the first slot (Side A) is disabled
        Assertions.assertTrue(layoutTraverser.getSlotList().get(0).isDisabled(), "First slot (Side A) should be disabled after editing.");
    }

    @Test
    public void testEditTraverserCancel() {
        LayoutTraverserEditor editor = new LayoutTraverserEditor(layoutEditor);
        editor.editLayoutTrack(layoutTraverserView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTraverser"));
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();
    }

    @Test
    public void testTraverserIsDrawn() {
        // The layoutTraverserView is created and added to layoutEditor in setUp()
        // Verify it's in the editor's contents
        Assertions.assertTrue(layoutEditor.getContents().contains(layoutTraverserView),
            "LayoutTraverserView should be present in LayoutEditor's contents.");

        // Optionally, verify it's a visible component on the frame using Jemmy
        // This requires the LayoutEditor to be visible, which is handled in setUp()
        JFrameOperator editorFrameOperator = new JFrameOperator(layoutEditor.getTitle());
        new ComponentOperator(editorFrameOperator, layoutTraverserView.getClass());
        // If the above line doesn't throw an exception, it means Jemmy found the component.
        // We don't need a specific assertion here, as a ComponentNotFoundException would fail the test.
    }

    private LayoutTraverserView layoutTraverserView = null;
    private LayoutTraverser layoutTraverser = null;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        Point2D point = new Point2D.Double(300.0, 100.0);
        
        layoutTraverser = new LayoutTraverser("Traverser", layoutEditor);
        layoutTraverserView = new LayoutTraverserView(layoutTraverser, point, layoutEditor);
        layoutEditor.addLayoutTrack(layoutTraverser, layoutTraverserView);
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (layoutTraverser != null) {
            layoutTraverser.remove();
        }
        layoutTraverser = null;
        super.tearDown();
    }
}
