package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.swing.JOptionPane;

import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.util.ThreadingUtil;
import jmri.jmrit.logix.ThrottleSetting.CommandValue;
import jmri.jmrit.logix.ThrottleSetting.ValueType;

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
    private boolean _addTracker;    // start tracker when warrant ends normally.
    private boolean _noRamp; // do not ramp speed changes. make immediate speed change when entering approach block.
    private boolean _nxWarrant = false;

    // transient members
    private LearnThrottleFrame _student; // need to callback learning throttle in learn mode
    private boolean _tempRunBlind; // run mode flag to allow running on ET only
    protected boolean _delayStart; // allows start block unoccupied and wait for train
    protected int _idxCurrentOrder; // Index of block at head of train (if running)
    protected int _idxLastOrder; // Index of block at tail of train just left
    private String _curSpeedType; // name of last moving speedType, i.e. never "Stop".  Used to restore previous speed

    protected int _runMode = MODE_NONE;
    private Engineer _engineer; // thread that runs the train
    @GuardedBy("this")
    private CommandDelay _delayCommand; // thread for delayed ramp down
    private boolean _allocated; // initial Blocks of _orders have been allocated
    private boolean _totalAllocated; // All Blocks of _orders have been allocated
    private boolean _routeSet; // all allocated Blocks of _orders have paths set for route
    protected OBlock _stoppingBlock; // Block occupied by rogue train or halted
    private NamedBean _protectSignal; // Signal stopping train movement
    private int _idxProtectSignal;      // BlockOrder index of _protectSignal
    // Crossovers typically have both switches controlled by one TO, although each switch is in a different block
    // At the origin and destination of warrants, TO's shared between warrants may set conflicting paths
    private OBlock _myShareBlock;   // block belonging to this warrant
    private OBlock _otherShareBlock;   // block belonging to another warrant

    private boolean _waitForSignal; // train may not move until false
    private boolean _waitForBlock; // train may not move until false
    private boolean _waitForWarrant;
    protected String _message; // last message returned from an action
    private ThrottleManager tm;

    // Throttle modes
    public static final int MODE_NONE = 0;
    public static final int MODE_LEARN = 1; // Record a command list
    public static final int MODE_RUN = 2;   // Autorun, playback the command list
    public static final int MODE_MANUAL = 3; // block detection of manually run train
    public static final String[] MODES = {"none", "LearnMode", "RunAuto", "RunManual", "Abort"};
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
    protected static final int DEBUG = 8;
    protected static final int SPEED_UP = 7;
    protected static final String[] CNTRL_CMDS = {"Stop", "Halt", "Resume", "Abort", "Retry", "EStop", "SmoothHalt", "SpeedUp", "Debug"};
    protected static final String[] RUN_STATE = {"HaltStart", "atHalt", "Resumed", "Aborts", "Retried",
            "EStop", "HaltPending", "Running", "changeSpeed", "WaitingForClear", "WaitingForSensor",
            "RunningLate", "WaitingForStart", "RecordingScript", "StopPending"};

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
        _speedUtil = new SpeedUtil();
        tm = InstanceManager.getNullableDefault(ThrottleManager.class);
    }

    protected void setNXWarrant(boolean set) {
        _nxWarrant = set;
    }
    protected boolean isNXWarrant() {
        return _nxWarrant;
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
        // warrant state is computed from other values
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
        return NamedBean.UNKNOWN;
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
        if (ts == null) {
            log.error("warrant {} cannot add null ThrottleSetting", getDisplayName());
        } else {
            _commands.add(ts);
        }
    }

    public void setTrackSpeeds() {
        float speed = 0.0f;
        for (ThrottleSetting ts :_commands) {
            CommandValue cmdVal = ts.getValue();
            ValueType valType = cmdVal.getType();
            switch (valType) {
                case VAL_FLOAT:
                    speed = _speedUtil.getTrackSpeed(cmdVal.getFloat());
                    break;
                case VAL_TRUE:
                    _speedUtil.setIsForward(true);
                    break;
                case VAL_FALSE:
                    _speedUtil.setIsForward(false);
                    break;
                default:
            }
            ts.setTrackSpeed(speed);
        }
    }

    public void setNoRamp(boolean set) {
        _noRamp = set;
    }

    public void setShareRoute(boolean set) {
        _partialAllocate = set;
    }

    public void setAddTracker (boolean set) {
        _addTracker = set;
    }

    public boolean getNoRamp() {
        return _noRamp;
    }

    public boolean getShareRoute() {
        return _partialAllocate;
    }

    public boolean getAddTracker() {
        return _addTracker;
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
            if ((block.getState() & Block.OCCUPIED) != 0) {
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

    protected String getRunningMessage() {
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
                if ((block.getState() & (Block.OCCUPIED | Block.UNDETECTED)) == 0) {
                    return Bundle.getMessage("LostTrain", _trainName, block.getDisplayName());
                }
                String blockName = block.getDisplayName();
                String speedType;
                if (_engineer != null && _engineer.isRamping()) {
                    speedType = _engineer.getRampSpeedType();
                } else {
                    speedType = _curSpeedType;
                }
                String speed = getSpeedMessage(speedType);
                int runState = _engineer.getRunState();

                switch (runState) {
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
                        return makeWaitMessage(blockName, cmdIdx);

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
                        return Bundle.getMessage("WhereRunning", blockName, cmdIdx, speed);
                    case Warrant.SPEED_RESTRICTED:
                        return Bundle.getMessage("changeSpeed", blockName, cmdIdx, speed);

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
                speed = _engineer.getSpeedSetting() * 100;
                float scriptSpeed = _engineer.getScriptSpeed();
                scriptSpeed = (scriptSpeed > 0 ? (speed/scriptSpeed) : 0);
                units = Bundle.getMessage("percentNormal", Math.round(scriptSpeed));
                break;
            case SignalSpeedMap.PERCENT_THROTTLE:
                units = Bundle.getMessage("percentThrottle");
                speed = _engineer.getSpeedSetting() * 100;
                break;
            case SignalSpeedMap.SPEED_MPH:
                units = Bundle.getMessage("mph");
                speed = _speedUtil.getTrackSpeed(_engineer.getSpeedSetting()) * speedMap.getLayoutScale();
                speed = speed * 2.2369363f;
                break;
            case SignalSpeedMap.SPEED_KMPH:
                units = Bundle.getMessage("kph");
                speed = _speedUtil.getTrackSpeed(_engineer.getSpeedSetting()) * speedMap.getLayoutScale();
                speed = speed * 3.6f;
                break;
            default:
                log.error("Unknown speed interpretation {}", speedMap.getInterpretation());
                throw new java.lang.IllegalArgumentException("Unknown speed interpretation " + speedMap.getInterpretation());
        }
        return Bundle.getMessage("atSpeed", speedType, Math.round(speed), units);
    }

    private String makeWaitMessage(String blockName, int cmdIdx) {
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
            int runState = _engineer.getRunState();
            return Bundle.getMessage(RUN_STATE[runState], blockName, cmdIdx);
        }
        return Bundle.getMessage("RampWaitForClear",
                getTrainName(), getCurrentBlockName(), s);
    }

    @jmri.InvokeOnLayoutThread
    private void startTracker() {
        ThreadingUtil.runOnLayoutEventually(() -> {
            new Tracker(getCurrentBlockOrder().getBlock(), _trainName,
                    null, InstanceManager.getDefault(TrackerTableAction.class));
        });
    }

    synchronized public void stopWarrant(boolean abort, boolean turnOffFunctions) {
        _delayStart = false;
        clearWaitFlags();
        _curSpeedType = Normal;
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

        if (_student != null) {
            _student.dispose(); // releases throttle
            _student = null;
        }
        if (_engineer != null) {
            _engineer.stopRun(abort, turnOffFunctions); // releases throttle
            // let engineer die
            if (!Thread.currentThread().equals(_engineer)) {
                try {   // can't join yourself if called by _engineer
                    _engineer.join(200);
                } catch (InterruptedException ex) {
                    log.info("_engineer.join() interrupted. warrant {}", getDisplayName());
                }
                _engineer = null;
            }   // else can't do anything but hope this instance of _engineer terminates eventually
        }

        int oldMode = _runMode;
        _runMode = MODE_NONE;
        if (turnOffFunctions && _idxCurrentOrder == _orders.size()-1) { // run was complete to end
            _speedUtil.stopRun(true);   // write speed profile measurements
            if (_addTracker) {
                startTracker();
                _runMode = MODE_MANUAL;
            }
        }
        fireRunStatus("runMode", oldMode, _runMode);
        if (log.isDebugEnabled()) {
            log.debug("Warrant \"{}\" terminated {}.", getDisplayName(), (abort ? "- aborted!" : "normally"));
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
        _delayStart = false;
        _curSpeedType = Normal;
        clearWaitFlags();
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
                if ((state & (Block.OCCUPIED | Block.UNDETECTED)) == 0) {
                    // continuing with no occupation of starting block
                    setStoppingBlock(getBlockAt(0));
                    _delayStart = true;
                }
            }
        } else {
            stopWarrant(true, true);
        }
        getBlockAt(0)._entryTime = System.currentTimeMillis();
        _tempRunBlind = runBlind;
        if (!_delayStart) {
            if (mode != MODE_MANUAL) {
                _message = acquireThrottle();
            } else {
                startupWarrant(); // assuming manual operator will go to start block
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Exit setRunMode()  _runMode= {}, msg= {}", MODES[_runMode], _message);
        }
        fireRunStatus("runMode", MODE_NONE, _runMode);
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
     * No steal or share decisions made locally
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
    }

    protected void releaseThrottle(DccThrottle throttle) {
        if (throttle != null) {
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
        stopWarrant(true, true);
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
                case SPEED_UP:
                    fireRunStatus("SpeedChange", null, idx);
                    break;
                case STOP:
                case ABORT:
                    if (_runMode == Warrant.MODE_LEARN) {
                        // let WarrantFrame do the abort. (WarrantFrame listens for "abortLearn")
                        fireRunStatus("abortLearn", -MODE_LEARN, _idxCurrentOrder);
                    } else {
                        fireRunStatus("controlChange", MODE_RUN, ABORT);
                        stopWarrant(true, true);
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
                    cancelDelayRamp(false);
                    _engineer.rampSpeedTo(Warrant.Stop, -1);  // ramp down
                    _engineer.setHalt(true);
                    ret = true;
                    break;
                case RESUME:
                    OBlock block = getBlockAt(_idxCurrentOrder); 
                    if ((block.getState() & OBlock.OCCUPIED) == 0) {
                        break;
                    }
                    if (_waitForSignal || _waitForBlock || _waitForWarrant) {
                        ret = askResumeQuestion(block);
                        if (ret) {
                            log.info(Bundle.getMessage("userOverRide", _trainName, block.getDisplayName()));
                            ret = reStartTrain();
                        }
                    } else {
                        ret = reStartTrain();
                    }
                    break;
                case SPEED_UP:
                    // user wants to increase throttle of stalled train slowly
                    block = getBlockAt(_idxCurrentOrder); 
                    if ((block.getState() & OBlock.OCCUPIED) == 0) {
                        break;
                    }
                    runState = _engineer.getRunState();
                    if ((_waitForSignal || _waitForBlock || _waitForWarrant) ||
                            (runState != RUNNING && runState != SPEED_RESTRICTED)) {
                        ret = askResumeQuestion(block);
                        if (ret) {
                            log.info(Bundle.getMessage("userOverRide", _trainName, block.getDisplayName()));
                            ret = bumpSpeed();
                        }
                    } else {
                        ret = bumpSpeed();
                    }
                    break;
                case RETRY: // Force move into next block
                    block = getBlockAt(_idxCurrentOrder + 1); 
                    runState = _engineer.getRunState();
                    if (block.allocate(this) == null && (block.getState() & OBlock.OCCUPIED) != 0) {
                        if ((_waitForSignal || _waitForBlock || _waitForWarrant) ||
                                (runState != RUNNING && runState != SPEED_RESTRICTED)) {
                            ret = askResumeQuestion(block);
                            if (ret) {
                                log.info(Bundle.getMessage("userOverRide", _trainName, block.getDisplayName()));
                                ret = moveToNextBlock(block);
                            }
                        } else {
                            ret = moveToNextBlock(block);
                        }
                    }
                    break;
                case ABORT:
                    stopWarrant(true, true);
                    ret = true;
                    break;
                case HALT:
                case STOP:
                    cancelDelayRamp(false);
                    _engineer.setStop(false, true); // sets _halt
                    ret = true;
                    break;
                case ESTOP:
                    cancelDelayRamp(false);
                    _engineer.setStop(true, true); // E-stop & halt
                    ret = true;
                    break;
                case DEBUG:
                default:
                    ret = debugInfo();
                    fireRunStatus("SpeedChange", null, idx);
                    return ret;
            }
        }
        int state = runState;
        if (state == Warrant.HALT) {
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

    private boolean askResumeQuestion(OBlock block) {
        String msg = Bundle.getMessage("ResumeQuestion",
                makeWaitMessage(block.getDisplayName(), _idxCurrentOrder));
            boolean ret = jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> {
                int result = JOptionPane.showConfirmDialog(WarrantTableFrame.getDefault(), msg, Bundle.getMessage("ResumeTitle"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result==JOptionPane.YES_OPTION) {
                    return true;
                }
            return false;
        });
        return ret;
    }
    // User insists to run train
    private boolean reStartTrain() {
        OBlock block = getBlockAt(_idxCurrentOrder);
        // OK, will do as it long as you own it, and you are where  you think you are there.
        if (block.allocate(this) == null && (block.getState() & OBlock.OCCUPIED) != 0) {
            _engineer.setHalt(false);
            clearWaitFlags();
            boolean ret = restoreRunning(_curSpeedType, -1);
            return ret;
        }
        return false;
    }

    // User increases speed
    private boolean bumpSpeed() {
        OBlock block = getBlockAt(_idxCurrentOrder);
        // OK, will do as it long as you own it, and you are where you think you are.
        if (block.allocate(this) == null && (block.getState() & OBlock.OCCUPIED) != 0) {
            _engineer.setHalt(false);
            clearWaitFlags();
            float speedSetting = _engineer.getSpeedSetting();
            if (speedSetting < 0) { // may have done E-Stop
                speedSetting = 0.0f;
            }
            _engineer.setSpeed(speedSetting + _speedUtil.getRampThrottleIncrement(), _curSpeedType);
            return true;
        }
        return false;
    }

    private boolean moveToNextBlock (OBlock block) {
        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder + 1);
        _message = bo.setPath(this);
        if (_message != null) {
            log.warn("Cannot clear path for warrant \"{}\" at block \"{}\" - msg = {}",
                    getDisplayName(), block.getDisplayName(), _message);
            return false;
        }
        _idxCurrentOrder++;
        if (block.equals(_stoppingBlock)) {
            clearStoppingBlock();
        }
        goingActive(block);
        return true;
    }

    protected boolean debugInfo() {
        StringBuffer info = new StringBuffer("\nWarrant: ");
        info.append(getDisplayName()); info.append(" - Head Block \"");
        info.append(getBlockAt(_idxCurrentOrder).getDisplayName());
        info.append("\" BlockOrder idx= "); info.append(_idxCurrentOrder);
        info.append("\n\tWarrant flags: _waitForSignal= ");
        info.append(_waitForSignal); info.append(", _waitForBlock= ");
        info.append(_waitForBlock); info.append(", _waitForWarrant= "); info.append(_waitForWarrant);
        if (_protectSignal != null) {
            info.append("\n\tSignal \"");info.append(_protectSignal.getDisplayName());info.append("\" protects block ");
            info.append(getBlockAt(_idxProtectSignal).getDisplayName()); info.append("\" from approch block \"");
            info.append(getBlockAt(_idxProtectSignal - 1).getDisplayName()); info.append("\".");
        } else {
            info.append("\n\tNo signals ahead with speed restrictions");
        }
        if(_stoppingBlock != null) {
            info.append("\n\tRogue Occupies Block \""); info.append(_stoppingBlock.getDisplayName()); info.append("\"");
        } else {
            info.append("\n\tNo occupied blocks ahead");
        }
        if (_message != null) {
            info.append("\n\tLast message \"");info.append(_message);info.append("\"");
        } else {
            info.append("\n\tNo messages.");
        }
        
        if (_engineer != null) {
            info.append(_engineer.debugInfo());
         } else {
            info.append("No engineer.");
        }
        log.info(info.toString());
        return true;
    }

    protected void startupWarrant() {
        _idxCurrentOrder = 0;
        _idxLastOrder = 0;
        // set block state to show our train occupies the block
        BlockOrder bo = getBlockOrderAt(0);
        OBlock b = bo.getBlock();
        b.setValue(_trainName);
        b.setState(b.getState() | OBlock.RUNNING);
    }

    private void runWarrant(DccThrottle throttle) {
        if (_runMode == MODE_LEARN) {
            synchronized (this) {
                // No Engineer. LearnControlPanel does throttle settings
                _student.notifyThrottleFound(throttle);
            }
        } else {
            if (_engineer != null && !_engineer.getState().equals(Thread.State.TERMINATED)) {
                // should not happen
                log.error(_engineer.debugInfo());
                if (!Thread.currentThread().equals(_engineer)) {
                    try {   // can't join yourself if called by _engineer
                        _engineer.join(200);
                    } catch (InterruptedException ex) {
                        log.info("_engineer.join() interrupted. warrant {}", getDisplayName());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(_engineer.debugInfo());
                    }
                }   // else can't do anything but hope this instance of_engineer terminates
                _engineer = null;
            }
             _engineer = new Engineer(this, throttle);
            if (_tempRunBlind) {
                _engineer.setRunOnET(true);
            }
            if (_delayStart) {
                _engineer.setHalt(true);    // throttle already at 0
            }
            // if there may be speed changes due to signals or rogue occupation.
            if (!_noRamp) { // _noRamp makes immediate speed changes
                _speedUtil.getBlockSpeedTimes(_commands);   // initialize SpeedUtil
            }

            _engineer.start();

            if (_delayStart) {
                // user must explicitly start train (resume) in a dark block
                fireRunStatus("ReadyToRun", -1, 0);   // ready to start msg
            }
            if (_engineer.getRunState() == Warrant.RUNNING) {
                setMovement();
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
        String msg = allocateFromIndex(false, 1);
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
     * @param setOne only allocates and sets path in one block, the 'index' block
     * show the entire route but do not set any turnouts in occupied blocks
     */
    private String allocateFromIndex(boolean setOne, int index) {
        int limit;
        if (_partialAllocate || setOne) {
            limit = Math.min(index + 1, _orders.size());
        } else {
            limit = _orders.size();
        }
        OBlock currentBlock = getBlockAt(_idxCurrentOrder);
        if (log.isDebugEnabled()) {
            log.debug("allocateFromIndex({}) block= {} _partialAllocate= {} for warrant \"{}\".",
                    index, currentBlock.getDisplayName(), _partialAllocate, getDisplayName());
        }
        _message = null;
        boolean passageDenied = false;      // cannot move beyond this point
        boolean allocationDenied = false;   // cannot allocate beyond this point
        for (int i = index; i < limit; i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
            String msg = block.allocate(this);
            if (msg != null && _message == null) {
                _message = msg;
                passageDenied = true;
                allocationDenied = true;
            }
            if (!passageDenied) {
                // loop back routes may enter a block a second time
                // Do not make current block a stopping block
                if (!currentBlock.equals(block)) {
                    if ((block.getState() & OBlock.OCCUPIED) != 0 && !_delayStart) {
                        if (_message == null) {
                            _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                        }
                        passageDenied = true;
                    }
                    if (!passageDenied && Warrant.Stop.equals(getPermissibleSpeedAt(bo)) && !_delayStart) {
                        if (_message == null) {
                            _message = Bundle.getMessage("BlockStopAspect", block.getDisplayName());
                        }
                        passageDenied = true;
                    }
                }
                if (!passageDenied && setOne) {
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
        if (!allocationDenied && limit == _orders.size()) {
            _totalAllocated = true;
        }
        return _message;
    }

    /**
     * Deallocates blocks from the current BlockOrder list
     */
    public void deAllocate() {
        _allocated = false;
        _totalAllocated = false;
        _routeSet = false;
        for (int i = 0; i < _orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
            if (block.isAllocatedTo(this)) {
                block.deAllocate(this);
            }
        }
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
     * @param show If true allocateRoute returns messages for display.
     * @param orders  BlockOrder list of route. If null, use permanent warrant
     *            copy.
     * @return message if the first block fails allocation, otherwise null
     */
    public String setRoute(boolean show, List<BlockOrder> orders) {
        // we assume our train is occupying the first block
        _routeSet = false;
        // allocateRoute may set _message for status info, but return null msg
        String msg = allocateRoute(show, orders);
        if (msg != null) {
            _message = msg;
            log.debug("setRoute: {}", msg);
            return _message;
        }
//        _allocated = true;
        BlockOrder bo = _orders.get(0);
        msg = bo.setPath(this);
        if (msg != null) {
            _message = msg;
            log.debug("setRoute: {}", msg);
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
                    log.debug("setRoute: {}", msg);
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
        log.debug("checkforTrackers at block {}", block.getDisplayName());
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
        if (_orders==null || _orders.size() == 0) {
            return Bundle.getMessage("noBlockOrders");
        }
        OBlock startBlock = _orders.get(0).getBlock();
        for (int i = 1; i < _orders.size(); i++) {
            OBlock block = _orders.get(i).getBlock();
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

    /*
     * _protectSignal made an aspect change
     */
    private void readStoppingSignal() {
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
        if (speedType != null && !speedType.equals(Warrant.Stop)) {
            if (_idxProtectSignal == _idxCurrentOrder && 
                    _engineer.getRunState() == WAIT_FOR_CLEAR && _engineer.getSpeedSetting() < .0001) {
                // signal protecting next block just released its hold
                int idxStop = getIndexOfBlock(_stoppingBlock, _idxCurrentOrder);
                if (idxStop == _idxCurrentOrder + 1) {
                    // Most likely train overran signal and set _stoppingBlock. if not, either way signal says proceed
                    _waitForBlock = false;
                    OBlock block = getBlockAt(idxStop);
                    block.setValue(_trainName);
                    block.setState(block.getState() | OBlock.RUNNING);
                    _idxCurrentOrder++;         // advance marker and claim occupation
                }
            }
            log.info(Bundle.getMessage("SignalCleared", _protectSignal.getDisplayName(), _trainName));
            _waitForSignal = false;
            ThreadingUtil.runOnGUIDelayed(() -> {
                restoreRunning(speedType, -1);
            }, 3500);   // 3.5 seconds
        }
    }

    private boolean doStoppingBlockClear() {
        if (_stoppingBlock == null) {
            return true;
        }
        log.info(Bundle.getMessage("BlockCleared", _stoppingBlock.getDisplayName(), _trainName));
        _stoppingBlock.removePropertyChangeListener(this);
        _stoppingBlock = null;
        _waitForBlock = false;
        return restoreRunning(_curSpeedType, -1);
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
        String msg = allocateFromIndex(true, _idxCurrentOrder + 1);
        if (msg == null && doStoppingBlockClear()) {
            return true;
        }

        if (log.isDebugEnabled())
            log.debug("Warrant \"{}\" {}. runState= {}",
                getDisplayName(),  (msg==null?"ReStart not allowed":msg),
               (_engineer!=null?RUN_STATE[_engineer.getRunState()]:"No Engineer"));
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
                    synchronized (this) {
                        while (time < 200) {
                            msg = allocateFromIndex(true, _idxCurrentOrder + 1);
                            if (msg == null && doStoppingBlockClear()) {
                                break;
                            }
                            wait(20);
                            time += 20;
                        }
                        if (msg != null) {
                            log.warn("Warrant \"{}\" unable to clear StoppingBlock message= \"{}\" time= {}", getDisplayName(), msg, time);
                        }
                        _message = msg;
                    }
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
            Thread doit = jmri.util.ThreadingUtil.newThread(
                    () -> {
                        try {
                            javax.swing.SwingUtilities.invokeAndWait(allocateBlocks);
                        }
                        catch (Exception e) {
                            log.error("Exception in allocateBlocks", e);
                        }
                    },
                    "Warrant doit");
            doit.start();
        }
        return true;
    }

    private boolean okToRun() {
        int runState = -1;
        boolean ret = false;
        boolean rampOk = okToRamp(_curSpeedType, -1);
        if (_engineer != null) {
            runState = _engineer.getRunState();
            if (!_waitForSignal && !_waitForBlock && !_waitForWarrant && rampOk &&
                    runState != HALT && runState != RAMP_HALT) {
                ret = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("okTorun()= {}: runState={} _waitForSignal={} _waitForBlock={} _waitForWarrant={} rampOk={}. warrant= {}",
                    ret, (runState < 0 ? "No Engineer":RUN_STATE[runState]),
                    _waitForSignal, _waitForBlock, _waitForWarrant, rampOk, getDisplayName());
        }
        return ret;
    }

    /**
     * A layout condition that has restricted or stopped a train has been cleared.
     * i.e. Signal aspect, rogue occupied block, contesting warrant or user halt. 
     * This may or may not be all the conditions restricting speed.
     * @return true if automatic restart is done
     */
    synchronized private boolean restoreRunning(String speedType, int endBlockIdx) {
        if (okToRun()) {
            getBlockOrderAt(_idxCurrentOrder).setPath(this);
            _engineer.rampSpeedTo(speedType, endBlockIdx);
            fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder);
            if (log.isDebugEnabled()) {
                log.debug("restoreRunning(): rampSpeedTo to \"{}\". warrant= {}",
                        speedType, getDisplayName());
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
        if (log.isDebugEnabled()) {
            String msg = _orders.get(getIndexOfBlock(_myShareBlock, _idxCurrentOrder)).setPath(this);
            log.info("_otherShareBlock= \"{}\" Cleared. {}",
                    _otherShareBlock.getDisplayName(), (msg==null?"":"msg"));
        }
        _otherShareBlock = null;
        _myShareBlock = null;
        _waitForWarrant = false;
        restoreRunning(_curSpeedType, -1);
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
        if (_protectSignal != null && _protectSignal.equals(signal)) {
            // Must be the route coming back to the same block
            if (_idxProtectSignal < idx && idx >= 0) {
                _idxProtectSignal = idx;
            }
            return;
        }
        if (_protectSignal != null) {   // this is prevSignal
            if ((_idxProtectSignal <= _idxCurrentOrder && !_waitForSignal) ||
                    (signal != null && idx < _idxProtectSignal)) {
                _protectSignal.removePropertyChangeListener(this);
                _protectSignal = null;
                _idxProtectSignal = -1;
            } else {    // keep current _protectSignal
                if (log.isDebugEnabled()) {
                    log.debug("Block \"{}\" of warrant \"{}\" keeps signal \"{}\" for block \"{}\"",
                            getBlockAt(idx).getDisplayName(), getDisplayName(), _protectSignal.getDisplayName(),
                            getBlockAt(_idxProtectSignal).getDisplayName());
                }
                return;
            }
        }
        signalAddListener(prevSignal, signal, idx);
    }

    private void signalAddListener(NamedBean prevSignal, NamedBean  signal,  int signalIndex) {
        if (signal != null) {
            _protectSignal = signal;
            _idxProtectSignal = signalIndex;
            _protectSignal.addPropertyChangeListener(this);
        }
        if (log.isDebugEnabled()) {
            if (signal == null && prevSignal == null) {
                return;
            }
            String msg = "Block \"{}\" Warrant \"{}\"";
            if (prevSignal != null) {
                msg = msg + ", removes signal= \"" + prevSignal.getDisplayName() + "\"";
            }
            if (signal != null) {
                msg = msg + " sets _protectSignal= \"" + _protectSignal.getDisplayName() + "\"";
            }
            log.debug(msg, getBlockAt(signalIndex).getDisplayName(), getDisplayName());
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
        if (log.isDebugEnabled()) {
            if (!ThreadingUtil.isLayoutThread()) {
                log.error("invoked on wrong thread", new Exception("traceback"));
            }
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
                fireRunStatus("SpeedChange", null, activeIdx);
            }
            return;
        }
        int runState;
        if (_engineer == null) {
            runState = -1;
            if (_runMode == MODE_RUN) {
                log.error("NoEngineer! warrant= {}", getDisplayName());
                return;
            }
        } else {
            runState =  _engineer.getRunState();
        }
        if (activeIdx == _idxCurrentOrder) {
            // unusual occurrence.  dirty track? sensor glitch?
            log.info("Head of Train {} regained detection at Block= {}", getTrainName(), block.getDisplayName());
        } else if (activeIdx == _idxCurrentOrder + 1) {
            if (_delayStart || (runState == HALT && _engineer.getSpeedSetting() > 0.0f)) {
                log.warn("Rogue entered Block \"{}\" ahead of {}.", block.getDisplayName(), getTrainName());
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return;
            }
            // be sure path is set for train in this block
            _message = getBlockOrderAt(activeIdx).setPath(this);
            if (_message != null) {
                log.error("goingActive setPath fails: {}", _message);
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
            prevBlock._entryTime = System.currentTimeMillis() - 1000; // arbitrarily say
            prevBlock.setValue(_trainName);
            prevBlock.setState(prevBlock.getState() | OBlock.RUNNING);
            if (log.isDebugEnabled()) {
                log.debug("Train leaving UNDETECTED block \"{}\" now entering block\"{}\". warrant {}",
                        prevBlock.getDisplayName(), block.getDisplayName(), getDisplayName());
            }
            // Since we are moving we assume it is our train entering the block
            // continue on.
        } else if (_idxCurrentOrder > activeIdx) {
            // unusual occurrence.  dirty track, sensor glitch, too fast for goingInactive() for complete?
            log.info("Tail of Train {} regained detection behind Block= {} at block= {}", 
                    getTrainName(), block.getDisplayName(), getBlockAt(activeIdx).getDisplayName());
            return;
        }
        setHeadOfTrain(block);
        if (_engineer != null) {
            _engineer.clearWaitForSync(); // Sync commands if train is faster than ET
        }
        fireRunStatus("blockChange", getBlockAt(activeIdx - 1), block);
        // _idxCurrentOrder has been incremented. Warranted train has entered this block.
        // Do signals, speed etc.
        if (_idxCurrentOrder < _orders.size() - 1) {
            allocateFromIndex(true, _idxCurrentOrder + 1);
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
                stopWarrant(false, true);
            }
        }
        if (log.isTraceEnabled()) {
            log.debug("end of goingActive. leaving \"{}\" entered \"{}\". warrant {}",
                    getBlockAt(activeIdx - 1).getDisplayName(), block.getDisplayName(), getDisplayName());
        }
        setMovement();
    } //end goingActive

    private void setHeadOfTrain(OBlock block ) {
        block.setValue(_trainName);
        block.setState(block.getState() | OBlock.RUNNING);
        block._entryTime = System.currentTimeMillis();
        if (_runMode == MODE_RUN) {
            float length = 0;
            // Just left _idxCurrentOrder-1 and entered _idxCurrentOrder
            if (_idxCurrentOrder == 1 || _idxCurrentOrder == _orders.size() - 1) {
                // Starting and ending position of the train is not known
                // estimate distance to be mid-block
                BlockOrder bo = getBlockOrderAt(_idxCurrentOrder-1);
                length = bo.getPath().getLengthMm() / 2;
            } else {
                for (int i = _idxLastOrder; i < _idxCurrentOrder; i++) {
                    // add length of possible dark block
                    BlockOrder bo = getBlockOrderAt(i);
                    length += bo.getPath().getLengthMm();
                }
            }
            _speedUtil.leavingBlock(getBlockAt(_idxCurrentOrder-1), length);
        }
    }

    /**
     * @param block Block in the route is going Inactive
     */
    @jmri.InvokeOnLayoutThread
    protected void goingInactive(OBlock block) {
        if (log.isDebugEnabled()) {
            if (!ThreadingUtil.isLayoutThread()) {
                log.error("invoked on wrong thread", new Exception("traceback"));
            }
        }
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
        if (_partialAllocate) { // shared route
            for (int i = idx; i > -1; i--) {
                OBlock prevBlock = getBlockAt(i);
                prevBlock.setValue(null);
                prevBlock.deAllocate(this);
                _totalAllocated = false;
                fireRunStatus("blockRelease", null, block);
            }
        } else {
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
    }


    /**
     * Finds speed change type at entrance of a block. Called by:
     * getSpeedTypeForBlock
     * setMovement
     * allocateFromIndex
     * setRoute
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
        }
        if (speedType == null) {
            speedType = _curSpeedType;
        }
        return speedType;
    }

    /*
     * Stop current CommandDelay thread and any executing ramp thread
     * @param hold if true, and ramp thread is executing, hold the ramp lock
     * to prevent main engineer thread from running. Caller will (must)
     * immediately follow with a call to engineer.rampSpeedTo().
     */
    synchronized private void cancelDelayRamp(boolean hold) {
        if (_delayCommand != null) {
            _delayCommand.interrupt();
            log.debug("cancelDelayRamp called on warrant {}", getDisplayName());
            _delayCommand = null;
        }
        if (_engineer.cancelRamp(false)) {
            log.debug("Ramp Cancelled. warrant= {}", getDisplayName());
        }
    }

    private boolean okToRamp(String speedType, int endBlockIdx) {
        if (_delayCommand != null) {
            synchronized (this) {
                if (_delayCommand.isDuplicate(speedType, 0, endBlockIdx)) {
                    return false;
                }
            }
        }
        if (_engineer != null && _engineer.isRamping()) {
            return false;
        }
        return true;
    }

    synchronized private void rampDelayDone() {
        _delayCommand = null;
    }

    @Override
    public void dispose() {
        stopWarrant(false, true);
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
        float _waitSpeed;
        boolean quit = false;
        int _endBlockIdx;

        CommandDelay(String speedType, long startWait, float waitSpeed, int endBlockIdx) {
            nextSpeedType = speedType;
            _waitTime = startWait;
            _waitSpeed = waitSpeed;
            _endBlockIdx = endBlockIdx;
            setName("CommandDelay(" + getTrainName() +")");
            if (log.isDebugEnabled()) {
                log.debug("CommandDelay: will wait {}ms, then Ramp to {} in block {}. warrant {}",
                        startWait, speedType, getBlockAt(endBlockIdx).getDisplayName(), getDisplayName());
            }
        }

        // check if request for a duplicate CommandDelay can be cancelled
        boolean isDuplicate(String speedType, long startWait, int endBlockIdx) {
            if (endBlockIdx == _endBlockIdx && speedType.equals(nextSpeedType) &&
                    (_waitTime - (System.currentTimeMillis() - _startTime)) < startWait) {
                return true;
            }
            return false;   // not a duplicate or shortens time wait.
        }

        @Override
        @SuppressFBWarnings(value = "WA_NOT_IN_LOOP", justification = "notify never called on this thread")
        public void run() {
            synchronized (this) {
                _startTime = System.currentTimeMillis();
                boolean ramping = _engineer.isRamping();
                if (ramping) {
                    long time = 0;
                    while (time <= _waitTime) {
                        if (_engineer.getSpeedSetting() > _waitSpeed) {
                            break; // stop ramping beyond this speed 
                        }
                        try {
                            wait(50);
                        } catch (InterruptedException ie) {
                            if (log.isDebugEnabled()) {
                                log.debug("CommandDelay interrupt.  Ramp to {} not done. warrant {}",
                                        nextSpeedType, getDisplayName());
                            }
                            quit = true;
                        }
                        time += 50;
                    }
                } else {
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
                    _engineer.rampSpeedTo(nextSpeedType, _endBlockIdx);
                }
            }
            rampDelayDone();
        }
    }

    // called by engineer when ramp has stopped ramp to speedType
    protected void setCurrentSpeedType(String speedType) {
        _curSpeedType = speedType;
    }

    private void clearWaitFlags() {
        _waitForBlock = false;
        _waitForSignal = false;
        _waitForWarrant = false;
    }
    /**
     * Orders are parsed to get any speed restrictions. Called from
     * setMovement
     * controlRunTrain (RESUME)
     * 
     * @param idxBlockOrder index of Orders
     * @return Speed type name
     */
    private String getSpeedTypeForBlock(int idxBlockOrder, boolean okToAllocate) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlockOrder);
        OBlock block = blkOrder.getBlock();

        String speedType = getPermissibleSpeedAt(blkOrder);
        setStoppingSignal(idxBlockOrder);
        if (speedType.equals(Warrant.Stop)) {
            // block speed cannot be Stop, so OK to assume signal
            _waitForSignal = true;
        }

        if (okToAllocate) {
            _message = block.allocate(this);
            if (_message != null) {  // only 2 messages possible
                Warrant w = block.getWarrant();
                if (w != null &&!this.equals(w)) {
                    _waitForWarrant = true; // w owns block
                } else {
                    _waitForBlock = true;   // OUT_OF_SERVICE
                }
                setStoppingBlock(block);
                speedType = Warrant.Stop;
            }            
        }
        if ((block.getState() & OBlock.OCCUPIED) != 0) {
            if (idxBlockOrder > _idxCurrentOrder) {
                _waitForBlock = true;       // OCCUPIED
                setStoppingBlock(block);
                speedType = Warrant.Stop;
            }
        } 
        if (!speedType.equals(Warrant.Stop) && okToAllocate) {
            _message = blkOrder.setPath(this);
            if (_message != null) {
                // when setPath fails, it calls setShareTOBlock
                _waitForWarrant = true;
                speedType = Warrant.Stop;
                log.warn(_message);
            }
        }

        if (log.isDebugEnabled()) {
            if (_waitForSignal || _waitForBlock || _waitForWarrant) {
                log.debug ("Found \"{}\" speed change of type \"{}\" shown to enter block \"{}\".",
                        (_waitForSignal?"Signal":(_waitForWarrant?"Warrant":"Block")), speedType, block.getDisplayName());
            }
        }
        return speedType;
    }

    /*
     * Return pathLength of the block.
     */
    private float getAvailableDistance(int idxBlockOrder) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlockOrder);
        float pathLength = blkOrder.getPath().getLengthMm();
        if (idxBlockOrder == 0 || pathLength <= 1.0f) {
            // Position in block is unknown. use calculated distances instead
            float blkDist = _speedUtil.getBlockSpeedInfo(idxBlockOrder).getDistance();
            if (log.isDebugEnabled()) {
                log.debug("getAvailableDistance: in block \"{}\" using calculated blkDist= {}, pathLength= {}. warrant= {}",
                        blkOrder.getBlock().getDisplayName(), blkDist, pathLength, getDisplayName());
            }
            return blkDist;
        } else {
            return pathLength;
        }
    }

    private float getRampLengthForEntry(float scriptSpeed, float endSpeed) {
        float currentSpeed = _speedUtil.modifySpeed(scriptSpeed, _curSpeedType);
        RampData ramp = _speedUtil.getRampForSpeedChange(currentSpeed, endSpeed);
        float enterLen = ramp.getRampLength(_curSpeedType);
        if (log.isDebugEnabled()) {
            log.debug("getRampLengthForEntry: from speed={} to speed={}. rampLen={} warrant= {}",
                    scriptSpeed, endSpeed, enterLen, getDisplayName());
        }
        return enterLen;
    }

    private float getEntranceBufferDist(int idxBlockOrder) {
        float bufDist = 6096 / WarrantPreferences.getDefault().getLayoutScale();// add 20 scale feet for safety distance
        if (_waitForSignal) {        // signal restricting speed
            bufDist+= getBlockOrderAt(idxBlockOrder).getEntranceSpace(); // signal's adjustment
        }
        return bufDist;
    }

    private void speedOverrun(BlockOrder blkOrder, String currentType) {
        OBlock curBlock = blkOrder.getBlock();
        jmri.NamedBean signal = blkOrder.getSignal();
        jmri.NamedBean prevSignal = _protectSignal;
        if (_protectSignal != null) {
            if (_idxProtectSignal < _idxCurrentOrder){
                _protectSignal.removePropertyChangeListener(this);
                _protectSignal = null;
                _idxProtectSignal = -1;
                signalAddListener(prevSignal, signal, _idxCurrentOrder);
            }
        }

        log.info("Train {} moved past required speed of \"{}\" at speed \"{}\" of signal \"{}\" in block \"{}\"! warrant= {}",
                getTrainName(), currentType, _curSpeedType, signal.getDisplayName(), curBlock.getDisplayName(), getDisplayName());
         fireRunStatus("SpeedRestriction", signal.getDisplayName(), currentType); // message of speed violation
    }

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
     * @return false on errors
     */
    private boolean setMovement() {
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
            log.debug("SET MOVEMENT Block\"{}\" runState= {}, _curSpeedType= {}. {} warrant {}.",
                    curBlock.getDisplayName(), RUN_STATE[runState], _curSpeedType,
                    (_partialAllocate ? "ShareRoute" : ""), getDisplayName());
        }

        _message = blkOrder.setPath(this);
        if (_message != null) {
            log.error("Train {} in block \"{}\" but path cannot be set! msg= {}, warrant= {}",
                    getTrainName(), curBlock.getDisplayName(), _message, getDisplayName());
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

        float speedSetting = _engineer.getSpeedSetting();
        if (log.isDebugEnabled()) {
            log.debug("Stopping flags: _waitForBlock={}, _waitForSignal={}, _waitForWarrant={} runState= {}, speedSetting= {}, SpeedType= {}. warrant {}",
                    _waitForBlock, _waitForSignal, _waitForWarrant, RUN_STATE[runState], speedSetting, currentType, getDisplayName());
        }

        if (_noRamp) {
            if (_idxCurrentOrder < _orders.size() - 1) {
                currentType = getSpeedTypeForBlock(_idxCurrentOrder + 1, true);
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
                _engineer.cancelRamp(false);
                _engineer.setSpeedToType(currentType); // immediate decrease

                speedOverrun(blkOrder, currentType);
             // continue for possible speed changes ahead
            } else {    // speed increases since type currentType >= _curSpeedType
                restoreRunning(currentType, -1);
                // continue, there may be blocks ahead that need a speed decrease to begin in this block
            }
        } else {
            if (runState == WAIT_FOR_CLEAR || runState == HALT || runState == STOP_PENDING || runState == RAMP_HALT) {
                if (log.isDebugEnabled()) {
                    log.debug("Train drift into block \"{}\". runState= {}, Engineer to hold. speedSetting= {}. warrant {}",
                            curBlock.getDisplayName(), RUN_STATE[runState], speedSetting, getDisplayName());
                }
                fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder); // message reason for hold
                return true;
            }
            // Continue, look ahead for possible speed modification decrease.
        }

        // look ahead for speed type slower than current type
        int idxBlockOrder = _idxCurrentOrder;   // index where speed changes before entry
        String speedType = _curSpeedType;
        float availDist = 0;    // distance to speed requirement
        if (idxBlockOrder < _orders.size() - 1) {
            idxBlockOrder++;       // advance to next block so distance is from end of this block
            boolean okToAlloc = !_partialAllocate || idxBlockOrder <= _idxCurrentOrder + 1;
            speedType = getSpeedTypeForBlock(idxBlockOrder, okToAlloc);  // speed for entry into next block
            while (!_speedUtil.secondGreaterThanFirst(speedType, _curSpeedType) && idxBlockOrder < _orders.size() - 1) {
                availDist += getAvailableDistance(idxBlockOrder++);   // distance to next block
                okToAlloc = !_partialAllocate || idxBlockOrder <= _idxCurrentOrder + 1;
                speedType = getSpeedTypeForBlock(idxBlockOrder, okToAlloc);  // speed for entry into next block
            }
        }

        if (log.isDebugEnabled()) {
            if (runState == RAMP_HALT || runState == SPEED_RESTRICTED || runState == STOP_PENDING) {
                // Engineer has a speed change in progress. speedType should be pending type
                String sType = _engineer.getRampSpeedType();
                log.debug("Engineer runstate={}. Ramping speedType={}", RUN_STATE[runState], (sType==null?"null":sType));
            }
        }
        // distance to entrance of block with speed change from exit of this block
        float scriptSpeed = _speedUtil.getBlockSpeedInfo(idxBlockOrder).getEntranceSpeed();
        float endSpeed = _speedUtil.modifySpeed(scriptSpeed, speedType);
        if (Math.abs(scriptSpeed - endSpeed) <.005f) {
            if (log.isDebugEnabled()) {
                log.debug("Ramp unnecessay. Script decreases speed from {} to {} before entering block \"{}\". warrant {}",
                        scriptSpeed, endSpeed, getBlockAt(idxBlockOrder).getDisplayName(), getDisplayName());
            }
            return true;    // OK, script makes the indicated speed change
        }

        float enterLen = getRampLengthForEntry(scriptSpeed, endSpeed);
        float bufferDist = getEntranceBufferDist(idxBlockOrder);       
        if (log.isDebugEnabled()) {
            log.debug("SpeedTypeDistance: speedType \"{}\"  _idxCurrentOrder={}, idxBlockOrder={}, availDist={}, enterDist={}",
                    speedType, _idxCurrentOrder, idxBlockOrder, availDist,  enterLen + bufferDist);
        }

        if (idxBlockOrder == _orders.size() - 1) {// found no speed decreases, except for possibly the last
            // went through remaining BlockOrders,
            if (!_speedUtil.secondGreaterThanFirst(speedType, _curSpeedType)) {
                // speed change, if found, not less than current speed
                if (log.isDebugEnabled()) {
                    if (_curSpeedType.equals(speedType)) {
                        log.debug("No speed modifications for runState= {} from {} found after block \"{}\". warrant {}",
                                RUN_STATE[runState], _curSpeedType, curBlock.getDisplayName(), getDisplayName());
                    }
                }
                if (runState == STOP_PENDING || runState == RAMP_HALT) {
                    // ramp to stop in progress - no need for further stop calls
                     return true;
                }
                clearWaitFlags();
                return true;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Speed decrease to \"{}\" from {} needed before entering block \"{}\" warrant {}",
                    speedType, _curSpeedType, getBlockAt(idxBlockOrder).getDisplayName(), getDisplayName());
        }

        if (enterLen + bufferDist <= availDist) {
            // sufficient ramp room was found without including the current block
            // therefore no speed change needed yet.
            if (log.isDebugEnabled()) {
                log.debug("Will decrease speed for runState= {} to {} from {} later for block \"{}\", warrant {}",
                        RUN_STATE[runState], speedType, _curSpeedType, getBlockAt(idxBlockOrder).getDisplayName(),
                        getDisplayName());
            }
            clearWaitFlags();
            return true; // change speed later
        }

        int endBlockIdx = idxBlockOrder - 1;    // index of block where ramp ends
        availDist += getAvailableDistance(_idxCurrentOrder);   // Add length of this block
        if (log.isDebugEnabled()) {
            log.debug("SpeedTypeDistance: speedType \"{}\" _idxCurrentOrder={}, endBlockIdx={}, availDist={}, enterDist={}",
                    speedType, _idxCurrentOrder, endBlockIdx, availDist, (enterLen + bufferDist));
        }

        if (enterLen + bufferDist > availDist) {
            log.warn("No room for train {} to ramp to speed \"{}\" in block \"{}\"!. availDist={}, enterDist={} warrant= {}",
                    getTrainName(), speedType, curBlock.getDisplayName(), availDist,  enterLen + bufferDist, getDisplayName());
        }

        // Must start the ramp in current block. (_idxCurrentOrder)
        // find the time when ramp should start in this block, then use thread CommandDelay
        // waitSpeed is throttleSpeed when ramp is started. Start with it being at the entrance to the block.
        // final ramp should modify waitSpeed to endSpeed and must end at the exit of end block (endBlockIdx)
        float throttle = 0; // script throttle setting
        float modThrottle = 0; // setting modified by _curSpeedType
        float waitSpeed = 0;
        long waitTime = 0; // time to wait after entering this block before starting ramp

        BlockSpeedInfo blkSpeedInfo = _speedUtil.getBlockSpeedInfo(_idxCurrentOrder);
        if (idxBlockOrder == 0) {
            scriptSpeed = 0.0f;
        } else {
            throttle = blkSpeedInfo.getEntranceSpeed();
            scriptSpeed = _speedUtil.getTrackSpeed(throttle);
            modThrottle = _speedUtil.modifySpeed(throttle, _curSpeedType);
            waitSpeed = _speedUtil.getTrackSpeed(modThrottle);
        }
        float accumDist = 0;    // accumulated distance from commands up to ramp start
        float accumTime = 0;    // accumulated time of commands up to ramp start
        float timeRatio; // time adjustment for current speed type.
        if (Math.abs(scriptSpeed - waitSpeed) > .001f) {
            timeRatio = scriptSpeed / waitSpeed;
        } else {
            timeRatio = 1.0f;
        }
        /// time values are the times train covers the distances at the current speed type
        float rampLen = enterLen;   // length of entrance ramp
        float deltaTime = 0;    // time to next command
        float deltaDist = 0;    // distance traveled to next command

        int startIdx = blkSpeedInfo.getFirstIndex();
        int endIdx = blkSpeedInfo.getLastIndex();
        if (log.isDebugEnabled()) {
            log.debug("cmdIdx {} to {} at speedType \"{}\" throttle={} speedSetting={}, timeRatio={}",
                    startIdx, endIdx, _curSpeedType, modThrottle, speedSetting, timeRatio);
        }
        for (int i = startIdx; i <= endIdx; i++) {
            ThrottleSetting ts = _commands.get(i);
            long time = ts.getTime();
            deltaTime = time * timeRatio;
            deltaDist = waitSpeed * deltaTime;
            accumTime += deltaTime; // total time up to this command
            accumDist += deltaDist; // total distance  up to this command
            float nextDist = accumDist + rampLen + bufferDist;
            if (availDist < nextDist) {
                // subtract excess - done.
                float remDist =  nextDist - availDist;
                accumDist -= remDist;
                accumTime -= deltaTime * (deltaDist - remDist) / deltaDist;
                break;
            }

            ThrottleSetting.CommandValue cmdVal = ts.getValue();
            if (cmdVal.getType() == ThrottleSetting.ValueType.VAL_FLOAT) {
                throttle = cmdVal.getFloat();
                scriptSpeed = _speedUtil.getTrackSpeed(throttle);
                modThrottle = _speedUtil.modifySpeed(throttle, _curSpeedType);
                float newSpeed = _speedUtil.getTrackSpeed(modThrottle);
                timeRatio = scriptSpeed / newSpeed;
                rampLen = getRampLengthForEntry(throttle, endSpeed);
                if (rampLen + bufferDist + accumDist >= availDist) {
                    // this speed command is too much - done.
                    break;
                }
                waitSpeed = newSpeed;
            }
            log.debug("#{} accumDist={}, accumTime={}, deltaDist={},deltaTime={}", i, accumDist, accumTime, deltaDist, deltaTime);
        }
        waitTime = Math.round(accumTime);
        log.debug("accumDist={}, accumTime={}, deltaDist={},deltaTime={} rampLen={} sum={}",
                accumDist, accumTime, deltaDist, deltaTime, rampLen, accumDist + rampLen + bufferDist);

        if (log.isDebugEnabled()) {
            log.debug("RAMP waitTime={}, waitThrottle={}, waitDist={}, availDist={}, enterLen={} for ramp start",
                    waitTime, modThrottle, accumDist, availDist, rampLen + bufferDist);
        }

        waitTime -= 20;     // Subtract a bit to avoid last unwanted speed command
        rampSpeedDelay(waitTime, speedType, modThrottle, endBlockIdx);
        return true;
    }   // end setMovement

    synchronized private void rampSpeedDelay (long waitTime, String speedType, float waitSpeed, int endBlockIdx) {
        if (_delayCommand != null) {
            if (_delayCommand.isDuplicate(speedType, waitTime, endBlockIdx)) {
                return;
            }
            cancelDelayRamp(true);
        }
        if (waitTime <= 0) {
            _engineer.rampSpeedTo(speedType, endBlockIdx);
        } else {    // cancelDelayRamp has been called
            synchronized(this) {
                _delayCommand = new CommandDelay(speedType, waitTime, waitSpeed, endBlockIdx);
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

    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            if (bean.equals(getBlockingWarrant())) {
                report.add(new NamedBeanUsageReport("WarrantBlocking"));
            }
            getBlockOrders().forEach((blockOrder) -> {
                if (bean.equals(blockOrder.getBlock())) {
                    report.add(new NamedBeanUsageReport("WarrantBlock"));
                }
                if (bean.equals(blockOrder.getSignal())) {
                    report.add(new NamedBeanUsageReport("WarrantSignal"));
                }
            });
        }
        return report;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Warrant.class);
}
