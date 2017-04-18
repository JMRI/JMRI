package jmri.server.json.operations;

import static jmri.server.json.operations.JsonOperations.TRAIN;

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

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonOperationsSocketService extends JsonSocketService {

    private Locale locale;
    private final JsonOperationsHttpService service;
    private final HashMap<String, TrainListener> trains = new HashMap<>();

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
                if (!this.trains.containsKey(id)) {
                    this.trains.put(id, new TrainListener(id));
                    TrainManager.instance().getTrainById(id).addPropertyChangeListener(this.trains.get(id));
                }
                //$FALL-THROUGH$
            default:
                this.connection.sendMessage(this.service.doPost(type, id, data, locale));
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        this.trains.values().forEach((listener) -> {
            listener.train.removePropertyChangeListener(listener);
        });
        this.trains.clear();
    }

    private class TrainListener implements PropertyChangeListener {

        protected final Train train;

        protected TrainListener(String id) {
            this.train = TrainManager.instance().getTrainById(id);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)
                    || e.getPropertyName().equals(Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY)) {
                try {
                    try {
                        connection.sendMessage(service.doGet(TRAIN, this.train.getId(), locale));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    this.train.removePropertyChangeListener(this);
                    trains.remove(this.train.getId());
                }
            }
        }
    }

}
