//SimpleSensorServer.java
package jmri.jmris.json;

import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.ROUTE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractRouteServer;
import jmri.jmris.JmriConnection;

/**
 * JSON Web Socket interface between the JMRI Sensor manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21313 $
 */
public class JsonRouteServer extends AbstractRouteServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    public JsonRouteServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String routeName, int status) throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getRoute(this.connection.getLocale(), routeName)));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendErrorStatus(String routeName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage(this.connection.getLocale(), "ErrorObject", ROUTE, routeName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        JsonUtil.setRoute(locale, name, data);
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getRoute(locale, name)));
        this.addRouteToList(name);
    }
}
