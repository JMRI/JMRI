package jmri.jmrix.openlcb;

import jmri.IdTagManager;
import jmri.InstanceManager;

import org.openlcb.EventID;
import org.openlcb.EventNameStore;

/**
 * JMRI's implementation of part of the OpenLcb EventNameStore interface.
 *
 * @author Bob Jacobsen Copyright (C) 2024
 */
public final class OlcbEventNameStore implements EventNameStore {

    public OlcbEventNameStore() {
    }

    final IdTagManager tagmgr = InstanceManager.getDefault(IdTagManager.class); // only one of these
    
    /**
     * @param eventID The EventID being searched for
     * @return The name associated with that EventID or null
     */
    public String getEventName(EventID eventID) {
        var tag = tagmgr.getBySystemName(OlcbConstants.tagPrefix+eventID.toShortString());
        if (tag == null) return null;
        var name = tag.getUserName();
        if (name == null || name.isEmpty()) return null;
        return name;
    }
    
    /**
     * @param name The event name being searched for
     * @return The EventID associated with that name
     */
    public EventID getEventID(String name) {
        var tag = tagmgr.getByUserName(name);
        if (tag == null) return null;
        
        var eid = tag.getSystemName().substring(OlcbConstants.tagPrefix.length());
        return new EventID(eid);
    }
        
    /**
     * Create a new name <-> EventID association
     * @param eventID
     * @param name
     */
    public void addMatch(EventID eventID, String name) {
        tagmgr.provideIdTag(OlcbConstants.tagPrefix+eventID.toShortString())
            .setUserName(name);
    }
    
    public java.util.Set<EventID> getMatches() {
        var set = new java.util.HashSet<EventID>();
        for (var tag: tagmgr.getNamedBeanSet()) {
            if (tag.getSystemName().startsWith(OlcbConstants.tagPrefix)) {
                var eid = tag.getSystemName().substring(OlcbConstants.tagPrefix.length());
                set.add(new EventID(eid));
            }
        }
        return set;
        
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbEventNameStore.class);

}
