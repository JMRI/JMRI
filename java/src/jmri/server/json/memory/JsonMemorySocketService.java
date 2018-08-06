package jmri.server.json.memory;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.memory.JsonMemory.MEMORIES;
import static jmri.server.json.memory.JsonMemory.MEMORY;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonMemorySocketService extends JsonSocketService<JsonMemoryHttpService> {

    private final HashMap<String, MemoryListener> memoryListeners = new HashMap<>();
    private final MemoriesListener memoriesListener = new MemoriesListener();
    private final static Logger log = LoggerFactory.getLogger(JsonMemorySocketService.class);

    public JsonMemorySocketService(JsonConnection connection) {
        super(connection,new JsonMemoryHttpService(connection.getObjectMapper()));
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
        if (!this.memoryListeners.containsKey(name)) {
            Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(name);
            if (memory != null) {
                MemoryListener listener = new MemoryListener(memory);
                memory.addPropertyChangeListener(listener);
                this.memoryListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding MemoriesListener");
        InstanceManager.getDefault(MemoryManager.class).addPropertyChangeListener(memoriesListener); //add parent listener
        addListenersToChildren();
    }
    
    private void addListenersToChildren() {
        InstanceManager.getDefault(MemoryManager.class).getSystemNameList().stream().forEach((mn) -> { //add listeners to each child (if not already)
            if (!memoryListeners.containsKey(mn)) {
                log.debug("adding MemoryListener for Memory '{}'", mn);
                Memory m = InstanceManager.getDefault(MemoryManager.class).getMemory(mn);
                if (m != null) {
                    memoryListeners.put(mn, new MemoryListener(m));
                    m.addPropertyChangeListener(this.memoryListeners.get(mn));
                }
            }
        });
    }

    @Override
    public void onClose() {
        memoryListeners.values().stream().forEach((memory) -> {
            memory.memory.removePropertyChangeListener(memory);
        });
        memoryListeners.clear();
    }

    private class MemoryListener implements PropertyChangeListener {

        protected final Memory memory;

        public MemoryListener(Memory memory) {
            this.memory = memory;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in MemoryListener for '{}' '{}' ('{}'=>'{}')", this.memory.getSystemName(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                try {
                    try {
                        connection.sendMessage(service.doGet(MEMORY, this.memory.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    memory.removePropertyChangeListener(this);
                    memoryListeners.remove(this.memory.getSystemName());
                }
//            }
        }
    }
    private class MemoriesListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in MemoriesListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(MEMORIES, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Memories: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering memoriesListener due to IOException");
                InstanceManager.getDefault(MemoryManager.class).removePropertyChangeListener(memoriesListener);
            }
        }
    }


}
