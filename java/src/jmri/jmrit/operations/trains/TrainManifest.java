package jmri.jmrit.operations.trains;

import java.io.*;
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
 * @author Daniel Boudreau Copyright (C) 2011, 2012, 2013, 2015
 *
 */
public class TrainManifest extends TrainCommon {

    private static final Logger log = LoggerFactory.getLogger(TrainManifest.class);

    String messageFormatText = ""; // the text being formated in case there's an exception

    public TrainManifest(Train train) {
        // create manifest file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createTrainManifestFile(train.getName());
        PrintWriter fileOut;

        try {
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), // NOI18N
                    true);
        } catch (IOException e) {
            log.error("Can not open train manifest file: " + file.getName());
            return;
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

            if (!train.getComment().equals(Train.NONE)) {
                newLine(fileOut, train.getComment());
            }

            List<Engine> engineList = engineManager.getByTrainBlockingList(train);

            if (Setup.isPrintRouteCommentsEnabled() && !train.getRoute().getComment().equals(Route.NONE)) {
                newLine(fileOut, train.getRoute().getComment());
            }

            List<Car> carList = carManager.getByTrainDestinationList(train);
            log.debug("Train has {} cars assigned to it", carList.size());

            boolean hadWork = false;
            boolean noWork = false;
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
                String routeLocationName = splitString(rl.getName());
                if (!routeLocationName.equals(previousRouteLocationName) || (hasWork && !hadWork)) {
                    if (hasWork) {
                        newLine(fileOut);
                        hadWork = true;
                        noWork = false;
                        printHeader = true;
                        String expectedArrivalTime = train.getExpectedArrivalTime(rl);
                        String workAt = MessageFormat.format(messageFormatText = TrainManifestText
                                .getStringScheduledWork(), new Object[]{routeLocationName, train.getName(),
                                        train.getDescription()});
                        if (!train.isShowArrivalAndDepartureTimesEnabled()) {
                            newLine(fileOut, workAt);
                        } else if (rl == train.getRoute().getDepartsRouteLocation()) {
                            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                                    .getStringWorkDepartureTime(), new Object[]{routeLocationName,
                                            train.getFormatedDepartureTime(), train.getName(),
                                            train.getDescription()}));
                        } else if (!rl.getDepartureTime().equals(RouteLocation.NONE)) {
                            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                                    .getStringWorkDepartureTime(), new Object[]{routeLocationName,
                                            rl.getFormatedDepartureTime(), train.getName(), train.getDescription()}));
                        } else if (Setup.isUseDepartureTimeEnabled() &&
                                rl != train.getRoute().getTerminatesRouteLocation()) {
                            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                                    .getStringWorkDepartureTime(), new Object[]{routeLocationName,
                                            train.getExpectedDepartureTime(rl), train.getName(),
                                            train.getDescription()}));
                        } else if (!expectedArrivalTime.equals(Train.ALREADY_SERVICED)) {
                            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                                    .getStringWorkArrivalTime(), new Object[]{routeLocationName, expectedArrivalTime,
                                            train.getName(), train.getDescription()}));
                        } else {
                            newLine(fileOut, workAt);
                        }
                        // add route location comment
                        if (!rl.getComment().trim().equals(RouteLocation.NONE)) {
                            newLine(fileOut, rl.getFormatedColorComment());
                        }

                        // add location comment
                        if (Setup.isPrintLocationCommentsEnabled() &&
                                !rl.getLocation().getComment().equals(Location.NONE)) {
                            newLine(fileOut, rl.getLocation().getComment());
                        }
                    }
                }
                // remember location name
                previousRouteLocationName = routeLocationName;

                // add track comments
                printTrackComments(fileOut, rl, carList, IS_MANIFEST);

                // engine change or helper service?
                if (train.getSecondLegOptions() != Train.NO_CABOOSE_OR_FRED) {
                    if (rl == train.getSecondLegStartLocation()) {
                        printChange(fileOut, rl, train, train.getSecondLegOptions());
                    }
                    if (rl == train.getSecondLegEndLocation() && train.getSecondLegOptions() == Train.HELPER_ENGINES) {
                        newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                                .getStringRemoveHelpers(), new Object[]{splitString(rl.getName()), train.getName(),
                                        train.getDescription()}));
                    }
                }
                if (train.getThirdLegOptions() != Train.NO_CABOOSE_OR_FRED) {
                    if (rl == train.getThirdLegStartLocation()) {
                        printChange(fileOut, rl, train, train.getThirdLegOptions());
                    }
                    if (rl == train.getThirdLegEndLocation() && train.getThirdLegOptions() == Train.HELPER_ENGINES) {
                        newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText
                                .getStringRemoveHelpers(), new Object[]{splitString(rl.getName()), train.getName(),
                                        train.getDescription()}));
                    }
                }

                if (Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
                    pickupEngines(fileOut, engineList, rl, IS_MANIFEST);
                    // if switcher show loco drop at end of list
                    if (train.isLocalSwitcher()) {
                        blockCarsByTrack(fileOut, train, carList, routeList, rl, printHeader, IS_MANIFEST);
                        dropEngines(fileOut, engineList, rl, IS_MANIFEST);
                    } else {
                        dropEngines(fileOut, engineList, rl, IS_MANIFEST);
                        blockCarsByTrack(fileOut, train, carList, routeList, rl, printHeader, IS_MANIFEST);
                    }
                } else if (Setup.getManifestFormat().equals(Setup.TWO_COLUMN_FORMAT)) {
                    blockLocosTwoColumn(fileOut, engineList, rl, IS_MANIFEST);
                    blockCarsTwoColumn(fileOut, carList, routeList, rl, printHeader, IS_MANIFEST);
                } else {
                    blockLocosTwoColumn(fileOut, engineList, rl, IS_MANIFEST);
                    blockCarsByTrackNameTwoColumn(fileOut, carList, routeList, rl, printHeader, IS_MANIFEST);
                }

                if (rl != train.getRoute().getTerminatesRouteLocation()) {
                    // Is the next location the same as the current?
                    RouteLocation rlNext = train.getRoute().getNextRouteLocation(rl);
                    if (routeLocationName.equals(splitString(rlNext.getName()))) {
                        continue;
                    }
                    if (hadWork) {
                        hadWork = false;
                        if (Setup.isPrintHeadersEnabled() || !Setup.getManifestFormat().equals(Setup.STANDARD_FORMAT)) {
                            printHorizontalLine(fileOut, IS_MANIFEST);
                        }
                        String trainDeparts = "";
                        if (Setup.isPrintLoadsAndEmptiesEnabled()) {
                            // Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
                            trainDeparts = MessageFormat.format(messageFormatText = TrainManifestText
                                    .getStringTrainDepartsLoads(), new Object[]{routeLocationName,
                                            rl.getTrainDirectionString(), cars - emptyCars, emptyCars,
                                            train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
                                            train.getTrainWeight(rl), train.getTrainTerminatesName(), train.getName()});
                        } else {
                            // Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
                            trainDeparts = MessageFormat.format(messageFormatText = TrainManifestText
                                    .getStringTrainDepartsCars(), new Object[]{routeLocationName,
                                            rl.getTrainDirectionString(), cars, train.getTrainLength(rl),
                                            Setup.getLengthUnit().toLowerCase(), train.getTrainWeight(rl),
                                            train.getTrainTerminatesName(), train.getName()});
                        }
                        newLine(fileOut, trainDeparts);
                    } else {
                        // no work at this location
                        if (!noWork) {
                            newLine(fileOut);
                        }
                        noWork = true;
                        String s = MessageFormat.format(messageFormatText = TrainManifestText
                                .getStringNoScheduledWork(), new Object[]{routeLocationName, train.getName(),
                                        train.getDescription()});
                        // if a route comment, then only use location name and route comment, useful for passenger
                        // trains
                        if (!rl.getComment().equals(RouteLocation.NONE)) {
                            s = routeLocationName;
                            if (rl.getComment().trim().length() > 0) {
                                s = MessageFormat.format(messageFormatText = TrainManifestText
                                        .getStringNoScheduledWorkWithRouteComment(),
                                        new Object[]{routeLocationName, rl.getFormatedColorComment(), train.getName(),
                                                train.getDescription()});
                            }
                        }
                        if (train.isShowArrivalAndDepartureTimesEnabled()) {
                            if (rl == train.getRoute().getDepartsRouteLocation()) {
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
                                !rl.getLocation().getComment().equals(Location.NONE)) {
                            newLine(fileOut, rl.getLocation().getComment());
                        }
                    }
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
                                    train.getDescription()}));
                }
            }
            // Are there any cars that need to be found?
            addCarsLocationUnknown(fileOut, IS_MANIFEST);

        } catch (IllegalArgumentException e) {
            newLine(fileOut, MessageFormat.format(Bundle.getMessage("ErrorIllegalArgument"), new Object[]{
                    Bundle.getMessage("TitleManifestText"), e.getLocalizedMessage()}));
            newLine(fileOut, messageFormatText);
            log.error("Illegal argument", e);
        }

        fileOut.flush();
        fileOut.close();

        train.setModified(false);
    }

    private void printChange(PrintWriter fileOut, RouteLocation rl, Train train, int legOptions)
            throws IllegalArgumentException {
        if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText.getStringAddHelpers(),
                    new Object[]{splitString(rl.getName()), train.getName(), train.getDescription()}));
        } else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES &&
                ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                        (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)) {
            newLine(fileOut, MessageFormat.format(
                    messageFormatText = TrainManifestText.getStringLocoAndCabooseChange(), new Object[]{
                            splitString(rl.getName()), train.getName(), train.getDescription()}));
        } else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText.getStringLocoChange(),
                    new Object[]{splitString(rl.getName()), train.getName(), train.getDescription()}));
        } else if ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            newLine(fileOut, MessageFormat.format(messageFormatText = TrainManifestText.getStringCabooseChange(),
                    new Object[]{splitString(rl.getName()), train.getName(), train.getDescription()}));
        }
    }

    private void newLine(PrintWriter file, String string) {
        if (!string.isEmpty()) {
            newLine(file, string, IS_MANIFEST);
        }
    }

}
