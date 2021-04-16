package jmri;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.beans.*;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.implementation.AbstractNamedBean;

import jmri.jmrit.display.layoutEditor.ConnectivityUtil; // normally these would be rolloed
import jmri.jmrit.display.layoutEditor.HitPointType;     // up into jmri.jmrit.display.layoutEditor.*
import jmri.jmrit.display.layoutEditor.LayoutBlock;      // but during the LE migration it's
import jmri.jmrit.display.layoutEditor.LayoutBlockManager; // useful to be able to see 
import jmri.jmrit.display.layoutEditor.LayoutEditor;     // what specific classe are used.
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.layoutEditor.TrackNode;
import jmri.jmrit.display.layoutEditor.TrackSegment;

import jmri.util.JmriJFrame;
import jmri.util.NonNullArrayList;

/**
 * Sections represent a group of one or more connected Blocks that may be
 * allocated to a train traveling in a given direction.
 * <p>
 * A Block may be in multiple Sections. All Blocks contained in a given section
 * must be unique. Blocks are kept in order--the first block is connected to the
 * second, the second is connected to the third, etc.
 * <p>
 * A Block in a Section must be connected to the Block before it (if there is
 * one) and to the Block after it (if there is one), but may not be connected to
 * any other Block in the Section. This restriction is enforced when a Section
 * is created, and checked when a Section is loaded from disk.
 * <p>
 * A Section has a "direction" defined by the sequence in which Blocks are added
 * to the Section. A train may run through a Section in either the forward
 * direction (from first block to last block) or reverse direction (from last
 * block to first block).
 * <p>
 * A Section has one or more EntryPoints. Each EntryPoint is a Path of one of
 * the Blocks in the Section that defines a connection to a Block outside of the
 * Section. EntryPoints are grouped into two lists: "forwardEntryPoints" - entry
 * through which will result in a train traveling in the "forward" direction
 * "reverseEntryPoints" - entry through which will result in a train traveling
 * in the "reverse" direction Note that "forwardEntryPoints" are also reverse
 * exit points, and vice versa.
 * <p>
 * A Section has one of the following states" FREE - available for allocation by
 * a dispatcher FORWARD - allocated for travel in the forward direction REVERSE
 * - allocated for travel in the reverse direction
 * <p>
 * A Section has an occupancy. A Section is OCCUPIED if any of its Blocks is
 * OCCUPIED. A Section is UNOCCUPIED if all of its Blocks are UNOCCUPIED
 * <p>
 * A Section of may be allocated to only one train at a time, even if the trains
 * are travelling in the same direction. If a Section has sufficient space for
 * multiple trains travelling in the same direction it should be broken up into
 * multiple Sections so the trains can follow each other through the original
 * Section.
 * <p>
 * A Section may not contain any reverse loops. The track that is reversed in a
 * reverse loop must be in a separate Section.
 * <p>
 * Each Section optionally carries two direction sensors, one for the forward
 * direction and one for the reverse direction. These sensors force signals for
 * travel in their respective directions to "RED" when they are active. When the
 * Section is free, both the sensors are Active. These internal sensors follow
 * the state of the Section, permitting signals to function normally in the
 * direction of allocation.
 * <p>
 * Each Section optionally carries two stopping sensors, one for the forward
 * direction and one for the reverse direction. These sensors change to active
 * when a train traversing the Section triggers its sensing device. Stopping
 * sensors are physical layout sensors, and may be either point sensors or
 * occupancy sensors for short blocks at the end of the Section. A stopping
 * sensor is used during automatic running to stop a train that has reached the
 * end of its allocated Section. This is needed, for example, to allow a train
 * to enter a passing siding and clear the track behind it. When not running
 * automatically, these sensors may be used to light panel lights to notify the
 * dispatcher that the train has reached the end of the Section.
 * <p>
 * This Section implementation provides for delayed initialization of blocks and
 * direction sensors to be independent of order of items in panel files.
 *
 * @author Dave Duchamp Copyright (C) 2008,2010
 */
public class Section extends AbstractNamedBean {

    private static final NamedBean.DisplayOptions USERSYS = NamedBean.DisplayOptions.USERNAME_SYSTEMNAME;
    
    /**
     * The value of {@link #getState()} if section is available for allocation.
     */
    public static final int FREE = 0x02;
    
    /**
     * The value of {@link #getState()} if section is allocated for travel in
     * the forward direction.
     */
    public static final int FORWARD = 0x04;
    
    /**
     * The value of {@link #getState()} if section is allocated for travel in
     * the reverse direction.
     */
    public static final int REVERSE = 0X08;

    public Section(String systemName, String userName) {
        super(systemName, userName);
    }

    public Section(String systemName) {
        super(systemName);
    }

    /**
     * Value representing an occupied section.
     */
    public static final int OCCUPIED = Block.OCCUPIED;
    
    /**
     * Value representing an unoccupied section.
     */
    public static final int UNOCCUPIED = Block.UNOCCUPIED;
    
    /**
     * Persistent instance variables (saved between runs)
     */
    private String mForwardBlockingSensorName = "";
    private String mReverseBlockingSensorName = "";
    private String mForwardStoppingSensorName = "";
    private String mReverseStoppingSensorName = "";
    private final List<Block> mBlockEntries = new NonNullArrayList<>();
    private final List<EntryPoint> mForwardEntryPoints = new NonNullArrayList<>();
    private final List<EntryPoint> mReverseEntryPoints = new NonNullArrayList<>();

    /**
     * Operational instance variables (not saved between runs).
     */
    private int mState = FREE;
    private int mOccupancy = UNOCCUPIED;
    private boolean mOccupancyInitialized = false;
    private Block mFirstBlock = null;
    private Block mLastBlock = null;

    private NamedBeanHandle<Sensor> mForwardBlockingNamedSensor = null;
    private NamedBeanHandle<Sensor> mReverseBlockingNamedSensor = null;
    private NamedBeanHandle<Sensor> mForwardStoppingNamedSensor = null;
    private NamedBeanHandle<Sensor> mReverseStoppingNamedSensor = null;

    private final List<PropertyChangeListener> mBlockListeners = new ArrayList<>();
    protected jmri.NamedBeanHandleManager nbhm = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    /**
     * Get the state of the Section.
     * UNKNOWN, FORWARD, REVERSE, FREE
     *
     * @return the section state
     */
    @Override
    public int getState() {
        return mState;
    }

    /**
     * Set the state of the Section.
     * FREE, FORWARD or REVERSE.
     * <br>
     * UNKNOWN state not accepted here.
     * @param state the state to set
     */
    @Override
    public void setState(int state) {
        if ((state == Section.FREE) || (state == Section.FORWARD) || (state == Section.REVERSE)) {
            int old = mState;
            mState = state;
            firePropertyChange("state", old, mState);
            // update the forward/reverse blocking sensors as needed
            switch (state) {
                case FORWARD:
                    try {
                        if ((getForwardBlockingSensor() != null) && (getForwardBlockingSensor().getState() != Sensor.INACTIVE)) {
                            getForwardBlockingSensor().setState(Sensor.INACTIVE);
                        }
                        if ((getReverseBlockingSensor() != null) && (getReverseBlockingSensor().getState() != Sensor.ACTIVE)) {
                            getReverseBlockingSensor().setKnownState(Sensor.ACTIVE);
                        }
                    } catch (jmri.JmriException reason) {
                        log.error("Exception when setting Sensors for Section {}", getDisplayName(USERSYS));
                    }
                    break;
                case REVERSE:
                    try {
                        if ((getReverseBlockingSensor() != null) && (getReverseBlockingSensor().getState() != Sensor.INACTIVE)) {
                            getReverseBlockingSensor().setKnownState(Sensor.INACTIVE);
                        }
                        if ((getForwardBlockingSensor() != null) && (getForwardBlockingSensor().getState() != Sensor.ACTIVE)) {
                            getForwardBlockingSensor().setKnownState(Sensor.ACTIVE);
                        }
                    } catch (jmri.JmriException reason) {
                        log.error("Exception when setting Sensors for Section {}", getDisplayName(USERSYS));
                    }
                    break;
                case FREE:
                    try {
                        if ((getForwardBlockingSensor() != null) && (getForwardBlockingSensor().getState() != Sensor.ACTIVE)) {
                            getForwardBlockingSensor().setKnownState(Sensor.ACTIVE);
                        }
                        if ((getReverseBlockingSensor() != null) && (getReverseBlockingSensor().getState() != Sensor.ACTIVE)) {
                            getReverseBlockingSensor().setKnownState(Sensor.ACTIVE);
                        }
                    } catch (jmri.JmriException reason) {
                        log.error("Exception when setting Sensors for Section {}", getDisplayName(USERSYS));
                    }
                    break;
                default:
                    break;
            }
        } else {
            log.error("Attempt to set state of Section {} to illegal value - {}", getDisplayName(USERSYS), state);
        }
    }

    /**
     * Get the occupancy of a Section.
     *
     * @return {@link #OCCUPIED}, {@link #UNOCCUPIED}, or the state of the first
     *         block that is neither occupied or unoccupied
     */
    public int getOccupancy() {
        if (mOccupancyInitialized) {
            return mOccupancy;
        }
        // initialize occupancy
        mOccupancy = UNOCCUPIED;
        for (Block block : mBlockEntries) {
            if (block.getState() == OCCUPIED) {
                mOccupancy = OCCUPIED;
            } else if (block.getState() != UNOCCUPIED) {
                log.warn("Occupancy of block {} is not OCCUPIED or UNOCCUPIED in Section - {}",
                        block.getDisplayName(USERSYS), getDisplayName(USERSYS));
                return (block.getState());
            }
        }
        mOccupancyInitialized = true;
        return mOccupancy;
    }

    private void setOccupancy(int occupancy) {
        int old = mOccupancy;
        mOccupancy = occupancy;
        firePropertyChange("occupancy", old, mOccupancy);
    }

    public String getForwardBlockingSensorName() {
        if (mForwardBlockingNamedSensor != null) {
            return mForwardBlockingNamedSensor.getName();
        }
        return mForwardBlockingSensorName;
    }

    public Sensor getForwardBlockingSensor() {
        if (mForwardBlockingNamedSensor != null) {
            return mForwardBlockingNamedSensor.getBean();
        }
        if ((mForwardBlockingSensorName != null)
                && (!mForwardBlockingSensorName.isEmpty())) {
            Sensor s = InstanceManager.sensorManagerInstance().
                    getSensor(mForwardBlockingSensorName);
            if (s == null) {
                log.error("Missing Sensor - {} - when initializing Section - {}",
                        mForwardBlockingSensorName, getDisplayName(USERSYS));
                return null;
            }
            mForwardBlockingNamedSensor = nbhm.getNamedBeanHandle(mForwardBlockingSensorName, s);
            return s;
        }
        return null;
    }

    public Sensor setForwardBlockingSensorName(String forwardSensor) {
        if ((forwardSensor == null) || (forwardSensor.length() <= 0)) {
            mForwardBlockingSensorName = "";
            mForwardBlockingNamedSensor = null;
            return null;
        }
        tempSensorName = forwardSensor;
        Sensor s = validateSensor();
        if (s == null) {
            // sensor name not correct or not in sensor table
            log.error("Sensor name - {} invalid when setting forward sensor in Section {}",
                    forwardSensor, getDisplayName(USERSYS));
            return null;
        }
        mForwardBlockingNamedSensor = nbhm.getNamedBeanHandle(tempSensorName, s);
        mForwardBlockingSensorName = tempSensorName;
        return s;
    }

    public void delayedSetForwardBlockingSensorName(String forwardSensor) {
        mForwardBlockingSensorName = forwardSensor;
    }

    public String getReverseBlockingSensorName() {
        if (mReverseBlockingNamedSensor != null) {
            return mReverseBlockingNamedSensor.getName();
        }
        return mReverseBlockingSensorName;
    }

    public Sensor setReverseBlockingSensorName(String reverseSensor) {
        if ((reverseSensor == null) || (reverseSensor.length() <= 0)) {
            mReverseBlockingNamedSensor = null;
            mReverseBlockingSensorName = "";
            return null;
        }
        tempSensorName = reverseSensor;
        Sensor s = validateSensor();
        if (s == null) {
            // sensor name not correct or not in sensor table
            log.error("Sensor name - {} invalid when setting reverse sensor in Section {}",
                    reverseSensor, getDisplayName(USERSYS));
            return null;
        }
        mReverseBlockingNamedSensor = nbhm.getNamedBeanHandle(tempSensorName, s);
        mReverseBlockingSensorName = tempSensorName;
        return s;
    }

    public void delayedSetReverseBlockingSensorName(String reverseSensor) {
        mReverseBlockingSensorName = reverseSensor;
    }

    public Sensor getReverseBlockingSensor() {
        if (mReverseBlockingNamedSensor != null) {
            return mReverseBlockingNamedSensor.getBean();
        }
        if ((mReverseBlockingSensorName != null)
                && (!mReverseBlockingSensorName.isEmpty())) {
            Sensor s = InstanceManager.sensorManagerInstance().
                    getSensor(mReverseBlockingSensorName);
            if (s == null) {
                log.error("Missing Sensor - {} - when initializing Section - {}",
                        mReverseBlockingSensorName, getDisplayName(USERSYS));
                return null;
            }
            mReverseBlockingNamedSensor = nbhm.getNamedBeanHandle(mReverseBlockingSensorName, s);
            return s;
        }
        return null;
    }

    public Block getLastBlock() {
        return mLastBlock;
    }

    private String tempSensorName = "";

    @CheckForNull
    private Sensor validateSensor() {
        // check if anything entered
        if (tempSensorName.length() < 1) {
            // no sensor specified
            return null;
        }
        // get the sensor corresponding to this name
        Sensor s = InstanceManager.sensorManagerInstance().getSensor(tempSensorName);
        if (s == null) {
            return null;
        }
        if (!tempSensorName.equals(s.getUserName()) && s.getUserName() != null) {
            tempSensorName = s.getUserName();
        }
        return s;
    }

    public String getForwardStoppingSensorName() {
        if (mForwardStoppingNamedSensor != null) {
            return mForwardStoppingNamedSensor.getName();
        }
        return mForwardStoppingSensorName;
    }

    @CheckForNull
    public Sensor getForwardStoppingSensor() {
        if (mForwardStoppingNamedSensor != null) {
            return mForwardStoppingNamedSensor.getBean();
        }
        if ((mForwardStoppingSensorName != null)
                && (!mForwardStoppingSensorName.isEmpty())) {
            Sensor s = InstanceManager.sensorManagerInstance().
                    getSensor(mForwardStoppingSensorName);
            if (s == null) {
                log.error("Missing Sensor - {} - when initializing Section - {}",
                        mForwardStoppingSensorName, getDisplayName(USERSYS));
                return null;
            }
            mForwardStoppingNamedSensor = nbhm.getNamedBeanHandle(mForwardStoppingSensorName, s);
            return s;
        }
        return null;
    }

    public Sensor setForwardStoppingSensorName(String forwardSensor) {
        if ((forwardSensor == null) || (forwardSensor.length() <= 0)) {
            mForwardStoppingNamedSensor = null;
            mForwardStoppingSensorName = "";
            return null;
        }
        tempSensorName = forwardSensor;
        Sensor s = validateSensor();
        if (s == null) {
            // sensor name not correct or not in sensor table
            log.error("Sensor name - {} invalid when setting forward sensor in Section {}",
                    forwardSensor, getDisplayName(USERSYS));
            return null;
        }
        mForwardStoppingNamedSensor = nbhm.getNamedBeanHandle(tempSensorName, s);
        mForwardStoppingSensorName = tempSensorName;
        return s;
    }

    public void delayedSetForwardStoppingSensorName(String forwardSensor) {
        mForwardStoppingSensorName = forwardSensor;
    }

    public String getReverseStoppingSensorName() {
        if (mReverseStoppingNamedSensor != null) {
            return mReverseStoppingNamedSensor.getName();
        }
        return mReverseStoppingSensorName;
    }

    @CheckForNull
    public Sensor setReverseStoppingSensorName(String reverseSensor) {
        if ((reverseSensor == null) || (reverseSensor.length() <= 0)) {
            mReverseStoppingNamedSensor = null;
            mReverseStoppingSensorName = "";
            return null;
        }
        tempSensorName = reverseSensor;
        Sensor s = validateSensor();
        if (s == null) {
            // sensor name not correct or not in sensor table
            log.error("Sensor name - {} invalid when setting reverse sensor in Section {}",
                    reverseSensor, getDisplayName(USERSYS));
            return null;
        }
        mReverseStoppingNamedSensor = nbhm.getNamedBeanHandle(tempSensorName, s);
        mReverseStoppingSensorName = tempSensorName;
        return s;
    }

    public void delayedSetReverseStoppingSensorName(String reverseSensor) {
        mReverseStoppingSensorName = reverseSensor;
    }

    @CheckForNull
    public Sensor getReverseStoppingSensor() {
        if (mReverseStoppingNamedSensor != null) {
            return mReverseStoppingNamedSensor.getBean();
        }
        if ((mReverseStoppingSensorName != null)
                && (!mReverseStoppingSensorName.isEmpty())) {
            Sensor s = InstanceManager.sensorManagerInstance().
                    getSensor(mReverseStoppingSensorName);
            if (s == null) {
                log.error("Missing Sensor - {}  - when initializing Section - {}",
                        mReverseStoppingSensorName, getDisplayName(USERSYS));
                return null;
            }
            mReverseStoppingNamedSensor = nbhm.getNamedBeanHandle(mReverseStoppingSensorName, s);
            return s;
        }
        return null;
    }

    /**
     * Add a Block to the Section. Block and sequence number must be unique
     * within the Section. Block sequence numbers are set automatically as
     * blocks are added.
     *
     * @param b the block to add
     * @return true if Block was added or false if Block does not connect to the
     *         current Block, or the Block is not unique.
     */
    public boolean addBlock(Block b) {
        // validate that this entry is unique, if not first.
        if (mBlockEntries.isEmpty()) {
            mFirstBlock = b;
        } else {
            // check that block is unique
            for (Block block : mBlockEntries) {
                if (block == b) {
                    return false; // already present
                }            // Note: connectivity to current block is assumed to have been checked
            }
        }

        // a lot of this code searches for blocks by their user name.
        // warn if there isn't one.
        if (b.getUserName() == null) {
            log.warn("Block {} does not have a user name, may not work correctly in Section {}",
                    b.getDisplayName(USERSYS), getDisplayName(USERSYS));
        }
        // add Block to the Block list
        mBlockEntries.add(b);
        mLastBlock = b;
        // check occupancy
        if (b.getState() == OCCUPIED) {
            if (mOccupancy != OCCUPIED) {
                setOccupancy(OCCUPIED);
            }
        }
        PropertyChangeListener listener = (PropertyChangeEvent e) -> {
            handleBlockChange(e);
        };
        b.addPropertyChangeListener(listener);
        mBlockListeners.add(listener);
        return true;
    }
    private boolean initializationNeeded = false;
    private final List<String> blockNameList = new ArrayList<>();

    public void delayedAddBlock(String blockName) {
        initializationNeeded = true;
        blockNameList.add(blockName);
    }

    private void initializeBlocks() {
        for (int i = 0; i < blockNameList.size(); i++) {
            Block b = InstanceManager.getDefault(jmri.BlockManager.class).getBlock(blockNameList.get(i));
            if (b == null) {
                log.error("Missing Block - {} - when initializing Section - {}",
                        blockNameList.get(i), getDisplayName(USERSYS));
            } else {
                if (mBlockEntries.isEmpty()) {
                    mFirstBlock = b;
                }
                mBlockEntries.add(b);
                mLastBlock = b;
                PropertyChangeListener listener = (PropertyChangeEvent e) -> {
                    handleBlockChange(e);
                };
                b.addPropertyChangeListener(listener);
                mBlockListeners.add(listener);
            }
        }
        initializationNeeded = false;
    }

    /**
     * Handle change in occupancy of a Block in the Section.
     *
     * @param e event with change
     */
    void handleBlockChange(PropertyChangeEvent e) {
        int o = UNOCCUPIED;
        for (Block block : mBlockEntries) {
            if (block.getState() == OCCUPIED) {
                o = OCCUPIED;
                break;
            }
        }
        if (mOccupancy != o) {
            setOccupancy(o);
        }
    }

    /**
     * Get a list of blocks in this section
     *
     * @return a list of blocks
     */
    @Nonnull
    public List<Block> getBlockList() {
        if (initializationNeeded) {
            initializeBlocks();
        }
        return new ArrayList<>(mBlockEntries);
    }

    /**
     * Gets the number of Blocks in this Section
     *
     * @return the number of blocks
     */
    public int getNumBlocks() {
        if (initializationNeeded) {
            initializeBlocks();
        }
        return mBlockEntries.size();
    }

    /**
     * Get the scale length of Section. Length of the Section is calculated by
     * summing the lengths of all Blocks in the section. If all Block lengths
     * have not been entered, length will not be correct.
     *
     * @param meters true to return length in meters, false to use feet
     * @param scale  the scale; one of {@link jmri.Scale}
     * @return the scale length
     */
    public float getLengthF(boolean meters, Scale scale) {
        if (initializationNeeded) {
            initializeBlocks();
        }
        float length = 0.0f;
        for (Block block : mBlockEntries) {
            length = length + block.getLengthMm();
        }
        length = length / (float) (scale.getScaleFactor());
        if (meters) {
            return (length * 0.001f);
        }
        return (length * 0.00328084f);
    }

    public int getLengthI(boolean meters, Scale scale) {
        return ((int) ((getLengthF(meters, scale) + 0.5f)));
    }

    /**
     * Gets the actual length of the Section without any scaling
     *
     * @return the real length in millimeters
     */
    public int getActualLength() {
        if (initializationNeeded) {
            initializeBlocks();
        }
        int len = 0;
        for (Block b : mBlockEntries) {
            len = len + ((int) b.getLengthMm());
        }
        return len;
    }

    /**
     * Get Block by its Sequence number in the Section.
     *
     * @param seqNumber the sequence number
     * @return the block or null if the sequence number is invalid
     */
    @CheckForNull
    public Block getBlockBySequenceNumber(int seqNumber) {
        if (initializationNeeded) {
            initializeBlocks();
        }
        if ((seqNumber < mBlockEntries.size()) && (seqNumber >= 0)) {
            return mBlockEntries.get(seqNumber);
        }
        return null;
    }

    /**
     * Get the sequence number of a Block.
     *
     * @param b the block to get the sequence of
     * @return the sequence number of b or -1 if b is not in the Section
     */
    public int getBlockSequenceNumber(Block b) {
        for (int i = 0; i < mBlockEntries.size(); i++) {
            if (b == mBlockEntries.get(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Remove all Blocks, Block Listeners, and Entry Points
     */
    public void removeAllBlocksFromSection() {
        for (int i = mBlockEntries.size(); i > 0; i--) {
            Block b = mBlockEntries.get(i - 1);
            b.removePropertyChangeListener(mBlockListeners.get(i - 1));
            mBlockListeners.remove(i - 1);
            mBlockEntries.remove(i - 1);
        }
        for (int i = mForwardEntryPoints.size(); i > 0; i--) {
            mForwardEntryPoints.remove(i - 1);
        }
        for (int i = mReverseEntryPoints.size(); i > 0; i--) {
            mReverseEntryPoints.remove(i - 1);
        }
        initializationNeeded = false;
    }
    /**
     * Gets Blocks in order. If state is FREE or FORWARD, returns Blocks in
     * forward order. If state is REVERSE, returns Blocks in reverse order.
     * First call getEntryBlock, then call getNextBlock until null is returned.
     */
    private int blockIndex = 0;  // index of last block returned

    @CheckForNull
    public Block getEntryBlock() {
        if (initializationNeeded) {
            initializeBlocks();
        }
        if (mBlockEntries.size() <= 0) {
            return null;
        }
        if (mState == REVERSE) {
            blockIndex = mBlockEntries.size();
        } else {
            blockIndex = 1;
        }
        return mBlockEntries.get(blockIndex - 1);
    }

    @CheckForNull
    public Block getNextBlock() {
        if (initializationNeeded) {
            initializeBlocks();
        }
        if (mState == REVERSE) {
            blockIndex--;
        } else {
            blockIndex++;
        }
        if ((blockIndex > mBlockEntries.size()) || (blockIndex <= 0)) {
            return null;
        }
        return mBlockEntries.get(blockIndex - 1);
    }

    @CheckForNull
    public Block getExitBlock() {
        if (initializationNeeded) {
            initializeBlocks();
        }
        if (mBlockEntries.size() <= 0) {
            return null;
        }
        if (mState == REVERSE) {
            blockIndex = 1;
        } else {
            blockIndex = mBlockEntries.size();
        }
        return mBlockEntries.get(blockIndex - 1);
    }

    public boolean containsBlock(Block b) {
        for (Block block : mBlockEntries) {
            if (b == block) {
                return true;
            }
        }
        return false;
    }

    public boolean connectsToBlock(Block b) {
        if (mForwardEntryPoints.stream().anyMatch((ep) -> (ep.getFromBlock() == b))) {
            return true;
        }
        return mReverseEntryPoints.stream().anyMatch((ep) -> (ep.getFromBlock() == b));
    }

    public String getBeginBlockName() {
        if (initializationNeeded) {
            initializeBlocks();
        }
        if (mFirstBlock == null) {
            return "unknown";
        }
        return mFirstBlock.getDisplayName();
    }

    public String getEndBlockName() {
        if (initializationNeeded) {
            initializeBlocks();
        }
        if (mLastBlock == null) {
            return "unknown";
        }
        return mLastBlock.getDisplayName();
    }

    public void addToForwardList(EntryPoint ep) {
        if (ep != null) {
            mForwardEntryPoints.add(ep);
        }
    }

    public void addToReverseList(EntryPoint ep) {
        if (ep != null) {
            mReverseEntryPoints.add(ep);
        }
    }

    public void removeEntryPoint(EntryPoint ep) {
        for (int i = mForwardEntryPoints.size(); i > 0; i--) {
            if (mForwardEntryPoints.get(i - 1) == ep) {
                mForwardEntryPoints.remove(i - 1);
            }
        }
        for (int i = mReverseEntryPoints.size(); i > 0; i--) {
            if (mReverseEntryPoints.get(i - 1) == ep) {
                mReverseEntryPoints.remove(i - 1);
            }
        }
    }

    public List<EntryPoint> getForwardEntryPointList() {
        return new ArrayList<>(this.mForwardEntryPoints);
    }

    public List<EntryPoint> getReverseEntryPointList() {
        return new ArrayList<>(this.mReverseEntryPoints);
    }

    public List<EntryPoint> getEntryPointList() {
        List<EntryPoint> list = new ArrayList<>(this.mForwardEntryPoints);
        list.addAll(this.mReverseEntryPoints);
        return list;
    }

    public boolean isForwardEntryPoint(EntryPoint ep) {
        for (int i = 0; i < mForwardEntryPoints.size(); i++) {
            if (ep == mForwardEntryPoints.get(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReverseEntryPoint(EntryPoint ep) {
        for (int i = 0; i < mReverseEntryPoints.size(); i++) {
            if (ep == mReverseEntryPoints.get(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the EntryPoint for entry from the specified Section for travel in
     * specified direction.
     *
     * @param s   the section
     * @param dir the direction of travel; one of {@link #FORWARD} or
     *            {@link #REVERSE}
     * @return the entry point or null if not found
     */
    @CheckForNull
    public EntryPoint getEntryPointFromSection(Section s, int dir) {
        if (dir == FORWARD) {
            for (EntryPoint ep : mForwardEntryPoints) {
                if (s.containsBlock(ep.getFromBlock())) {
                    return ep;
                }
            }
        } else if (dir == REVERSE) {
            for (EntryPoint ep : mReverseEntryPoints) {
                if (s.containsBlock(ep.getFromBlock())) {
                    return ep;
                }
            }
        }
        return null;
    }

    /**
     * Get the EntryPoint for exit to specified Section for travel in the
     * specified direction.
     *
     * @param s   the section
     * @param dir the direction of travel; one of {@link #FORWARD} or
     *            {@link #REVERSE}
     * @return the entry point or null if not found
     */
    @CheckForNull
    public EntryPoint getExitPointToSection(Section s, int dir) {
        if (s == null) {
            return null;
        }
        if (dir == REVERSE) {
            for (EntryPoint ep : mForwardEntryPoints) {
                if (s.containsBlock(ep.getFromBlock())) {
                    return ep;
                }
            }
        } else if (dir == FORWARD) {
            for (EntryPoint ep : mReverseEntryPoints) {
                if (s.containsBlock(ep.getFromBlock())) {
                    return ep;
                }
            }
        }
        return null;
    }

    /**
     * Get the EntryPoint for entry from the specified Block for travel in the
     * specified direction.
     *
     * @param b   the block
     * @param dir the direction of travel; one of {@link #FORWARD} or
     *            {@link #REVERSE}
     * @return the entry point or null if not found
     */
    @CheckForNull
    public EntryPoint getEntryPointFromBlock(Block b, int dir) {
        if (dir == FORWARD) {
            for (EntryPoint ep : mForwardEntryPoints) {
                if (b == ep.getFromBlock()) {
                    return ep;
                }
            }
        } else if (dir == REVERSE) {
            for (EntryPoint ep : mReverseEntryPoints) {
                if (b == ep.getFromBlock()) {
                    return ep;
                }
            }
        }
        return null;
    }

    /**
     * Get the EntryPoint for exit to the specified Block for travel in the
     * specified direction.
     *
     * @param b   the block
     * @param dir the direction of travel; one of {@link #FORWARD} or
     *            {@link #REVERSE}
     * @return the entry point or null if not found
     */
    @CheckForNull
    public EntryPoint getExitPointToBlock(Block b, int dir) {
        if (dir == REVERSE) {
            for (EntryPoint ep : mForwardEntryPoints) {
                if (b == ep.getFromBlock()) {
                    return ep;
                }
            }
        } else if (dir == FORWARD) {
            for (EntryPoint ep : mReverseEntryPoints) {
                if (b == ep.getFromBlock()) {
                    return ep;
                }
            }
        }
        return null;
    }

    /**
     * Returns EntryPoint.FORWARD if proceeding from the throat to the other end
     * is movement in the forward direction. Returns EntryPoint.REVERSE if
     * proceeding from the throat to the other end is movement in the reverse
     * direction. Returns EntryPoint.UNKNOWN if cannot determine direction. This
     * should only happen if blocks are not set up correctly--if all connections
     * go to the same Block, or not all Blocks set. An error message is logged
     * if EntryPoint.UNKNOWN is returned.
     */
    private int getDirectionStandardTurnout(LayoutTurnout t, ConnectivityUtil cUtil) {
        LayoutBlock aBlock = ((TrackSegment) t.getConnectA()).getLayoutBlock();
        LayoutBlock bBlock = ((TrackSegment) t.getConnectB()).getLayoutBlock();
        LayoutBlock cBlock = ((TrackSegment) t.getConnectC()).getLayoutBlock();
        if ((aBlock == null) || (bBlock == null) || (cBlock == null)) {
            log.error("All blocks not assigned for track segments connecting to turnout - {}.",
                    t.getTurnout().getDisplayName(USERSYS));
            return EntryPoint.UNKNOWN;
        }
        Block exBlock = checkDualDirection(aBlock, bBlock, cBlock);
        if ((exBlock != null) || ((aBlock == bBlock) && (aBlock == cBlock))) {
            // using Entry Points directly will lead to a problem, try following track - first from A following B
            int dir = EntryPoint.UNKNOWN;
            Block tBlock = null;
            TrackNode tn = new TrackNode(t, HitPointType.TURNOUT_A, (TrackSegment) t.getConnectA(),
                    false, Turnout.CLOSED);
            while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                tn = cUtil.getNextNode(tn, 0);
                tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock);
            }
            if (tBlock == null) {
                // try from A following C
                tn = new TrackNode(t, HitPointType.TURNOUT_A, (TrackSegment) t.getConnectA(),
                        false, Turnout.THROWN);
                while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                    tn = cUtil.getNextNode(tn, 0);
                    tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock);
                }
            }
            if (tBlock != null) {
                String userName = tBlock.getUserName();
                if (userName != null) {
                    LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                    if (lb != null) {
                        dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, lb);
                    }
                }
            }
            if (dir == EntryPoint.UNKNOWN) {
                // try from B following A
                tBlock = null;
                tn = new TrackNode(t, HitPointType.TURNOUT_B, (TrackSegment) t.getConnectB(),
                        false, Turnout.CLOSED);
                while ((tBlock == null) && (tn != null && (!tn.reachedEndOfTrack()))) {
                    tn = cUtil.getNextNode(tn, 0);
                    tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock);
                }
                if (tBlock != null) {
                    String userName = tBlock.getUserName();
                    if (userName != null) {
                        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                        if (lb != null) {
                            dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, lb);
                        }
                    }
                }
            }
            if (dir == EntryPoint.UNKNOWN) {
                log.error("Block definition ambiguity - cannot determine direction of Turnout {} in Section {}",
                        t.getTurnout().getDisplayName(USERSYS), getDisplayName(USERSYS));
            }
            return dir;
        }
        if ((aBlock != bBlock) && containsBlock(aBlock.getBlock()) && containsBlock(bBlock.getBlock())) {
            // both blocks are different, but are in this Section
            if (getBlockSequenceNumber(aBlock.getBlock()) < getBlockSequenceNumber(bBlock.getBlock())) {
                return EntryPoint.FORWARD;
            } else {
                return EntryPoint.REVERSE;
            }
        } else if ((aBlock != cBlock) && containsBlock(aBlock.getBlock()) && containsBlock(cBlock.getBlock())) {
            // both blocks are different, but are in this Section
            if (getBlockSequenceNumber(aBlock.getBlock()) < getBlockSequenceNumber(cBlock.getBlock())) {
                return EntryPoint.FORWARD;
            } else {
                return EntryPoint.REVERSE;
            }
        }
        LayoutBlock tBlock = t.getLayoutBlock();
        if (tBlock == null) {
            log.error("Block not assigned for turnout {}", t.getTurnout().getDisplayName(USERSYS));
            return EntryPoint.UNKNOWN;
        }
        if (containsBlock(aBlock.getBlock()) && (!containsBlock(bBlock.getBlock()))) {
            // aBlock is in Section, bBlock is not
            int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, bBlock);
            if (dir != EntryPoint.UNKNOWN) {
                return dir;
            }
            if ((tBlock != bBlock) && (!containsBlock(tBlock.getBlock()))) {
                dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, tBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
        }
        if (containsBlock(aBlock.getBlock()) && (!containsBlock(cBlock.getBlock()))) {
            // aBlock is in Section, cBlock is not
            int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, cBlock);
            if (dir != EntryPoint.UNKNOWN) {
                return dir;
            }
            if ((tBlock != cBlock) && (!containsBlock(tBlock.getBlock()))) {
                dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, tBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
        }
        if ((containsBlock(bBlock.getBlock()) || containsBlock(cBlock.getBlock()))
                && (!containsBlock(aBlock.getBlock()))) {
            // bBlock or cBlock is in Section, aBlock is not
            int dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, aBlock);
            if (dir != EntryPoint.UNKNOWN) {
                return dir;
            }
            if ((tBlock != aBlock) && (!containsBlock(tBlock.getBlock()))) {
                dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, tBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
        }
        if (!containsBlock(aBlock.getBlock()) && !containsBlock(bBlock.getBlock()) && !containsBlock(cBlock.getBlock()) && containsBlock(tBlock.getBlock())) {
            //is the turnout in a section of its own?
            int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, aBlock);
            return dir;
        }

        // should never get here
        log.error("Unexpected error in getDirectionStandardTurnout when working with turnout {}",
                t.getTurnout().getDisplayName(USERSYS));
        return EntryPoint.UNKNOWN;
    }

    /**
     * Returns EntryPoint.FORWARD if proceeding from A to B (or D to C) is
     * movement in the forward direction. Returns EntryPoint.REVERSE if
     * proceeding from A to B (or D to C) is movement in the reverse direction.
     * Returns EntryPoint.UNKNOWN if cannot determine direction. This should
     * only happen if blocks are not set up correctly--if all connections go to
     * the same Block, or not all Blocks set. An error message is logged if
     * EntryPoint.UNKNOWN is returned.
     */
    private int getDirectionXoverTurnout(LayoutTurnout t, ConnectivityUtil cUtil) {
        LayoutBlock aBlock = ((TrackSegment) t.getConnectA()).getLayoutBlock();
        LayoutBlock bBlock = ((TrackSegment) t.getConnectB()).getLayoutBlock();
        LayoutBlock cBlock = ((TrackSegment) t.getConnectC()).getLayoutBlock();
        LayoutBlock dBlock = ((TrackSegment) t.getConnectD()).getLayoutBlock();
        if ((aBlock == null) || (bBlock == null) || (cBlock == null) || (dBlock == null)) {
            log.error("All blocks not assigned for track segments connecting to crossover turnout - {}.",
                    t.getTurnout().getDisplayName(USERSYS));
            return EntryPoint.UNKNOWN;
        }
        if ((aBlock == bBlock) && (aBlock == cBlock) && (aBlock == dBlock)) {
            log.error("Block setup problem - All track segments connecting to crossover turnout - {} are assigned to the same Block.",
                    t.getTurnout().getDisplayName(USERSYS));
            return EntryPoint.UNKNOWN;
        }
        if ((containsBlock(aBlock.getBlock())) || (containsBlock(bBlock.getBlock()))) {
            LayoutBlock exBlock = null;
            if (aBlock == bBlock) {
                if ((t.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER) && (cBlock == dBlock)) {
                    exBlock = cBlock;
                }
            }
            if (exBlock != null) {
                // set direction by tracking from a or b
                int dir = EntryPoint.UNKNOWN;
                Block tBlock = null;
                TrackNode tn = new TrackNode(t, HitPointType.TURNOUT_A, (TrackSegment) t.getConnectA(),
                        false, Turnout.CLOSED);
                while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                    tn = cUtil.getNextNode(tn, 0);
                    tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock.getBlock());
                }
                if (tBlock != null) {
                    String userName = tBlock.getUserName();
                    if (userName != null) {
                        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                        if (lb != null) {
                            dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, lb);
                        }
                    }
                } else { // no tBlock found on leg A
                    tn = new TrackNode(t, HitPointType.TURNOUT_B, (TrackSegment) t.getConnectB(),
                            false, Turnout.CLOSED);
                    while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                        tn = cUtil.getNextNode(tn, 0);
                        tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock.getBlock());
                    }
                    if (tBlock != null) {
                        String userName = tBlock.getUserName();
                        if (userName != null) {
                            LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                            if (lb != null) {
                                dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, lb);
                            }
                        }
                    }
                }
                if (dir == EntryPoint.UNKNOWN) {
                    log.error("Block definition ambiguity - cannot determine direction of crossover Turnout {} in Section {}",
                            t.getTurnout().getDisplayName(USERSYS), getDisplayName(USERSYS));
                }
                return dir;
            }
            if ((aBlock != bBlock) && containsBlock(aBlock.getBlock()) && containsBlock(bBlock.getBlock())) {
                if (getBlockSequenceNumber(aBlock.getBlock()) < getBlockSequenceNumber(bBlock.getBlock())) {
                    return EntryPoint.FORWARD;
                } else {
                    return EntryPoint.REVERSE;
                }
            }
            if (containsBlock(aBlock.getBlock()) && (!containsBlock(bBlock.getBlock()))) {
                int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, bBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if (containsBlock(bBlock.getBlock()) && (!containsBlock(aBlock.getBlock()))) {
                int dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, aBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if ((t.getTurnoutType() != LayoutTurnout.TurnoutType.LH_XOVER) && containsBlock(aBlock.getBlock())
                    && (!containsBlock(cBlock.getBlock()))) {
                int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, cBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if ((t.getTurnoutType() != LayoutTurnout.TurnoutType.RH_XOVER) && containsBlock(bBlock.getBlock())
                    && (!containsBlock(dBlock.getBlock()))) {
                int dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, dBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
        }
        if ((containsBlock(dBlock.getBlock())) || (containsBlock(cBlock.getBlock()))) {
            LayoutBlock exBlock = null;
            if (dBlock == cBlock) {
                if ((t.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER) && (bBlock == aBlock)) {
                    exBlock = aBlock;
                }
            }
            if (exBlock != null) {
                // set direction by tracking from c or d
                int dir = EntryPoint.UNKNOWN;
                Block tBlock = null;
                TrackNode tn = new TrackNode(t, HitPointType.TURNOUT_D, (TrackSegment) t.getConnectD(),
                        false, Turnout.CLOSED);
                while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                    tn = cUtil.getNextNode(tn, 0);
                    tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock.getBlock());
                }
                if (tBlock != null) {
                    String userName = tBlock.getUserName();
                    if (userName != null) {
                        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                        if (lb != null) {
                            dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, lb);
                        }
                    }
                } else {
                    tn = new TrackNode(t, HitPointType.TURNOUT_C, (TrackSegment) t.getConnectC(),
                            false, Turnout.CLOSED);
                    while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                        tn = cUtil.getNextNode(tn, 0);
                        tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock.getBlock());
                    }
                    if (tBlock != null) {
                        String userName = tBlock.getUserName();
                        if (userName != null) {
                            LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                            if (lb != null) {
                                dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, lb);
                            }
                        }
                    }
                }
                if (dir == EntryPoint.UNKNOWN) {
                    log.error("Block definition ambiguity - cannot determine direction of crossover Turnout {} in Section {}.",
                            t.getTurnout().getDisplayName(USERSYS), getDisplayName(USERSYS));
                }
                return dir;
            }
            if ((dBlock != cBlock) && containsBlock(dBlock.getBlock()) && containsBlock(cBlock.getBlock())) {
                if (getBlockSequenceNumber(dBlock.getBlock()) < getBlockSequenceNumber(cBlock.getBlock())) {
                    return EntryPoint.FORWARD;
                } else {
                    return EntryPoint.REVERSE;
                }
            }
            if (containsBlock(dBlock.getBlock()) && (!containsBlock(cBlock.getBlock()))) {
                int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, cBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if (containsBlock(cBlock.getBlock()) && (!containsBlock(dBlock.getBlock()))) {
                int dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, dBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if ((t.getTurnoutType() != LayoutTurnout.TurnoutType.RH_XOVER) && containsBlock(dBlock.getBlock())
                    && (!containsBlock(bBlock.getBlock()))) {
                int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, bBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if ((t.getTurnoutType() != LayoutTurnout.TurnoutType.LH_XOVER) && containsBlock(cBlock.getBlock())
                    && (!containsBlock(aBlock.getBlock()))) {
                int dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, aBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
        }
        log.error("Entry point checks failed - cannot determine direction of crossover Turnout {} in Section {}.",
                t.getTurnout().getDisplayName(USERSYS), getDisplayName(USERSYS));
        return EntryPoint.UNKNOWN;
    }

    /**
     * Returns EntryPoint.FORWARD if proceeding from A to C or D (or B to D or
     * C) is movement in the forward direction. Returns EntryPoint.REVERSE if
     * proceeding from C or D to A (or D or C to B) is movement in the reverse
     * direction. Returns EntryPoint.UNKNOWN if cannot determine direction. This
     * should only happen if blocks are not set up correctly--if all connections
     * go to the same Block, or not all Blocks set. An error message is logged
     * if EntryPoint.UNKNOWN is returned.
     *
     * @param t Actually of type LayoutSlip, this is the track segment to check.
     */
    private int getDirectionSlip(LayoutTurnout t, ConnectivityUtil cUtil) {
        LayoutBlock aBlock = ((TrackSegment) t.getConnectA()).getLayoutBlock();
        LayoutBlock bBlock = ((TrackSegment) t.getConnectB()).getLayoutBlock();
        LayoutBlock cBlock = ((TrackSegment) t.getConnectC()).getLayoutBlock();
        LayoutBlock dBlock = ((TrackSegment) t.getConnectD()).getLayoutBlock();
        if ((aBlock == null) || (bBlock == null) || (cBlock == null) || (dBlock == null)) {
            log.error("All blocks not assigned for track segments connecting to crossover turnout - {}.",
                    t.getTurnout().getDisplayName(USERSYS));
            return EntryPoint.UNKNOWN;
        }
        if ((aBlock == bBlock) && (aBlock == cBlock) && (aBlock == dBlock)) {
            log.error("Block setup problem - All track segments connecting to crossover turnout - {} are assigned to the same Block.",
                    t.getTurnout().getDisplayName(USERSYS));
            return EntryPoint.UNKNOWN;
        }
        if ((containsBlock(aBlock.getBlock())) || (containsBlock(cBlock.getBlock()))) {
            LayoutBlock exBlock = null;
            if (aBlock == cBlock) {
                if ((t.getTurnoutType() == LayoutSlip.TurnoutType.DOUBLE_SLIP) && (bBlock == dBlock)) {
                    exBlock = bBlock;
                }
            }
            if (exBlock != null) {
                // set direction by tracking from a or b
                int dir = EntryPoint.UNKNOWN;
                Block tBlock = null;
                TrackNode tn = new TrackNode(t, HitPointType.SLIP_A, (TrackSegment) t.getConnectA(),
                        false, LayoutTurnout.STATE_AC);
                while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                    tn = cUtil.getNextNode(tn, 0);
                    tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock.getBlock());
                }
                if (tBlock != null) {
                    String userName = tBlock.getUserName();
                    if (userName != null) {
                        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                        if (lb != null) {
                            dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, lb);
                        }
                    }
                } else {
                    tn = new TrackNode(t, HitPointType.SLIP_C, (TrackSegment) t.getConnectC(),
                            false, LayoutTurnout.STATE_AC);
                    while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                        tn = cUtil.getNextNode(tn, 0);
                        tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock.getBlock());
                    }
                    if (tBlock != null) {
                        String userName = tBlock.getUserName();
                        if (userName != null) {
                            LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                            if (lb != null) {
                                dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, lb);
                            }
                        }
                    }
                }
                if (dir == EntryPoint.UNKNOWN) {
                    log.error("Block definition ambiguity - cannot determine direction of crossover slip {} in Section {}.",
                            t.getTurnout().getDisplayName(USERSYS), getDisplayName(USERSYS));
                }
                return dir;
            }
            if ((aBlock != cBlock) && containsBlock(aBlock.getBlock()) && containsBlock(cBlock.getBlock())) {
                if (getBlockSequenceNumber(aBlock.getBlock()) < getBlockSequenceNumber(cBlock.getBlock())) {
                    return EntryPoint.FORWARD;
                } else {
                    return EntryPoint.REVERSE;
                }
            }
            if (containsBlock(aBlock.getBlock()) && (!containsBlock(cBlock.getBlock()))) {
                int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, cBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if (containsBlock(cBlock.getBlock()) && (!containsBlock(aBlock.getBlock()))) {
                int dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, aBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, dBlock);
            if (dir != EntryPoint.UNKNOWN) {
                return dir;
            }
        }

        if ((containsBlock(dBlock.getBlock())) || (containsBlock(bBlock.getBlock()))) {
            LayoutBlock exBlock = null;
            if (dBlock == bBlock) {
                if ((t.getTurnoutType() == LayoutSlip.TurnoutType.DOUBLE_SLIP) && (cBlock == aBlock)) {
                    exBlock = aBlock;
                }
            }
            if (exBlock != null) {
                // set direction by tracking from c or d
                int dir = EntryPoint.UNKNOWN;
                Block tBlock = null;
                TrackNode tn = new TrackNode(t, HitPointType.SLIP_D, (TrackSegment) t.getConnectD(),
                        false, LayoutTurnout.STATE_BD);
                while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                    tn = cUtil.getNextNode(tn, 0);
                    tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock.getBlock());
                }
                if (tBlock != null) {
                    String userName = tBlock.getUserName();
                    if (userName != null) {
                        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                        if (lb != null) {
                            dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, lb);
                        }
                    }
                } else {
                    tn = new TrackNode(t, HitPointType.TURNOUT_B, (TrackSegment) t.getConnectB(),
                            false, LayoutTurnout.STATE_BD);
                    while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                        tn = cUtil.getNextNode(tn, 0);
                        tBlock = cUtil.getExitBlockForTrackNode(tn, exBlock.getBlock());
                    }
                    if (tBlock != null) {
                        String userName = tBlock.getUserName();
                        if (userName != null) {
                            LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                            if (lb != null) {
                                dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, lb);
                            }
                        }
                    }
                }
                if (dir == EntryPoint.UNKNOWN) {
                    log.error("Block definition ambiguity - cannot determine direction of slip {} in Section {}.",
                            t.getTurnout().getDisplayName(USERSYS), getDisplayName(USERSYS));
                }
                return dir;
            }
            if ((dBlock != bBlock) && containsBlock(dBlock.getBlock()) && containsBlock(bBlock.getBlock())) {
                if (getBlockSequenceNumber(dBlock.getBlock()) < getBlockSequenceNumber(bBlock.getBlock())) {
                    return EntryPoint.FORWARD;
                } else {
                    return EntryPoint.REVERSE;
                }
            }
            if (containsBlock(dBlock.getBlock()) && (!containsBlock(bBlock.getBlock()))) {
                int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, bBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if (containsBlock(bBlock.getBlock()) && (!containsBlock(dBlock.getBlock()))) {
                int dir = checkLists(mForwardEntryPoints, mReverseEntryPoints, dBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
            if (t.getTurnoutType() == LayoutSlip.TurnoutType.DOUBLE_SLIP) {
                int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, aBlock);
                if (dir != EntryPoint.UNKNOWN) {
                    return dir;
                }
            }
        }
        //If all else fails the slip must be in a block of its own so we shall work it out from there.
        if (t.getLayoutBlock() != aBlock) {
            //Block is not the same as that connected to A
            int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, aBlock);
            if (dir != EntryPoint.UNKNOWN) {
                return dir;
            }
        }
        if (t.getLayoutBlock() != bBlock) {
            //Block is not the same as that connected to B
            int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, bBlock);
            if (dir != EntryPoint.UNKNOWN) {
                return dir;
            }
        }
        if (t.getLayoutBlock() != cBlock) {
            //Block is not the same as that connected to C
            int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, cBlock);
            if (dir != EntryPoint.UNKNOWN) {
                return dir;
            }
        }
        if (t.getLayoutBlock() != dBlock) {
            //Block is not the same as that connected to D
            int dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, dBlock);
            if (dir != EntryPoint.UNKNOWN) {
                return dir;
            }
        }
        return EntryPoint.UNKNOWN;
    }

    private boolean placeSensorInCrossover(String b1Name, String b2Name, String c1Name, String c2Name,
            int direction, ConnectivityUtil cUtil) {
        SignalHead b1Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(b1Name);
        SignalHead b2Head = null;
        SignalHead c1Head = null;
        SignalHead c2Head = null;
        boolean success = true;
        if ((b2Name != null) && (!b2Name.isEmpty())) {
            b2Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(b2Name);
        }
        if ((c1Name != null) && (!c1Name.isEmpty())) {
            c1Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(c1Name);
        }
        if ((c2Name != null) && (!c2Name.isEmpty())) {
            c2Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(c2Name);
        }
        if (b2Head != null) {
            if (!checkDirectionSensor(b1Head, direction, ConnectivityUtil.OVERALL, cUtil)) {
                success = false;
            }
        } else {
            if (!checkDirectionSensor(b1Head, direction, ConnectivityUtil.CONTINUING, cUtil)) {
                success = false;
            }
        }
        if (c2Head != null) {
            if (!checkDirectionSensor(c2Head, direction, ConnectivityUtil.OVERALL, cUtil)) {
                success = false;
            }
        } else if (c1Head != null) {
            if (!checkDirectionSensor(c1Head, direction, ConnectivityUtil.DIVERGING, cUtil)) {
                success = false;
            }
        }
        return success;
    }

    private int checkLists(List<EntryPoint> forwardList, List<EntryPoint> reverseList, LayoutBlock lBlock) {
        for (int i = 0; i < forwardList.size(); i++) {
            if (forwardList.get(i).getFromBlock() == lBlock.getBlock()) {
                return EntryPoint.FORWARD;
            }
        }
        for (int i = 0; i < reverseList.size(); i++) {
            if (reverseList.get(i).getFromBlock() == lBlock.getBlock()) {
                return EntryPoint.REVERSE;
            }
        }
        return EntryPoint.UNKNOWN;
    }

    @CheckForNull
    private Block checkDualDirection(LayoutBlock aBlock, LayoutBlock bBlock, LayoutBlock cBlock) {
        for (int i = 0; i < mForwardEntryPoints.size(); i++) {
            Block b = mForwardEntryPoints.get(i).getFromBlock();
            for (int j = 0; j < mReverseEntryPoints.size(); j++) {
                if (mReverseEntryPoints.get(j).getFromBlock() == b) {
                    // possible dual direction
                    if (aBlock.getBlock() == b) {
                        return b;
                    } else if (bBlock.getBlock() == b) {
                        return b;
                    } else if ((cBlock.getBlock() == b) && (aBlock == bBlock)) {
                        return b;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the direction for proceeding from LayoutBlock b to LayoutBlock a.
     * LayoutBlock a must be in the Section. LayoutBlock b may be in this
     * Section or may be an Entry Point to the Section.
     */
    private int getDirectionForBlocks(LayoutBlock a, LayoutBlock b) {
        if (containsBlock(b.getBlock())) {
            // both blocks are within this Section
            if (getBlockSequenceNumber(a.getBlock()) > getBlockSequenceNumber(b.getBlock())) {
                return EntryPoint.FORWARD;
            } else {
                return EntryPoint.REVERSE;
            }
        }
        // bBlock must be an entry point
        for (int i = 0; i < mForwardEntryPoints.size(); i++) {
            if (mForwardEntryPoints.get(i).getFromBlock() == b.getBlock()) {
                return EntryPoint.FORWARD;
            }
        }
        for (int j = 0; j < mReverseEntryPoints.size(); j++) {
            if (mReverseEntryPoints.get(j).getFromBlock() == b.getBlock()) {
                return EntryPoint.REVERSE;
            }
        }
        // should never get here
        log.error("Unexpected error in getDirectionForBlocks when working with LevelCrossing in Section {}",
                getDisplayName(USERSYS));
        return EntryPoint.UNKNOWN;
    }

    /**
     * @return 'true' if successfully checked direction sensor by follow
     *         connectivity from specified track node; 'false' if an error
     *         occurred
     */
    private boolean setDirectionSensorByConnectivity(TrackNode tNode, TrackNode altNode, SignalHead sh,
            Block cBlock, ConnectivityUtil cUtil) {
        boolean successful = false;
        TrackNode tn = tNode;
        if ((tn != null) && (sh != null)) {
            Block tBlock = null;
            LayoutBlock lb;
            int dir = EntryPoint.UNKNOWN;
            while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                tn = cUtil.getNextNode(tn, 0);
                tBlock = (tn == null) ? null : cUtil.getExitBlockForTrackNode(tn, null);
            }
            if (tBlock != null) {
                String userName = tBlock.getUserName();
                if (userName != null) {
                    lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                    if (lb != null) {
                        dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, lb);
                    }
                }
            } else {
                tn = altNode;
                while ((tBlock == null) && (tn != null) && (!tn.reachedEndOfTrack())) {
                    tn = cUtil.getNextNode(tn, 0);
                    tBlock = (tn == null) ? null : cUtil.getExitBlockForTrackNode(tn, null);
                }
                if (tBlock != null) {
                    String userName = tBlock.getUserName();
                    if (userName != null) {
                        lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                        if (lb != null) {
                            dir = checkLists(mReverseEntryPoints, mForwardEntryPoints, lb);
                            if (dir == EntryPoint.REVERSE) {
                                dir = EntryPoint.FORWARD;
                            } else if (dir == EntryPoint.FORWARD) {
                                dir = EntryPoint.REVERSE;
                            }
                        }
                    }
                }
            }
            if (dir != EntryPoint.UNKNOWN) {
                if (checkDirectionSensor(sh, dir, ConnectivityUtil.OVERALL, cUtil)) {
                    successful = true;
                }
            } else {
                log.error("Trouble following track in Block {} in Section {}.",
                        cBlock.getDisplayName(USERSYS), getDisplayName(USERSYS));
            }
        }
        return successful;
    }

    /**
     * Place direction sensors in SSL for all Signal Heads in this Section if
     * the Sensors are not already present in the SSL.
     * <p>
     * Only anchor point block boundaries that have assigned signals are
     * considered. Only turnouts that have assigned signals are considered. Only
     * level crossings that have assigned signals are considered. Turnouts and
     * anchor points without signals are counted, and reported in warning
     * messages during this procedure, if there are any missing signals.
     * <p>
     * If this method has trouble, an error message is placed in the log
     * describing the trouble.
     *
     * @param panel the panel to place direction sensors on
     * @return the number or errors placing sensors; 1 is returned if no
     *         direction sensor is defined for this section
     */
    public int placeDirectionSensors(LayoutEditor panel) {
        int missingSignalsBB = 0;
        int missingSignalsTurnouts = 0;
        int missingSignalsLevelXings = 0;
        int errorCount = 0;
        if (panel == null) {
            log.error("Null Layout Editor panel on call to 'placeDirectionSensors'");
            return 1;
        }
        if (initializationNeeded) {
            initializeBlocks();
        }
        if ((mForwardBlockingSensorName == null) || (mForwardBlockingSensorName.isEmpty())
                || (mReverseBlockingSensorName == null) || (mReverseBlockingSensorName.isEmpty())) {
            log.error("Missing direction sensor in Section {}", getDisplayName(USERSYS));
            return 1;
        }
        LayoutBlockManager layoutBlockManager = InstanceManager.getDefault(LayoutBlockManager.class);
        ConnectivityUtil cUtil = panel.getConnectivityUtil();
        LayoutBlock lBlock = null;
        for (Block cBlock : mBlockEntries) {
            String userName = cBlock.getUserName();
            if (userName != null) {
                lBlock = layoutBlockManager.getByUserName(userName);
                if (lBlock == null) {
                    continue;
                }
            }
            List<PositionablePoint> anchorList = cUtil.getAnchorBoundariesThisBlock(cBlock);
            for (int j = 0; j < anchorList.size(); j++) {
                PositionablePoint p = anchorList.get(j);
                if ((!p.getEastBoundSignal().isEmpty()) && (!p.getWestBoundSignal().isEmpty())) {
                    // have a signalled block boundary
                    SignalHead sh = cUtil.getSignalHeadAtAnchor(p, cBlock, false);
                    if (sh == null) {
                        log.warn("Unexpected missing signal head at boundary of Block {}", cBlock.getDisplayName(USERSYS));
                        errorCount++;
                    } else {
                        int direction = cUtil.getDirectionFromAnchor(mForwardEntryPoints,
                                mReverseEntryPoints, p);
                        if (direction == EntryPoint.UNKNOWN) {
                            // anchor is at a Block boundary within the Section
                            sh = cUtil.getSignalHeadAtAnchor(p, cBlock, true);
                            Block otherBlock = ((p.getConnect1()).getLayoutBlock()).getBlock();
                            if (otherBlock == cBlock) {
                                otherBlock = ((p.getConnect2()).getLayoutBlock()).getBlock();
                            }
                            if (getBlockSequenceNumber(cBlock) < getBlockSequenceNumber(otherBlock)) {
                                direction = EntryPoint.FORWARD;
                            } else {
                                direction = EntryPoint.REVERSE;
                            }
                        }
                        if (!checkDirectionSensor(sh, direction, ConnectivityUtil.OVERALL, cUtil)) {
                            errorCount++;
                        }
                    }
                } else {
                    errorCount++;
                    missingSignalsBB++;
                }
            }
            List<LevelXing> xingList = cUtil.getLevelCrossingsThisBlock(cBlock);
            for (int k = 0; k < xingList.size(); k++) {
                LevelXing x = xingList.get(k);
                LayoutBlock alBlock = ((TrackSegment) x.getConnectA()).getLayoutBlock();
                LayoutBlock blBlock = ((TrackSegment) x.getConnectB()).getLayoutBlock();
                LayoutBlock clBlock = ((TrackSegment) x.getConnectC()).getLayoutBlock();
                LayoutBlock dlBlock = ((TrackSegment) x.getConnectD()).getLayoutBlock();
                if (cUtil.isInternalLevelXingAC(x, cBlock)) {
                    // have an internal AC level crossing - is it signaled?
                    if (!x.getSignalAName().isEmpty() || (!x.getSignalCName().isEmpty())) {
                        // have a signaled AC level crossing internal to this block
                        if (!x.getSignalAName().isEmpty()) {
                            // there is a signal at A in the level crossing
                            TrackNode tn = new TrackNode(x, HitPointType.LEVEL_XING_A,
                                    (TrackSegment) x.getConnectA(), false, 0);
                            TrackNode altNode = new TrackNode(x, HitPointType.LEVEL_XING_C,
                                    (TrackSegment) x.getConnectC(), false, 0);
                            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    x.getSignalAName());
                            if (!setDirectionSensorByConnectivity(tn, altNode, sh, cBlock, cUtil)) {
                                errorCount++;
                            }
                        }
                        if (!x.getSignalCName().isEmpty()) {
                            // there is a signal at C in the level crossing
                            TrackNode tn = new TrackNode(x, HitPointType.LEVEL_XING_C,
                                    (TrackSegment) x.getConnectC(), false, 0);
                            TrackNode altNode = new TrackNode(x, HitPointType.LEVEL_XING_A,
                                    (TrackSegment) x.getConnectA(), false, 0);
                            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    x.getSignalCName());
                            if (!setDirectionSensorByConnectivity(tn, altNode, sh, cBlock, cUtil)) {
                                errorCount++;
                            }
                        }
                    }
                } else if (alBlock == lBlock) {
                    // have a level crossing with AC spanning a block boundary, with A in this Block
                    int direction = getDirectionForBlocks(alBlock, clBlock);
                    if (direction != EntryPoint.UNKNOWN) {
                        if (!x.getSignalCName().isEmpty()) {
                            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    x.getSignalCName());
                            if (!checkDirectionSensor(sh, direction, ConnectivityUtil.OVERALL, cUtil)) {
                                errorCount++;
                            }
                        }
                    } else {
                        errorCount++;
                    }
                } else if (clBlock == lBlock) {
                    // have a level crossing with AC spanning a block boundary, with C in this Block
                    int direction = getDirectionForBlocks(clBlock, alBlock);
                    if (direction != EntryPoint.UNKNOWN) {
                        if (!x.getSignalAName().isEmpty()) {
                            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    x.getSignalAName());
                            if (!checkDirectionSensor(sh, direction, ConnectivityUtil.OVERALL, cUtil)) {
                                errorCount++;
                            }
                        }
                    } else {
                        errorCount++;
                    }
                }
                if (cUtil.isInternalLevelXingBD(x, cBlock)) {
                    // have an internal BD level crossing - is it signaled?
                    if ((!x.getSignalBName().isEmpty()) || (!x.getSignalDName().isEmpty())) {
                        // have a signaled BD level crossing internal to this block
                        if (!x.getSignalBName().isEmpty()) {
                            // there is a signal at B in the level crossing
                            TrackNode tn = new TrackNode(x, HitPointType.LEVEL_XING_B,
                                    (TrackSegment) x.getConnectB(), false, 0);
                            TrackNode altNode = new TrackNode(x, HitPointType.LEVEL_XING_D,
                                    (TrackSegment) x.getConnectD(), false, 0);
                            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    x.getSignalBName());
                            if (!setDirectionSensorByConnectivity(tn, altNode, sh, cBlock, cUtil)) {
                                errorCount++;
                            }
                        }
                        if (!x.getSignalDName().isEmpty()) {
                            // there is a signal at C in the level crossing
                            TrackNode tn = new TrackNode(x, HitPointType.LEVEL_XING_D,
                                    (TrackSegment) x.getConnectD(), false, 0);
                            TrackNode altNode = new TrackNode(x, HitPointType.LEVEL_XING_B,
                                    (TrackSegment) x.getConnectB(), false, 0);
                            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    x.getSignalDName());
                            if (!setDirectionSensorByConnectivity(tn, altNode, sh, cBlock, cUtil)) {
                                errorCount++;
                            }
                        }
                    }
                } else if (blBlock == lBlock) {
                    // have a level crossing with BD spanning a block boundary, with B in this Block
                    int direction = getDirectionForBlocks(blBlock, dlBlock);
                    if (direction != EntryPoint.UNKNOWN) {
                        if (!x.getSignalDName().isEmpty()) {
                            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    x.getSignalDName());
                            if (!checkDirectionSensor(sh, direction, ConnectivityUtil.OVERALL, cUtil)) {
                                errorCount++;
                            }
                        }
                    } else {
                        errorCount++;
                    }
                } else if (dlBlock == lBlock) {
                    // have a level crossing with BD spanning a block boundary, with D in this Block
                    int direction = getDirectionForBlocks(dlBlock, blBlock);
                    if (direction != EntryPoint.UNKNOWN) {
                        if (!x.getSignalBName().isEmpty()) {
                            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    x.getSignalBName());
                            if (!checkDirectionSensor(sh, direction, ConnectivityUtil.OVERALL, cUtil)) {
                                errorCount++;
                            }
                        }
                    } else {
                        errorCount++;
                    }
                }
            }
            List<LayoutTurnout> turnoutList = cUtil.getLayoutTurnoutsThisBlock(cBlock);
            for (int m = 0; m < turnoutList.size(); m++) {
                LayoutTurnout t = turnoutList.get(m);
                if (cUtil.layoutTurnoutHasRequiredSignals(t)) {
                    // have a signalled turnout
                    if ((t.getLinkType() == LayoutTurnout.LinkType.NO_LINK)
                            && ((t.getTurnoutType() == LayoutTurnout.TurnoutType.RH_TURNOUT)
                            || (t.getTurnoutType() == LayoutTurnout.TurnoutType.LH_TURNOUT)
                            || (t.getTurnoutType() == LayoutTurnout.TurnoutType.WYE_TURNOUT))) {
                        // standard turnout - nothing special
                        // Note: direction is for proceeding from the throat to either other track
                        int direction = getDirectionStandardTurnout(t, cUtil);
                        int altDirection = EntryPoint.FORWARD;
                        if (direction == EntryPoint.FORWARD) {
                            altDirection = EntryPoint.REVERSE;
                        }
                        if (direction == EntryPoint.UNKNOWN) {
                            errorCount++;
                        } else {
                            SignalHead aHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalA1Name());
                            SignalHead a2Head = null;
                            String a2Name = t.getSignalA2Name(); // returns "" for empty name, never null
                            if (!a2Name.isEmpty()) {
                                a2Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(a2Name);
                            }
                            SignalHead bHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalB1Name());
                            SignalHead cHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalC1Name());
                            if (t.getLayoutBlock().getBlock() == cBlock) {
                                // turnout is in this block, set direction sensors on all signal heads
                                // Note: need allocation to traverse this turnout
                                if (!checkDirectionSensor(aHead, direction,
                                        ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                                if (a2Head != null) {
                                    if (!checkDirectionSensor(a2Head, direction,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                }
                                if (!checkDirectionSensor(bHead, altDirection,
                                        ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                                if (!checkDirectionSensor(cHead, altDirection,
                                        ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                            } else {
                                if (((TrackSegment) t.getConnectA()).getLayoutBlock().getBlock() == cBlock) {
                                    // throat Track Segment is in this Block
                                    if (!checkDirectionSensor(bHead, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                    if (!checkDirectionSensor(cHead, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                } else if (((t.getContinuingSense() == Turnout.CLOSED)
                                        && (((TrackSegment) t.getConnectB()).getLayoutBlock().getBlock() == cBlock))
                                        || ((t.getContinuingSense() == Turnout.THROWN)
                                        && (((TrackSegment) t.getConnectC()).getLayoutBlock().getBlock() == cBlock))) {
                                    // continuing track segment is in this block, normal continuing sense - or -
                                    //  diverging track segment is in this block, reverse continuing sense.
                                    if (a2Head == null) {
                                        // single head at throat
                                        if (!checkDirectionSensor(aHead, direction,
                                                ConnectivityUtil.CONTINUING, cUtil)) {
                                            errorCount++;
                                        }
                                    } else {
                                        // two heads at throat
                                        if (!checkDirectionSensor(aHead, direction,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                    }
                                    if (!checkDirectionSensor(bHead, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                } else if (((t.getContinuingSense() == Turnout.CLOSED)
                                        && (((TrackSegment) t.getConnectC()).getLayoutBlock().getBlock() == cBlock))
                                        || ((t.getContinuingSense() == Turnout.THROWN)
                                        && (((TrackSegment) t.getConnectB()).getLayoutBlock().getBlock() == cBlock))) {
                                    // diverging track segment is in this block, normal continuing sense - or -
                                    //  continuing track segment is in this block, reverse continuing sense.
                                    if (a2Head == null) {
                                        // single head at throat
                                        if (!checkDirectionSensor(aHead, direction,
                                                ConnectivityUtil.DIVERGING, cUtil)) {
                                            errorCount++;
                                        }
                                    } else {
                                        // two heads at throat
                                        if (!checkDirectionSensor(a2Head, direction,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                    }
                                    if (!checkDirectionSensor(cHead, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                }
                            }
                        }
                    } else if (t.getLinkType() != LayoutTurnout.LinkType.NO_LINK) {
                        // special linked turnout
                        LayoutTurnout tLinked = getLayoutTurnoutFromTurnoutName(t.getLinkedTurnoutName(), panel);
                        if (tLinked == null) {
                            log.error("null Layout Turnout linked to turnout {}", t.getTurnout().getDisplayName(USERSYS));
                        } else if (t.getLinkType() == LayoutTurnout.LinkType.THROAT_TO_THROAT) {
                            SignalHead b1Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalB1Name());
                            SignalHead b2Head = null;
                            String hName = t.getSignalB2Name(); // returns "" for empty name, never null
                            if (!hName.isEmpty()) {
                                b2Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(hName);
                            }
                            SignalHead c1Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalC1Name());
                            SignalHead c2Head = null;
                            hName = t.getSignalC2Name(); // returns "" for empty name, never null
                            if (!hName.isEmpty()) {
                                c2Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(hName);
                            }
                            int direction = getDirectionStandardTurnout(t, cUtil);
                            int altDirection = EntryPoint.FORWARD;
                            if (direction == EntryPoint.FORWARD) {
                                altDirection = EntryPoint.REVERSE;
                            }
                            if (direction != EntryPoint.UNKNOWN) {
                                if (t.getLayoutBlock().getBlock() == cBlock) {
                                    // turnout is in this block, set direction sensors on all signal heads
                                    // Note: need allocation to traverse this turnout
                                    if (!checkDirectionSensor(b1Head, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                    if (b2Head != null) {
                                        if (!checkDirectionSensor(b2Head, altDirection,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                    }
                                    if (!checkDirectionSensor(c1Head, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                    if (c2Head != null) {
                                        if (!checkDirectionSensor(c2Head, altDirection,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                    }
                                } else {
                                    // turnout is not in this block, switch to heads of linked turnout
                                    b1Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                            tLinked.getSignalB1Name());
                                    hName = tLinked.getSignalB2Name(); // returns "" for empty name, never null
                                    b2Head = null;
                                    if (!hName.isEmpty()) {
                                        b2Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(hName);
                                    }
                                    c1Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                            tLinked.getSignalC1Name());
                                    c2Head = null;
                                    hName = tLinked.getSignalC2Name(); // returns "" for empty name, never null
                                    if (!hName.isEmpty()) {
                                        c2Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(hName);
                                    }
                                    if (((t.getContinuingSense() == Turnout.CLOSED)
                                            && (((TrackSegment) t.getConnectB()).getLayoutBlock().getBlock() == cBlock))
                                            || ((t.getContinuingSense() == Turnout.THROWN)
                                            && (((TrackSegment) t.getConnectC()).getLayoutBlock().getBlock() == cBlock))) {
                                        // continuing track segment is in this block
                                        if (b2Head != null) {
                                            if (!checkDirectionSensor(b1Head, direction,
                                                    ConnectivityUtil.OVERALL, cUtil)) {
                                                errorCount++;
                                            }
                                        } else {
                                            if (!checkDirectionSensor(b1Head, direction,
                                                    ConnectivityUtil.CONTINUING, cUtil)) {
                                                errorCount++;
                                            }
                                        }
                                        if (c2Head != null) {
                                            if (!checkDirectionSensor(c1Head, direction,
                                                    ConnectivityUtil.OVERALL, cUtil)) {
                                                errorCount++;
                                            }
                                        } else {
                                            if (!checkDirectionSensor(c1Head, direction,
                                                    ConnectivityUtil.CONTINUING, cUtil)) {
                                                errorCount++;
                                            }
                                        }
                                    } else if (((t.getContinuingSense() == Turnout.CLOSED)
                                            && (((TrackSegment) t.getConnectC()).getLayoutBlock().getBlock() == cBlock))
                                            || ((t.getContinuingSense() == Turnout.THROWN)
                                            && (((TrackSegment) t.getConnectB()).getLayoutBlock().getBlock() == cBlock))) {
                                        // diverging track segment is in this block
                                        if (b2Head != null) {
                                            if (!checkDirectionSensor(b2Head, direction,
                                                    ConnectivityUtil.OVERALL, cUtil)) {
                                                errorCount++;
                                            }
                                        } else {
                                            if (!checkDirectionSensor(b1Head, direction,
                                                    ConnectivityUtil.DIVERGING, cUtil)) {
                                                errorCount++;
                                            }
                                        }
                                        if (c2Head != null) {
                                            if (!checkDirectionSensor(c2Head, direction,
                                                    ConnectivityUtil.OVERALL, cUtil)) {
                                                errorCount++;
                                            }
                                        } else {
                                            if (!checkDirectionSensor(c1Head, direction,
                                                    ConnectivityUtil.DIVERGING, cUtil)) {
                                                errorCount++;
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (t.getLinkType() == LayoutTurnout.LinkType.FIRST_3_WAY) {
                            SignalHead a1Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalA1Name());
                            SignalHead a2Head = null;
                            String hName = t.getSignalA2Name();
                            if (!hName.isEmpty()) {
                                a2Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(hName);
                            }
                            SignalHead a3Head = null;
                            hName = t.getSignalA3Name(); // returns "" for empty name, never null
                            if (!hName.isEmpty()) {
                                a3Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(hName);
                            }
                            SignalHead cHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalC1Name());
                            int direction = getDirectionStandardTurnout(t, cUtil);
                            int altDirection = EntryPoint.FORWARD;
                            if (direction == EntryPoint.FORWARD) {
                                altDirection = EntryPoint.REVERSE;
                            }
                            if (direction != EntryPoint.UNKNOWN) {
                                if (t.getLayoutBlock().getBlock() == cBlock) {
                                    // turnout is in this block, set direction sensors on all signal heads
                                    // Note: need allocation to traverse this turnout
                                    if (!checkDirectionSensor(a1Head, direction,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                    if ((a2Head != null) && (a3Head != null)) {
                                        if (!checkDirectionSensor(a2Head, direction,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                        if (!checkDirectionSensor(a3Head, direction,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                    }
                                    if (!checkDirectionSensor(cHead, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                } else {
                                    // turnout is not in this block
                                    if (((TrackSegment) t.getConnectA()).getLayoutBlock().getBlock() == cBlock) {
                                        // throat Track Segment is in this Block
                                        if (!checkDirectionSensor(cHead, altDirection,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                    } else if (((TrackSegment) t.getConnectC()).getLayoutBlock().getBlock() == cBlock) {
                                        // diverging track segment is in this Block
                                        if (a2Head != null) {
                                            if (!checkDirectionSensor(a2Head, direction,
                                                    ConnectivityUtil.OVERALL, cUtil)) {
                                                errorCount++;
                                            }
                                        } else {
                                            if (!checkDirectionSensor(a1Head, direction,
                                                    ConnectivityUtil.DIVERGING, cUtil)) {
                                                errorCount++;
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (t.getLinkType() == LayoutTurnout.LinkType.SECOND_3_WAY) {
                            SignalHead bHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalB1Name());
                            SignalHead cHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    t.getSignalC1Name());
                            SignalHead a1Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(
                                    tLinked.getSignalA1Name());
                            SignalHead a3Head = null;
                            String hName = tLinked.getSignalA3Name();
                            if (!hName.isEmpty()) {
                                a3Head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(hName);
                            }
                            int direction = getDirectionStandardTurnout(t, cUtil);
                            int altDirection = EntryPoint.FORWARD;
                            if (direction == EntryPoint.FORWARD) {
                                altDirection = EntryPoint.REVERSE;
                            }
                            if (direction != EntryPoint.UNKNOWN) {
                                if (t.getLayoutBlock().getBlock() == cBlock) {
                                    // turnout is in this block, set direction sensors on b and c signal heads
                                    // Note: need allocation to traverse this turnout
                                    if (!checkDirectionSensor(bHead, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                    if (!checkDirectionSensor(cHead, altDirection,
                                            ConnectivityUtil.OVERALL, cUtil)) {
                                        errorCount++;
                                    }
                                }
                                if (((TrackSegment) t.getConnectC()).getLayoutBlock().getBlock() == cBlock) {
                                    // diverging track segment is in this Block
                                    if (a3Head != null) {
                                        if (!checkDirectionSensor(a3Head, direction,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                    } else {
                                        log.warn("Turnout {} - SSL for head {} cannot handle direction sensor for second diverging track.",
                                                tLinked.getTurnout().getDisplayName(USERSYS),( a1Head==null ? "Unknown" : a1Head.getDisplayName(USERSYS)));
                                        errorCount++;
                                    }
                                } else if (((TrackSegment) t.getConnectB()).getLayoutBlock().getBlock() == cBlock) {
                                    // continuing track segment is in this Block
                                    if (a3Head != null) {
                                        if (!checkDirectionSensor(a1Head, direction,
                                                ConnectivityUtil.OVERALL, cUtil)) {
                                            errorCount++;
                                        }
                                    } else {
                                        if (!checkDirectionSensor(a1Head, direction,
                                                ConnectivityUtil.CONTINUING, cUtil)) {
                                            errorCount++;
                                        }
                                    }
                                }
                            }
                        }
                    } else if ((t.getTurnoutType() == LayoutTurnout.TurnoutType.RH_XOVER)
                            || (t.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER)
                            || (t.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER)) {
                        // crossover turnout
                        // Note: direction is for proceeding from A to B (or D to C)
                        int direction = getDirectionXoverTurnout(t, cUtil);
                        int altDirection = EntryPoint.FORWARD;
                        if (direction == EntryPoint.FORWARD) {
                            altDirection = EntryPoint.REVERSE;
                        }
                        if (direction == EntryPoint.UNKNOWN) {
                            errorCount++;
                        } else {
                            if (((TrackSegment) t.getConnectA()).getLayoutBlock().getBlock() == cBlock) {
                                if ((t.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER)
                                        || (t.getTurnoutType() == LayoutTurnout.TurnoutType.RH_XOVER)) {
                                    if (!placeSensorInCrossover(t.getSignalB1Name(), t.getSignalB2Name(),
                                            t.getSignalC1Name(), t.getSignalC2Name(), altDirection, cUtil)) {
                                        errorCount++;
                                    }
                                } else {
                                    if (!placeSensorInCrossover(t.getSignalB1Name(), t.getSignalB2Name(),
                                            null, null, altDirection, cUtil)) {
                                        errorCount++;
                                    }
                                }
                            }
                            if (((TrackSegment) t.getConnectB()).getLayoutBlock().getBlock() == cBlock) {
                                if ((t.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER)
                                        || (t.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER)) {
                                    if (!placeSensorInCrossover(t.getSignalA1Name(), t.getSignalA2Name(),
                                            t.getSignalD1Name(), t.getSignalD2Name(), direction, cUtil)) {
                                        errorCount++;
                                    }
                                } else {
                                    if (!placeSensorInCrossover(t.getSignalA1Name(), t.getSignalA2Name(),
                                            null, null, direction, cUtil)) {
                                        errorCount++;
                                    }
                                }
                            }
                            if (((TrackSegment) t.getConnectC()).getLayoutBlock().getBlock() == cBlock) {
                                if ((t.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER)
                                        || (t.getTurnoutType() == LayoutTurnout.TurnoutType.RH_XOVER)) {
                                    if (!placeSensorInCrossover(t.getSignalD1Name(), t.getSignalD2Name(),
                                            t.getSignalA1Name(), t.getSignalA2Name(), direction, cUtil)) {
                                        errorCount++;
                                    }
                                } else {
                                    if (!placeSensorInCrossover(t.getSignalD1Name(), t.getSignalD2Name(),
                                            null, null, direction, cUtil)) {
                                        errorCount++;
                                    }
                                }
                            }
                            if (((TrackSegment) t.getConnectD()).getLayoutBlock().getBlock() == cBlock) {
                                if ((t.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER)
                                        || (t.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER)) {
                                    if (!placeSensorInCrossover(t.getSignalC1Name(), t.getSignalC2Name(),
                                            t.getSignalB1Name(), t.getSignalB2Name(), altDirection, cUtil)) {
                                        errorCount++;
                                    }
                                } else {
                                    if (!placeSensorInCrossover(t.getSignalC1Name(), t.getSignalC2Name(),
                                            null, null, altDirection, cUtil)) {
                                        errorCount++;
                                    }
                                }
                            }
                        }
                    } else if (t.isTurnoutTypeSlip()) {
                        int direction = getDirectionSlip(t, cUtil);
                        int altDirection = EntryPoint.FORWARD;
                        if (direction == EntryPoint.FORWARD) {
                            altDirection = EntryPoint.REVERSE;
                        }
                        if (direction == EntryPoint.UNKNOWN) {
                            errorCount++;
                        } else {
                            if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalA1Name()), altDirection, ConnectivityUtil.OVERALL, cUtil)) {
                                errorCount++;
                            }
                            if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalA2Name()), altDirection, ConnectivityUtil.OVERALL, cUtil)) {
                                errorCount++;
                            }
                            if (t.getTurnoutType() == LayoutSlip.TurnoutType.SINGLE_SLIP) {
                                if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalB1Name()), altDirection, ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                            } else {
                                if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalB1Name()), altDirection, ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                                if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalB2Name()), altDirection, ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                            }
                            if (t.getTurnoutType() == LayoutSlip.TurnoutType.SINGLE_SLIP) {
                                if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalC1Name()), direction, ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                            } else {
                                if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalC1Name()), direction, ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                                if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalC2Name()), direction, ConnectivityUtil.OVERALL, cUtil)) {
                                    errorCount++;
                                }
                            }
                            if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalD1Name()), direction, ConnectivityUtil.OVERALL, cUtil)) {
                                errorCount++;
                            }
                            if (!checkDirectionSensor(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(t.getSignalD2Name()), direction, ConnectivityUtil.OVERALL, cUtil)) {
                                errorCount++;
                            }
                        }
                    } else {
                        log.error("Unknown turnout type for turnout {} in Section {}.",
                                t.getTurnout().getDisplayName(USERSYS), getDisplayName(USERSYS));
                        errorCount++;
                    }
                } else {
                    // signal heads missing in turnout
                    missingSignalsTurnouts++;
                }
            }
        }
        // set up missing signal head message, if any
        if ((missingSignalsBB + missingSignalsTurnouts + missingSignalsLevelXings) > 0) {
            String s = "Section - " + getDisplayName(USERSYS);
            if (missingSignalsBB > 0) {
                s = s + ", " + (missingSignalsBB) + " anchor point signal heads missing";
            }
            if (missingSignalsTurnouts > 0) {
                s = s + ", " + (missingSignalsTurnouts) + " turnouts missing signals";
            }
            if (missingSignalsLevelXings > 0) {
                s = s + ", " + (missingSignalsLevelXings) + " level crossings missing signals";
            }
            log.warn(s);
        }

        return errorCount;
    }

    private boolean checkDirectionSensor(SignalHead sh, int direction, int where,
            ConnectivityUtil cUtil) {
        String sensorName = "";
        if (direction == EntryPoint.FORWARD) {
            sensorName = getForwardBlockingSensorName();
        } else if (direction == EntryPoint.REVERSE) {
            sensorName = getReverseBlockingSensorName();
        }
        return (cUtil.addSensorToSignalHeadLogic(sensorName, sh, where));
    }

    private LayoutTurnout getLayoutTurnoutFromTurnoutName(String turnoutName, LayoutEditor panel) {
        Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
        if (t == null) {
            return null;
        }
        for (LayoutTurnout lt : panel.getLayoutTurnouts()) {
            if (lt.getTurnout() == t) {
                return lt;
            }
        }
        return null;
    }

    /**
     * Check that there are Signal Heads at all Entry Points to this Section.
     * This method will warn if it finds unsignaled internal turnouts, but will
     * continue checking. Unsignaled entry points except for those at unsignaled
     * internal turnouts will be considered errors, and will be reported to the
     * user. This method stops searching when it find the first missing Signal
     * Head.
     *
     * @param frame ignored
     * @param panel the panel containing signals to check
     * @return true if successful; false otherwise
     * @deprecated 4.19.7  No usages, so no replacement
     */
    @Deprecated // 4.19.7 for removal;  No usages, so no replacement
    public boolean checkSignals(JmriJFrame frame, LayoutEditor panel) {
        jmri.util.LoggingUtil.deprecationWarning(log, "checkSignals");
        if (panel == null) {
            log.error("Null Layout Editor panel on call to 'checkSignals'");
            return false;
        }
        if (initializationNeeded) {
            initializeBlocks();
        }

        // need code to fully implement checkSignals if (getListOfForwardBlockEntryPoints(getEntryBlock()).size() > 0)
        return true;
    }

    @SuppressWarnings("unused") // not used now, preserved for later use
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "was previously marked with @SuppressWarnings, reason unknown")
    private List<EntryPoint> getListOfForwardBlockEntryPoints(Block b) {
        if (initializationNeeded) {
            initializeBlocks();
        }
        List<EntryPoint> a = new ArrayList<>();
        for (int i = 0; i < mForwardEntryPoints.size(); i++) {
            if (b == (mForwardEntryPoints.get(i)).getBlock()) {
                a.add(mForwardEntryPoints.get(i));
            }
        }
        return a;
    }

    /**
     * Validate the Section. This checks block connectivity, warns of redundant
     * EntryPoints, and otherwise checks internal consistency of the Section. An
     * appropriate error message is logged if a problem is found. This method
     * assumes that Block Paths are correctly initialized.
     *
     * @param lePanel panel containing blocks that will be checked to be
     *                initialized; if null no blocks are checked
     * @return an error description or empty string if there are no errors
     */
    public String validate(LayoutEditor lePanel) {
        if (initializationNeeded) {
            initializeBlocks();
        }
        // validate Paths and Bean Settings if a Layout Editor panel is available
        if (lePanel != null) {
            for (int i = 0; i < (mBlockEntries.size() - 1); i++) {
                Block test = getBlockBySequenceNumber(i);
                if (test == null){
                    log.error("Block {} not found in Block Entries. Paths not checked.",i );
                    break;
                }
                String userName = test.getUserName();
                if (userName != null) {
                    LayoutBlock lBlock = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                    if (lBlock == null) {
                        log.error("Layout Block {} not found. Paths not checked.", userName);
                    } else {
                        lBlock.updatePathsUsingPanel(lePanel);
                    }
                }
            }
        }
        // check connectivity between internal blocks
        if (mBlockEntries.size() > 1) {
            for (int i = 0; i < (mBlockEntries.size() - 1); i++) {
                Block thisBlock = getBlockBySequenceNumber(i);
                Block nextBlock = getBlockBySequenceNumber(i + 1);
                if ( thisBlock == null || nextBlock == null ) {
                        return "Sequential blocks " + i + " " + thisBlock + " or "
                            + i+1 + " " + nextBlock + " are empty in Block List.";
                    }
                if (!connected(thisBlock, nextBlock)) {
                    String s = "Sequential Blocks - " + thisBlock.getDisplayName(USERSYS)
                            + ", " + nextBlock.getDisplayName(USERSYS)
                            + " - are not connected in Section " + getDisplayName(USERSYS) + ".";
                    return s;
                }
                if (!connected(nextBlock, thisBlock)) {
                    String s = "Sequential Blocks - " + thisBlock.getDisplayName(USERSYS)
                            + ", " + nextBlock.getDisplayName(USERSYS)
                            + " - Paths are not consistent - Section " + getDisplayName(USERSYS) + ".";
                    return s;
                }
            }
        }
        // validate entry points
        if ((mForwardEntryPoints.isEmpty()) && (mReverseEntryPoints.isEmpty())) {
            String s = "Section " + getDisplayName(USERSYS) + "has no Entry Points.";
            return s;
        }
        if (mForwardEntryPoints.size() > 0) {
            for (int i = 0; i < mForwardEntryPoints.size(); i++) {
                EntryPoint ep = mForwardEntryPoints.get(i);
                if (!containsBlock(ep.getBlock())) {
                    String s = "Entry Point Block, " + ep.getBlock().getDisplayName(USERSYS)
                            + ", is not a Block in Section " + getDisplayName(USERSYS) + ".";
                    return s;
                }
                if (!connectsToBlock(ep.getFromBlock())) {
                    String s = "Entry Point From Block, " + ep.getBlock().getDisplayName(USERSYS)
                            + ", is not connected to a Block in Section " + getDisplayName(USERSYS) + ".";
                    return s;
                }
                if (!ep.isForwardType()) {
                    String s = "Direction of FORWARD Entry Point From Block "
                            + ep.getFromBlock().getDisplayName(USERSYS) + " to Section "
                            + getDisplayName(USERSYS) + " is incorrectly set.";
                    return s;
                }
                if (!connected(ep.getBlock(), ep.getFromBlock())) {
                    String s = "Entry Point Blocks, " + ep.getBlock().getDisplayName(USERSYS)
                            + " and " + ep.getFromBlock().getDisplayName(USERSYS)
                            + ", are not connected in Section " + getDisplayName(USERSYS) + ".";
                    return s;
                }
            }
        }
        if (mReverseEntryPoints.size() > 0) {
            for (int i = 0; i < mReverseEntryPoints.size(); i++) {
                EntryPoint ep = mReverseEntryPoints.get(i);
                if (!containsBlock(ep.getBlock())) {
                    String s = "Entry Point Block, " + ep.getBlock().getDisplayName(USERSYS)
                            + ", is not a Block in Section " + getDisplayName(USERSYS) + ".";
                    return s;
                }
                if (!connectsToBlock(ep.getFromBlock())) {
                    String s = "Entry Point From Block, " + ep.getBlock().getDisplayName(USERSYS)
                            + ", is not connected to a Block in Section " + getDisplayName(USERSYS) + ".";
                    return s;
                }
                if (!ep.isReverseType()) {
                    String s = "Direction of REVERSE Entry Point From Block "
                            + ep.getFromBlock().getDisplayName(USERSYS) + " to Section "
                            + getDisplayName(USERSYS) + " is incorrectly set.";
                    return s;
                }
                if (!connected(ep.getBlock(), ep.getFromBlock())) {
                    String s = "Entry Point Blocks, " + ep.getBlock().getDisplayName(USERSYS)
                            + " and " + ep.getFromBlock().getDisplayName(USERSYS)
                            + ", are not connected in Section " + getDisplayName(USERSYS) + ".";
                    return s;
                }
            }
        }
        return "";
    }

    private boolean connected(Block b1, Block b2) {
        if ((b1 != null) && (b2 != null)) {
            List<Path> paths = b1.getPaths();
            for (int i = 0; i < paths.size(); i++) {
                if (paths.get(i).getBlock() == b2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set/reset the display to use alternate color for unoccupied blocks in
     * this section. If Layout Editor panel is not present, Layout Blocks will
     * not be present, and nothing will be set.
     *
     * @param set true to use alternate unoccupied color; false otherwise
     */
    public void setAlternateColor(boolean set) {
        for (Block b : mBlockEntries) {
            String userName = b.getUserName();
            if (userName != null) {
                LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                if (lb != null) {
                    lb.setUseExtraColor(set);
                }
            }
        }
    }

    /**
     * Set/reset the display to use alternate color for unoccupied blocks in
     * this Section. If the Section already contains an active block, then the
     * alternative color will be set from the active block, if no active block
     * is found or we are clearing the alternative color then all the blocks in
     * the Section will be set. If Layout Editor panel is not present, Layout
     * Blocks will not be present, and nothing will be set.
     *
     * @param set true to use alternate unoccupied color; false otherwise
     */
    public void setAlternateColorFromActiveBlock(boolean set) {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        boolean beenSet = false;
        if (!set || getState() == FREE || getState() == UNKNOWN) {
            setAlternateColor(set);
        } else if (getState() == FORWARD) {
            for (Block b : mBlockEntries) {
                if (b.getState() == Block.OCCUPIED) {
                    beenSet = true;
                }
                if (beenSet) {
                    String userName = b.getUserName();
                    if (userName != null) {
                        LayoutBlock lb = lbm.getByUserName(userName);
                        if (lb != null) {
                            lb.setUseExtraColor(set);
                        }
                    }
                }
            }
        } else if (getState() == REVERSE) {
            for (Block b : mBlockEntries) {
                if (b.getState() == Block.OCCUPIED) {
                    beenSet = true;
                }
                if (beenSet) {
                    String userName = b.getUserName();
                    if (userName != null) {
                        LayoutBlock lb = lbm.getByUserName(userName);
                        if (lb != null) {
                            lb.setUseExtraColor(set);
                        }
                    }
                }
            }
        }
        if (!beenSet) {
            setAlternateColor(set);
        }
    }

    /**
     * Set the block values for blocks in this Section.
     *
     * @param name the value to set all blocks to
     */
    public void setNameInBlocks(String name) {
        for (Block b : mBlockEntries) {
            b.setValue(name);
        }
    }

    /**
     * Set the block values for blocks in this Section.
     *
     * @param value the name to set block values to
     */
    public void setNameInBlocks(Object value) {
        for (Block b : mBlockEntries) {
            b.setValue(value);
        }
    }

    public void setNameFromActiveBlock(Object value) {
        boolean beenSet = false;
        if (value == null || getState() == FREE || getState() == UNKNOWN) {
            setNameInBlocks(value);
        } else if (getState() == FORWARD) {
            for (Block b : mBlockEntries) {
                if (b.getState() == Block.OCCUPIED) {
                    beenSet = true;
                }
                if (beenSet) {
                    b.setValue(value);
                }
            }
        } else if (getState() == REVERSE) {
            for (Block b : mBlockEntries) {
                if (b.getState() == Block.OCCUPIED) {
                    beenSet = true;
                }
                if (beenSet) {
                    b.setValue(value);
                }
            }
        }
        if (!beenSet) {
            setNameInBlocks(value);
        }
    }

    /**
     * Clear the block values for blocks in this Section.
     */
    public void clearNameInUnoccupiedBlocks() {
        for (Block b : mBlockEntries) {
            if (b.getState() == Block.UNOCCUPIED) {
                b.setValue(null);
            }
        }
    }

    /**
     * Suppress the update of a memory variable when a block goes to unoccupied,
     * so the text set above doesn't get wiped out.
     *
     * @param set true to suppress the update; false otherwise
     */
    public void suppressNameUpdate(boolean set) {
        for (Block b : mBlockEntries) {
            String userName = b.getUserName();
            if (userName != null) {
                LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(userName);
                if (lb != null) {
                    lb.setSuppressNameUpdate(set);
                }
            }
        }
    }

    final public static int USERDEFINED = 0x01; //Default Save all the information
    final public static int SIGNALMASTLOGIC = 0x02; //Save only the name, blocks will be added by the signalmast logic
    final public static int DYNAMICADHOC = 0x00;  //created on an as required basis, not to be saved.

    private int sectionType = USERDEFINED;

    /**
     * Set Section Type.
     * <ul>
     * <li>USERDEFINED - Default Save all the information.
     * <li>SIGNALMASTLOGIC - Save only the name, blocks will be added by the SignalMast logic.
     * <li>DYNAMICADHOC - Created on an as required basis, not to be saved.
     * </ul>
     * @param type constant of section type.
     */
    public void setSectionType(int type) {
        sectionType = type;
    }

    /**
     * Get Section Type.
     * Defaults to USERDEFINED.
     * @return constant of section type.
     */
    public int getSectionType() {
        return sectionType;
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSection");
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            NamedBean nb = (NamedBean) evt.getOldValue();
            if (nb instanceof Sensor) {
                if (nb.equals(getForwardBlockingSensor())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("VetoBlockingSensor", nb.getBeanType(), Bundle.getMessage("Forward"), Bundle.getMessage("Blocking"), getDisplayName()), e); // NOI18N
                }
                if (nb.equals(getForwardStoppingSensor())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("VetoBlockingSensor", nb.getBeanType(), Bundle.getMessage("Forward"), Bundle.getMessage("Stopping"), getDisplayName()), e);
                }
                if (nb.equals(getReverseBlockingSensor())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("VetoBlockingSensor", nb.getBeanType(), Bundle.getMessage("Reverse"), Bundle.getMessage("Blocking"), getDisplayName()), e);
                }
                if (nb.equals(getReverseStoppingSensor())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("VetoBlockingSensor", nb.getBeanType(), Bundle.getMessage("Reverse"), Bundle.getMessage("Stopping"), getDisplayName()), e);
                }
            }
            if (nb instanceof Block) {
                Block check = (Block)nb;
                if (getBlockList().contains(check)) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("VetoBlockInSection", getDisplayName()), e);
                }
            }
        }
        // "DoDelete" case, if needed, should be handled here.
    }

    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            getBlockList().forEach((block) -> {
                if (bean.equals(block)) {
                    report.add(new NamedBeanUsageReport("SectionBlock"));
                }
            });
            if (bean.equals(getForwardBlockingSensor())) {
                report.add(new NamedBeanUsageReport("SectionSensorForwardBlocking"));
            }
            if (bean.equals(getForwardStoppingSensor())) {
                report.add(new NamedBeanUsageReport("SectionSensorForwardStopping"));
            }
            if (bean.equals(getReverseBlockingSensor())) {
                report.add(new NamedBeanUsageReport("SectionSensorReverseBlocking"));
            }
            if (bean.equals(getReverseStoppingSensor())) {
                report.add(new NamedBeanUsageReport("SectionSensorReverseStopping"));
            }
        }
        return report;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Section.class);

}
