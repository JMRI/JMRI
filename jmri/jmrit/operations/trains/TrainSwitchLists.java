// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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

public class TrainSwitchLists extends TrainCommon {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	private static final String FEET = Setup.FEET;
	private static final String BOX = " [ ] ";

	TrainManager manager = TrainManager.instance();
	
	// builds a switchlist for a location
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
		addLine(fileOut, "Switchlist for " + location.getName());
		addLine(fileOut, "Valid " + new Date());
		
		// get a list of trains
		List trains = manager.getTrainsByTimeList();
		CarManager carManager = CarManager.instance();
		EngineManager engineManager = EngineManager.instance();
		for (int i=0; i<trains.size(); i++){
			int pickupCars = 0;
			int dropCars = 0;
			int stops = 1;
			Train train = manager.getTrainById((String)trains.get(i));
			if (!train.getBuilt())
				continue;	// train wasn't built so skip
			List carList = carManager.getCarsByTrainList(train);
			List enginesList = engineManager.getEnginesByTrainList(train);
			// does the train stop once or more at this location?
			Route route = train.getRoute();
			if (route == null)
				continue;	// no route for this train
			List routeList = route.getLocationsBySequenceList();
			for (int r=0; r<routeList.size(); r++){
				RouteLocation rl = route.getLocationById((String)routeList.get(r));
				if (rl.getName().equals(location.getName())){
					if (stops > 1){
						newLine(fileOut);
						addLine(fileOut, "Visit number "+stops);
					} else {
						newLine(fileOut);
						addLine(fileOut, "Scheduled work for Train (" + train.getName() +") "+train.getDescription());
						String expected = "";
						if (r != 0)
							expected = " expected arrival " + train.getExpectedArrivalTime(rl);
						if (train.isTrainInRoute()){
							addLine(fileOut, "Departed "+train.getTrainDepartsName()+ ", expect to arrive in "+ train.getExpectedArrivalTime(rl));
						} else {
							addLine(fileOut, "Departs "+train.getTrainDepartsName()+" at " + train.getDepartureTime() + expected);
						}
					}
					// go through the list of engines and determine if the engine departs here
					for (int j = 0; j < enginesList.size(); j++) {
						Engine engine = engineManager
								.getEngineById((String) enginesList.get(j));
						if (engine.getRouteLocation() == rl	&& !engine.getTrackName().equals("")){
							pickupEngine(fileOut, engine);
						}
					}
					// get a list of cars and determine if this location is serviced
//					block cars by destination
					for (int j = 0; j < routeList.size(); j++) {
						RouteLocation rld = train.getRoute().getLocationById((String) routeList.get(j));
						for (int k = 0; k < carList.size(); k++) {
							Car car = carManager.getCarById((String) carList.get(k));
							if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
									&& car.getRouteDestination() == rld) {
								pickupCar(fileOut, car);
								pickupCars++;
							}
						}
					}
					for (int j = 0; j < enginesList.size(); j++) {
						Engine engine = engineManager
								.getEngineById((String) enginesList.get(j));
						if (engine.getRouteDestination() == rl){
							dropEngine(fileOut, engine);
						}
					}
					for (int j=0; j<carList.size(); j++){
						Car car = carManager.getCarById((String)carList.get(j));
						if (car.getRouteDestination() == rl){
							dropCar(fileOut, car);
							dropCars++;
						}
					}
					stops++;
				}
			}
			if (stops > 1 && pickupCars == 0){
				addLine(fileOut, "No car pickups for this train at this location");
			}
	
			if (stops > 1 && dropCars == 0){
				addLine(fileOut, "No car drops for this train at this location");
			}
		}
		fileOut.flush();
		fileOut.close();
	}

	public void printSwitchList(Location location, boolean preview){
		File buildFile = TrainManagerXml.instance().getSwitchListFile(location.getName());
		Train.printReport(buildFile, "Switchlist " + location.getName(), preview, Setup.getFontName(), false);
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(TrainSwitchLists.class.getName());
}
