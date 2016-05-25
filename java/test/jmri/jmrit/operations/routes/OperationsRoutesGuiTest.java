//OperationsRoutesGuiTest.java

package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.JmriJFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.jfcunit.eventdata.*;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Tests for the Operations Routes GUI class
 *  
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class OperationsRoutesGuiTest extends jmri.util.SwingTestCase {
	
	public void testRoutesTableFrame(){
		// remove previous routes
		RouteManager.instance().dispose();
		// create 5 routes
		RouteManager rManager = RouteManager.instance();
		Route r1 = rManager.newRoute("Test Route E");
		r1.setComment("Comment test route E");
		Route r2 = rManager.newRoute("Test Route D");
		r2.setComment("Comment test route D");
		Route r3 = rManager.newRoute("Test Route C");
		r3.setComment("Comment test route C");
		Route r4 = rManager.newRoute("Test Route B");
		r4.setComment("Comment test route B");
		Route r5 = rManager.newRoute("Test Route A");
		r5.setComment("Comment test route A");
		RoutesTableFrame f = new RoutesTableFrame();
		
		// should be 5 rows
		Assert.assertEquals("number of rows", 5, f.routesModel.getRowCount());
		// default is sort by name
		Assert.assertEquals("1st route", "Test Route A", f.routesModel.getValueAt(0, RoutesTableModel.NAMECOLUMN));
		Assert.assertEquals("2nd route", "Test Route B", f.routesModel.getValueAt(1, RoutesTableModel.NAMECOLUMN));
		Assert.assertEquals("3rd route", "Test Route C", f.routesModel.getValueAt(2, RoutesTableModel.NAMECOLUMN));
		Assert.assertEquals("4th route", "Test Route D", f.routesModel.getValueAt(3, RoutesTableModel.NAMECOLUMN));
		Assert.assertEquals("5th route", "Test Route E", f.routesModel.getValueAt(4, RoutesTableModel.NAMECOLUMN));
			
		// create add route frame
		//f.addButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addButton ) );
	    // confirm panel creation
		JmriJFrame ref = JmriJFrame.getFrame("Add Route");
        Assert.assertNotNull("route edit frame", ref);

		// create edit route frame
		f.routesModel.setValueAt(null, 2, RoutesTableModel.EDITCOLUMN);
		
		ref.dispose();
		f.dispose();
	}

	public void testRouteEditFrame(){
		RouteEditFrame f = new RouteEditFrame();
		f.setTitle("Test Add Route Frame");
		f.initComponents(null);
		
		f.routeNameTextField.setText("New Test Route");
		f.commentTextField.setText("New Text Route Comment");
		//f.addRouteButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addRouteButton ) );
		
		RouteManager rManager = RouteManager.instance();
		Assert.assertEquals("should be 6 routes", 6, rManager.getRoutesByNameList().size());
		Route newRoute = rManager.getRouteByName("New Test Route");	
		Assert.assertNotNull(newRoute);
		Assert.assertEquals("route comment", "New Text Route Comment", newRoute.getComment());
		
		// Add some locations to the route
		LocationManager lManager = LocationManager.instance();
		f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc B"));
		//f.addLocationButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocationButton ) );
		f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc D"));
		//f.addLocationButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocationButton ) );
		f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc A"));
		//f.addLocationButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocationButton ) );
		
		// put the next two locations at the start of the route
		//f.addLocAtTop.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocAtTop ) );
		f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc C"));
		//f.addLocationButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocationButton ) );
		f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc E"));
		//f.addLocationButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocationButton ) );
		
		// confirm that the route sequence is correct
		List<String> routeLocations = newRoute.getLocationsBySequenceList();
		Assert.assertEquals("1st location", "Test Loc E", newRoute.getLocationById(routeLocations.get(0)).getName());
		Assert.assertEquals("2nd location", "Test Loc C", newRoute.getLocationById(routeLocations.get(1)).getName());
		Assert.assertEquals("3rd location", "Test Loc B", newRoute.getLocationById(routeLocations.get(2)).getName());
		Assert.assertEquals("4th location", "Test Loc D", newRoute.getLocationById(routeLocations.get(3)).getName());
		Assert.assertEquals("5th location", "Test Loc A", newRoute.getLocationById(routeLocations.get(4)).getName());
		
		f.routeNameTextField.setText("Newer Test Route");
		//f.saveRouteButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveRouteButton ) );
		
		Assert.assertEquals("changed route name", "Newer Test Route", newRoute.getName());
		
		// test delete button
		//f.deleteRouteButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.deleteRouteButton ) );
		// click "Yes" in the confirm popup
		pressDialogButton(f, "Yes");
		
		Assert.assertEquals("should be 5 routes", 5, rManager.getRoutesByNameList().size());
		
		f.dispose();
	}
	
	public void testRouteEditFrameRead(){
		RouteManager lManager = RouteManager.instance();
		Route l2 = lManager.getRouteByName("Test Route C");
		
		RouteEditFrame f = new RouteEditFrame();
		f.setTitle("Test Edit Route Frame");
		f.initComponents(l2);
		
		Assert.assertEquals("route name", "Test Route C", f.routeNameTextField.getText());
		Assert.assertEquals("route comment", "Comment test route C", f.commentTextField.getText());
		
		f.dispose();
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

	}

	public OperationsRoutesGuiTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsRoutesGuiTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsRoutesGuiTest.class);
		return suite;
	}

	@SuppressWarnings("unchecked")
	private void pressDialogButton(JmriJFrame f, String buttonName){
		//  (with JfcUnit, not pushing this off to another thread)			                                            
		// Locate resulting dialog box
        List<javax.swing.JDialog> dialogList = new DialogFinder(null).findAll(f);
        javax.swing.JDialog d = dialogList.get(0);
        // Find the button
        AbstractButtonFinder finder = new AbstractButtonFinder(buttonName);
        javax.swing.JButton button = ( javax.swing.JButton ) finder.find( d, 0);
        Assert.assertNotNull("button not found", button);   
        // Click button
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );		
	}
	
	// The minimal setup for log4J
	@Override
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
