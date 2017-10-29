package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Turnout;
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
 * Swing jfcUnit tests for the turnout table. Do not convert to JUnit4 (no
 * support for enterClickAndLeave() etc.)
 *
 * @author	Bob Jacobsen Copyright 2009, 2010, 2017
 */
public class TurnoutTableWindowTest extends jmri.util.SwingTestCase {

    public void testShowAndClose() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }

        // ask for the window to open
        TurnoutTableAction a = new TurnoutTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find new table window by name
        JmriJFrame ft = JmriJFrame.getFrame(Bundle.getMessage("TitleTurnoutTable"));
        flushAWT();

        // Find the Automatic retry checkbox
        AbstractButtonFinder arfinder = new AbstractButtonFinder(Bundle.getMessage("AutomaticRetry"));
        JCheckBox ar = (JCheckBox) arfinder.find(ft, 0);
        Assert.assertNotNull(ar);
        // Click checkbox to select Automatic retry
        getHelper().enterClickAndLeave(new MouseEventData(this, ar));
        Assert.assertNotNull("AR selected", ar.getSelectedObjects());

        // Find the Show Feedback information checkbox
        AbstractButtonFinder fbfinder = new AbstractButtonFinder(Bundle.getMessage("ShowFeedbackInfo"));
        JCheckBox fb = (JCheckBox) fbfinder.find(ft, 0);
        Assert.assertNotNull(fb);
        // Click checkbox to select Show feedback information
        getHelper().enterClickAndLeave(new MouseEventData(this, fb));
        Assert.assertNotNull("FBbox selected", fb.getSelectedObjects());

        // Find the Show Lock information checkbox
        AbstractButtonFinder lkfinder = new AbstractButtonFinder(Bundle.getMessage("ShowLockInfo"));
        JCheckBox lk = (JCheckBox) lkfinder.find(ft, 0);
        Assert.assertNotNull(lk);
        // Click checkbox to select Show feedback information
        getHelper().enterClickAndLeave(new MouseEventData(this, lk));
        Assert.assertNotNull("LKbox selected", lk.getSelectedObjects());

        // Find the Show Turnout Speed details checkbox
        AbstractButtonFinder tsfinder = new AbstractButtonFinder(Bundle.getMessage("ShowTurnoutSpeedDetails"));
        JCheckBox ts = (JCheckBox) tsfinder.find(ft, 0);
        Assert.assertNotNull(ts);
        // Click checkbox to select Show feedback information
        getHelper().enterClickAndLeave(new MouseEventData(this, ts));
        Assert.assertNotNull("TSbox selected", ts.getSelectedObjects());

        // Find the Add... button
        AbstractButtonFinder abfinder = new AbstractButtonFinder(Bundle.getMessage("ButtonAdd"));
        JButton ad = (JButton) abfinder.find(ft, 0);
        Assert.assertNotNull(ad);

        // Click button to open Add Turnout pane
        getHelper().enterClickAndLeave(new MouseEventData(this, ad));

        // Find Add Turnout pane by name
        JmriJFrame fa = JmriJFrame.getFrame(Bundle.getMessage("TitleAddTurnout"));
        Assert.assertNotNull("Add window found", fa);

        // Find hardware address field
        NamedComponentFinder ncfinder = new NamedComponentFinder(JComponent.class, "hwAddressTextField");
        JTextField hwAddressField = (JTextField) ncfinder.find(fa, 0);
        Assert.assertNotNull("hwAddressTextField", hwAddressField);

        // set to "C/MRI"
        a.connectionChoice = "C/MRI";
        // set address to "a" (invalid for C/MRI)
        hwAddressField.setText("a");
        // test silent entry validation
        boolean _valid = hwAddressField.isValid();
        Assert.assertEquals("invalid entry", false, _valid);

        // set address to "1"
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

        // Open the Edit Turnout IT1 pane, how to find & click the LT1 Edit col button?
        // Find the Edit button in EDITCOL of line 0 (for LT1)
        //AbstractButtonFinder edfinder = new AbstractButtonFinder("Edit");
        //JButton editbutton = (JButton) edfinder.find(ft, 0);
        //Assert.assertNotNull(editbutton);
        // Click button to edit turnout
        //getHelper().enterClickAndLeave(new MouseEventData(this, editbutton));
        // open Edit pane by method instead
        Turnout it1 = InstanceManager.turnoutManagerInstance().getTurnout("IT1");
        a.editButton(it1); // open edit pane

        // Find Edit Turnout pane by name
        JmriJFrame fe = JmriJFrame.getFrame("Edit Turnout IT1");
        Assert.assertNotNull("Edit window", fe);
        // Find the Edit Cancel button
        AbstractButtonFinder canceleditfinder = new AbstractButtonFinder(Bundle.getMessage("ButtonCancel"));
        JButton cancelbutton = (JButton) canceleditfinder.find(fe, 0);
        Assert.assertNotNull(cancelbutton);
        // Click button to cancel edit turnout
        getHelper().enterClickAndLeave(new MouseEventData(this, cancelbutton));
        // Ask to close Edit pane
        TestHelper.disposeWindow(fe, this);

        // Ask to close turnout table window
        TestHelper.disposeWindow(ft, this);

        flushAWT();

        // check that turnout was created
        Assert.assertNotNull(jmri.InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
    }

    // from here down is testing infrastructure
    public TurnoutTableWindowTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TurnoutTableWindowTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TurnoutTableWindowTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.tearDown();
        super.tearDown();
    }

}
