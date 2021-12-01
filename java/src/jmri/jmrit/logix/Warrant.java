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
    private boolean _delayStart; // allows start block unoccupied and wait for train
    private int _idxCurrentOrder; // Index of block at head of train (if running)
    private int _idxLastOrder; // Index of block at tail of train just left

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
    private String _curSignalAspect;   // speed type to restore when flags are cleared; 
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
    protected static final String[] CNTRL_CMDS = {"Stop", "Halt", "Resume", "Abort", "MoveToNext", "EStop", "SmoothHalt", "SpeedUp", "Debug"};
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

    /*
     * Used only by SCWarrant
     * SCWarrant overrides goingActive
     */
    protected void incrementCurrentOrderIndex() {
        _idxCurrentOrder++;
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
        ThreadingUtil.runOnGUIEventually(() -> { // OK but can be quite late in reporting speed changes
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
                    // perhaps redundant, but be sure to stop train
                    _engineer.setSpeedToType(EStop);
                    return Bundle.getMessage("LostTrain", _trainName, block.getDisplayName());
                }
                String blockName = block.getDisplayName();
                String speedMsg = getSpeedMessage(_engineer.getSpeedType(true));
                int runState = _engineer.getRunState();

                switch (runState) {
                    case Warrant.ABORT:
                        if (cmdIdx == _commands.size() - 1) {
                            _engineer = null;
                            return Bundle.getMessage("endOfScript", _trainName);
                        }
                        return Bundle.getMessage("Aborted", blockName, cmdIdx);

                    case Warrant.HALT:
                    case Warrant.WAIT_FOR_CLEAR:
                        String msg = makeWaitMessage(blockName, cmdIdx);
                        return msg;

                    case Warrant.WAIT_FOR_TRAIN:
                        int blkIdx = _idxCurrentOrder + 1;
                        if (blkIdx >= _orders.size()) {
                            blkIdx = _orders.size() - 1;
                        }
                        return Bundle.getMessage("WaitForTrain", cmdIdx,
                                getBlockOrderAt(blkIdx).getBlock().getDisplayName(), speedMsg);

                    case Warrant.WAIT_FOR_SENSOR:
                        return Bundle.getMessage("WaitForSensor",
                                cmdIdx, _engineer.getWaitSensor().getDisplayName(),
                                blockName, speedMsg);

                    case Warrant.RUNNING:
                        return Bundle.getMessage("WhereRunning", blockName, cmdIdx, speedMsg);
                    case Warrant.SPEED_RESTRICTED:
                        return Bundle.getMessage("changeSpeed", blockName, cmdIdx, speedMsg);

                    case Warrant.RAMP_HALT:
                        return Bundle.getMessage("HaltPending", speedMsg, blockName);

                    case Warrant.STOP_PENDING:
                        return Bundle.getMessage("StopPending", speedMsg, blockName, (_waitForSignal
                                ? Bundle.getMessage("Signal") : (_waitForWarrant
                                        ? Bundle.getMessage("Warrant") :Bundle.getMessage("Occupancy"))));

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

    protected String makeWaitMessage(String blockName, int cmdIdx) {
        String which;
        String where = null;
        if (_waitForSignal) {
            which = Bundle.getMessage("Signal");
            OBlock protectedBlock = getBlockAt(_idxProtectSignal);
            if (protectedBlock != null) {
                where = protectedBlock.getDisplayName();
            }
        } else if (_waitForWarrant) {
            Warrant w = getBlockingWarrant();
            if (w != null) {
                which = Bundle.getMessage("WarrantWait",  w.getDisplayName());
            } else {
                which = Bundle.getMessage("WarrantWait", "Unknown");
            }
            if (_stoppingBlock != null) {
                where = _stoppingBlock.getDisplayName();
            }
        } else if (_waitForBlock) {
            which = Bundle.getMessage("Occupancy");
            if (_stoppingBlock != null) {
                where = _stoppingBlock.getDisplayName();
            }
        } else if (_engineer.isRamping() ) {
            String speedMsg = getSpeedMessage(_engineer.getSpeedType(true));
            return Bundle.getMessage("changeSpeed", blockName, cmdIdx, speedMsg);
        } else {
            return Bundle.getMessage(RUN_STATE[_engineer.getRunState()], blockName, cmdIdx);
        }
        if (where != null) {
            return Bundle.getMessage("WaitForClear", blockName, which, where);
        }
        log.warn("makeWaitMessage has flags. But no source for condition!");
        debugInfo();
        return Bundle.getMessage(RUN_STATE[_engineer.getRunState()], blockName, "Error");
    }

    @jmri.InvokeOnLayoutThread
    private void startTracker() {
        ThreadingUtil.runOnGUIEventually(() -> {
            new Tracker(getCurrentBlockOrder().getBlock(), _trainName,
                    null, InstanceManager.getDefault(TrackerTableAction.class));
        });
    }

    public void stopWarrant(boolean abort, boolean turnOffFunctions) {
        _delayStart = false;
        clearWaitFlags(true);
        if (_student != null) {
            _student.dispose(); // releases throttle
            _student = null;
        }
        int oldMode = _runMode;
        _runMode = MODE_NONE;
        _curSignalAspect = null;
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
        deAllocate();

        if (turnOffFunctions && _idxCurrentOrder == _orders.size()-1) { // run was complete to end
            _speedUtil.stopRun(true);   // write speed profile measurements
            if (_addTracker) {
                startTracker();
                _runMode = MODE_MANUAL;
            }
        }
        _addTracker = false;
        fireRunStatus("runMode", oldMode, _runMode);
        if (log.isDebugEnabled()) {
            log.debug("{}: terminated {}.", getDisplayName(), (abort ? "- aborted!" : "normally"));
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
            log.debug("setRunMode({}) ({}) called with _runMode= {}.",
                  getDisplayName(), mode, MODES[mode], MODES[_runMode]);
        }
        _message = null;
        if (_runMode != MODE_NONE) {
            _message = getRunModeMessage();
            log.error(_message);
            return _message;
        }
        _delayStart = false;
        clearWaitFlags(true);
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
                    _waitForBlock = true;
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
            log.debug("{}: Exit setRunMode(). msg= {}", getDisplayName(), _message);
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
            log.debug("{}: acquireThrottle request at {}",
                    getDisplayName(), dccAddress);
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
            log.debug("{}: notifyThrottleFound for address= {}, class= {},",
                  getDisplayName(), throttle.getLocoAddress(), throttle.getClass().getName());
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
                log.debug("{}: controlRunTrain({})= \"{}\" for train {} runMode= {}",
                      getDisplayName(), idx, CNTRL_CMDS[idx], getTrainName(), MODES[_runMode]);
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
            log.debug("{}: controlRunTrain({})= \"{}\" for train {} runstate= {}, in block {}.",
                    idx, CNTRL_CMDS[idx], getTrainName(), RUN_STATE[runState],
                    getDisplayName(), getBlockAt(_idxCurrentOrder).getDisplayName());
        }
        synchronized (this) {
            switch (idx) {
                case RAMP_HALT:
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
                            if (WarrantPreferences.getDefault().getTrace()) {
                                log.info(Bundle.getMessage("userOverRide", _trainName, block.getDisplayName()));
                            }
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
                    if ((_waitForSignal || _waitForBlock || _waitForWarrant) ||
                            (runState != RUNNING && runState != SPEED_RESTRICTED)) {
                        ret = askResumeQuestion(block);
                        if (ret) {
                            if (WarrantPreferences.getDefault().getTrace()) {
                                log.info(Bundle.getMessage("userOverRide", _trainName, block.getDisplayName()));
                            }
                            ret = bumpSpeed();
                        }
                    } else {
                        ret = bumpSpeed();
                    }
                    break;
                case RETRY: // Force move into next block
                    block = getBlockAt(_idxCurrentOrder + 1); 
                    if (block.allocate(this) == null && (block.getState() & OBlock.OCCUPIED) != 0) {
                        if ((_waitForSignal || _waitForBlock || _waitForWarrant) ||
                                (runState != RUNNING && runState != SPEED_RESTRICTED)) {
                            ret = askResumeQuestion(block);
                            if (ret) {
                                if (WarrantPreferences.getDefault().getTrace()) {
                                    log.info(Bundle.getMessage("userOverRide", _trainName, block.getDisplayName()));
                                }
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
                    cancelDelayRamp();
                    _engineer.setSpeedToType(Stop); // sets _halt
                    ret = true;
                    break;
                case ESTOP:
                    cancelDelayRamp();
                    _engineer.setSpeedToType(EStop); // E-stop & halt
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
            clearWaitFlags(false);
            if (_idxCurrentOrder == 0) {
                return setMovement(false);
            } else {
                // get speedType at prior to ramp finish
                return restoreRunning(_engineer.getSpeedType(false));
            }
        }
        return false;
    }

    // User increases speed
    private boolean bumpSpeed() {
        OBlock block = getBlockAt(_idxCurrentOrder);
        // OK, will do as it long as you own it, and you are where you think you are.
        if (block.allocate(this) == null && (block.getState() & OBlock.OCCUPIED) != 0) {
            _engineer.setHalt(false);
            clearWaitFlags(false);
            float speedSetting = _engineer.getSpeedSetting();
            if (speedSetting < 0) { // may have done E-Stop
                speedSetting = 0.0f;
            }
            _engineer.setSpeed(speedSetting + _speedUtil.getRampThrottleIncrement());
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
            if (_waitForWarrant) {
                info.append("\n\tWarrant \"");info.append(getBlockingWarrant().getDisplayName());
                info.append("\" owns block \"");info.append(_stoppingBlock.getDisplayName()); info.append("\"");
            } else {
                Object what = _stoppingBlock.getValue();
                String who;
                if (what != null) {
                    who = what.toString();
                } else {
                    who = "Unknown Train";
                }
                info.append("\n\t\""); info.append(who); info.append("\" occupies Block \""); 
                info.append(_stoppingBlock.getDisplayName()); info.append("\"");
            }
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

            if (!_noRamp) { // _noRamp makes immediate speed changes
                _speedUtil.getBlockSpeedTimes(_commands);   // initialize SpeedUtil
            }
             _engineer = new Engineer(this, throttle);
            if (_tempRunBlind) {
                _engineer.setRunOnET(true);
            }
            if (_delayStart) {
                _engineer.setHalt(true);    // throttle already at 0
                // user must explicitly start train (resume) in a dark block
                fireRunStatus("ReadyToRun", -1, 0);   // ready to start msg
            }

            _engineer.start();

            if (_engineer.getRunState() == Warrant.RUNNING) {
                setMovement(false);
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
     * used so not to totally reallocate whenever entering a block.
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
            log.debug("{}: allocateFromIndex({}) block= {} _partialAllocate= {}.",
                getDisplayName(), index, currentBlock.getDisplayName(), _partialAllocate);
        }
        _message = null;
        boolean passageDenied = false;      // cannot move beyond this point
        boolean allocationDenied = false;   // cannot allocate beyond this point
        for (int i = index; i < limit; i++) {
            BlockOrder bo = _orders.get(i);
            OBlock block = bo.getBlock();
            if (!allocationDenied) {
                // loop back routes may enter a block a second time
                // Do not make current block a stopping block
                if (!currentBlock.equals(block)) {
                    if ((block.getState() & OBlock.OCCUPIED) != 0 && !_delayStart) {
                        if (_message == null) {
                            _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                        }
                        if (_partialAllocate) { // shared warrants may not allocate beyond a stop block
                            allocationDenied = true;
                        }
                        passageDenied = true;   // do not set paths beyond a stop block
                    }
                    if (Warrant.Stop.equals(getPermissibleSpeedAt(bo)) && !_delayStart) {
                        if (_message == null) {
                            _message = Bundle.getMessage("BlockStopAspect", block.getDisplayName());
                        }
                        if (_partialAllocate) {
                            allocationDenied = true;
                        }
                        passageDenied = true;
                    }
                }
                if (!allocationDenied) {
                    String msg = block.allocate(this);
                    if (msg != null && _message == null) {
                        _message = msg;
                        passageDenied = true;
                        allocationDenied = true;
                    }
                }
                if (!passageDenied) {
                    String msg = bo.setPath(this);
                    // setOne==true sets the first time through loop only
                    if (setOne || msg != null) {
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
            log.debug("{}: deallocated Route.", getDisplayName());
        }
    }

    /**
     * Convenience routine to use from Python to start a warrant.
     *
     * @param mode run mode
     */
    public void runWarrant(int mode) {
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
        if (_partialAllocate) { // full route of a shared warrant may be displayed
            deAllocate();   // clear route to allow sharing with another warrant
        }
        // we assume our train is occupying the first block
        _routeSet = false;
        // allocateRoute may set _message for status info, but return null msg
        String msg = allocateRoute(show, orders);
        if (msg != null) {
            _message = msg;
            log.debug("setRoute: {}", msg);
            return _message;
        }
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
            log.debug("{}: checkStartBlock.", getDisplayName());
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
        log.debug("{}: checkforTrackers at block {}", getDisplayName(), block.getDisplayName());
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
            log.debug("{}: checkRoute.", getDisplayName());
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
            log.debug("{}: propertyChange \"{}\" new= {} source= {}", getDisplayName(),
                    property, evt.getNewValue(), ((NamedBean) evt.getSource()).getDisplayName());
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
/*                                } else {
                                    _delayStart = false;
                                    log.error("StoppingBlock \"{}\" set with mode {}", block.getDisplayName(),
                                            MODES[_runMode]);*/
                                }
                                block._entryTime = System.currentTimeMillis();
                                block.setValue(_trainName);
                                block.setState(block.getState() | OBlock.RUNNING);
                            }
                        }
                    }
                } else if ((((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                    //  blocking occupation has left the stopping block
                    ThreadingUtil.runOnGUIDelayed(() -> {
                        clearStoppingBlock();
                    }, 7000);   // 7 seconds
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

    private String getSignalSpeedType(NamedBean signal) {
        String speedType;
        if (signal instanceof SignalHead) {
            SignalHead head = (SignalHead) signal;
            int appearance = head.getAppearance();
            speedType = jmri.InstanceManager.getDefault(SignalSpeedMap.class)
                    .getAppearanceSpeed(head.getAppearanceName(appearance));
            if (log.isDebugEnabled()) {
                log.debug("{}: SignalHead {} sets appearance speed to {}",
                      getDisplayName(), signal.getDisplayName(), speedType);
            }
        } else {
            SignalMast mast = (SignalMast) signal;
            String aspect = mast.getAspect();
            speedType = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAspectSpeed(aspect,
                    mast.getSignalSystem());
            if (log.isDebugEnabled()) {
                log.debug("{}: SignalMast {} sets aspect speed to {}",
                      getDisplayName(), signal.getDisplayName(), speedType);
            }
        }
        return speedType;
    }

    /*
     * _protectSignal made an aspect change
     */
    private void readStoppingSignal() {
        if (_idxProtectSignal < _idxCurrentOrder) { // behind train, irrelevant
            signalAddListener(null, _idxCurrentOrder);  // remove signal
            return;
        }

        String speedType = getSignalSpeedType(_protectSignal);
        if (log.isDebugEnabled()) {
            log.debug("{}: Signal \"{}\" sets speed \"{}\" {} blocks ahead.", 
                    getDisplayName(), _protectSignal.getDisplayName(), speedType, _idxProtectSignal-_idxCurrentOrder);
        }
        
        if (speedType.equals(Warrant.Stop)) {
            float availDist = getAvailableDistance(_idxProtectSignal);
            float changeDist = getEntranceDistance(_engineer.getSpeedSetting(), _idxProtectSignal, speedType);
            if (changeDist < availDist) {
                return;
            } else {
                setMovement(true);
            }
        } else {
            // signal protecting next block just released its hold
            _waitForSignal = false;
            if (WarrantPreferences.getDefault().getTrace()) {
                log.info(Bundle.getMessage("SignalCleared", _protectSignal.getDisplayName(), speedType, _trainName));
            }
            _message = allocateFromIndex(true, _idxCurrentOrder + 1);
            _curSignalAspect = speedType;
            if (_message == null) {
                ThreadingUtil.runOnGUIDelayed(() -> {
                    restoreRunning(speedType);
                }, 3500);   // 3.5 seconds
            } else {
                setStoppingBlock(getBlockAt(_idxCurrentOrder + 1));
                _waitForBlock = true;
                fireRunStatus("waiting", - 1, getRunningMessage());
            }
        }
    }

    /*
     * return distance from the exit of the current block "_idxCurrentOrder"
     * to the entrance of the "idxChange" block.
     */
    private float getAvailableDistance(int idxChange) {
        float availDist = 0;
        int idxBlockOrder = _idxCurrentOrder + 1;
        if (idxBlockOrder < _orders.size() - 1) {
            while (idxBlockOrder < idxChange) {
                availDist += getAvailableDistanceAt(idxBlockOrder++);   // distance to next block
            }
        }
        return availDist;
    }

    /*
     * Get distance needed to ramp so the speed into the next block satisfies the speedType
     * @param idxBlockOrder blockOrder index of entrance block
     */
    private float getEntranceDistance(float speedSetting, int idxBlockOrder, String speedType) {
        float scriptSpeed = _speedUtil.getBlockSpeedInfo(idxBlockOrder).getEntranceSpeed();
        float endSpeed = _speedUtil.modifySpeed(scriptSpeed, speedType);
        float enterLen;
        if (speedSetting <= endSpeed || Math.abs(scriptSpeed - endSpeed) <.005f) {
            if (log.isDebugEnabled()) {
                log.debug("{}: Ramp unnecessary. Script decreases speed from {} to {} before entering block \"{}\".",
                      getDisplayName(), scriptSpeed, endSpeed, getBlockAt(idxBlockOrder).getDisplayName());
            }
            enterLen = 0;
        }
        // compare distance needed for script throttle at entrance to entrance speed,
        // to the distance needed for current throttle to entrance speed.
        enterLen = getRampLengthForEntry(scriptSpeed, endSpeed);
        float nowLen = getRampLengthForEntry(speedSetting, endSpeed);
        if (nowLen > enterLen) {
            enterLen = nowLen;
        }
        // add buffers for signal and safety clearance
        float bufDist = getEntranceBufferDist(idxBlockOrder);
        return enterLen + bufDist;
    }

    private void doStoppingBlockClear() {
        _stoppingBlock.removePropertyChangeListener(this);
        if (_waitForBlock) {
            _waitForBlock = false;
        }
        if (_waitForWarrant) {
            _waitForWarrant = false;
        }
        if (WarrantPreferences.getDefault().getTrace()) {
            log.info(Bundle.getMessage("StopBlockCleared", _stoppingBlock.getDisplayName(), getTrainName()));
        }
        _stoppingBlock = null;
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

        if (allocateFromIndex(true, _idxCurrentOrder + 1) == null) {
            doStoppingBlockClear();
            if (_delayStart) {    // _delayStart
                return true;
            }
            String speedType;
            if (_curSignalAspect != null) {
                speedType = _curSignalAspect;
            } else {
                speedType = _engineer.getSpeedType(false);
            }
            return restoreRunning(speedType);
        }

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
                try {
                    synchronized (this) {
                        String speedType;
                        if (_curSignalAspect != null) {
                            speedType = _curSignalAspect;
                        } else {
                            speedType = _engineer.getSpeedType(false);
                        }
                        while (time < 200) {
                            _message = allocateFromIndex(true, _idxCurrentOrder + 1);
                            if (_message == null) {
                                doStoppingBlockClear();
                                restoreRunning(speedType);
                                break;
                            }
                            wait(20);
                            time += 20;
                        }
                        if (_message != null) {
                            log.warn("{}: unable to clear StoppingBlock message= \"{}\" time= {}", 
                                    getDisplayName(), _message, time);
                        }
                    }
                }
                catch (InterruptedException ie) {
                    log.warn("Warrant \"{}\" InterruptedException message= \"{}\" time= {}", 
                            getDisplayName(), ie.toString(), time);
                    Thread.currentThread().interrupt();
                }
                if (log.isDebugEnabled())
                    log.debug("{}: waited {}ms for clearStoppingBlock to allocateFrom block \"{}\"",
                           getDisplayName(), time, getBlockAt(_idxCurrentOrder + 1).getDisplayName());
            }
        };

        synchronized (allocateBlocks) {
            Thread doit = jmri.util.ThreadingUtil.newThread(
                    () -> {
                        try {
                            javax.swing.SwingUtilities.invokeLater(allocateBlocks);
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
        if (_engineer != null) {
            runState = _engineer.getRunState();
            if (!_waitForSignal && !_waitForBlock && !_waitForWarrant &&
                    runState != HALT && runState != RAMP_HALT) {
                ret = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("{}: okToRun()= {}: runState={} _waitForSignal={} _waitForBlock={} _waitForWarrant={}.",
                    getDisplayName(), ret, (runState < 0 ? "No Engineer":RUN_STATE[runState]),
                    _waitForSignal, _waitForBlock, _waitForWarrant);
        }
        return ret;
    }

    /**
     * A layout condition that has restricted or stopped a train has been cleared.
     * i.e. Signal aspect, rogue occupied block, contesting warrant or user halt. 
     * This may or may not be all the conditions restricting speed.
     * @return true if automatic restart is done
     */
    private boolean restoreRunning(String speedType) {
        if (okToRun()) {
            getBlockOrderAt(_idxCurrentOrder).setPath(this);
            cancelDelayRamp();
            _engineer.rampSpeedTo(speedType, -1);
            _curSignalAspect = null;
            fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder);
            if (log.isDebugEnabled()) {
                log.debug("{}: restoreRunning(): rampSpeedTo to \"{}\"",
                        getDisplayName(), speedType);
            }
            return true;
        }
        log.warn("{}: Cannot restore Running.", getDisplayName());
        fireRunStatus("waiting", - 1, getRunningMessage());
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
        String speedType;
        if (_curSignalAspect != null) {
            speedType = _curSignalAspect;
        } else {
            speedType = _engineer.getSpeedType(false);
        }
        restoreRunning(speedType);
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
            String msg = "{}: sets _shareTOBlock= \"{}\" owned by warrant \"{}\"";
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
        log.info(Bundle.getMessage("StopBlockSet", _stoppingBlock.getDisplayName(), getTrainName()));
        if (log.isDebugEnabled()) {
            String msg = "{}: sets _stoppingBlock= \"{}\"";
            if (prevBlk != null) {
                msg = msg + ", removes \"{}\"";
            }
            log.debug(msg, getDisplayName(), _stoppingBlock.getDisplayName(),
                    (prevBlk == null ? "" : prevBlk.getDisplayName()));
        }
    }

    /*
     * return true if signal is set.
     */
    private boolean setStoppingSignal(int idx) {
        BlockOrder blkOrder = getBlockOrderAt(idx);
        NamedBean signal = blkOrder.getSignal();

        if (_protectSignal != null && _protectSignal.equals(signal)) {
            // Must be the route coming back to the same block. Same signal, move index only.
            if (_idxProtectSignal < idx && idx >= 0) {
                _idxProtectSignal = idx;
            }
            return true;
        }

        if (_protectSignal != null) {
            if (idx > _idxProtectSignal && _idxProtectSignal > _idxCurrentOrder) {
                return true;
            }
        }

        return signalAddListener(signal, idx);
    }

    private boolean signalAddListener(NamedBean  signal,  int signalIndex) {
        if (signalIndex == _idxProtectSignal) {
            return true;
        }
        StringBuffer sb = new StringBuffer(getDisplayName());
        if (_protectSignal != null) {
            _protectSignal.removePropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                sb.append(" - removes signal \""); 
                sb.append(_protectSignal.getDisplayName());
                sb.append(" at \""); 
                sb.append(getBlockAt(_idxProtectSignal).getDisplayName());
                sb.append("\"");
            }
            _protectSignal = null;
            _idxProtectSignal = -1;
        }
        boolean ret = false;
        if (signal != null) {
            _protectSignal = signal;
            _idxProtectSignal = signalIndex;
            _protectSignal.addPropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                sb.append(" - sets signal \"");
                sb.append(_protectSignal.getDisplayName());
                sb.append(" at \""); 
                sb.append(getBlockAt(_idxProtectSignal).getDisplayName());
                sb.append("\"");
            }
            ret = true;
        }
        if (log.isDebugEnabled()) {
            log.debug(sb.toString());
        }
        return ret;
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
                stopWarrant(true, true);
                return;
            }
        }

        if (_runMode == MODE_NONE) {
            return;
        }
        int activeIdx = getIndexOfBlock(block, _idxCurrentOrder);
        if (log.isDebugEnabled()) {
            log.debug("{}: **Block \"{}\" goingActive. activeIdx= {}, _idxCurrentOrder= {}.",
                    getDisplayName(), block.getDisplayName(), activeIdx, _idxCurrentOrder);
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
                log.error("NoEngineer! warrant {}", getDisplayName());
                return;
            }
        } else {
            runState =  _engineer.getRunState();
        }
        if (activeIdx == _idxCurrentOrder) {
            // unusual occurrence.  dirty track? sensor glitch?
            if (WarrantPreferences.getDefault().getTrace()) {
                log.info("{}: Train \"{}\" regained Detection at \"{}\"", 
                        getDisplayName(), getTrainName(), block.getDisplayName());
            }
        } else if (activeIdx == _idxCurrentOrder + 1) {
            if (_delayStart) {
                log.warn("{}: Rogue entered Block \"{}\" ahead of {}.",
                        getDisplayName(), block.getDisplayName(), getTrainName());
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
                    // Apparently NOT already stopped and NOT about to be.
                    // Therefore, assume a Rogue has just entered.
                    log.info("Train \"{}\" at speed 0, runState= {}, but block \"{}\" entered.", 
                            getTrainName(), RUN_STATE[runState], block.getDisplayName());
                    setStoppingBlock(block);
                    _waitForBlock = true;   // normally SetMovement would do this
                    _engineer.setSpeedToType(Warrant.Stop);     // for safety
                    return;
                }
            }
            // Since we are moving at speed we assume it is our train entering the block
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
                log.debug("{}: Train leaving UNDETECTED block \"{}\" now entering block\"{}\"",
                        getDisplayName(), prevBlock.getDisplayName(), block.getDisplayName());
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
            _engineer.clearWaitForSync(block); // Sync commands if train is faster than ET
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
        } else { // train is in last block. past all signals, etc
            if (_runMode == MODE_MANUAL) { // no script, so terminate warrant run
                stopWarrant(false, true);
            }
        }
        if (log.isTraceEnabled()) {
            log.debug("{}: end of goingActive. leaving \"{}\" entered \"{}\"",
                    getDisplayName(), getBlockAt(activeIdx - 1).getDisplayName(), block.getDisplayName());
        }
        setMovement(false);
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

        int idx = getIndexOfBlockBefore(_idxCurrentOrder, block); // if idx >= 0, it is in this warrant
        if (log.isDebugEnabled()) {
            log.debug("{}: *Block \"{}\" goingInactive. idx= {}, _idxCurrentOrder= {}.",
                    getDisplayName(), block.getDisplayName(), idx, _idxCurrentOrder);
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
                               _idxCurrentOrder -= 1;       // set head to previous BlockOrder
                               prevBlock.allocate(this);    // insure it is still allocated.
                               lost = false;
                           }
                       }
                       if (lost) {
                           log.warn("block \"{}\" goingInactive. train is lost! warrant {}",
                                       block.getDisplayName(), getDisplayName());
                           fireRunStatus("blockChange", block, null);
                           if (_engineer != null) {
                               _engineer.setSpeedToType(EStop);   // halt and set 0 throttle
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
                    _idxCurrentOrder -= 1;      // set head to previous BlockOrder
                    prevBlock.allocate(this);   // insure it is still allocated.
                } else {
                    log.warn("block \"{}\" Last Block goingInactive. train is lost! warrant {}",
                            block.getDisplayName(), getDisplayName());
                    if (_engineer != null) {
                        _engineer.setSpeedToType(Stop);   // set 0 throttle
                    }
                }
            }
        }
    } // end goingInactive

    /**
     * Deallocates all blocks prior to and including block
     * Comes from goingInactive, i.e. warrant has a listener on the block.
     */
    private void releaseBlock(OBlock block, int idx) {
        /*
         * Deallocate block if train will not use the block again. Warrant
         * could loop back and re-enter blocks previously traversed. That is,
         * they will need to re-allocation of blocks ahead. 
         * Previous Dark blocks also need deallocation.
         */
        if (_partialAllocate) { // shared route
            for (int i = idx; i > -1; i--) {
                OBlock prevBlock = getBlockAt(i);
                if (!prevBlock.deAllocate(this)) {
                    // not allocated to this or owned by someone else
                    break;
                } else if (prevBlock.equals(_stoppingBlock)){
                    doStoppingBlockClear();
                }
                _totalAllocated = false;
            }
        } else {
            for (int i = idx; i > -1; i--) {
                boolean neededLater = false;
                OBlock prevBlock = getBlockAt(i);
                for (int j = i + 1; j < _orders.size(); j++) {
                    if (prevBlock.equals(getBlockAt(j))) {
                        neededLater = true;
                    }
                }
                if (!neededLater) {
                    if (!prevBlock.deAllocate(this)) {
                        // not allocated to this or owned by someone else
                        break;
                    } else if (prevBlock.equals(_stoppingBlock)){
                        doStoppingBlockClear();
                    }
                    _totalAllocated = false;
                } else {
                    if (prevBlock.isAllocatedTo(this)) {
                        // must deallocate, but must listen for followers
                        // who may be occupying the block
                        prevBlock.setValue(null);
//                        prevBlock.releaseWarrant(this);
                    }
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
                log.debug("{}: getPermissibleSpeedAt(): \"{}\" Signal speed= {}",
                        getDisplayName(), block.getDisplayName(), speedType);
            }
        } else { //  if signal is configured, ignore block
            speedType = block.getBlockSpeed();
            if (speedType.equals("")) {
                speedType = null;
            }
            if (speedType != null) {
                if (log.isDebugEnabled()) {
                    log.debug("{}: getPermissibleSpeedAt(): \"{}\" Block speed= {}",
                            getDisplayName(), block.getDisplayName(), speedType);
                }
            }
        }
/*        if (speedType == null) {
            speedType = Normal;
        }*/
        return speedType;
    }

    synchronized private void cancelDelayRamp() {
        if (_delayCommand != null) {
            _delayCommand.interrupt();
            log.debug("{}: cancelDelayRamp called.", getDisplayName());
            _delayCommand = null;
        }
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
                        log.debug("{}: CommandDelay: after wait of {}ms, start Ramp to {}.",
                                getDisplayName(), _waitTime, nextSpeedType);
                    }
                    _engineer.rampSpeedTo(nextSpeedType, _endBlockIdx);
                }
            }
            endDelayCommand();
        }
    }

    synchronized private void endDelayCommand() {
        _delayCommand = null;
    }

    private void clearWaitFlags(boolean removeListeners) {
        log.debug("{}: Flags cleared {}.", getDisplayName(), removeListeners?"and removed Listeners":"only");
        _waitForBlock = false;
        _waitForSignal = false;
        _waitForWarrant = false;
        if (removeListeners) {
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
        }
    }
    /**
     * Orders are parsed to get any speed restrictions. Called from
     * setMovement
     * 
     * @param idxBlockOrder index of Orders
     * @return Speed type name
     */
    private String getSpeedTypeForBlock(int idxBlockOrder, boolean okToAllocate) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlockOrder);
        OBlock block = blkOrder.getBlock();

        String speedType = getPermissibleSpeedAt(blkOrder);
        if (Warrant.Stop.equals(speedType)) {
            // block speed cannot be Stop, so OK to assume signal
            _waitForSignal = true;
            if (_partialAllocate) {
                okToAllocate = false;
            }
        }

        if (okToAllocate) {
            _message = block.allocate(this);
        }
        if (_message != null) {  // only 2 messages possible
            Warrant w = block.getWarrant();
            if (w != null && !this.equals(w)) {
                _waitForWarrant = true; // w owns block
            } else {
                _waitForBlock = true;   // OUT_OF_SERVICE
            }
            speedType = Warrant.Stop;
        }            

        if ((block.getState() & OBlock.OCCUPIED) != 0) {
            if (idxBlockOrder > _idxCurrentOrder) {
                _waitForBlock = true;       // OCCUPIED
                speedType = Warrant.Stop;
            }
        } 
        if (!Warrant.Stop.equals(speedType) && okToAllocate) {
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
                log.debug ("{}: \"{}\" speed change of type \"{}\" needed to enter block \"{}\".",
                        getDisplayName(), (_waitForSignal?"Signal":(_waitForWarrant?"Warrant":"Block")), 
                        speedType, block.getDisplayName());
            }
        }
        return speedType;
    }

    /*
     * Return pathLength of the block.
     */
    private float getAvailableDistanceAt(int idxBlockOrder) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlockOrder);
        float pathLength = blkOrder.getPath().getLengthMm();
        if (idxBlockOrder == 0 || pathLength <= 1.0f) {
            // Position in block is unknown. use calculated distances instead
            float blkDist = _speedUtil.getBlockSpeedInfo(idxBlockOrder).getDistance();
            if (log.isDebugEnabled()) {
                log.debug("{}: getAvailableDistanceAt: block \"{}\" using calculated blkDist= {}, pathLength= {}",
                        getDisplayName(), blkOrder.getBlock().getDisplayName(), blkDist, pathLength);
            }
            return blkDist;
        } else {
            return pathLength;
        }
    }

    private float getRampLengthForEntry(float scriptSpeed, float endSpeed) {
        String speedType = _engineer.getSpeedType(false);
        float currentSpeed = _speedUtil.modifySpeed(scriptSpeed, speedType);
        RampData ramp = _speedUtil.getRampForSpeedChange(currentSpeed, endSpeed);
        float enterLen = ramp.getRampLength(speedType);
        if (log.isTraceEnabled()) {
            log.debug("{}: getRampLengthForEntry: from speed={} to speed={}. rampLen={}",
                    getDisplayName(), scriptSpeed, endSpeed, enterLen);
        }
        return enterLen;
    }

    private float getEntranceBufferDist(int idxBlockOrder) {
        float bufDist = 9144 / WarrantPreferences.getDefault().getLayoutScale();// add 30 scale feet for safety distance
        if (_waitForSignal) {        // signal restricting speed
            bufDist+= getBlockOrderAt(idxBlockOrder).getEntranceSpace(); // signal's adjustment
        }
        return bufDist;
    }
/*
    private void speedOverrun(BlockOrder blkOrder, String speedType) {
        jmri.NamedBean signal = blkOrder.getSignal();
        signalAddListener(signal, _idxCurrentOrder);
        log.info("Train {} moved past required speed of \"{}\" for signal \"{}\" in block \"{}\"! warrant {}",
                getTrainName(), speedType, signal.getDisplayName(), blkOrder.getBlock().getDisplayName(), getDisplayName());
        fireRunStatus("SignalOverrun", signal.getDisplayName(), speedType); // message of speed violation
    }*/

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
     *@param signalChange speed change occurred at while traversing a block 
     * @return false on errors
     */
    private boolean setMovement(boolean signalChange) {
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
        String currentSpeedType = _engineer.getSpeedType(true); // current or pending speed type
        if (log.isDebugEnabled()) {
            log.debug("{}: SET MOVEMENT Block\"{}\" runState= {}, currentSpeedType= {}. {}.",
                    getDisplayName(), curBlock.getDisplayName(), RUN_STATE[runState], currentSpeedType,
                    (_partialAllocate ? "ShareRoute" : ""));
        }

        _message = blkOrder.setPath(this);
        if (_message != null) {
            log.error("Train {} in block \"{}\" but path cannot be set! msg= {}, warrant {}",
                    getTrainName(), curBlock.getDisplayName(), _message, getDisplayName());
            _engineer.setSpeedToType(Stop);   // speed set to 0.0 (not E-top) User must restart
            return false;
        }

        if ((curBlock.getState() & (OBlock.OCCUPIED | OBlock.UNDETECTED)) == 0) {
            log.error("Train {} expected in block \"{}\" but block is unoccupied! warrant {}",
                    getTrainName(), curBlock.getDisplayName(), getDisplayName());
            _engineer.setSpeedToType(Stop); // user needs to see what happened and restart
            return false;
        }
        // Error checking done.

        // checking situation for the current block
        // currentSpeedType is the speed type train is currently running or ramping to
        // entrySpeedType is the required speed limit for this block

        float speedSetting = _engineer.getSpeedSetting();   // current speed
        if (log.isDebugEnabled()) {
            log.debug("{}: Stopping flags: _waitForBlock={}, _waitForSignal={}, _waitForWarrant={} runState= {}, speedSetting= {}, speedType= {}.",
                    getDisplayName(), _waitForBlock, _waitForSignal, _waitForWarrant, RUN_STATE[runState], speedSetting, currentSpeedType);
        }

        if (_noRamp) {
            if (_idxCurrentOrder < _orders.size() - 1) {
                String entrySpeedType = getSpeedTypeForBlock(_idxCurrentOrder + 1, true);
                if (entrySpeedType != null && _speedUtil.secondGreaterThanFirst(entrySpeedType, currentSpeedType)) {
                    if (log.isDebugEnabled()) {
                        log.debug("{}: No ramp speed change of \"{}\" from \"{}\" in block \"{}\"",
                              getDisplayName(), entrySpeedType, currentSpeedType, curBlock.getDisplayName());
                    }
                    _engineer.setSpeedToType(entrySpeedType);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("{}: Exit setMovement due to no ramping.", getDisplayName());
            }
            return true;
        }
        
        if (runState == WAIT_FOR_CLEAR || runState == HALT || runState == STOP_PENDING || runState == RAMP_HALT) {
            if (log.isDebugEnabled()) {
                log.debug("{}: Train drift into block \"{}\". runState= {}, Engineer holds train. speedSetting= {}",
                        getDisplayName(), curBlock.getDisplayName(), RUN_STATE[runState], speedSetting);
            }
            int idxStop = getIndexOfBlock(_stoppingBlock, _idxCurrentOrder);
            if (_idxProtectSignal == _idxCurrentOrder) {
                fireRunStatus("SignalOverrun", _protectSignal.getDisplayName(), currentSpeedType); // message of speed violation
            } else if (idxStop == _idxCurrentOrder) {
                fireRunStatus("OccupyOverrun", _stoppingBlock.getDisplayName(), _trainName); // message of speed violation
            } else {
                fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder); // message reason for hold
            }
            currentSpeedType = Stop;
        }

        if (currentSpeedType.equals(Stop)) {
            if (speedSetting > 0) {
                _engineer.setSpeedToType(Stop); // immediate decrease
            }
            fireRunStatus("SpeedChange", _idxCurrentOrder - 1, _idxCurrentOrder); // message reason for hold
            return true;
        }

        if (_idxCurrentOrder == _orders.size() - 1) {
            return true;
        }

        clearWaitFlags(false);
        // look ahead for speed type slower than current type, refresh flags

        String speedType = currentSpeedType;
        int idxSpeedChange = _idxCurrentOrder;   // idxBlockOrder where speed changes before entry
        while (!_speedUtil.secondGreaterThanFirst(speedType, currentSpeedType) && idxSpeedChange < _orders.size() - 1) {
            idxSpeedChange++;
            String s = getSpeedTypeForBlock(idxSpeedChange, false);  // speed for entry into next block
            if (s != null) {
                speedType = s;
            }
        }

        OBlock block = getBlockAt(idxSpeedChange);
        // Now set stopping condition flags for blocks, if any
        if (_waitForBlock || _waitForWarrant) {
            setStoppingBlock(block);
        }
        float availDist = getAvailableDistance(idxSpeedChange);  // distance ahead (excluding current block
        float changeDist = getEntranceDistance(speedSetting, idxSpeedChange, speedType);    // distance needed to change speed for speedType

        if (log.isDebugEnabled()) {
            log.debug("{}: Speed \"{}\" at block \"{}\" until speed \"{}\" at block \"{}\", availDist={}, enterDist={}",
                    getDisplayName(), currentSpeedType, curBlock.getDisplayName(), speedType, block.getDisplayName(), availDist, changeDist);
        }

        if (changeDist <= availDist) {
            // watch next signal after current block for speed change
            for (int i = _idxCurrentOrder + 1; i < _orders.size() - 1; i++) {
                if (setStoppingSignal(i)) {
                   break;
               }
            }
            clearWaitFlags(false);
            return true;
        }

        // Begin a ramp for speed change in this block. If due to a signal, watch that one
        if(_waitForSignal) {    // watch the signal requiring the speed change
            setStoppingSignal(idxSpeedChange);
        } else {    // watch whichever is next signal
            for (int i = _idxCurrentOrder + 1; i < _orders.size() - 1; i++) {
                 if (setStoppingSignal(i)) {
                    break;
                }
            }
        }

        // either ramp in progress or no changes needed. Stopping conditions set, so move on.
        if (currentSpeedType.equals(speedType)) {
            return true;
        }

        float curAvail = getAvailableDistanceAt(_idxCurrentOrder);  // length of this block
        if (signalChange) { // setMovement called at time of change.
            curAvail -= _speedUtil.getDistanceTravelled();
            if (curAvail < 0) {
                curAvail = 0;
            }
        }
        availDist += curAvail;   // Add available length in this block

        if (changeDist > availDist) {
            log.warn("No room for train {} to ramp to speed \"{}\" in block \"{}\"!. availDist={}, enterDist={} on warrant {}",
                    getTrainName(), speedType, curBlock.getDisplayName(), availDist,  changeDist, getDisplayName());
            _engineer.rampSpeedTo(speedType, idxSpeedChange - 1);
            return false;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("{}: Speed decrease to \"{}\" from {} needed before entering block \"{}\", availDist={}, enterDist={}",
                        getDisplayName(), speedType, currentSpeedType, block.getDisplayName(), availDist, changeDist);
            }
        }

        // Must start the ramp in current block. ( at _idxCurrentOrder)
        // find the time when ramp should start in this block, then use thread CommandDelay to start the ramp.
        // Train must travel a deltaDist for a deltaTime to the start of the ramp.
        // It travels at throttle settings of scriptSpeed(s) modified by currentSpeedType.
        // trackSpeed(s) of these modified scriptSettings are computed from SpeedProfile
        // waitThrottle is throttleSpeed when ramp is started. This may not be the scriptSpeed now
        // Start with waitThrottle (modSetting) being at the entrance to the block.
        // modSetting gives the current trackSpeed.
        // accumulate the time and distance and determine the distance (changeDist) needed for entrance into 
        // block (at idxSpeedChange) requiring speed change to speedType
        // final ramp should modify waitSpeed to endSpeed and must end at the exit of end block (endBlockIdx)
        BlockSpeedInfo blkSpeedInfo = _speedUtil.getBlockSpeedInfo(_idxCurrentOrder);

        int cmdIdx = blkSpeedInfo.getFirstIndex();
        int endIdx = _speedUtil.getBlockSpeedInfo(idxSpeedChange - 1).getLastIndex();
        float scriptSpeed;  // script throttle setting
        float modSetting;   // modified setting
        float trackSpeed;   // mm/sec track speed at modSetting
        float timeRatio; // time adjustment for current speed type.
        if (_idxCurrentOrder == 0) {
            scriptSpeed = 0;
            modSetting = 0;
            trackSpeed = 0;
            timeRatio = 1;
            changeDist = 0;
        } else {
            scriptSpeed = blkSpeedInfo.getEntranceSpeed();
            modSetting = _speedUtil.modifySpeed(scriptSpeed, currentSpeedType);
            trackSpeed = _speedUtil.getTrackSpeed(modSetting);
            timeRatio = _speedUtil.getTrackSpeed(scriptSpeed) / trackSpeed;
        }
        if (log.isDebugEnabled()) {
            log.debug("cmdIdx #{} to #{} at speedType \"{}\" modScriptSetting={} speedSetting={}, timeRatio={}",
                    cmdIdx+1, endIdx+1, currentSpeedType, modSetting, speedSetting, timeRatio);
            // command index numbers biased by 1
        }
        float accumTime = 0;    // accumulated time of commands up to ramp start
        float accumDist = 0;
        float remDist = availDist - changeDist;
        float deltaTime;
        if (trackSpeed > 0) {
            deltaTime = remDist / trackSpeed;
        } else {
            // actually infinity (Float.MAX_VALUE) Must be a low value in case there are no speed changes
            deltaTime = 100;
        }
        for (int i = cmdIdx; i <= endIdx; i++) {
            ThrottleSetting ts = _commands.get(i);
            accumDist += trackSpeed * ts.getTime() * timeRatio;
            accumTime += ts.getTime() * timeRatio;
            if (changeDist + accumDist >= availDist) {
                float overDist = changeDist + accumDist - availDist;
                if (trackSpeed <= 0) {
                    log.error("Cannot ramp to \"{}\". trackSpeed= {} in block \"{}\".",
                            speedType, trackSpeed, curBlock.getDisplayName());
                }
                accumTime -= overDist / trackSpeed;
                deltaTime = accumTime;
                break;
            }
            ThrottleSetting.CommandValue cmdVal = ts.getValue();
            if (cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_FLOAT)) {
                scriptSpeed = cmdVal.getFloat();
                changeDist = getEntranceDistance(scriptSpeed, idxSpeedChange, speedType);
                modSetting = _speedUtil.modifySpeed(scriptSpeed, currentSpeedType);
                trackSpeed = _speedUtil.getTrackSpeed(modSetting);
                timeRatio = _speedUtil.getTrackSpeed(scriptSpeed) / trackSpeed;
           }
           log.debug("{}: cmd#{} accumTime= {} accumDist= {} changeDist= {}", getDisplayName(), i+1, accumTime, accumDist, changeDist);
        }

        int waitTime = Math.round(deltaTime);
        if (log.isDebugEnabled()) {
            log.debug("{}: RAMP waitTime={}, waitThrottle={}, availDist={}, enterLen={} for ramp start",
                    getDisplayName(), waitTime, modSetting, availDist, changeDist);
        }

        waitTime -= 50;     // Subtract a bit to avoid last unwanted speed command
        rampSpeedDelay(waitTime, speedType, modSetting, idxSpeedChange - 1);
        return true;
    }   // end setMovement

    synchronized private void rampSpeedDelay (long waitTime, String speedType, float waitSpeed, int endBlockIdx) {
        if (_delayCommand != null) {
            if (_delayCommand.isDuplicate(speedType, waitTime, endBlockIdx)) {
                return;
            }
            cancelDelayRamp();
        }
        if (waitTime <= 0) {
            _engineer.rampSpeedTo(speedType, endBlockIdx);
        } else {    // cancelDelayRamp has been called
            _delayCommand = new CommandDelay(speedType, waitTime, waitSpeed, endBlockIdx);
            _delayCommand.start();
            if (log.isDebugEnabled()) {
                log.debug("{}: CommandDelay: will wait {}ms, then Ramp to {} in block {}.",
                        getDisplayName(), waitTime, speedType, getBlockAt(endBlockIdx).getDisplayName());
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
