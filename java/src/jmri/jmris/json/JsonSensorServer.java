package jmri.jmris.json;

import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.JSON.SENSOR;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractSensorServer;
import jmri.jmris.JmriConnection;
import jmri.server.json.JsonException;

/**
 * JSON Web Socket interface between the JMRI Sensor manager and a network
 * connection
 *
 * This server sends a message containing the sensor state whenever a sensor
 * that has been previously requested is open or thrown. When a client requests
 * or updates a sensor, the server replies with all known sensor details, but
 * only sends the new sensor state when sending a status update.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2013
 * @deprecated Use {@link jmri.server.json.sensor.JsonSensorSocketService} instead.
 */
public class JsonSensorServer extends AbstractSensorServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    public JsonSensorServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String sensorName, int status) throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getSensor(this.connection.getLocale(), sensorName)));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendErrorStatus(String sensorName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage(this.connection.getLocale(), "ErrorObject", SENSOR, sensorName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putSensor(locale, name, data);
        } else {
            JsonUtil.setSensor(locale, name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getSensor(locale, name)));
        this.addSensorToList(name);
    }
}
