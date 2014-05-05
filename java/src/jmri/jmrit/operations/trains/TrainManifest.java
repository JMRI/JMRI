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
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a train's manifest.
 * 
 * @author Daniel Boudreau Copyright (C) 2011, 2012, 2013
 * @version $Revision: 1 $
 */
public class TrainManifest extends TrainCommon {

	private static final Logger log = LoggerFactory.getLogger(TrainManifest.class);
	private static final boolean isManifest = true;

	public TrainManifest(Train train) {
		// create manifest file
		File file = TrainManagerXml.instance().createTrainManifestFile(train.getName());
		PrintWriter fileOut;

		try {
			fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), // NOI18N
					true);
		} catch (IOException e) {
			log.error("Can not open train manifest file: " + file.getName());
			return;
		}
		// build header
		if (!train.getRailroadName().equals(""))
			newLine(fileOut, train.getRailroadName());
		else
			newLine(fileOut, Setup.getRailroadName());
		newLine(fileOut); // empty line
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

		List<Engine> engineList = engineManager.getByTrainBlockingList(train);

		if (Setup.isPrintRouteCommentsEnabled() && !train.getRoute().getComment().equals(""))
			newLine(fileOut, train.getRoute().getComment());

		List<Car> carList = carManager.getByTrainDestinationList(train);
		log.debug("Train has " + carList.size() + " cars assigned to it");

		boolean work = false;
		String previousRouteLocationName = null;
		List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();

		for (int r = 0; r < routeList.size(); r++) {
			RouteLocation rl = routeList.get(r);
			boolean oldWork = work;
			boolean printHeader = false;
			work = isThereWorkAtLocation(carList, engineList, rl);

			// print info only if new location
			String routeLocationName = splitString(rl.getName());
			if (!routeLocationName.equals(previousRouteLocationName) || (work && !oldWork && !newWork)) {
				if (work) {
					// add line break between locations without work and ones with work
					// TODO sometimes an extra line break appears when the user has two or more locations with the
					// "same" name and the second location doesn't have work
					if (!oldWork)
						newLine(fileOut);
					newWork = true;
					printHeader = true;
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
				if (rl == train.getSecondLegEndLocation() && train.getSecondLegOptions() == Train.HELPER_ENGINES)
					newLine(fileOut, MessageFormat.format(TrainManifestText.getStringRemoveHelpers(),
							new Object[] { splitString(rl.getName()) }));
			}
			if (train.getThirdLegOptions() != Train.NONE) {
				if (rl == train.getThirdLegStartLocation())
					printChange(fileOut, rl, train.getThirdLegOptions());
				if (rl == train.getThirdLegEndLocation() && train.getThirdLegOptions() == Train.HELPER_ENGINES)
					newLine(fileOut, MessageFormat.format(TrainManifestText.getStringRemoveHelpers(),
							new Object[] { splitString(rl.getName()) }));
			}
			
			if (isThereWorkAtLocation(null, engineList, rl))
				printEngineHeader(fileOut, isManifest);

			if (Setup.isTwoColumnFormatEnabled()) {
				blockLocosTwoColumn(fileOut, engineList, rl, isManifest);
			} else {
				pickupEngines(fileOut, engineList, rl, isManifest);
				dropEngines(fileOut, engineList, rl, isManifest);
			}

			if (Setup.isTwoColumnFormatEnabled())
				blockCarsByTrackTwoColumn(fileOut, train, carList, routeList, rl, r, printHeader, isManifest);
			else
				blockCarsByTrack(fileOut, train, carList, routeList, rl, r, printHeader, isManifest);

			if (r != routeList.size() - 1) {
				// Is the next location the same as the previous?
				RouteLocation rlNext = routeList.get(r + 1);
				if (!routeLocationName.equals(splitString(rlNext.getName()))) {
					if (newWork) {
						if (Setup.isPrintHeadersEnabled())
							printHorizontalLine(fileOut, isManifest);
						// Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
						String trainDeparts = MessageFormat.format(TrainManifestText.getStringTrainDepartsCars(),
								new Object[] { routeLocationName, rl.getTrainDirectionString(), cars,
										train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
										train.getTrainWeight(rl) });
						// Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
						if (Setup.isPrintLoadsAndEmptiesEnabled())
							trainDeparts = MessageFormat.format(TrainManifestText.getStringTrainDepartsLoads(),
									new Object[] { routeLocationName, rl.getTrainDirectionString(), cars - emptyCars,
											emptyCars, train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
											train.getTrainWeight(rl) });
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
								s = MessageFormat.format(TrainManifestText.getStringNoScheduledWorkWithRouteComment(),
										new Object[] { routeLocationName, rl.getComment() });
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
				if (Setup.isPrintHeadersEnabled())
					printHorizontalLine(fileOut, isManifest);
				newLine(fileOut, MessageFormat.format(TrainManifestText.getStringTrainTerminates(),
						new Object[] { routeLocationName }));
			}
			previousRouteLocationName = routeLocationName;
		}
		// Are there any cars that need to be found?
		addCarsLocationUnknown(fileOut, isManifest);

		fileOut.flush();
		fileOut.close();

		train.setModified(false);
	}

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

	private void printTrackComments(PrintWriter fileOut, RouteLocation rl, List<Car> carList) {
		Location location = rl.getLocation();
		if (location != null) {
			List<Track> tracks = location.getTrackByNameList(null);
			for (Track track : tracks) {
				// any pick ups or set outs to this track?
				boolean pickup = false;
				boolean setout = false;
				for (int j = 0; j < carList.size(); j++) {
					Car car = carList.get(j);
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
		newLine(file, string, isManifest);
	}

}
