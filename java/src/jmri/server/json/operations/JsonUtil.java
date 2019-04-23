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

import com.fasterxml.jackson.databind.JsonNode;
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

    public JsonNode getCar(String id, Locale locale) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, CAR);
        root.set(DATA, this.getCar(InstanceManager.getDefault(CarManager.class).getById(id), locale));
        return root;
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
        ObjectNode node = this.getRollingStock(engine, locale);
        node.put(JSON.MODEL, engine.getModel());
        node.put(JsonConsist.CONSIST, engine.getConsistName());
        return node;
    }

    public JsonNode getEngine(String id, Locale locale) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ENGINE);
        root.set(DATA, this.getEngine(InstanceManager.getDefault(EngineManager.class).getById(id), locale));
        return root;
    }

    /**
     * Get a JSON representation of a Car using the default locale.
     *
     * @param car    the Car
     * @return the JSON representation of car
     * @deprecated since 4.15.6; use {@link #getCar(Car, Locale)} instead
     */
    @Deprecated
    public ObjectNode getCar(Car car) {
        return getCar(car, Locale.getDefault());
    }

    /**
     * Get a JSON representation of a Car.
     *
     * @param car the Car
     * @param locale the client's locale
     * @return the JSON representation of car
     */
    public ObjectNode getCar(Car car, Locale locale) {
        ObjectNode node = this.getRollingStock(car, locale);
        node.put(JSON.LOAD, car.getLoadName()); // NOI18N
        node.put(JSON.HAZARDOUS, car.isHazardous());
        node.put(JSON.REMOVE_COMMENT, car.getDropComment());
        node.put(JSON.ADD_COMMENT, car.getPickupComment());
        node.put(JSON.KERNEL, car.getKernel() != null ? car.getKernelName() : null);
        node.put(JSON.UTILITY, car.isUtility());
        if (car.getFinalDestinationTrack() != null) {
            node.set(JSON.FINAL_DESTINATION, this.getLocationAndTrack(car.getFinalDestinationTrack(), null, locale));
        } else if (car.getFinalDestination() != null) {
            node.set(JSON.FINAL_DESTINATION, this.getLocation(car.getFinalDestination(), (RouteLocation) null, locale));
        } else {
            node.set(JSON.FINAL_DESTINATION, null);
        }
        if (car.getReturnWhenEmptyDestTrack() != null) {
            node.set(JSON.RETURN_WHEN_EMPTY, this.getLocationAndTrack(car.getReturnWhenEmptyDestTrack(), null, locale));
        } else if (car.getReturnWhenEmptyDestination() != null) {
            node.set(JSON.RETURN_WHEN_EMPTY, this.getLocation(car.getReturnWhenEmptyDestination(), (RouteLocation) null, locale));
        } else {
            node.set(JSON.RETURN_WHEN_EMPTY, null);
        }
        node.put(JSON.STATUS, car.getStatus());
        return node;
    }

    public JsonNode getLocation(@Nonnull Location location, Locale locale) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LOCATION);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, location.getName());
        data.put(ID, location.getId());
        data.put(LENGTH, location.getLength());
        data.put(COMMENT, location.getComment());
        ArrayNode types = data.putArray(TYPE);
        for (String type : location.getTypeNames()) {
            types.add(type);
        }
        return root;
    }

    public JsonNode getLocation(String id, Locale locale) throws JsonException {
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

    public JsonNode getTrain(String id, Locale locale) throws JsonException {
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
        ArrayNode root = this.mapper.createArrayNode();
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            root.add(getTrain(train.getId(), locale));
        }
        return root;
    }

    private ArrayNode getCarsForTrain(Train train, Locale locale) {
        ArrayNode clan = mapper.createArrayNode();
        CarManager carManager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = carManager.getByTrainDestinationList(train);
        carList.forEach((car) -> {
            clan.add(getCar(car.getId(), locale).get(DATA)); //add each car's data to the carList array
        });
        return clan;  //return array of car data
    }

    private ArrayNode getEnginesForTrain(Train train, Locale locale) {
        ArrayNode elan = mapper.createArrayNode();
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
        List<Engine> engineList = engineManager.getByTrainBlockingList(train);
        engineList.forEach((engine) -> {
            elan.add(getEngine(engine.getId(), locale).get(DATA)); //add each engine's data to the engineList array
        });
        return elan;  //return array of engine data
    }

    private ArrayNode getRouteLocationsForTrain(Train train, Locale locale) throws JsonException {
        ArrayNode rlan = mapper.createArrayNode();
        List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
        for (RouteLocation route : routeList) {
            ObjectNode rln = mapper.createObjectNode();
            RouteLocation rl = route;
            rln.put(ID, rl.getId());
            rln.put(NAME, rl.getName());
            rln.put(DIRECTION, rl.getTrainDirectionString());
            rln.put(COMMENT, rl.getComment());
            rln.put(SEQUENCE, rl.getSequenceNumber());
            rln.put(EXPECTED_ARRIVAL, train.getExpectedArrivalTime(rl));
            rln.put(EXPECTED_DEPARTURE, train.getExpectedDepartureTime(rl));
            rln.set(LOCATION, getLocation(rl.getLocation().getId(), locale).get(DATA));
            rlan.add(rln); //add this routeLocation to the routeLocation array
        }
        return rlan;  //return array of routeLocations
    }

}
