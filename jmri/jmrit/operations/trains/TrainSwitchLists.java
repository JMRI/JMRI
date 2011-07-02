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
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a switch list for a location on the railroad
 * @author Daniel Boudreau (C) Copyright 2008, 2011
 *
 */
public class TrainSwitchLists extends TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	TrainManager manager = TrainManager.instance();
	
	// builds a switch list for a location
	public void buildSwitchList(Location location, boolean newTrainsOnly){
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
		addLine(fileOut, MessageFormat.format(rb.getString("SwitchListFor"), new Object[]{splitString(location.getName())}));
		addLine(fileOut, MessageFormat.format(rb.getString("Valid"), new Object[]{getDate()}));
		
		// get a list of trains
		List<String> trains = manager.getTrainsByTimeList();
		CarManager carManager = CarManager.instance();
		EngineManager engineManager = EngineManager.instance();
		for (int i=0; i<trains.size(); i++){
			int pickupCars = 0;
			int dropCars = 0;
			int stops = 1;
			boolean trainDone = false;
			Train train = manager.getTrainById(trains.get(i));
			if (!train.isBuilt())
				continue;	// train wasn't built so skip
			if (newTrainsOnly && train.getSwitchListStatus().equals(Train.PRINTED))
				continue;	// already printed this train
			List<String> carList = carManager.getByTrainDestinationList(train);
			List<String> enginesList = engineManager.getByTrainList(train);
			// does the train stop once or more at this location?
			Route route = train.getRoute();
			if (route == null)
				continue;	// no route for this train
			List<String> routeList = route.getLocationsBySequenceList();
			for (int r=0; r<routeList.size(); r++){
				RouteLocation rl = route.getLocationById(routeList.get(r));
				if (splitString(rl.getName()).equals(splitString(location.getName()))){
					String expectedArrivalTime = train.getExpectedArrivalTime(rl);
					if (expectedArrivalTime.equals("-1")){
						trainDone = true;
						expectedArrivalTime = "0";
					}
					if (stops > 1){
						// Print visit number only if previous location wasn't the same
						RouteLocation rlPrevious = route.getLocationById(routeList.get(r-1));
						if (!splitString(rl.getName()).equals(splitString(rlPrevious.getName()))){
							newLine(fileOut);
							if (train.isTrainInRoute()){
								if (r != routeList.size()-1)
									addLine(fileOut, MessageFormat.format(rb.getString("VisitNumberDeparted"), new Object[]{stops, train.getName(), expectedArrivalTime, rl.getTrainDirectionString()}));
								else
									addLine(fileOut, MessageFormat.format(rb.getString("VisitNumberTerminatesDeparted"), new Object[]{stops, train.getName(), expectedArrivalTime, rl.getName()}));
							} else {
								if (r != routeList.size()-1)
									addLine(fileOut, MessageFormat.format(rb.getString("VisitNumber"), new Object[]{stops, train.getName(), expectedArrivalTime, rl.getTrainDirectionString()}));
								else
									addLine(fileOut, MessageFormat.format(rb.getString("VisitNumberTerminates"), new Object[]{stops, train.getName(), expectedArrivalTime, rl.getName()}));
							}
						} else {
							stops--;	// don't bump stop count, same location
							// Does the train reverse direction?
							if (rl.getTrainDirection() != rlPrevious.getTrainDirection())
								addLine(fileOut, MessageFormat.format(rb.getString("TrainDirectionChange"), new Object[]{train.getName(), rl.getTrainDirectionString()}));
						}
					} else {
						newLine(fileOut);
						addLine(fileOut, MessageFormat.format(rb.getString("ScheduledWork"), new Object[]{train.getName(), train.getDescription()}));										
						if (train.isTrainInRoute()){
							if (!trainDone){
								addLine(fileOut, MessageFormat.format(rb.getString("DepartedExpected"), new Object[]{train.getTrainDepartsName(), expectedArrivalTime, rl.getTrainDirectionString()}));
							}
						} else {
							if (r == 0 && routeList.size()>1)
								addLine(fileOut, MessageFormat.format(rb.getString("DepartsAt"), new Object[]{train.getTrainDepartsName(), rl.getTrainDirectionString(), train.getDepartureTime()}));
							else if (routeList.size()>1)
								addLine(fileOut, MessageFormat.format(rb.getString("DepartsAtExpectedArrival"), new Object[]{train.getTrainDepartsName(), train.getDepartureTime(), expectedArrivalTime, rl.getTrainDirectionString()}));
						}
					}
					// go through the list of engines and determine if the engine departs here
					
					pickupEngines(fileOut, enginesList, rl);

					// get a list of cars and determine if this location is serviced
					// block cars by destination
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
					
					dropEngines(fileOut, enginesList, rl);
					
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
			if (trainDone && pickupCars == 0 && dropCars == 0){
				addLine(fileOut, rb.getString("TrainDone"));
			} else {
				if (stops > 1 && pickupCars == 0){
					addLine(fileOut, rb.getString("NoCarPickUps"));
				}

				if (stops > 1 && dropCars == 0){
					addLine(fileOut, rb.getString("NoCarDrops"));
				}
			}
		}
		// Are there any cars that need to be found?
		getCarsLocationUnknown(fileOut);
		fileOut.flush();
		fileOut.close();
	}

	public void printSwitchList(Location location, boolean preview) {
		File buildFile = TrainManagerXml.instance().getSwitchListFile(location.getName());
		TrainPrintUtilities.printReport(buildFile, rb.getString("SwitchList")
				+ " " + location.getName(), preview, Setup.getFontName(),
				false, Setup.getManifestLogoURL(), location.getDefaultPrinterName());
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainSwitchLists.class.getName());
}
