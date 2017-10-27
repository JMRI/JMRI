package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Swing jfcUnit tests for the light table. Do not convert to JUnit4 (no support
 * for enterClickAndLeave() etc.)
 *
 * @author	Bob Jacobsen Copyright 2009, 2010, 2017
 */
public class LightTableWindowTest extends jmri.util.SwingTestCase {

    public void testShowAndClose() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }

        // ask for the window to open
        LightTableAction a = new LightTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find new table window by name
        JmriJFrame ft = JmriJFrame.getFrame(Bundle.getMessage("TitleLightTable"));
        flushAWT();

        // Find the Add... button
        AbstractButtonFinder abfinder = new AbstractButtonFinder(Bundle.getMessage("ButtonAdd"));
        JButton ad = (JButton) abfinder.find(ft, 0);
        Assert.assertNotNull(ad);

        // Click button to open Add Light pane
        getHelper().enterClickAndLeave(new MouseEventData(this, ad));

        // Find Add Light pane by name
        JmriJFrame fa = JmriJFrame.getFrame(Bundle.getMessage("TitleAddLight"));
        Assert.assertNotNull("Add window found", fa);

        // Find hardware address field
        NamedComponentFinder ncfinder = new NamedComponentFinder(JComponent.class, "hwAddressTextField");
        JTextField hwAddressField = (JTextField) ncfinder.find(fa, 0);
        Assert.assertNotNull("hwAddressTextField", hwAddressField);

        // set to "1"
        // The following line works on the CI servers, but not in some standalone cases
        //getHelper().sendString(new StringEventData(this, hwAddressField, "1"));
        hwAddressField.setText("1"); // workaround
        NamedComponentFinder ncfinder2 = new NamedComponentFinder(JComponent.class, "createButton");
        JButton createButton = (JButton) ncfinder2.find(fa, 0);
        createButton.setEnabled(true); // skip validation

        flushAWT();
        Assert.assertEquals("name content", "1", hwAddressField.getText());

        // Find system combobox
        ncfinder = new NamedComponentFinder(JComponent.class, "prefixBox");
        JComboBox<?> prefixBox = (JComboBox<?>) ncfinder.find(fa, 0);
        Assert.assertNotNull(prefixBox);
        // set to "Internal"
        prefixBox.setSelectedItem("Internal");
        Assert.assertEquals("Selected system item", "Internal", prefixBox.getSelectedItem()); // this connection type is always available

        // Find the Add Create button
        AbstractButtonFinder createfinder = new AbstractButtonFinder(Bundle.getMessage("ButtonCreate"));
        JButton createbutton = (JButton) createfinder.find(fa, 0);
        Assert.assertNotNull(createbutton);
        // Click button to add turnout
        getHelper().enterClickAndLeave(new MouseEventData(this, createbutton));
        // Ask to close Add pane
        TestHelper.disposeWindow(fa, this);

        // don't test edit pane, identical to create pane
        // Ask to close turnout table window
        TestHelper.disposeWindow(ft, this);

        flushAWT();

        // check that light was created
        Assert.assertNotNull(jmri.InstanceManager.lightManagerInstance().getLight("IL1"));
    }

    // from here down is testing infrastructure
    public LightTableWindowTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LightTableWindowTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LightTableWindowTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.tearDown();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        JUnitUtil.tearDown();
    }
}
