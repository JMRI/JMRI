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
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.FileUtil;

/**
 * Builds a train's manifest using Comma Separated Values (csv).
 *
 * @author Daniel Boudreau Copyright (C) 2011, 2015
 *
 */
public class TrainCsvManifest extends TrainCsvCommon {

    public TrainCsvManifest(Train train) {
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
            // add logo
            String logoURL = FileUtil.getExternalFilename(Setup.getManifestLogoURL());
            if (!train.getManifestLogoPathName().equals(Train.NONE)) {
                logoURL = FileUtil.getExternalFilename(train.getManifestLogoPathName());
            }
            if (!logoURL.isEmpty()) {
                fileOut.printRecord("LOGO", Bundle.getMessage("csvLogoFilePath"), logoURL);
            }
            printValidity(fileOut, getDate(true));
            // train comment can have multiple lines
            if (!train.getComment().equals(Train.NONE)) {
                String[] comments = train.getComment().split(NEW_LINE);
                for (String comment : comments) {
                    fileOut.printRecord("TC", Bundle.getMessage("csvTrainComment"), comment); // NOI18N
                }
            }
            if (Setup.isPrintRouteCommentsEnabled()) {
                fileOut.printRecord("RC", Bundle.getMessage("csvRouteComment"), train.getRoute().getComment()); // NOI18N
            }

            // get engine and car lists
            List<Engine> engineList = engineManager.getByTrainBlockingList(train);
            List<Car> carList = carManager.getByTrainDestinationList(train);

            boolean newWork = false;
            String previousRouteLocationName = null;
            List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
            for (RouteLocation rl : routeList) {
                // print info only if new location
                String routeLocationName = splitString(rl.getName());
                String locationName = routeLocationName;
                if (!routeLocationName.equals(previousRouteLocationName)) {
                    printLocationName(fileOut, locationName);
                    if (rl != train.getRoute().getDepartsRouteLocation()) {
                        fileOut.printRecord("AT", Bundle.getMessage("csvArrivalTime"), train.getExpectedArrivalTime(rl)); // NOI18N
                    }
                    if (rl == train.getRoute().getDepartsRouteLocation()) {
                        fileOut.printRecord("DT", Bundle.getMessage("csvDepartureTime"), train.getFormatedDepartureTime()); // NOI18N
                    } else if (!rl.getDepartureTime().equals(RouteLocation.NONE)) {
                        fileOut.printRecord("DTR", Bundle.getMessage("csvDepartureTimeRoute"), rl.getFormatedDepartureTime()); // NOI18N
                    } else {
                        fileOut.printRecord("EDT", Bundle.getMessage("csvEstimatedDepartureTime"), train.getExpectedDepartureTime(rl)); // NOI18N
                    }

                    Location location = rl.getLocation();
                    // add location comment
                    if (Setup.isPrintLocationCommentsEnabled() && !location.getComment().equals(Location.NONE)) {
                        // location comment can have multiple lines
                        String[] comments = location.getComment().split(NEW_LINE); // NOI18N
                        for (String comment : comments) {
                            printLocationComment(fileOut, comment);
                        }
                    }
                    if (Setup.isPrintTruncateManifestEnabled() && location.isSwitchListEnabled()) {
                        fileOut.printRecord("TRUN", Bundle.getMessage("csvTruncate"));
                    }
                }
                // add route comment
                if (!rl.getComment().equals(RouteLocation.NONE)) {
                    printRouteLocationComment(fileOut, rl.getComment());
                }

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

                // block pick up cars by destination
                boolean found = false; // begin blocking at rl
                for (RouteLocation rld : routeList) {
                    if (rld != rl && !found) {
                        continue;
                    }
                    found = true;
                    for (Car car : carList) {
                        if (car.getRouteLocation() == rl && car.getRouteDestination() == rld) {
                            newWork = true;
                            int count = 0;
                            if (car.isUtility()) {
                                count = countPickupUtilityCars(carList, car, IS_MANIFEST);
                                if (count == 0) {
                                    continue; // already done this set of utility cars
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
                List<Car> rsByLocation = carManager.getByLocationList();
                List<Car> cList = new ArrayList<>();
                for (Car rs : rsByLocation) {
                    if (rs.getLocation() == rl.getLocation() && rs.getRouteLocation() == null && rs.getTrack() != null) {
                        cList.add(rs);
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
                if (rl != train.getRoute().getTerminatesRouteLocation()) {
                    // Is the next location the same as the previous?
                    RouteLocation rlNext = train.getRoute().getNextRouteLocation(rl);
                    String nextRouteLocationName = splitString(rlNext.getName());
                    if (!routeLocationName.equals(nextRouteLocationName)) {
                        if (newWork) {
                            printTrainDeparts(fileOut, locationName, rl.getTrainDirectionString());
                            printTrainLength(fileOut, train.getTrainLength(rl), train.getNumberEmptyCarsInTrain(rl),
                                    train.getNumberCarsInTrain(rl));
                            printTrainWeight(fileOut, train.getTrainWeight(rl));
                            newWork = false;
                        } else {
                            fileOut.printRecord("NW", Bundle.getMessage("csvNoWork"));
                        }
                    }
                } else {
                    printTrainTerminates(fileOut, locationName);
                }
                previousRouteLocationName = routeLocationName;
            }
            // Are there any cars that need to be found?
            listCarsLocationUnknown(fileOut);

            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            log.error("Can not open CSV manifest file: {}", file.getName());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainCsvManifest.class);
}
