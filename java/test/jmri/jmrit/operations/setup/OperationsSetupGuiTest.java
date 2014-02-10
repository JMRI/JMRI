//OperationsSetupGuiTest.java

package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.jfcunit.eventdata.*;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import jmri.jmrit.display.LocoIcon;
import jmri.util.JmriJFrame;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Tests for the Operations Setup GUI class
 *  
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class OperationsSetupGuiTest extends jmri.util.SwingTestCase {
	
	public void testDirectionCheckBoxes(){
		OperationsSetupFrame f = new OperationsSetupFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();
				
		//both east/west and north/south checkboxes should be set	
		Assert.assertTrue("North selected", f.northCheckBox.isSelected());
		Assert.assertTrue("East selected", f.eastCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.northCheckBox ) );	
		Assert.assertFalse("North deselected", f.northCheckBox.isSelected());
		Assert.assertTrue("East selected", f.eastCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );		
		Assert.assertTrue("North selected", f.northCheckBox.isSelected());
		Assert.assertFalse("East deselected", f.eastCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );
		Assert.assertTrue("North selected", f.northCheckBox.isSelected());
		Assert.assertTrue("East selected", f.eastCheckBox.isSelected());
		
		//done
		f.dispose();
	}
	
	public void testSetupFrameWrite(){
		// force creation of backup
		Setup.setCarTypes(Setup.AAR);
		
		OperationsSetupFrame f = new OperationsSetupFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();
		
		f.railroadNameTextField.setText("Test Railroad Name");
		f.maxLengthTextField.setText("1234");
		f.maxEngineSizeTextField.setText("6");
		f.switchTimeTextField.setText("3");
		f.travelTimeTextField.setText("4");
		//f.ownerTextField.setText("Bob J");
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.scaleHO ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.typeDesc ) );
		
		f.panelTextField.setText("Test Panel Name");
		
		f.eastComboBox.setSelectedItem(LocoIcon.RED);
		f.westComboBox.setSelectedItem(LocoIcon.BLUE);
		f.northComboBox.setSelectedItem(LocoIcon.WHITE);
		f.southComboBox.setSelectedItem(LocoIcon.GREEN);
		f.terminateComboBox.setSelectedItem(LocoIcon.GRAY);
		f.localComboBox.setSelectedItem(LocoIcon.YELLOW);

		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		// confirm delete dialog window should appear
		pressDialogButton(f, "OK");
		//done
		f.dispose();
	}
	
	public void testSetupFrameRead(){
		OperationsSetupFrame f = new OperationsSetupFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();
		
		Assert.assertEquals("railroad name", "Test Railroad Name", f.railroadNameTextField.getText());
		Assert.assertEquals("max length", "1234", f.maxLengthTextField.getText());
		Assert.assertEquals("max engines", "6", f.maxEngineSizeTextField.getText());
		Assert.assertEquals("switch time", "3", f.switchTimeTextField.getText());
		Assert.assertEquals("travel time", "4", f.travelTimeTextField.getText());
		//Assert.assertEquals("owner", "Bob J", f.ownerTextField.getText());
				
		Assert.assertTrue("HO scale", f.scaleHO.isSelected());
		Assert.assertFalse("N scale", f.scaleN.isSelected());
		Assert.assertFalse("Z scale", f.scaleZ.isSelected());
		Assert.assertFalse("TT scale", f.scaleTT.isSelected());
		Assert.assertFalse("HOn3 scale", f.scaleHOn3.isSelected());
		Assert.assertFalse("OO scale", f.scaleOO.isSelected());
		Assert.assertFalse("Sn3 scale", f.scaleSn3.isSelected());
		Assert.assertFalse("S scale", f.scaleS.isSelected());
		Assert.assertFalse("On3 scale", f.scaleOn3.isSelected());
		Assert.assertFalse("O scale", f.scaleO.isSelected());
		Assert.assertFalse("G scale", f.scaleG.isSelected());
		
		Assert.assertTrue("descriptive", f.typeDesc.isSelected());
		Assert.assertFalse("AAR", f.typeAAR.isSelected());
		
		Assert.assertEquals("panel name", "Test Panel Name", f.panelTextField.getText());
		
		Assert.assertEquals("east color", LocoIcon.RED, f.eastComboBox.getSelectedItem());
		Assert.assertEquals("west color", LocoIcon.BLUE, f.westComboBox.getSelectedItem());
		Assert.assertEquals("north color", LocoIcon.WHITE, f.northComboBox.getSelectedItem());
		Assert.assertEquals("south color", LocoIcon.GREEN, f.southComboBox.getSelectedItem());
		Assert.assertEquals("terminate color", LocoIcon.GRAY, f.terminateComboBox.getSelectedItem());
		Assert.assertEquals("local color", LocoIcon.YELLOW, f.localComboBox.getSelectedItem());
		//done
		f.dispose();
	}

	public void testOptionFrameWrite(){
		OptionFrame f = new OptionFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();		
		
		// confirm defaults
		Assert.assertTrue("build normal", f.buildNormal.isSelected());
		Assert.assertFalse("build aggressive", f.buildAggressive.isSelected());
		Assert.assertFalse("local", f.localSpurCheckBox.isSelected());
		Assert.assertFalse("interchange", f.localInterchangeCheckBox.isSelected());
		Assert.assertFalse("yard", f.localYardCheckBox.isSelected());
		Assert.assertFalse("rfid", f.rfidCheckBox.isSelected());
		Assert.assertFalse("car logger", f.carLoggerCheckBox.isSelected());
		Assert.assertFalse("engine logger", f.engineLoggerCheckBox.isSelected());
		Assert.assertTrue("router", f.routerCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.buildAggressive ) );
		Assert.assertFalse("build normal", f.buildNormal.isSelected());
		Assert.assertTrue("build aggressive", f.buildAggressive.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.localSpurCheckBox ) );
		Assert.assertTrue("local", f.localSpurCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.localInterchangeCheckBox ) );
		Assert.assertTrue("interchange", f.localInterchangeCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.localYardCheckBox ) );
		Assert.assertTrue("yard", f.localYardCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.rfidCheckBox ) );
		Assert.assertTrue("rfid", f.rfidCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.carLoggerCheckBox ) );
		Assert.assertTrue("car logger", f.carLoggerCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.engineLoggerCheckBox ) );
		Assert.assertTrue("engine logger", f.engineLoggerCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.routerCheckBox ) );
		Assert.assertFalse("router", f.routerCheckBox.isSelected());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		//done
		f.dispose();
	}
	
	public void testOptionFrameRead(){
		OptionFrame f = new OptionFrame();
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents();		
		
		Assert.assertFalse("build normal",f.buildNormal.isSelected());
		Assert.assertTrue("build aggressive",f.buildAggressive.isSelected());
		Assert.assertTrue("local", f.localSpurCheckBox.isSelected());
		Assert.assertTrue("interchange", f.localInterchangeCheckBox.isSelected());
		Assert.assertTrue("yard", f.localYardCheckBox.isSelected());
		Assert.assertTrue("rfid", f.rfidCheckBox.isSelected());
		Assert.assertTrue("car logger", f.carLoggerCheckBox.isSelected());
		Assert.assertTrue("engine logger", f.engineLoggerCheckBox.isSelected());
		Assert.assertFalse("router", f.routerCheckBox.isSelected());
		
		//done
		f.dispose();
	}
	
	@SuppressWarnings("unchecked")
	private void pressDialogButton(JmriJFrame f, String buttonName){
		//  (with JfcUnit, not pushing this off to another thread)			                                            
		// Locate resulting dialog box
        List<javax.swing.JDialog> dialogList = new DialogFinder(null).findAll(f);
        javax.swing.JDialog d = dialogList.get(0);
        Assert.assertNotNull("dialog not found", d); 
        // Find the button
        AbstractButtonFinder finder = new AbstractButtonFinder(buttonName);
        javax.swing.JButton button = ( javax.swing.JButton ) finder.find( d, 0);
        Assert.assertNotNull("button not found", button);   
        // Click button
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );		
	}
	
	// Ensure minimal setup for log4J
	@Override
    protected void setUp() throws Exception { 
        super.setUp();
		apps.tests.Log4JFixture.setUp();
		
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
		
		// Repoint OperationsSetupXml to JUnitTest subdirectory
		OperationsSetupXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		// Change file names to ...Test.xml
		OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");
		
		new jmri.InstanceManager(); // for WebServer and the railroad name

	}

	public OperationsSetupGuiTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsSetupGuiTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsSetupGuiTest.class);
		suite.addTestSuite(OperationsBackupGuiTest.class);
		return suite;
	}

	// The minimal setup for log4J
	@Override
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
