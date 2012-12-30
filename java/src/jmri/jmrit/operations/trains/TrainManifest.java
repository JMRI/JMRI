// TrainManifest.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;

import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a train's manifest.
 * 
 * @author Daniel Boudreau Copyright (C) 2011, 2012
 * @version $Revision: 1 $
 */
public class TrainManifest extends TrainCommon {

	int cars = 0;
	int emptyCars = 0;
	boolean newWork = false;

	public TrainManifest(Train train) {
		// create manifest file
		File file = TrainManagerXml.instance().createTrainManifestFile(train.getName());
		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
		} catch (IOException e) {
			log.error("can not open train manifest file");
			return;
		}
		// build header
		if (!train.getRailroadName().equals(""))
			newLine(fileOut, train.getRailroadName());
		else
			newLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);
		newLine(fileOut,
				MessageFormat.format(Bundle.getString("ManifestForTrain"), new Object[] { train.getName(),
						train.getDescription() }));

		String valid = MessageFormat.format(Bundle.getString("Valid"), new Object[] { getDate() });

		if (Setup.isPrintTimetableNameEnabled()) {
			TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(
					TrainManager.instance().getTrainScheduleActiveId());
			if (sch != null)
				valid = valid + " (" + sch.getName() + ")";
		}
		if (Setup.isPrintValidEnabled())
			newLine(fileOut, valid);

		if (!train.getComment().equals(""))
			newLine(fileOut, train.getComment());

		List<String> engineList = engineManager.getByTrainList(train);
		// pickupEngines(fileOut, engineList, train.getTrainDepartsRouteLocation());

		if (Setup.isPrintRouteCommentsEnabled() && !train.getRoute().getComment().equals(""))
			newLine(fileOut, train.getRoute().getComment());

		List<String> carList = carManager.getByTrainDestinationList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");

		boolean work = false;
		String previousRouteLocationName = null;
		List<String> routeList = train.getRoute().getLocationsBySequenceList();

		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(r));
			boolean oldWork = work;
			work = isThereWorkAtLocation(carList, engineList, rl);

			// print info only if new location
			String routeLocationName = splitString(rl.getName());
			if (!routeLocationName.equals(previousRouteLocationName)
					|| (routeLocationName.equals(previousRouteLocationName) && oldWork == false
							&& work == true && newWork == false)) {
				if (work) {
					// add line break between locations without work and ones with work
					// TODO sometimes an extra line break appears when the user has two or more locations with the
					// "same" name
					// and the second location doesn't have work
					if (oldWork == false)
						newLine(fileOut);
					newWork = true;
					String expectedArrivalTime = train.getExpectedArrivalTime(rl);
					String workAt = MessageFormat.format(Bundle.getString("ScheduledWorkAt"),
							new Object[] { routeLocationName });
					if (!train.isShowArrivalAndDepartureTimesEnabled()) {
						newLine(fileOut, workAt);
					} else if (r == 0) {
						newLine(fileOut,
								workAt
										+ MessageFormat.format(Bundle.getString("departureTime"),
												new Object[] { train.getFormatedDepartureTime() }));
					} else if (!rl.getDepartureTime().equals("")) {
						newLine(fileOut,
								workAt
										+ MessageFormat.format(Bundle.getString("departureTime"),
												new Object[] { rl.getFormatedDepartureTime() }));
					} else if (Setup.isUseDepartureTimeEnabled() && r != routeList.size() - 1) {
						newLine(fileOut,
								workAt
										+ MessageFormat
												.format(Bundle.getString("departureTime"),
														new Object[] { train
																.getExpectedDepartureTime(rl) }));
					} else if (!expectedArrivalTime.equals("-1")) {
						newLine(fileOut,
								workAt
										+ MessageFormat.format(Bundle.getString("estimatedArrival"),
												new Object[] { expectedArrivalTime }));
					} else {
						newLine(fileOut, workAt);
					}
					// add route comment
					if (!rl.getComment().trim().equals(""))
						newLine(fileOut, rl.getComment());
				}
				// add location comment
				if (Setup.isPrintLocationCommentsEnabled()
						&& !rl.getLocation().getComment().equals(""))
					newLine(fileOut, rl.getLocation().getComment());
			}

			// engine change or helper service?
			if (train.getSecondLegOptions() != Train.NONE) {
				if (rl == train.getSecondLegStartLocation())
					printChange(fileOut, rl, train.getSecondLegOptions());
				if (rl == train.getSecondLegEndLocation()
						&& train.getSecondLegOptions() == Train.HELPER_ENGINES)
					newLine(fileOut, MessageFormat.format(Bundle.getString("RemoveHelpersAt"),
							new Object[] { splitString(rl.getName()) }));
			}
			if (train.getThirdLegOptions() != Train.NONE) {
				if (rl == train.getThirdLegStartLocation())
					printChange(fileOut, rl, train.getThirdLegOptions());
				if (rl == train.getThirdLegEndLocation()
						&& train.getThirdLegOptions() == Train.HELPER_ENGINES)
					newLine(fileOut, MessageFormat.format(Bundle.getString("RemoveHelpersAt"),
							new Object[] { splitString(rl.getName()) }));
			}

			pickupEngines(fileOut, engineList, rl, Setup.getManifestOrientation());

			if (Setup.isSortByTrackEnabled())
				blockCarsByTrack(fileOut, train, carList, routeList, rl, r);
			else
				blockCarsByPickUpAndSetOut(fileOut, train, carList, routeList, rl, r);

			dropEngines(fileOut, engineList, rl, Setup.getManifestOrientation());

			if (r != routeList.size() - 1) {
				// Is the next location the same as the previous?
				RouteLocation rlNext = train.getRoute().getLocationById(routeList.get(r + 1));
				if (!routeLocationName.equals(splitString(rlNext.getName()))) {
					if (newWork) {
						// Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
						String trainDeparts = MessageFormat.format(
								Bundle.getString("TrainDepartsCars"),
								new Object[] { routeLocationName, rl.getTrainDirectionString(),
										cars, train.getTrainLength(rl),
										Setup.getLengthUnit().toLowerCase(),
										train.getTrainWeight(rl) });
						// Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
						if (Setup.isPrintLoadsAndEmptiesEnabled())
							trainDeparts = MessageFormat.format(
									Bundle.getString("TrainDepartsLoads"),
									new Object[] { routeLocationName, rl.getTrainDirectionString(),
											cars - emptyCars, emptyCars, train.getTrainLength(rl),
											Setup.getLengthUnit().toLowerCase(),
											train.getTrainWeight(rl) });
						newLine(fileOut, trainDeparts);
						newWork = false;
						newLine(fileOut);
					} else {
						// no work at this location
						String s = MessageFormat.format(Bundle.getString("NoScheduledWorkAt"),
								new Object[] { routeLocationName });
						// if a route comment, then only use location name and route comment, useful for passenger
						// trains
						if (!rl.getComment().equals("")) {
							s = routeLocationName;
							if (rl.getComment().trim().length() > 0)
								s = s + ", " + rl.getComment();
						}
						if (train.isShowArrivalAndDepartureTimesEnabled()) {
							if (r == 0)
								s = s
										+ MessageFormat.format(Bundle.getString("departureTime"),
												new Object[] { train.getDepartureTime() });
							else if (!rl.getDepartureTime().equals(""))
								s = s
										+ MessageFormat.format(Bundle.getString("departureTime"),
												new Object[] { rl.getFormatedDepartureTime() });
							else if (Setup.isUseDepartureTimeEnabled()
									&& !rl.getComment().equals("") && r != routeList.size() - 1)
								s = s
										+ MessageFormat
												.format(Bundle.getString("departureTime"),
														new Object[] { train
																.getExpectedDepartureTime(rl) });
						}
						newLine(fileOut, s);
					}
				}
			} else {
				newLine(fileOut, MessageFormat.format(Bundle.getString("TrainTerminatesIn"),
						new Object[] { routeLocationName }));
			}
			previousRouteLocationName = routeLocationName;
		}
		// Are there any cars that need to be found?
		addCarsLocationUnknown(fileOut);

		fileOut.flush();
		fileOut.close();

		train.setModified(false);
	}

	/**
	 * Block cars by pick up and set out for each location in a train's route.
	 */
	private void blockCarsByPickUpAndSetOut(PrintWriter fileOut, Train train, List<String> carList,
			List<String> routeList, RouteLocation rl, int r) {
		// block car pick ups by destination
		for (int j = r; j < routeList.size(); j++) {
			RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
			utilityCarTypes.clear(); // list utility cars by quantity
			for (int k = 0; k < carList.size(); k++) {
				Car car = carManager.getById(carList.get(k));
				if (car.getRouteLocation() == rl && car.getRouteDestination() == rld) {
					if (car.isUtility())
						pickupCars(fileOut, carList, car, rl, rld);
					// use truncated format if there's a switch list
					else if (Setup.isTruncateManifestEnabled()
							&& rl.getLocation().isSwitchListEnabled())
						pickUpCarTruncated(fileOut, car);
					else
						pickUpCar(fileOut, car);
					cars++;
					newWork = true;
					if (CarLoads.instance().getLoadType(car.getType(), car.getLoad())
							.equals(CarLoad.LOAD_TYPE_EMPTY))
						emptyCars++;
				}
			}
		}
		utilityCarTypes.clear(); // list utility cars by quantity
		for (int j = 0; j < carList.size(); j++) {
			Car car = carManager.getById(carList.get(j));
			if (car.getRouteDestination() == rl) {
				if (car.isUtility())
					setoutCars(
							fileOut,
							carList,
							car,
							rl,
							car.getRouteLocation().equals(car.getRouteDestination())
									&& car.getTrack() != null);
				// use truncated format if there's a switch list
				else if (Setup.isTruncateManifestEnabled()
						&& rl.getLocation().isSwitchListEnabled())
					truncatedDropCar(fileOut, car);
				else
					dropCar(fileOut, car);
				cars--;
				newWork = true;
				if (CarLoads.instance().getLoadType(car.getType(), car.getLoad())
						.equals(CarLoad.LOAD_TYPE_EMPTY))
					emptyCars--;
			}
		}
	}

	/**
	 * Block cars by track, then pick up and set out for each location in a train's route.
	 */
	private void blockCarsByTrack(PrintWriter fileOut, Train train, List<String> carList,
			List<String> routeList, RouteLocation rl, int r) {
		List<String> trackIds = rl.getLocation().getTrackIdsByNameList(null);
		for (int i = 0; i < trackIds.size(); i++) {
			Track track = rl.getLocation().getTrackById(trackIds.get(i));
			// block car pick ups by destination
			for (int j = r; j < routeList.size(); j++) {
				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
				utilityCarTypes.clear(); // list utility cars by quantity
				for (int k = 0; k < carList.size(); k++) {
					Car car = carManager.getById(carList.get(k));
					if (car.getRouteLocation() == rl && track == car.getTrack()
							&& car.getRouteDestination() == rld) {
						if (car.isUtility())
							pickupCars(fileOut, carList, car, rl, rld);
						// use truncated format if there's a switch list
						else if (Setup.isTruncateManifestEnabled()
								&& rl.getLocation().isSwitchListEnabled())
							pickUpCarTruncated(fileOut, car);
						else
							pickUpCar(fileOut, car);
						cars++;
						newWork = true;
						if (CarLoads.instance().getLoadType(car.getType(), car.getLoad())
								.equals(CarLoad.LOAD_TYPE_EMPTY))
							emptyCars++;
					}
				}
			}
			utilityCarTypes.clear(); // list utility cars by quantity
			for (int j = 0; j < carList.size(); j++) {
				Car car = carManager.getById(carList.get(j));
				if (car.getRouteDestination() == rl && track == car.getDestinationTrack()) {
					if (car.isUtility())
						setoutCars(
								fileOut,
								carList,
								car,
								rl,
								car.getRouteLocation().equals(car.getRouteDestination())
										&& car.getTrack() != null);
					// use truncated format if there's a switch list
					else if (Setup.isTruncateManifestEnabled()
							&& rl.getLocation().isSwitchListEnabled())
						truncatedDropCar(fileOut, car);
					else
						dropCar(fileOut, car);
					cars--;
					newWork = true;
					if (CarLoads.instance().getLoadType(car.getType(), car.getLoad())
							.equals(CarLoad.LOAD_TYPE_EMPTY))
						emptyCars--;
				}
			}
		}
	}

	// returns true if there's work at location
	private boolean isThereWorkAtLocation(List<String> carList, List<String> engList,
			RouteLocation rl) {
		for (int i = 0; i < carList.size(); i++) {
			Car car = carManager.getById(carList.get(i));
			if (car.getRouteLocation() == rl || car.getRouteDestination() == rl)
				return true;
		}
		for (int i = 0; i < engList.size(); i++) {
			Engine eng = engineManager.getById(engList.get(i));
			if (eng.getRouteLocation() == rl || eng.getRouteDestination() == rl)
				return true;
		}
		return false;
	}

	private void printChange(PrintWriter fileOut, RouteLocation rl, int legOptions) {
		if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES)
			newLine(fileOut,
					MessageFormat.format(Bundle.getString("AddHelpersAt"),
							new Object[] { splitString(rl.getName()) }));
		else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES
				&& ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE || (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE))
			newLine(fileOut, MessageFormat.format(Bundle.getString("EngineAndCabooseChangeAt"),
					new Object[] { splitString(rl.getName()) }));
		else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES)
			newLine(fileOut,
					MessageFormat.format(Bundle.getString("EngineChangeAt"),
							new Object[] { splitString(rl.getName()) }));
		else if ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			newLine(fileOut, MessageFormat.format(Bundle.getString("CabooseChangeAt"),
					new Object[] { splitString(rl.getName()) }));
	}

	private void newLine(PrintWriter file, String string) {
		newLine(file, string, Setup.getManifestOrientation());
	}
}
