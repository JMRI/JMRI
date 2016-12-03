package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.List;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Warrant contains the operating permissions and directives needed for a
 * train to proceed from an Origin to a Destination. There are three modes that
 * a Warrant may execute;
 * <p>
 * MODE_LEARN - Warrant is created or edited in WarrantFrame and then launched
 * from WarrantFrame who records throttle commands from "_student" throttle.
 * Warrant fires PropertyChanges for WarrantFrame to record when blocks are
 * entered. "_engineer" thread is null.
 * </p><P>
 * MODE_RUN - Warrant may be launched from several places. An array of
 * BlockOrders, _savedOrders, and corresponding _throttleCommands allow an
 * "_engineer" thread to execute the throttle commands. The blockOrders
 * establish the route for the Warrant to acquire and reserve OBlocks. The
 * Warrant monitors block activity (entrances and exits, signals, rouge
 * occupancy etc) and modifies speed as needed.
 * </p><P>
 * MODE_MANUAL - Warrant may be launched from several places. The Warrant to
 * acquires and reserves the route from the array of BlockOrders. Throttle
 * commands are done by a human operator. "_engineer" and "_throttleCommands"
 * are not used. Warrant monitors block activity but does not set _stoppingBlock
 * or _shareTOBlock since it cannot control speed. It does attempt to realign
 * the route as needed, but can be thwarted.
 * </p><P>
 * Version 1.11 - remove setting of SignalHeads
 *
 * @author  Pete Cressman Copyright (C) 2009, 2010
 */
public class Warrant extends jmri.implementation.AbstractNamedBean
        implements ThrottleListener, java.beans.PropertyChangeListener {

    public static final String Stop = "Stop";   // NOI18N
    public static final String EStop = "EStop";     // NOI18N
    public static final String Normal = "Normal";   // NOI18N
    public static final String Clear = "Clear";     // NOI18N
    
    // permanent members.
    private ArrayList <BlockOrder> _savedOrders = new ArrayList <BlockOrder>();
    private BlockOrder _viaOrder;
    private BlockOrder _avoidOrder;
    private List<ThrottleSetting> _throttleCommands = new ArrayList<ThrottleSetting>();
    protected String _trainName;      // User train name for icon
    private String _trainId;        // Roster Id
    private DccLocoAddress _dccAddress;
    private boolean _runBlind;      // Unable to use block detection, must run on et only
    private boolean _noRamp;        // do immediate speed change at approach block.

    // transient members
    protected List<BlockOrder> _orders;       // temp orders used in run mode
    private LearnThrottleFrame _student;    // need to callback learning throttle in learn mode
    private boolean _tempRunBlind;          // run mode flag to allow running on ET only
    private boolean _delayStart;            // allows start block unoccupied and wait for train
    protected List <ThrottleSetting> _commands;   // temp commands used in run mode
    protected int     _idxCurrentOrder;       // Index of block at head of train (if running)
    protected int     _idxLastOrder;          // Index of block at tail of train just left
    private String  _curSpeedType;          // name of last moving speed, i.e. never "Stop"
//    private int     _idxSpeedChange;        // Index of last BlockOrder where slower speed changes were scheduled
    private ArrayList<BlockSpeedInfo> _speedInfo; // map max speeds and occupation times of each block in route
//    private String _exitSpeed;            // name of speed to exit the "protected" block

    protected int _runMode;
    protected Engineer _engineer;       // thread that runs the train
    private CommandDelay _delayCommand; // thread for delayed ramp down
    private boolean _allocated;         // initial Blocks of _orders have been allocated
    private boolean _totalAllocated;    // All Blocks of _orders have been allocated
    private boolean _routeSet;          // all allocated Blocks of _orders have paths set for route
    protected OBlock _stoppingBlock;     // Block allocated to another warrant or a rouge train
    private NamedBean _stoppingSignal;  // Signal stopping train movement
    private OBlock _shareTOBlock;       // Block in another warrant that controls a turnout in this block
    private String _message;            // last message returned from an action
    private Calibrater _calibrater;     // Calibrates throttle speed factor
    RosterEntry     _train;

    // Throttle modes
    public static final int MODE_NONE = 0;
    public static final int MODE_LEARN = 1; // Record a command list
    public static final int MODE_RUN = 2;   // Autorun, playback the command list
    public static final int MODE_MANUAL = 3;    // block detection of manually run train
    public static final String[] MODES = {"none", "LearnMode", "RunAuto", "RunManual"};
    public static final int MODE_ABORT = 4; // used to set status string in WarrantTableFrame

    // control states
    public static final int STOP = 0;
    public static final int HALT = 1;
    public static final int RESUME = 2;
    public static final int ABORT = 3;
    public static final int RETRY = 4;
    public static final int ESTOP = 5;
    protected static final int RUNNING = 5;
    protected static final int SPEED_RESTRICTED = 6;
    protected static final int WAIT_FOR_CLEAR = 7;
    protected static final int WAIT_FOR_SENSOR = 8;
    protected static final int WAIT_FOR_TRAIN = 9;
    protected static final int WAIT_FOR_DELAYED_START = 10;
    protected static final int LEARNING = 11;
    protected static final String[] CNTRL_CMDS = {"Stop", "Halt", "Resume", "Abort", "Retry", "EStop"};
    protected static final String[] RUN_STATE = {"HaltStart", "atHalt", "Resumed", "Aborts", "Retried",
        "Running", "RestrictSpeed", "WaitingForClear", "WaitingForSensor", "RunningLate"};

    // Estimated positions of the train in the block it occupies
    static final int BEG    = 1;
    static final int MID    = 2;
    static final int END    = 3;
    /**
     * Create an object with no route defined. The list of BlockOrders is the
     * route from an Origin to a Destination
     * @param sName system name
     * @param uName user name
     */
    public Warrant(String sName, String uName) {
        super(sName.toUpperCase(), uName);
        _idxCurrentOrder = 0;
        _idxLastOrder = 0;
        _orders = new ArrayList<BlockOrder>();
        _runBlind = false;
    }

    public int getState() {
        if (_engineer != null) {
            return _engineer.getRunState();
        }
        if (_delayStart) {
            return WAIT_FOR_DELAYED_START;
        }
        if (_runMode==MODE_LEARN) {
            return LEARNING;
        }
        if (_runMode!=MODE_NONE) {
            return RUNNING;
        }
        return -1;
    }

    public void setState(int state) {
    }
    
    /**
     * Return permanently saved BlockOrders
     * @return list of block orders
     */
    public List<BlockOrder> getBlockOrders() {
        ArrayList<BlockOrder> orders = new ArrayList<BlockOrder>();        
        for (int i = 0; i < _savedOrders.size(); i++) {
            orders.add(new BlockOrder(_savedOrders.get(i)));
        }
        return orders;
    }

    /**
     * Add permanently saved BlockOrder
     * @param order block order
     */
    public void addBlockOrder(BlockOrder order) {
        _savedOrders.add(order);
    }

    public void setBlockOrders(List<BlockOrder> orders) {
        _savedOrders.clear();
        for (int i = 0; i < orders.size(); i++) {
            _savedOrders.add(new BlockOrder(orders.get(i)));
        }
    }
    protected void setOrders(List<BlockOrder> orders) {
        _orders = orders;
    }
    protected List<BlockOrder> getOrders() {
        if (_orders!=null && _orders.size()>0) {
            return _orders;            
        }
        return getBlockOrders();
    }

    /**
     * Return permanently saved Origin
     * @return origin block order
     */
    public BlockOrder getfirstOrder() {
        if (_savedOrders.size() == 0) {
            return null;
        }
        return new BlockOrder(_savedOrders.get(0));
    }

    /**
     * Return permanently saved Destination
     * @return destination block order
     */
    public BlockOrder getLastOrder() {
        if (_savedOrders.size() == 0) {
            if (_orders.size()==0) {
                return null;
            } else {
                return new BlockOrder(_orders.get(_orders.size() - 1));                
            }
        }
        return new BlockOrder(_savedOrders.get(_savedOrders.size() - 1));
    }

    /**
     * Return permanently saved BlockOrder that must be included in the route
     * @return via block order
     */
    public BlockOrder getViaOrder() {
        if (_viaOrder == null) {
            return null;
        }
        return new BlockOrder(_viaOrder);
    }

    public void setViaOrder(BlockOrder order) {
        _viaOrder = order;
    }

    public BlockOrder getAvoidOrder() {
        if (_avoidOrder == null) {
            return null;
        }
        return new BlockOrder(_avoidOrder);
    }

    public void setAvoidOrder(BlockOrder order) {
        _avoidOrder = order;
    }

    protected String getRoutePathInBlock(OBlock block) {
        List<BlockOrder> orders = _orders;
        if (orders == null) {
            orders = getBlockOrders();
        }
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getBlock().equals(block)) {
                return orders.get(i).getPathName();
            }
        }
        return null;
    }

    /**
     * @return block order currently at the train position
     */
    final public BlockOrder getCurrentBlockOrder() {
        return getBlockOrderAt(_idxCurrentOrder);
    }

    /**
     * @return index of block order currently at the train position
     */
    final public int getCurrentOrderIndex() {
        return _idxCurrentOrder;
    }

    /**
     * @param block used by the warrant
     * @param startIdx index of block order 
     * @return index of block ahead of block order index, -1 if not found
     */
    protected int getIndexOfBlock(OBlock block, int startIdx) {
        for (int i = startIdx; i < _orders.size(); i++) {
            if (_orders.get(i).getBlock().equals(block)) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Call is only valid when in MODE_LEARN and MODE_RUN (previously start was
     * i=_idxCurrentOrder)
     */
    protected int getIndexOfBlock(String name, int startIdx) {
        for (int i = startIdx; i < _orders.size(); i++) {
            if (_orders.get(i).getBlock().getDisplayName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Call is only valid when in MODE_LEARN and MODE_RUN
     * @param index index of block order
     * @return block order or null if not found
     */
    protected BlockOrder getBlockOrderAt(int index) {
        if (index >= 0 && index < _orders.size()) {
            return _orders.get(index);
        }
        return null;
    }

    /**
     * Call is only valid when in MODE_LEARN and MODE_RUN
     * @param idx index of block order
     * @return block of the block order
     */
    protected OBlock getBlockAt(int idx) {

        BlockOrder bo = getBlockOrderAt(idx);
        if (bo != null) {
            return bo.getBlock();
        }
        return null;
    }

    /**
     * Call is only valid when in MODE_LEARN and MODE_RUN
     * @return Name of OBlock currently occupied 
     */
    public String getCurrentBlockName() {
        OBlock block = getBlockAt(_idxCurrentOrder);
        if (block == null) {
            return "";
        } else {
            return block.getDisplayName();
        }
    }
    
    /**
     * Call is only valid when in MODE_LEARN and MODE_RUN
     */
    private int getBlockStateAt(int idx) {

        OBlock b = getBlockAt(idx);
        if (b != null) {
            return b.getState();
        }
        return OBlock.UNKNOWN;
    }

    synchronized public List<ThrottleSetting> getThrottleCommands() {
        ArrayList<ThrottleSetting> list = new ArrayList<ThrottleSetting>();
        for (int i = 0; i < _throttleCommands.size(); i++) {
            list.add(new ThrottleSetting(_throttleCommands.get(i)));
        }
        return list;
    }

    public void setThrottleCommands(List<ThrottleSetting> list) {
        _throttleCommands = list;
    }

    public void addThrottleCommand(ThrottleSetting ts) {
        _throttleCommands.add(ts);
    }
    
    public void setNoRamp(boolean set) {
        _noRamp = set;
    }
    
    public boolean getNoRamp() {
        return _noRamp;
    }

    public String getTrainName() {
        return _trainName;
    }

    public void setTrainName(String name) {
        _trainName = name;
    }

    public String getTrainId() {
        return _trainId;
    }

    /**
     * Fetches RosterEntry
     * @param id may be either Roster entry or DCC address
     * @return true if RosterEntry found
     */
    public boolean setTrainId(String id) {
        _trainId = id;
        if (id == null || id.trim().length() == 0) {
            return false;
        }
        _train = Roster.getDefault().entryFromTitle(id);
        if (_train != null) {
            _dccAddress = _train.getDccLocoAddress();
        } else {
            return setDccAddress(id);
        }
        return true;
    }
    
    protected RosterEntry getRosterEntry() {
        return _train;
    }

    public DccLocoAddress getDccAddress() {
        return _dccAddress;
    }

    /**
     * Sets dccAddress and fetches RosterEntry
     * @param id address as a String
     * @return true if address found for id
     */
    public boolean setDccAddress(String id) {
        _train = Roster.getDefault().entryFromTitle(id);
        if (_train == null) {
            int index = id.indexOf('(');
            String numId;
            if (index >= 0) {
                numId = id.substring(0, index);
            } else {
                numId = id;
            }
            try {
                List<RosterEntry> l = Roster.getDefault().matchingList(null, null, numId, null, null, null, null);
                if (l.size() > 0) {
                    _train = l.get(0);
                    if (_trainId == null) {
                        // In some systems, such as Maerklin MFX or ESU ECOS M4, the DCC address is always 0.
                        // That should not make us overwrite the _trainId.
                        _trainId = _train.getId();
                    }
                } else {
                    _train = null;
                    _trainId = null;
                    boolean isLong = true;
                    if ((index + 1) < id.length()
                            && (id.charAt(index + 1) == 'S' || id.charAt(index + 1) == 's')) {
                        isLong = false;
                    }
                    int num = Integer.parseInt(numId);
                    _dccAddress = new DccLocoAddress(num, isLong);
                    _trainId = _dccAddress.toString();
               }
            } catch (NumberFormatException e) {
                _dccAddress = null;
                return false;
            }            
        } else {
            _trainId = id;
            _dccAddress = _train.getDccLocoAddress();           
        }
        return true;
    }

    public boolean getRunBlind() {
        return _runBlind;
    }

    public void setRunBlind(boolean runBlind) {
        _runBlind = runBlind;
    }

    synchronized protected DccThrottle getThrottle() {
        if (_engineer!=null) {
            return _engineer.getThrottle();
        }
        return null;
    }
   
    protected void setCalibrater(Calibrater c) {
        _calibrater = c;
    }

    /*
     * Engineer reports its status
     */
    protected void fireRunStatus(String property, Object old, Object status) {
        // error if not on Layout thread
        if (!ThreadingUtil.isLayoutThread()) log.error("invoked on wrong thread", new Exception("traceback"));
        
        firePropertyChange(property, old, status);
    }

    /**
     * ****************************** state queries ****************
     */
    /**
     * @return true if all listeners are installed for the route
     */
    public boolean isAllocated() {
        return _allocated;
    }

    public boolean isTotalAllocated() {
        return _totalAllocated;
    }

    /**
     * Turnouts are set for the route
     * @return true if turnouts are set
     */
    public boolean hasRouteSet() {
        return _routeSet;
    }

    /**
     * Test if the permanent saved blocks of this warrant are free (unoccupied
     * and unallocated)
     * @return true if route is free
     */
    public boolean routeIsFree() {
        for (int i = 0; i < _savedOrders.size(); i++) {
            OBlock block = _savedOrders.get(i).getBlock();
            if (!block.isFree()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test if the permanent saved blocks of this warrant are occupied
     * @return true if any block is occupied
     */
    public boolean routeIsOccupied() {
        for (int i = 1; i < _savedOrders.size(); i++) {
            OBlock block = _savedOrders.get(i).getBlock();
            if ((block.getState() & OBlock.OCCUPIED) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * ************* Methods for running trains ***************
     * @return ID of run mode
     */
    public int getRunMode() {
        return _runMode;
    }

    protected String getRunModeMessage() {
        String modeDesc = null;
        switch (_runMode) {
            case MODE_NONE:
                return Bundle.getMessage("NotRunning", getDisplayName());
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
        return Bundle.getMessage("WarrantInUse", modeDesc, getDisplayName());

    }

    synchronized protected String getRunningMessage() {
        if (_delayStart) {
            return Bundle.getMessage("waitForDelayStart",
                    _trainName, getBlockOrderAt(0).getBlock().getDisplayName());
        }
        switch (_runMode) {
            case Warrant.MODE_NONE:
                if (getBlockOrders().size() == 0) {
                    return Bundle.getMessage("BlankWarrant");
                }
                if (getDccAddress() == null) {
                    return Bundle.getMessage("NoLoco");
                }
                if (getThrottleCommands().size() == 0) {
                    return Bundle.getMessage("NoCommands", getDisplayName());
                }
                if (_idxCurrentOrder!=0 && _idxLastOrder==_idxCurrentOrder) {
                    return Bundle.getMessage("locationUnknown", _trainName, getCurrentBlockName())+_message;
                }
                if (_message == null) {
                    return Bundle.getMessage("Idle");
                }
                return Bundle.getMessage("Idle1", _message);
            case Warrant.MODE_LEARN:
                return Bundle.getMessage("Learning", getCurrentBlockName());
            case Warrant.MODE_RUN:
                if (_engineer == null) {
                    return Bundle.getMessage("engineerGone", getCurrentBlockName());
                }
                int cmdIdx = _engineer.getCurrentCommandIndex();
                if (cmdIdx>=_commands.size()) {
                    cmdIdx =_commands.size()-1; 
                }
                int blkIdx = _idxCurrentOrder+1;
                if (blkIdx>=_orders.size()) {
                    blkIdx = _orders.size()-1;
                }
                OBlock block = getCurrentBlockOrder().getBlock();
                if ((block.getState() & (OBlock.OCCUPIED | OBlock.DARK))==0) {
                    return Bundle.getMessage("LostTrain", _trainName, block.getDisplayName());
                }
                String blockName = block.getDisplayName();
                String speed = _engineer.getSpeedRestriction();
                
                String msg = null;
                switch (_engineer.getRunState()) {
                    case Warrant.HALT:
                        msg = Bundle.getMessage("Halted", blockName, cmdIdx);
                        break;
                    case Warrant.RESUME:
                        msg = Bundle.getMessage("reStarted", blockName, cmdIdx, speed);
                        break;
                    case Warrant.RETRY:
                        msg = Bundle.getMessage("reRetry", blockName, cmdIdx, speed);
                        break;
                    case Warrant.ABORT:
                        if (cmdIdx == _commands.size() - 1) {
                            _engineer = null;
                            return Bundle.getMessage("endOfScript", _trainName);
                        }
                        msg = Bundle.getMessage("Aborted", blockName, cmdIdx);
                        break;
                    case Warrant.WAIT_FOR_CLEAR:
                        msg = Bundle.getMessage("WaitForClear", _trainName, blockName, (_stoppingSignal==null ? "occupancy" : "signal"));
                        break;
                    case Warrant.WAIT_FOR_TRAIN:
                        return Bundle.getMessage("WaitForTrain", cmdIdx,
                                getBlockOrderAt(blkIdx).getBlock().getDisplayName(), speed);
                    case Warrant.WAIT_FOR_SENSOR:
                        return Bundle.getMessage("WaitForSensor",
                                cmdIdx, _engineer.getWaitSensor().getDisplayName(),
                                _commands.get(cmdIdx).getBlockName());
                    case Warrant.SPEED_RESTRICTED:
                        msg = Bundle.getMessage("SpeedRestricted", blockName, cmdIdx, speed);
                        break;
                    default:
                        msg = Bundle.getMessage("WhereRunning", blockName, cmdIdx, speed);
                }
                if (_message != null) {
                    return Bundle.getMessage("Append", msg, _message);
                }
                return msg;

            case Warrant.MODE_MANUAL:
                BlockOrder bo = getCurrentBlockOrder();
                if (bo != null) {
                    return Bundle.getMessage("ManualRunning", bo.getBlock().getDisplayName());
                }
        }
        return "ERROR mode= " + MODES[_runMode];
    }

    protected void startTracker() {
        // error if not on Layout thread
        if (!ThreadingUtil.isLayoutThread()) log.error("invoked on wrong thread", new Exception("traceback"));
        
        TrackerTableAction.markNewTracker(getCurrentBlockOrder().getBlock(), _trainName);
    }

    synchronized public void stopWarrant(boolean abort) {
        _delayStart = false;
        if (_stoppingSignal != null) {
            _stoppingSignal.removePropertyChangeListener(this);
            _stoppingSignal = null;
        }
        if (_stoppingBlock != null) {
            _stoppingBlock.removePropertyChangeListener(this);
            _stoppingBlock = null;
        }
        if (_shareTOBlock != null) {
            _shareTOBlock.removePropertyChangeListener(this);
            _shareTOBlock = null;
        }
        if (_student != null) {
            _student.dispose();     // releases throttle
            _student = null;
        }
        _calibrater = null;                
        if (_engineer != null) {
            if (abort) {
                _engineer.abort();
            }
            _engineer.releaseThrottle();
            _engineer = null;
        }
        deAllocate();
        int oldMode = _runMode;
        _runMode = MODE_NONE;
        if (abort) {
            firePropertyChange("runMode", Integer.valueOf(oldMode), Integer.valueOf(_runMode));            
        } else {
            firePropertyChange("runMode", Integer.valueOf(oldMode), Integer.valueOf(MODE_ABORT));            
        }
        if (log.isDebugEnabled()) {
            log.debug("Warrant \"{}\" terminated.", getDisplayName());
        }
    }

    /**
     * Sets up recording and playing back throttle commands - also cleans up
     * afterwards. MODE_LEARN and MODE_RUN sessions must end by calling again
     * with MODE_NONE. It is important that the route be deAllocated (remove
     * listeners).
     * <p>
     * Rule for (auto) MODE_RUN: 1. At least the Origin block must be owned
     * (allocated) by this warrant. (block._warrant == this) and path set for
     * Run Mode Rule for (auto) LEARN_RUN: 2. Entire Route must be allocated and
     * Route Set for Learn Mode. i.e. this warrant has listeners on all block
     * sensors in the route. Rule for MODE_MANUAL The Origin block must be
     * allocated to this warrant and path set for the route
     * @param mode run mode
     * @param address DCC loco address
     * @param student throttle frame for learn mode parameters
     * @param commands list of throttle commands
     * @param runBlind true if occupancy should be ignored
     * @return error message, if any
*/
    public String setRunMode(int mode, DccLocoAddress address,
            LearnThrottleFrame student,
            List<ThrottleSetting> commands, boolean runBlind) {
        if (log.isDebugEnabled()) {
            log.debug("setRunMode({}) ({}) called with _runMode= {}. warrant= {}", 
                    mode, MODES[mode], MODES[_runMode], getDisplayName());
        }
        _message = null;
        if (_runMode != MODE_NONE) {
            _message = getRunModeMessage();
            log.error(_message);
            return _message;
        }
        _idxLastOrder = 0;
        _delayStart = false;
        _curSpeedType = Normal;
        if (mode == MODE_LEARN) {
            // Cannot record if block 0 is not occupied or not dark. If dark, user is responsible for occupation
            if (student == null) {
                _message = Bundle.getMessage("noLearnThrottle", getDisplayName());
                log.error(_message);
                return _message;
            }
            synchronized(this) {
               _student = student;
            }
        } else if (mode == MODE_RUN || mode == MODE_MANUAL) {
            if (commands == null || commands.size() == 0) {
                _commands = _throttleCommands;
            } else {
                _commands = commands;
            }
            // set mode before setStoppingBlock and callback to notifyThrottleFound are called
            _idxCurrentOrder = 0;
            _runMode = mode;
            // Delayed start is OK if block 0 is not occupied. Note can't delay start if block is dark
            if (!runBlind) {
                int state = getBlockStateAt(0);
                if ((state & (OBlock.OCCUPIED | OBlock.DARK)) == 0) {
                    // continuing with no occupation of starting block
                    setStoppingBlock(getBlockAt(0));
                    _delayStart = true;                    
                }
            }
            if (_dccAddress == null) {  // if brand new warrant being tested. needed for a delayed start
                _dccAddress = address;
                setTrainId(_dccAddress.toString());     // get RosterEntry
            }
        } else {
            stopWarrant(true);
        }
        // set mode before setStoppingBlock and callback to notifyThrottleFound are called
        _runMode = mode;
        getBlockAt(0)._entryTime = System.currentTimeMillis();
        if (_runBlind) {
            _tempRunBlind = _runBlind;            
        } else {
            _tempRunBlind = runBlind;            
        }
        if (!_delayStart) {
            if (mode!=MODE_MANUAL) {
                 if (address==null) {
                     _message = acquireThrottle(_dccAddress);
                 } else {
                     _message = acquireThrottle(address);                     
                 }
             } else {
                startupWarrant();       // assuming operator will go to start block              
             }
        }
        firePropertyChange("runMode", Integer.valueOf(MODE_NONE), Integer.valueOf(_runMode));
        if (log.isDebugEnabled()) {
            log.debug("Exit setRunMode()  _runMode= {}, msg= {}", MODES[_runMode], _message);
        }
        return _message;
    }   // end setRunMode
    
    protected String acquireThrottle(DccLocoAddress address) {
        String msg = null;
        if (address == null)  {
            msg = Bundle.getMessage("NoAddress", getDisplayName());
            abortWarrant(msg);
            return msg;
        }
        jmri.ThrottleManager tm = InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
        if (tm==null) {
            msg = Bundle.getMessage("noThrottle");
        } else {
            if (!tm.requestThrottle(address.getNumber(), address.isLongAddress(),this)) {
                msg = Bundle.getMessage("trainInUse", address.getNumber());
            }           
        }
        if (msg!=null) {
            abortWarrant(msg);
            firePropertyChange("throttleFail", null, msg);          
            return msg;
        }
        if(log.isDebugEnabled()) log.debug("Throttle at {} requested for warrant {}",
                address.toString(), getDisplayName());          
        return null;
    }

    protected void abortWarrant(String msg) {
        log.error("Abort warrant \"{}\" - {} ", getDisplayName(), msg);
        stopWarrant(true);
    }

    /**
     * Pause and resume auto-running train or abort any allocation state
     * _engineer.abort() calls setRunMode(MODE_NONE,...) which calls deallocate
     * all.
     * @param idx index of control command
     * @return false if command cannot be given
     */
    public boolean controlRunTrain(int idx) {
        if (log.isDebugEnabled()) {
            log.debug("controlRunTrain({})= {} runMode= {}, warrant= {}", 
                    idx, CNTRL_CMDS[idx], MODES[_runMode], getDisplayName());
        }
        boolean ret = false;
        if (_engineer == null) {
            switch (idx) {
                case HALT:
                case RESUME:
                case RETRY:
                    firePropertyChange("SpeedChange", null, Integer.valueOf(idx));
                    break;
                case STOP:
                case ABORT:
                    if (_runMode == Warrant.MODE_LEARN) {
                        // let WarrantFrame do the abort. (WarrantFrame listens for "abortLearn") 
                        firePropertyChange("abortLearn", Integer.valueOf(-MODE_LEARN), Integer.valueOf(_idxCurrentOrder));
                    } else {
                        stopWarrant(true);
                        ret = true;
                    }
            }
            return ret;
        } 
        int runState = _engineer.getRunState();
        synchronized (_engineer) {
            switch (idx) {
                case HALT:
                    _engineer.setHalt(true);
                    ret = true;
                    break;
                case RESUME:
                    _engineer.setHalt(false);
                    BlockOrder bo = getBlockOrderAt(_idxCurrentOrder);
                    OBlock block = bo.getBlock();
                    if ((block.getState() & (OBlock.OCCUPIED | OBlock.DARK)) != 0) {
                        ret = setMovement(MID);
                    }
                    break;
                case RETRY: // Force move into next block
                    bo = getBlockOrderAt(_idxCurrentOrder + 1);
                    // if block belongs to this warrant, then move unconditionally into block
                    if (bo != null) {
                        block = bo.getBlock();
                        if (block.allocate(this) == null && (block.getState() & OBlock.OCCUPIED) != 0) {
                            _idxCurrentOrder++;
                            if (block.equals(_stoppingBlock) && clearStoppingBlock()) {
                                _engineer.rampSpeedTo(_curSpeedType);
                            }
                            bo.setPath(this);
                            goingActive(block);
                            ret = true;
                        }
                    }
                    break;
                case ABORT:
                    stopWarrant(true);
                    ret = true;
                    break;
                case STOP:
                    _engineer.setStop(false);
                    ret = true;
                    break;
                case ESTOP:
                    _engineer.setStop(true);    // E-stop
                    ret = true;
                    break;
            }
        }
        if (ret) {
            firePropertyChange("controlChange", Integer.valueOf(runState), Integer.valueOf(idx));
        }
        return ret;
    }

    public void notifyThrottleFound(DccThrottle throttle) {
        if (throttle == null) {
            abortWarrant("notifyThrottleFound: null throttle(?)!");
            firePropertyChange("throttleFail", null, Bundle.getMessage("noThrottle"));
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("notifyThrottleFound for address= {}, _runMode= {}", 
                    throttle.getLocoAddress().toString(), MODES[_runMode]);
        }

        startupWarrant();
        if (_runMode == MODE_LEARN) {
            synchronized(this) {
               _student.notifyThrottleFound(throttle);
            }
        } else {
            getBlockSpeedTimes();
            _engineer = new Engineer(this, throttle);
            if (_tempRunBlind) {
                _engineer.setRunOnET(true);
            }
            if (_delayStart) {
                controlRunTrain(HALT);
            }
            new Thread(_engineer).start();
            
            if ((getBlockAt(0).getState() & OBlock.DARK) !=0) {
                _engineer.setHalt(true);
                firePropertyChange("Command", -1, 0);
            }
            if (_engineer.getRunState()==Warrant.RUNNING) {
                setMovement(MID);
            }
            _delayStart = false;    // script should start when user resumes - no more delay
        }
    }   //end notifyThrottleFound

    protected void startupWarrant() {
        _idxCurrentOrder = 0;
        _idxLastOrder = 0;
        _curSpeedType = Normal;
        // set block state to show our train occupies the block
        BlockOrder bo = getBlockOrderAt(0);
        OBlock b = bo.getBlock();
        b.setValue(_trainName);
        b.setState(b.getState() | OBlock.RUNNING);
    }

    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        abortWarrant("notifyFailedThrottleRequest address= " + address.toString() + 
                " _runMode= " + MODES[_runMode] + " due to " + reason);
        firePropertyChange("throttleFail", null, reason);
    }

    /**
     * Called from allocateRoute() (only)
     * _orders have been set
     * @return error message
     */
    private String checkInService() {
        String msg = null;
        // Check route is in usable
        for (int i = 0; i < _orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
            if ((block.getState() & OBlock.OUT_OF_SERVICE) != 0) {
                _orders.get(0).getBlock().deAllocate(this);
                msg = Bundle.getMessage("UnableToAllocate", getDisplayName())
                        + Bundle.getMessage("BlockOutOfService", block.getDisplayName());
                break;
            }
        }
        return msg;
    }

    /**
     * Allocate as many blocks as possible from the start of the warrant.
     * Installs listeners for the entire route. Sets this warrant into allocated
     * blocks
     * @param orders list of block orders
     * @return error message, if unable to allocate first block or if any block
     * is OUT_OF_SERVICE
     */
    public String allocateRoute(List<BlockOrder> orders) {
        if (_totalAllocated) {
            return null;
        }
        if (orders == null) {
            _orders = getBlockOrders();
        } else {
            _orders = orders;
        }
        _allocated = false;
        _totalAllocated = true;
        String msg = checkInService();
        if (msg!=null) {
            _totalAllocated = false;
            return msg;
        }
        OBlock block = getBlockAt(0);
            msg = block.allocate(this);
            if (msg!=null) {
            return msg;
        }
        // allocate all possible blocks
        // Only allocate up to a block reserved by another warrant
        allocateFromIndex(1);
        return null;
    }
    private void  allocateFromIndex(int index) {
        boolean stoppingBlockSet = false;
        for (int i = index; i < _orders.size(); i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
             if ((block.getState() & OBlock.OCCUPIED) != 0 && !stoppingBlockSet) {
                 // TODO is this necessary?
                setStoppingBlock(block);
                stoppingBlockSet = true;
                log.info(block.getDisplayName() + " not allocated, but Occupied.");
                _totalAllocated = false;
                if (_runMode==MODE_RUN) {
                    return;                    
                }
             }
             _message = block.allocate(this);
            if (_message != null) {
                _totalAllocated = false;
                return;
            }
            _allocated = true;      // partial allocation
        }
    }

    /**
     * Deallocates blocks from the current BlockOrder list
     */
    public void deAllocate() {
        for (int i = 0; i < _orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
            block.deAllocate(this);
        }
        _allocated = false;
        _totalAllocated = false;
        _routeSet = false;
        _message = null;
        if (log.isDebugEnabled()) {
            log.debug("deallocated Route for warrant \"{}\".", getDisplayName());
        }
//        firePropertyChange("deallocate", Boolean.valueOf(old), Boolean.valueOf(false));
    }
    
    /**
     * Convenience routine to use from Python to start a warrant.
     * @param mode run mode
     */
    public void runWarrant(int mode) {
        setRoute(0,null);
        setRunMode(mode,null,null,null,false);
    }

    /**
     * Set the route paths and turnouts for the warrant. Returns the name of the
     * first block that failed allocation to this warrant. When running with
     * block detection, only the first block must be allocated and have its path
     * set.
     *
     * @param delay - delay in seconds, between setting signals and throwing
     * turnouts
     * @param orders - BlockOrder list of route. If null, use permanent warrant
     * copy.
     * @return message of block that failed allocation to this warrant or null if success
     */
    public String setRoute(int delay, List<BlockOrder> orders) {
        // we assume our train is occupying the first block
        _routeSet = true;
        _message = allocateRoute(orders);
        if (_message != null) {
            _routeSet = false;
            return _message;
        }
        _allocated = true;
        _totalAllocated = true;
        BlockOrder bo = _orders.get(0);
        _message = bo.setPath(this);
        if (_message != null) {
            return _message;
        }
        for (int i = 1; i < _orders.size(); i++) {
            bo = _orders.get(i);
            OBlock block = bo.getBlock();
            if ((block.getState() & OBlock.OCCUPIED) != 0) {
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                _routeSet = false;
                return null; // this seems to be an error return, not a success
            }
            _message = bo.setPath(this);
            if (_message != null) {
                _routeSet = false;
                return null;  // this seems to be an error return, not a success
            }
        }
        return null;
    }   // setRoute

    /**
     * Check start block for occupied for start of run
     * @param mode run mode
     * @return error message, if any
     */
    public String checkStartBlock(int mode) {
        if(log.isDebugEnabled()) log.debug("checkStartBlock for warrant \"{}\".", getDisplayName());
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
        if ((state & OBlock.DARK) != 0 || _tempRunBlind) {
            msg = "BlockDark";
        } else if ((state & OBlock.OCCUPIED) == 0) {
            if (mode==MODE_MANUAL) {
                msg = "warnStartManual";                
            } else {
                msg = "warnStart";                                
            }
        } else {
            // check if tracker is on this train
            TrackerTableAction.stopTrackerIn(block);
        }
        return msg;
    }

    /**
     * Report any occupied blocks in the route
     * @return String
     */
    public String checkRoute() {
        if(log.isDebugEnabled()) log.debug("checkRoute for warrant \"{}\".", getDisplayName());
        String msg =null;
        OBlock startBlock = _orders.get(0).getBlock();
        for (BlockOrder bo : _orders) {
            OBlock block = bo.getBlock();
            if ((block.getState() & OBlock.OCCUPIED) != 0 && !startBlock.equals(block)) {
                msg = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                _totalAllocated = false;
            }
        }
        return msg;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
            return;
        }
        String property = evt.getPropertyName();
        _message = null;
        if (log.isDebugEnabled()) log.debug("propertyChange \"{}\" new= {} source= {} - warrant= {}",
            property, evt.getNewValue(), ((NamedBean) evt.getSource()).getDisplayName(), getDisplayName());

        if (_stoppingSignal != null && _stoppingSignal == evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // signal blocking warrant has changed. Should (MUST) be the next block.
                _stoppingSignal.removePropertyChangeListener(this);
                log.debug("Warrant \"{}\" _stoppingSignal= \"{}\" Cleared.",
                        getDisplayName(), _stoppingSignal.getDisplayName());               
//                _engineer.rampSpeedTo(_curSpeedType);
                _stoppingSignal = null;
                setMovement(END);
                return;
            }
        } else if (property.equals("state") && _stoppingBlock != null && _stoppingBlock.equals(evt.getSource())) {
                // starting block is allocated but not occupied
                if (_delayStart) {  // wait for arrival of train to begin the run 
                    if ((((Number) evt.getNewValue()).intValue() & OBlock.OCCUPIED) != 0) {
                        // train arrived at starting block
                        Warrant w = _stoppingBlock.getWarrant();
                        if (this.equals(w) || w == null) {
                            OBlock tempSave = _stoppingBlock;  // checkStoppingBlock() nulls _stoppingBlock
                            if (clearStoppingBlock()) {
                                OBlock block = getBlockAt(_idxCurrentOrder);
                                block._entryTime = System.currentTimeMillis();
                                if (_runMode == MODE_RUN) {
                                    _message = acquireThrottle(_dccAddress);
                                } else if (_runMode == MODE_MANUAL) {
                                    firePropertyChange("Command", -1, 0);                                    
                                    _delayStart = false;
                                } else {
                                    _delayStart = false;
                                    log.error("StoppingBlock \"{}\" set with mode {}", tempSave.getDisplayName(),  MODES[_runMode]);
                                }
                                tempSave.setValue(_trainName);
                                tempSave.setState(tempSave.getState() | OBlock.RUNNING);
                            }
                        }
                    }
                } else if ((((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                    // normal wait for a train underway but blocked ahead by occupation
                    //  blocking occupation has left the stopping block
                    int idx = getIndexOfBlock(_stoppingBlock, _idxLastOrder);
                    if (idx >= 0) {
                        if (clearStoppingBlock()) {
//                          _engineer.rampSpeedTo(_curSpeedType);
                          setMovement(END);
                      } else {
                          log.error("StoppingBlock not located after index {}. _stoppingBlock {}", _idxLastOrder, _stoppingBlock);
                      }
                    }
                }
            } else if (_delayStart && property.equals("runMode") && ((Number) evt.getNewValue()).intValue()==MODE_NONE)  {
                // Starting block was owned by another warrant for this engine 
                // Engine has arrived and Blocking Warrant has finished
                ((Warrant)evt.getSource()).removePropertyChangeListener(this);
                if (clearStoppingBlock()) {
                    _message = acquireThrottle(_dccAddress);
                }
            } else if (property.equals("state") && _shareTOBlock!=null && _shareTOBlock==evt.getSource()) {
                if ((((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                    checkShareTOBlock();
                }
            }
        if (_message != null) {
            abortWarrant(_message);
        }
    }   //end propertyChange

    /**
     * Called from propertyChange()
     * For the start block a return of true will allow warrant to acquire a throttle and
     * launch an engineer.  return ignored for all other blocks
     */
    private boolean clearStoppingBlock() {
        if (_stoppingBlock==null) {
            return false;
        }
        String msg = _stoppingBlock.allocate(this);
        if (msg==null) {
            int idx = getIndexOfBlock(_stoppingBlock, _idxLastOrder);
            if (idx>=0) {
                msg = _orders.get(idx).setPath(this);                                       
            } else {
                msg = "BlockOrder not found. _idxLastOrder= "+_idxLastOrder;
            }
        }
        if (log.isDebugEnabled()) {
            if (msg==null) {
                log.debug("Warrant \"{}\" _stoppingBlock= \"{}\" Cleared.",
                        getDisplayName(), _stoppingBlock.getDisplayName());               
            } else {
                log.debug("Warrant \"{}\" _stoppingBlock= \"{}\" failed. {}",
                        getDisplayName(), _stoppingBlock, msg);
            }
         }
        if (msg==null && (_runMode==MODE_RUN || _runMode==MODE_MANUAL)) {
            _stoppingBlock.removePropertyChangeListener(this);
            _stoppingBlock = null;
            return true;
        }
        return false;
    }

    /**
     * block (nextBlock) sharing a turnout with _shareTOBlock is already
     * allocated.
     */
    private void checkShareTOBlock() {
        _shareTOBlock.removePropertyChangeListener(this);
        if (log.isDebugEnabled()) log.debug("_shareTOBlock= {} Cleared.", _shareTOBlock.getDisplayName());
        _shareTOBlock = null;                   
        String msg = _orders.get(_idxCurrentOrder+1).setPath(this);                     
        if (msg==null) {
            setMovement(END);
        } else {
            // another block is sharing a turnout. and is set by callback
            log.info("Warrant \"{}\" shares a turnout. {}", getDisplayName(), msg);
        }
    }

    /**
     * Callback from trying to setPath() for this warrant. This warrant's Oblock
     * notices that another warrant has its path set and uses a turnout also
     * used by the current path of this. Rights to the turnout must be
     * negotiated, otherwise warrants will deadlock.
     *
     * @param block of other warrant that has a path set
     */
    protected void setShareTOBlock(OBlock block) {
        _shareTOBlock = block;
        _shareTOBlock.addPropertyChangeListener(this);
        log.info("Warrant \"{}\" sets _shareTOBlock= \"{}\". current block= {}",
                getDisplayName(), _shareTOBlock.getDisplayName(), getBlockAt(_idxCurrentOrder).getDisplayName());
    }

    /**
     * Stopping block only used in MODE_RUN _stoppingBlock is an occupied OBlock
     * preventing the train from continuing the route
     *
     */
    private void setStoppingBlock(OBlock block) {
        if (_runMode != MODE_RUN && _runMode != MODE_MANUAL) {
            return;
        }
        if (_stoppingBlock!=null) {
            if (_stoppingBlock.equals(block)) {
                return;
            }
            int idx1 = getIndexOfBlock(_stoppingBlock, _idxLastOrder);
            int idx2 = getIndexOfBlock(block, _idxLastOrder);
            if (idx2<idx1 && idx2>=0) {
                _stoppingBlock.removePropertyChangeListener(this);
            } else {
                return;
            }
        }
        _stoppingBlock = block;
        _stoppingBlock.addPropertyChangeListener(this);
        log.info("Warrant \"{}\" sets _stoppingBlock= \"{}\". current block= {}",
                getDisplayName(), _stoppingBlock.getDisplayName(), getBlockAt(_idxCurrentOrder).getDisplayName());
    }

    private void setStoppingSignal(NamedBean signal) {
        if (_runMode != MODE_RUN) {
            return;
        }
        if (_stoppingSignal!=null) {
            if (_stoppingSignal.equals(signal)) {
                return;
            }
            _stoppingSignal.removePropertyChangeListener(this);
        }
        _stoppingSignal = signal;
        _stoppingSignal.addPropertyChangeListener(this);
        log.info("Warrant \"{}\" sets _stoppingSignal= \"{}\" at current block {}",
                getDisplayName(), _stoppingSignal.getDisplayName(), getBlockAt(_idxCurrentOrder).getDisplayName());
    }


    /**
     * Check if this is the next block of the train moving under the warrant
     * Learn mode assumes route is set and clear.
     * Run mode update conditions.
     *<p>
     * Must be called on GUI thread.
     * @param block Block in the route is going active.
     */
    protected void goingActive(OBlock block) {
        // error if not on Layout thread
        if (!ThreadingUtil.isLayoutThread()) log.error("invoked on wrong thread", new Exception("traceback"));

        if (_runMode == MODE_NONE) {
            return;
        }
        int oldIndex = _idxCurrentOrder;
        int activeIdx = getIndexOfBlock(block, _idxCurrentOrder);
        if (log.isDebugEnabled()) {
            log.debug("**Block \"{}\" goingActive. activeIdx= {}, _idxCurrentOrder= {}. warrant= {}", 
                    block.getDisplayName(), activeIdx, _idxCurrentOrder, getDisplayName());
        }
        if (activeIdx <= 0) {
            // Not found or starting block, in which case 0 is handled as the _stoppingBlock
            if (activeIdx == 0 && _idxCurrentOrder == 0) {
                getBlockOrderAt(activeIdx).setPath(this);
            }
            return;
        }
        if (activeIdx == _idxCurrentOrder) {
            // Unusual case of current block losing detection, then regaining it.  i.e. dirty track, derail etc.
            // Also, can force train to move into occupied block with "Move into next Block" command.
            // This is an unprotected move.
            if (_engineer != null
                    && (_engineer.getRunState() == WAIT_FOR_CLEAR/* || _engineer.getRunState()==HALT*/)) {
                // Ordinarily block just occupied would be this train, but train is stopped! - must be a rouge entry.
                log.info("Forced move into next Block= {}", block.getDisplayName());
                _engineer.setHalt(false);
            }
        } else if (activeIdx == _idxCurrentOrder + 1) {
            if (_delayStart) {
                log.warn("Rouge entering next Block \"{}\".", block.getDisplayName());
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return;
            }
            if (_engineer != null) {
                // Possible case where ramping down to a stop may have the train pass beyond the block where
                // the ramp was started. e.g. a short block preceeds the _stoppingBlock.
                if ( _engineer.getSpeed() > 0.0f || _engineer.ramping()) {
                    if (_engineer.getRunState() == WAIT_FOR_CLEAR || _engineer.getRunState() == HALT) {
                        _idxCurrentOrder = activeIdx;   //  ramp down still in progress                        
                    } else {
                        _engineer.clearWaitForSync();      // Sync commands if train is faster than ET                                                
                    }
                } else {
                    if (!statusOK(block)) {
                        // after ramping and already stopped, next block may get occupied or 
                        // signal aspect may be set to Stop
                        setStoppingCondition(_idxCurrentOrder+1);
                        return;
                    }
                }
            }  //if (_runMode != MODE_LEARN) { run mode engineer lost }
            // Since we are moving we assume it is our train entering the block
            _idxCurrentOrder = activeIdx;
        } else if (activeIdx > _idxCurrentOrder + 1) {
            if (_runMode == MODE_LEARN) {
                log.error("Block \"{}\" became occupied before block \"{}\". ABORT recording.",
                      block.getDisplayName(), getBlockAt(_idxCurrentOrder + 1).getDisplayName());
                firePropertyChange("abortLearn", Integer.valueOf(activeIdx), Integer.valueOf(_idxCurrentOrder));
                return;
            }
            // if previous blocks are dark, this could be for our train
            for (int idx = _idxCurrentOrder+1; idx < activeIdx; idx++) {
                OBlock preBlock = getBlockAt(idx);                
                if ((preBlock.getState() & OBlock.DARK) == 0 ) {    // not dark
                    setStoppingBlock(block);
                    if (log.isDebugEnabled()) {
                        OBlock curBlock = getBlockAt(_idxCurrentOrder);
                        log.debug("Rouge train entered block \"{}\" ahead of train {} at block \"{}\"!",
                                    block.getDisplayName(), _trainName, curBlock.getDisplayName());
                    }
                    return;
                }
            }
            // previous blocks were checked as DARK above
            if (!statusOK(block)) {
                return;
            }
            // Indicate the previous dark block was entered
            OBlock prevBlock = getBlockAt(activeIdx-1);
            prevBlock._entryTime = System.currentTimeMillis() - 500;    // arbitrarily say
            prevBlock.setValue(_trainName);
            prevBlock.setState(prevBlock.getState() | OBlock.RUNNING);
            if (log.isDebugEnabled()) {
                log.debug("Train leaving DARK block \"{}\" now entering block\"{}\". warrant {}", 
                        prevBlock.getDisplayName(), block.getDisplayName(), getDisplayName());
            }
            firePropertyChange("blockChange", getBlockAt(oldIndex), prevBlock);
            oldIndex = activeIdx-1;
            _idxCurrentOrder = activeIdx;

        } else if (_idxCurrentOrder > 0) {
            log.error("Mystifying ERROR: activeIdx = {},  _idxCurrentOrder = {}!",
                    activeIdx, _idxCurrentOrder);
            return;
        }
        block.setValue(_trainName);
        block.setState(block.getState() | OBlock.RUNNING);
        block._entryTime = System.currentTimeMillis();
        if (_calibrater !=null) {
            _calibrater.calibrateAt(_idxCurrentOrder);                
        }
        // _idxCurrentOrder has been incremented. Warranted train has entered this block. 
        // Do signals, speed etc.
        if (_idxCurrentOrder < _orders.size() - 1) {
            if (_engineer != null) {
                OBlock nextBlock = _orders.get(_idxCurrentOrder+1).getBlock();
                if ((nextBlock.getState() & OBlock.DARK) != 0) {
                    // can't detect next block, use ET
                    _engineer.setRunOnET(true);                
                } else if (!_tempRunBlind) {
                    _engineer.setRunOnET(false);                                    
                }
            }
            setMovement(BEG);

            // attempt to allocate remaining blocks in the route up to next occupation
            for (int i = _idxCurrentOrder + 2; i < _orders.size(); i++) {
                BlockOrder bo = _orders.get(i);
                OBlock b = bo.getBlock();
                if (b.allocate(this) != null) {
                    break;
                }
                if ((b.getState() & OBlock.OCCUPIED) != 0) {
                    break;
                }
            }
        } else if (_idxCurrentOrder == _orders.size() - 1) {
            setMovement(BEG);            
        }

        if (_idxCurrentOrder == activeIdx) {
            // fire notification last so engineer's state can be documented in whatever GUI is listening.
            if (log.isDebugEnabled()) {
                log.debug("end of goingActive. leaving \"{}\" entered \"{}\". warrant {}",
                        getBlockAt(oldIndex).getDisplayName(), block.getDisplayName(), getDisplayName());
            }
            firePropertyChange("blockChange", getBlockAt(oldIndex), block);
        }
        if (_tempRunBlind && _idxCurrentOrder>0) {
            goingInactive(getBlockAt(_idxCurrentOrder-1));            
        }
    }       //end goingActive

    /**
     * Return false if not moving
     */
    private boolean statusOK(OBlock block) {
        if (_engineer != null) {
            if ( (_engineer.getRunState() == WAIT_FOR_CLEAR || _engineer.getRunState() == HALT)) {
                // expected block just occupied for this train, but train is stopped! - must be a rouge entry.
//                log.warn("Engineer waiting at Block \"{}\" on warrant {}", block.getDisplayName(), getDisplayName());
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return false;
            }
            _engineer.clearWaitForSync();      // Sync commands if train is faster than ET
        }
        return true;
    }
    /**
     * @param block Block in the route is going Inactive
     */
    protected void goingInactive(OBlock block) {
        if (_runMode == MODE_NONE) {
            return;
        }

        // error if not on Layout thread
        if (!ThreadingUtil.isLayoutThread()) log.error("invoked on wrong thread", new Exception("traceback"));

        int idx = getIndexOfBlock(block, _idxLastOrder);  // if idx >= 0, it is in this warrant
        if (log.isDebugEnabled()) {
            log.debug("Block \"{}\" goingInactive. idx= {}, _idxCurrentOrder= {}. warrant= {}",
                   block.getDisplayName(), idx, _idxCurrentOrder, getDisplayName());
        }
        if (idx < _idxCurrentOrder) {
            releaseBlock(block, idx);
        } else if (idx == _idxCurrentOrder) {
            // Train not visible if current block goes inactive
            if (_idxCurrentOrder + 1 < _orders.size()) {
                OBlock nextBlock = getBlockAt(_idxCurrentOrder + 1);
                if ((nextBlock.getState() & OBlock.DARK) != 0) {
                    if (_engineer != null) {
                        goingActive(nextBlock); // fake occupancy for dark block
                        releaseBlock(block, idx);
                    } else {
                        if (_runMode == MODE_LEARN) {
                            _idxCurrentOrder++;     // assume train has moved into the dark block
                            firePropertyChange("blockChange", block, nextBlock);
                        } else if (_runMode == MODE_RUN) {
                            controlRunTrain(ABORT);
                        }
                    }
                } else {
                    // train is lost
                    if (log.isDebugEnabled()) {
                        log.debug("block \"{}\" goingInactive. train is lost. warrant {}",
                                block.getDisplayName(), getDisplayName());
                    }
                    firePropertyChange("blockChange", block, null);
                    if (_engineer != null) {
                        _engineer.setHalt(true);
                    } else {
                        controlRunTrain(ABORT);
                    }
                    return;
                }
            } else {
                abortWarrant("Warrant "+getDisplayName()+" at last block "+block.getDisplayName()+
                        " and going inactive!");        // abort this
            }
        }
    }           // end goingInactive

    /**
     * Deallocates all blocks prior to and including block
     */
    private void releaseBlock(OBlock block, int idx) {
        /* Only deallocate block if train will not use the  block again.  Blocks ahead could loop back over
         * blocks previously traversed.  That is, don't disturb re-allocation of blocks ahead.
         * Previous Dark blocks do need deallocation
         */
        _idxLastOrder = idx;
        firePropertyChange("blockRelease", null, block);
        for (int i = idx; i > -1; i--) {
            boolean dealloc = true;
            OBlock prevBlock = getBlockAt(i);
            for (int j = i + 1; j < _orders.size(); j++) {
                if (prevBlock.equals(getBlockAt(j))) {
                    dealloc = false;
                }
            }
            if (dealloc) {
                prevBlock.setValue(null);
                prevBlock.deAllocate(this);
            }
        }
    }

    // utility for setMovement()
    private float getBlockPathLength(BlockOrder blkOrder) {
        float len = blkOrder.getPath().getLengthMm();
        if (len <= 0) {
            //a rampLen guess - half throttle for 7 sec.
            len = _engineer.getDistanceTraveled(0.5f, Normal, 7000);
            // len = 155000.0f*scale
        }
        return len;
    }
    

    /**
     * build map of BlockSpeedInfo's for the route.
     * 
     * Put max speed and time in block's first occurrence after current command index
     */
    protected void getBlockSpeedTimes() {
        _speedInfo =  new ArrayList<BlockSpeedInfo>();
        String blkName = null;
        float firstSpeed = 0.0f;    // used for entrance
        float maxSpeed = 0.0f;
        float lastSpeed = 0.0f;
        long blkTime = 0;
        int firstIdx = 0;
        boolean hasSpeedChange = false;
        for (int i=0; i<_commands.size(); i++) {
            ThrottleSetting ts = _commands.get(i);
            String cmd = ts.getCommand().toUpperCase();
            if (cmd.equals("NOOP")) {
                // make map entry
                blkTime += ts.getTime();
                _speedInfo.add(new BlockSpeedInfo(firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, i));
                if(log.isDebugEnabled()) log.debug("block: {} speeds: entrance= {} max= {} exit= {} time: {}ms. index {} to {}",
                        blkName, firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, i);
                blkName = ts.getBlockName();               
                blkTime = 0;
                firstSpeed = lastSpeed;
                maxSpeed = lastSpeed;
                hasSpeedChange = false;
                firstIdx = i;
            } else {        // collect block info
                blkName = ts.getBlockName();               
                blkTime += ts.getTime();
                if (cmd.equals("SPEED")) {
                    lastSpeed = Float.parseFloat(ts.getValue());
                    if (hasSpeedChange) {
                        if (lastSpeed>maxSpeed) {
                            maxSpeed = lastSpeed;
                        }                        
                    } else {
                        hasSpeedChange = true;
                    }
                }
            }
        }
        _speedInfo.add(new BlockSpeedInfo(firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, _commands.size()-1));
        if(log.isDebugEnabled()) log.debug("block: {} speeds: entrance= {} max= {} exit= {} time: {}ms. index {} to {}",
                blkName, firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, (_commands.size()-1));
    }
    
    private class BlockSpeedInfo {
        float entranceSpeed;
        float maxSpeed;
        float exitSpeed;
        long time;
        int firstIdx;
        int lastIdx;
        BlockSpeedInfo(float ens, float ms, float exs, long t, int fi, int li) {
            entranceSpeed = ens;
            maxSpeed = ms;
            exitSpeed = exs;
            time = t;
            firstIdx = fi;
            lastIdx = li;
        }
        float getEntranceSpeed() {
            return entranceSpeed;
        }
        float getMaxSpeed() {
            return maxSpeed;
        }
        float getExitSpeed() {
            return exitSpeed;
        }
        long getTime() {
            return time;
        }
        int getFirstIndex() {
            return firstIdx;
        }
        int getLastIndex() {
            return lastIdx;
        }
    }

    /**  
     *  Finds speed change type at entrance of a block.
     *  Called by:
     *
     * @return a speed type or null for continue at current type
     */
    private String getPermissibleSpeedAt(BlockOrder bo) {
        OBlock block = bo.getBlock();
        String speedType = bo.getPermissibleEntranceSpeed();
        if (speedType!=null ) {
            if (log.isDebugEnabled()) log.debug("getPermissibleSpeedAt(): \"{}\" Signal speed= {} warrant= {}",
                    block.getDisplayName(), speedType, getDisplayName());                
            return speedType;
        } else {    //  if signal is configured, ignore block
            speedType = block.getBlockSpeed();
            if (speedType.equals("")) {
                speedType = null;
            }                
            if(speedType!=null) {
                if (log.isDebugEnabled()) log.debug("getPermissibleSpeedAt(): \"{}\" Block speed= {} warrant= {}",
                        block.getDisplayName(), speedType, getDisplayName());                
            }
            return speedType;
        }
    }
    
    private String setStoppingCondition(int idxBlkOrder) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlkOrder);
        String speedType = getPermissibleSpeedAt(blkOrder);
        OBlock block = blkOrder.getBlock();
        speedType = getPermissibleSpeedAt(blkOrder);
        if (speedType!=null && speedType.equals(Stop)) {
            // block speed cannot be Stop, so OK to assume signal
            setStoppingSignal(blkOrder.getSignal());
        }
        String blockMsg = block.allocate(this);
        if (blockMsg!=null || (block.getState() & OBlock.OCCUPIED) != 0) {
            speedType = Stop;
            setStoppingBlock(block);
        }           
        return speedType;        
    }

    @Override
    public void dispose() {
        stopWarrant(true);
        super.dispose();
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameWarrant");
    }

    private class CommandDelay extends Thread implements Runnable {
        String nextSpeedType;
        long _startWait = 0;
        int _cmdIndex;
        boolean quit = false;

        CommandDelay(String speedType, long startWait, int cmdIndex) {
            nextSpeedType = speedType;
            if (startWait>0) {
                _startWait = startWait;             
            }
            _cmdIndex = cmdIndex;
            if(log.isDebugEnabled()) log.debug("CommandDelay: will wait {}ms, then Ramp to {}", startWait, speedType);
        }

        public void run() {
            synchronized(this) {
                if (_startWait>0.0) {
                    try {
                        wait(_startWait);
                    } catch (InterruptedException ie) {
                        if(log.isDebugEnabled()) log.debug("CommandDelay interrupt.  Ramp to {} not done.", nextSpeedType);
                        quit = true;
                    }
                }
                if (!quit) {
                    if(log.isDebugEnabled()) log.debug("CommandDelay: after wait of {}ms, did Ramp to {}", 
                            _startWait, nextSpeedType);
                    jmri.util.ThreadingUtil.runOnLayout(() ->{ // move to layout-handling thread
                        _engineer.rampSpeedTo(nextSpeedType);                      
                        if (nextSpeedType.equals(Stop) || nextSpeedType.equals(EStop)) {
                            _engineer.setWaitforClear(true);                    
                        } else {
                            _curSpeedType = nextSpeedType;
                        }
                        _engineer.setCurrentCommandIndex(_cmdIndex);
                    });                    
                }
            }
        }
    }

    /**
     * Called to set the correct speed for the train. Assumes 
     * the train occupies the block of the current block order.
     * Will resume movement is train is stopped.
     * Look for speed restrictions in this block and ahead. 
     * If speed restriction changes are required in this block, set the 
     * appropriate flags in the Engineer. if the change is not immediate
     * determine the proper time delay to change speed.
     * @param position estimated position of train inn the block
     * @return false on errors
     */
    private boolean setMovement(int position) {
        if (_runMode != Warrant.MODE_RUN || _idxCurrentOrder >= _orders.size()-1) {
            return false;
        }
        if (_engineer==null) {
            controlRunTrain(ABORT);
            return false;
        }
        int runState = _engineer.getRunState();
        BlockOrder blkOrder = getBlockOrderAt(_idxCurrentOrder);
        OBlock curBlock = blkOrder.getBlock();
        if(log.isDebugEnabled()) log.debug("setMovement({}) runState= {} speedType= {} in block\"{}\".", 
                position, RUN_STATE[runState], _curSpeedType, curBlock.getDisplayName());
        
        if ((curBlock.getState() & (OBlock.OCCUPIED | OBlock.DARK)) == 0) {
            log.error("Train {} expected in block \"{}\" but block is unoccupied! warrant= {}",
                    getTrainName(), curBlock.getDisplayName(), getDisplayName());
            return false;
        }
        // checking situation for the current block
        String currentType = _curSpeedType;
        String speedType = getPermissibleSpeedAt(blkOrder);
        if (speedType==null) {
            speedType = currentType;
        } else if (speedType.equals(Stop)) {
            setStoppingSignal(blkOrder.getSignal());            
        }
         if (!currentType.equals(speedType)) {
            if (_engineer.secondGreaterThanFirst(speedType, currentType)) {
                log.warn("Train {} moved past required speed of \"{}\" at speed \"{}\" in block \"{}\"! warrant= {}",
                        getTrainName(), speedType, currentType, curBlock.getDisplayName(), getDisplayName());
                _engineer.setSpeedToType(speedType);
                if (speedType.equals(Stop) || speedType.equals(EStop)) {
                    _engineer.setWaitforClear(true);                    
                } else {
                    _curSpeedType = speedType;
                }
            } else {
                if(log.isDebugEnabled()) log.debug("Increasing speed to \"{}\" from \"{}\" in block \"{}\" warrant= {}",
                        speedType, currentType, curBlock.getDisplayName(), getDisplayName());
                if (_delayCommand!=null) {
                    _delayCommand.interrupt();
                }
                if (_noRamp) {
                    _engineer.setSpeedToType(speedType);                    
                } else {
                    // TODO - verify whether safe to do this speedType, in case subsequent blocks have speed restrictions
                    _engineer.rampSpeedTo(speedType);                    
                }
                if (!speedType.equals(Stop) && !speedType.equals(EStop)) {
                    _curSpeedType = speedType;
                }
            }
            return true;
        }
         if (_noRamp) {
             speedType = setStoppingCondition(_idxCurrentOrder+1);
             if (speedType==null) {
                 speedType = _curSpeedType;
             }
             if(log.isDebugEnabled()) log.debug("No Ramp speed change to \"{}\" from \"{}\" in block \"{}\". warrant= {}",
                     speedType, currentType, getBlockAt(_idxCurrentOrder+1).getDisplayName(), getDisplayName());
             _engineer.setSpeedToType(speedType);                 
             return true;
         }
        
        // look ahead for a speed change slower than the current speed
        int idxBlockOrder = _idxCurrentOrder;
        OBlock block = curBlock;
        while (!_engineer.secondGreaterThanFirst(speedType, currentType)  && idxBlockOrder < _orders.size()-1) {
            idxBlockOrder++;
            blkOrder = getBlockOrderAt(idxBlockOrder);
            block = blkOrder.getBlock();
            speedType = getPermissibleSpeedAt(blkOrder);
            if (speedType==null) {
                speedType = currentType;                   
            } else if (speedType.equals(Stop)) {
                // block speed cannot be Stop, so ok to assume signal
                setStoppingSignal(blkOrder.getSignal());
            }
            String blockMsg = block.allocate(this);
            if (blockMsg!=null || (block.getState() & OBlock.OCCUPIED) != 0) {
                speedType = Stop;
                setStoppingBlock(block);
            }           
        }
        if (idxBlockOrder >= _orders.size()-1) {
            if (currentType.equals(speedType)) {
                // no speed changes 
                if (runState==WAIT_FOR_CLEAR) {
                    // if cleared during a halt, _engineer runningState can't change. Need to resume currentType
                    _engineer.rampSpeedTo(currentType);                    
                }
                if(log.isDebugEnabled()) log.debug("No speed changes for runState= {} from {} found after block \"{}\" warrant {}",
                        RUN_STATE[runState], currentType, curBlock.getDisplayName(), getDisplayName());
                return true;    // no speed changes found                
            }
        }
        // if speedType > currentType - make change after entering the block (as was done above)
        if (_engineer.secondGreaterThanFirst(currentType, speedType)) {
            if(log.isDebugEnabled()) log.debug("Increase speedfor runState= {} speed {} to {} after entering block \"{}\" warrant {}",
                    RUN_STATE[runState], currentType, speedType, block.getDisplayName(), getDisplayName());
            return true;    //  increase speed later
            
        }
        
        if(log.isDebugEnabled()) log.debug("Speed decrease to {} from {} needed before entering block \"{}\" warrant {}",
                speedType, currentType, getBlockOrderAt(idxBlockOrder).getBlock().getDisplayName(), getDisplayName());
        // there is a speed change entering block
        // make change before entering the block
        // walk back to check if there is enough room to make the change
        // from the beginning of the block
        float rampLen = 1.0f;
        float availDist = 0.0f;
        BlockSpeedInfo blkSpeedInfo = null;
        while (idxBlockOrder > _idxCurrentOrder && rampLen > availDist) {
            idxBlockOrder--;    // start at block before the block with slower speed type 
            blkOrder = getBlockOrderAt(idxBlockOrder);
            if (idxBlockOrder==_idxCurrentOrder) {
                if (position==MID) {
                    availDist += getBlockPathLength(blkOrder)/2;                                   
                } else if (position==BEG) {
                    availDist += getBlockPathLength(blkOrder);                
                }   // position==END, hardly any track available
            } else {
                availDist += getBlockPathLength(blkOrder);                
            }
            blkSpeedInfo = _speedInfo.get(idxBlockOrder);
            
            float speed = blkSpeedInfo.getEntranceSpeed();
            if (speed<0.0001f) { // first block has 0.0 entrance speed
                speed = blkSpeedInfo.getMaxSpeed();
            }
            rampLen = _engineer.rampLengthForSpeedChange(speed, _curSpeedType, speedType)
                    +blkOrder.getEntranceSpace();
            if(log.isDebugEnabled()) log.debug("availDist= {}, at Block \"{}\" for ramp= {} to speed {} from {} warrant {}", 
                    availDist, getBlockOrderAt(idxBlockOrder).getBlock().getDisplayName(), rampLen, 
                    _engineer.modifySpeed(speed,speedType), speed, getDisplayName());
        }
        if (idxBlockOrder > _idxCurrentOrder) {
            if (runState==WAIT_FOR_CLEAR) {
                // if cleared during a halt, _engineer runningState can't change. Need to resume currentType
                _engineer.rampSpeedTo(currentType);                    
            }
            if(log.isDebugEnabled()) 
                log.debug("Will decrease speed for runState= {} to {} from {} later in block \"{}\", warrant {}",
                        RUN_STATE[runState], speedType, _curSpeedType, blkOrder.getBlock().getDisplayName(), getDisplayName());    
            return true;    // change speed later
        }
        if(log.isDebugEnabled()) log.debug("availDist= {} for rampLen= {} from entrance of block \"{}\" ",
                availDist, rampLen, blkOrder.getBlock().getDisplayName());

        // need to start ramp in the block of idxBlockOrder
        // now refine the point at which the ramp should begin
        // find wait time until starting ramp down
        if (rampLen >= availDist) {
            _engineer.rampSpeedTo(speedType);                
            log.warn("Train {} ramping to speed \"{}\" in block \"{}\", warrant= {}",
                    getTrainName(), speedType, curBlock.getDisplayName(), getDisplayName());
            firePropertyChange("SpeedChange", _idxCurrentOrder-1, _idxCurrentOrder);
            if (speedType.equals(Stop) || speedType.equals(EStop)) {
                _engineer.setWaitforClear(true);                    
            } else {
                _curSpeedType = speedType;
            }
            return true;
        }
        long waitTime = getWaitTime(idxBlockOrder, availDist, blkOrder.getEntranceSpace(), speedType);
        blkSpeedInfo = _speedInfo.get(idxBlockOrder);
        if (waitTime < 300) {
            _engineer.rampSpeedTo(speedType);            
            if (speedType.equals(Stop) || speedType.equals(EStop)) {
                _engineer.setWaitforClear(true);                    
            } else {
                _curSpeedType = speedType;
            }
        } else {
            CommandDelay _delayCommand = new CommandDelay(speedType, waitTime, blkSpeedInfo.getLastIndex());
            _delayCommand.start();            
        }
        return true;
    }

    /**
     * @param idxBlockOrder index of BlockOrder where ramp is started
     * @param availDist distance available for ramp
     * @param distAdj extra space user wants before entering block
     * @return time to wait before starting ramp down
     */
    private long getWaitTime(int idxBlockOrder, float availDist, float distAdj, String endSpeedType) {
        BlockSpeedInfo blkSpeedInfo = _speedInfo.get(idxBlockOrder);
        int startIdx;
        float speed = blkSpeedInfo.getEntranceSpeed();  // unmodified recorded speed
        if (idxBlockOrder==0) {
            startIdx = blkSpeedInfo.getFirstIndex();
//            speed = blkSpeedInfo.getMaxSpeed();
        } else {
            startIdx = blkSpeedInfo.getFirstIndex() + 1;            
//            speed = blkSpeedInfo.getEntranceSpeed();  // unmodified recorded speed
        }
        float waitSpeed = _engineer.modifySpeed(speed, _curSpeedType); // speed while waiting to start ramp
        long waitTime = 0;      // time to wait before starting ramp
        long speedTime = 0;     // time running at a given speed
        boolean hasSpeed = (speed>0.0001f);
        float timeRatio;
        if (Math.abs(speed - waitSpeed) > .0001f) {
            timeRatio = speed / waitSpeed;
        } else {
            timeRatio = 1.0f;
        }
        
        float rampLen = _engineer.rampLengthForSpeedChange(speed, _curSpeedType, endSpeedType)+distAdj;
        float waitDist = 0.0f;      // distance traveled until ramp is started
        int endIdx = blkSpeedInfo.getLastIndex();
        for (int i=startIdx; i<=endIdx; i++) {
            ThrottleSetting ts = _commands.get(i);
            String cmd = ts.getCommand().toUpperCase();
            if (hasSpeed) {
                speedTime = (long)(ts.getTime()*timeRatio);
                float dist = _engineer.getDistanceTraveled(speed, _curSpeedType, speedTime);
                rampLen = _engineer.rampLengthForSpeedChange(speed, _curSpeedType, endSpeedType)+distAdj;
                
                if (rampLen >= availDist - waitDist - dist) {
                    float backupDist = availDist - waitDist;    // overshoot distance, if any                    
                    if (availDist < rampLen) {
                        log.error("(availDist-rampLen) negative!", new Exception("traceback")); 
                    }
                    waitTime = _engineer.getTimeForDistance(waitSpeed, backupDist);
//                    waitTime += (long)(speedTime * (backupDist/dist));
                    waitDist +=  backupDist;
                    break;
                    
                }
                waitDist += dist;
            } 
            waitTime += (long)(ts.getTime()*timeRatio);
            if (cmd.equals("SPEED")) {
                speed = Float.parseFloat(ts.getValue());
                hasSpeed = (speed>0.0001f);
                if (hasSpeed) {
                    waitSpeed = _engineer.modifySpeed(speed, _curSpeedType);
                    if (Math.abs(speed - waitSpeed) > .0001f) {
                        timeRatio = speed / waitSpeed;
                    } else {
                        timeRatio = 1.0f;
                    }
                }
            }
            if(log.isDebugEnabled()) log.debug("getWaitTime: rampLen= {}, waitDist= {}, waitTime= {} waitSpeed= {} -{}",
                    rampLen, waitDist, waitTime, waitSpeed, ts.toString());
        }
        if(log.isDebugEnabled()) log.debug("getWaitTime: waitDist= {}, waitTime= {}, rampLen= {}, ramp start speed= {}",
                waitDist, waitTime, rampLen, waitSpeed);
        return waitTime;
    }
    
    private final static Logger log = LoggerFactory.getLogger(Warrant.class.getName());
}