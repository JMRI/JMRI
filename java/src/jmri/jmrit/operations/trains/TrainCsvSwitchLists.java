// TrainCsvSwitchLists.java
package jmri.jmrit.operations.trains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a comma separated value (csv) switch list for a location on the
 * railroad.
 *
 * @author Daniel Boudreau (C) Copyright 2011, 2013, 2014, 2015
 * @version $Revision: 1 $
 *
 */
public class TrainCsvSwitchLists extends TrainCsvCommon {

    /**
     * builds a csv file containing the switch list for a location
     *
     * @param location
     * @return File
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public File buildSwitchList(Location location) {

        // create csv switch list file
        File file = TrainManagerXml.instance().createCsvSwitchListFile(location.getName());
        PrintWriter fileOut = null;

        try {
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")),// NOI18N
                    true); // NOI18N
        } catch (IOException e) {
            log.error("Can not open CSV switch list file: {}", file.getName());
            return null;
        }
        // build header
        addLine(fileOut, HEADER);
        addLine(fileOut, SWL); // this is a switch list
        addLine(fileOut, RN + ESC + Setup.getRailroadName() + ESC);

        addLine(fileOut, LN + ESC + splitString(location.getName()) + ESC);
        addLine(fileOut, PRNTR + ESC + location.getDefaultPrinterName() + ESC);
        addLine(fileOut, SWLC + ESC + location.getSwitchListComment() + ESC);
        // add location comment
        if (Setup.isPrintLocationCommentsEnabled() && !location.getComment().equals(Location.NONE)) {
            // location comment can have multiple lines
            String[] comments = location.getComment().split(NEW_LINE); // NOI18N
            for (String comment : comments) {
                addLine(fileOut, LC + ESC + comment + ESC);
            }
        }
        addLine(fileOut, VT + getDate(true));

        for (Train train : TrainManager.instance().getTrainsByTimeList()) {
            if (!train.isBuilt()) {
                continue; // train wasn't built so skip
            }
            if (!Setup.isSwitchListRealTime() && train.getSwitchListStatus().equals(Train.PRINTED)) {
                continue; // already printed this train
            }
            int pickupCars = 0;
            int dropCars = 0;
            int stops = 1;
            boolean trainDone = false;
            List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
            List<Engine> enginesList = EngineManager.instance().getByTrainBlockingList(train);
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
                // if it terminate at this location
                if (stops == 1) {
                    // newLine(fileOut);
                    addLine(fileOut, TN + train.getName());
                    addLine(fileOut, TM + train.getDescription());

                    if (train.isTrainEnRoute()) {
                        addLine(fileOut, TIR);
                        addLine(fileOut, ETE + expectedArrivalTime);
                    } else {
                        addLine(fileOut, DL + splitString(splitString(train.getTrainDepartsName())));
                        addLine(fileOut, DT + train.getDepartureTime());
                        if (rl == train.getRoute().getDepartsRouteLocation() && routeList.size() > 1) {
                            addLine(fileOut, TD + splitString(rl.getName()) + DEL + rl.getTrainDirectionString());
                        }
                        if (rl != train.getRoute().getDepartsRouteLocation()) {
                            addLine(fileOut, ETA + expectedArrivalTime);
                            addLine(fileOut, TA + splitString(rl.getName()) + DEL + rl.getTrainDirectionString());
                        }
                    }
                    if (rl == train.getRoute().getTerminatesRouteLocation()) {
                        addLine(fileOut, TT + splitString(rl.getName()));
                    }
                }
                if (stops > 1) {
                    // Print visit number, etc. only if previous location wasn't the same
                    if (rlPrevious == null || !splitString(rl.getName()).equals(splitString(rlPrevious.getName()))) {
                        // After the first time a train stops at a location provide:
                        // if the train has started its route
                        // the arrival time or relative time if the train has started its route
                        // the train's direction when it arrives
                        // if it terminate at this location

                        addLine(fileOut, VN + stops);
                        if (train.isTrainEnRoute()) {
                            addLine(fileOut, ETE + expectedArrivalTime);
                        } else {
                            addLine(fileOut, ETA + expectedArrivalTime);
                        }
                        addLine(fileOut, TA + splitString(rl.getName()) + DEL + rl.getTrainDirectionString());
                        if (rl == train.getRoute().getTerminatesRouteLocation()) {
                            addLine(fileOut, TT + splitString(rl.getName()));
                        }
                    } else {
                        stops--; // don't bump stop count, same location
                        // Does the train change direction?
                        if (rl.getTrainDirection() != rlPrevious.getTrainDirection()) {
                            addLine(fileOut, TDC + rl.getTrainDirectionString());
                        }
                    }
                }

                rlPrevious = rl;

                // add route comment
                if (!rl.getComment().equals(RouteLocation.NONE)) {
                    addLine(fileOut, RLC + ESC + rl.getComment() + ESC);
                }

                // engine change or helper service?
                checkForEngineOrCabooseChange(fileOut, train, rl);

                // go through the list of engines and determine if the engine departs here
                for (Engine engine : enginesList) {
                    if (engine.getRouteLocation() == rl && engine.getTrack() != null) {
                        fileOutCsvEngine(fileOut, engine, PL);
                    }
                }

                // get a list of cars and determine if this location is serviced
                // block pick up cars by destination
                for (RouteLocation rld : routeList) {
                    for (Car car : carList) {
                        if (car.getRouteLocation() == rl && car.getTrack() != null && car.getRouteDestination() == rld) {
                            pickupCars++;
                            int count = 0;
                            if (car.isUtility()) {
                                count = countPickupUtilityCars(carList, car, !IS_MANIFEST);
                                if (count == 0) {
                                    continue; // already done this set of utility cars
                                }
                            }
                            fileOutCsvCar(fileOut, car, PC, count);
                        }
                    }
                }

                for (Engine engine : enginesList) {
                    if (engine.getRouteDestination() == rl) {
                        fileOutCsvEngine(fileOut, engine, SL);
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
                        fileOutCsvCar(fileOut, car, SC, count);
                    }
                }
                stops++;
            }
            if (trainDone && pickupCars == 0 && dropCars == 0) {
                addLine(fileOut, TDONE);
            } else {
                if (stops > 1 && pickupCars == 0) {
                    addLine(fileOut, NCPU);
                }

                if (stops > 1 && dropCars == 0) {
                    addLine(fileOut, NCSO);
                }
            }
        }
        addLine(fileOut, END); // done with switch list
        
        // now list hold cars
        List<RollingStock> rsByLocation = CarManager.instance().getByLocationList();
        List<Car> carList = new ArrayList<Car>();
        for (RollingStock rs : rsByLocation) {
            if (rs.getLocation() != null && splitString(rs.getLocation().getName()).equals(splitString(location.getName())) 
                    && rs.getRouteLocation() == null) {
                carList.add((Car)rs);
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
            fileOutCsvCar(fileOut, car, HOLD, count);
        }
        addLine(fileOut, END); // done with hold cars
        
        // Are there any cars that need to be found?
        listCarsLocationUnknown(fileOut);
        fileOut.flush();
        fileOut.close();
        location.setStatus(Location.CSV_GENERATED);
        return file;
    }

    private final static Logger log = LoggerFactory.getLogger(TrainCsvSwitchLists.class.getName());
}
