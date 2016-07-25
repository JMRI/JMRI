package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractLightServer;
import jmri.jmris.JmriConnection;
import jmri.server.json.JsonException;
import static jmri.server.json.JSON.LIGHT;
import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;

/**
 * JSON Server interface between the JMRI light manager and a network connection
 *
 * This server sends a message containing the light state whenever a light that
 * has been previously requested is open or thrown. When a client requests or
 * updates a light, the server replies with all known light details, but only
 * sends the new light state when sending a status update.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2012, 2013
 * @deprecated Since 4.3.6; replaced with {@link jmri.server.json.light.JsonLightSocketService}
 */
@Deprecated
public class JsonLightServer extends AbstractLightServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    public JsonLightServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String lightName, int status) throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getLight(this.connection.getLocale(), lightName)));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendErrorStatus(String lightName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage(this.connection.getLocale(), "ErrorObject", LIGHT, lightName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putLight(locale, name, data);
        } else {
            JsonUtil.setLight(locale, name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getLight(locale, name)));
        this.addLightToList(name);
    }
}
