package jmri.jmrit.operations.trains;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;

/**
 * Builds a train's manifest. User has the ability to modify the text of the
 * messages which can cause an IllegalArgumentException. Some messages have more
 * arguments than the default message allowing the user to customize the message
 * to their liking.
 *
 * @author Daniel Boudreau Copyright (C) 2011, 2012, 2013, 2015, 2024
 */
public class TrainManifest extends TrainCommon {

    private static final Logger log = LoggerFactory.getLogger(TrainManifest.class);

    String messageFormatText = ""; // the text being formated in case there's an exception

    public TrainManifest(Train train) throws BuildFailedException {
        // create manifest file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createTrainManifestFile(train.getName());
        PrintWriter fileOut;

        try {
            fileOut = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                    true);
        } catch (IOException e) {
            log.error("Can not open train manifest file: {}", e.getLocalizedMessage());
            throw new BuildFailedException(e);
        }

        try {
            // build header
            if (!train.getRailroadName().equals(Train.NONE)) {
                newLine(fileOut, train.getRailroadName());
            } else {
                newLine(fileOut, Setup.getRailroadName());
            }
            newLine(fileOut); // empty line
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText.getStringManifestForTrain(),
                    new Object[]{train.getName(), train.getDescription()}));

            String valid = MessageFormat.format(messageFormatText = TrainManifestText.getStringValid(),
                    new Object[]{getDate(true)});

            if (Setup.isPrintTrainScheduleNameEnabled()) {
                TrainSchedule sch = InstanceManager.getDefault(TrainScheduleManager.class).getActiveSchedule();
                if (sch != null) {
                    valid = valid + " (" + sch.getName() + ")";
                }
            }
            if (Setup.isPrintValidEnabled()) {
                newLine(fileOut, valid);
            }

            if (!train.getCommentWithColor().equals(Train.NONE)) {
                newLine(fileOut, train.getCommentWithColor());
            }

            List<Engine> engineList = engineManager.getByTrainBlockingList(train);

            if (Setup.isPrintRouteCommentsEnabled() && !train.getRoute().getComment().equals(Route.NONE)) {
                newLine(fileOut, train.getRoute().getComment());
            }

            List<Car> carList = carManager.getByTrainDestinationList(train);
            log.debug("Train has {} cars assigned to it", carList.size());

            boolean hadWork = false;
            String previousRouteLocationName = null;
            List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();

            /*
             * Go through the train's route and print out the work for each
             * location. Locations with "similar" names are combined to look
             * like one location.
             */
            for (RouteLocation rl : routeList) {
                boolean printHeader = false;
                boolean hasWork = isThereWorkAtLocation(carList, engineList, rl);
                // print info only if new location
                String routeLocationName = rl.getSplitName();
                if (!routeLocationName.equals(previousRouteLocationName) || (hasWork && !hadWork)) {
                    if (hasWork) {
                        newLine(fileOut);
                        hadWork = true;
                        printHeader = true;

                        // add arrival message
                        arrivalMessage(fileOut, train, rl);

                        // add route location comment
                        if (!rl.getComment().trim().equals(RouteLocation.NONE)) {
                            newLine(fileOut, rl.getCommentWithColor());
                        }

                        // add location comment
                        if (Setup.isPrintLocationCommentsEnabled() &&
                                !rl.getLocation().getCommentWithColor().equals(Location.NONE)) {
                            newLine(fileOut, rl.getLocation().getCommentWithColor());
                        }
                    }
                }
                // remember location name
                previousRouteLocationName = routeLocationName;

                // add track comments
                printTrackComments(fileOut, rl, carList, IS_MANIFEST);

                // engine change or helper service?
                if (train.getSecondLegOptions() != Train.NO_CABOOSE_OR_FRED) {
                    if (rl == train.getSecondLegStartRouteLocation()) {
                        printChange(fileOut, rl, train, train.getSecondLegOptions());
                    }
                    if (rl == train.getSecondLegEndRouteLocation() &&
                            train.getSecondLegOptions() == Train.HELPER_ENGINES) {
                        newLine(fileOut,
                                MessageFormat.format(messageFormatText = TrainManifestText.getStringRemoveHelpers(),
                                        new Object[]{rl.getSplitName(), train.getName(),
                                                train.getDescription(), train.getSecondLegNumberEngines(),
                                                train.getSecondLegEngineModel(), train.getSecondLegEngineRoad()}));
                    }
                }
                if (train.getThirdLegOptions() != Train.NO_CABOOSE_OR_FRED) {
                    if (rl == train.getThirdLegStartRouteLocation()) {
                        printChange(fileOut, rl, train, train.getThirdLegOptions());
                    }
                    if (rl == train.getThirdLegEndRouteLocation() &&
                            train.getThirdLegOptions() == Train.HELPER_ENGINES) {
                        newLine(fileOut,
                                MessageFormat.format(messageFormatText = TrainManifestText.getStringRemoveHelpers(),
                                        new Object[]{rl.getSplitName(), train.getName(),
                                                train.getDescription(), train.getThirdLegNumberEngines(),
                                                train.getThirdLegEngineModel(), train.getThirdLegEngineRoad()}));
                    }
                }

                if (Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
                    pickupEngines(fileOut, engineList, rl, IS_MANIFEST);
                    // if switcher show loco drop at end of list
                    if (train.isLocalSwitcher()) {
                        blockCarsByTrack(fileOut, train, carList, rl, printHeader, IS_MANIFEST);
                        dropEngines(fileOut, engineList, rl, IS_MANIFEST);
                    } else {
                        dropEngines(fileOut, engineList, rl, IS_MANIFEST);
                        blockCarsByTrack(fileOut, train, carList, rl, printHeader, IS_MANIFEST);
                    }
                } else if (Setup.getManifestFormat().equals(Setup.TWO_COLUMN_FORMAT)) {
                    blockLocosTwoColumn(fileOut, engineList, rl, IS_MANIFEST);
                    blockCarsTwoColumn(fileOut, train, carList, rl, printHeader, IS_MANIFEST);
                } else {
                    blockLocosTwoColumn(fileOut, engineList, rl, IS_MANIFEST);
                    blockCarsByTrackNameTwoColumn(fileOut, train, carList, rl, printHeader, IS_MANIFEST);
                }

                if (rl != train.getTrainTerminatesRouteLocation()) {
                    // Is the next location the same as the current?
                    RouteLocation rlNext = train.getRoute().getNextRouteLocation(rl);
                    if (routeLocationName.equals(rlNext.getSplitName())) {
                        continue;
                    }
                    departureMessage(fileOut, train, rl, hadWork);

                    hadWork = false;

                } else {
                    // last location in the train's route, print train terminates message
                    if (!hadWork) {
                        newLine(fileOut);
                    } else if (Setup.isPrintHeadersEnabled() ||
                            !Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
                        printHorizontalLine(fileOut, IS_MANIFEST);
                    }
                    newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                            .getStringTrainTerminates(),
                            new Object[]{routeLocationName, train.getName(),
                                    train.getDescription(), rl.getLocation().getDivisionName()}));
                }
            }
            // Are there any cars that need to be found?
            addCarsLocationUnknown(fileOut, IS_MANIFEST);

        } catch (IllegalArgumentException e) {
            newLine(fileOut, Bundle.getMessage("ErrorIllegalArgument",
                    Bundle.getMessage("TitleManifestText"), e.getLocalizedMessage()));
            newLine(fileOut, messageFormatText);
            log.error("Illegal argument", e);
        }

        fileOut.flush();
        fileOut.close();

        train.setModified(false);
    }

    private void arrivalMessage(PrintWriter fileOut, Train train, RouteLocation rl) {
        String expectedArrivalTime = train.getExpectedArrivalTime(rl);
        String routeLocationName = rl.getSplitName();
        // Scheduled work at {0}
        String workAt = MessageFormat.format(messageFormatText = TrainManifestText
                .getStringScheduledWork(),
                new Object[]{routeLocationName, train.getName(),
                        train.getDescription(), rl.getLocation().getDivisionName()});
        if (!train.isShowArrivalAndDepartureTimesEnabled()) {
            // Scheduled work at {0}
            newLine(fileOut, workAt);
        } else if (rl == train.getTrainDepartsRouteLocation()) {
            // Scheduled work at {0}, departure time {1}
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                    .getStringWorkDepartureTime(),
                    new Object[]{routeLocationName,
                            train.getFormatedDepartureTime(), train.getName(),
                            train.getDescription(), rl.getLocation().getDivisionName()}));
        } else if (!rl.getDepartureTime().equals(RouteLocation.NONE)) {
            // Scheduled work at {0}, departure time {1}
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                    .getStringWorkDepartureTime(),
                    new Object[]{routeLocationName,
                            rl.getFormatedDepartureTime(), train.getName(), train.getDescription(),
                            rl.getLocation().getDivisionName()}));
        } else if (Setup.isUseDepartureTimeEnabled() &&
                rl != train.getTrainTerminatesRouteLocation()) {
            // Scheduled work at {0}, departure time {1}
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                    .getStringWorkDepartureTime(),
                    new Object[]{routeLocationName,
                            train.getExpectedDepartureTime(rl), train.getName(),
                            train.getDescription(), rl.getLocation().getDivisionName()}));
        } else if (!expectedArrivalTime.equals(Train.ALREADY_SERVICED)) {
            // Scheduled work at {0}, arrival time {1}
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                    .getStringWorkArrivalTime(),
                    new Object[]{routeLocationName, expectedArrivalTime,
                            train.getName(), train.getDescription(),
                            rl.getLocation().getDivisionName()}));
        } else {
            // Scheduled work at {0}
            newLine(fileOut, workAt);
        }
    }

    private void departureMessage(PrintWriter fileOut, Train train, RouteLocation rl, boolean hadWork) {
        String routeLocationName = rl.getSplitName();
        if (!hadWork) {
            newLine(fileOut);
            // No work at {0}
            String s = MessageFormat.format(messageFormatText = TrainManifestText
                    .getStringNoScheduledWork(),
                    new Object[]{routeLocationName, train.getName(),
                            train.getDescription(), rl.getLocation().getDivisionName()});
            // if a route comment, then only use location name and route comment, useful for passenger
            // trains
            if (!rl.getComment().equals(RouteLocation.NONE)) {
                s = routeLocationName;
                if (!rl.getComment().isBlank()) {
                    s = MessageFormat.format(messageFormatText = TrainManifestText
                            .getStringNoScheduledWorkWithRouteComment(),
                            new Object[]{routeLocationName, rl.getCommentWithColor(), train.getName(),
                                    train.getDescription(), rl.getLocation().getDivisionName()});
                }
            }
            // append arrival or departure time if enabled
            if (train.isShowArrivalAndDepartureTimesEnabled()) {
                if (rl == train.getTrainDepartsRouteLocation()) {
                    s += MessageFormat.format(messageFormatText = TrainManifestText
                            .getStringDepartTime(), new Object[]{train.getFormatedDepartureTime()});
                } else if (!rl.getDepartureTime().equals(RouteLocation.NONE)) {
                    s += MessageFormat.format(messageFormatText = TrainManifestText
                            .getStringDepartTime(), new Object[]{rl.getFormatedDepartureTime()});
                } else if (Setup.isUseDepartureTimeEnabled() &&
                        !rl.getComment().equals(RouteLocation.NONE)) {
                    s += MessageFormat
                            .format(messageFormatText = TrainManifestText.getStringDepartTime(),
                                    new Object[]{train.getExpectedDepartureTime(rl)});
                }
            }
            newLine(fileOut, s);

            // add location comment
            if (Setup.isPrintLocationCommentsEnabled() &&
                    !rl.getLocation().getCommentWithColor().equals(Location.NONE)) {
                newLine(fileOut, rl.getLocation().getCommentWithColor());
            }
        } else if (Setup.isPrintHeadersEnabled() || !Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
            printHorizontalLine(fileOut, IS_MANIFEST);
        }
        if (Setup.isPrintLoadsAndEmptiesEnabled()) {
            int emptyCars = train.getNumberEmptyCarsInTrain(rl);
            // Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                    .getStringTrainDepartsLoads(),
                    new Object[]{routeLocationName,
                            rl.getTrainDirectionString(), train.getNumberCarsInTrain(rl) - emptyCars,
                            emptyCars,
                            train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
                            train.getTrainWeight(rl), train.getTrainTerminatesName(), train.getName()}));
        } else {
            // Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                    .getStringTrainDepartsCars(),
                    new Object[]{routeLocationName,
                            rl.getTrainDirectionString(), train.getNumberCarsInTrain(rl),
                            train.getTrainLength(rl),
                            Setup.getLengthUnit().toLowerCase(), train.getTrainWeight(rl),
                            train.getTrainTerminatesName(), train.getName()}));
        }
    }

    private void printChange(PrintWriter fileOut, RouteLocation rl, Train train, int legOptions)
            throws IllegalArgumentException {
        if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
            // assume 2nd leg for helper change
            String numberEngines = train.getSecondLegNumberEngines();
            String endLocationName = train.getSecondLegEndLocationName();
            String engineModel = train.getSecondLegEngineModel();
            String engineRoad = train.getSecondLegEngineRoad();
            if (rl == train.getThirdLegStartRouteLocation()) {
                numberEngines = train.getThirdLegNumberEngines();
                endLocationName = train.getThirdLegEndLocationName();
                engineModel = train.getThirdLegEngineModel();
                engineRoad = train.getThirdLegEngineRoad();
            }
            newLine(fileOut,
                    MessageFormat.format(messageFormatText = TrainManifestText.getStringAddHelpers(),
                            new Object[]{rl.getSplitName(), train.getName(), train.getDescription(),
                                    numberEngines, endLocationName, engineModel, engineRoad}));
        } else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                        (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)) {
            newLine(fileOut, MessageFormat.format(
                    messageFormatText = TrainManifestText.getStringLocoAndCabooseChange(), new Object[]{
                            rl.getSplitName(), train.getName(), train.getDescription(),
                            rl.getLocation().getDivisionName()}));
        } else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText.getStringLocoChange(),
                    new Object[]{rl.getSplitName(), train.getName(), train.getDescription(),
                            rl.getLocation().getDivisionName()}));
        } else if ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText.getStringCabooseChange(),
                    new Object[]{rl.getSplitName(), train.getName(), train.getDescription(),
                            rl.getLocation().getDivisionName()}));
        }
    }

    private void newLine(PrintWriter file, String string) {
        if (!string.isEmpty()) {
            newLine(file, string, IS_MANIFEST);
        }
    }

}
