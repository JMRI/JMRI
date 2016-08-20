package jmri.jmrit.beantable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Swing jfcUnit tests for the sensor table
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 */
public class SensorTableWindowTest extends jmri.util.SwingTestCase {

    public void testShowAndClose() throws Exception {

        // ask for the window to open
        SensorTableAction a = new SensorTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find new table window by name
        JmriJFrame ft = JmriJFrame.getFrame(Bundle.getMessage("TitleSensorTable"));
        flushAWT();

        // Find the add button
        AbstractButtonFinder abfinder = new AbstractButtonFinder("Add...");
        JButton button = (JButton) abfinder.find(ft, 0);
        Assert.assertNotNull(button);

        // Click button to open add window
        getHelper().enterClickAndLeave(new MouseEventData(this, button));

        // Find add window by name
        JmriJFrame fa = JmriJFrame.getFrame("Add New Sensor");
        Assert.assertNotNull("add window", fa);

        // Find system name field
        NamedComponentFinder ncfinder = new NamedComponentFinder(JComponent.class, "sysName");
        JTextField sysNameField = (JTextField) ncfinder.find(fa, 0);
        Assert.assertNotNull("sys name field", sysNameField);

        // set to "1"
        
        // The following line works on the CI servers, but not in some standalone cases
        //getHelper().sendString(new StringEventData(this, sysNameField, "1"));
        sysNameField.setText("1"); // workaround
        
        flushAWT();
        Assert.assertEquals("name content", "1", sysNameField.getText());
        
        // Find system combobox
        ncfinder = new NamedComponentFinder(JComponent.class, "prefixBox");
        JComboBox<?> prefixBox = (JComboBox<?>) ncfinder.find(fa, 0);
        Assert.assertNotNull(prefixBox);
        // set to "Internal"
        prefixBox.setSelectedItem("Internal");
        Assert.assertEquals("Selected system item", "Internal", prefixBox.getSelectedItem());

        // Find the OK button
        abfinder = new AbstractButtonFinder("OK");
        button = (JButton) abfinder.find(fa, 0);
        Assert.assertNotNull(button);

        // Click button to add sensor
        getHelper().enterClickAndLeave(new MouseEventData(this, button));

        // Ask to close add window
        TestHelper.disposeWindow(fa, this);

        // Ask to close table window
        TestHelper.disposeWindow(ft, this);

        flushAWT();
        
        // check for existing sensor
        Assert.assertNotNull(jmri.InstanceManager.sensorManagerInstance().getSensor("IS1"));
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
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
