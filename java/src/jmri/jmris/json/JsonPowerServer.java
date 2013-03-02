package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmris.AbstractPowerServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;

/**
 * Send power status over WebSockets as a JSON String
 *
 * This server creates a JSON string for a Power object with an integer value
 * for the Power state. These states are identical to the
 * {@link jmri.PowerManager} power states, with the addition of -1 as an error
 * indicator.
 *
 * The JSON string is in the form:
 * <code>{type:'power', data:{state:&lt;integer&gt;}}</code>
 *
 * @author rhwood
 */
public class JsonPowerServer extends AbstractPowerServer {

    private JmriConnection connection;
    private ObjectMapper mapper;

    public JsonPowerServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
        mgrOK();
    }

    @Override
    public void sendStatus(int status) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, POWER);
        ObjectNode data = root.putObject(DATA);
        data.put(STATE, status);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus() throws IOException {
        this.sendErrorStatus(500);
    }

    public void sendErrorStatus(int status) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(CODE, status);
        data.put(MESSAGE, Bundle.getMessage("ErrorPower"));
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        this.parseRequest(this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException {
        switch (data.path(STATE).asInt(PowerManager.UNKNOWN)) {
            case PowerManager.OFF:
                this.setOffStatus();
                break;
            case PowerManager.ON:
                this.setOnStatus();
                break;
            case PowerManager.UNKNOWN:
                break;
            default:
                this.sendErrorStatus(400);
                break;
        }
        this.sendStatus(InstanceManager.powerManagerInstance().getPower());
    }
}
