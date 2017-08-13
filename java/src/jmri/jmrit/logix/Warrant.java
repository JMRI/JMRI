package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.ThrottleListener;
import jmri.implementation.SignalSpeedMap;
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
 * Warrant monitors block activity (entrances and exits, signals, rogue
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
            implements  ThrottleListener, java.beans.PropertyChangeListener {

    public static final String Stop = "Stop";   // NOI18N
    public static final String EStop = "EStop";     // NOI18N
    public static final String Normal = "Normal";   // NOI18N
    public static final String Clear = "Clear";     // NOI18N
    
    // permanent members.
    protected List<BlockOrder> _orders;
    private BlockOrder _viaOrder;
    private BlockOrder _avoidOrder;
    private List<ThrottleSetting> _commands = new ArrayList<ThrottleSetting>();
    protected String _trainName;    // User train name for icon
    private SpeedUtil _speedUtil;
    private boolean _runBlind;      // Unable to use block detection, must run on et only
    private boolean _partialAllocate;// only allocate one block at a time for sharing route.
    private boolean _noRamp;        // do not ramp speed changes. make immediate speed change when entering approach block.

    // transient members
    private LearnThrottleFrame _student;    // need to callback learning throttle in learn mode
    private boolean _tempRunBlind;          // run mode flag to allow running on ET only
    private boolean _delayStart;            // allows start block unoccupied and wait for train
    protected int     _idxCurrentOrder;       // Index of block at head of train (if running)
    protected int     _idxLastOrder;          // Index of block at tail of train just left
    private String  _curSpeedType;  // name of last moving speed, i.e. never "Stop".  Used to restore previous speed 
    private ArrayList<BlockSpeedInfo> _speedInfo; // map max speeds and occupation times of each block in route

    protected int _runMode;
    protected Engineer _engineer;       // thread that runs the train
    private CommandDelay _delayCommand; // thread for delayed ramp down
    private boolean _allocated;         // initial Blocks of _orders have been allocated
    private boolean _totalAllocated;    // All Blocks of _orders have been allocated
    private boolean _routeSet;          // all allocated Blocks of _orders have paths set for route
    protected OBlock _stoppingBlock;    // Block allocated to another warrant or a rogue train
    private NamedBean _protectSignal;   // Signal stopping train movement
    private int _idxProtectSignal;
    private boolean _waitForSignal;     // train may not move until false
    private boolean _waitForBlock;      // train may not move until false
    private OBlock _shareTOBlock;       // Block in another warrant that controls a turnout in this block
    protected String _message;          // last message returned from an action

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
        _speedUtil = new SpeedUtil(this);
    }

    @Override
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

    @Override
    public void setState(int state) {
    }
    
    public SpeedUtil getSpeedUtil() {
        return _speedUtil;
    }
    
    public void setSpeedUtil(SpeedUtil su) {
        _speedUtil = su;
        _speedUtil.setWarrant(this);
    }
    
    /**
     * Return BlockOrders
     * @return list of block orders
     */
    public List<BlockOrder> getBlockOrders() {
        return _orders;
    }
    
    /**
     * Add permanently saved BlockOrder
     * @param order block order
     */
    public void addBlockOrder(BlockOrder order) {
        _orders.add(order);
    }

    public void setBlockOrders(List<BlockOrder> orders) {
        _orders = orders;
    }

    /**
     * Return permanently saved Origin
     * @return origin block order
     */
    public BlockOrder getfirstOrder() {
        if (_orders.size() == 0) {
            return null;
        }
        return new BlockOrder(_orders.get(0));
    }

    /**
     * Return permanently saved Destination
     * @return destination block order
     */
    public BlockOrder getLastOrder() {
        int size = _orders.size();
        if (size < 2) {
            return null;
        }
        return new BlockOrder(_orders.get(size - 1));
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
        for (int i = 0; i < _orders.size(); i++) {
            if (_orders.get(i).getBlock().equals(block)) {
                return _orders.get(i).getPathName();
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
     * Find block AFTER startIdx
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
    
    /**
     * Find block BEFORE endIdx
     * @param endIdx index of block order 
     * @param block used by the warrant
     * @return index of block ahead of block order index, -1 if not found
     */
    protected int getIndexOfBlockBefore(int endIdx, OBlock block) {
        for (int i = endIdx; i >= 0; i--) {
            if (_orders.get(i).getBlock().equals(block)) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Find block after startIdx
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

    /**
     * @return throttle commands
     */
    public List<ThrottleSetting> getThrottleCommands() {
        return  _commands;
    }

    public void setThrottleCommands(List<ThrottleSetting> list) {
        _commands = list;
    }

    public void addThrottleCommand(ThrottleSetting ts) {
        _commands.add(ts);
    }
    
    public boolean commandsHaveTrackSpeeds() {
        for (ThrottleSetting ts : _commands) {
            if  (ts.getSpeed() > 0.0f) {
                return true;
            }
        }
        return false;
    }
    
    public void setNoRamp(boolean set) {
        _noRamp = set;
    }
    public void setShareRoute(boolean set) {
        _partialAllocate = set;
    }
    
    public boolean getNoRamp() {
        return _noRamp;
    }
    public boolean getShareRoute() {
        return _partialAllocate;
    }

    public String getTrainName() {
        return _trainName;
    }

    public void setTrainName(String name) {
        _trainName = name;
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
        for (int i = 0; i < _orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
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
        for (int i = 1; i < _orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
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
            default:
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
                if (_speedUtil.getDccAddress() == null) {
                    return Bundle.getMessage("NoLoco");
                }
                if (!(this instanceof SCWarrant) && _commands.size() <= _orders.size()) {
                    return Bundle.getMessage("NoCommands", getDisplayName());
                }
                if (_message == null) {
                    return Bundle.getMessage("Idle");
                }
                if (_idxCurrentOrder!=0 && _idxLastOrder ==_idxCurrentOrder) {
                    return Bundle.getMessage("locationUnknown", _trainName, getCurrentBlockName())+_message;
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
                OBlock block = getCurrentBlockOrder().getBlock();
                if ((block.getState() & (OBlock.OCCUPIED | OBlock.UNDETECTED))==0) {
                    return Bundle.getMessage("LostTrain", _trainName, block.getDisplayName());
                }
                String blockName = block.getDisplayName();
                String speed = _speedUtil.getSpeedMessage(_curSpeedType);
                
                switch (_engineer.getRunState()) {
                    case Warrant.HALT:
                        return Bundle.getMessage("Halted", blockName, cmdIdx);
                        
                    case Warrant.RESUME:
                        return Bundle.getMessage("reStarted", blockName, cmdIdx, speed);

                    case Warrant.RETRY:
                        return Bundle.getMessage("reRetry", blockName, cmdIdx, speed);

                    case Warrant.ABORT:
                        if (cmdIdx == _commands.size() - 1) {
                            _engineer = null;
                            return Bundle.getMessage("endOfScript", _trainName);
                        }
                        return Bundle.getMessage("Aborted", blockName, cmdIdx);

                    case Warrant.WAIT_FOR_CLEAR:
                        return Bundle.getMessage("WaitForClear", _trainName, blockName, (_waitForSignal ?
                                Bundle.getMessage("Signal") : Bundle.getMessage("Occupancy")));

                    case Warrant.WAIT_FOR_TRAIN:
                        int blkIdx = _idxCurrentOrder+1;
                        if (blkIdx>=_orders.size()) {
                            blkIdx = _orders.size()-1;
                        }
                        return Bundle.getMessage("WaitForTrain", cmdIdx,
                                getBlockOrderAt(blkIdx).getBlock().getDisplayName(), speed);
                        
                    case Warrant.WAIT_FOR_SENSOR:
                        return Bundle.getMessage("WaitForSensor",
                                cmdIdx, _engineer.getWaitSensor().getDisplayName(),
                                blockName, speed);
                        
                    case Warrant.RUNNING:
                    case Warrant.SPEED_RESTRICTED:
                        return Bundle.getMessage("WhereRunning", blockName, cmdIdx, speed);

                    default:
                       return _message;
                }

            case Warrant.MODE_MANUAL:
                BlockOrder bo = getCurrentBlockOrder();
                if (bo != null) {
                    return Bundle.getMessage("ManualRunning", bo.getBlock().getDisplayName());
                }
                return Bundle.getMessage("ManualRun");
                
            default:
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
        if (_protectSignal != null) {
            _protectSignal.removePropertyChangeListener(this);
            _protectSignal = null;
            _idxProtectSignal = -1;
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
            _speedUtil.stopRun(!abort); // don't write speed profile measurements
            _engineer.stopRun(abort);   // release throttle
            _engineer = null;
        }
        deAllocate();
        int oldMode = _runMode;
        _runMode = MODE_NONE;
        if (abort) {
            firePropertyChange("runMode", Integer.valueOf(oldMode), Integer.valueOf(MODE_ABORT));            
        } else {
            firePropertyChange("runMode", Integer.valueOf(oldMode), Integer.valueOf(_runMode));            
        }
        if (log.isDebugEnabled()) {
            log.debug("Warrant \"{}\" terminated {}.", getDisplayName(), (abort==true?"-aborted":"normally"));
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
        _waitForSignal = false;
        _waitForBlock = false;
        if (address != null) {
            _speedUtil.setDccAddress(address);
        }
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
            // set mode before notifyThrottleFound is called
            _runMode = mode;
        } else if (mode == MODE_RUN || mode == MODE_MANUAL) {
            if (commands != null && commands.size() > _orders.size()) {
                _commands = commands;
            }
            // set mode before setStoppingBlock and callback to notifyThrottleFound are called
            _idxCurrentOrder = 0;
            _runMode = mode;
            // Delayed start is OK if block 0 is not occupied. Note can't delay start if block is dark
            if (!runBlind) {
                int state = getBlockStateAt(0);
                if ((state & (OBlock.OCCUPIED | OBlock.UNDETECTED)) == 0) {
                    // continuing with no occupation of starting block
                    setStoppingBlock(getBlockAt(0));
                    _delayStart = true;                    
                }
            }
        } else {
            stopWarrant(true);
        }
        getBlockAt(0)._entryTime = System.currentTimeMillis();
        if (_runBlind) {
            _tempRunBlind = _runBlind;            
        } else {
            _tempRunBlind = runBlind;            
        }
        if (!_delayStart) {
            if (mode!=MODE_MANUAL) {
                _message = acquireThrottle();
             } else {
                startupWarrant();       // assuming manual operator will go to start block              
             }
        }
        firePropertyChange("runMode", Integer.valueOf(MODE_NONE), Integer.valueOf(_runMode));
        if (log.isDebugEnabled()) {
            log.debug("Exit setRunMode()  _runMode= {}, msg= {}", MODES[_runMode], _message);
        }
        return _message;
    }   // end setRunMode
    
    /************** start warrant run - end of create/edit/setup methods ******************/

    /**
     * @return error message if any
     */
    protected String acquireThrottle() {
        String msg = null;
        DccLocoAddress dccAddress = _speedUtil.getDccAddress();
        if(log.isDebugEnabled()) log.debug("acquireThrottle request at {} for warrant {}",
                dccAddress, getDisplayName());
        if (dccAddress == null)  {
            msg = Bundle.getMessage("NoAddress", getDisplayName());
        } else {
            jmri.ThrottleManager tm = InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
            if (tm==null) {
                msg = Bundle.getMessage("noThrottle", _speedUtil.getDccAddress().getNumber());
            } else {
                if (!tm.requestThrottle(dccAddress.getNumber(), dccAddress.isLongAddress(), this)) {
                    return Bundle.getMessage("trainInUse", dccAddress.getNumber());
                }           
            }
        }
        if (msg!=null) {
            abortWarrant(msg);
            firePropertyChange("throttleFail", null, msg);          
            return msg;
        }
        return null;
    }

    @Override
    public void notifyThrottleFound(DccThrottle throttle) {
        if (throttle == null) {
            String msg = Bundle.getMessage("noThrottle", getDisplayName());
            abortWarrant(msg);
            firePropertyChange("throttleFail", null, msg);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("notifyThrottleFound for address= {}, class= {}, warrant {}", 
                    throttle.getLocoAddress().toString(), throttle.getClass().getName(), getDisplayName());
        }
        _speedUtil.setThrottle(throttle);
        startupWarrant();
        runWarrant(throttle);
    }   //end notifyThrottleFound

    @Override
    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        abortWarrant( Bundle.getMessage("noThrottle", (reason +" "+ (address!=null?address.getNumber():getDisplayName()))));
        fireRunStatus("throttleFail", null, reason);
    }

    @Override
    public void notifyStealThrottleRequired(DccLocoAddress address){
        // this is an automatically stealing impelementation.
        InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
    }
    
    protected void releaseThrottle(DccThrottle throttle) {
        if (throttle != null) {
            jmri.ThrottleManager tm = InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
            if (tm != null) {
                tm.releaseThrottle(throttle, this);
            } else {
                log.error(Bundle.getMessage("noThrottle", throttle.getLocoAddress()));
            }
        }
    }

    protected void abortWarrant(String msg) {
        log.error("Abort warrant \"{}\" - {} ", getDisplayName(), msg);
        stopWarrant(true);
    }

    /**
     * Pause and resume auto-running train or abort any allocation state
     * User issued overriding commands during run time of warrant
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
                        firePropertyChange("controlChange", Integer.valueOf(MODE_RUN), Integer.valueOf(ABORT));
                        stopWarrant(true);
                    }
                    break;
                default:
            }
            return ret;
        } 
        int runState = _engineer.getRunState();
        synchronized (_engineer) {
            switch (idx) {
                case HALT:
                    cancelRamp();
                    _engineer.setHalt(true);    // does rampSpeedTo(Stop);
                    ret = true;
                    break;
                case RESUME:
                    _engineer.setHalt(false);   // user insists train not halted.
                    BlockOrder bo = getBlockOrderAt(_idxCurrentOrder);
                    OBlock block = bo.getBlock();
                    if ((block.getState() & (OBlock.OCCUPIED | OBlock.UNDETECTED)) != 0) {
                        ret = setMovement(MID);
                    } // else? train must be lost. maybe fall through to retry?
                    break;
                case RETRY: // Force move into next block, which should be seen as rogue occupied
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
                    cancelRamp();
                    _engineer.setStop(false);
                    ret = true;
                    break;
                case ESTOP:
                    cancelRamp();
                    _engineer.setStop(true);    // E-stop
                    ret = true;
                    break;
                default:
            }
        }
        if (ret) {
            firePropertyChange("controlChange", Integer.valueOf(runState), Integer.valueOf(idx));
        }
        return ret;
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

    protected void runWarrant(DccThrottle throttle) {
        if (_runMode == MODE_LEARN) {
            synchronized(this) {
               _student.notifyThrottleFound(throttle);
            }
        } else {
            _engineer = new Engineer(this, throttle);
            if (_tempRunBlind) {
                _engineer.setRunOnET(true);
            }
            if (_delayStart) {
                controlRunTrain(HALT);
            }
            // if there may be speed changes due to signals or rogue occupation. 
            if (_noRamp) { // make immediate speed changes
                _speedInfo = null;
            } else { // make smooth speed changes by ramping
                getBlockSpeedTimes();                
            }
            new Thread(_engineer).start();
            
            if ((getBlockAt(0).getState() & OBlock.UNDETECTED) !=0) {
                // user must explicitly start train (resume) in a dark block
                _engineer.setHalt(true); 
                firePropertyChange("Command", -1, 0);
            }
            if (_engineer.getRunState()==Warrant.RUNNING) {
                setMovement(MID);
            }
            _delayStart = false;    // script should start when user resumes - no more delay
        }
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
        _totalAllocated = false;
        if (orders != null) {
            _orders = orders;
        }
        _allocated = false;
        String msg = checkInService();
        if (msg!=null) {
            return msg;
        }
        OBlock block = getBlockAt(0);
        msg = block.allocate(this);
        if (msg!=null) {
            _message = msg;
            return msg;
        }
            
        _allocated = true;      // partial allocation
        // don't return message, if any, OK to have warrant in effect from origin block
        if (!_partialAllocate) {
            allocateFromIndex(1);                        
        }
        return null;
    }
    
    private String allocateFromIndex(int index) {
        int limit;
        if (_partialAllocate) {
            limit = index+1;
        } else {
            limit = _orders.size();
        }
        for (int i = index; i < limit; i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
            _message = block.allocate(this);
            // loop back routes may enter a block a second time
            // Do not make current block a stopping block
            OBlock currentBlock = getBlockOrderAt(_idxCurrentOrder).getBlock();
            if (!currentBlock.equals(block)) {
                if (!block.isAllocatedTo(this) || 
                        ((block.getState() & OBlock.OCCUPIED) != 0)) {
                    setStoppingBlock(block);
                    _totalAllocated = false;
                    return _message;                   
                }                
            }
        }
        if (!_partialAllocate) {
            _totalAllocated = true;            
        }
        _message = Bundle.getMessage("completeAllocate", getDisplayName());
        return null;
    }

    /**
     * Deallocates blocks from the current BlockOrder list
     */
    public void deAllocate() {
        for (int i = 0; i < _orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
            if (block.isAllocatedTo(this)) {
                block.deAllocate(this);
            }
        }
        _allocated = false;
        _totalAllocated = false;
        _routeSet = false;
        _message = null;
        if (log.isDebugEnabled()) {
            log.debug("deallocated Route for warrant \"{}\".", getDisplayName());
        }
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
     * @param show - value==1 will ignore _partialAllocate (to show route only)
     * parm name delay of turnout steting deprecated
     * @param orders - BlockOrder list of route. If null, use permanent warrant
     * copy.
     * @return message of block that failed allocation to this warrant or null if success
     */
    public String setRoute(int show, List<BlockOrder> orders) {
        // we assume our train is occupying the first block
        _routeSet = false;
        _message = allocateRoute(orders);
        if (_message != null) {
            return _message;
        }
        _allocated = true;
        BlockOrder bo = _orders.get(0);
        _message = bo.setPath(this);
        if (_message != null) {
            return _message;
        }
        if (!_partialAllocate || show == 1) {
            for (int i = 1; i < _orders.size(); i++) {
                bo = _orders.get(i);
                OBlock block = bo.getBlock();
                _message = block.allocate(this);
                if (_message != null) {
                    return null;  // OK. warning status is posted with _message
                }
                if ((block.getState() & OBlock.OCCUPIED) != 0) {
                    _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                    return null; // OK. warning status is posted with _message
                }
                _message = bo.setPath(this);
                if (_message != null) {
                    return null;  // OK. warning status is posted with _message
                }
            }
        }
        _routeSet = true;
        _message = Bundle.getMessage("pathsSet", getDisplayName());
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
        if ((state & OBlock.UNDETECTED) != 0 || _tempRunBlind) {
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
            }
        }
        return msg;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
            return;
        }
        String property = evt.getPropertyName();
        String msg = null;
        if (log.isDebugEnabled()) log.debug("propertyChange \"{}\" new= {} source= {} - warrant= {}",
            property, evt.getNewValue(), ((NamedBean) evt.getSource()).getDisplayName(), getDisplayName());

        if (_protectSignal != null && _protectSignal == evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // signal controlling warrant has changed.
                if (readStoppingSignal()) {
                    setMovement(MID);                    
                }
            }
        } else if (property.equals("state")) {
            if (_stoppingBlock != null && _stoppingBlock.equals(evt.getSource())) {
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
                                    msg = acquireThrottle();
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
                          setMovement(END);
                      }
                    }
                }
            } else if (_shareTOBlock!=null && _shareTOBlock==evt.getSource()) {
                if ((((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                    checkShareTOBlock();
                }
            }
        } else if (_delayStart && property.equals("runMode") && ((Number) evt.getNewValue()).intValue()==MODE_NONE)  {
            // Starting block was owned by another warrant for this engine 
            // Engine has arrived and Blocking Warrant has finished
            ((Warrant)evt.getSource()).removePropertyChangeListener(this);
            if (clearStoppingBlock()) {
                msg = acquireThrottle();
            }
        }
        if (msg != null) {
            abortWarrant(msg);
            _message = msg;
        }
    }   //end propertyChange

    private boolean readStoppingSignal() {
        String speed;
        if (_protectSignal instanceof SignalHead) {
            SignalHead head = (SignalHead)_protectSignal;
            int appearance = head.getAppearance();
            speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAppearanceSpeed(head.getAppearanceName(appearance));
            if (log.isDebugEnabled()) log.debug("SignalHead {} sets appearance to {} - warrant= {}",
                    _protectSignal.getDisplayName(), speed, getDisplayName());
        } else {
            SignalMast mast = (SignalMast)_protectSignal;
            String aspect = mast.getAspect();
            speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAspectSpeed(aspect, mast.getSignalSystem());
            if (log.isDebugEnabled()) log.debug("SignalMast {} sets aspect to {} - warrant= {}",
                    _protectSignal.getDisplayName(), speed, getDisplayName());
        }
        if (speed == null) {
            return false;   // dark or no specified speed
        } else if (speed.equals(Stop)) {
            _waitForSignal = true;
        } else {
            if (!_waitForBlock && _engineer !=null) {
                _engineer.setWaitforClear(false);
                firePropertyChange("SpeedChange", _idxCurrentOrder, _idxCurrentOrder);
            }
            _waitForSignal = false;
        }
        return true;
    }

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
                log.debug("Warrant \"{}\" Cleared _stoppingBlock= \"{}\".",
                        getDisplayName(), _stoppingBlock.getDisplayName());               
            } else {
                log.debug("Warrant \"{}\" did not clear _stoppingBlock= \"{}\". {}",
                        getDisplayName(), _stoppingBlock, msg);
            }
         }
        if (msg==null) {
            _stoppingBlock.removePropertyChangeListener(this);
            _stoppingBlock = null;
            _waitForBlock = false;
            if (!_waitForSignal && _engineer!=null) {
                _engineer.setWaitforClear(false);
            }
            allocateFromIndex(_idxCurrentOrder + 1);
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
        if (block == null) {
            return;
        }
        if (_stoppingBlock!=null) {
            if (_stoppingBlock.equals(block)) {
                return;
            }
            
            int idx1 = getIndexOfBlock(_stoppingBlock, _idxCurrentOrder);
            int idx2 = getIndexOfBlock(block, _idxCurrentOrder);
            if ((idx2 < idx1 && idx2 >= 0) || idx1 < 0) {
                _stoppingBlock.removePropertyChangeListener(this);
            } else {
                return;
            }
        }
        OBlock prevBlk = _stoppingBlock;
        _stoppingBlock = block;
        _stoppingBlock.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) {
            String msg = "Warrant \"{}\" sets _stoppingBlock= \"{}\"";
            if (prevBlk != null) {
                msg = msg + ", removes \"{}\"";
            }
            log.debug( msg, getDisplayName(), _stoppingBlock.getDisplayName(),
                    (prevBlk == null ? "" : prevBlk.getDisplayName()));
        }
    }

    private void setStoppingSignal(int idx) {
        BlockOrder blkOrder = getBlockOrderAt(idx);
        NamedBean signal = blkOrder.getSignal();

        if (_protectSignal != null) {
            if (_protectSignal.equals(signal)) {
                // Must be the route coming back to the same block
                if (_idxProtectSignal < idx && idx >= 0) {
                    _idxProtectSignal = idx;
                }
                return;
            } else {
                if (_idxProtectSignal <= _idxCurrentOrder && !_waitForSignal) {
                    _protectSignal.removePropertyChangeListener(this);
                } else {
                    return;
                }
            }
        }

        NamedBean prevSignal = _protectSignal;
        if (signal != null) {
            _protectSignal = signal;
            _idxProtectSignal = idx;
            _protectSignal.addPropertyChangeListener(this);
        }
        if (log.isDebugEnabled()) {
            if (signal == null && prevSignal == null ) {
                return;
            }
            String msg = "Signal at block \"{}\" Warrant \"{}\"";
            if (signal != null) {
                msg = msg + " sets _protectSignal= \""+_protectSignal.getDisplayName()+"\"";
            }
            if (prevSignal != null) {
                msg = msg + ", removes signal= \""+prevSignal.getDisplayName()+"\"";
            }
            log.debug( msg,  blkOrder.getBlock().getDisplayName(), getDisplayName());
        }
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
                    && (_engineer.getRunState() != WAIT_FOR_CLEAR && _engineer.getRunState()!=HALT)) {
                // Ordinarily block just occupied would be this train, but train is stopped! - could be user's retry.
                log.info("Train {} regained detection at Block= {}", getTrainName(), block.getDisplayName());
                _engineer.rampSpeedTo(_curSpeedType);
            }
        } else if (activeIdx == _idxCurrentOrder + 1) {
            if (_delayStart) {
                log.warn("Rogue entered Block \"{}\" ahead of {}.", block.getDisplayName(), getTrainName());
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return;
            }
            // be sure path is set for train in this block
            String msg = getBlockOrderAt(activeIdx).setPath(this);
            if (msg !=null) {
                log.error("goingActive setPath fails: {}",  msg);
            }
            if (_engineer != null && _speedUtil.getSpeedSetting() <= 0.000001f) {
                // Already stopped, cannot have just entered block 
                // Rogue may have just entered or signal aspect just set to Stop
                setStoppingBlock(block);
                _waitForBlock = true;
                _engineer.setWaitforClear(true);                       
                return;                        
            }
            // Since we are moving we assume it is our train entering the block
            // continue on.
        } else if (activeIdx > _idxCurrentOrder + 1) {
            if (_runMode == MODE_LEARN) {
                log.error("Block \"{}\" became occupied before block \"{}\". ABORT recording.",
                      block.getDisplayName(), getBlockAt(_idxCurrentOrder + 1).getDisplayName());
                firePropertyChange("abortLearn", Integer.valueOf(activeIdx), Integer.valueOf(_idxCurrentOrder));
                return;
            }
            // if previous blocks are dark, this could be for our train
            // check from current block to this just activated block
            for (int idx = _idxCurrentOrder+1; idx < activeIdx; idx++) {
                OBlock preBlock = getBlockAt(idx);                
                if ((preBlock.getState() & OBlock.UNDETECTED) == 0 ) {
                    // not dark, therefore not our train
                    setStoppingBlock(block);
                    if (log.isDebugEnabled()) {
                        OBlock curBlock = getBlockAt(_idxCurrentOrder);
                        log.debug("Rogue train entered block \"{}\" ahead of train {} currently in block \"{}\"!",
                                    block.getDisplayName(), _trainName, curBlock.getDisplayName());
                    }
                    return;
                }
                // we assume this is our train entering block
            }
            // previous blocks were checked as UNDETECTED above
            if (!statusOK(block)) {
                return;
            }
            // Indicate the previous dark block was entered
            OBlock prevBlock = getBlockAt(activeIdx-1);
            prevBlock._entryTime = System.currentTimeMillis() - 500;    // arbitrarily say
            prevBlock.setValue(_trainName);
            prevBlock.setState(prevBlock.getState() | OBlock.RUNNING);
            if (log.isDebugEnabled()) {
                log.debug("Train leaving UNDETECTED block \"{}\" now entering block\"{}\". warrant {}", 
                        prevBlock.getDisplayName(), block.getDisplayName(), getDisplayName());
            }
            // Since we are moving we assume it is our train entering the block
            // continue on.
        } else if (_idxCurrentOrder > activeIdx) {
            log.error("Mystifying ERROR goingActive: activeIdx = {},  _idxCurrentOrder = {}!",
                    activeIdx, _idxCurrentOrder);
            return;
        }
        if (_engineer != null) {
            _engineer.clearWaitForSync();
        }
        block.setValue(_trainName);
        block.setState(block.getState() | OBlock.RUNNING);
        block._entryTime = System.currentTimeMillis();
        if (_runMode == MODE_RUN) {
            _speedUtil.enteredBlock(_idxCurrentOrder, activeIdx);               
        }
        _idxLastOrder = _idxCurrentOrder;            
        _idxCurrentOrder = activeIdx;
        // _idxCurrentOrder has been incremented. Warranted train has entered this block. 
        // Do signals, speed etc.
        if (_idxCurrentOrder < _orders.size() - 1) {
            if (_engineer != null) {
                OBlock nextBlock = _orders.get(_idxCurrentOrder+1).getBlock();
                if ((nextBlock.getState() & OBlock.UNDETECTED) != 0) {
                    // can't detect next block, use ET
                    _engineer.setRunOnET(true);                
                } else if (!_tempRunBlind) {
                    _engineer.setRunOnET(false);                                    
                }
            }
            allocateFromIndex(_idxCurrentOrder + 1);
            BlockOrder bo = getBlockOrderAt(_idxCurrentOrder + 1);
            bo.setPath(this);
        } else { // train is in last block. past all signals
            if (_protectSignal != null) {
                _protectSignal.removePropertyChangeListener(this);
                _protectSignal = null;
                _idxProtectSignal = -1;
            }
            if (_runMode == MODE_MANUAL) { // no script, so terminate warrant run
                stopWarrant(false);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("end of goingActive. leaving \"{}\" entered \"{}\". warrant {}",
                    getBlockAt(activeIdx-1).getDisplayName(), block.getDisplayName(), getDisplayName());
        }
        setMovement(BEG);
        firePropertyChange("blockChange", getBlockAt(activeIdx-1), block);
    }       //end goingActive

    /**
     * Return false if not moving
     */
    private boolean statusOK(OBlock block) {
        if (_engineer != null) {
            if ( (_engineer.getRunState() == WAIT_FOR_CLEAR || _engineer.getRunState() == HALT)) {
                // expected block just occupied for this train, but train is stopped! - must be a rogue entry.
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return false;
            }
            _engineer.clearWaitForSync();      // Sync commands if train is faster than ET
            return true;
        }
        return false;
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

        int idx = getIndexOfBlockBefore(_idxCurrentOrder, block);  // if idx >= 0, it is in this warrant
        if (log.isDebugEnabled()) {
            log.debug("*Block \"{}\" goingInactive. idx= {}, _idxCurrentOrder= {}. warrant= {}",
                   block.getDisplayName(), idx, _idxCurrentOrder, getDisplayName());
        }
        if (idx < _idxCurrentOrder) {
            releaseBlock(block, idx);
        } else if (idx == _idxCurrentOrder) {
            // Train not visible if current block goes inactive
            if (_idxCurrentOrder + 1 < _orders.size()) {
                OBlock nextBlock = getBlockAt(_idxCurrentOrder + 1);
                if ((nextBlock.getState() & OBlock.UNDETECTED) != 0) {
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
                        cancelRamp();
                        _engineer.setHalt(true);    // does rampSpeedTo(Stop);
                        if (_idxCurrentOrder == 0) {
                            setStoppingBlock(block);
                            _delayStart = true;
                        }
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
        firePropertyChange("blockRelease", null, block);
        for (int i = idx; i > -1; i--) {
            boolean dealloc = true;
            OBlock prevBlock = getBlockAt(i);
            for (int j = i + 1; j < _orders.size(); j++) {
                if (prevBlock.equals(getBlockAt(j))) {
                    dealloc = false;
                }
            }
            if (dealloc && prevBlock.isAllocatedTo(this)) {
                prevBlock.setValue(null);
                prevBlock.deAllocate(this);
            }
        }
    }

    /**
     * build map of BlockSpeedInfo's for the route.
     * 
     * Put max speed and time in block's first occurrence after current command index
     */
    private void getBlockSpeedTimes() {
        _speedInfo =  new ArrayList<BlockSpeedInfo>();
        String blkName = null;
        float firstSpeed = 0.0f;    // used for entrance
        float maxSpeed = 0.0f;
        float lastSpeed = 0.0f;
        long blkTime = 0;
        int firstIdx = 0;      // for all blocks except first, this is index of NOOP command
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
                blkName = ts.getBeanDisplayName();               
                blkTime = 0;
                firstSpeed = lastSpeed;
                maxSpeed = lastSpeed;
                hasSpeedChange = false;
                firstIdx = i+1;     // first in next block is next index
            } else {        // collect block info
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
    
    static private class BlockSpeedInfo {
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

    private void cancelRamp() {
        if (_delayCommand!=null) {
            _delayCommand.interrupt();
            _delayCommand = null;
        }
        _engineer.cancelRamp();
        
    }

    @Override
    public void dispose() {
        stopWarrant(false);
        super.dispose();
    }

    @Override
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
            if(log.isDebugEnabled()) log.debug("CommandDelay: will wait {}ms, then Ramp to {}. warrant {}",
                    startWait, speedType, getDisplayName());
        }
        
        @Override
        @SuppressFBWarnings(value = "WA_NOT_IN_LOOP", justification = "notify never called on this thread")
        public void run() {
            synchronized(this) {
                if (_startWait>0.0) {
                    try {
                        wait(_startWait);
                    } catch (InterruptedException ie) {
                        if(log.isDebugEnabled()) log.debug("CommandDelay interrupt.  Ramp to {} not done. warrant {}",
                                nextSpeedType, getDisplayName());
                        quit = true;
                    }
                }
                if (!quit && _engineer != null) {
                    if(log.isDebugEnabled()) log.debug("CommandDelay: after wait of {}ms, start Ramp to {}. warrant {}", 
                            _startWait, nextSpeedType, getDisplayName());
                    jmri.util.ThreadingUtil.runOnLayout(() ->{ // move to layout-handling thread
                        if (nextSpeedType.equals(Stop) || nextSpeedType.equals(EStop)) {
                            _engineer.setWaitforClear(true);    // will ramp to 0.0
                        } else {
                            _engineer.rampSpeedTo(nextSpeedType);                      
                            _curSpeedType = nextSpeedType;                            
                        }
                        _engineer.advanceToCommandIndex(_cmdIndex);
                    });                    
                }
            }
        }
    }
    
    private String getSpeedTypeForBlock(int idxBlockOrder) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlockOrder);
        OBlock block = blkOrder.getBlock();
        String speedType = getPermissibleSpeedAt(blkOrder);
        setStoppingSignal(idxBlockOrder);
        if (speedType==null) {
            speedType = _curSpeedType;                   
        } else {
            if (speedType.equals(Stop)) {
            // block speed cannot be Stop, so OK to assume signal
            _waitForSignal = true;
            }
        }

        if (block.allocate(this) != null || (block.getState() & OBlock.OCCUPIED) != 0) {
            speedType = Stop;
            setStoppingBlock(block);
            _waitForBlock = true;
        }
        if(log.isTraceEnabled()) log.trace("Look for speed change at block {}. speedType={}",
                block.getDisplayName(), speedType); 
         return speedType;
    }
    
    private float getPathLength(BlockOrder bo) {
        float len = bo.getPath().getLengthMm();
        if (len <= 0) {
            log.warn(Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName()));
            len = 100;
        }
        return len;
    }

    /**
     * Called to set the correct speed for the train when the
     * scripted speed must be modified due to a track condition 
     * (signaled speed or rogue occupation). Also called to
     *  return to the scripted speed after the condition
     * is cleared. Assumes the train occupies the block of the current
     * block order.
     * <p>Looks for speed requirements of this block and takes immediate
     * action if found. Otherwise looks ahead for future speed change
     * needs.  If speed restriction changes are required to begin in  
     * this block, but the change is not immediate, then
     * determine the proper time delay to start the speed change.
     * @param position estimated position of train inn the block
     * @return false on errors
     */
    private boolean setMovement(int position) {
        if (_runMode != Warrant.MODE_RUN || _idxCurrentOrder > _orders.size()-1) {
            return false;
        }
        if (_engineer==null) {
            controlRunTrain(ABORT);
            return false;
        }
        int runState = _engineer.getRunState();
        BlockOrder blkOrder = getBlockOrderAt(_idxCurrentOrder);
        OBlock curBlock = blkOrder.getBlock();
        if(log.isDebugEnabled()) log.debug("setMovement({}) runState= {} current speedType= {} in block\"{}\" {} warrant {}.", 
                position, RUN_STATE[runState], _curSpeedType, curBlock.getDisplayName(), (_partialAllocate ? "ShareRoute" : ""), getDisplayName());
        
        String msg = blkOrder.setPath(this);
        if (msg != null) {
            log.error("Train {} in block \"{}\" but path cannot be set! msg= {}, warrant= {}",
                    getTrainName(), curBlock.getDisplayName(), msg, getDisplayName());
            _engineer.setSpeedToType(Stop);
            return false;
        }
        
        if ((curBlock.getState() & (OBlock.OCCUPIED | OBlock.UNDETECTED)) == 0) {
            log.error("Train {} expected in block \"{}\" but block is unoccupied! warrant= {}",
                    getTrainName(), curBlock.getDisplayName(), getDisplayName());
            _engineer.setSpeedToType(Stop);
            return false;
        }
        // Error checking done.

        if(log.isDebugEnabled())
            log.debug("Stopping flags _waitForBlock= {}, _waitForSignal= {}, speedSetting= {}, warrant {}",
                    _waitForBlock, _waitForSignal, _speedUtil.getSpeedSetting(), getDisplayName());

        if ((runState==WAIT_FOR_CLEAR || runState==HALT) /*&& _engineer.getSpeed()<= 0.0001f*/) {
            if(log.isDebugEnabled())
                log.debug("Hold train at block \"{}\" runState= {}, speedSetting= {}.warrant {}",
                        curBlock.getDisplayName(), RUN_STATE[runState], _speedUtil.getSpeedSetting(), getDisplayName());
            firePropertyChange("SpeedChange", _idxCurrentOrder-1, _idxCurrentOrder);    // message reason for hold
            return true;
        }

        // Cancel any delayed speed changes currently in progress.
        cancelRamp();

        // checking situation for the current block
        // _curSpeedType is the speed type train is currently running
        // currentType is the required speed limit for this block
        String currentType = getPermissibleSpeedAt(blkOrder);
        if (currentType==null) {
            currentType = _curSpeedType;
        }
        
        if (_noRamp || _speedInfo==null) {
            if (_idxCurrentOrder <  _orders.size() - 1) {
                currentType = getSpeedTypeForBlock(_idxCurrentOrder+1);
                if (_speedUtil.secondGreaterThanFirst(currentType, _curSpeedType)) {
                    if(log.isDebugEnabled()) log.debug("No ramp speed change of \"{}\" from \"{}\" in block \"{}\" warrant= {}",
                            currentType, _curSpeedType, curBlock.getDisplayName(), getDisplayName());
                    _engineer.setSpeedToType(currentType);
                    _curSpeedType = currentType;
                }                
            }
            return true;
        }

        if (!currentType.equals(_curSpeedType)) {
            if (_speedUtil.secondGreaterThanFirst(currentType, _curSpeedType)) {
                if (currentType.equals(Stop) || currentType.equals(EStop)) {
                    _engineer.setSpeedToType(currentType);
                    log.info("Train missed Stop signal in block \"{}\". warrant= {}", curBlock.getDisplayName(), getDisplayName());
                    return true;    // don't do anything else until stopping condition cleared
                } else {
                    log.info("Train {} moved past required speed of \"{}\" at speed \"{}\" in block \"{}\"! Set speed to {}. warrant= {}",
                            getTrainName(), currentType, _curSpeedType, curBlock.getDisplayName(), currentType, getDisplayName());
                    float current = _speedUtil.getSpeedSetting();
                    if (current < _engineer.getExpectedSpeed(currentType)) {
                        _engineer.rampSpeedTo(currentType); // always ramp increases                        
                    } else {
                        _engineer.setSpeedToType(currentType);                        
                    }
                    _curSpeedType = currentType;
                }
            } else {
                if(log.isDebugEnabled()) log.debug("Increasing speed to \"{}\" from \"{}\" in block \"{}\" warrant= {}",
                        currentType, _curSpeedType, curBlock.getDisplayName(), getDisplayName());
                _curSpeedType = currentType;    // cannot be Stop) or EStop
                _engineer.rampSpeedTo(currentType);                    
                    // TODO Merging of ramp commands with scripted command
                    // Ramp may intrude into this block's speed commands
            }
            // continue, there may be blocks ahead that need a speed decrease to begin in this block
        }
        
        // Halt and waitForClear may have been freed
        _engineer.rampSpeedTo(currentType);                    
        
        //look ahead for a speed change slower than the current speed
        // Note: blkOrder still is blkOrder = getBlockOrderAt(_idxCurrentOrder);
        // Do while speedType >= currentType
        int idxBlockOrder = _idxCurrentOrder;
        String speedType = _curSpeedType;
        while (!_speedUtil.secondGreaterThanFirst(speedType, _curSpeedType)  && idxBlockOrder < _orders.size()-1) {
            speedType = getSpeedTypeForBlock(++idxBlockOrder);
        }
        if (_speedUtil.secondGreaterThanFirst(currentType, speedType)) {
            speedType = currentType;    // any next speed cannot exceed the limit of this block
        }
        OBlock block = getBlockOrderAt(idxBlockOrder).getBlock();
        
        // went through remaining BlockOrders, found no speed decreases
        if (idxBlockOrder >= _orders.size()-1) {
            if (_curSpeedType.equals(speedType)) {
                // no speed changes 
                if(log.isDebugEnabled())
                    log.debug("No speed changes for runState= {} from {} found after block \"{}\" warrant {}",
                            RUN_STATE[runState], _curSpeedType, curBlock.getDisplayName(), getDisplayName());
                _waitForBlock = false;
                _waitForSignal = false;
                return true;    
            } else if (_speedUtil.secondGreaterThanFirst(_curSpeedType, speedType)){
                if(log.isDebugEnabled())
                    log.debug("Increase speedfor runState= {} speed {} to {} after entering block \"{}\" warrant {}",
                            RUN_STATE[runState], _curSpeedType, speedType, block.getDisplayName(), getDisplayName());
                if (idxBlockOrder < _orders.size()-1) {
                    _waitForBlock = false;
                    _waitForSignal = false;
                    return true;    
                }
            }
        }
        
        if(log.isTraceEnabled()) log.trace("Speed decrease to {} from {} needed before entering block \"{}\" warrant {}",
                speedType, _curSpeedType, block.getDisplayName(), getDisplayName());
        // There is a speed change needed before entering the above block
        // From this block, check if there is enough room to make the change 
        // using the entrance speed of the block. Walk back using previous
        // blocks, if necessary.
        float rampLen = 1.0f;
        float availDist = 0.0f;
        BlockSpeedInfo blkSpeedInfo = null;
        while (idxBlockOrder > _idxCurrentOrder && rampLen > availDist) {
            idxBlockOrder--;    // start at block before the block with slower speed type 
            blkOrder = getBlockOrderAt(idxBlockOrder);
            if (idxBlockOrder==_idxCurrentOrder) {
                float dist = _speedUtil.getDistanceTravelled();
                if (dist < 0) {
                    switch (position) {
                        case BEG:
                            dist =  getPathLength(blkOrder);
                            break;
                        case MID:
                            dist =  getPathLength(blkOrder) / 2;
                            break;
                        default:    // END:
                            dist = 0;
                    }
                }
                availDist += getPathLength(blkOrder) - dist;
            } else {
                availDist += getPathLength(blkOrder);                
            }
            blkSpeedInfo = _speedInfo.get(idxBlockOrder);
            
            float speed = blkSpeedInfo.getEntranceSpeed();
            if (speed<0.0001f) { // first block has 0.0 entrance speed
                speed = blkSpeedInfo.getMaxSpeed();
            }
            Engineer.RampData rampData = _speedUtil.rampLengthForSpeedChange(speed, _curSpeedType, speedType, _engineer.getIsForward());
            rampLen = rampData.getLength() + blkOrder.getEntranceSpace();
            if(log.isTraceEnabled()) log.trace("availDist= {}, at Block \"{}\" for ramp= {} to speed {} from {}. warrant {}", 
                    availDist, getBlockOrderAt(idxBlockOrder).getBlock().getDisplayName(), rampLen, 
                    _speedUtil.modifySpeed(speed, speedType, _engineer.getIsForward()), speed, getDisplayName());
        }
        if (idxBlockOrder > _idxCurrentOrder) {
            // sufficient ramp room was found before walking all the way back to current block
            if(log.isDebugEnabled()) 
                log.debug("Will decrease speed for runState= {} to {} from {} later in block \"{}\", warrant {}",
                        RUN_STATE[runState], speedType, _curSpeedType, blkOrder.getBlock().getDisplayName(), getDisplayName());
            _waitForBlock = false;
            _waitForSignal = false;
            return true;    // change speed later                
        }
        // need to start ramp in the block of _idxCurrentOrder
        
        if(log.isDebugEnabled()) log.debug("Schedule a speed change to {} now in block \"{}\", warrant= {}",
                speedType, blkOrder.getBlock().getDisplayName(), getDisplayName());

        // not enough room (shouldn't happen) so do ramp immediately
        if (rampLen >= availDist) {
            log.warn("Train {} ramping to speed \"{}\" in block \"{}\", warrant= {}",
                    getTrainName(), speedType, curBlock.getDisplayName(), getDisplayName());
            if (speedType.equals(Stop) || speedType.equals(EStop)) {
                _engineer.setWaitforClear(true);    // will ramp to 0.0
            } else {
                _engineer.rampSpeedTo(speedType);                
                _curSpeedType = speedType;                
            }
            firePropertyChange("SpeedChange", _idxCurrentOrder-1, _idxCurrentOrder);
            return true;
        }
        // now refine the point at which the ramp should begin
        // find wait time until starting ramp down
        if(log.isDebugEnabled()) log.debug("Get time to wait in block \"{}\" before ramping to {}. warrant {}",
                getBlockOrderAt(idxBlockOrder).getBlock().getDisplayName(), speedType, getDisplayName());
        blkSpeedInfo = _speedInfo.get(idxBlockOrder);
        long waitTime = getWaitTime(idxBlockOrder, availDist, blkOrder.getEntranceSpace(), speedType);
        waitTime -= 500;    // shorten time a bit to allow for possible processing time.
        if (waitTime <= 0) {
            if (speedType.equals(Stop) || speedType.equals(EStop)) {
                _engineer.setWaitforClear(true);    // will ramp to 0.0
            } else {
                _engineer.rampSpeedTo(speedType);            
                _curSpeedType = speedType;                
            }
        } else {
            _delayCommand = new CommandDelay(speedType, waitTime, blkSpeedInfo.getLastIndex());
            _delayCommand.start();            
        }
        return true;
    }

    /**
     * Should only be called for decreasing a speed
     * @param idxBlockOrder index of BlockOrder where ramp is started
     * @param availDist distance available for ramp
     * @param distAdj extra space user wants before entering block
     * @return time to wait before starting ramp down
     */
    private long getWaitTime(int idxBlockOrder, float availDist, float distAdj, String endSpeedType) {
        if (_speedUtil.secondGreaterThanFirst(_curSpeedType, endSpeedType)) {
            log.error("getWaitTime called when increasing speed _curSpeedType={} endSpeedType={}", _curSpeedType, endSpeedType);
            return 0;
        }
        BlockSpeedInfo blkSpeedInfo = _speedInfo.get(idxBlockOrder);
        float speed = blkSpeedInfo.getEntranceSpeed();  // unmodified recorded speed
        boolean isForward = _engineer.getIsForward();
        float waitSpeed = _speedUtil.modifySpeed(speed, _curSpeedType, isForward); // speed while waiting to start ramp
        long waitTime = 0;      // time to wait before starting ramp
        long speedTime = 0;     // time running at a given speed
        boolean hasSpeed = (speed>0.0001f);
        float timeRatio;
        if (Math.abs(speed - waitSpeed) > .0001f) {
            timeRatio = speed / waitSpeed;
        } else {
            timeRatio = 1.0f;
        }

        // look at all the commands done in this block and find the time and distance to wait before ramping
        // the speed change to the endspeed.
        // Note getDistanceTraveled() and getTimeForDistance() are generally unreliable when only a
        // throttle factor is known (i.e. no speed profile)
        float rampLen = 0.0f;   //_engineer.rampLengthForSpeedChange(speed, _curSpeedType, endSpeedType)+distAdj;
        float waitDist = 0.0f;      // distance traveled until ramp is started
        int startIdx = blkSpeedInfo.getFirstIndex();
        int endIdx = blkSpeedInfo.getLastIndex();
        for (int i=startIdx; i<=endIdx; i++) {
            ThrottleSetting ts = _commands.get(i);
            String cmd = ts.getCommand().toUpperCase();
            if (hasSpeed) {
                speedTime = (long)(ts.getTime()*timeRatio);
                float dist = _speedUtil.getDistanceTraveled(speed, _curSpeedType, speedTime, isForward);
                Engineer.RampData rampData = _speedUtil.rampLengthForSpeedChange(speed, _curSpeedType, endSpeedType, isForward);
                rampLen = rampData.getLength() + distAdj;
                if (rampLen > availDist) {
                    log.error("rampLen > availDist! availDist= {} rampLen= {} waitTime= {}", availDist, rampLen, waitTime);
                    break;
                }
                
                if ((waitDist + dist) > (availDist - rampLen)) {
                    float backupDist = (waitDist + dist) - (availDist - rampLen);
                    waitTime += rampData.getTime();
                    waitDist += backupDist;
                    break;
                    
                }
                waitDist += dist;
            } 
            waitTime += (long)(ts.getTime()*timeRatio);
            if (cmd.equals("SPEED")) {
                speed = Float.parseFloat(ts.getValue());
                hasSpeed = (speed>0.0001f);
                if (hasSpeed) {
                    waitSpeed = _speedUtil.modifySpeed(speed, _curSpeedType, isForward);
                    if (Math.abs(speed - waitSpeed) > .0001f) {
                        timeRatio = speed / waitSpeed;
                    } else {
                        timeRatio = 1.0f;
                    }
                }
            }
            if(log.isTraceEnabled()) log.trace("getWaitTime: rampLen= {}, waitDist= {}, waitTime= {} waitSpeed= {} -{}",
                    rampLen, waitDist, waitTime, waitSpeed, ts.toString());
        }
        
        if(log.isDebugEnabled()) log.debug("getWaitTime:  waitTime= {}, availDist= {} waitDist= {}, rampLen= {}, ramp start speed= {}",
                waitTime, availDist, waitDist, rampLen, waitSpeed);
        return waitTime;
    }
    
    private final static Logger log = LoggerFactory.getLogger(Warrant.class.getName());
}
