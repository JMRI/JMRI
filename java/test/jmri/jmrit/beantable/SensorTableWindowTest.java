package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Swing tests for the sensor table.
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 * @author	Egbert Broerse Copyright 2018
 */
public class SensorTableWindowTest {

    @Test
    public void testShowAndClose() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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
        Assert.assertNotNull("add window", fa);

        // Find hardware address field
        JTextField hwAddressField = JTextFieldOperator.findJTextField(fa, new NameComponentChooser("hwAddressTextField"));
        Assert.assertNotNull("hwAddressTextField", hwAddressField);

        // set to "1"
        new JTextFieldOperator(hwAddressField).enterText("1");
        JButton createButton = JButtonOperator.findJButton(fa, new NameComponentChooser("createButton"));
        createButton.setEnabled(true); // skip validation

        new QueueTool().waitEmpty();
        Assert.assertEquals("name content", "1", hwAddressField.getText());

        // Find system combobox
        JComboBox<?> prefixBox = JComboBoxOperator.findJComboBox(fa, new NameComponentChooser("prefixBox"));
        Assert.assertNotNull(prefixBox);
        // set to "Internal"
        prefixBox.setSelectedItem("Internal");
        SensorManager internal = InstanceManager.getDefault(InternalSystemConnectionMemo.class).getSensorManager();
        Assert.assertEquals("Selected system item", internal, prefixBox.getSelectedItem());

        // Find and click the Add Create button to add sensor
        JUnitUtil.pressButton(fa, Bundle.getMessage("ButtonCreate"));
        // Ask to close Add pane
        new JFrameOperator(fa).dispose();

        // don't test edit pane, identical to create pane
        // Ask to close sensor table window
        new JFrameOperator(ft).dispose();

        new QueueTool().waitEmpty();

        // check for existing sensor
        Assert.assertNotNull(jmri.InstanceManager.sensorManagerInstance().getSensor("IS1"));
    }

    @Test
    public void testMenus() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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
        Assert.assertNotNull("Not found Global Debounce dialog", dialog);
        // Find the cancel button
        JUnitUtil.pressButton(dialog, Bundle.getMessage("ButtonCancel"));

        // ask for the Initial Sensor State menu to open
        ThreadingUtil.runOnGUIEventually(() -> {
            a.setDefaultState(null);
        });
        new QueueTool().waitEmpty();
        // Find new dialog window by name
        dialog = JUnitUtil.findContainer(Bundle.getMessage("InitialSensorState"));
        Assert.assertNotNull("Not found Global Debounce dialog", dialog);
        // Find the cancel button
        JUnitUtil.pressButton(dialog, Bundle.getMessage("ButtonCancel"));

        // Ask to close table window
        new JFrameOperator(ft).dispose();

        new QueueTool().waitEmpty();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
