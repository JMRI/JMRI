// TrainManifest.java

package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a train's manifest.
 * 
 * @author Daniel Boudreau Copyright (C) 2011, 2012, 2013
 * @version $Revision: 1 $
 */
public class TrainManifest extends TrainCommon {

	public TrainManifest(Train train) {
		// create manifest file
		File file = TrainManagerXml.instance().createTrainManifestFile(train.getName());
		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8")), true);	// NOI18N
		} catch (IOException e) {
			log.error("can not open train manifest file");
			return;
		}
		// build header
		if (!train.getRailroadName().equals(""))
			newLine(fileOut, train.getRailroadName());
		else
			newLine(fileOut, Setup.getRailroadName());
		newLine(fileOut);	// empty line
		newLine(fileOut, MessageFormat.format(TrainManifestText.getStringManifestForTrain(), new Object[] {
				train.getName(), train.getDescription() }));

		String valid = MessageFormat.format(TrainManifestText.getStringValid(), new Object[] { getDate(true) });

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
					|| (work && !oldWork && !newWork)) {
				if (work) {
					// add line break between locations without work and ones with work
					// TODO sometimes an extra line break appears when the user has two or more locations with the
					// "same" name
					// and the second location doesn't have work
					if (!oldWork)
						newLine(fileOut);
					newWork = true;
					String expectedArrivalTime = train.getExpectedArrivalTime(rl);
					String workAt = MessageFormat.format(TrainManifestText.getStringScheduledWork(),
							new Object[] { routeLocationName });
					if (!train.isShowArrivalAndDepartureTimesEnabled()) {
						newLine(fileOut, workAt);
					} else if (r == 0) {
						newLine(fileOut, MessageFormat.format(TrainManifestText.getStringWorkDepartureTime(),
								new Object[] { routeLocationName, train.getFormatedDepartureTime() }));
					} else if (!rl.getDepartureTime().equals("")) {
						newLine(fileOut, MessageFormat.format(TrainManifestText.getStringWorkDepartureTime(),
								new Object[] { routeLocationName, rl.getFormatedDepartureTime() }));
					} else if (Setup.isUseDepartureTimeEnabled() && r != routeList.size() - 1) {
						newLine(fileOut, MessageFormat.format(TrainManifestText.getStringWorkDepartureTime(),
								new Object[] { routeLocationName, train.getExpectedDepartureTime(rl) }));
					} else if (!expectedArrivalTime.equals("-1")) {// NOI18N
						newLine(fileOut, MessageFormat.format(TrainManifestText.getStringWorkArrivalTime(),
								new Object[] { routeLocationName, expectedArrivalTime }));
					} else {
						newLine(fileOut, workAt);
					}
					// add route comment
					if (!rl.getComment().trim().equals(""))
						newLine(fileOut, rl.getComment());
					
					printTrackComments(fileOut, rl, carList);
					
					// add location comment
					if (Setup.isPrintLocationCommentsEnabled() && !rl.getLocation().getComment().equals(""))
						newLine(fileOut, rl.getLocation().getComment());					
				}
			}

			// engine change or helper service?
			if (train.getSecondLegOptions() != Train.NONE) {
				if (rl == train.getSecondLegStartLocation())
					printChange(fileOut, rl, train.getSecondLegOptions());
				if (rl == train.getSecondLegEndLocation()
						&& train.getSecondLegOptions() == Train.HELPER_ENGINES)
					newLine(fileOut, MessageFormat.format(TrainManifestText.getStringRemoveHelpers(),
							new Object[] { splitString(rl.getName()) }));
			}
			if (train.getThirdLegOptions() != Train.NONE) {
				if (rl == train.getThirdLegStartLocation())
					printChange(fileOut, rl, train.getThirdLegOptions());
				if (rl == train.getThirdLegEndLocation()
						&& train.getThirdLegOptions() == Train.HELPER_ENGINES)
					newLine(fileOut, MessageFormat.format(TrainManifestText.getStringRemoveHelpers(),
							new Object[] { splitString(rl.getName()) }));
			}

			if (Setup.isTwoColumnFormatEnabled())
				blockLocosTwoColumn(fileOut, engineList, rl, true);
			else
				pickupEngines(fileOut, engineList, rl, Setup.getManifestOrientation());

			if (Setup.isTwoColumnFormatEnabled())
				blockCarsByTrackTwoColumn(fileOut, train, carList, routeList, rl, r, true);
			else
				blockCarsByTrack(fileOut, train, carList, routeList, rl, r, true);

			if (!Setup.isTwoColumnFormatEnabled())
				dropEngines(fileOut, engineList, rl, Setup.getManifestOrientation());

			if (r != routeList.size() - 1) {
				// Is the next location the same as the previous?
				RouteLocation rlNext = train.getRoute().getLocationById(routeList.get(r + 1));
				if (!routeLocationName.equals(splitString(rlNext.getName()))) {
					if (newWork) {
						// Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
						String trainDeparts = MessageFormat.format(TrainManifestText.getStringTrainDepartsCars(),
								new Object[] { routeLocationName, rl.getTrainDirectionString(), cars,
										train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
										train.getTrainWeight(rl) });
						// Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
						if (Setup.isPrintLoadsAndEmptiesEnabled())
							trainDeparts = MessageFormat.format(TrainManifestText.getStringTrainDepartsLoads(),
									new Object[] { routeLocationName, rl.getTrainDirectionString(),
											cars - emptyCars, emptyCars, train.getTrainLength(rl),
											Setup.getLengthUnit().toLowerCase(), train.getTrainWeight(rl) });
						newLine(fileOut, trainDeparts);
						newWork = false;
						newLine(fileOut);
					} else {
						// no work at this location
						String s = MessageFormat.format(TrainManifestText.getStringNoScheduledWork(),
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
										+ MessageFormat.format(TrainManifestText.getStringDepartTime(),
												new Object[] { train.getDepartureTime() });
							else if (!rl.getDepartureTime().equals(""))
								s = s
										+ MessageFormat.format(TrainManifestText.getStringDepartTime(),
												new Object[] { rl.getFormatedDepartureTime() });
							else if (Setup.isUseDepartureTimeEnabled() && !rl.getComment().equals("")
									&& r != routeList.size() - 1)
								s = s
										+ MessageFormat.format(TrainManifestText.getStringDepartTime(),
												new Object[] { train.getExpectedDepartureTime(rl) });
						}
						newLine(fileOut, s);
						
						// add location comment
						if (Setup.isPrintLocationCommentsEnabled() && !rl.getLocation().getComment().equals(""))
							newLine(fileOut, rl.getLocation().getComment());
					}
				}
			} else {
				newLine(fileOut, MessageFormat.format(TrainManifestText.getStringTrainTerminates(),
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

//	/**
//	 * Block cars by pick up and set out for each location in a train's route.
//	 */
//	private void blockCarsByPickUpAndSetOut(PrintWriter fileOut, Train train, List<String> carList,
//			List<String> routeList, RouteLocation rl, int r) {
//		// block car pick ups by destination
//		for (int j = r; j < routeList.size(); j++) {
//			RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
//			clearUtilityCarTypes(); // list utility cars by quantity
//			for (int k = 0; k < carList.size(); k++) {
//				Car car = carManager.getById(carList.get(k));
//				if (car.getRouteLocation() == rl && car.getRouteDestination() == rld) {
//					if (car.isUtility())
//						pickupUtilityCars(fileOut, carList, car, rl, rld, true);
//					// use truncated format if there's a switch list
//					else if (Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
//						pickUpCarTruncated(fileOut, car);
//					else
//						pickUpCar(fileOut, car);
//					cars++;
//					newWork = true;
//					if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
//							CarLoad.LOAD_TYPE_EMPTY))
//						emptyCars++;
//				}
//			}
//		}
////		clearUtilityCarTypes(); // list utility cars by quantity
//		for (int j = 0; j < carList.size(); j++) {
//			Car car = carManager.getById(carList.get(j));
//			if (car.getRouteDestination() == rl) {
//				if (car.isUtility())
//					setoutUtilityCars(fileOut, carList, car, rl, true);
//				// use truncated format if there's a switch list
//				else if (Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
//					truncatedDropCar(fileOut, car);
//				else
//					dropCar(fileOut, car);
//				cars--;
//				newWork = true;
//				if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
//						CarLoad.LOAD_TYPE_EMPTY))
//					emptyCars--;
//			}
//		}
//	}

//	/**
//	 * Block cars by track, then pick up and set out for each location in a train's route.
//	 */
//	private void blockCarsByTrack(PrintWriter fileOut, Train train, List<String> carList,
//			List<String> routeList, RouteLocation rl, int r) {
//		List<String> trackIds = rl.getLocation().getTrackIdsByNameList(null);
//		List<String> trackNames = new ArrayList<String>();
//		for (int i = 0; i < trackIds.size(); i++) {
//			Track track = rl.getLocation().getTrackById(trackIds.get(i));
//			if (trackNames.contains(splitString(track.getName())))
//				continue;
//			trackNames.add(splitString(track.getName())); // use a track name once
//			// block car pick ups by destination
//			for (int j = r; j < routeList.size(); j++) {
//				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
//				clearUtilityCarTypes(); // list utility cars by quantity
//				for (int k = 0; k < carList.size(); k++) {
//					Car car = carManager.getById(carList.get(k));
//					if (Setup.isSortByTrackEnabled()
//							&& !splitString(track.getName()).equals(splitString(car.getTrackName())))
//						continue;
//					if (car.getRouteLocation() == rl && car.getRouteDestination() == rld) {
//						if (car.isUtility())
//							pickupUtilityCars(fileOut, carList, car, rl, rld, true);
//						// use truncated format if there's a switch list
//						else if (Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
//							pickUpCarTruncated(fileOut, car);
//						else
//							pickUpCar(fileOut, car);
//						cars++;
//						newWork = true;
//						if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
//								CarLoad.LOAD_TYPE_EMPTY))
//							emptyCars++;
//					}
//				}
//			}
//			for (int j = 0; j < carList.size(); j++) {
//				Car car = carManager.getById(carList.get(j));
//				if (Setup.isSortByTrackEnabled()
//						&& !splitString(track.getName()).equals(
//								splitString(car.getDestinationTrack().getName())))
//					continue;
//				if (car.getRouteDestination() == rl) {
//					if (car.isUtility())
//						setoutUtilityCars(fileOut, carList, car, rl, true);
//					// use truncated format if there's a switch list
//					else if (Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
//						truncatedDropCar(fileOut, car);
//					else
//						dropCar(fileOut, car);
//					cars--;
//					newWork = true;
//					if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
//							CarLoad.LOAD_TYPE_EMPTY))
//						emptyCars--;
//				}
//			}
//			if (!Setup.isSortByTrackEnabled())
//				break; // done
//		}
//	}
//
//	int lineLength = getLineLength(Setup.getManifestOrientation());
	
//	private void blockLocosTwoColumn(PrintWriter fileOut, List<String> engineList, RouteLocation rl) {
//		for (int k = 0; k < engineList.size(); k++) {
//			Engine engine = engineManager.getById(engineList.get(k));
//			if (engine.getRouteLocation() == rl) { 
//				newLine(fileOut, pickupEngine(engine).trim());
//			}
//			if (engine.getRouteDestination() == rl) {
//				String s = padString("", lineLength / 2);
//				s = s + " |" + dropEngine(engine);
//				if (s.length() > lineLength)
//					s = s.substring(0, lineLength);
//				newLine(fileOut, s);
//			}
//		}
//	}
	
//	/**
//	 * Produces a two column format for car pick ups and set outs.  Sorted by track and then by destination.
//	 */
//	private void blockCarsByTrackTwoColumn(PrintWriter fileOut, Train train, List<String> carList,
//			List<String> routeList, RouteLocation rl, int r, boolean isManfest) {
//		index = 0;
//		List<String> trackIds = rl.getLocation().getTrackIdsByNameList(null);
//		List<String> trackNames = new ArrayList<String>();
//		clearUtilityCarTypes(); // list utility cars by quantity
//		for (int i = 0; i < trackIds.size(); i++) {
//			Track track = rl.getLocation().getTrackById(trackIds.get(i));
//			if (trackNames.contains(splitString(track.getName())))
//				continue;
//			trackNames.add(splitString(track.getName())); // use a track name once
//			// block car pick ups by destination
//			for (int j = r; j < routeList.size(); j++) {
//				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
//				for (int k = 0; k < carList.size(); k++) {
//					Car car = carManager.getById(carList.get(k));
//					if (Setup.isSortByTrackEnabled()
//							&& !splitString(track.getName()).equals(splitString(car.getTrackName())))
//						continue;
//					if (car.getRouteLocation() == rl && car.getRouteDestination() == rld) {
//						cars++; // car added to train
//						if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
//								CarLoad.LOAD_TYPE_EMPTY))
//							emptyCars++;
//						String s;
//						if (car.isUtility()) {
//							s = pickupUtilityCars(carList, car, rl, rld, isManfest);
//							if (s == null)
//								continue;
//							s = s.trim();
//						} else {
//							s = pickupCar(car, isManfest).trim();
//						}
//						s = padString(s, lineLength / 2);
//						if (s.length() > lineLength / 2)
//							s = s.substring(0, lineLength / 2);
//						if (isLocalMove(car)) {
//							String sl = appendSetoutString(s, carList, car.getRouteDestination(), car, isManfest);
//							// check for utility car, and local route with two or more locations
//							if (!sl.equals(s)) {
//								s = sl;
//								carList.remove(car.getId());	// done with this car, remove from list
//								k--;
//							}
//						} else {
//							s = appendSetoutString(s, carList, rl, true, isManfest);
//						}
//						newLine(fileOut, s);
//						newWork = true;
//					}
//				}
//			}
//			if (!Setup.isSortByTrackEnabled())
//				break;	//done
//		}
//		while (index < carList.size()) {
//			String s = padString("", lineLength / 2);
//			s = appendSetoutString(s, carList, rl, false, isManfest);
//			String test = s.trim();
//			if (test.length() > 0)
//				newLine(fileOut, s);
//		}
//	}
//
//	int index = 0;
//
//	private String appendSetoutString(String s, List<String> carList, RouteLocation rl, boolean local, boolean isManfest) {
//		while (index < carList.size()) {
//			Car car = carManager.getById(carList.get(index++));
//			if (local && isLocalMove(car))
//				continue;	// skip local moves
//			// car list is already sorted by destination track
//			if (car.getRouteDestination() == rl) {
//				String so = appendSetoutString(s, carList, rl, car, isManfest);
//				// check for utility car
//				if (!so.equals(s))
//					return so;
//			}
//		}
//		return s;
//	}
//
//	private String appendSetoutString(String s, List<String> carList, RouteLocation rl, Car car, boolean isManfest) {
//		cars--; // one less car in the train
//		if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
//				CarLoad.LOAD_TYPE_EMPTY))
//			emptyCars--; // one less empty
//		String newS;
//		// use truncated format if there's a switch list
//		// else if (Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
//		// truncatedDropCar(fileOut, car);
//		if (isLocalMove(car))
//			newS = s + "->";
//		else
//			newS = s + " |";
//		if (car.isUtility()) {
//			String so = setoutUtilityCars(carList, car, rl, false, isManfest);
//			if (so == null)
//				return s;	// no changes to the input string
//			newS = newS + so;
//		} else {
//			newS = newS + dropCar(car, isManfest);
//		}
//		if (newS.length() > lineLength)
//			newS = newS.substring(0, lineLength);
//		newWork = true;
//		return newS;
//	}
	
//	// returns true if there's work at location
//	private boolean isThereWorkAtLocation(List<String> carList, List<String> engList, RouteLocation rl) {
//		for (int i = 0; i < carList.size(); i++) {
//			Car car = carManager.getById(carList.get(i));
//			if (car.getRouteLocation() == rl || car.getRouteDestination() == rl)
//				return true;
//		}
//		for (int i = 0; i < engList.size(); i++) {
//			Engine eng = engineManager.getById(engList.get(i));
//			if (eng.getRouteLocation() == rl || eng.getRouteDestination() == rl)
//				return true;
//		}
//		return false;
//	}

	private void printChange(PrintWriter fileOut, RouteLocation rl, int legOptions) {
		if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES)
			newLine(fileOut, MessageFormat.format(TrainManifestText.getStringAddHelpers(),
					new Object[] { splitString(rl.getName()) }));
		else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES
				&& ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE || (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE))
			newLine(fileOut, MessageFormat.format(TrainManifestText.getStringLocoAndCabooseChange(),
					new Object[] { splitString(rl.getName()) }));
		else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES)
			newLine(fileOut, MessageFormat.format(TrainManifestText.getStringLocoChange(),
					new Object[] { splitString(rl.getName()) }));
		else if ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
				|| (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)
			newLine(fileOut, MessageFormat.format(TrainManifestText.getStringCabooseChange(),
					new Object[] { splitString(rl.getName()) }));
	}

	private void printTrackComments(PrintWriter fileOut, RouteLocation rl, List<String> carList) {
		Location location = rl.getLocation();
		if (location != null) {
			List<String> trackIds = location.getTrackIdsByNameList(null);
			for (int i = 0; i < trackIds.size(); i++) {
				Track track = location.getTrackById(trackIds.get(i));
				// any pick ups or set outs to this track?
				boolean pickup = false;
				boolean setout = false;
				for (int j = 0; j < carList.size(); j++) {
					Car car = carManager.getById(carList.get(j));
					if (car.getRouteLocation() == rl && car.getTrack() != null && car.getTrack() == track)
						pickup = true;
					if (car.getRouteDestination() == rl && car.getDestinationTrack() != null
							&& car.getDestinationTrack() == track)
						setout = true;
				}
				// print the appropriate comment if there's one
				if (pickup && setout && !track.getCommentBoth().equals(""))
					newLine(fileOut, track.getCommentBoth());
				else if (pickup && !setout && !track.getCommentPickup().equals(""))
					newLine(fileOut, track.getCommentPickup());
				else if (!pickup && setout && !track.getCommentSetout().equals(""))
					newLine(fileOut, track.getCommentSetout());
			}
		}
	}

	private void newLine(PrintWriter file, String string) {
		newLine(file, string, Setup.getManifestOrientation());
	}
	
}
