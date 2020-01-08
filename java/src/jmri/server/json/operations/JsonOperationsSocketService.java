package jmri.server.json.operations;

import static jmri.server.json.operations.JsonOperations.CAR;
import static jmri.server.json.operations.JsonOperations.CARS;
import static jmri.server.json.operations.JsonOperations.ENGINE;
import static jmri.server.json.operations.JsonOperations.ENGINES;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.operations.JsonOperations.LOCATIONS;
import static jmri.server.json.operations.JsonOperations.LOCATION_COMMENT;
import static jmri.server.json.operations.JsonOperations.LOCATION_NAME;
import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author Randall Wood (C) 2016, 2019
 */
public class JsonOperationsSocketService extends JsonSocketService<JsonOperationsHttpService> {

    private final HashMap<String, TrainListener> trainListeners = new HashMap<>();
    private final TrainsListener trainsListener = new TrainsListener();
    private final HashMap<String, CarListener> carListeners = new HashMap<>();
    private final CarsListener carsListener = new CarsListener();
    private final HashMap<String, LocationListener> locationListeners = new HashMap<>();
    private final LocationsListener locationsListener = new LocationsListener();
    private final HashMap<String, EngineListener> engineListeners = new HashMap<>();
    private final EnginesListener enginesListener = new EnginesListener();

    private static final Logger log = LoggerFactory.getLogger(JsonOperationsSocketService.class);

    public JsonOperationsSocketService(JsonConnection connection) {
        this(connection, new JsonOperationsHttpService(connection.getObjectMapper()));
    }

    protected JsonOperationsSocketService(JsonConnection connection, JsonOperationsHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, JsonRequest request)
            throws IOException, JmriException, JsonException {
        String name = data.path(JSON.NAME).asText();
        // add listener to name if not already listening
        if (!method.equals(JSON.DELETE)) {
            switch (type) {
                case TRAIN:
                    if (!trainListeners.containsKey(name)) {
                        trainListeners.put(name, new TrainListener(name));
                        InstanceManager.getDefault(TrainManager.class).getTrainById(name)
                                .addPropertyChangeListener(trainListeners.get(name));
                    }
                    break;
                case CAR:
                    if (!carListeners.containsKey(name)) {
                        carListeners.put(name, new CarListener(name));
                        InstanceManager.getDefault(CarManager.class).getById(name)
                                .addPropertyChangeListener(carListeners.get(name));
                    }
                    break;
                case LOCATION:
                    if (!locationListeners.containsKey(name)) {
                        locationListeners.put(name, new LocationListener(name));
                        InstanceManager.getDefault(LocationManager.class).getLocationById(name)
                                .addPropertyChangeListener(locationListeners.get(name));
                    }
                    break;
                case ENGINE:
                    if (!engineListeners.containsKey(name)) {
                        engineListeners.put(name, new EngineListener(name));
                        InstanceManager.getDefault(EngineManager.class).getById(name)
                                .addPropertyChangeListener(engineListeners.get(name));
                    }
                    break;
                default:
                    // other types ignored
                    break;
            }
        }
        switch (method) {
            case JSON.GET:
                connection.sendMessage(service.doGet(type, name, data, request), request.id);
                break;
            case JSON.DELETE:
                service.doDelete(type, name, data, request);
                break;
            case JSON.PUT:
                connection.sendMessage(service.doPut(type, name, data, request), request.id);
                break;
            case JSON.POST:
            default:
                connection.sendMessage(service.doPost(type, name, data, request), request.id);
        }
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGetList(type, data, request), request.id);
        switch (type) {
            case TRAIN:
            case TRAINS:
                log.debug("adding TrainsListener");
                InstanceManager.getDefault(TrainManager.class).addPropertyChangeListener(trainsListener); //add parent listener
                addListenersToTrains();
                break;
            case CAR:
            case CARS:
                log.debug("adding CarsListener");
                InstanceManager.getDefault(CarManager.class).addPropertyChangeListener(carsListener); //add parent listener
                addListenersToCars();
                break;
            case LOCATION:
            case LOCATIONS:
                log.debug("adding LocationsListener");
                InstanceManager.getDefault(LocationManager.class).addPropertyChangeListener(locationsListener); //add parent listener
                addListenersToLocations();
                break;
            case ENGINE:
            case ENGINES:
                log.debug("adding EnginesListener");
                InstanceManager.getDefault(EngineManager.class).addPropertyChangeListener(enginesListener); //add parent listener
                addListenersToEngines();
                break;
            default:
                break;
        }
    }

    private void addListenersToTrains() {
        InstanceManager.getDefault(TrainManager.class).getTrainsByIdList().stream().forEach(t -> { //add listeners to each child (if not already)
            if (!trainListeners.containsKey(t.getId())) {
                log.debug("adding TrainListener for Train ID '{}'", t.getId());
                trainListeners.put(t.getId(), new TrainListener(t.getId()));
                t.addPropertyChangeListener(trainListeners.get(t.getId()));
            }
        });
    }

    private void addListenersToCars() {
        InstanceManager.getDefault(CarManager.class).getByIdList().stream().forEach(t -> { //add listeners to each child (if not already)
            if (!carListeners.containsKey(t.getId())) {
                log.debug("adding CarListener for Car ID '{}'", t.getId());
                carListeners.put(t.getId(), new CarListener(t.getId()));
                t.addPropertyChangeListener(carListeners.get(t.getId()));
            }
        });
    }

    private void addListenersToLocations() {
        InstanceManager.getDefault(LocationManager.class).getLocationsByIdList().stream().forEach(t -> { //add listeners to each child (if not already)
            if (!locationListeners.containsKey(t.getId())) {
                log.debug("adding LocationListener for Location ID '{}'", t.getId());
                locationListeners.put(t.getId(), new LocationListener(t.getId()));
                t.addPropertyChangeListener(locationListeners.get(t.getId()));
            }
        });
    }

    private void addListenersToEngines() {
        InstanceManager.getDefault(EngineManager.class).getByIdList().stream().forEach(t -> { //add listeners to each child (if not already)
            if (!engineListeners.containsKey(t.getId())) {
                log.debug("adding EngineListener for Engine ID '{}'", t.getId());
                engineListeners.put(t.getId(), new EngineListener(t.getId()));
                t.addPropertyChangeListener(engineListeners.get(t.getId()));
            }
        });
    }

    @Override
    public void onClose() {
        trainListeners.values().forEach(listener -> listener.train.removePropertyChangeListener(listener));
        trainListeners.clear();
        InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(trainsListener);
    }

    private class TrainListener implements PropertyChangeListener {

        protected final Train train;

        protected TrainListener(String id) {
            train = InstanceManager.getDefault(TrainManager.class).getTrainById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in TrainListener for '{}' '{}' ('{}'=>'{}')", train.getId(), evt.getPropertyName(),
                    evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGet(TRAIN, train.getId(),
                            connection.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), 0)), 0);
                } catch (JsonException ex) {
                    log.warn("json error sending Train: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering trainListener due to IOException");
                train.removePropertyChangeListener(this);
                trainListeners.remove(train.getId());
            }
        }
    }

    private class TrainsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in TrainsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(),
                    evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGetList(TRAINS, service.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), 0)), 0);
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToTrains();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Trains: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering trainsListener due to IOException");
                InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(trainsListener);
            }
        }
    }

    private class CarListener implements PropertyChangeListener {

        protected final Car car;

        protected CarListener(String id) {
            car = InstanceManager.getDefault(CarManager.class).getById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in CarListener for '{}' '{}' ('{}'=>'{}')", car.getId(), evt.getPropertyName(),
                    evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGet(CAR, car.getId(),
                            connection.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), 0)), 0);
                } catch (JsonException ex) {
                    log.warn("json error sending Car: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering carListener due to IOException");
                car.removePropertyChangeListener(this);
                carListeners.remove(car.getId());
            }
        }
    }

    private class CarsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in CarsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(),
                    evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGetList(CARS, service.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), 0)), 0);
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("RollingStockListLength")) { // NOI18N
                        addListenersToCars();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Cars: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering carsListener due to IOException");
                InstanceManager.getDefault(CarManager.class).removePropertyChangeListener(carsListener);
            }
        }
    }

    private class LocationListener implements PropertyChangeListener {

        protected final Location location;

        protected LocationListener(String id) {
            location = InstanceManager.getDefault(LocationManager.class).getLocationById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in LocationListener for '{}' '{}' ('{}'=>'{}')", location.getId(), evt.getPropertyName(),
                    evt.getOldValue(), evt.getNewValue());
            //only send changes to properties that are included in object
            if (evt.getPropertyName().equals(JSON.ID) ||
                    evt.getPropertyName().equals(LOCATION_NAME) ||
                    evt.getPropertyName().equals(JSON.LENGTH) ||
                    evt.getPropertyName().equals(LOCATION_COMMENT)) {
                try {
                    try {
                        connection.sendMessage(service.doGet(LOCATION, location.getId(),
                                connection.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), 0)), 0);
                        log.debug(" sent Location '{}'", location.getId());
                    } catch (JsonException ex) {
                        log.warn("json error sending Location: {}", ex.getJsonMessage());
                        connection.sendMessage(ex.getJsonMessage(), 0);
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    log.debug("deregistering locationListener due to IOException");
                    location.removePropertyChangeListener(this);
                    locationListeners.remove(location.getId());
                }
            }
        }
    }

    private class LocationsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in LocationsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(),
                    evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGetList(LOCATIONS, service.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), 0)), 0);
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToLocations();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Locations: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering locationsListener due to IOException");
                InstanceManager.getDefault(LocationManager.class).removePropertyChangeListener(locationsListener);
            }
        }
    }

    private class EngineListener implements PropertyChangeListener {

        protected final Engine engine;

        protected EngineListener(String id) {
            engine = InstanceManager.getDefault(EngineManager.class).getById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in EngineListener for '{}' '{}' ('{}'=>'{}')", engine.getId(), evt.getPropertyName(),
                    evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGet(ENGINE, engine.getId(),
                            connection.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), 0)), 0);
                } catch (JsonException ex) {
                    log.warn("json error sending Engine: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering engineListener due to IOException");
                engine.removePropertyChangeListener(this);
                carListeners.remove(engine.getId());
            }
        }
    }

    private class EnginesListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in EnginesListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(),
                    evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGetList(ENGINES, service.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), 0)), 0);
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("RollingStockListLength")) { // NOI18N
                        addListenersToEngines();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Engines: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering enginesListener due to IOException");
                InstanceManager.getDefault(CarManager.class).removePropertyChangeListener(enginesListener);
            }
        }
    }

}
