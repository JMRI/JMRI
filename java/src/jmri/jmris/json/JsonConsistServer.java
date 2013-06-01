// JsonConsistServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.ConsistListListener;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import jmri.jmrit.consisttool.ConsistFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class JsonConsistServer {

    private JmriConnection connection;
    private ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(JsonConsistServer.class);

    public JsonConsistServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
        InstanceManager.consistManagerInstance().requestUpdateFromLayout();
        try {
            (new ConsistFile()).readFile();
        } catch (Exception e) {
            log.warn("error reading consist file: " + e);
        }
        InstanceManager.consistManagerInstance().addConsistListListener(new JsonConsistListListener());
    }

    public void parseRequest(JsonNode data) throws IOException, JsonException {
        DccLocoAddress address;
        if (data.path(ADDRESS).canConvertToInt()) {
            address = new DccLocoAddress(data.path(ADDRESS).asInt(), data.path(IS_LONG_ADDRESS).asBoolean(false));
        } else {
            address = JsonUtil.addressForString(data.path(ADDRESS).asText());
        }
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putConsist(address, data);
        } else if (data.path(METHOD).asText().equals(DELETE)) {
            JsonUtil.delConsist(address);
        } else {
            JsonUtil.setConsist(address, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getConsist(address)));
        InstanceManager.consistManagerInstance().getConsist(address).addConsistListener(new JsonConsistListener(address));
    }

    private class JsonConsistListListener implements ConsistListListener {

        @Override
        public void notifyConsistListChanged() {
            JsonNode message;
            try {
                message = JsonUtil.getConsists();
            } catch (JsonException ex) {
                message = ex.getJsonMessage();
            }
            try {
                connection.sendMessage(mapper.writeValueAsString(message));
            } catch (IOException ex) {
                InstanceManager.consistManagerInstance().removeConsistListListener(this);
            }
        }
    }

    private class JsonConsistListener implements ConsistListener {

        private DccLocoAddress consistAddress;

        private JsonConsistListener(DccLocoAddress address) {
            this.consistAddress = address;
        }

        @Override
        public void consistReply(DccLocoAddress locoAddress, int status) {
            JsonNode message;
            try {
                message = JsonUtil.getConsist(this.consistAddress);
                if (!locoAddress.equals(this.consistAddress)) {
                    ObjectNode data = (ObjectNode) message.get(DATA);
                    ObjectNode op = data.putObject(STATUS);
                    op.put(ADDRESS, locoAddress.getNumber());
                    op.put(IS_LONG_ADDRESS, locoAddress.isLongAddress());
                    op.put(STATUS, status);
                }
            } catch (JsonException ex) {
                message = ex.getJsonMessage();
            }
            try {
                connection.sendMessage(mapper.writeValueAsString(message));
            } catch (IOException ex) {
                InstanceManager.consistManagerInstance().getConsist(this.consistAddress).removeConsistListener(this);
            }
        }
    }
}
