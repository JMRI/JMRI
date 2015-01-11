// TurnoutTableWindowTest.java

package jmri.jmrit.beantable;

import javax.swing.*;

import jmri.util.JmriJFrame;

import junit.framework.*;
import junit.extensions.jfcunit.*;
import junit.extensions.jfcunit.finder.*;
import junit.extensions.jfcunit.eventdata.*;

/**
 * Swing jfcUnit tests for the turnout table
 * @author			Bob Jacobsen  Copyright 2009, 2010
 * @version         $Revision$
 */
public class TurnoutTableWindowTest extends jmri.util.SwingTestCase {

	public void testShowAndClose() throws Exception {

        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        //jmri.util.JUnitAppender.assertWarnMessage("Won't protect preferences at shutdown without registered ShutDownManager");
        
        TurnoutTableAction a = new TurnoutTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));
        
        // Find new table window by name
        JmriJFrame ft = JmriJFrame.getFrame("Turnout Table");
        
        // Find the add button
        AbstractButtonFinder abfinder = new AbstractButtonFinder("Add..." );
        JButton button = ( JButton ) abfinder.find( ft, 0);
        Assert.assertNotNull(button);   

        // Click button to open add window
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );

        // Find add window by name
        JmriJFrame fa = JmriJFrame.getFrame("Add New Turnout");
        
        // Find hardware number field
        NamedComponentFinder ncfinder = new NamedComponentFinder(JComponent.class, "sysName" );
        JTextField sysNameField = (JTextField) ncfinder.find(fa, 0);
        Assert.assertNotNull(sysNameField);
        // set to "1"
        getHelper().sendString( new StringEventData( this, sysNameField, "1" ) );
        
        // Find system combobox
        ncfinder = new NamedComponentFinder(JComponent.class, "prefixBox" );
        JComboBox prefixBox = (JComboBox) ncfinder.find(fa, 0);
        Assert.assertNotNull(prefixBox);
        // set to "Internal"
        prefixBox.setSelectedItem("Internal");
        
        // Find the OK button
        abfinder = new AbstractButtonFinder("OK" );
        button = ( JButton ) abfinder.find( fa, 0);
        Assert.assertNotNull(button);   

        // Click button to add sensor
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );
        
        // check for existing sensor
        Assert.assertNotNull(jmri.InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        
        // Ask to close add window
        TestHelper.disposeWindow(fa, this);

        // Ask to close table window
        TestHelper.disposeWindow(ft, this);
        
	}

    
	// from here down is testing infrastructure
	public TurnoutTableWindowTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", TurnoutTableWindowTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(TurnoutTableWindowTest.class);  
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
