package jmri.jmrix.jmriclient.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jmri.Light;
import jmri.implementation.AbstractLight;
import jmri.server.json.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRI JSON Client implementation of a light.
 *
 * @author Randall Wood (C) 2014
 */
public class JsonClientLight extends AbstractLight implements JsonClientListener {

    /**
     *
     */
    private static final long serialVersionUID = -8476285407567592168L;
    private static final Logger log = LoggerFactory.getLogger(JsonClientLight.class);
    private final JsonClientSystemConnectionMemo memo;

    public JsonClientLight(String id, JsonClientSystemConnectionMemo memo) {
        super(memo.getSystemPrefix() + id);
        this.memo = memo;
    }

    @Override
    protected void doNewState(int oldState, int newState) {
        if (oldState != newState) {
            ObjectNode root = this.memo.getTrafficController().mapper.createObjectNode();
            root.put(JSON.TYPE, JSON.LIGHT);
            // TODO: if server indicates that PUT is not valid, use SET
            // TODO: if server indicates that SET is not valid, do nothing
            // TODO: Note that JSON protocol needs to be extended to support this
            root.put(JSON.METHOD, JSON.PUT);
            ObjectNode data = root.putObject(JSON.DATA);
            data.put(JSON.NAME, this.getSystemName());
            data.put(JSON.USERNAME, this.getUserName());
            data.put(JSON.COMMENT, this.getComment());
            switch (newState) {
                case Light.OFF:
                    data.put(JSON.STATE, JSON.OFF);
                    break;
                case Light.ON:
                    data.put(JSON.STATE, JSON.ON);
                    break;
                case Light.UNKNOWN:
                    data.put(JSON.STATE, JSON.UNKNOWN);
                    break;
                default:
                    // if newState is not one of the above, don't bother sending
                    // it; it will only get rejected by the server
                    return;
            }
            this.memo.getTrafficController().sendJsonClientMessage(new JsonClientMessage(root), this);
        }
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
        if (reply.getMessage().path(JSON.TYPE).asText().equals(JSON.LIGHT)) {
            this.reply(reply.getData());
        }
    }

    @Override
    public void reply(JsonNode reply) {
        log.debug("Replied {}", reply.toString());
        int state = reply.path(JSON.STATE).asInt(JSON.UNKNOWN);
        switch (state) {
            case JSON.OFF:
                this.notifyStateChange(this.mState, Light.OFF);
                break;
            case JSON.ON:
                this.notifyStateChange(this.mState, Light.ON);
                break;
            case JSON.UNKNOWN:
                this.notifyStateChange(this.mState, Light.UNKNOWN);
                break;
        }
    }

}
