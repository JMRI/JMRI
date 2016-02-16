package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
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
 * @version $Revision$
 * @author  Pete Cressman Copyright (C) 2009, 2010
 */
public class Warrant extends jmri.implementation.AbstractNamedBean
        implements ThrottleListener, java.beans.PropertyChangeListener {

    public static final String Stop = "Stop";   // NOI18N
    public static final String EStop = "EStop";     // NOI18N
    public static final String Normal = "Normal";   // NOI18N
    public static final String Clear = "Clear";     // NOI18N
    
    private static final long serialVersionUID = 7798395667392538744L;
    // permanent members.
    private ArrayList <BlockOrder> _savedOrders = new ArrayList <BlockOrder>();
    private BlockOrder _viaOrder;
    private BlockOrder _avoidOrder;
    private List<ThrottleSetting> _throttleCommands = new ArrayList<ThrottleSetting>();
    protected String _trainName;      // User train name for icon
    private String _trainId;        // Roster Id
    private DccLocoAddress _dccAddress;
    private boolean _runBlind;              // don't use block detection
    boolean _debug;

    // transient members
    protected List<BlockOrder> _orders;       // temp orders used in run mode
    private LearnThrottleFrame _student;    // need to callback learning throttle in learn mode
    private boolean _tempRunBlind;          // run mode flag
    private boolean _delayStart;            // allows start block unoccupied and wait for train
    protected List <ThrottleSetting> _commands;   // temp commands used in run mode
    protected int     _idxCurrentOrder;       // Index of block at head of train (if running)
    protected int     _idxLastOrder;          // Index of block at tail of train just left
    private String  _curSpeedType;          // name of last moving speed, i.e. never "Stop"
    private int     _idxSpeedChange;        // Index of last BlockOrder where slower speed changes were scheduled
    private HashMap<String, BlockSpeedInfo> _speedTimeMap;          // map max speeds and occupation times of each block in route
//    private String _exitSpeed;            // name of speed to exit the "protected" block

    protected int _runMode;
    protected Engineer _engineer;         // thread that runs the train
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

    // control states
    public static final int HALT = 1;
    public static final int RESUME = 2;
    public static final int ABORT = 3;
    public static final int RETRY = 4;
    protected static final int RUNNING = 5;
    protected static final int SPEED_RESTRICTED = 6;
    protected static final int WAIT_FOR_CLEAR = 7;
    protected static final int WAIT_FOR_SENSOR = 8;
    protected static final int WAIT_FOR_TRAIN = 9;
    protected static final String[] CNTRL_CMDS = {"Stop", "Halt", "Resume", "Abort", "Retry"};
    protected static final String[] RUN_STATE = {"HaltStart", "atHalt", "Resume", "Aborted", "Retry",
        "Running", "RestrictSpeed", "WaitingForClear", "WaitingForSensor", "RunningLate"};

    // Estimated positions of the train in the block it occupies
    static final int BEG    = 1;
    static final int MID    = 2;
    static final int END    = 3;
    /**
     * Create an object with no route defined. The list of BlockOrders is the
     * route from an Origin to a Destination
     */
    public Warrant(String sName, String uName) {
        super(sName.toUpperCase(), uName);
        _idxCurrentOrder = 0;
        _idxLastOrder = 0;
        _orders = new ArrayList<BlockOrder>();
        _runBlind = false;
        _debug = log.isDebugEnabled();
    }

    // _state not used (yet?)
    public int getState() {
        if (_engineer != null) {
            return _engineer.getRunState();
        }
        return 0;
    }

    public void setState(int state) {
    }
    
    /**
     * Return permanently saved BlockOrders
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
     */
    public BlockOrder getfirstOrder() {
        if (_savedOrders.size() == 0) {
            return null;
        }
        return new BlockOrder(_savedOrders.get(0));
    }

    /**
     * Return permanently saved Destination
     */
    public BlockOrder getLastOrder() {
        if (_savedOrders.size() == 0) {
            return null;
        }
        return new BlockOrder(_savedOrders.get(_savedOrders.size() - 1));
    }

    /**
     * Return permanently saved BlockOrder that must be included in the route
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
        for (int i = startIdx; i < _orders.size(); i++) {
            if (_orders.get(i).getBlock().equals(block)) {
                return i;
            }
        }
        return -1;
    }

    /**
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
     */
    protected BlockOrder getBlockOrderAt(int index) {
        if (index >= 0 && index < _orders.size()) {
            return _orders.get(index);
        }
        return null;
    }

    /**
     * Call is only valid when in MODE_LEARN and MODE_RUN
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
        _train = Roster.instance().entryFromTitle(id);
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
     * @return
     */
    public boolean setDccAddress(String id) {
        _train = Roster.instance().entryFromTitle(id);
        if (_train == null) {
            int index = id.indexOf('(');
            String numId;
            if (index >= 0) {
                numId = id.substring(0, index);
            } else {
                numId = id;
            }
            try {
                List<RosterEntry> l = Roster.instance().matchingList(null, null, numId, null, null, null, null);
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

    /**
     * Engineer reports its status
     */
    protected void fireRunStatus(String property, Object old, Object status) {
        firePropertyChange(property, old, status);
    }

    /**
     * ****************************** state queries ****************
     */
    /**
     * Listeners are installed for the route
     */
    public boolean isAllocated() {
        return _allocated;
    }

    public boolean isTotalAllocated() {
        return _totalAllocated;
    }

    /**
     * Turnouts are set for the route
     */
    public boolean hasRouteSet() {
        return _routeSet;
    }

    /**
     * Test if the permanent saved blocks of this warrant are free (unoccupied
     * and unallocated)
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
                    return Bundle.getMessage("locationUnknown", _trainName, getCurrentBlockOrder().getBlock().getDisplayName())+_message;
                }
                if (_message == null) {
                    return Bundle.getMessage("Idle");
                }
                return Bundle.getMessage("Idle1", _message);
            case Warrant.MODE_LEARN:
                return Bundle.getMessage("Learning",
                        getCurrentBlockOrder().getBlock().getDisplayName());
            case Warrant.MODE_RUN:
                if (_engineer == null) {
                    return Bundle.getMessage("engineerGone");
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
                if ((block.getState() | OBlock.OCCUPIED)==0) {
                    return Bundle.getMessage("LostTrain", _trainName, getCurrentBlockOrder().getBlock().getDisplayName());
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
                        if (_engineer != null) {
                            if (cmdIdx == _commands.size() - 1) {
                                _engineer = null;
                                return Bundle.getMessage("endOfScript", _trainName);
                            }
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
        return "ERROR mode= " + _runMode;
    }

    protected void startTracker() {
        TrackerTableAction.markNewTracker(getCurrentBlockOrder().getBlock(), _trainName);
    }

    synchronized public void stopWarrant(boolean abort) {
        _delayStart = false;
        if (_stoppingSignal != null) {
            log.error("signal " + _stoppingSignal.getSystemName());
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
                log.info(getDisplayName() + " Aborted.");
            }
            _engineer.releaseThrottle();
            _engineer = null;
        }
        deAllocate();
        int oldMode = _runMode;
        _runMode = MODE_NONE;
        firePropertyChange("runMode", Integer.valueOf(oldMode), Integer.valueOf(_runMode));
        if (_debug) {
            log.debug("stopWarrant() " + getDisplayName() + ". prev mode= " + oldMode);
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
     */
    public String setRunMode(int mode, DccLocoAddress address,
            LearnThrottleFrame student,
            List<ThrottleSetting> commands, boolean runBlind) {
        if (_debug) {
            log.debug("setRunMode(" + mode + ")  _runMode= " + _runMode + " - warrant= " + getDisplayName());
        }
        _message = null;
        if (_runMode != MODE_NONE) {
            _message = getRunModeMessage();
            log.error(_message);
            return _message;
        }
        _runBlind = runBlind;
        _idxLastOrder = 0;
        _delayStart = false;
        _curSpeedType = Normal;
//        _exitSpeed = "Normal";
        if (mode == MODE_LEARN) {
            // Cannot record if block 0 is not occupied or not dark. If dark, user is responsible for occupation
            if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED | OBlock.DARK)) == 0) {
                _message = Bundle.getMessage("badStart", getDisplayName());
                log.error("Block " + getBlockAt(0).getDisplayName() + ", state= " + getBlockStateAt(0) + " err=" + _message);
                return _message;
            } else if (student == null) {
                _message = Bundle.getMessage("noLearnThrottle", getDisplayName());
                log.error(_message);
                return _message;
            }
            _student = student;
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
            if (!runBlind && (getBlockStateAt(0) & (OBlock.OCCUPIED | OBlock.DARK)) == 0) {
                // continuing with no occupation of starting block
                setStoppingBlock(getBlockAt(0));
                _delayStart = true;
                log.info("Warrant " + getDisplayName() + " train \"" + _trainName
                        + "\" does not occupy block " + _stoppingBlock.getDisplayName());
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
        _tempRunBlind = runBlind;
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
        if (_debug) {
            log.debug("Exit setRunMode()  _runMode= " + _runMode + ", msg= " + _message);
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
        jmri.ThrottleManager tm = InstanceManager.throttleManagerInstance();
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
        if(_debug) log.debug("Throttle at "+address.toString()+" requested for warrant "+getDisplayName());          
        return null;
    }

    protected void abortWarrant(String msg) {
        _delayStart = false;
        log.error("Abort warrant \"" + getDisplayName() + "\" " + msg);
        stopWarrant(true);
    }

    /**
     * Pause and resume auto-running train or abort any allocation state
     * _engineer.abort() calls setRunMode(MODE_NONE,...) which calls deallocate
     * all.
     */
    public boolean controlRunTrain(int idx) {
        if (_debug) {
            log.debug("controlRunTrain= " + idx + " runMode= " + _runMode + " - warrant= " + getDisplayName());
        }
        boolean ret = true;
        int oldIndex = -MODE_MANUAL;
        if (_engineer == null) {
            switch (idx) {
                case HALT:
                case RESUME:
                case RETRY:
                    ret = false;
                    break;
                case ABORT:
                    if (_runMode == Warrant.MODE_LEARN) {
                        // let WarrantFrame do the abort. (WarrantFrame listens for "abortLearn") 
                        firePropertyChange("abortLearn", Integer.valueOf(-MODE_LEARN), Integer.valueOf(_idxCurrentOrder));
                    } else {
                        stopWarrant(true);
                    }
                    break;
            }
        } else {
            synchronized (_engineer) {
                oldIndex = _engineer.getRunState();
                switch (idx) {
                    case HALT:
                        _engineer.setHalt(true);
                        break;
                    case RESUME:
                        _engineer.setHalt(false);
                        restart();
                        ret = moveIntoNextBlock(MID);   //check for clearance ahead                         
                        break;
                    case RETRY: // Force move into next block
                        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder + 1);
                        // if block belongs to this warrant, then move unconditionally into block
                        ret = false;
                        if (bo != null) {
                            OBlock b = bo.getBlock();
                            if (b.allocate(this) == null && (b.getState() & OBlock.OCCUPIED) > 0) {
                                _idxCurrentOrder++;
                                if (b.equals(_stoppingBlock)) {
                                    _stoppingBlock.removePropertyChangeListener(this);
                                    _stoppingBlock = null;
                                }
                                bo.setPath(this);
                                restart();
                                goingActive(b);
                                ret = true;
                            }
                        }
                        break;
                    case ABORT:
                        stopWarrant(true);
                        break;
                }
            }
        }
        if (ret) {
            firePropertyChange("controlChange", Integer.valueOf(oldIndex), Integer.valueOf(idx));
        }
        return ret;
    }

    public void notifyThrottleFound(DccThrottle throttle) {
        if (throttle == null) {
            abortWarrant("notifyThrottleFound: null throttle(?)!");
            firePropertyChange("throttleFail", null, Bundle.getMessage("noThrottle"));
            return;
        }
        if (_debug) {
            log.debug("notifyThrottleFound address= " + throttle.getLocoAddress().toString() + " _runMode= " + _runMode);
        }

        startupWarrant();
        if (_runMode == MODE_LEARN) {
            _student.notifyThrottleFound(throttle);
        } else {
            getBlockSpeedTimes();
            _idxSpeedChange = -1;
            _engineer = new Engineer(this, throttle);
            if (_tempRunBlind) {
                _engineer.setRunOnET(true);
            }
            new Thread(_engineer).start();
            if (_delayStart) {
                controlRunTrain(HALT);
            } else {
                moveIntoNextBlock(MID);     //nextSpeed = checkCurrentBlock(MID);
            }
            _delayStart = false;    // script should start when user resumes - no more delay
            firePropertyChange("runMode", Integer.valueOf(MODE_NONE), Integer.valueOf(_runMode));
        }
    }

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
    /*
     * Engineer sets when ramp is finished
     */
    protected void setSpeedType(String type) {
        if (!type.equals(Stop) && !type.equals(EStop)) {
            _curSpeedType = type;          
            if (_debug) {
                log.debug("setSpeedType to "+_curSpeedType+" for warrant \"" + getDisplayName() + "\".");
            }
        }        
    }

    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        abortWarrant("notifyFailedThrottleRequest address= " + address.toString() + " _runMode= " + _runMode
                + " due to " + reason);
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
     *
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
             if ((block.getState() & OBlock.OCCUPIED) > 0 && !stoppingBlockSet) {
                setStoppingBlock(block);
                stoppingBlockSet = true;
                log.info(block.getDisplayName() + " not allocated, but Occupied.");
                _totalAllocated = false;
                return;
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
        if (_debug) {
            log.debug("deallocated Route for warrant \"" + getDisplayName() + "\".");
        }
//        firePropertyChange("deallocate", Boolean.valueOf(old), Boolean.valueOf(false));
    }
    
    /**
     * Convenience routine to use from Python to start a warrant.
     */
    public void runWarrant(int RunMode) {
        setRoute(0,null);
        setRunMode(RunMode,null,null,null,false);
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
     * @return message of block that failed allocation to this warrant or null
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
            if ((block.getState() & OBlock.OCCUPIED) > 0) {
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                _routeSet = false;
                return null;
            }
            _message = bo.setPath(this);
            if (_message != null) {
                _routeSet = false;
                return null;
            }
        }
//        firePropertyChange("setRoute", Boolean.valueOf(false), Boolean.valueOf(_routeSet));
        if (_message != null) {
            log.info("Paths for route of warrant \"" + getDisplayName() + "\" not set at " + _message);
        }
        return null;
    }   // setRoute

    /**
     * Check start block for occupied for start of run
     */
    public String checkStartBlock(int mode) {
        if(_debug) log.debug("checkStartBlock for warrant \""+getDisplayName()+"\".");
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
            msg = Bundle.getMessage("BlockDark", block.getDisplayName());
        } else if ((state & OBlock.OCCUPIED) == 0) {
            if (mode==MODE_LEARN) {
                msg = "learnStart";                                
            } else{
                msg = "warnStart";                
            }
            msg = Bundle.getMessage(msg, getTrainName(), block.getDisplayName());                
        } else {
            // check if tracker is on this train
            TrackerTableAction.stopTrackerIn(block);
        }
        return msg;
    }

    /**
     * Report any occupied blocks in the route
     */
    public String checkRoute() {
        if(_debug) log.debug("checkRoute for warrant \""+getDisplayName()+"\".");
        String msg =null;
        OBlock startBlock = _orders.get(0).getBlock();
        for (BlockOrder bo : _orders) {
            OBlock block = bo.getBlock();
            if ((block.getState() & OBlock.OCCUPIED) > 0 && !startBlock.equals(block)) {
                msg = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                _totalAllocated = false;
            }
        }
        return msg;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
//            if (_debug) log.debug("propertyChange \""+evt.getPropertyName()+
//                                                "\" old= "+evt.getOldValue()+" new= "+evt.getNewValue());
            return;
        }
        String property = evt.getPropertyName();
        _message = null;
        if (_debug) {
            log.debug("propertyChange \"" + property + "\" new= " + evt.getNewValue()
                    + " source= " + ((NamedBean) evt.getSource()).getDisplayName()
                    + " - warrant= " + getDisplayName());
        }
        if (_stoppingSignal != null && _stoppingSignal == evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // signal blocking warrant has changed. Should (MUST) be the next block.
                _stoppingSignal.removePropertyChangeListener(this);
                _stoppingSignal = null;
                restart();
                moveIntoNextBlock(END);
                return;
            }
        } else if (property.equals("state") && _stoppingBlock != null && _stoppingBlock == evt.getSource()) {
            // starting block is allocated but not occupied
            if (_delayStart) {  // wait for arrival of train to begin the run 
                if ((((Number) evt.getNewValue()).intValue() & OBlock.OCCUPIED) != 0) {
                    // train arrived at starting block
                    Warrant w = _stoppingBlock.getWarrant();
                    if (this.equals(w) || w == null) {
                        OBlock tempSave = _stoppingBlock;  // checkStoppingBlock() nulls _stoppingBlock
                        if (checkStoppingBlock()) {
                            OBlock block = getBlockAt(_idxCurrentOrder);
                            block._entryTime = System.currentTimeMillis();
                            if (_runMode == MODE_RUN) {
                                _message = acquireThrottle(_dccAddress);
                            } else {
                                _delayStart = false;
                                log.error("StoppingBlock " + tempSave.getDisplayName() + " set with mode " + _runMode);
                            }
                            tempSave.setValue(_trainName);
                            tempSave.setState(tempSave.getState() | OBlock.RUNNING);
                            tempSave = null;
                        }
                    } else {
                        // starting block allocated to another warrant for the SAME engine
                        // which has just arrived at the starting block for this warrant
                        // However, we must wait for the other warrant to finish
                        w.addPropertyChangeListener(this);
                    }
                }
            } else if ((((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                // normal wait for a train underway but blocked ahead by occupation
                //  blocking occupation has left the stopping block
                if (checkStoppingBlock()) {
                    restart();
                    moveIntoNextBlock(END);                                     
                }
            }
        } else if (_delayStart && property.equals("runMode") &&
                            ((Number)evt.getNewValue()).intValue()==MODE_NONE)  {
            // Starting block was owned by another warrant for this engine 
            // Engine has arrived and Blocking Warrant has finished
            ((Warrant)evt.getSource()).removePropertyChangeListener(this);
            if (checkStoppingBlock()) {
                _message = acquireThrottle(_dccAddress);
            }
        } else if (property.equals("state") && _shareTOBlock!=null && _shareTOBlock==evt.getSource()) {
            if ((((Number)evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                checkShareTOBlock();
            }
        }
        if (_message != null) {
            abortWarrant(_message);
        }
    }

    /**
     * Called from propertyChange()
     * For the start block a return of true will allow warrant to acquire a throttle and
     * launch an engineer.  return ignored for all other blocks
     * @return
     */
    private boolean checkStoppingBlock() {
        boolean ret = false;
        _stoppingBlock.removePropertyChangeListener(this);
        if (_debug) {
            log.debug("checkStoppingBlock for warrant \"" + getDisplayName() + "\" _stoppingBlock= \""
                    + _stoppingBlock.getDisplayName());
        }
        String msg = _stoppingBlock.allocate(this);
        if (msg==null) {
            int idx = getIndexOfBlock(_stoppingBlock, _idxLastOrder);
            if (idx>=0) {
                msg = _orders.get(idx).setPath(this);                                       
            } else {
                msg = "BlockOrder not found for _stoppingBlock= "+_stoppingBlock.getDisplayName()+" _idxLastOrder= "+_idxLastOrder;
            }
            if (msg==null) {
                if (_runMode==MODE_RUN) {
                    ret = true;
                }
            } else {
                // callback sets _shareTOBlock
                log.info("Warrant \"" + getDisplayName() + "\" shares a turnout. " + msg);
                ret = false;
            }
            if (_debug) {
                log.debug("Warrant \"" + getDisplayName() + "\" _stoppingBlock= \"" + _stoppingBlock.getDisplayName() + "\" Cleared.");
            }
            _stoppingBlock = null;
        } else {
            // allocation failed, continue to wait
            _stoppingBlock.addPropertyChangeListener(this);
            log.warn("StoppingBlock not alllocated in warrant \"" + getDisplayName() + "\". " + msg);
            ret = false;
        }
        if (_debug) {
            log.debug("checkStoppingBlock " + ret + " for warrant \"" + getDisplayName() + ", msg= " + msg);
        }
        return ret;
    }

    /**
     * block (nextBlock) sharing a turnout with _shareTOBlock is already
     * allocated.
     */
    private void checkShareTOBlock() {
        _shareTOBlock.removePropertyChangeListener(this);
        if (_debug) log.debug("_shareTOBlock= "+_shareTOBlock.getDisplayName()+" Cleared.");
        _shareTOBlock = null;                   
        String msg = _orders.get(_idxCurrentOrder+1).setPath(this);                     
        if (msg==null) {
            restart();
            moveIntoNextBlock(END);
        } else {
            // another block is sharing a turnout. and is set by callback
            log.info("Warrant \"" + getDisplayName() + "\" shares a turnout. " + msg);
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
        if (_debug) {
            log.debug("Warrant " + getDisplayName() + " setShareTOBlock for block= "
                    + block.getDisplayName() + ". current block= " + getBlockAt(_idxCurrentOrder).getDisplayName());
        }
        _shareTOBlock = block;
        _shareTOBlock.addPropertyChangeListener(this);
        log.info("Warrant \"" + getDisplayName() + "\" sets _shareTOBlock= \""
                + _shareTOBlock.getDisplayName() + "\". current block= " + getBlockAt(_idxCurrentOrder).getDisplayName());
    }

    /**
     * Stopping block only used in MODE_RUN _stoppingBlock is an occupied OBlock
     * preventing the train from continuing the route
     *
     * @param block
     */
    protected void setStoppingBlock(OBlock block) {
        if (_runMode != MODE_RUN) {
            return;
        }
        _stoppingBlock = block;
        _stoppingBlock.addPropertyChangeListener(this);
        log.info("Warrant \"" + getDisplayName() + "\" sets _stoppingBlock= \""
                + _stoppingBlock.getDisplayName() + "\"");
    }

    /**
     * Block in the route is going active. check if this is the next block of
     * the train moving under the warrant Learn mode assumes route is set and
     * clear
     */
    protected void goingActive(OBlock block) {
        if (_runMode == MODE_NONE) {
            return;
        }
        int oldIndex = _idxCurrentOrder;
        int activeIdx = getIndexOfBlock(block, _idxCurrentOrder);
        if (_debug) {
            log.debug("**Block \"" + block.getDisplayName() + "\" goingActive. activeIdx= "
                    + activeIdx + ", _idxCurrentOrder= " + _idxCurrentOrder
                    + " - warrant= " + getDisplayName());
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
                log.info("Forced move into next Block " + block.getDisplayName());
                _engineer.setHalt(false);
            }
            block._entryTime = System.currentTimeMillis();
        } else if (activeIdx == _idxCurrentOrder + 1) {
            if (_delayStart) {
                log.warn("Rouge entering next Block " + block.getDisplayName());
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return;
            }
//            _engineer.setRunOnET(false);
            if (!statusOK(block)) {
                return;
            }
            // Since we are moving we assume it is our train entering the block
            _idxCurrentOrder = activeIdx;
            block._entryTime = System.currentTimeMillis();
            
        } else if (activeIdx > _idxCurrentOrder + 1) {
            if (_runMode == MODE_LEARN) {
                log.error("Block " + block.getDisplayName() + " became occupied before block "
                        + getBlockAt(_idxCurrentOrder + 1).getDisplayName() + " ABORT recording.");
                firePropertyChange("abortLearn", Integer.valueOf(activeIdx), Integer.valueOf(_idxCurrentOrder));
                return;
            }
            // if previous blocks are dark, this could be for our train
            for (int idx = _idxCurrentOrder+1; idx < activeIdx; idx++) {
                OBlock preBlock = getBlockAt(idx);                
                if ((preBlock.getState() & OBlock.DARK) == 0 ) {
                    if (_debug) {
                        OBlock curBlock = getBlockAt(_idxCurrentOrder);
                        log.debug("Rouge train  at block \"" + block.getDisplayName() + "\" ahead of train "
                                    + _trainName + " at block \"" + curBlock.getDisplayName()+"\"!");
                    }
                    return;
                }
            }
            // previous block was dark
            if (!statusOK(block)) {
                return;
            }
            // Indicate the previous dark block was entered
            OBlock prevBlock = getBlockAt(activeIdx-1);
            prevBlock._entryTime = System.currentTimeMillis();
            prevBlock.setValue(_trainName);
            prevBlock.setState(prevBlock.getState() | OBlock.RUNNING);
            if (_debug) {
                log.debug("firePropertyChange(\"blockChange\", " + prevBlock.getDisplayName()
                        + ", " + block.getDisplayName() + ") - warrant= " + getDisplayName());
            }
            firePropertyChange("blockChange", getBlockAt(oldIndex), prevBlock);
            oldIndex = activeIdx-1;
            _idxCurrentOrder = activeIdx;
            block._entryTime = System.currentTimeMillis();

        } else if (_idxCurrentOrder > 0) {
            log.error("Mystifying error: activeIdx = " + activeIdx + ",  _idxCurrentOrder = " + _idxCurrentOrder + "!");
            return;
        }
        block.setValue(_trainName);
        block.setState(block.getState() | OBlock.RUNNING);
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
            moveIntoNextBlock(BEG);

            // attempt to allocate remaining blocks in the route up to next occupation
            for (int i = _idxCurrentOrder + 2; i < _orders.size(); i++) {
                BlockOrder bo = _orders.get(i);
                OBlock b = bo.getBlock();
                if (b.allocate(this) != null) {
                    break;
                }
                if ((b.getState() & OBlock.OCCUPIED) > 0) {
                    break;
                }
            }
        }

        if (_idxCurrentOrder == activeIdx) {
            // fire notification last so engineer's state can be documented in whatever GUI is listening.
            if (_debug) {
                log.debug("firePropertyChange(\"blockChange\", " + getBlockAt(oldIndex).getDisplayName()
                        + ", " + block.getDisplayName() + ") - warrant= " + getDisplayName());
            }
//            block._entryTime = System.currentTimeMillis();        already done above
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
                log.warn("Engineer waiting at Block " + block.getDisplayName());
                        _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return false;
            }
            _engineer.checkHalt();      // Sync commands if train is faster than ET
        }
        return true;
    }
    /**
     * Block in the route is going Inactive
     */
    protected void goingInactive(OBlock block) {
        if (_runMode == MODE_NONE) {
            return;
        }

        int idx = getIndexOfBlock(block, _idxLastOrder);  // if idx >= 0, it is in this warrant
        if (_debug) {
            log.debug("Block \"" + block.getDisplayName() + "\" goingInactive. idx= "
                    + idx + ", _idxCurrentOrder= " + _idxCurrentOrder
                    + " - warrant= " + getDisplayName());
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
                    if (_debug) {
                        log.debug("firePropertyChange(\"blockChange\", " + block.getDisplayName()
                                + ", null) - warrant= " + getDisplayName());
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
        } else if (idx == _idxCurrentOrder + 1) {
            // Assume Rouge train has left this block
            restart();
            moveIntoNextBlock(END);
        } else {
            // Assume Rouge train has left this block
            block.allocate(this);
        }
    }           // end goingInactive

    /**
     * Deallocates all blocks prior to and including block
     */
    protected void releaseBlock(OBlock block, int idx) {
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


    // called when stopping or signal listeners fire.  Also error condition restarts
    private void restart() {
        if (_engineer==null) {
            controlRunTrain(ABORT);
            return;
        }
        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder);
//        enterBlock(bo.getBlock().getState());
        if (_stoppingBlock==null && _stoppingSignal==null && _shareTOBlock==null) {
            if (_engineer!=null) {
                _engineer.setWaitforClear(false);
            }           
        }       
        
        // check signal
        String nextSpeed = getPermissibleSpeedAt(bo);
        // does next block belong to us
        bo = getBlockOrderAt(_idxCurrentOrder+1);
        String nextNextSpeed;
        if (bo!=null) {
            if (!allocateNextBlock(bo)) {
                nextSpeed = Stop;
            }
            nextNextSpeed = getPermissibleSpeedAt(bo);            
        } else {    // at last block
            nextNextSpeed = _curSpeedType;
        }
        nextSpeed = _engineer.minSpeedType(nextSpeed, _curSpeedType);
        nextSpeed = _engineer.minSpeedType(nextSpeed, nextNextSpeed);
        if(_debug) log.debug("restart: at speed= "+nextSpeed+" CurrentSpeed= "+_curSpeedType);
        _engineer.rampSpeedTo(nextSpeed);   
    }

    // utility for moveIntoNextBlock()
    private float getLength(BlockOrder blkOrder) {
        float len = blkOrder.getPath().getLengthMm();
        if (len <= 0) {
            //a rampLen guess - half throttle for 7 sec.
            len = _engineer.getDistanceTraveled(0.5f, Normal, 7000);
        }
        return len;
    }
    
    // utility for moveIntoNextBlock()
    private String getMinSpeedType(BlockOrder blkOrder, String nextSpeedType) {
        String speedType;
        if (!allocateNextBlock(blkOrder)) {
            // next block occupied. stop before entering
            speedType = Stop;
        } else {
            // speed type for entering next block
            speedType = getPermissibleSpeedAt(blkOrder);
            speedType = _engineer.minSpeedType(nextSpeedType, speedType);            
        }
        if (speedType==null) {
            speedType = _curSpeedType;
        }
        return speedType;
    }

    /**
     *  if next block is allocated, set the path. If there are no
     * occupation problems get the permitted speed from the signals and make
     * the speed change.  Call assumes train is capable of movement.
     * Called from goingActive() when train is confirmed as entering nextBlock
     * Called from controlRunTrain() from "resume" command
     * Looks ahead for a speed change.
     * Notifies Engineer of speed to run
     * Block is at the _idxCurrentOrder
     * @param position - estimate of train's position in block at _idxCurrentOrder
     * @return true if a speed change is requested
     */
    private boolean moveIntoNextBlock(int position) {
        if (_runMode != Warrant.MODE_RUN || _idxCurrentOrder==_orders.size()-1) {
            return true;
        }
        if (_engineer==null) {
            controlRunTrain(ABORT);
            return false;
        }
        BlockOrder blkOrder = getBlockOrderAt(_idxCurrentOrder);
        OBlock curBlock = blkOrder.getBlock();
        // verify we occupy current block
        if ((curBlock.getState() & (OBlock.OCCUPIED | OBlock.DARK))==0 && !_tempRunBlind) {
            _engineer.setHalt(true);        // immediate setspeed = 0
            // should not happen, but...what if...
            log.error("checkCurrentBlock, block \""+curBlock.getDisplayName()+"\" not occupied! warrant "+getDisplayName());
            return true;
        }
        // An estimate for how far to look ahead for a possible speed change
        float availDist;
        float curLen = getLength(blkOrder);
        BlockSpeedInfo blkSpeedInfo = _speedTimeMap.get(curBlock.getDisplayName());
        float maxSpeed = blkSpeedInfo.getMaxSpeed();
        switch (position) {
            case BEG:      // entering a new block
                availDist = curLen;
//                enterBlock(curBlock.getState());
                break;
            case MID:      // halted or startup
                availDist = curLen/2;
                break;
            case END:      // stopped for signal or occupancy
                availDist = curLen/20;
                break;
            default:
                availDist = 0.0f;
        }
        // speed type for entering current block
        String nextSpeedType = getPermissibleSpeedAt(blkOrder);
        // look ahead to next block. Get slowest type compared to current type
        blkOrder = getBlockOrderAt(_idxCurrentOrder+1);
        nextSpeedType = getMinSpeedType(blkOrder, nextSpeedType);
        
        if(_debug) log.debug("moveIntoNextBlock("+position+"): \""+curBlock.getDisplayName()+
                "\" availDist= "+availDist+" _curSpeedType= "+_curSpeedType+". Change to speedType= "+nextSpeedType);

        // need to know exit speed of previous block for an immediate speed change
        // otherwise need to know some(?) speed of this block when speed is to be changed
    
        if (!_curSpeedType.equals(nextSpeedType)) {
            
            if (_engineer.secondGreaterThanFirst(_curSpeedType, nextSpeedType) || position==END) {
                if(_debug) log.debug("Immediate Speed change from "+_curSpeedType+" to "+nextSpeedType+
                        "in \""+curBlock.getDisplayName()+"\"");                
                _engineer.rampSpeedTo(nextSpeedType);   // should be increase speed
                _idxSpeedChange = _idxCurrentOrder;
                return true;
            } else if (_idxSpeedChange < _idxCurrentOrder) {
                // first estimate of distance needed for ramp
                float distAdj =  blkOrder.getEntranceSpace();
                float lookAheadLen = _engineer.rampLengthForSpeedChange(maxSpeed, _curSpeedType, nextSpeedType) + distAdj;
                if(_debug) log.debug("Change speed for \""+blkOrder.getBlock().getDisplayName()+
                        "\" with maxSpeed= "+maxSpeed+",  available distance= "+availDist+", lookAheadLen= "+lookAheadLen);
                
                // Revise lookAheadLen estimate to get a more accurate waitTime, if possible
                float speed = blkSpeedInfo.getEntranceSpeed();
                float waitSpeed = _engineer.modifySpeed(speed, _curSpeedType);
                float timeRatio;
                if (!_curSpeedType.equals(Normal)) {
                    timeRatio = speed/waitSpeed;                            
                } else {
                    timeRatio = 1.0f;                            
                }
                long waitTime = 0;
                long speedTime = 0;     // time running at a given speed
                boolean hasSpeed = (speed>0.0001f);
                float dist = availDist;
                float rampLen = 0.0f;
                int startIdx = blkSpeedInfo.getFirstIndex();
                int endIdx = blkSpeedInfo.getLastIndex();
                for (int i=startIdx; i<endIdx; i++) {
                    ThrottleSetting ts = _commands.get(i);
                    String cmd = ts.getCommand().toUpperCase();
                    if (hasSpeed) {
                        speedTime += ts.getTime()*timeRatio;
                    } else if (dist>=rampLen && !cmd.equals("NOOP")) {
                        waitTime += ts.getTime()*timeRatio;
                    } 
                    if (cmd.equals("SPEED")) {
                        float nextSpeed = Float.parseFloat(ts.getValue());
                        if (hasSpeed) { // get distance for previous speed
                            // available distance left at this speed change point
                            dist -= _engineer.getDistanceTraveled(speed, _curSpeedType, speedTime);
                            rampLen = _engineer.rampLengthForSpeedChange(speed, _curSpeedType, nextSpeedType)+distAdj;
                            if (dist>=rampLen) {
                                lookAheadLen = rampLen;
                                availDist = dist;
                                waitSpeed = _engineer.modifySpeed(speed, _curSpeedType);
                                waitTime += speedTime;
                            }                                
                        }
                        speed = _engineer.modifySpeed(nextSpeed, _curSpeedType);
                        if (!_curSpeedType.equals(Normal)) {
                            timeRatio = nextSpeed/speed;                            
                        } else {
                            timeRatio = 1.0f;                            
                        }
                        speed = nextSpeed;
                        hasSpeed = (speed>0.0001f);
                        speedTime = 0;
                    }
                }
                waitTime += _engineer.getTimeForDistance(waitSpeed, availDist-lookAheadLen);
                
                if(_debug) log.debug("waitSpeed= "+waitSpeed+", waitTime= "+waitTime+",  available distance= "+availDist+",lookAheadLen= "+lookAheadLen);
                if (availDist<=lookAheadLen) {
                    if(_debug) log.debug("!!Immediate Speed decrease!! from "+_curSpeedType+" to "+nextSpeedType+
                            " in \""+curBlock.getDisplayName()+"\"");                
                    _engineer.rampSpeedTo(nextSpeedType);
                    _engineer.setCurrentCommandIndex(blkSpeedInfo.getLastIndex());
                    _idxSpeedChange = _idxCurrentOrder;
                    return true;
                }
                CommandDelay thread = new CommandDelay(nextSpeedType, waitTime, blkSpeedInfo.getLastIndex());
                new Thread(thread).start();                
                _idxSpeedChange = _idxCurrentOrder;
                return true;                
            }            
        } else {
            // do following blocks need help for their speed change
            int index = _idxCurrentOrder+1;
            float len = getLength(blkOrder);
            
            blkSpeedInfo = _speedTimeMap.get(blkOrder.getBlock().getDisplayName());
            maxSpeed = blkSpeedInfo.getMaxSpeed();
            nextSpeedType = getPermissibleSpeedAt(blkOrder);
            BlockOrder nextBlkOrder = getBlockOrderAt(index+1);
            if (nextBlkOrder!=null) {
                nextSpeedType = getMinSpeedType(nextBlkOrder, nextSpeedType);
                
                float distAdj =  nextBlkOrder.getEntranceSpace();
                float lookAheadLen = _engineer.rampLengthForSpeedChange(maxSpeed, _curSpeedType, nextSpeedType)+distAdj;
                
                if (len<lookAheadLen && !_curSpeedType.equals(nextSpeedType)) {
                    availDist += len;
                    if (_engineer.secondGreaterThanFirst(_curSpeedType, nextSpeedType)) {
                        if(_debug) log.debug("Speed increase noted ahead from "+_curSpeedType+" to "+nextSpeedType+
                                " in \""+blkOrder.getBlock().getDisplayName()+"\" from "+curBlock.getDisplayName());                
                        return false;
                    } else if (_idxSpeedChange < _idxCurrentOrder) {
                        // first estimate of distance needed for ramp
                        if(_debug) log.debug("Change speed for \""+nextBlkOrder.getBlock().getDisplayName()+
                                "\" with maxSpeed= "+maxSpeed+",  available distance= "+availDist+", lookAheadLen= "+lookAheadLen);
                        
                        BlockSpeedInfo nextBlkSpeedInfo = _speedTimeMap.get(blkOrder.getBlock().getDisplayName());
                        // Revise lookAheadLen estimate to get a more accurate waitTime, if possible
                        float speed = blkSpeedInfo.getEntranceSpeed();
                        float waitSpeed = _engineer.modifySpeed(speed, _curSpeedType);
                        float timeRatio;
                        if (!_curSpeedType.equals(Normal)) {
                            timeRatio = speed/waitSpeed;                            
                        } else {
                            timeRatio = 1.0f;                            
                        }
                        long waitTime = 0;
                        long speedTime = 0;     // time running at a given speed
                        boolean hasSpeed = (speed>0.0001f);
                        float dist = availDist;
                        float rampLen = 0.0f;
                        int startIdx = blkSpeedInfo.getFirstIndex();
                        int endIdx = blkSpeedInfo.getLastIndex();
                        for (int i=startIdx; i<endIdx; i++) {
                            ThrottleSetting ts = _commands.get(i);
                            String cmd = ts.getCommand().toUpperCase();
                            if (hasSpeed) {
                                speedTime += ts.getTime()*timeRatio;
                            } else if (dist>=rampLen && !cmd.equals("NOOP")) {
                                waitTime += ts.getTime()*timeRatio;
                            } 
                            if (cmd.equals("SPEED")) {
                                float nextSpeed = Float.parseFloat(ts.getValue());
                                if (hasSpeed) { // get distance for previous speed
                                    // available distance left at this speed change point
                                    dist -= _engineer.getDistanceTraveled(speed, _curSpeedType, speedTime);
                                    rampLen = _engineer.rampLengthForSpeedChange(speed, _curSpeedType, nextSpeedType)+distAdj;
                                    if (dist>=rampLen) {
                                        lookAheadLen = rampLen;
                                        availDist = dist;
                                        waitSpeed = _engineer.modifySpeed(speed, _curSpeedType);
                                        waitTime += speedTime;
                                    }                                
                                }
                                speed = _engineer.modifySpeed(nextSpeed, _curSpeedType);
                                if (!_curSpeedType.equals(Normal)) {
                                    timeRatio = nextSpeed/speed;                            
                                } else {
                                    timeRatio = 1.0f;                            
                                }
                                speed = nextSpeed;
                                hasSpeed = (speed>0.0001f);
                                speedTime = 0;
                            }
                        }
                        waitTime += _engineer.getTimeForDistance(waitSpeed, availDist-lookAheadLen);
                        
                        if(_debug) log.debug("waitSpeed= "+waitSpeed+", waitTime= "+waitTime+",  available distance= "+availDist+",lookAheadLen= "+lookAheadLen);
                        if (availDist<=lookAheadLen) {
                            if(_debug) log.debug("!!Immediate Speed decrease!! from "+_curSpeedType+" to "+nextSpeedType+
                                    " in \""+curBlock.getDisplayName()+"\"");                
                            _engineer.rampSpeedTo(nextSpeedType);
                            _engineer.setCurrentCommandIndex(nextBlkSpeedInfo.getLastIndex());
                            _idxSpeedChange = index;
                            return true;
                        }
                        CommandDelay thread = new CommandDelay(nextSpeedType, waitTime, nextBlkSpeedInfo.getLastIndex());
                        new Thread(thread).start();                
                        _idxSpeedChange = index;
                        return true;                
                    }
                }
                
            }
        }
        if(_debug) log.debug("moveIntoNextBlock with no speed change from block \""+
                curBlock.getDisplayName()+"\" - Warrant "+getDisplayName());       
        
        return false;
    }

    /**
     * build map of BlockSpeedInfo's for the route.
     * 
     * @return max speed and time in block's first occurrence after current command index
     */
    protected void getBlockSpeedTimes() {
        _speedTimeMap =  new HashMap<String, BlockSpeedInfo>();
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
                _speedTimeMap.put(blkName, new BlockSpeedInfo(firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, i));
                if(_debug) log.debug("block: "+blkName+" entrance= "+firstSpeed+" max= "+maxSpeed+" exit= "+
                        lastSpeed+" time= "+blkTime+" from "+firstIdx+" to "+i);
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
        _speedTimeMap.put(blkName, new BlockSpeedInfo(firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, _commands.size()-1));
        if(_debug) log.debug("block: "+blkName+" entrance= "+firstSpeed+" max= "+maxSpeed+" exit= "+lastSpeed+
                " time= "+blkTime+" from "+firstIdx+" to "+(_commands.size()-1));
    }
    
    class BlockSpeedInfo {
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
     * If block cannot be allocated, will set a listener on the block.
     * @param block is the next block from some current location
     * @return true if block is allocated to this warrant
     */
    private boolean allocateNextBlock(BlockOrder bo) {
        if (bo == null) {
            log.info("allocateNextBlock: BlockOrder null");
            return false;
        }
        OBlock block = bo.getBlock();
        String blockMsg = block.allocate(this);
        if (blockMsg != null || (block.getState() & OBlock.OCCUPIED) > 0) {
            setStoppingBlock(block);
            log.info("allocateNextBlock "+(blockMsg != null ? blockMsg : (block.getDisplayName() + " allocated, but Occupied.")));
            return false;
        }
        blockMsg = bo.setPath(this);
        if (blockMsg != null) {
            // _shareTOBlock is set by callback from setPath()
            log.info("allocateNextBlock: Warrant \"" + getDisplayName() + "\" shares a turnout. " + blockMsg);
            return false;
        }
        return true;
    }

    /**  
     *  Finds speed change in advance of move into the next block.
     *  Called by:
     *      restart() 
     *      moveIntoNextBlock() to get max speed permitted
     * @return a speed type or null for continue at current type
     */
    private String getPermissibleSpeedAt(BlockOrder bo) {
        OBlock nextBlock = bo.getBlock();
        String nextSpeed = bo.getPermissibleEntranceSpeed();
//        String exitSpeed = bo.getPermissibleExitSpeed();
        if (nextSpeed!=null ) {
            if (nextSpeed.equals(Stop)) {
                _stoppingSignal = bo.getSignal();
                _stoppingSignal.addPropertyChangeListener(this);
            }
        } else {    //  if signal is configured, ignore block
            nextSpeed = nextBlock.getBlockSpeed();
            if (nextSpeed=="") {
                nextSpeed = null;
            }                
         }
        /*      or should we do alternate?
        String blkSpeed = nextBlock.getBlockSpeed();
        if (blkSpeed=="") {
            blkSpeed = null;
        }
        nextSpeed = _engineer.minSpeedType(nextSpeed, blkSpeed);
        */
        if(_debug) log.debug("getPermissibleSpeedAt(): \""+nextBlock.getDisplayName()+"\" Speed= "+
                                nextSpeed+" - warrant= "+getDisplayName());
        return nextSpeed;
    }

    @Override
    public void dispose() {
        deAllocate();
        super.dispose();
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameWarrant");
    }

    private class CommandDelay implements Runnable {
        String nextSpeedType;
        long _startWait = 0;
        int _cmdIndex;

        CommandDelay(String speedType, long startWait, int cmdIndex) {
            nextSpeedType = speedType;
            if (startWait>0) {
                _startWait = startWait;             
            }
            _cmdIndex = cmdIndex;
            if(_debug) log.debug("CommandDelay: will wait "+startWait+" ms, then Ramp to "+speedType);
        }

        public void run() {
            synchronized(this) {
                if (_startWait>0.0) {
                    try {
                        wait(_startWait);
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException "+ie);
                    }
                }
                if(_debug) log.debug("CommandDelay: after wait of "+_startWait+" ms, did Ramp to "+nextSpeedType);
                _engineer.rampSpeedTo(nextSpeedType);                      
                _engineer.setCurrentCommandIndex(_cmdIndex);
            }
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(Warrant.class.getName());
}
