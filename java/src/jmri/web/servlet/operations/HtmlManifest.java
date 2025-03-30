package jmri.web.servlet.operations;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.JsonManifest;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.server.json.JSON;
import jmri.server.json.operations.JsonOperations;

/**
 *
 * @author Randall Wood
 */
public class HtmlManifest extends HtmlTrainCommon {

    protected ObjectMapper mapper;
    private JsonNode jsonManifest = null;
    private final static Logger log = LoggerFactory.getLogger(HtmlManifest.class);

    public HtmlManifest(Locale locale, Train train) throws IOException {
        super(locale, train);
        this.mapper = new ObjectMapper();
        this.resourcePrefix = "Manifest";
    }

    // TODO cache the results so a quick check that if the JsonManifest file is not
    // newer than the Html manifest, the cached copy is returned instead.
    public String getLocations() throws IOException {
        // build manifest from JSON manifest
        if (this.getJsonManifest() == null) {
            return "Error manifest file not found for this train";
        }
        StringBuilder builder = new StringBuilder();
        JsonNode locations = this.getJsonManifest().path(JSON.LOCATIONS);
        String previousLocationName = null;
        boolean hasWork;
        for (JsonNode location : locations) {
            RouteLocation routeLocation = train.getRoute().getRouteLocationById(location.path(JSON.NAME).textValue());
            log.debug("Processing {} ({})", routeLocation.getName(), location.path(JSON.NAME).textValue());
            String routeLocationName = location.path(JSON.USERNAME).textValue();
            builder.append(String.format(locale, strings.getProperty("LocationStart"), routeLocation.getId())); // NOI18N
            hasWork = (location.path(JsonOperations.CARS).path(JSON.ADD).size() > 0
                    || location.path(JsonOperations.CARS).path(JSON.REMOVE).size() > 0
                    || location.path(JSON.ENGINES).path(JSON.ADD).size() > 0 || location.path(JSON.ENGINES).path(
                            JSON.REMOVE).size() > 0);
            if (hasWork && !routeLocationName.equals(previousLocationName)) {
                if (!train.isShowArrivalAndDepartureTimesEnabled()) {
                    builder.append(String.format(locale, strings.getProperty("ScheduledWorkAt"), routeLocationName)); // NOI18N
                } else if (routeLocation == train.getTrainDepartsRouteLocation()) {
                    builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName,
                            train.getFormatedDepartureTime())); // NOI18N
                } else if (!routeLocation.getDepartureTime().equals(RouteLocation.NONE)) {
                    builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName,
                            routeLocation.getFormatedDepartureTime())); // NOI18N
                } else if (Setup.isUseDepartureTimeEnabled()
                        && routeLocation != train.getTrainTerminatesRouteLocation()) {
                    builder.append(String.format(locale, strings.getProperty("WorkDepartureTime"), routeLocationName,
                            train.getExpectedDepartureTime(routeLocation))); // NOI18N
                } else if (!train.getExpectedArrivalTime(routeLocation).equals(Train.ALREADY_SERVICED)) { // NOI18N
                    builder.append(String.format(locale, strings.getProperty("WorkArrivalTime"), routeLocationName,
                            train.getExpectedArrivalTime(routeLocation))); // NOI18N
                } else {
                    builder.append(String.format(locale, strings.getProperty("ScheduledWorkAt"), routeLocationName)); // NOI18N
                }
                // add route comment
                if (!location.path(JSON.COMMENT).textValue().isBlank()) {
                    builder.append(String.format(locale, strings.getProperty("RouteLocationComment"), 
                            location.path(JSON.COMMENT).textValue()));
                }

                // add location comment
                if (Setup.isPrintLocationCommentsEnabled()
                        && !location.path(JSON.LOCATION).path(JSON.COMMENT).textValue().isBlank()) {
                    builder.append(String.format(locale, strings.getProperty("LocationComment"), location.path(
                            JSON.LOCATION).path(JSON.COMMENT).textValue()));
                }

                // add track comments
                builder.append(
                        getTrackComments(location.path(JSON.TRACK), location.path(JsonOperations.CARS)));
            }

            previousLocationName = routeLocationName;

            // engine change or helper service?
            if (location.path(JSON.OPTIONS).size() > 0) {
                boolean changeEngines = false;
                boolean changeCaboose = false;
                for (JsonNode option : location.path(JSON.OPTIONS)) {
                    switch (option.asText()) {
                        case JSON.CHANGE_ENGINES:
                            changeEngines = true;
                            break;
                        case JSON.CHANGE_CABOOSE:
                            changeCaboose = true;
                            break;
                        case JSON.ADD_HELPERS:
                            builder.append(String.format(strings.getProperty("AddHelpersAt"), routeLocationName));
                            break;
                        case JSON.REMOVE_HELPERS:
                            builder.append(String.format(strings.getProperty("RemoveHelpersAt"), routeLocationName));
                            break;
                        default:
                            break;
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
            builder.append(blockCars(location.path(JsonOperations.CARS), routeLocation, true));
            builder.append(dropEngines(location.path(JSON.ENGINES).path(JSON.REMOVE)));

            if (routeLocation != train.getTrainTerminatesRouteLocation()) {
                // Is the next location the same as the current?
                RouteLocation rlNext = train.getRoute().getNextRouteLocation(routeLocation);
                if (!routeLocationName.equals(rlNext.getSplitName())) {
                    if (hasWork) {
                        if (!Setup.isPrintLoadsAndEmptiesEnabled()) {
                            // Message format: Train departs Boston Westbound with 12 cars, 450 feet, 3000 tons
                            builder.append(String.format(strings.getProperty("TrainDepartsCars"), routeLocationName,
                                    strings.getProperty("Heading"
                                            + Setup.getDirectionString(location.path(JSON.TRAIN_DIRECTION).intValue())),
                                    location.path(JSON.LENGTH).path(JSON.LENGTH).intValue(), location.path(JSON.LENGTH)
                                    .path(JSON.UNIT).asText().toLowerCase(), location.path(JSON.WEIGHT)
                                    .intValue(), location.path(JsonOperations.CARS).path(JSON.TOTAL).intValue()));
                        } else {
                            // Message format: Train departs Boston Westbound with 4 loads, 8 empties, 450 feet, 3000
                            // tons
                            builder.append(String.format(strings.getProperty("TrainDepartsLoads"), routeLocationName,
                                    strings.getProperty("Heading"
                                            + Setup.getDirectionString(location.path(JSON.TRAIN_DIRECTION).intValue())),
                                    location.path(JSON.LENGTH).path(JSON.LENGTH).intValue(), location.path(JSON.LENGTH)
                                    .path(JSON.UNIT).asText().toLowerCase(), location.path(JSON.WEIGHT)
                                    .intValue(), location.path(JsonOperations.CARS).path(JSON.LOADS).intValue(), location
                                    .path(JsonOperations.CARS).path(JSON.EMPTIES).intValue()));
                        }
                    } else {
                        log.debug("No work ({})", routeLocation.getComment());
                        if (routeLocation.getComment().isBlank()) {
                            // no route comment, no work at this location
                            if (train.isShowArrivalAndDepartureTimesEnabled()) {
                                if (routeLocation == train.getTrainDepartsRouteLocation()) {
                                    builder.append(String.format(locale, strings
                                            .getProperty("NoScheduledWorkAtWithDepartureTime"), routeLocationName,
                                            train.getFormatedDepartureTime()));
                                } else if (!routeLocation.getDepartureTime().isEmpty()) {
                                    builder.append(String.format(locale, strings
                                            .getProperty("NoScheduledWorkAtWithDepartureTime"), routeLocationName,
                                            routeLocation.getFormatedDepartureTime()));
                                } else if (Setup.isUseDepartureTimeEnabled()) {
                                    builder.append(String.format(locale, strings
                                            .getProperty("NoScheduledWorkAtWithDepartureTime"), routeLocationName,
                                            location.path(JSON.EXPECTED_DEPARTURE)));
                                } else { // fall back to generic no scheduled work message
                                    builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAt"),
                                            routeLocationName));
                                }
                            } else {
                                builder.append(String.format(locale, strings.getProperty("NoScheduledWorkAt"),
                                        routeLocationName));
                            }
                        } else {
                            // if a route comment, then only use location name and route comment, useful for passenger
                            // trains
                            if (!routeLocation.getComment().isBlank()) {
                                builder.append(String.format(locale, strings.getProperty("CommentAt"), // NOI18N
                                        routeLocationName, StringEscapeUtils
                                                .escapeHtml4(routeLocation.getCommentWithColor())));
                            }
                            if (train.isShowArrivalAndDepartureTimesEnabled()) {
                                if (routeLocation == train.getTrainDepartsRouteLocation()) {
                                    builder.append(String.format(locale, strings
                                            .getProperty("CommentAtWithDepartureTime"), routeLocationName, train // NOI18N
                                            .getFormatedDepartureTime(), StringEscapeUtils
                                            .escapeHtml4(routeLocation.getComment())));
                                } else if (!routeLocation.getDepartureTime().equals(RouteLocation.NONE)) {
                                    builder.append(String.format(locale, strings
                                            .getProperty("CommentAtWithDepartureTime"), routeLocationName, // NOI18N
                                            routeLocation.getFormatedDepartureTime(), StringEscapeUtils
                                            .escapeHtml4(routeLocation.getComment())));
                                } else if (Setup.isUseDepartureTimeEnabled() &&
                                        !routeLocation.getComment().equals(RouteLocation.NONE)) {
                                    builder.append(String.format(locale, strings
                                            .getProperty("NoScheduledWorkAtWithDepartureTime"), routeLocationName, // NOI18N
                                            train.getExpectedDepartureTime(routeLocation)));
                                }
                            }                           
                        }
                        // add location comment
                        if (Setup.isPrintLocationCommentsEnabled()
                                && !routeLocation.getLocation().getComment().isEmpty()) {
                            builder.append(String.format(locale, strings.getProperty("LocationComment"),
                                    StringEscapeUtils.escapeHtml4(routeLocation.getLocation().getCommentWithColor())));
                        }
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
        log.debug("Cars is {}", cars);

        //copy the adds into a sortable arraylist
        ArrayList<JsonNode> adds = new ArrayList<JsonNode>();
        cars.path(JSON.ADD).forEach(adds::add);
            
        //sort if requested
        if (adds.size() > 0 && Setup.isSortByTrackNameEnabled()) {
            adds.sort(Comparator.comparing(o -> o.path("location").path("track").path("userName").asText()));
        }
        //format each car for output
        // use truncated format if there's a switch list
        for (JsonNode car : adds) {
            if (!this.isLocalMove(car)) {
                if (this.isUtilityCar(car)) {
                    builder.append(pickupUtilityCars(adds, car, location, isManifest));
                } else {
                    String[] messageFormat;
                    if (isManifest &&
                            Setup.isPrintTruncateManifestEnabled() &&
                            location.getLocation().isSwitchListEnabled()) {
                        messageFormat = Setup.getPickupTruncatedManifestMessageFormat();
                    } else {
                        messageFormat = Setup.getPickupManifestMessageFormat();
                    }
                    Setup.stringToTagConversion(messageFormat);
                    builder.append(pickUpCar(car, messageFormat));
                }
            }
        }

        //copy the drops into a sortable arraylist
        ArrayList<JsonNode> drops = new ArrayList<JsonNode>();
        cars.path(JSON.REMOVE).forEach(drops::add);

        for (JsonNode car : drops) {
            boolean local = isLocalMove(car);
            if (this.isUtilityCar(car)) {
                builder.append(setoutUtilityCars(drops, car, location, isManifest));
            } else if (isManifest &&
                    Setup.isPrintTruncateManifestEnabled() &&
                    location.getLocation().isSwitchListEnabled() &&
                    !train.isLocalSwitcher()) {
                builder.append(dropCar(car, Setup.getDropTruncatedManifestMessageFormat(), local));
            } else {
                String[] format;
                if (isManifest) {
                    format = (!local) ? Setup.getDropManifestMessageFormat() : Setup
                            .getLocalManifestMessageFormat();
                } else {
                    format = (!local) ? Setup.getDropSwitchListMessageFormat() : Setup
                            .getLocalSwitchListMessageFormat();
                }
                Setup.stringToTagConversion(format);
                builder.append(dropCar(car, format, local));
            }
        }
        return String.format(locale, strings.getProperty("CarsList"), builder.toString());
    }

    protected String pickupUtilityCars(ArrayList<JsonNode> jnCars, JsonNode jnCar, RouteLocation location,
            boolean isManifest) {
        List<Car> cars = getCarList(jnCars);
        Car car = getCar(jnCar);
        return pickupUtilityCars(cars, car, isManifest);
    }

    protected String setoutUtilityCars(ArrayList<JsonNode> jnCars, JsonNode jnCar, RouteLocation location,
            boolean isManifest) {
        List<Car> cars = getCarList(jnCars);
        Car car = getCar(jnCar);
        return setoutUtilityCars(cars, car, isManifest);
    }

    protected List<Car> getCarList(ArrayList<JsonNode> jnCars) {
        List<Car> cars = new ArrayList<>();
        for (JsonNode kar : jnCars) { 
            cars.add(getCar(kar));
        }
        return cars;
    }
    
    protected Car getCar(JsonNode jnCar) {
        String id = jnCar.path(JSON.NAME).asText();
        Car car = InstanceManager.getDefault(CarManager.class).getById(id);
        return car;
    }

    protected String pickUpCar(JsonNode car, String[] format) {
        if (isLocalMove(car)) {
            return ""; // print nothing for local move, see dropCar()
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<span style=\"color: " + Setup.getPickupTextColor() + ";\">");
        builder.append(Setup.getPickupCarPrefix()).append(" ");
        for (String attribute : format) {
            if (!attribute.trim().isEmpty()) {
                attribute = attribute.toLowerCase();
                log.trace("Adding car with attribute {}", attribute);
                if (attribute.equals(JsonOperations.LOCATION) || attribute.equals(JsonOperations.TRACK)) {
                    attribute = JsonOperations.LOCATION; // treat "track" as "location"
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getPickupLocation(car.path(attribute),
                                            ShowLocation.track))).append(" "); // NOI18N
                } else if (attribute.equals(JsonOperations.DESTINATION)) {
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getDropLocation(car.path(attribute),
                                            ShowLocation.location))).append(" "); // NOI18N
                } else if (attribute.equals(JsonOperations.DESTINATION_TRACK)) {
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getDropLocation(car.path(JsonOperations.DESTINATION),
                                            ShowLocation.both))).append(" "); // NOI18N
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
        if (!isLocal) {
            builder.append("<span style=\"color: " + Setup.getDropTextColor() + ";\">");
            builder.append(Setup.getDropCarPrefix()).append(" ");
        } else {
            builder.append("<span style=\"color: " + Setup.getLocalTextColor() + ";\">");
            builder.append(Setup.getLocalPrefix()).append(" ");
        }
        log.debug("dropCar {}", car);
        for (String attribute : format) {
            if (!attribute.trim().isEmpty()) {
                attribute = attribute.toLowerCase();
                log.trace("Removing car with attribute {}", attribute);
                if (attribute.equals(JsonOperations.DESTINATION) || attribute.equals(JsonOperations.TRACK)) {
                    attribute = JsonOperations.DESTINATION; // treat "track" as "destination"
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getDropLocation(car.path(attribute),
                                            ShowLocation.track))).append(" "); // NOI18N
                } else if (attribute.equals(JsonOperations.DESTINATION_TRACK)) {
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getDropLocation(car.path(JsonOperations.DESTINATION),
                                            ShowLocation.both))).append(" "); // NOI18N
                } else if (attribute.equals(JsonOperations.LOCATION) && isLocal) {
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getPickupLocation(car.path(attribute),
                                            ShowLocation.track))).append(" "); // NOI18N
                } else if (attribute.equals(JsonOperations.LOCATION)) {
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getPickupLocation(car.path(attribute),
                                            ShowLocation.location))).append(" "); // NOI18N
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
        engines.forEach((engine) -> {
            builder.append(this.dropEngine(engine));
        });
        return String.format(locale, strings.getProperty("EnginesList"), builder.toString());
    }

    protected String dropEngine(JsonNode engine) {
        StringBuilder builder = new StringBuilder();
        builder.append("<span style=\"color: " + Setup.getDropEngineTextColor() + ";\">");
        builder.append(Setup.getDropEnginePrefix()).append(" ");
        String[] formatMessage = Setup.getDropEngineMessageFormat();
        Setup.stringToTagConversion(formatMessage);
        for (String attribute : formatMessage) {
            if (!attribute.trim().isEmpty()) {
                attribute = attribute.toLowerCase();
                if (attribute.equals(JsonOperations.DESTINATION) || attribute.equals(JsonOperations.TRACK)) {
                    attribute = JsonOperations.DESTINATION; // treat "track" as "destination"
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getDropLocation(engine.path(attribute),
                                    ShowLocation.track)))
                            .append(" "); // NOI18N
                } else if (attribute.equals(JsonOperations.LOCATION)) {
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getPickupLocation(engine.path(attribute),
                                    ShowLocation.location)))
                            .append(" "); // NOI18N
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
        builder.append("<span style=\"color: " + Setup.getPickupEngineTextColor() + ";\">");
        builder.append(Setup.getPickupEnginePrefix()).append(" ");
        String[] messageFormat =  Setup.getPickupEngineMessageFormat();
        Setup.stringToTagConversion(messageFormat);
        log.debug("PickupEngineMessageFormat: {}", (Object) messageFormat);
        for (String attribute : messageFormat) {
            if (!attribute.trim().isEmpty()) {
                attribute = attribute.toLowerCase();
                if (attribute.equals(JsonOperations.LOCATION) || attribute.equals(JsonOperations.TRACK)) {
                    attribute = JsonOperations.LOCATION; // treat "track" as "location"
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getPickupLocation(engine.path(attribute),
                                    ShowLocation.track)))
                            .append(" "); // NOI18N
                } else if (attribute.equals(JsonOperations.DESTINATION)) {
                    builder.append(
                            this.getFormattedAttribute(attribute, this.getDropLocation(engine.path(attribute),
                                    ShowLocation.location)))
                            .append(" "); // NOI18N
                } else {
                    builder.append(this.getTextAttribute(attribute, engine)).append(" "); // NOI18N
                }
            }
        }
        log.debug("Picking up engine: {}", builder);
        return String.format(locale, strings.getProperty(this.resourcePrefix + "PickUpEngine"), builder.toString());
    }

    protected String getDropLocation(JsonNode location, ShowLocation show) {
        return this.getFormattedLocation(location, show, "To"); // NOI18N
    }

    protected String getPickupLocation(JsonNode location, ShowLocation show) {
        return this.getFormattedLocation(location, show, "From"); // NOI18N
    }

    protected String getTextAttribute(String attribute, JsonNode rollingStock) {
        if (attribute.equals(JsonOperations.HAZARDOUS)) {
            return this.getFormattedAttribute(attribute, (rollingStock.path(attribute).asBoolean() ? Setup
                    .getHazardousMsg() : "")); // NOI18N
        } else if (attribute.equals(JsonOperations.PICKUP_COMMENT)) {
            return this.getFormattedAttribute(JsonOperations.PICKUP_COMMENT, rollingStock.path(JsonOperations.PICKUP_COMMENT).textValue());
        } else if (attribute.equals(JsonOperations.SETOUT_COMMENT)) {
            return this.getFormattedAttribute(JsonOperations.SETOUT_COMMENT, rollingStock.path(JsonOperations.SETOUT_COMMENT).textValue());
        } else if (attribute.equals(JsonOperations.RETURN_WHEN_EMPTY)) {
            return this.getFormattedLocation(rollingStock.path(JsonOperations.RETURN_WHEN_EMPTY), ShowLocation.both, "RWE"); // NOI18N
        } else if (attribute.equals(JsonOperations.FINAL_DESTINATION)) {
            return this.getFormattedLocation(rollingStock.path(JsonOperations.FINAL_DESTINATION), ShowLocation.location, "FinalDestination"); // NOI18N
        } else if (attribute.equals(JsonOperations.FINAL_DEST_TRACK)) {
            return this.getFormattedLocation(rollingStock.path(JsonOperations.FINAL_DESTINATION), ShowLocation.both, "FinalDestination"); // NOI18N
        }
        return this.getFormattedAttribute(attribute, rollingStock.path(attribute).asText());
    }

    protected String getFormattedAttribute(String attribute, String value) {
        return String.format(locale, strings.getProperty("Attribute"), StringEscapeUtils.escapeHtml4(value), attribute);
    }

    protected String getFormattedLocation(JsonNode location, ShowLocation show, String prefix) {
        if (location.isNull() || location.isEmpty()) {
            // return an empty string if location is an empty or null
            return "";
        }
        // TODO handle tracks without names
        switch (show) {
            case location:
                return String.format(locale, strings.getProperty(prefix + "Location"),
                        splitString(location.path(JSON.USERNAME).asText()));
            case track:
                return String.format(locale, strings.getProperty(prefix + "Track"),
                        splitString(location.path(JSON.TRACK).path(JSON.USERNAME).asText()));
            case both:
            default: // default here ensures the method always returns
                return String.format(locale, strings.getProperty(prefix + "LocationAndTrack"),
                        splitString(location.path(JSON.USERNAME).asText()),
                        splitString(location.path(JSON.TRACK).path(JSON.USERNAME).asText()));
        }
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
                        if (track.getKey().equals(car.path(JSON.LOCATION).path(JSON.TRACK)
                                .path(JSON.NAME).asText())) {
                            pickup = true;
                            break; // we do not need to iterate all cars
                        }
                    }
                }
                if (cars.path(JSON.REMOVE).size() > 0) {
                    for (JsonNode car : cars.path(JSON.REMOVE)) {
                        if (track.getKey().equals(car.path(JsonOperations.DESTINATION).path(JSON.TRACK)
                                .path(JSON.NAME).textValue())) {
                            setout = true;
                            break; // we do not need to iterate all cars
                        }
                    }
                }
                if (pickup && setout) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getValue().path(
                            JSON.ADD_AND_REMOVE).textValue()));
                } else if (pickup) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getValue().path(
                            JSON.ADD).textValue()));
                } else if (setout) {
                    builder.append(String.format(locale, strings.getProperty("TrackComments"), track.getValue().path(
                            JSON.REMOVE).textValue()));
                }
            }
        }
        return builder.toString();
    }

    protected boolean isLocalMove(JsonNode car) {
        return car.path(JsonOperations.IS_LOCAL).booleanValue();        
    }

    protected boolean isUtilityCar(JsonNode car) {
        return car.path(JsonOperations.UTILITY).booleanValue();
    }

    protected JsonNode getJsonManifest() throws IOException {
        if (this.jsonManifest == null) {
            try {
                this.jsonManifest = this.mapper.readTree((new JsonManifest(this.train)).getFile());
            } catch (IOException e) {
                log.error("Json manifest file not found for train ({})", this.train.getName());
            }
        }
        return this.jsonManifest;
    }

    @Override
    public String getValidity() {
        try {
            if (Setup.isPrintTrainScheduleNameEnabled()) {
                return String.format(locale, strings.getProperty(this.resourcePrefix + "ValidityWithSchedule"),
                        getDate((new StdDateFormat()).parse(this.getJsonManifest().path(JsonOperations.DATE).textValue())),
                        InstanceManager.getDefault(TrainScheduleManager.class).getActiveSchedule().getName());
            } else {
                return String.format(locale, strings.getProperty(this.resourcePrefix + "Validity"),
                        getDate((new StdDateFormat()).parse(this.getJsonManifest().path(JsonOperations.DATE).textValue())));
            }
        } catch (NullPointerException ex) {
            log.warn("Manifest for train {} (id {}) does not have any validity.", this.train.getIconName(), this.train
                    .getId());
        } catch (ParseException ex) {
            log.error("Date of JSON manifest could not be parsed as a Date.");
        } catch (IOException ex) {
            log.error("JSON manifest could not be read.");
        }
        return "";
    }
}
