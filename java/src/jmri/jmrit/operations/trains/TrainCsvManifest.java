package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a train's manifest using Comma Separated Values (csv).
 *
 * @author Daniel Boudreau Copyright (C) 2011, 2015
 *
 */
public class TrainCsvManifest extends TrainCsvCommon {

    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    private final static Logger log = LoggerFactory.getLogger(TrainCsvManifest.class);

    public TrainCsvManifest(Train train) {
        // create comma separated value manifest file
        File file = InstanceManager.getDefault(TrainManagerXml.class).createTrainCsvManifestFile(train.getName());

        PrintWriter fileOut;

        try {
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")),// NOI18N
            true); // NOI18N
        } catch (IOException e) {
            log.error("Can not open CSV manifest file: {}", file.getName());
            return;
        }
        // build header
        addLine(fileOut, HEADER);
        addLine(fileOut, RN + ESC + Setup.getRailroadName() + ESC);
        addLine(fileOut, TN + ESC + train.getName() + ESC);
        addLine(fileOut, TM + ESC + train.getDescription() + ESC);
        addLine(fileOut, PRNTR + ESC
                + locationManager.getLocationByName(train.getTrainDepartsName()).getDefaultPrinterName() + ESC);
        // add logo
        String logoURL = FileUtil.getExternalFilename(Setup.getManifestLogoURL());
        if (!train.getManifestLogoPathName().equals(Train.NONE)) {
            logoURL = FileUtil.getExternalFilename(train.getManifestLogoPathName());
        }
        if (!logoURL.equals("")) {
            addLine(fileOut, LOGO + logoURL);
        }
        addLine(fileOut, VT + getDate(true));
        // train comment can have multiple lines
        if (!train.getComment().equals(Train.NONE)) {
            String[] comments = train.getComment().split(NEW_LINE); // NOI18N
            for (String comment : comments) {
                addLine(fileOut, TC + ESC + comment + ESC);
            }
        }
        if (Setup.isPrintRouteCommentsEnabled()) {
            addLine(fileOut, RC + ESC + train.getRoute().getComment() + ESC);
        }

        // get engine and car lists
        List<Engine> engineList = engineManager.getByTrainBlockingList(train);
        List<Car> carList = carManager.getByTrainDestinationList(train);

        int cars = 0;
        int emptyCars = 0;
        boolean newWork = false;
        String previousRouteLocationName = null;
        List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
        for (RouteLocation rl : routeList) {
            // print info only if new location
            String routeLocationName = splitString(rl.getName());
            String locationName = routeLocationName;
            if (locationName.contains(DEL)) {
                log.debug("location name has delimiter: " + locationName);
                locationName = ESC + routeLocationName + ESC;
            }
            if (!routeLocationName.equals(previousRouteLocationName)) {
                addLine(fileOut, LN + locationName);
                if (rl != train.getRoute().getDepartsRouteLocation()) {
                    addLine(fileOut, AT + train.getExpectedArrivalTime(rl));
                }
                if (rl == train.getRoute().getDepartsRouteLocation()) {
                    addLine(fileOut, DT + train.getFormatedDepartureTime());
                } else if (!rl.getDepartureTime().equals(RouteLocation.NONE)) {
                    addLine(fileOut, DTR + rl.getFormatedDepartureTime());
                } else {
                    addLine(fileOut, EDT + train.getExpectedDepartureTime(rl));
                }

                Location location = rl.getLocation();
                // add location comment
                if (Setup.isPrintLocationCommentsEnabled() && !location.getComment().equals(Location.NONE)) {
                    // location comment can have multiple lines
                    String[] comments = location.getComment().split(NEW_LINE); // NOI18N
                    for (String comment : comments) {
                        addLine(fileOut, LC + ESC + comment + ESC);
                    }
                }
                if (Setup.isTruncateManifestEnabled() && location.isSwitchListEnabled()) {
                    addLine(fileOut, TRUN);
                }
            }
            // add route comment
            if (!rl.getComment().equals(RouteLocation.NONE)) {
                addLine(fileOut, RLC + ESC + rl.getComment() + ESC);
            }

            printTrackComments(fileOut, rl, carList);

            // engine change or helper service?
            checkForEngineOrCabooseChange(fileOut, train, rl);

            for (Engine engine : engineList) {
                if (engine.getRouteLocation() == rl) {
                    fileOutCsvEngine(fileOut, engine, PL);
                }
            }
            for (Engine engine : engineList) {
                if (engine.getRouteDestination() == rl) {
                    fileOutCsvEngine(fileOut, engine, SL);
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
                        cars++;
                        newWork = true;
                        if (car.getLoadType().equals(CarLoad.LOAD_TYPE_EMPTY)) {
                            emptyCars++;
                        }
                        int count = 0;
                        if (car.isUtility()) {
                            count = countPickupUtilityCars(carList, car, IS_MANIFEST);
                            if (count == 0) {
                                continue; // already done this set of utility cars
                            }
                        }
                        fileOutCsvCar(fileOut, car, PC, count);
                    }
                }
            }
            // car set outs
            for (Car car : carList) {
                if (car.getRouteDestination() == rl) {
                    cars--;
                    newWork = true;
                    if (InstanceManager.getDefault(CarLoads.class).getLoadType(car.getTypeName(), car.getLoadName()).equals(
                            CarLoad.LOAD_TYPE_EMPTY)) {
                        emptyCars--;
                    }
                    int count = 0;
                    if (car.isUtility()) {
                        count = countSetoutUtilityCars(carList, car, false, IS_MANIFEST);
                        if (count == 0) {
                            continue; // already done this set of utility cars
                        }
                    }
                    fileOutCsvCar(fileOut, car, SC, count);
                }
            }
            // car holds
            List<Car> rsByLocation = InstanceManager.getDefault(CarManager.class).getByLocationList();
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
                        && car.getTrack().acceptsPickupTrain(train) && car.getTrack().getPickupIds().length == 1
                        && car.getTrack().getPickupOption().equals(Track.TRAINS)) {
                    int count = 0;
                    if (car.isUtility()) {
                        count = countPickupUtilityCars(cList, car, !IS_MANIFEST);
                        if (count == 0) {
                            continue; // already done this set of utility cars
                        }
                    }
                    fileOutCsvCar(fileOut, car, HOLD, count);
                }
            }
            if (rl != train.getRoute().getTerminatesRouteLocation()) {
                // Is the next location the same as the previous?
                RouteLocation rlNext = train.getRoute().getNextRouteLocation(rl);
                String nextRouteLocationName = splitString(rlNext.getName());
                if (!routeLocationName.equals(nextRouteLocationName)) {
                    if (newWork) {
                        addLine(fileOut, TD + locationName + DEL + rl.getTrainDirectionString());
                        addLine(fileOut, TL + train.getTrainLength(rl) + DEL + emptyCars + DEL + cars);
                        addLine(fileOut, TW + train.getTrainWeight(rl));
                        newWork = false;
                    } else {
                        addLine(fileOut, NW);
                    }
                }
            } else {
                addLine(fileOut, TT + locationName);
            }
            previousRouteLocationName = routeLocationName;
        }
        // Are there any cars that need to be found?
        listCarsLocationUnknown(fileOut);

        fileOut.flush();
        fileOut.close();
    }
}
