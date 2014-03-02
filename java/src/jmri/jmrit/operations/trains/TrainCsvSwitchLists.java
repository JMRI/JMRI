// TrainCsvSwitchLists.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a comma separated value (csv) switch list for a location on the railroad
 * 
 * @author Daniel Boudreau (C) Copyright 2011, 2013
 * 
 */
public class TrainCsvSwitchLists extends TrainCsvCommon {

	TrainManager trainManager = TrainManager.instance();

	/**
	 * builds a csv file containing the switch list for a location
	 * @param location
	 * @return File
	 */
	public File buildSwitchList(Location location) {
		boolean newTrainsOnly = !Setup.isSwitchListRealTime();
		// create csv switch list file
		File file = TrainManagerXml.instance().createCsvSwitchListFile(location.getName());
		PrintWriter fileOut = null;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")),
					true); // NOI18N
		} catch (IOException e) {
			log.error("can not open cvs switchlist file");
			return null;
		}
		// build header
		addLine(fileOut, HEADER);
		addLine(fileOut, SWL); // this is a switch list
		addLine(fileOut, RN + ESC + Setup.getRailroadName() + ESC);

		addLine(fileOut, LN + ESC + splitString(location.getName()) + ESC);
		addLine(fileOut, PRNTR + ESC + location.getDefaultPrinterName() + ESC);
		addLine(fileOut, VT + getDate(true));

		// get a list of trains
		List<Train> trains = trainManager.getTrainsByTimeList();
		CarManager carManager = CarManager.instance();
		EngineManager engineManager = EngineManager.instance();
		for (int i = 0; i < trains.size(); i++) {
			int pickupCars = 0;
			int dropCars = 0;
			int stops = 1;
			boolean trainDone = false;
			Train train = trains.get(i);
			if (!train.isBuilt())
				continue; // train wasn't built so skip
			if (newTrainsOnly && train.getSwitchListStatus().equals(Train.PRINTED))
				continue; // already printed this train
			List<Car> carList = carManager.getByTrainDestinationList(train);
			List<RollingStock> enginesList = engineManager.getByTrainList(train);
			// does the train stop once or more at this location?
			Route route = train.getRoute();
			if (route == null)
				continue; // no route for this train
			List<RouteLocation> routeList = route.getLocationsBySequenceList();
			for (int r = 0; r < routeList.size(); r++) {
				RouteLocation rl = routeList.get(r);
				if (splitString(rl.getName()).equals(splitString(location.getName()))) {
					String expectedArrivalTime = train.getExpectedArrivalTime(rl);
					if (expectedArrivalTime.equals("-1")) { // NOI18N
						trainDone = true;
					}
					// First time a train stops at a location provide:
					// train name
					// train description
					// if the train has started its route
					// the arrival time or relative time if the train has started its route
					// the departure location
					// the departure time
					// the train's direction when it arrives
					// if it terminate at this location
					if (stops == 1) {
						// newLine(fileOut);
						addLine(fileOut, TN + train.getName());
						addLine(fileOut, TM + train.getDescription());

						if (train.isTrainInRoute()) {
							addLine(fileOut, TIR);
							addLine(fileOut, ETE + expectedArrivalTime);
						} else {
							addLine(fileOut, DL + splitString(splitString(train.getTrainDepartsName())));
							addLine(fileOut, DT + train.getDepartureTime());
							if (r == 0 && routeList.size() > 1)
								addLine(fileOut, TD + splitString(rl.getName()) + DEL + rl.getTrainDirectionString());
							if (r != 0) {
								addLine(fileOut, ETA + expectedArrivalTime);
								addLine(fileOut, TA + splitString(rl.getName()) + DEL + rl.getTrainDirectionString());
							}
						}
						if (r == routeList.size() - 1)
							addLine(fileOut, TT + splitString(rl.getName()));
					}
					if (stops > 1) {
						// Print visit number, etc. only if previous location wasn't the same
						RouteLocation rlPrevious = routeList.get(r - 1);
						if (!splitString(rl.getName()).equals(splitString(rlPrevious.getName()))) {
							// After the first time a train stops at a location provide:
							// if the train has started its route
							// the arrival time or relative time if the train has started its route
							// the train's direction when it arrives
							// if it terminate at this location

							addLine(fileOut, VN + stops);
							if (train.isTrainInRoute()) {
								addLine(fileOut, ETE + expectedArrivalTime);
							} else {
								addLine(fileOut, ETA + expectedArrivalTime);
							}
							addLine(fileOut, TA + splitString(rl.getName()) + DEL + rl.getTrainDirectionString());
							if (r == routeList.size() - 1)
								addLine(fileOut, TT + splitString(rl.getName()));
						} else {
							stops--; // don't bump stop count, same location
							// Does the train change direction?
							if (rl.getTrainDirection() != rlPrevious.getTrainDirection())
								addLine(fileOut, TDC + rl.getTrainDirectionString());
						}
					}
					// go through the list of engines and determine if the engine departs here
					for (int j = 0; j < enginesList.size(); j++) {
						Engine engine = (Engine) enginesList.get(j);
						if (engine.getRouteLocation() == rl && !engine.getTrackName().equals(""))
							fileOutCsvEngine(fileOut, engine, PL);
					}

					// get a list of cars and determine if this location is serviced
					// block cars by destination
					for (int j = 0; j < routeList.size(); j++) {
						RouteLocation rld = routeList.get(j);
						for (int k = 0; k < carList.size(); k++) {
							Car car = carList.get(k);
							if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
									&& car.getRouteDestination() == rld) {
								pickupCars++;
								int count = 0;
								if (car.isUtility()) {
									count = countPickupUtilityCars(carList, car, rl, rld, false);
									if (count == 0)
										continue; // already done this set of utility cars
								}
								fileOutCsvCar(fileOut, car, PC, count);
							}
						}
					}

					for (int j = 0; j < enginesList.size(); j++) {
						Engine engine = (Engine) enginesList.get(j);
						if (engine.getRouteDestination() == rl)
							fileOutCsvEngine(fileOut, engine, SL);
					}

					for (int j = 0; j < carList.size(); j++) {
						Car car = carList.get(j);
						if (car.getRouteDestination() == rl) {
							dropCars++;
							int count = 0;
							if (car.isUtility()) {
								count = countSetoutUtilityCars(carList, car, rl, false, false);
								if (count == 0)
									continue; // already done this set of utility cars
							}
							fileOutCsvCar(fileOut, car, SC, count);
						}
					}
					stops++;
				}
			}
			if (trainDone && pickupCars == 0 && dropCars == 0) {
				addLine(fileOut, TDONE);
			} else {
				if (stops > 1 && pickupCars == 0) {
					addLine(fileOut, NCPU);
				}

				if (stops > 1 && dropCars == 0) {
					addLine(fileOut, NCSO);
				}
			}
		}
		// TODO Are there any cars that need to be found?
		// getCarsLocationUnknown(fileOut);
		addLine(fileOut, END); // done with switch list
		fileOut.flush();
		fileOut.close();
		return file;
	}

	static Logger log = LoggerFactory.getLogger(TrainCsvSwitchLists.class.getName());
}
