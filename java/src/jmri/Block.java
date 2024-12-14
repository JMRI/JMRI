package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import jmri.implementation.AbstractNamedBean;
import jmri.implementation.SignalSpeedMap;
import jmri.util.PhysicalLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a particular piece of track, more informally a "Block".
 * <p>
 * A Block (at least in this implementation) corresponds exactly to the track
 * covered by at most one sensor. That could be generalized in the future.
 * <p>
 * As trains move around the layout, a set of Block objects that are attached to
 * sensors can interact to keep track of which train is where, going in which
 * direction.
 * As a result of this, the set of Block objects pass around "token"
 * (value) Objects representing the trains.
 * This could be e.g. a Throttle to control the train, or something else.
 * <p>
 * A block maintains a "direction" flag that is set from the direction of the
 * incoming train.
 * When an arriving train is detected via the connected sensor
 * and the Block's status information is sufficient to determine that it is
 * arriving via a particular Path, that Path's getFromBlockDirection
 * becomes the direction of the train in this Block.
 * <p>
 * Optionally, a Block can be associated with a Reporter.
 * In this case, the Reporter will provide the Block with the "token" (value).
 * This could be e.g an RFID reader reading an ID tag attached to a locomotive.
 * Depending on the specific Reporter implementation,
 * either the current reported value or the last reported value will be relevant,
 * this can be configured.
 * <p>
 * Objects of this class are Named Beans, so can be manipulated through tables,
 * have listeners, etc.
 * <p>
 * The type letter used in the System Name is 'B' for 'Block'.
 * The default implementation is not system-specific, so a system letter
 * of 'I' is appropriate. This leads to system names like "IB201".
 * <p>
 * Issues:
 * <ul>
 * <li>The tracking doesn't handle a train pulling in behind another well:
 * <ul>
 * <li>When the 2nd train arrives, the Sensor is already active, so the value is
 * unchanged (but the value can only be a single object anyway)
 * <li>When the 1st train leaves, the Sensor stays active, so the value remains
 * that of the 1st train
 * </ul>
 * <li> The assumption is that a train will only go through a set turnout.
 * For example, a train could come into the turnout block from the main even if the
 * turnout is set to the siding. (Ignoring those layouts where this would cause
 * a short; it doesn't do so on all layouts)
 * <li> Does not handle closely-following trains where there is only one
 * electrical block per signal.
 * To do this, it probably needs some type of "assume a train doesn't back up" logic.
 * A better solution is to have multiple
 * sensors and Block objects between each signal head.
 * <li> If a train reverses in a block and goes back the way it came
 * (e.g. b1 to b2 to b1),
 * the block that's re-entered will get an updated direction,
 * but the direction of this block (b2 in the example) is not updated.
 * In other words,
 * we're not noticing that the train must have reversed to go back out.
 * </ul>
 * <p>
 * Do not assume that a Block object uniquely represents a piece of track.
 * To allow independent development, it must be possible for multiple Block objects
 * to take care of a particular section of track.
 * <p>
 * Possible state values:
 * <ul>
 * <li>UNKNOWN - The sensor shows UNKNOWN, so this block doesn't know if it's
 * occupied or not.
 * <li>INCONSISTENT - The sensor shows INCONSISTENT, so this block doesn't know
 * if it's occupied or not.
 * <li>OCCUPIED - This sensor went active. Note that OCCUPIED will be set even
 * if the logic is unable to figure out which value to take.
 * <li>UNOCCUPIED - No content, because the sensor has determined this block is
 * unoccupied.
 * <li>UNDETECTED - No sensor configured.
 * </ul>
 * <p>
 * Possible Curvature attributes (optional)
 * User can set the curvature if desired for use in automatic running of trains,
 * to indicate where slow down is required.
 * <ul>
 * <li>NONE - No curvature in Block track, or Not entered.
 * <li>GRADUAL - Gradual curve - no action by engineer is warranted - full speed
 * OK
 * <li>TIGHT - Tight curve in Block track - Train should slow down some
 * <li>SEVERE - Severe curve in Block track - Train should slow down a lot
 * </ul>
 * <p>
 * The length of the block may also optionally be entered if desired.
 * This attribute is for use in automatic running of trains.
 * Length should be the actual length of model railroad track in the block.
 * It is always stored here in millimeter units.
 * A length of 0.0 indicates no entry of length by the user.
 *
 * <p><a href="doc-files/Block.png"><img src="doc-files/Block.png" alt="State diagram for train tracking" height="33%" width="33%"></a>
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2008, 2014
 * @author Dave Duchamp Copywright (C) 2009
 */

/*
 * @startuml jmri/doc-files/Block.png
 * hide empty description
 * note as N1 #E0E0FF
 *     State diagram for tracking through sequential blocks with train
 *     direction information. "Left" and "Right" refer to blocks on either
 *     side. There's one state machine associated with each block.
 *     Assumes never more than one train in a block, e.g. due to signals.
 * end note
 *
 * state Empty
 *
 * state "Train >>>" as TR
 *
 * state "<<< Train" as TL
 *
 * [*] --> Empty
 *
 * TR -up-> Empty : Goes Unoccupied
 * Empty -down-> TR : Goes Occupied & Left >>>
 * note on link #FFAAAA: Copy Train From Left
 *
 * Empty -down-> TL : Goes Occupied & Right <<<
 * note on link #FFAAAA: Copy Train From Right
 * TL -up-> Empty : Goes Unoccupied

 * TL -right-> TR : Tracked train changes direction to >>>
 * TR -left-> TL : Tracked train changes direction to <<<
 *
 * state "Intervention Required" as IR
 * note bottom of IR #FFAAAA : Something else needs to set Train ID and Direction in Block
 *
 * Empty -right-> IR : Goes Occupied & ! (Left >>> | Right <<<)
 * @enduml
 */

public class Block extends AbstractNamedBean implements PhysicalLocationReporter {

    /**
     * Create a new Block.
     * @param systemName Block System Name.
     */
    public Block(String systemName) {
        super(systemName);
    }

    /**
     * Create a new Block.
     * @param systemName system name.
     * @param userName user name.
     */
    public Block(String systemName, String userName) {
        super(systemName, userName);
    }

    static final public int OCCUPIED = Sensor.ACTIVE;
    static final public int UNOCCUPIED = Sensor.INACTIVE;

    /**
     * Undetected status, i.e a "Dark" block.
     * A Block with unknown status could be waiting on feedback from a Sensor,
     * hence undetected may be more appropriate if no Sensor.
     * <p>
     * OBlocks use this constant in combination with other OBlock status flags.
     * Block uses this constant as initial status, also when a Sensor is unset
     * from the block.
     *
     */
    static final public int UNDETECTED = 0x100;  // bit coded, just in case; really should be enum

    /**
     * No Curvature.
     */
    static final public int NONE = 0x00;

    /**
     * Gradual Curvature.
     */
    static final public int GRADUAL = 0x01;

    /**
     * Tight Curvature.
     */
    static final public int TIGHT = 0x02;

    /**
     * Severe Curvature.
     */
    static final public int SEVERE = 0x04;

    /**
     * Create a Debug String,
     * this should only be used for debugging...
     * @return Block User name, System name, current state as string value.
     */
    public String toDebugString() {
        String result = getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME) + " ";
        switch (getState()) {
            case UNDETECTED: {
                result += "UNDETECTED";
                break;
            }
            case UNOCCUPIED: {
                result += "UNOCCUPIED";
                break;
            }
            case OCCUPIED: {
                result += "OCCUPIED";
                break;
            }
            default: {
                result += "unknown " + getState();
                break;
            }
        }
        return result;
    }

    /**
     * Property name change fired when a Sensor is set to / removed from a Block.
     * The fired event includes
     * old value: Sensor Bean Object if previously set, else null
     * new value: Sensor Bean Object if being set, may be null if Sensor removed.
     */
    public final static String OCC_SENSOR_CHANGE = "OccupancySensorChange"; // NOI18N

    /**
     * Set the sensor by name.
     * Fires propertyChange "OccupancySensorChange" when changed.
     * @param pName the name of the Sensor to set
     * @return true if a Sensor is set and is not null; false otherwise
     */
    public boolean setSensor(String pName) {
        Sensor oldSensor = getSensor();
        if ((pName == null || pName.isEmpty())) {
                if (oldSensor!=null) {
                    setNamedSensor(null);
                    firePropertyChange(OCC_SENSOR_CHANGE, oldSensor, null);
                }
                return false;
        }
        if (InstanceManager.getNullableDefault(SensorManager.class) != null) {
            try {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                if (sensor.equals(oldSensor)) {
                    return false;
                }
                setNamedSensor(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
                firePropertyChange(OCC_SENSOR_CHANGE, oldSensor, sensor);
                return true;
            } catch (IllegalArgumentException ex) {
                setNamedSensor(null);
                firePropertyChange(OCC_SENSOR_CHANGE, oldSensor, null);
                log.error("Sensor '{}' not available", pName);
            }
        } else {
            log.error("No SensorManager for this protocol");
        }
        return false;
    }

    /**
     * Set Block Occupancy Sensor.
     * If Sensor set, Adds PCL, sets Block Occupancy Status to Sensor.
     * Block State PropertyChange Event will fire.
     * Does NOT route initial Sensor Status via goingUnknown() / goingActive() etc.
     * <p>
     * If Sensor null, removes PCL on previous Sensor, sets Block status to UNDETECTED.
     * @param s Handle for Sensor.
     */
    public void setNamedSensor(NamedBeanHandle<Sensor> s) {
        if (_namedSensor != null) {
            if (_sensorListener != null) {
                _namedSensor.getBean().removePropertyChangeListener(_sensorListener);
                _sensorListener = null;
            }
        }
        _namedSensor = s;

        if (_namedSensor != null) {
            _namedSensor.getBean().addPropertyChangeListener(_sensorListener = (PropertyChangeEvent e) -> {
                handleSensorChange(e);
            }, s.getName(), "Block Sensor " + getDisplayName());
            setState(_namedSensor.getBean().getState()); // At present does NOT route via goingUnknown() / goingActive() etc.
        } else {
            setState(UNDETECTED); // Does NOT route via goingUnknown() / goingActive() etc.
        }
    }

    /**
     * Get the Block Occupancy Sensor.
     * @return Sensor if one attached to Block, may be null.
     */
    public Sensor getSensor() {
        if (_namedSensor != null) {
            return _namedSensor.getBean();
        }
        return null;
    }

    public NamedBeanHandle<Sensor> getNamedSensor() {
        return _namedSensor;
    }

    /**
     * Property name change fired when a Sensor is set to / removed from a Block.
     * The fired event includes
     * old value: Sensor Bean Object if previously set, else null
     * new value: Sensor Bean Object if being set, may be null if Sensor removed.
     */
    public final static String BLOCK_REPORTER_CHANGE = "BlockReporterChange"; // NOI18N

    /**
     * Set the Reporter that should provide the data value for this block.
     * Fires propertyChange "BlockReporterChange" when changed.
     * @see Reporter
     * @param reporter Reporter object to link, or null to clear
     */
    public void setReporter(Reporter reporter) {
        if (Objects.equals(reporter,_reporter)) {
            return;
        }
        if (_reporter != null) {
            // remove reporter listener
            if (_reporterListener != null) {
                _reporter.removePropertyChangeListener(_reporterListener);
                _reporterListener = null;
            }
        }
        Reporter oldReporter = _reporter;
        _reporter = reporter;
        if (_reporter != null) {
            // attach listener
            _reporter.addPropertyChangeListener(_reporterListener = (PropertyChangeEvent e) -> {
                handleReporterChange(e);
            });
        }
        firePropertyChange(BLOCK_REPORTER_CHANGE, oldReporter, reporter);
    }

    /**
     * Retrieve the Reporter that is linked to this Block
     *
     * @see Reporter
     * @return linked Reporter object, or null if not linked
     */
    public Reporter getReporter() {
        return _reporter;
    }

    /**
     * Property name change fired when the Block reporting Current flag changes.
     * The fired event includes
     * old value: previous value, Boolean.
     * new value: new value, Boolean.
     */
    public final static String BLOCK_REPORTING_CURRENT = "BlockReportingCurrent"; // NOI18N

    /**
     * Define if the Block's value should be populated from the
     * {@link Reporter#getCurrentReport() current report} or from the
     * {@link Reporter#getLastReport() last report}.
     * Fires propertyChange "BlockReportingCurrent" when changed.
     * @see Reporter
     * @param reportingCurrent true if to use current report; false if to use
     *                         last report
     */
    public void setReportingCurrent(boolean reportingCurrent) {
        if (_reportingCurrent != reportingCurrent) {
            _reportingCurrent = reportingCurrent;
            firePropertyChange(BLOCK_REPORTING_CURRENT, !reportingCurrent, reportingCurrent);
        }
    }

    /**
     * Determine if the Block's value is being populated from the
     * {@link Reporter#getCurrentReport() current report} or from the
     * {@link Reporter#getLastReport() last report}.
     *
     * @see Reporter
     * @return true if populated by
     *         {@link Reporter#getCurrentReport() current report}; false if from
     *         {@link Reporter#getLastReport() last report}.
     */
    public boolean isReportingCurrent() {
        return _reportingCurrent;
    }

    /**
     * Get the Block State.
     * OBlocks may well return a combination of states,
     * Blocks will return a single State.
     * @return Block state.
     */
    @Override
    public int getState() {
        return _current;
    }

    private final ArrayList<Path> paths = new ArrayList<>();

    /**
     * Add a Path to List of Paths.
     * @param p Path to add, not null.
     */
    public void addPath(@Nonnull Path p) {
        if (p == null) {
            throw new IllegalArgumentException("Can't add null path");
        }
        paths.add(p);
    }

    /**
     * Remove a Path from the Block.
     * @param p Path to remove.
     */
    public void removePath(Path p) {
        int j = -1;
        for (int i = 0; i < paths.size(); i++) {
            if (p == paths.get(i)) {
                j = i;
            }
        }
        if (j > -1) {
            paths.remove(j);
        }
    }

    /**
     * Check if Block has a particular Path.
     * @param p Path to test against.
     * @return true if Block has the Path, else false.
     */
    public boolean hasPath(Path p) {
        return paths.stream().anyMatch((t) -> (t.equals(p)));
    }

    /**
     * Get a copy of the list of Paths.
     *
     * @return the paths or an empty list
     */
    @Nonnull
    public List<Path> getPaths() {
        return new ArrayList<>(paths);
    }

    /**
     * Provide a general method for updating the report.
     * Fires propertyChange "state" when called.
     *
     * @param v the new state
     */
    @SuppressWarnings("deprecation")    // The method getId() from the type Thread is deprecated since version 19
                                        // The replacement Thread.threadId() isn't available before version 19
    @Override
    public void setState(int v) {
        int old = _current;
        _current = v;
        // notify

        // It is rather unpleasant that the following needs to be done in a try-catch, but exceptions have been observed
        try {
            firePropertyChange("state", old, _current);
        } catch (Exception e) {
            log.error("{} got exception during firePropertyChange({},{}) in thread {} {}", getDisplayName(), old, _current,
                    Thread.currentThread().getName(), Thread.currentThread().getId(), e);
        }
    }

    /**
     * Set the value retained by this Block.
     * Also used when the Block itself gathers a value from an adjacent Block.
     * This can be overridden in a subclass if
     * e.g. you want to keep track of Blocks elsewhere,
     * but make sure you also eventually invoke the super.setValue() here.
     * Fires propertyChange "value" when changed.
     *
     * @param value The new Object resident in this block, or null if none
     */
    public void setValue(Object value) {
        //ignore if unchanged
        if (value != _value) {
            log.debug("Block {} value changed from '{}' to '{}'", getDisplayName(), _value, value);
            _previousValue = _value;
            _value = value;
            firePropertyChange("value", _previousValue, _value); // NOI18N
        }
    }

    /**
     * Get the Block Contents Value.
     * @return object with current value, could be null.
     */
    public Object getValue() {
        return _value;
    }

    /**
     * Set Block Direction of Travel.
     * Fires propertyChange "direction" when changed.
     * @param direction Path Constant form, see {@link Path Path.java}
     */
    public void setDirection(int direction) {
        //ignore if unchanged
        if (direction != _direction) {
            log.debug("Block {} direction changed from {} to {}", getDisplayName(), Path.decodeDirection(_direction), Path.decodeDirection(direction));
            int oldDirection = _direction;
            _direction = direction;
            // this is a bound parameter
            firePropertyChange("direction", oldDirection, direction); // NOI18N
        }
    }

    /**
     * Get Block Direction of Travel.
     * @return direction in Path Constant form, see {@link Path Path.java}
     */
    public int getDirection() {
        return _direction;
    }

    //Deny traffic entering from this block
    private final ArrayList<NamedBeanHandle<Block>> blockDenyList = new ArrayList<>(1);

    /**
     * Add to the Block Deny List.
     *
     * The block deny list, is used by higher level code, to determine if
     * traffic/trains should be allowed to enter from an attached block, the
     * list only deals with blocks that access should be denied from.
     * <p>
     * If we want to prevent traffic from following from this Block to another,
     * then this Block must be added to the deny list of the other Block.
     * By default no Block is barred, so traffic flow is bi-directional.
     * @param pName name of the block to add, which must exist
     */
    public void addBlockDenyList(@Nonnull String pName) {
        Block blk = InstanceManager.getDefault(BlockManager.class).getBlock(pName);
        if (blk == null) {
            throw new IllegalArgumentException("addBlockDenyList requests block \"" + pName + "\" exists");
        }
        NamedBeanHandle<Block> namedBlock = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(pName, blk);
        if (!blockDenyList.contains(namedBlock)) {
            blockDenyList.add(namedBlock);
        }
    }

    public void addBlockDenyList(Block blk) {
        NamedBeanHandle<Block> namedBlock = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(blk.getDisplayName(), blk);
        if (!blockDenyList.contains(namedBlock)) {
            blockDenyList.add(namedBlock);
        }
    }

    public void removeBlockDenyList(String blk) {
        NamedBeanHandle<Block> toremove = null;
        for (NamedBeanHandle<Block> bean : blockDenyList) {
            if (bean.getName().equals(blk)) {
                toremove = bean;
            }
        }
        if (toremove != null) {
            blockDenyList.remove(toremove);
        }
    }

    public void removeBlockDenyList(Block blk) {
        NamedBeanHandle<Block> toremove = null;
        for (NamedBeanHandle<Block> bean : blockDenyList) {
            if (bean.getBean() == blk) {
                toremove = bean;
            }
        }
        if (toremove != null) {
            blockDenyList.remove(toremove);
        }
    }

    public List<String> getDeniedBlocks() {
        List<String> list = new ArrayList<>(blockDenyList.size());
        blockDenyList.forEach((bean) -> {
            list.add(bean.getName());
        });
        return list;
    }

    public boolean isBlockDenied(String deny) {
        return blockDenyList.stream().anyMatch((bean) -> (bean.getName().equals(deny)));
    }

    public boolean isBlockDenied(Block deny) {
        return blockDenyList.stream().anyMatch((bean) -> (bean.getBean() == deny));
    }

    /**
     * Get if Block can have permissive working.
     * Blocks default to non-permissive, i.e. false.
     * @return true if permissive, else false.
     */
    public boolean getPermissiveWorking() {
        return _permissiveWorking;
    }

    /**
     * Property name change fired when the Block Permissive Status changes.
     * The fired event includes
     * old value: previous permissive status.
     * new value: new permissive status.
     */
    public final static String BLOCK_PERMISSIVE_CHANGE = "BlockPermissiveWorking"; // NOI18N

    /**
     * Set Block as permissive.
     * Fires propertyChange "BlockPermissiveWorking" when changed.
     * @param w true permissive, false NOT permissive
     */
    public void setPermissiveWorking(boolean w) {
        if (_permissiveWorking != w) {
            _permissiveWorking = w;
            firePropertyChange(BLOCK_PERMISSIVE_CHANGE, !w, w); // NOI18N
        }
    }

    private boolean _permissiveWorking = false;

    public float getSpeedLimit() {
        if ((_blockSpeed == null) || (_blockSpeed.isEmpty())) {
            return -1;
        }
        String speed = _blockSpeed;
        if (_blockSpeed.equals("Global")) {
            speed = InstanceManager.getDefault(BlockManager.class).getDefaultSpeed();
        }

        try {
            return Float.parseFloat(speed);
        } catch (NumberFormatException nx) {
            //considered normal if the speed is not a number.
        }
        try {
            return InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(speed);
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }

    private String _blockSpeed = "";

    public String getBlockSpeed() {
        if (_blockSpeed.equals("Global")) {
            return (Bundle.getMessage("UseGlobal", "Global") + " " + InstanceManager.getDefault(BlockManager.class).getDefaultSpeed());
            // Ensure the word "Global" is always in the speed name for later comparison
        }
        return _blockSpeed;
    }

    /**
     * Property name change fired when the Block Speed changes.
     * The fired event includes
     * old value: previous speed String.
     * new value: new speed String.
     */
    public final static String BLOCK_SPEED_CHANGE = "BlockSpeedChange"; // NOI18N

    /**
     * Set the Block Speed Name.
     * <p>
     * Does not perform name validity checking.
     * Does not send Property Change Event.
     * @param s new Speed Name String.
     */
    public void setBlockSpeedName(String s) {
        if (s == null) {
            _blockSpeed = "";
        } else {
            _blockSpeed = s;
        }
    }

    /**
     * Set the Block Speed, preferred method.
     * <p>
     * Fires propertyChange "BlockSpeedChange" when changed.
     * @param s Speed String
     * @throws JmriException if Value of requested block speed is not valid.
     */
    public void setBlockSpeed(String s) throws JmriException {
        if ((s == null) || (_blockSpeed.equals(s))) {
            return;
        }
        if (s.contains("Global")) {
            s = "Global";
        } else {
            try {
                Float.parseFloat(s);
            } catch (NumberFormatException nx) {
                try {
                    InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(s);
                } catch (IllegalArgumentException ex) {
                    throw new JmriException("Value of requested block speed is not valid");
                }
            }
        }
        String oldSpeed = _blockSpeed;
        _blockSpeed = s;
        firePropertyChange(BLOCK_SPEED_CHANGE, oldSpeed, s);
    }

    /**
     * Property name change fired when the Block Curvature changes.
     * The fired event includes
     * old value: previous Block Curvature Constant.
     * new value: new Block Curvature Constant.
     */
    public final static String BLOCK_CURVATURE_CHANGE = "BlockCurvatureChange"; // NOI18N

    /**
     * Set Block Curvature Constant.
     * Valid values :
     * Block.NONE, Block.GRADUAL, Block.TIGHT, Block.SEVERE
     * Fires propertyChange "BlockCurvatureChange"  when changed.
     * @param c Constant, e.g. Block.GRADUAL
     */
    public void setCurvature(int c) {
        if (_curvature!=c) {
            int oldCurve = _curvature;
            _curvature = c;
            firePropertyChange(BLOCK_CURVATURE_CHANGE, oldCurve, c);
        }
    }

    /**
     * Get Block Curvature Constant.
     * Defaults to Block.NONE
     * @return constant, e.g. Block.TIGHT
     */
    public int getCurvature() {
        return _curvature;
    }

    /**
     * Property name change fired when the Block Length changes.
     * The fired event includes
     * old value: previous float length (mm).
     * new value: new float length (mm).
     */
    public final static String BLOCK_LENGTH_CHANGE = "BlockLengthChange"; // NOI18N

    /**
     * Set length in millimeters.
     * Paths will inherit this length, if their length is not specifically set.
     * This length is the maximum length of any Path in the block.
     * Path lengths exceeding this will be set to the default length.
     * <p>
     * Fires propertyChange "BlockLengthChange"  when changed, float values in mm.
     * @param l length in millimeters
     */
    public void setLength(float l) {
        float oldLen = getLengthMm();
        if (Math.abs(oldLen - l) > 0.0001){ // length value is different
            _length = l;
            getPaths().stream().forEach(p -> {
                if (p.getLength() > l) {
                    p.setLength(0); // set to default
                }
            });
            firePropertyChange(BLOCK_LENGTH_CHANGE, oldLen, l);
        }
    }

    /**
     * Get Block Length in Millimetres.
     * Default 0.0f.
     * @return length in mm.
     */
    public float getLengthMm() {
        return _length;
    }

    /**
     * Get Block Length in Centimetres.
     * Courtesy method using result from getLengthMm.
     * @return length in centimetres.
     */
    public float getLengthCm() {
        return (_length / 10.0f);
    }

    /**
     * Get Block Length in Inches.
     * Courtesy method using result from getLengthMm.
     * @return length in inches.
     */
    public float getLengthIn() {
        return (_length / 25.4f);
    }

    /**
     * Note: this has to make choices about identity values (always the same)
     * and operation values (can change as the block works). Might be missing
     * some identity values.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(getClass() == obj.getClass())) {
            return false;
        } else {
            Block b = (Block) obj;
            return b.getSystemName().equals(this.getSystemName());
        }
    }

    @Override
    // This can't change, so can't include mutable values
    public int hashCode() {
        return this.getSystemName().hashCode();
    }

    // internal data members
    private int _current = UNDETECTED; // state until sensor is set
    //private Sensor _sensor = null;
    private NamedBeanHandle<Sensor> _namedSensor = null;
    private PropertyChangeListener _sensorListener = null;
    private Object _value;
    private Object _previousValue;
    private int _direction;
    private int _curvature = NONE;
    private float _length = 0.0f;  // always stored in millimeters
    private Reporter _reporter = null;
    private PropertyChangeListener _reporterListener = null;
    private boolean _reportingCurrent = false;

    private Path[] pListOfPossibleEntrancePaths = null;
    private int cntOfPossibleEntrancePaths = 0;

    void resetCandidateEntrancePaths() {
        pListOfPossibleEntrancePaths = null;
        cntOfPossibleEntrancePaths = 0;
    }

    boolean setAsEntryBlockIfPossible(Block b) {
        for (int i = 0; i < cntOfPossibleEntrancePaths; i++) {
            Block CandidateBlock = pListOfPossibleEntrancePaths[i].getBlock();
            if (CandidateBlock == b) {
                setValue(CandidateBlock.getValue());
                setDirection(pListOfPossibleEntrancePaths[i].getFromBlockDirection());
                log.info("Block {} gets LATE new value from {}, direction= {}", getDisplayName(), CandidateBlock.getDisplayName(), Path.decodeDirection(getDirection()));
                resetCandidateEntrancePaths();
                return true;
            }
        }
        return false;
    }

    /**
     * Handle change in sensor state.
     * <p>
     * Defers real work to goingActive, goingInactive methods.
     *
     * @param e the event
     */
    void handleSensorChange(PropertyChangeEvent e) {
        Sensor s = getSensor();
        if (e.getPropertyName().equals("KnownState") && s!=null) {
            int state = s.getState();
            switch (state) {
                case Sensor.ACTIVE:
                    goingActive();
                    break;
                case Sensor.INACTIVE:
                    goingInactive();
                    break;
                case Sensor.UNKNOWN:
                    goingUnknown();
                    break;
                default:
                    goingInconsistent();
                    break;
            }
        }
    }

    public void goingUnknown() {
        setValue(null);
        setState(UNKNOWN);
    }

    public void goingInconsistent() {
        setValue(null);
        setState(INCONSISTENT);
    }

    /**
     * Handle change in Reporter value.
     *
     * @param e PropertyChangeEvent
     */
    void handleReporterChange(PropertyChangeEvent e) {
        if ((_reportingCurrent && e.getPropertyName().equals("currentReport"))
                || (!_reportingCurrent && e.getPropertyName().equals("lastReport"))) {
            setValue(e.getNewValue());
        }
    }

    private Instant _timeLastInactive;

    /**
     * Handles Block sensor going INACTIVE: this block is empty
     */
    public void goingInactive() {
        log.debug("Block {} goes UNOCCUPIED", getDisplayName());
        for (Path path : paths) {
            Block b = path.getBlock();
            if (b != null) {
                b.setAsEntryBlockIfPossible(this);
            }
        }
        setValue(null);
        setDirection(Path.NONE);
        setState(UNOCCUPIED);
        _timeLastInactive = Instant.now();
    }

    private final int maxInfoMessages = 5;
    private int infoMessageCount = 0;

    /**
     * Handles Block sensor going ACTIVE: this block is now occupied, figure out
     * from who and copy their value.
     */
    public void goingActive() {
        if (getState() == OCCUPIED) {
            return;
        }
        log.debug("Block {} goes OCCUPIED", getDisplayName());
        resetCandidateEntrancePaths();
        // index through the paths, counting
        int count = 0;
        Path next = null;
        // get statuses of everything once
        int currPathCnt = paths.size();
        Path[] pList = new Path[currPathCnt];
        boolean[] isSet = new boolean[currPathCnt];
        boolean[] isActive = new boolean[currPathCnt];
        int[] pDir = new int[currPathCnt];
        int[] pFromDir = new int[currPathCnt];
        for (int i = 0; i < currPathCnt; i++) {
            pList[i] = paths.get(i);
            isSet[i] = pList[i].checkPathSet();
            Block b = pList[i].getBlock();
            if (b != null) {
                isActive[i] = b.getState() == OCCUPIED;
                pDir[i] = b.getDirection();
            } else {
                isActive[i] = false;
                pDir[i] = -1;
            }
            pFromDir[i] = pList[i].getFromBlockDirection();
            if (isSet[i] && isActive[i]) {
                count++;
                next = pList[i];
            }
        }
        // sort on number of neighbors
        switch (count) {
            case 0:
                if (null != _previousValue) {
                    // restore the previous value under either of these circumstances:
                    // 1. the block has been 'unoccupied' only very briefly
                    // 2. power has just come back on
                    Instant tn = Instant.now();
                    BlockManager bm = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
                    if (bm.timeSinceLastLayoutPowerOn() < 5000 || (_timeLastInactive != null && tn.toEpochMilli() - _timeLastInactive.toEpochMilli() < 2000)) {
                        setValue(_previousValue);
                        if (infoMessageCount < maxInfoMessages) {
                            log.debug("Sensor ACTIVE came out of nowhere, no neighbors active for block {}. Restoring previous value.", getDisplayName());
                            infoMessageCount++;
                        }
                    } else if (log.isDebugEnabled()) {
                        if (null != _timeLastInactive) {
                            log.debug("not restoring previous value, block {} has been inactive for too long ({}ms) and layout power has not just been restored ({}ms ago)", getDisplayName(), tn.toEpochMilli() - _timeLastInactive.toEpochMilli(), bm.timeSinceLastLayoutPowerOn());
                        } else {
                            log.debug("not restoring previous value, block {} has been inactive since the start of this session and layout power has not just been restored ({}ms ago)", getDisplayName(), bm.timeSinceLastLayoutPowerOn());
                        }
                    }
                } else {
                    if (infoMessageCount < maxInfoMessages) {
                        log.debug("Sensor ACTIVE came out of nowhere, no neighbors active for block {}. Value not set.", getDisplayName());
                        infoMessageCount++;
                    }
                }
                break;
            case 1:
                // simple case
                if ((next != null) && (next.getBlock() != null)) {
                    // normal case, transfer value object
                    setValue(next.getBlock().getValue());
                    setDirection(next.getFromBlockDirection());
                    log.debug("Block {} gets new value '{}' from {}, direction={}",
                            getDisplayName(),
                            next.getBlock().getValue(),
                            next.getBlock().getDisplayName(),
                            Path.decodeDirection(getDirection()));
                } else if (next == null) {
                    log.error("unexpected next==null processing block {}", getDisplayName());
                } else {
                    log.error("unexpected next.getBlock()=null processing block {}", getDisplayName());
                }
                break;
            default:
                // count > 1, check for one with proper direction
                // this time, count ones with proper direction
                log.debug("Block {} has {} active linked blocks, comparing directions", getDisplayName(), count);
                next = null;
                count = 0;
                boolean allNeighborsAgree = true;  // true until it's found that some neighbor blocks contain different contents (trains)

                // scan for neighbors without matching direction
                for (int i = 0; i < currPathCnt; i++) {
                    if (isSet[i] && isActive[i]) {  //only consider active reachable blocks
                        log.debug("comparing {} ({}) to {} ({})",
                                pList[i].getBlock().getDisplayName(), Path.decodeDirection(pDir[i]),
                                getDisplayName(), Path.decodeDirection(pFromDir[i]));
                        if ((pDir[i] & pFromDir[i]) > 0) { //use bitwise comparison to support combination directions such as "North, West"
                            if (next != null && next.getBlock() != null && next.getBlock().getValue() != null &&
                                    ! next.getBlock().getValue().equals(pList[i].getBlock().getValue())) {
                                allNeighborsAgree = false;
                            }
                            count++;
                            next = pList[i];
                        }
                    }
                }

                // If loop above didn't find neighbors with matching direction, scan w/out direction for neighbors
                // This is used when directions are not being used
                if (next == null) {
                    for (int i = 0; i < currPathCnt; i++) {
                        if (isSet[i] && isActive[i]) {
                            if (next != null && next.getBlock() != null && next.getBlock().getValue() != null &&
                                    ! next.getBlock().getValue().equals(pList[i].getBlock().getValue())) {
                                allNeighborsAgree = false;
                            }
                            count++;
                            next = pList[i];
                        }
                    }
                }

                if (next != null && count == 1) {
                    // found one block with proper direction, use it
                    setValue(next.getBlock().getValue());
                    setDirection(next.getFromBlockDirection());
                    log.debug("Block {} gets new value '{}' from {}, direction {}",
                            getDisplayName(), next.getBlock().getValue(),
                            next.getBlock().getDisplayName(), Path.decodeDirection(getDirection()));
                } else {
                    // handle merging trains: All neighbors with same content (train ID)
                    if (allNeighborsAgree && next != null) {
                        setValue(next.getBlock().getValue());
                        setDirection(next.getFromBlockDirection());
                    } else {
                    // don't all agree, so can't determine unique value
                        log.warn("count of {} ACTIVE neighbors with proper direction can't be handled for block {} but maybe it can be determined when another block becomes free", count, getDisplayName());
                        pListOfPossibleEntrancePaths = new Path[currPathCnt];
                        cntOfPossibleEntrancePaths = 0;
                        for (int i = 0; i < currPathCnt; i++) {
                            if (isSet[i] && isActive[i]) {
                                pListOfPossibleEntrancePaths[cntOfPossibleEntrancePaths] = pList[i];
                                cntOfPossibleEntrancePaths++;
                            }
                        }
                    }
                }
                break;
        }
        setState(OCCUPIED);
    }

    /**
     * Find which path this Block became Active, without actually modifying the
     * state of this block.
     * <p>
     * (this is largely a copy of the 'Search' part of the logic from
     * goingActive())
     *
     * @return the next path
     */
    public Path findFromPath() {
        // index through the paths, counting
        int count = 0;
        Path next = null;
        // get statuses of everything once
        int currPathCnt = paths.size();
        Path[] pList = new Path[currPathCnt];
        boolean[] isSet = new boolean[currPathCnt];
        boolean[] isActive = new boolean[currPathCnt];
        int[] pDir = new int[currPathCnt];
        int[] pFromDir = new int[currPathCnt];
        for (int i = 0; i < currPathCnt; i++) {
            pList[i] = paths.get(i);
            isSet[i] = pList[i].checkPathSet();
            Block b = pList[i].getBlock();
            if (b != null) {
                isActive[i] = b.getState() == OCCUPIED;
                pDir[i] = b.getDirection();
            } else {
                isActive[i] = false;
                pDir[i] = -1;
            }
            pFromDir[i] = pList[i].getFromBlockDirection();
            if (isSet[i] && isActive[i]) {
                count++;
                next = pList[i];
            }
        }
        // sort on number of neighbors
        if ((count == 0) || (count == 1)) {
            // do nothing.  OK to return null from this function.  "next" is already set.
        } else {
            // count > 1, check for one with proper direction
            // this time, count ones with proper direction
            log.debug("Block {} - count of active linked blocks = {}", getDisplayName(), count);
            next = null;
            count = 0;
            for (int i = 0; i < currPathCnt; i++) {
                if (isSet[i] && isActive[i]) {  //only consider active reachable blocks
                    log.debug("comparing {} ({}) to {} ({})",
                            pList[i].getBlock().getDisplayName(), Path.decodeDirection(pDir[i]),
                            getDisplayName(), Path.decodeDirection(pFromDir[i]));
                    if ((pDir[i] & pFromDir[i]) > 0) { //use bitwise comparison to support combination directions such as "North, West"
                        count++;
                        next = pList[i];
                    }
                }
            }
            if (next == null) {
                log.debug("next is null!");
            }
            if (next != null && count == 1) {
                // found one block with proper direction, assume that
            } else {
                // no unique path with correct direction - this happens frequently from noise in block detectors!!
                log.warn("count of {} ACTIVE neighbors with proper direction can't be handled for block {}", count, getDisplayName());
            }
        }
        // in any case, go OCCUPIED
        if (log.isDebugEnabled()) { // avoid potentially expensive non-logging
            log.debug("Block {} with direction {} gets new value from {} + (informational. No state change)", getDisplayName(), Path.decodeDirection(getDirection()), (next != null ? next.getBlock().getDisplayName() : "(no next block)"));
        }
        return (next);
    }

    /**
     * This allows the layout block to inform any listeners to the block
     * that the higher level layout block has been set to "useExtraColor" which is an
     * indication that it has been allocated to a section by the AutoDispatcher.
     * The value set is not retained in any form by the block,
     * it is purely to trigger a propertyChangeEvent.
     * @param boo Allocation status
     */
    public void setAllocated(Boolean boo) {
        firePropertyChange("allocated", !boo, boo);
    }

    // Methods to implmement PhysicalLocationReporter Interface
    //
    // If we have a Reporter that is also a PhysicalLocationReporter,
    // we will defer to that Reporter's methods.
    // Else we will assume a LocoNet style message to be parsed.

    /**
     * Parse a given string and return the LocoAddress value that is presumed
     * stored within it based on this object's protocol. The Class Block
     * implementation defers to its associated Reporter, if it exists.
     *
     * @param rep String to be parsed
     * @return LocoAddress address parsed from string, or null if this Block
     *         isn't associated with a Reporter, or is associated with a
     *         Reporter that is not also a PhysicalLocationReporter
     */
    @Override
    public LocoAddress getLocoAddress(String rep) {
        // Defer parsing to our associated Reporter if we can.
        if (rep == null) {
            log.warn("String input is null!");
            return (null);
        }
        if ((this.getReporter() != null) && (this.getReporter() instanceof PhysicalLocationReporter)) {
            return (((PhysicalLocationReporter) this.getReporter()).getLocoAddress(rep));
        } else {
            // Assume a LocoNet-style report.  This is (nascent) support for handling of Faller cars
            // for Dave Merrill's project.
            log.debug("report string: {}", rep);
            // NOTE: This pattern is based on the one defined in LocoNet-specific LnReporter
            Pattern ln_p = Pattern.compile("(\\d+) (enter|exits|seen)\\s*(northbound|southbound)?");  // Match a number followed by the word "enter".  This is the LocoNet pattern.
            Matcher m = ln_p.matcher(rep);
            if (m.find()) {
                log.debug("Parsed address: {}", m.group(1));
                return (new DccLocoAddress(Integer.parseInt(m.group(1)), LocoAddress.Protocol.DCC));
            } else {
                return (null);
            }
        }
    }

    /**
     * Parses out a (possibly old) LnReporter-generated report string to extract
     * the direction from within it based on this object's protocol. The Class
     * Block implementation defers to its associated Reporter, if it exists.
     *
     * @param rep String to be parsed
     * @return PhysicalLocationReporter.Direction direction parsed from string,
     *         or null if this Block isn't associated with a Reporter, or is
     *         associated with a Reporter that is not also a
     *         PhysicalLocationReporter
     */
    @Override
    public PhysicalLocationReporter.Direction getDirection(String rep) {
        if (rep == null) {
            log.warn("String input is null!");
            return (null);
        }
        // Defer parsing to our associated Reporter if we can.
        if ((this.getReporter() != null) && (this.getReporter() instanceof PhysicalLocationReporter)) {
            return (((PhysicalLocationReporter) this.getReporter()).getDirection(rep));
        } else {
            log.debug("report string: {}", rep);
            // NOTE: This pattern is based on the one defined in LocoNet-specific LnReporter
            Pattern ln_p = Pattern.compile("(\\d+) (enter|exits|seen)\\s*(northbound|southbound)?");  // Match a number followed by the word "enter".  This is the LocoNet pattern.
            Matcher m = ln_p.matcher(rep);
            if (m.find()) {
                log.debug("Parsed direction: {}", m.group(2));
                switch (m.group(2)) {
                    case "enter":
                        // LocoNet Enter message
                        return (PhysicalLocationReporter.Direction.ENTER);
                    case "seen":
                        // Lissy message.  Treat them all as "entry" messages.
                        return (PhysicalLocationReporter.Direction.ENTER);
                    default:
                        return (PhysicalLocationReporter.Direction.EXIT);
                }
            } else {
                return (PhysicalLocationReporter.Direction.UNKNOWN);
            }
        }
    }

    /**
     * Return this Block's physical location, if it exists.
     * Defers actual work to the helper methods in class PhysicalLocation.
     *
     * @return PhysicalLocation : this Block's location.
     */
    @Override
    public PhysicalLocation getPhysicalLocation() {
        // We have our won PhysicalLocation. That's the point.  No need to defer to the Reporter.
        return (PhysicalLocation.getBeanPhysicalLocation(this));
    }

    /**
     * Return this Block's physical location, if it exists.
     * Does not use the parameter s.
     * Defers actual work to the helper methods in class PhysicalLocation
     *
     * @param s (this parameter is ignored)
     * @return PhysicalLocation : this Block's location.
     */
    @Override
    public PhysicalLocation getPhysicalLocation(String s) {
        // We have our won PhysicalLocation. That's the point.  No need to defer to the Reporter.
        // Intentionally ignore the String s
        return (PhysicalLocation.getBeanPhysicalLocation(this));
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getSensor())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
            if (evt.getOldValue() instanceof Reporter) {
                if (evt.getOldValue().equals(getReporter())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getSensor())) {
                    setSensor(null);
                }
            }
            if (evt.getOldValue() instanceof Reporter) {
                if (evt.getOldValue().equals(getReporter())) {
                    setReporter(null);
                }
            }
        }
    }

    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            if (bean.equals(getSensor())) {
                report.add(new NamedBeanUsageReport("BlockSensor"));  // NOI18N
            }
            if (bean.equals(getReporter())) {
                report.add(new NamedBeanUsageReport("BlockReporter"));  // NOI18N
            }
            // Block paths
            getPaths().forEach((path) -> {
                if (bean.equals(path.getBlock())) {
                    report.add(new NamedBeanUsageReport("BlockPathNeighbor"));  // NOI18N
                }
                path.getSettings().forEach((setting) -> {
                    if (bean.equals(setting.getBean())) {
                        report.add(new NamedBeanUsageReport("BlockPathTurnout"));  // NOI18N
                    }
                });
            });
        }
        return report;
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameBlock");
    }

    private final static Logger log = LoggerFactory.getLogger(Block.class);
}
