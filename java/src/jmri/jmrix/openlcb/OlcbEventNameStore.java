package jmri.jmrix.openlcb;

import java.util.HashMap;
import java.util.Map;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.jmrix.openlcb.configurexml.OlcbEventNameStoreXml;

import org.openlcb.EventID;
import org.openlcb.EventNameStore;

/**
 * JMRI's implementation of part of the OpenLcb EventNameStore interface.
 *
 * @author Bob Jacobsen Copyright (C) 2024
 */
public final class OlcbEventNameStore implements EventNameStore {

    public OlcbEventNameStore() {

        readDetails();
        
        initShutdownTask();
    }

    private final Map<String, EventID> nameToId = new HashMap<>();
    private final Map<EventID, String> idToName = new HashMap<>();
    public boolean dirty = false;

    /**
     * @param eventID The EventID being searched for
     * @return The name associated with that EventID or the event ID in dotted hex
     */
    @Override
    public String getEventName(EventID eventID) {
        var name = idToName.get(eventID);
        if (name == null || name.isEmpty()) return eventID.toShortString();
        return name;
    }
    
    /**
     * @param eventID The EventID being searched for
     * @return true if there is an associated name
     */
    public boolean hasEventName(EventID eventID) {
        var name = idToName.get(eventID);
        if (name == null || name.isEmpty()) return false;
        return true;
    }

    /**
     * @param name The event name being searched for
     * @return The EventID associated with that name or an event ID constructed from the input
     */
    @Override
    public EventID getEventID(String name) {
        var eid = nameToId.get(name);
        if (eid == null) return new EventID(name);
        return eid;    
    }

    /**
     * @param name The event name being searched for
     * @return true if an EventID is associated with that name
     */
    public boolean hasEventID(String name) {
        var eid = nameToId.get(name);
        if (eid == null) return false;
        return true;    
    }

    /**
     * Create a new name to/from EventID association
     * @param eventID associated EventID
     * @param name  associated name
     */
    public void addMatch(EventID eventID, String name) {
        nameToId.put(name, eventID);
        idToName.put(eventID, name);
        log.trace("setting dirty true");
        dirty = true;
    }

    /**
     * Get all the EventIDs available
     * @return Set of all available EventIDs
     */
    public java.util.Set<EventID> getMatches() {
        return idToName.keySet();        
    }

    public void readDetails() {
        log.debug("reading Event Name Details");
        new OlcbEventNameStoreXml(this,"EventNames.xml").load();  // NOI18N
        log.debug("...done reading Event Name details");
    }

    private Runnable shutDownTask = null;

    protected void initShutdownTask(){
        // Create shutdown task to save
        log.debug("Register ShutDown task");
        if (this.shutDownTask == null) {
            this.shutDownTask = () -> {
                // Save event name details prior to exit, if necessary
                log.debug("Start writing event name details...");
                try {
                    writeEventNameDetails();
                } catch (java.io.IOException ioe) {
                    log.error("Exception writing event name", ioe);
                }
            };
            InstanceManager.getDefault(ShutDownManager.class).register(this.shutDownTask);
        }
    }

    /**
     * De-register the Shutdown task.
     */
    public void deregisterShutdownTask(){
        log.debug("Deregister ShutDown task");
        if ( shutDownTask != null ) {
            InstanceManager.getDefault(ShutDownManager.class).deregister(shutDownTask);
        }
    }

    public void writeEventNameDetails() throws java.io.IOException {
        log.debug("storing event name map {}", dirty);
        if (this.dirty) {
            new OlcbEventNameStoreXml(this,"EventNames.xml").store();  // NOI18N
            this.dirty = false;
            log.debug("...done writing event name details");
        }
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbEventNameStore.class);

}
