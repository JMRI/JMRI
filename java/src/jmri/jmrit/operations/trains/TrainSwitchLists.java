package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.timetable.TrainSchedule;
import jmri.jmrit.operations.trains.timetable.TrainScheduleManager;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a switch list for a location on the railroad
 *
 * @author Daniel Boudreau (C) Copyright 2008, 2011, 2012, 2013, 2015
 *
 *
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
     * message to their liking.
     *
     * There also an option to list all of the car work by track name. This option
     * is only available in real time and is shown after the switch list by
     * train.
     *
     * @param location The Location needing a switch list
     */
    public void buildSwitchList(Location location) {
        // Append switch list data if not operating in real time
        boolean newTrainsOnly = !Setup.isSwitchListRealTime();
        boolean append = false; // add text to end of file when true
        boolean checkFormFeed = true; // used to determine if FF needed between trains
        if (newTrainsOnly) {
            if (!location.getStatus().equals(Location.MODIFIED) && !Setup.isSwitchListAllTrainsEnabled()) {
                return; // nothing to add
            }
            append = location.getSwitchListState() == Location.SW_APPEND;
            if (location.getSwitchListState() != Location.SW_APPEND) {
                location.setSwitchListState(Location.SW_APPEND);
            }
            location.setStatus(Location.UPDATED);
        }

        log.debug("Append: {} for location ({})", append, location.getName());

        // create switch list file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createSwitchListFile(location.getName());

        PrintWriter fileOut = null;
        try {
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append),
                    "UTF-8")), true); // NOI18N
        } catch (IOException e) {
            log.error("Can not open switchlist file: {}", file.getName());
            return;
        }
        try {
            // build header
            if (!append) {
                newLine(fileOut, Setup.getRailroadName());
                newLine(fileOut);
                newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText.getStringSwitchListFor(),
                        new Object[]{splitString(location.getName())}));
                if (!location.getSwitchListComment().equals(Location.NONE)) {
                    newLine(fileOut, location.getSwitchListComment());
                }
            }

            String valid = MessageFormat.format(messageFormatText = TrainManifestText.getStringValid(),
                    new Object[]{getDate(true)});
            if (Setup.isPrintTimetableNameEnabled()) {
                TrainSchedule sch = InstanceManager.getDefault(TrainScheduleManager.class).getScheduleById(
                        trainManager.getTrainScheduleActiveId());
                if (sch != null) {
                    valid = valid + " (" + sch.getName() + ")";
                }
            }

            // get a list of trains sorted by arrival time
            List<Train> trains = trainManager.getTrainsArrivingThisLocationList(location);
            for (Train train : trains) {
                if (!train.isBuilt()) {
                    continue; // train wasn't built so skip
                }
                if (newTrainsOnly && train.getSwitchListStatus().equals(Train.PRINTED)) {
                    continue; // already printed this train
                }
                Route route = train.getRoute();
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
                        newLine(fileOut, valid);
                    }
                } else if (!Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL)) {
                    fileOut.write(FORM_FEED);
                }
                checkFormFeed = false; // done with FF for this train
                // some cars booleans and the number of times this location get's serviced
                pickupCars = false; // when true there was a car pick up
                dropCars = false; // when true there was a car set out
                int stops = 1;
                boolean trainDone = false;
                // get engine and car lists
                List<Engine> engineList = engineManager.getByTrainBlockingList(train);
                List<Car> carList = carManager.getByTrainDestinationList(train);
                List<RouteLocation> routeList = route.getLocationsBySequenceList();
                RouteLocation rlPrevious = null;
                // does the train stop once or more at this location?
                for (RouteLocation rl : routeList) {
                    if (!splitString(rl.getName()).equals(splitString(location.getName()))) {
                        rlPrevious = rl;
                        continue;
                    }
                    String expectedArrivalTime = train.getExpectedArrivalTime(rl);
                    if (expectedArrivalTime.equals(Train.ALREADY_SERVICED)) {
                        trainDone = true;
                    }
                    // first time at this location?
                    if (stops == 1) {
                        newLine(fileOut);
                        newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                .getStringScheduledWork(), new Object[]{train.getName(), train.getDescription()}));
                        if (train.isTrainEnRoute()) {
                            if (!trainDone) {
                                newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                        .getStringDepartedExpected(), new Object[]{
                                                splitString(train.getTrainDepartsName()), expectedArrivalTime,
                                                rl.getTrainDirectionString()}));
                            }
                        } else if (!train.isLocalSwitcher()) {
                            if (rl == train.getRoute().getDepartsRouteLocation()) {
                                newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                        .getStringDepartsAt(), new Object[]{splitString(train.getTrainDepartsName()),
                                                rl.getTrainDirectionString(), train.getFormatedDepartureTime()}));
                            } else {
                                newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                        .getStringDepartsAtExpectedArrival(), new Object[]{
                                                splitString(train.getTrainDepartsName()),
                                                train.getFormatedDepartureTime(),
                                                expectedArrivalTime, rl.getTrainDirectionString()}));
                            }
                        }
                    } else {
                        // multiple visits to this location
                        // Print visit number only if previous location wasn't the same
                        if (rlPrevious == null ||
                                !splitString(rl.getName()).equals(splitString(rlPrevious.getName()))) {
                            if (Setup.getSwitchListPageFormat().equals(Setup.PAGE_PER_VISIT)) {
                                fileOut.write(FORM_FEED);
                            }
                            newLine(fileOut);
                            if (train.isTrainEnRoute()) {
                                if (expectedArrivalTime.equals(Train.ALREADY_SERVICED)) {
                                    newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                            .getStringVisitNumberDone(), new Object[]{stops, train.getName(),
                                                    train.getDescription()}));
                                } else if (rl != train.getRoute().getTerminatesRouteLocation()) {
                                    newLine(fileOut, MessageFormat
                                            .format(messageFormatText = TrainSwitchListText
                                                    .getStringVisitNumberDeparted(), new Object[]{stops,
                                                            train.getName(), expectedArrivalTime,
                                                            rl.getTrainDirectionString(),
                                                            train.getDescription()}));
                                } else {
                                    // message: Visit number {0} for train ({1}) expect to arrive in {2}, terminates {3}
                                    newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                            .getStringVisitNumberTerminatesDeparted(), new Object[]{stops,
                                                    train.getName(), expectedArrivalTime, splitString(rl.getName()),
                                                    train.getDescription()}));
                                }
                            } else {
                                // train hasn't departed
                                if (rl != train.getRoute().getTerminatesRouteLocation()) {
                                    newLine(fileOut, MessageFormat
                                            .format(messageFormatText = TrainSwitchListText.getStringVisitNumber(),
                                                    new Object[]{stops, train.getName(), expectedArrivalTime,
                                                            rl.getTrainDirectionString(), train.getDescription()}));
                                } else {
                                    newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                            .getStringVisitNumberTerminates(), new Object[]{stops, train.getName(),
                                                    expectedArrivalTime, splitString(rl.getName()),
                                                    train.getDescription()}));
                                }
                            }
                        } else {
                            stops--; // don't bump stop count, same location
                            // Does the train reverse direction?
                            if (rl.getTrainDirection() != rlPrevious.getTrainDirection() &&
                                    !TrainSwitchListText.getStringTrainDirectionChange().equals("")) {
                                newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                        .getStringTrainDirectionChange(), new Object[]{train.getName(),
                                                rl.getTrainDirectionString(), train.getDescription(),
                                                train.getTrainTerminatesName()}));
                            }
                        }
                    }

                    rlPrevious = rl; // save current location in case there's back to back location with the same name

                    // add route comment
                    if (Setup.isSwitchListRouteLocationCommentEnabled() && !rl.getComment().trim().equals("")) {
                        newLine(fileOut, rl.getComment());
                    }

                    // now print out the work for this location
                    if (Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
                        pickupEngines(fileOut, engineList, rl, !IS_MANIFEST);
                        // if switcher show loco drop at end of list
                        if (train.isLocalSwitcher()) {
                            blockCarsByTrack(fileOut, train, carList, routeList, rl, IS_PRINT_HEADER, !IS_MANIFEST);
                            dropEngines(fileOut, engineList, rl, !IS_MANIFEST);
                        } else {
                            dropEngines(fileOut, engineList, rl, !IS_MANIFEST);
                            blockCarsByTrack(fileOut, train, carList, routeList, rl, IS_PRINT_HEADER, !IS_MANIFEST);
                        }
                    } else if (Setup.getManifestFormat().equals(Setup.TWO_COLUMN_FORMAT)) {
                        blockLocosTwoColumn(fileOut, engineList, rl, !IS_MANIFEST);
                        blockCarsByTrackTwoColumn(fileOut, train, carList, routeList, rl, IS_PRINT_HEADER,
                                !IS_MANIFEST);
                    } else {
                        blockLocosTwoColumn(fileOut, engineList, rl, !IS_MANIFEST);
                        blockCarsByTrackNameTwoColumn(fileOut, train, carList, routeList, rl, IS_PRINT_HEADER,
                                !IS_MANIFEST);
                    }
                    if (Setup.isPrintHeadersEnabled() || !Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
                        printHorizontalLine(fileOut, !IS_MANIFEST);
                    }

                    stops++;

                    // done with work, now print summary for this location if we're done
                    if (rl != train.getRoute().getTerminatesRouteLocation()) {
                        RouteLocation nextRl = train.getRoute().getNextRouteLocation(rl);
                        if (splitString(rl.getName()).equals(splitString(nextRl.getName()))) {
                            continue; // the current location name is the "same" as the next
                        }
                        // print departure text if not a switcher
                        if (!train.isLocalSwitcher()) {
                            String trainDeparts = "";
                            if (Setup.isPrintLoadsAndEmptiesEnabled()) {
                                int emptyCars = train.getNumberEmptyCarsInTrain(rl);
                                // Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet,
                                // 3000 tons
                                trainDeparts = MessageFormat.format(TrainSwitchListText.getStringTrainDepartsLoads(),
                                        new Object[]{TrainCommon.splitString(rl.getName()),
                                                rl.getTrainDirectionString(),
                                                train.getNumberCarsInTrain(rl) - emptyCars, emptyCars,
                                                train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
                                                train.getTrainWeight(rl), train.getTrainTerminatesName(),
                                                train.getName()});
                            } else {
                                // Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
                                trainDeparts = MessageFormat.format(TrainSwitchListText.getStringTrainDepartsCars(),
                                        new Object[]{TrainCommon.splitString(rl.getName()),
                                                rl.getTrainDirectionString(), train.getNumberCarsInTrain(rl),
                                                train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
                                                train.getTrainWeight(rl), train.getTrainTerminatesName(),
                                                train.getName()});
                            }
                            newLine(fileOut, trainDeparts);
                        }
                    }
                }
                if (trainDone && !pickupCars && !dropCars) {
                    // Default message: Train ({0}) has serviced this location
                    newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText.getStringTrainDone(),
                            new Object[]{train.getName(), train.getDescription(), splitString(location.getName())}));
                } else {
                    if (stops > 1 && !pickupCars) {
                        // Default message: No car pick ups for train ({0}) at this location
                        newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                .getStringNoCarPickUps(), new Object[]{train.getName(), train.getDescription(),
                                        splitString(location.getName())}));
                    }
                    if (stops > 1 && !dropCars) {
                        // Default message: No car set outs for train ({0}) at this location
                        newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                .getStringNoCarDrops(), new Object[]{train.getName(), train.getDescription(),
                                        splitString(location.getName())}));
                    }
                }
            }

            // now report car movement by tracks at location
            if (Setup.isTrackSummaryEnabled() && Setup.isSwitchListRealTime()) {
                clearUtilityCarTypes(); // list utility cars by quantity
                if (Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL)) {
                    newLine(fileOut);
                    newLine(fileOut);
                } else {
                    fileOut.write(FORM_FEED);
                }
                newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                        .getStringSwitchListByTrack(), new Object[]{splitString(location.getName())}));

                // we only need the cars delivered to or at this location
                List<Car> rsList = carManager.getByTrainList();
                List<Car> carList = new ArrayList<>();
                for (Car rs : rsList) {
                    if ((rs.getLocation() != null &&
                            splitString(rs.getLocation().getName()).equals(splitString(location.getName()))) ||
                            (rs.getDestination() != null &&
                                    splitString(rs.getDestination().getName()).equals(splitString(location.getName()))))
                        carList.add(rs);
                }

                List<String> trackNames = new ArrayList<>(); // locations and tracks can have "similar" names, only list track names once
                for (Location loc : locationManager.getLocationsByNameList()) {
                    if (!splitString(loc.getName()).equals(splitString(location.getName())))
                        continue;
                    for (Track track : loc.getTrackByNameList(null)) {
                        String trackName = splitString(track.getName());
                        if (trackNames.contains(trackName))
                            continue;
                        trackNames.add(trackName);

                        String trainName = ""; // for printing train message once
                        newLine(fileOut);
                        newLine(fileOut, trackName); // print out just the track name
                        // now show the cars pickup and holds for this track
                        for (Car car : carList) {
                            if (!splitString(car.getTrackName()).equals(trackName)) {
                                continue;
                            }
                            // is the car scheduled for pickup?
                            if (car.getRouteLocation() != null) {
                                if (splitString(car.getRouteLocation().getLocation().getName())
                                        .equals(splitString(location.getName()))) {
                                    // cars are sorted by train name, print train message once
                                    if (!trainName.equals(car.getTrainName())) {
                                        trainName = car.getTrainName();
                                        newLine(fileOut,
                                                MessageFormat.format(messageFormatText = TrainSwitchListText
                                                        .getStringScheduledWork(),
                                                        new Object[]{car.getTrainName(),
                                                                car.getTrain().getDescription()}));
                                        printPickupCarHeader(fileOut, !IS_MANIFEST, !IS_TWO_COLUMN_TRACK);
                                    }
                                    if (car.isUtility()) {
                                        pickupUtilityCars(fileOut, carList, car, !IS_MANIFEST);
                                    } else {
                                        pickUpCar(fileOut, car, !IS_MANIFEST);
                                    }
                                }
                                // car holds
                            } else if (car.isUtility()) {
                                String s = pickupUtilityCars(carList, car, !IS_MANIFEST, !IS_TWO_COLUMN_TRACK);
                                if (s != null) {
                                    newLine(fileOut,
                                            TrainSwitchListText.getStringHoldCar().split("\\{")[0] + s.trim()); // NOI18N
                                }
                            } else {
                                newLine(fileOut, MessageFormat.format(
                                        messageFormatText = TrainSwitchListText.getStringHoldCar(),
                                        new Object[]{padAndTruncateString(car.getRoadName(),
                                                InstanceManager.getDefault(CarRoads.class).getMaxNameLength()),
                                                padAndTruncateString(TrainCommon.splitString(car.getNumber()),
                                                        Control.max_len_string_print_road_number),
                                                padAndTruncateString(car.getTypeName().split("-")[0],
                                                        InstanceManager.getDefault(CarTypes.class).getMaxNameLength()),
                                                padAndTruncateString(car.getLength() + LENGTHABV,
                                                        Control.max_len_string_length_name),
                                                padAndTruncateString(car.getLoadName(),
                                                        InstanceManager.getDefault(CarLoads.class).getMaxNameLength()),
                                                padAndTruncateString(trackName,
                                                        locationManager.getMaxTrackNameLength()),
                                                padAndTruncateString(car.getColor(),
                                                        InstanceManager.getDefault(CarColors.class).getMaxNameLength())}));
                            }
                        }
                        // now do set outs at this location
                        for (Car car : carList) {
                            if (!splitString(car.getDestinationTrackName()).equals(trackName)) {
                                continue;
                            }
                            if (car.getRouteDestination() != null &&
                                    splitString(car.getRouteDestination().getLocation().getName())
                                            .equals(splitString(location.getName()))) {
                                // cars are sorted by train name, print train message once
                                if (!trainName.equals(car.getTrainName())) {
                                    trainName = car.getTrainName();
                                    newLine(fileOut, MessageFormat.format(messageFormatText = TrainSwitchListText
                                            .getStringScheduledWork(),
                                            new Object[]{car.getTrainName(), car.getTrain().getDescription()}));
                                    printDropCarHeader(fileOut, !IS_MANIFEST, !IS_TWO_COLUMN_TRACK);
                                }
                                if (car.isUtility()) {
                                    setoutUtilityCars(fileOut, carList, car, !IS_MANIFEST);
                                } else {
                                    dropCar(fileOut, car, !IS_MANIFEST);
                                }
                            }
                        }
                    }
                }
            }

        } catch (IllegalArgumentException e) {
            newLine(fileOut, MessageFormat.format(Bundle.getMessage("ErrorIllegalArgument"), new Object[]{
                    Bundle.getMessage("TitleSwitchListText"), e.getLocalizedMessage()}));
            newLine(fileOut, messageFormatText);
            e.printStackTrace();
        }

        // Are there any cars that need to be found?
        addCarsLocationUnknown(fileOut, !IS_MANIFEST);
        fileOut.flush();
        fileOut.close();
    }

    public void printSwitchList(Location location, boolean isPreview) {
        File buildFile = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(location.getName());
        if (!buildFile.exists()) {
            log.warn("Switch list file missing for location ({})", location.getName());
            return;
        }
        if (isPreview && Setup.isManifestEditorEnabled()) {
            TrainUtilities.openDesktop(buildFile);
        } else {
            TrainPrintUtilities.printReport(buildFile, location.getName(), isPreview, Setup.getFontName(), false,
                    FileUtil.getExternalFilename(Setup.getManifestLogoURL()), location.getDefaultPrinterName(), Setup
                            .getSwitchListOrientation(),
                    Setup.getManifestFontSize());
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
