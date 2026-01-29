package jmri.jmrit.beantable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Swing tests for the sensor table.
 *
 * @author Bob Jacobsen Copyright 2009, 2010
 * @author Egbert Broerse Copyright 2018
 */
public class SensorTableWindowTest {

    @Test
    @DisabledIfHeadless
    public void testShowAndClose() {

        // ask for the window to open
        SensorTableAction a = new SensorTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find new table window by name
        JmriJFrame ft = JmriJFrame.getFrame(Bundle.getMessage("TitleSensorTable"));
        new QueueTool().waitEmpty();

        // Find the add button and click it to open add window
        JUnitUtil.pressButton(ft, Bundle.getMessage("ButtonAdd"));

        // Find add window by name
        JmriJFrame fa = JmriJFrame.getFrame("Add New Sensor");
        assertNotNull( fa, "add window");

        // Find hardware address field
        JTextField hwAddressField = JTextFieldOperator.findJTextField(fa, new NameComponentChooser("hwAddressTextField"));
        assertNotNull( hwAddressField, "hwAddressTextField");

        // set to "1"
        new JTextFieldOperator(hwAddressField).typeText("1");
        JButton createButton = JButtonOperator.findJButton(fa, new NameComponentChooser("createButton"));
        createButton.setEnabled(true); // skip validation

        new QueueTool().waitEmpty();
        assertEquals( "1", hwAddressField.getText(), "name content");

        // Find system combobox
        JComboBox<?> prefixBox = JComboBoxOperator.findJComboBox(fa, new NameComponentChooser("prefixBox"));
        assertNotNull(prefixBox);
        // set to "Internal"
        prefixBox.setSelectedItem("Internal");
        SensorManager internal = InstanceManager.getDefault(InternalSystemConnectionMemo.class).getSensorManager();
        assertEquals( internal, prefixBox.getSelectedItem(), "Selected system item");

        // Find and click the Add Create button to add sensor
        JUnitUtil.pressButton(fa, Bundle.getMessage("ButtonCreate"));
        // Ask to close Add pane
        new JFrameOperator(fa).dispose();

        // don't test edit pane, identical to create pane
        // Ask to close sensor table window
        new JFrameOperator(ft).dispose();

        new QueueTool().waitEmpty();

        // check for existing sensor
        assertNotNull(jmri.InstanceManager.sensorManagerInstance().getSensor("IS1"));
    }

    @Test
    @DisabledIfHeadless
    public void testMenus() {

        SensorTableAction a = new SensorTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find sensor table window by name
        JmriJFrame ft = JmriJFrame.getFrame(Bundle.getMessage("TitleSensorTable"));

        // ask for the Debounce menu to open
        ThreadingUtil.runOnGUIEventually(() -> {
            a.setDefaultDebounce(null);
        });
        new QueueTool().waitEmpty();
        // Find new dialog window by name
        java.awt.Container dialog = JUnitUtil.findContainer(Bundle.getMessage("SensorGlobalDebounceMessageTitle"));
        assertNotNull( dialog, "Not found Global Debounce dialog");
        // Find the cancel button
        JUnitUtil.pressButton(dialog, Bundle.getMessage("ButtonCancel"));

        // ask for the Initial Sensor State menu to open
        ThreadingUtil.runOnGUIEventually(() -> {
            a.setDefaultState(null);
        });
        new QueueTool().waitEmpty();
        // Find new dialog window by name
        dialog = JUnitUtil.findContainer(Bundle.getMessage("InitialSensorState"));
        assertNotNull( dialog, "Not found Global Debounce dialog");
        // Find the cancel button
        JUnitUtil.pressButton(dialog, Bundle.getMessage("ButtonCancel"));

        // Ask to close table window
        new JFrameOperator(ft).dispose();

        new QueueTool().waitEmpty();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
