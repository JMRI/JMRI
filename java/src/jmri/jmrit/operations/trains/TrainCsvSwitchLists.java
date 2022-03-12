package jmri.jmrit.operations.trains;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;

/**
 * Builds a comma separated value (csv) switch list for a location on the
 * railroad.
 *
 * @author Daniel Boudreau (C) Copyright 2011, 2013, 2014, 2015, 2021
 *
 *
 */
public class TrainCsvSwitchLists extends TrainCsvCommon {

    /**
     * builds a csv file containing the switch list for a location
     *
     * @param location The Location requesting a switch list.
     *
     * @return File
     */
    public File buildSwitchList(Location location) {      
        if (!Setup.isGenerateCsvSwitchListEnabled()) {
            return null; // done, not enabled
        }
        
        // Append switch list data if not operating in real time
        boolean append = false; // add text to end of file when true

        if (!Setup.isSwitchListRealTime()) {
            if (location.getStatus().equals(Location.UPDATED)) {
                return null; // nothing to add
            }
            append = location.getSwitchListState() == Location.SW_APPEND;
        }
        // create CSV switch list file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createCsvSwitchListFile(location.getName());

        log.debug("Append CSV file: {} for location ({})", append, location.getName());

        // need to delete CSV data from file from tags "END" which is car hold list for
        // this location
        if (append) {
            trimCvsFile(file, location);
        }

        try (CSVPrinter fileOut = new CSVPrinter(
                new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT)) {
            if (!append) {
                // build header
                printHeader(fileOut);
                fileOut.printRecord("SWL", Bundle.getMessage("csvSwitchList")); // NOI18N
                printRailroadName(fileOut, Setup.getRailroadName());
                printLocationName(fileOut, splitString(location.getName()));
                printPrinterName(fileOut, location.getDefaultPrinterName());
                fileOut.printRecord("SWLC", Bundle.getMessage("csvSwitchListComment"), location.getSwitchListCommentWithColor());
                // add location comment

                if (Setup.isPrintLocationCommentsEnabled() && !location.getCommentWithColor().equals(Location.NONE)) {
                    // location comment can have multiple lines
                    String[] comments = location.getCommentWithColor().split(NEW_LINE); // NOI18N
                    for (String comment : comments) {
                        printLocationComment(fileOut, comment);
                    }
                }
            }
            printValidity(fileOut, getDate(true));

            // get a list of trains sorted by arrival time
            List<Train> trains = InstanceManager.getDefault(TrainManager.class)
                    .getTrainsArrivingThisLocationList(location);
            for (Train train : trains) {
                if (!train.isBuilt()) {
                    continue; // train wasn't built so skip
                }
                if (!Setup.isSwitchListRealTime() && train.getSwitchListStatus().equals(Train.PRINTED)) {
                    continue; // already loaded this train
                }
                int pickupCars = 0;
                int dropCars = 0;
                int stops = 1;
                boolean trainDone = false;
                List<Car> carList = InstanceManager.getDefault(CarManager.class).getByTrainDestinationList(train);
                List<Engine> enginesList = InstanceManager.getDefault(EngineManager.class)
                        .getByTrainBlockingList(train);
                // does the train stop once or more at this location?
                Route route = train.getRoute();
                if (route == null) {
                    continue; // no route for this train
                }
                List<RouteLocation> routeList = route.getLocationsBySequenceList();
                RouteLocation rlPrevious = null;
                // need to know where in the route we are for the various comments
                for (RouteLocation rl : routeList) {
                    if (!splitString(rl.getName()).equals(splitString(location.getName()))) {
                        rlPrevious = rl;
                        continue;
                    }
                    String expectedArrivalTime = train.getExpectedArrivalTime(rl);
                    if (expectedArrivalTime.equals(Train.ALREADY_SERVICED)) {
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
                    // if it terminates at this location
                    if (stops == 1) {
                        // newLine(fileOut);
                        printTrainName(fileOut, train.getName());
                        printTrainDescription(fileOut, train.getDescription());

                        if (train.isTrainEnRoute()) {
                            fileOut.printRecord("TIR", Bundle.getMessage("csvTrainEnRoute")); // NOI18N
                            printEstimatedTimeEnRoute(fileOut, expectedArrivalTime);
                        } else {
                            fileOut.printRecord("DL", Bundle.getMessage("csvDepartureLocationName"),
                                    splitString(splitString(train.getTrainDepartsName()))); // NOI18N
                            printDepartureTime(fileOut, train.getFormatedDepartureTime());
                            if (rl == train.getTrainDepartsRouteLocation() && !train.isLocalSwitcher()) {
                                printTrainDeparts(fileOut, splitString(rl.getName()), rl.getTrainDirectionString());
                            }
                            if (rl != train.getTrainDepartsRouteLocation()) {
                                printExpectedTimeArrival(fileOut, expectedArrivalTime);
                                printTrainArrives(fileOut, splitString(rl.getName()), rl.getTrainDirectionString());
                            }
                        }
                        if (rl == train.getTrainTerminatesRouteLocation()) {
                            printTrainTerminates(fileOut, splitString(rl.getName()));
                        }
                    }
                    if (stops > 1) {
                        // Print visit number, etc. only if previous location wasn't the same
                        if (rlPrevious == null ||
                                !splitString(rl.getName()).equals(splitString(rlPrevious.getName()))) {
                            // After the first time a train stops at a location provide:
                            // if the train has started its route
                            // the arrival time or relative time if the train has started its route
                            // the train's direction when it arrives
                            // if it terminate at this location

                            fileOut.printRecord("VN", Bundle.getMessage("csvVisitNumber"), stops);
                            if (train.isTrainEnRoute()) {
                                printEstimatedTimeEnRoute(fileOut, expectedArrivalTime);
                            } else {
                                printExpectedTimeArrival(fileOut, expectedArrivalTime);
                            }
                            printTrainArrives(fileOut, splitString(rl.getName()), rl.getTrainDirectionString());
                            if (rl == train.getTrainTerminatesRouteLocation()) {
                                printTrainTerminates(fileOut, splitString(rl.getName()));
                            }
                        } else {
                            stops--; // don't bump stop count, same location
                            // Does the train change direction?
                            if (rl.getTrainDirection() != rlPrevious.getTrainDirection()) {
                                fileOut.printRecord("TDC", Bundle.getMessage("csvTrainChangesDirection"),
                                        rl.getTrainDirectionString()); // NOI18N
                            }
                        }
                    }

                    rlPrevious = rl;

                    // add route comment
                    if (!rl.getComment().equals(RouteLocation.NONE)) {
                        printRouteLocationComment(fileOut, rl.getComment());
                    }

                    // engine change or helper service?
                    checkForEngineOrCabooseChange(fileOut, train, rl);

                    // go through the list of engines and determine if the engine departs here
                    for (Engine engine : enginesList) {
                        if (engine.getRouteLocation() == rl && engine.getTrack() != null) {
                            printEngine(fileOut, engine, "PL", Bundle.getMessage("csvPickUpLoco"));
                        }
                    }

                    // get a list of cars and determine if this location is serviced
                    // block pick up cars
                    for (RouteLocation rld : train.getTrainBlockingOrder()) {
                        for (Car car : carList) {
                            if (car.getRouteLocation() == rl &&
                                    car.getTrack() != null &&
                                    car.getRouteDestination() == rld) {
                                pickupCars++;
                                int count = 0;
                                if (car.isUtility()) {
                                    count = countPickupUtilityCars(carList, car, !IS_MANIFEST);
                                    if (count == 0) {
                                        continue; // already done this set of utility cars
                                    }
                                }
                                printCar(fileOut, car, "PC", Bundle.getMessage("csvPickUpCar"), count);
                            }
                        }
                    }

                    for (Engine engine : enginesList) {
                        if (engine.getRouteDestination() == rl) {
                            printEngine(fileOut, engine, "SL", Bundle.getMessage("csvSetOutLoco"));
                        }
                    }
                    // now do car set outs
                    for (Car car : carList) {
                        if (car.getRouteDestination() == rl) {
                            dropCars++;
                            int count = 0;
                            if (car.isUtility()) {
                                count = countSetoutUtilityCars(carList, car, !LOCAL, !IS_MANIFEST);
                                if (count == 0) {
                                    continue; // already done this set of utility cars
                                }
                            }
                            printCar(fileOut, car, "SC", Bundle.getMessage("csvSetOutCar"), count);
                        }
                    }
                    stops++;
                    if (rl != train.getTrainTerminatesRouteLocation()) {
                        printTrainLength(fileOut, train.getTrainLength(rl), train.getNumberEmptyCarsInTrain(rl),
                                train.getNumberCarsInTrain(rl));
                        printTrainWeight(fileOut, train.getTrainWeight(rl));
                    }
                }
                if (trainDone && pickupCars == 0 && dropCars == 0) {
                    fileOut.printRecord("TDONE", Bundle.getMessage("csvTrainHasAlreadyServiced"));
                } else if (stops > 1) {
                    if (pickupCars == 0) {
                        fileOut.printRecord("NCPU", Bundle.getMessage("csvNoCarPickUp"));
                    }
                    if (dropCars == 0) {
                        fileOut.printRecord("NCSO", Bundle.getMessage("csvNoCarSetOut"));
                    }
                    fileOut.printRecord("TEND", Bundle.getMessage("csvTrainEnd"), train.getName()); // done with this
                                                                                                    // train // NOI18N
                }
            }
            printEnd(fileOut); // done with switch list

            if (Setup.isSwitchListRealTime() && Setup.isPrintTrackSummaryEnabled()) {
                // now list hold cars
                List<Car> rsByLocation = InstanceManager.getDefault(CarManager.class).getByLocationList();
                List<Car> carList = new ArrayList<>();
                for (Car rs : rsByLocation) {
                    if (rs.getLocation() != null &&
                            splitString(rs.getLocation().getName()).equals(splitString(location.getName())) &&
                            rs.getRouteLocation() == null) {
                        carList.add(rs);
                    }
                }
                clearUtilityCarTypes(); // list utility cars by quantity
                for (Car car : carList) {
                    int count = 0;
                    if (car.isUtility()) {
                        count = countPickupUtilityCars(carList, car, !IS_MANIFEST);
                        if (count == 0) {
                            continue; // already done this set of utility cars
                        }
                    }
                    printCar(fileOut, car, "HOLD", Bundle.getMessage("csvHoldCar"), count);
                }
            }
            printEnd(fileOut); // done with hold cars

            // Are there any cars that need to be found?
            listCarsLocationUnknown(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            log.error("Can not open CSV switch list file: {}", file.getName());
            return null;
        }
        return file;
    }

    protected final void printEnd(CSVPrinter printer) throws IOException {
        printer.printRecord("END", Bundle.getMessage("csvEnd")); // NOI18N
    }

    protected final void printExpectedTimeArrival(CSVPrinter printer, String time) throws IOException {
        printer.printRecord("ETA", Bundle.getMessage("csvExpectedTimeArrival"), time); // NOI18N
    }

    protected final void printEstimatedTimeEnRoute(CSVPrinter printer, String time) throws IOException {
        printer.printRecord("ETE", Bundle.getMessage("csvEstimatedTimeEnRoute"), time); // NOI18N
    }

    protected final void printTrainArrives(CSVPrinter printer, String name, String direction) throws IOException {
        printer.printRecord("TA", Bundle.getMessage("csvTrainArrives"), name, direction); // NOI18N
    }

    /*
     * Used to delete CSV data from file from tags "END" which is car hold list for
     * this location. Creates a backup file and then copies the needed lines back
     * into the original file.
     */
    private void trimCvsFile(File file, Location location) {
        // need to delete CSV data from file from tags "END" which is car hold list for
        // this location
        try (PrintWriter fileOut = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)), true)) {
            File backupFile = new File(
                    InstanceManager.getDefault(TrainManagerXml.class).backupFileName(file.getAbsolutePath()));
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(backupFile), StandardCharsets.UTF_8))) {
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break; // done
                    }
                    if (!line.startsWith("END")) {
                        fileOut.println(line);
                    } else {
                        break; // done
                    }
                }
                in.close();
            } catch (FileNotFoundException e) {
                log.error("Can not open CSV switch list file: {}", file.getName());
            }
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            log.error("Can not open CSV switch list file: {}", file.getName());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainCsvSwitchLists.class);
}
