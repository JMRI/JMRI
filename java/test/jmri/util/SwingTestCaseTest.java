// SwingTestCaseTest.java
package jmri.util;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.SwingTestCase class.
 *
 * @author	Bob Jacobsen Copyright 2009
 * @version	$Revision$
 */
public class SwingTestCaseTest extends SwingTestCase {

    /**
     * Simple test of creating a Swing frame with a checkbox, checking the box,
     * and seeing that the check changed its state.
     * <p>
     * In this case, the frame is left open after the test completes because
     * this is the JMRI default.
     */
    public void testCheckBox() {
        // create a little GUI with a single check box
        JFrame f = new JFrame("SwingTextCaseTest");
        f.setSize(100, 100);	        // checkbox must be visible for test to work
        JCheckBox b = new JCheckBox("Check");
        b.setName("Check");
        f.add(b);
        f.setVisible(true);

        // find the check box and confirm not yet checked
        NamedComponentFinder finder = new NamedComponentFinder(JCheckBox.class, "Check");
        JCheckBox testBox = (JCheckBox) finder.find(f, 0);
        Assert.assertNotNull(testBox);
        Assert.assertTrue(!testBox.isSelected());

        // set the check in the box by clicking it
        getHelper().enterClickAndLeave(new MouseEventData(this, testBox));

        // test for selected
        Assert.assertTrue(testBox.isSelected());

        f.dispose();
    }

    // from here down is testing infrastructure
    public SwingTestCaseTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SwingTestCaseTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        Test suite = new TestSuite(SwingTestCaseTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        //apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        //apps.tests.Log4JFixture.tearDown();
    }

	//static Logger log = LoggerFactory.getLogger(SwingTestCaseTest.class.getName());
}
