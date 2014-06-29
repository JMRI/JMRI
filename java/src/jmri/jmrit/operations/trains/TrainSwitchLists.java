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
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
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
	private static final boolean isManifest = false;

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

		PrintWriter fileOut = null;
		try {
			fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append),
					"UTF-8")), true); // NOI18N
		} catch (IOException e) {
			log.error("Can not open switchlist file: "+file.getName());
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
			TrainSchedule sch = TrainScheduleManager.instance()
					.getScheduleById(trainManager.getTrainScheduleActiveId());
			if (sch != null)
				valid = valid + " (" + sch.getName() + ")";
		}

		// get a list of trains sorted by arrival time
		List<Train> trains = trainManager.getTrainsArrivingThisLocationList(location);
		for (Train train : trains) {
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
				log.debug("No work for train (" + train.getName() + ") at location (" + location.getName() + ")");
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
			List<Engine> engineList = engineManager.getByTrainBlockingList(train);
			List<Car> carList = carManager.getByTrainDestinationList(train);
			List<RouteLocation> routeList = route.getLocationsBySequenceList();
			// does the train stop once or more at this location?
			for (int r = 0; r < routeList.size(); r++) {
				RouteLocation rl = routeList.get(r);
				if (splitString(rl.getName()).equals(splitString(location.getName()))) {
					String expectedArrivalTime = train.getExpectedArrivalTime(rl);
					if (expectedArrivalTime.equals("-1")) { // NOI18N
						trainDone = true;
					}
					if (stops > 1) {
						// Print visit number only if previous location wasn't the same
						RouteLocation rlPrevious = routeList.get(r - 1);
						if (!splitString(rl.getName()).equals(splitString(rlPrevious.getName()))) {
							newLine(fileOut);
							if (train.isTrainInRoute()) {
								if (expectedArrivalTime.equals("-1")) // NOI18N
									newLine(fileOut, MessageFormat.format(TrainSwitchListText
											.getStringVisitNumberDone(), new Object[] { stops, train.getIconName() }));
								else if (r != routeList.size() - 1)
									newLine(fileOut, MessageFormat.format(TrainSwitchListText
											.getStringVisitNumberDeparted(), new Object[] { stops, train.getIconName(),
											expectedArrivalTime, rl.getTrainDirectionString() }));
								else
									newLine(fileOut, MessageFormat.format(TrainSwitchListText
											.getStringVisitNumberTerminatesDeparted(), new Object[] { stops,
											train.getIconName(), expectedArrivalTime, splitString(rl.getName()) }));
							} else {
								if (r != routeList.size() - 1)
									newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringVisitNumber(),
											new Object[] { stops, train.getIconName(), expectedArrivalTime,
													rl.getTrainDirectionString() }));
								else
									newLine(fileOut, MessageFormat.format(TrainSwitchListText
											.getStringVisitNumberTerminates(), new Object[] { stops,
											train.getIconName(), expectedArrivalTime, splitString(rl.getName()) }));
							}
						} else {
							stops--; // don't bump stop count, same location
							// Does the train reverse direction?
							if (rl.getTrainDirection() != rlPrevious.getTrainDirection())
								newLine(fileOut, MessageFormat.format(TrainSwitchListText
										.getStringTrainDirectionChange(), new Object[] { train.getIconName(),
										rl.getTrainDirectionString() }));
						}
					} else {
						newLine(fileOut);
						newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringScheduledWork(),
								new Object[] { train.getIconName(), train.getDescription() }));
						if (train.isTrainInRoute()) {
							if (!trainDone) {
								newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringDepartedExpected(),
										new Object[] { splitString(train.getTrainDepartsName()), expectedArrivalTime,
												rl.getTrainDirectionString() }));
							}
						} else {
							if (r == 0 && routeList.size() > 1)
								newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringDepartsAt(),
										new Object[] { splitString(train.getTrainDepartsName()),
												rl.getTrainDirectionString(), train.getFormatedDepartureTime() }));
							else if (routeList.size() > 1)
								newLine(fileOut, MessageFormat.format(TrainSwitchListText
										.getStringDepartsAtExpectedArrival(), new Object[] {
										splitString(train.getTrainDepartsName()), train.getFormatedDepartureTime(),
										expectedArrivalTime, rl.getTrainDirectionString() }));
						}
					}
					
					// add route comment
					if (Setup.isSwitchListRouteLocationCommentEnabled() && !rl.getComment().trim().equals(""))
						newLine(fileOut, rl.getComment());
					
					if (Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
						pickupEngines(fileOut, engineList, rl, isManifest);
						dropEngines(fileOut, engineList, rl, isManifest);
						blockCarsByTrack(fileOut, train, carList, routeList, rl, r, true, isManifest);
					} else if (Setup.getManifestFormat().equals(Setup.TWO_COLUMN_FORMAT)) {
						blockLocosTwoColumn(fileOut, engineList, rl, isManifest);
						blockCarsByTrackTwoColumn(fileOut, train, carList, routeList, rl, r, true, isManifest);
					} else {
						blockLocosTwoColumn(fileOut, engineList, rl, isManifest);
						blockCarsByTrackNameTwoColumn(fileOut, train, carList, routeList, rl, r, true, isManifest);
					}
					if (Setup.isPrintHeadersEnabled() || !Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT))
						printHorizontalLine(fileOut, isManifest);
						
					stops++;
				}
			}
			if (trainDone && !pickupCars && !dropCars) {
				newLine(fileOut, MessageFormat.format(TrainSwitchListText.getStringTrainDone(), new Object[] { train }));
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
		addCarsLocationUnknown(fileOut, isManifest);
		fileOut.flush();
		fileOut.close();
	}

	public void printSwitchList(Location location, boolean isPreview) {
		File buildFile = TrainManagerXml.instance().getSwitchListFile(location.getName());
		if (!buildFile.exists()) {
			log.warn("Switch list file missing for location (" + location.getName() + ")");
			return;
		}
		if (isPreview && Setup.isManifestEditorEnabled()) {
			TrainPrintUtilities.openDesktopEditor(buildFile);
		} else {
			TrainPrintUtilities.printReport(buildFile, Bundle.getMessage("SwitchList") + " " + location.getName(),
					isPreview, Setup.getFontName(), false, Setup.getManifestLogoURL(),
					location.getDefaultPrinterName(), Setup.getSwitchListOrientation(), Setup.getManifestFontSize());
		}
		if (!isPreview) {
			location.setStatus(Location.PRINTED);
			location.setSwitchListState(Location.SW_PRINTED);
		}
	}

	protected void newLine(PrintWriter file, String string) {
		newLine(file, string, isManifest);
	}

	static Logger log = LoggerFactory.getLogger(TrainSwitchLists.class.getName());
}
