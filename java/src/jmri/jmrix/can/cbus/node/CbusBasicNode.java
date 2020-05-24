package jmri.jmrix.can.cbus.node;

import java.beans.PropertyChangeListener;
import java.util.concurrent.CopyOnWriteArraySet;
// import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusSend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusBasicNode {
    protected CanSystemConnectionMemo _memo;
    private int _nodeNumber;    
    
    private int _canId;
    private boolean _inSetupMode;
    private boolean _inlearnMode;
    private boolean _inFLiMMode;
    
    public CbusSend send;
    
    // data members to hold contact with the property listeners
    protected final CopyOnWriteArraySet<PropertyChangeListener> _listeners;
    
    /**
     * Create a new CbusBasicNodeWithChangeListener.
     *
     * @param connmemo The CAN Connection to use
     * @param nodenumber The Node Number
     */
    public CbusBasicNode ( CanSystemConnectionMemo connmemo, int nodenumber ){
        _memo = connmemo;
        _nodeNumber = nodenumber;
        _canId = -1;
        _inSetupMode = false;
        _inlearnMode = false;
        _inFLiMMode = true;
        
        send = new CbusSend(_memo);
        _listeners = new CopyOnWriteArraySet<>();
    }
    
    /**
     * Register for notification if any of the properties change.
     *
     * @param l The Listener to attach to Node
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!_listeners.contains(l)) {
            _listeners.add(l);
            // log.info("Added listener {}, new size is {}", l,_listeners.size());
        }
    }
    
    /**
     * Remove notification listener.
     * @param l Listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (_listeners.contains(l)) {
            _listeners.remove(l);
            // log.info("Removed listener {}, new size is {}", l,_listeners.size());
        }
    }

    /**
     * Trigger the notification of Node PropertyChangeListeners.
     * 
     * Properties include 
     * PARAMETER, 
     * BACKUPS, 
     * SINGLENVUPDATE ( newValue NV index (0 is NV1, 5 is NV6) )
     * ALLNVUPDATE
     * SINGLEEVUPDATE ( newValue event row )
     * ALLEVUPDATE
     * DELETEEVCOMPLETE ( newValue Error String else null )
     * ADDEVCOMPLETE ( newValue Error String else null )
     * ADDALLEVCOMPLETE ( Event Teach Loop Completed, newValue error count )
     * TEACHNVCOMPLETE ( newValue error count )
     * NAMECHANGE
     * 
     * @param property Node property
     * @param oldValue Old Value
     * @param newValue New Value
     */
    protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        if ((oldValue != null && oldValue.equals(newValue)) || (newValue != null && newValue.equals(oldValue))) {
            log.error("notifyPropertyChangeListener without change");
            return;
        }
        _listeners.forEach((listener) -> {
            listener.propertyChange(new java.beans.PropertyChangeEvent(this, property, oldValue, newValue));
        });
    }
    
    /**
     * Returns Node Number.
     *
     * @return Node Number,1-65535
     */
    public int getNodeNumber() {
        return _nodeNumber;
    }
    
    /**
     * Set Node Number.
     * @param newnumber Node Number, should be 1-65535
     */
    public void setNodeNumber ( int newnumber ) {
        _nodeNumber = newnumber;
        notifyPropertyChangeListener("PARAMETER", null, null);
    }

    /**
     * Set Node CAN ID.
     * @param newcanid CAN ID of the node
     */
    public final void setCanId ( int newcanid ) {
        _canId = newcanid;
        notifyPropertyChangeListener("CANID", null, _canId);
    }
    
    /**
     * Get the Node CAN ID.
     * min 1 , ( max 128? )
     * @return CAN ID of the node, -1 if unset
     */
    public int getNodeCanId() {
        return _canId;
    }

    /**
     * Set flag for this Node in Setup Mode.
     * <p>
     * Does NOT send instruction to actual node
     *
     * @param setup use true if in Setup, else false
     */
    public void setNodeInSetupMode( boolean setup ) {
        _inSetupMode = setup;
        notifyPropertyChangeListener("PARAMETER", null, null);
    }
    
    /**
     * Get if this Node is in Setup Mode.
     *
     * @return true if in Setup, else false
     */
    public boolean getNodeInSetupMode() {
        return _inSetupMode;
    }
    
    /**
     * Set if the Node is in Learn Mode.
     * Used to track node status, does NOT update Physical Node
     * 
     * @param inlearnmode set true if in Learn else false
     */
    public void setNodeInLearnMode( boolean inlearnmode) {
        boolean oldLearnmode = _inlearnMode;
        _inlearnMode = inlearnmode;
        if (oldLearnmode != _inlearnMode) {
            notifyPropertyChangeListener("LEARNMODE",oldLearnmode,_inlearnMode);
        }
    }
    
    /**
     * Get if the Node is in Learn Mode.
     * <p>
     * Defaults to false if unset
     * 
     * @return true if in Learn else false
     */
    public boolean getNodeInLearnMode() {
        return _inlearnMode;
    }

    /**
     * Set if the Node is in FLiM Mode.
     * <p>
     * Defaults to true if unset
     * 
     * @param inFlimMode set true if in FlIM else false
     */
    public void setNodeInFLiMMode( boolean inFlimMode ) {
        _inFLiMMode = inFlimMode;
    }    
    
    /**
     * Get if the Node is in FLiM Mode.
     * <p>
     * Defaults to true if unset
     * 
     * @return true if in FlIM else false
     */
    public boolean getNodeInFLiMMode() {
        return _inFLiMMode;
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusBasicNode.class);
    
}
