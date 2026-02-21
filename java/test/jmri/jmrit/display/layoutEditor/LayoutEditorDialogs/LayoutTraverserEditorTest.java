package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.geom.Point2D;

import javax.swing.*;
import jmri.SignalMastManager;
import jmri.SignalHeadManager;
import jmri.implementation.DefaultSignalHead;
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

        // Set Width
        JTextFieldOperator widthTxt = new JTextFieldOperator(jFrameOperator, 1);
        widthTxt.setText("60");

        // Set Slot Offset
        JTextFieldOperator slotOffsetTxt = new JTextFieldOperator(jFrameOperator, 2);
        slotOffsetTxt.setText("39");

        // Set orientation
        JComboBoxOperator orientationCbo = new JComboBoxOperator(jFrameOperator, 0);
        orientationCbo.selectItem(1); // Vertical
//        orientationCbo.selectItem(0); // Horizontal

        // Set mainline for traverser
        JComboBoxOperator mainlineCbo = new JComboBoxOperator(jFrameOperator, 2);
        mainlineCbo.selectItem(Bundle.getMessage("Mainline")); // Mainline
        //        mainlineCbo.selectItem(0); // Sideline

        // Add 2 slot pairs
        JButtonOperator addSlotButtonOperator = new JButtonOperator(
                jFrameOperator, Bundle.getMessage("AddSlotPair"));
        addSlotButtonOperator.doClick();
        addSlotButtonOperator.doClick();

        // Enable DCC control before setting turnouts
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("TraverserDCCControlled")).doClick();

        // Disable one side of the first slot pair
        JCheckBoxOperator disabledCheckBox = new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("Disabled"), 0);
        disabledCheckBox.doClick();

        // Enable Signal Masts before setting masts
        new JCheckBoxOperator(jFrameOperator, Bundle.getMessage("TraverserUseSignalMasts")).doClick();

        // Set Global Masts (Exit and Buffer are global for the traverser)
        // Indices:
        // 0-2: Initial (Orientation, Block, Mainline)
        // 3-6: Pair 0 (Turnout A, State A, Turnout B, State B)
        // 7-10: Pair 1 (Turnout A, State A, Turnout B, State B)
        // 11-14: Approach Masts (Slot 0, 1, 2, 3)
        JComboBoxOperator exitCbo = new JComboBoxOperator(jFrameOperator, 15);
        selectItemSafe(exitCbo, "Mast SA-Exit");  // instead of exitCbo.selectItem("Mast SA-Exit") as there is a null in the first item
        // 15: Exit Mast
        // 16: Buffer Mast

        JComboBoxOperator bufferCbo = new JComboBoxOperator(jFrameOperator, 16);
        selectItemSafe(bufferCbo, "Mast SA-Buff");

        // Set turnouts and approach masts for active slots
        // Indices are based on the order components are added to the panel.
        // Initial cbos: 0=Orientation, 1=BlockName, 2=Mainline 2=ExitMast, 3=BufferMast
        // Each slot adds 3 cbos: Turnout, State, Approach
        // Slot 1A: cbos 3-4. Slot 1B: cbos 5-6. Slot 2A: cbos 7-8. Slot 2B: cbos 9-10.
        // Mast 1A: 11, Mast 1B: 12, Mast 2A: 13, Mast 2B: 14

        // Slot 1B (second slot of first pair) is active
        new JComboBoxOperator(jFrameOperator, 5).selectItem(2); // Turnout (LT2)
        new JComboBoxOperator(jFrameOperator, 6).selectItem(0); // State (Closed)
        JComboBoxOperator jFrameOperator1B = new JComboBoxOperator(jFrameOperator, 12);
        selectItemSafe(jFrameOperator1B, "Mast App-1B"); // Approach Mast

        // Slot 2A (first slot of second pair) is active
        new JComboBoxOperator(jFrameOperator, 7).selectItem(3); // Turnout (LT3)
        new JComboBoxOperator(jFrameOperator, 8).selectItem(1); // State (Thrown)
        JComboBoxOperator jFrameOperator2A = new JComboBoxOperator(jFrameOperator, 13);
        selectItemSafe(jFrameOperator2A, "Mast App-2A"); // Approach Mast

        // Slot 2B (second slot of second pair) is active
        new JComboBoxOperator(jFrameOperator, 9).selectItem(4); // Turnout (LT4)
        new JComboBoxOperator(jFrameOperator, 10).selectItem(0); // State (Closed)
        JComboBoxOperator jFrameOperator2B = new JComboBoxOperator(jFrameOperator, 14);
        selectItemSafe(jFrameOperator2B, "Mast App-2B"); // Approach Mast

        // Test invalid width validation on "Done"
        Thread badWidthModalDialogOperatorThread = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));
        widthTxt.setText("qqq");
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        JUnitUtil.waitFor(() -> !badWidthModalDialogOperatorThread.isAlive(), "badWidth finished");

        // Put a good value back in and finish
        widthTxt.setText("60");
        slotOffsetTxt.setText("15");
        slotOffsetTxt.pressKey(java.awt.event.KeyEvent.VK_TAB); // Trigger FocusListener
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();    // make sure the dialog actually closed

        // --- VERIFY ---
        Assertions.assertEquals(60.0, layoutTraverser.getDeckWidth(), 0.01, "Check width");
        Assertions.assertEquals(15.0, layoutTraverser.getSlotOffset(), 0.01, "Check slot offset");
        Assertions.assertEquals(LayoutTraverser.VERTICAL, layoutTraverser.getOrientation(), "Check orientation");
        Assertions.assertTrue(layoutTraverser.isMainline(), "Check mainline is true");
        Assertions.assertEquals("Mast SA-Exit", layoutTraverser.getExitSignalMastName(), "Check Global Exit Mast");
        Assertions.assertEquals("Mast SA-Buff", layoutTraverser.getBufferSignalMastName(), "Check Global Buffer Mast");

        Assertions.assertEquals(4, layoutTraverser.getSlotList().size(), "Should be 4 slots (2 pairs)");
        Assertions.assertTrue(layoutTraverser.getSlotList().get(0).isDisabled(), "Slot 0 (1A) should be disabled");
        Assertions.assertFalse(layoutTraverser.getSlotList().get(1).isDisabled(), "Slot 1 (1B) should be enabled");
        Assertions.assertFalse(layoutTraverser.getSlotList().get(2).isDisabled(), "Slot 2 (2A) should be enabled");
        Assertions.assertFalse(layoutTraverser.getSlotList().get(3).isDisabled(), "Slot 3 (2B) should be enabled");

        // Assert Slot 1B
        LayoutTraverser.SlotTrack slot1B = layoutTraverser.getSlotList().get(1);
        Assertions.assertEquals("Turnout 102", slot1B.getTurnoutName(), "Slot 1B turnout name");
        Assertions.assertEquals(jmri.Turnout.CLOSED, slot1B.getTurnoutState(), "Slot 1B turnout state");
        Assertions.assertEquals("Mast App-1B", slot1B.getApproachMast().getUserName(), "Slot 1B approach mast");

        // Assert Slot 2A
        LayoutTraverser.SlotTrack slot2A = layoutTraverser.getSlotList().get(2);
        Assertions.assertEquals("Turnout 103", slot2A.getTurnoutName(), "Slot 2A turnout name");
        Assertions.assertEquals(jmri.Turnout.THROWN, slot2A.getTurnoutState(), "Slot 2A turnout state");
        Assertions.assertEquals("Mast App-2A", slot2A.getApproachMast().getUserName(), "Slot 2A approach mast");

        // Assert Slot 2B
        LayoutTraverser.SlotTrack slot2B = layoutTraverser.getSlotList().get(3);
        Assertions.assertEquals("Turnout 104", slot2B.getTurnoutName(), "Slot 2B turnout name");
        Assertions.assertEquals(jmri.Turnout.CLOSED, slot2B.getTurnoutState(), "Slot 2B turnout state");
        Assertions.assertEquals("Mast App-2B", slot2B.getApproachMast().getUserName(), "Slot 2B approach mast");
    }

    private void selectItemSafe(JComboBoxOperator cbo, String name) {
        // Get the actual JComboBox component from the operator
        JComboBox<?> cb = (JComboBox<?>) cbo.getSource();

        // Iterate through all items in the combo box
        for (int i = 0; i < cb.getItemCount(); i++) {
            Object item = cb.getItemAt(i);

            // Skip null items to avoid NullPointerException
            if (item != null) {
                String displayName = "";

                // If the item is a NamedBean (common in JMRI), use its display name
                if (item instanceof jmri.NamedBean) {
                    displayName = ((jmri.NamedBean) item).getDisplayName();
                } else {
                    // Otherwise, use toString()
                    displayName = item.toString();
                }

                // Check if the name matches
                if (name.equals(displayName)) {
                    // Found it! Select by index using the operator
                    cbo.selectItem(i);
                    return;
                }
            }
        }
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
    public void testEditTraverserClose() {
        LayoutTraverserEditor editor = new LayoutTraverserEditor(layoutEditor);
        editor.editLayoutTrack(layoutTraverserView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTraverser"));
        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();
        jFrameOperator.waitClosed();
    }

    @Test
    public void testEditTraverserErrors() {
        LayoutTraverserEditor editor = new LayoutTraverserEditor(layoutEditor);
        editor.editLayoutTrack(layoutTraverserView);
        JFrameOperator jFrameOperator = new JFrameOperator(Bundle.getMessage("EditTraverser"));

        // Set invalid width
        JTextFieldOperator widthTxt = new JTextFieldOperator(jFrameOperator, 1);
        widthTxt.setText("xyz");

        Thread badWidth = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("ErrorTitle"),
                Bundle.getMessage("ButtonOK"));

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonDone")).doClick();

        JUnitUtil.waitFor(() -> !badWidth.isAlive(), "badWidth finished");

        new JButtonOperator(jFrameOperator, Bundle.getMessage("ButtonCancel")).doClick();
        jFrameOperator.waitClosed();
    }

    private LayoutTraverserView layoutTraverserView = null;
    private LayoutTraverser layoutTraverser = null;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        createSignalHeads();
        createSignalMasts();

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

    private void createSignalHeads() {
        SignalHeadManager shm = jmri.InstanceManager.getDefault(SignalHeadManager.class);
        shm.register(new DefaultSignalHead("IH1") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH10") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH2") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH20") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH3") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH30") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH4") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH40") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH5") {
            @Override
            protected void updateOutput() {
            }
        });
        shm.register(new DefaultSignalHead("IH50") {
            @Override
            protected void updateOutput() {
            }
        });
    }

    private void createSignalMasts() {
        SignalMastManager smm = jmri.InstanceManager.getDefault(SignalMastManager.class);
        jmri.SignalMast mast1 = smm.provideSignalMast("IF$shsm:basic:two-searchlight:IH1:IH10");
        mast1.setUserName("Mast App-1B");
        jmri.SignalMast mast2 = smm.provideSignalMast("IF$shsm:basic:two-searchlight:IH2:IH20");
        mast2.setUserName("Mast App-2A");
        jmri.SignalMast mast3 = smm.provideSignalMast("IF$shsm:basic:two-searchlight:IH3:IH30");
        mast3.setUserName("Mast App-2B");
        jmri.SignalMast mast4 = smm.provideSignalMast("IF$shsm:basic:two-searchlight:IH4:IH40");
        mast4.setUserName("Mast SA-Exit");
        jmri.SignalMast mast5 = smm.provideSignalMast("IF$shsm:basic:two-searchlight:IH5:IH50");
        mast5.setUserName("Mast SA-Buff");
    }
}
