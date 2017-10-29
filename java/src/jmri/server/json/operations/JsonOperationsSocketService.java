package jmri.server.json.operations;

import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.JmriException;
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
public class JsonOperationsSocketService extends JsonSocketService {

    private Locale locale;
    private final JsonOperationsHttpService service;
    private final HashMap<String, TrainListener> trainListeners = new HashMap<>();
    private final TrainsListener trainsListener = new TrainsListener();
    private final static Logger log = LoggerFactory.getLogger(JsonOperationsSocketService.class);

    public JsonOperationsSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonOperationsHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        String id = data.path(JSON.ID).asText(); // Operations uses ID attribute instead of name attribute
        switch (type) {
            case TRAIN:
                this.connection.sendMessage(this.service.doPost(type, id, data, locale));
                if (!this.trainListeners.containsKey(id)) {
                    this.trainListeners.put(id, new TrainListener(id));
                    TrainManager.instance().getTrainById(id).addPropertyChangeListener(this.trainListeners.get(id));
                }
                break;
            default:
                // other types get no special handling
                break;
        }
        this.connection.sendMessage(this.service.doPost(type, id, data, locale));
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding TrainsListener");
        TrainManager.instance().addPropertyChangeListener(trainsListener); //add parent listener
        addListenersToChildren();
    }

    private void addListenersToChildren() {
        TrainManager.instance().getTrainsByIdList().stream().forEach((t) -> { //add listeners to each child (if not already)
            if (!trainListeners.containsKey(t.getId())) {
                log.debug("adding TrainListener for Train ID {}", t.getId());
                trainListeners.put(t.getId(), new TrainListener(t.getId()));
                t.addPropertyChangeListener(this.trainListeners.get(t.getId()));
            }
        });
    }

    @Override
    public void onClose() {
        this.trainListeners.values().forEach((listener) -> {
            listener.train.removePropertyChangeListener(listener);
        });
        this.trainListeners.clear();
        TrainManager.instance().removePropertyChangeListener(trainsListener);
    }

    private class TrainListener implements PropertyChangeListener {

        protected final Train train;

        protected TrainListener(String id) {
            this.train = TrainManager.instance().getTrainById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in SensorListener for '{}' '{}' ('{}'=>'{}')", this.train.getId(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGet(TRAIN, this.train.getId(), locale));
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
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
                    connection.sendMessage(service.doGetList(TRAINS, locale));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Trains: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering trainsListener due to IOException");
                TrainManager.instance().removePropertyChangeListener(trainsListener);
            }
        }
    }

}
