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
import java.util.HashSet;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mstevetodd Copyright (C) 2018
 * @author Randall Wood
 */
public class JsonLayoutBlockSocketService extends JsonSocketService<JsonLayoutBlockHttpService> {

    private final HashMap<LayoutBlock, LayoutBlockListener> layoutBlockListeners = new HashMap<>();
    private final LayoutBlocksListener layoutBlocksListener = new LayoutBlocksListener();
    private static final Logger log = LoggerFactory.getLogger(JsonLayoutBlockSocketService.class);

    public JsonLayoutBlockSocketService(JsonConnection connection) {
        super(connection, new JsonLayoutBlockHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        String name = data.path(NAME).asText();
        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
        if (!request.method.equals(PUT) && layoutBlock != null && !layoutBlock.getSystemName().equals(name)) {
            name = layoutBlock.getSystemName();
        }
        switch (request.method) {
            case JSON.DELETE:
                service.doDelete(type, name, data, request);
                break;
            case JSON.POST:
                connection.sendMessage(service.doPost(type, name, data, request), request.id);
                break;
            case JSON.PUT:
                connection.sendMessage(service.doPut(type, name, data, request), request.id);
                break;
            default:
            case JSON.GET:
                connection.sendMessage(service.doGet(type, name, data, request), request.id);
                break;
        }
        layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
        if (layoutBlock != null && !layoutBlockListeners.containsKey(layoutBlock)) {
                LayoutBlockListener listener = new LayoutBlockListener(layoutBlock);
                layoutBlock.addPropertyChangeListener(listener);
                layoutBlockListeners.put(layoutBlock, listener);
        }
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGetList(type, data, request), request.id);
        log.debug("adding LayoutBlocksListener");
        InstanceManager.getDefault(LayoutBlockManager.class).addPropertyChangeListener(layoutBlocksListener); //add parent listener
    }

    @Override
    public void onClose() {
        layoutBlockListeners.values().stream().forEach(layoutblock ->
            layoutblock.layoutBlock.removePropertyChangeListener(layoutblock));
        layoutBlockListeners.clear();
    }

    private class LayoutBlockListener implements PropertyChangeListener {

        protected final LayoutBlock layoutBlock;

        public LayoutBlockListener(LayoutBlock layoutblock) {
            layoutBlock = layoutblock;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("redraw")) {
                log.debug("{} property '{}' changed from '{}' to '{}'", layoutBlock.getUserName(),
                        e.getPropertyName(), e.getOldValue(), e.getNewValue());
                try {
                    try {
                        connection.sendMessage(service.doGet(LAYOUTBLOCK, layoutBlock.getSystemName(), connection.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), JSON.GET, 0)), 0);
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage(), 0);
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    layoutBlock.removePropertyChangeListener(this);
                    layoutBlockListeners.remove(layoutBlock);
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
                    connection.sendMessage(service.doGetList(LAYOUTBLOCKS, service.getObjectMapper().createObjectNode(), new JsonRequest(getLocale(), getVersion(), JSON.GET, 0)), 0);
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        removeListenersFromRemovedBeans();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending LayoutBlocks: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage(), 0);
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering layoutBlocksListener due to IOException");
                InstanceManager.getDefault(LayoutBlockManager.class).removePropertyChangeListener(layoutBlocksListener);
            }
        }

        private void removeListenersFromRemovedBeans() {
            for (LayoutBlock layoutBlock : new HashSet<>(layoutBlockListeners.keySet())) {
                if (InstanceManager.getDefault(LayoutBlockManager.class).getBySystemName(layoutBlock.getSystemName()) == null) {
                    layoutBlockListeners.remove(layoutBlock);
                }
            }
        }
    }

}
