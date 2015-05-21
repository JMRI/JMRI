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
    private String _trainName;      // User train name for icon
    private String _trainId;        // Roster Id
    private DccLocoAddress _dccAddress;
    private boolean _runBlind;              // don't use block detection
    boolean _debug;

    // transient members
    private List<BlockOrder> _orders;       // temp orders used in run mode
    private LearnThrottleFrame _student;    // need to callback learning throttle in learn mode
    private boolean _tempRunBlind;          // run mode flag
    private boolean _delayStart;            // allows start block unoccupied and wait for train
    private float   _throttleFactor = 0.75f;
    protected List <ThrottleSetting> _commands;   // temp commands used in run mode
    private int     _idxCurrentOrder;       // Index of block at head of train (if running)
    private int     _idxLastOrder;          // Index of block at tail of train just left
    private String  _currentSpeed;      // name of last moving speed, i.e. never "Stop"
    private float   _lookAheadLen;      // estimate of the length needed to make a speed change
    private int     _idxSpeedChange;    // Index of last BlockOrder where slower speed changes have been scheduled
//    private String _exitSpeed;            // name of speed to exit the "protected" block

    private int _runMode;
    private Engineer _engineer;         // thread that runs the train
    private boolean _allocated;         // initial Blocks of _orders have been allocated
    private boolean _totalAllocated;    // All Blocks of _orders have been allocated
    private boolean _routeSet;          // all allocated Blocks of _orders have paths set for route
    private OBlock _stoppingBlock;     // Block allocated to another warrant or a rouge train
    private NamedBean _stoppingSignal;  // Signal stopping train movement
    private OBlock _shareTOBlock;       // Block in another warrant that controls a turnout in this block
    private String _message;            // last message returned from an action
    private Calibrater _calibrater;     // Calibrates throttle speed factor

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
        } else {
            return getBlockOrders();
        }
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
    private int getBlockStateAt(int idx) {

        OBlock b = getBlockAt(idx);
        if (b != null) {
            return b.getState();
        }
        return OBlock.UNKNOWN;
    }

    public List<ThrottleSetting> getThrottleCommands() {
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
     * @param id may be either Roster entry or DCC address
     * @return id is valid
     */
    public boolean setTrainId(String id) {
        _trainId = id;
        if (id == null || id.trim().length() == 0) {
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
            List<RosterEntry> l = Roster.instance().matchingList(null, null, numId, null, null, null, null);
            if (l.size() > 0) {
                try {
                    _dccAddress = l.get(0).getDccLocoAddress();
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                boolean isLong = true;
                if ((index + 1) < id.length()
                        && (id.charAt(index + 1) == 'S' || id.charAt(index + 1) == 's')) {
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

    public DccLocoAddress getDccAddress() {
        return _dccAddress;
    }

    public void setDccAddress(DccLocoAddress address) {
        _dccAddress = address;
        if (address != null && _trainId == null) {
            _trainId = address.toString();
        }
    }

    public boolean getRunBlind() {
        return _runBlind;
    }

    public void setRunBlind(boolean runBlind) {
        _runBlind = runBlind;
    }

    public void setThrottleFactor(float f) {
        _throttleFactor = f;
    }
    public float getThrottleFactor() {
        return _throttleFactor;
    }
    public String setThrottleFactor(String sFactor) {
        float fac = 0.75f;
        try {
            fac = Float.parseFloat(sFactor);
        } catch (NumberFormatException nfe) {
            return Bundle.getMessage("MustBeFloat");
        }
        if (fac > 10 || fac <0.05) {
            return Bundle.getMessage("InvalidFactor", sFactor);                                             
        }
        _throttleFactor = fac;          
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

    protected String getRunningMessage() {
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
                } else {
                    if (_message == null) {
                        return Bundle.getMessage("Idle");
                    } else {
                        return Bundle.getMessage("Idle1", _message);
                    }
                }
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

    public void stopWarrant(boolean abort) {
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
            log.debug("setRunMode(" + mode + ")  _runMode= " + _runMode + " for warrant= " + getDisplayName());
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
        _currentSpeed = Normal;
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
                     address = _dccAddress;
                 }
                 _message = acquireThrottle(address);
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

    private void abortWarrant(String msg) {
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
            log.debug("controlRunTrain= " + idx + " runMode= " + _runMode + " for warrant= " + getDisplayName());
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
            return ret;
        } else {
            synchronized (_engineer) {
                oldIndex = _engineer.getRunState();
                switch (idx) {
                    case HALT:
                        _engineer.setHalt(true);
                        break;
                    case RESUME:
                        _engineer.setHalt(false);
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
//                                enterBlock(b.getState());
                                _engineer.rampSpeedTo(_currentSpeed);
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

        if (_runMode == MODE_LEARN) {
            _student.notifyThrottleFound(throttle);
            startupWarrant();
        } else {
            _engineer = new Engineer(this, throttle);
            if (_tempRunBlind) {
                _engineer.setRunOnET(true);
            }
            startupWarrant();       // need engineer for ramp info
            new Thread(_engineer).start();
            if (_delayStart) {
                controlRunTrain(HALT);
            } else {
                moveIntoNextBlock(MID);     //nextSpeed = checkCurrentBlock(MID);
            }
            _delayStart = false;    // script should start when user resumes - no more delay
            firePropertyChange("runMode", MODE_NONE, Integer.valueOf(_runMode));
        }
    }

    /**
     * Only called in MODE_RUN
     */
    protected void startupWarrant() {
        _idxCurrentOrder = 0;
        _idxLastOrder = 0;
        _currentSpeed = Normal;
        
        if (_engineer!=null) {
        // get an estimate for length needed to ramp speed changes
            _lookAheadLen = _engineer.lookAheadLen();
            _idxSpeedChange = -1;
        }
        
        // set block state to show our train occupies the block
        BlockOrder bo = getBlockOrderAt(0);
        OBlock b = bo.getBlock();
        b.setValue(_trainName);
        b.setState(b.getState() | OBlock.RUNNING);
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
            _message = block.allocate(this);
            if (_message != null) {
                _totalAllocated = false;
                return;
            } else {
                if ((block.getState() & OBlock.OCCUPIED) > 0 && !stoppingBlockSet) {
                    setStoppingBlock(block);
                    stoppingBlockSet = true;
                    log.info(block.getDisplayName() + " allocated, but Occupied.");
                 }
                _allocated = true;      // partial allocation
            }
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
                break;
            }
            _message = bo.setPath(this);
            if (_message != null) {
                _routeSet = false;
                break;
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
        if ((state & OBlock.DARK) != 0) {
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
                    + " for warrant= " + getDisplayName());
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
    private void setStoppingBlock(OBlock block) {
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
                    + " _orders.size()= " + _orders.size()
                    + " for warrant= " + getDisplayName());
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
            if (_engineer != null
                    && (_engineer.getRunState() == WAIT_FOR_CLEAR || _engineer.getRunState() == HALT)) {
                // Ordinarily block just occupied would be this train, but train is stopped! - must be a rouge entry.
                log.warn("Rouge entering next Block " + block.getDisplayName());
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return;
            } else {
                // log.info("Train "+_trainName+" entering Block "+block.getDisplayName()+" on warrant= "+getDisplayName());
                // if we are moving we assume it is our train entering the block
                //  - cannot guarantee it, but what else?
                _idxCurrentOrder = activeIdx;
                block._entryTime = System.currentTimeMillis();
            }
        } else if (activeIdx > _idxCurrentOrder + 1) {
            OBlock nextBlock = getBlockAt(_idxCurrentOrder + 1);
            if ((nextBlock.getState() & OBlock.DARK) != 0 && block.equals(getBlockAt(_idxCurrentOrder + 2))) {
                // passing through a dark block - only one allowed                  
                _idxCurrentOrder = activeIdx;
                if (_debug) {
                    log.debug("firePropertyChange(\"blockChange\", " + getBlockAt(oldIndex).getDisplayName()
                            + ", " + getBlockAt(oldIndex + 1).getDisplayName() + ") for warrant= " + getDisplayName());
                }
                getBlockAt(oldIndex + 1)._entryTime = System.currentTimeMillis();
                firePropertyChange("blockChange", getBlockAt(oldIndex), getBlockAt(oldIndex + 1));
                oldIndex++;
            } else {
                if (_runMode == MODE_LEARN) {
                    log.error("Block " + block.getDisplayName() + " became occupied before block "
                            + getBlockAt(_idxCurrentOrder + 1).getDisplayName() + " ABORT recording.");
                    firePropertyChange("abortLearn", Integer.valueOf(activeIdx), Integer.valueOf(_idxCurrentOrder));
                } else {
                    log.warn("Rouge train ahead of train " + _trainName + " at block \"" + block.getDisplayName() + "\"!");
                }
                return;
            }
        } else if (_idxCurrentOrder > 0) {
            log.error("Mystifying error: activeIdx = " + activeIdx + ",  _idxCurrentOrder = " + _idxCurrentOrder + "!");
            return;
        }
        block.setValue(_trainName);
        block.setState(block.getState() | OBlock.RUNNING);
        // _idxCurrentOrder has been incremented. Warranted train has entered this block. 
        // Do signals, speed etc.
        if (_idxCurrentOrder >= _orders.size() - 1) {
            // must be in destination block, No 'next block' for last BlockOrder
            // Let script finish according to recorded times. No speed changes
            // End of script will deallocate warrant.
            enterBlock(block.getState());
        } else {
            if (_calibrater !=null) {
                _calibrater.calibrateAt(_idxCurrentOrder);                
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
                        + ", " + block.getDisplayName() + ") for warrant= " + getDisplayName());
            }
//            block._entryTime = System.currentTimeMillis();        already done above
            firePropertyChange("blockChange", getBlockAt(oldIndex), block);
        }
    }       //end goingActive

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
                    + " for warrant= " + getDisplayName());
        }
        if (idx < _idxCurrentOrder) {
            _idxLastOrder = idx;
            /* Only deallocate block if train will not use the  block again.  Blocks ahead could loop back over
             * blocks previously traversed.  That is, don't disturb re-allocation of blocks ahead.
             * Previous Dark blocks do need deallocation
             */
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
        } else if (idx == _idxCurrentOrder) {
            // Train not visible if current block goes inactive
            if (_idxCurrentOrder + 1 < _orders.size()) {
                OBlock nextBlock = getBlockAt(_idxCurrentOrder + 1);
                if ((nextBlock.getState() & OBlock.DARK) != 0) {
                    if (_engineer != null) {
                        _idxCurrentOrder++;     // assume train has moved into the dark block
                        _engineer.setRunOnET(true);
                        goingActive(nextBlock); // fake occupancy
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
                                + ", null) for warrant= " + getDisplayName());
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

    private void enterBlock(int state) {
        if (_runMode != Warrant.MODE_RUN) {
            return;
        }
        if (((state & OBlock.DARK) != 0) || _tempRunBlind) {
            _engineer.setRunOnET(true);
        } else if (!_tempRunBlind) {
            _engineer.setRunOnET(false);
        }
        if (_stoppingBlock==null && _stoppingSignal==null && _shareTOBlock==null) {
            if (_engineer!=null) {
                _engineer.setWaitforClear(false);
            }           
        }       
    }

    private void restart() {
        if (_engineer==null) {
            controlRunTrain(ABORT);
            return;
        }
        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder);
        enterBlock(bo.getBlock().getState());
        
        // check signal
        String nextSpeed = getPermissibleSpeedAt(bo);
        // does next block belong to us
        bo = getBlockOrderAt(_idxCurrentOrder+1);
        if (!allocateNextBlock(bo)) {
            nextSpeed = Stop;
        }
        String nextNextSpeed = getPermissibleSpeedAt(bo);
        nextSpeed = _engineer.minSpeed(nextSpeed, _currentSpeed);
        nextSpeed = _engineer.minSpeed(nextSpeed, nextNextSpeed);
        if(_debug) log.debug("restart: at speed= "+nextSpeed+" CurrentSpeed= "+_currentSpeed);
        _engineer.rampSpeedTo(nextSpeed);   
    }
    
    private float getLength(OPath path) {
        float len = path.getLengthIn();
        if (len <= 0) {
            len = _lookAheadLen;      //rampLen;
        }
        return len;
    }

    /**
     * Called from goingActive() when train is confirmed as entering nextBlock
     * Called from controlRunTrain() from "resume" command
     * Looks ahead for a speed change.
     * Notifies Engineer of speed to run
     * Block is at the _idxCurrentOrder
     * @param position - estimate of train's position in block at _idxCurrentOrder
     * @return true if able to move
     */
    private boolean moveIntoNextBlock(int position) {
        if (_runMode != Warrant.MODE_RUN) {
            return true;
        }
        if (_engineer==null) {
            controlRunTrain(ABORT);
            return false;
        }
        BlockOrder blkOrder = getBlockOrderAt(_idxCurrentOrder);
        OBlock curBlock = blkOrder.getBlock();
        // verify we occupy current block
        if ((curBlock.getState() & (OBlock.OCCUPIED | OBlock.DARK))==0) {
            _engineer.setHalt(true);
            // should not happen, but...what if...
            log.error("checkCurrentBlock, block \""+curBlock.getDisplayName()+"\" not occupied! warrant "+getDisplayName());
            return false;
        }
        // An estimate for how far to look ahead for a possible speed change
        float dist;
        float len = getLength(blkOrder.getPath());
        switch (position) {
            case BEG:      // entering a new block
                dist = len;
                enterBlock(curBlock.getState());
                break;
            case MID:      // halted or startup
                dist = len/2;
                break;
            case END:      // stopped for signal or occupancy
                dist = len/10;
                break;
            default:  dist = 0.0f;
        }
        if(_debug) log.debug("moveIntoNextBlock("+position+"): \""+curBlock.getDisplayName()+"\". look ahead distance= "+
                _lookAheadLen+" distance= "+dist+" _idxSpeedChange= "+_idxSpeedChange);

        String nextSpeed = getPermissibleSpeedAt(blkOrder);
        
        // look ahead to next block
        blkOrder = getBlockOrderAt(_idxCurrentOrder+1);
        if (!allocateNextBlock(blkOrder)) {
            // next block occupied. stop before entering
            nextSpeed = Stop;
        }
        
        if (dist<=_lookAheadLen && nextSpeed.equals(Stop)) {
            log.warn("Speed change to "+Stop+" before entering next block! Insufficient ramp space!");                
            _engineer.rampSpeedTo(nextSpeed);
            _idxSpeedChange = _idxCurrentOrder;
            return false;
        }
        if(_debug) log.debug("Current block "+curBlock.getDisplayName()+" change speed from "+
                _currentSpeed+" to "+nextSpeed+" within distance "+dist+" Do it = "+(_idxSpeedChange < _idxCurrentOrder));
        
/*        if (_engineer.secondGreaterThanFirst(_currentSpeed, nextSpeed)) {
            _engineer.rampSpeedTo(nextSpeed);   // increase speed for this block
            return true;           
        }*/
        if (_idxSpeedChange < _idxCurrentOrder) {
            scheduleSpeedChange(dist, _currentSpeed, nextSpeed, _idxCurrentOrder);
        }        
        
        // speed change to exit this block
        String nextNextSpeed = nextSpeed;
        int index = _idxCurrentOrder+1;
        allocateFromIndex(index);        // sets first stopping block found ahead
        
        OBlock nextBlock = blkOrder.getBlock();
        len = getLength(blkOrder.getPath());
        dist += len;    // 
        
        while (dist<_lookAheadLen && index<_orders.size()-1 && !nextSpeed.equals(Stop) ) {
            blkOrder = getBlockOrderAt(index+1);   // speed change in this block
            nextBlock = blkOrder.getBlock();
            len = getLength(blkOrder.getPath());
            
            if (!allocateNextBlock(blkOrder)) {
                // next block occupied. stop before entering nextBlock
                nextNextSpeed = Stop;
            } else {
                nextNextSpeed = getPermissibleSpeedAt(blkOrder);                
            }
            
            // nextNextSpeed should not be greater than nextSpeed from last setting  ??
//            nextNextSpeed = _engineer.minSpeed(nextSpeed, nextNextSpeed);
            if(_debug) log.debug("Block "+nextBlock.getDisplayName()+" len= "+len+" change speed from "+
                        nextSpeed+" to "+nextNextSpeed+" within distance "+dist+" Do it = "+(_idxSpeedChange < index));
            if (_idxSpeedChange < index) {
                scheduleSpeedChange(dist, nextSpeed, nextNextSpeed, index);
                nextSpeed = nextNextSpeed;
            }
            dist += len;
            index++;
        }
        // check that following block is large enough for speed changes within it
        blkOrder = getBlockOrderAt(index+1);   // possible speed change in this block
        if (len<_lookAheadLen && blkOrder!=null && index<_orders.size() && !nextSpeed.equals(Stop)) {
            nextBlock = blkOrder.getBlock();
            if (!allocateNextBlock(blkOrder)) {
                // next block occupied. stop before entering nextBlock
                nextNextSpeed = Stop;
            } else {
                // if stop or any other speed change, do before exiting nextBlock
                nextNextSpeed = getPermissibleSpeedAt(blkOrder);
            }
            // add another speed change into last block
//            nextNextSpeed = _engineer.minSpeed(nextSpeed, nextNextSpeed);
            if(_debug) log.debug("Block "+nextBlock.getDisplayName()+" length is "+len+". scheduled speed is "+
                    nextSpeed+" within distance "+dist+" Do it for "+(index-_idxCurrentOrder)+" blocks ahead.");
            scheduleSpeedChange(dist, nextSpeed, nextNextSpeed, index);
            nextSpeed = nextNextSpeed;
        }
        
        if(_debug) log.debug("moveIntoNextBlock End: for speed "+
                nextSpeed+" within distance= "+dist+" from block "+curBlock.getDisplayName()+
                " to block "+nextBlock.getDisplayName()+" on Warrant \""+getDisplayName());       
        
        if (!nextSpeed.equals(Stop) && !nextSpeed.equals(EStop)) {
            _currentSpeed = nextSpeed;          
        }
        return !Stop.equals(nextSpeed);
    }
    
    private void scheduleSpeedChange(float distance, String fromSpeedType, String toSpeedType, int blkOrderIdx) {
        if (toSpeedType.equals(fromSpeedType)) {
            return;
        }
        if(_debug) log.debug("scheduleSpeedChange: from Speed \""+fromSpeedType+"\" to \""+toSpeedType+"\" within distance "+distance);
        _idxSpeedChange = blkOrderIdx;
        if (distance<=_lookAheadLen) {
            log.info("Speed change to "+toSpeedType+" with distance= "+distance+" inadequate.");
            _engineer.rampSpeedTo(toSpeedType);
        } else {
            // The most difficult parameter: the time needed to delay issuing the speed
            // change command.  
            // Method 1: accumulate the elapsed time from the current
            // command to the time when the speed change is completed - the point
            // when the entrance speed (toSpeedType) must be met.
            long time1 = 0L;     // total time until reaching point where scheduled
            // speed change is completed.                
            // Go through commands until command is for a block after nextblock
            // only take elapsed time when train is moving.
            int idx=_engineer.getCurrentCommandIndex();
            boolean firstNoop = _commands.get(idx).getCommand().toUpperCase().equals("NOOP");
            boolean hasSpeed = true;
            boolean speedChanged = false;
            while ( idx<_commands.size()) {
                ThrottleSetting ts = _commands.get(idx);
                if (getIndexOfBlock(ts.getBlockName(), _idxCurrentOrder) > blkOrderIdx) {
                    break;
                }
                if (ts.getCommand().toUpperCase().equals("SPEED")) {
                    hasSpeed = (Float.parseFloat(ts.getValue())>.001f);
                    speedChanged = true;
                }
                if (!firstNoop && hasSpeed) {
//                    if(_debug) log.debug("Add "+ts.getTime()+" ms from "+ts.getBlockName());
                    time1 += ts.getTime();                    
                    firstNoop = false;
                }
                idx++;
            }
            // include noop time into next block
            if (idx<_commands.size() && hasSpeed) {                
                time1 += _commands.get(idx).getTime();                    
            }

            // Method 2: If an unchanging steady speed is known until the entrance point, then
            // elapsed time is a simple calculation.
            long time2 = _engineer.getTimeForDistance(distance, fromSpeedType);
            if(_debug) log.debug("scheduleSpeedChange: Current speed= "+_engineer.getSpeed()+". During Cmds time of "+time1+
                    " ms. speedChanged= "+speedChanged+". TimeForDistance= "+time2+" ms. over distance "+distance);
            
            long ramptime = _engineer.rampTimeForSpeedChange(fromSpeedType, toSpeedType);
            long time;
            if (speedChanged) {
                time = time1;
            } else {
                time = time2;
            }
            if (time < ramptime) {
                _engineer.rampSpeedTo(toSpeedType);
            } else {
                BlockOrder blkOrder = getBlockOrderAt(_idxSpeedChange);
                CommandDelay thread = new CommandDelay(toSpeedType, time-ramptime-blkOrder.getEntranceSpace());
                new Thread(thread).start();                
            }
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
            log.info("allocateNextBlock"+(blockMsg != null ? blockMsg : (block.getDisplayName() + " allocated, but Occupied.")));
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
     *  if next block is allocated, set the path. If there are no
     *  occupation problems get the permitted speed from the signals
     *  Finds speed change in advance of move into the next block.
     *  Called by: 
     *      moveIntoNextBlock to get movement mode
     * @return an "occupied" (Stop or continue) speed change
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
        } else {    //  specified signal speed always takes precedence
            // continue at current speed
            nextSpeed = _currentSpeed;
            String blockspeed = nextBlock.getBlockSpeed();
            if (blockspeed=="" || blockspeed==null) {
                blockspeed = _currentSpeed;
            }
            if (blockspeed.equals(Normal)) {   // Allow Block to reset speed to normal
                nextSpeed = Normal;
            } else {
                nextSpeed = _engineer.minSpeed(nextSpeed, blockspeed);               
                
            }
         }
//        _exitSpeed = exitSpeed;
        if(_debug) log.debug("getPermissibleSpeedAt(): Entrance speed for \""+nextBlock.getDisplayName()+"\"= \""+
                                nextSpeed+"\" for warrant= "+getDisplayName());
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

        CommandDelay(String speedType, long startWait) {
            nextSpeedType = speedType;
            if (startWait>0) {
                _startWait = startWait;             
            }
            if(_debug) log.debug("CommandDelay: Wait "+startWait+" ms, then Ramp to "+speedType);
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
                if(_debug) log.debug("CommandDelay: after wait of "+_startWait+" ms, Ramp to "+nextSpeedType);
                _engineer.rampSpeedTo(nextSpeedType);                      
            }
        }
    }
    
    static Logger log = LoggerFactory.getLogger(Warrant.class.getName());
}
