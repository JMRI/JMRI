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
    protected String _currentSpeed;			// name of last moving speed, i.e. never "Stop"

    private int     _runMode;
    private Engineer _engineer;         // thread that runs the train
    private boolean _allocated;         // initial Blocks of _orders have been allocated
    private boolean _totalAllocated;    // All Blocks of _orders have been allocated
    private boolean _routeSet;          // all allocated Blocks of _orders have paths set for route
    private OBlock  _stoppingBlock;     // Block allocated to another warrant or a rouge train
    private NamedBean _stoppingSignal;  // Signal stopping train movement

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
    public static final int WAIT_FOR_SENSOR = 8;

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
    	List <BlockOrder> orders = _orders;
    	if (orders==null) {
    		orders = _savedOrders;
    	}
        for (int i=0; i<orders.size(); i++){
            if (orders.get(i).getBlock().equals(block)) {
                return orders.get(i).getPathName();
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
    protected int getIndexOfBlock(OBlock block, int startIdx) {
        for (int i=startIdx; i<_orders.size(); i++){
            if (_orders.get(i).getBlock().equals(block)) {
                return i;
            }
        }
        return -1;
    }
    /**
    * Call is only valid when in MODE_LEARN and MODE_RUN
    * (previously start was i=_idxCurrentOrder)
    */
    protected int getIndexOfBlock(String name, int startIdx) {
        for (int i=startIdx; i<_orders.size(); i++){
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
    public boolean isTotalAllocated() { return _totalAllocated; }
    /**
    * Turnouts are set for the route
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

    protected String getRunningMessage() {
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
            	int cmdIdx = _engineer.getCurrentCommandIndex()+1;
            	if (cmdIdx>=_commands.size()) {
            		cmdIdx =_commands.size()-1; 
            	}
            	int blkIdx = _idxCurrentOrder+1;
            	if (blkIdx>=_orders.size()) {
            		blkIdx = _orders.size()-1;
            	}
                switch (_engineer.getRunState()) {
                    case Warrant.HALT:
                        key = "Halted";
                        break;
                    case Warrant.ABORT:
                    	if (_commands!=null && 
                    			_engineer.getCurrentCommandIndex()>=_commands.size()-1) {
                    		_engineer = null;
                    		return rb.getString("endOfScript");
                    	}
                        return rb.getString("Aborted");
                    case Warrant.WAIT_FOR_CLEAR:
                        key = "WaitForClear";
                        break;
                    case Warrant.WAIT_FOR_TRAIN:
                        return java.text.MessageFormat.format(rb.getString("WaitForTrain"),
                        			cmdIdx, getBlockOrderAt(blkIdx).getBlock().getDisplayName());
                    case Warrant.WAIT_FOR_SENSOR:
                        return java.text.MessageFormat.format(rb.getString("WaitForSensor"),
                        			cmdIdx, _engineer.getWaitSensor().getDisplayName(),
                        			_commands.get(cmdIdx).getBlockName());
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
            	BlockOrder bo = getCurrentBlockOrder();
            	if (bo!=null) {
                    return java.text.MessageFormat.format(rb.getString("ManualRunning"),
                            bo.getBlock().getDisplayName());            		
            	}
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
    public String setRunMode(int mode, DccLocoAddress address, 
                                 LearnThrottleFrame student, 
                                 List <ThrottleSetting> commands, boolean runBlind) 
    {
        if(_debug) log.debug("setRunMode("+mode+")  _runMode= "+_runMode+" for warrant= "+getDisplayName());
        String msg = null;
        int oldMode = _runMode;
        if (mode == MODE_NONE) {
            _delayStart = false;
            if (_stoppingSignal!=null) {
                log.error("signal "+_stoppingSignal.getSystemName());
                _stoppingSignal.removePropertyChangeListener(this);
                _stoppingSignal = null;
            }
            if (_stoppingBlock!=null) {
                _stoppingBlock.removePropertyChangeListener(this);
                _stoppingBlock = null;
            }
            if (_student !=null) {
                _student.dispose();
                _student = null;
            }
            if (_engineer!=null && _engineer.getRunState() != Warrant.ABORT) {
            	_engineer.abort();
            	_engineer = null;
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
                return null;
            }
            if (mode == MODE_LEARN) {
                // start is OK if block 0 is occupied (or dark - in which case user is responsible)
                if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED|OBlock.DARK))==0) {
                    msg = java.text.MessageFormat.format(rb.getString("badStart"),getDisplayName());
                    log.error("Block "+getBlockAt(0).getDisplayName()+", state= "+getBlockStateAt(0)+" err="+msg);
                    return msg;
                } else if (student == null) {
                    msg = java.text.MessageFormat.format(rb.getString("noLearnThrottle"), getDisplayName());
                    log.error(msg);
                    return msg;
                }
                _student = student;
                _currentSpeed = "Normal";
             } else if (mode == MODE_RUN) {
                 if (commands == null || commands.size()== 0) {
                     _commands = _throttleCommands;
                 } else {
                     _commands = commands;
                	 
                 }
                 // start is OK if block 0 is occupied (or dark - in which case user is responsible)
                 if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED|OBlock.DARK))==0) {
                     // continuing with no occupation of starting block
                     _stoppingBlock = getBlockAt(0);
                     _stoppingBlock.addPropertyChangeListener(this);
                     _delayStart = true;
                	 log.info(java.text.MessageFormat.format(rb.getString("warnStart"),
     			 			_trainName, _stoppingBlock.getDisplayName()));
                 }
             }
            _runMode = mode;	// set mode before callback to notifyThrottleFound is called
            if (mode!=MODE_MANUAL) {
                 _tempRunBlind = runBlind;
                 if (!_delayStart) {
                	 if (address==null) {
                		 address = _dccAddress;
                	 }
                     msg = acquireThrottle(address);
                     if (msg!=null){
                    	 return msg;
                     }                	 
                 }
            }
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
        if(_debug) log.debug("controlRunTrain= "+idx+" runMode= "+_runMode+" for warrant= "+getDisplayName());
        boolean ret = true;
        int oldIndex = MODE_MANUAL;
        if (_engineer == null) { 
            switch (idx) {
                case HALT:
                case RESUME:
                    ret = false;
                    break;
                case ABORT:
                	if (_runMode==Warrant.MODE_LEARN) {
                		// let WarrantFrame do the abort
                        firePropertyChange("abortLearn", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));                		
                	} else {
                        String msg = setRunMode(Warrant.MODE_NONE, null, null, null, false);                		
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

        if(_debug) {
           log.debug("notifyThrottleFound address= " +throttle.getLocoAddress().toString()+" _runMode= "+_runMode);
        }
        _idxCurrentOrder = 0;

        if (_runMode == MODE_LEARN) {
            _student.notifyThrottleFound(throttle);
        } else {
            getSpeedMap();      // initialize speedMap for getPermissibleEntranceSpeed() calls
            _engineer = new Engineer(this, throttle);
            startupWarrant();
            new Thread(_engineer).start();
        }
    }
    
    protected void startupWarrant() {
        _idxCurrentOrder = 0;
        // set block state to show our train occupies the block
        BlockOrder bo = getBlockOrderAt(0);
        OBlock b = bo.getBlock();          
        b.setValue(_trainName);
        b.setState(b.getState() | OBlock.RUNNING);
        // getNextSpeed() calls allocateNextBlock() who will set _stoppingBlock, if necessary
        // do before starting throttle commands in engineer
        _currentSpeed = "Normal";
        _currentSpeed = getNextSpeed();		// will modify _currentSpeed, if necessary
        _engineer.rampSpeedTo(_currentSpeed, 0);    	
    }

    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        log.error("notifyFailedThrottleRequest address= " +address.toString()+" _runMode= "+_runMode+
        		" due to "+reason);
    }

    /**
    * Allocate the current saved blocks of this warrant.
    * Installs listeners for the entire route.  Sets this warrant into
    * @return error message, if any
    */
    public String allocateRoute(List <BlockOrder> orders) {
        if (_totalAllocated) {
            return null;
        }
        if (orders==null) {
            _orders = _savedOrders;
        } else {
            _orders = orders;
        }
        _allocated = false;
        _totalAllocated = true;
        OBlock block = null;
        String msg = null;
        // Check route is in usable
        for (int i=0; i<_orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            block = bo.getBlock();
            if ((block.getState() & OBlock.OUT_OF_SERVICE) !=0) {
                _orders.get(0).getBlock().deAllocate(this);
                msg = java.text.MessageFormat.format(rb.getString("UnableToAllocate"), getDisplayName()) +
                    java.text.MessageFormat.format(rb.getString("BlockOutOfService"),block.getDisplayName());
                _totalAllocated = false;
                break;
            }
        }
        // allocate all possible, report unoccupied blocks - changed 9/30/12
        // Only allocate up to occupied block (if any)
        for (int i=0; i<_orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            block = bo.getBlock();
            int state = block.getState();
            msg = block.allocate(this);
            if (msg!=null) {
            	_totalAllocated = false;
                break;
            } else {
            	_allocated = true;		// partial allocation
            }
            if ((state & OBlock.OCCUPIED) > 0 && !this.equals(block.getWarrant())) {
                msg = java.text.MessageFormat.format(rb.getString("BlockRougeOccupied"), 
                		block.getWarrant().getDisplayName(), block.getDisplayName());
                _totalAllocated = false;
                break;
            }
        }
        
        if(_debug) log.debug("allocateRoute for warrant \""+getDisplayName()+"\"  _allocated= "+_allocated);
        firePropertyChange("allocate", Boolean.valueOf(false), Boolean.valueOf(_allocated));
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
        _totalAllocated = false;
        _routeSet = false;
        if(_debug) log.debug("deallocated Route for warrant \""+getDisplayName()+"\".");
//        firePropertyChange("deallocate", Boolean.valueOf(old), Boolean.valueOf(false));
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
        _routeSet = true;
        String msg = allocateRoute(_orders);
    	OBlock startBlock = _orders.get(0).getBlock();
        for (int i=0; i<_orders.size(); i++) {
        	BlockOrder bo = _orders.get(i);
        	OBlock block = bo.getBlock();
        	if (i!=0 && (block.getState() & OBlock.OCCUPIED)>0 && !startBlock.equals(block)) {
        		msg = java.text.MessageFormat.format(rb.getString("BlockRougeOccupied"), 
                        		getDisplayName(), block.getDisplayName());
        		_routeSet = false;  // don't throw switches under a rouge train
        		break;
        	} else {
            	msg = bo.setPath(this);
            	if (msg!=null) {
            		if (i==0) { _routeSet = false; }
            		break;
            	}        		
        	}
        }
        firePropertyChange("setRoute", Boolean.valueOf(false), Boolean.valueOf(_routeSet));
        if(_debug) log.debug("setRoute for warrant \""+getDisplayName()+"\"  _routeSet= "+_routeSet);
        return msg;
    }   // setRoute

    /**
     * Check start block for occupied for start of run
     * @return
     */
    protected String checkStartBlock() {
     	BlockOrder bo = _orders.get(0);
    	OBlock block = bo.getBlock();
       	String msg = block.allocate(this);
       	if (msg!=null) {
       		return msg;
       	}
       	msg = bo.setPath(this);
       	if (msg!=null) {
       		return msg;
       	}       	
        int state = block.getState();
        if ((state & OBlock.DARK) != 0) {
            msg = java.text.MessageFormat.format(rb.getString("BlockDark"), 
                                                block.getDisplayName());
         } else if ((state & OBlock.OCCUPIED) == 0) {
            msg = java.text.MessageFormat.format(rb.getString("badStart"), 
                                                block.getDisplayName());
        }
    	return msg;
    }
    /**
     * Report any occupied blocks in the route
     * @return
     */
    public String checkRoute() {
    	String msg =null;
    	OBlock startBlock = _orders.get(0).getBlock();
        for (int i=1; i<_orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
            if ((block.getState() & OBlock.OCCUPIED)>0 && !startBlock.equals(block)) {
            	msg = java.text.MessageFormat.format(rb.getString("BlockRougeOccupied"),
            			getDisplayName(), block.getDisplayName());
            	_totalAllocated = false;
            }
        }
        return msg;
    }
    public String checkForContinuation() {
    	Warrant w = getfirstOrder().getBlock().getWarrant();
    	if (this.equals(w)) {
    		return null;
    	}
    	// another warrant has the starting block
    	if (w.getLastOrder().getBlock().equals(getfirstOrder().getBlock())     			
    			&& _dccAddress.equals(w.getDccAddress()) ) {
        	return null;
    	}
    	return java.text.MessageFormat.format(rb.getString("OriginBlockNotSet"), w.getDisplayName());
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
//            if (_debug) log.debug("propertyChange \""+evt.getPropertyName()+
//                                                "\" old= "+evt.getOldValue()+" new= "+evt.getNewValue());
            return;
        }
        String property = evt.getPropertyName();
    	String msg = null;
        if (_debug) log.debug("propertyChange \""+property+"\" new= "+evt.getNewValue()+
                                            " source= "+((NamedBean)evt.getSource()).getDisplayName()
                                            +" for warrant= "+getDisplayName());
        if (_stoppingSignal != null && _stoppingSignal==evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // signal blocking warrant has changed. Should (MUST) be the next block.
                _stoppingSignal.removePropertyChangeListener(this);
                _stoppingSignal = null;
                if (_engineer!=null) {
                    _engineer.synchNotify(getBlockAt(_idxCurrentOrder)); // notify engineer of control point
                    _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder) ,0);
                }
                return;
            }
        } else if (property.equals("state") && _stoppingBlock!=null && _stoppingBlock==evt.getSource()) {
        	// starting block is allocated but not occupied
        	if (_delayStart) {	// wait for arrival of train to begin the run 
        		if ( (((Number)evt.getNewValue()).intValue() & OBlock.OCCUPIED) != 0 ) {
        			// train arrived at starting block
        			Warrant w = _stoppingBlock.getWarrant();
        			if (this.equals(w) || w==null) {
        				if (checkStoppingBlock()) {
                            msg = acquireThrottle(_dccAddress);
                            if (msg!=null){
                             	log.error("Abort warrant \""+ getDisplayName()+"\" "+msg);
                             	if (_engineer!=null) {
                                	_engineer.abort();                             		
                             	}
                            }
        				}
        			} else {
        				// starting block allocated to another warrant for the same engine
        				// which has just arrived at the starting block for this warrant
        				// However, we must wait for the other warrant to finish
        				w.addPropertyChangeListener(this);
        			}
                }
        	} else if ((((Number)evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
        		// normal wait for a train underway but blocked ahead by occupation
                //  blocking occupation has left the stopping block
    			if (checkStoppingBlock()) {
    			}
        	}
        } else if (_delayStart && property.equals("runMode") &&
    						((Number)evt.getNewValue()).intValue()==MODE_NONE)  {
    		// Starting block was owned by another warrant for this engine 
    		// Engine has arrived and Blocking Warrant has finished
			((Warrant)evt.getSource()).removePropertyChangeListener(this);
			if (checkStoppingBlock()) {
                msg = acquireThrottle(_dccAddress);
                if (msg!=null) {
                 	log.error("Abort warrant \""+ getDisplayName()+"\" "+msg);
                 	if (_engineer!=null) {
                    	_engineer.abort();
                 	}
                }
			}
        }
        if (msg!=null) {
        	log.error(msg);
        }
     }
    
    private boolean checkStoppingBlock() {
        _stoppingBlock.removePropertyChangeListener(this);
        String msg = _stoppingBlock.allocate(this);
        if (_debug) log.debug("checkStoppingBlock for _stoppingBlock= "+_stoppingBlock.getDisplayName()+
        		" allocate msg= "+msg);
    	if (msg==null) {
    		int idx = getIndexOfBlock(_stoppingBlock, 0);
    		msg = _orders.get(idx).setPath(this);                    	
            if (msg!=null) {
            	log.warn("StoppingBlock path warrant \""+ getDisplayName()+"\" "+msg);
            } else {
            	if (idx==_idxCurrentOrder) {
                	_stoppingBlock.setValue(_trainName);
                	_stoppingBlock.setState(_stoppingBlock.getState() | OBlock.RUNNING);            		
            	}
            	if (_engineer!=null) {
                    _engineer.synchNotify(_stoppingBlock); // notify engineer of control point
                    _engineer.rampSpeedTo(getNextSpeed(), 0);
           		
            	} else {
                	_stoppingBlock = null;            		
            	}
                 return true;
            }
    	} else {
    		// allocation failed, continue to wait
            _stoppingBlock.addPropertyChangeListener(this);    		
    	}
    	return false;
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
        int activeIdx = getIndexOfBlock(block, _idxCurrentOrder);
        boolean rougeEntry = false;
        if (_debug) log.debug("Block "+block.getDisplayName()+" goingActive. activeIdx= "+
                                            activeIdx+", _idxCurrentOrder= "+_idxCurrentOrder+
                                            " _orders.size()= "+_orders.size()
                                            +" for warrant= "+getDisplayName());
        if (activeIdx<=0) {
        	// Not found or starting block, in which case 0 is handled as the _stoppingBlock
        	return;
        }
        // skip over dark blocks
        if ((getBlockAt(_idxCurrentOrder).getState() & OBlock.DARK) > 0) {
            firePropertyChange("blockChange", Integer.valueOf(_idxCurrentOrder), Integer.valueOf(_idxCurrentOrder+1));
        }
        if (activeIdx == _idxCurrentOrder+1) {
            if (_engineer!=null && _engineer.getRunState()==WAIT_FOR_CLEAR) {
                // Ordinarily block just occupied would be this train, but train is stopped! - must be a rouge entry.
                rougeEntry = true;
                log.warn("Rouge entering next Block "+block.getDisplayName());
            } else {
                if (_debug) log.debug("Train entering Block "+block.getDisplayName()
                							+" for warrant= "+getDisplayName());
                // we assume it is our train entering the block - cannot guarantee it, but what else?
                _idxCurrentOrder = activeIdx;
                // set block state to show our train occupies the block
                block.setValue(_trainName);
                block.setState(block.getState() | OBlock.RUNNING);
            }
        } else if (activeIdx > _idxCurrentOrder+1) {
            // rouge train invaded route ahead.
            rougeEntry = true;
        } else if (_idxCurrentOrder > 0) {
            log.error("activeIdx ("+activeIdx+") < _idxCurrentOrder ("+_idxCurrentOrder+")!"); 
        }

        if (rougeEntry) {
            log.warn("Rouge train ahead at block \""+block.getDisplayName()+"\"!");
        }

        String currentSpeed = "Stop";
        if (_idxCurrentOrder == _orders.size()-1) {
            // must be in destination block, No 'next block' for last BlockOrder
            // If Auto running, let script finish according to recorded times.
        	// End of script will deallocate warrant.
            currentSpeed = getCurrentSpeedAt(_idxCurrentOrder);        	
            if (_engineer!=null) {
                _engineer.synchNotify(block); // notify engineer of control point
                _engineer.rampSpeedTo(currentSpeed, 0);
            }

        	if (_runMode==MODE_MANUAL) {
                String msg = setRunMode(Warrant.MODE_NONE, null, null, null, false);
                if (msg!=null) {
                	deAllocate();
                }
        	}
        } else {
            if (allocateNextBlock(getBlockAt(_idxCurrentOrder+1))) {
                currentSpeed = getNextSpeed();        	
            }
            if (_engineer!=null) {
                _engineer.synchNotify(block); // notify engineer of control point
                _engineer.rampSpeedTo(currentSpeed, 0);
            }

            if (_idxCurrentOrder==activeIdx && (_runMode==MODE_LEARN || _tempRunBlind)) {
                // recording must done with signals and occupancy clear.
                if (currentSpeed.equals("Stop")) {
                    firePropertyChange("abortLearn", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
                }
            }

            // attempt to allocate remaining blocks in the route up to next occupation
            for (int i=_idxCurrentOrder+2; i<_orders.size(); i++) {
            	BlockOrder bo = _orders.get(i);
            	OBlock b = bo.getBlock();
                b.allocate(this);
                bo.setPath(this);
                if (i!=0 && (b.getState() & OBlock.OCCUPIED) > 0) {
                     break;
                }
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

        int idx = getIndexOfBlock(block, 0);  // if idx >= 0, it is in this warrant
        if (_debug) log.debug("Block "+block.getDisplayName()+" goingInactive. idx= "+
                                            idx+", _idxCurrentOrder= "+_idxCurrentOrder
                                            +" for warrant= "+getDisplayName());
        if (idx < _idxCurrentOrder) {
            // block is behind train.  Assume we have left.
            // block.deAllocate(this);
            for (int i=idx; i>-1; i--) {
            	OBlock prevBlock = getBlockAt(i);
            	if ((prevBlock.getState() & OBlock.DARK) > 0) {
                	prevBlock.deAllocate(this);            		
            	}
            }
        } else if (idx==_idxCurrentOrder) {
            // Train not visible if current block goes inactive 
            // skip over dark blocks
        	OBlock nextBlock = getBlockAt(_idxCurrentOrder+1);
            while (_idxCurrentOrder+1 < _orders.size() && (nextBlock.getState() & OBlock.DARK) > 0) {
                block.setValue(null);
                block.deAllocate(this);
            	int oldIndex = _idxCurrentOrder;
            	_idxCurrentOrder++;
                firePropertyChange("blockChange", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
                block = nextBlock;
            	nextBlock = getBlockAt(_idxCurrentOrder+1);
            }
            if (_runMode==MODE_RUN || _runMode==MODE_MANUAL) {
            	//  at last block           	
                if (_engineer!=null) {
                    block.setValue(_trainName);
                    block.setState(block.getState() | OBlock.RUNNING);
                    _engineer.synchNotify(block); // notify engineer of control point
                    _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder), 0);
                } else if (_idxCurrentOrder+1 == _orders.size()){
                	// this would be a very weird case
                	setRunMode(Warrant.MODE_NONE, null, null, null, false);
                }
           }
        } else if (idx==_idxCurrentOrder+1) {
            // Assume Rouge train has left this block
            // Since it is the next block ahead of the train, we can move.
            // Presumably we have stopped at the exit of the current block.
            if (_runMode==MODE_RUN) {
                if (_engineer!=null && allocateNextBlock(block)) {
                    _engineer.synchNotify(block); // notify engineer of control point
                    _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder+1), 0);
                }
            }
        } else {
            // Assume Rouge train has left this block
            block.allocate(this);
        }
    }

    /**
     * Set a delay on when the speed change for a signal should occur.
     * @param index
     * @return
     */
    private long getSpeedChangeWait(int index) {
        BlockOrder bo = getBlockOrderAt(index);
        return bo.getEntranceSpeedChangeWait();
    }

    /**
     * Warrant already owns the block, get permissible speed from
     * the signals.
     * Called by: 
     * 	propertyChange -when _stoppingSignal clears
     * 	goingActive -when at current block
     * 	goingInactive -when next block clears from rouge train or at last block
     * @param index
     * @return
     */
    private String getCurrentSpeedAt(int index) {
    	if (index==0) {
    		index++;	//use entrance speed of next block for starting up
    	}
        BlockOrder bo = getBlockOrderAt(index);
        bo.setPath(this);
        String speed = bo.getPermissibleEntranceSpeed();
    	long speedOffset = 1000*getSpeedChangeWait(_idxCurrentOrder);
        if (speed==null && index>0) {
        	bo = getBlockOrderAt(index-1);
        	speed = bo.getPermissibleExitSpeed();
        	speedOffset = 1000*getSpeedChangeWait(_idxCurrentOrder-1);
        }
        if (speed!=null) {
        	// speed change from signals
        	if (speed.equals("Stop")) {
                _stoppingSignal = bo.getSignal();
                _stoppingSignal.addPropertyChangeListener(this);
        	}
        	if(_debug) log.debug("signal indicates \""+speed+"\" speed aspect on Warrant \""+getDisplayName()+
            		"\". Set speed change Delay to "+speedOffset+"ms for entrance into "+
            		getBlockOrderAt(index).getBlock().getDisplayName()+
            		(_stoppingSignal==null ? ".": " _stoppingSignal= \""+_stoppingSignal.getDisplayName()+"\""));
        } else {
        	// continue as before
        	speed = _currentSpeed;
        }
        if(_debug) log.debug("getCurrentSpeedAt("+index+"): speed= \""+speed+"\" for warrant= "+getDisplayName());
        return speed;
    }

    /**
     * If block cannot be allocated, will set a listener on the block.
     * @param block is the next block from some current location
     * @return
     */
    private boolean allocateNextBlock(OBlock block) {
    	if (block==null) {
    		return false;
    	}
        String blockMsg = block.allocate(this);
        if ( blockMsg != null || (block.getState() & OBlock.OCCUPIED)>0) {
            _stoppingBlock = block;
            _stoppingBlock.addPropertyChangeListener(this);
            log.info((blockMsg!=null ? blockMsg : (block.getDisplayName()+" Occupied."))+" Warrant \""+getDisplayName()+
            		"\" sets _stoppingBlock= \""+_stoppingBlock.getDisplayName()+"\"");
            return false;
        }
        return true;
    }

    /**
     *  if next block is allocated, set the path. If there are no
     *  occupation problems get the permitted speed from the signals
     *  Sets adlay speed change in advance of the next block
     *  Called by: 
     *  	startWarrant at start
     *  	checkStoppingBlock when stopping block has cleared
     * @return an "occupied" (Stop or continue) speed change
     */
    private String getNextSpeed() {
        String nextSpeed = null;
        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder+1);
        OBlock nextBlock = bo.getBlock();
        if (allocateNextBlock(nextBlock)) {
            bo.setPath(this);
            nextSpeed = bo.getPermissibleEntranceSpeed();
            long speedOffset = 1000*getSpeedChangeWait(_idxCurrentOrder+1);
            if (nextSpeed==null) {
            	bo = getBlockOrderAt(_idxCurrentOrder);
            	nextSpeed = bo.getPermissibleExitSpeed();
               	speedOffset = 1000*getSpeedChangeWait(_idxCurrentOrder);
           }
            if (nextSpeed!=null ) {
            	if (nextSpeed.equals("Stop")) {
                    _stoppingSignal = bo.getSignal();
                    _stoppingSignal.addPropertyChangeListener(this);
            	}
             	if(_debug) log.debug("signal indicates \""+nextSpeed+"\" speed aspect on Warrant \""+getDisplayName()+
                		"\". Set change speed Delay to "+speedOffset+"ms for entrance into "+nextBlock.getDisplayName()+
                		(_stoppingSignal==null ? ".": " _stoppingSignal= \""+_stoppingSignal.getDisplayName()+"\""));
            } else if ((nextBlock.getState() & OBlock.OCCUPIED) != 0) {
                // Rule 292 - "visible" obstacle ahead. no signals or they didn't detect it.
                nextSpeed = "Stop";
            }
            // If next block is dark, check blocks beyond for occupancy
            if ((nextBlock.getState() & OBlock.DARK) != 0) {
            	for (int idx=_idxCurrentOrder+2; idx < _orders.size(); idx++) {
                    bo = getBlockOrderAt(idx);
                    if ((bo.getBlock().getState() & OBlock.OCCUPIED) != 0) {
                        _stoppingBlock = bo.getBlock();
                        _stoppingBlock.addPropertyChangeListener(this);
                        nextSpeed = "Stop";
                        if(_debug) log.debug("Block Occupied. Warrant \""+getDisplayName()+
                        		"\" sets _stoppingBlock= \""+_stoppingBlock.getDisplayName()+"\"");
                        break;
                    }
            		
            	}
            }
        } else {
            _stoppingBlock = bo.getBlock();
            _stoppingBlock.addPropertyChangeListener(this);
            nextSpeed = "Stop";
            log.info("Block can't be allocated. Warrant \""+getDisplayName()+
            		"\" sets _stoppingBlock= \""+_stoppingBlock.getDisplayName()+"\"");
        }
        if (nextSpeed!=null) {
        	if (!"Stop".equals(nextSpeed)) {
            	_currentSpeed = nextSpeed;        		
        	}
        } else {
        	nextSpeed = _currentSpeed;
        }
        if(_debug) log.debug("getNextSpeed(): Entrance speed for \""+nextBlock.getDisplayName()+"\"= \""+
        						nextSpeed+"\" for warrant= "+getDisplayName());
        return nextSpeed;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Warrant.class.getName());
}
