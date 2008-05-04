package jmri;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a particular piece of track, more informally a "Block".
 * As trains move around the layout, a set of Block objects interact to
 * keep track of which train is where, going in which direction.
 * As a result of this, the set of Block objects pass around  "token" (value)
 * Objects representing the trains.  This could be e.g. a Throttle to
 * control the train, or something else.
 * <P>
 * A Block (at least in this implementation) corresponds exactly to the
 * track covered by a single sensor. That should be generalized in the future.
 * 
 *<p>
 * Objects of this class are Named Beans, so can be manipulated through tables,
 * have listeners, etc.
 *
 * <p>
 * There is no functional requirement for a type letter in the System Name, but
 * by convention we use 'B' for 'Block'. The default implementation is not 
 * system-specific, so a system letter of 'I' is appropriate.  This leads to 
 * system names like "IB201".
 *
 *<p>The direction of a Block is set from the direction of the incoming
 * train. When a train is found to be coming in on a particular Path, that
 * Path's getFromBlockDirection becomes the direction of the train in this Block.
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
 * <LI> Does not handle closely-following trains where there is only one 
 *      electrical block per signal.   To do this, it probably needs some type of
 *      "assume a train doesn't back up" logic.  A better solution is to have multiple
 *      sensors and Block objects between each signal head.
 * <li> If a train reverses in a block and goes back the way it came (e.g. b1 to b2 to b1), 
 *      the block that's re-entered will get an updated direction, but the direction of this block (b2 in the example)
 *      is not updated.  In other words, we're not noticing that the train must have reversed to go back out.
 *</UL>
 *
 *<P>
 * Do not assume that a Block object uniquely represents a piece of track.
 * To allow independent development, it must be possible for multiple Block objects
 * to take care of a particular section of track.
 *
 *<P>
 * Possible state values:
 *<ul>
 * <li>UNKNOWN - The sensor shows UNKNOWN, so this block doesn't know if it's occupied or not.
 * <li>INCONSISTENT - The sensor shows INCONSISTENT, so this block doesn't know if it's occupied or not.
 * <li>OCCUPIED - This sensor went active. Note that OCCUPIED will be set
 *              even if the logic is unable to figure out which value to take.
 * <li>UNOCCUPIED - No content, because the sensor has determined this block is unoccupied.
 *</ul>
 *
 * @author	Bob Jacobsen  Copyright (C) 2006, 2008
 * @version	$Revision: 1.9 $
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
			// remove sensor listener
			if (_sensorListener != null) {
				_sensor.removePropertyChangeListener(_sensorListener);
				_sensorListener = null;
			}
        }        
        _sensor = sensor;                
        if (_sensor != null) {
            // attach listener
            _sensor.addPropertyChangeListener(_sensorListener = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { handleSensorChange(e); }
            });
        }
    }
    public Sensor getSensor() { return _sensor; }
    
    public int getState() {return _current;}
    
    ArrayList paths = new ArrayList();
    public void addPath(Path p) {
        if (p==null) throw new IllegalArgumentException("Can't add null path");
        paths.add(p);
    }
    public void removePath(Path p) {
		int j = -1;
		for (int i = 0; i<paths.size(); i++) {
			if (p == (Path) paths.get(i))
				j = i;
		}
		if (j>-1) paths.remove(j);
    }
    
    /**
     * Get a copy of the list of Paths
     */
    public List getPaths() {
        return new ArrayList(paths);
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
    
    /**
     * Set the value retained by this Block.
     * Also used when the Block itself gathers a value from an 
     * adjacent Block.  This can be overridden in a subclass if
     * e.g. you want to keep track of Blocks elsewhere, but make
     * sure you also eventually invoke the super.setValue() here.
     * <p>
     * @param value The new Object resident in this block, or null if none.
     */
    public void setValue(Object value) {
        Object old = _value;
        _value = value;
        firePropertyChange("value", old, _value);
    }
    public Object getValue() { return _value; }
    
    public void setDirection(int direction) {
        int oldDirection = _direction;
        _direction = direction;
        // this is a bound parameter
        firePropertyChange("direction", new Integer(oldDirection), new Integer(direction));
    }
    public int getDirection() { return _direction; }
    
    // internal data members
    private int _current; // state
    private Sensor _sensor;
	private java.beans.PropertyChangeListener _sensorListener = null;
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
        else if (count == 1) { // simple case
        
            if ((next!= null) && (next.getBlock()!=null)) {
                // normal case, transfer value object 
                setValue(next.getBlock().getValue());
                setDirection(next.getFromBlockDirection());
                if (log.isDebugEnabled()) log.debug("Block "+getSystemName()+" gets new value from "+next.getBlock().getSystemName()+", direction="+next.decodeDirection(getDirection()));
            } else if (next == null) log.error("unexpected next==null processing signal in block "+getSystemName());
            else if (next.getBlock() == null) log.error("unexpected next.getBlock()=null processing signal in block "+getSystemName());
        }
        else {  // count > 1, check for one with proper direction
            // this time, count ones with proper direction
			if (log.isDebugEnabled()) log.debug ("Block "+getSystemName()+"- count of active linked blocks = "+count);
            next = null;
            count = 0;
            for (int i = 0; i<paths.size(); i++) {
                Path p = (Path) paths.get(i);
                if (p.checkPathSet() && (p.getBlock().getSensor().getState()==Sensor.ACTIVE)
                    && (p.getBlock().getDirection() == p.getFromBlockDirection())) {
                    count++;
                    next = p;
                } 
            }       
            if (count==1) {
                // found one block with proper direction, assume that
                setValue(next.getBlock().getValue());
                setDirection(next.getFromBlockDirection());
                if (log.isDebugEnabled()) log.debug("Block "+getSystemName()+" with direction "+Path.decodeDirection(getDirection())+" gets new value from "+next.getBlock().getSystemName()+", direction="+next.decodeDirection(getDirection()));
            } else {
                // no unique path with correct direction - this happens frequently from noise in block detectors!!
                log.debug("count of "+count+" ACTIVE neightbors with proper direction can't be handled for block "+getSystemName());
            }
        }
        // in any case, go OCCUPIED
        setState(OCCUPIED);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Block.class.getName());
}
