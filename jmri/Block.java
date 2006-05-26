package jmri;

import com.sun.java.util.collections.ArrayList;

/**
 * Represents a particular piece of track, more informally a "Block".
 * <P>
 * A Block (at least in this implementation) corresponds exactly to the
 * track covered by a single sensor. That can be generalized in the future.
 *
 *<p>
 * Objects of this class are Named Beans, so can be manipulated through tables,
 * have listeners, etc.
 *
 * <p>
 * There is no real requirement for a type letter in the System Name, but
 * by convention we use 'B' for 'Block'.
 *
 * <P>Issues:
 * <UL>
 * <LI> Doesn't handle a train pulling in behind another well:
 *      <UL>
 *      <LI>When the 2nd train arrives, the Sensor is already active, so the value is unchanged (but the value can only
 *          be a single object anyway)
 *      <LI>When the 1st train leaves, the Sensor stays active, so the value remains that of the 1st train
 *      </UL>
 * <LI> The assumption is that a train will only go through a set turnout.  For example, a train could
 *      come into the turnout block from the main even if the turnout is set to the siding.  (Ignoring those
 *      layouts where this would cause a short; it doesn't do so on all layouts)  
 * </UL>
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
public class Block extends jmri.AbstractNamedBean {

    public Block(String systemName) {
        super(systemName);
    }

    public Block(String systemName, String userName) {
        super(systemName, userName);
    }

    static final public int OCCUPIED = Sensor.ACTIVE;
    static final public int UNOCCUPIED = Sensor.INACTIVE;
    
    public void setSensor(Sensor sensor) {
        if (_sensor!=null) {
            log.error("Sensor already set for Block "+getSystemName());
            // this is bad because we don't have a way to remove the listener
        }
        
        _sensor = sensor;        
        
        if (_sensor != null) {
            // attach listener
            _sensor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { handleSensorChange(e); }
            });
        }
    }
    public Sensor getSensor() { return _sensor; }
    
    public int getState() {return _current;}
    
    ArrayList paths = new ArrayList();
    public void addPath(Path p) {
        paths.add(p);
    }
    
    /**
     * Provide a general method for updating the report.
     */
    public void setState(int v) {
        int old = _current;
        _current = v;
        // notify
        firePropertyChange("state", new Integer(old), new Integer(_current));
    }
    
    public void setValue(Object value) {
        Object old = _value;
        _value = value;
        firePropertyChange("value", old, _value);
    }
    public Object getValue() { return _value; }
    
    public void setDirection(int direction) {
        _direction = direction;
    }
    public int getDirection() { return _direction; }
    
    // internal data members
    private int _current; // state
    private Sensor _sensor;
    private Object _value;
    private int _direction;
    
    /** Handle change in sensor state.
     * <P>
     * Defers real work to goingActive, goingInactive methods
     */
    void handleSensorChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName() == "KnownState") {
            int state = _sensor.getState();
            if (state == Sensor.ACTIVE) goingActive();
            else if (state == Sensor.INACTIVE) goingInactive();
            else if (state == Sensor.UNKNOWN) {
                setValue(null);
                setState(UNKNOWN);
            } else {
                setValue(null);
                setState(INCONSISTENT);
            }
        }
    }
    
    /**
     * Handles Block sensor going INACTIVE: this block is empty
     */
    void goingInactive() {
        if (log.isDebugEnabled()) log.debug("Block "+getSystemName()+" goes UNOCCUPIED");
        setValue(null);
        setDirection(Path.NONE);
        setState(UNOCCUPIED);
    }

    /**
     * Handles Block sensor going ACTIVE: this block is now occupied, 
     * figure out from who and copy their value.
     */
    void goingActive() {
        // index through the paths, counting
        int count = 0;
        Path next = null;
        for (int i = 0; i<paths.size(); i++) {
            Path p = (Path) paths.get(i);
            if (p.checkPathSet() && (p.getBlock().getSensor().getState()==Sensor.ACTIVE)) {
                count++;
                next = p;
            }
        }
        // sort on number of neighbors
        if (count == 0) log.info("Sensor ACTIVE came out of nowhere, no neighbors active for block "+getSystemName()+". Value not set.");
        else if (count > 1) log.error("count of "+count+" ACTIVE neightbors can't be handled for block "+getSystemName());
        else {
        
            if ((next!= null) && (next.getBlock()!=null)) {
                // normal case, transfer value object 
                setValue(next.getBlock().getValue());
                setDirection(next.getFromBlockDirection());
                if (log.isDebugEnabled()) log.debug("Block "+getSystemName()+" gets new value from "+next.getBlock().getSystemName()+", direction="+next.decodeDirection(getDirection()));
            } else if (next == null) log.error("unexpected next==null processing signal in block "+getSystemName());
            else if (next.getBlock() == null) log.error("unexpected next.getBlock()=null processing signal in block "+getSystemName());
        }
        // in any case, go OCCUPIED
        setState(OCCUPIED);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Block.class.getName());
}
