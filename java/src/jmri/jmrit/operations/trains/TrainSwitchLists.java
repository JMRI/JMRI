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
		
	/**
	 * builds a switch list for a location
	 * @param location The Location needing a switch list
	 * @param newTrainsOnly When true, ignore trains that have already been added to the switch lists
	 */
	public void buildSwitchList(Location location, boolean newTrainsOnly){
		// load message formats
		pickupUtilityMessageFormat = Setup.getSwitchListPickupUtilityCarMessageFormat();
		setoutUtilityMessageFormat = Setup.getSwitchListSetoutUtilityCarMessageFormat();
		localUtilityMessageFormat = Setup.getSwitchListLocalUtilityCarMessageFormat();

		// create manifest file
		File file = TrainManagerXml.instance().createSwitchListFile(location.getName());
		
		// Append switch list data if not operating in real time
		boolean append = !Setup.isSwitchListRealTime() && location.getSwitchListState() == Location.SW_APPEND;
		if (append)
			newTrainsOnly = true;
		if (!Setup.isSwitchListRealTime() && location.getSwitchListState() != Location.SW_APPEND){
			location.setSwitchListState(Location.SW_APPEND);
		}
		
		PrintWriter fileOut;
		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file, append)), true);
		} catch (IOException e) {
			log.error("can not open switchlist file");
			return;
		}
		// build header
		if (!append)
			addLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);
		addLine(fileOut, MessageFormat.format(rb.getString("SwitchListFor"), new Object[]{splitString(location.getName())}));
		String valid = MessageFormat.format(rb.getString("Valid"), new Object[]{getDate()});
		
		if (Setup.isPrintTimetableNameEnabled()){
			TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(manager.getTrainScheduleActiveId());
			if (sch != null)
				valid = valid + " ("+sch.getName()+")"; 
		}
		addLine(fileOut, valid);
		
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
									addLine(fileOut, MessageFormat.format(rb.getString("VisitNumberTerminatesDeparted"), new Object[]{stops, train.getName(), expectedArrivalTime, splitString(rl.getName())}));
							} else {
								if (r != routeList.size()-1)
									addLine(fileOut, MessageFormat.format(rb.getString("VisitNumber"), new Object[]{stops, train.getName(), expectedArrivalTime, rl.getTrainDirectionString()}));
								else
									addLine(fileOut, MessageFormat.format(rb.getString("VisitNumberTerminates"), new Object[]{stops, train.getName(), expectedArrivalTime, splitString(rl.getName())}));
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
								addLine(fileOut, MessageFormat.format(rb.getString("DepartedExpected"), new Object[]{splitString(train.getTrainDepartsName()), expectedArrivalTime, rl.getTrainDirectionString()}));
							}
						} else {
							if (r == 0 && routeList.size()>1)
								addLine(fileOut, MessageFormat.format(rb.getString("DepartsAt"), new Object[]{splitString(train.getTrainDepartsName()), rl.getTrainDirectionString(), train.getFormatedDepartureTime()}));
							else if (routeList.size()>1)
								addLine(fileOut, MessageFormat.format(rb.getString("DepartsAtExpectedArrival"), new Object[]{splitString(train.getTrainDepartsName()), train.getFormatedDepartureTime(), expectedArrivalTime, rl.getTrainDirectionString()}));
						}
					}
					// go through the list of engines and determine if the engine departs here
					
					pickupEngines(fileOut, enginesList, rl);

					// get a list of cars and determine if this location is serviced
					// block cars by destination
					for (int j = 0; j < routeList.size(); j++) {						
						RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
						utilityCarTypes.clear();	// list utility cars by quantity
						for (int k = 0; k < carList.size(); k++) {
							Car car = carManager.getById(carList.get(k));
							if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
									&& car.getRouteDestination() == rld) {
								if (car.isUtility())
									pickupCars(fileOut, carList, car, rl, rld);
								else
									switchListPickUpCar(fileOut, car);
								pickupCars++;
							}
						}
					}
					
					dropEngines(fileOut, enginesList, rl);
					
					utilityCarTypes.clear();	// list utility cars by quantity
					for (int j=0; j<carList.size(); j++){
						Car car = carManager.getById(carList.get(j));
						if (car.getRouteDestination() == rl){
							if (car.isUtility())
								setoutCars(fileOut, carList, car, rl, car.getRouteLocation().equals(car.getRouteDestination()) && car.getTrack()!=null);
							else
								switchListDropCar(fileOut, car);
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

	public void printSwitchList(Location location, boolean isPreview) {
		File buildFile = TrainManagerXml.instance().getSwitchListFile(location.getName());
		if (isPreview && Setup.isManifestEditorEnabled())
			TrainPrintUtilities.openDesktopEditor(buildFile);
		else
			TrainPrintUtilities.printReport(buildFile, rb.getString("SwitchList")
					+ " " + location.getName(), isPreview, Setup.getFontName(),
					false, Setup.getManifestLogoURL(), location.getDefaultPrinterName(), Setup.getSwitchListOrientation());
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainSwitchLists.class.getName());
}
