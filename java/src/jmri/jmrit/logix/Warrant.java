package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ThrottleListener;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
    private List <BlockOrder> _orders;		// temp orders used in run mode
    private LearnThrottleFrame _student;    // need to callback learning throttle in learn mode
    private boolean _tempRunBlind;			// run mode flag
    private boolean _delayStart;			// allows start block unoccupied and wait for train
    protected float _throttleFactor = 1.0f;
    protected List <ThrottleSetting> _commands;   // temp commands used in run mode
    private int   _idxCurrentOrder;       // Index of block at head of train (if running)
    protected int   _idxLastOrder;       	// Index of block at tail of train just left
    protected String _currentSpeed;			// name of last moving speed, i.e. never "Stop"
    protected String _exitSpeed;			// name of speed to exit the "protected" block

    private int     _runMode;
    private Engineer _engineer;         // thread that runs the train
    private boolean _allocated;         // initial Blocks of _orders have been allocated
    private boolean _totalAllocated;    // All Blocks of _orders have been allocated
    private boolean _routeSet;          // all allocated Blocks of _orders have paths set for route
    private OBlock  _stoppingBlock;     // Block allocated to another warrant or a rouge train
    private NamedBean _stoppingSignal;  // Signal stopping train movement
    private OBlock _shareTOBlock;		// Block in another warrant that controls a turnout in this block

    // Throttle modes
    public static final int MODE_NONE 	= 0;
    public static final int MODE_LEARN 	= 1;	// Record a command list
    public static final int MODE_RUN 	= 2;	// Autorun, playback the command list
    public static final int MODE_MANUAL = 3;	// block detection of manually run train

    // control states
    public static final int HALT = 1;
    public static final int RESUME = 2;
    public static final int ABORT = 3;
    public static final int WAIT_FOR_TRAIN = 4;
    public static final int RUNNING = 5;    
    public static final int SPEED_RESTRICTED = 6;    
    public static final int WAIT_FOR_CLEAR = 7;
    public static final int WAIT_FOR_SENSOR = 8;
    public static final int RETRY =9;

    private static jmri.implementation.SignalSpeedMap _speedMap;

    /**
     * Create an object with no route defined.
     * The list of BlockOrders is the route from an Origin to a Destination
     */
    public Warrant(String sName, String uName) {
        super(sName.toUpperCase(), uName);
        _idxCurrentOrder = 0;
        _idxLastOrder = 0;
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
    public void addBlockOrders(List<BlockOrder> orders) {
        for (int i=0; i<orders.size(); i++) {
        	_savedOrders.add(new BlockOrder(orders.get(i)));
        }
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
    final public BlockOrder getCurrentBlockOrder() {
        return getBlockOrderAt(_idxCurrentOrder);
    }

    final public int getCurrentOrderIndex() {
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
            return Bundle.getMessage("MustBeFloat");
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
    		return Bundle.getMessage("waitForDelayStart",
    				_trainName, getDisplayName(), getBlockOrderAt(0).getBlock().getDisplayName());
    	}
        switch (_runMode) {
            case Warrant.MODE_NONE:
                if (getOrders().size()==0) {
                    return Bundle.getMessage("BlankWarrant");
                }
                if (getDccAddress()==null){
                    return Bundle.getMessage("NoLoco");
                }
                if (getThrottleCommands().size() == 0) {
                    return Bundle.getMessage("NoCommands", getDisplayName());
                }
                if (_idxLastOrder==-1) {
                	_idxLastOrder = 0;
                	return Bundle.getMessage("locationUnknown", _trainName);
                } else {
                    return Bundle.getMessage("Idle");                	
                }
            case Warrant.MODE_LEARN:
                return Bundle.getMessage("Learning",
                                           getCurrentBlockOrder().getBlock().getDisplayName());
            case Warrant.MODE_RUN: 
                if (_engineer==null) {
                    return Bundle.getMessage("engineerGone");
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
                    	if (_engineer.getCurrentCommandIndex()>=_commands.size()-1) {
                    		_engineer = null;
                    		return Bundle.getMessage("endOfScript");
                    	}
                        return Bundle.getMessage("Aborted");
                    case Warrant.WAIT_FOR_CLEAR:
                        key = "WaitForClear";
                        break;
                    case Warrant.WAIT_FOR_TRAIN:
                        return Bundle.getMessage("WaitForTrain",
                        			cmdIdx, getBlockOrderAt(blkIdx).getBlock().getDisplayName());
                    case Warrant.WAIT_FOR_SENSOR:
                        return Bundle.getMessage("WaitForSensor",
                        			cmdIdx, _engineer.getWaitSensor().getDisplayName(),
                        			_commands.get(cmdIdx).getBlockName());
                    case Warrant.SPEED_RESTRICTED:
                        key = "SpeedRestricted";
                        break;
                    default:
                        key = "WhereRunning";
                }
                return Bundle.getMessage(key, getCurrentBlockOrder().getBlock().getDisplayName(), 
                             _engineer.getCurrentCommandIndex()+1, _engineer.getSpeedRestriction());
            case Warrant.MODE_MANUAL:
            	BlockOrder bo = getCurrentBlockOrder();
            	if (bo!=null) {
                    return Bundle.getMessage("ManualRunning", bo.getBlock().getDisplayName());            		
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
            if (_shareTOBlock!=null) {
            	_shareTOBlock.removePropertyChangeListener(this);
            	_shareTOBlock = null;
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
//            _idxLastOrder = 0;		can indicate an abort running message for lost routes
            _orders = _savedOrders;
        } else if (_runMode!=MODE_NONE) {
        	String modeDesc = null;
        	switch (_runMode) {
        		case MODE_LEARN:
        			modeDesc = Bundle.getMessage("Recording");
        			break;
        		case MODE_RUN:
        			modeDesc = Bundle.getMessage("AutoRun");
        			break;
        		case MODE_MANUAL:
        			modeDesc = Bundle.getMessage("ManualRun");
        			break;
        	}
            msg = Bundle.getMessage("WarrantInUse", modeDesc);
            log.error(msg);
            return msg;
        } else {
        	_idxLastOrder = 0;
            _delayStart = false;
            if (!_routeSet && runBlind) {
                msg = Bundle.getMessage("BlindRouteNotSet", getDisplayName());
                return null;
            }
            if (mode == MODE_LEARN) {
                // Cannot record if block 0 is not occupied or not dark. If dark, user is responsible for occupation
                if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED|OBlock.DARK))==0) {
                    msg = Bundle.getMessage("badStart", getDisplayName());
                    log.error("Block "+getBlockAt(0).getDisplayName()+", state= "+getBlockStateAt(0)+" err="+msg);
                    return msg;
                } else if (student == null) {
                    msg = Bundle.getMessage("noLearnThrottle", getDisplayName());
                    log.error(msg);
                    return msg;
                }
                _student = student;
                _currentSpeed = "Normal";
                _exitSpeed = "Normal";
            } else if (mode == MODE_RUN || mode==MODE_MANUAL) {
                if (commands == null || commands.size()== 0) {
                    _commands = _throttleCommands;
                } else {
                    _commands = commands;
               	 
                }
                // Delayed start is OK if block 0 is not occupied. Note can't delay start if block is dark
                if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED|OBlock.DARK))==0) {
                    // continuing with no occupation of starting block
                  	 setStoppingBlock(getBlockAt(0));
                   	 _delayStart = true;
                   	 log.info("Warrant "+getDisplayName()+" train \""+_trainName+
                   			 "\" does not occupy block "+ _stoppingBlock.getDisplayName());
                }
              	 if (_dccAddress==null) {	// if brand new warrant being tested. needed for a delayed start
            		 _dccAddress = address;
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
            } else if (!_delayStart) {
            	OBlock b = getBlockAt(0);
                b.setValue(_trainName);
                b.setState(b.getState() | OBlock.RUNNING);
            }
        }
        _runMode = mode;
        firePropertyChange("runMode", Integer.valueOf(oldMode), Integer.valueOf(_runMode));
        if(_debug) log.debug("Exit setRunMode()  _runMode= "+_runMode+", msg= "+msg);
        return msg;
    }	// end setRunMode
    
	//@SuppressWarnings("null")
    @Nullable private String acquireThrottle(@Nullable DccLocoAddress address) {
    	String msg = null;
        if (address == null)  {
            msg = Bundle.getMessage("NoAddress", getDisplayName());
            abortWarrant(msg);
            return msg;
        }
        if (InstanceManager.throttleManagerInstance()==null) {
            msg = Bundle.getMessage("noThrottle");
            abortWarrant(msg);
            return msg;
        }
        if (!InstanceManager.throttleManagerInstance().
                requestThrottle(address.getNumber(), address.isLongAddress(),this)) {
                msg = Bundle.getMessage("trainInUse", address.getNumber());
                abortWarrant(msg);
                return msg;
        }
        log.debug("Throttle at "+address.toString()+" acquired for warrant "+getDisplayName());        	
    	return null;
    }

    void abortWarrant(@NonNull String msg) {
        _delayStart = false;	// script should start - no more delay
        log.error("Abort warrant \""+ getDisplayName()+"\" "+msg);
        if (_engineer!=null) {
            _engineer.abort();
        }
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
                case RETRY:
                    ret = false;
                    break;
                case ABORT:
                	if (_runMode==Warrant.MODE_LEARN) {
                		// let WarrantFrame do the abort
                        firePropertyChange("abortLearn", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));                		
                	} else {
                        /*String msg = */setRunMode(Warrant.MODE_NONE, null, null, null, false);                		
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
                    case RETRY:
                    	retryBlocKWaiting();
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
        _idxLastOrder = 0;

        if (_runMode == MODE_LEARN) {
            _student.notifyThrottleFound(throttle);
            OBlock b = getCurrentBlockOrder().getBlock();
            b.setValue(_trainName);
            b.setState(b.getState() | OBlock.RUNNING);
        } else {
            getSpeedMap();      // initialize speedMap for getPermissibleEntranceSpeed() calls
            _engineer = new Engineer(this, throttle);
            if (_tempRunBlind) {
            	_engineer.setRunOnET(true);
            }
            startupWarrant();
            new Thread(_engineer).start();
            if (_delayStart) {
            	controlRunTrain(HALT);
            }
            _delayStart = false;	// script should start when user resumes - no more delay
        }
    }
    
    protected void startupWarrant() {
        _idxCurrentOrder = 0;
        _idxLastOrder = 0;
        // set block state to show our train occupies the block
        BlockOrder bo = getBlockOrderAt(0);
        OBlock b = bo.getBlock();          
        b.setValue(_trainName);
        b.setState(b.getState() | OBlock.RUNNING);
        // getNextSpeed() calls allocateNextBlock() who will set _stoppingBlock, if necessary
        // do before starting throttle commands in engineer
        _currentSpeed = getNextSpeed();		// will modify _currentSpeed, if necessary
        _engineer.rampSpeedTo(_currentSpeed, 0);    	
    }

    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        log.error("notifyFailedThrottleRequest address= " +address.toString()+" _runMode= "+_runMode+
        		" due to "+reason);
    }

    private String checkInService(List <BlockOrder> orders) {
        String msg = null;
        if (orders==null) {
            _orders = _savedOrders;
        } else {
            _orders = orders;
        }
        // Check route is in usable
        for (int i=0; i<_orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
            if ((block.getState() & OBlock.OUT_OF_SERVICE) !=0) {
                _orders.get(0).getBlock().deAllocate(this);
                msg = Bundle.getMessage("UnableToAllocate", getDisplayName()) +
                    Bundle.getMessage("BlockOutOfService", block.getDisplayName());
                break;
            }
        }
        return msg;
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
/*        if (orders==null) {
            _orders = _savedOrders;
        } else {
            _orders = orders;
        }*/
        _allocated = false;
        _totalAllocated = true;
        String msg = checkInService(orders);
        if (msg!=null) {
            _totalAllocated = false;
        	return msg;
        }
        // allocate all possible, report unoccupied blocks - changed 9/30/12
        // Only allocate up to occupied block (if any)
        for (int i=0; i<_orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
            int state = block.getState();
            msg = block.allocate(this);
            if (msg!=null) {
            	_totalAllocated = false;
                break;
            } else {
            	_allocated = true;		// partial allocation
            }
            if ((state & OBlock.OCCUPIED) > 0 && !this.equals(block.getWarrant())) {
                msg = Bundle.getMessage("BlockRougeOccupied", 
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
        String msg = checkInService(orders);
        if (msg!=null) {
        	_routeSet = false;
        	return msg;
        }
        _allocated = true;
        _totalAllocated = true;
    	OBlock startBlock = _orders.get(0).getBlock();
        for (int i=0; i<_orders.size(); i++) {
        	BlockOrder bo = _orders.get(i);
        	OBlock block = bo.getBlock();
        	if (i!=0 && (block.getState() & OBlock.OCCUPIED)>0 && !startBlock.equals(block)) {
        		msg = Bundle.getMessage("BlockRougeOccupied", 
                        		getDisplayName(), block.getDisplayName());
                _totalAllocated = false;
        		break;
        	} else {
            	msg = bo.setPath(this);
            	if (msg!=null) {
                    _totalAllocated = false;
            		if (i==0) { 
                        _allocated = false;
            			_routeSet = false;
            		}
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
            msg = Bundle.getMessage("BlockDark", block.getDisplayName());
         } else if ((state & OBlock.OCCUPIED) == 0) {
            msg = Bundle.getMessage("badStart", block.getDisplayName());
        }
    	return msg;
    }
    /**
     * Report any occupied blocks in the route
     */
    public String checkRoute() {
    	String msg =null;
    	OBlock startBlock = _orders.get(0).getBlock();
        for (int i=1; i<_orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
            if ((block.getState() & OBlock.OCCUPIED)>0 && !startBlock.equals(block)) {
            	msg = Bundle.getMessage("BlockRougeOccupied",
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
    	// another warrant has the starting block, but it could be a continuation of this train
    	if (w.getLastOrder().getBlock().equals(getfirstOrder().getBlock())     			
    			&& _dccAddress.equals(w.getDccAddress()) ) {
        	return null;
    	}
    	return Bundle.getMessage("OriginBlockNotSet",Bundle.getMessage("AllocatedToWarrant", 
    				getDisplayName(), getfirstOrder().getBlock().getDisplayName()));
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
        				OBlock tempSave = _stoppingBlock;  // checkStoppingBlock() nulls _stoppingBlock
    					if (checkStoppingBlock()) {
            				if (_runMode==MODE_RUN) {
                                msg = acquireThrottle(_dccAddress);        						
             				} else if (_runMode==MODE_MANUAL){
             	            	_delayStart = false;
            	            	firePropertyChange("blockChange", null, getBlockAt(_idxCurrentOrder));
            				}
               				tempSave.setValue(_trainName);
               				tempSave.setState(tempSave.getState() | OBlock.RUNNING);
             				tempSave =null;
    					}
        			} else {
        				// starting block allocated to another warrant for the SAME engine
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
			}
        } else if (property.equals("state") && _shareTOBlock!=null && _shareTOBlock==evt.getSource()) {
        	if ((((Number)evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
        		checkShareTOBlock();
        	}
        }
        if (msg!=null) {
        	log.error(msg);
        	deAllocate();
            setRunMode(Warrant.MODE_NONE, null, null, null, false);
            removePropertyChangeListener(this);
        }
     }

    /**
     * For the start block a return of true will allow warrant to acquire a throttle and
     * launch an engineer.  NOOP for all other blocks
     * @return
     */
    private boolean checkStoppingBlock() {
    	boolean ret = true;
        _stoppingBlock.removePropertyChangeListener(this);
        if (_debug) log.debug("checkStoppingBlock for warrant \""+getDisplayName()+"\" _stoppingBlock= \""+
    			_stoppingBlock.getDisplayName());
        String msg = _stoppingBlock.allocate(this);
    	if (msg==null) {
    		int idx = getIndexOfBlock(_stoppingBlock, _idxLastOrder);
    		msg = _orders.get(idx).setPath(this);                    	
            if (msg==null) {
            	if (_runMode==MODE_RUN) {
                	if (_idxCurrentOrder==0) {// in Starting block, no engineer yet
                		OBlock block = getBlockAt(_idxCurrentOrder);
                		if ((block.getState() &OBlock.DARK) != 0) {
                            _engineer.synchNotify(block); // notify engineer of control point
                            _engineer.rampSpeedTo(getNextSpeed(), 0);          			
                		}
                	} else {
                		if (_engineer!=null) {
                            _engineer.synchNotify(_stoppingBlock); // notify engineer of control point
                            _engineer.rampSpeedTo(getNextSpeed(), 0);
                    	} else {
                    		log.error("checkStoppingBlock: No Engineer for warrant "+getDisplayName());
                        	ret = false;
                    	}            		
                	}            		
            	} else if (_runMode==MODE_MANUAL){
             	}
            } else {
            	// callback sets _shareTOBlock
                log.info( "Warrant \""+getDisplayName()+"\" shares a turnout. "+msg);
                ret = false;
            }
            if (_debug) log.debug("Warrant \""+getDisplayName()+"\" _stoppingBlock= \""+_stoppingBlock.getDisplayName()+"\" Cleared.");
        	_stoppingBlock = null;
    	} else {
    		// allocation failed, continue to wait
            _stoppingBlock.addPropertyChangeListener(this);    		
        	log.warn("StoppingBlock not alllocated in warrant \""+ getDisplayName()+"\". "+msg);
            ret = false;
    	}
        if (_debug) log.debug("checkStoppingBlock "+ret+" for warrant \""+getDisplayName()+", msg= "+msg);
    	return ret;
    }

    /**
     * block (nextBlock) sharing a turnout with _shareTOBlock is already allocated.  
     */
    private void checkShareTOBlock() {
    	_shareTOBlock.removePropertyChangeListener(this);
        if (_debug) log.debug("_shareTOBlock= "+_shareTOBlock.getDisplayName()+" Cleared.");
        _shareTOBlock = null;            		
		String msg = _orders.get(_idxCurrentOrder+1).setPath(this);                    	
        if (msg==null) {
    		if (_engineer!=null) {
                _engineer.synchNotify(getBlockAt(_idxCurrentOrder+1)); // notify engineer of control point
                _engineer.rampSpeedTo(getNextSpeed(), 0);
        	} else {
        		log.error("checkShareTOBlock: No Engineer for warrant "+getDisplayName());
        	}
        } else {
        	// another block is sharing a turnout. and is set by callback
            log.info( "Warrant \""+getDisplayName()+"\" shares a turnout. "+msg);
        }   	
    }
    
    private void retryBlocKWaiting() {
    	if (_stoppingBlock==null && _shareTOBlock==null) {
            _engineer.synchNotify(getBlockAt(_idxCurrentOrder+1)); // notify engineer of control point
            _engineer.rampSpeedTo(getNextSpeed(), 0);
            return;
    	}
    	if (_stoppingBlock!=null && (_stoppingBlock.getState() & OBlock.UNOCCUPIED)==0) {
    		checkStoppingBlock();
    	}
    	if (_shareTOBlock!=null && (_shareTOBlock.getState() & OBlock.UNOCCUPIED)==0) {
    		checkShareTOBlock();
    	}
    }
    
    /**
     * Callback from nextblock of this warrant that has another block using a turnout
     * also used by the current path.  Rights to the turnout must be negotiated, 
     * otherwise warrants will deadlock. 
     * @param block
     * @return true if set
     */
    protected boolean setShareTOBlock(OBlock block) {
        if (_debug) log.debug("Warrant "+getDisplayName()+ " setShareTOBlock for block= "+
        		block.getDisplayName()+". current block= "+getBlockAt(_idxCurrentOrder).getDisplayName());
    	// check ahead to see who has rights to the turnout
        boolean owns = false;
        for (int i=_idxCurrentOrder+1; i<_orders.size() && i<_idxCurrentOrder+3; i++) {
            OBlock b = getBlockAt(i);
            owns = this.equals(b.getWarrant()); 
            if (_debug) log.debug("\t block= "+b.getDisplayName()+" is owned "+owns);
            if (!owns) break;
        }
        if (!owns) {
        	_shareTOBlock = block;
            _shareTOBlock.addPropertyChangeListener(this);    	
            log.info( "Warrant \""+getDisplayName()+"\" sets _shareTOBlock= \""+
            		_shareTOBlock.getDisplayName()+"\"");        	
        }
        return !owns;
    }

    private void setStoppingBlock(OBlock block)
    {
        _stoppingBlock = block;
        _stoppingBlock.addPropertyChangeListener(this);
        log.info( "Warrant \""+getDisplayName()+"\" sets _stoppingBlock= \""+
        		_stoppingBlock.getDisplayName()+"\"");
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
//        block.setValue(_trainName);
//        block.setState(block.getState() | OBlock.RUNNING);
        int oldIndex = _idxCurrentOrder;
        int activeIdx = getIndexOfBlock(block, _idxCurrentOrder);
        if (_debug) log.debug("Block "+block.getDisplayName()+" goingActive. activeIdx= "+
                                            activeIdx+", _idxCurrentOrder= "+_idxCurrentOrder+
                                            " _orders.size()= "+_orders.size()
                                            +" for warrant= "+getDisplayName());
        if (activeIdx<=0) {
        	// Not found or starting block, in which case 0 is handled as the _stoppingBlock
        	if (activeIdx==0 && _idxCurrentOrder==0) {
                block.setOccupationPending(true);
                // safety for late turnout throws -especially when coming from a dark block
                getBlockOrderAt(activeIdx).setPath(this);
        	}
        	return;
        }
        if (activeIdx == _idxCurrentOrder+1) {
            if (_engineer!=null && 
            		(_engineer.getRunState()==WAIT_FOR_CLEAR || _engineer.getRunState()==HALT)){
                // Ordinarily block just occupied would be this train, but train is stopped! - must be a rouge entry.
            	log.warn("Rouge entering next Block "+block.getDisplayName());
                return;
            } else {
                if (_debug) log.debug("Train entering Block "+block.getDisplayName()
                							+" for warrant= "+getDisplayName());
                // we assume it is our train entering the block - cannot guarantee it, but what else?
                _idxCurrentOrder = activeIdx;
                getBlockOrderAt(activeIdx).setPath(this);// safety for late TO throws -especially when coming from a dark block
                // set block state to show our train occupies the block
//                block.setValue(_trainName);
//                block.setState(block.getState() | OBlock.RUNNING);
            }
        } else if (activeIdx > _idxCurrentOrder+1) {
            log.warn("Rouge train ahead at block \""+block.getDisplayName()+"\"!");
        	if (_runMode==MODE_LEARN) {
	            log.error("Block "+block.getDisplayName()+" became occupied before block "+
	            		getBlockAt(_idxCurrentOrder+1).getDisplayName()+ " ABORT recording.");        				
                firePropertyChange("abortLearn", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
    		}
        	return;
        } else if (_idxCurrentOrder > 0) {
        	log.error("activeIdx ("+activeIdx+") < _idxCurrentOrder ("+_idxCurrentOrder+")!"); 
        }
        block.setValue(_trainName);
        block.setState(block.getState() | OBlock.RUNNING);        	
        // _idxCurrentOrder has been incremented. Warranted train has entered this block. 
        // Do signals, speed etc.
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
        	// allocate and set path, if necessary
            if (allocateNextBlock(getBlockOrderAt(_idxCurrentOrder+1))) {
                currentSpeed = getNextSpeed();        	
            }
            float len = getBlockAt(_idxCurrentOrder).getLengthIn();
            if ("Stop".equals(currentSpeed) && 0<len && len<6) {
            	currentSpeed = "EStop";
            }
            if (_engineer!=null) {
    			OBlock nextBlock = getBlockAt(_idxCurrentOrder+1);
    			if ((nextBlock.getState() & OBlock.DARK) > 0 || _tempRunBlind) {
    				_engineer.setRunOnET(true);
    			} else {
    				_engineer.setRunOnET(false);
    			}
    			_engineer.synchNotify(block); // notify engineer of control point
                _engineer.rampSpeedTo(currentSpeed, 0);
            }

            if (_runMode==MODE_LEARN || _tempRunBlind) {
                // recording must be done with signals and occupancy clear.
                if (currentSpeed.equals("Stop")) {
    	            log.error("Signals or occupancy ahead forces train to stop at block "+
    	            				getBlockAt(_idxCurrentOrder).getDisplayName()+ " ABORT recording.");        				
                    firePropertyChange("abortLearn", Integer.valueOf(oldIndex), Integer.valueOf(_idxCurrentOrder));
                }
            }

            // attempt to allocate remaining blocks in the route up to next occupation
            for (int i=_idxCurrentOrder+2; i<_orders.size(); i++) {
            	BlockOrder bo = _orders.get(i);
            	OBlock b = bo.getBlock();
                if (b.allocate(this)!=null) {
                	break;
                }
//                bo.setPath(this);		only set path in allocateNextBlock()
                if ((b.getState() & OBlock.OCCUPIED) > 0) {
                     break;
                }
           }
        }

        if (_idxCurrentOrder==activeIdx) {
            // fire notification last so engineer's state can be documented in whatever GUI is listening.
            firePropertyChange("blockChange", getBlockAt(oldIndex), block);
        }
    }		//end goingActive

    /**
    * Block in the route is going Inactive 
    */
    protected void goingInactive(OBlock block) {
        if (_runMode==MODE_NONE)  { return; }

        int idx = getIndexOfBlock(block, _idxLastOrder);  // if idx >= 0, it is in this warrant
        if (_debug) log.debug("Block "+block.getDisplayName()+" goingInactive. idx= "+
                                            idx+", _idxCurrentOrder= "+_idxCurrentOrder
                                            +" for warrant= "+getDisplayName());
        if (idx < _idxCurrentOrder) {
        	_idxLastOrder = idx;
        	/* Only deallocate block if train will not use the  block again.  Blocks ahead could loop back over
        	 * blocks previously traversed.  That is, don't disturb re-allocation of blocks ahead.
        	 * Previous Dark blocks do need deallocation
        	 */ 
            for (int i=idx; i>-1; i--) {
            	OBlock prevBlock = getBlockAt(i);
        		boolean dealloc = true;
        		for (int j=idx+1; j<_orders.size(); j++) {
        			if (prevBlock.equals(getBlockAt(j))) {
        				dealloc = false;
        				break;
        			}
        		}
        		if (dealloc) {
                	prevBlock.deAllocate(this);            			
        		}
            }
        } else if (idx==_idxCurrentOrder) {
            // Train not visible if current block goes inactive
        	if (_idxCurrentOrder+1<_orders.size()) {
               	OBlock nextBlock = getBlockAt(_idxCurrentOrder+1);
               	if ((nextBlock.getState() & OBlock.DARK) > 0) {
                   	if (_runMode==MODE_LEARN) {
                   		goingActive(nextBlock);
                   	} else if (_engineer!=null) {
                   		_engineer.setRunOnET(true);
                   	}               		
               	} else {
               		// train is lost
                	_idxLastOrder = -1;
                    firePropertyChange("blockChange", block, null);
                	setRunMode(Warrant.MODE_NONE, null, null, null, false);
                    return;
               	}
        	} else {
               	log.error("Warrant "+getDisplayName()+" at last block "+block.getDisplayName()+
               			" and going inactive!");               		
        	}
            if (_engineer!=null) {
                block.setValue(_trainName);
                block.setState(block.getState() | OBlock.RUNNING);
                _engineer.synchNotify(block); // notify engineer of control point
                _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder), 0);
            } else if (_runMode!=MODE_LEARN &&_idxCurrentOrder+1 == _orders.size()){
            	// this would be a very weird case
            	setRunMode(Warrant.MODE_NONE, null, null, null, false);
            }
        } else if (idx==_idxCurrentOrder+1) {
            // Assume Rouge train has left this block
            // Since it is the next block ahead of the train, we can move.
            // Presumably we have stopped within the current block.
            if (_engineer!=null && allocateNextBlock(getBlockOrderAt(idx))) {
                _engineer.synchNotify(block); // notify engineer of control point
                _engineer.rampSpeedTo(getCurrentSpeedAt(_idxCurrentOrder+1), 0);
            }
        } else {
            // Assume Rouge train has left this block
            block.allocate(this);
        }
    }			// end goingInactive

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
        String msg = bo.setPath(this);
        String speed = bo.getPermissibleEntranceSpeed();
        String exitSpeed = bo.getPermissibleExitSpeed();
    	long speedOffset = 1000*getSpeedChangeWait(_idxCurrentOrder);
        if (speed!=null) {
        	// speed change from signals
        	if (speed.equals("Stop")) {
                _stoppingSignal = bo.getSignal();
                _stoppingSignal.addPropertyChangeListener(this);
        	}
        	if(_debug) log.debug("signal indicates \""+speed+"\" entrance speed and \""+exitSpeed+
        			"\" exit speed on Warrant \""+getDisplayName()+
            		"\".\n Set speed change Delay to "+speedOffset+"ms for entrance into "+
            		getBlockOrderAt(index).getBlock().getDisplayName()+
            		(_stoppingSignal==null ? ".": " _stoppingSignal= \""+_stoppingSignal.getDisplayName()+"\""));
        } else {
        	if (_exitSpeed!=null) {
        		// saved exit speed from last block
            	speed = _exitSpeed;        		
        	} else {
            	// continue as before
            	speed = _currentSpeed;        		
        	}
        }
        if (msg!=null) {
            log.info("Getting speed in warrant \""+getDisplayName()+"\": "+msg);        	
        }
/*        OBlock block = bo.getBlock();
        float len = block.getLengthIn();
        if ("Stop".equals(speed) && 0 <len && len < 6.0 ) {
        	speed = "EStop";
        }*/
        _exitSpeed = exitSpeed;
        if(_debug) log.debug("getCurrentSpeedAt("+index+"): speed= \""+speed+"\" for warrant= "+getDisplayName());
        if (_stoppingBlock==null && _stoppingSignal==null && _shareTOBlock==null) {
            if (_engineer!=null) {
            	_engineer.setWaitforClear(false);
            }        	
        }
        return speed;
    }

    /**
     * If block cannot be allocated, will set a listener on the block.
     * @param block is the next block from some current location
     * @return
     */
    private boolean allocateNextBlock(BlockOrder bo) {
    	if (bo==null) {
    		return false;
    	}
    	OBlock block = bo.getBlock();
        String blockMsg = block.allocate(this);
        if ( blockMsg != null || (block.getState() & OBlock.OCCUPIED)>0) {
        	setStoppingBlock(block);
            log.info((blockMsg!=null ? blockMsg : (block.getDisplayName()+" allocated, but Occupied.")));
            return false;
        }
        blockMsg = bo.setPath(this);
        if (blockMsg!=null) {
        	// _shareTOBlock is set by callback from setPath()
            log.info( "Warrant \""+getDisplayName()+"\" shares a turnout. "+blockMsg);
            return false;
        }
        block.setOccupationPending(true);
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
        String exitSpeed = null;
        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder+1);
        OBlock nextBlock = bo.getBlock();
        if (allocateNextBlock(bo)) {
            nextSpeed = bo.getPermissibleEntranceSpeed();
            exitSpeed = bo.getPermissibleExitSpeed();
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
             	if(_debug) log.debug("signal indicates \""+nextSpeed+"\" entrance speed and \""+exitSpeed+
            			"\" exit speed on Warrant \""+getDisplayName()+"\".\n\t\t Set change speed Delay to "+
                		speedOffset+"ms for entrance into "+nextBlock.getDisplayName()+
                		(_stoppingSignal==null ? ".": " _stoppingSignal= \""+_stoppingSignal.getDisplayName()+"\""));
            } else { 
        		// No signals have set speeds
            	if ((nextBlock.getState() & OBlock.OCCUPIED) != 0) {
            		// Rule 292 - "visible" obstacle ahead. no signals or they didn't detect it.
            		nextSpeed = "Stop";
            	} else {
            		nextSpeed = "Normal";
            	}
            }
            // If next block is dark, check blocks beyond for occupancy
            if ((nextBlock.getState() & OBlock.DARK) != 0) {
            	for (int idx=_idxCurrentOrder+2; idx < _orders.size(); idx++) {
                    bo = getBlockOrderAt(idx);
                    if ((bo.getBlock().getState() & OBlock.OCCUPIED) != 0) {
                    	setStoppingBlock(bo.getBlock());
                        nextSpeed = "Stop";
                        if(_debug) log.debug("Block beyond dark block Occupied. Warrant \""+getDisplayName());
                        break;
                    }
            		
            	}
            }
        } else {
        	// _stoppingBlock has been set to nextBlock
            nextSpeed = "Stop";
        }
    	if (!"Stop".equals(nextSpeed)) {
        	_currentSpeed = nextSpeed;        		
    	}
        _exitSpeed = exitSpeed;
        if(_debug) log.debug("getNextSpeed(): Entrance speed for \""+nextBlock.getDisplayName()+"\"= \""+
        						nextSpeed+"\" for warrant= "+getDisplayName());
        return nextSpeed;
    }
    
    public void dispose() {
    	deAllocate();
    	super.dispose();
    }
    
    static Logger log = LoggerFactory.getLogger(Warrant.class.getName());
}
