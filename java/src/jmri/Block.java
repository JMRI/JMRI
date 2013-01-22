package jmri;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jmri.util.PhysicalLocation;

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
 * <p>
 * Optionally, a Block can be associated with a Reporter. In this case, the
 * Reporter will provide the Block with the "token" (value). This could be e.g
 * an RFID reader reading an ID tag attached to a locomotive. Depending on the
 * specific Reporter implementation, either the current reported value or the
 * last reported value will be relevant - this can be configured
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
 *<P>
 * Possible Curvature attributes (optional) User can set the curvature if desired. 
 *     For use in automatic running of trains, to indicate where slow down is required.
 *<ul>
 * <li>NONE - No curvature in Block track, or Not entered.
 * <li>GRADUAL - Gradual curve - no action by engineer is warranted - full speed OK
 * <li>TIGHT - Tight curve in Block track - Train should slow down some
 * <li>SEVERE - Severe curve in Block track - Train should slow down a lot
 *</ul>
 *
 *<P>
 * The length of the block may also optionally be entered if desired.  This attribute
 *		is for use in automatic running of trains.
 * Length should be the actual length of model railroad track in the block.  It is 
 *		always stored here in millimeter units. A length of 0.0 indicates no entry of 
 *		length by the user.
 *
 * @author	Bob Jacobsen  Copyright (C) 2006, 2008
 * @author  Dave Duchamp Copywright (C) 2009
 * @version	$Revision$
 * GT 10-Aug-2008 - Fixed problem in goingActive() that resulted in a 
 * NULL pointer exception when no sensor was associated with the block
 */
public class Block extends jmri.implementation.AbstractNamedBean implements PhysicalLocationReporter {

    public Block(String systemName) {
        super(systemName.toUpperCase());
    }

    public Block(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    static final public int OCCUPIED = Sensor.ACTIVE;
    static final public int UNOCCUPIED = Sensor.INACTIVE;
	
	// Curvature attributes
	static final public int NONE = 0x00;
	static final public int GRADUAL = 0x01;
	static final public int TIGHT = 0x02;
	static final public int SEVERE = 0x04;
    
    public void setSensor(String pName){
        if(pName==null || pName.equals("")){
            setNamedSensor(null);
            return;
        }
        if (InstanceManager.sensorManagerInstance()!=null) {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
                setNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
            } else {
                setNamedSensor(null);
                log.error("Sensor '"+pName+"' not available");
            }
        } else {
            log.error("No SensorManager for this protocol");
        }
    }
    
    public void setNamedSensor(NamedBeanHandle<Sensor> s) {
        if (_namedSensor != null) {
            if (_sensorListener != null) {
				getSensor().removePropertyChangeListener(_sensorListener);
				_sensorListener = null;
			}
        }
        _namedSensor = s;
        
        if(_namedSensor !=null){
            getSensor().addPropertyChangeListener(_sensorListener = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { handleSensorChange(e); }
            }, s.getName(), "Block Sensor " + getDisplayName());
            _current = getSensor().getState();
        } else {
            _current = UNOCCUPIED;
        }
    }
    
    public Sensor getSensor() { 
        if (_namedSensor!=null)
            return _namedSensor.getBean();
        return null;
    }

    public NamedBeanHandle <Sensor> getNamedSensor() {
        return _namedSensor;
    }

    /**
     * Set the Reporter that should provide the data value for this block.
     *
     * @see Reporter
     * @param reporter Reporter object to link, or null to clear
     */
    public void setReporter(Reporter reporter) {
        if (_reporter != null) {
            // remove reporter listener
            if (_reporterListener != null) {
                _reporter.removePropertyChangeListener(_reporterListener);
                _reporterListener = null;
            }
        }
        _reporter = reporter;
        if (_reporter != null) {
            // attach listener
            _reporter.addPropertyChangeListener(_reporterListener = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { handleReporterChange(e); }
            });

        }
    }

    /**
     * Retrieve the Reporter that is linked to this Block
     *
     * @see Reporter
     * @return linked Reporter object, or null if not linked
     */
    public Reporter getReporter() { return _reporter; }

    /**
     * Define if the Block's value should be populated from the
     * {@link Reporter#getCurrentReport() current report}
     * or from the {@link Reporter#getLastReport() last report}.
     *
     * @see Reporter
     * @param reportingCurrent
     */
    public void setReportingCurrent(boolean reportingCurrent) {
        _reportingCurrent = reportingCurrent;
    }

    /**
     * Determine if the Block's value is being populated from the
     * {@link Reporter#getCurrentReport() current report}
     * or from the {@link Reporter#getLastReport() last report}.
     *
     * @see Reporter
     * @return true if populated by {@link Reporter#getCurrentReport() current report};
     * false if from {@link Reporter#getLastReport() last report}.
     */
    public boolean isReportingCurrent() { return _reportingCurrent; }
    
    public int getState() {return _current;}
    
    ArrayList<Path>paths = new ArrayList<Path>();
    public void addPath(Path p) {
        if (p==null) throw new IllegalArgumentException("Can't add null path");
        paths.add(p);
    }
    public void removePath(Path p) {
		int j = -1;
		for (int i = 0; i<paths.size(); i++) {
			if (p == paths.get(i))
				j = i;
		}
		if (j>-1) paths.remove(j);
    }
    
    /**
     * Get a copy of the list of Paths
     */
    public List<Path> getPaths() {
        return new ArrayList<Path>(paths);
    }
    
    /**
     * Provide a general method for updating the report.
     */
    public void setState(int v) {
        int old = _current;
        _current = v;
        // notify
        firePropertyChange("state", Integer.valueOf(old), Integer.valueOf(_current));
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
        firePropertyChange("direction", Integer.valueOf(oldDirection), Integer.valueOf(direction));
    }
    public int getDirection() { return _direction; }
    
    //Deny traffic entering from this block
    ArrayList<NamedBeanHandle<Block>> blockDenyList = new ArrayList<NamedBeanHandle<Block>>(1);
    
    /**
    * The block deny list, is used by higher level code, to determine if 
    * traffic/trains should be allowed to enter from an attached block,
    * the list only deals with blocks that access should be denied from.
    * If we want to prevent traffic from following from this block to another
    * then this block must be added to the deny list of the other block.
    * By default no block is barred, so traffic flow is bi-directional.
    */
    public void addBlockDenyList(String pName){
        Block blk = jmri.InstanceManager.blockManagerInstance().getBlock(pName);
        NamedBeanHandle<Block> namedBlock = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, blk);
        if(!blockDenyList.contains(namedBlock))
            blockDenyList.add(namedBlock);
    }
    
    public void addBlockDenyList(Block blk){
        NamedBeanHandle<Block> namedBlock = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(blk.getDisplayName(), blk);
        if(!blockDenyList.contains(namedBlock))
            blockDenyList.add(namedBlock);
    }
    
    public void removeBlockDenyList(String blk){
        NamedBeanHandle<Block> toremove = null;
        for(NamedBeanHandle<Block> bean: blockDenyList){
            if(bean.getName().equals(blk))
                toremove=bean;
        }
        if(toremove!=null){
            blockDenyList.remove(toremove);
        }
    }
    
    public void removeBlockDenyList(Block blk){
        NamedBeanHandle<Block> toremove = null;
        for(NamedBeanHandle<Block> bean: blockDenyList){
            if(bean.getBean()==blk)
                toremove=bean;
        }
        if(toremove!=null){
            blockDenyList.remove(toremove);
        }
    }
    
    public List<String> getDeniedBlocks(){
        List<String> list = new ArrayList<String>(blockDenyList.size());
        for(NamedBeanHandle<Block> bean: blockDenyList){
            list.add(bean.getName());
        }
        return list;
    }
    
    public boolean isBlockDenied(String deny){
        for(NamedBeanHandle<Block> bean: blockDenyList){
            if(bean.getName().equals(deny))
                return true;
        }
        return false;
    }
    
    public boolean isBlockDenied(Block deny){
        for(NamedBeanHandle<Block> bean: blockDenyList){
            if(bean.getBean()==deny)
                return true;
        }
        return false;
    }
    
    public boolean getPermissiveWorking() { return _permissiveWorking; }
    public void setPermissiveWorking(boolean w) { _permissiveWorking=w; }
    private boolean _permissiveWorking=false;

    public float getSpeedLimit() { 
        if ((_blockSpeed==null) || (_blockSpeed.equals("")))
            return -1;
        String speed = _blockSpeed;
        if(_blockSpeed.equals("Global")){
            speed = InstanceManager.blockManagerInstance().getDefaultSpeed();
        }
        
        try {
            return new Float(speed);
        } catch (NumberFormatException nx) {
            //considered normal if the speed is not a number.
        }
        try{
            return jmri.implementation.SignalSpeedMap.getMap().getSpeed(speed);
        } catch (Exception ex){
            return -1;
        }
    }
    
    private String _blockSpeed = "";
    public String getBlockSpeed() { 
        if(_blockSpeed.equals("Global"))
            return ("Use Global " + InstanceManager.blockManagerInstance().getDefaultSpeed());
        return _blockSpeed;
    }

    public void setBlockSpeed(String s) throws JmriException {
        if((s==null) || (_blockSpeed.equals(s)))
            return;
        if(s.contains("Global"))
            s = "Global";
        else {
            try {
                Float.parseFloat(s);
            } catch (NumberFormatException nx) {
                try{
                    jmri.implementation.SignalSpeedMap.getMap().getSpeed(s);
                } catch (Exception ex){
                    throw new JmriException("Value of requested block speed is not valid");
                }
            }
        }
        String oldSpeed = _blockSpeed;
        _blockSpeed=s;
        firePropertyChange("BlockSpeedChange", oldSpeed, s);
    }
	
	public void setCurvature(int c) { _curvature = c; }
	public int getCurvature() { return _curvature; }
	public void setLength(float l) { _length = l; }  // l must be in millimeters
	public float getLengthMm() { return _length; } // return length in millimeters
	public float getLengthCm() { return (_length/10.0f); }  // return length in centimeters
	public float getLengthIn() { return (_length/25.4f); }  // return length in inches
    
    // internal data members
    private int _current = UNOCCUPIED; // state
    //private Sensor _sensor = null;
    private NamedBeanHandle<Sensor> _namedSensor = null;
	private java.beans.PropertyChangeListener _sensorListener = null;
    private Object _value;
    private int _direction;
	private int _curvature = NONE;
	private float _length = 0.0f;  // always stored in millimeters
    private Reporter _reporter = null;
    private java.beans.PropertyChangeListener _reporterListener = null;
    private boolean _reportingCurrent = false;
    
    /** Handle change in sensor state.
     * <P>
     * Defers real work to goingActive, goingInactive methods
     */
    void handleSensorChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState")) {
            int state = getSensor().getState();
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
     * Handle change in Reporter value.
     * 
     * @param e PropertyChangeEvent
     */
    void handleReporterChange(java.beans.PropertyChangeEvent e) {
        if ((_reportingCurrent && e.getPropertyName().equals("currentReport"))
                || (!_reportingCurrent && e.getPropertyName().equals("lastReport"))) {
            setValue(e.getNewValue());
        }
    }
    
    /**
     * Handles Block sensor going INACTIVE: this block is empty
     */
    public void goingInactive() {
        if (log.isDebugEnabled()) log.debug("Block "+getSystemName()+" goes UNOCCUPIED");
        setValue(null);
        setDirection(Path.NONE);
        setState(UNOCCUPIED);
    }

	private int maxInfoMessages = 5;
	private int infoMessageCount = 0;
    /**
     * Handles Block sensor going ACTIVE: this block is now occupied, 
     * figure out from who and copy their value.
     */
	public void goingActive() {
        // index through the paths, counting
        int count = 0;
        Path next = null;
        // get statuses of everything once
        int currPathCnt = paths.size();
        Path pList[] = new Path[currPathCnt];
        boolean isSet[] = new boolean[currPathCnt];
        Sensor pSensor[] = new Sensor[currPathCnt];
        boolean isActive[] = new boolean[currPathCnt];
        int pDir[] = new int[currPathCnt];
        int pFromDir[] = new int[currPathCnt];
        for (int i = 0; i < currPathCnt; i++) {
        	pList[i] = paths.get(i);
            isSet[i] = pList[i].checkPathSet();
            Block b = pList[i].getBlock();
            if (b != null) {
                pSensor[i] = b.getSensor();
                if (pSensor[i] != null) {
                    isActive[i] = pSensor[i].getState() == Sensor.ACTIVE;
                } else {
                    isActive[i] = false;
                }
                pDir[i] = b.getDirection();
            } else {
                pSensor[i] = null;
                isActive[i] = false;
                pDir[i] = -1;
            }
            pFromDir[i] = pList[i].getFromBlockDirection();
            if (isSet[i] && pSensor[i] != null && isActive[i]) {
                count++;
                next = pList[i];
            }
        }
        // sort on number of neighbors
        if (count == 0) {
			if (infoMessageCount < maxInfoMessages) {
				log.info("Sensor ACTIVE came out of nowhere, no neighbors active for block "+getSystemName()+". Value not set.");
				infoMessageCount ++;
			}
		}
        else if (count == 1) { // simple case
        
            if ((next != null) && (next.getBlock() != null)) {
                // normal case, transfer value object 
                setValue(next.getBlock().getValue());
                setDirection(next.getFromBlockDirection());
                if (log.isDebugEnabled()) log.debug("Block "+getSystemName()+" gets new value from "+next.getBlock().getSystemName()+", direction="+Path.decodeDirection(getDirection()));
            } else if (next == null) log.error("unexpected next==null processing signal in block "+getSystemName());
            else if (next.getBlock() == null) log.error("unexpected next.getBlock()=null processing signal in block "+getSystemName());
        }
        else {  // count > 1, check for one with proper direction
            // this time, count ones with proper direction
			if (log.isDebugEnabled()) log.debug ("Block "+getSystemName()+"- count of active linked blocks = "+count);
            next = null;
            count = 0;
            for (int i = 0; i < currPathCnt; i++) {
                if (isSet[i] && pSensor[i] != null && isActive[i] && (pDir[i] == pFromDir[i])) {
                    count++;
                    next = pList[i];
                } 
            }
            if (next == null)
            	if (log.isDebugEnabled()) log.debug("next is null!");
            if (next != null && count == 1) {
                // found one block with proper direction, assume that
                setValue(next.getBlock().getValue());
                setDirection(next.getFromBlockDirection());
                if (log.isDebugEnabled()) log.debug("Block "+getSystemName()+" with direction "+Path.decodeDirection(getDirection())+" gets new value from "+next.getBlock().getSystemName()+", direction="+Path.decodeDirection(getDirection()));
            } else {
                // no unique path with correct direction - this happens frequently from noise in block detectors!!
                log.warn("count of " + count + " ACTIVE neightbors with proper direction can't be handled for block " + getSystemName());
            }
        }
        // in any case, go OCCUPIED
        setState(OCCUPIED);
    }

    /**
     * Find which path this Block became Active, without actually
     * modifying the state of this block.
     *
     * (this is largely a copy of the 'Search' part of the logic
     * from goingActive())
     */
    public Path findFromPath() {
        // index through the paths, counting
        int count = 0;
        Path next = null;
        // get statuses of everything once
        int currPathCnt = paths.size();
        Path pList[] = new Path[currPathCnt];
        boolean isSet[] = new boolean[currPathCnt];
        Sensor pSensor[] = new Sensor[currPathCnt];
        boolean isActive[] = new boolean[currPathCnt];
        int pDir[] = new int[currPathCnt];
        int pFromDir[] = new int[currPathCnt];
        for (int i = 0; i < currPathCnt; i++) {
	    pList[i] = paths.get(i);
            isSet[i] = pList[i].checkPathSet();
            Block b = pList[i].getBlock();
            if (b != null) {
                pSensor[i] = b.getSensor();
                if (pSensor[i] != null) {
                    isActive[i] = pSensor[i].getState() == Sensor.ACTIVE;
                } else {
                    isActive[i] = false;
                }
                pDir[i] = b.getDirection();
            } else {
                pSensor[i] = null;
                isActive[i] = false;
                pDir[i] = -1;
            }
            pFromDir[i] = pList[i].getFromBlockDirection();
            if (isSet[i] && pSensor[i] != null && isActive[i]) {
                count++;
                next = pList[i];
            }
        }
        // sort on number of neighbors
        if ((count == 0) || (count == 1)){
	    // do nothing.  OK to return null from this function.  "next" is already set.
	} else {  
	    // count > 1, check for one with proper direction
            // this time, count ones with proper direction
	    if (log.isDebugEnabled()) log.debug ("Block "+getSystemName()+"- count of active linked blocks = "+count);
            next = null;
            count = 0;
            for (int i = 0; i < currPathCnt; i++) {
                if (isSet[i] && pSensor[i] != null && isActive[i] && (pDir[i] == pFromDir[i])) {
                    count++;
                    next = pList[i];
                } 
            }
            if (next == null)
            	if (log.isDebugEnabled()) log.debug("next is null!");
            if (next != null && count == 1) {
                // found one block with proper direction, assume that
            } else {
                // no unique path with correct direction - this happens frequently from noise in block detectors!!
                log.warn("count of " + count + " ACTIVE neightbors with proper direction can't be handled for block " + getSystemName());
            }
        }
        // in any case, go OCCUPIED
	if (log.isDebugEnabled()) log.debug("Block "+getSystemName()+" with direction "+Path.decodeDirection(getDirection())+" gets new value from "+next.getBlock().getSystemName() + "(informational. No state change)");
	return(next);
    }

    // Methods to implmement PhysicalLocationReporter Interface
    //
    // If we have a Reporter that is also a PhysicalLocationReporter,
    // we will defer to that Reporter's methods.
    // Else we will assume a LocoNet style message to be parsed.

    /** Parse a given string and return the LocoAddress value that is presumed stored
     * within it based on this object's protocol.
     * The Class Block implementationd defers to its associated Reporter, if it exists.
     *
     * @param rep String to be parsed
     * @return LocoAddress address parsed from string, or null if this Block isn't associated
     *         with a Reporter, or is associated with a Reporter that is not also a
     *         PhysicalLocationReporter
     */
    public LocoAddress getLocoAddress(String rep) {
	// Defer parsing to our associated Reporter if we can.
	if (rep == null) { 
	    log.warn("String input is null!");
	    return(null);
	}
	if ((this.getReporter() != null) && (this.getReporter() instanceof PhysicalLocationReporter)) {
	    return(((PhysicalLocationReporter)this.getReporter()).getLocoAddress(rep));
	} else {
	    // Assume a LocoNet-style report.  This is (nascent) support for handling of Faller cars
	    // for Dave Merrill's project.
	    log.debug("report string: " + rep);
	    // NOTE: This pattern is based on the one defined in jmri.jmrix.loconet.LnReporter
	    Pattern ln_p = Pattern.compile("(\\d+) (enter|exits|seen)\\s*(northbound|southbound)?");  // Match a number followed by the word "enter".  This is the LocoNet pattern.
	    Matcher m = ln_p.matcher(rep);
	    if ((m != null) && m.find()) {
		log.debug("Parsed address: " + m.group(1));
		return(new DccLocoAddress(Integer.parseInt(m.group(1)), LocoAddress.Protocol.DCC));
	    } else {
		return(null);
	    }
	}
    }
     
    /** Parses out a (possibly old) LnReporter-generated report string to extract the direction from
     * within it based on this object's protocol.
     * The Class Block implementationd defers to its associated Reporter, if it exists.
     *
     * @param rep String to be parsed
     * @return PhysicalLocationReporter.Direction direction parsed from string, or null if 
     *         this Block isn't associated with a Reporter, or is associated with a Reporter 
     *         that is not also a PhysicalLocationReporter
     */
    public PhysicalLocationReporter.Direction getDirection(String rep) {
	if (rep == null) { 
	    log.warn("String input is null!");
	    return(null);
	}
	// Defer parsing to our associated Reporter if we can.
	if ((this.getReporter() != null) && (this.getReporter() instanceof PhysicalLocationReporter)) {
	    return(((PhysicalLocationReporter)this.getReporter()).getDirection(rep));
	} else {
	    log.debug("report string: " + rep);
	    // NOTE: This pattern is based on the one defined in jmri.jmrix.loconet.LnReporter
	    Pattern ln_p = Pattern.compile("(\\d+) (enter|exits|seen)\\s*(northbound|southbound)?");  // Match a number followed by the word "enter".  This is the LocoNet pattern.
	    Matcher m = ln_p.matcher(rep);
	    if (m.find()) {
		log.debug("Parsed direction: " + m.group(2));
		if (m.group(2).equals("enter")) {
		    // LocoNet Enter message
		    return(PhysicalLocationReporter.Direction.ENTER);
		} else if (m.group(2).equals("seen")) {
		    // Lissy message.  Treat them all as "entry" messages.
		    return(PhysicalLocationReporter.Direction.ENTER);
		} else {
		    return(PhysicalLocationReporter.Direction.EXIT);
		}
	    } else {
		return(PhysicalLocationReporter.Direction.UNKNOWN);
	    }
	}
    }

    /** Return this Block's physical location, if it exists.
     * Defers actual work to the helper methods in class PhysicalLocation
     *
     * @return PhysicalLocation : this Block's location.
     */
    public PhysicalLocation getPhysicalLocation() {
	// We have our won PhysicalLocation. That's the point.  No need to defer to the Reporter.
	return(PhysicalLocation.getBeanPhysicalLocation(this));
    }

    /** Return this Block's physical location, if it exists.
     * Does not use the parameter s
     * Defers actual work to the helper methods in class PhysicalLocation
     *
     * @param s (this parameter is ignored)
     * @return PhysicalLocation : this Block's location.
     */
    public PhysicalLocation getPhysicalLocation(String s) {
	// We have our won PhysicalLocation. That's the point.  No need to defer to the Reporter.
	// Intentionally ignore the String s
	return(PhysicalLocation.getBeanPhysicalLocation(this));
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Block.class.getName());
}
