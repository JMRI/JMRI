package jmri.jmrit.operations.trains;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.util.FileUtil;

/**
 * Builds a switch list for a location on the railroad
 *
 * @author Daniel Boudreau (C) Copyright 2008, 2011, 2012, 2013, 2015, 2024
 */
public class TrainSwitchLists extends TrainCommon {

    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
    private static final char FORM_FEED = '\f';
    private static final boolean IS_PRINT_HEADER = true;

    String messageFormatText = ""; // the text being formated in case there's an exception

    /**
     * Builds a switch list for a location showing the work by train arrival
     * time. If not running in real time, new train work is appended to the end
     * of the file. User has the ability to modify the text of the messages
     * which can cause an IllegalArgumentException. Some messages have more
     * arguments than the default message allowing the user to customize the
     * message to their liking. There also an option to list all of the car work
     * by track name. This option is only available in real time and is shown
     * after the switch list by train.
     *
     * @param location The Location needing a switch list
     */
    public void buildSwitchList(Location location) {

        boolean append = false; // add text to end of file when true
        boolean checkFormFeed = true; // used to determine if FF needed between trains

        // Append switch list data if not operating in real time
        if (!Setup.isSwitchListRealTime()) {
            if (!location.getStatus().equals(Location.MODIFIED) && !Setup.isSwitchListAllTrainsEnabled()) {
                return; // nothing to add
            }
            append = location.getSwitchListState() == Location.SW_APPEND;
            location.setSwitchListState(Location.SW_APPEND);
        }

        log.debug("Append: {} for location ({})", append, location.getName());

        // create switch list file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createSwitchListFile(location.getName());

        PrintWriter fileOut = null;
        try {
            fileOut = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8)), true);
        } catch (IOException e) {
            log.error("Can not open switchlist file: {}", e.getLocalizedMessage());
            return;
        }
        try {
            // build header
            if (!append) {
                newLine(fileOut, Setup.getRailroadName());
                newLine(fileOut);
                newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText.getStringSwitchListFor(),
                        new Object[]{location.getSplitName()}));
                if (!location.getSwitchListCommentWithColor().isEmpty()) {
                    newLine(fileOut, location.getSwitchListCommentWithColor());
                }
            } else {
                newLine(fileOut);
            }

            // get a list of built trains sorted by arrival time
            List<Train> trains = trainManager.getTrainsArrivingThisLocationList(location);
            for (Train train : trains) {
                if (!Setup.isSwitchListRealTime() && train.getSwitchListStatus().equals(Train.PRINTED)) {
                    continue; // already printed this train
                }
                Route route = train.getRoute();
                // TODO throw exception? only built trains should be in the list, so no route is
                // an error
                if (route == null) {
                    continue; // no route for this train
                } // determine if train works this location
                boolean works = isThereWorkAtLocation(train, location);
                if (!works && !Setup.isSwitchListAllTrainsEnabled()) {
                    log.debug("No work for train ({}) at location ({})", train.getName(), location.getName());
                    continue;
                }
                // we're now going to add to the switch list
                if (checkFormFeed) {
                    if (append && !Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL)) {
                        fileOut.write(FORM_FEED);
                    }
                    if (Setup.isPrintValidEnabled()) {
                        newLine(fileOut, getValid());
                    }
                } else if (!Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL)) {
                    fileOut.write(FORM_FEED);
                }
                checkFormFeed = false; // done with FF for this train
                // some cars booleans and the number of times this location get's serviced
                _pickupCars = false; // when true there was a car pick up
                _dropCars = false; // when true there was a car set out
                int stops = 1;
                boolean trainDone = false;
                // get engine and car lists
                List<Engine> engineList = engineManager.getByTrainBlockingList(train);
                List<Car> carList = carManager.getByTrainDestinationList(train);
                List<RouteLocation> routeList = route.getLocationsBySequenceList();
                RouteLocation rlPrevious = null;
                // does the train stop once or more at this location?
                for (RouteLocation rl : routeList) {
                    if (!rl.getSplitName().equals(location.getSplitName())) {
                        rlPrevious = rl;
                        continue;
                    }
                    if (train.getExpectedArrivalTime(rl).equals(Train.ALREADY_SERVICED)) {
                        trainDone = true;
                    }
                    // first time at this location?
                    if (stops == 1) {
                        firstTimeMessages(fileOut, train, rl);
                        stops++;
                    } else {
                        // multiple visits to this location
                        // Print visit number only if previous location isn't the same
                        if (rlPrevious == null ||
                                !rl.getSplitName().equals(rlPrevious.getSplitName())) {
                            multipleVisitMessages(fileOut, train, rl, rlPrevious, stops);
                            stops++;
                        } else {
                            // don't bump stop count, same location
                            // Does the train reverse direction?
                            reverseDirectionMessage(fileOut, train, rl, rlPrevious);
                        }
                    }

                    // save current location in case there's back to back location with the same name
                    rlPrevious = rl;

                    // add route comment
                    if (Setup.isSwitchListRouteLocationCommentEnabled() && !rl.getComment().trim().isEmpty()) {
                        newLine(fileOut, rl.getCommentWithColor());
                    }

                    printTrackComments(fileOut, rl, carList, !IS_MANIFEST);

                    if (isThereWorkAtLocation(carList, engineList, rl)) {
                        // now print out the work for this location
                        if (Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
                            pickupEngines(fileOut, engineList, rl, !IS_MANIFEST);
                            // if switcher show loco drop at end of list
                            if (train.isLocalSwitcher()) {
                                blockCarsByTrack(fileOut, train, carList, rl, IS_PRINT_HEADER, !IS_MANIFEST);
                                dropEngines(fileOut, engineList, rl, !IS_MANIFEST);
                            } else {
                                dropEngines(fileOut, engineList, rl, !IS_MANIFEST);
                                blockCarsByTrack(fileOut, train, carList, rl, IS_PRINT_HEADER, !IS_MANIFEST);
                            }
                        } else if (Setup.getManifestFormat().equals(Setup.TWO_COLUMN_FORMAT)) {
                            blockLocosTwoColumn(fileOut, engineList, rl, !IS_MANIFEST);
                            blockCarsTwoColumn(fileOut, train, carList, rl, IS_PRINT_HEADER, !IS_MANIFEST);
                        } else {
                            blockLocosTwoColumn(fileOut, engineList, rl, !IS_MANIFEST);
                            blockCarsByTrackNameTwoColumn(fileOut, train, carList, rl, IS_PRINT_HEADER, !IS_MANIFEST);
                        }
                        // print horizontal line if there was work and enabled
                        if (Setup.isPrintHeadersEnabled() || !Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
                            printHorizontalLine(fileOut, !IS_MANIFEST);
                        }
                    }

                    // done with work, now print summary for this location if we're done
                    if (rl != train.getTrainTerminatesRouteLocation()) {
                        RouteLocation nextRl = train.getRoute().getNextRouteLocation(rl);
                        if (rl.getSplitName().equals(nextRl.getSplitName())) {
                            continue; // the current location name is the "same" as the next
                        }
                        // print departure text if not a switcher
                        if (!train.isLocalSwitcher() && !trainDone) {
                            departureMessages(fileOut, train, rl);
                        }
                    }
                }
                // report if no pick ups or set outs or train has left
                trainSummaryMessages(fileOut, train, location, trainDone, stops);
            }

            // now report car movement by tracks at location
            reportByTrack(fileOut, location);

        } catch (IllegalArgumentException e) {
            newLine(fileOut, Bundle.getMessage("ErrorIllegalArgument",
                    Bundle.getMessage("TitleSwitchListText"), e.getLocalizedMessage()));
            newLine(fileOut, messageFormatText);
            log.error("Illegal argument", e);
        }

        // Are there any cars that need to be found?
        addCarsLocationUnknown(fileOut, !IS_MANIFEST);
        fileOut.flush();
        fileOut.close();
        location.setStatus(Location.UPDATED);
    }

    private String getValid() {
        String valid = MessageFormat.format(messageFormatText = TrainManifestText.getStringValid(),
                new Object[]{getDate(true)});
        if (Setup.isPrintTrainScheduleNameEnabled()) {
            TrainSchedule sch = InstanceManager.getDefault(TrainScheduleManager.class).getActiveSchedule();
            if (sch != null) {
                valid = valid + " (" + sch.getName() + ")";
            }
        }
        return valid;
    }

    /*
     * Messages for the switch list when the train first arrives
     */
    private void firstTimeMessages(PrintWriter fileOut, Train train, RouteLocation rl) {
        String expectedArrivalTime = train.getExpectedArrivalTime(rl);
        newLine(fileOut);
        newLine(fileOut,
                MessageFormat.format(messageFormatText = TrainSwitchListText.getStringScheduledWork(),
                        new Object[]{train.getName(), train.getDescription()}));
        if (train.isTrainEnRoute()) {
            if (!expectedArrivalTime.equals(Train.ALREADY_SERVICED)) {
                // Departed {0}, expect to arrive in {1}, arrives {2}bound
                newLine(fileOut,
                        MessageFormat.format(
                                messageFormatText = TrainSwitchListText.getStringDepartedExpected(),
                                new Object[]{splitString(train.getTrainDepartsName()),
                                        expectedArrivalTime, rl.getTrainDirectionString(),
                                        train.getCurrentLocationName()}));
            }
        } else if (!train.isLocalSwitcher()) {
            // train hasn't departed
            if (rl == train.getTrainDepartsRouteLocation()) {
                // Departs {0} {1}bound at {2}
                newLine(fileOut, MessageFormat.format(
                        messageFormatText = TrainSwitchListText.getStringDepartsAt(),
                        new Object[]{splitString(train.getTrainDepartsName()),
                                rl.getTrainDirectionString(),
                                train.getFormatedDepartureTime()}));
            } else if (Setup.isUseSwitchListDepartureTimeEnabled() &&
                    rl != train.getTrainTerminatesRouteLocation()) {
                // Departs {0} at {1} expected arrival {2}, arrives {3}bound
                newLine(fileOut, MessageFormat.format(
                        messageFormatText = TrainSwitchListText.getStringDepartsAtExpectedArrival(),
                        new Object[]{splitString(rl.getName()),
                                train.getExpectedDepartureTime(rl), expectedArrivalTime,
                                rl.getTrainDirectionString()}));
            } else {
                // Departs {0} at {1} expected arrival {2}, arrives {3}bound
                newLine(fileOut, MessageFormat.format(
                        messageFormatText = TrainSwitchListText.getStringDepartsAtExpectedArrival(),
                        new Object[]{splitString(train.getTrainDepartsName()),
                                train.getFormatedDepartureTime(), expectedArrivalTime,
                                rl.getTrainDirectionString()}));
            }
        }
    }

    /*
     * Messages when a train services the location two or more times
     */
    private void multipleVisitMessages(PrintWriter fileOut, Train train, RouteLocation rl, RouteLocation rlPrevious,
            int stops) {
        String expectedArrivalTime = train.getExpectedArrivalTime(rl);
        if (rlPrevious == null ||
                !rl.getSplitName().equals(rlPrevious.getSplitName())) {
            if (Setup.getSwitchListPageFormat().equals(Setup.PAGE_PER_VISIT)) {
                fileOut.write(FORM_FEED);
            }
            newLine(fileOut);
            if (train.isTrainEnRoute()) {
                if (expectedArrivalTime.equals(Train.ALREADY_SERVICED)) {
                    // Visit number {0} for train ({1})
                    newLine(fileOut,
                            MessageFormat.format(
                                    messageFormatText = TrainSwitchListText.getStringVisitNumberDone(),
                                    new Object[]{stops, train.getName(), train.getDescription()}));
                } else if (rl != train.getTrainTerminatesRouteLocation()) {
                    // Visit number {0} for train ({1}) expect to arrive in {2}, arrives {3}bound
                    newLine(fileOut, MessageFormat.format(
                            messageFormatText = TrainSwitchListText.getStringVisitNumberDeparted(),
                            new Object[]{stops, train.getName(), expectedArrivalTime,
                                    rl.getTrainDirectionString(), train.getDescription()}));
                } else {
                    // Visit number {0} for train ({1}) expect to arrive in {2}, terminates {3}
                    newLine(fileOut,
                            MessageFormat.format(
                                    messageFormatText = TrainSwitchListText
                                            .getStringVisitNumberTerminatesDeparted(),
                                    new Object[]{stops, train.getName(), expectedArrivalTime,
                                            rl.getSplitName(), train.getDescription()}));
                }
            } else {
                // train hasn't departed
                if (rl != train.getTrainTerminatesRouteLocation()) {
                    // Visit number {0} for train ({1}) expected arrival {2}, arrives {3}bound
                    newLine(fileOut,
                            MessageFormat.format(
                                    messageFormatText = TrainSwitchListText.getStringVisitNumber(),
                                    new Object[]{stops, train.getName(), expectedArrivalTime,
                                            rl.getTrainDirectionString(), train.getDescription()}));
                    if (Setup.isUseSwitchListDepartureTimeEnabled()) {
                        // Departs {0} {1}bound at {2}
                        newLine(fileOut, MessageFormat.format(
                                messageFormatText = TrainSwitchListText.getStringDepartsAt(),
                                new Object[]{splitString(rl.getName()),
                                        rl.getTrainDirectionString(),
                                        train.getExpectedDepartureTime(rl)}));
                    }
                } else {
                    // Visit number {0} for train ({1}) expected arrival {2}, terminates {3}
                    newLine(fileOut, MessageFormat.format(
                            messageFormatText = TrainSwitchListText.getStringVisitNumberTerminates(),
                            new Object[]{stops, train.getName(), expectedArrivalTime,
                                    rl.getSplitName(), train.getDescription()}));
                }
            }
        }
    }

    private void reverseDirectionMessage(PrintWriter fileOut, Train train, RouteLocation rl, RouteLocation rlPrevious) {
        // Does the train reverse direction?
        if (rl.getTrainDirection() != rlPrevious.getTrainDirection() &&
                !TrainSwitchListText.getStringTrainDirectionChange().isEmpty()) {
            // Train ({0}) direction change, departs {1}bound
            newLine(fileOut,
                    MessageFormat.format(
                            messageFormatText = TrainSwitchListText.getStringTrainDirectionChange(),
                            new Object[]{train.getName(), rl.getTrainDirectionString(),
                                    train.getDescription(), train.getTrainTerminatesName()}));
        }
    }

    /*
     * Train departure messages at the end of the switch list
     */
    private void departureMessages(PrintWriter fileOut, Train train, RouteLocation rl) {
        String trainDeparts = "";
        if (Setup.isPrintLoadsAndEmptiesEnabled()) {
            int emptyCars = train.getNumberEmptyCarsInTrain(rl);
            // Train departs {0} {1}bound with {2} loads, {3} empties, {4} {5}, {6} tons
            trainDeparts = MessageFormat.format(TrainSwitchListText.getStringTrainDepartsLoads(),
                    new Object[]{rl.getSplitName(),
                            rl.getTrainDirectionString(),
                            train.getNumberCarsInTrain(rl) - emptyCars, emptyCars,
                            train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
                            train.getTrainWeight(rl), train.getTrainTerminatesName(),
                            train.getName()});
        } else {
            // Train departs {0} {1}bound with {2} cars, {3} {4}, {5} tons
            trainDeparts = MessageFormat.format(TrainSwitchListText.getStringTrainDepartsCars(),
                    new Object[]{rl.getSplitName(),
                            rl.getTrainDirectionString(), train.getNumberCarsInTrain(rl),
                            train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
                            train.getTrainWeight(rl), train.getTrainTerminatesName(),
                            train.getName()});
        }
        newLine(fileOut, trainDeparts);
    }

    private void trainSummaryMessages(PrintWriter fileOut, Train train, Location location, boolean trainDone,
            int stops) {
        if (trainDone && !_pickupCars && !_dropCars) {
            // Default message: Train ({0}) has serviced this location
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText.getStringTrainDone(),
                    new Object[]{train.getName(), train.getDescription(), location.getSplitName()}));
        } else {
            if (stops > 1 && !_pickupCars) {
                // Default message: No car pick ups for train ({0}) at this location
                newLine(fileOut,
                        MessageFormat.format(messageFormatText = TrainSwitchListText.getStringNoCarPickUps(),
                                new Object[]{train.getName(), train.getDescription(),
                                        location.getSplitName()}));
            }
            if (stops > 1 && !_dropCars) {
                // Default message: No car set outs for train ({0}) at this location
                newLine(fileOut,
                        MessageFormat.format(messageFormatText = TrainSwitchListText.getStringNoCarDrops(),
                                new Object[]{train.getName(), train.getDescription(),
                                        location.getSplitName()}));
            }
        }
    }

    private void reportByTrack(PrintWriter fileOut, Location location) {
        if (Setup.isPrintTrackSummaryEnabled() && Setup.isSwitchListRealTime()) {
            clearUtilityCarTypes(); // list utility cars by quantity
            if (Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL)) {
                newLine(fileOut);
                newLine(fileOut);
            } else {
                fileOut.write(FORM_FEED);
            }
            newLine(fileOut,
                    MessageFormat.format(messageFormatText = TrainSwitchListText.getStringSwitchListByTrack(),
                            new Object[]{location.getSplitName()}));

            // we only need the cars delivered to or at this location
            List<Car> rsList = carManager.getByTrainList();
            List<Car> carList = new ArrayList<>();
            for (Car rs : rsList) {
                if ((rs.getLocation() != null &&
                        rs.getLocation().getSplitName().equals(location.getSplitName())) ||
                        (rs.getDestination() != null &&
                                rs.getSplitDestinationName().equals(location.getSplitName())))
                    carList.add(rs);
            }

            List<String> trackNames = new ArrayList<>(); // locations and tracks can have "similar" names, only list
                                                         // track names once
            for (Location loc : locationManager.getLocationsByNameList()) {
                if (!loc.getSplitName().equals(location.getSplitName()))
                    continue;
                for (Track track : loc.getTracksByNameList(null)) {
                    String trackName = track.getSplitName();
                    if (trackNames.contains(trackName))
                        continue;
                    trackNames.add(trackName);

                    String trainName = ""; // for printing train message once
                    newLine(fileOut);
                    newLine(fileOut, trackName); // print out just the track name
                    // now show the cars pickup and holds for this track
                    for (Car car : carList) {
                        if (!car.getSplitTrackName().equals(trackName)) {
                            continue;
                        }
                        // is the car scheduled for pickup?
                        if (car.getRouteLocation() != null) {
                            if (car.getRouteLocation().getLocation().getSplitName()
                                    .equals(location.getSplitName())) {
                                // cars are sorted by train name, print train message once
                                if (!trainName.equals(car.getTrainName())) {
                                    trainName = car.getTrainName();
                                    newLine(fileOut, MessageFormat.format(
                                            messageFormatText = TrainSwitchListText.getStringScheduledWork(),
                                            new Object[]{car.getTrainName(), car.getTrain().getDescription()}));
                                    printPickupCarHeader(fileOut, !IS_MANIFEST, !IS_TWO_COLUMN_TRACK);
                                }
                                if (car.isUtility()) {
                                    pickupUtilityCars(fileOut, carList, car, false, !IS_MANIFEST);
                                } else {
                                    pickUpCar(fileOut, car, !IS_MANIFEST);
                                }
                            }
                            // car holds
                        } else if (car.isUtility()) {
                            String s = pickupUtilityCars(carList, car, !IS_MANIFEST, !IS_TWO_COLUMN_TRACK);
                            if (s != null) {
                                newLine(fileOut, TrainSwitchListText.getStringHoldCar().split("\\{")[0] + s.trim()); // NOI18N
                            }
                        } else {
                            newLine(fileOut,
                                    MessageFormat.format(messageFormatText = TrainSwitchListText.getStringHoldCar(),
                                            new Object[]{
                                                    padAndTruncateIfNeeded(car.getRoadName(),
                                                            InstanceManager.getDefault(CarRoads.class)
                                                                    .getMaxNameLength()),
                                                    padAndTruncateIfNeeded(
                                                            TrainCommon.splitString(car.getNumber()),
                                                            Control.max_len_string_print_road_number),
                                                    padAndTruncateIfNeeded(
                                                            car.getTypeName().split(TrainCommon.HYPHEN)[0],
                                                            InstanceManager.getDefault(CarTypes.class)
                                                                    .getMaxNameLength()),
                                                    padAndTruncateIfNeeded(
                                                            car.getLength() + Setup.getLengthUnitAbv(),
                                                            Control.max_len_string_length_name),
                                                    padAndTruncateIfNeeded(car.getLoadName(),
                                                            InstanceManager.getDefault(CarLoads.class)
                                                                    .getMaxNameLength()),
                                                    padAndTruncateIfNeeded(trackName,
                                                            locationManager.getMaxTrackNameLength()),
                                                    padAndTruncateIfNeeded(car.getColor(), InstanceManager
                                                            .getDefault(CarColors.class).getMaxNameLength())}));
                        }
                    }
                    // now do set outs at this location
                    for (Car car : carList) {
                        if (!car.getSplitDestinationTrackName().equals(trackName)) {
                            continue;
                        }
                        if (car.getRouteDestination() != null &&
                                car.getRouteDestination().getLocation().getSplitName()
                                        .equals(location.getSplitName())) {
                            // cars are sorted by train name, print train message once
                            if (!trainName.equals(car.getTrainName())) {
                                trainName = car.getTrainName();
                                newLine(fileOut, MessageFormat.format(
                                        messageFormatText = TrainSwitchListText.getStringScheduledWork(),
                                        new Object[]{car.getTrainName(), car.getTrain().getDescription()}));
                                printDropCarHeader(fileOut, !IS_MANIFEST, !IS_TWO_COLUMN_TRACK);
                            }
                            if (car.isUtility()) {
                                setoutUtilityCars(fileOut, carList, car, false, !IS_MANIFEST);
                            } else {
                                dropCar(fileOut, car, !IS_MANIFEST);
                            }
                        }
                    }
                }
            }
        }
    }

    public void printSwitchList(Location location, boolean isPreview) {
        File switchListFile = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(location.getName());
        if (!switchListFile.exists()) {
            log.warn("Switch list file missing for location ({})", location.getName());
            return;
        }
        if (isPreview && Setup.isManifestEditorEnabled()) {
            TrainUtilities.openDesktop(switchListFile);
        } else {
            TrainPrintUtilities.printReport(switchListFile, location.getName(), isPreview, Setup.getFontName(), false,
                    FileUtil.getExternalFilename(Setup.getManifestLogoURL()), location.getDefaultPrinterName(),
                    Setup.getSwitchListOrientation(), Setup.getManifestFontSize(), Setup.isPrintPageHeaderEnabled());
        }
        if (!isPreview) {
            location.setStatus(Location.PRINTED);
            location.setSwitchListState(Location.SW_PRINTED);
        }
    }

    protected void newLine(PrintWriter file, String string) {
        if (!string.isEmpty()) {
            newLine(file, string, !IS_MANIFEST);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainSwitchLists.class);
}
