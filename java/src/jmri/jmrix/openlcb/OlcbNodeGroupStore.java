package jmri.jmrix.openlcb;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.*;
import java.text.Collator;
import java.util.*;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.ShutDownManager;
import jmri.jmrix.openlcb.configurexml.OlcbNodeGroupStoreXml;

import org.openlcb.NodeID;

/**
 * Maintain information about which group(s) contain a node.
 *
 * @author Bob Jacobsen Copyright (C) 2024
 */
public class OlcbNodeGroupStore implements InstanceManagerAutoDefault { // not final for testing

    public OlcbNodeGroupStore() {
        log.debug("Initialising");
        // Load when created
        load();
        initShutdownTask();
        dirty = false;   // undo changes during load
    }
       
    // maps a string group name to a set of NodeIDs it contains
    private HashMap<String, HashSet<NodeID>> mapOfGroups = new HashMap<>();
     
    private HashMap<NodeID, TreeSet<String>> mapOfNodes = new HashMap<>();
    
    private boolean dirty = false;
    private Runnable shutDownTask = null;
    
    protected void load() {
        new OlcbNodeGroupStoreXml(this,"NodeGroupAssociations.xml").load();  // NOI18N
    }
    
    protected void store() throws java.io.IOException {
        log.debug("Store invoked");
        if (dirty) {
            new OlcbNodeGroupStoreXml(this,"NodeGroupAssociations.xml").store();  // NOI18N
            dirty = false;
        }
    }
    
    protected void initShutdownTask() {
        // Create shutdown task to save
        log.debug("Register ShutDown task");
        if (this.shutDownTask == null) {
            this.shutDownTask = () -> {
                try {
                    store();
                } catch (java.io.IOException ioe) {
                    log.error("Exception writing node group associations", ioe);
                }
            };
            InstanceManager.getDefault(ShutDownManager.class).register(this.shutDownTask);
        }
    }

    /**
     * Add a Node to a group
     */
    public void addNodeToGroup(NodeID node, String group) {
        if (!mapOfGroups.containsKey(group)) mapOfGroups.put(group, new HashSet<NodeID>());
        if (!mapOfNodes.containsKey(node)) mapOfNodes.put(node, new TreeSet<String>());
        mapOfGroups.get(group).add(node);
        mapOfNodes.get(node).add(group);
        fireChangeEvent();
    } 
    
    /**
     * Remove a node from a group
     */
    public void removeNodeFromGroup(NodeID node, String group) {
        var listNodes = mapOfGroups.get(group);
        if (listNodes != null) listNodes.remove(node);
        var listGroups = mapOfNodes.get(node);
        if (listGroups != null) listGroups.remove(group);
        
        // when you remove the last entry in the group, the
        // group itself goes away
        listNodes = mapOfGroups.get(group);
        if (listNodes != null && listNodes.size() == 0) {
            removeGroup(group);
        }
        fireChangeEvent();
    } 
    
    /**
     * Remove a group, including all the associations it contains
     */
    public void removeGroup(String group) {
        mapOfGroups.remove(group);
        // find all the references in the mapOfNodes and remove each one
        for (Collection<NodeID> set : mapOfGroups.values()) {
            for (NodeID node : set) {
                mapOfNodes.get(node).remove(group);
            }
        }
        fireChangeEvent();
    }
    
    /**
     * Get alphanumerically-sorted  List of existing group names
     */
    public List<String> getGroupNames() {
        var retval = new ArrayList<String>(mapOfGroups.keySet());
        retval.sort(Collator.getInstance());
        return retval;
    }
 
    /**
     * Get a Set of nodes in a group
     */
    public Set<NodeID> getGroupNodes(String group) {
        return mapOfGroups.get(group);
    }
   
    /**
     * Get an ordered set of groups a node belongs to.
     */
    public List<String> getNodesGroups(NodeID node) {
        var retval = new ArrayList<String>(mapOfNodes.get(node));
        retval.sort(Collator.getInstance());
        return retval;
    } 
     
    /**
     * Does a particular node belong to a specific group?
     */
    public boolean isNodeInGroup(NodeID node, String group) {
        var list = mapOfNodes.get(node);
        if (list == null) return false;
        return list.contains(group);
    } 
    
    // notify listeners that the content has changed;
    // doesn't have a finer resolution than that.
    protected void fireChangeEvent() {
        pcs.firePropertyChange("Associations", false, true);
        dirty = true;
    }

     private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this, true);

     public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
     }

     public void removePropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.removePropertyChangeListener(listener);
     }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbNodeGroupStore.class);

}
