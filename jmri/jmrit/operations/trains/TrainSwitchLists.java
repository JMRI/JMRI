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
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

public class TrainSwitchLists {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

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
		List trains = manager.getTrainsByNameList();
		CarManager carManager = CarManager.instance();
		for (int i=0; i<trains.size(); i++){
			int pickupCars = 0;
			int dropCars = 0;
			int stops = 1;
			Train train = manager.getTrainById((String)trains.get(i));
			if (!train.getBuilt())
				continue;	// train wasn't built so skip
			List carsInTrain = carManager.getCarsByTrainList(train);
			// does the train stop once or more at this location?
			Route route = train.getRoute();
			if (route == null)
				continue;	// no route for this train
			List routeLocations = route.getLocationsBySequenceList();
			for (int r=0; r<routeLocations.size(); r++){
				RouteLocation rl = route.getLocationById((String)routeLocations.get(r));
				if (rl.getName().equals(location.getName())){
					if (stops > 1){
						newLine(fileOut);
						addLine(fileOut, "Visit number "+stops);
					} else {
						newLine(fileOut);
						addLine(fileOut, "Scheduled work for Train (" + train.getName() +") "+train.getDescription());
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
				addLine(fileOut, "No car pickups for this train at this location");
			}
	
			if (stops > 1 && dropCars == 0){
				addLine(fileOut, "No car drops for this train at this location");
			}
		}
		fileOut.flush();
		fileOut.close();
	}
	
	private void  pickupCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		String[] carType = car.getType().split("-"); // ignore lading
		String carComment = (Setup.isAppendCarCommentEnabled() ? " "+car.getComment() : "");
		addLine(file, rb.getString("Pickup")+" " + car.getRoad() + " "
				+ carNumber[0] + " " + carType[0] + " "
				+ car.getLength() + " " + car.getColor()
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+") " : " ")
				+ (car.hasFred() ? " ("+rb.getString("fred")+") " : " ") + rb.getString("from")+ " "
				+ car.getTrackName() + carComment);
	}
	
	private void dropCar(PrintWriter file, Car car){
		String[] carNumber = car.getNumber().split("-"); // ignore any duplicate car numbers
		String[] carType = car.getType().split("-"); // ignore lading
		String carComment = (Setup.isAppendCarCommentEnabled() ? " "+car.getComment() : "");
		addLine(file, rb.getString("Drop")+ " " + car.getRoad() + " "
				+ carNumber[0] + " " + carType[0] + " "
				+ car.getLength() + " " + car.getColor()
				+ (car.isHazardous() ? " ("+rb.getString("Hazardous")+") " : " ")
				+ rb.getString("to") + " " + car.getDestinationTrackName()
				+ carComment);
	}
	
	// writes string to console and file
	private void addLine (PrintWriter file, String string){
		if(log.isDebugEnabled())
			log.debug(string);
		if (file != null)
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
