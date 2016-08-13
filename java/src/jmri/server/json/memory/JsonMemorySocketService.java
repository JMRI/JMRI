package jmri.server.json.memory;

import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.memory.JsonMemoryServiceFactory.MEMORY;

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

/**
 *
 * @author Randall Wood
 */
public class JsonMemorySocketService extends JsonSocketService {

    private final JsonMemoryHttpService service;
    private final HashMap<String, MemoryListener> memories = new HashMap<>();
    private Locale locale;

    public JsonMemorySocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonMemoryHttpService(connection.getObjectMapper());
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
        if (!this.memories.containsKey(name)) {
            Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(name);
            if (memory != null) {
                MemoryListener listener = new MemoryListener(memory);
                memory.addPropertyChangeListener(listener);
                this.memories.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        memories.values().stream().forEach((memory) -> {
            memory.memory.removePropertyChangeListener(memory);
        });
        memories.clear();
    }

    private class MemoryListener implements PropertyChangeListener {

        protected final Memory memory;

        public MemoryListener(Memory memory) {
            this.memory = memory;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("value")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(MEMORY, this.memory.getSystemName(), locale));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    memory.removePropertyChangeListener(this);
                    memories.remove(this.memory.getSystemName());
                }
            }
        }
    }

}
