//AbstractMemoryServer.java
package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between a JMRI memory and a network connection
 *
 * @author mstevetodd Copyright (C) 2012 (copied from AbstractSensorServer)
 * @author Randall Wood Copyright (C) 2013, 2014
 * @version $Revision: $
 */
abstract public class AbstractMemoryServer {

    private final HashMap<String, MemoryListener> memories;
    private static final Logger log = LoggerFactory.getLogger(AbstractMemoryServer.class);

    public AbstractMemoryServer() {
        memories = new HashMap<String, MemoryListener>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String memory, String Status) throws IOException;

    abstract public void sendErrorStatus(String memory) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addMemoryToList(String memoryName) {
        if (!memories.containsKey(memoryName)) {
            memories.put(memoryName, new MemoryListener(memoryName));
            InstanceManager.memoryManagerInstance().getMemory(memoryName).addPropertyChangeListener(memories.get(memoryName));
        }
    }

    synchronized protected void removeMemoryFromList(String memoryName) {
        if (memories.containsKey(memoryName)) {
            InstanceManager.memoryManagerInstance().getMemory(memoryName).removePropertyChangeListener(memories.get(memoryName));
            memories.remove(memoryName);
        }
    }

    public Memory initMemory(String memoryName) {
        Memory memory = InstanceManager.memoryManagerInstance().provideMemory(memoryName);
        this.addMemoryToList(memoryName);
        return memory;
    }

    public void setMemoryValue(String memoryName, String memoryValue) {
        Memory memory;
        try {
            addMemoryToList(memoryName);
            memory = InstanceManager.memoryManagerInstance().getMemory(memoryName);
            if (memory == null) {
                log.error("Memory {} is not available", memoryName);
            } else {
                if (!(memory.getValue().equals(memoryValue))) {
                    memory.setValue(memoryValue);
                } else {
                    try {
                        sendStatus(memoryName, memoryValue);
                    } catch (IOException ex) {
                        log.error("Error sending appearance", ex);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("error setting memory value", ex);
        }
    }

    public void dispose() {
        for (Map.Entry<String, MemoryListener> memory : this.memories.entrySet()) {
            InstanceManager.memoryManagerInstance().getMemory(memory.getKey()).removePropertyChangeListener(memory.getValue());
        }
        this.memories.clear();
    }

    class MemoryListener implements PropertyChangeListener {

        MemoryListener(String memoryName) {
            name = memoryName;
            memory = InstanceManager.memoryManagerInstance().getMemory(memoryName);
        }

        // update state as state of memory changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("value")) {
                String state = (String) e.getNewValue();
                try {
                    sendStatus(name, state);
                } catch (IOException ie) {
                    log.debug("Error sending status, removing listener from memory {}", name);
                    // if we get an error, de-register
                    memory.removePropertyChangeListener(this);
                    removeMemoryFromList(name);
                }
            }
        }
        String name = null;
        Memory memory = null;
    }
}
