// MultiJfcUnitTest.java

package jmri.util.swing.multipane;

import javax.swing.*;
import jmri.util.swing.*;
import junit.extensions.jfcunit.*;
import junit.extensions.jfcunit.eventdata.*;
import junit.extensions.jfcunit.finder.*;
import junit.framework.*;

/**
 * Swing jfcUnit tests for the Multipane (IDE) GUI 
 * @author			Bob Jacobsen  Copyright 2010
 * @version         $Revision$
 */
public class MultiJfcUnitTest extends jmri.util.SwingTestCase {

    public void testShow() throws Exception {
        // show the window
        JFrame f1 = new MultiPaneWindow("test",
                "java/test/jmri/util/swing/xml/Gui3LeftTree.xml", 
    	        "java/test/jmri/util/swing/xml/Gui3Menus.xml", 
    	        "java/test/jmri/util/swing/xml/Gui3MainToolBar.xml"
        );
        f1.setSize(new java.awt.Dimension(500,500));
        f1.setVisible(true);
        
        Assert.assertNotNull("found main frame", f1);
        
        // Find the button that loads the license
        AbstractButtonFinder finder = new AbstractButtonFinder("License" );
        JButton button = ( JButton ) finder.find( f1, 0);
        Assert.assertNotNull("License button found", button);   
        
        // Click it to load license
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );
        
        // Find the button that opens a sample panel
        AbstractButtonFinder samplefinder = new AbstractButtonFinder("Sample" );
        JButton samplebutton = ( JButton ) samplefinder.find( f1, 0);
        Assert.assertNotNull("Sample button found", samplebutton);   
        
        // Click it to load new pane over license
        getHelper().enterClickAndLeave( new MouseEventData( this, samplebutton ) );

        // Find the button on new panel
        finder = new AbstractButtonFinder("Next1" );
        JButton next1button = ( JButton ) finder.find( f1, 0);
        Assert.assertNotNull("Next1 button found", next1button);   

        // Click it to load new window with Next2
        getHelper().enterClickAndLeave( new MouseEventData( this, next1button ) );
        
        // nobody disposed yet
        Assert.assertEquals("no panes disposed", 0, SamplePane.disposed.size() );
        
        // Find the Next2 button on new panel
        finder = new AbstractButtonFinder("Next2" );
        JButton next2button = ( JButton ) finder.find( f1, 0);
        Assert.assertNotNull("Next2 button found", next2button);   

        // Click sample to reload 0 pane over 1 pane
        getHelper().enterClickAndLeave( new MouseEventData( this, samplebutton ) );
        
        // Find the button on restored panel
        finder = new AbstractButtonFinder("Next1" );
        button = ( JButton ) finder.find( f1, 0);
        Assert.assertEquals("found same pane", next1button, button);   

        // Find the button to open a pane in lower window
        finder = new AbstractButtonFinder("Extend1" );
        JButton extendButton = ( JButton ) finder.find( f1, 0);
        Assert.assertNotNull("Extend1 button found", extendButton);   
        // Press it
        getHelper().enterClickAndLeave( new MouseEventData( this, extendButton ) );
        
        // Both Close1 and Close3 should be present
        finder = new AbstractButtonFinder("Close1" );
        button = ( JButton ) finder.find( f1, 0);
        Assert.assertNotNull("Closee1 button found", button);   
        finder = new AbstractButtonFinder("Close3" );
        button = ( JButton ) finder.find( f1, 0);
        Assert.assertNotNull("Close3 button found", button);   
        
        // nobody disposed yet
        Assert.assertEquals("no panes disposed", 0, SamplePane.disposed.size() );

        // Close entire frame directly
        TestHelper.disposeWindow(f1, this);

        // Now they're disposed
        Assert.assertEquals("panes disposed", 3, SamplePane.disposed.size() );

    }
            
	// from here down is testing infrastructure
	public MultiJfcUnitTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {MultiJfcUnitTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(MultiJfcUnitTest.class);  
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        //jmri.util.JUnitUtil.initInternalTurnoutManager();
        //jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.swing.SamplePane.disposed = new java.util.ArrayList<Integer>();
        jmri.util.swing.SamplePane.index = 0;
    }
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
