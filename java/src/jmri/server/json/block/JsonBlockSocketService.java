package jmri.server.json.block;

import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.block.JsonBlockServiceFactory.BLOCK;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author mstevetodd Copyright (C) 2016 (copied from JsonMemorySocketService)
 * @author Randall Wood
 */
public class JsonBlockSocketService extends JsonSocketService {

    private final JsonBlockHttpService service;
    private final HashMap<String, BlockListener> blocks = new HashMap<>();
    private Locale locale;

    public JsonBlockSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonBlockHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.blocks.containsKey(name)) {
            Block block = InstanceManager.getDefault(BlockManager.class).getBlock(name);
            BlockListener listener = new BlockListener(block);
            block.addPropertyChangeListener(listener);
            this.blocks.put(name, listener);
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        blocks.values().stream().forEach((block) -> {
            block.block.removePropertyChangeListener(block);
        });
        blocks.clear();
    }

    private class BlockListener implements PropertyChangeListener {

        protected final Block block;

        public BlockListener(Block block) {
            this.block = block;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("value")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(BLOCK, this.block.getSystemName(), locale));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    block.removePropertyChangeListener(this);
                    blocks.remove(this.block.getSystemName());
                }
            }
        }
    }

}
