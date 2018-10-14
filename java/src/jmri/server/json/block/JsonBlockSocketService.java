package jmri.server.json.block;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.block.JsonBlock.BLOCK;
import static jmri.server.json.block.JsonBlock.BLOCKS;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mstevetodd Copyright (C) 2018 (copied from JsonMemorySocketService)
 * @author Randall Wood
 */
public class JsonBlockSocketService extends JsonSocketService<JsonBlockHttpService> {

    protected final HashMap<String, BlockListener> blockListeners = new HashMap<>();
    private final BlocksListener blocksListener = new BlocksListener();
    private final static Logger log = LoggerFactory.getLogger(JsonBlockSocketService.class);


    public JsonBlockSocketService(JsonConnection connection) {
        super(connection, new JsonBlockHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        String name = data.path(NAME).asText();
        if (method.equals(PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.blockListeners.containsKey(name)) {
            Block block = InstanceManager.getDefault(BlockManager.class).getBlock(name);
            if (block != null) {
                BlockListener listener = new BlockListener(block);
                block.addPropertyChangeListener(listener);
                this.blockListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding BlocksListener");
        InstanceManager.getDefault(BlockManager.class).addPropertyChangeListener(blocksListener); //add parent listener
        addListenersToChildren();
    }

    private void addListenersToChildren() {
        InstanceManager.getDefault(BlockManager.class).getSystemNameList().stream().forEach((bn) -> { //add listeners to each child (if not already)
            if (!blockListeners.containsKey(bn)) {
                log.debug("adding BlockListener for Block '{}'", bn);
                Block b = InstanceManager.getDefault(BlockManager.class).getBlock(bn);
                if (b != null) {
                    blockListeners.put(bn, new BlockListener(b));
                    b.addPropertyChangeListener(this.blockListeners.get(bn));
                }
            }
        });
    }    

    @Override
    public void onClose() {
        blockListeners.values().stream().forEach((block) -> {
            block.block.removePropertyChangeListener(block);
        });
        blockListeners.clear();
    }

    private class BlockListener implements PropertyChangeListener {

        protected final Block block;

        public BlockListener(Block block) {
            this.block = block;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
//            if (!e.getPropertyName().equals("allocated")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(BLOCK, this.block.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    block.removePropertyChangeListener(this);
                    blockListeners.remove(this.block.getSystemName());
                }
//            }
        }
    }
    
    private class BlocksListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in BlocksListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(BLOCKS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Blocks: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering blocksListener due to IOException");
                InstanceManager.getDefault(BlockManager.class).removePropertyChangeListener(blocksListener);
            }
        }
    }

}
