package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An Warrant contains the operating permissions and directives needed for
 * a train to proceed from an Origin to a Destination
 * <P>
 * Version 1.11 - remove setting of SignalHeads
 *
 * @version $Revision: 1.30 $
 * @author	Pete Cressman  Copyright (C) 2009, 2010
 */
public class Warrant extends jmri.implementation.AbstractNamedBean 
                    implements ThrottleListener, java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");
    // permanent members.
    private ArrayList <BlockOrder> _savedOrders = new ArrayList <BlockOrder>();
    private BlockOrder _viaOrder;
    private BlockOrder _avoidOrder;
    private ArrayList <ThrottleSetting> _throttleCommands = new ArrayList <ThrottleSetting>();
    private String _trainId;
    private DccLocoAddress _dccAddress;
    private boolean _runBlind;              // don't use block detection
    private float _throttleFactor = 1.0f;

    // transient members
    private List <BlockOrder> _orders;          // temp orders used in run mode
    private List <ThrottleSetting> _commands;   // temp commands used in run mode
    private DccThrottle _throttle;              // run mode throttle
    private LearnThrottleFrame _student;        // need to callback learning throttle in learn mode
    private boolean _tempRunBlind;              // run mode flag

    private int     _idxCurrentOrder;       // Index of block at head of train (if running)
//    private int     _idxTrailingOrder;      // index of block train has just left (if running)
    private int     _runMode;
    private Engineer _engineer;         // thread that runs the train
    private boolean _allocated;         // all Blocks of _orders have been allocated
    private boolean _routeSet;          // all Blocks of _orders have paths set for route
    private OBlock  _stoppingBlock;     // Block allocated to another warrant or a rouge train
    private NamedBean _stoppingSignal;  // Signal stopping train movement

    // Throttle modes
    public static final int MODE_NONE = 0;
    public static final int MODE_LEARN = 1;
    public static final int MODE_RUN = 2;

    // control states
    public static final int HALT = 1;
    public static final int RESUME = 2;
    public static final int ABORT = 3;
    public static final int WAIT = 4;
    public static final int RUNNING = 5;    
    public static final int SPEED_RESTRICTED = 6;    

    private static jmri.implementation.SignalSpeedMap _speedMap;

    /**
     * Create an object with no route defined.
     * The list of BlockOrders is the route from an Origin to a Destination
     */
    public Warrant(String sName, String uName) {
        super(sName.toUpperCase(), uName);
        _idxCurrentOrder = 0;
//        _idxTrailingOrder = -1;
        _orders = _savedOrders;
        _runBlind = false;
    }
    public final static jmri.implementation.SignalSpeedMap getSpeedMap() {
        if (_speedMap==null) {
            _speedMap = jmri.implementation.SignalSpeedMap.getMap();
        }
        return _speedMap;
    }

    // _state not used (yet?)
    public int getState() { 
        return UNKNOWN;  
    }
    public void setState(int state) {
    }

    public void clearAll() {
        _savedOrders = new ArrayList <BlockOrder>();
        _viaOrder = null;
        _avoidOrder = null;
        _throttleCommands = new ArrayList <ThrottleSetting>();
        _trainId = null;
        _dccAddress = null;
        _orders = _savedOrders;
        _runBlind = false;
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
        if (_orders.size()==0) { return null; }
        return new BlockOrder(_orders.get(0)); 
    }

    /**
    * Return permanently saved Destination
    */
    public BlockOrder getLastOrder() {
        if (_orders.size()==0) { return null; }
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

    public BlockOrder getAvoidOrder() {
        if (_avoidOrder==null) { return null; }
        return new BlockOrder(_avoidOrder);
    }
    public void setAvoidOrder(BlockOrder order) { _avoidOrder = order; }

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
    private int getIndexOfBlock(String name) {
        for (int i=0; i<_orders.size(); i++){
            if (_orders.get(i).getBlock().getDisplayName().equals(name)) {
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
    public void setDccAddress(DccLocoAddress address) { 
        _dccAddress = address;
        String id = null;
        if (address!=null) {
            id = address.toString();
            if (_trainId!=null) {
                id = _trainId;
            }
        }
        firePropertyChange("trainId", "", id);
    }

    public boolean getRunBlind() {return _runBlind; }
    public void setRunBlind(boolean runBlind) { _runBlind = runBlind; }

    public String setThrottleFactor(String sFactor) {
        try {
            _throttleFactor = Float.parseFloat(sFactor);
        } catch (NumberFormatException nfe) {
            return rb.getString("MustBeFloat");
        }
        return null;
    }

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

    public String getRunningMessage() {
        if (_engineer==null) {
            return "ERROR";
        }
        String key;
        switch (_engineer.getRunState()) {
            case Warrant.HALT:
                key = "Halted";
                break;
            case Warrant.ABORT:
                return rb.getString("Aborted");
            case Warrant.WAIT:
                key = "Waiting";
                break;
            case Warrant.SPEED_RESTRICTED:
                key = "SpeedRestricted";
                break;
            default:
                key = "Running";
        }
        return java.text.MessageFormat.format(rb.getString(key),
                            getCurrentBlockOrder().getBlock().getDisplayName(), 
                            _engineer.getCurrentCommandIndex()+1, 
                            _engineer.getSpeedRestriction());
    }

    public int getCurrentCommandIndex() {
        if (_engineer!=null) {
            return _engineer.getCurrentCommandIndex();
        }
        return 0;
    }

    /**
    * Sets up recording and playing back throttle commands - also cleans up afterwards.
    *  MODE_LEARN and MODE_RUN sessions must end by calling again with MODE_NONE.  It is
    * important that the route be deAllocated (remove listeners).
    * <p>
    * Rule for (auto) MODE_RUN:
    * 1. At least the Origin block must be owned (allocated) by this warrant.
    * (block._warrant == this)  and path setfor Run Mode
    * Rule for (auto) LEARN_RUN:
    * 2. Entire Route must be allocated and Route Set for Learn Mode. 
    * i.e. this warrant has listeners on all block sensors in the route.
    */
    protected String setRunMode(int mode, DccLocoAddress address, 
                                 LearnThrottleFrame student, 
                                 List <ThrottleSetting> commands, boolean runBlind) 
    {
        if(log.isDebugEnabled()) log.debug("setRunMode("+mode+")  _runMode= "+_runMode);
        String msg = null;
        int oldMode = _runMode;
        if (mode == MODE_NONE) {
            if (_stoppingSignal!=null) {
                _stoppingSignal.removePropertyChangeListener(this);
            }
            if (_stoppingBlock!=null) {
                _stoppingBlock.removePropertyChangeListener(this);
            }
            if (_engineer != null) {
                _engineer.abort();
                _engineer = null;
            }
            if (_student !=null) {
                _student.dispose();
                _student = null;
            }
            deAllocate();
            if (_throttle != null) {
                try {
                    //_throttle.removePropertyChangeListener(this);
                    _throttle.release();
                    DccLocoAddress l = (DccLocoAddress) _throttle.getLocoAddress();
                    InstanceManager.throttleManagerInstance().cancelThrottleRequest(l.getNumber(), this);
                } catch (Exception e) {
                    // null pointer catch and maybe other such.
                    log.warn("Throttle release and cancel threw: "+e);
                }
            }
            _runMode = mode;
            _idxCurrentOrder = 0;
//            _idxTrailingOrder = -1;
            _orders = _savedOrders;
            _throttleFactor = 1.0f;
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
            } else {
                // set block state to show our train occupies the block
                OBlock b = getBlockAt(0);
                b.setValue(_trainId);
                b.setState(b.getState() | OBlock.RUNNING);
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
        }
        _runMode = mode;
        firePropertyChange("runMode", Integer.valueOf(oldMode), Integer.valueOf(_runMode));
        if(log.isDebugEnabled()) log.debug("Exit setRunMode()  _runMode= "+_runMode+", msg= "+msg);
        return msg;
    }

    /**
    * Pause and resume auto-running train
    */
    public boolean controlRunTrain(int idx) {
        if (_engineer == null) { return false; }
        if(log.isDebugEnabled()) log.debug("controlRunTrain= "+idx);
        int oldIndex = _engineer.getRunState();
        synchronized(_engineer) { 
            switch (idx) {
                case HALT:
                    _engineer.setHalt(true);
                    break;
                case RESUME:
                    _engineer.setHalt(false);
                    break;
                case ABORT:
                    _engineer.abort();
                    _engineer.notifyAll();
                    break;
            }
        }
        firePropertyChange("controlChange", Integer.valueOf(oldIndex), Integer.valueOf(idx));
        return true;
    }

    public void notifyThrottleFound(DccThrottle t)
    {
    	if (t == null) { return; }
        _throttle = t;
        if(log.isDebugEnabled()) {
           log.debug("notifyThrottleFound address= " +t.getLocoAddress().toString()+" _runMode= "+_runMode);
        }
        //_throttle.addPropertyChangeListener(this);
        _idxCurrentOrder = 0;
//        _idxTrailingOrder = -1;

        if (_runMode == MODE_LEARN) {
            _student.notifyThrottleFound(_throttle);
        } else {
            _engineer = new Engineer(_commands);
            new Thread(_engineer).start();
            _engineer.rampSpeedTo(getNextSpeed(), getSpeedChangeWait(1));
        }
    }

    /**
    * Allocate the current saved blocks of this warrant.
    * Installs listeners for the entire route.  Sets this warrant into
    * OBlock's _warrant field.  Returns the index of a block allocated to
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
        firePropertyChange("allocate", Boolean.valueOf(old), Boolean.valueOf(_allocated));
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
        firePropertyChange("allocate", Boolean.valueOf(old), Boolean.valueOf(false));
    }

    /**
    * Set the route paths and turnouts for the warrant.  Returns the name
    * of the first block that failed allocation to this warrant.  When running with 
    * block detection, only the first block must be allocated and have its path set.
    * @param delay - delay in seconds, between setting signals and throwing turnouts
    * @param orders - BlockOrder list of route.  If null, use permanent warrant copy.
    * @return message of block that failed allocation to this warrant or null
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
            if (log.isDebugEnabled()) log.debug("setRoute: block state= "+block.getState()+", for "+bo.toString());
            // allocated to this, We assume the train of this warrant occupies the first block 
            // exit speed is determined by getPermissibleEntranceSpeed() into next block.
            bo.setPath();
            for (int i=1; i<_orders.size(); i++) {
                bo = _orders.get(i);
                block = bo.getBlock();
                name = block.allocate(this); 
                if (name==null) {
                    if (log.isDebugEnabled()) log.debug("setRoute: block state= "+block.getState()+
                                                        ", for "+bo.toString());
                    if ((block.getState() & OBlock.OCCUPIED) != 0) {
                        msg = java.text.MessageFormat.format(rb.getString("BlockRougeOccupied"), 
                                                            block.getDisplayName());
                        routeSet = false;
                    }
                    if ((block.getState() & OBlock.DARK) != 0) {
                        msg = java.text.MessageFormat.format(rb.getString("BlockDark"), 
                                                            block.getDisplayName());
                        routeSet = false;
                    }
                    if (bo.getPermissibleEntranceSpeed().equals("Stop")) {
                        msg = java.text.MessageFormat.format(rb.getString("BlockStopAspect"), 
                                        block.getDisplayName());
                        routeSet = false;
                    } else if (routeSet) {
                        bo.setPath();
                    }
                } else {
                    msg = java.text.MessageFormat.format(rb.getString("BlockNotAllocated"), 
                                                        name, block.getDisplayName());
                    allocated = false;
                    routeSet = false;
                    break;
                }
            }
        }

        _allocated = allocated;
        boolean old = _routeSet;
        _routeSet = routeSet;
        firePropertyChange("setRoute", Boolean.valueOf(old), Boolean.valueOf(_routeSet));
        return msg;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
            if (log.isDebugEnabled()) log.debug("propertyChange "+evt.getPropertyName()+
                                                " old= "+evt.getOldValue()+" new= "+evt.getNewValue());
            return;
        }
        String property = evt.getPropertyName();
        if (log.isDebugEnabled()) log.debug("propertyChange "+property+
                                            " old= "+evt.getOldValue()+" new= "+evt.getNewValue()+
                                            " bean= "+((NamedBean)evt.getSource()).getDisplayName());
        if (_stoppingSignal != null && _stoppingSignal==evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // signal blocking warrant has changed. Should (MUST) be the next block.
                _stoppingSignal.removePropertyChangeListener(this);
                _stoppingSignal = null;
                _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder+1),
                                      getSpeedChangeWait(_idxCurrentOrder+1));
                return;
            }
        } else if (_stoppingBlock != null && _stoppingBlock==evt.getSource()) {
            if ( property.equals("deallocate") || 
                (((Number)evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0 ) {
                //  blocking warrant has changed deallocated or blocks after
                _stoppingBlock.removePropertyChangeListener(this);
                _stoppingBlock = null;
                _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder+1),
                                      getSpeedChangeWait(_idxCurrentOrder+1));
                return;
            }
        }

        if (log.isDebugEnabled()) log.debug("propertyChange skipped");
    }

    /**
    * Block in the route is going active.
    * check if this is the next block of the train moving under the warrant
    * Learn mode assumes route is set and clear
    */
    protected void goingActive(OBlock block) {
        if (_runMode==MODE_NONE) { 
            return;
        }
        int oldIndex = _idxCurrentOrder;
        int activeIdx = getIndexOfBlock(block);
        boolean rougeEntry = false;
        if (log.isDebugEnabled()) log.debug("Block "+block.getDisplayName()+" goingActive. activeIdx= "+
                                            activeIdx+", _idxCurrentOrder= "+_idxCurrentOrder+
                                            " _orders.size()= "+_orders.size());
        if (activeIdx == _idxCurrentOrder+1) {
            if (_engineer!=null && _engineer.getRunState()==WAIT) {
                // Next block just occupied, but train is stopped - must be a rouge entry.
                rougeEntry = true;
                log.warn("Rouge entering next Block "+block.getDisplayName());
            } else {
                if (log.isDebugEnabled()) log.debug("Train entering Block "+block.getDisplayName());
                // we assume it is our train entering the block - cannot guarantee it, but what else?
                _idxCurrentOrder = activeIdx;
                // set block state to show our train occupies the block
                block.setValue(_trainId);
                block.setState(block.getState() | OBlock.RUNNING);
            }
        } else if (activeIdx > _idxCurrentOrder+1) {
            // rouge train invaded route.
            rougeEntry = true;
        } else if (_idxCurrentOrder > 0) {
            log.error("activeIdx ("+activeIdx+") < _idxCurrentOrder ("+_idxCurrentOrder+")!"); 
        }
        if (_runMode==MODE_LEARN || _tempRunBlind) {
            // recording must done with signals and occupancy clear.
            firePropertyChange("blockChange", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
            return;
        }
        String currentSpeed = getCurrentSpeedAt(_idxCurrentOrder);
        if (currentSpeed==null) {
            // originating block may not have an entrance portal. (i.e. may be a stub siding)
            // otherwise. error
            if (_idxCurrentOrder!=0) {
                log.error("Block "+block.getDisplayName()+" does not have an entrance portal!");
            }
        } else {
            _engineer.rampSpeedTo(currentSpeed, getSpeedChangeWait(_idxCurrentOrder));
        }

        _engineer.synchNotify(block); // notify engineer of control point

        if (_idxCurrentOrder < _orders.size()-1) {
            // No 'next block' for last BlockOrder
            String nextSpeed = getNextSpeed();
            if (!nextSpeed.equals(currentSpeed)) {
                // ramp speed from current to speed restriction.
                // back off call (wait) so that endspeed occurs at exit of block.
                _engineer.rampSpeedTo(nextSpeed, getSpeedChangeWait(_idxCurrentOrder+1));
            }
        }
        if (_idxCurrentOrder == _orders.size()-1) {
            // must be in destination block, let script finish according to recorded speeds
        } else {
            // attempt to allocatable remaining blocks in the route
            for (int i=_idxCurrentOrder+2; i<_orders.size(); i++) {
                getBlockAt(i).allocate(this);
            }
        }
        // fire notification last so engineer's state can be documented in whatever GUI is listening.
        if (rougeEntry) {
            boolean old = _routeSet;
            _routeSet = false;
            firePropertyChange("setRoute", Boolean.valueOf(old), Boolean.valueOf(_routeSet));
        } else {
            firePropertyChange("blockChange", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
        }
    }

    /**
    * Block in the route is going Inactive 
    */
    protected void goingInactive(OBlock block) {
        if (_runMode==MODE_NONE)  { return; }

        int idx = getIndexOfBlock(block);  // if idx >= 0, it is in this warrant
        if (log.isDebugEnabled()) log.debug("Block "+block.getDisplayName()+" goingInactive. idx= "+
                                            idx+", _idxCurrentOrder= "+_idxCurrentOrder);
        if (idx < _idxCurrentOrder) {
            // block is behind train.  Assume we are leaving.
//            _idxTrailingOrder = idx;
            if (this.equals(block.getWarrant())) {
                block.setValue(null);
                block.deAllocate(this);
            }
        } else if (_runMode==MODE_RUN ) {
            // if it is the next block ahead of the train, we can move.
            // Presumeably we have stopped at the exit of the current block.
            if (idx==_idxCurrentOrder+1) {
                if (allocateNextBlock(block)) {
                    _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder+1),
                                          getSpeedChangeWait(_idxCurrentOrder+1));
                }
            } else if (idx > _idxCurrentOrder){
                block.allocate(this);
            } else { // idx==_idxCurrentOrder - something is wrong
                log.error("Current Block "+block.getDisplayName()+" goingInactive!");
            }
        }
    }

    private long getSpeedChangeWait(int index) {
        BlockOrder bo = getBlockOrderAt(index);
        return bo.getEntranceSpeedChangeWait();
    }

    private String getCurrentSpeedAt(int index) {
        BlockOrder bo = getBlockOrderAt(index);
        bo.setPath();
        return bo.getPermissibleEntranceSpeed();
    }

    private boolean allocateNextBlock(OBlock block) {
        String blockName = block.allocate(this);
        if ( blockName != null) {
            log.warn("Block \""+block.getDisplayName()+"\" in warrant \""+getDisplayName()+
                     "\" is allocated to warrant \"" +blockName+"\"");
            _stoppingBlock = block;
            _stoppingBlock.addPropertyChangeListener(this);
            return false;
        }
        return true;
    }

    // if movement is permitted, set path
    private String getNextSpeed() {
        String nextSpeed = "Normal";
        BlockOrder nextBO = getBlockOrderAt(_idxCurrentOrder+1);
        if (nextBO!=null && allocateNextBlock(nextBO.getBlock())) {
            nextBO.setPath();
            nextSpeed = nextBO.getPermissibleEntranceSpeed();
            if (nextSpeed.equals("Stop")) {
                _stoppingSignal = nextBO.getSignal();
                _stoppingSignal.addPropertyChangeListener(this);
            } else if ((nextBO.getBlock().getState() & OBlock.OCCUPIED) != 0) {
                // Rule 292 - "visible" obstacle ahead.
                nextSpeed = "Stop";
            }
            // If next block is dark, check blocks beyond for occupancy
            int idx = _idxCurrentOrder+1;
            while ((nextBO.getBlock().getState() & OBlock.DARK) != 0 && idx < _orders.size()) {
                nextBO = getBlockOrderAt(idx);
                if ((nextBO.getBlock().getState() & OBlock.OCCUPIED) != 0) {
                    _stoppingBlock = nextBO.getBlock();
                    _stoppingBlock.addPropertyChangeListener(this);
                    nextSpeed = "Stop";
                    break;
                }
                idx++;
            }
        } else {
            nextSpeed = "Stop";
        }
        return nextSpeed;
    }

    /************************** Thread running the train *****************/

    class Engineer extends Thread implements Runnable {

        private int     _idxCurrentCommand;     // current throttle command
        private float   _speed;
        private String  _speedType = "Normal";
        private float   _minSpeed = 1.0f/127;
        private boolean _abort = false;
        private boolean _halt = false;  // halt/resume from user's control
        private boolean _wait = false;  // waits for signals/occupancy/allocation to clear
        //private boolean _waitForSync = false;  // waits for train to catch up to commands
        private boolean _speedOverride = false; // speed changing due to signal or occupancy
        private int     _syncIdx;
        private List <ThrottleSetting> _throttleCommands;

        final ReentrantLock _lock = new ReentrantLock();


        Engineer(List <ThrottleSetting> commands) {
            _idxCurrentCommand = -1;
            _throttleCommands = commands;
            _syncIdx = 0;
            setSpeedStepMode(_throttle.getSpeedStepMode());
        }

        public void run() {
            if (log.isDebugEnabled()) log.debug("Engineer started");
            goingActive(_orders.get(0).getBlock());

            while (_idxCurrentCommand+1 < _throttleCommands.size() && !_abort) {
                long et = System.currentTimeMillis();
                ThrottleSetting ts = _throttleCommands.get(_idxCurrentCommand+1);
                long time = ts.getTime();
                String command = ts.getCommand().toUpperCase();
                // actual playback total elapsed time is "ts.getTime()" before record time.
                // current block at playback may also be before current block at record
                synchronized(this) {
                    _syncIdx = getIndexOfBlock(ts.getBlockName());
                    try {
                        if (time > 0) {
                            wait(time);
                        }
                        if (_abort) { break; }
                        // Having waited, time=ts.getTime(), blocks should agree.  if not, wait.
                        // blind runs cannot detect entrance
                        if (!_tempRunBlind && _syncIdx > _idxCurrentOrder) {
                            // commands are ahead of current train position 
                            if (log.isDebugEnabled()) log.debug("Command Block "+ts.getBlockName()+
                                                                " wait for train to enter.");
                            wait();  //
                        }
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException "+ie);
                    } catch (java.lang.IllegalArgumentException iae) {
                        log.error("IllegalArgumentException "+iae);
                    }
                }
                if (_abort) { break; }
                _idxCurrentCommand++;
                synchronized(this) {
                    try {
                        if (_wait || _halt) {
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
                        _lock.lock();
                        try {
                            setSpeed(modifySpeed(speed, _speedType));
                        } finally {
                          _lock.unlock();
                        }
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
                    firePropertyChange("Command", Integer.valueOf(_idxCurrentCommand-1), Integer.valueOf(_idxCurrentCommand));
                    et = System.currentTimeMillis()-et;
                    if (log.isDebugEnabled()) log.debug("Cmd #"+_idxCurrentCommand+": "+
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

        /**
        * If waiting to sync entrance to a block boundary with record time,
        * this call will free the wait.
        * @param  block train has just entered.
        */
        synchronized public void synchNotify(OBlock block) {
            if (!_halt && !_wait) {
                if (_syncIdx <= _idxCurrentOrder) { 
                    this.notify();
                }
            }
        }

        /**
        * Occupancy of blocks and aspects of Portal signals may modify normal traim speed
        * Ramp speed change.
        */
        synchronized public void rampSpeedTo(String endSpeedType, long waitTime) {
            if (_speedType.equals(endSpeedType)) {
                return;
            }
            ThrottleRamp ramp = new ThrottleRamp(endSpeedType, waitTime);
            new Thread(ramp).start();
        }

        /**
        * Get the last normal speed setting.  Regress through commends, if necessary.  
        */
        private float getLastSpeedCommand(int currentIndex) {
            float speed = 0.0f;
            if (currentIndex<0) {
                return speed;
            }
            ThrottleSetting ts = _throttleCommands.get(currentIndex);
            String command = ts.getCommand().toUpperCase();
            try {
                if (command.equals("SPEED")) {
                    speed = Float.parseFloat(ts.getValue());
                }
                int idx = currentIndex;
                while (!command.equals("SPEED") && idx>0) {
                    idx--;
                    ts = _throttleCommands.get(idx);
                    command = ts.getCommand().toUpperCase();
                    if (command.equals("SPEED")) {
                        speed = Float.parseFloat(ts.getValue());
                    }
                }
                if (log.isDebugEnabled()) log.debug("getLastSpeedCommand: speed= "+speed+", from Command #"+idx);
            } catch (NumberFormatException nfe) {
                  log.warn(ts.toString()+" - "+nfe);
            }
            return speed;
        }

        synchronized private void resetSpeed() {
            _lock.lock();
            try {
                setSpeed(modifySpeed(getLastSpeedCommand(_idxCurrentCommand), _speedType));
                if (!_wait && !_halt) {
                    this.notify();
                }
            } finally {
                _lock.unlock();
            }
            if (log.isDebugEnabled()) log.debug("resetSpeed: throttle speed= "+
                                                _throttle.getSpeedSetting()+" _wait= "+_wait);
//            this.notify();
        }

        private float modifySpeed(float s, String sType) {
            jmri.implementation.SignalSpeedMap map = Warrant.getSpeedMap();
            float speed = map.getSpeed(sType)/100;

            if (map.isRatioOfNormalSpeed()) {
                speed *= s;
            }
            speed *= _throttleFactor;
            if (0.0f < speed && speed < _minSpeed) {
                speed = 0.0f;
            }
            return speed;
        }

        private void setSpeed(float speed) {
            _speed = speed;
            _throttle.setSpeedSetting(_speed);
            if (log.isDebugEnabled()) log.debug("_speedType="+_speedType+", Speed set to "+
                                                _speed+" _wait= "+_wait);
        }

        public int getRunState() {
            if (_wait) {
                return WAIT;
            } else if (_halt) {
                return HALT;
            } else if (_abort) {
                return ABORT;
            } else if (!_speedType.equals("Normal")) {
                return SPEED_RESTRICTED;
            }
            return RUNNING;
        }

        public String getSpeedRestriction() {
            if (_speedOverride) {
                return "Change to "+_speedType;
            }
            else if (_speed == 0.0f) {
                return "At Stop";
            } else {
                return _speedType;
            }
        }

        /**
        * Flag from user's control
        */
        public void setHalt(boolean halt) {
            _halt = halt;
            if (!_halt) { 
                resetSpeed();
            } else {
                _throttle.setSpeedSetting(0.0f);
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

        class ThrottleRamp implements Runnable {
            String endSpeedType;

            ThrottleRamp(String type, long startWait) {
                endSpeedType = type;
                synchronized(this) {
                    if (startWait>0.0) {
                        try {
                            wait(startWait);
                        } catch (InterruptedException ie) {
                            log.error("InterruptedException "+ie);
                        }
                    }
                }
                _speedOverride = true;
            }

            public void run() {
                _lock.lock();
                try {
                    float endSpeed = getLastSpeedCommand(_idxCurrentCommand);
                    endSpeed = modifySpeed(endSpeed, endSpeedType);
                    String old = _speedType;
                    _speedType = endSpeedType;   // transistion
                    if (log.isDebugEnabled()) log.debug("rampSpeed from "+old+" to "+endSpeedType);
                    float speed = _throttle.getSpeedSetting();
                    firePropertyChange("SpeedRestriction", old, 
                                       (endSpeed > speed ? "increasing" : "decreasing"));

                    if (!_speedType.equals("Stop")) {
                        synchronized(_engineer) {
                            _engineer.notify();
                            _wait = false;
                        }
                    } else {
                        _wait = true;
                    }
                    float incr = Math.max(_throttle.getSpeedIncrement(), _minSpeed);
                    switch (_throttle.getSpeedStepMode()) {
                        case DccThrottle.SpeedStepMode14:
                            break;
                        case DccThrottle.SpeedStepMode27:
                        case DccThrottle.SpeedStepMode28:
                            incr *= 2;
                            break;
                        default:    // SpeedStepMode128
                            incr *= 4;
                            break;
                    }
                    jmri.implementation.SignalSpeedMap map = Warrant.getSpeedMap();
                    incr *= map.getNumSteps();
                    int delay = map.getStepDelay();
                    
                    if (endSpeed > speed) {
                        synchronized(this) {
                            while (speed < endSpeed) {
                                speed += incr;
                                if (speed > endSpeed) { // don't overshoot
                                    speed = endSpeed;
                                }
                                setSpeed(speed);
                                try {
                                    wait(delay);
                                } catch (InterruptedException ie) {
                                    log.error("InterruptedException "+ie);
                                }
                            }
                        }
                    } else {
                        synchronized(this) {
                            while (speed > endSpeed) {
                                speed -= incr;
                                if (speed < endSpeed) { // don't undershoot
                                    speed = endSpeed;
                                }
                                setSpeed(speed);
                                try {
                                    wait(delay);
                                } catch (InterruptedException ie) {
                                    log.error("InterruptedException "+ie);
                                }
                            }
                        }
                    }
                    _speedOverride = false;
                    firePropertyChange("SpeedRestriction", old, _speedType);
                } finally {
                  _lock.unlock();
                }
            }
        }
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Warrant.class.getName());
}
