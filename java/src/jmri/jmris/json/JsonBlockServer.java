package jmri.jmris.json;

import static jmri.jmris.json.JSON.BLOCK;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.PUT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractBlockServer;
import jmri.jmris.JmriConnection;
import jmri.server.json.JsonException;

/**
 * JSON server interface between the JMRI Block manager and a network
 * connection
 *
 * This server sends a message containing the block value whenever a block
 * object that has been previously requested changes. When a client requests or
 * updates a block object, the server replies with all known block object
 * details, but only sends the new block value when sending a status update.
 *
 * @author mstevetodd Copyright (C) 2016 (copied from JsonMemoryServer)
 * @author Randall Wood Copyright (C) 2013
 */
public class JsonBlockServer extends AbstractBlockServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    public JsonBlockServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String blockName, String status) throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getBlock(this.connection.getLocale(), blockName)));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendErrorStatus(String blockName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage(this.connection.getLocale(), "ErrorObject", BLOCK, blockName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putBlock(locale, name, data);
        } else {
            JsonUtil.setBlock(locale, name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getBlock(locale, name)));
        this.addBlockToList(name);
    }
}
