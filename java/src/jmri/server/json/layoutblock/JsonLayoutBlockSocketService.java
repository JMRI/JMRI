package jmri.server.json.layoutblock;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCK;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCKS;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
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
public class JsonLayoutBlockSocketService extends JsonSocketService<JsonLayoutBlockHttpService> {

    private final HashMap<String, LayoutBlockListener> layoutBlockListeners = new HashMap<>();
    private final LayoutBlocksListener layoutBlocksListener = new LayoutBlocksListener();
    private static final Logger log = LoggerFactory.getLogger(JsonLayoutBlockServiceFactory.class);

    public JsonLayoutBlockSocketService(JsonConnection connection) {
        super(connection, new JsonLayoutBlockHttpService(connection.getObjectMapper()));
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
        if (!this.layoutBlockListeners.containsKey(name)) {
            LayoutBlock layoutblock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
            if (layoutblock != null) {
                LayoutBlockListener listener = new LayoutBlockListener(layoutblock);
                layoutblock.addPropertyChangeListener(listener);
                this.layoutBlockListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding LayoutBlocksListener");
        InstanceManager.getDefault(LayoutBlockManager.class).addPropertyChangeListener(layoutBlocksListener); //add parent listener
        addListenersToChildren();        
    }

    private void addListenersToChildren() {
        InstanceManager.getDefault(LayoutBlockManager.class).getSystemNameList().stream().forEach((lbn) -> { //add listeners to each child (if not already)
            if (!layoutBlockListeners.containsKey(lbn)) {
                log.debug("adding LayoutBlockListener for LayoutBlock '{}'", lbn);
                LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(lbn);
                if (lb != null) {
                    layoutBlockListeners.put(lbn, new LayoutBlockListener(lb));
                    lb.addPropertyChangeListener(this.layoutBlockListeners.get(lbn));
                }
            }
        });
    }    

    @Override
    public void onClose() {
        layoutBlockListeners.values().stream().forEach((layoutblock) -> {
            layoutblock.layoutBlock.removePropertyChangeListener(layoutblock);
        });
        layoutBlockListeners.clear();
    }

    private class LayoutBlockListener implements PropertyChangeListener {

        protected final LayoutBlock layoutBlock;

        public LayoutBlockListener(LayoutBlock layoutblock) {
            this.layoutBlock = layoutblock;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("redraw")) {
                log.debug("{} property '{}' changed from '{}' to '{}'", this.layoutBlock.getUserName(),
                        e.getPropertyName(), e.getOldValue(), e.getNewValue());
                try {
                    try {
                        connection.sendMessage(service.doGet(LAYOUTBLOCK, this.layoutBlock.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    layoutBlock.removePropertyChangeListener(this);
                    layoutBlockListeners.remove(this.layoutBlock.getSystemName());
                }
            }
        }
    }

    private class LayoutBlocksListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in LayoutBlocksListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(LAYOUTBLOCKS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending LayoutBlocks: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering layoutBlocksListener due to IOException");
                InstanceManager.getDefault(LayoutBlockManager.class).removePropertyChangeListener(layoutBlocksListener);
            }
        }
    }

}
