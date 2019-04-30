package jmri.server.json.operations;

import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.JSON.FORCE_DELETE;
import static jmri.server.json.JSON.LENGTH;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.NULL;
import static jmri.server.json.JSON.RENAME;
import static jmri.server.json.operations.JsonOperations.CAR;
import static jmri.server.json.operations.JsonOperations.CAR_TYPE;
import static jmri.server.json.operations.JsonOperations.CARS;
import static jmri.server.json.operations.JsonOperations.ENGINE;
import static jmri.server.json.operations.JsonOperations.KERNEL;
import static jmri.server.json.operations.JsonOperations.LEAD;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.operations.JsonOperations.LOCATIONS;
import static jmri.server.json.operations.JsonOperations.ROLLING_STOCK;
import static jmri.server.json.operations.JsonOperations.TRACK;
import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;
import static jmri.server.json.operations.JsonOperations.WEIGHT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood (C) 2016, 2018, 2019
 */
public class JsonOperationsHttpService extends JsonHttpService {

    // private final static Logger log = LoggerFactory.getLogger(JsonOperationsHttpService.class);
    private final JsonUtil utilities;

    public JsonOperationsHttpService(ObjectMapper mapper) {
        super(mapper);
        this.utilities = new JsonUtil(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case CAR:
                return message(CAR, utilities.getCar(name, locale), id);
            case CAR_TYPE:
                return getCarType(name, locale, id);
            case ENGINE:
                return message(ENGINE, utilities.getEngine(name, locale), id);
            case KERNEL:
                Kernel kernel = getCarManager().getKernelByName(name);
                if (kernel == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(locale, "ErrorNotFound", type, name), id);
                }
                return getKernel(kernel, locale, id);
            case LOCATION:
                return message(LOCATION, utilities.getLocation(name, locale, id), id);
            case ROLLING_STOCK:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        Bundle.getMessage(locale, "GetNotAllowed", type), id);
            case TRAIN:
            case TRAINS:
                return message(TRAIN, utilities.getTrain(name, locale, id), id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(locale, "ErrorInternal", type), id);
        }
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        String newName = name;
        switch (type) {
            case TRAIN:
                this.setTrain(name, data, locale, id);
                return message(TRAIN, utilities.getTrain(name, locale, id), id);
            case CAR:
                this.setCar(name, data, locale, id);
                return message(CAR, utilities.getCar(name, locale), id);
            case CAR_TYPE:
                if (!data.path(RENAME).isMissingNode() && data.path(RENAME).isTextual()) {
                    newName = data.path(RENAME).asText();
                    InstanceManager.getDefault(CarTypes.class).replaceName(name, newName);
                }
                return getCarType(newName, locale, id).put(RENAME, name);
            case KERNEL:
                if (!data.path(RENAME).isMissingNode() && data.path(RENAME).isTextual()) {
                    newName = data.path(RENAME).asText();
                    getCarManager().replaceKernelName(name, newName);
                }
                return getKernel(getCarManager().getKernelByName(name), locale, id).put(RENAME, name);
            case ENGINE:
            case LOCATION:
            case TRAINS:
                return this.doGet(type, name, data, locale, id);
            default:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        Bundle.getMessage(locale, "PostNotAllowed", type), id); // NOI18N
        }
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case CAR_TYPE:
                InstanceManager.getDefault(CarTypes.class).addName(name);
                return getCarType(name, locale, id);
            case KERNEL:
                Kernel kernel = getCarManager().newKernel(name);
                return getKernel(kernel, locale, id);
            default:
                return super.doPut(type, name, data, locale, id);
        }
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case CAR:
            case CARS:
                return message(getCars(locale, id), id);
            case CAR_TYPE:
                return this.getCarTypes(locale, id);
            case ENGINE:
            case ENGINES:
                return message(getEngines(locale, id), id);
            case KERNEL:
                return this.getKernels(locale, id);
            case LOCATION:
            case LOCATIONS:
                return this.getLocations(locale, id);
            case ROLLING_STOCK:
                return message(getCars(locale, id).addAll(getEngines(locale, id)), id);
            case TRAIN:
            case TRAINS:
                return message(utilities.getTrains(locale), id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(locale, "ErrorInternal", type), id); // NOI18N
        }
    }

    @Override
    public void doDelete(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        String token = data.path(FORCE_DELETE).asText();
        switch (type) {
            case KERNEL:
                Kernel kernel = getCarManager().getKernelByName(name);
                if (kernel == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(locale, "ErrorNotFound", type, name), id);
                }
                if (kernel.getSize() != 0 && !acceptForceDeleteToken(type, name, token)) {
                    throwDeleteConflictException(type, name, getKernelCars(kernel, true, locale), locale, id);
                }
                getCarManager().deleteKernel(name);
                break;
            case CAR_TYPE:
                List<Car> cars = getCarManager().getByTypeList(name);
                List<Location> locations = new ArrayList<>();
                for (Location location : getLocationManager().getList()) {
                    if (location.acceptsTypeName(name)) {
                        locations.add(location);
                    }
                }
                if ((cars.size() != 0 || locations.size() != 0) && !acceptForceDeleteToken(type, name, token)) {
                    ArrayNode conflicts = mapper.createArrayNode();
                    for (Car car : cars) {
                        conflicts.add(message(CAR, utilities.getCar(car, locale), 0));
                    }
                    for (Location location : locations) {
                        conflicts.add(message(LOCATION, utilities.getLocation(location, locale), 0));
                    }
                    throwDeleteConflictException(type, name, conflicts, locale, id);
                }
                InstanceManager.getDefault(CarTypes.class).deleteName(name);
                break;
            default:
                super.doDelete(type, name, data, locale, id);
        }
    }

    private ObjectNode getCarType(String name, Locale locale, int id) throws JsonException {
        CarTypes manager = InstanceManager.getDefault(CarTypes.class);
        if (!manager.containsName(name)) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, "ErrorNotFound", CAR_TYPE, name), id);
        }
        ObjectNode data = mapper.createObjectNode();
        data.put(NAME, name);
        ArrayNode cars = data.putArray(CARS);
        getCarManager().getByTypeList(name).forEach((car) -> {
            cars.add(utilities.getCar(car, locale));
        });
        ArrayNode locations = data.putArray(LOCATIONS);
        getLocationManager().getList().stream().filter((location) -> {
            return location.acceptsTypeName(name);
        }).forEach((location) -> {
            locations.add(utilities.getLocation(location, locale));
        });
        return message(CAR_TYPE, data, id);
    }

    private JsonNode getCarTypes(Locale locale, int id) throws JsonException {
        ArrayNode array = mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(CarTypes.class).getNames()) {
            array.add(getCarType(name, locale, id));
        }
        return message(array, id);
    }

    private ObjectNode getKernel(Kernel kernel, Locale locale, int id) {
        ObjectNode data = mapper.createObjectNode();
        data.put(NAME, kernel.getName());
        data.put(WEIGHT, kernel.getAdjustedWeightTons());
        data.put(LENGTH, kernel.getTotalLength());
        Car lead = kernel.getLead();
        if (lead != null) {
            data.set(LEAD, utilities.getCar(kernel.getLead(), locale));
        } else {
            data.putNull(LEAD);
        }
        data.set(CARS, getKernelCars(kernel, false, locale));
        return message(KERNEL, data, id);
    }

    private ArrayNode getKernelCars(Kernel kernel, boolean asMessage, Locale locale) {
        ArrayNode array = mapper.createArrayNode();
        kernel.getCars().forEach((car) -> {
            if (asMessage) {
                array.add(message(CAR, utilities.getCar(car, locale), 0));
            } else {
                array.add(utilities.getCar(car, locale));
            }
        });
        return array;
    }

    private JsonNode getKernels(Locale locale, int id) {
        ArrayNode array = mapper.createArrayNode();
        getCarManager().getKernelNameList().forEach((kernel) -> {
            array.add(getKernel(getCarManager().getKernelByName(kernel), locale, id));
        });
        return message(array, id);
    }

    public ArrayNode getCars(Locale locale, int id) {
        ArrayNode array = mapper.createArrayNode();
        getCarManager().getByIdList().forEach((car) -> {
            array.add(message(CAR, utilities.getCar(car, locale), id));
        });
        return array;
    }

    public ArrayNode getEngines(Locale locale, int id) {
        ArrayNode array = mapper.createArrayNode();
        InstanceManager.getDefault(EngineManager.class).getByIdList().forEach((engine) -> {
            array.add(message(ENGINE, utilities.getEngine(engine, locale), id));
        });
        return array;
    }

    public JsonNode getLocations(Locale locale, int id) throws JsonException {
        ArrayNode array = mapper.createArrayNode();
        getLocationManager().getLocationsByIdList().forEach((location) -> {
            array.add(message(LOCATION, utilities.getLocation(location, locale), id));
        });
        return message(array, id);
    }

    /**
     * Set the properties in the data parameter for the train with the given id.
     * <p>
     * Currently only moves the train to the location given with the key
     * {@value jmri.server.json.operations.JsonOperations#LOCATION}. If the move
     * cannot be completed, throws error code 428.
     *
     * @param name   id of the train
     * @param data   train data to change
     * @param locale locale to throw exceptions in
     * @param id     message id set by client
     * @throws jmri.server.json.JsonException if the train cannot move to the
     *                                            location in data.
     */
    public void setTrain(String name, JsonNode data, Locale locale, int id) throws JsonException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById(name);
        if (!data.path(LOCATION).isMissingNode()) {
            String location = data.path(LOCATION).asText();
            if (location.equals(NULL)) {
                train.terminate();
            } else if (!train.move(location)) {
                throw new JsonException(428, Bundle.getMessage(locale, "ErrorTrainMovement", name, location), id);
            }
        }
    }

    /**
     * Set the properties in the data parameter for the car with the given id.
     * <p>
     * Currently only sets the location of the car.
     *
     * @param name   id of the car
     * @param data   car data to change
     * @param locale locale to throw exceptions in
     * @param id     message id set by client
     * @throws jmri.server.json.JsonException if the car cannot be set to the
     *                                            location in data
     */
    public void setCar(String name, JsonNode data, Locale locale, int id) throws JsonException {
        Car car = getCarManager().getById(name);
        if (!data.path(LOCATION).isMissingNode()) {
            String locationId = data.path(LOCATION).asText();
            String trackId = data.path(TRACK).asText();
            Location location = getLocationManager().getLocationById(locationId);
            Track track = (trackId != null) ? location.getTrackById(trackId) : null;
            if (!car.setLocation(location, track, true).equals(Track.OKAY)) {
                throw new JsonException(428, Bundle.getMessage(locale, "ErrorMovingCar", name, locationId, trackId), id);
            }
        }
    }

    private CarManager getCarManager() {
        return InstanceManager.getDefault(CarManager.class);
    }

    private LocationManager getLocationManager() {
        return InstanceManager.getDefault(LocationManager.class);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case CAR:
            case CARS:
                return doSchema(type,
                        server,
                        "jmri/server/json/operations/car-server.json",
                        "jmri/server/json/operations/car-client.json",
                        id);
            case CAR_TYPE:
                return doSchema(type,
                        server,
                        "jmri/server/json/operations/carType-server.json",
                        "jmri/server/json/operations/carType-client.json",
                        id);
            case ENGINE:
            case ENGINES:
                return doSchema(type,
                        server,
                        "jmri/server/json/operations/engine-server.json",
                        "jmri/server/json/operations/engine-client.json",
                        id);
            case KERNEL:
                return doSchema(type,
                        server,
                        "jmri/server/json/operations/kernel-server.json",
                        "jmri/server/json/operations/kernel-client.json",
                        id);
            case LOCATION:
            case LOCATIONS:
                return doSchema(type,
                        server,
                        "jmri/server/json/operations/location-server.json",
                        "jmri/server/json/operations/location-client.json",
                        id);
            case ROLLING_STOCK:
                if (server) {
                    try {
                        return doSchema(type, server,
                                this.mapper.readTree(this.getClass().getClassLoader().getResource("jmri/server/json/operations/rollingStock-server.json")), id);
                    } catch (IOException ex) {
                        throw new JsonException(500, ex, id);
                    }
                } else {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "NotAClientType", type), id);
                }
            case TRAIN:
            case TRAINS:
                return doSchema(type,
                        server,
                        "jmri/server/json/operations/train-server.json",
                        "jmri/server/json/operations/train-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(locale, "ErrorUnknownType", type), id);
        }
    }

}
