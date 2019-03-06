package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.SwingTestCase;
import jmri.util.ThreadingUtil;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Swing jfcUnit tests for the sensor table. Do not convert to JUnit4 (no
 * support for enterClickAndLeave() etc.)
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 * @author	Egbert Broerse Copyright 2018
 */
public class SensorTableWindowTest extends SwingTestCase {

    public void testShowAndClose() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }

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
        Assert.assertEquals("Selected system item", "Internal", prefixBox.getSelectedItem());

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

    public void testMenus() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }

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

    // from here down is testing infrastructure
    public SensorTableWindowTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SensorTableWindowTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SensorTableWindowTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.tearDown();
        super.tearDown();
    }

}
