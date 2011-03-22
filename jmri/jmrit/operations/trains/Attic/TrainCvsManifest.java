// TrainCvsManifest.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

public class TrainCvsManifest extends TrainCommon {
	
	EngineManager engineManager = EngineManager.instance();
	CarManager carManager = CarManager.instance();
	LocationManager locationManager = LocationManager.instance();
	
	private String del = ","; 	// delimiter
	public TrainCvsManifest(Train train){
			// create comma separated value manifest file
			File file = TrainManagerXml.instance().createTrainCsvManifestFile(
					train.getName());
			PrintWriter fileOut;

			try {
				fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)),
						true);
			} catch (IOException e) {
				log.error("can not open train csv manifest file");
				return;
			}
			// build header
			addLine(fileOut, "Operator"+del+"Description"+del+"Parameters");
			if (!train.getRailroadName().equals(""))
				addLine(fileOut, "RN"+del+"Railroad Name"+del+train.getRailroadName());
			else
				addLine(fileOut, "RN"+del+"Railroad Name"+del+Setup.getRailroadName());
			addLine(fileOut, "TN"+del+"Train Name"+del+train.getName());
			addLine(fileOut, "TD"+del+"Train Description"+del+train.getDescription());		
			addLine(fileOut, "VT"+del+"Valid"+del+getDate());
			if (!train.getComment().equals("")){
				addLine(fileOut, "TC"+del+"Train Comment"+del+train.getComment());
			}
			
			// get engine and car lists
			List<String> engineList = engineManager.getByTrainList(train);			
			List<String> carList = carManager.getByTrainDestinationList(train);
			
			int cars = 0;
			int emptyCars = 0;
			boolean work = false;
			String previousRouteLocationName = null;
			List<String> routeList = train.getRoute().getLocationsBySequenceList();
			for (int r = 0; r < routeList.size(); r++) {
				RouteLocation rl = train.getRoute().getLocationById(routeList.get(r));
				work = isThereWorkAtLocation(carList, rl);			
				// print info only if new location
				String routeLocationName = splitString(rl.getName());
		       	String locationName = routeLocationName;
	        	if (locationName.contains(del)){
	        		log.debug("location name has delimiter: "+locationName);
	        		locationName = "\""+routeLocationName+"\"";
	        	}
				if (!routeLocationName.equals(previousRouteLocationName)){
					addLine(fileOut, "LN"+del+"Location Name"+del+ locationName);
					if (r == 0){addLine(fileOut, "DT"+del+"Departure Time"+del+train.getDepartureTime());
					} else if (!rl.getDepartureTime().equals("")){
						addLine(fileOut, "DTR"+del+"Departure Time Route"+del+rl.getDepartureTime());
					} else {
						addLine(fileOut, "AT"+del+"Arrival Time"+del+train.getExpectedArrivalTime(rl));
					}
					if (!work){
						// no work at location
						addLine(fileOut, "NW"+del+"No Work");
					}
					// add location comment
					if (Setup.isPrintLocationCommentsEnabled()){
						Location l = locationManager.getLocationByName(rl.getName());
						if (!l.getComment().equals("")){
							String comment = l.getComment();
				        	if (comment.contains(del)){
				        		log.debug("comment has delimiter: "+comment);
				        		comment = "\""+comment+"\"";
				        	}
							addLine(fileOut, "LC"+del+"Location Comment"+del+comment);
						}
					}
				}
				// add route comment
				if (!rl.getComment().equals("")){
					String comment = rl.getComment();
		        	if (comment.contains(del)){
		        		log.debug("route comment has delimiter: "+comment);
		        		comment = "\""+comment+"\"";
		        	}
					addLine(fileOut, "RC"+del+"Route Comment"+del+comment);
				}			
				// engine change or helper service?
				if (train.getSecondLegOptions() != Train.NONE){
					if (rl == train.getSecondLegStartLocation()){
						engineCsvChange(fileOut, rl, train.getSecondLegOptions());
					}
					if (rl == train.getSecondLegEndLocation())
						addLine(fileOut, "RH"+del+"Remove Helpers");
				}
				if (train.getThirdLegOptions() != Train.NONE){
					if (rl == train.getThirdLegStartLocation()){
						engineCsvChange(fileOut, rl, train.getThirdLegOptions());
					}
					if (rl == train.getThirdLegEndLocation())
						addLine(fileOut, "RH"+del+"Remove Helpers");
				}
				
				for (int i =0; i < engineList.size(); i++){
					Engine engine = engineManager.getById(engineList.get(i));
					if (engine.getRouteLocation() == rl)
						fileOutCsvEngine(fileOut, engine, "PL"+del+"Pick up loco");
				}	
				for (int i =0; i < engineList.size(); i++){
					Engine engine = engineManager.getById(engineList.get(i));
					if (engine.getRouteDestination() == rl)
						fileOutCsvEngine(fileOut, engine, "SL"+del+"Set out loco");
				}	

				// block cars by destination
				for (int j = r; j < routeList.size(); j++) {
					RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
					for (int k = 0; k < carList.size(); k++) {
						Car car = carManager.getById(carList.get(k));
						if (car.getRouteLocation() == rl
								&& car.getRouteDestination() == rld) {
							fileOutCsvCar(fileOut, car, "PC"+del+"Pick up car");
							cars++;
							if (car.getLoad().equals(CarLoads.instance().getDefaultEmptyName()))
								emptyCars++;
						}
					}
				}
				// car set outs
				for (int j = 0; j < carList.size(); j++) {
					Car car = carManager.getById(carList.get(j));
					if (car.getRouteDestination() == rl) {
						fileOutCsvCar(fileOut, car, "SC"+del+"Set out car");
						cars--;
						if (car.getLoad().equals(CarLoads.instance().getDefaultEmptyName()))
							emptyCars--;
					}
				}
				if (r != routeList.size() - 1) {
					// Is the next location the same as the previous?
					RouteLocation rlNext = train.getRoute().getLocationById(routeList.get(r+1));
					String nextRouteLocationName = splitString(rlNext.getName());
					if (!routeLocationName.equals(nextRouteLocationName) && work){
						addLine(fileOut, "TD"+del+"Train Departs"+del+locationName+del+rl.getTrainDirectionString());
						addLine(fileOut, "TL"+del+"Train Length"+del+rl.getTrainLength()+del+emptyCars+del+cars);
						addLine(fileOut, "TW"+del+"Train Weight"+del+rl.getTrainWeight());
					}
				} else {
					addLine(fileOut, "TT"+del+"Train Terminates"+del+ locationName);
				}
				previousRouteLocationName = routeLocationName;
			}
			// Are there any cars that need to be found?
			//getCarsLocationUnknown(fileOut);
			
			fileOut.flush();
			fileOut.close();
		}

	// returns true if there's work at location
	private boolean isThereWorkAtLocation(List<String> carList, RouteLocation rl){
		for (int i = 0; i < carList.size(); i++) {
			Car car = carManager.getById(carList.get(i));
			if (car.getRouteLocation() == rl || car.getRouteDestination() == rl)
				return true;
		}
		return false;
	}
	
	private void fileOutCsvCar(PrintWriter fileOut, Car car, String operation){
		// check for delimiter in names
      	String carType = car.getType();
    	if (carType.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in type field: "+carType);
    		carType = "\""+car.getType()+"\"";
    	}
       	String carLocationName = car.getLocationName();
    	if (carLocationName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in location field: "+carLocationName);
    		carLocationName = "\""+car.getLocationName()+"\"";
    	}
    	String carTrackName = car.getTrackName();
    	if (carTrackName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in track field: "+carTrackName);
    		carTrackName = "\""+car.getTrackName()+"\"";
    	}
       	String carDestName = car.getDestinationName();
    	if (carDestName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in destination field: "+carDestName);
    		carDestName = "\""+car.getDestinationName()+"\"";
    	}
    	String carDestTrackName = car.getDestinationTrackName();
    	if (carDestTrackName.contains(del)){
    		log.debug("Car ("+car.toString()+") has delimiter in destination track field: "+carDestTrackName);
    		carDestTrackName = "\""+car.getDestinationTrackName()+"\"";
    	}
		addLine(fileOut, operation 
				+del+car.getRoad()
				+del+car.getNumber()
				+del+carType
				+del+car.getLength()
				+del+car.getLoad()
				+del+car.getColor()								
				+del+carLocationName
				+del+carTrackName
				+del+carDestName
				+del+carDestTrackName
				+del+car.getOwner()
				+del+car.getKernelName()
				+del+car.getComment()
				+del+CarLoads.instance().getPickupComment(car.getType(), car.getLoad())
				+del+CarLoads.instance().getDropComment(car.getType(), car.getLoad())
				+del+(car.isCaboose()?"C":"")
				+del+(car.hasFred()?"F":"")
				+del+(car.isHazardous()?"H":"")
				+del+car.getRfid());
	}
	
	private void fileOutCsvEngine(PrintWriter fileOut, Engine engine, String operation){	
		// check for delimiter in names
		String engineLocationName = engine.getLocationName();
		if (engineLocationName.contains(del)){
			log.debug("Engine ("+engine.toString()+") has delimiter in location field: "+engineLocationName);
			engineLocationName = "\""+engine.getLocationName()+"\"";
		}
		String engineTrackName = engine.getTrackName();
		if (engineTrackName.contains(del)){
			log.debug("Engine ("+engine.toString()+") has delimiter in track field: "+engineTrackName);
			engineTrackName = "\""+engine.getTrackName()+"\"";
		}
		String engineDestName = engine.getDestinationName();
		if (engineDestName.contains(del)){
			log.debug("Engine ("+engine.toString()+") has delimiter in destination field: "+engineDestName);
			engineDestName = "\""+engine.getDestinationName()+"\"";
		}
		String engineDestTrackName = engine.getDestinationTrackName();
		if (engineDestTrackName.contains(del)){
			log.debug("Engine ("+engine.toString()+") has delimiter in destination track field: "+engineDestTrackName);
			engineDestTrackName = "\""+engine.getDestinationTrackName()+"\"";
		}
		addLine(fileOut, operation
				+del+engine.getRoad()
				+del+engine.getNumber()
				+del+engine.getModel()						
				+del+engine.getLength()
				+del+engine.getType()
				+del+engine.getHp()								
				+del+engineLocationName
				+del+engineTrackName
				+del+engineDestName
				+del+engineDestTrackName
				+del+engine.getOwner()
				+del+engine.getConsistName()
				+del+engine.getComment()
				+del+engine.getRfid());
	}
	
	private void engineCsvChange(PrintWriter fileOut, RouteLocation rl, int legOptions){
		if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES)
			addLine(fileOut, "AH"+del+"Add Helpers");
		else if ((legOptions & Train.CHANGE_CABOOSE) == Train.CHANGE_CABOOSE)
			addLine(fileOut, "CC"+del+"Change Locos and Caboose");
		else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES)
			addLine(fileOut, "CL"+del+"Change Locos");
	}
}

