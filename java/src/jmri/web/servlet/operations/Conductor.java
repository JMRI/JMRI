package jmri.web.servlet.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import static jmri.jmrit.operations.trains.TrainCommon.isThereWorkAtLocation;
import static jmri.jmrit.operations.trains.TrainCommon.splitString;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class Conductor extends Manifest {

    private final static Logger log = LoggerFactory.getLogger(Manifest.class);

    public Conductor(Locale locale, Train train) throws IOException {
        super(locale, train);
        this.resourcePrefix = "Conductor";
    }

    public String getLocation() throws IOException {
        RouteLocation location = train.getCurrentLocation();
        if (location == null) {
            return String.format(locale,
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale, "ConductorSnippet.html"))),
                    train.getIconName(),
                    train.getDescription(),
                    train.getComment(),
                    Setup.isPrintRouteCommentsEnabled() ? train.getRoute().getComment() : "",
                    strings.getProperty("Terminated"),
                    "", // terminated train has nothing to do
                    "", // engines in separate section
                    "", // pickup=true, local=false
                    "", // pickup=false, local=false
                    "", // pickup=false, local=true
                    "", // engines in separate section
                    "", // terminate with null string, use empty string to indicate terminated
                    strings.getProperty("Terminated")
            );
        }

        StringBuilder builder = new StringBuilder();
        List<RollingStock> engineList = EngineManager.instance().getByTrainList(train);
        List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
        log.debug("Train has {} cars assigned to it", carList.size());

        int sequenceId = location.getSequenceId();
        int sequence = train.getRoute().getLocationsBySequenceList().size();
        boolean work = isThereWorkAtLocation(train, location.getLocation());
        String pickups = performWork(true, false);  // pickup=true, local=false
        String setouts = performWork(false, false); // pickup=false, local=false
        String localMoves = performWork(false, true); // pickup=false, local=true

        return String.format(locale,
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale, "ConductorSnippet.html"))),
                train.getIconName(),
                train.getDescription(),
                train.getComment(),
                Setup.isPrintRouteCommentsEnabled() ? train.getRoute().getComment() : "",
                getCurrentAndNextLocation(),
                getLocationComments(),
                pickupEngines(engineList, location), // engines in separate section
                pickups,
                setouts,
                localMoves,
                dropEngines(engineList, location), // engines in separate section
                (train.getNextLocation(train.getCurrentLocation()) != null) ? train.getNextLocationName() : null,
                getMoveButton()
        );
    }

    private String getCurrentAndNextLocation() {
        if (train.getCurrentLocation() != null && train.getNextLocation(train.getCurrentLocation()) != null) {
            return String.format(locale, strings.getProperty("CurrentAndNextLocation"),
                    train.getCurrentLocationName(),
                    train.getNextLocationName());
        } else if (train.getCurrentLocation() != null) {
            return train.getCurrentLocationName();
        }
        return strings.getProperty("Terminated");
    }

    private String getMoveButton() {
        if (train.getNextLocation(train.getCurrentLocation()) != null) {
            return String.format(locale, strings.getProperty("MoveTo"), train.getNextLocationName());
        } else if (train.getCurrentLocation() != null) {
            return strings.getProperty("Terminate");
        }
        return strings.getProperty("Terminated");
    }

    // needed for location comments, not yet in formatter
    private String getEngineChanges(RouteLocation location) {
        // engine change or helper service?
        if (train.getSecondLegOptions() != Train.NONE) {
            if (location == train.getSecondLegStartLocation()) {
                return engineChange(location, train.getSecondLegOptions());
            }
            if (location == train.getSecondLegEndLocation() && train.getSecondLegOptions() == Train.HELPER_ENGINES) {
                return String.format(strings.getProperty("RemoveHelpersAt"), splitString(location.getName())); // NOI18N
            }
        }
        if (train.getThirdLegOptions() != Train.NONE) {
            if (location == train.getThirdLegStartLocation()) {
                return engineChange(location, train.getSecondLegOptions());
            }
            if (location == train.getThirdLegEndLocation() && train.getThirdLegOptions() == Train.HELPER_ENGINES) {
                return String.format(strings.getProperty("RemoveHelpersAt"), splitString(location.getName())); // NOI18N
            }
        }
        return "";
    }

    private String getLocationComments() {
        List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
        StringBuilder builder = new StringBuilder();
        RouteLocation location = train.getCurrentLocation();
        int r = location.getSequenceId();
        List<RouteLocation> sequence = train.getRoute().getLocationsBySequenceList();
        boolean work = isThereWorkAtLocation(train, location.getLocation());

        // print info only if new location
        String routeLocationName = splitString(location.getName());
        if (work) {
            // add line break between locations without work and ones with work
            // TODO sometimes an extra line break appears when the user has two or more locations with the
            // "same" name and the second location doesn't have work
            this.newWork = true;
            if (!train.isShowArrivalAndDepartureTimesEnabled()) {
                builder.append(String.format(locale, strings.getProperty("ScheduledWorkAt"), routeLocationName)); // NOI18N
            } else if (r == 0) {
                builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName, train.getFormatedDepartureTime())); // NOI18N
            } else if (!location.getDepartureTime().equals("")) {
                builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName, location.getFormatedDepartureTime())); // NOI18N
            } else if (Setup.isUseDepartureTimeEnabled() && r != sequence.size() - 1) {
                builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName, train.getExpectedDepartureTime(location))); // NOI18N
            } else if (!train.getExpectedArrivalTime(location).equals("-1")) { // NOI18N
                builder.append(String.format(locale, strings.getProperty("WorkArrivalTime"), routeLocationName, train.getExpectedArrivalTime(location))); // NOI18N
            } else {
                builder.append(String.format(locale, strings.getProperty("ScheduledWorkAt"), routeLocationName)); // NOI18N
            }
            // add route comment
            if (!location.getComment().trim().equals("")) {
                builder.append(String.format(locale, strings.getProperty("RouteLocationComment"), location.getComment()));
            }

            builder.append(getTrackComments(location, carList));

            // add location comment
            if (Setup.isPrintLocationCommentsEnabled() && !location.getLocation().getComment().equals("")) {
                builder.append(String.format(locale, strings.getProperty("LocationComment"), location.getLocation().getComment()));
            }
        }

        // engine change or helper service?
        if (train.getSecondLegOptions() != Train.NONE) {
            if (location == train.getSecondLegStartLocation()) {
                builder.append(engineChange(location, train.getSecondLegOptions()));
            }
            if (location == train.getSecondLegEndLocation() && train.getSecondLegOptions() == Train.HELPER_ENGINES) {
                builder.append(String.format(strings.getProperty("RemoveHelpersAt"), splitString(location.getName()))); // NOI18N
            }
        }
        if (train.getThirdLegOptions() != Train.NONE) {
            if (location == train.getThirdLegStartLocation()) {
                builder.append(engineChange(location, train.getSecondLegOptions()));
            }
            if (location == train.getThirdLegEndLocation() && train.getThirdLegOptions() == Train.HELPER_ENGINES) {
                builder.append(String.format(strings.getProperty("RemoveHelpersAt"), splitString(location.getName()))); // NOI18N
            }
        }

        if (r < sequence.size() - 1) {
            // Is the next location the same as the previous?
            RouteLocation rlNext = sequence.get(r + 1);
            if (!routeLocationName.equals(splitString(rlNext.getName()))) {
                if (newWork) {
                    if (!Setup.isPrintLoadsAndEmptiesEnabled()) {
                        // Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
                        builder.append(String.format(strings.getProperty("TrainDepartsCars"),
                                routeLocationName,
                                location.getTrainDirectionString(),
                                train.getTrainLength(location),
                                Setup.getLengthUnit().toLowerCase(),
                                train.getTrainWeight(location),
                                cars));
                    } else {
                        // Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
                        builder.append(String.format(strings.getProperty("TrainDepartsLoads"),
                                routeLocationName,
                                location.getTrainDirectionString(),
                                train.getTrainLength(location),
                                Setup.getLengthUnit().toLowerCase(),
                                train.getTrainWeight(location),
                                cars - emptyCars,
                                emptyCars));
                    }
                    newWork = false;
                } else {
                    if (location.getComment().trim().isEmpty()) {
                        // no route comment, no work at this location
                        if (train.isShowArrivalAndDepartureTimesEnabled()) {
                            if (r == 0) {
                                builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAtWithDepartureTime"),
                                        routeLocationName,
                                        train.getFormatedDepartureTime()));
                            } else if (!location.getDepartureTime().isEmpty()) {
                                builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAtWithDepartureTime"),
                                        routeLocationName,
                                        location.getFormatedDepartureTime()));
                            } else if (Setup.isUseDepartureTimeEnabled()) {
                                builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAtWithDepartureTime"),
                                        routeLocationName,
                                        train.getExpectedDepartureTime(location)));
                            }
                        } else {
                            builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAt"), routeLocationName));
                        }
                    } else {
                        // route comment, so only use location and route comment (for passenger trains)
                        if (train.isShowArrivalAndDepartureTimesEnabled()) {
                            if (r == 0) {
                                builder.append(String.format(locale, strings.getProperty("CommentAtWithDepartureTime"),
                                        routeLocationName,
                                        train.getFormatedDepartureTime(),
                                        location.getComment()));
                            } else if (!location.getDepartureTime().isEmpty()) {
                                builder.append(String.format(locale, strings.getProperty("CommentAtWithDepartureTime"),
                                        routeLocationName,
                                        location.getFormatedDepartureTime(),
                                        location.getComment()));
                            }
                        } else {
                            builder.append(String.format(locale, strings.getProperty("CommentAt"), routeLocationName, null, location.getComment()));
                        }
                    }
                    // add location comment
                    if (Setup.isPrintLocationCommentsEnabled() && !location.getLocation().getComment().isEmpty()) {
                        builder.append(String.format(locale, strings.getProperty("LocationComment"), location.getLocation().getComment()));
                    }
                }
            }
        } else {
            builder.append(String.format(strings.getProperty("TrainTerminatesIn"), routeLocationName));
        }
        return builder.toString();
    }

    private String performWork(boolean pickup, boolean local) {
        if (pickup) { // pick up
            StringBuilder builder = new StringBuilder();
            RouteLocation location = train.getCurrentLocation();
            List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
            List<Track> tracks = location.getLocation().getTrackByNameList(null);
            List<String> trackNames = new ArrayList<String>();
            List<String> pickedUp = new ArrayList<String>();
            this.clearUtilityCarTypes();
            for (Track track : tracks) {
                if (trackNames.contains(splitString(track.getName()))) {
                    continue;
                }
                trackNames.add(splitString(track.getName())); // use a track name once
                // block cars by destination
                for (RouteLocation rld : train.getRoute().getLocationsBySequenceList()) {
                    for (Car car : carList) {
                        if (pickedUp.contains(car.getId())
                                || (Setup.isSortByTrackEnabled()
                                && !splitString(track.getName()).equals(splitString(car.getTrackName())))) {
                            continue;
                        }
                        // note that a car in train doesn't have a track assignment
                        if (car.getRouteLocation() == location && car.getTrack() != null && car.getRouteDestination() == rld) {
                            pickedUp.add(car.getId());
                            if (car.isUtility()) {
                                builder.append(pickupUtilityCars(carList, car, location, rld, false));
                            } // use truncated format if there's a switch list
                            else if (Setup.isTruncateManifestEnabled() && location.getLocation().isSwitchListEnabled()) {
                                builder.append(pickUpCar(car, Setup.getTruncatedPickupManifestMessageFormat()));
                            } else {
                                builder.append(pickUpCar(car, Setup.getPickupCarMessageFormat()));
                            }
                            pickupCars = true;
                            cars++;
                            newWork = true;
                            if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(CarLoad.LOAD_TYPE_EMPTY)) {
                                emptyCars++;
                            }
                        }
                    }
                }
            }
            return builder.toString();
        } else { // local move
            return dropCars(local);
        }
    }

    private String dropCars(boolean local) {
        StringBuilder builder = new StringBuilder();
        RouteLocation location = train.getCurrentLocation();
        List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
        List<Track> tracks = location.getLocation().getTrackByNameList(null);
        List<String> trackNames = new ArrayList<String>();
        List<String> dropped = new ArrayList<String>();
        for (Track track : tracks) {
            if (trackNames.contains(splitString(track.getName()))) {
                continue;
            }
            trackNames.add(splitString(track.getName())); // use a track name once
            for (Car car : carList) {
                if (dropped.contains(car.getId())
                        || (Setup.isSortByTrackEnabled()
                        && !splitString(track.getName()).equals(splitString(car.getDestinationTrackName())))) {
                    continue;
                }
                if (isLocalMove(car) == local && (car.getRouteDestination() == location && car.getDestinationTrack() != null)) {
                    dropped.add(car.getId());
                    if (car.isUtility()) {
                        builder.append(setoutUtilityCars(carList, car, location, local));
                    } else if (Setup.isTruncateManifestEnabled() && location.getLocation().isSwitchListEnabled()) {
                        // use truncated format if there's a switch list
                        builder.append(dropCar(car, Setup.getTruncatedSetoutManifestMessageFormat(), local));
                    } else {
                        String[] format = (!local) ? Setup.getSwitchListDropCarMessageFormat() : Setup.getSwitchListLocalMessageFormat();
                        if (Setup.isSwitchListFormatSameAsManifest()) {
                            format = (!local) ? Setup.getDropCarMessageFormat() : Setup.getLocalMessageFormat();
                        }
                        builder.append(dropCar(car, format, local));
                    }
                    dropCars = true;
                    cars--;
                    newWork = true;
                    if (CarLoads.instance().getLoadType(car.getTypeName(), car.getLoadName()).equals(
                            CarLoad.LOAD_TYPE_EMPTY)) {
                        emptyCars--;
                    }
                }
            }
        }
        return builder.toString();
    }
}
