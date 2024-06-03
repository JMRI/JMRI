package jmri.server.json.operations;

import static jmri.server.json.reporter.JsonReporter.REPORTER;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.*;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.consist.JsonConsist;

/**
 * Utilities used by JSON services for Operations
 * 
 * @author Randall Wood Copyright 2019
 */
public class JsonUtil {

    private final ObjectMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * Create utilities.
     * 
     * @param mapper the mapper used to create JSON nodes
     */
    public JsonUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Get the JSON representation of a Car.
     * 
     * @param name   the ID of the Car
     * @param locale the client's locale
     * @param id     the message id set by the client
     * @return the JSON representation of the Car
     * @throws JsonException if no car by name exists
     */
    public ObjectNode getCar(String name, Locale locale, int id) throws JsonException {
        Car car = carManager().getById(name);
        if (car == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, JsonOperations.CAR, name), id);
        }
        return this.getCar(car, locale);
    }

    /**
     * Get the JSON representation of an Engine.
     *
     * @param engine the Engine
     * @param locale the client's locale
     * @return the JSON representation of engine
     */
    public ObjectNode getEngine(Engine engine, Locale locale) {
        return getEngine(engine, getRollingStock(engine, locale), locale);
    }

    /**
     * Get the JSON representation of an Engine.
     *
     * @param engine the Engine
     * @param data   the JSON data from
     *               {@link #getRollingStock(RollingStock, Locale)}
     * @param locale the client's locale
     * @return the JSON representation of engine
     */
    public ObjectNode getEngine(Engine engine, ObjectNode data, Locale locale) {
        data.put(JSON.MODEL, engine.getModel());
        data.put(JsonConsist.CONSIST, engine.getConsistName());
        return data;
    }

    /**
     * Get the JSON representation of an Engine.
     *
     * @param name   the ID of the Engine
     * @param locale the client's locale
     * @param id     the message id set by the client
     * @return the JSON representation of engine
     * @throws JsonException if no engine exists by name
     */
    public ObjectNode getEngine(String name, Locale locale, int id) throws JsonException {
        Engine engine = engineManager().getById(name);
        if (engine == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, JsonOperations.ENGINE, name), id);
        }
        return this.getEngine(engine, locale);
    }

    /**
     * Get a JSON representation of a Car.
     *
     * @param car    the Car
     * @param locale the client's locale
     * @return the JSON representation of car
     */
    public ObjectNode getCar(@Nonnull Car car, Locale locale) {
        return getCar(car, getRollingStock(car, locale), locale);
    }

    /**
     * Get a JSON representation of a Car.
     *
     * @param car    the Car
     * @param data   the JSON data from
     *               {@link #getRollingStock(RollingStock, Locale)}
     * @param locale the client's locale
     * @return the JSON representation of car
     */
    public ObjectNode getCar(@Nonnull Car car, @Nonnull ObjectNode data, Locale locale) {
        data.put(JSON.LOAD, car.getLoadName()); // NOI18N
        data.put(JSON.HAZARDOUS, car.isHazardous());
        data.put(JsonOperations.CABOOSE, car.isCaboose());
        data.put(JsonOperations.PASSENGER, car.isPassenger());
        data.put(JsonOperations.FRED, car.hasFred());
        data.put(JSON.REMOVE_COMMENT, car.getDropComment());
        data.put(JSON.ADD_COMMENT, car.getPickupComment());
        data.put(JSON.KERNEL, car.getKernelName());
        data.put(JSON.UTILITY, car.isUtility());
        data.put(JSON.IS_LOCAL, car.isLocalMove());
        if (car.getFinalDestinationTrack() != null) {
            data.set(JSON.FINAL_DESTINATION, this.getRSLocationAndTrack(car.getFinalDestinationTrack(), null, locale));
        } else if (car.getFinalDestination() != null) {
            data.set(JSON.FINAL_DESTINATION,
                    this.getRSLocation(car.getFinalDestination(), (RouteLocation) null, locale));
        } else {
            data.set(JSON.FINAL_DESTINATION, null);
        }
        if (car.getReturnWhenEmptyDestTrack() != null) {
            data.set(JSON.RETURN_WHEN_EMPTY,
                    this.getRSLocationAndTrack(car.getReturnWhenEmptyDestTrack(), null, locale));
        } else if (car.getReturnWhenEmptyDestination() != null) {
            data.set(JSON.RETURN_WHEN_EMPTY,
                    this.getRSLocation(car.getReturnWhenEmptyDestination(), (RouteLocation) null, locale));
        } else {
            data.set(JSON.RETURN_WHEN_EMPTY, null);
        }
        if (car.getReturnWhenLoadedDestTrack() != null) {
            data.set(JSON.RETURN_WHEN_LOADED,
                    this.getRSLocationAndTrack(car.getReturnWhenLoadedDestTrack(), null, locale));
        } else if (car.getReturnWhenLoadedDestination() != null) {
            data.set(JSON.RETURN_WHEN_LOADED,
                    this.getRSLocation(car.getReturnWhenLoadedDestination(), (RouteLocation) null, locale));
        } else {
            data.set(JSON.RETURN_WHEN_LOADED, null);
        }
        data.put(JSON.DIVISION, car.getDivisionName());
        data.put(JSON.STATUS, car.getStatus().replace("<", "&lt;").replace(">", "&gt;"));
        return data;
    }

    /**
     * Get the JSON representation of a Location.
     * <p>
     * <strong>Note:</strong>use {@link #getRSLocation(Location, Locale)} if
     * including in rolling stock or train.
     * 
     * @param location the location
     * @param locale   the client's locale
     * @return the JSON representation of location
     */
    public ObjectNode getLocation(@Nonnull Location location, Locale locale) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.USERNAME, location.getName());
        data.put(JSON.NAME, location.getId());
        data.put(JSON.LENGTH, location.getLength());
        data.put(JSON.COMMENT, location.getCommentWithColor());
        Reporter reporter = location.getReporter();
        data.put(REPORTER, reporter != null ? reporter.getSystemName() : "");
        // note type defaults to all in-use rolling stock types
        ArrayNode types = data.putArray(JsonOperations.CAR_TYPE);
        for (String type : location.getTypeNames()) {
            types.add(type);
        }
        ArrayNode tracks = data.putArray(JsonOperations.TRACK);
        for (Track track : location.getTracksList()) {
            tracks.add(getTrack(track, locale));
        }
        return data;
    }

    /**
     * Get the JSON representation of a Location.
     * 
     * @param name   the ID of the location
     * @param locale the client's locale
     * @param id     the message id set by the client
     * @return the JSON representation of the location
     * @throws JsonException if id does not match a known location
     */
    public ObjectNode getLocation(String name, Locale locale, int id) throws JsonException {
        if (locationManager().getLocationById(name) == null) {
            log.error("Unable to get location id [{}].", name);
            throw new JsonException(404,
                    Bundle.getMessage(locale, JsonException.ERROR_OBJECT, JsonOperations.LOCATION, name), id);
        }
        return getLocation(locationManager().getLocationById(name), locale);
    }

    /**
     * Get a Track in JSON.
     * <p>
     * <strong>Note:</strong>use {@link #getRSTrack(Track, Locale)} if including
     * in rolling stock or train.
     * 
     * @param track  the track to get
     * @param locale the client's locale
     * @return a JSON representation of the track
     */
    public ObjectNode getTrack(Track track, Locale locale) {
        ObjectNode node = mapper.createObjectNode();
        node.put(JSON.USERNAME, track.getName());
        node.put(JSON.NAME, track.getId());
        node.put(JSON.COMMENT, track.getComment());
        node.put(JSON.LENGTH, track.getLength());
        // only includes location ID to avoid recursion
        node.put(JsonOperations.LOCATION, track.getLocation().getId());
        Reporter reporter = track.getReporter();
        node.put(REPORTER, reporter != null ? reporter.getSystemName() : "");
        node.put(JSON.TYPE, track.getTrackType());
        // note type defaults to all in-use rolling stock types
        ArrayNode types = node.putArray(JsonOperations.CAR_TYPE);
        for (String type : track.getTypeNames()) {
            types.add(type);
        }
        return node;
    }

    /**
     * Get the JSON representation of a Location for use in rolling stock or
     * train.
     * <p>
     * <strong>Note:</strong>use {@link #getLocation(Location, Locale)} if not
     * including in rolling stock or train.
     * 
     * @param location the location
     * @param locale   the client's locale
     * @return the JSON representation of location
     */
    public ObjectNode getRSLocation(@Nonnull Location location, Locale locale) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.USERNAME, location.getName());
        data.put(JSON.NAME, location.getId());
        return data;
    }

    private ObjectNode getRSLocation(Location location, RouteLocation routeLocation, Locale locale) {
        ObjectNode node = getRSLocation(location, locale);
        if (routeLocation != null) {
            node.put(JSON.ROUTE, routeLocation.getId());
        } else {
            node.put(JSON.ROUTE, (String) null);
        }
        return node;
    }

    private ObjectNode getRSLocationAndTrack(Track track, RouteLocation routeLocation, Locale locale) {
        ObjectNode node = this.getRSLocation(track.getLocation(), routeLocation, locale);
        node.set(JsonOperations.TRACK, this.getRSTrack(track, locale));
        return node;
    }

    /**
     * Get a Track in JSON for use in rolling stock or train.
     * <p>
     * <strong>Note:</strong>use {@link #getTrack(Track, Locale)} if not
     * including in rolling stock or train.
     * 
     * @param track  the track to get
     * @param locale the client's locale
     * @return a JSON representation of the track
     */
    public ObjectNode getRSTrack(Track track, Locale locale) {
        ObjectNode node = mapper.createObjectNode();
        node.put(JSON.USERNAME, track.getName());
        node.put(JSON.NAME, track.getId());
        return node;
    }

    public ObjectNode getRollingStock(@Nonnull RollingStock rs, Locale locale) {
        ObjectNode node = mapper.createObjectNode();
        node.put(JSON.NAME, rs.getId());
        node.put(JSON.NUMBER, TrainCommon.splitString(rs.getNumber()));
        node.put(JSON.ROAD, rs.getRoadName().split(TrainCommon.HYPHEN)[0]);
        // second half of string can be anything
        String[] type = rs.getTypeName().split(TrainCommon.HYPHEN, 2);
        node.put(JSON.RFID, rs.getRfid());
        if (!rs.getWhereLastSeenName().equals(Car.NONE)) {
            node.put(JSON.WHERELASTSEEN, rs.getWhereLastSeenName() +
                    (rs.getTrackLastSeenName().equals(Car.NONE) ? "" : " (" + rs.getTrackLastSeenName() + ")"));
        } else {
            node.set(JSON.WHERELASTSEEN, null);        
        }
        if (!rs.getWhenLastSeenDate().equals(Car.NONE)) {
            node.put(JSON.WHENLASTSEEN, rs.getWhenLastSeenDate());
        } else {
            node.set(JSON.WHENLASTSEEN, null);            
        }
        node.put(JsonOperations.CAR_TYPE, type[0]);
        node.put(JsonOperations.CAR_SUB_TYPE, type.length == 2 ? type[1] : "");
        node.put(JSON.LENGTH, rs.getLengthInteger());
        try {
            node.put(JsonOperations.WEIGHT, Double.parseDouble(rs.getWeight()));
        } catch (NumberFormatException ex) {
            node.put(JsonOperations.WEIGHT, 0.0);
        }
        try {
            node.put(JsonOperations.WEIGHT_TONS, Double.parseDouble(rs.getWeightTons()));
        } catch (NumberFormatException ex) {
            node.put(JsonOperations.WEIGHT_TONS, 0.0);
        }
        node.put(JSON.COLOR, rs.getColor());
        node.put(JSON.OWNER, rs.getOwnerName());
        node.put(JsonOperations.BUILT, rs.getBuilt());
        node.put(JSON.COMMENT, rs.getComment());
        node.put(JsonOperations.OUT_OF_SERVICE, rs.isOutOfService());
        node.put(JsonOperations.LOCATION_UNKNOWN, rs.isLocationUnknown());
        if (rs.getTrack() != null) {
            node.set(JsonOperations.LOCATION, this.getRSLocationAndTrack(rs.getTrack(), rs.getRouteLocation(), locale));
        } else if (rs.getLocation() != null) {
            node.set(JsonOperations.LOCATION, this.getRSLocation(rs.getLocation(), rs.getRouteLocation(), locale));
        } else {
            node.set(JsonOperations.LOCATION, null);
        }
        if (rs.getTrain() != null) {
            node.put(JsonOperations.TRAIN_ID, rs.getTrain().getId());
            node.put(JsonOperations.TRAIN_NAME, rs.getTrain().getName());
            node.put(JsonOperations.TRAIN_ICON_NAME, rs.getTrain().getIconName());
        } else {
            node.set(JsonOperations.TRAIN_ID, null);
            node.set(JsonOperations.TRAIN_NAME, null);
            node.set(JsonOperations.TRAIN_ICON_NAME, null);
        }  
        if (rs.getDestinationTrack() != null) {
            node.set(JsonOperations.DESTINATION,
                    this.getRSLocationAndTrack(rs.getDestinationTrack(), rs.getRouteDestination(), locale));
        } else if (rs.getDestination() != null) {
            node.set(JsonOperations.DESTINATION, this.getRSLocation(rs.getDestination(), rs.getRouteDestination(), locale));
        } else {
            node.set(JsonOperations.DESTINATION, null);
        }
        return node;
    }

    /**
     * Get the JSON representation of a Train.
     * 
     * @param train  the train
     * @param locale the client's locale
     * @return the JSON representation of train
     */
    public ObjectNode getTrain(Train train, Locale locale) {
        ObjectNode data = this.mapper.createObjectNode();
        data.put(JSON.USERNAME, train.getName());
        data.put(JSON.ICON_NAME, train.getIconName());
        data.put(JSON.NAME, train.getId());
        data.put(JSON.DEPARTURE_TIME, train.getFormatedDepartureTime());
        data.put(JSON.DESCRIPTION, train.getDescription());
        data.put(JSON.COMMENT, train.getComment());
        if (train.getRoute() != null) {
            data.put(JSON.ROUTE, train.getRoute().getName());
            data.put(JSON.ROUTE_ID, train.getRoute().getId());
            data.set(JsonOperations.LOCATIONS, this.getRouteLocationsForTrain(train, locale));
        }
        data.set(JSON.ENGINES, this.getEnginesForTrain(train, locale));
        data.set(JsonOperations.CARS, this.getCarsForTrain(train, locale));
        if (train.getTrainDepartsName() != null) {
            data.put(JSON.DEPARTURE_LOCATION, train.getTrainDepartsName());
        }
        if (train.getTrainTerminatesName() != null) {
            data.put(JSON.TERMINATES_LOCATION, train.getTrainTerminatesName());
        }
        data.put(JsonOperations.LOCATION, train.getCurrentLocationName());
        if (train.getCurrentRouteLocation() != null) {
            data.put(JsonOperations.LOCATION_ID, train.getCurrentRouteLocation().getId());
        }
        data.put(JSON.STATUS, train.getStatus(locale));
        data.put(JSON.STATUS_CODE, train.getStatusCode());
        data.put(JSON.LENGTH, train.getTrainLength());
        data.put(JsonOperations.WEIGHT, train.getTrainWeight());
        if (train.getLeadEngine() != null) {
            data.put(JsonOperations.LEAD_ENGINE, train.getLeadEngine().toString());
        }
        data.put(JsonOperations.CABOOSE, train.getCabooseRoadAndNumber());
        return data;
    }

    /**
     * Get the JSON representation of a Train.
     * 
     * @param name   the id of the train
     * @param locale the client's locale
     * @param id     the message id set by the client
     * @return the JSON representation of the train with id
     * @throws JsonException if id does not represent a known train
     */
    public ObjectNode getTrain(String name, Locale locale, int id) throws JsonException {
        if (trainManager().getTrainById(name) == null) {
            log.error("Unable to get train id [{}].", name);
            throw new JsonException(404,
                    Bundle.getMessage(locale, JsonException.ERROR_OBJECT, JsonOperations.TRAIN, name), id);
        }
        return getTrain(trainManager().getTrainById(name), locale);
    }

    /**
     * Get all trains.
     * 
     * @param locale the client's locale
     * @return an array of all trains
     */
    public ArrayNode getTrains(Locale locale) {
        ArrayNode array = this.mapper.createArrayNode();
        trainManager().getTrainsByNameList()
                .forEach(train -> array.add(getTrain(train, locale)));
        return array;
    }

    private ArrayNode getCarsForTrain(Train train, Locale locale) {
        ArrayNode array = mapper.createArrayNode();
        carManager().getByTrainDestinationList(train)
                .forEach(car -> array.add(getCar(car, locale)));
        return array;
    }

    private ArrayNode getEnginesForTrain(Train train, Locale locale) {
        ArrayNode array = mapper.createArrayNode();
        engineManager().getByTrainBlockingList(train)
                .forEach(engine -> array.add(getEngine(engine, locale)));
        return array;
    }

    private ArrayNode getRouteLocationsForTrain(Train train, Locale locale) {
        ArrayNode array = mapper.createArrayNode();
        train.getRoute().getLocationsBySequenceList().forEach(route -> {
            ObjectNode root = mapper.createObjectNode();
            RouteLocation rl = route;
            root.put(JSON.NAME, rl.getId());
            root.put(JSON.USERNAME, rl.getName());
            root.put(JSON.TRAIN_DIRECTION, rl.getTrainDirectionString());
            root.put(JSON.COMMENT, rl.getCommentWithColor());
            root.put(JSON.SEQUENCE, rl.getSequenceNumber());
            root.put(JSON.EXPECTED_ARRIVAL, train.getExpectedArrivalTime(rl));
            root.put(JSON.EXPECTED_DEPARTURE, train.getExpectedDepartureTime(rl));
            root.set(JsonOperations.LOCATION, getRSLocation(rl.getLocation(), locale));
            array.add(root);
        });
        return array;
    }

    private CarManager carManager() {
        return InstanceManager.getDefault(CarManager.class);
    }

    private EngineManager engineManager() {
        return InstanceManager.getDefault(EngineManager.class);
    }

    private LocationManager locationManager() {
        return InstanceManager.getDefault(LocationManager.class);
    }

    private TrainManager trainManager() {
        return InstanceManager.getDefault(TrainManager.class);
    }
}
