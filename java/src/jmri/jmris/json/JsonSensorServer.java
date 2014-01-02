//JsonSensorServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jmri.JmriException;
import jmri.jmris.AbstractSensorServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.PUT;
import static jmri.jmris.json.JSON.SENSOR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @version $Revision: 21313 $
 */
public class JsonSensorServer extends AbstractSensorServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonSensorServer.class);

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
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getSensor(sensorName)));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendErrorStatus(String sensorName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage("ErrorObject", SENSOR, sensorName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putSensor(name, data);
        } else {
            JsonUtil.setSensor(name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getSensor(name)));
        this.addSensorToList(name);
    }
}
