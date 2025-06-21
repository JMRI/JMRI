package jmri.jmrit.operations.trains.csv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;

/**
 * Builds a train's manifest using Comma Separated Values (csv).
 *
 * @author Daniel Boudreau Copyright (C) 2011, 2015
 *
 */
public class TrainCsvManifest extends TrainCsvCommon {

    public TrainCsvManifest(Train train) throws BuildFailedException {
        if (!Setup.isGenerateCsvManifestEnabled()) {
            return;
        }
        // create comma separated value manifest file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createTrainCsvManifestFile(train.getName());

        try (CSVPrinter fileOut = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                CSVFormat.DEFAULT)) {
            // build header
            printHeader(fileOut);
            printRailroadName(fileOut,
                    train.getRailroadName().isEmpty() ? Setup.getRailroadName() : train.getRailroadName());
            printTrainName(fileOut, train.getName());
            printTrainDescription(fileOut, train.getDescription());
            printPrinterName(fileOut, locationManager.getLocationByName(train.getTrainDepartsName()).getDefaultPrinterName());
            printLogoURL(fileOut, train);
            printValidity(fileOut, getDate(true));
            printTrainComment(fileOut, train);
            printRouteComment(fileOut, train);

            // get engine and car lists
            List<Engine> engineList = engineManager.getByTrainBlockingList(train);
            List<Car> carList = carManager.getByTrainDestinationList(train);

            boolean newWork = false;
            String previousRouteLocationName = null;
            List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
            for (RouteLocation rl : routeList) {
                // print info only if new location
                if (!rl.getSplitName().equals(previousRouteLocationName)) {
                    printLocationName(fileOut, rl.getSplitName());
                    if (rl != train.getTrainDepartsRouteLocation()) {
                        fileOut.printRecord("AT", Bundle.getMessage("csvArrivalTime"), train.getExpectedArrivalTime(rl)); // NOI18N
                    }
                    if (rl == train.getTrainDepartsRouteLocation()) {
                        fileOut.printRecord("DT", Bundle.getMessage("csvDepartureTime"), train.getFormatedDepartureTime()); // NOI18N
                    } else if (!rl.getDepartureTime().equals(RouteLocation.NONE)) {
                        fileOut.printRecord("DTR", Bundle.getMessage("csvDepartureTimeRoute"), rl.getFormatedDepartureTime()); // NOI18N
                    } else {
                        fileOut.printRecord("EDT", Bundle.getMessage("csvEstimatedDepartureTime"), train.getExpectedDepartureTime(rl)); // NOI18N
                    }
                    printLocationComment(fileOut, rl.getLocation());
                    if (Setup.isPrintTruncateManifestEnabled() && rl.getLocation().isSwitchListEnabled()) {
                        fileOut.printRecord("TRUN", Bundle.getMessage("csvTruncate"));
                    }
                }
                printRouteLocationComment(fileOut, rl);
                printTrackComments(fileOut, rl, carList);

                // engine change or helper service?
                checkForEngineOrCabooseChange(fileOut, train, rl);

                for (Engine engine : engineList) {
                    if (engine.getRouteLocation() == rl) {
                        printEngine(fileOut, engine, "PL", Bundle.getMessage("csvPickUpLoco"));
                    }
                }
                for (Engine engine : engineList) {
                    if (engine.getRouteDestination() == rl) {
                        printEngine(fileOut, engine, "SL", Bundle.getMessage("csvSetOutLoco"));
                    }
                }
                // block pick up cars
                // caboose or FRED is placed at end of the train
                // passenger cars are already blocked in the car list
                // passenger cars with negative block numbers are placed at
                // the front of the train, positive numbers at the end of
                // the train.
                for (RouteLocation rld : train.getTrainBlockingOrder()) {
                    for (Car car : carList) {
                        if (isNextCar(car, rl, rld)) {
                            newWork = true;
                            int count = 0;
                            if (car.isUtility()) {
                                count = countPickupUtilityCars(carList, car, IS_MANIFEST);
                                if (count == 0) {
                                    continue; // already done this set of
                                              // utility cars
                                }
                            }
                            printCar(fileOut, car, "PC", Bundle.getMessage("csvPickUpCar"), count);
                        }
                    }
                }
                // car set outs
                for (Car car : carList) {
                    if (car.getRouteDestination() == rl) {
                        newWork = true;
                        int count = 0;
                        if (car.isUtility()) {
                            count = countSetoutUtilityCars(carList, car, false, IS_MANIFEST);
                            if (count == 0) {
                                continue; // already done this set of utility cars
                            }
                        }
                        printCar(fileOut, car, "SC", Bundle.getMessage("csvSetOutCar"), count);
                    }
                }
                // car holds
                List<Car> carsByLocation = carManager.getByLocationList();
                List<Car> cList = new ArrayList<>();
                for (Car car : carsByLocation) {
                    if (car.getLocation() == rl.getLocation() && car.getRouteLocation() == null && car.getTrack() != null) {
                        cList.add(car);
                    }
                }
                clearUtilityCarTypes(); // list utility cars by quantity
                for (Car car : cList) {
                    // list cars on tracks that only this train can service
                    if (!car.getTrack().getLocation().isStaging()
                            && car.getTrack().isPickupTrainAccepted(train) && car.getTrack().getPickupIds().length == 1
                            && car.getTrack().getPickupOption().equals(Track.TRAINS)) {
                        int count = 0;
                        if (car.isUtility()) {
                            count = countPickupUtilityCars(cList, car, !IS_MANIFEST);
                            if (count == 0) {
                                continue; // already done this set of utility cars
                            }
                        }
                        printCar(fileOut, car, "HOLD", Bundle.getMessage("csvHoldCar"), count);
                    }
                }
                if (rl != train.getTrainTerminatesRouteLocation()) {
                    // Is the next location the same as the previous?
                    RouteLocation rlNext = train.getRoute().getNextRouteLocation(rl);
                    if (!rl.getSplitName().equals(rlNext.getSplitName())) {
                        if (newWork) {
                            printTrainDeparts(fileOut, rl.getSplitName(), rl.getTrainDirectionString());
                            printTrainLength(fileOut, train.getTrainLength(rl), train.getNumberEmptyCarsInTrain(rl),
                                    train.getNumberCarsInTrain(rl));
                            printTrainWeight(fileOut, train.getTrainWeight(rl));
                            newWork = false;
                        } else {
                            fileOut.printRecord("NW", Bundle.getMessage("csvNoWork"));
                        }
                    }
                } else {
                    printTrainTerminates(fileOut, rl.getSplitName());
                }
                previousRouteLocationName = rl.getSplitName();
            }
            // Are there any cars that need to be found?
            listCarsLocationUnknown(fileOut);

            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            log.error("Can not open CSV manifest file: {}", e.getLocalizedMessage());
            throw new BuildFailedException(e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainCsvManifest.class);
}
