package jmri.jmrit.display.layoutEditor;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;

/**
 * Swing jfcUnit tests for the LayoutEditor
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 */
public class LayoutEditorWindowTest extends jmri.util.SwingTestCase {

    @SuppressWarnings("unchecked")
    public void testShowAndClose() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        // load and display sample file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/SimpleLayoutEditorTest.xml");
        cm.load(f);
        sleep(100); // time for internal listeners to calm down

        // Find new window by name (should be more distinctive, comes from sample file)
        LayoutEditor le = (LayoutEditor) jmri.util.JmriJFrame.getFrame("My Layout");

        // It's up at this point, and can be manipulated
        // Ask to close window
        TestHelper.disposeWindow(le, this);

        // Dialog has popped up, so handle that. First, locate it.
        List<JDialog> dialogList = new DialogFinder(null).findAll(le);
        JDialog d = dialogList.get(0);

        // Find the button that deletes the panel
        AbstractButtonFinder finder = new AbstractButtonFinder("Delete Panel");
        JButton button = (JButton) finder.find(d, 0);
        Assert.assertNotNull(button);

        // Click button to delete panel and close window
        getHelper().enterClickAndLeave(new MouseEventData(this, button));

        // another dialog has popped up, so handle that by finding the "Yes - Delete" button.
        dialogList = new DialogFinder(null).findAll(le);
        d = dialogList.get(0);
        finder = new AbstractButtonFinder("Yes - Delete");
        button = (JButton) finder.find(d, 0);
        Assert.assertNotNull(button);

        // Click to say yes, I really mean it.
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
    }

    // from here down is testing infrastructure
    public LayoutEditorWindowTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LayoutEditorWindowTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutEditorWindowTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.instance().dispose();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.instance().dispose();
        JUnitUtil.resetInstanceManager();
        super.tearDown();
    }
}
