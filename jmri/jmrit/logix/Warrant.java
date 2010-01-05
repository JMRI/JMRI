package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
//import jmri.Path;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * An Warrant contains the operating permissions and directives needed for
 * a train to proceed from an Origin to a Destination
 * <P>
 * Version 1.11 - remove setting of SignalHeads
 *
 * @version
 * @author	Pete Cressman  Copyright (C) 2009, 2010
 */
public class Warrant extends jmri.implementation.AbstractNamedBean 
                    implements ThrottleListener, java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");
    // permanent members.
    private ArrayList <BlockOrder> _savedOrders = new ArrayList <BlockOrder>();
    private BlockOrder _viaOrder;
    private ArrayList <ThrottleSetting> _throttleCommands = new ArrayList <ThrottleSetting>();
    private String _trainId;
    private DccLocoAddress _dccAddress;
    private boolean _runBlind;              // don't use block detection

    // transient members
    private List <BlockOrder> _orders;          // temp orders used in run mode
    private List <ThrottleSetting> _commands;   // temp commands used in run mode
    private DccThrottle _throttle;              // run mode throttle
    private LearnThrottleFrame _student;        // need to callback learning throttle in learn mode
    private boolean _tempRunBlind;              // run mode flag

    private int     _idxCurrentOrder;       // Index of block at head of train (if running)
    private int     _idxTrailingOrder;      // index of block train has just left (if running)
    private int     _runMode;
    private Engineer _engineer;         // thread that runs the train
    private boolean _allocated;         // all Blocks of _orders have been allocated
    private boolean _routeSet;          // all Blocks of _orders have paths set for route
    private String  _blockingName;      // Block Name allocated to another warrant, blocking allocation by this

    // Throttle modes
    public static final int MODE_NONE = 0;
    public static final int MODE_LEARN = 1;
    public static final int MODE_RUN = 2;

    // control states
    public static final int HALT = 1;
    public static final int RESUME = 2;
    public static final int ABORT = 3;

    /* Speed aspects as defined by Doughlas A. Kerr - "Rail Signal Aspects and Indications"
    * doug.kerr.home.att.net/pumpkin/Rail_Signal_Aspects.pdf 
    */
    public static final int SPEED_MASK      = 0x07;     // least significant 3 bits
    public static final int STOP_SPEED      = 0x01;     // No Speed
    public static final int RESTRICTED_SPEED= 0x02;     // Train able to stop within 1/2 visual range (10-15mph)
    public static final int SLOW_SPEED      = 0x03;     // Typically 15 mph  (25% of NORMAL)
    public static final int MEDIUM_SPEED    = 0x04;     // Typically 30 mph (50% of NORMAL)
    public static final int LIMITED_SPEED   = 0x05;     // Typically 40-45 mph  (75% of NORMAL)
    public static final int NORMAL_SPEED    = 0x06;     // Varies with road and location
    public static final int MAXIMUM_SPEED   = 0x07;     // "full" throttle

    /*
    /**
     * Create an object with no route defined.
     * The list of BlockOrders is the route from an Origin to a Destination
     */
    public Warrant(String sName, String uName) {
        super(sName.toUpperCase(), uName);
        _idxCurrentOrder = 0;
        _idxTrailingOrder = -1;
        _orders = _savedOrders;
        _runBlind = false;
    }

    // _state not used (yet)
    public int getState() { 
        return UNKNOWN;  
    }
    public void setState(int state) {
    }

    public void clearAll() {
        _savedOrders = new ArrayList <BlockOrder>();
        _viaOrder = null;
        _throttleCommands = new ArrayList <ThrottleSetting>();
        _trainId = null;
        _dccAddress = null;
        _orders = _savedOrders;
        _runBlind = false;
        firePropertyChange("save", _trainId, null);
    }
    /**
    * Return permanently saved BlockOrders
    */
    public List <BlockOrder> getOrders() {
        return _savedOrders;
    }
    /**
    * Add permanently saved BlockOrder
    */
    public void addBlockOrder(BlockOrder order) {
        _savedOrders.add(order);
    }

    /**
    * Return permanently saved Origin
    */
    public BlockOrder getfirstOrder() {
        if (_savedOrders.size()==0) { return null; }
        return new BlockOrder(_orders.get(0)); 
    }

    /**
    * Return permanently saved Destination
    */
    public BlockOrder getLastOrder() {
        if (_savedOrders.size()==0) { return null; }
        return new BlockOrder(_orders.get(_savedOrders.size()-1)); 
    }

    /**
    * Return permanently saved BlockOrder that must be included in the route
    */
    public BlockOrder getViaOrder() {
        if (_viaOrder==null) { return null; }
        return new BlockOrder(_viaOrder);
    }
    public void setViaOrder(BlockOrder order) { _viaOrder = order; }

    public BlockOrder getCurrentBlockOrder() {
        return getBlockOrderAt(_idxCurrentOrder);
    }

    public int getCurrentOrderIndex() {
        return _idxCurrentOrder;
    }


    /**
    * Call is only valid when in MODE_LEARN and MODE_RUN
    */
    private int getIndexOfBlock(OBlock block) {
        for (int i=0; i<_orders.size(); i++){
            if (_orders.get(i).getBlock().equals(block)) {
                return i;
            }
        }
        return -1;
    }
    /**
    * Call is only valid when in MODE_LEARN and MODE_RUN
    */
    protected BlockOrder getBlockOrderAt(int index) {
        if (index>=0 && index<_orders.size()) {
            return _orders.get(index); 
        }
        return null;
    }
    /**
    * Call is only valid when in MODE_LEARN and MODE_RUN
    */
    protected OBlock getBlockAt(int idx) {

        BlockOrder bo = getBlockOrderAt(idx);
        if (bo!=null) {
            return bo.getBlock();
        }
        return null;
    }
    /**
    * Call is only valid when in MODE_LEARN and MODE_RUN
    */
    private int getBlockStateAt(int idx) {

        OBlock b = getBlockAt(idx);
        if (b!= null) {
            return b.getState();
        }
        return  OBlock.UNKNOWN;
    }

    public List <ThrottleSetting> getThrottleCommands() {
        return _throttleCommands;
    }
    public void addThrottleCommand(ThrottleSetting ts) {
        _throttleCommands.add(ts);
    }


    public String getTrainId() { return _trainId; }
    public boolean setTrainId(String id) {
        _trainId = id; 
        RosterEntry train = Roster.instance().entryFromTitle(id);
        if (train != null) {
            _dccAddress = train.getDccLocoAddress();
            return true;
        }
        return false;
    }

    public DccLocoAddress getDccAddress() { return _dccAddress;  }
    public void setDccAddress(DccLocoAddress address) { _dccAddress = address;  }

    public boolean getRunBlind() {return _runBlind; }
    public void setRunBlind(boolean runBlind) { _runBlind = runBlind; }

    /******************************** state queries *****************/
    /**
    * Listeners are installed for the route
    */
    public boolean isAllocated() { return _allocated; }
    /**
    * Turnouts and signals are set for the route
    */
    public boolean hasRouteSet() { return _routeSet; }

    /**
    * Test if the permanent saved blocks of this warrant are free
    * (unoccupied and unallocated)
    */
    public boolean routeIsFree() {
        for (int i=0; i<_savedOrders.size(); i++) {
            OBlock block = _savedOrders.get(i).getBlock();
            if (!block.isFree()) { return false; }
        }
        return true;
    }

    /**
    * Test if the permanent saved blocks of this warrant are occupied
    */
    public boolean routeIsOccupied() {
        for (int i=1; i<_savedOrders.size(); i++) {
            OBlock block = _savedOrders.get(i).getBlock();
            if ((block.getState() & OBlock.OCCUPIED) !=0) { 
                return true; 
            }
        }
        return false;
    }

    boolean checkCommands(List <ThrottleSetting> commands) {
        for (int i=0; i<commands.size(); i++) {
            if (commands.get(i).getTime() instanceof String) {
                return false;   // must be a synch command
            }
        }
        return true;
    }
    
    /*************** Methods for running trains ****************/

    public int getRunMode() { return _runMode; }

    /**
    * Starts or ends an automated train run.
    * setRoute nust be called before calling this method.
    * @param run set Run throttle command or Stop 
    * @return returns an error message (or null on success)
    */
    public String runAutoTrain(boolean run) {
        return setRunMode(run?MODE_RUN:MODE_NONE, _dccAddress, null, _throttleCommands, _runBlind);
    }

    public boolean isWaiting() {
        if (_engineer!=null){
            return _engineer.isWaiting();
        }
        return false;
    }


    /**
    * Sets up recording and playing back throttle commands - also cleans up afterwards.
    *  MODE_LEARN and MODE_RUN sessions must end by calling again with MODE_NONE.  It is
    * important that the route be deAllocated (remove listeners).
    * <p>
    * Rule for (auto) MODE_RUN:
    * 1. At least the Origin block must be owned (allocated) by this warrant.
    * (block.value == this)  and path setfor Run Mode
    * Rule for (auto) LEARN_RUN:
    * 2. Entire Route must be allocated and Route Set for Learn Mode. 
    * i.e. this warrant has listeners on all blocks in the route.
    */
    protected String setRunMode(int mode, DccLocoAddress address, 
                                 LearnThrottleFrame student, 
                                 List <ThrottleSetting> commands, boolean runBlind) 
    {
        if(log.isDebugEnabled()) log.debug("setRunMode("+mode+")  _runMode= "+_runMode);
        String msg = null;
        int oldMode = _runMode;
        if (mode == MODE_NONE) {
            if (_engineer != null) {
                _engineer.abort();
                _engineer = null;
            }
            if (_student !=null) {
                _student.dispose();
                _student = null;
            }
            if (_throttle != null) {
                try {
                    _throttle.removePropertyChangeListener(this);
                    _throttle.release();
                    DccLocoAddress l = (DccLocoAddress) _throttle.getLocoAddress();
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(l.getNumber(), this);
                } catch (Exception e) {
                    // null pointer catch and maybe other such.
                    log.warn("Throttle release and cancel threw: "+e);
                }
                for (int i=0; i<_orders.size(); i++) {
                    OBlock block = _orders.get(i).getBlock();
                    Sensor sensor = block.getSensor();
                    if (sensor != null ) {
                        sensor.removePropertyChangeListener(this);
                    }
                }
            }
            _runMode = mode;
            deAllocate();       // "fleet" not implemented
            _idxCurrentOrder = 0;
            _idxTrailingOrder = -1;
            _orders = _savedOrders;
        } else if (_runMode==MODE_LEARN || _runMode==MODE_RUN) {
            msg = java.text.MessageFormat.format(rb.getString("WarrantInUse"),
                        (_runMode==Warrant.MODE_RUN ? 
                                 rb.getString("RunTrain"):rb.getString("LearnMode")));
            log.error(msg);
            return msg;
        } else {
            if (!_routeSet && runBlind) {
                msg = java.text.MessageFormat.format(rb.getString("BlindRouteNotSet"),getDisplayName());
                log.error(msg);
                return msg;
            }
            // start is OK if block 0 is occupied (or dark - in which case user is responsible)
            if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED|OBlock.DARK))==0) {
                if(log.isDebugEnabled()) log.debug("Block "+getBlockAt(0).getDisplayName()+", state= "+getBlockStateAt(0));
                msg = java.text.MessageFormat.format(rb.getString("badStart"),getDisplayName());
                log.error(msg);
                return msg;
            }
            if (mode == MODE_LEARN) {
                if (student == null) {
                    msg = java.text.MessageFormat.format(rb.getString("noLearnThrottle"),getDisplayName());
                    log.error(msg);
                    return msg;
                }
                _student = student; 
             } else if (mode == MODE_RUN) {
                 if (commands == null || commands.size()== 0) {
                     msg = java.text.MessageFormat.format(rb.getString("NoCommands"),getDisplayName());
                     log.error(msg);
                     return msg;
                 }
                 if (!checkCommands(commands)) {
                    if ( runBlind ) {
                        msg = java.text.MessageFormat.format(rb.getString("CannotSynchBlind"),getDisplayName());
                        log.error(msg);
                         return msg;
                     }
                 }
                 _commands = commands;
             }
            if (address == null)  {
                msg = java.text.MessageFormat.format(rb.getString("NoAddress"),getDisplayName());
                log.error(msg);
                return msg;
            }
            _runMode = mode;
            _tempRunBlind = runBlind;
            if (!InstanceManager.throttleManagerInstance().
                requestThrottle(address.getNumber(), address.isLongAddress(),this)) {
                msg = java.text.MessageFormat.format(rb.getString("trainInUse"), address.getNumber());
                log.error(msg);
                return msg;
            }
            /*
            if (mode == MODE_RUN) {
                msg = allocateRoute();
                if (msg != null) {
                    log.warn(msg);
                    // Must be able to allocate orgin block
                    BlockOrder bo = _orders.get(0);
                    OBlock block = bo.getBlock();
                    msg = block.allocate(this);
                    if (msg != null) {
                        msg = java.text.MessageFormat.format(rb.getString("BlockNotAllocated"), 
                                        msg, block.getDisplayName());
                        log.error(msg);
                        return msg;
                    }
                }
            } */
        }
        _runMode = mode;
        firePropertyChange("runMode", new Integer(oldMode), new Integer(_runMode));
        if(log.isDebugEnabled()) log.debug("Exit setRunMode()  _runMode= "+_runMode+", msg= "+msg);
        return msg;
    }

    public boolean controlRunTrain(int idx) {
        if (_engineer == null) { return false; }
        if(log.isDebugEnabled()) log.debug("controlRunTrain= "+idx);
        int oldIndex = 0;
        if (_engineer.isWaiting()){
            oldIndex = HALT; 
        }
        synchronized(_engineer) { 
            try {
                switch (idx) {
                    case HALT:
                        _engineer.setHalt(true);
                        break;
                    case RESUME:
                        _engineer.setHalt(false);
                        //_engineer.notify();
                        break;
                    case ABORT:
                        _engineer.abort();
                        _engineer.notifyAll();
                        break;
                }
            } catch (java.lang.IllegalMonitorStateException imse) {
                log.error("IllegalMonitorStateException "+imse);
                return false;
            }
        }
        firePropertyChange("controlChange", new Integer(oldIndex), new Integer(idx));
        return true;
    }

    public void notifyThrottleFound(DccThrottle t)
    {
    	if (t == null) { return; }
        _throttle = t;
        if(log.isDebugEnabled()) {
           log.debug("notifyThrottleFound address= " +t.getLocoAddress().toString()+" _runMode= "+_runMode);
        }
        _throttle.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) log.debug("addPropertyChangeListener ");
        _idxCurrentOrder = 0;
        _idxTrailingOrder = -1;

        for (int i=0; i<_orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
            Sensor sensor = block.getSensor();
            if (sensor != null ) {
                sensor.addPropertyChangeListener(this);
            }
        }
        if (_runMode == MODE_LEARN) {
            _student.notifyThrottleFound(_throttle);
        } else {
            _engineer = new Engineer(_commands);
            new Thread(_engineer).start();
        }
    }

    /**
    * Allocate the current saved blocks of this warrant.
    * Installs listeners for the entire route.  Sets this warrant into
    * Block's value field.  Returns the index of a block allocated to
    * another warrant, (i.e. value field not null). 
    * @return index of block that failed to be allocated to this warrant
    * @return -1 if entire route is allocated to this warrant
    */
    public String allocateRoute() {
        if (_allocated) {
            return null;
        }
        boolean allocated = true;
        String msg = null;
        for (int i=0; i<_orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
            String name = block.allocate(this);
            if (name != null) {
                allocated = false;
                msg = java.text.MessageFormat.format(rb.getString("BlockNotAllocated"), 
                                name, getBlockOrderAt(i).getBlock().getDisplayName());
                deAllocate();
                break;
            }
        }
        boolean old = _allocated; 
        _allocated = allocated;
        firePropertyChange("allocate", new Boolean(old), new Boolean(_allocated));
        return msg;
    }

    /**
    * Deallocates blocks from the current BlockOrder list
    */
    public void deAllocate() {
        for (int i=0; i<_orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
            block.deAllocate(this);
        }
        boolean old = _allocated;
        _allocated = false;
        _routeSet = false;
        firePropertyChange("allocate", new Boolean(old), new Boolean(false));
    }

    /**
    * Set the route paths and signal protection for the warrant.  Returns the index
    * of the first block that failed allocation to this warrant.  When running with 
    * block detection, only the first block must be allocated and have its path set.
    * @param delay - delay in seconds, between setting signals and throwing turnouts
    * @param orders - BlockOrder list of route.  If null, use permanent warrant copy.
    * @return index of block that failed allocation to this warrant
    * @return -1 if entire route is allocated to this warrant
    */
    public String setRoute(int delay, List <BlockOrder> orders) {
        // we assume our train is occupying the first block
        boolean routeSet = true;
        boolean allocated = true;
        String msg = null;
        if (orders==null) {
            _orders = _savedOrders;
        } else {
            _orders = orders;
        }
        if (log.isDebugEnabled()) log.debug("setRoute: _orders.size()= "+_orders.size());
        if (_orders.size()==0) {
            return java.text.MessageFormat.format(rb.getString("NoRouteSet"),"here","there");
        }
        BlockOrder bo = _orders.get(0);
        OBlock block = bo.getBlock();
        String name = block.allocate(this);
        if (name != null) {
            routeSet = false;
            msg = java.text.MessageFormat.format(rb.getString("BlockNotAllocated"), 
                            name, block.getDisplayName());
            allocated = false;
        } else {
             // allocated to this, We assume the train of this warrant occupies the first block 
            // exit speed is determined by setPathAndGetPermissibleSpeed() into next block (i=1).
            block.setPath(bo.getPathName(), 0);
            for (int i=1; i<_orders.size(); i++) {
                bo = _orders.get(i);
                block = bo.getBlock();
                name = block.allocate(this); 
                if (name==null) {
                    if (log.isDebugEnabled()) log.debug("setRoute: block state= "+block.getState()+", for "+bo.toString());
                    if (bo.setPathAndGetPermissibleSpeed() == STOP_SPEED) {
                        msg = java.text.MessageFormat.format(rb.getString("BlockRougeOccupied"), 
                                        block.getDisplayName());
                        routeSet = false;
                    }
                } else {
                    routeSet = false;
                    msg = java.text.MessageFormat.format(rb.getString("BlockNotAllocated"), 
                                    name, block.getDisplayName());
                    allocated = false;
                    deAllocate();
                    break;
                }
            }
        }

        _allocated = allocated;
        boolean old = _routeSet;
        _routeSet = routeSet;
        firePropertyChange("setRoute", new Boolean(old), new Boolean(_routeSet));
        return msg;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) log.debug("propertyChange "+evt.getPropertyName()+
                                            " old= "+evt.getOldValue()+" new= "+evt.getNewValue()+" _blockingName= "+_blockingName);
        if (evt.getPropertyName() == "KnownState") {
            Sensor sensor = (Sensor)evt.getSource();
            int newState = ((Number) evt.getNewValue()).intValue();
            int oldState = ((Number) evt.getOldValue()).intValue();
            if (newState==oldState) {
                return;
            }
            for (int i=_idxTrailingOrder+1; i<_orders.size(); i++) {
                OBlock block = getBlockAt(i);
                if (block!=null && sensor.equals(block.getSensor()) ) {
                    if (newState == Sensor.ACTIVE) { 
                        goingActive(block);
                    }
                    else if (newState == Sensor.INACTIVE) {
                        goingInactive(block);
                    }
                    // otherwise state == Sensor.UNKNOWN) or Sensor.INCONSISTENT
                    return;
                }
            }
        } else if (_blockingName != null && evt.getPropertyName() == "value") {
            // another warrant has surrendered ownership of block
            Object obj = evt.getOldValue();
            if (obj instanceof jmri.implementation.AbstractNamedBean) {
                if (_blockingName.equals(((jmri.implementation.AbstractNamedBean)obj).getDisplayName())) {
                    // block allocated to another warrant has been deallocated
                    getBlockAt(_idxCurrentOrder+1).removePropertyChangeListener(this);
                    _blockingName = null;
                    acquireNextTwoBlocks();
                }
            }
        }
    }

    /**
    * Block in the route is going active.
    * check if this is the next block of the train moving under the warrant
    *
    */
    public void goingActive(OBlock block) {
        if (_runMode==MODE_NONE)  { return; }
        int oldIndex = _idxCurrentOrder;
        int activeIdx = getIndexOfBlock(block);
        if (activeIdx < 0) {
            log.error("\""+block.getDisplayName()+"\" is not a block in this route!");
        }
        if (log.isDebugEnabled()) log.debug("Block "+block.getDisplayName()+" goingActive. activeIdx= "+
                                            activeIdx+", _idxCurrentOrder= "+_idxCurrentOrder+
                                            " _orders.size()= "+_orders.size());
        if (_runMode==MODE_LEARN) {
            if (activeIdx > _idxCurrentOrder) {
                if (log.isDebugEnabled()) log.debug("Learn mode Train entering Block "+block.getDisplayName());
                // we assume it is our train entering the block - cannot guarantee it, but what else?
                _idxCurrentOrder = activeIdx;
                firePropertyChange("blockChange", new Integer(oldIndex), new Integer(_idxCurrentOrder));
                return;
            }
        } else if (activeIdx == _idxCurrentOrder+1) {
            if (log.isDebugEnabled()) log.debug("Auto-Run Train entering Block "+block.getDisplayName());
            // we assume it is our train entering the block - cannot guarantee it, but what else?
            _idxCurrentOrder = activeIdx;
            firePropertyChange("blockChange", new Integer(oldIndex), new Integer(_idxCurrentOrder));
            if (_tempRunBlind) { return; }

            if (_runMode==MODE_RUN ) {
                _engineer.synchNotify(activeIdx); // notify engineer of possible control point
                if (_idxCurrentOrder == _orders.size()-1) {
                    // must be in destination block
                    //BlockOrder activeBO = getBlockOrderAt(activeIdx);
                    //_engineer.setSpeedRestriction(activeBO.setPathAndGetPermissibleSpeed());
                    return;
                }
            }
        }
        if (_tempRunBlind) { return; }
        if (_runMode==MODE_RUN ) {
            if (activeIdx >= _idxCurrentOrder && activeIdx < _orders.size()-1) {
                acquireNextTwoBlocks();
            }
        }
    }

    /**
    * Block in the route is going Inactive 
    */
    public int goingInactive(OBlock block) {
        if (_runMode==MODE_NONE)  { return OBlock.UNOCCUPIED; }
        int idx = getIndexOfBlock(block);
        if (log.isDebugEnabled()) log.debug("Block "+block.getDisplayName()+" goingInactive. idx= "+
                                            idx+", _idxCurrentOrder= "+_idxCurrentOrder);
        if (idx < _idxCurrentOrder) {
            // block is behind train
            _idxTrailingOrder = idx;
            if (this.equals(block.getValue())) {
                block.setValue(null);       // "fleet" not implemented
            }
            return OBlock.UNOCCUPIED;
        }
        // if it is the next block ahead of the train, we can move (presumeably we have stopped)
        if (_runMode==MODE_RUN && (idx -_idxCurrentOrder) > 1 ) {
            block.setState(OBlock.UNOCCUPIED | OBlock.ALLOCATED);
            if (idx < _orders.size()-1) {
                acquireNextTwoBlocks();
            }
        }
        return OBlock.UNOCCUPIED | OBlock.ALLOCATED;
    }

    /**
    * Allocate next blocks to this warrant, if not already done so.  Set speed according to
    * the signal at the entrance to the block (setPathAndGetPermissibleSpeed) and 
    * use the most restrictive for conditions found in the next two blocks from the current block.
    * "setPathAndGetPermissibleSpeed" can only be set on blocks reserved (allocated)
    * by this warrant.
    */
    private void acquireNextTwoBlocks() {
        int speedType = NORMAL_SPEED;
        BlockOrder nextBO = getBlockOrderAt(_idxCurrentOrder+1);
        OBlock nextBlock = nextBO.getBlock();
        _blockingName = nextBlock.allocate(this);
        if ( _blockingName != null) {
            log.warn("Block \""+nextBlock.getDisplayName()+"\" in warrant \""+getDisplayName()+
                     "\" is allocated to warrant \"" +_blockingName+"\"");
            speedType = STOP_SPEED;
            nextBlock.addPropertyChangeListener(this);
        } else {
            speedType = nextBO.setPathAndGetPermissibleSpeed();
            // in case there is no signal, also check occupancy
            if ((getBlockStateAt(_idxCurrentOrder+2) & OBlock.OCCUPIED) != 0) {
                // block beyond occupied. if aquirable, Approach slow, else stop.
                speedType = Math.min(MEDIUM_SPEED, speedType);
            } // else next 2 blocks unoccupied, 
        }
        // attempt to allocatable remaining blocks in the route
        for (int i=_idxCurrentOrder+2; i<_orders.size(); i++) {
            nextBlock = getBlockAt(i);
            nextBlock.allocate(this);
        }
        _engineer.setSpeedRestriction(speedType);
    }

    public int getCurrentCommandIndex() {
        if (_engineer != null) {
            return _engineer.getCurrentCommandIndex();
        }
        return -1;
    }


    Float[] _speedRatio = { -1.0F, 0.0F, 0.15F, 0.25F, 0.50F, 0.75F, 1.0F, 1.25F };

    /************************** Thread running the train *****************/

    class Engineer implements Runnable {

        private int     _idxCurrentCommand;     // current throttle command
        private int     _speedType = NORMAL_SPEED;
        private float   _minSpeed = 1.0f;
        private boolean _abort = false;
        private boolean _halt = false;  // halt/resume from user's control
        private boolean _wait = false;  // waits for signals/occupancy/allocation to clear
        private int     _orderIndex;
        private List <ThrottleSetting> _throttleCommands;

        Engineer(List <ThrottleSetting> commands) {
            _idxCurrentCommand = -1;
            _throttleCommands = commands;
            setSpeedStepMode(_throttle.getSpeedStepMode());
        }

        public void run() {
            if (log.isDebugEnabled()) log.debug("Engineer started");
            goingActive(_orders.get(0).getBlock());

            while (_idxCurrentCommand+1 < _throttleCommands.size() && !_abort) {
                long et = System.currentTimeMillis();
                ThrottleSetting ts = _throttleCommands.get(_idxCurrentCommand+1);
                Object time = ts.getTime();
                String command = ts.getCommand().toUpperCase();
                _orderIndex = -1;
                if (!command.equals("NOOP"))  {
                    synchronized(this) {
                        try {
                            if (time instanceof Long) {
                                long t = ((Long)time).longValue();
                                if (t > 0){
                                    wait(t);
                                }
                            } else if (time.toString().equals ("Synch")) {
                                _orderIndex = getIndexOfBlock(InstanceManager.oBlockManagerInstance().
                                                              provideOBlock(ts.getBlockName()));
                                if (_orderIndex < 0) {
                                    log.error("\""+ts.getBlockName()+"\" is not a block in this route!");
                                }
                                wait();
                            }
                        } catch (InterruptedException ie) {
                            log.error("InterruptedException "+ie);
                        } catch (java.lang.IllegalArgumentException iae) {
                            log.error("IllegalArgumentException "+iae);
                        }
                    }
                }
                if (_abort) { break; }
                _idxCurrentCommand++;
                synchronized(this) {
                    try {
                        if (_wait) {
                            wait();
                        }
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException "+ie);
                    }
                }
                if (_abort) { break; }
                try {
                    if (command.equals("SPEED")) {
                        float speed = Float.parseFloat(ts.getValue());
                        setSpeed(speed);
                    } else if (command.equals("SPEEDSTEP")) {
                        int step = Integer.parseInt(ts.getValue());
                        setStep(step);
                    } else if (command.equals("FORWARD")) {
                        boolean isForward = Boolean.parseBoolean(ts.getValue());
                        _throttle.setIsForward(isForward);
                    } else if (command.startsWith("F")) {
                        int cmdNum = Integer.parseInt(command.substring(1));
                        boolean isTrue = Boolean.parseBoolean(ts.getValue());
                        setFunction(cmdNum, isTrue);
                    } else if (command.startsWith("LOCKF")) {
                        int cmdNum = Integer.parseInt(command.substring(5));
                        boolean isTrue = Boolean.parseBoolean(ts.getValue());
                        setLockFunction(cmdNum, isTrue);
                    }
                    firePropertyChange("Command", new Integer(_idxCurrentCommand-1), new Integer(_idxCurrentCommand));
                    et = System.currentTimeMillis()-et;
                    if (log.isDebugEnabled()) log.debug("Command #"+_idxCurrentCommand+": "+
                                                        ts.toString()+" et= "+et);
                } catch (Exception e) {
                      log.error("Command failed! "+ts.toString()+" - "+e);
                }
             }
            // shut down
            setRunMode(MODE_NONE, null, null, null, false);
            if (log.isDebugEnabled()) log.debug("Engineer shut down.");
        }

        private void setStep(int step) {
            setSpeedStepMode(step);
            _throttle.setSpeedStepMode(step);
        }

        private void setSpeedStepMode(int step) {
            switch (step) {
                case DccThrottle.SpeedStepMode14:
                    _minSpeed = 1.0f/15;
                    break;
                case DccThrottle.SpeedStepMode27:
                    _minSpeed = 1.0f/28;
                    break;
                case DccThrottle.SpeedStepMode28:
                    _minSpeed = 1.0f/29;
                    break;
                default:
                    _minSpeed = 1.0f/127;
                    break;
            }
        }

        public int getCurrentCommandIndex() {
            return _idxCurrentCommand;
        }

        synchronized public void synchNotify(int blockIndex) {
            if (log.isDebugEnabled()) log.debug("synch: index of block going active= "+blockIndex+
                                                ", index of block in current throttle order= "+_orderIndex);
            if (blockIndex == _orderIndex && !_halt) {
                try {
                    this.notify();
                } catch (java.lang.IllegalMonitorStateException imse) {
                    log.error("synch("+blockIndex+"): IllegalMonitorStateException "+imse);
                }
            }
        }
        /**
        * Occupancy of blocks and Portal signals my modify traim speed
        */
        synchronized public void setSpeedRestriction(int restriction) {

            if (_speedType==restriction && !_wait) {
                return;
            }
            _speedType = restriction; 
            _wait = (_speedType == STOP_SPEED);
            if (log.isDebugEnabled()) log.debug("setSpeedRestriction: "+_speedType+
                                                " changed to "+restriction+" _wait= "+_wait);
            resetSpeed();
        }


        synchronized private void resetSpeed() {
            if (_idxCurrentCommand<0) {
                return;
            }
            ThrottleSetting ts = _throttleCommands.get(_idxCurrentCommand);
            String command = ts.getCommand().toUpperCase();
            float speed = 0.0f;
            try {
                if (command.equals("SPEED")) {
                    speed = Float.parseFloat(ts.getValue());
                }
                int idx = _idxCurrentCommand;
                while (!command.equals("SPEED") && idx>0) {
                    idx--;
                    ts = _throttleCommands.get(idx);
                    command = ts.getCommand().toUpperCase();
                    if (command.equals("SPEED")) {
                        speed = Float.parseFloat(ts.getValue());
                    }
                }
                setSpeed(speed);
                if (!_wait && !_halt) {
                    this.notify();
                }
                if (log.isDebugEnabled()) log.debug("Throttle speed= "+_throttle.getSpeedSetting()+", Resetting from Command #"+idx);
            } catch (NumberFormatException nfe) {
                  log.warn(ts.toString()+" - "+nfe);
            } catch (java.lang.IllegalMonitorStateException imse) {
                log.error("resetSpeed: IllegalMonitorStateException "+imse);
            }
        }

        private void setSpeed(float speed) {
            speed *= _speedRatio[_speedType];
            if (0.0f < speed && speed < _minSpeed) {
                speed = _minSpeed;
            }
            /*if (_speedType == STOP_SPEED) {
                _wait = true;
            }*/
            _throttle.setSpeedSetting(speed);
            if (log.isDebugEnabled()) log.debug("Speed set to "+speed+", _wait= "+_wait);
        }

        public boolean isWaiting() {
            return _wait;
        }

        /**
        * Flag from user's control
        */
        public void setHalt(boolean halt) {
            _halt = halt;
            if (_halt) { 
                _wait = true; 
            } else { 
                resetSpeed();
            }
        }

        /**
        * Flag from user to end run
        */
        public void abort() {
            _abort = true;
            _throttle.setSpeedSetting(-1.0f);
            _throttle.setSpeedSetting(0.0f);
        }

        private void setFunction(int cmdNum, boolean isSet) {
            switch (cmdNum)
            {
                case 0: _throttle.setF0(isSet); break;
                case 1: _throttle.setF1(isSet); break;
                case 2: _throttle.setF2(isSet); break;
                case 3: _throttle.setF3(isSet); break;
                case 4: _throttle.setF4(isSet); break;
                case 5: _throttle.setF5(isSet); break;
                case 6: _throttle.setF6(isSet); break;
                case 7: _throttle.setF7(isSet); break;
                case 8: _throttle.setF8(isSet); break;
                case 9: _throttle.setF9(isSet); break;
                case 10: _throttle.setF10(isSet); break;
                case 11: _throttle.setF11(isSet); break;
                case 12: _throttle.setF12(isSet); break;
                case 13: _throttle.setF13(isSet); break;
                case 14: _throttle.setF14(isSet); break;
                case 15: _throttle.setF15(isSet); break;
                case 16: _throttle.setF16(isSet); break;
                case 17: _throttle.setF17(isSet); break;
                case 18: _throttle.setF18(isSet); break;
                case 19: _throttle.setF19(isSet); break;
                case 20: _throttle.setF20(isSet); break;
                case 21: _throttle.setF21(isSet); break;
                case 22: _throttle.setF22(isSet); break;
                case 23: _throttle.setF23(isSet); break;
                case 24: _throttle.setF24(isSet); break;
                case 25: _throttle.setF25(isSet); break;
                case 26: _throttle.setF26(isSet); break;
                case 27: _throttle.setF27(isSet); break;
                case 28: _throttle.setF28(isSet); break;
            }
        }

        private void setLockFunction(int cmdNum, boolean isTrue) {
            switch (cmdNum)
            {
                case 0: _throttle.setF0Momentary(!isTrue); break;
                case 1: _throttle.setF1Momentary(!isTrue); break;
                case 2: _throttle.setF2Momentary(!isTrue); break;
                case 3: _throttle.setF3Momentary(!isTrue); break;
                case 4: _throttle.setF4Momentary(!isTrue); break;
                case 5: _throttle.setF5Momentary(!isTrue); break;
                case 6: _throttle.setF6Momentary(!isTrue); break;
                case 7: _throttle.setF7Momentary(!isTrue); break;
                case 8: _throttle.setF8Momentary(!isTrue); break;
                case 9: _throttle.setF9Momentary(!isTrue); break;
                case 10: _throttle.setF10Momentary(!isTrue); break;
                case 11: _throttle.setF11Momentary(!isTrue); break;
                case 12: _throttle.setF12Momentary(!isTrue); break;
                case 13: _throttle.setF13Momentary(!isTrue); break;
                case 14: _throttle.setF14Momentary(!isTrue); break;
                case 15: _throttle.setF15Momentary(!isTrue); break;
                case 16: _throttle.setF16Momentary(!isTrue); break;
                case 17: _throttle.setF17Momentary(!isTrue); break;
                case 18: _throttle.setF18Momentary(!isTrue); break;
                case 19: _throttle.setF19Momentary(!isTrue); break;
                case 20: _throttle.setF20Momentary(!isTrue); break;
                case 21: _throttle.setF21Momentary(!isTrue); break;
                case 22: _throttle.setF22Momentary(!isTrue); break;
                case 23: _throttle.setF23Momentary(!isTrue); break;
                case 24: _throttle.setF24Momentary(!isTrue); break;
                case 25: _throttle.setF25Momentary(!isTrue); break;
                case 26: _throttle.setF26Momentary(!isTrue); break;
                case 27: _throttle.setF27Momentary(!isTrue); break;
                case 28: _throttle.setF28Momentary(!isTrue); break;
            }
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Warrant.class.getName());
}
