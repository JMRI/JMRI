package jmri.server.json.operations;

import static jmri.server.json.JSON.COLOR;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.DIRECTION;
import static jmri.server.json.JSON.EXPECTED_ARRIVAL;
import static jmri.server.json.JSON.EXPECTED_DEPARTURE;
import static jmri.server.json.JSON.ID;
import static jmri.server.json.JSON.LENGTH;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.NUMBER;
import static jmri.server.json.JSON.OWNER;
import static jmri.server.json.JSON.ROAD;
import static jmri.server.json.JSON.ROUTE;
import static jmri.server.json.JSON.SEQUENCE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.operations.JsonOperations.CAR;
import static jmri.server.json.operations.JsonOperations.DESTINATION;
import static jmri.server.json.operations.JsonOperations.ENGINE;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.operations.JsonOperations.TRACK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.consist.JsonConsist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonUtil {

    private final ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(JsonUtil.class);

    public JsonUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ObjectNode getCar(String id, Locale locale) {
        return this.getCar(InstanceManager.getDefault(CarManager.class).getById(id), locale);
    }

    /**
     * Get the JSON representation of an Engine.
     *
     * @param engine the Engine
     * @return the JSON representation of engine
     * @deprecated since 4.15.6; use {@link #getEngine(Engine, Locale)} instead
     */
    @Deprecated
    public ObjectNode getEngine(Engine engine) {
        return getEngine(engine, Locale.getDefault());
    }

    /**
     * Get the JSON representation of an Engine.
     *
     * @param engine the Engine
     * @param locale the client's locale
     * @return the JSON representation of engine
     */
    public ObjectNode getEngine(Engine engine, Locale locale) {
        ObjectNode data = this.getRollingStock(engine, locale);
        data.put(JSON.MODEL, engine.getModel());
        data.put(JsonConsist.CONSIST, engine.getConsistName());
        return getMessage(ENGINE, data);
    }

    public ObjectNode getEngine(String id, Locale locale) {
        return this.getEngine(InstanceManager.getDefault(EngineManager.class).getById(id), locale);
    }

    /**
     * Get a JSON representation of a Car.
     *
     * @param car the Car
     * @param locale the client's locale
     * @return the JSON representation of car
     */
    public ObjectNode getCar(Car car, Locale locale) {
        ObjectNode data = this.getRollingStock(car, locale);
        data.put(JSON.LOAD, car.getLoadName()); // NOI18N
        data.put(JSON.HAZARDOUS, car.isHazardous());
        data.put(JSON.REMOVE_COMMENT, car.getDropComment());
        data.put(JSON.ADD_COMMENT, car.getPickupComment());
        data.put(JSON.KERNEL, car.getKernel() != null ? car.getKernelName() : null);
        data.put(JSON.UTILITY, car.isUtility());
        if (car.getFinalDestinationTrack() != null) {
            data.set(JSON.FINAL_DESTINATION, this.getLocationAndTrack(car.getFinalDestinationTrack(), null, locale));
        } else if (car.getFinalDestination() != null) {
            data.set(JSON.FINAL_DESTINATION, this.getLocation(car.getFinalDestination(), (RouteLocation) null, locale));
        } else {
            data.set(JSON.FINAL_DESTINATION, null);
        }
        if (car.getReturnWhenEmptyDestTrack() != null) {
            data.set(JSON.RETURN_WHEN_EMPTY, this.getLocationAndTrack(car.getReturnWhenEmptyDestTrack(), null, locale));
        } else if (car.getReturnWhenEmptyDestination() != null) {
            data.set(JSON.RETURN_WHEN_EMPTY, this.getLocation(car.getReturnWhenEmptyDestination(), (RouteLocation) null, locale));
        } else {
            data.set(JSON.RETURN_WHEN_EMPTY, null);
        }
        data.put(JSON.STATUS, car.getStatus());
        return getMessage(CAR, data);
    }

    public ObjectNode getLocation(@Nonnull Location location, Locale locale) {
        ObjectNode data = mapper.createObjectNode();
        data.put(NAME, location.getName());
        data.put(ID, location.getId());
        data.put(LENGTH, location.getLength());
        data.put(COMMENT, location.getComment());
        ArrayNode types = data.putArray(TYPE);
        for (String type : location.getTypeNames()) {
            types.add(type);
        }
        return getMessage(LOCATION, data);
    }

    public ObjectNode getLocation(String id, Locale locale) throws JsonException {
        try {
            return getLocation(InstanceManager.getDefault(LocationManager.class).getLocationById(id), locale);
        } catch (NullPointerException e) {
            log.error("Unable to get location id [{}].", id);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LOCATION, id));
        }
    }

    private ObjectNode getLocation(Location location, RouteLocation routeLocation, Locale locale) {
        ObjectNode node = mapper.createObjectNode();
        node.put(NAME, location.getName());
        node.put(ID, location.getId());
        if (routeLocation != null) {
            node.put(ROUTE, routeLocation.getId());
        } else {
            node.put(ROUTE, (String) null);
        }
        return node;
    }

    private ObjectNode getLocationAndTrack(Track track, RouteLocation routeLocation, Locale locale) {
        ObjectNode node = this.getLocation(track.getLocation(), routeLocation, locale);
        node.set(TRACK, this.getTrack(track, locale));
        return node;
    }

    private ObjectNode getTrack(Track track, Locale locale) {
        ObjectNode node = mapper.createObjectNode();
        node.put(NAME, track.getName());
        node.put(ID, track.getId());
        return node;
    }

    private ObjectNode getRollingStock(RollingStock rs, Locale locale) {
        ObjectNode node = mapper.createObjectNode();
        node.put(ID, rs.getId());
        node.put(NUMBER, TrainCommon.splitString(rs.getNumber()));
        node.put(ROAD, rs.getRoadName());
        String[] type = rs.getTypeName().split("-"); // second half of string
        // can be anything
        node.put(TYPE, type[0]);
        node.put(LENGTH, rs.getLength());
        node.put(COLOR, rs.getColor());
        node.put(OWNER, rs.getOwner());
        node.put(COMMENT, rs.getComment());
        if (rs.getTrack() != null) {
            node.set(LOCATION, this.getLocationAndTrack(rs.getTrack(), rs.getRouteLocation(), locale));
        } else if (rs.getLocation() != null) {
            node.set(LOCATION, this.getLocation(rs.getLocation(), rs.getRouteLocation(), locale));
        } else {
            node.set(LOCATION, null);
        }
        if (rs.getDestinationTrack() != null) {
            node.set(DESTINATION, this.getLocationAndTrack(rs.getDestinationTrack(), rs.getRouteDestination(), locale));
        } else if (rs.getDestination() != null) {
            node.set(DESTINATION, this.getLocation(rs.getDestination(), rs.getRouteDestination(), locale));
        } else {
            node.set(DESTINATION, null);
        }
        return node;
    }

    public ObjectNode getTrain(String id, Locale locale) throws JsonException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, JsonOperations.TRAIN);
        ObjectNode data = root.putObject(JSON.DATA);
        try {
            Train train = InstanceManager.getDefault(TrainManager.class).getTrainById(id);
            data.put(NAME, train.getName());
            data.put(JSON.ICON_NAME, train.getIconName());
            data.put(ID, train.getId());
            data.put(JSON.DEPARTURE_TIME, train.getFormatedDepartureTime());
            data.put(JSON.DESCRIPTION, train.getDescription());
            data.put(COMMENT, train.getComment());
            if (train.getRoute() != null) {
                data.put(ROUTE, train.getRoute().getName());
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
            data.put(LOCATION, train.getCurrentLocationName());
            if (train.getCurrentLocation() != null) {
                data.put(JsonOperations.LOCATION_ID, train.getCurrentLocation().getId());
            }
            data.put(JSON.STATUS, train.getStatus(locale));
            data.put(JSON.STATUS_CODE, train.getStatusCode());
            data.put(LENGTH, train.getTrainLength());
            data.put(JsonOperations.WEIGHT, train.getTrainWeight());
            if (train.getLeadEngine() != null) {
                data.put(JsonOperations.LEAD_ENGINE, train.getLeadEngine().toString());
            }
            data.put(JsonOperations.CABOOSE, train.getCabooseRoadAndNumber());
        } catch (NullPointerException ex) {
            log.error("Unable to get train id [{}].", id, ex);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", JsonOperations.TRAIN, id));
        }
        return root;
    }

    public ArrayNode getTrains(Locale locale) throws JsonException {
        ArrayNode array = this.mapper.createArrayNode();
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            array.add(getTrain(train.getId(), locale));
        }
        return array;
    }

    private ArrayNode getCarsForTrain(Train train, Locale locale) {
        ArrayNode array = mapper.createArrayNode();
        List<Car> cars = InstanceManager.getDefault(CarManager.class).getByTrainDestinationList(train);
        cars.forEach((car) -> {
            array.add(getCar(car, locale));
        });
        return array;
    }

    private ArrayNode getEnginesForTrain(Train train, Locale locale) {
        ArrayNode array = mapper.createArrayNode();
        List<Engine> engines = InstanceManager.getDefault(EngineManager.class).getByTrainBlockingList(train);
        engines.forEach((engine) -> {
            array.add(getEngine(engine, locale));
        });
        return array;
    }

    private ArrayNode getRouteLocationsForTrain(Train train, Locale locale) throws JsonException {
        ArrayNode array = mapper.createArrayNode();
        List<RouteLocation> routes = train.getRoute().getLocationsBySequenceList();
        for (RouteLocation route : routes) {
            ObjectNode root = mapper.createObjectNode();
            RouteLocation rl = route;
            root.put(ID, rl.getId());
            root.put(NAME, rl.getName());
            root.put(DIRECTION, rl.getTrainDirectionString());
            root.put(COMMENT, rl.getComment());
            root.put(SEQUENCE, rl.getSequenceNumber());
            root.put(EXPECTED_ARRIVAL, train.getExpectedArrivalTime(rl));
            root.put(EXPECTED_DEPARTURE, train.getExpectedDepartureTime(rl));
            root.set(LOCATION, getLocation(rl.getLocation().getId(), locale));
            array.add(root); //add this routeLocation to the routeLocation array
        }
        return array;  //return array of routeLocations
    }

    private ObjectNode getMessage(String type, ObjectNode data) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, type);
        root.set(DATA, data);
        return root;
    }
}
