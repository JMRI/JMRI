package jmri.server.json.operations;

import static jmri.server.json.JSON.ID;
import static jmri.server.json.JSON.LENGTH;
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

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
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
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2016
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

    private final static Logger log = LoggerFactory.getLogger(JsonOperationsSocketService.class);

    public JsonOperationsSocketService(JsonConnection connection) {
        super(connection, new JsonOperationsHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        String id = data.path(JSON.ID).asText(); // Operations uses ID attribute instead of name attribute
        // add listener to id if not already listening
        switch (type) {
            case TRAIN:
                if (!this.trainListeners.containsKey(id)) {
                    this.trainListeners.put(id, new TrainListener(id));
                    InstanceManager.getDefault(TrainManager.class).getTrainById(id).addPropertyChangeListener(this.trainListeners.get(id));
                }
                break;
            case CAR:
                if (!this.carListeners.containsKey(id)) {
                    this.carListeners.put(id, new CarListener(id));
                    InstanceManager.getDefault(CarManager.class).getById(id).addPropertyChangeListener(this.carListeners.get(id));
                }
                break;
            case LOCATION:
                if (!this.locationListeners.containsKey(id)) {
                    this.locationListeners.put(id, new LocationListener(id));
                    InstanceManager.getDefault(LocationManager.class).getLocationById(id).addPropertyChangeListener(this.locationListeners.get(id));
                }
                break;
            case ENGINE:
                if (!this.engineListeners.containsKey(id)) {
                    this.engineListeners.put(id, new EngineListener(id));
                    InstanceManager.getDefault(EngineManager.class).getById(id).addPropertyChangeListener(this.engineListeners.get(id));
                }
                break;
            default:
                // other types ignored
                break;
        }
        //post the message as it may contain incoming changes
        this.connection.sendMessage(this.service.doPost(type, id, data, locale));
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        switch (type) {
            case TRAINS:
                log.debug("adding TrainsListener");
                InstanceManager.getDefault(TrainManager.class).addPropertyChangeListener(trainsListener); //add parent listener
                addListenersToTrains();
                break;
            case CARS:
                log.debug("adding CarsListener");
                InstanceManager.getDefault(CarManager.class).addPropertyChangeListener(carsListener); //add parent listener
                addListenersToCars();
                break;
            case LOCATIONS:
                log.debug("adding LocationsListener");
                InstanceManager.getDefault(LocationManager.class).addPropertyChangeListener(locationsListener); //add parent listener
                addListenersToLocations();
                break;
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
        InstanceManager.getDefault(TrainManager.class).getTrainsByIdList().stream().forEach((t) -> { //add listeners to each child (if not already)
            if (!trainListeners.containsKey(t.getId())) {
                log.debug("adding TrainListener for Train ID '{}'", t.getId());
                trainListeners.put(t.getId(), new TrainListener(t.getId()));
                t.addPropertyChangeListener(this.trainListeners.get(t.getId()));
            }
        });
    }

    private void addListenersToCars() {
        InstanceManager.getDefault(CarManager.class).getByIdList().stream().forEach((t) -> { //add listeners to each child (if not already)
            if (!carListeners.containsKey(t.getId())) {
                log.debug("adding CarListener for Car ID '{}'", t.getId());
                carListeners.put(t.getId(), new CarListener(t.getId()));
                t.addPropertyChangeListener(this.carListeners.get(t.getId()));
            }
        });
    }

    private void addListenersToLocations() {
        InstanceManager.getDefault(LocationManager.class).getLocationsByIdList().stream().forEach((t) -> { //add listeners to each child (if not already)
            if (!locationListeners.containsKey(t.getId())) {
                log.debug("adding LocationListener for Location ID '{}'", t.getId());
                locationListeners.put(t.getId(), new LocationListener(t.getId()));
                t.addPropertyChangeListener(this.locationListeners.get(t.getId()));
            }
        });
    }

    private void addListenersToEngines() {
        InstanceManager.getDefault(EngineManager.class).getByIdList().stream().forEach((t) -> { //add listeners to each child (if not already)
            if (!engineListeners.containsKey(t.getId())) {
                log.debug("adding EngineListener for Engine ID '{}'", t.getId());
                engineListeners.put(t.getId(), new EngineListener(t.getId()));
                t.addPropertyChangeListener(this.engineListeners.get(t.getId()));
            }
        });
    }

    @Override
    public void onClose() {
        this.trainListeners.values().forEach((listener) -> {
            listener.train.removePropertyChangeListener(listener);
        });
        this.trainListeners.clear();
        InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(trainsListener);
    }

    private class TrainListener implements PropertyChangeListener {

        protected final Train train;

        protected TrainListener(String id) {
            this.train = InstanceManager.getDefault(TrainManager.class).getTrainById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in TrainListener for '{}' '{}' ('{}'=>'{}')", this.train.getId(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGet(TRAIN, this.train.getId(), getLocale()));
                } catch (JsonException ex) {
                    log.warn("json error sending Train: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering trainListener due to IOException");
                this.train.removePropertyChangeListener(this);
                trainListeners.remove(this.train.getId());
            }
        }
    }

    private class TrainsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in TrainsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGetList(TRAINS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToTrains();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Trains: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
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
            this.car = InstanceManager.getDefault(CarManager.class).getById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in CarListener for '{}' '{}' ('{}'=>'{}')", this.car.getId(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGet(CAR, this.car.getId(), getLocale()));
                } catch (JsonException ex) {
                    log.warn("json error sending Car: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering carListener due to IOException");
                this.car.removePropertyChangeListener(this);
                carListeners.remove(this.car.getId());
            }
        }
    }

    private class CarsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in CarsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGetList(CARS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("RollingStockListLength")) { // NOI18N
                        addListenersToCars();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Cars: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
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
            this.location = InstanceManager.getDefault(LocationManager.class).getLocationById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in LocationListener for '{}' '{}' ('{}'=>'{}')", this.location.getId(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            //only send changes to properties that are included in object
            if (evt.getPropertyName().equals(ID) || evt.getPropertyName().equals(LOCATION_NAME) ||
                    evt.getPropertyName().equals(LENGTH) || evt.getPropertyName().equals(LOCATION_COMMENT)) {
                try {
                    try {
                        connection.sendMessage(service.doGet(LOCATION, this.location.getId(), getLocale()));
                        log.debug(" sent Location '{}'", this.location.getId());
                    } catch (JsonException ex) {
                        log.warn("json error sending Location: {}", ex.getJsonMessage());
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    log.debug("deregistering locationListener due to IOException");
                    this.location.removePropertyChangeListener(this);
                    locationListeners.remove(this.location.getId());
                }
            }
        }
    }

    private class LocationsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in LocationsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGetList(LOCATIONS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToLocations();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Locations: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
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
            this.engine = InstanceManager.getDefault(EngineManager.class).getById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in EngineListener for '{}' '{}' ('{}'=>'{}')", this.engine.getId(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGet(ENGINE, this.engine.getId(), getLocale()));
                } catch (JsonException ex) {
                    log.warn("json error sending Engine: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering engineListener due to IOException");
                this.engine.removePropertyChangeListener(this);
                carListeners.remove(this.engine.getId());
            }
        }
    }

    private class EnginesListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in EnginesListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGetList(ENGINES, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("RollingStockListLength")) { // NOI18N
                        addListenersToEngines();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Engines: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering enginesListener due to IOException");
                InstanceManager.getDefault(CarManager.class).removePropertyChangeListener(enginesListener);
            }
        }
    }

}
