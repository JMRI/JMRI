// SwingTestCaseTest.java

package jmri.util;

import javax.swing.*;
import junit.framework.*;
import junit.extensions.jfcunit.*;
import junit.extensions.jfcunit.finder.*;
import junit.extensions.jfcunit.eventdata.*;

/**
 * Tests for the jmri.util.SwingTestCase class.
 * @author	Bob Jacobsen  Copyright 2009
 * @version	$Revision: 1.1 $
 */
public class SwingTestCaseTest extends SwingTestCase {

    public void testCheckBoxClosing() {
        // create a little GUI
        JFrame f = new JFrame("testCheckBoxClosing");
        JCheckBox b = new JCheckBox("Check");
        b.setName("Check");
        f.add(b);
        f.setVisible(true);
        // find the check box
        NamedComponentFinder finder = new NamedComponentFinder(JCheckBox.class, "Check" );
        JCheckBox testBox = ( JCheckBox ) finder.find( f, 0);
        Assert.assertNotNull(testBox);
        Assert.assertTrue(!testBox.isSelected());
        // check it
        getHelper().enterClickAndLeave( new MouseEventData( this, testBox ) );
        // test for selected
        Assert.assertTrue(testBox.isSelected());
    }
    
    public void testCheckBoxOpen() {
        // create a little GUI
        JFrame f = new JFrame("testCheckBoxOpen");
        JCheckBox b = new JCheckBox("Check");
        b.setName("Check");
        f.add(b);
        f.setVisible(true);
        // find the check box
        NamedComponentFinder finder = new NamedComponentFinder(JCheckBox.class, "Check" );
        JCheckBox testBox = ( JCheckBox ) finder.find( f, 0);
        Assert.assertNotNull(testBox);
        Assert.assertTrue(!testBox.isSelected());
        // check it
        getHelper().enterClickAndLeave( new MouseEventData( this, testBox ) );
        // test for selected
        Assert.assertTrue(testBox.isSelected());
        
        // request this window be left open
        getHelper().addSystemWindow("testCheckBoxOpen");
        
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

	//static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SwingTestCaseTest.class.getName());
}
