package jmri.jmrix.jmriclient.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.beans.Bean;
import jmri.jmris.json.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class JsonClientPowerManager extends Bean implements PowerManager, JsonClientListener {

    private final JsonClientSystemConnectionMemo memo;
    private final JsonClientTrafficController trafficController;
    private int power = PowerManager.UNKNOWN;
    private final static Logger log = LoggerFactory.getLogger(JsonClientPowerManager.class);

    public JsonClientPowerManager(JsonClientSystemConnectionMemo memo) {
        this.memo = memo;
        this.trafficController = memo.getTrafficController();
    }

    @Override
    public void setPower(int v) throws JmriException {
        this.power = PowerManager.UNKNOWN; // while waiting for reply
        ObjectNode root = this.trafficController.mapper.createObjectNode();
        root.put(JSON.TYPE, JSON.POWER);
        ObjectNode data = root.putObject(JSON.DATA);
        switch (v) {
            case PowerManager.OFF:
                data.put(JSON.STATE, JSON.OFF);
                break;
            case PowerManager.ON:
                data.put(JSON.STATE, JSON.ON);
                break;
            default:
                data.put(JSON.STATE, JSON.UNKNOWN);
                break;
        }
        this.trafficController.sendJsonClientMessage(new JsonClientMessage(root), this);
        this.firePropertyChange(PowerManager.POWER, null, null);
    }

    @Override
    public int getPower() throws JmriException {
        return this.power;
    }

    @Override
    public void dispose() throws JmriException {
        this.trafficController.removeJsonClientListener(this);
    }

    @Override
    public String getUserName() {
        return this.memo.getUserName();
    }

    @Override
    public void message(JsonClientMessage message) {
        // do nothing
    }

    @Override
    public void message(JsonNode message) {
        // do nothing
    }

    @Override
    public void reply(JsonClientReply reply) {
        if (reply.getMessage().path(JSON.TYPE).asText().equals(JSON.POWER)) {
            this.reply(reply.getData());
        }
    }

    @Override
    public void reply(JsonNode reply) {
        log.debug("Replied {}", reply.toString());
        int state = reply.path(JSON.STATE).asInt(JSON.UNKNOWN);
        switch (state) {
            case JSON.OFF:
                this.power = PowerManager.OFF;
                break;
            case JSON.ON:
                this.power = PowerManager.ON;
                break;
            case JSON.UNKNOWN:
                this.power = PowerManager.UNKNOWN;
                break;
        }
        this.firePropertyChange(PowerManager.POWER, null, null);
    }

}
