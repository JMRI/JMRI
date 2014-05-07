package jmri.web.servlet.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import jmri.jmris.json.JSON;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.JsonManifest;
import jmri.jmrit.operations.trains.Train;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class Manifest extends HtmlTrainCommon {

    protected ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(Manifest.class);

    public Manifest(Locale locale, Train train) throws IOException {
        super(locale, train);
        this.mapper = new ObjectMapper();
        // TODO move loading strings to super constructor
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

    // TODO cache the results so a quick check that if the JsonManifest file is not
    // newer than the Html manifest, the cached copy is returned instead.
    public String getLocations() throws IOException {
        // build manifest from JSON manifest
        StringBuilder builder = new StringBuilder();
        JsonNode json = this.mapper.readTree((new JsonManifest(this.train)).getFile());
        ArrayNode locations = (ArrayNode) json.path(JSON.LOCATIONS);
        boolean hasWork;
        for (int r = 0; r < locations.size(); r++) {
            JsonNode location = locations.get(r);
            RouteLocation routeLocation = this.train.getRoute().getLocationById(location.path(JSON.ID).textValue());
            log.debug("Processing {} ({})", routeLocation.getName(), location.path(JSON.ID).textValue());
            String routeLocationName = location.path(JSON.NAME).textValue();
            builder.append(strings.getProperty("LocationStart")); // NOI18N
            hasWork = (location.path(JSON.CARS).path(JSON.ADD).size() > 0
                    || location.path(JSON.CARS).path(JSON.REMOVE).size() > 0
                    || location.path(JSON.ENGINES).path(JSON.ADD).size() > 0
                    || location.path(JSON.ENGINES).path(JSON.REMOVE).size() > 0);
            if (hasWork) {
                if (!train.isShowArrivalAndDepartureTimesEnabled()) {
                    builder.append(String.format(locale, strings.getProperty("ScheduledWorkAt"), routeLocationName)); // NOI18N
                } else if (r == 0) {
                    builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName, train.getFormatedDepartureTime())); // NOI18N
                } else if (!routeLocation.getDepartureTime().equals("")) {
                    builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName, routeLocation.getFormatedDepartureTime())); // NOI18N
                } else if (Setup.isUseDepartureTimeEnabled() && r != locations.size() - 1) {
                    builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName, train.getExpectedDepartureTime(routeLocation))); // NOI18N
                } else if (!train.getExpectedArrivalTime(routeLocation).equals("-1")) { // NOI18N
                    builder.append(String.format(locale, strings.getProperty("WorkArrivalTime"), routeLocationName, train.getExpectedArrivalTime(routeLocation))); // NOI18N
                } else {
                    builder.append(String.format(locale, strings.getProperty("ScheduledWorkAt"), routeLocationName)); // NOI18N
                }
                // add route comment
                if (!location.path(JSON.COMMENT).textValue().trim().equals("")) {
                    builder.append(String.format(locale, strings.getProperty("RouteLocationComment"), routeLocation.getComment().trim()));
                }

                builder.append(getTrackComments(location.path(JSON.TRACK), location.path(JSON.CARS)));

                // add location comment
                if (Setup.isPrintLocationCommentsEnabled() && !routeLocation.getLocation().getComment().trim().equals("")) {
                    builder.append(String.format(locale, strings.getProperty("LocationComment"), routeLocation.getLocation().getComment()));
                }
            }

            // engine change or helper service?
            if (location.path(JSON.OPTIONS).size() > 0) {
                Iterator<JsonNode> options = location.path(JSON.OPTIONS).elements();
                boolean changeEngines = false;
                boolean changeCaboose = false;
                while (options.hasNext()) {
                    String option = options.next().textValue();
                    if (option.equals(JSON.CHANGE_ENGINES)) {
                        changeEngines = true;
                    } else if (option.equals(JSON.CHANGE_CABOOSE)) {
                        changeCaboose = true;
                    } else if (option.equals(JSON.ADD_HELPERS)) {
                        builder.append(String.format(strings.getProperty("AddHelpersAt"), routeLocationName));
                    } else if (option.equals(JSON.REMOVE_HELPERS)) {
                        builder.append(String.format(strings.getProperty("RemoveHelpersAt"), routeLocationName));
                    }
                }
                if (changeEngines && changeCaboose) {
                    builder.append(String.format(strings.getProperty("LocoAndCabooseChangeAt"), routeLocationName)); // NOI18N
                } else if (changeEngines) {
                    builder.append(String.format(strings.getProperty("LocoChangeAt"), routeLocationName)); // NOI18N
                } else if (changeCaboose) {
                    builder.append(String.format(strings.getProperty("CabooseChangeAt"), routeLocationName)); // NOI18N
                }
            }

            builder.append(pickupEngines(location.path(JSON.ENGINES).path(JSON.ADD)));
            builder.append(blockCars(location.path(JSON.CARS), routeLocation, true));
            builder.append(dropEngines(location.path(JSON.ENGINES).path(JSON.REMOVE)));

            if (r != locations.size() - 1) {
                // work to be done?
                if (hasWork) {
                    if (!Setup.isPrintLoadsAndEmptiesEnabled()) {
                        // Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
                        builder.append(String.format(strings.getProperty("TrainDepartsCars"),
                                routeLocationName,
                                strings.getProperty("Heading" + Setup.getDirectionString(location.path(JSON.DIRECTION).intValue())),
                                location.path(JSON.LENGTH).path(JSON.LENGTH).intValue(),
                                location.path(JSON.LENGTH).path(JSON.UNIT).asText().toLowerCase(),
                                location.path(JSON.WEIGHT).intValue(),
                                location.path(JSON.CARS).path(JSON.TOTAL).intValue()));
                    } else {
                        // Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000 tons
                        builder.append(String.format(strings.getProperty("TrainDepartsLoads"),
                                routeLocationName,
                                strings.getProperty("Heading" + Setup.getDirectionString(location.path(JSON.DIRECTION).intValue())),
                                location.path(JSON.LENGTH).path(JSON.LENGTH).asText(),
                                location.path(JSON.LENGTH).path(JSON.UNIT).asText().toLowerCase(),
                                location.path(JSON.WEIGHT),
                                location.path(JSON.CARS).path(JSON.LOADS).intValue(),
                                location.path(JSON.CARS).path(JSON.EMPTIES).intValue()));
                    }
                } else {
                    log.debug("No work ({})", routeLocation.getComment());
                    if (routeLocation.getComment().trim().isEmpty()) {
                        // no route comment, no work at this location
                        if (train.isShowArrivalAndDepartureTimesEnabled()) {
                            if (r == 0) {
                                builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAtWithDepartureTime"),
                                        routeLocationName,
                                        train.getFormatedDepartureTime()));
                            } else if (!routeLocation.getDepartureTime().isEmpty()) {
                                builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAtWithDepartureTime"),
                                        routeLocationName,
                                        routeLocation.getFormatedDepartureTime()));
                            } else if (Setup.isUseDepartureTimeEnabled()) {
                                builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAtWithDepartureTime"),
                                        routeLocationName,
                                        location.path(JSON.EXPECTED_DEPARTURE)));
                            } else { // fall back to generic no scheduled work message
                                builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAt"), routeLocationName));
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
                                        routeLocation.getComment()));
                            } else if (!routeLocation.getDepartureTime().isEmpty()) {
                                builder.append(String.format(locale, strings.getProperty("CommentAtWithDepartureTime"),
                                        routeLocationName,
                                        routeLocation.getFormatedDepartureTime(),
                                        routeLocation.getComment()));
                            }
                        } else {
                            builder.append(String.format(locale, strings.getProperty("CommentAt"), routeLocationName, null, routeLocation.getComment()));
                        }
                    }
                    // add location comment
                    if (Setup.isPrintLocationCommentsEnabled() && !routeLocation.getLocation().getComment().isEmpty()) {
                        builder.append(String.format(locale, strings.getProperty("LocationComment"), routeLocation.getLocation().getComment()));
                    }
                }
            } else {
                builder.append(String.format(strings.getProperty("TrainTerminatesIn"), routeLocationName));
            }
        }
        return builder.toString();
    }

    protected String blockCars(JsonNode cars, RouteLocation location, boolean isManifest) {
        StringBuilder builder = new StringBuilder();
        if (cars.path(JSON.ADD).size() > 0 || cars.path(JSON.REMOVE).size() > 0) {
            if (cars.path(JSON.ADD).size() > 0) {
                for (JsonNode car : cars.path(JSON.ADD)) {
                    if (!this.isLocalMove(car)) {
                        if (this.isUtilityCar(car)) {
                            builder.append(pickupUtilityCars(cars, car, location, isManifest));
                        } // use truncated format if there's a switch list
                        else if (isManifest && Setup.isTruncateManifestEnabled() && location.getLocation().isSwitchListEnabled()) {
                            builder.append(pickUpCar(car, Setup.getTruncatedPickupManifestMessageFormat()));
                        } else {
                            builder.append(pickUpCar(car, Setup.getPickupCarMessageFormat()));
                        }
                    }
                }
            }
            if (cars.path(JSON.REMOVE).size() > 0) {
                for (JsonNode car : cars.path(JSON.REMOVE)) {
                    boolean local = isLocalMove(car);
                    if (this.isUtilityCar(car)) {
                        builder.append(setoutUtilityCars(cars, car, location, isManifest));
                    } else if (isManifest && Setup.isTruncateManifestEnabled() && location.getLocation().isSwitchListEnabled()) {
                        // use truncated format if there's a switch list
                        builder.append(dropCar(car, Setup.getTruncatedSetoutManifestMessageFormat(), this.isLocalMove(car)));
                    } else {
                        String[] format = (!local) ? Setup.getSwitchListDropCarMessageFormat() : Setup.getSwitchListLocalMessageFormat();
                        if (isManifest || Setup.isSwitchListFormatSameAsManifest()) {
                            format = (!local) ? Setup.getDropCarMessageFormat() : Setup.getLocalMessageFormat();
                        }
                        builder.append(dropCar(car, format, this.isLocalMove(car)));
                    }
                }
            }
        }
        return String.format(locale, strings.getProperty("CarsList"), builder.toString());
    }

    protected String pickupUtilityCars(JsonNode cars, JsonNode car, RouteLocation location, boolean isManifest) {
        // list utility cars by type, track, length, and load
        String[] messageFormat = Setup.getSwitchListPickupUtilityCarMessageFormat();
        if (isManifest || Setup.isSwitchListFormatSameAsManifest()) {
            messageFormat = Setup.getPickupUtilityCarMessageFormat();
        }
        // TODO: reimplement following commented out code
//        if (this.countUtilityCars(messageFormat, carList, car, location, rld, PICKUP) == 0) {
//            return ""; // already printed out this car type
//        }
        return this.pickUpCar(car, messageFormat);
    }

    protected String setoutUtilityCars(JsonNode cars, JsonNode car, RouteLocation location, boolean isManifest) {
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
        // TODO: reimplement following commented out code
//        if (countUtilityCars(messageFormat, carList, car, location, null, !PICKUP) == 0) {
//            return ""; // already printed out this car type
//        }
        return dropCar(car, messageFormat, isLocal);
    }

    protected String pickUpCar(JsonNode car, String[] format) {
        if (isLocalMove(car)) {
            return ""; // print nothing for local move, see dropCar()
        }
        StringBuilder builder = new StringBuilder();
        for (String attribute : format) {
            if (!attribute.trim().equals("")) {
                attribute = attribute.toLowerCase();
                log.debug("Adding car with attribute {}", attribute);
                if (attribute.equals(JSON.LOCATION)) {
                    builder.append(this.getFormattedAttribute(attribute, this.getPickupLocation(car.path(attribute)))).append(" "); // NOI18N
                } else {
                    builder.append(this.getTextAttribute(attribute, car)).append(" "); // NOI18N
                }
            }
        }
        log.debug("Picking up car {}", builder);
        return String.format(locale, strings.getProperty(this.resourcePrefix + "PickUpCar"), builder.toString()); // NOI18N
    }

    protected String dropCar(JsonNode car, String[] format, boolean isLocal) {
        StringBuilder builder = new StringBuilder();
        for (String attribute : format) {
            if (!attribute.trim().equals("")) {
                attribute = attribute.toLowerCase();
                log.debug("Removing car with attribute {}", attribute);
                if (attribute.equals(JSON.DESTINATION)) {
                    builder.append(this.getFormattedAttribute(attribute, this.getDropLocation(car.path(attribute)))).append(" "); // NOI18N
                } else if (attribute.equals(JSON.LOCATION)) {
                    builder.append(this.getFormattedAttribute(attribute, this.getPickupLocation(car.path(attribute)))).append(" "); // NOI18N
                } else {
                    builder.append(this.getTextAttribute(attribute, car)).append(" "); // NOI18N
                }
            }
        }
        log.debug("Dropping {}car {}", (isLocal) ? "local " : "", builder);
        if (!isLocal) {
            return String.format(locale, strings.getProperty(this.resourcePrefix + "DropCar"), builder.toString()); // NOI18N
        } else {
            return String.format(locale, strings.getProperty(this.resourcePrefix + "LocalCar"), builder.toString()); // NOI18N
        }
    }

    protected String dropEngines(JsonNode engines) {
        StringBuilder builder = new StringBuilder();
        if (engines.size() > 0) {
            for (JsonNode engine : engines) {
                builder.append(this.dropEngine(engine));
            }
        }
        return String.format(locale, strings.getProperty("EnginesList"), builder.toString());
    }

    protected String dropEngine(JsonNode engine) {
        StringBuilder builder = new StringBuilder();
        for (String attribute : Setup.getDropEngineMessageFormat()) {
            if (!attribute.trim().equals("")) {
                attribute = attribute.toLowerCase();
                if (attribute.equals(JSON.DESTINATION)) {
                    builder.append(this.getFormattedAttribute(attribute, this.getDropLocation(engine.path(attribute)))).append(" "); // NOI18N
                } else {
                    builder.append(this.getTextAttribute(attribute, engine)).append(" "); // NOI18N
                }
            }
        }
        log.debug("Drop engine: {}", builder);
        return String.format(locale, strings.getProperty(this.resourcePrefix + "DropEngine"), builder.toString());
    }

    protected String pickupEngines(JsonNode engines) {
        StringBuilder builder = new StringBuilder();
        if (engines.size() > 0) {
            for (JsonNode engine : engines) {
                builder.append(this.pickupEngine(engine));
            }
        }
        return String.format(locale, strings.getProperty("EnginesList"), builder.toString());
    }

    protected String pickupEngine(JsonNode engine) {
        StringBuilder builder = new StringBuilder();
        log.debug("PickupEngineMessageFormat: {}", (Object) Setup.getPickupEngineMessageFormat());
        for (String attribute : Setup.getPickupEngineMessageFormat()) {
            if (!attribute.trim().equals("")) {
                attribute = attribute.toLowerCase();
                if (attribute.equals(JSON.LOCATION)) {
                    builder.append(this.getFormattedAttribute(attribute, this.getPickupLocation(engine.path(attribute)))).append(" "); // NOI18N
                } else {
                    builder.append(this.getTextAttribute(attribute, engine)).append(" "); // NOI18N
                }
            }
        }
        log.debug("Picking up engine: {}", builder);
        return String.format(locale, strings.getProperty(this.resourcePrefix + "PickUpEngine"), builder.toString());
    }

    protected String getDropLocation(JsonNode location) {
        return this.getFormattedLocation(location, strings.getProperty("ToLocation"), strings.getProperty("ToTrack")); // NOI18N
    }

    protected String getPickupLocation(JsonNode location) {
        return this.getFormattedLocation(location, strings.getProperty("FromLocation"), strings.getProperty("FromTrack")); // NOI18N
    }

    protected String getTextAttribute(String attribute, JsonNode rollingStock) {
        if (attribute.equals(JSON.HAZARDOUS)) {
            return this.getFormattedAttribute(attribute, (rollingStock.path(attribute).asBoolean() ? Setup.getHazardousMsg() : "")); // NOI18N
        } else if (attribute.equals(Setup.PICKUP_COMMENT.toLowerCase())) { // NOI18N
            return this.getFormattedAttribute(JSON.ADD_COMMENT, rollingStock.path(JSON.ADD_COMMENT).textValue());
        } else if (attribute.equals(Setup.DROP_COMMENT.toLowerCase())) { // NOI18N
            return this.getFormattedAttribute(JSON.ADD_COMMENT, rollingStock.path(JSON.ADD_COMMENT).textValue());
        }
        return this.getFormattedAttribute(attribute, rollingStock.path(attribute).textValue());
    }

    protected String getFormattedAttribute(String attribute, String value) {
        return String.format(locale, strings.getProperty("Attribute"), value, attribute);
    }

    protected String getFormattedLocation(JsonNode location, String locationFormat, String trackFormat) {
        // TODO handle tracks without names
        return String.format(locale, trackFormat, StringEscapeUtils.escapeHtml4(location.path(JSON.TRACK).path(JSON.NAME).asText()));
    }

    private String getTrackComments(JsonNode tracks, JsonNode cars) {
        StringBuilder builder = new StringBuilder();
        if (tracks.size() > 0) {
            Iterator<Entry<String, JsonNode>> iterator = tracks.fields();
            while (iterator.hasNext()) {
                Entry<String, JsonNode> track = iterator.next();
                boolean pickup = false;
                boolean setout = false;
                if (cars.path(JSON.ADD).size() > 0) {
                    for (JsonNode car : cars.path(JSON.ADD)) {
                        if (track.getKey().equals(car.path(JSON.TRACK).path(JSON.ID).textValue())) {
                            pickup = true;
                            break; // we do not need to iterate all cars
                        }
                    }
                }
                if (cars.path(JSON.REMOVE).size() > 0) {
                    for (JsonNode car : cars.path(JSON.REMOVE)) {
                        if (track.getKey().equals(car.path(JSON.TRACK).path(JSON.ID).textValue())) {
                            setout = true;
                            break; // we do not need to iterate all cars
                        }
                    }
                }
                if (pickup && setout) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getValue().path(JSON.ADD_AND_REMOVE).textValue()));
                } else if (pickup) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getValue().path(JSON.ADD).textValue()));
                } else if (setout) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getValue().path(JSON.REMOVE).textValue()));
                }
            }
        }
        return builder.toString();
    }

    protected boolean isLocalMove(JsonNode car) {
        if (car.path(JSON.LOCATION).path(JSON.ROUTE).isMissingNode()
                || car.path(JSON.DESTINATION).path(JSON.ROUTE).isMissingNode()) {
            return false;
        }
        return car.path(JSON.LOCATION).path(JSON.ROUTE).equals(car.path(JSON.DESTINATION).path(JSON.ROUTE));
    }

    protected boolean isUtilityCar(JsonNode car) {
        return car.path(JSON.UTILITY).booleanValue();
    }
}
