package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.concurrent.GuardedBy;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.ThrottleListener;
import jmri.implementation.SignalSpeedMap;
import jmri.util.ThreadingUtil;

/**
 * An Warrant contains the operating permissions and directives needed for a
 * train to proceed from an Origin to a Destination. There are three modes that
 * a Warrant may execute;
 * <p>
 * MODE_LEARN - Warrant is created or edited in WarrantFrame and then launched
 * from WarrantFrame who records throttle commands from "_student" throttle.
 * Warrant fires PropertyChanges for WarrantFrame to record when blocks are
 * entered. "_engineer" thread is null.
 * <p>
 * MODE_RUN - Warrant may be launched from several places. An array of
 * BlockOrders, _savedOrders, and corresponding _throttleCommands allow an
 * "_engineer" thread to execute the throttle commands. The blockOrders
 * establish the route for the Warrant to acquire and reserve OBlocks. The
 * Warrant monitors block activity (entrances and exits, signals, rogue
 * occupancy etc) and modifies speed as needed.
 * <p>
 * MODE_MANUAL - Warrant may be launched from several places. The Warrant to
 * acquires and reserves the route from the array of BlockOrders. Throttle
 * commands are done by a human operator. "_engineer" and "_throttleCommands"
 * are not used. Warrant monitors block activity but does not set _stoppingBlock
 * or _shareTOBlock since it cannot control speed. It does attempt to realign
 * the route as needed, but can be thwarted.
 * <p>
 * Version 1.11 - remove setting of SignalHeads
 *
 * @author Pete Cressman Copyright (C) 2009, 2010
 */
public class Warrant extends jmri.implementation.AbstractNamedBean implements ThrottleListener, java.beans.PropertyChangeListener {

    public static final String Stop = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getNamedSpeed(0.0f); // aspect name
    public static final String EStop = Bundle.getMessage("EStop");
    public static final String Normal ="Normal";    // Cannot determine which SignalSystem(s) and their name(s) for "Clear"

    // permanent members.
    private List<BlockOrder> _orders;
    private BlockOrder _viaOrder;
    private BlockOrder _avoidOrder;
    private List<ThrottleSetting> _commands = new ArrayList<>();
    protected String _trainName; // User train name for icon
    private SpeedUtil _speedUtil;
    private boolean _runBlind; // Unable to use block detection, must run on et only
    private boolean _partialAllocate;// only allocate one block at a time for sharing route.
    private boolean _noRamp; // do not ramp speed changes. make immediate speed change when entering approach block.
    protected Warrant _self = this;

    // transient members
    private LearnThrottleFrame _student; // need to callback learning throttle in learn mode
    private boolean _tempRunBlind; // run mode flag to allow running on ET only
    protected boolean _delayStart; // allows start block unoccupied and wait for train
    protected int _idxCurrentOrder; // Index of block at head of train (if running)
    protected int _idxLastOrder; // Index of block at tail of train just left
    private String _curSpeedType; // name of last moving speed, i.e. never "Stop".  Used to restore previous speed
    protected ArrayList<BlockSpeedInfo> _speedInfo; // map max speeds and occupation times of each block in route

    protected int _runMode;
    private Engineer _engineer; // thread that runs the train
    @GuardedBy("this")
    private CommandDelay _delayCommand; // thread for delayed ramp down
    private boolean _allocated; // initial Blocks of _orders have been allocated
    private boolean _totalAllocated; // All Blocks of _orders have been allocated
    private boolean _routeSet; // all allocated Blocks of _orders have paths set for route
    protected OBlock _stoppingBlock; // Block occupied by rogue train or halted
    private NamedBean _protectSignal; // Signal stopping train movement
    private int _idxProtectSignal;
    // Crossovers typically have both switches controlled by one TO, although each switch is in a different block
    // At the origin and destination of warrants, TO's shared between warrants may set conflicting paths
    private OBlock _myShareBlock;   // block belonging to this warrant
    private OBlock _otherShareBlock;   // block belonging to another warrant

    private boolean _waitForSignal; // train may not move until false
    private boolean _waitForBlock; // train may not move until false
    private boolean _waitForWarrant;
    protected String _message; // last message returned from an action

    // Throttle modes
    public static final int MODE_NONE = 0;
    public static final int MODE_LEARN = 1; // Record a command list
    public static final int MODE_RUN = 2;   // Autorun, playback the command list
    public static final int MODE_MANUAL = 3; // block detection of manually run train
    public static final String[] MODES = {"none", "LearnMode", "RunAuto", "RunManual"};
    public static final int MODE_ABORT = 4; // used to set status string in WarrantTableFrame

    // control states
    public static final int STOP = 0;
    public static final int HALT = 1;
    public static final int RESUME = 2;
    public static final int ABORT = 3;
    public static final int RETRY = 4;
    public static final int ESTOP = 5;
    protected static final int RAMP_HALT = 6;
    protected static final int RUNNING = 7;
    protected static final int SPEED_RESTRICTED = 8;
    protected static final int WAIT_FOR_CLEAR = 9;
    protected static final int WAIT_FOR_SENSOR = 10;
    protected static final int WAIT_FOR_TRAIN = 11;
    protected static final int WAIT_FOR_DELAYED_START = 12;
    protected static final int LEARNING = 13;
    protected static final int STOP_PENDING = 14;
    protected static final int RAMPING_UP = 15;
    protected static final int DEBUG = 7;
    protected static final String[] CNTRL_CMDS = {"Stop", "Halt", "Resume", "Abort", "Retry", "EStop", "Ramp", "Debug"};
    protected static final String[] RUN_STATE = {"HaltStart", "atHalt", "Resumed", "Aborts", "Retried", 
            "EStop", "Ramp", "Running", "RestrictSpeed", "WaitingForClear", "WaitingForSensor", 
            "RunningLate", "WaitingForStart", "RecordingScript", "StopPending", "RampingUp"};

    // Estimated positions of the train in the block it occupies
    static final int BEG = 1;
    static final int MID = 2;
    static final int END = 3;

    /**
     * Create an object with no route defined. The list of BlockOrders is the
     * route from an Origin to a Destination
     *
     * @param sName system name
     * @param uName user name
     */
    public Warrant(String sName, String uName) {
        super(sName, uName);
        _idxCurrentOrder = 0;
        _idxLastOrder = 0;
        _orders = new ArrayList<>();
        _runBlind = false;
        _speedUtil = new SpeedUtil(_orders);
    }

    @Override
    public int getState() {
        if (_engineer != null) {
            return _engineer.getRunState();
        }
        if (_delayStart) {
            return WAIT_FOR_DELAYED_START;
        }
        if (_runMode == MODE_LEARN) {
            return LEARNING;
        }
        if (_runMode != MODE_NONE) {
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
    }

    /**
     * Return BlockOrders.
     *
     * @return list of block orders
     */
    public List<BlockOrder> getBlockOrders() {
        return _orders;
    }

    /**
     * Add permanently saved BlockOrder.
     *
     * @param order block order
     */
    public void addBlockOrder(BlockOrder order) {
        _orders.add(order);
    }

    public void setBlockOrders(List<BlockOrder> orders) {
        _orders = orders;
    }

    /**
     * Return permanently saved Origin.
     *
     * @return origin block order
     */
    public BlockOrder getfirstOrder() {
        if (_orders.isEmpty()) {
            return null;
        }
        return new BlockOrder(_orders.get(0));
    }

    /**
     * Return permanently saved Destination.
     *
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
     * Return permanently saved BlockOrder that must be included in the route.
     *
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
     * Find block AFTER startIdx.
     *
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
     * Find block BEFORE endIdx.
     *
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
     * Find block after startIdx. Call is only valid when in MODE_LEARN and
     * MODE_RUN (previously start was i=_idxCurrentOrder)
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
     * Call is only valid when in MODE_LEARN and MODE_RUN.
     *
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
     * Call is only valid when in MODE_LEARN and MODE_RUN.
     *
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
     * Call is only valid when in MODE_LEARN and MODE_RUN.
     *
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
     * Call is only valid when in MODE_LEARN and MODE_RUN.
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
        return _commands;
    }

    public void setThrottleCommands(List<ThrottleSetting> list) {
        _commands = list;
    }

    public void addThrottleCommand(ThrottleSetting ts) {
        _commands.add(ts);
    }

    public boolean commandsHaveTrackSpeeds() {
        for (ThrottleSetting ts : _commands) {
            if (ts.getSpeed() > 0.0f) {
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

    /*
     * Engineer reports its status
     */
    @jmri.InvokeOnLayoutThread
    protected void fireRunStatus(String property, Object old, Object status) {
//        jmri.util.ThreadingUtil.runOnLayout(() -> {   // Will hang GUI!
        ThreadingUtil.runOnLayoutEventually(() -> { // OK but can be quite late in reporting speed changes
            firePropertyChange(property, old, status);
        });
    }

    /**
     * ****************************** state queries ****************
     */
    /**
     * @return true if listeners are installed enough to run
     */
    public boolean isAllocated() {
        return _allocated;
    }

    /**
     * @return true if listeners are installed for entire route
     */
    public boolean isTotalAllocated() {
        return _totalAllocated;
    }

    /**
     * Turnouts are set for the route
     *
     * @return true if turnouts are set
     */
    public boolean hasRouteSet() {
        return _routeSet;
    }

    /**
     * Test if the permanent saved blocks of this warrant are free (unoccupied
     * and unallocated)
     *
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
     *
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

    /* ************* Methods for running trains *****************/

    protected boolean isWaitingForSignal() {
        return _waitForSignal;
    }
    protected boolean isWaitingForClear() {
        return _waitForBlock;
    }
    protected boolean isWaitingForWarrant() {
        return _waitForWarrant;
    }
    protected Warrant getBlockingWarrant() {
        if (_stoppingBlock != null && !this.equals(_stoppingBlock.getWarrant())) {
            return _stoppingBlock.getWarrant();
        }
        if (_otherShareBlock != null) {
            return _otherShareBlock.getWarrant();
        }
        return null;
    }
     /**
      *  @return ID of run mode
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
                if (getBlockOrders().isEmpty()) {
                    return Bundle.getMessage("BlankWarrant");
                }
                if (_speedUtil.getAddress() == null) {
                    return Bundle.getMessage("NoLoco");
                }
                if (!(this instanceof SCWarrant) && _commands.size() <= _orders.size()) {
                    return Bundle.getMessage("NoCommands", getDisplayName());
                }
                if (_message == null) {
                    return Bundle.getMessage("Idle");
                }
                if (_idxCurrentOrder != 0 && _idxLastOrder == _idxCurrentOrder) {
                    return Bundle.getMessage("locationUnknown", _trainName, getCurrentBlockName()) + _message;
                }
                return Bundle.getMessage("Idle1", _message);
            case Warrant.MODE_LEARN:
                return Bundle.getMessage("Learning", getCurrentBlockName());
            case Warrant.MODE_RUN:
                if (_engineer == null) {
                    return Bundle.getMessage("engineerGone", getCurrentBlockName());
                }
                int cmdIdx = _engineer.getCurrentCommandIndex();
                if (cmdIdx >= _commands.size()) {
                    cmdIdx = _commands.size() - 1;
                }
                cmdIdx++;   // display is 1-based
                OBlock block = getCurrentBlockOrder().getBlock();
                if ((block.getState() & (OBlock.OCCUPIED | OBlock.UNDETECTED)) == 0) {
                    return Bundle.getMessage("LostTrain", _trainName, block.getDisplayName());
                }
                String blockName = block.getDisplayName();
                String speed = getSpeedMessage(_curSpeedType);

                switch (_engineer.getRunState()) {
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

                    case Warrant.HALT:
                    case Warrant.WAIT_FOR_CLEAR:
                        String s = "";
                        if (_waitForSignal) {
                            s = Bundle.getMessage("Signal");
                        } else if (_waitForWarrant) {
                            Warrant w = getBlockingWarrant();
                            if (w != null) {
                                s = Bundle.getMessage("WarrantWait",  w.getDisplayName());
                            } else {
                                s = Bundle.getMessage("WaitForClear", blockName, (_waitForSignal
                                        ? Bundle.getMessage("Signal") : Bundle.getMessage("Occupancy")));
                            }
                        } else if (_waitForBlock) {
                            s = Bundle.getMessage("Occupancy");
                        } else {
                            return Bundle.getMessage("Halted", blockName, cmdIdx);
                        }
                        return Bundle.getMessage("RampWaitForClear", 
                                getTrainName(), getCurrentBlockName(), s);

                    case Warrant.WAIT_FOR_TRAIN:
                        int blkIdx = _idxCurrentOrder + 1;
                        if (blkIdx >= _orders.size()) {
                            blkIdx = _orders.size() - 1;
                        }
                        return Bundle.getMessage("WaitForTrain", cmdIdx,
                                getBlockOrderAt(blkIdx).getBlock().getDisplayName(), speed);

                    case Warrant.WAIT_FOR_SENSOR:
                        return Bundle.getMessage("WaitForSensor",
                                cmdIdx, _engineer.getWaitSensor().getDisplayName(),
                                blockName, speed);

                    case Warrant.RUNNING:
                    case Warrant.SPEED_RESTRICTED:
                    case Warrant.RAMPING_UP:
                        return Bundle.getMessage("WhereRunning", blockName, cmdIdx, speed);

                    case Warrant.RAMP_HALT:
                        return Bundle.getMessage("HaltPending", speed, blockName);

                    case Warrant.STOP_PENDING:
                        return Bundle.getMessage("StopPending", speed, blockName, (_waitForSignal
                                ? Bundle.getMessage("Signal") : (_waitForBlock 
                                        ? Bundle.getMessage("Occupancy") : Bundle.getMessage("Warrant"))));

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

    /**
     * Calculates the scale speed of the current throttle setting for display
     * @param speedType name of current speed
     * @return text message
     */
    private String getSpeedMessage(String speedType) {
        float speed = 0;
        String units;
        SignalSpeedMap speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
        switch (speedMap.getInterpretation()) {
            case SignalSpeedMap.PERCENT_NORMAL:
/*                units = Bundle.getMessage("percentNormal");
                if (_idxCurrentOrder == _orders.size() - 1 
                        || _engineer.getCurrentCommandIndex() >= _commands.size() - 1) {
                    speed = _speedInfo.get(_idxCurrentOrder).getEntranceSpeed();
                } else {
                    for (int idx = _engineer.getCurrentCommandIndex(); idx >=0; idx--) {
                        ThrottleSetting ts = _commands.get(idx);
                        if ("SPEED".equals(ts.getCommand().toUpperCase())) {
                            speed = Float.parseFloat(ts.getValue());
                            break;
                        }
                    }                    
                }
                speed = (_engineer.getSpeedSetting() / speed) * 100;
                break;*/
            case SignalSpeedMap.PERCENT_THROTTLE:
                units = Bundle.getMessage("percentThrottle");
                speed = _engineer.getSpeedSetting() * 100;
                break;
            case SignalSpeedMap.SPEED_MPH:
                units = "Mph";
                speed = _speedUtil.getTrackSpeed(_engineer.getSpeedSetting()) * speedMap.getLayoutScale();
                speed = speed * 2.2369363f;
                break;
            case SignalSpeedMap.SPEED_KMPH:
                units = "Kmph";
                speed = _speedUtil.getTrackSpeed(_engineer.getSpeedSetting()) * speedMap.getLayoutScale();
                speed = speed * 3.6f;
                break;
            default:
                log.error("Unknown speed interpretation {}", speedMap.getInterpretation());
                throw new java.lang.IllegalArgumentException("Unknown speed interpretation " + speedMap.getInterpretation());
        }
        return Bundle.getMessage("atSpeed", speedType, Math.round(speed), units);
    }

    @jmri.InvokeOnLayoutThread
    protected void startTracker() {
        // error if not on Layout thread
        if (!ThreadingUtil.isLayoutThread()) {
            log.error("invoked on wrong thread", new Exception("traceback"));
        }

        InstanceManager.getDefault(TrackerTableAction.class).markNewTracker(getCurrentBlockOrder().getBlock(),
                _trainName, null);
    }

    synchronized public void stopWarrant(boolean abort) {
        stopWarrant(abort, true);
    }
    synchronized public void stopWarrant(boolean abort, boolean turnOffFunctions) {
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
        if (_otherShareBlock != null) {
            _otherShareBlock.removePropertyChangeListener(this);
            _otherShareBlock = null;
            _myShareBlock = null;
        }
        deAllocate();
        int oldMode = _runMode;
        _runMode = MODE_NONE;

        if (_student != null) {
            _student.dispose(); // releases throttle
            _student = null;
        }
        if (_engineer != null) {
            _speedUtil.stopRun(!abort); // don't write speed profile measurements
            _engineer.stopRun(abort, turnOffFunctions); // release throttle
            _engineer = null;
        }
        if (abort) {
            fireRunStatus("runMode", oldMode, MODE_ABORT);
        } else {
            fireRunStatus("runMode", oldMode, _runMode);
        }
        if (log.isDebugEnabled()) {
            log.debug("Warrant \"{}\" terminated {}.", getDisplayName(), (abort == true ? "- aborted!" : "normally"));
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
     *
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
        _waitForWarrant = false;
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
            synchronized (this) {
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
            if (mode != MODE_MANUAL) {
                _message = acquireThrottle();
            } else {
                startupWarrant(); // assuming manual operator will go to start block
            }
        }
        fireRunStatus("runMode", MODE_NONE, _runMode);
        if (log.isDebugEnabled()) {
            log.debug("Exit setRunMode()  _runMode= {}, msg= {}", MODES[_runMode], _message);
        }
        return _message;
    } // end setRunMode

    /////////////// start warrant run - end of create/edit/setup methods //////////////////

    /**
     * @return error message if any
     */
    protected String acquireThrottle() {
        String msg = null;
        DccLocoAddress dccAddress = _speedUtil.getDccAddress();
        if (log.isDebugEnabled()) {
            log.debug("acquireThrottle request at {} for warrant {}",
                    dccAddress, getDisplayName());
        }
        if (dccAddress == null) {
            msg = Bundle.getMessage("NoAddress", getDisplayName());
        } else {
            jmri.ThrottleManager tm = InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
            if (tm == null) {
                msg = Bundle.getMessage("noThrottle", _speedUtil.getDccAddress().getNumber());
            } else {
                if (!tm.requestThrottle(dccAddress, this, false)) {
                    return Bundle.getMessage("trainInUse", dccAddress.getNumber());
                }
            }
        }
        if (msg != null) {
            abortWarrant(msg);
            fireRunStatus("throttleFail", null, msg);
            return msg;
        }
        return null;
    }

    @Override
    public void notifyThrottleFound(DccThrottle throttle) {
        if (throttle == null) {
            String msg = Bundle.getMessage("noThrottle", getDisplayName());
            abortWarrant(msg);
            fireRunStatus("throttleFail", null, msg);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("notifyThrottleFound for address= {}, class= {}, warrant {}",
                    throttle.getLocoAddress(), throttle.getClass().getName(), getDisplayName());
        }
        _speedUtil.setThrottle(throttle);
        _speedUtil.setOrders(_orders);
        startupWarrant();
        runWarrant(throttle);
    } //end notifyThrottleFound

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        abortWarrant(Bundle.getMessage("noThrottle",
                (reason + " " + (address != null ? address.getNumber() : getDisplayName()))));
        fireRunStatus("throttleFail", null, reason);
    }
    
    /**
     * {@inheritDoc}
     * @deprecated since 4.15.7; use #notifyDecisionRequired
     */
    @Override
    @Deprecated
    public void notifyStealThrottleRequired(jmri.LocoAddress address) {
        InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL );
    }

    /**
     * No steal or share decisions made locally
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
    }

    protected void releaseThrottle(DccThrottle throttle) {
        if (throttle != null) {
            jmri.ThrottleManager tm = InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
            if (tm != null) {
                tm.releaseThrottle(throttle, this);
            } else {
                log.error("{} on thread {}",Bundle.getMessage("noThrottle", throttle.getLocoAddress()),
                        Thread.currentThread().getName());
            }
        }
    }

    protected void abortWarrant(String msg) {
        log.error("Abort warrant \"{}\" - {} ", getDisplayName(), msg);
        stopWarrant(true);
    }

    /**
     * Pause and resume auto-running train or abort any allocation state User
     * issued overriding commands during run time of warrant _engineer.abort()
     * calls setRunMode(MODE_NONE,...) which calls deallocate all.
     *
     * @param idx index of control command
     * @return false if command cannot be given
     */
    public boolean controlRunTrain(int idx) {
        if (idx < 0) {
            return false;
        }
        boolean ret = false;
        if (_engineer == null) {
            if (log.isDebugEnabled()) {
                log.debug("controlRunTrain({})= \"{}\" for train {} runMode= {}. warrant {}",
                        idx, CNTRL_CMDS[idx], getTrainName(), MODES[_runMode], getDisplayName());
            }
            switch (idx) {
                case HALT:
                case RESUME:
                case RETRY:
                case RAMP_HALT:
                    fireRunStatus("SpeedChange", null, idx);
                    break;
                case STOP:
                case ABORT:
                    if (_runMode == Warrant.MODE_LEARN) {
                        // let WarrantFrame do the abort. (WarrantFrame listens for "abortLearn")
                        fireRunStatus("abortLearn", -MODE_LEARN, _idxCurrentOrder);
                    } else {
                        fireRunStatus("controlChange", MODE_RUN, ABORT);
                        stopWarrant(true);
                    }
                    break;
                case DEBUG:
                    ret = debugInfo();
                    fireRunStatus("SpeedChange", null, idx);
                    break;
                default:
            }
            return ret;
        }
        int runState = _engineer.getRunState();
        if (log.isDebugEnabled()) {
            log.debug("controlRunTrain({})= \"{}\" for train {} runstate= {}, in block {}. warrant {}",
                    idx, CNTRL_CMDS[idx], getTrainName(), RUN_STATE[runState], 
                    getBlockAt(_idxCurrentOrder).getDisplayName(), getDisplayName());
        }
        synchronized (_engineer) {
            switch (idx) {
                case RAMP_HALT:
                    cancelDelayRamp();
                    _engineer.rampSpeedTo(Warrant.Stop, 0, false);  // immediate ramp down
                    _engineer.setHalt(true);
//                    setMovement(MID);
                    ret = true;
                    break;
                case RESUME:
                    BlockOrder bo = getBlockOrderAt(_idxCurrentOrder);
                    OBlock block = bo.getBlock();
                    if ((block.getState() & (OBlock.OCCUPIED | OBlock.UNDETECTED)) != 0) {
                        // we assume this occupation is this train. user should know
                        if (runState == WAIT_FOR_CLEAR || runState == HALT) {
                            // However user knows if condition may have cleared due to overrun.
                            _message = allocateFromIndex(false, true, _idxCurrentOrder + 1);
                            // This is user's decision to reset and override wait flags
                            Engineer.ThrottleRamp ramp = _engineer.getRamp();
                            if (ramp == null || ramp.ready) {   // do not change flags when ramping
                                // recheck flags
                                if (_idxCurrentOrder < _orders.size() - 1 &&
                                		Stop.equals(getSpeedTypeForBlock(_idxCurrentOrder + 1))) {
                                	break;	// cannot overide flags
                                }
                            	if (_idxCurrentOrder == 0) {
                                    _engineer.setHalt(false);
                            	}
                                _waitForSignal = false;
                                _waitForBlock = false;
                                _waitForWarrant = false;
//                                _engineer.setWaitforClear(false);
                                // engineer will clear its flags when ramp completes
                                _engineer.rampSpeedTo(_curSpeedType, 0, false);
                            }
                        } else if (runState == WAIT_FOR_TRAIN || runState == SPEED_RESTRICTED) {
                            // user wants to increase throttle of stalled train slowly
                            float speedSetting = _engineer.getSpeedSetting();
                            _engineer.setSpeed(speedSetting + _speedUtil.getRampThrottleIncrement());
                        } else {    // last resort from user to get on script
                            _engineer.setSpeed(_speedUtil.modifySpeed(_engineer.getScriptSpeed(), _curSpeedType));
                        }
                        ret = true;
                    } else {    // train must be lost. 
                        // user wants to increase throttle of stalled train slowly
                        float speedSetting = _engineer.getSpeedSetting();
                        _engineer.setSpeed(speedSetting + _speedUtil.getRampThrottleIncrement());
                    }
                    break;
                case RETRY: // Force move into next block, which was seen as rogue occupied
                    bo = getBlockOrderAt(_idxCurrentOrder + 1);
                    // if block belongs to this warrant, then move unconditionally into block
                    if (bo != null) {
                        block = bo.getBlock();
                        if (block.allocate(this) == null && (block.getState() & OBlock.OCCUPIED) != 0) {
                            _idxCurrentOrder++;
                            if (block.equals(_stoppingBlock)) {
                                clearStoppingBlock();
                            }
                            String msg = bo.setPath(this);
                            if (msg != null) {
                                log.warn("Cannot clear path for warrant \"{}\" at block \"{}\" - msg = {}",
                                        getDisplayName(), block.getDisplayName(), msg);
                            }
                            goingActive(block);
                            ret = true;
                        }
                    }
                    break;
                case ABORT:
                    stopWarrant(true);
                    ret = true;
                    break;
                case HALT:
                case STOP:
                    cancelDelayRamp();
                    _engineer.setStop(false, true); // sets _halt
                    ret = true;
                    break;
                case ESTOP:
                    cancelDelayRamp();
                    _engineer.setStop(true, true); // E-stop & halt
                    ret = true;
                    break;
                case DEBUG:
                    ret = debugInfo();
                    fireRunStatus("SpeedChange", null, idx);
                    return ret;
                default:
                    return false;
            }
        }
        int state = runState;
        if ( state == Warrant.HALT) {
        	if (_waitForSignal || _waitForBlock || _waitForWarrant) {
        		state = WAIT_FOR_CLEAR;
        	}
        }
        if (ret) {
            fireRunStatus("controlChange", state, idx);
        } else {
            fireRunStatus("controlFailed", state, idx);
        }
        return ret;
    }
    
    protected boolean debugInfo() {
        StringBuffer info = new StringBuffer("\nWarrant ");
        info.append(getDisplayName()); info.append(", ");
        info.append("Current BlockOrder idx= "); info.append(_idxCurrentOrder);
        info.append(",  Block \""); info.append(getBlockAt(_idxCurrentOrder).getDisplayName()); info.append("\"");
        info.append("\n\tWarrant flags: _waitForSignal= ");
        info.append(_waitForSignal); info.append(", _waitForBlock= ");
        info.append(_waitForBlock); info.append(", _waitForWarrant= ");
        info.append(_waitForWarrant); info.append("\n\t");
        if (_engineer != null) {
            info.append("Engineer runstate= ");
            info.append(RUN_STATE[_engineer.getRunState()]); info.append(", ");
            info.append("speed setting= ");
            info.append(_engineer.getSpeedSetting()); info.append(", ");
            info.append("scriptSpeed= ");
            info.append(_engineer.getScriptSpeed()); info.append("\n\t");
            int cmdIdx = _engineer.getCurrentCommandIndex();
            info.append("current Cmd Index #");
            // Note: command indexes biased from 0 to 1 to match Warrant's 1-based display of commands.
            info.append(cmdIdx + 1); info.append(", Command: ");
            info.append(getThrottleCommands().get(cmdIdx).toString()); info.append("\n\t");
            info.append(_engineer.getFlags()); info.append("\n\t\t");
            for (StackTraceElement elem :_engineer.getStackTrace()) {
                info.append(elem.getClassName()); info.append(".");
                info.append(elem.getMethodName()); info.append(", line ");
                info.append(elem.getLineNumber()); info.append("\n\t\t");
            }
            info.append("\tEngineer Thread.State= ");
            info.append(_engineer.getState()); info.append("\n\t");
            Engineer.ThrottleRamp ramp = _engineer.getRamp();
            if (ramp != null) {
                info.append("Ramp Thread.State= "); info.append(ramp.getState());
                info.append(", ready= "); info.append(ramp.ready); info.append("\n\t\t");
                for (StackTraceElement elem : ramp.getStackTrace()) {
                    info.append(elem.getClassName()); info.append(".");
                    info.append(elem.getMethodName()); info.append(", line ");
                    info.append(elem.getLineNumber()); info.append("\n\t\t");
                }
            } else {
                info.append("No ramp");
            }
        } else {
            info.append("No engineer");
        }
        log.info(info.toString());
        return true;
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

    private void runWarrant(DccThrottle throttle) {
        if (_runMode == MODE_LEARN) {
            synchronized (this) {
                _student.notifyThrottleFound(throttle);
            }
        } else {
             _engineer = new Engineer(this, throttle);
            if (_tempRunBlind) {
                _engineer.setRunOnET(true);
            }
            if (_delayStart) {
                _engineer.setHalt(true);    // throttle already at 0
            }
            // if there may be speed changes due to signals or rogue occupation.
            if (_noRamp) { // make immediate speed changes
                _speedInfo = null;
            } else { // make smooth speed changes by ramping
                getBlockSpeedTimes();
            }
            _engineer.start();

            if (_delayStart) {
                // user must explicitly start train (resume) in a dark block
                fireRunStatus("ReadyToRun", -1, 0);   // ready to start msg
            }
            if (_engineer.getRunState() == Warrant.RUNNING) {
                setMovement(MID);
            }
            _delayStart = false; // script should start when user resumes - no more delay
        }
    }

    /**
     * Allocate as many blocks as possible from the start of the warrant.
     * The first block must be allocated and all blocks of the route must 
     * be in service. Otherwise partial success is OK.
     * Installs listeners for the entire route.
     * If occupation by another train is detected, a message will be
     * posted to the Warrant List Window. Note that warrants sharing their
     * clearance only allocate and set paths one block in advance.
     *
     * @param orders list of block orders
     * @param show (for use ONLY to display a temporary route) continues to 
     *  allocate skipping over blocks occupied or owned by another warrant. 
     * @return error message, if unable to allocate first block or if any block
     *         is OUT_OF_SERVICE
     */
    public String allocateRoute(boolean show, List<BlockOrder> orders) {
        if (_totalAllocated) {
            return null;
        }
        if (orders != null) {
            _orders = orders;
        }
        _allocated = false;
        OBlock block = getBlockAt(0);
        _message = block.allocate(this);
        if (_message != null) {
            return _message;
        }

        _allocated = true; // start block allocated
        String msg = allocateFromIndex(show, false, 1);
        if (msg != null) {
            _message = msg;
        } else if (_partialAllocate) {
            _message = Bundle.getMessage("sharedRoute");
        }
        if (show) {
            return _message;
        }
        return null;
    }

    /*
     * Allocate and set path
     * Only return a message if allocation of first index block fails.
     * @param show true when displaying a temporary route.  N.B. Allocates beyond clearance issues.
     * when false does not allocate beyond a clearance issue.
     * @param set only allocates and sets path in one block, the 'index' block
     * show the entire route but do not set any turnouts in occupied blocks
     */
    private String allocateFromIndex(boolean show, boolean set, int index) {
        int limit;
        if (_partialAllocate || set) {
            limit = Math.min(index + 1, _orders.size());
        } else {
            limit = _orders.size();
        }
        OBlock currentBlock = getBlockOrderAt(_idxCurrentOrder).getBlock();
        if (log.isDebugEnabled()) {
            log.debug("allocateFromIndex({}) block= {} _partialAllocate= {} for warrant \"{}\".",
                    index, currentBlock.getDisplayName(), _partialAllocate, getDisplayName());
        }
        _message = null;
        boolean passageDenied = false;  // cannot allocate beyond this point
        for (int i = index; i < limit; i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
            String msg = block.allocate(this);
            if (msg != null && _message == null) {
                _message = msg;
                if (!this.equals(block.getWarrant())) {
                    _waitForWarrant = true;
                } else {
                    _waitForBlock = true;
                }
                passageDenied = true;
            }
            if (!passageDenied) {
                // loop back routes may enter a block a second time
                // Do not make current block a stopping block
                if (!currentBlock.equals(block)) {
                    if ((block.getState() & OBlock.OCCUPIED) != 0) {  // (!block.isAllocatedTo(this) || ) removed 7/1/18
                        if (_message == null) {
                            _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                        }
                        passageDenied = true;
                    }
                    if (!passageDenied && Warrant.Stop.equals(getPermissibleSpeedAt(bo))) {
                        if (_message == null) {
                            _message = Bundle.getMessage("BlockStopAspect", block.getDisplayName());                        
                        }
                        passageDenied = true;
                    }
                }
                if (!passageDenied && set) {
                    msg = bo.setPath(this);
                    if (msg != null) {
                        if (_message == null) {
                            _message = msg;
                        }
                        passageDenied = true;
                    }
                }
            }
        }
        if (!passageDenied && limit == _orders.size()) {
            _totalAllocated = true;
        }
        return _message;
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
     *
     * @param mode run mode
     */
    public void runWarrant(int mode) {
        if (_partialAllocate) {
            deAllocate();   // allow route to be shared with another warrant
        }
        setRoute(false, null);
        setRunMode(mode, null, null, null, false);
    }

    /**
     * Set the route paths and turnouts for the warrant. Only the first block
     * must be allocated and have its path set. Partial success is OK.
     * A message of the first subsequent block that fails allocation
     * or path setting is written to a field that is
     * displayed in the Warrant List window. When running with block
     * detection, occupation by another train or block 'not in use' or
     * Signals denying movement are reasons
     * for such a message, otherwise only allocation to another warrant
     * prevents total success. Note that warrants sharing their clearance
     * only allocate and set paths one block in advance.
     *
     * @param show  value==1 will ignore _partialAllocate (to show route only)
     *            parm name delay of turnout steting deprecated
     * @param orders  BlockOrder list of route. If null, use permanent warrant
     *            copy.
     * @return message if the first block fails allocation, otherwise null
     */
    public String setRoute(boolean show, List<BlockOrder> orders) {
        // we assume our train is occupying the first block
        _routeSet = false;
        String msg = allocateRoute(show, orders);
        if (msg != null) {
            _message = msg;
            return _message;
        }
        _allocated = true;
        BlockOrder bo = _orders.get(0);
        msg = bo.setPath(this);
        if (msg != null) {
            _message = msg;
            return _message;
        }
        _routeSet = true;   // partially set OK
        if (!_partialAllocate) {
            for (int i = 1; i < _orders.size(); i++) {
                bo = _orders.get(i);
                OBlock block = bo.getBlock();
                if ((block.getState() & OBlock.OCCUPIED) != 0) {
                    if (_message != null) {
                        _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                    }
                    break; // OK. warning status is posted with _message
                }
                if (Warrant.Stop.equals(getPermissibleSpeedAt(bo))) {
                    if (_message != null) {
                        _message = Bundle.getMessage("BlockStopAspect", block.getDisplayName());
                    }
                    break; // OK. warning status is posted with _message
                }
                msg = bo.setPath(this);
                if (msg != null && _message == null) {
                    _message = msg;
                    break; // OK. warning status is posted with _message
                }
            }
        }
        _routeSet = true;
        return null;
    } // setRoute

    /**
     * Check start block for occupied for start of run
     *
     * @return error message, if any
     */
    public String checkStartBlock() {
        if (log.isDebugEnabled()) {
            log.debug("checkStartBlock for warrant \"{}\".", getDisplayName());
        }
        BlockOrder bo = _orders.get(0);
        OBlock block = bo.getBlock();
        String msg = block.allocate(this);
        if (msg != null) {
            return msg;
        }
        msg = bo.setPath(this);
        if (msg != null) {
            return msg;
        }
        int state = block.getState();
        if ((state & OBlock.UNDETECTED) != 0 || _tempRunBlind) {
            msg = "BlockDark";
        } else if ((state & OBlock.OCCUPIED) == 0) {
            msg = "warnStart";
        }
        return msg;
    }

    protected String checkforTrackers() {
        BlockOrder bo = _orders.get(0);
        OBlock block = bo.getBlock();
        Tracker t = InstanceManager.getDefault(TrackerTableAction.class).findTrackerIn(block);
        if (t != null) {
            return Bundle.getMessage("blockInUse", t.getTrainName(), block.getDisplayName());
        }
        return null;
    }
    
    /**
     * Report any occupied blocks in the route
     *
     * @return String
     */
    public String checkRoute() {
        if (log.isDebugEnabled()) {
            log.debug("checkRoute for warrant \"{}\".", getDisplayName());
        }
        OBlock startBlock = _orders.get(0).getBlock();
        for (BlockOrder bo : _orders) {
            OBlock block = bo.getBlock();
            if ((block.getState() & OBlock.OCCUPIED) != 0 && !startBlock.equals(block)) {
                return Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
            }
            Warrant w = block.getWarrant();
            if (w !=null && !this.equals(w)) {
                return Bundle.getMessage("AllocatedToWarrant",
                        w.getDisplayName(), block.getDisplayName(), w.getTrainName());
            }
        }
        return null;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
            return;
        }
        String property = evt.getPropertyName();
        if (log.isDebugEnabled()) {
            log.debug("propertyChange \"{}\" new= {} source= {} - warrant= {}",
                    property, evt.getNewValue(), ((NamedBean) evt.getSource()).getDisplayName(), getDisplayName());
        }

        if (_protectSignal != null && _protectSignal == evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // signal controlling warrant has changed.
                readStoppingSignal();
            }
        } else if (property.equals("state")) {
            if (_stoppingBlock != null && _stoppingBlock.equals(evt.getSource())) {
                // starting block is allocated but not occupied
                if (_delayStart) { // wait for arrival of train to begin the run
                    if ((((Number) evt.getNewValue()).intValue() & OBlock.OCCUPIED) != 0) {
                        // train arrived at starting block
                        Warrant w = _stoppingBlock.getWarrant();
                        if (this.equals(w) || w == null) {
                            if (clearStoppingBlock()) {
                                OBlock block = getBlockAt(_idxCurrentOrder);
                                if (_runMode == MODE_RUN) {
                                    acquireThrottle();
                                } else if (_runMode == MODE_MANUAL) {
                                    fireRunStatus("ReadyToRun", -1, 0);   // ready to start msg
                                    _delayStart = false;
                                } else {
                                    _delayStart = false;
                                    log.error("StoppingBlock \"{}\" set with mode {}", block.getDisplayName(),
                                            MODES[_runMode]);
                                }
                                block._entryTime = System.currentTimeMillis();
                                block.setValue(_trainName);
                                block.setState(block.getState() | OBlock.RUNNING);
                            }
                        }
                    }
                } else if ((((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                    // normal wait for a train underway but blocked ahead by occupation
                    //  blocking occupation has left the stopping block
                    int idx = getIndexOfBlock(_stoppingBlock, _idxLastOrder);
                    if (idx >= 0) {
                        // Wait to allow departing rogue train to clear turnouts before re-allocation
                        // of this warrant resets the path. Rogue may leave on a conflicting path
                        // whose turnout control is shared with this path
                        ThreadingUtil.runOnGUIDelayed(() -> {
                            clearStoppingBlock();
                        }, 7000);   // 7 seconds
                        
                    }
                }
            } else if (_otherShareBlock != null && _otherShareBlock == evt.getSource()) {
                if ((((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                    clearShareTOBlock();
                }
            }
        } else if (_delayStart && property.equals("runMode") && ((Number) evt.getNewValue()).intValue() == MODE_NONE) {
            // Starting block was owned by another warrant for this engine
            // Engine has arrived and Blocking Warrant has finished
            ((Warrant) evt.getSource()).removePropertyChangeListener(this);
            if (clearStoppingBlock()) {
                acquireThrottle();
            }
        }
    } //end propertyChange

    private boolean readStoppingSignal() {
        String speedType;
        if (_protectSignal instanceof SignalHead) {
            SignalHead head = (SignalHead) _protectSignal;
            int appearance = head.getAppearance();
            speedType = jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getAppearanceSpeed(head.getAppearanceName(appearance));
            if (log.isDebugEnabled()) {
                log.debug("SignalHead {} sets appearance speed to {} - warrant= {}",
                        _protectSignal.getDisplayName(), speedType, getDisplayName());
            }
        } else {
            SignalMast mast = (SignalMast) _protectSignal;
            String aspect = mast.getAspect();
            speedType = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAspectSpeed(aspect,
                    mast.getSignalSystem());
            if (log.isDebugEnabled()) {
                log.debug("SignalMast {} sets aspect speed to {} - warrant= {}",
                        _protectSignal.getDisplayName(), speedType, getDisplayName());
            }
        }
        if (speedType == null) {
            return false; // dark or no specified speed
        } else if (speedType.equals(Warrant.Stop)) {
            _waitForSignal = true;
            return true;
        } else {
            _curSpeedType = speedType;
            _waitForSignal = false;
            if (!_waitForBlock && !_waitForWarrant && _engineer != null) {
                allocateFromIndex(false, true, _idxCurrentOrder + 1);
             // engineer will clear its flags when ramp completes
                _engineer.rampSpeedTo(speedType, 0, false);
//                setMovement(END);
                return true;
            }
            fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder);
            return false;
        }
    }

    private boolean doStoppingBlockClear() {
        if (_stoppingBlock == null) {
            return true;
        }
        String blockName = _stoppingBlock.getDisplayName();
        _stoppingBlock.removePropertyChangeListener(_self);
        _stoppingBlock = null;
        if (log.isDebugEnabled())
            log.debug("Warrant \"{}\" Cleared _stoppingBlock= \"{}\".", getDisplayName(), blockName);
        return restoreRunning();
    }

    /**
     * Called when a rogue train has left a block. Allows the warrant to continue to run.
     * Also called from propertyChange() to allow warrant to acquire a throttle
     * and launch an engineer. Also called by retry control command to help user
     * work out of an error condition.
     */
    private boolean clearStoppingBlock() {
        if (_stoppingBlock == null) {
            return false;
        }
        String blockName = _stoppingBlock.getDisplayName();
        if (log.isDebugEnabled())
            log.debug("Warrant \"{}\" clearing _stoppingBlock= \"{}\".",
                getDisplayName(), blockName); 

        String msg = allocateFromIndex(false, true, _idxCurrentOrder + 1);
        if (msg == null && doStoppingBlockClear()) {
            return true;
        }

        if (log.isDebugEnabled())
            log.debug("Warrant \"{}\" allocation failed. {}. runState= {}",
                getDisplayName(), msg, (_engineer!=null?RUN_STATE[_engineer.getRunState()]:"NoEngineer"));
        // If this warrant is waiting for the block that another
        // warrant has occupied, and now the latter warrant leaves
        // the block - there are notifications to each warrant "simultaneously". 
        // The latter warrant's deallocation may not have happened yet and
        // this has prevented allocation to this warrant.  For this case,
        // wait until leaving warrant's deallocation is seen and completed.
        @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification="false postive, guarded by while statement")
        final Runnable allocateBlocks = new Runnable() {
            @Override
            public void run() {
                long time = 0;
                String msg = null;
                try {
                    while (time < 200) {
                        msg = allocateFromIndex(false, true, _idxCurrentOrder + 1);
                        if (msg == null && doStoppingBlockClear()) {
                            break;
                        }
                        synchronized (this) {
                            wait(20);
                            time += 20;
                        }
                    }
                    if (msg != null) {
                        log.warn("Warrant \"{}\" unable to clear StoppingBlock message= \"{}\" time= {}", getDisplayName(), msg, time);
                    }
                    _message = msg;
                }
                catch (InterruptedException ie) {
                    log.warn("Warrant \"{}\" InterruptedException message= \"{}\" time= {}", getDisplayName(), ie.toString(), time);
                    Thread.currentThread().interrupt();
                }
                if (log.isDebugEnabled())
                    log.debug("Warrant \"{}\" waited {}ms for clearStoppingBlock to allocateFrom {}",
                           getDisplayName(), time, getBlockAt(_idxCurrentOrder + 1).getDisplayName());
            }
        };
         
        synchronized (allocateBlocks) {
            Thread doit = new Thread() {
                @Override
                public void run() {
                    try {
                        javax.swing.SwingUtilities.invokeAndWait(allocateBlocks);
                    }
                    catch (Exception e) {
                        log.error("Exception in allocateBlocks", e);
                    }
                }
            };
            doit.start();
        }
        return true;
    }

    private boolean restoreRunning() {
        int runState = -1;
        if (_engineer != null) {
            runState = _engineer.getRunState();
            if (log.isDebugEnabled()) {
                log.debug("restoreRunning(): rampSpeedTo to \"{}\". runState= {}. warrant= {}",
                        _curSpeedType, RUN_STATE[runState], getDisplayName());
            }
            if (runState == HALT || runState == RAMP_HALT) {
                _waitForBlock = true;                    
            } else {
                _waitForBlock = false;
                _waitForWarrant = false;
            }
            if (!_waitForSignal && !_waitForBlock && !_waitForWarrant) {
                getBlockOrderAt(_idxCurrentOrder).setPath(this);
                _engineer.rampSpeedTo(_curSpeedType, 0, false);
                //setMovement(MID);
            }
            return true;
        }
        return false;
    }

    /**
     * block (nextBlock) sharing a turnout with _shareTOBlock is already
     * allocated.
     */
    private void clearShareTOBlock() {
        if (_otherShareBlock == null) {
            return;
        }
        _otherShareBlock.removePropertyChangeListener(this);
        String msg = _orders.get(getIndexOfBlock(_myShareBlock, _idxCurrentOrder)).setPath(this);
        if (log.isDebugEnabled()) {
            log.debug("_otherShareBlock= \"{}\" Cleared. {}",
                    _otherShareBlock.getDisplayName(), (msg==null?"":"msg"));
        }
        _otherShareBlock = null;
        _myShareBlock = null;
        if (_waitForWarrant) {
            _waitForWarrant = false;
            restoreRunning();
        }
    }

    /**
     * Callback from trying to setPath() for this warrant. This warrant's Oblock
     * notices that another warrant has its path set and uses a turnout also
     * used by the current path of this. Rights to the turnout must be
     * negotiated, otherwise warrants may deadlock or derail a train.
     *
     * @param block block of another warrant that has a path set
     * @param myBlock block in this warrant sharing a TO with 'block'
     */
    protected void setShareTOBlock(OBlock block, OBlock myBlock) {
        OBlock prevBlk = _otherShareBlock;
        if (_myShareBlock != null) {
            if (_myShareBlock.equals(myBlock)) {
                return;
            }
            int idxBlock = getIndexOfBlock(myBlock, _idxCurrentOrder);
            int idxStop = getIndexOfBlock(_myShareBlock, _idxCurrentOrder);
            if (idxStop < idxBlock && idxStop >= 0) {
                return;
            }
            _otherShareBlock.removePropertyChangeListener(this);
        }
        _myShareBlock = myBlock;
        _otherShareBlock = block;
        _otherShareBlock.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) {
            String msg = "Warrant \"{}\" sets _shareTOBlock= \"{}\" owned by warrant \"{}\"";
            if (prevBlk != null) {
                msg = msg + ", removes \"{}\"";
            }
            log.debug(msg, getDisplayName(), block.getDisplayName(), block.getWarrant(),
                    (prevBlk == null ? "" : prevBlk.getDisplayName()));
        }
    }

    /**
     * Stopping block only used in MODE_RUN _stoppingBlock is an occupied OBlock
     * preventing the train from continuing the route
     * <p>
     */
    private void setStoppingBlock(OBlock block) {
        if (block == null) {
            return;
        }
        int idxBlock = getIndexOfBlock(block, _idxCurrentOrder);
        // _idxCurrentOrder == 0 may be a delayed start waiting for loco.
        // Otherwise don't set _stoppingBlock for a block occupied by train
        if (idxBlock < 0 || (_idxCurrentOrder == idxBlock && _idxCurrentOrder > 0)) {
            return;
        }
        OBlock prevBlk = _stoppingBlock;
        if (_stoppingBlock != null) {
            if (_stoppingBlock.equals(block)) {
                return;
            }

            int idxStop = getIndexOfBlock(_stoppingBlock, _idxCurrentOrder);
            if ((idxBlock < idxStop) || idxStop < 0) {
                prevBlk.removePropertyChangeListener(this);
            } else {
                return;
            }
        }
        _stoppingBlock = block;
        _stoppingBlock.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) {
            String msg = "Warrant \"{}\" sets _stoppingBlock= \"{}\"";
            if (prevBlk != null) {
                msg = msg + ", removes \"{}\"";
            }
            log.debug(msg, getDisplayName(), _stoppingBlock.getDisplayName(),
                    (prevBlk == null ? "" : prevBlk.getDisplayName()));
        }
    }

    private void setStoppingSignal(int idx) {
        BlockOrder blkOrder = getBlockOrderAt(idx);
        NamedBean signal = blkOrder.getSignal();

        NamedBean prevSignal = _protectSignal;
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
                    _protectSignal = null;
                    _idxProtectSignal = -1;
                }
            }
        }

        if (signal != null) {
            _protectSignal = signal;
            _idxProtectSignal = idx;
            _protectSignal.addPropertyChangeListener(this);
        }
        if (log.isDebugEnabled()) {
            if (signal == null && prevSignal == null) {
                return;
            }
            String msg = "Block \"{}\" Warrant \"{}\"";
            if (signal != null) {
                msg = msg + " sets _protectSignal= \"" + _protectSignal.getDisplayName() + "\"";
            }
            if (prevSignal != null) {
                msg = msg + ", removes signal= \"" + prevSignal.getDisplayName() + "\"";
            }
            log.debug(msg, blkOrder.getBlock().getDisplayName(), getDisplayName());
        }
    }

    /**
     * Check if this is the next block of the train moving under the warrant
     * Learn mode assumes route is set and clear. Run mode update conditions.
     * <p>
     * Must be called on Layout thread.
     *
     * @param block Block in the route is going active.
     */
    @jmri.InvokeOnLayoutThread
    protected void goingActive(OBlock block) {
        // error if not on Layout thread
        if (!ThreadingUtil.isLayoutThread()) {
            log.error("invoked on wrong thread", new Exception("traceback"));
        }

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
        int runState = -1;
        if (_engineer != null) {
            runState = _engineer.getRunState();
        }
        if (activeIdx == _idxCurrentOrder) {
            // Unusual case of current block losing detection, then regaining it.  i.e. dirty track, derail etc.
            // Also, can force train to move into occupied block with "Move into next Block" command.
            // This is an unprotected move.
            if (_engineer != null && runState != WAIT_FOR_CLEAR && runState != HALT) {
                // Ordinarily block just occupied would be this train, but train is stopped! - could be user's retry.
                log.info("Train {} regained detection at Block= {}", getTrainName(), block.getDisplayName());
                _engineer.rampSpeedTo(_curSpeedType, 0, false); // maybe let setMovement() do this?
            }
        } else if (activeIdx == _idxCurrentOrder + 1) {
            if (_delayStart || (runState == HALT && _engineer.getSpeedSetting() > 0.0f)) {
                log.warn("Rogue entered Block \"{}\" ahead of {}.", block.getDisplayName(), getTrainName());
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return;
            }
            // be sure path is set for train in this block
            String msg = getBlockOrderAt(activeIdx).setPath(this);
            if (msg != null) {
                log.error("goingActive setPath fails: {}", msg);
            }
            if (_engineer != null && _engineer.getSpeedSetting() <= 0.0f) {
                // Train can still be moving after throttle set to 0. Block 
                // boundaries can be crossed.  This is due to momentum 'gliding'
                // for any nonE-Stop or by choosing ramping to a stop.
                // spotbugs knows runState != HALT here
                if (runState != WAIT_FOR_CLEAR && runState != STOP_PENDING && runState != RAMP_HALT) {
                    // Apparently NOT already stopped or just about to be.
                    // Therefore, assume a Rogue has just entered.
                    setStoppingBlock(block);
                    _engineer.setWaitforClear(true);
                    _engineer.setSpeedToType(Warrant.Stop);     // for safety
                    return;
                }
            }
            // Since we are moving we assume it is our train entering the block
            // continue on.
            _idxLastOrder = _idxCurrentOrder;
            _idxCurrentOrder = activeIdx;
        } else if (activeIdx > _idxCurrentOrder + 1) {
            if (_runMode == MODE_LEARN) {
                log.error("Block \"{}\" became occupied before block \"{}\". ABORT recording.",
                        block.getDisplayName(), getBlockAt(_idxCurrentOrder + 1).getDisplayName());
                fireRunStatus("abortLearn", activeIdx, _idxCurrentOrder);
                return;
            }
            // if previous blocks are dark, this could be for our train
            // check from current block to this just activated block
            for (int idx = _idxCurrentOrder + 1; idx < activeIdx; idx++) {
                OBlock preBlock = getBlockAt(idx);
                if ((preBlock.getState() & OBlock.UNDETECTED) == 0) {
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
            // Indicate the previous dark block was entered
            OBlock prevBlock = getBlockAt(activeIdx - 1);
            prevBlock._entryTime = System.currentTimeMillis() - 500; // arbitrarily say
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
        setHeadOfTrain(block);
        fireRunStatus("blockChange", getBlockAt(activeIdx - 1), block);
        if (_engineer != null) {
            _engineer.clearWaitForSync(); // Sync commands if train is faster than ET
        }
        // _idxCurrentOrder has been incremented. Warranted train has entered this block.
        // Do signals, speed etc.
        if (_idxCurrentOrder < _orders.size() - 1) {
            allocateFromIndex(false, true, _idxCurrentOrder + 1);
            if (_engineer != null) {
                BlockOrder bo = _orders.get(_idxCurrentOrder + 1);
                if ((bo.getBlock().getState() & OBlock.UNDETECTED) != 0) {
                    // can't detect next block, use ET
                    _engineer.setRunOnET(true);
                } else if (!_tempRunBlind) {
                    _engineer.setRunOnET(false);
                }
            }
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
        if (log.isTraceEnabled()) {
            log.debug("end of goingActive. leaving \"{}\" entered \"{}\". warrant {}",
                    getBlockAt(activeIdx - 1).getDisplayName(), block.getDisplayName(), getDisplayName());
        }
        setMovement(BEG);
    } //end goingActive

    private void setHeadOfTrain(OBlock block ) {
        block.setValue(_trainName);
        block.setState(block.getState() | OBlock.RUNNING);
        block._entryTime = System.currentTimeMillis();
        if (_runMode == MODE_RUN) {
            _speedUtil.enteredBlock(_idxLastOrder, _idxCurrentOrder);
        }
    }
    /**
     * @param block Block in the route is going Inactive
     */
    @jmri.InvokeOnLayoutThread
    protected void goingInactive(OBlock block) {
        if (_runMode == MODE_NONE) {
            return;
        }

        // error if not on Layout thread
        if (!ThreadingUtil.isLayoutThread()) {
            log.error("invoked on wrong thread", new Exception("traceback"));
        }

        int idx = getIndexOfBlockBefore(_idxCurrentOrder, block); // if idx >= 0, it is in this warrant
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
                            _idxCurrentOrder++; // assume train has moved into the dark block
                            fireRunStatus("blockChange", block, nextBlock);
                        } else if (_runMode == MODE_RUN) {
                            controlRunTrain(ABORT);
                        }
                    }
                } else {
                    if ((nextBlock.getState() & OBlock.OCCUPIED) != 0 && (_waitForBlock || _waitForWarrant)) {
                        // assume train rolled into occupied ahead block. 
                        // Should _idxCurrentOrder & _idxLastOrder be incremented? Better to let user take control?
                        releaseBlock(block, idx);
                        setHeadOfTrain(nextBlock);
                        fireRunStatus("blockChange", block, nextBlock);
                        log.warn("block \"{}\" goingInactive. train has entered rogue occupied block {}! warrant {}",
                                block.getDisplayName(), nextBlock.getDisplayName(), getDisplayName());
                   } else {
                       boolean lost = true;
                       if (_idxCurrentOrder > 0) {
                           OBlock prevBlock = getBlockAt(_idxCurrentOrder - 1);
                           if ((prevBlock.getState() & OBlock.OCCUPIED) != 0 && this.equals(prevBlock.getWarrant())) {
                               // assume nosed into block, then lost contact
                               _idxCurrentOrder -= 1;  // set head to previous BlockOrder
                               lost = false;
                           }                         
                       }
                       if (lost) {
                           log.warn("block \"{}\" goingInactive. train is lost! warrant {}",
                                       block.getDisplayName(), getDisplayName());
                           fireRunStatus("blockChange", block, null);
                           if (_engineer != null) {
                               _engineer.setStop(false, true);   // halt and set 0 throttle
                               if (_idxCurrentOrder == 0) {
                                   setStoppingBlock(block);
                                   _delayStart = true;
                               }
                           }
                       }
                   }
                }
            } else {    // at last block
                 OBlock prevBlock = getBlockAt(_idxCurrentOrder - 1);
                if ((prevBlock.getState() & OBlock.OCCUPIED) != 0 && this.equals(prevBlock.getWarrant())) {
                    // assume nosed into block, then lost contact
                    _idxCurrentOrder -= 1;  // set head to previous BlockOrder
                } else {
                    log.warn("block \"{}\" Last Block goingInactive. train is lost! warrant {}",
                            block.getDisplayName(), getDisplayName());
                    if (_engineer != null) {    
                        _engineer.setStop(false, false);   // set 0 throttle
                    }
                }
            }
        }
    } // end goingInactive

    /**
     * Deallocates all blocks prior to and including block
     */
    private void releaseBlock(OBlock block, int idx) {
        /*
         * Only deallocate block if train will not use the block again. Blocks
         * ahead could loop back over blocks previously traversed. That is,
         * don't disturb re-allocation of blocks ahead. Previous Dark blocks do
         * need deallocation
         */
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
                _totalAllocated = false;
                fireRunStatus("blockRelease", null, block);
            }
        }
    }

    /**
     * build map of BlockSpeedInfo's for the route.
     * <p>
     * Put max speed and time in block's first occurrence after current command
     * index
     */
    private void getBlockSpeedTimes() {
        _speedInfo = new ArrayList<>();
        String blkName = null;
        float firstSpeed = 0.0f; // used for entrance
        float maxSpeed = 0.0f;
        float lastSpeed = 0.0f;
        long blkTime = 0;
        int firstIdx = 0; // for all blocks except first, this is index of NOOP command
        boolean hasSpeedChange = false;
        for (int i = 0; i < _commands.size(); i++) {
            ThrottleSetting ts = _commands.get(i);
            String cmd = ts.getCommand().toUpperCase();
            if (cmd.equals("NOOP")) {
                // make map entry
                blkTime += ts.getTime();
                _speedInfo.add(new BlockSpeedInfo(firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, i));
                if (log.isDebugEnabled()) {
                    log.debug("block: {} speeds: entrance= {} max= {} exit= {} time: {}ms. index {} to {}",
                            blkName, firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, i);
                }
                blkName = ts.getBeanDisplayName();
                blkTime = 0;
                firstSpeed = lastSpeed;
                maxSpeed = lastSpeed;
                hasSpeedChange = false;
                firstIdx = i + 1; // first in next block is next index
            } else { // collect block info
                blkTime += ts.getTime();
                if (cmd.equals("SPEED")) {
                    lastSpeed = Float.parseFloat(ts.getValue());
                    if (hasSpeedChange) {
                        if (lastSpeed > maxSpeed) {
                            maxSpeed = lastSpeed;
                        }
                    } else {
                        hasSpeedChange = true;
                    }
                }
            }
        }
        _speedInfo.add(new BlockSpeedInfo(firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, _commands.size() - 1));
        if (log.isDebugEnabled()) {
            log.debug("block: {} speeds: entrance= {} max= {} exit= {} time: {}ms. index {} to {}",
                    blkName, firstSpeed, maxSpeed, lastSpeed, blkTime, firstIdx, (_commands.size() - 1));
        }
    }

    static class BlockSpeedInfo {

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

        // Throttle setting at entrance of block
        float getEntranceSpeed() {
            return entranceSpeed;
        }

        // Maximum throttle setting in block
        float getMaxSpeed() {
            return maxSpeed;
        }

        // Throttle setting at exit of block
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

    // utility for Engineer to look ahead and find ramp up speed it should match
    protected float getEntranceSpeed(OBlock block) {
        return _speedInfo.get(getIndexOfBlock(block, _idxCurrentOrder)).getEntranceSpeed();
    }

    /**
     * Finds speed change type at entrance of a block. Called by:
     *
     * @return a speed type or null for continue at current type
     */
    private String getPermissibleSpeedAt(BlockOrder bo) {
        OBlock block = bo.getBlock();
        String speedType = bo.getPermissibleEntranceSpeed();
        if (speedType != null) {
            if (log.isDebugEnabled()) {
                log.debug("getPermissibleSpeedAt(): \"{}\" Signal speed= {} warrant= {}",
                        block.getDisplayName(), speedType, getDisplayName());
            }
            return speedType;
        } else { //  if signal is configured, ignore block
            speedType = block.getBlockSpeed();
            if (speedType.equals("")) {
                speedType = null;
            }
            if (speedType != null) {
                if (log.isDebugEnabled()) {
                    log.debug("getPermissibleSpeedAt(): \"{}\" Block speed= {} warrant= {}",
                            block.getDisplayName(), speedType, getDisplayName());
                }
            }
            return speedType;
        }
    }

    synchronized private void cancelDelayRamp() {
        if (_delayCommand != null) {
            _delayCommand.interrupt();
            log.debug("cancelDelayRamp called on warrant {}", getDisplayName());
            _delayCommand = null;
        }
    }

    synchronized private void rampDelayDone() {
        _delayCommand = null;
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

    private class CommandDelay extends Thread {

        String nextSpeedType;
        long _startTime = 0;
        long _waitTime = 0;
        boolean quit = false;
        int _endBlockIdx;
        boolean _useIndex;

        CommandDelay(String speedType, long startWait, int endBlockIdx, boolean useIndex) {
            nextSpeedType = speedType;
            if (startWait > 0) {
                _waitTime = startWait;
            }
            _endBlockIdx = endBlockIdx;
            _useIndex = useIndex;
            setName("CommandDelay(" + getTrainName() +")");
            if (log.isDebugEnabled()) {
                log.debug("CommandDelay: will wait {}ms, then Ramp to {} in block {}. warrant {}",
                        startWait, speedType, getBlockAt(endBlockIdx).getDisplayName(), getDisplayName());
            }
        }

        // check if request for a duplicate CommandDelay can be cancelled
        boolean doNotCancel(String speedType, long startWait, int endBlockIdx) {
            if (endBlockIdx == _endBlockIdx && speedType.equals(nextSpeedType) &&
                    (_waitTime - (System.currentTimeMillis() - _startTime)) < startWait) {
                return true;
            }
            return false;   // not a duplicate or shortens time wait.
        }

        @Override
        @SuppressFBWarnings(value = "WA_NOT_IN_LOOP", justification = "notify never called on this thread")
        public void run() {
            _startTime = System.currentTimeMillis();
            synchronized (this) {
                if (_waitTime > 0.0) {
                    try {
                        wait(_waitTime);
                    } catch (InterruptedException ie) {
                        if (log.isDebugEnabled()) {
                            log.debug("CommandDelay interrupt.  Ramp to {} not done. warrant {}",
                                    nextSpeedType, getDisplayName());
                        }
                        quit = true;
                    }
                }
                if (!quit && _engineer != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("CommandDelay: after wait of {}ms, start Ramp to {}. warrant {}",
                                _waitTime, nextSpeedType, getDisplayName());
                    }
                    _engineer.rampSpeedTo(nextSpeedType, _endBlockIdx, _useIndex);
                    // start ramp first
                    if (nextSpeedType.equals(Stop) || nextSpeedType.equals(EStop)) {
                        _engineer.setWaitforClear(true);
                    } else {
                        _curSpeedType = nextSpeedType;
                    }
                }
            }
            rampDelayDone();
        }
    }

    /**
     * Orders are parsed to get any speed restrictions
     * @param idxBlockOrder index of Orders
     * @return Speed type name
     */
    private String getSpeedTypeForBlock(int idxBlockOrder) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlockOrder);
        OBlock block = blkOrder.getBlock();

        String speedType = getPermissibleSpeedAt(blkOrder);
        setStoppingSignal(idxBlockOrder);
        if (speedType == null) {
            speedType = _curSpeedType;
        } else {
            if (speedType.equals(Warrant.Stop)) {
                // block speed cannot be Stop, so OK to assume signal
                _waitForSignal = true;
                speedType = Warrant.Stop;
            }
        }

        String msg = block.allocate(this);
        if (msg != null) {
            if (!this.equals(block.getWarrant())) {
                _waitForWarrant = true;
            } else {
                _waitForBlock = true;
            }
            setStoppingBlock(block);
            speedType = Warrant.Stop;
        } else if ((block.getState() & OBlock.OCCUPIED) != 0) {
            if (idxBlockOrder > _idxCurrentOrder) {
                setStoppingBlock(block);
                _waitForBlock = true;
                speedType = Warrant.Stop;
            }
        } else {
            msg = blkOrder.setPath(this);
            if (msg != null) {
                // when setPath fails, it calls setShareTOBlock
                _waitForWarrant = true;
                speedType = Warrant.Stop;
                _message = msg;
                log.warn(msg);
            }
        }

        if (log.isDebugEnabled()) {
            if (_waitForSignal || _waitForBlock || _waitForWarrant) {
                log.debug ("Found \"{}\" speed change of type \"{}\" needed to enter block \"{}\".",
                        (_waitForSignal?"Signal":(_waitForWarrant?"Warrant":"Block")), speedType, block.getDisplayName());
            }
        }
        return speedType;
    }

    private float getPathLength(BlockOrder bo) {
        float len = bo.getPath().getLengthMm();
        if (len < 1.0f) {
            log.warn(Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName()));
            len = 100;
        }
        return len;
    }

    /*
     * position only applies to current block otherwise full path length is available
     */
    private float getAvailableDistance(int idxBlockOrder, int position) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlockOrder);
        float distAvail;
        float pathLength = getPathLength(blkOrder);
        distAvail = pathLength - _speedUtil.getDistanceTravelled();
        if (log.isDebugEnabled()) {
            log.debug("getAvailableDistance(): from {} in block \"{}\" distAvail= {} warrant= {}",
                    position, blkOrder.getBlock().getDisplayName(), distAvail, getDisplayName());
        }
        return distAvail;
    }
    
    /**
     * Get ramp length needed to change speed using the WarrantPreference deltas for 
     * throttle increment and time increment.  This should only be used for ramping down.
     * @param idxBlockOrder index of BlockOrder
     * @param toSpeedType Speed type change
     * @return distance in millimeters
     */
    private float rampLengthOfEntrance(int idxBlockOrder, String toSpeedType) {
/*        if (_curSpeedType.equals(toSpeedType)) {
            return 0.0f;
        }*/
        BlockSpeedInfo blkSpeedInfo = _speedInfo.get(idxBlockOrder);
        float throttleSetting = blkSpeedInfo.getEntranceSpeed();
        float rampLen = _speedUtil.rampLengthForRampDown(throttleSetting, _curSpeedType, toSpeedType);
        if (_waitForBlock || _waitForWarrant) {    // occupied or foreign track ahead.
            rampLen *= RAMP_ADJUST;    // provide more space to avoid collisions
        } else if (_waitForSignal) {        // signal restricting speed
            rampLen += getBlockOrderAt(idxBlockOrder).getEntranceSpace(); // signal's adjustment
        }
        return rampLen;
    }

    private static float RAMP_ADJUST = 1.05f;

    /**
     * Called to set the correct speed for the train when the scripted speed
     * must be modified due to a track condition (signaled speed or rogue
     * occupation). Also called to return to the scripted speed after the
     * condition is cleared. Assumes the train occupies the block of the current
     * block order.
     * <p>
     * Looks for speed requirements of this block and takes immediate action if
     * found. Otherwise looks ahead for future speed change needs. If speed
     * restriction changes are required to begin in this block, but the change
     * is not immediate, then determine the proper time delay to start the speed
     * change.
     *
     * @param position estimated position of train inn the block
     * @return false on errors
     */
    private boolean setMovement(int position) {
        if (_runMode != Warrant.MODE_RUN || _idxCurrentOrder > _orders.size() - 1) {
            return false;
        }
        if (_engineer == null) {
            controlRunTrain(ABORT);
            return false;
        }
        int runState = _engineer.getRunState();
        BlockOrder blkOrder = getBlockOrderAt(_idxCurrentOrder);
        OBlock curBlock = blkOrder.getBlock();
        if (log.isDebugEnabled()) {
            log.debug("!-setMovement({}) Block\"{}\" runState= {} current speedType= {} {} warrant {}.",
                    position, curBlock.getDisplayName(), RUN_STATE[runState], _curSpeedType,
                    (_partialAllocate ? "ShareRoute" : ""), getDisplayName());
        }

        String msg = blkOrder.setPath(this);
        if (msg != null) {
            log.error("Train {} in block \"{}\" but path cannot be set! msg= {}, warrant= {}",
                    getTrainName(), curBlock.getDisplayName(), msg, getDisplayName());
            _engineer.setStop(false, true);   // speed set to 0.0 (not E-top) User must restart
            return false;
        }

        if ((curBlock.getState() & (OBlock.OCCUPIED | OBlock.UNDETECTED)) == 0) {
            log.error("Train {} expected in block \"{}\" but block is unoccupied! warrant= {}",
                    getTrainName(), curBlock.getDisplayName(), getDisplayName());
            _engineer.setStop(false, true); // user needs to see what happened and restart
            return false;
        }
        // Error checking done.

        // checking situation for the current block
        // _curSpeedType is the speed type train is currently running
        // currentType is the required speed limit for this block
        String currentType = getPermissibleSpeedAt(blkOrder);
        if (currentType == null) {
            currentType = _curSpeedType;
        }

        float speedSetting = _engineer.getSpeedSetting();
        if (log.isDebugEnabled()) {
            log.debug("Stopping flags: _waitForBlock={}, _waitForSignal={}, _waitForWarrant={} runState= {}, speedSetting= {}, SpeedType= {}. warrant {}",
                    _waitForBlock, _waitForSignal, _waitForWarrant, RUN_STATE[runState], speedSetting, currentType, getDisplayName());
        }

        if (_noRamp || _speedInfo == null) {
            if (_idxCurrentOrder < _orders.size() - 1) {
                currentType = getSpeedTypeForBlock(_idxCurrentOrder + 1);
                if (_speedUtil.secondGreaterThanFirst(currentType, _curSpeedType)) {
                    if (log.isDebugEnabled()) {
                        log.debug("No ramp speed change of \"{}\" from \"{}\" in block \"{}\" warrant= {}",
                                currentType, _curSpeedType, curBlock.getDisplayName(), getDisplayName());
                    }
                    _engineer.setSpeedToType(currentType);
                    if (!currentType.equals(Stop) && !currentType.equals(EStop)) {
                        _curSpeedType = currentType;
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Exit setMovement due to no ramping. warrant= {}", getDisplayName());
            }
            return true;
        }

        if (!currentType.equals(_curSpeedType)) {
            if (_speedUtil.secondGreaterThanFirst(currentType, _curSpeedType)) {
                // currentType Speed violation!
                // Cancel any delayed speed changes currently in progress.
                cancelDelayRamp();
                jmri.NamedBean signal = blkOrder.getSignal();
                String name;
                if (signal != null) {
                    name = signal.getDisplayName();
                } else {
                    name = curBlock.getDisplayName();
                }
                if (currentType.equals(Stop) || currentType.equals(EStop)) {
                    _engineer.setStop(currentType.equals(EStop), false);   // sets speed 0
                    log.info("Train missed Stop signal {} in block \"{}\". warrant= {}", 
                                name, curBlock.getDisplayName(), getDisplayName());
                    fireRunStatus("SpeedRestriction", name, currentType); // message of speed violation
                    return true; // don't do anything else until stopping condition cleared
                } else {
                    _curSpeedType = currentType;
                    _engineer.setSpeedToType(currentType); // immediate decrease
                    if (log.isDebugEnabled()) {
                        log.debug(
                        "Train {} moved past required speed of \"{}\" at speed \"{}\" in block \"{}\"! Set speed to {}. warrant= {}",
                                getTrainName(), currentType, _curSpeedType, curBlock.getDisplayName(), currentType,
                                getDisplayName());
                    }
                    fireRunStatus("SpeedRestriction", name, currentType); // message of speed violation
                 }
            } else {    // speed increases types currentType >_curSpeedType
                // Cancel any delayed speed changes currently in progress.
                cancelDelayRamp();
                if (log.isTraceEnabled()) {
                    log.trace("Increasing speed to \"{}\" from \"{}\" in block \"{}\" warrant= {}",
                            currentType, _curSpeedType, curBlock.getDisplayName(), getDisplayName());
                }
                _curSpeedType = currentType; // cannot be Stop or EStop
                _engineer.rampSpeedTo(currentType, 0, false);
            }
            // continue, there may be blocks ahead that need a speed decrease to begin in this block
        } else {
            if (runState == WAIT_FOR_CLEAR || runState == HALT) {
                // trust that STOP_PENDING or RAMP_HALT makes hard stop unnecessary
                cancelDelayRamp();
                _engineer.setStop(false, false);
                if (log.isDebugEnabled()) {
                    log.debug("Set Stop to hold train at block \"{}\" runState= {}, speedSetting= {}.warrant {}",
                            curBlock.getDisplayName(), RUN_STATE[runState], speedSetting, getDisplayName());
                }
                fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder); // message reason for hold
                return true;
            } else if (runState == STOP_PENDING || runState == RAMP_HALT) {
                if (log.isDebugEnabled()) {
                    log.debug("Hold train at block \"{}\" runState= {}, speedSetting= {}.warrant {}",
                            curBlock.getDisplayName(), RUN_STATE[runState], speedSetting, getDisplayName());
                }
                fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder); // message reason for hold
                return true;
            }
            // Continue, look ahead for possible speed modification decrease.
        }

        //look ahead for a speed change slower than the current speed
        // Note: blkOrder still is blkOrder = getBlockOrderAt(_idxCurrentOrder);
        // Do while speedType >= currentType
        int idxBlockOrder = _idxCurrentOrder;
        String speedType = _curSpeedType;
        float availDist = getAvailableDistance(idxBlockOrder, position);
        // getSpeedTypeForBlock() sets flags if speed change is detected
        while (!_speedUtil.secondGreaterThanFirst(speedType, _curSpeedType) && idxBlockOrder < _orders.size() - 1) {
            speedType = getSpeedTypeForBlock(++idxBlockOrder);  // speed for entry into next block
            availDist += getPathLength(getBlockOrderAt(idxBlockOrder));
        }

        if (idxBlockOrder == _orders.size() - 1) {
            // went through remaining BlockOrders,
            if (!_speedUtil.secondGreaterThanFirst(speedType, _curSpeedType)) {
                // found no speed decreases, except for possibly the last
                float curSpeedRampLen = _speedUtil.rampLengthForRampDown(speedSetting, _curSpeedType, Stop);
                if (log.isDebugEnabled()) {
                    if (_curSpeedType.equals(speedType)) {
                        log.debug("No speed modifications for runState= {} from {} found after block \"{}\". RampLen = {} warrant {}",
                                RUN_STATE[runState], _curSpeedType, curBlock.getDisplayName(), curSpeedRampLen, getDisplayName());
                    }
                }
                if (curSpeedRampLen > availDist) {
                    log.info("Throttle at {} needs {}mm to ramp to Stop but only {}mm available. May Overrun. Warrant {}",
                            speedSetting, curSpeedRampLen, availDist, getDisplayName());
                    if (runState == STOP_PENDING || runState == RAMP_HALT) {
                        // ramp to stop in progress - no need for further stop calls
                         return true;
                    }
                } // else {Space exceeded when ramping down over several blocks is not catastrophic - let it be}
                _waitForBlock = false;
                _waitForSignal = false;
                _waitForWarrant = false;
                return true;
            }
        }

        // blkOrder.getBlock() is the block that must be entered at a speed modified by speedType.  
        blkOrder = getBlockOrderAt(idxBlockOrder);
        if (log.isDebugEnabled()) {
            log.debug("Speed decrease to {} from {} needed before entering block \"{}\" warrant {}",
                    speedType, _curSpeedType, blkOrder.getBlock().getDisplayName(), getDisplayName());
        }
        // There is a speed decrease needed before entering the above block
        // From this block, check if there is enough room to make the change
        // using the exit speed of the block. Walk back using previous
        // blocks, if necessary.
        float rampLen = rampLengthOfEntrance(idxBlockOrder, speedType);
        availDist = 0.0f;
        int endBlockIdx = idxBlockOrder - 1;    // index of block where ramp ends

        while (idxBlockOrder > _idxCurrentOrder && rampLen > availDist) {
            // start at block before the block with slower speed type and walk
            // back to current block. Get availDist to cover rampLen
            idxBlockOrder--;
            if (idxBlockOrder == 0) {
                availDist += getAvailableDistance(0, MID);
            } else {
                availDist += getPathLength(getBlockOrderAt(idxBlockOrder));
            }
            rampLen = rampLengthOfEntrance(idxBlockOrder, speedType);
        }
        if (log.isDebugEnabled()) {
            log.debug("availDist= {}, at Block \"{}\" for rampLen= {}. warrant {}",
                    availDist, getBlockOrderAt(idxBlockOrder).getBlock().getDisplayName(), rampLen, getDisplayName());
        }

        if (idxBlockOrder > _idxCurrentOrder && rampLen <= availDist) {
            // sufficient ramp room was found before walking all the way back to current block
            // therefore no speed change needed yet.
            if (log.isDebugEnabled()) {
                log.debug("Will decrease speed for runState= {} to {} from {} later at block \"{}\", warrant {}",
                        RUN_STATE[runState], speedType, _curSpeedType, blkOrder.getBlock().getDisplayName(),
                        getDisplayName());
            }
            _waitForBlock = false;
            _waitForWarrant = false;
            _waitForSignal = false;
            return true; // change speed later
        }

        // Need to start a ramp down in the block of _idxCurrentOrder
        blkOrder = getBlockOrderAt(_idxCurrentOrder);
        if (log.isDebugEnabled()) {
            log.debug("Schedule speed change to {} in block \"{}\" availDist={}, warrant= {}",
                    speedType, blkOrder.getBlock().getDisplayName(), availDist, getDisplayName());
        }
        float signalDistAdj = blkOrder.getEntranceSpace() + 50f; // signal's adjustment plus ~2"
        // find the time when ramp should start in this block, then use thread CommandDelay  
        // ramp length to change to modified speed from current speed.

        runState = _engineer.getRunState();
        if (runState == RAMPING_UP) {
            // Interrupt ramp up with ramp down in time to reach 0 speed.
            float endSpeed = _speedUtil.modifySpeed(_engineer.getScriptSpeed(), speedType);
            speedSetting = _engineer.getSpeedSetting();
            RampData rampdata = _engineer.getRamp().getRampData();
            ListIterator<Float> iter = rampdata.speedIterator(true);
            while (iter.hasNext() && iter.next().floatValue() < speedSetting) {}

            int timeIncrement = rampdata.getRampTimeIncrement();
            float topSpeed = speedSetting;
            float rampUpDist = 0.0f;
            // Get distances that match where speed changes
            float rampDownDist = _speedUtil.getRampForSpeedChange(topSpeed, endSpeed).getRampLength();
            long time = 0;
            // continue to ramp up while building the ramp down until distances exceed availDist 
            while ((rampDownDist + rampUpDist) <= availDist) {
                time += timeIncrement;
                if (iter.hasNext()) {
                    topSpeed = iter.next().floatValue();
                } else {
                    break;
                }
                rampUpDist = _speedUtil.getRampForSpeedChange(speedSetting, topSpeed).getRampLength();
                rampDownDist = _speedUtil.getRampForSpeedChange(topSpeed, endSpeed).getRampLength();
                if (_waitForSignal) {
                    rampDownDist += signalDistAdj; // signal's adjustment
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("rampDown delayTime= {}, availDist= {} rampUpDist= {}, from= {} to {}, rampDownDist= {}, down to {}",
                        time, availDist, rampUpDist, speedSetting, topSpeed, rampDownDist, endSpeed);
            }
            rampSpeedDelay(time, speedType, endBlockIdx);
            fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder);
            return true;
       } else if (rampLen >= availDist) { // not enough room (shouldn't happen) so do ramp/stop immediately
            log.warn("No room for Train {} ramp to speed \"{}\" in block \"{}\", warrant= {}",
                    getTrainName(), speedType, curBlock.getDisplayName(), getDisplayName());
            _engineer.setSpeedToType(speedType);
            if (!speedType.equals(Stop) && !speedType.equals(EStop)) {
                _curSpeedType = speedType;
            }
            fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder);
            return true;
        }
        
        // otherwise, figure out time to ramp down from script
        // waitSpeed is throttleSpeed when ramp is started. Start with it being at the entrance to the block.
        float waitSpeed = 0;
        long waitTime = 0; // time to wait after entering the block before starting ramp
        // availDist - rampLen = waitDist == waitSpeed * waitTime
        // currently rampLen is rampLengthOfEntrance and availDist is distance to block that has speedType requirement

        // set throttleSpeed to what it is at the start of the block
        boolean hasSpeed = false;
        BlockSpeedInfo blkSpeedInfo = _speedInfo.get(_idxCurrentOrder);
        float throttleSetting;
        if (idxBlockOrder == 0) {
            throttleSetting = 0.0f;
        } else {
            throttleSetting = blkSpeedInfo.getEntranceSpeed();
            waitSpeed = _speedUtil.getTrackSpeed(_speedUtil.modifySpeed(throttleSetting, _curSpeedType));
            waitTime = Math.round((availDist - rampLen) / waitSpeed);
            hasSpeed = true;
        }
        float timeRatio; // time adjustment for current speed type.
        if (Math.abs(throttleSetting - waitSpeed) > .0001f) {
            timeRatio = throttleSetting / waitSpeed;
        } else {
            timeRatio = 1.0f;
        }
        
        long speedTime = 0; // time running at a given speed until next speed change
        int startIdx = blkSpeedInfo.getFirstIndex();
        int endIdx = blkSpeedInfo.getLastIndex();
        for (int i = startIdx; i <= endIdx; i++) {
            ThrottleSetting ts = _commands.get(i);
            if (hasSpeed) { 
                speedTime += ts.getTime() * timeRatio;
                if (speedTime >= waitTime) {
                    break;
                }
            }
            String cmd = ts.getCommand().toUpperCase();
            if (cmd.equals("SPEED")) {
                throttleSetting = Float.parseFloat(ts.getValue()); // new speed
                if (throttleSetting > .0001f) {
                    hasSpeed = true;
                    waitSpeed = _speedUtil.getTrackSpeed(_speedUtil.modifySpeed(throttleSetting, _curSpeedType));
                    rampLen = _speedUtil.rampLengthForRampDown(throttleSetting, _curSpeedType, speedType);
                    long nextWait = Math.round((availDist - rampLen) / waitSpeed);
                    if (speedTime >= nextWait) {
                        waitTime = nextWait;
                        break;
                    }
                } else {
                    hasSpeed = false;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(" waitTime= {}, availDist= {} waitSpeed= {}, rampLen= {}, ramp start speed= {}",
                    waitTime, availDist, waitSpeed, rampLen,speedSetting);
        }
        rampSpeedDelay(waitTime, speedType, endBlockIdx);
        return true;
    }
    
    private void rampSpeedDelay (long waitTime, String speedType, int endBlockIdx) {
        synchronized(this) {
           if (_delayCommand != null) {
               if (_delayCommand.doNotCancel(speedType, waitTime, endBlockIdx)) {
                   return;
               }
               cancelDelayRamp();
           }
        }
        if (waitTime <= 0) {
            _engineer.rampSpeedTo(speedType, endBlockIdx, true);
            if (speedType.equals(Stop) || speedType.equals(EStop)) {
                _engineer.setWaitforClear(true);
            } else {
                _curSpeedType = speedType;
            }
        } else {    // cancelDelayRamp has been called
            synchronized(this) {
                _delayCommand = new CommandDelay(speedType, waitTime, endBlockIdx, true);
                _delayCommand.start();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation tests that 
     * {@link jmri.NamedBean#getSystemName()}
     * is equal for this and obj.
     * To allow a warrant to run with sections, DccLocoAddress is included to test equality
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false; // by contract

        if (obj instanceof Warrant) {  // NamedBeans are not equal to things of other types
            Warrant b = (Warrant) obj;
            DccLocoAddress addr = this._speedUtil.getDccAddress();
            if (addr == null) {
                if (b._speedUtil.getDccAddress() != null) {
                    return false;
                }
                return (this.getSystemName().equals(b.getSystemName()));
            }
            return (this.getSystemName().equals(b.getSystemName()) && addr.equals(b._speedUtil.getDccAddress()));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @return hash code value is based on the system name and DccLocoAddress.
     */
    @Override
    public int hashCode() {
        return (getSystemName().concat(_speedUtil.getDccAddress().toString())).hashCode();
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Warrant.class);
}
