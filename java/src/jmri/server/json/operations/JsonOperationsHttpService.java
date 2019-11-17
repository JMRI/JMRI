package jmri.server.json.operations;

import static jmri.server.json.JSON.COLOR;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.JSON.FORCE_DELETE;
import static jmri.server.json.JSON.HAZARDOUS;
import static jmri.server.json.JSON.LENGTH;
import static jmri.server.json.JSON.MODEL;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.NUMBER;
import static jmri.server.json.JSON.OWNER;
import static jmri.server.json.JSON.RENAME;
import static jmri.server.json.JSON.RFID;
import static jmri.server.json.JSON.ROAD;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.JSON.UTILITY;
import static jmri.server.json.operations.JsonOperations.BUILT;
import static jmri.server.json.operations.JsonOperations.CABOOSE;
import static jmri.server.json.operations.JsonOperations.CAR;
import static jmri.server.json.operations.JsonOperations.CARS;
import static jmri.server.json.operations.JsonOperations.CAR_TYPE;
import static jmri.server.json.operations.JsonOperations.DESTINATION;
import static jmri.server.json.operations.JsonOperations.ENGINE;
import static jmri.server.json.operations.JsonOperations.FRED;
import static jmri.server.json.operations.JsonOperations.KERNEL;
import static jmri.server.json.operations.JsonOperations.LEAD;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.operations.JsonOperations.LOCATIONS;
import static jmri.server.json.operations.JsonOperations.OUT_OF_SERVICE;
import static jmri.server.json.operations.JsonOperations.PASSENGER;
import static jmri.server.json.operations.JsonOperations.ROLLING_STOCK;
import static jmri.server.json.operations.JsonOperations.TRACK;
import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;
import static jmri.server.json.operations.JsonOperations.WEIGHT;
import static jmri.server.json.operations.JsonOperations.WEIGHT_TONS;
import static jmri.server.json.reporter.JsonReporter.REPORTER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 * @author Randall Wood (C) 2016, 2018, 2019
 */
public class JsonOperationsHttpService extends JsonHttpService {

    private final JsonUtil utilities;

    public JsonOperationsHttpService(ObjectMapper mapper) {
        super(mapper);
        utilities = new JsonUtil(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case CAR:
                return message(type, utilities.getCar(name, locale, id), id);
            case CAR_TYPE:
                return getCarType(name, locale, id);
            case ENGINE:
                return message(type, utilities.getEngine(name, locale, id), id);
            case KERNEL:
                Kernel kernel = carManager().getKernelByName(name);
                if (kernel == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, type, name), id);
                }
                return getKernel(kernel, locale, id);
            case LOCATION:
                return message(type, utilities.getLocation(name, locale, id), id);
            case ROLLING_STOCK:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        Bundle.getMessage(locale, "GetNotAllowed", type), id);
            case TRAIN:
            case TRAINS:
                return message(TRAIN, utilities.getTrain(name, locale, id), id);
            case TRACK:
                return message(type, utilities.getTrack(getTrackByName(name, data, locale, id), locale), id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(locale, "ErrorInternal", type), id);
        }
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        String newName = name;
        switch (type) {
            case CAR:
                return message(type, postCar(name, data, locale, id), id);
            case CAR_TYPE:
                if (!data.path(RENAME).isMissingNode() && data.path(RENAME).isTextual()) {
                    newName = data.path(RENAME).asText();
                    InstanceManager.getDefault(CarTypes.class).replaceName(name, newName);
                }
                return getCarType(newName, locale, id).put(RENAME, name);
            case ENGINE:
                return message(type, postEngine(name, data, locale, id), id);
            case KERNEL:
                if (!data.path(RENAME).isMissingNode() && data.path(RENAME).isTextual()) {
                    newName = data.path(RENAME).asText();
                    carManager().replaceKernelName(name, newName);
                }
                return getKernel(carManager().getKernelByName(name), locale, id).put(RENAME, name);
            case LOCATION:
                return message(type, postLocation(name, data, locale, id), id);
            case TRAIN:
                setTrain(name, data, locale, id);
                break;
            case TRACK:
                return message(type, postTrack(name, data, locale, id), id);
            case TRAINS:
                // do nothing
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        Bundle.getMessage(locale, "PostNotAllowed", type), id); // NOI18N
        }
        return doGet(type, name, data, locale, id);
    }

    @Override
    // Nullable overrides super class'es non-null requirement for name
    public JsonNode doPut(String type, @CheckForNull String name, JsonNode data, Locale locale, int id)
            throws JsonException {
        switch (type) {
            case CAR:
                if (data.path(ROAD).isMissingNode()) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, ROAD, type), id); // NOI18N
                }
                if (data.path(NUMBER).isMissingNode()) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, NUMBER, type), id); // NOI18N
                }
                String road = data.path(ROAD).asText();
                String number = data.path(NUMBER).asText();
                if (carManager().getById(name) != null || carManager().getByRoadAndNumber(road, number) != null) {
                    throw new JsonException(HttpServletResponse.SC_CONFLICT,
                            Bundle.getMessage(locale, "ErrorPutRollingStockConflict", type, road, number), id); // NOI18N
                }
                Car car = carManager().newRS(road, number);
                return message(type, postCar(car, data, locale, id), id);
            case CAR_TYPE:
                if (name == null) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, NAME, type), id); // NOI18N
                }
                InstanceManager.getDefault(CarTypes.class).addName(name);
                return getCarType(name, locale, id);
            case ENGINE:
                if (data.path(ROAD).isMissingNode()) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, ROAD, type), id); // NOI18N
                }
                if (data.path(NUMBER).isMissingNode()) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, NUMBER, type), id); // NOI18N
                }
                road = data.path(ROAD).asText();
                number = data.path(NUMBER).asText();
                if (engineManager().getById(name) != null || engineManager().getByRoadAndNumber(road, number) != null) {
                    throw new JsonException(HttpServletResponse.SC_CONFLICT,
                            Bundle.getMessage(locale, "ErrorPutRollingStockConflict", type, road, number), id); // NOI18N
                }
                Engine engine = engineManager().newRS(road, number);
                return message(type, postEngine(engine, data, locale, id), id);
            case KERNEL:
                if (name == null) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, NAME, type), id); // NOI18N
                }
                Kernel kernel = carManager().newKernel(name);
                return getKernel(kernel, locale, id);
            case LOCATION:
                if (data.path(USERNAME).isMissingNode()) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, USERNAME, type), id); // NOI18N
                }
                String userName = data.path(USERNAME).asText();
                if (name != null && locationManager().getLocationById(name) != null) {
                    throw new JsonException(HttpServletResponse.SC_CONFLICT,
                            Bundle.getMessage(locale, "ErrorPutNameConflict", type, name), id); // NOI18N
                }
                if (locationManager().getLocationByName(userName) != null) {
                    throw new JsonException(HttpServletResponse.SC_CONFLICT,
                            Bundle.getMessage(locale, "ErrorPutUserNameConflict", type, userName), id); // NOI18N
                }
                Location location = locationManager().newLocation(userName);
                return message(type, postLocation(location, data, locale, id), id);
            case TRACK:
                if (data.path(USERNAME).isMissingNode()) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, USERNAME, type), id); // NOI18N
                }
                userName = data.path(USERNAME).asText();
                if (data.path(TYPE).isMissingNode()) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, TYPE, type), id); // NOI18N
                }
                String trackType = data.path(TYPE).asText();
                if (data.path(LOCATION).isMissingNode()) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, LOCATION, type), id); // NOI18N
                }
                String locationName = data.path(LOCATION).asText();
                location = locationManager().getLocationById(locationName);
                if (location == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, LOCATION, locationName), id); // NOI18N
                }
                if (name != null && location.getTrackById(name) != null) {
                    throw new JsonException(HttpServletResponse.SC_CONFLICT,
                            Bundle.getMessage(locale, "ErrorPutNameConflict", type, name), id); // NOI18N
                }
                if (location.getTrackByName(userName, trackType) != null) {
                    throw new JsonException(HttpServletResponse.SC_CONFLICT,
                            Bundle.getMessage(locale, "ErrorPutUserNameConflict", type, userName), id); // NOI18N
                }
                Track track = location.addTrack(userName, trackType);
                return message(type, postTrack(track, data, locale, id), id);
            default:
                if (name == null) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, JsonException.ERROR_MISSING_PROPERTY_PUT, NAME, type), id); // NOI18N
                }
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
                return getCarTypes(locale, id);
            case ENGINE:
            case ENGINES:
                return message(getEngines(locale, id), id);
            case KERNEL:
                return getKernels(locale, id);
            case LOCATION:
            case LOCATIONS:
                return getLocations(locale, id);
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
            case CAR:
                // TODO: do not remove an in use car
                deleteCar(name, locale, id);
                break;
            case CAR_TYPE:
                List<Car> cars = carManager().getByTypeList(name);
                List<Location> locations = new ArrayList<>();
                for (Location location : locationManager().getList()) {
                    if (location.acceptsTypeName(name)) {
                        locations.add(location);
                    }
                }
                if ((!cars.isEmpty() || !locations.isEmpty()) && !acceptForceDeleteToken(type, name, token)) {
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
            case ENGINE:
                // TODO: do not remove an in use engine
                deleteEngine(name, locale, id);
                break;
            case KERNEL:
                Kernel kernel = carManager().getKernelByName(name);
                if (kernel == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, type, name), id);
                }
                if (kernel.getSize() != 0 && !acceptForceDeleteToken(type, name, token)) {
                    throwDeleteConflictException(type, name, getKernelCars(kernel, true, locale), locale, id);
                }
                carManager().deleteKernel(name);
                break;
            case LOCATION:
                // TODO: do not remove an in use location
                deleteLocation(name, locale, id);
                break;
            case TRACK:
                // TODO: do not remove an in use track
                deleteTrack(name, data, locale, id);
                break;
            default:
                super.doDelete(type, name, data, locale, id);
        }
    }

    private ObjectNode getCarType(String name, Locale locale, int id) throws JsonException {
        CarTypes manager = InstanceManager.getDefault(CarTypes.class);
        if (!manager.containsName(name)) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, CAR_TYPE, name), id);
        }
        ObjectNode data = mapper.createObjectNode();
        data.put(NAME, name);
        ArrayNode cars = data.putArray(CARS);
        carManager().getByTypeList(name).forEach(car -> cars.add(utilities.getCar(car, locale)));
        ArrayNode locations = data.putArray(LOCATIONS);
        locationManager().getList().stream()
                .filter(location -> location.acceptsTypeName(name))
                .forEach(location -> locations.add(utilities.getLocation(location, locale)));
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
        kernel.getCars().forEach(car -> {
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
        carManager().getKernelNameList()
                // individual kernels should not have id in array, but same
                // method is used to get single kernels as requested, so pass
                // additive inverse of id to allow errors
                .forEach(kernel -> array.add(getKernel(carManager().getKernelByName(kernel), locale, id * -1)));
        return message(array, id);
    }

    public ArrayNode getCars(Locale locale, int id) {
        ArrayNode array = mapper.createArrayNode();
        carManager().getByIdList()
                .forEach(car -> array.add(message(CAR, utilities.getCar(car, locale), id)));
        return array;
    }

    public ArrayNode getEngines(Locale locale, int id) {
        ArrayNode array = mapper.createArrayNode();
        engineManager().getByIdList()
                .forEach(engine -> array.add(message(ENGINE, utilities.getEngine(engine, locale), id)));
        return array;
    }

    public JsonNode getLocations(Locale locale, int id) {
        ArrayNode array = mapper.createArrayNode();
        locationManager().getLocationsByIdList()
                .forEach(location -> array.add(message(LOCATION, utilities.getLocation(location, locale), id)));
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
     *                                        location in data.
     */
    public void setTrain(String name, JsonNode data, Locale locale, int id) throws JsonException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById(name);
        JsonNode location = data.path(LOCATION);
        if (!location.isMissingNode()) {
            if (location.isNull()) {
                train.terminate();
            } else if (!train.move(location.asText())) {
                throw new JsonException(428, Bundle.getMessage(locale, "ErrorTrainMovement", name, location.asText()),
                        id);
            }
        }
    }

    public ObjectNode postLocation(String name, JsonNode data, Locale locale, int id) throws JsonException {
        return postLocation(getLocationByName(name, locale, id), data, locale, id);
    }

    public ObjectNode postLocation(Location location, JsonNode data, Locale locale, int id) throws JsonException {
        // set things that throw exceptions first
        if (!data.path(REPORTER).isMissingNode()) {
            String name = data.path(REPORTER).asText();
            Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getBeanBySystemName(name);
            if (reporter != null) {
                location.setReporter(reporter);
            } else {
                throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                        Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, REPORTER, name), id);
            }
        }
        location.setName(data.path(USERNAME).asText(location.getName()));
        location.setComment(data.path(COMMENT).asText(location.getComment()));
        return utilities.getLocation(location, locale);
    }

    public ObjectNode postTrack(String name, JsonNode data, Locale locale, int id) throws JsonException {
        return postTrack(getTrackByName(name, data, locale, id), data, locale, id);
    }

    public ObjectNode postTrack(Track track, JsonNode data, Locale locale, int id) throws JsonException {
        // set things that throw exceptions first
        if (!data.path(REPORTER).isMissingNode()) {
            String name = data.path(REPORTER).asText();
            Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getBeanBySystemName(name);
            if (reporter != null) {
                track.setReporter(reporter);
            } else {
                throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                        Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, REPORTER, name), id);
            }
        }
        track.setName(data.path(USERNAME).asText(track.getName()));
        track.setLength(data.path(LENGTH).asInt(track.getLength()));
        track.setComment(data.path(COMMENT).asText(track.getComment()));
        return utilities.getTrack(track, locale);
    }

    /**
     * Set the properties in the data parameter for the given car.
     * <p>
     * <strong>Note</strong> returns the modified car because changing the road
     * or number of a car changes its name in the JSON representation.
     *
     * @param name   the operations id of the car to change
     * @param data   car data to change
     * @param locale locale to throw exceptions in
     * @param id     message id set by client
     * @return the JSON representation of the car
     * @throws JsonException if a car by name cannot be found
     */
    public ObjectNode postCar(String name, JsonNode data, Locale locale, int id) throws JsonException {
        return postCar(getCarByName(name, locale, id), data, locale, id);
    }

    /**
     * Set the properties in the data parameter for the given car.
     * <p>
     * <strong>Note</strong> returns the modified car because changing the road
     * or number of a car changes its name in the JSON representation.
     *
     * @param car    the car to change
     * @param data   car data to change
     * @param locale locale to throw exceptions in
     * @param id     message id set by client
     * @return the JSON representation of the car
     * @throws JsonException if unable to set location
     */
    public ObjectNode postCar(@Nonnull Car car, JsonNode data, Locale locale, int id) throws JsonException {
        ObjectNode result = postRollingStock(car, data, locale, id);
        car.setCaboose(data.path(CABOOSE).asBoolean(car.isCaboose()));
        car.setHazardous(data.path(HAZARDOUS).asBoolean(car.isHazardous()));
        car.setPassenger(data.path(PASSENGER).asBoolean(car.isPassenger()));
        car.setFred(data.path(FRED).asBoolean(car.hasFred()));
        car.setUtility(data.path(UTILITY).asBoolean(car.isUtility()));
        return utilities.getCar(car, result, locale);
    }

    /**
     * Set the properties in the data parameter for the given engine.
     * <p>
     * <strong>Note</strong> returns the modified engine because changing the
     * road or number of an engine changes its name in the JSON representation.
     *
     * @param name   the operations id of the engine to change
     * @param data   engine data to change
     * @param locale locale to throw exceptions in
     * @param id     message id set by client
     * @return the JSON representation of the engine
     * @throws JsonException if a engine by name cannot be found
     */
    public ObjectNode postEngine(String name, JsonNode data, Locale locale, int id) throws JsonException {
        return postEngine(getEngineByName(name, locale, id), data, locale, id);
    }

    /**
     * Set the properties in the data parameter for the given engine.
     * <p>
     * <strong>Note</strong> returns the modified engine because changing the
     * road or number of an engine changes its name in the JSON representation.
     *
     * @param engine the engine to change
     * @param data   engine data to change
     * @param locale locale to throw exceptions in
     * @param id     message id set by client
     * @return the JSON representation of the engine
     * @throws JsonException if unable to set location
     */
    public ObjectNode postEngine(@Nonnull Engine engine, JsonNode data, Locale locale, int id) throws JsonException {
        // set model early, since setting other values depend on it
        engine.setModel(data.path(MODEL).asText(engine.getModel()));
        ObjectNode result = postRollingStock(engine, data, locale, id);
        return utilities.getEngine(engine, result, locale);
    }

    /**
     * Set the properties in the data parameter for the given rolling stock.
     * <p>
     * <strong>Note</strong> returns the modified rolling stock because changing
     * the road or number of a rolling stock changes its name in the JSON
     * representation.
     *
     * @param rs     the rolling stock to change
     * @param data   rolling stock data to change
     * @param locale locale to throw exceptions in
     * @param id     message id set by client
     * @return the JSON representation of the rolling stock
     * @throws JsonException if unable to set location
     */
    public ObjectNode postRollingStock(@Nonnull RollingStock rs, JsonNode data, Locale locale, int id)
            throws JsonException {
        String name = rs.getId();
        // make changes that can throw an exception first
        JsonNode node = data.path(LOCATION);
        if (!node.isMissingNode() && !node.path(NAME).isMissingNode()) {
            Location location = locationManager().getLocationById(node.path(NAME).asText());
            if (location != null) {
                Track track = location.getTrackById(node.path(TRACK).path(NAME).asText());
                if (!rs.setLocation(location, track).equals(Track.OKAY)) {
                    throw new JsonException(HttpServletResponse.SC_CONFLICT, Bundle.getMessage(locale, "ErrorMovingCar",
                            rs.getId(), location.getId(), track != null ? track.getId() : null), id);
                }
            }
        }
        node = data.path(DESTINATION);
        if (!node.isMissingNode() && !node.path(NAME).isMissingNode()) {
            Location location = locationManager().getLocationById(node.path(NAME).asText());
            if (location != null) {
                Track track = location.getTrackById(node.path(TRACK).path(NAME).asText());
                if (!rs.setDestination(location, track).equals(Track.OKAY)) {
                    throw new JsonException(HttpServletResponse.SC_CONFLICT,
                            Bundle.getMessage(locale, "ErrorMovingCar", rs.getId(),
                                    location.getId(), track != null ? track.getId() : null),
                            id);
                }
            }
        }
        // set properties using the existing property as the default
        rs.setRoadName(data.path(ROAD).asText(rs.getRoadName()));
        rs.setNumber(data.path(NUMBER).asText(rs.getNumber()));
        rs.setColor(data.path(COLOR).asText(rs.getColor()));
        rs.setComment(data.path(COMMENT).asText(rs.getComment()));
        rs.setOwner(data.path(OWNER).asText(rs.getOwner()));
        rs.setBuilt(data.path(BUILT).asText(rs.getBuilt()));
        if (data.path(WEIGHT).isValueNode()) {
            rs.setWeight(Double.toString(data.path(WEIGHT).asDouble()));
        }
        if (data.path(WEIGHT_TONS).isValueNode()) {
            rs.setWeightTons(Double.toString(data.path(WEIGHT_TONS).asDouble()));
        }
        rs.setRfid(data.path(RFID).asText(rs.getRfid()));
        rs.setLength(Integer.toString(data.path(LENGTH).asInt(rs.getLengthInteger())));
        rs.setOutOfService(data.path(OUT_OF_SERVICE).asBoolean(rs.isOutOfService()));
        rs.setTypeName(data.path(CAR_TYPE).asText(rs.getTypeName()));
        ObjectNode result = utilities.getRollingStock(rs, locale);
        if (!rs.getId().equals(name)) {
            result.put(RENAME, name);
        }
        return result;
    }

    public void deleteCar(@Nonnull String name, @Nonnull Locale locale, int id)
            throws JsonException {
        carManager().deregister(getCarByName(name, locale, id));
    }

    public void deleteEngine(@Nonnull String name, @Nonnull Locale locale, int id)
            throws JsonException {
        engineManager().deregister(getEngineByName(name, locale, id));
    }

    public void deleteLocation(@Nonnull String name, @Nonnull Locale locale, int id)
            throws JsonException {
        locationManager().deregister(getLocationByName(name, locale, id));
    }

    public void deleteTrack(@Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale, int id)
            throws JsonException {
        Track track = getTrackByName(name, data, locale, id);
        track.getLocation().deleteTrack(track);
    }

    protected @Nonnull Car getCarByName(@Nonnull String name, @Nonnull Locale locale, int id) throws JsonException {
        Car car = carManager().getById(name);
        if (car == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, CAR, name), id);
        }
        return car;
    }

    protected @Nonnull Engine getEngineByName(@Nonnull String name, @Nonnull Locale locale, int id)
            throws JsonException {
        Engine engine = engineManager().getById(name);
        if (engine == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, ENGINE, name), id);
        }
        return engine;
    }

    protected @Nonnull Location getLocationByName(@Nonnull String name, @Nonnull Locale locale, int id)
            throws JsonException {
        Location location = locationManager().getLocationById(name);
        if (location == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, LOCATION, name), id);
        }
        return location;
    }

    protected @Nonnull Track getTrackByName(@Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale,
            int id) throws JsonException {
        if (data.path(LOCATION).isMissingNode()) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                    Bundle.getMessage(locale, "ErrorMissingAttribute", LOCATION, TRACK), id);
        }
        Location location = getLocationByName(data.path(LOCATION).asText(), locale, id);
        Track track = location.getTrackById(name);
        if (track == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, TRACK, name), id);
        }
        return track;
    }

    protected CarManager carManager() {
        return InstanceManager.getDefault(CarManager.class);
    }

    protected EngineManager engineManager() {
        return InstanceManager.getDefault(EngineManager.class);
    }

    protected LocationManager locationManager() {
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
            case KERNEL:
            case TRACK:
                return doSchema(type,
                        server,
                        "jmri/server/json/operations/" + type + "-server.json",
                        "jmri/server/json/operations/" + type + "-client.json",
                        id);
            case ENGINE:
            case ENGINES:
                return doSchema(type,
                        server,
                        "jmri/server/json/operations/engine-server.json",
                        "jmri/server/json/operations/engine-client.json",
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
                                mapper.readTree(getClass().getClassLoader()
                                        .getResource("jmri/server/json/operations/rollingStock-server.json")),
                                id);
                    } catch (IOException ex) {
                        throw new JsonException(500, ex, id);
                    }
                } else {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(locale, "NotAClientType", type), id);
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
                        Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

}
