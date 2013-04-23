//SimpleSensorServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Route;
import jmri.jmris.AbstractRouteServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Web Socket interface between the JMRI Sensor manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21313 $
 */
public class JsonRouteServer extends AbstractRouteServer {

    private JmriConnection connection;
    private ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonRouteServer.class);

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
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ROUTE);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, routeName);
        data.put(STATE, status);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String routeName) throws IOException {
        this.sendErrorStatus(routeName, 500);
    }

    private void sendErrorStatus(String routeName, int code) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(NAME, routeName);
        data.put(CODE, code);
        data.put(MESSAGE, Bundle.getMessage("ErrorObject", ROUTE, routeName));
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        this.parseRequest(this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException {
        int state = data.path(STATE).asInt(UNKNOWN);
        String name = data.path(NAME).asText();
        try {
            Route route = InstanceManager.routeManagerInstance().getRoute(name);
            switch (state) {
                case ACTIVE:
                    this.setRoute(name);
                    break;
                default:
                    this.sendStatus(name, route.getTurnoutsAlgdSensor().getKnownState());
                    break;
            }
            this.addRouteToList(name);
        } catch (NullPointerException ex) {
            this.sendErrorStatus(name, 404);
        }
    }
}
