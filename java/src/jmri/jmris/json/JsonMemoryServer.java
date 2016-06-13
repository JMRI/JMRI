//JsonMemoryServer.java
package jmri.jmris.json;

import static jmri.jmris.json.JSON.MEMORY;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.PUT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractMemoryServer;
import jmri.jmris.JmriConnection;

/**
 * JSON server interface between the JMRI Memory manager and a network
 * connection
 *
 * This server sends a message containing the memory value whenever a memory
 * object that has been previously requested changes. When a client requests or
 * updates a memory object, the server replies with all known memory object
 * details, but only sends the new memory value when sending a status update.
 *
 * @author mstevetodd Copyright (C) 2012 (copied from JsonSensorServer)
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision: $
 */
public class JsonMemoryServer extends AbstractMemoryServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    public JsonMemoryServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String memoryName, String status) throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getMemory(this.connection.getLocale(), memoryName)));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendErrorStatus(String memoryName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage(this.connection.getLocale(), "ErrorObject", MEMORY, memoryName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putMemory(locale, name, data);
        } else {
            JsonUtil.setMemory(locale, name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getMemory(locale, name)));
        this.addMemoryToList(name);
    }
}
