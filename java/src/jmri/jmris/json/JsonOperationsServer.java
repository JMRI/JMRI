package jmri.jmris.json;

import static jmri.jmris.json.JSON.CODE;
import static jmri.jmris.json.JSON.ERROR;
import static jmri.jmris.json.JSON.ID;
import static jmri.jmris.json.JSON.MESSAGE;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.TYPE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import javax.management.Attribute;
import jmri.JmriException;
import jmri.jmris.AbstractOperationsServer;
import jmri.jmris.JmriConnection;
import jmri.jmrit.operations.trains.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple interface between the JMRI operations and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Dan Boudreau Copyright (C) 2012 (Documented the code, changed reply
 * format, and some minor refactoring)
 */
public class JsonOperationsServer extends AbstractOperationsServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(JsonOperationsServer.class);

    public JsonOperationsServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /**
     * Overridden method to do nothing.
     *
     */
    @Override
    public void sendMessage(ArrayList<Attribute> contents) throws IOException {
        // Do nothing. This should never be called in the JSON context.
    }

    /**
     * Constructs an error message and sends it to the client as a JSON message
     *
     * @param errorStatus is the error message. It need not include any padding
     *                    - this method will add it. It should be plain text.
     * @throws IOException if there is a problem sending the error message
     */
    @Override
    public void sendErrorStatus(String errorStatus) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(CODE, -1);
        data.put(MESSAGE, errorStatus);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    /**
     * Overridden method to do nothing.
     *
     */
    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        // Do nothing. This should never be called in the JSON context.
    }

    @Override
    public void sendTrainList() {
        try {
            try {
                this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTrains(this.connection.getLocale())));
            } catch (JsonException ex) {
                this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
            }
        } catch (IOException ex) {
            try {
                this.connection.close();
            } catch (IOException e1) {
                log.warn("Unable to close connection.", e1);
            }
            log.warn("Unable to send message, closing connection.", ex);
        }
    }

    @Override
    public void sendLocationList() {
        try {
            try {
                this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getLocations(this.connection.getLocale())));
            } catch (JsonException ex) {
                this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
            }
        } catch (IOException ex) {
            try {
                this.connection.close();
            } catch (IOException e1) {
                log.warn("Unable to close connection.", e1);
            }
            log.warn("Unable to send message, closing connection.", ex);
        }
    }

    @Override
    public void sendFullStatus(Train train) throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTrain(this.connection.getLocale(), train.getId())));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        log.debug("property change: {} old: {} new: {}", e.getPropertyName(), e.getOldValue(), e.getNewValue());
        if (e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY)) {
            try {
                sendFullStatus((Train) e.getSource());
            } catch (IOException e1) {
                log.error(e1.getLocalizedMessage(), e1);
            }
        }
    }

    public void parseTrainRequest(Locale locale, JsonNode data) throws IOException, JsonException {
        String id = data.path(ID).asText();
        if (!data.path(METHOD).isMissingNode()) {
            JsonUtil.setTrain(locale, id, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTrain(locale, id)));
        this.addTrainToList(id);
    }
}
