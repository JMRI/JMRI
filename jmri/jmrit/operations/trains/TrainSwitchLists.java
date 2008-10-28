// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

public class TrainSwitchLists {
	
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
		addStatusLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);
		addStatusLine(fileOut, "Switchlist for " + location.getName());
		addStatusLine(fileOut, "Valid " + new Date());
		
		// get a list of trains
		List trains = manager.getTrainsByNameList();
		CarManager carManager = CarManager.instance();
		for (int i=0; i<trains.size(); i++){
			int pickupCars = 0;
			int dropCars = 0;
			int stops = 1;
			Train train = manager.getTrainById((String)trains.get(i));
			List carsInTrain = carManager.getCarsByTrainList(train);
			// does the train stop once or more at this location?
			Route route = train.getRoute();
			List routeLocations = route.getLocationsBySequenceList();
			for (int r=0; r<routeLocations.size(); r++){
				RouteLocation rl = route.getLocationById((String)routeLocations.get(r));
				if (rl.getName().equals(location.getName())){
					if (stops > 1){
						newLine(fileOut);
						addStatusLine(fileOut, "Visit number "+stops);
					} else {
						newLine(fileOut);
						addStatusLine(fileOut, "Scheduled work for Train (" + train.getName() +") "+train.getDescription());
					}
					// get a list of cars and determine if this location is serviced
					for (int j=0; j<carsInTrain.size(); j++){
						Car car = carManager.getCarById((String)carsInTrain.get(j));
						// if car is in train (no track) ignore
						if (car.getRouteLocation() == rl && !car.getTrackName().equals("")){
							pickupCar(fileOut, car);
							pickupCars++;
						}
					}
					for (int j=0; j<carsInTrain.size(); j++){
						Car car = carManager.getCarById((String)carsInTrain.get(j));
						if (car.getRouteDestination() == rl){
							dropCar(fileOut, car);
							dropCars++;
						}
					}
					stops++;
				}
			}
			if (stops > 1 && pickupCars == 0){
				addStatusLine(fileOut, "No car pickups for this train at this location");
			}
	
			if (stops > 1 && dropCars == 0){
				addStatusLine(fileOut, "No car drops for this train at this location");
			}
		}
		fileOut.flush();
		fileOut.close();
	}
	
	private void  pickupCar(PrintWriter file, Car car){
		addStatusLine(file, "Pickup " + car.getRoad() + " "
				+ car.getNumber() + " " + car.getType() + " "
				+ car.getLength() + "' " + car.getColor()
				+ (car.isHazardous() ? " (Hazardous)" : "")
				+ (car.hasFred() ? " (FRED)" : "") + " from "
				+ car.getTrackName());
	}
	
	private void dropCar(PrintWriter file, Car car){
		addStatusLine(file, "Drop " + car.getRoad() + " "
				+ car.getNumber() + " " + car.getType() + " "
				+ car.getLength() + "' " + car.getColor()
				+ (car.isHazardous() ? " (Hazardous)" : "")
				+ " to " + car.getDestinationTrackName());
	}
	
	private void addStatusLine (PrintWriter file, String string){
		if(log.isDebugEnabled())
			log.debug(string);
		file.println(string);
	}
	
	private void newLine (PrintWriter file){
		file.println(" ");
	}
	
	public void printSwitchList(Location location, boolean preview){
		File buildFile = TrainManagerXml.instance().getSwitchListFile(location.getName());
		Train.printReport(buildFile, "Switchlist ", preview, Setup.getFontName());
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(TrainSwitchLists.class.getName());
}
