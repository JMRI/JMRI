package jmri.server.json.trainrecord;

import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.POST;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.TRAIN_RECORD;

import com.fasterxml.jackson.databind.JsonNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;

/**
 * Train records over a JSON socket: a client that asks for a train record,
 * or for the list, is sent the record again each time it changes.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class JsonTrainRecordSocketService extends JsonSocketService<JsonTrainRecordHttpService> {

    private final Map<TrainRecord, PropertyChangeListener> listeners = new HashMap<>();

    /**
     * @param connection the connection to the client
     */
    public JsonTrainRecordSocketService(@Nonnull JsonConnection connection) {
        this(connection, new JsonTrainRecordHttpService(connection.getObjectMapper()));
    }

    /**
     * @param connection the connection to the client
     * @param service    the service answering requests
     */
    protected JsonTrainRecordSocketService(@Nonnull JsonConnection connection,
            @Nonnull JsonTrainRecordHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JsonException {
        String name = data.path(NAME).asText();
        JsonNode reply = POST.equals(request.method)
                ? service.doPost(type, name, data, request)
                : service.doGet(type, name, data, request);
        listen(InstanceManager.getDefault(TrainRecordManager.class).getByName(name));
        connection.sendMessage(reply, request.id);
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JsonException {
        for (TrainRecord r : InstanceManager.getDefault(TrainRecordManager.class).getRecords()) {
            listen(r);
        }
        connection.sendMessage(service.doGetList(type, data, request), request.id);
    }

    @Override
    public void onClose() {
        listeners.forEach((r, l) -> r.removePropertyChangeListener(l));
        listeners.clear();
    }

    private void listen(TrainRecord record) {
        if (record != null && !listeners.containsKey(record)) {
            PropertyChangeListener l = evt -> changed(record, evt);
            listeners.put(record, l);
            record.addPropertyChangeListener(l);
        }
    }

    private void changed(TrainRecord record, PropertyChangeEvent evt) {
        try {
            try {
                connection.sendMessage(service.doGet(TRAIN_RECORD, record.getName(),
                        connection.getObjectMapper().createObjectNode(),
                        new JsonRequest(connection.getLocale(), connection.getVersion(), GET, 0)), 0);
            } catch (JsonException ex) {
                // the record has gone or been renamed: stop sending it
                record.removePropertyChangeListener(listeners.remove(record));
                connection.sendMessage(ex.getJsonMessage(), 0);
            }
        } catch (IOException ex) {
            // the client has gone; the connection closes the service
            log.debug("could not send train record {}: {}", record.getName(), ex.getMessage());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonTrainRecordSocketService.class);
}
