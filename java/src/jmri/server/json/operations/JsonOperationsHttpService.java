package jmri.server.json.operations;

import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.JSON.NULL;
import static jmri.server.json.operations.JsonOperations.CAR;
import static jmri.server.json.operations.JsonOperations.CARS;
import static jmri.server.json.operations.JsonOperations.ENGINE;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.operations.JsonOperations.LOCATIONS;
import static jmri.server.json.operations.JsonOperations.TRACK;
import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonOperationsHttpService extends JsonHttpService {

    // private final static Logger log = LoggerFactory.getLogger(JsonOperationsHttpService.class);
    private final JsonUtil utilities;

    public JsonOperationsHttpService(ObjectMapper mapper) {
        super(mapper);
        this.utilities = new JsonUtil(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        switch (type) {
            case CAR:
                return this.utilities.getCar(locale, name);
            case ENGINE:
                return this.utilities.getEngine(locale, name);
            case LOCATION:
                return this.utilities.getLocation(locale, name);
            case TRAIN:
            case TRAINS:
                return this.utilities.getTrain(locale, name);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorInternal", type)); // NOI18N
        }
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        switch (type) {
            case TRAIN:
                this.setTrain(locale, name, data);
                return this.utilities.getTrain(locale, name);
            case CAR:
                this.setCar(locale, name, data);
                return this.utilities.getCar(locale, name);
            case ENGINE:
            case LOCATION:
            case TRAINS:
                return this.doGet(type, name, locale);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorInternal", type)); // NOI18N
        }
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        switch (type) {
            case CARS:
                return this.getCars(locale);
            case ENGINES:
                return this.getEngines(locale);
            case LOCATIONS:
                return this.getLocations(locale);
            case TRAINS:
                return this.utilities.getTrains(locale);
            case TRAIN:
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type)); // NOI18N
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorInternal", type)); // NOI18N
        }
    }

    public ArrayNode getCars(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        InstanceManager.getDefault(CarManager.class).getByIdList().forEach((rs) -> {
            root.add(this.utilities.getCar(locale, rs.getId()));
        });
        return root;
    }

    public ArrayNode getEngines(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        InstanceManager.getDefault(EngineManager.class).getByIdList().forEach((rs) -> {
            root.add(this.utilities.getEngine(locale, rs.getId()));
        });
        return root;
    }

    public ArrayNode getLocations(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (Location location : InstanceManager.getDefault(LocationManager.class).getLocationsByIdList()) {
            root.add(this.utilities.getLocation(locale, location.getId()));
        }
        return root;
    }

    /**
     * Set the properties in the data parameter for the train with the given id.
     * <p>
     * Currently only moves the train to the location given with the key
     * {@value jmri.server.json.operations.JsonOperations#LOCATION}. If the move
     * cannot be completed, throws error code 428.
     *
     * @param locale The locale to throw exceptions in.
     * @param id     The id of the train.
     * @param data   Train data to change.
     * @throws jmri.server.json.JsonException if the train cannot move to the
     *                                        location in data.
     */
    public void setTrain(Locale locale, String id, JsonNode data) throws JsonException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById(id);
        if (!data.path(LOCATION).isMissingNode()) {
            String location = data.path(LOCATION).asText();
            if (location.equals(NULL)) {
                train.terminate();
            } else if (!train.move(location)) {
                throw new JsonException(428, Bundle.getMessage(locale, "ErrorTrainMovement", id, location));
            }
        }
    }

    /**
     * Set the properties in the data parameter for the car with the given id.
     * <p>
     * Currently only sets the location of the car.
     *
     * @param locale locale to throw exceptions in
     * @param id     id of the car
     * @param data   car data to change
     * @throws jmri.server.json.JsonException if the car cannot be set to the
     *                                        location in data
     */
    public void setCar(Locale locale, String id, JsonNode data) throws JsonException {
        Car car = InstanceManager.getDefault(CarManager.class).getById(id);
        if (!data.path(LOCATION).isMissingNode()) {
            String locationId = data.path(LOCATION).asText();
            String trackId = data.path(TRACK).asText();
            Location location = InstanceManager.getDefault(LocationManager.class).getLocationById(locationId);
            Track track = (trackId != null) ? location.getTrackById(trackId) : null;
            if (!car.setLocation(location, track, true).equals(Track.OKAY)) {
                throw new JsonException(428, Bundle.getMessage(locale, "ErrorMovingCar", id, locationId, trackId));
            }
        }
    }

}
