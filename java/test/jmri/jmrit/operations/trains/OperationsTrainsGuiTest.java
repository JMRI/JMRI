//OperationsTrainsGuiTest.java

package jmri.jmrit.operations.trains;

import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.jfcunit.finder.*;
import junit.extensions.jfcunit.eventdata.*;
import jmri.util.JmriJFrame;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Tests for the Operations Trains GUI class
 * 
 * @author Dan Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class OperationsTrainsGuiTest extends jmri.util.SwingTestCase {

	private final int DIRECTION_ALL = Location.EAST + Location.WEST + Location.NORTH + Location.SOUTH;

	/**
	 * Adds some cars for the various tests in this suite
	 */
	public void testTrainsAddCars() {
		CarManager cm = CarManager.instance();
		// add caboose to the roster
		Car c = cm.newCar("NH", "687");
		c.setCaboose(true);
		c = cm.newCar("CP", "435");
		c.setCaboose(true);

	}

	public void testTrainsTableFrame() {
		TrainManager tmanager = TrainManager.instance();
		// turn off build fail messages
		tmanager.setBuildMessagesEnabled(true);
		// turn off print preview
		tmanager.setPrintPreviewEnabled(false);

		// load 5 trains
		for (int i = 0; i < 5; i++)
			tmanager.newTrain("Test_Train " + i);

		// load 6 locations
		for (int i = 0; i < 6; i++)
			LocationManager.instance().newLocation("Test_Location " + i);

		TrainsTableFrame f = new TrainsTableFrame();
		f.setVisible(true);
		f.setLocation(10, 20);
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));

		Assert.assertEquals("sort by name", TrainsTableModel.TIMECOLUMNNAME, f.getSortBy());
		Assert.assertTrue("Build Messages", tmanager.isBuildMessagesEnabled());
		Assert.assertFalse("Build Report", tmanager.isBuildReportEnabled());
		Assert.assertFalse("Print Review", tmanager.isPrintPreviewEnabled());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.showTime));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.buildMsgBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.buildReportBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread

		Assert.assertFalse("Build Messages 2", tmanager.isBuildMessagesEnabled());
		Assert.assertTrue("Build Report 2", tmanager.isBuildReportEnabled());
		Assert.assertFalse("Print Review 2", tmanager.isPrintPreviewEnabled());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.showId));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.buildMsgBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.printPreviewBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread

		Assert.assertTrue("Build Messages 3", tmanager.isBuildMessagesEnabled());
		Assert.assertTrue("Build Report 3", tmanager.isBuildReportEnabled());
		Assert.assertTrue("Print Review 3", tmanager.isPrintPreviewEnabled());

		// create the TrainEditFrame
		getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		// confirm panel creation
		JmriJFrame tef = JmriJFrame.getFrame("Add Train");
		Assert.assertNotNull("train edit frame", tef);

		// create the TrainSwichListEditFrame
		getHelper().enterClickAndLeave(new MouseEventData(this, f.printSwitchButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		// confirm panel creation
		JmriJFrame tsle = JmriJFrame.getFrame("Switch Lists by Location");
		Assert.assertNotNull("train switchlist edit frame", tsle);

		// kill panels
		tef.dispose();
		tsle.dispose();
		f.dispose();
	}

	/**
	 * This test relies on OperationsTrainsTest having been run to initialize the train fields.
	 */
	public void testTrainEditFrame() {

		// load 5 routes
		RouteManager.instance().newRoute("Test Route A");
		RouteManager.instance().newRoute("Test Route B");
		RouteManager.instance().newRoute("Test Route C");
		RouteManager.instance().newRoute("Test Route D");
		RouteManager.instance().newRoute("Test Route E");

		// load engines
		EngineManager emanager = EngineManager.instance();
		Engine e1 = emanager.newEngine("E", "1");
		e1.setModel("GP40");
		Engine e2 = emanager.newEngine("E", "2");
		e2.setModel("GP40");
		Engine e3 = emanager.newEngine("UP", "3");
		e3.setModel("GP40");
		Engine e4 = emanager.newEngine("UP", "4");
		e4.setModel("FT");

		TrainEditFrame trainEditFrame = new TrainEditFrame();
		trainEditFrame.initComponents(null);
		trainEditFrame.setTitle("Test Edit Train Frame");
		// fill in name and description fields
		trainEditFrame.trainNameTextField.setText("Test Train Name");
		trainEditFrame.trainDescriptionTextField.setText("Test Train Description");
		trainEditFrame.commentTextArea.setText("Test Train Comment");
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.addTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");

		// test defaults
		Assert.assertEquals("train name", "Test Train Name", t.getName());
		Assert.assertEquals("train description", "Test Train Description", t.getDescription());
		Assert.assertEquals("train comment", "Test Train Comment", t.getComment());
		Assert.assertEquals("train depart time", "00:00", t.getDepartureTime());
		Assert.assertEquals("train route", null, t.getRoute());
		Assert.assertTrue("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));
		Assert.assertEquals("train roads", Train.ALLROADS, t.getRoadOption());
		Assert.assertEquals("train requirements", Train.NONE, t.getRequirements());

		// test departure time fields
		trainEditFrame.hourBox.setSelectedItem("15");
		trainEditFrame.minuteBox.setSelectedItem("45");
		// shouldn't change until Save
		Assert.assertEquals("train comment", "00:00", t.getDepartureTime());
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		// clear no route dialogue box
		pressDialogButton(trainEditFrame, "OK");

		Assert.assertEquals("train comment", "15:45", t.getDepartureTime());

		// test route field, 5 routes and a blank
		Assert.assertEquals("Route Combobox item count", 6, trainEditFrame.routeBox.getItemCount());
		trainEditFrame.routeBox.setSelectedIndex(3); // the 3rd item should be "Test Route C"
		Assert.assertEquals("train route 2", "Test Route C", t.getRoute().getName());
		// test route edit button
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.editButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		// confirm panel creation
		JmriJFrame ref = JmriJFrame.getFrame("Edit Route");
		Assert.assertNotNull("route add frame", ref);
		
		// increase screen size so clear and set buttons are shown
		trainEditFrame.setLocation(10, 0);
		trainEditFrame.setSize(trainEditFrame.getWidth(), trainEditFrame.getHeight() + 200);
		
		// test car types using the clear and set buttons
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.clearButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		Assert.assertFalse("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.setButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		Assert.assertTrue("train accepts car type Boxcar", t.acceptsTypeName("Boxcar"));

		// test engine fields
		Assert.assertEquals("number of engines", "0", t.getNumberEngines());
		Assert.assertEquals("engine model", "", t.getEngineModel());
		Assert.assertEquals("engine road", "", t.getEngineRoad());
		// now change them
		trainEditFrame.numEnginesBox.setSelectedItem("3");
		trainEditFrame.modelEngineBox.setSelectedItem("FT");
		trainEditFrame.roadEngineBox.setSelectedItem("UP");
		// shouldn't change until Save
		Assert.assertEquals("number of engines 1", "0", t.getNumberEngines());
		Assert.assertEquals("engine model 1", "", t.getEngineModel());
		Assert.assertEquals("engine road 1", "", t.getEngineRoad());
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("number of engines 2", "3", t.getNumberEngines());
		Assert.assertEquals("engine model 2", "FT", t.getEngineModel());
		Assert.assertEquals("engine road 2", "UP", t.getEngineRoad());

		// test caboose and FRED buttons and fields
		// require a car with FRED
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.fredRadioButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		// shouldn't change until Save
		Assert.assertEquals("train requirements 1", Train.NONE, t.getRequirements());
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train requirements 2", Train.FRED, t.getRequirements());
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.cabooseRadioButton));
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train requirements 3", Train.CABOOSE, t.getRequirements());
		Assert.assertEquals("caboose road 1", "", t.getCabooseRoad());
		// shouldn't change until Save
		trainEditFrame.roadCabooseBox.setSelectedItem("NH");
		Assert.assertEquals("caboose road 2", "", t.getCabooseRoad());
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("caboose road 3", "NH", t.getCabooseRoad());
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.noneRadioButton));
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train requirements 4", Train.NONE, t.getRequirements());

		// test frame size and location
		trainEditFrame.setSize(650, 600);
		trainEditFrame.setLocation(25, 30);
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread

		// test delete button
		// the delete opens a dialog window to confirm the delete
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.deleteTrainButton));
		// don't delete, we need this train for the next two tests
		// testTrainBuildOptionFrame() and testTrainEditFrameRead()
		pressDialogButton(trainEditFrame, "No");

		ref.dispose();
		trainEditFrame.dispose();
	}

	public void testTrainEditFrameBuildOptionFrame() {
		// test build options
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");

		// Add a route to this train
		Route route = RouteManager.instance().newRoute("Test Train Route");
		route.addLocation(LocationManager.instance().newLocation("Test Train Location A"));
		route.addLocation(LocationManager.instance().newLocation("Test Train Location B"));
		route.addLocation(LocationManager.instance().newLocation("Test Train Location C"));
		t.setRoute(route);

		TrainEditFrame trainEditFrame = new TrainEditFrame();
		trainEditFrame.setLocation(0, 0); // entire panel must be visible for tests to work properly
		trainEditFrame.initComponents(t);
		trainEditFrame.setTitle("Test Build Options Train Frame");

		TrainEditBuildOptionsFrame f = new TrainEditBuildOptionsFrame();
		f.setLocation(0, 0); // entire panel must be visible for tests to work properly
		f.initComponents(trainEditFrame);
		f.setTitle("Test Train Build Options");

		// confirm defaults
		Assert.assertEquals("Build normal", false, t.isBuildTrainNormalEnabled());
		Assert.assertEquals("send to terminal", false, t.isSendCarsToTerminalEnabled());
		Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
		Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
		Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

		// test options
		getHelper().enterClickAndLeave(new MouseEventData(this, f.buildNormalCheckBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
		Assert.assertEquals("send to terminal", false, t.isSendCarsToTerminalEnabled());
		Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
		Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
		Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.sendToTerminalCheckBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
		Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
		Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
		Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
		Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.returnStagingCheckBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
		Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
		// the return to staging checkbox should be disabled
		Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
		Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
		Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.allowLocalMovesCheckBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
		Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
		Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
		Assert.assertEquals("allow local moves", false, t.isAllowLocalMovesEnabled());
		Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.allowThroughCarsCheckBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
		Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
		Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
		Assert.assertEquals("allow local moves", false, t.isAllowLocalMovesEnabled());
		Assert.assertEquals("allow through cars", false, t.isAllowThroughCarsEnabled());

		// test car owner options
		getHelper().enterClickAndLeave(new MouseEventData(this, f.ownerNameExclude));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train car owner exclude", Train.EXCLUDEOWNERS, t.getOwnerOption());
		getHelper().enterClickAndLeave(new MouseEventData(this, f.ownerNameInclude));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train car owner include", Train.INCLUDEOWNERS, t.getOwnerOption());
		getHelper().enterClickAndLeave(new MouseEventData(this, f.ownerNameAll));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train car owner all", Train.ALLOWNERS, t.getOwnerOption());

		// test car date options
		getHelper().enterClickAndLeave(new MouseEventData(this, f.builtDateAfter));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		f.builtAfterTextField.setText("1956");
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train car built after", "1956", t.getBuiltStartYear());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.builtDateBefore));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		f.builtBeforeTextField.setText("2010");
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train car built before", "2010", t.getBuiltEndYear());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.builtDateRange));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		f.builtAfterTextField.setText("1888");
		f.builtBeforeTextField.setText("2000");
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train car built after range", "1888", t.getBuiltStartYear());
		Assert.assertEquals("train car built before range", "2000", t.getBuiltEndYear());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.builtDateAll));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("train car built after all", "", t.getBuiltStartYear());
		Assert.assertEquals("train car built before all", "", t.getBuiltEndYear());

		// test optional loco and caboose changes
		getHelper().enterClickAndLeave(new MouseEventData(this, f.change1Engine));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		// clear dialogue box
		pressDialogButton(f, "OK");
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("loco 1 change", Train.CHANGE_ENGINES, t.getSecondLegOptions());
		Assert.assertEquals("loco 1 departure name", "", t.getSecondLegStartLocationName());

		f.routePickup1Box.setSelectedIndex(1); // should be "Test Train Location A"
		f.numEngines1Box.setSelectedIndex(3); // should be 3 locos
		f.modelEngine1Box.setSelectedItem("FT");
		f.roadEngine1Box.setSelectedItem("UP");

		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("loco 1 change", Train.CHANGE_ENGINES, t.getSecondLegOptions());
		Assert.assertEquals("loco 1 departure name", "Test Train Location A", t
				.getSecondLegStartLocationName());
		Assert.assertEquals("loco 1 number of engines", "3", t.getSecondLegNumberEngines());
		Assert.assertEquals("loco 1 model", "FT", t.getSecondLegEngineModel());
		Assert.assertEquals("loco 1 road", "UP", t.getSecondLegEngineRoad());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.modify1Caboose));
		f.routePickup1Box.setSelectedIndex(0);
		f.roadCaboose1Box.setSelectedItem("NH");
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		// clear dialogue box
		pressDialogButton(f, "OK");
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("caboose 1 change", Train.ADD_CABOOSE, t.getSecondLegOptions());

		f.routePickup1Box.setSelectedIndex(2);
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread

		Assert.assertEquals("caboose 1 road", "NH", t.getSecondLegCabooseRoad());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.helper1Service));
		f.routePickup1Box.setSelectedIndex(0);
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		// clear dialogue box
		pressDialogButton(f, "OK");
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("helper 1 change", Train.HELPER_ENGINES, t.getSecondLegOptions());

		f.routePickup1Box.setSelectedIndex(2); // Should be "Test Train Location B"
		f.routeDrop1Box.setSelectedIndex(3); // Should be "Test Train Location C"
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread

		Assert.assertEquals("Helper 1 start location name", "Test Train Location B", t
				.getSecondLegStartLocationName());
		Assert.assertEquals("Helper 1 end location name", "Test Train Location C", t
				.getSecondLegEndLocationName());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.none1));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("none 1", 0, t.getSecondLegOptions());

		// now do the second set of locos and cabooses
		getHelper().enterClickAndLeave(new MouseEventData(this, f.change2Engine));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		// clear dialogue box
		pressDialogButton(f, "OK");
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("loco 2 change", Train.CHANGE_ENGINES, t.getThirdLegOptions());
		Assert.assertEquals("loco 2 departure name", "", t.getThirdLegStartLocationName());

		f.routePickup2Box.setSelectedIndex(1); // should be "Test Train Location A"
		f.numEngines2Box.setSelectedIndex(3); // should be 3 locos
		f.modelEngine2Box.setSelectedItem("FT");
		f.roadEngine2Box.setSelectedItem("UP");

		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("loco 2 change", Train.CHANGE_ENGINES, t.getThirdLegOptions());
		Assert.assertEquals("loco 2 departure name", "Test Train Location A", t
				.getThirdLegStartLocationName());
		Assert.assertEquals("loco 2 number of engines", "3", t.getThirdLegNumberEngines());
		Assert.assertEquals("loco 2 model", "FT", t.getThirdLegEngineModel());
		Assert.assertEquals("loco 2 road", "UP", t.getThirdLegEngineRoad());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.modify2Caboose));
		f.routePickup2Box.setSelectedIndex(0);
		f.roadCaboose2Box.setSelectedItem("NH");
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		// clear dialogue box
		pressDialogButton(f, "OK");
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("caboose 2 change", Train.ADD_CABOOSE, t.getThirdLegOptions());

		f.routePickup2Box.setSelectedIndex(2);
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread

		Assert.assertEquals("caboose 2 road", "NH", t.getThirdLegCabooseRoad());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.helper2Service));
		f.routePickup2Box.setSelectedIndex(0);
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		// clear dialogue box
		pressDialogButton(f, "OK");
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("helper 2 change", Train.HELPER_ENGINES, t.getThirdLegOptions());

		f.routePickup2Box.setSelectedIndex(2); // Should be "Test Train Location B"
		f.routeDrop2Box.setSelectedIndex(3); // Should be "Test Train Location C"
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread

		Assert.assertEquals("Helper 2 start location name", "Test Train Location B", t
				.getThirdLegStartLocationName());
		Assert.assertEquals("Helper 2 end location name", "Test Train Location C", t
				.getThirdLegEndLocationName());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.none2));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveTrainButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertEquals("none 2", 0, t.getThirdLegOptions());

		trainEditFrame.dispose();
		f.dispose();
	}

	public void testTrainEditFrameRead() {
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");

		// change the train so it doesn't match the add test
		t.setRequirements(Train.CABOOSE);
		t.setCabooseRoad("CP");

		TrainEditFrame f = new TrainEditFrame();
		f.initComponents(t);
		f.setTitle("Test Edit Train Frame");

		Assert.assertEquals("train name", "Test Train Name", f.trainNameTextField.getText());
		Assert.assertEquals("train description", "Test Train Description", f.trainDescriptionTextField
				.getText());
		Assert.assertEquals("train comment", "Test Train Comment", f.commentTextArea.getText());
		Assert.assertEquals("train depart hour", "15", f.hourBox.getSelectedItem());
		Assert.assertEquals("train depart minute", "45", f.minuteBox.getSelectedItem());
		Assert.assertEquals("train route", t.getRoute(), f.routeBox.getSelectedItem());
		Assert.assertEquals("number of engines", "3", f.numEnginesBox.getSelectedItem());
		Assert.assertEquals("engine model", "FT", f.modelEngineBox.getSelectedItem());
		Assert.assertEquals("engine road", "UP", f.roadEngineBox.getSelectedItem());
		Assert.assertEquals("caboose road", "CP", f.roadCabooseBox.getSelectedItem());
		// check radio buttons
		Assert.assertTrue("caboose selected", f.cabooseRadioButton.isSelected());
		Assert.assertFalse("none selected", f.noneRadioButton.isSelected());
		Assert.assertFalse("FRED selected", f.fredRadioButton.isSelected());

		f.dispose();
	}

	public void testTrainModifyFrame() {
		// confirm that train default accepts Boxcars
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		Assert.assertTrue("accepts Boxcar 1", t.acceptsTypeName("Boxcar"));

		TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
		f.initComponents("Boxcar");

		// remove Boxcar from trains
		getHelper().enterClickAndLeave(new MouseEventData(this, f.clearButton));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertFalse("accepts Boxcar 2", t.acceptsTypeName("Boxcar"));

		// now add Boxcar to trains
		getHelper().enterClickAndLeave(new MouseEventData(this, f.setButton));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertTrue("accepts Boxcar 3", t.acceptsTypeName("Boxcar"));

		f.dispose();
	}

	public void testTrainSwitchListEditFrame() {
		// check defaults
		Assert.assertTrue("All Trains", Setup.isSwitchListAllTrainsEnabled());
		Assert.assertFalse("Page per Train", Setup.isSwitchListPagePerTrainEnabled());
		Assert.assertTrue("Real Time", Setup.isSwitchListRealTime());

		TrainSwitchListEditFrame f = new TrainSwitchListEditFrame();
		f.initComponents();

		LocationManager lmanager = LocationManager.instance();
		List<String> locations = lmanager.getLocationsByNameList();

		// default switch list will print all locations
		for (int i = 0; i < locations.size(); i++) {
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertTrue("print switchlist 1", l.isSwitchListEnabled());
		}
		// now clear all locations
		getHelper().enterClickAndLeave(new MouseEventData(this, f.clearButton));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		for (int i = 0; i < locations.size(); i++) {
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertFalse("print switchlist 2", l.isSwitchListEnabled());
		}
		// now set all locations
		getHelper().enterClickAndLeave(new MouseEventData(this, f.setButton));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		for (int i = 0; i < locations.size(); i++) {
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertTrue("print switchlist 3", l.isSwitchListEnabled());
		}

		// test the three check box options
		getHelper().enterClickAndLeave(new MouseEventData(this, f.switchListRealTimeCheckBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertTrue("All Trains", Setup.isSwitchListAllTrainsEnabled());
		Assert.assertFalse("Page per Train", Setup.isSwitchListPagePerTrainEnabled());
		Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.switchListAllTrainsCheckBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertFalse("All Trains", Setup.isSwitchListAllTrainsEnabled());
		Assert.assertFalse("Page per Train", Setup.isSwitchListPagePerTrainEnabled());
		Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

		getHelper().enterClickAndLeave(new MouseEventData(this, f.switchListPageCheckBox));
		getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertFalse("All Trains", Setup.isSwitchListAllTrainsEnabled());
		Assert.assertTrue("Page per Train", Setup.isSwitchListPagePerTrainEnabled());
		Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

		f.dispose();
	}

	/**
	 * Test that delete train works
	 */
	public void testTrainEditFrameDelete() {
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");

		TrainEditFrame trainEditFrame = new TrainEditFrame();
		trainEditFrame.initComponents(t);
		trainEditFrame.setTitle("Test Delete Train Frame");

		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.deleteTrainButton));
		// And now press the confirmation button
		pressDialogButton(trainEditFrame, "Yes");
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread

		t = tmanager.getTrainByName("Test Train Name");
		Assert.assertNull("train deleted", t);

		// Now add it back
		getHelper().enterClickAndLeave(new MouseEventData(this, trainEditFrame.addTrainButton));
		jmri.util.JUnitUtil.releaseThread(trainEditFrame, 1); // compensate for race between GUI and test thread
		t = tmanager.getTrainByName("Test Train Name");
		Assert.assertNotNull("train added", t);

		trainEditFrame.dispose();
	}

	public void testTrainByCarTypeFrame() {
		TrainManager tmanager = TrainManager.instance();
		Train train = tmanager.getTrainByName("Test Train Name");
		TrainByCarTypeFrame f = new TrainByCarTypeFrame();
		f.initComponents(train);

		Assert.assertNotNull("frame exists", f);
		f.dispose();
	}

	public void testTrainsScheduleTableFrame() {
		TrainsScheduleTableFrame f = new TrainsScheduleTableFrame();
		f.setVisible(true);

		Assert.assertNotNull("frame exists", f);
		f.dispose();
	}

	public void testTrainsScheduleEditFrame() {
		TrainsScheduleEditFrame f = new TrainsScheduleEditFrame();
		Assert.assertNotNull("frame exists", f);

		f.addTextBox.setText("A New Day");
		getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread

		TrainScheduleManager tsm = TrainScheduleManager.instance();
		Assert.assertNotNull("Train schedule manager exists", tsm);
		Assert.assertNotNull("A new Day schedule exists", tsm.getScheduleByName("A New Day"));

		getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertNull("A new Day schedule does not exist", tsm.getScheduleByName("A New Day"));

		getHelper().enterClickAndLeave(new MouseEventData(this, f.replaceButton));
		jmri.util.JUnitUtil.releaseThread(f, 1); // compensate for race between GUI and test thread
		Assert.assertNotNull("A new Day schedule exists", tsm.getScheduleByName("A New Day"));

		f.dispose();
	}

	// test TrainIcon attributes
	public void testTrainIconAttributes() {
		Train train1 = new Train("TESTTRAINID", "TESTNAME");

		Assert.assertEquals("Train Id", "TESTTRAINID", train1.getId());
		Assert.assertEquals("Train Name", "TESTNAME", train1.getName());
		Assert.assertEquals("Train toString", "TESTNAME", train1.toString());

		jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor(
				"Test Panel");
		Assert.assertNotNull("New editor", editor);
		TrainIcon trainicon1 = editor.addTrainIcon("TestName");
		trainicon1.setTrain(train1);
		Assert.assertEquals("TrainIcon set train", "TESTNAME", trainicon1.getTrain().getName());

		// test color change
		String[] colors = TrainIcon.getLocoColors();
		for (int i = 0; i < colors.length; i++) {
			trainicon1.setLocoColor(colors[i]);
		}
		editor.getTargetFrame().dispose();
	}

	public void testTrainIcon() {
		TrainManager tmanager = TrainManager.instance();
		RouteManager rmanager = RouteManager.instance();
		LocationManager lmanager = LocationManager.instance();
		EngineManager emanager = EngineManager.instance();
		EngineTypes et = EngineTypes.instance();

		// create and register a panel
		jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor(
				"Train Test Panel");
		PanelMenu.instance().addEditorPanel(editor);

		// Place train icons on panel
		Setup.setPanelName("Train Test Panel");
		// Set terminate color to yellow
		Setup.setTrainIconColorTerminate(TrainIcon.YELLOW);
		// add engine number
		Setup.setTrainIconAppendEnabled(true);

		et.addName("Diesel");

		// Set up four engines in two consists
		Consist con1 = emanager.newConsist("C16");
		Consist con2 = emanager.newConsist("C14");

		Engine e1 = new Engine("PC", "5016");
		e1.setModel("GP40");
		e1.setConsist(con1);
		e1.setMoves(123);
		e1.setOwner("AT");
		e1.setBuilt("1990");
		Assert.assertEquals("Engine 1 Length", "59", e1.getLength());
		emanager.register(e1);

		Engine e2 = new Engine("PC", "5019");
		e2.setModel("GP40");
		e2.setConsist(con1);
		e2.setMoves(321);
		e2.setOwner("AT");
		e2.setBuilt("1990");
		Assert.assertEquals("Engine 2 Length", "59", e2.getLength());
		emanager.register(e2);

		Engine e3 = new Engine("PC", "5524");
		e3.setModel("SD45");
		e3.setConsist(con2);
		e3.setOwner("DAB");
		e3.setBuilt("1980");
		Assert.assertEquals("Engine 3 Length", "66", e3.getLength());
		emanager.register(e3);

		Engine e4 = new Engine("PC", "5559");
		e4.setModel("SD45");
		e4.setConsist(con2);
		e4.setOwner("DAB");
		e4.setBuilt("1980");
		Assert.assertEquals("Engine 4 Length", "66", e4.getLength());
		emanager.register(e4);

		// Set up a route of 3 locations: North End Staging (2 tracks),
		// North Industries (1 track), and South End Staging (2 tracks).
		Location l1 = new Location("1", "North End");
		Assert.assertEquals("Location 1 Id", "1", l1.getId());
		Assert.assertEquals("Location 1 Name", "North End", l1.getName());
		Assert.assertEquals("Location 1 Initial Length", 0, l1.getLength());
		l1.setLocationOps(Location.STAGING);
		l1.setTrainDirections(DIRECTION_ALL);
		l1.setSwitchListEnabled(true);
		lmanager.register(l1);

		Track l1s1 = new Track("1s1", "North End 1", Track.STAGING, l1);
		l1s1.setLength(300);
		Assert.assertEquals("Location 1s1 Id", "1s1", l1s1.getId());
		Assert.assertEquals("Location 1s1 Name", "North End 1", l1s1.getName());
		Assert.assertEquals("Location 1s1 LocType", "Staging", l1s1.getLocType());
		Assert.assertEquals("Location 1s1 Length", 300, l1s1.getLength());
		l1s1.setTrainDirections(DIRECTION_ALL);
		l1s1.setRoadOption(Track.ALLROADS);
		l1s1.setDropOption(Track.ANY);
		l1s1.setPickupOption(Track.ANY);

		Track l1s2 = new Track("1s2", "North End 2", Track.STAGING, l1);
		l1s2.setLength(400);
		Assert.assertEquals("Location 1s2 Id", "1s2", l1s2.getId());
		Assert.assertEquals("Location 1s2 Name", "North End 2", l1s2.getName());
		Assert.assertEquals("Location 1s2 LocType", "Staging", l1s2.getLocType());
		Assert.assertEquals("Location 1s2 Length", 400, l1s2.getLength());
		l1s2.setTrainDirections(DIRECTION_ALL);
		l1s2.setRoadOption(Track.ALLROADS);
		l1s2.setDropOption(Track.ANY);
		l1s2.setPickupOption(Track.ANY);

		l1.addTrack("North End 1", Track.STAGING);
		l1.addTrack("North End 2", Track.STAGING);
		List<String> templist1 = l1.getTrackIdsByNameList("");
		for (int i = 0; i < templist1.size(); i++) {
			if (i == 0) {
				Assert.assertEquals("RL 1 Staging 1 Name", "North End 1", templist1.get(i));
			}
			if (i == 1) {
				Assert.assertEquals("RL 1 Staging 2 Name", "North End 2", templist1.get(i));
			}
		}

		l1.register(l1s1);
		l1.register(l1s2);

		Assert.assertEquals("Location 1 Length", 700, l1.getLength());

		Location l2 = new Location("2", "North Industries");
		Assert.assertEquals("Location 2 Id", "2", l2.getId());
		Assert.assertEquals("Location 2 Name", "North Industries", l2.getName());
		l2.setLocationOps(Location.NORMAL);
		l2.setTrainDirections(DIRECTION_ALL);
		l2.setSwitchListEnabled(true);
		lmanager.register(l2);

		Track l2s1 = new Track("2s1", "NI Yard", Track.YARD, l2);
		l2s1.setLength(432);
		Assert.assertEquals("Location 2s1 Id", "2s1", l2s1.getId());
		Assert.assertEquals("Location 2s1 Name", "NI Yard", l2s1.getName());
		Assert.assertEquals("Location 2s1 LocType", Track.YARD, l2s1.getLocType());
		Assert.assertEquals("Location 2s1 Length", 432, l2s1.getLength());
		l2s1.setTrainDirections(DIRECTION_ALL);

		l2.register(l2s1);
		Assert.assertEquals("Location 2 Length", 432, l2.getLength());

		Location l3 = new Location("3", "South End");
		Assert.assertEquals("Location 3 Id", "3", l3.getId());
		Assert.assertEquals("Location 3 Name", "South End", l3.getName());
		Assert.assertEquals("Location 3 Initial Length", 0, l3.getLength());
		l3.setLocationOps(Location.STAGING);
		l3.setTrainDirections(DIRECTION_ALL);
		l3.setSwitchListEnabled(true);
		lmanager.register(l3);

		Track l3s1 = new Track("3s1", "South End 1", Track.STAGING, l3);
		l3s1.setLength(300);
		Assert.assertEquals("Location 3s1 Id", "3s1", l3s1.getId());
		Assert.assertEquals("Location 3s1 Name", "South End 1", l3s1.getName());
		Assert.assertEquals("Location 3s1 LocType", "Staging", l3s1.getLocType());
		Assert.assertEquals("Location 3s1 Length", 300, l3s1.getLength());
		l3s1.setTrainDirections(DIRECTION_ALL);
		l3s1.setRoadOption(Track.ALLROADS);
		l3s1.setDropOption(Track.ANY);
		l3s1.setPickupOption(Track.ANY);

		Track l3s2 = new Track("3s2", "South End 2", Track.STAGING, l3);
		l3s2.setLength(401);
		Assert.assertEquals("Location 3s2 Id", "3s2", l3s2.getId());
		Assert.assertEquals("Location 3s2 Name", "South End 2", l3s2.getName());
		Assert.assertEquals("Location 3s2 LocType", "Staging", l3s2.getLocType());
		Assert.assertEquals("Location 3s2 Length", 401, l3s2.getLength());
		l3s2.setTrainDirections(DIRECTION_ALL);
		l3s2.setRoadOption(Track.ALLROADS);
		l3s2.setDropOption(Track.ANY);
		l3s2.setPickupOption(Track.ANY);

		l3.addTrack("South End 1", Track.STAGING);
		l3.addTrack("South End 2", Track.STAGING);
		List<String> templist3 = l3.getTrackIdsByNameList("");
		for (int i = 0; i < templist3.size(); i++) {
			if (i == 0) {
				Assert.assertEquals("RL 3 Staging 1 Name", "South End 1", templist3.get(i));
			}
			if (i == 1) {
				Assert.assertEquals("RL 3 Staging 2 Name", "South End 2", templist3.get(i));
			}
		}

		l3.register(l3s1);
		l3.register(l3s2);

		Assert.assertEquals("Location 3 Length", 701, l3.getLength());

		// Place Engines on Staging tracks
		Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
		Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
		Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
		Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

		// Define the route.
		Route r1 = new Route("1", "Southbound Main Route");
		Assert.assertEquals("Route Id", "1", r1.getId());
		Assert.assertEquals("Route Name", "Southbound Main Route", r1.getName());

		RouteLocation rl1 = new RouteLocation("1r1", l1);
		rl1.setSequenceId(1);
		rl1.setTrainDirection(RouteLocation.SOUTH);
		rl1.setMaxCarMoves(5);
		rl1.setMaxTrainLength(1000);
		rl1.setTrainIconX(25); // set the train icon coordinates
		rl1.setTrainIconY(25);

		Assert.assertEquals("Route Location 1 Id", "1r1", rl1.getId());
		Assert.assertEquals("Route Location 1 Name", "North End", rl1.getName());
		RouteLocation rl2 = new RouteLocation("1r2", l2);
		rl2.setSequenceId(2);
		rl2.setTrainDirection(RouteLocation.SOUTH);
		// test for only 1 pickup and 1 drop
		rl2.setMaxCarMoves(2);
		rl2.setMaxTrainLength(1000);
		rl2.setTrainIconX(75); // set the train icon coordinates
		rl2.setTrainIconY(25);

		Assert.assertEquals("Route Location 2 Id", "1r2", rl2.getId());
		Assert.assertEquals("Route Location 2 Name", "North Industries", rl2.getName());
		RouteLocation rl3 = new RouteLocation("1r3", l3);
		rl3.setSequenceId(3);
		rl3.setTrainDirection(RouteLocation.SOUTH);
		rl3.setMaxCarMoves(5);
		rl3.setMaxTrainLength(1000);
		rl3.setTrainIconX(125); // set the train icon coordinates
		rl3.setTrainIconY(35);

		Assert.assertEquals("Route Location 3 Id", "1r3", rl3.getId());
		Assert.assertEquals("Route Location 3 Name", "South End", rl3.getName());

		r1.register(rl1);
		r1.register(rl2);
		r1.register(rl3);

		rmanager.register(r1);

		// Finally ready to define the trains.
		Train train1 = new Train("1", "STF");
		Assert.assertEquals("Train Id", "1", train1.getId());
		Assert.assertEquals("Train Name", "STF", train1.getName());
		train1.setRoute(r1);
		tmanager.register(train1);

		Train train2 = new Train("2", "SFF");
		Assert.assertEquals("Train Id", "2", train2.getId());
		Assert.assertEquals("Train Name", "SFF", train2.getName());
		train2.setRoute(r1);
		tmanager.register(train2);

		// Last minute checks.
		Assert.assertEquals("Train 1 Departs Name", "North End", train1.getTrainDepartsName());
		Assert.assertEquals("Train 1 Route Departs Name", "North End", train1.getTrainDepartsRouteLocation()
				.getName());
		Assert.assertEquals("Train 1 Terminates Name", "South End", train1.getTrainTerminatesName());
		Assert.assertEquals("Train 1 Route Terminates Name", "South End", train1
				.getTrainTerminatesRouteLocation().getName());
		Assert.assertEquals("Train 1 Next Location Name", "", train1.getNextLocationName());
		Assert.assertEquals("Train 1 Route Name", "Southbound Main Route", train1.getRoute().getName());

		Assert.assertEquals("Train 2 Departs Name", "North End", train2.getTrainDepartsName());
		Assert.assertEquals("Train 2 Route Departs Name", "North End", train2.getTrainDepartsRouteLocation()
				.getName());
		Assert.assertEquals("Train 2 Terminates Name", "South End", train2.getTrainTerminatesName());
		Assert.assertEquals("Train 2 Route Terminates Name", "South End", train2
				.getTrainTerminatesRouteLocation().getName());
		Assert.assertEquals("Train 2 Next Location Name", "", train2.getNextLocationName());
		Assert.assertEquals("Train 2 Route Name", "Southbound Main Route", train2.getRoute().getName());

		// disable build messages
		tmanager.setBuildMessagesEnabled(false);
		// disable build reports
		tmanager.setBuildReportEnabled(false);

		train1.build();
		train2.build();
		Assert.assertEquals("Train 1 after build", true, train1.isBuilt());
		Assert.assertEquals("Train 2 after build", true, train2.isBuilt());
		// check train icon location and name
		TrainIcon ti1 = train1.getTrainIcon();
		Assert.assertNotNull("Train 1 icon exists", ti1);
		Assert.assertEquals("Train 1 icon text", "STF 5016", ti1.getText());
		TrainIcon ti2 = train2.getTrainIcon();
		Assert.assertNotNull("Train 2 icon exists", ti2);
		Assert.assertEquals("Train 2 icon text", "SFF 5524", ti2.getText());

		// icon uses TrainIconAnimation 2 pixels every 3 mSec
		// X=0 to X=25 25/2 * 3 = 38 mSec
		// Y=0 to Y=25 25/2 * 3 = 38 mSec

		// need to wait for icon to finish moving
		for (int i = 0; i < 200; i++) {
			if (ti2.getX() == 25 && ti2.getY() == 25)
				break;
			sleep(10); // need to wait on slow machines
		}

		Assert.assertEquals("Train 1 icon X", 25, ti1.getX());
		Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
		Assert.assertEquals("Train 2 icon X", 25, ti2.getX());
		Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());

		// move the trains
		train1.move();

		// icon uses TrainIconAnimation 2 pixels every 3 mSec

		// need to wait for icon to finish moving
		for (int i = 0; i < 200; i++) {
			if (ti1.getX() == 75 && ti1.getY() == 25)
				break;
			sleep(10); // need to wait on slow machines
		}

		Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
		Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
		// train 2 shouldn't move
		Assert.assertEquals("Train 2 icon X", 25, ti2.getX());
		Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());

		train2.move();

		// need to wait for icon to finish moving
		for (int i = 0; i < 200; i++) {
			if (ti2.getX() == 75 && ti2.getY() == 25)
				break;
			sleep(10); // need to wait on slow machines
		}

		Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
		Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
		Assert.assertEquals("Train 2 icon X", 75, ti2.getX());
		Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());

		train2.move();

		// need to wait for icon to finish moving
		for (int i = 0; i < 200; i++) {
			if (ti2.getX() == 125 && ti2.getY() == 35)
				break;
			sleep(10); // need to wait on slow machines
		}

		Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
		Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
		Assert.assertEquals("Train 2 icon X", 125, ti2.getX());
		Assert.assertEquals("Train 2 icon Y", 35, ti2.getY());

		editor.getTargetFrame().dispose();
	}

	public void testTrainTestPanel() {
		// confirm panel creation
		JmriJFrame f = JmriJFrame.getFrame("Train Test Panel");
		Assert.assertNotNull(f);

	}

	@SuppressWarnings("unchecked")
	private void pressDialogButton(JmriJFrame f, String buttonName) {
		// (with JfcUnit, not pushing this off to another thread)
		// Locate resulting dialog box
		List<javax.swing.JDialog> dialogList = new DialogFinder(null).findAll(f);
		if (dialogList.size() == 0)
			Assert.fail("No diaglog windows found");
		javax.swing.JDialog d = dialogList.get(0);
		Assert.assertNotNull("dialog not found", d);
		// Find the button
		AbstractButtonFinder finder = new AbstractButtonFinder(buttonName);
		javax.swing.JButton button = (javax.swing.JButton) finder.find(d, 0);
		Assert.assertNotNull("button not found", button);
		// Click button
		getHelper().enterClickAndLeave(new MouseEventData(this, button));
	}

	// Ensure minimal setup for log4J
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		apps.tests.Log4JFixture.setUp();

		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);

		// Repoint OperationsSetupXml to JUnitTest subdirectory
		OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
		// Change file names to ...Test.xml
		OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml");
		RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

		Setup.setAutoSaveEnabled(false);

	}

	public OperationsTrainsGuiTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = { "-noloading", OperationsTrainsGuiTest.class.getName() };
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsTrainsGuiTest.class);
		return suite;
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() throws Exception {
		apps.tests.Log4JFixture.tearDown();
		super.tearDown();
	}
}
