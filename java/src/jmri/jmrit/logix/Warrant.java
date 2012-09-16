package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * An Warrant contains the operating permissions and directives needed for
 * a train to proceed from an Origin to a Destination
 * <P>
 * Version 1.11 - remove setting of SignalHeads
 *
 * @version $Revision$
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
    private String _trainName;      // User train name for icon
    private String _trainId;        // Roster Id
    private DccLocoAddress _dccAddress;
    private boolean _runBlind;              // don't use block detection
    boolean _debug;

    // transient members
    private List <BlockOrder> _orders;          // temp orders used in run mode
    private LearnThrottleFrame _student;        // need to callback learning throttle in learn mode
    protected boolean _tempRunBlind;            // run mode flag
    private boolean _delayStart;				// allows start block unoccupied and wait for train
    protected float _throttleFactor = 1.0f;
    protected List <ThrottleSetting> _commands;   // temp commands used in run mode
    protected int   _idxCurrentOrder;       // Index of block at head of train (if running)

    private int     _runMode;
    private Engineer _engineer;         // thread that runs the train
    private boolean _allocated;         // all Blocks of _orders have been allocated
    private boolean _routeSet;          // all Blocks of _orders have paths set for route
    private OBlock  _stoppingBlock;     // Block allocated to another warrant or a rouge train
    private NamedBean _stoppingSignal;  // Signal stopping train movement
    private DccThrottle _throttle;

    // Throttle modes
    public static final int MODE_NONE 	= 0;
    public static final int MODE_LEARN 	= 1;	// Record command list
    public static final int MODE_RUN 	= 2;	// Autorun (playback) command list
    public static final int MODE_MANUAL = 3;	// block detection/reservation for manually run train

    // control states
    public static final int HALT = 1;
    public static final int RESUME = 2;
    public static final int ABORT = 3;
    public static final int WAIT_FOR_TRAIN = 4;
    public static final int RUNNING = 5;    
    public static final int SPEED_RESTRICTED = 6;    
    public static final int WAIT_FOR_CLEAR = 7;

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
        _debug = log.isDebugEnabled();
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
        _trainName = null;
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

    protected String getRoutePathInBlock(OBlock block) {
        for (int i=0; i<_savedOrders.size(); i++){
            if (_savedOrders.get(i).getBlock().equals(block)) {
                return _savedOrders.get(i).getPathName();
            }
        }
        return null;
    }

    /**
    * Call is only valid when in MODE_LEARN and MODE_RUN
    */
    public BlockOrder getCurrentBlockOrder() {
        return getBlockOrderAt(_idxCurrentOrder);
    }

    public int getCurrentOrderIndex() {
        return _idxCurrentOrder;
    }

    /**
    * Call is only valid when in MODE_LEARN and MODE_RUN
    */
    protected int getIndexOfBlock(OBlock block) {
        for (int i=_idxCurrentOrder; i<_orders.size(); i++){
            if (_orders.get(i).getBlock().equals(block)) {
                return i;
            }
        }
        return -1;
    }
    /**
    * Call is only valid when in MODE_LEARN and MODE_RUN
    */
    protected int getIndexOfBlock(String name) {
        for (int i=_idxCurrentOrder; i<_orders.size(); i++){
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

    public String getTrainName() { return _trainName; }
    public void setTrainName(String name) {
        _trainName = name;
    }

    public String getTrainId() { return _trainId; }
    /**
    * @param id may be either Roster entry or DCC address
    * @return id is valid
    */
    public boolean setTrainId(String id) {
        _trainId = id;
        if (id==null || id.trim().length()==0) {
            return false;
        }
        RosterEntry train = Roster.instance().entryFromTitle(id);
        if (train != null) {
            _dccAddress = train.getDccLocoAddress();
        } else {
            int index = id.indexOf('(');
            String numId;
            if (index >= 0) {
                numId = id.substring(0, index);
            } else {
                numId = id;
            }
            List<RosterEntry> l = Roster.instance().matchingList(null, null, numId, null, null, null, null );
            if (l.size() > 0) {
                try {
                    _dccAddress = l.get(0).getDccLocoAddress();
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                boolean isLong = true;
                if ((index+1)<id.length() &&
                    (id.charAt(index+1)=='S' || id.charAt(index+1)=='s')) {
                    isLong = false;
                }
                try {
                    int num = Integer.parseInt(numId);
                    _dccAddress = new DccLocoAddress(num, isLong);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
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

    /**
    * Engineer reports its status
    */
    protected void fireRunStatus(String property, Object old, Object status) {
        firePropertyChange(property, old, status);
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
    	String msg = getBlockAt(0).allocate(this);
        if (msg!=null) {
            return msg;
        }
        allocateRoute();
        return setRunMode(run?MODE_RUN:MODE_MANUAL, _dccAddress, null, _throttleCommands, _runBlind);
    }

    public String getRunningMessage() {
    	if (_delayStart) {
    		return java.text.MessageFormat.format(WarrantTableAction.rb.getString("waitForDelayStart"),
    				_trainName, getDisplayName(), getBlockOrderAt(0).getBlock().getDisplayName());
    	}
        switch (_runMode) {
            case Warrant.MODE_NONE:
                if (getOrders().size()==0) {
                    return WarrantTableAction.rb.getString("BlankWarrant");
                }
                if (getDccAddress()==null){
                    return WarrantTableAction.rb.getString("NoLoco");
                }
                if (getThrottleCommands().size() == 0) {
                    return java.text.MessageFormat.format(
                        WarrantTableAction.rb.getString("NoCommands"), getDisplayName());
                }
                return WarrantTableAction.rb.getString("Idle");
            case Warrant.MODE_LEARN:
                return java.text.MessageFormat.format(WarrantTableAction.rb.getString("Learning"),
                                           getCurrentBlockOrder().getBlock().getDisplayName());
            case Warrant.MODE_RUN: 
                if (_engineer==null) {
                    return WarrantTableAction.rb.getString("engineerGone");
                }
                String key;
                switch (_engineer.getRunState()) {
                    case Warrant.HALT:
                        key = "Halted";
                        break;
                    case Warrant.ABORT:
                        return rb.getString("Aborted");
                    case Warrant.WAIT_FOR_CLEAR:
                        key = "WaitForClear";
                        break;
                    case Warrant.WAIT_FOR_TRAIN:
                        return java.text.MessageFormat.format(rb.getString("WaitForTrain"),
                                            _engineer.getCurrentCommandIndex()+1, 
                                            getBlockOrderAt(_idxCurrentOrder+1).getBlock().getDisplayName()); 
                    case Warrant.SPEED_RESTRICTED:
                        key = "SpeedRestricted";
                        break;
                    default:
                        key = "WhereRunning";
                }
                return java.text.MessageFormat.format(rb.getString(key),
                                    getCurrentBlockOrder().getBlock().getDisplayName(), 
                                    _engineer.getCurrentCommandIndex()+1, 
                                    _engineer.getSpeedRestriction());
            case Warrant.MODE_MANUAL: 
                return java.text.MessageFormat.format(rb.getString("ManualRunning"),
                                    getCurrentBlockOrder().getBlock().getDisplayName());
        }
        return "ERROR mode= "+_runMode;
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
    * (block._warrant == this)  and path set for Run Mode
    * Rule for (auto) LEARN_RUN:
    * 2. Entire Route must be allocated and Route Set for Learn Mode. 
    * i.e. this warrant has listeners on all block sensors in the route.
    * Rule for MODE_MANUAL
    * The Origin block must be allocated to this warrant and path set for the route
    */
    protected String setRunMode(int mode, DccLocoAddress address, 
                                 LearnThrottleFrame student, 
                                 List <ThrottleSetting> commands, boolean runBlind) 
    {
        if(_debug) log.debug("setRunMode("+mode+")  _runMode= "+_runMode);
        String msg = null;
        int oldMode = _runMode;
        if (mode == MODE_NONE) {
            _delayStart = false;
        	if (_throttle != null) {
        		_throttle.release(this);
        		_throttle = null;
        	}
            if (_stoppingSignal!=null) {
                log.error("signal "+_stoppingSignal.getSystemName());
                _stoppingSignal.removePropertyChangeListener(this);
                _stoppingSignal = null;
            }
            if (_stoppingBlock!=null) {
                _stoppingBlock.removePropertyChangeListener(this);
                _stoppingBlock = null;
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
            _runMode = mode;
            _idxCurrentOrder = 0;
            _orders = _savedOrders;
        } else if (_runMode!=MODE_NONE) {
        	String modeDesc = null;
        	switch (_runMode) {
        		case MODE_LEARN:
        			modeDesc = rb.getString("Recording");
        			break;
        		case MODE_RUN:
        			modeDesc = rb.getString("AutoRun");
        			break;
        		case MODE_MANUAL:
        			modeDesc = rb.getString("ManualRun");
        			break;
        	}
            msg = java.text.MessageFormat.format(rb.getString("WarrantInUse"), modeDesc);
            log.error(msg);
            return msg;
        } else {
            _delayStart = false;
            if (!_routeSet && runBlind) {
                msg = java.text.MessageFormat.format(rb.getString("BlindRouteNotSet"),getDisplayName());
                log.error(msg);
                return msg;
            }
            if (mode == MODE_LEARN) {
                // start is OK if block 0 is occupied (or dark - in which case user is responsible)
                if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED|OBlock.DARK))==0) {
                    if(_debug) log.debug("Block "+getBlockAt(0).getDisplayName()+", state= "+getBlockStateAt(0));
                    msg = java.text.MessageFormat.format(rb.getString("badStart"),getDisplayName());
                    log.error(msg);
                    return msg;
                } else if (student == null) {
                    msg = java.text.MessageFormat.format(rb.getString("noLearnThrottle"), getDisplayName());
                    log.error(msg);
                    return msg;
                }
                _student = student; 
             } else if (mode == MODE_RUN) {
                 if (commands == null || commands.size()== 0) {
                     msg = java.text.MessageFormat.format(rb.getString("NoCommands"), getDisplayName());
                     log.error(msg);
                     return msg;
                 }
                 _commands = commands;
                 // start is OK if block 0 is occupied (or dark - in which case user is responsible)
                 if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED|OBlock.DARK))==0) {
                     if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(
                    		 java.text.MessageFormat.format(rb.getString("warnStart"),_trainName,getBlockAt(0).getDisplayName()),
                             rb.getString("WarningTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))) {
                         log.error(msg);
                         return msg; 
                     }
                     // continuing with no occupation of starting block
                     _stoppingBlock = getBlockAt(0);
                     _stoppingBlock.addPropertyChangeListener(this);
                     _delayStart = true;
                 }
             }
            _runMode = mode;	// set mode before callback to notifyThrottleFound is called
            if (mode!=MODE_MANUAL) {
                 _tempRunBlind = runBlind;
                 if (!_delayStart) {
                     msg = acquireThrottle(address);
                     if (msg!=null){
                    	 return msg;
                     }                	 
                 }
            }
            // set block state to show our train occupies the block
            BlockOrder bo = getBlockOrderAt(0);
            OBlock b = bo.getBlock();
            bo.setPath(this);
            b.setValue(_trainId);
            b.setValue(_trainName);
            b.setState(b.getState() | OBlock.RUNNING);
        }
        _runMode = mode;
        firePropertyChange("runMode", Integer.valueOf(oldMode), Integer.valueOf(_runMode));
        if(_debug) log.debug("Exit setRunMode()  _runMode= "+_runMode+", msg= "+msg);
        return msg;
    }
    
    private String acquireThrottle(DccLocoAddress address) {
    	String msg = null;
        if (address == null)  {
            msg = java.text.MessageFormat.format(rb.getString("NoAddress"),getDisplayName());
            log.error(msg);
            return msg;
        }
        if (InstanceManager.throttleManagerInstance()==null) {
            msg = rb.getString("noThrottle");
            log.error(msg);
            return msg;
        }
        if (!InstanceManager.throttleManagerInstance().
            requestThrottle(address.getNumber(), address.isLongAddress(),this)) {
            msg = java.text.MessageFormat.format(rb.getString("trainInUse"), address.getNumber());
            log.error(msg);
            return msg;
        }
        _delayStart = false;	// script should start - no more delay
    	return msg;
    }

    /**
    * Pause and resume auto-running train or abort any allocation state
    * _engineer.abort() calls setRunMode(MODE_NONE,...) which calls deallocate all.
    */
    public boolean controlRunTrain(int idx) {
        if(_debug) log.debug("controlRunTrain= "+idx+" runMode= "+_runMode);
        boolean ret = true;
        int oldIndex = MODE_MANUAL;
        if (_engineer == null) { 
            switch (idx) {
                case HALT:
                case RESUME:
                    ret = false;
                    break;
                case ABORT:
                    String msg = setRunMode(Warrant.MODE_NONE, null, null, null, false);
                    if (msg!=null) {
                    	deAllocate();
                    }
                    break;
            }
        } else {
            synchronized(_engineer) { 
                oldIndex = _engineer.getRunState();
                switch (idx) {
                    case HALT:
                        _engineer.setHalt(true);
                        break;
                    case RESUME:
                        _engineer.setHalt(false);
                        break;
                    case ABORT:
                        _engineer.abort();
                        break;
                }
            }
        }
        firePropertyChange("controlChange", Integer.valueOf(oldIndex), Integer.valueOf(idx));
        return ret;
    }

    public void notifyThrottleFound(DccThrottle throttle)
    {
    	if (throttle == null) {
            log.warn("notifyThrottleFound: null throttle(?)!");
            return; 
        }
        _throttle = throttle;

        if(_debug) {
           log.debug("notifyThrottleFound address= " +throttle.getLocoAddress().toString()+" _runMode= "+_runMode);
        }
        _idxCurrentOrder = 0;

        if (_runMode == MODE_LEARN) {
            _student.notifyThrottleFound(throttle);
        } else {
            getSpeedMap();      // initialize speedMap for getPermissibleEntranceSpeed() calls
            _engineer = new Engineer(this, throttle);
            goingActive(_orders.get(0).getBlock());
            new Thread(_engineer).start();
            _engineer.rampSpeedTo(getNextSpeed(), getSpeedChangeWait(1));
        }
    }

    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        log.error("notifyFailedThrottleRequest address= " +address.toString()+" _runMode= "+_runMode+
        		" due to "+reason);
    }

    /**
    * Allocate the current saved blocks of this warrant.
    * Installs listeners for the entire route.  Sets this warrant into
    * @return error messge, if any
    */
    public String allocateRoute() {
        if (_allocated) {
            return null;
        }
        String msg = null;
        // Must be able to allocate starting block - otherwise exit
        OBlock block = _orders.get(0).getBlock();
        msg = block.allocate(this);
        if (msg != null) {
            msg = java.text.MessageFormat.format(rb.getString("UnableToAllocate"), getDisplayName())+msg;
            return msg;
        }
        // Check route is in use
        for (int i=1; i<_orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            block = bo.getBlock();
            if ((block.getState() & OBlock.OUT_OF_SERVICE) !=0) {
                _orders.get(0).getBlock().deAllocate(this);
                return java.text.MessageFormat.format(rb.getString("UnableToAllocate"), getDisplayName()) +
                    java.text.MessageFormat.format(rb.getString("BlockOutOfService"),block.getDisplayName()); 
            }
        }
        // first block allocated and route is available - set name and state
        block = _orders.get(0).getBlock();
        block.setValue(_trainName);
        int state = block.getState();
        if ((state & OBlock.OCCUPIED) > 0) {
            block.setState(state | OBlock.RUNNING);
        }

        // continue and only allocate unoccupied blocks
        for (int i=1; i<_orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            block = bo.getBlock();
            state = block.getState();
            if ((state & OBlock.OCCUPIED) > 0) {
                if (msg==null) {
                    msg = java.text.MessageFormat.format(rb.getString("ConditionalAllocation"), getDisplayName())+
                          java.text.MessageFormat.format(rb.getString("BlockRougeOccupied"), block.getDisplayName());
                }
            } else {
                String str = block.allocate(this);
                if (str != null && msg==null) {
                    msg = java.text.MessageFormat.format(rb.getString("ConditionalAllocation"), getDisplayName())+str;
                }
            }
        }
        boolean old = _allocated; 
        _allocated = true;
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
        if (orders==null) {
            _orders = _savedOrders;
        } else {
            _orders = orders;
        }
        if (_debug) log.debug("setRoute: _orders.size()= "+_orders.size());
        if (_orders.size()==0) {
            return java.text.MessageFormat.format(rb.getString("NoRouteSet"),"here","there");
        }
        String msg = allocateRoute();
        if (msg != null) {
            return msg;
        }
        BlockOrder bo = _orders.get(0);
        // allocated to this, We assume the train of this warrant occupies the first block 
        // exit speed is determined by getPermissibleEntranceSpeed() into next block.
        bo.setPath(this);
        for (int i=1; i<_orders.size(); i++) {
            bo = _orders.get(i);
            OBlock block = bo.getBlock();
//            if (_debug) log.debug("setRoute: block state= "+block.getState()+", for "+bo.toString());
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
                bo.setPath(this);
            }
        }
        if (routeSet) {
            boolean old = _routeSet;
            _routeSet = routeSet;
            firePropertyChange("setRoute", Boolean.valueOf(old), Boolean.valueOf(_routeSet));
        }
        return msg;
    }   // setRoute

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
            if (_debug) log.debug("propertyChange \""+evt.getPropertyName()+
                                                "\" old= "+evt.getOldValue()+" new= "+evt.getNewValue());
            return;
        }
        String property = evt.getPropertyName();
        if (_debug) log.debug("propertyChange "+property+
                                            " old= "+evt.getOldValue()+" new= "+evt.getNewValue()+
                                            " bean= "+((NamedBean)evt.getSource()).getDisplayName());
        if (_stoppingSignal != null && _stoppingSignal==evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // signal blocking warrant has changed. Should (MUST) be the next block.
                _stoppingSignal.removePropertyChangeListener(this);
                _stoppingSignal = null;
                if (_engineer!=null) {
                    _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder+1),
                                          getSpeedChangeWait(_idxCurrentOrder+1));
                }
                return;
            }
        } else if (_stoppingBlock != null && _stoppingBlock==evt.getSource()) {
        	if (_delayStart) {	// wait for arrival of train to begin the run 
        		if ((property.equals("state") && 
                        (((Number)evt.getNewValue()).intValue() & OBlock.OCCUPIED) != 0) ){
                    String msg = acquireThrottle(_dccAddress);
                    if (msg!=null){
                    	log.error(msg);
                    }
                    return;
                }
        	} else {	// normal wait for a train underway but blocked
                if ( property.equals("deallocate") || (property.equals("state") && 
                        (((Number)evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) ) {
                        //  blocking warrant has changed deallocated or blocks after
                    _stoppingBlock.removePropertyChangeListener(this);
                    _stoppingBlock = null;
                    if (_engineer!=null) {
                        _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder+1),
                                              getSpeedChangeWait(_idxCurrentOrder+1));
                    }
                        return;
                }
        	}
        }
        if (_debug) log.debug("propertyChange skipped");
    }

    /**
    * Block in the route is going active.
    * check if this is the next block of the train moving under the warrant
    * Learn mode assumes route is set and clear
    */
    protected void goingActive(OBlock block) {
        if (_runMode==MODE_NONE) {
            if (_debug) log.debug("Block "+block.getDisplayName()+" goingActive. MODE_NONE");
            return;
        }
        int oldIndex = _idxCurrentOrder;
        int activeIdx = getIndexOfBlock(block);
        boolean rougeEntry = false;
        if (_debug) log.debug("Block "+block.getDisplayName()+" goingActive. activeIdx= "+
                                            activeIdx+", _idxCurrentOrder= "+_idxCurrentOrder+
                                            " _orders.size()= "+_orders.size());
        // skip over dark blocks
        while ((getBlockAt(_idxCurrentOrder).getState() & OBlock.DARK) > 0) {
            firePropertyChange("blockSkip", Integer.valueOf(_idxCurrentOrder), Integer.valueOf(_idxCurrentOrder+1));
            _idxCurrentOrder++;
        }
        if (activeIdx == _idxCurrentOrder+1) {
            if (_engineer!=null && _engineer.getRunState()==WAIT_FOR_CLEAR) {
                // Next block just occupied, but train is stopped - must be a rouge entry.
                rougeEntry = true;
                log.warn("Rouge entering next Block "+block.getDisplayName());
            } else {
                if (_debug) log.debug("Train entering Block "+block.getDisplayName());
                // we assume it is our train entering the block - cannot guarantee it, but what else?
                _idxCurrentOrder = activeIdx;
                // set block state to show our train occupies the block
                block.setValue(_trainName);
                block.setState(block.getState() | OBlock.RUNNING);
            }
        } else if (activeIdx > _idxCurrentOrder+1) {
            // rouge train invaded route.
            rougeEntry = true;
        } else if (_idxCurrentOrder > 0) {
            log.error("activeIdx ("+activeIdx+") < _idxCurrentOrder ("+_idxCurrentOrder+")!"); 
        }

        String currentSpeed = getCurrentSpeedAt(_idxCurrentOrder);
        if (_engineer!=null) {
            _engineer.synchNotify(block); // notify engineer of control point
            _engineer.rampSpeedTo(currentSpeed, getSpeedChangeWait(_idxCurrentOrder));
        }

        if (rougeEntry) {
            log.warn("Rouge train ahead at block \""+block.getDisplayName()+"\"!");
        }

        if (_idxCurrentOrder == _orders.size()-1) {
            // must be in destination block, 
            // If Auto running, let script finish according to recorded times.
        	// End of script will deallocate warrant.
        	if (_runMode==MODE_MANUAL) {
                class Closer implements Runnable {
                    Warrant war;
                    Closer(Warrant w){
                        war = w;
                    }
                    public void run() {
                        synchronized(this) {
                            try {
                                // hold allocation 10 sec, then deallocate warrant
                                wait(10000);
                            } catch (InterruptedException ie) {
                                // ignore
                            }
                            String msg = war.setRunMode(Warrant.MODE_NONE, null, null, null, false);
                            if (msg!=null) {
                            	deAllocate();
                            }
                        }
                    }
                }
                Closer close = new Closer(this);
                javax.swing.SwingUtilities.invokeLater(close);
        	}
        } else {
            // No 'next block' for last BlockOrder
            String nextSpeed = getNextSpeed();
            if (_debug) log.debug("Block "+block.getDisplayName()+". currentSpeed= "+
                                                currentSpeed+", nextSpeed= "+nextSpeed);

            if (!nextSpeed.equals(currentSpeed) && _engineer!=null) {
                // ramp speed from current to speed restriction.
                // back off call (wait) so that endspeed occurs at exit of block.
                _engineer.rampSpeedTo(nextSpeed, getSpeedChangeWait(_idxCurrentOrder+1));
            }

            if (_idxCurrentOrder==activeIdx && (_runMode==MODE_LEARN || _tempRunBlind)) {
                // recording must done with signals and occupancy clear.
                if (currentSpeed.equals("Stop") || nextSpeed.equals("Stop")) {
                    firePropertyChange("abortLearn", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
                }
            }

            // attempt to allocate remaining blocks in the route
            for (int i=_idxCurrentOrder+2; i<_orders.size(); i++) {
                getBlockAt(i).allocate(this);
            }
        }

        if (_idxCurrentOrder==activeIdx) {
            // fire notification last so engineer's state can be documented in whatever GUI is listening.
            firePropertyChange("blockChange", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
        }
    }

    /**
    * Block in the route is going Inactive 
    */
    protected void goingInactive(OBlock block) {
        if (_runMode==MODE_NONE)  { return; }

        int idx = getIndexOfBlock(block);  // if idx >= 0, it is in this warrant
        if (_debug) log.debug("Block "+block.getDisplayName()+" goingInactive. idx= "+
                                            idx+", _idxCurrentOrder= "+_idxCurrentOrder);
        if (idx < _idxCurrentOrder) {
            // block is behind train.  Assume we are leaving.
            if (this.equals(block.getWarrant())) {
                block.setValue(null);
                block.deAllocate(this);
                _routeSet = false;
                _allocated = false;
            }
        } else if (idx==_idxCurrentOrder) {
            // Train not visible if current block goes inactive 
            // skip over dark blocks
            while (_idxCurrentOrder+1 < _orders.size() &&
                   (getBlockAt(_idxCurrentOrder+1).getState() & OBlock.DARK) > 0) {
                int oldIndex = _idxCurrentOrder;
                _idxCurrentOrder++;
                if (_runMode==MODE_LEARN || _tempRunBlind) {
                    // recording must done with signals and occupancy clear.
                    firePropertyChange("blockChange", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
                }
            }
        } else if (idx==_idxCurrentOrder+1) {
            // Assume Rouge train has left this block
            // Since it is the next block ahead of the train, we can move.
            // Presumeably we have stopped at the exit of the current block.
            if (_runMode==MODE_RUN || _runMode==MODE_MANUAL) {
                if (allocateNextBlock(block) && _engineer!=null) {
                    _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder+1),
                                          getSpeedChangeWait(_idxCurrentOrder+1));
                }
            }
        } else {
            // Assume Rouge train has left this block
            block.allocate(this);
        }
    }

    private long getSpeedChangeWait(int index) {
        BlockOrder bo = getBlockOrderAt(index);
        return bo.getEntranceSpeedChangeWait();
    }

    private String getCurrentSpeedAt(int index) {
        BlockOrder bo = getBlockOrderAt(index);
        bo.setPath(this);
        return bo.getPermissibleEntranceSpeed();
    }

    private boolean allocateNextBlock(OBlock block) {
        String blockMsg = block.allocate(this);
        if ( blockMsg != null) {
            log.warn("Block \""+block.getDisplayName()+"\" in warrant \""+getDisplayName()+
                     "\" is allocated to warrant \"" +blockMsg+"\"");
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
            nextBO.setPath(this);
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


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Warrant.class.getName());
}
