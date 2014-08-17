// AddSignalMastPanelTest.java

package jmri.jmrit.beantable.signalmast;

import javax.swing.JFrame;

import junit.framework.*;

/**
 * @author	Bob Jacobsen  Copyright 2014
 * @version	$Revision$
 */
public class AddSignalMastPanelTest extends TestCase {

    public void testCreate() {
    }

    // from here down is testing infrastructure

    public AddSignalMastPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AddSignalMastPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AddSignalMastPanelTest.class);

        return suite;
    }
    
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    
}
