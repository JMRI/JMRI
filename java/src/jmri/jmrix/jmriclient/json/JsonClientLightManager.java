package jmri.jmrix.jmriclient.json;

import com.fasterxml.jackson.databind.JsonNode;
import jmri.Light;
import jmri.server.json.JSON;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood Copyright (C) 2014
 */
public class JsonClientLightManager extends AbstractLightManager implements JsonClientListener {

    /**
     *
     */
    private static final long serialVersionUID = -5468040873828297534L;

    private final JsonClientSystemConnectionMemo memo;

    private final static Logger log = LoggerFactory.getLogger(JsonClientLightManager.class);

    public JsonClientLightManager(JsonClientSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    protected Light createNewLight(String systemName, String userName) {
        JsonClientLight light;
        light = new JsonClientLight(systemName, memo);
        this.memo.getTrafficController().addJsonClientListener(light);
        light.setUserName(userName);
        return light;
    }

    @Override
    public String getSystemPrefix() {
        return this.memo.getSystemPrefix();
    }

    @Override
    public boolean validSystemNameFormat(String systemName) {
        return (systemName.startsWith(this.getSystemPrefix())
                && systemName.substring(this.getSystemPrefix().length(), this.getSystemPrefix().length() + 1).equalsIgnoreCase("L")
                && Integer.parseInt(systemName.substring(this.getSystemPrefix().length() + 1)) > 0);
    }

    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (systemName.equals(this.memo.getNodeIdentity())
                || systemName.equals(this.memo.getSystemPrefix()));
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
    public void reply(JsonClientReply m) {
        log.debug("Got reply {}", m.getMessage());
        if (m.getMessage().isArray()) {
            for (JsonNode node : m.getMessage()) {
                this.reply(node);
            }
        }
    }

    @Override
    public void reply(JsonNode reply) {
        log.debug("Got reply {}", reply);
        if (reply.path(JSON.TYPE).asText().equals(JSON.LIGHT)) {
            JsonClientLight light = (JsonClientLight) this.getInstanceBySystemName(reply.path(JSON.NAME).asText());
            if (light == null) {
                light = (JsonClientLight) this.createNewLight(reply.path(JSON.NAME).asText(), reply.path(JSON.USERNAME).asText());
                light.setComment(reply.path(JSON.COMMENT).asText());
            }
            light.reply(reply.path(JSON.DATA));
        }
    }

}
