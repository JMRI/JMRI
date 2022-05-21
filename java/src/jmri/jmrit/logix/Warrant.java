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
 * or _protectSignal since it cannot control speed. It does attempt to realign
 * the route as needed, but can be thwarted.
 * <p>
 * Version 1.11 - remove setting of SignalHeads
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2022
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
    private boolean _shareRoute;// only allocate one block at a time for sharing route.
    private boolean _addTracker;    // start tracker when warrant ends normally.
    private boolean _haltStart;     // Hold train in Origin block until Resume command
    private boolean _noRamp; // do not ramp speed changes. make immediate speed change when entering approach block.
    private boolean _nxWarrant = false;

    // transient members
    private LearnThrottleFrame _student; // need to callback learning throttle in learn mode
    private boolean _tempRunBlind; // run mode flag to allow running on ET only
    private boolean _delayStart; // allows start block unoccupied and wait for train
    private boolean _lost;      // helps recovery if _idxCurrentOrder block goes inactive
    private boolean _overrun;   // train overran a signal or warrant stop
    private boolean _overBlkOccupied;  // test for overruns. =true assumes occupied by another train
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
    private int _idxStoppingBlock;      // BlockOrder index of _stoppingBlock
    private NamedBean _protectSignal; // Signal stopping train movement
    private int _idxProtectSignal;      // BlockOrder index of _protectSignal

    private boolean _waitForSignal; // train may not move until false
    private boolean _waitForBlock; // train may not move until false
    private boolean _waitForWarrant;
    private String _curSignalAspect;   // speed type to restore when flags are cleared;
    protected String _message; // last message returned from an action
    private ThrottleManager tm;

    // Running modes
    public static final int MODE_NONE = 0;
    public static final int MODE_LEARN = 1; // Record a command list
    public static final int MODE_RUN = 2;   // Autorun, playback the command list
    public static final int MODE_MANUAL = 3; // block detection of manually run train
    static final String[] MODES = {"none", "LearnMode", "RunAuto", "RunManual", "Abort"};
    public static final int MODE_ABORT = 4; // used to set status string in WarrantTableFrame

    // control states
    public static final int STOP = 0;
    public static final int HALT = 1;
    public static final int RESUME = 2;
    public static final int ABORT = 3;
    public static final int RETRY_FWD = 4;
    public static final int ESTOP = 5;
    public static final int RAMP_HALT = 6;
    public static final int SPEED_UP = 7;
    public static final int RETRY_BKWD = 8;
    public static final int DEBUG = 9;
    static final String[] CNTRL_CMDS = {"Stop", "Halt", "Resume", "Abort", "MoveToNext", 
            "EStop", "SmoothHalt", "SpeedUp", "MoveToPrevious","Debug"};

    // engineer running states
    protected static final int RUNNING = 7;
    protected static final int SPEED_RESTRICTED = 8;
    protected static final int WAIT_FOR_CLEAR = 9;
    protected static final int WAIT_FOR_SENSOR = 10;
    protected static final int WAIT_FOR_TRAIN = 11;
    protected static final int WAIT_FOR_DELAYED_START = 12;
    protected static final int LEARNING = 13;
    protected static final int STOP_PENDING = 14;
    static final String[] RUN_STATE = {"HaltStart", "atHalt", "Resumed", "Aborts", "Retried",
            "EStop", "HaltPending", "Running", "changeSpeed", "WaitingForClear", "WaitingForSensor",
            "RunningLate", "WaitingForStart", "RecordingScript", "StopPending"};

    static final float BUFFER_DISTANCE = 50*12*25.4F / WarrantPreferences.getDefault().getLayoutScale(); // 50 scale feet for safety distance
    protected boolean _trace = WarrantPreferences.getDefault().getTrace();

    // Speed states: steady, increasing, decreasing
    static final int AT_SPEED = 1;
    static final int RAMP_DOWN = 2;
    static final int RAMP_UP = 3;
    public enum SpeedState {
        STEADY_SPEED(AT_SPEED, "SteadySpeed"),
        RAMPING_DOWN(RAMP_DOWN, "RampingDown"),
        RAMPING_UP(RAMP_UP, "RampingUp");

        int _speedStateId;  // state id
        String _bundleKey; // key to get state display name

        SpeedState(int id, String bundleName) {
            _speedStateId = id;
            _bundleKey = bundleName;
        }

        public int getIntId() {
            return _speedStateId;
        }

        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }

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
        _idxProtectSignal = -1;
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

    protected int getNumOrders() {
        return _orders.size();
    }
    /*
     * Used only by SCWarrant
     * SCWarrant overrides goingActive
     */
    protected void incrementCurrentOrderIndex() {
        _idxCurrentOrder++;
    }

    /**
     * Find index of a block AFTER BlockOrder index.
     *
     * @param block used by the warrant
     * @param idx start index of search
     * @return index of block after of block order index, -1 if not found
     */
    protected int getIndexOfBlockAfter(OBlock block, int idx) {
        for (int i = idx; i < _orders.size(); i++) {
            if (_orders.get(i).getBlock().equals(block)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find index of block BEFORE BlockOrder index.
     *
     * @param idx start index of search
     * @param block used by the warrant
     * @return index of block before of block order index, -1 if not found
     */
    protected int getIndexOfBlockBefore(int idx, OBlock block) {
        for (int i = idx; i >= 0; i--) {
            if (_orders.get(i).getBlock().equals(block)) {
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
        _shareRoute = set;
    }

    public void setAddTracker (boolean set) {
        _addTracker = set;
    }

    public void setHaltStart (boolean set) {
        _haltStart = set;
    }

    public boolean getNoRamp() {
        return _noRamp;
    }

    public boolean getShareRoute() {
        return _shareRoute;
    }

    public boolean getAddTracker() {
        return _addTracker;
    }

    public boolean getHaltStart() {
        return _haltStart;
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
/*
    protected void setWaitingForSignal(Boolean set) {
        _waitForSignal = set;
    }
    protected void setWaitingForBlock(Boolean set) {
        _waitForBlock = set;
    }
    protected void setWaitingForWarrant(Boolean set) {
        _waitForWarrant = set;
    }
    */
    protected boolean isWaitingForSignal() {
        return _waitForSignal;
    }
    protected boolean isWaitingForBlock() {
        return _waitForBlock;
    }
    protected boolean isWaitingForWarrant() {
        return _waitForWarrant;
    }
    protected Warrant getBlockingWarrant() {
        if (_stoppingBlock != null && !this.equals(_stoppingBlock.getWarrant())) {
            return _stoppingBlock.getWarrant();
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

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    protected String getRunningMessage() {
        if (_delayStart) {
            return Bundle.getMessage("waitForDelayStart", _trainName, getBlockAt(0).getDisplayName());
        }
        switch (_runMode) {
            case Warrant.MODE_NONE:
                _message = null;
            case Warrant.MODE_ABORT:
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
                OBlock block = getBlockAt(_idxCurrentOrder);
                if ((block.getState() & (Block.OCCUPIED | Block.UNDETECTED)) == 0) {
                    return Bundle.getMessage("TrackerNoCurrentBlock", _trainName, block.getDisplayName());
                }
                String blockName = block.getDisplayName();
                String speedMsg = getSpeedMessage(_engineer.getSpeedType(true));
                int runState = _engineer.getRunState();

                switch (runState) {
                    case Warrant.ABORT:
                        if (cmdIdx == _commands.size() - 1) {
                            return Bundle.getMessage("endOfScript", _trainName);
                        }
                        return Bundle.getMessage("Aborted", blockName, cmdIdx);

                    case Warrant.HALT:
                        return Bundle.getMessage("RampHalt", getTrainName(), blockName);
                    case Warrant.WAIT_FOR_CLEAR:
                        SpeedState ss = _engineer.getSpeedState();
                        if (ss.equals(SpeedState.STEADY_SPEED)) {
                            return  makeWaitMessage(blockName, cmdIdx);
                        } else {
                            return Bundle.getMessage("Ramping", ss.toString(), speedMsg, blockName);
                        }
                    case Warrant.WAIT_FOR_TRAIN:
                        if (_engineer.getSpeedSetting() <= 0) {
                            return makeWaitMessage(blockName, cmdIdx);
                        } else {
                            return Bundle.getMessage("WaitForTrain", cmdIdx,
                                    _engineer.getSynchBlock().getDisplayName(), speedMsg);
                        }
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
                log.error("{} Unknown speed interpretation {}", getDisplayName(), speedMap.getInterpretation());
                throw new java.lang.IllegalArgumentException("Unknown speed interpretation " + speedMap.getInterpretation());
        }
        return Bundle.getMessage("atSpeed", speedType, Math.round(speed), units);
    }

    private String makeWaitMessage(String blockName, int cmdIdx) {
        String which = null;
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
        }
        int runState = _engineer.getRunState();
        if (which == null) {
            if (runState == HALT || runState == RAMP_HALT) {
                which = Bundle.getMessage("Halt");
                where = blockName;
            }
        }
        if (_engineer.isRamping() && runState != RAMP_HALT) {
            String speedMsg = getSpeedMessage(_engineer.getSpeedType(true));
            return Bundle.getMessage("changeSpeed", blockName, cmdIdx, speedMsg);
        }
        
        if (where == null) {
            // flags can't identify cause.
            if (_message == null) {
                _message = Bundle.getMessage(RUN_STATE[runState], blockName);
            }
            return Bundle.getMessage("trainWaiting", getTrainName(), _message, blockName);
        }
        return Bundle.getMessage("WaitForClear", blockName, which, where);
    }

    @jmri.InvokeOnLayoutThread
    private void startTracker() {
        ThreadingUtil.runOnGUIEventually(() -> {
            new Tracker(getCurrentBlockOrder().getBlock(), _trainName,
                    null, InstanceManager.getDefault(TrackerTableAction.class));
        });
    }

    // Get engineer thread to TERMINATED state - didn't answer CI test problem, but let it be.
    private void killEngineer(Engineer engineer, boolean abort, boolean functionFlag) {
        engineer.stopRun(abort, functionFlag); // releases throttle
        engineer.interrupt();
        if (!engineer.getState().equals(Thread.State.TERMINATED)) {
            Thread curThread = Thread.currentThread();
            if (!curThread.equals(_engineer)) {
                kill( engineer, abort, functionFlag, curThread);
            } else {   // can't join yourself if called by _engineer
                class Killer implements Runnable {
                    Engineer victim;
                    boolean abortFlag;
                    boolean functionFlag;
                    Killer (Engineer v, boolean a, boolean f) {
                        victim = v;
                        abortFlag = a;
                        functionFlag = f;
                    }
                    @Override
                    public void run() {
                        kill(victim, abortFlag, functionFlag, victim);
                    }
                }
                final Runnable killer = new Killer(engineer, abort, functionFlag);
                synchronized (killer) {
                    Thread hit = jmri.util.ThreadingUtil.newThread(killer,
                            getDisplayName()+" Killer");
                    hit.start();
                }
            }
        }
    }

    private void kill(Engineer eng, boolean a, boolean f, Thread monitor) {
        long time = 0;
        while (!eng.getState().equals(Thread.State.TERMINATED) && time < 100) {
            try {
                eng.stopRun(a, f); // releases throttle
                monitor.join(10);
            } catch (InterruptedException ex) {
                log.info("victim.join() interrupted. warrant {}", getDisplayName());
            }
            time += 10;
        }
        log.debug("{}: engineer state {} after {}ms", getDisplayName(), eng.getState().toString(), time);
    }

    public void stopWarrant(boolean abort, boolean turnOffFunctions) {
        _delayStart = false;
        clearWaitFlags(true);
        if (_student != null) {
            _student.dispose(); // releases throttle
            _student = null;
        }
        _curSignalAspect = null;
        cancelDelayRamp(true);
        // insulate possible non-GUI thread making a block state change
        ThreadingUtil.runOnGUI(()-> deAllocate());

        if (_engineer != null) {
            if (!_engineer.getState().equals(Thread.State.TERMINATED)) {
                killEngineer(_engineer, abort, turnOffFunctions);
            }
            _engineer = null;
        }

        int oldMode = _runMode;
        int newMode;
        if (abort) {
            newMode = MODE_ABORT;
        } else {
            newMode = MODE_NONE;
        }
        if (turnOffFunctions && _idxCurrentOrder == _orders.size()-1) { // run was complete to end
            _speedUtil.stopRun(true);   // write speed profile measurements
            if (_addTracker) {
                startTracker();
                _runMode = MODE_MANUAL;
            }
        }
        _addTracker = false;
        if (_trace || log.isDebugEnabled()) {
            if (abort) {
                log.info("{} block {}", Bundle.getMessage("warrantAbort", getTrainName(), getDisplayName()), 
                        getBlockAt(_idxCurrentOrder).getDisplayName());
            } else {
                log.info("{} : Warrant Complete", Bundle.getMessage("warrantComplete",
                        getTrainName(), getDisplayName(), getBlockAt(_idxCurrentOrder).getDisplayName()));
            }
        }
        fireRunStatus("runMode", oldMode, newMode);
        _runMode = MODE_NONE;
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
            log.debug("{}: setRunMode({}) ({}) called with _runMode= {}.",
                  getDisplayName(), mode, MODES[mode], MODES[_runMode]);
        }
        _message = null;
        if (_runMode != MODE_NONE) {
            _message = getRunModeMessage();
            log.error("{} called setRunMode when mode= {}. {}", getDisplayName(), MODES[_runMode],  _message);
            return _message;
        }
        _delayStart = false;
        _lost = false;
        _overrun = false;
        _overBlkOccupied = true;   // safety to assume somebody else is ahead
        clearWaitFlags(true);
        if (address != null) {
            _speedUtil.setDccAddress(address);
        }
        _message = setPathAt(0);
        if (_message != null) {
            return _message;
        }

        if (mode == MODE_LEARN) {
            // Cannot record if block 0 is not occupied or not dark. If dark, user is responsible for occupation
            if (student == null) {
                _message = Bundle.getMessage("noLearnThrottle", getDisplayName());
                log.error("{} called setRunMode for mode= {}. {}", getDisplayName(), MODES[mode],  _message);
                return _message;
            }
            synchronized (this) {
                _student = student;
            }
            // set mode before notifyThrottleFound is called
            _runMode = mode;
        } else if (mode == MODE_RUN) {
            if (commands != null && commands.size() > 1) {
                _commands = commands;
            }
            // set mode before setStoppingBlock and callback to notifyThrottleFound are called
            _idxCurrentOrder = 0;
            _runMode = mode;
            OBlock b = getBlockAt(0);
            if (b.isDark()) {
                _haltStart = true;
            } else if (!b.isOccupied()) {
                // continuing with no occupation of starting block
                setStoppingBlock(0);
                _delayStart = true;
            }
        } else if (mode == MODE_MANUAL) {
            if (commands != null) {
                _commands = commands;
            }
        } else {
            deAllocate();
            return _message;
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
            fireRunStatus("throttleFail", null, msg);
            abortWarrant(msg);
            return msg;
        }
        return null;
    }

    @Override
    public void notifyThrottleFound(DccThrottle throttle) {
        if (throttle == null) {
            _message = Bundle.getMessage("noThrottle", getDisplayName());
            fireRunStatus("throttleFail", null, _message);
            abortWarrant(_message);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("{}: notifyThrottleFound for address= {}, class= {},",
                  getDisplayName(), throttle.getLocoAddress(), throttle.getClass().getName());
        }
        _speedUtil.setThrottle(throttle);
        if (_trace) {
            log.info("{} : Warrant Start", Bundle.getMessage("warrantStart", getTrainName(), getDisplayName(),
                        getBlockAt(0).getDisplayName()));
        }
        startupWarrant();
        runWarrant(throttle);
    } //end notifyThrottleFound

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        _message = Bundle.getMessage("noThrottle",
                (reason + " " + (address != null ? address.getNumber() : getDisplayName())));
        fireRunStatus("throttleFail", null, reason);
        abortWarrant(_message);
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
                log.error("{} releaseThrottle. {} on thread {}",
                        getDisplayName(), Bundle.getMessage("noThrottle", throttle.getLocoAddress()),
                        Thread.currentThread().getName());
            }
        }
    }

    protected void abortWarrant(String msg) {
        log.error("Abort warrant \"{}\" - {} ", getDisplayName(), msg);
        _engineer.stopRun(true, true);
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
                case RETRY_FWD:
                case RETRY_BKWD:
                case RAMP_HALT:
                case SPEED_UP:
                    break;
                case STOP:
                case ABORT:
                    if (_runMode == Warrant.MODE_LEARN) {
                        // let WarrantFrame do the abort. (WarrantFrame listens for "abortLearn")
                        fireRunStatus("abortLearn", -MODE_LEARN, _idxCurrentOrder);
                    } else {
                        stopWarrant(true, true);
                    }
                    break;
                case DEBUG:
                    ret = debugInfo();
                    break;
                default:
            }
            return ret;
        }
        int runState = _engineer.getRunState();
        if (runState == Warrant.HALT) {
            if (_waitForSignal || _waitForBlock || _waitForWarrant) {
                runState = WAIT_FOR_CLEAR;
            }
        }
        if (_trace || log.isDebugEnabled()) {
            log.info("{} : Control Change", Bundle.getMessage("controlChange",
                    getTrainName(), Bundle.getMessage(Warrant.CNTRL_CMDS[idx]),
                    getCurrentBlockName()));
        }
        synchronized (this) {
            switch (idx) {
                case RAMP_HALT:
                    rampSpeedTo(Warrant.Stop, -1);  // ramp down
                    _engineer.setHalt(true);
                    ret = true;
                    break;
                case RESUME:
                    OBlock block = getBlockAt(_idxCurrentOrder);
                    String msg = null;
                    if (_waitForSignal || _waitForBlock || _waitForWarrant) {
                        msg = makeWaitMessage(block.getDisplayName(), _idxCurrentOrder);
                    } else {
                        String train = (String)block.getValue();
                        if (train == null) {
                            train = Bundle.getMessage("unknownTrain");
                        }
                        if (block.isOccupied() && !_trainName.equals(train)) {
                            msg = Bundle.getMessage("blockInUse", train, block.getDisplayName());
                        }
                    }
                    if (msg != null) {
                        ret = askResumeQuestion(block, msg);
                        if (ret) {
                            ret = reStartTrain();
                        }
                    } else {
                        ret = reStartTrain();
                    }
                    if (!ret) {
                        _engineer.setHalt(true);
                    }
                    break;
                case SPEED_UP:
                    // user wants to increase throttle of stalled train slowly
                    if (checkBlockForRunning(_idxCurrentOrder)) {
                        if ((_waitForSignal || _waitForBlock || _waitForWarrant) ||
                                (runState != RUNNING && runState != SPEED_RESTRICTED)) {
                            block = getBlockAt(_idxCurrentOrder); 
                            msg = makeWaitMessage(block.getDisplayName(), _idxCurrentOrder);
                            ret = askResumeQuestion(block, msg);
                            if (ret) {
                                ret = bumpSpeed();
                            }
                        } else {
                            ret = bumpSpeed();
                        }
                    }
                    break;
                case RETRY_FWD: // Force move into next block
                    if (checkBlockForRunning(_idxCurrentOrder + 1)) {
                        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder + 1);
                        block = bo.getBlock(); 
                        if ((_waitForSignal || _waitForBlock || _waitForWarrant) ||
                                (runState != RUNNING && runState != SPEED_RESTRICTED)) {
                            msg = makeWaitMessage(block.getDisplayName(), _idxCurrentOrder);
                            ret = askResumeQuestion(block, msg);
                            if (ret) {
                                ret = moveToBlock(bo, _idxCurrentOrder + 1);
                            }
                        } else {
                            ret = moveToBlock(bo, _idxCurrentOrder + 1);
                        }
                    }
                    break;
                case RETRY_BKWD: // Force move into previous block - Not enabled.
                    if (checkBlockForRunning(_idxCurrentOrder - 1)) {
                        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder - 1);
                        block = bo.getBlock(); 
                        if ((_waitForSignal || _waitForBlock || _waitForWarrant) ||
                                (runState != RUNNING && runState != SPEED_RESTRICTED)) {
                            msg = makeWaitMessage(block.getDisplayName(), _idxCurrentOrder);
                            ret = askResumeQuestion(block, msg);
                            if (ret) {
                                ret = moveToBlock(bo, _idxCurrentOrder - 1);
                            }
                        } else {
                            ret = moveToBlock(bo, _idxCurrentOrder - 1);
                        }
                    }
                    break;
                case ABORT:
                    stopWarrant(true, true);
                    ret = true;
                    break;
                case HALT:
                case STOP:
                    setSpeedToType(Stop); // sets _halt
                    _engineer.setHalt(true);
                    ret = true;
                    break;
                case ESTOP:
                    setSpeedToType(EStop); // E-stop & halt
                    _engineer.setHalt(true);
                    ret = true;
                    break;
                case DEBUG:
                    ret = debugInfo();
                    break;
                default:
            }
        }
        if (ret) {
            fireRunStatus("controlChange", runState, idx);
        } else {
            if (_trace || log.isDebugEnabled()) {
                log.info("{} : Control Failed", Bundle.getMessage("controlFailed",
                        getTrainName(), _message,
                        Bundle.getMessage(Warrant.CNTRL_CMDS[idx])));
            }
            fireRunStatus("controlFailed", _message, idx);
        }
        return ret;
    }

    private boolean askResumeQuestion(OBlock block, String reason) {
        String msg = Bundle.getMessage("ResumeQuestion", reason);
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
        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder);
        OBlock block = bo.getBlock();
        if (!block.isOccupied() && !block.isDark()) {
            _message = Bundle.getMessage("blockUnoccupied", block.getDisplayName());
            return false;
        }
        // OK, will do it as it long as you own it, and you are where you think you are there.
        _engineer.setHalt(false);
        clearWaitFlags(false);
        _overrun = true;    // allows doRestoreRunning to run at an OCCUPY state
        return restoreRunning(_engineer.getSpeedType(false));
    }

    // returns true if block is owned and occupied by this warrant
    private boolean checkBlockForRunning(int idxBlockOrder) {
        BlockOrder bo = getBlockOrderAt(idxBlockOrder);
        if (bo == null) {
            _message = Bundle.getMessage("BlockNotInRoute", "?");
            return false;
        }
        OBlock block = bo.getBlock();
        if ((block.getState() & OBlock.OCCUPIED) == 0) {
            _message = Bundle.getMessage("blockUnoccupied", block.getDisplayName());
            return false;
        }
        return true;
    }
 
    // User increases speed
    private boolean bumpSpeed() {
        // OK, will do as it long as you own it, and you are where you think you are.
        _engineer.setHalt(false);
        clearWaitFlags(false);
        float speedSetting = _engineer.getSpeedSetting();
        if (speedSetting < 0) { // may have done E-Stop
            speedSetting = 0.0f;
        }
        float bumpSpeed = Math.max(WarrantPreferences.getDefault().getSpeedAssistance(), _speedUtil.getRampThrottleIncrement());
        _engineer.setSpeed(speedSetting + bumpSpeed);
        return true;
    }

    private boolean moveToBlock(BlockOrder bo, int idx) {
        _idxCurrentOrder = idx;
        _message = setPathAt(idx);    // no checks. Force path set and allocation
        if (_message != null) {
            return false;
        }
        OBlock block = bo.getBlock();
        if (block.equals(_stoppingBlock)) {
            clearStoppingBlock();
            _engineer.setHalt(false);
        }
        goingActive(block);
        return true;
    }

    protected boolean debugInfo() {
        if ( !log.isInfoEnabled() ) {
            return true;
        }
        StringBuilder info = new StringBuilder("\""); info.append(getDisplayName());
        info.append("\" Train \""); info.append(getTrainName()); info.append("\" - Current Block \"");
        info.append(getBlockAt(_idxCurrentOrder).getDisplayName());
        info.append("\" BlockOrder idx= "); info.append(_idxCurrentOrder);
        info.append("\n\tWait flags: _waitForSignal= "); info.append(_waitForSignal);
        info.append(", _waitForBlock= "); info.append(_waitForBlock);
        info.append(", _waitForWarrant= "); info.append(_waitForWarrant);
        info.append("\n\tStatus flags: _overrun= "); info.append(_overrun); info.append(", _overBlkOccupied= "); 
        info.append(_overBlkOccupied);info.append(", _lost= "); info.append(_lost);
        if (_protectSignal != null) {
            info.append("\n\tWait for Signal \"");info.append(_protectSignal.getDisplayName());info.append("\" protects block ");
            info.append(getBlockAt(_idxProtectSignal).getDisplayName()); info.append("\" from approch block \"");
            info.append(getBlockAt(_idxProtectSignal - 1).getDisplayName()); info.append("\". Shows aspect \"");
            info.append(getSignalSpeedType(_protectSignal)); info.append("\".");
        } else {
            info.append("\n\tNo signals ahead with speed restrictions");
        }
        if(_stoppingBlock != null) {
            if (_waitForWarrant) {
                info.append("\n\tWait for Warrant \"");
                Warrant w = getBlockingWarrant(); info.append((w != null?w.getDisplayName():"Unknown"));
                info.append("\" owns block \"");info.append(_stoppingBlock.getDisplayName()); info.append("\"");
            } else {
                Object what = _stoppingBlock.getValue();
                String who;
                if (what != null) {
                    who = what.toString();
                } else {
                    who = "Unknown Train";
                }
                info.append("\n\tWait for \""); info.append(who); info.append("\" occupying Block \""); 
                info.append(_stoppingBlock.getDisplayName()); info.append("\"");
            }
        } else {
            info.append("\n\tNo occupied blocks ahead");
        }
        if (_message != null) {
            info.append("\n\tLast message = ");info.append(_message);
        } else {
            info.append("\n\tNo messages.");
        }

        if (_engineer != null) {
            info.append(_engineer.debugInfo());
         } else {
            info.append("No engineer.");
        }
        log.info("\n Warrant: {}", info.toString());
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
            if (_engineer != null) {    // should not happen
                killEngineer(_engineer, true, true);
            }
            _engineer = new Engineer(this, throttle);

            if (!_noRamp) { // _noRamp makes immediate speed changes
                _speedUtil.getBlockSpeedTimes(_commands, this);   // initialize SpeedUtil
            }
            if (_tempRunBlind) {
                _engineer.setRunOnET(true);
            }
            if (_delayStart || _haltStart) {
                _engineer.setHalt(true);    // throttle already at 0
                // user must explicitly start train (resume) in a dark block
                fireRunStatus("ReadyToRun", -1, 0);   // ready to start msg
            }
            _delayStart = false;
            _engineer.start();

            int runState = _engineer.getRunState();
            if (runState != HALT && runState != RAMP_HALT) {
                setMovement();
            }
        }
    }

    private String setPathAt(int idx) {
        BlockOrder bo = _orders.get(idx);
        OBlock b = bo.getBlock();
        String msg = b.allocate(this);
        if (msg == null) {
            msg = bo.setPath(this); // No TrainOrder checks
            b.showAllocated(this, bo.getPathName());
        }
        return msg;
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
     * @param show _message for use ONLY to display a temporary route) continues to
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
        _message = null;

        int idxSpeedChange = 0;  // idxBlockOrder where speed changes
        do {
            TrainOrder to = getBlockOrderAt(idxSpeedChange).allocatePaths(this, true);
            switch (to._cause) {
                case NONE:
                    break;
               case WARRANT:
                   _waitForWarrant = true;
                   if (_message == null) {
                       _message = to._message;
                   }
                   if (!show && to._idxContrlBlock == 0) {
                       return _message;
                   }
                   break;
                case OCCUPY:
                    _waitForBlock = true;
                    if (_message == null) {
                        _message = to._message;
                    }
                    break;
                case SIGNAL:
                    if (Stop.equals(to._speedType)) {
                        _waitForSignal = true;
                        if (_message == null) {
                            _message = to._message;
                        }
                    }
                    break;
                default:
                    log.error("{}: allocateRoute at block \"{}\" setPath returns: {}", 
                            getDisplayName(), getBlockAt(idxSpeedChange).getDisplayName(), to.toString());
                    if (_message == null) {
                        _message = to._message;
                    }
            }
            if (!show) {
                if (_message != null || (_shareRoute && idxSpeedChange > 1)) {
                    break;
                }
            }
            idxSpeedChange++;
        } while (idxSpeedChange < _orders.size());

        if (log.isDebugEnabled()) {
            log.debug("{}: allocateRoute() _shareRoute= {} show= {}. Break at {} of {}. msg= {}",
                getDisplayName(), _shareRoute, show, idxSpeedChange, _orders.size(), _message);
        }
        _allocated = true; // start block allocated
        if (_message == null) {
            _totalAllocated = true;
            if (show && _shareRoute) {
                _message = Bundle.getMessage("sharedRoute");
            }
        }
        if (show) {
            return _message;
        }
        return null;
    }

    /**
     * Deallocates blocks from the current BlockOrder list
     */
    public void deAllocate() {
        _allocated = false;
        _totalAllocated = false;
        _routeSet = false;
        for (int i = 0; i < _orders.size(); i++) {
            deAllocateBlock(_orders.get(i).getBlock());
        }
    }

    private boolean deAllocateBlock(OBlock block) {
        if (block.isAllocatedTo(this)) {
            block.deAllocate(this);
            if (block.equals(_stoppingBlock)){
                doStoppingBlockClear();
            }
            return true;
        }
        return false;
    }

    /**
     * Convenience routine to use from Python to start a warrant.
     *
     * @param mode run mode
     */
    public void runWarrant(int mode) {
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
        if (_shareRoute) { // full route of a shared warrant may be displayed
            deAllocate();   // clear route to allow sharing with another warrant
        }

        // allocateRoute may set _message for status info, but return null msg
        _message = allocateRoute(show, orders);
        if (_message != null) {
            log.debug("{}: setRoute: {}", getDisplayName(), _message);
            return _message;
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
        if (block.isDark() || _tempRunBlind) {
            msg = "BlockDark";
        } else if (!block.isOccupied()) {
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
            if (block.isOccupied() && !startBlock.equals(block)) {
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
                int newState = ((Number) evt.getNewValue()).intValue();
                if ((newState & OBlock.OCCUPIED) != 0) {
                    if (_delayStart) { // wait for arrival of train to begin the run
                        // train arrived at starting block or last known block of lost train is found
                        clearStoppingBlock();
                        OBlock block = getBlockAt(_idxCurrentOrder);
                        if (_runMode == MODE_RUN && _engineer == null) {
                            acquireThrottle();
                        } else if (_runMode == MODE_MANUAL) {
                            fireRunStatus("ReadyToRun", -1, 0);   // ready to start msg
                            _delayStart = false;
                        }
                        block._entryTime = System.currentTimeMillis();
                        block.setValue(_trainName);
                        block.setState(block.getState() | OBlock.RUNNING);
                    } else if ((((Number) evt.getNewValue()).intValue() & OBlock.ALLOCATED) == 0) {
                        // blocking warrant has released allocation but train still occupies the block
                        clearStoppingBlock();
                        log.debug("\"{}\" cleared its wait. but block \"{}\" remains occupied", getDisplayName(), 
                                (((Block)evt.getSource()).getDisplayName()));
                    }
                } else if ((((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != 0) {
                    //  blocking occupation has left the stopping block
                    clearStoppingBlock();
                }
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
        if (_idxProtectSignal < _idxCurrentOrder) { // signal is behind train. ignore
            changeSignalListener(null, _idxCurrentOrder);  // remove signal
            return;
        }
        // Signals may change after entry and while the train in the block.
        // Normally these changes are ignored.
        // However for the case of an overrun stop aspect, the train is waiting.
        if (_idxProtectSignal == _idxCurrentOrder && !_waitForSignal) { // not waiting
            changeSignalListener(null, _idxCurrentOrder);  // remove signal
            return; // normal case
        }// else Train previously overran stop aspect. Continue and respond to signal.

        String speedType = getSignalSpeedType(_protectSignal);
        String curSpeedType;
        if (_waitForSignal) {
            curSpeedType = Stop;
        } else {
            curSpeedType = _engineer.getSpeedType(true);    // current or pending ramp completion
        }
        if (log.isDebugEnabled()) {
            log.debug("{}: Signal \"{}\" changed to aspect \"{}\" {} blocks ahead. curSpeedType= {}",
                    getDisplayName(), _protectSignal.getDisplayName(), speedType, _idxProtectSignal-_idxCurrentOrder, curSpeedType);
        }

        if (curSpeedType.equals(speedType)) {
            return;
        }
        if (_idxProtectSignal > _idxCurrentOrder) {
            if (_speedUtil.secondGreaterThanFirst(speedType, curSpeedType)) {
                // change to slower speed. Check if speed change should occur now
                float availDist = getAvailableDistance(_idxProtectSignal);
                float changeDist = getEntranceDistance(_idxProtectSignal, speedType);
                if (changeDist > availDist) {
                    // Not enough room in blocks ahead. start ramp in current block
                    availDist += getAvailableDistanceAt(_idxCurrentOrder);
                    if (speedType.equals(Warrant.Stop)) {
                        _waitForSignal = true;
                    }
                    int cmdStartIdx = _engineer.getCurrentCommandIndex(); // blkSpeedInfo.getFirstIndex();
                    if (!doDelayRamp(availDist, changeDist, _idxProtectSignal, speedType, cmdStartIdx)) {
                        log.info("No room for train {} to ramp to \"{}\" from \"{}\" for signal \"{}\"!. availDist={}, changeDist={} on warrant {}",
                                getTrainName(), speedType, curSpeedType, _protectSignal.getDisplayName(),
                                availDist,  changeDist, getDisplayName());
                    }   // otherwise will do ramp when entering a block ahead
                }
                return;
            }
        }
        if (!speedType.equals(Warrant.Stop)) {  // a moving aspect clears a signal wait
            if (_waitForSignal) {
                // signal protecting next block just released its hold
                _curSignalAspect = speedType;
                _waitForSignal = false;
                if (_trace || log.isDebugEnabled()) {
                    log.info("{} : Signal Cleared", Bundle.getMessage("SignalCleared", _protectSignal.getDisplayName(), speedType, _trainName));
                }
                /* redundant, restoreRunning takes care of this
                 * 
                OBlock block = getBlockAt(_idxProtectSignal);
                // train is stopped. If occupied by a rogue don't move.
                if (block.isOccupied() && !_overrun) {
                    _waitForBlock = true;
                    setStoppingBlock(_idxProtectSignal);
                }
                Warrant w = block.getWarrant();
                // or owned by another warrant, don't move.
                if (w != null && !this.equals(w)) {
                    _waitForWarrant = true;
                    setStoppingBlock(_idxProtectSignal);
                }*/
                ThreadingUtil.runOnGUIDelayed(() -> {
                    restoreRunning(speedType);
                }, 2000);
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
    private float getEntranceDistance(int idxBlockOrder, String speedType) {
        float speedSetting = _engineer.getSpeedSetting();       // current speed
        // Estimate speed at start of ramp
        float enterSpeed;   // speed at start of ramp
        if (speedSetting > 0.1f && _idxCurrentOrder == idxBlockOrder - 1) {
            // if in the block immediately before the entrance block, use current speed
            enterSpeed = speedSetting;
        } else { // else use entrance speed of previous block
            String currentSpeedType = _engineer.getSpeedType(true); // current or pending speed type
            float scriptSpeed = _speedUtil.getBlockSpeedInfo(idxBlockOrder - 1).getEntranceSpeed();
            enterSpeed = _speedUtil.modifySpeed(scriptSpeed, currentSpeedType);
        }
        float scriptSpeed = _speedUtil.getBlockSpeedInfo(idxBlockOrder).getEntranceSpeed();
        float endSpeed = _speedUtil.modifySpeed(scriptSpeed, speedType);
        // compare distance needed for script throttle at entrance to entrance speed,
        // to the distance needed for current throttle to entrance speed.
        float enterLen = _speedUtil.getRampLengthForEntry(enterSpeed, endSpeed);
        // add buffers for signal and safety clearance
        float bufDist = getEntranceBufferDist(idxBlockOrder);
        return enterLen + bufDist;
    }

    private void doStoppingBlockClear() {
        if (_stoppingBlock == null) {
            return;
        }
        _stoppingBlock.removePropertyChangeListener(this);
        _stoppingBlock = null;
        _idxStoppingBlock = -1;
    }

    /**
     * Called when a rogue or warranted train has left a block.
     * Also called from propertyChange() to allow warrant to acquire a throttle
     * and launch an engineer. Also called by retry control command to help user
     * work out of an error condition.
     */
    synchronized private void clearStoppingBlock() {
        if (_stoppingBlock == null) {
            return;
        }
        String name = _stoppingBlock.getDisplayName();
        doStoppingBlockClear();

        if (_delayStart) {
            return;    // don't start. Let user resume start
        }
        if (_trace || log.isDebugEnabled()) {
            String reason;
            if (_waitForBlock) {
                reason = Bundle.getMessage("Occupancy");
            } else {
                reason = Bundle.getMessage("Warrant");
            }
            log.info("{} : Stop Block Cleared", Bundle.getMessage("StopBlockCleared",
                    getTrainName(), getDisplayName(), reason, name));
        }
        cancelDelayRamp(true);
        int time = 1000;
        if (_waitForBlock) {
            _waitForBlock = false;
            time = 4000;
        }
        if (_waitForWarrant) {
            _waitForWarrant = false;
            time = 3000;
        }
        String speedType;
        if (_curSignalAspect != null) {
            speedType = _curSignalAspect;
        } else {
            speedType = _engineer.getSpeedType(false);
        }
        ThreadingUtil.runOnGUIDelayed(() -> {
            restoreRunning(speedType);
        }, time);
    }

    private String okToRun() {
        boolean cannot = false;
        StringBuffer sb = new StringBuffer();
        if (_waitForSignal) {
            sb.append(Bundle.getMessage("Signal"));
            cannot = true;
        }
        if (_waitForWarrant) {
            if (cannot) {
                sb.append(", ");
            } else {
                cannot = true;
            }
            Warrant w = getBlockingWarrant();
           if (w != null) {
                sb.append(Bundle.getMessage("WarrantWait",  w.getDisplayName()));
            } else {
                sb.append(Bundle.getMessage("WarrantWait", "Unknown"));
            }
        }
        if (_waitForBlock) {
            if (cannot) {
                sb.append(", ");
            } else {
                cannot = true;
            }
            sb.append(Bundle.getMessage("Occupancy"));
        }

        if (_engineer != null) {
            int runState = _engineer.getRunState();
            if (runState == HALT || runState == RAMP_HALT) {
                if (cannot) {
                    sb.append(", ");
                } else {
                    cannot = true;
                }
                sb.append(Bundle.getMessage("userHalt"));
            }
        }
        if (cannot) {
            return sb.toString();
        }
        return null;
    }

    /**
     * A layout condition that has restricted or stopped a train has been cleared.
     * i.e. Signal aspect, rogue occupied block, contesting warrant or user halt.
     * This may or may not be all the conditions restricting speed.
     * @return true if automatic restart is done
     */
    private boolean restoreRunning(String speedType) {
        _message = okToRun();
        if (_message == null) {
            BlockOrder bo = getBlockOrderAt(_idxCurrentOrder); 
            TrainOrder to = bo.allocatePaths(this, true);
            OBlock block = bo.getBlock();
            if (log.isDebugEnabled()) {
                log.debug("OK to run {}: {}", getDisplayName(), to.toString());   // so try I'll this
            }
            switch (to._cause) {    // to._cause - precedence of checks is WARRANT, OCCUPY, SIGNAL
                case NONE:
                    doRestoreRunning(block, speedType);
                    return true;
               case WARRANT:
                   _waitForWarrant = true;
                   _message = to._message;
                   setStoppingBlock(to._idxContrlBlock);
                   break;
                case OCCUPY:
                    if (_overrun) {
                        _message = setPathAt(_idxCurrentOrder);
                        if (_message == null) {
                            doRestoreRunning(block, speedType);
                            return true;
                        }
                        break;
                    }
                    _waitForBlock = true;
                    _message = to._message;
                    setStoppingBlock(to._idxContrlBlock);
                    break;
                case SIGNAL:
                    if (to._idxContrlBlock == _idxCurrentOrder) {
                        doRestoreRunning(block, speedType);
                        return true;
                    }
                    if (Stop.equals(to._speedType)) {
                        _waitForSignal = true;
                        _message = to._message;
                        setProtectingSignal(to._idxContrlBlock);
                        break;
                    }
                    speedType = to._speedType;
                    doRestoreRunning(block, speedType);
                    return true;
                default:
                    log.error("restoreRunning TrainOrder {}", to.toString());
                    _message = to._message;
            }
        }
        String blockName = getBlockAt(_idxCurrentOrder).getDisplayName();
        if (_trace || log.isDebugEnabled()) {
            log.info("{} : train Waiting", Bundle.getMessage("trainWaiting",
                    getTrainName(), _message, blockName));
        }
        fireRunStatus("cannotRun", blockName, _message);
        return false;
    }

    private void doRestoreRunning(OBlock block, String speedType) {
        _overrun = false;
        _overBlkOccupied = true;   // for safety, assume somebody else is ahead
        _curSignalAspect = null;
        _engineer.clearWaitForSync(block);
        if (log.isDebugEnabled()) {
            log.debug("{}: restoreRunning(): rampSpeedTo to \"{}\"",
                    getDisplayName(), speedType);
        }
        rampSpeedTo(speedType, -1);
        // continue, there may be blocks ahead that need a speed decrease to begin in this block
        if (_idxCurrentOrder < _orders.size() - 1) {
            lookAheadforSpeedChange(speedType, speedType);
        } // else at last block, forget about speed changes
    }

    /**
     * Stopping block only used in MODE_RUN _stoppingBlock is an occupied OBlock
     * preventing the train from continuing the route OR another warrant
     * is preventing this warrant from allocating the block to continue.
     * <p>
     */
    private void setStoppingBlock(int idxBlock) {
        OBlock block = getBlockAt(idxBlock);
        if (block == null) {
            return;
        }
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

            int idxStop = getIndexOfBlockAfter(_stoppingBlock, _idxCurrentOrder);
            if ((idxBlock < idxStop) || idxStop < 0) {
                prevBlk.removePropertyChangeListener(this);
            } else {
                if (idxStop < _idxCurrentOrder) {
                    log.error("{}: _stoppingBlock \"{}\" index {} < _idxCurrentOrder {}",
                            getDisplayName(), _stoppingBlock.getDisplayName(), idxStop, _idxCurrentOrder);
                }
                return;
            }
        }
        _stoppingBlock = block;
        _idxStoppingBlock = idxBlock;
        _stoppingBlock.addPropertyChangeListener(this);
        if ((_trace || log.isDebugEnabled()) && (_waitForBlock || _waitForWarrant)) {
            String reason;
            if (_waitForWarrant) {
                reason = Bundle.getMessage("Warrant");
            } else {
                reason = Bundle.getMessage("Occupancy");                
            }
            log.info("{} : StopBlock Set", Bundle.getMessage("StopBlockSet", _stoppingBlock.getDisplayName(), getTrainName(), reason));
        }
    }

    /**
     * set signal listening for aspect change for block at index.
     * return true if signal is set.
     */
    private boolean setProtectingSignal(int idx) {
        if (_idxProtectSignal == idx) {
            return true;
        }
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

        return changeSignalListener(signal, idx);
    }

    /**
     * if current listening signal is not at signalIndex, remove listener and
     * set new listening signal
     */
    private boolean changeSignalListener(NamedBean  signal,  int signalIndex) {
        if (signalIndex == _idxProtectSignal) {
            return true;
        }
        StringBuilder sb = new StringBuilder(getDisplayName());
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
            if (_trace || log.isDebugEnabled()) {
                log.info("{} : Protect Signal Set : {}", Bundle.getMessage("ProtectSignalSet", getTrainName(),
                        _protectSignal.getDisplayName(), getBlockAt(_idxProtectSignal).getDisplayName()), sb);
            }
            ret = true;
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
                log.error("{} invoked on wrong thread", getDisplayName(), new Exception("traceback"));
                stopWarrant(true, true);
                return;
            }
        }

        if (_runMode == MODE_NONE) {
            return;
        }
        int activeIdx = getIndexOfBlockAfter(block, _idxCurrentOrder);
        if (log.isDebugEnabled()) {
            log.debug("{}: **Block \"{}\" goingActive. activeIdx= {}, _idxCurrentOrder= {}.",
                    getDisplayName(), block.getDisplayName(), activeIdx, _idxCurrentOrder);
        }
        Warrant w = block.getWarrant();
        if (w == null || !this.equals(w)) {
            if (log.isDebugEnabled()) {
                log.debug("{}: **Block \"{}\" owned by {}!",
                        getDisplayName(), block.getDisplayName(), (w==null?"NO One":w.getDisplayName()));
            }
            return;
        }
        if (_lost && !getBlockAt(_idxCurrentOrder).isOccupied()) {
            _idxCurrentOrder = activeIdx;
            if (log.isDebugEnabled()) {
                log.debug("{}: Lost train {} found at block \"{}\".",
                        getDisplayName(), getTrainName(), block.getDisplayName());
            }
            _lost = false;
            if (_engineer != null) {
                _engineer.rampSpeedTo(_engineer.getSpeedType(true), - 1);
                setMovement();
           }
            return;
        }
        if (activeIdx <= 0) {
            return;
        }
        if (activeIdx == _idxCurrentOrder) {
            // unusual occurrence.  dirty track? sensor glitch?
            if (_trace || log.isDebugEnabled()) {
                log.info("{} : Regain Detection", Bundle.getMessage("RegainDetection", getTrainName(), block.getDisplayName()));
            }
        } else if (activeIdx == _idxCurrentOrder + 1) {
            if (_delayStart) {
                log.warn("{}: Rogue entered Block \"{}\" ahead of {}.",
                        getDisplayName(), block.getDisplayName(), getTrainName());
                _message = Bundle.getMessage("BlockRougeOccupied", block.getDisplayName());
                return;
            }
            // Since we are moving at speed we assume it is our train that entered the block
            // continue on.
            _idxLastOrder = _idxCurrentOrder;
            _idxCurrentOrder = activeIdx;
        } else if (activeIdx > _idxCurrentOrder + 1) {
            // if previous blocks are dark, this could be for our train
            // check from current (last known) block to this just activated block
            for (int idx = _idxCurrentOrder + 1; idx < activeIdx; idx++) {
                OBlock preBlock = getBlockAt(idx);
                if (!preBlock.isDark()) {
                    // not dark, therefore not our train
                    if (log.isDebugEnabled()) {
                        OBlock curBlock = getBlockAt(_idxCurrentOrder);
                        log.debug("Rogue train entered block \"{}\" ahead of train {} currently in block \"{}\"!",
                                block.getDisplayName(), _trainName, curBlock.getDisplayName());
                    }
                    return;
                }
                // we assume this is our train entering block
                _idxCurrentOrder = activeIdx;
            }
            // previous blocks were checked as UNDETECTED above
            // Indicate the previous dark block was entered
            OBlock prevBlock = getBlockAt(activeIdx - 1);
            prevBlock._entryTime = System.currentTimeMillis() - 1000; // arbitrarily say
            prevBlock.setValue(_trainName);
            prevBlock.setState(prevBlock.getState() | OBlock.RUNNING);
            if (log.isDebugEnabled()) {
                log.debug("{}: Train moving from UNDETECTED block \"{}\" now entering block\"{}\"",
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
        if (_trace) {
            log.info("{} : TrackerBlock Enter", Bundle.getMessage("TrackerBlockEnter", getTrainName(),  block.getDisplayName()));
        }
        fireRunStatus("blockChange", getBlockAt(activeIdx - 1), block);
        if (_runMode == MODE_LEARN) {
            return;
        }
        // _idxCurrentOrder has been incremented. Warranted train has entered this block.
        // Do signals, speed etc.
        if (_idxCurrentOrder < _orders.size() - 1) {
            if (_engineer != null) {
                BlockOrder bo = _orders.get(_idxCurrentOrder + 1);
                if (bo.getBlock().isDark()) {
                    // can't detect next block, use ET
                    _engineer.setRunOnET(true);
                } else if (!_tempRunBlind) {
                    _engineer.setRunOnET(false);
                }
            }
        }
        if (log.isTraceEnabled()) {
            log.debug("{}: end of goingActive. leaving \"{}\" entered \"{}\"",
                    getDisplayName(), getBlockAt(activeIdx - 1).getDisplayName(), block.getDisplayName());
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
                length = bo.getPathLength() / 2;
            } else {
                for (int i = _idxLastOrder; i < _idxCurrentOrder; i++) {
                    // add length of possible dark block
                    BlockOrder bo = getBlockOrderAt(i);
                    length += bo.getPathLength();
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
                log.error("{} invoked on wrong thread", getDisplayName(), new Exception("traceback"));
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
        if (idx > _idxCurrentOrder) {
            return;
        }
        releaseBlock(block, idx);
        block.setValue(null);
        if (idx == _idxCurrentOrder) {
            // Train not visible if current block goes inactive. This is OK if the next block is Dark.
            if (_idxCurrentOrder + 1 < _orders.size()) {
                OBlock nextBlock = getBlockAt(_idxCurrentOrder + 1);
                if (nextBlock.isDark()) {
                    goingActive(nextBlock); // fake occupancy for dark block
                    return;
                }
                Warrant w = nextBlock.getWarrant();
                if (w != null && !w.equals(this)) {
                    _message = nextBlock.getDisplayName() + " owned by " + w.getDisplayName();
                }
                if (nextBlock.isOccupied()) {   // this may be an overrun
                    _overrun = true;
                    if (w == null) {   // take ownership. 
                        _message = setPathAt(_idxCurrentOrder + 1);    //  no TrainOrder checks. allocates and sets path
                        if (_message == null) {
                            _overBlkOccupied = false;  // occupier is us
                            goingActive(nextBlock);
                            return;
                        }
                    } else if (w.equals(this)) {
                        return;
                    }
                }
                log.debug("{}: Current position in block \"{}\" goingInactive. next block \"{}\" is occupied! - {}",
                        getDisplayName(), block.getDisplayName(), nextBlock.getDisplayName(), _message);
            }
            _lost = true;
            if (_engineer != null) {
                setSpeedToType(Stop);   // set 0 throttle
                setStoppingBlock(_idxCurrentOrder);
            }
            if (_idxCurrentOrder == 0) {
                setStoppingBlock(0);
            }
            if (_trace) {
                log.info("{} : Changed Route", Bundle.getMessage("ChangedRoute", _trainName, block.getDisplayName(), getDisplayName()));
            }
            fireRunStatus("blockChange", block, null);  // train is lost
        }
    } // end goingInactive

    /**
     * Deallocates all blocks prior to and including block at index idx
     * of _orders, if not needed again.
     * Comes from goingInactive, i.e. warrant has a listener on the block.
     * @param block warrant is releasing
     * @param idx index in BlockOrder list
     */
    private void releaseBlock(OBlock block, int idx) {
        /*
         * Deallocate block if train will not use the block again. Warrant
         * could loop back and re-enter blocks previously traversed. That is,
         * they will need to re-allocation of blocks ahead.
         * Previous Dark blocks also need deallocation and other trains or cars
         * dropped may have prevented previous blocks from going inactive.
         * Thus we must deallocate backward until we reach inactive detectable blocks
         * or blocks we no longer own.
         */
/*        OBlock prevBlock =null;
        if (idx > 0) {
            prevBlock = getBlockAt(idx - 1);
        }*/
        if (_shareRoute) { // shared route
            OBlock nextBlock = getBlockAt(_idxCurrentOrder + 1);
            for (int i = idx; i > -1; i--) {
                OBlock curBlock = getBlockAt(i);
                if (!curBlock.equals(nextBlock)) {
                    if (deAllocateBlock(curBlock)) {
                        curBlock.setValue(null);
                        _totalAllocated = false;
                    }
                }
                /*
                if (prevBlock != null && 
                        ((!prevBlock.isDark() && !prevBlock.isOccupied()) || !this.equals(prevBlock.getWarrant()))) {
                    break;
                }
                prevBlock = curBlock;
                */
            }
        } else {
            for (int i = idx; i > -1; i--) {
                boolean neededLater = false;
                OBlock curBlock = getBlockAt(i);
                for (int j = i + 1; j < _orders.size(); j++) {
                    if (curBlock.equals(getBlockAt(j))) {
                        neededLater = true;
                    }
                }
                if (!neededLater) {
                    if (deAllocateBlock(curBlock)) {
                        curBlock.setValue(null);
                        _totalAllocated = false;
                    }
                } else {
                    if (curBlock.isAllocatedTo(this)) {
                        // Can't deallocate, but must listen for followers
                        // who may be occupying the block
                        if (_idxCurrentOrder != idx + 1) {
                            curBlock.setValue(null);
                        }
                        if (curBlock.equals(_stoppingBlock)){
                            doStoppingBlockClear();
                        }
                    }
                }
                /*
                if (prevBlock != null &&
                        ((!prevBlock.isDark() && !prevBlock.isOccupied()) || !this.equals(prevBlock.getWarrant()))) {
                    break;
                }
                prevBlock = curBlock;
                */
            }
        }
    }

    @Override
    public void dispose() {
        if (_runMode != MODE_NONE) {
            stopWarrant(true, true);
        }
        super.dispose();
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameWarrant");
    }

    private class CommandDelay extends Thread {

        String _speedType;
        long _startTime = 0;
        long _waitTime = 0;
        float _waitSpeed;
        boolean quit = false;
        int _endBlockIdx;

        CommandDelay(String speedType, long startWait, float waitSpeed, int endBlockIdx) {
            _speedType = speedType;
            _waitTime = startWait;
            _waitSpeed = waitSpeed;
            _endBlockIdx = endBlockIdx;
            setName("CommandDelay(" + getTrainName() +")");
        }

        // check if request for a duplicate CommandDelay can be cancelled
        boolean isDuplicate(String speedType, long startWait, int endBlockIdx) {
            if (endBlockIdx == _endBlockIdx && speedType.equals(_speedType) &&
                    (_waitTime - (System.currentTimeMillis() - _startTime)) < startWait) {
                return true;    // keeps this thread
            }
            return false;   // not a duplicate or does not shorten time wait. this thread will be cancelled
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
                            wait(100);
                        } catch (InterruptedException ie) {
                            if (log.isDebugEnabled()) {
                                log.debug("CommandDelay interrupt.  Ramp to {} not done. warrant {}",
                                        _speedType, getDisplayName());
                            }
                        }
                        time += 50;
                    }
                } else {
                    try {
                        wait(_waitTime);
                    } catch (InterruptedException ie) {
                        if (log.isDebugEnabled()) {
                            log.debug("CommandDelay interrupt.  Ramp to {} not done. warrant {}",
                                    _speedType, getDisplayName());
                        }
                    }
                }

                if (!quit && _engineer != null) {
                    fireRunStatus("RampBegin", String.valueOf(_waitTime), _speedType);
                    _engineer.rampSpeedTo(_speedType, _endBlockIdx);
                }
            }
            endDelayCommand();
        }
    }

    synchronized private void cancelDelayRamp(boolean quit) {
        if (_delayCommand != null) {
            _delayCommand.quit = quit;
            _delayCommand.interrupt();
            log.debug("{}: cancelDelayRamp({}) called. _speedType= {}", getDisplayName(), quit, _delayCommand._speedType);
            _delayCommand = null;
        }
    }

    synchronized private void endDelayCommand() {
        _delayCommand = null;
    }

    private void rampSpeedTo(String speedType, int idx) {
        cancelDelayRamp(true);
        _engineer.rampSpeedTo(speedType, idx);
        
    }

    private void setSpeedToType(String speedType) {
        cancelDelayRamp(true);
        _engineer.setSpeedToType(speedType);
        
    }
    
    private void clearWaitFlags(boolean removeListeners) {
        if (log.isTraceEnabled()) {
            log.trace("{}: Flags cleared {}.", getDisplayName(), removeListeners?"and removed Listeners":"only");
        }
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
                _idxStoppingBlock = -1;
            }
        }
    }

    /*
     * Return pathLength of the block.
     */
    private float getAvailableDistanceAt(int idxBlockOrder) {
        BlockOrder blkOrder = getBlockOrderAt(idxBlockOrder);
        float pathLength = blkOrder.getPathLength();
        if (idxBlockOrder == 0 || pathLength <= 20.0f) {
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

    private float getEntranceBufferDist(int idxBlockOrder) {
        float bufDist = BUFFER_DISTANCE;
        if (_waitForSignal) {        // signal restricting speed
            bufDist+= getBlockOrderAt(idxBlockOrder).getEntranceSpace(); // signal's adjustment
        }
        return bufDist;
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
     */
    private void setMovement() {
        BlockOrder curBlkOrder = getBlockOrderAt(_idxCurrentOrder);
        OBlock curBlock = curBlkOrder.getBlock();
        String currentSpeedType = _engineer.getSpeedType(true); // current or pending speed type
        String entrySpeedType = BlockOrder.getPermissibleSpeedAt(curBlkOrder); // expected speed type for this block
        if (entrySpeedType == null) {
            entrySpeedType = currentSpeedType;
        }
        curBlkOrder.setPath(this);  // restore running

        if (log.isDebugEnabled()) {
            SpeedState speedState = _engineer.getSpeedState();
            int runState = _engineer.getRunState();
            log.debug("{}: SET MOVEMENT Block \"{}\" runState= {}, speedState= {} for currentSpeedType= {}. entrySpeedType= {}.",
                    getDisplayName(), curBlock.getDisplayName(), RUN_STATE[runState], speedState.toString(),
                    currentSpeedType, entrySpeedType);
            log.debug("{}: Flags: _waitForBlock={}, _waitForSignal={}, _waitForWarrant={} curThrottle= {}.",
                    getDisplayName(), _waitForBlock, _waitForSignal, _waitForWarrant, _engineer.getSpeedSetting());
            if (_message != null) {
                log.debug("{}: _message ({}) ", getDisplayName(), _message);
            }
        }
        // Delayed ramps must begin within the blocks where there were made
        cancelDelayRamp(false); // interrupts and launches ramp

        if (_noRamp) {
            if (_idxCurrentOrder < _orders.size() - 1) {
                entrySpeedType = BlockOrder.getPermissibleSpeedAt(getBlockOrderAt(_idxCurrentOrder + 1));
                if (_speedUtil.secondGreaterThanFirst(entrySpeedType, currentSpeedType)) {
                    if (log.isDebugEnabled()) {
                        log.debug("{}: noRamping speed change of \"{}\" from \"{}\" in block \"{}\"",
                              getDisplayName(), entrySpeedType, currentSpeedType, curBlock.getDisplayName());
                    }
                    setSpeedToType(entrySpeedType);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("{}: Exit setMovement due to no ramping.", getDisplayName());
            }
            return;
        }

        // Check that flags and states agree with expected speed and position
        // A signal drop down can appear to be a speed violation, but only when a violation when expected
        if (_idxCurrentOrder > 0) {
            if (_waitForSignal) {
                if (_idxProtectSignal == _idxCurrentOrder) {
                    makeOverrunMessage(curBlkOrder);
                    setSpeedToType(Stop); // immediate decrease
                    return;
                }
            }
            if (_idxStoppingBlock == _idxCurrentOrder) {
                if (_waitForBlock || _waitForWarrant) {
                    makeOverrunMessage(curBlkOrder);
                    setSpeedToType(Stop); // immediate decrease
                    return;
                }
            }
            
            if (_speedUtil.secondGreaterThanFirst(entrySpeedType, currentSpeedType)) {
                // signal or block speed entrySpeedType is less than currentSpeedType.
                // Speed for this block is violated so set end speed immediately
                jmri.NamedBean signal = curBlkOrder.getSignal();
                if (signal != null) {
                    log.info("Train {} moved past required {} speed for signal \"{}\" at block \"{}\" on warrant {}!",
                            getTrainName(), entrySpeedType, signal.getDisplayName(), curBlock.getDisplayName(), getDisplayName());
                } else {
                    log.info("Train {} moved past required \"{}\" speed at block \"{}\" on warrant {}!",
                            getTrainName(), entrySpeedType, curBlock.getDisplayName(), getDisplayName());
                }
                fireRunStatus("SignalOverrun", (signal!=null?signal.getDisplayName():curBlock.getDisplayName()),
                        entrySpeedType); // message of speed violation
               setSpeedToType(entrySpeedType); // immediate decrease
               currentSpeedType = entrySpeedType;
            }
        } else {    // at origin block and train has arrived,. ready to move
            if (Stop.equals(currentSpeedType)) {
                currentSpeedType = Normal;
            }
        }

        if (_idxCurrentOrder < _orders.size() - 1) {
            lookAheadforSpeedChange(currentSpeedType, entrySpeedType);
        } // else at last block, forget about speed changes, return;
    }

    /*
     * entrySpeedType (expected type) will be either equal to or greater than currentSpeedType 
     * for all blocks except rhe first.
     */
    private void lookAheadforSpeedChange(String currentSpeedType, String entrySpeedType) {
        clearWaitFlags(false);
        // look ahead for speed type slower than current type, refresh flags
        // entrySpeedType is the expected speed to be reached, if no speed change ahead

        String speedType = currentSpeedType;    // first slower speedType ahead
        int idx = _idxCurrentOrder + 1;
        int idxSpeedChange = -1;  // idxBlockOrder where speed changes
        int idxContrlBlock = -1;
        int limit;
        if (_shareRoute) {
            limit = Math.min(_orders.size(), _idxCurrentOrder + 4); 
        } else {
            limit = _orders.size();
        }
        boolean allocate = true;
        int numAllocated = 0;
        do {
            TrainOrder to = getBlockOrderAt(idx).allocatePaths(this, allocate);
            switch (to._cause) {
                case NONE:
                    break;
               case WARRANT:
                   _waitForWarrant = true;
                   _message = to._message;
                   idxContrlBlock = to._idxContrlBlock;
                   idxSpeedChange = to._idxEnterBlock;
                   speedType = Stop;
                   break;
                case OCCUPY:
                    _waitForBlock = true;
                    _message = to._message;
                    idxContrlBlock = to._idxContrlBlock;
                    idxSpeedChange = to._idxEnterBlock;
                    speedType = Stop;
                    break;
                case SIGNAL:
                    speedType = to._speedType;
                    if (Stop.equals(speedType)) {
                        _waitForSignal = true;
                    }
                    idxContrlBlock = to._idxContrlBlock;
                    idxSpeedChange = to._idxEnterBlock;
                    _message = to._message;
                    break;
                default:
                    log.error("{}: lookAheadforSpeedChange at block \"{}\" setPath returns: {}", 
                            getDisplayName(), getBlockAt(_idxCurrentOrder).getDisplayName(), to.toString());
                    _message = to._message;
                    setSpeedToType(Stop);
                    return;
            }
            
            if (log.isDebugEnabled()) {
                if (to._cause.equals(TrainOrder.Cause.NONE)) {
                    log.debug("{}: TrainOrder speed change \"{}\" for \"{}\"",
                            getDisplayName(), to._cause, getBlockAt(to._idxEnterBlock).getDisplayName());
                } else {
                    log.debug("{}: TrainOrder speed change {} before \"{}\" due to {} at \"{}\". {}",
                            getDisplayName(), to._speedType, getBlockAt(to._idxEnterBlock).getDisplayName(),
                            to._cause, getBlockAt(to._idxContrlBlock).getDisplayName(), to._message);
                }
            }
            numAllocated++;
            if (Stop.equals(speedType)) {
                break;
            }
            if (_shareRoute && numAllocated >= 2 ) {
                allocate = false;
            }
            idx++;

        } while ((idxSpeedChange < 0) && (idx < limit) &&
                !_speedUtil.secondGreaterThanFirst(speedType, currentSpeedType));

        if (!Stop.equals(speedType)) {
            while ((idx < limit)) { // allocate and set paths beyond speed change
                TrainOrder to = getBlockOrderAt(idx).allocatePaths(this, false);
                if (Stop.equals(to._speedType)) {
                    break;
                }
                idx++;
            }
        }
        if (idxSpeedChange < 0) {
            idxSpeedChange = _orders.size() - 1;
        }

        float availDist = getAvailableDistance(idxSpeedChange);  // distance ahead (excluding current block
        float changeDist = getEntranceDistance(idxSpeedChange, speedType);    // distance needed to change speed for speedType

        if (!currentSpeedType.equals(entrySpeedType)) {
            // entrySpeedType is greater than currentSpeedType. i.e. increase speed.
            rampSpeedTo(entrySpeedType, -1);
        }

        // set next signal after current block for aspect speed change
        for (int i = _idxCurrentOrder + 1; i < _orders.size(); i++) {
            if (setProtectingSignal(i)) {
               break;
           }
        }

        OBlock block = getBlockAt(idxSpeedChange);
        if (log.isDebugEnabled()) {
            log.debug("{}: Speed \"{}\" at block \"{}\" until speed \"{}\" at block \"{}\", availDist={}, changeDist={}",
                    getDisplayName(), currentSpeedType, getBlockAt(_idxCurrentOrder).getDisplayName(), speedType,
                    block.getDisplayName(), availDist, changeDist);
        }

        if (changeDist <= availDist) {
            clearWaitFlags(false);
            return;
        }

        // Now set stopping condition of flags, if any. Not, if current block is also ahead. 
        if (_waitForBlock) {
            if (!getBlockAt(_idxCurrentOrder).equals(block)) {
                setStoppingBlock(idxContrlBlock);
            }
        } else if (_waitForWarrant) {
            // if block is allocated and unoccupied, but cannot set path exit.
            if (_stoppingBlock == null) {
                setStoppingBlock(idxContrlBlock);
            }
        }

        // Begin a ramp for speed change in this block. If due to a signal, watch that one
        if(_waitForSignal) {
            // Watch this signal. Should be the previous set signal above.
            // If not, then user has not configured signal system to allow room for speed changes. 
            setProtectingSignal(idxContrlBlock);
        }

        // either ramp in progress or no changes needed. Stopping conditions set, so move on.
        if (!_speedUtil.secondGreaterThanFirst(speedType, currentSpeedType)) {
            return;
        }

        availDist += getAvailableDistanceAt(_idxCurrentOrder);   // Add available length in this block

        int cmdStartIdx = _speedUtil.getBlockSpeedInfo(_idxCurrentOrder).getFirstIndex();
        if (!doDelayRamp(availDist, changeDist, idxSpeedChange, speedType, cmdStartIdx)) {
            log.warn("No room for train {} to ramp to \"{}\" from \"{}\" in block \"{}\"!. availDist={}, changeDist={} on warrant {}",
                    getTrainName(), speedType, currentSpeedType, getBlockAt(_idxCurrentOrder).getDisplayName(),
                    availDist,  changeDist, getDisplayName());
        }
    }

    /*
     * if there is sufficient room calculate a wait time, otherwise ramp immediately.
     */
    private boolean doDelayRamp(float availDist, float changeDist, int idxSpeedChange, String speedType, int cmdStartIdx) {
        String pendingSpeedType = _engineer.getSpeedType(true); // current or pending speed type
        if (_trace || log.isDebugEnabled()) {
            String reason;
            if(_waitForSignal) {
                reason = Bundle.getMessage("Signal");
            } else if (_waitForWarrant) {
                reason = Bundle.getMessage("Warrant");
            } else if (_waitForBlock) {
                reason = Bundle.getMessage("Occupancy");                
            } else {
                reason ="Unspecified";
            }

            if (log.isDebugEnabled()) {
                log.info("Train \"{}\" needs speed decrease to \"{}\" from \"{}\" for {} before entering block \"{}\", availDist={}, changeDist={}",
                        getTrainName(), speedType, pendingSpeedType, reason, getBlockAt(idxSpeedChange).getDisplayName(), 
                        availDist, changeDist);
           }
        }
        if (availDist <= 0) {
            log.error("{}: availDist= {}! for speed change to \"{}\" at block \"{}\"",
                    getDisplayName(), availDist, speedType, getBlockAt(_idxCurrentOrder).getDisplayName());
            setSpeedToType(speedType);
            return false;
        } else {
            SpeedState speedState = _engineer.getSpeedState();
            if (speedState.equals(SpeedState.STEADY_SPEED)) {
                if (changeDist > availDist && _idxCurrentOrder > 0) {
                    if (log.isDebugEnabled()) {
                        log.info("Train \"{}\" ramping down to speed \"{}\" in block \"{}\"",
                                getTrainName(), speedType, getBlockAt(_idxCurrentOrder).getDisplayName());
                    }
                    rampSpeedTo(speedType, idxSpeedChange);
                    return false;
                } else {
                    makespeedChange(availDist, changeDist, idxSpeedChange, speedType, cmdStartIdx);
                } 
            } else {    // ramping in progress
                if (log.isDebugEnabled()) {
                    String currentSpeedType = _engineer.getSpeedType(false); // current speed type
                    log.info("Train \"{}\" is {} to \"{}\" from \"{}\" in block \"{}\"",
                            getTrainName(), speedState, pendingSpeedType, currentSpeedType, getBlockAt(_idxCurrentOrder).getDisplayName());
                }
                
                int waitTime = _engineer.getRampTimeForDistance(availDist, changeDist, getEntranceBufferDist(idxSpeedChange));
                rampSpeedDelay(waitTime, speedType, 0, idxSpeedChange - 1);
            }
            return true;
        }
    }

    /**
     *  Must start the ramp in current block. ( at _idxCurrentOrder)
     *  find the time when ramp should start in this block, then use thread CommandDelay to start the ramp.
     *  Train must travel a deltaDist for a deltaTime to the start of the ramp.
     *  It travels at throttle settings of scriptSpeed(s) modified by currentSpeedType.
     *  trackSpeed(s) of these modified scriptSettings are computed from SpeedProfile
     *  waitThrottle is throttleSpeed when ramp is started. This may not be the scriptSpeed now
     *  Start with waitThrottle (modSetting) being at the entrance to the block.
     *  modSetting gives the current trackSpeed.
     *  accumulate the time and distance and determine the distance (changeDist) needed for entrance into
     *  block (at idxSpeedChange) requiring speed change to speedType
     *  final ramp should modify waitSpeed to endSpeed and must end at the exit of end block (endBlockIdx)
     *
     * @param availDist     distance available to make the ramp
     * @param changeDist    distance needed for the rmp
     * @param idxSpeedChange block order index of block to complete change before entry
     * @param speedType     speed aspect of speed change
     * @param cmdStartIdx   command index of delay  
     */
    private long makespeedChange(float availDist, float changeDist, int idxSpeedChange, String speedType, int cmdStartIdx) {
        String currentSpeedType = _engineer.getSpeedType(false); // current or pending speed type

        int cmdEndIdx = _speedUtil.getBlockSpeedInfo(idxSpeedChange - 1).getLastIndex();
        float scriptSpeed = _speedUtil.getBlockSpeedInfo(idxSpeedChange).getEntranceSpeed();
        float endSpeed = _speedUtil.modifySpeed(scriptSpeed, speedType);    // throttle at end of ramp

        scriptSpeed = _engineer.getScriptSpeed();  // script throttle setting
        float startSpeed = _engineer.getSpeedSetting();       // throttle at start of ramp

        float beginTrackSpeed = _speedUtil.getTrackSpeed(startSpeed);   // mm/sec track speed at modSetting
        float trackSpeed = beginTrackSpeed;
        if (_idxCurrentOrder == 0 && availDist > BUFFER_DISTANCE) {
            changeDist = 0;
        }
        if (log.isDebugEnabled()) {
            log.debug("makespeedChange cmdIdx #{} to #{} at speedType \"{}\" to \"{}\". speedSetting={}, changeDist={}, availDist={}",
                    cmdStartIdx+1, cmdEndIdx+1, currentSpeedType, speedType, startSpeed, changeDist, availDist);
            // command index numbers biased by 1
        }
        float accumTime = 0;    // accumulated time of commands up to ramp start
        float accumDist = 0;
        float timeRatio = 1; // time adjustment for current speed type
        if (trackSpeed > _speedUtil.getRampThrottleIncrement()) {
            timeRatio = _speedUtil.getTrackSpeed(scriptSpeed) / trackSpeed;
        } else {
            timeRatio = 1;
        }
        float bufDist = getEntranceBufferDist(idxSpeedChange);

        boolean messageFlag = false;
        for (int i = cmdStartIdx; i <= cmdEndIdx; i++) {
            ThrottleSetting ts = _commands.get(i);
            accumDist += trackSpeed * ts.getTime() * timeRatio;
            accumTime += ts.getTime() * timeRatio;
            ThrottleSetting.CommandValue cmdVal = ts.getValue();
            if (cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_FLOAT)) {
                scriptSpeed = cmdVal.getFloat();
                startSpeed = _speedUtil.modifySpeed(scriptSpeed, currentSpeedType);
                changeDist = _speedUtil.getRampLengthForEntry(startSpeed, endSpeed) + bufDist;
                trackSpeed = _speedUtil.getTrackSpeed(startSpeed);
                timeRatio = _speedUtil.getTrackSpeed(scriptSpeed) / trackSpeed;
            }
            if (log.isDebugEnabled()) {
                log.debug("{}: cmd#{} accumTime= {} accumDist= {} changeDist= {} startSpeed= {}",
                        getDisplayName(), i+1, accumTime, accumDist, changeDist, startSpeed);
            }
            if (changeDist + accumDist >= availDist) {
                float remDist = changeDist + accumDist - availDist;
                if (trackSpeed > 0) {
                    accumTime -= remDist / trackSpeed;
                } else {
                    messageFlag = true;
                    log.error("\"{}\" cmd#{} cannot make \"{}\" ramp for block \"{}\". trackSpeed= {} accumDist= {}", getDisplayName(),
                            i+1, speedType, getBlockAt(idxSpeedChange).getDisplayName(), trackSpeed, accumDist);
                }
                break;
            }
            if (cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_NOOP)) {
                // speed change is supposed to start in current block
                // start ramp in next block?
                log.error("{} cmd#{} NOOP. \"{}\" ramp premature for block \"{}\". accumDist= {} availDist= {} changeDist= {}",
                        getDisplayName(), i+1, speedType, getBlockAt(idxSpeedChange).getDisplayName(), accumDist, availDist, changeDist);
                if (changeDist + accumDist < availDist) {
                    float remDist = availDist - changeDist - accumDist;
                    if (trackSpeed > 0) {
                        accumTime += remDist / trackSpeed;
                    }
                }
                break;
            }
        }

        if (accumTime == 0 && beginTrackSpeed > 0) {
            accumTime = availDist - changeDist / beginTrackSpeed;
        }
        int waitTime = Math.round(accumTime);

        if (messageFlag || log.isDebugEnabled()) {
            log.info("{}: RAMP waitTime={} availDist={}, enterLen={} accumDist={} startSpeed={} endSpeed={} for ramp start",
                    getDisplayName(), waitTime, availDist, changeDist, accumDist, startSpeed, endSpeed);
        }

        rampSpeedDelay(waitTime, speedType, startSpeed, idxSpeedChange - 1);
        return waitTime;
    }

    synchronized private void rampSpeedDelay (long waitTime, String speedType, float waitSpeed, int endBlockIdx) {
        waitTime -= 50;     // Subtract a bit to avoid last unwanted speed command
        if( waitTime < 0) {
            rampSpeedTo(speedType, endBlockIdx);   // do it now on this thread.
            return;
        }
        if (_delayCommand != null) {
            if (_delayCommand.isDuplicate(speedType, waitTime, endBlockIdx)) {
                return;
            }
            cancelDelayRamp(true);
        }
        _delayCommand = new CommandDelay(speedType, waitTime, waitSpeed, endBlockIdx);
        _delayCommand.start();
        if (log.isDebugEnabled()) {
            log.debug("{}: CommandDelay: will wait {}ms, then Ramp to {} in block {}.",
                    getDisplayName(), waitTime, speedType, getBlockAt(endBlockIdx).getDisplayName());
        }
    }

    protected void downRampBegun(int endBlockIdx) {
        OBlock block = getBlockAt(endBlockIdx + 1);
        if (block != null) {
            _overBlkOccupied = block.isOccupied();
        } else {
            _overBlkOccupied = true;
        }
    }

    protected void downRampDone(boolean stop, boolean halted, String speedType, int endBlockIdx) {
        if (_idxCurrentOrder < endBlockIdx) {
            return;     // overrun not possible.
        }
        // look for overruns
        int nextIdx = endBlockIdx + 1;
        if (nextIdx > 0 && nextIdx < _orders.size()) {
            BlockOrder bo = getBlockOrderAt(nextIdx);
            OBlock block = bo.getBlock();
            if (block.isOccupied() && !_overBlkOccupied) { // was not occupied by another train at start of ramp.
                _overrun = true;    // endBlock occupied during ramp down. Speed overrun!
                // _waitForBlock == false means highly likely 'this' train is occupying block
                Warrant w = block.getWarrant();
                if (w != null && !w.equals(this)) { // probably redundant
                    _waitForWarrant = true;
                    setStoppingBlock(nextIdx);
                } else {    // take ownership, if possible
                    _message = setPathAt(nextIdx);
                }
                if (Stop.equals(BlockOrder.getPermissibleSpeedAt(bo))) { // probably redundant
                    _waitForSignal = true;
                    setProtectingSignal(nextIdx);
                }
                makeOverrunMessage(bo);
                _idxCurrentOrder = nextIdx;
            }   // case where  _waitForBlock was already set is indeterminate
        }
    }

    private void makeOverrunMessage(BlockOrder curBlkOrder) {
        OBlock curBlock = curBlkOrder.getBlock();
        String name = null;
        if (_waitForSignal) {
            jmri.NamedBean signal = curBlkOrder.getSignal();
            if (signal!=null) {
                name = signal.getDisplayName();
            } else {
                name = curBlock.getDisplayName();
            }
            _overrun = true;
            String entrySpeedType = BlockOrder.getPermissibleSpeedAt(curBlkOrder); // expected speed type for this block
            log.info("{} : Signal Over run", Bundle.getMessage("SignalOverrun", getTrainName(), entrySpeedType, name));
            fireRunStatus("SignalOverrun", name, entrySpeedType); // message of speed violation
            return;
        }
        String bundleKey = null;
        if (_waitForWarrant) {
            bundleKey ="WarrantOverrun";
            Warrant w = curBlock.getWarrant();
            if (w != null) {
                name = w.getDisplayName();
            }
        } else if (_waitForBlock){
            bundleKey ="OccupyOverrun";
            name = (String)curBlock.getValue();
        }
        if (name == null) {
            name = Bundle.getMessage("unknownTrain");
        }
        if (bundleKey != null) {
            _overrun = true;
            log.info("{} : Over run", Bundle.getMessage(bundleKey, getTrainName(), curBlock.getDisplayName(), name));
            fireRunStatus(bundleKey, curBlock.getDisplayName(), name); // message of speed violation
        } else {
            log.error("Train \"{}\" entered stopping block \"{}\" for unknown reason on warrant {}!",
                getTrainName(), curBlock.getDisplayName(), getDisplayName());
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
