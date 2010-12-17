// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a switch list for a location on the railroad
 * @author Daniel Boudreau (C) Copyright 2008
 *
 */
public class TrainSwitchLists extends TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	TrainManager manager = TrainManager.instance();
	
	// builds a switch list for a location
	public void buildSwitchList(Location location){
		// create manifest file
		File file = TrainManagerXml.instance().createSwitchListFile(
				location.getName());
		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)),
					true);
		} catch (IOException e) {
			log.error("can not open switchlist file");
			return;
		}
		// build header
		addLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);
		addLine(fileOut, MessageFormat.format(rb.getString("SwitchListFor"), new Object[]{location.getName()}));
		addLine(fileOut, MessageFormat.format(rb.getString("Valid"), new Object[]{getDate()}));
		
		// get a list of trains
		List<String> trains = manager.getTrainsByTimeList();
		CarManager carManager = CarManager.instance();
		EngineManager engineManager = EngineManager.instance();
		for (int i=0; i<trains.size(); i++){
			int pickupCars = 0;
			int dropCars = 0;
			int stops = 1;
			Train train = manager.getTrainById(trains.get(i));
			if (!train.isBuilt())
				continue;	// train wasn't built so skip
			List<String> carList = carManager.getByTrainDestinationList(train);
			List<String> enginesList = engineManager.getByTrainList(train);
			// does the train stop once or more at this location?
			Route route = train.getRoute();
			if (route == null)
				continue;	// no route for this train
			List<String> routeList = route.getLocationsBySequenceList();
			for (int r=0; r<routeList.size(); r++){
				RouteLocation rl = route.getLocationById(routeList.get(r));
				if (rl.getName().equals(location.getName())){
					if (stops > 1){
						// Print visit number only if previous location wasn't the same
						RouteLocation rlPrevious = route.getLocationById(routeList.get(r-1));
						if (!rl.getName().equals(rlPrevious.getName())){
							newLine(fileOut);
							addLine(fileOut, MessageFormat.format(rb.getString("VisitNumber"), new Object[]{stops}));
						}
					} else {
						newLine(fileOut);
						addLine(fileOut, MessageFormat.format(rb.getString("ScheduledWork"), new Object[]{train.getName(), train.getDescription()}));
						String expected = "";
						if (r != 0)
							expected = MessageFormat.format(rb.getString("expectedArrival"), new Object[]{train.getExpectedArrivalTime(rl)});
						if (train.isTrainInRoute()){
							addLine(fileOut, MessageFormat.format(rb.getString("DepartedExpected"), new Object[]{train.getTrainDepartsName(), train.getExpectedArrivalTime(rl)}));
						} else {
							addLine(fileOut, MessageFormat.format(rb.getString("DepartsAt"), new Object[]{train.getTrainDepartsName(), train.getDepartureTime(), expected}));
						}
					}
					// go through the list of engines and determine if the engine departs here
					for (int j = 0; j < enginesList.size(); j++) {
						Engine engine = engineManager
								.getById(enginesList.get(j));
						if (engine.getRouteLocation() == rl	&& !engine.getTrackName().equals("")){
							pickupEngine(fileOut, engine);
						}
					}
					// get a list of cars and determine if this location is serviced
//					block cars by destination
					for (int j = 0; j < routeList.size(); j++) {
						RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
						for (int k = 0; k < carList.size(); k++) {
							Car car = carManager.getById(carList.get(k));
							if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
									&& car.getRouteDestination() == rld) {
								pickupCar(fileOut, car);
								pickupCars++;
							}
						}
					}
					for (int j = 0; j < enginesList.size(); j++) {
						Engine engine = engineManager
								.getById(enginesList.get(j));
						if (engine.getRouteDestination() == rl){
							dropEngine(fileOut, engine);
						}
					}
					for (int j=0; j<carList.size(); j++){
						Car car = carManager.getById(carList.get(j));
						if (car.getRouteDestination() == rl){
							dropCar(fileOut, car);
							dropCars++;
						}
					}
					stops++;
				}
			}
			if (stops > 1 && pickupCars == 0){
				addLine(fileOut, rb.getString("NoCarPickUps"));
			}
	
			if (stops > 1 && dropCars == 0){
				addLine(fileOut, rb.getString("NoCarDrops"));
			}
		}
		// Are there any cars that need to be found?
		getCarsLocationUnknown(fileOut);
		fileOut.flush();
		fileOut.close();
	}

	public void printSwitchList(Location location, boolean preview){
		File buildFile = TrainManagerXml.instance().getSwitchListFile(location.getName());
		TrainPrintUtilities.printReport(buildFile, rb.getString("SwitchList")+" "+ location.getName(), preview, Setup.getFontName(), false, Setup.getManifestLogoURL());
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainSwitchLists.class.getName());
}
