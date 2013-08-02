// TrainSwitchLists.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a switch list for a location on the railroad
 * 
 * @author Daniel Boudreau (C) Copyright 2008, 2011, 2012, 2013
 * @version $Revision: 21846 $
 * 
 */
public class TrainSwitchLists extends TrainCommon {

	TrainManager trainManager = TrainManager.instance();
	char formFeed = '\f';

	/**
	 * builds a switch list for a location
	 * 
	 * @param location
	 *            The Location needing a switch list
	 */
	public void buildSwitchList(Location location) {
		// Append switch list data if not operating in real time
		boolean newTrainsOnly = !Setup.isSwitchListRealTime();
		boolean append = false;
		boolean nextTrain = false;
		if (newTrainsOnly) {
			if (!location.getStatus().equals(Location.MODIFIED) && !Setup.isSwitchListAllTrainsEnabled())
				return; // nothing to add
			append = location.getSwitchListState() == Location.SW_APPEND;
			if (location.getSwitchListState() != Location.SW_APPEND)
				location.setSwitchListState(Location.SW_APPEND);
			location.setStatus(Location.UPDATED);
		}

		// create switch list file
		File file = TrainManagerXml.instance().createSwitchListFile(location.getName());

		PrintWriter fileOut;
		try {
			fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					file, append), "UTF-8")), true);	// NOI18N
		} catch (IOException e) {
			log.error("can not open switchlist file");
			return;
		}
		// build header
		if (!append) {
			newLine(fileOut, Setup.getRailroadName());
			newLine(fileOut);
			newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringSwitchListFor(),
					new Object[] { splitString(location.getName()) }));
			if (!location.getSwitchListComment().equals(""))
				newLine(fileOut, location.getSwitchListComment());
		}

		String valid = MessageFormat.format(TrainManifestText.getStringValid(), new Object[] { getDate(true) });
		if (Setup.isPrintTimetableNameEnabled()) {
			TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(
					trainManager.getTrainScheduleActiveId());
			if (sch != null)
				valid = valid + " (" + sch.getName() + ")";
		}

		// get a list of trains sorted by arrival time
		List<Train> trains = trainManager.getTrainsArrivingThisLocationList(location);
		for (int i = 0; i < trains.size(); i++) {
			Train train = trains.get(i);
			if (!train.isBuilt())
				continue; // train wasn't built so skip
			if (newTrainsOnly && train.getSwitchListStatus().equals(Train.PRINTED))
				continue; // already printed this train
			Route route = train.getRoute();
			if (route == null)
				continue; // no route for this train
			// determine if train works this location
			boolean works = isThereWorkAtLocation(train, location);
			if (!works && !Setup.isSwitchListAllTrainsEnabled()) {
				log.debug("No work for train (" + train.getName() + ") at location (" + location.getName()
						+ ")");
				continue;
			}
			// we're now going to add to the switch list
			if (!nextTrain) {
				if (append && Setup.isSwitchListPagePerTrainEnabled()) {
				fileOut.write(formFeed);
			}
				if (Setup.isPrintValidEnabled())
					newLine(fileOut, valid);
			} else if (Setup.isSwitchListPagePerTrainEnabled()) {
				fileOut.write(formFeed);
			}
			nextTrain = true;
			// some cars booleans and the number of times this location get's serviced
			pickupCars = false;
			dropCars = false;
			int stops = 1;
			boolean trainDone = false;
			// get engine and car lists
			List<String> engineList = engineManager.getByTrainList(train);
			List<String> carList = carManager.getByTrainDestinationList(train);
			List<String> routeList = route.getLocationsBySequenceList();
			// does the train stop once or more at this location?
			for (int r = 0; r < routeList.size(); r++) {
				RouteLocation rl = route.getLocationById(routeList.get(r));
				if (splitString(rl.getName()).equals(splitString(location.getName()))) {
					String expectedArrivalTime = train.getExpectedArrivalTime(rl);
					if (expectedArrivalTime.equals("-1")) { // NOI18N
						trainDone = true;
					}
					if (stops > 1) {
						// Print visit number only if previous location wasn't the same
						RouteLocation rlPrevious = route.getLocationById(routeList.get(r - 1));
						if (!splitString(rl.getName()).equals(splitString(rlPrevious.getName()))) {
							newLine(fileOut);
							if (train.isTrainInRoute()) {
								if (expectedArrivalTime.equals("-1")) // NOI18N
									newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringVisitNumberDone(), new Object[] { stops,
											train.getIconName() }));
								else if (r != routeList.size() - 1)
									newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringVisitNumberDeparted(), new Object[] { stops,
											train.getIconName(), expectedArrivalTime,
											rl.getTrainDirectionString() }));
								else
									newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringVisitNumberTerminatesDeparted(), new Object[] {
											stops, train.getIconName(), expectedArrivalTime,
											splitString(rl.getName()) }));
							} else {
								if (r != routeList.size() - 1)
									newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringVisitNumber(),
											new Object[] { stops, train.getIconName(), expectedArrivalTime,
													rl.getTrainDirectionString() }));
								else
									newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringVisitNumberTerminates(), new Object[] { stops,
											train.getIconName(), expectedArrivalTime,
											splitString(rl.getName()) }));
							}
						} else {
							stops--; // don't bump stop count, same location
							// Does the train reverse direction?
							if (rl.getTrainDirection() != rlPrevious.getTrainDirection())
								newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringTrainDirectionChange(), new Object[] {
										train.getIconName(), rl.getTrainDirectionString() }));
						}
					} else {
						newLine(fileOut);
						newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringScheduledWork(),
								new Object[] { train.getIconName(), train.getDescription() }));
						if (train.isTrainInRoute()) {
							if (!trainDone) {
								newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringDepartedExpected(),
										new Object[] { splitString(train.getTrainDepartsName()),
												expectedArrivalTime, rl.getTrainDirectionString() }));
							}
						} else {
							if (r == 0 && routeList.size() > 1)
								newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringDepartsAt(),
										new Object[] { splitString(train.getTrainDepartsName()),
												rl.getTrainDirectionString(),
												train.getFormatedDepartureTime() }));
							else if (routeList.size() > 1)
								newLine(fileOut, MessageFormat.format(TrainSwitchListText
										.getStringDepartsAtExpectedArrival(), new Object[] {
										splitString(train.getTrainDepartsName()),
										train.getFormatedDepartureTime(), expectedArrivalTime,
										rl.getTrainDirectionString() }));
						}
					}
	
					if (Setup.isTwoColumnFormatEnabled())
						blockLocosTwoColumn(fileOut, engineList, rl, false);
					else
						pickupEngines(fileOut, engineList, rl, Setup.getSwitchListOrientation());

					if (Setup.isTwoColumnFormatEnabled())
						blockCarsByTrackTwoColumn(fileOut, train, carList, routeList, rl, r, false);
					else
						blockCarsByTrack(fileOut, train, carList, routeList, rl, r, false);

					if (!Setup.isTwoColumnFormatEnabled())
						dropEngines(fileOut, engineList, rl, Setup.getSwitchListOrientation());
					stops++;
				}
			}
			if (trainDone && !pickupCars && !dropCars) {
				newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringTrainDone(),
						new Object[] { train }));
			} else {
				if (stops > 1 && !pickupCars) {
					newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringNoCarPickUps(),
							new Object[] { train }));
				}
				if (stops > 1 && !dropCars) {
					newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringNoCarDrops(),
							new Object[] { train }));
				}
			}
		}
		// Are there any cars that need to be found?
		addCarsLocationUnknown(fileOut);
		fileOut.flush();
		fileOut.close();
	}

//	/**
//	 * Block cars by pick up and set out for each location in a train's route.
//	 */
//	private void blockCarsByPickUpAndSetOut(PrintWriter fileOut, Train train, List<String> carList,
//			List<String> routeList, RouteLocation rl) {
//		// block cars by destination
//		for (int j = 0; j < routeList.size(); j++) {
//			RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
//			clearUtilityCarTypes(); // list utility cars by quantity
//			for (int k = 0; k < carList.size(); k++) {
//				Car car = carManager.getById(carList.get(k));
//				if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
//						&& car.getRouteDestination() == rld) {
//					if (car.isUtility())
//						pickupUtilityCars(fileOut, carList, car, rl, rld, false);
//					else
//						switchListPickUpCar(fileOut, car);
//					pickupCars = true;
//				}
//			}
//		}
//
////		clearUtilityCarTypes(); // list utility cars by quantity
//		for (int j = 0; j < carList.size(); j++) {
//			Car car = carManager.getById(carList.get(j));
//			if (car.getRouteDestination() == rl) {
//				if (car.isUtility())
//					setoutUtilityCars(fileOut, carList, car, rl, false);
//				else
//					switchListDropCar(fileOut, car);
//				dropCars = true;
//			}
//		}
//	}

//	/**
//	 * Block cars by track, then pick up and set out for each location in a train's route.
//	 */
//	private void blockCarsByTrack(PrintWriter fileOut, Train train, List<String> carList,
//			List<String> routeList, RouteLocation rl, boolean isManifest) {
//		List<String> trackIds = rl.getLocation().getTrackIdsByNameList(null);
//		List<String> trackNames = new ArrayList<String>();
//		for (int i = 0; i < trackIds.size(); i++) {
//			Track track = rl.getLocation().getTrackById(trackIds.get(i));
//			if (trackNames.contains(splitString(track.getName())))
//				continue;
//			trackNames.add(splitString(track.getName())); // use a track name once
//			// block cars by destination
//			for (int j = 0; j < routeList.size(); j++) {
//				RouteLocation rld = train.getRoute().getLocationById(routeList.get(j));
//				clearUtilityCarTypes(); // list utility cars by quantity
//				for (int k = 0; k < carList.size(); k++) {
//					Car car = carManager.getById(carList.get(k));
//					if (Setup.isSortByTrackEnabled()
//							&& !splitString(track.getName()).equals(splitString(car.getTrackName())))
//						continue;
//					// note that a car in train doesn't have a track assignment
//					if (car.getRouteLocation() == rl && car.getTrack() != null
//							&& car.getRouteDestination() == rld) {
//						if (car.isUtility())
//							pickupUtilityCars(fileOut, carList, car, rl, rld, isManifest);
//						// use truncated format if there's a switch list
//						else if (isManifest && Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
//							pickUpCarTruncated(fileOut, car);
//						else
//							pickUpCar(fileOut, car, isManifest);
//						pickupCars = true;
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
//								splitString(car.getDestinationTrackName())))
//					continue;
//				if (car.getRouteDestination() == rl
//						&& car.getDestinationTrack() != null) {
//					if (car.isUtility())
//						setoutUtilityCars(fileOut, carList, car, rl, isManifest);
//					// use truncated format if there's a switch list
//					else if (isManifest && Setup.isTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled())
//						truncatedDropCar(fileOut, car);
//					else
//						dropCar(fileOut, car, isManifest);
//					dropCars = true;
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
	
//	int lineLength = getLineLength(Setup.getSwitchListOrientation());

//	/**
//	 * Produces a two column format for car pick ups and set outs. Sorted by track and then by destination.
//	 */
//	private void blockCarsByTrackTwoColumn(PrintWriter fileOut, Train train, List<String> carList,
//			List<String> routeList, RouteLocation rl, int r, boolean isManifest) {
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
//					if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
//							&& car.getRouteDestination() == rld) {
//						if (Setup.isSortByTrackEnabled()
//								&& !splitString(track.getName()).equals(splitString(car.getTrackName())))
//							continue;
//						pickupCars = true;
//						String s;
//						if (car.isUtility()) {
//							s = pickupUtilityCars(carList, car, rl, rld, isManifest);
//							if (s == null)
//								continue;
//							s = s.trim();
//						} else {
//							s = pickupCar(car, isManifest).trim();
//						}
//						s = padString(s, lineLength / 2);
//						if (s.length() > lineLength / 2)
//							s = s.substring(0, lineLength / 2);
//						if (isLocalMove(car)) {
//							String sl = appendSetoutString(s, carList, car.getRouteDestination(), car,
//									isManifest);
//							// check for utility car, and local route with two or more locations
//							if (!sl.equals(s)) {
//								s = sl;
//								carList.remove(car.getId()); // done with this car, remove from list
//								k--;
//							}
//						} else {
//							s = appendSetoutString(s, carList, rl, true, isManifest);
//						}
//						newLine(fileOut, s, isManifest);
//					}
//				}
//			}
//			if (!Setup.isSortByTrackEnabled())
//					break;	//done
//		}
//		while (index < carList.size()) {
//			String s = padString("", lineLength / 2);
//			s = appendSetoutString(s, carList, rl, false, isManifest);
//			String test = s.trim();
//			if (test.length() > 0)
//				newLine(fileOut, s, isManifest);
//		}
//	}

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

//	private String appendSetoutString(String s, List<String> carList, RouteLocation rl, Car car, boolean isManfest) {
//		dropCars = true;
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
//		return newS;
//	}

	public void printSwitchList(Location location, boolean isPreview) {
		File buildFile = TrainManagerXml.instance().getSwitchListFile(location.getName());
		if (!buildFile.exists()) {
			log.warn("Switch list file missing for location (" + location.getName() + ")");
			return;
		}
		if (isPreview && Setup.isManifestEditorEnabled()) {
			TrainPrintUtilities.openDesktopEditor(buildFile);
		} else {
			TrainPrintUtilities.printReport(buildFile, Bundle.getMessage("SwitchList") + " "
					+ location.getName(), isPreview, Setup.getFontName(), false, Setup.getManifestLogoURL(),
					location.getDefaultPrinterName(), Setup.getSwitchListOrientation(), Setup
							.getManifestFontSize());
		}
		if (!isPreview) {
			location.setStatus(Location.PRINTED);
			location.setSwitchListState(Location.SW_PRINTED);
		}
	}

	protected void newLine(PrintWriter file, String string) {
		newLine(file, string, Setup.getSwitchListOrientation());
	}

	static Logger log = LoggerFactory.getLogger(TrainSwitchLists.class.getName());
}
