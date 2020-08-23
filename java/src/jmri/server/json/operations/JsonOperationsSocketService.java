package jmri.server.json.operations;

import static jmri.server.json.operations.JsonOperations.CAR;
import static jmri.server.json.operations.JsonOperations.CARS;
import static jmri.server.json.operations.JsonOperations.ENGINE;
import static jmri.server.json.operations.JsonOperations.ENGINES;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.operations.JsonOperations.LOCATIONS;
import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.beans.Identifiable;
import jmri.beans.PropertyChangeProvider;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
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
 * @author Randall Wood (C) 2016, 2019, 2020
 */
public class JsonOperationsSocketService extends JsonSocketService<JsonOperationsHttpService> {

    private final HashMap<String, BeanListener<Car>> carListeners = new HashMap<>();
    private final HashMap<String, BeanListener<Engine>> engineListeners = new HashMap<>();
    private final HashMap<String, BeanListener<Location>> locationListeners = new HashMap<>();
    private final HashMap<String, BeanListener<Train>> trainListeners = new HashMap<>();
    private final CarsListener carsListener = new CarsListener();
    private final EnginesListener enginesListener = new EnginesListener();
    private final LocationsListener locationsListener = new LocationsListener();
    private final TrainsListener trainsListener = new TrainsListener();

    private static final Logger log = LoggerFactory.getLogger(JsonOperationsSocketService.class);

    public JsonOperationsSocketService(JsonConnection connection) {
        this(connection, new JsonOperationsHttpService(connection.getObjectMapper()));
    }

    protected JsonOperationsSocketService(JsonConnection connection, JsonOperationsHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request)
            throws IOException, JmriException, JsonException {
        String name = data.path(JSON.NAME).asText();
        switch (request.method) {
            case JSON.GET:
                connection.sendMessage(service.doGet(type, name, data, request), request.id);
                break;
            case JSON.DELETE:
                service.doDelete(type, name, data, request);
                // remove listener to object being deleted
                switch (type) {
                    case CAR:
                        carListeners.remove(name);
                        break;
                    case ENGINE:
                        engineListeners.remove(name);
                        break;
                    case LOCATION:
                        locationListeners.remove(name);
                        break;
                    case TRAIN:
                        trainListeners.remove(name);
                        break;
                    default:
                        // other types ignored
                        break;
                }
                break;
            case JSON.PUT:
                connection.sendMessage(service.doPut(type, name, data, request), request.id);
                break;
            case JSON.POST:
            default:
                connection.sendMessage(service.doPost(type, name, data, request), request.id);
        }
        // add listener to name if not already listening
        if (!request.method.equals(JSON.DELETE)) {
            if (request.method.equals(JSON.PUT) && name.isEmpty()) {
                // cover situations where object was just created, so client could not specify correct name
                if (CAR.equals(type) || ENGINE.equals(type)) {
                    name = RollingStock.createId(data.path(JSON.ROAD).asText(), data.path(JSON.NUMBER).asText());
                } else if (LOCATION.equals(type)) {
                    name = InstanceManager.getDefault(LocationManager.class).getLocationByName(data.path(JSON.USERNAME).asText()).getId();
                } else {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "ErrorMissingName", request.id);
                }
            }
            switch (type) {
                case CAR:
                    carListeners.computeIfAbsent(name, id -> {
                        CarListener l = new CarListener(id);
                        InstanceManager.getDefault(CarManager.class).getById(id).addPropertyChangeListener(l);
                        return l;
                    });
                    break;
                case ENGINE:
                    engineListeners.computeIfAbsent(name, id -> {
                        EngineListener l = new EngineListener(id);
                        InstanceManager.getDefault(EngineManager.class).getById(id).addPropertyChangeListener(l);
                        return l;
                    });
                    break;
                case LOCATION:
                    locationListeners.computeIfAbsent(name, id -> {
                        LocationListener l = new LocationListener(id);
                        InstanceManager.getDefault(LocationManager.class).getLocationById(id).addPropertyChangeListener(l);
                        return l;
                    });
                    break;
                case TRAIN:
                    trainListeners.computeIfAbsent(name, id -> {
                        TrainListener l = new TrainListener(id);
                        InstanceManager.getDefault(TrainManager.class).getTrainById(id).addPropertyChangeListener(l);
                        return l;
                    });
                    break;
                default:
                    // other types ignored
                    break;
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request)
            throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGetList(type, data, request), request.id);
        switch (type) {
            case CAR:
            case CARS:
                log.debug("adding CarsListener");
                InstanceManager.getDefault(CarManager.class).addPropertyChangeListener(carsListener);
                break;
            case ENGINE:
            case ENGINES:
                log.debug("adding EnginesListener");
                InstanceManager.getDefault(EngineManager.class).addPropertyChangeListener(enginesListener);
                break;
            case LOCATION:
            case LOCATIONS:
                log.debug("adding LocationsListener");
                InstanceManager.getDefault(LocationManager.class).addPropertyChangeListener(locationsListener);
                break;
            case TRAIN:
            case TRAINS:
                log.debug("adding TrainsListener");
                InstanceManager.getDefault(TrainManager.class).addPropertyChangeListener(trainsListener);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClose() {
        carListeners.values().forEach(listener -> listener.bean.removePropertyChangeListener(listener));
        carListeners.clear();
        engineListeners.values().forEach(listener -> listener.bean.removePropertyChangeListener(listener));
        engineListeners.clear();
        locationListeners.values().forEach(listener -> listener.bean.removePropertyChangeListener(listener));
        locationListeners.clear();
        trainListeners.values().forEach(listener -> listener.bean.removePropertyChangeListener(listener));
        trainListeners.clear();
        InstanceManager.getDefault(CarManager.class).removePropertyChangeListener(carsListener);
        InstanceManager.getDefault(EngineManager.class).removePropertyChangeListener(enginesListener);
        InstanceManager.getDefault(LocationManager.class).removePropertyChangeListener(locationsListener);
        InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(trainsListener);
    }

    protected abstract class BeanListener<B extends Identifiable & PropertyChangeProvider> implements PropertyChangeListener {
        
        protected final B bean;
        
        protected BeanListener(@Nonnull B bean) {
            this.bean = bean;
        }
        
        protected void propertyChange(String type, HashMap<String, BeanListener<B>> map) {
            try {
                sendSingleChange(type);
            } catch (IOException ex) {
                // stop listening to this object on error
                bean.removePropertyChangeListener(this);
                map.remove(bean.getId());
            }
        }

        private void sendSingleChange(String type) throws IOException {
            try {
                connection.sendMessage(service.doGet(type, bean.getId(),
                        connection.getObjectMapper().createObjectNode(),
                        new JsonRequest(getLocale(), getVersion(), JSON.GET, 0)), 0);
            } catch (JsonException ex) {
                log.warn("json error sending {}: {}", type, ex.getJsonMessage());
                connection.sendMessage(ex.getJsonMessage(), 0);
            }
        }
    }

    protected abstract class ManagerListener<M extends PropertyChangeProvider> implements PropertyChangeListener {
    
        protected final M manager;
        
        protected ManagerListener(@Nonnull M mgr) {
            Objects.requireNonNull(mgr);
            this.manager = mgr;
        }

        protected void propertyChange(String type) {
            try {
                sendListChange(type);
            } catch (IOException ex) {
                manager.removePropertyChangeListener(this);
            }
        }

        private void sendListChange(String type) throws IOException {
            try {
                connection.sendMessage(service.doGetList(type, service.getObjectMapper().createObjectNode(),
                        new JsonRequest(getLocale(), getVersion(), JSON.GET, 0)), 0);
            } catch (JsonException ex) {
                log.warn("json error sending {}: {}", type, ex.getJsonMessage());
                connection.sendMessage(ex.getJsonMessage(), 0);
            }
        }
    }

    private class CarListener extends BeanListener<Car> {

        protected CarListener(String id) {
            super(InstanceManager.getDefault(CarManager.class).getById(id));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChange(CAR, carListeners);
        }
    }

    private class CarsListener extends ManagerListener<CarManager> {

        protected CarsListener() {
            super(InstanceManager.getDefault(CarManager.class));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChange(CAR);
        }
    }

    private class EngineListener extends BeanListener<Engine> {

        protected EngineListener(String id) {
            super(InstanceManager.getDefault(EngineManager.class).getById(id));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChange(ENGINE, engineListeners);
        }
    }

    private class EnginesListener extends ManagerListener<EngineManager> {

        protected EnginesListener() {
            super(InstanceManager.getDefault(EngineManager.class));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChange(ENGINE);
        }
    }

    private class LocationListener extends BeanListener<Location> {

        protected LocationListener(String id) {
            super(InstanceManager.getDefault(LocationManager.class).getLocationById(id));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChange(LOCATION, locationListeners);
        }
    }

    private class LocationsListener extends ManagerListener<LocationManager> {

        protected LocationsListener() {
            super(InstanceManager.getDefault(LocationManager.class));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChange(LOCATION);
        }
    }

    private class TrainListener extends BeanListener<Train> {

        protected TrainListener(String id) {
            super(InstanceManager.getDefault(TrainManager.class).getTrainById(id));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChange(TRAIN, trainListeners);
        }
    }

    private class TrainsListener extends ManagerListener<TrainManager> {

        protected TrainsListener() {
            super(InstanceManager.getDefault(TrainManager.class));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChange(TRAIN);
        }
    }
}
