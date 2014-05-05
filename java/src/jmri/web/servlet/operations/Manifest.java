package jmri.web.servlet.operations;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainScheduleManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class Manifest extends TrainCommon {

    protected final Properties strings = new Properties();
    protected final Locale locale;
    protected final Train train;
    protected String resourcePrefix;
    private final static Logger log = LoggerFactory.getLogger(Manifest.class);

    public Manifest(Locale locale, Train train) throws IOException {
        this.locale = locale;
        this.train = train;
        FileInputStream is = null;
        try {
            is = new FileInputStream(Bundle.getMessage(locale, "ManifestStrings.properties"));
            strings.load(is);
            is.close();
        } catch (IOException ex) {
            if (is != null) {
                is.close();
            }
            throw ex;
        }
        this.cars = 0;
        this.emptyCars = 0;
        this.resourcePrefix = "Manifest";
    }

    public String getLocations() {
        // build manifest

        StringBuilder builder = new StringBuilder();
        List<Engine> engineList = EngineManager.instance().getByTrainBlockingList(train);

        List<Car> carList = CarManager.instance().getByTrainDestinationList(train);
        log.debug("Train has {} cars assigned to it", carList.size());

        boolean work = false;
        this.newWork = false; // true if current and next locations are the same
        boolean oldWork; // true if last location was worked
        String previousRouteLocationName = null;
        List<RouteLocation> sequence = train.getRoute().getLocationsBySequenceList();

        for (int r = 0; r < sequence.size(); r++) {
            RouteLocation location = sequence.get(r);
            oldWork = work;
            work = isThereWorkAtLocation(train, location.getLocation());

            // print info only if new location
            String routeLocationName = splitString(location.getName());
            if (work && !routeLocationName.equals(previousRouteLocationName) || (!oldWork && !this.newWork)) {
                // add line break between locations without work and ones with work
                // TODO sometimes an extra line break appears when the user has two or more locations with the
                // "same" name and the second location doesn't have work
                if (oldWork) {
                    builder.append("</li>"); // NOI18N
                }
                builder.append(strings.getProperty("LocationStart")); // NOI18N
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

            builder.append(pickupEngines(engineList, location));
            builder.append(blockCarsByTrack(carList, sequence, location, r, true));
            builder.append(dropEngines(engineList, location));

            if (r != sequence.size() - 1) {
                // Is the next location the same as the previous?
                RouteLocation rlNext = sequence.get(r + 1);
                if (!routeLocationName.equals(splitString(rlNext.getName()))) {
                    if (newWork) {
                        if (!Setup.isPrintLoadsAndEmptiesEnabled()) {
                            // Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
                            builder.append(String.format(strings.getProperty("TrainDepartsCars"),
                                    routeLocationName,
                                    strings.getProperty("Heading" + location.getTrainDirectionString()),
                                    train.getTrainLength(location),
                                    Setup.getLengthUnit().toLowerCase(),
                                    train.getTrainWeight(location),
                                    cars));
                        } else {
                            // Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
                            builder.append(String.format(strings.getProperty("TrainDepartsLoads"),
                                    routeLocationName,
                                    strings.getProperty("Heading" + location.getTrainDirectionString()),
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
            previousRouteLocationName = routeLocationName;
        }
        // Are there any cars that need to be found?
        builder.append(addCarsLocationUnknown());
        return builder.toString();
    }

    private String blockCarsByTrack(List<Car> carList, List<RouteLocation> routeList, RouteLocation location, int r, boolean isManifest) {
        StringBuilder builder = new StringBuilder();
        List<Track> tracks = location.getLocation().getTrackByNameList(null);
        List<String> trackNames = new ArrayList<String>();
        this.clearUtilityCarTypes();
        for (Track track : tracks) {
            if (trackNames.contains(splitString(track.getName()))) {
                continue;
            }
            trackNames.add(splitString(track.getName())); // use a track name once
            // block cars by destination
            for (int j = r; j < routeList.size(); j++) {
                RouteLocation rld = routeList.get(j);
                for (int k = 0; k < carList.size(); k++) {
                    Car car = carList.get(k);
                    if (Setup.isSortByTrackEnabled()
                            && !splitString(track.getName()).equals(splitString(car.getTrackName()))) {
                        continue;
                    }
                    // note that a car in train doesn't have a track assignment
                    if (car.getRouteLocation() == location && car.getTrack() != null && car.getRouteDestination() == rld) {
                        if (car.isUtility()) {
                            builder.append(pickupUtilityCars(carList, car, location, rld, isManifest));
                        } // use truncated format if there's a switch list
                        else if (isManifest && Setup.isTruncateManifestEnabled() && location.getLocation().isSwitchListEnabled()) {
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
            for (int j = 0; j < carList.size(); j++) {
                Car car = carList.get(j);
                if (Setup.isSortByTrackEnabled()
                        && !splitString(track.getName()).equals(splitString(car.getDestinationTrackName()))) {
                    continue;
                }
                if (car.getRouteDestination() == location && car.getDestinationTrack() != null) {
                    boolean local = isLocalMove(car);
                    if (car.isUtility()) {
                        builder.append(setoutUtilityCars(carList, car, location, isManifest));
                    } else if (isManifest && Setup.isTruncateManifestEnabled() && location.getLocation().isSwitchListEnabled()) {
                        // use truncated format if there's a switch list
                        builder.append(dropCar(car, Setup.getTruncatedSetoutManifestMessageFormat(), isLocalMove(car)));
                    } else {
                        String[] format = (!local) ? Setup.getSwitchListDropCarMessageFormat() : Setup.getSwitchListLocalMessageFormat();
                        if (isManifest || Setup.isSwitchListFormatSameAsManifest()) {
                            format = (!local) ? Setup.getDropCarMessageFormat() : Setup.getLocalMessageFormat();
                        }
                        builder.append(dropCar(car, format, isLocalMove(car)));
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
            if (!Setup.isSortByTrackEnabled()) {
                break; // done
            }
        }
        return String.format(locale, strings.getProperty("CarsList"), builder.toString());
    }

    @Override
    public String pickupUtilityCars(List<Car> carList, Car car, RouteLocation rl, RouteLocation rld, boolean isManifest) {
        // list utility cars by type, track, length, and load
        String[] messageFormat = Setup.getSwitchListPickupUtilityCarMessageFormat();
        if (isManifest || Setup.isSwitchListFormatSameAsManifest()) {
            messageFormat = Setup.getPickupUtilityCarMessageFormat();
        }
        if (this.countUtilityCars(messageFormat, carList, car, rl, rld, PICKUP) == 0) {
            return ""; // already printed out this car type
        }
        return pickUpCar(car, messageFormat);
    }

    protected String setoutUtilityCars(List<Car> carList, Car car, RouteLocation rl, boolean isManifest) {
        boolean isLocal = isLocalMove(car);
        if (Setup.isSwitchListFormatSameAsManifest()) {
            isManifest = true;
        }
        String[] messageFormat = Setup.getSetoutUtilityCarMessageFormat();
        if (isLocal && isManifest) {
            messageFormat = Setup.getLocalUtilityCarMessageFormat();
        } else if (isLocal && !isManifest) {
            messageFormat = Setup.getSwitchListLocalUtilityCarMessageFormat();
        } else if (!isLocal && !isManifest) {
            messageFormat = Setup.getSwitchListSetoutUtilityCarMessageFormat();
        }
        if (countUtilityCars(messageFormat, carList, car, rl, null, !PICKUP) == 0) {
            return ""; // already printed out this car type
        }
        return dropCar(car, messageFormat, isLocal);
    }

    protected String pickUpCar(Car car, String[] format) {
        if (isLocalMove(car)) {
            return ""; // print nothing local move, see dropCar
        }
        StringBuilder builder = new StringBuilder();
        for (String attribute : format) {
            builder.append(String.format(locale, strings.getProperty("Attribute"), getCarAttribute(car, attribute, PICKUP, !LOCAL), attribute.toLowerCase())).append(" "); // NOI18N
        }
        log.debug("Picking up car {}", builder);
        return String.format(locale, strings.getProperty(this.resourcePrefix + "PickUpCar"), builder.toString()); // NOI18N
    }

    protected String dropCar(Car car, String[] format, boolean isLocal) {
        StringBuilder builder = new StringBuilder();
        for (String attribute : format) {
            builder.append(String.format(locale, strings.getProperty("Attribute"), getCarAttribute(car, attribute, !PICKUP, isLocal), attribute.toLowerCase())).append(" "); // NOI18N
        }
        log.debug("Dropping {}car {}", (isLocal) ? "local " : "", builder);
        if (!isLocal) {
            return String.format(locale, strings.getProperty(this.resourcePrefix + "DropCar"), builder.toString()); // NOI18N
        } else {
            return String.format(locale, strings.getProperty(this.resourcePrefix + "LocalCar"), builder.toString()); // NOI18N
        }
    }

    private String addCarsLocationUnknown() {
        List<Car> miaCars = CarManager.instance().getCarsLocationUnknown();
        if (miaCars.isEmpty()) {
            return ""; // NOI18N // no cars to search for!
        }
        StringBuilder builder = new StringBuilder();
        for (Car car : miaCars) {
            builder.append(addSearchForCar(car));
        }
        return String.format(locale, strings.getProperty("CarsLocationUnknown"), builder.toString()); // NOI18N
    }

    private String addSearchForCar(Car car) {
        StringBuilder builder = new StringBuilder();
        for (String string : Setup.getMissingCarMessageFormat()) {
            builder.append(getCarAttribute(car, string, !PICKUP, !LOCAL));
        }
        return builder.toString();
    }

    protected String engineChange(RouteLocation location, int legOptions) {
        if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
            return String.format(strings.getProperty("AddHelpersAt"), splitString(location.getName())); // NOI18N
        } else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES
                && ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE || (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE)) {
            return String.format(strings.getProperty("LocoAndCabooseChangeAt"), splitString(location.getName())); // NOI18N
        } else if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            return String.format(strings.getProperty("LocoChangeAt"), splitString(location.getName())); // NOI18N
        } else if ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE
                || (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            return String.format(strings.getProperty("CabooseChangeAt"), splitString(location.getName())); // NOI18N
        }
        return "";
    }

    protected String dropEngines(List<Engine> engines, RouteLocation location) {
        StringBuilder builder = new StringBuilder();
        for (Engine engine : engines) {
            if (engine.getRouteDestination().equals(location)) {
                builder.append(dropEngine(engine));
            }
        }
        return String.format(strings.getProperty("EnginesList"), builder.toString());
    }

    @Override
    public String dropEngine(Engine engine) {
        StringBuilder builder = new StringBuilder();
        for (String attribute : Setup.getDropEngineMessageFormat()) {
            builder.append(String.format(locale, strings.getProperty("Attribute"), getEngineAttribute(engine, attribute, false), attribute.toLowerCase())).append(" ");
        }
        log.debug("Drop engine: {}", builder);
        return String.format(locale, strings.getProperty(this.resourcePrefix + "DropEngine"), builder.toString());
    }

    protected String pickupEngines(List<Engine> engines, RouteLocation location) {
        StringBuilder builder = new StringBuilder();
        for (Engine engine : engines) {
            if (engine.getRouteLocation().equals(location) && !engine.getTrackName().equals("")) {
                builder.append(pickupEngine(engine));
            }
        }
        return String.format(locale, strings.getProperty("EnginesList"), builder.toString());
    }

    @Override
    public String pickupEngine(Engine engine) {
        StringBuilder builder = new StringBuilder();
        for (String attribute : Setup.getPickupEngineMessageFormat()) {
            builder.append(String.format(locale, strings.getProperty("Attribute"), getEngineAttribute(engine, attribute, true), attribute.toLowerCase())).append(" ");
        }
        log.debug("Picking up engine: {}", builder);
        return String.format(locale, strings.getProperty(this.resourcePrefix + "PickUpEngine"), builder.toString());
    }

    private String getCarAttribute(Car car, String attribute, boolean isPickup, boolean isLocal) {
        if (attribute.equals(Setup.LOAD)) {
            return (car.isCaboose() || car.isPassenger()) ? "" : StringEscapeUtils.escapeHtml4(car.getLoadName()); // NOI18N
        } else if (attribute.equals(Setup.HAZARDOUS)) {
            return (car.isHazardous() ? Setup.getHazardousMsg() : ""); // NOI18N
        } else if (attribute.equals(Setup.DROP_COMMENT)) {
            return car.getDropComment();
        } else if (attribute.equals(Setup.PICKUP_COMMENT)) {
            return car.getPickupComment();
        } else if (attribute.equals(Setup.KERNEL)) {
            return car.getKernelName();
        } else if (attribute.equals(Setup.RWE)) {
            if (!car.getReturnWhenEmptyDestName().equals("")) {
                return String.format(locale, strings.getProperty("RWE"),
                        StringEscapeUtils.escapeHtml4(splitString(car.getReturnWhenEmptyDestinationName())),
                        StringEscapeUtils.escapeHtml4(splitString(car.getReturnWhenEmptyDestTrackName())));
            }
            return ""; // NOI18N
        } else if (attribute.equals(Setup.FINAL_DEST)) {
            if (!car.getFinalDestinationName().equals("")) {
                return String.format(locale, strings.getProperty("FinalDestination"),
                        StringEscapeUtils.escapeHtml4(splitString(car.getFinalDestinationName())));
            }
            return "";
        } else if (attribute.equals(Setup.FINAL_DEST_TRACK)) {
            if (!car.getFinalDestinationName().equals("")) {
                return String.format(locale, strings.getProperty("FinalDestinationWithTrack"),
                        StringEscapeUtils.escapeHtml4(splitString(car.getFinalDestinationName())),
                        StringEscapeUtils.escapeHtml4(splitString(car.getFinalDestinationTrackName())));
            }
            return "";
        }
        return getRollingStockAttribute(car, attribute, isPickup, isLocal);
    }

    private String getEngineAttribute(Engine engine, String attribute, boolean isPickup) {
        if (attribute.equals(Setup.MODEL)) {
            return engine.getModel();
        }
        if (attribute.equals(Setup.CONSIST)) {
            return engine.getConsistName();
        }
        return getRollingStockAttribute(engine, attribute, isPickup, false);
    }

    private String getRollingStockAttribute(RollingStock rs, String attribute, boolean isPickup, boolean isLocal) {
        if (attribute.equals(Setup.NUMBER)) {
            return splitString(rs.getNumber());
        } else if (attribute.equals(Setup.ROAD)) {
            return StringEscapeUtils.escapeHtml4(rs.getRoadName());
        } else if (attribute.equals(Setup.TYPE)) {
            String[] type = rs.getTypeName().split("-"); // second half of string
            // can be anything
            return type[0];
        } else if (attribute.equals(Setup.LENGTH)) {
            return rs.getLength();
        } else if (attribute.equals(Setup.COLOR)) {
            return rs.getColor();
        } else if (attribute.equals(Setup.LOCATION) && (isPickup || isLocal)) {
            if (rs.getTrack() != null) {
                return String.format(locale, strings.getProperty("FromTrack"),
                        StringEscapeUtils.escapeHtml4(rs.getTrackName()));
            }
            return "";
        } else if (attribute.equals(Setup.LOCATION) && !isPickup && !isLocal) {
            return String.format(locale, strings.getProperty("FromLocation"),
                    StringEscapeUtils.escapeHtml4(rs.getLocationName()));
        } else if (attribute.equals(Setup.DESTINATION) && isPickup) {
            return String.format(locale, strings.getProperty("ToLocation"),
                    StringEscapeUtils.escapeHtml4(splitString(rs.getDestinationName())));
        } else if (attribute.equals(Setup.DESTINATION) && !isPickup) {
            return String.format(locale, strings.getProperty("ToTrack"),
                    StringEscapeUtils.escapeHtml4(splitString(rs.getDestinationTrackName())));
        } else if (attribute.equals(Setup.DEST_TRACK)) {
            return String.format(locale, strings.getProperty("ToLocationAndTrack"),
                    StringEscapeUtils.escapeHtml4(splitString(rs.getDestinationName())),
                    StringEscapeUtils.escapeHtml4(splitString(rs.getDestinationTrackName())));
        } else if (attribute.equals(Setup.OWNER)) {
            return StringEscapeUtils.escapeHtml4(rs.getOwner());
        } else if (attribute.equals(Setup.COMMENT)) {
            return StringEscapeUtils.escapeHtml4(rs.getComment());
        } else if (attribute.equals(Setup.NONE) || attribute.equals(Setup.NO_NUMBER)
                || attribute.equals(Setup.NO_ROAD) || attribute.equals(Setup.NO_COLOR)
                || attribute.equals(Setup.NO_DESTINATION) || attribute.equals(Setup.NO_DEST_TRACK)
                || attribute.equals(Setup.NO_LOCATION) || attribute.equals(Setup.TAB)
                || attribute.equals(Setup.TAB2) || attribute.equals(Setup.TAB3)) { // attributes that don't print
            return "";
        }
        return Bundle.getMessage(locale, "ErrorPrintOptions"); // something is isn't right!
    }

    protected String getTrackComments(RouteLocation location, List<Car> cars) {
        StringBuilder builder = new StringBuilder();
        if (location.getLocation() != null) {
            List<Track> tracks = location.getLocation().getTrackByNameList(null);
            for (Track track : tracks) {
                // any pick ups or set outs to this track?
                boolean pickup = false;
                boolean setout = false;
                for (int j = 0; j < cars.size(); j++) {
                    Car car = cars.get(j);
                    if (car.getRouteLocation() == location && car.getTrack() != null && car.getTrack() == track) {
                        pickup = true;
                    }
                    if (car.getRouteDestination() == location && car.getDestinationTrack() != null
                            && car.getDestinationTrack() == track) {
                        setout = true;
                    }
                }
                // print the appropriate comment if there's one
                if (pickup && setout && !track.getCommentBoth().equals("")) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getCommentBoth()));
                } else if (pickup && !setout && !track.getCommentPickup().equals("")) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getCommentPickup()));
                } else if (!pickup && setout && !track.getCommentSetout().equals("")) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getCommentSetout()));
                }
            }
        }
        return builder.toString();
    }

    public String getValidity() {
        if (Setup.isPrintTimetableNameEnabled()) {
            return String.format(locale, strings.getProperty("ManifestValidityWithSchedule"), getDate(true), TrainScheduleManager.instance().getScheduleById(train.getId()));
        } else {
            return String.format(locale, strings.getProperty("ManifestValidity"), getDate(true));
        }
    }

}
