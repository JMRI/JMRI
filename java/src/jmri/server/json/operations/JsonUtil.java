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

    public JsonNode getCar(Locale locale, String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, CAR);
        root.put(DATA, this.getCar(InstanceManager.getDefault(CarManager.class).getById(id)));
        return root;
    }

    /**
     * Get the JSON representation of an Engine.
     *
     * @param engine the Engine
     * @return the JSON representation of engine
     */
    public ObjectNode getEngine(Engine engine) {
        ObjectNode node = this.getRollingStock(engine);
        node.put(JSON.MODEL, engine.getModel());
        node.put(JsonConsist.CONSIST, engine.getConsistName());
        return node;
    }

    public JsonNode getEngine(Locale locale, String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ENGINE);
        root.put(DATA, this.getEngine(InstanceManager.getDefault(EngineManager.class).getById(id)));
        return root;
    }

    /**
     * Get a JSON representation of a Car.
     *
     * @param car the Car
     * @return the JSON representation of car
     */
    public ObjectNode getCar(Car car) {
        ObjectNode node = this.getRollingStock(car);
        node.put(JSON.LOAD, car.getLoadName()); // NOI18N
        node.put(JSON.HAZARDOUS, car.isHazardous());
        node.put(JSON.REMOVE_COMMENT, car.getDropComment());
        node.put(JSON.ADD_COMMENT, car.getPickupComment());
        node.put(JSON.KERNEL, car.getKernelName());
        node.put(JSON.UTILITY, car.isUtility());
        if (car.getFinalDestinationTrack() != null) {
            node.put(JSON.FINAL_DESTINATION, this.getLocationAndTrack(car.getFinalDestinationTrack(), null));
        } else if (car.getFinalDestination() != null) {
            node.put(JSON.FINAL_DESTINATION, this.getLocation(car.getFinalDestination(), null));
        }
        if (car.getReturnWhenEmptyDestTrack() != null) {
            node.put(JSON.RETURN_WHEN_EMPTY, this.getLocationAndTrack(car.getReturnWhenEmptyDestTrack(), null));
        } else if (car.getReturnWhenEmptyDestination() != null) {
            node.put(JSON.RETURN_WHEN_EMPTY, this.getLocation(car.getReturnWhenEmptyDestination(), null));
        }
        return node;
    }

    public JsonNode getLocation(Locale locale, String id) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LOCATION);
        ObjectNode data = root.putObject(DATA);
        try {
            Location location = InstanceManager.getDefault(LocationManager.class).getLocationById(id);
            data.put(NAME, location.getName());
            data.put(ID, location.getId());
            data.put(LENGTH, location.getLength());
            data.put(COMMENT, location.getComment());
        } catch (NullPointerException e) {
            log.error("Unable to get location id [{}].", id);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LOCATION, id));
        }
        return root;
    }

    private ObjectNode getLocation(Location location, RouteLocation routeLocation) {
        ObjectNode node = mapper.createObjectNode();
        node.put(NAME, location.getName());
        node.put(ID, location.getId());
        if (routeLocation != null) {
            node.put(ROUTE, routeLocation.getId());
        }
        return node;
    }

    private ObjectNode getLocationAndTrack(Track track, RouteLocation routeLocation) {
        ObjectNode node = this.getLocation(track.getLocation(), routeLocation);
        node.put(TRACK, this.getTrack(track));
        return node;
    }

    private ObjectNode getTrack(Track track) {
        ObjectNode node = mapper.createObjectNode();
        node.put(NAME, track.getName());
        node.put(ID, track.getId());
        return node;
    }

    private ObjectNode getRollingStock(RollingStock rs) {
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
            node.put(LOCATION, this.getLocationAndTrack(rs.getTrack(), rs.getRouteLocation()));
        } else if (rs.getLocation() != null) {
            node.put(LOCATION, this.getLocation(rs.getLocation(), rs.getRouteLocation()));
        }
        if (rs.getDestinationTrack() != null) {
            node.put(DESTINATION, this.getLocationAndTrack(rs.getDestinationTrack(), rs.getRouteDestination()));
        } else if (rs.getDestination() != null) {
            node.put(DESTINATION, this.getLocation(rs.getDestination(), rs.getRouteDestination()));
        }
        return node;
    }

    public JsonNode getTrain(Locale locale, String id) throws JsonException {
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
                data.put(JsonOperations.LOCATIONS, this.getRouteLocationsForTrain(locale, train));
            }
            data.put(JSON.ENGINES, this.getEnginesForTrain(locale, train));
            data.put(JsonOperations.CARS, this.getCarsForTrain(locale, train));
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
        } catch (NullPointerException e) {
            log.error("Unable to get train id [{}].", id);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", JsonOperations.TRAIN, id));
        }
        return root;
    }

    public ArrayNode getTrains(Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            root.add(getTrain(locale, train.getId()));
        }
        return root;
    }

    private ArrayNode getCarsForTrain(Locale locale, Train train) {
        ArrayNode clan = mapper.createArrayNode();
        CarManager carManager = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = carManager.getByTrainDestinationList(train);
        carList.forEach((car) -> {
            clan.add(getCar(locale, car.getId()).get(DATA)); //add each car's data to the carList array
        });
        return clan;  //return array of car data
    }

    private ArrayNode getEnginesForTrain(Locale locale, Train train) {
        ArrayNode elan = mapper.createArrayNode();
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
        List<Engine> engineList = engineManager.getByTrainBlockingList(train);
        engineList.forEach((engine) -> {
            elan.add(getEngine(locale, engine.getId()).get(DATA)); //add each engine's data to the engineList array
        });
        return elan;  //return array of engine data
    }

    private ArrayNode getRouteLocationsForTrain(Locale locale, Train train) throws JsonException {
        ArrayNode rlan = mapper.createArrayNode();
        List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
        for (RouteLocation route : routeList) {
            ObjectNode rln = mapper.createObjectNode();
            RouteLocation rl = route;
            rln.put(ID, rl.getId());
            rln.put(NAME, rl.getName());
            rln.put(DIRECTION, rl.getTrainDirectionString());
            rln.put(COMMENT, rl.getComment());
            rln.put(SEQUENCE, rl.getSequenceId());
            rln.put(EXPECTED_ARRIVAL, train.getExpectedArrivalTime(rl));
            rln.put(EXPECTED_DEPARTURE, train.getExpectedDepartureTime(rl));
            rln.put(LOCATION, getLocation(locale, rl.getLocation().getId()).get(DATA));
            rlan.add(rln); //add this routeLocation to the routeLocation array
        }
        return rlan;  //return array of routeLocations
    }

}
