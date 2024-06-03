package jmri.jmrit.dispatcher;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import javax.annotation.CheckForNull;

import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.swing.JmriJOptionPane;

/**
 * This class holds information and options for an ActiveTrain when it is
 * running in AUTOMATIC mode. It is an extension to Active Train for automatic
 * running.
 * <p>
 * This class implements logic that follows a train around a layout. Train
 * follows signals, provided the next Section is allocated to it, and its
 * ActiveTrain's status is RUNNING.
 * <p>
 * This class is linked via its parent ActiveTrain object.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * The AutoEngineer sub class is based in part on code by Pete Cressman
 * contained in Warrants.java
 *
 * @author Dave Duchamp Copyright (C) 2010-2011
 */
public class AutoActiveTrain implements ThrottleListener {

    /**
     * Create an AutoActiveTrain.
     *
     * @param at the train to automate
     */
    public AutoActiveTrain(ActiveTrain at) {
        _activeTrain = at;
        at.setAutoActiveTrain(this);
        _autoTrainAction = new AutoTrainAction(this);
        _lbManager = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        // listen for additions in our allocated section table
        at.addPropertyChangeListener("sectionallocated",this::handleAnotherSectionAllocatedChange);
    }

    /* Speed aspects as defined by Douglas A. Kerr - "Rail Signal Aspects and Indications"
     * http://dougkerr.net/Pumpkin/articles/Rail_signal_aspects.pdf (from Pete Cressman)
     */
//    public static final int SPEED_MASK = 0x07;     // least significant 3 bits
    public static final int STOP_SPEED = 0x01;     // No Speed
    public static final int RESTRICTED_SPEED = 0x02;    // Train able to stop within 1/2 visual range (10mph)
    public static final int SLOW_SPEED = 0x03;     // Typically 15 mph  (25% of NORMAL)
    public static final int MEDIUM_SPEED = 0x04;     // Typically 30 mph (40% of NORMAL)
    public static final int LIMITED_SPEED = 0x05;     // Typically 40-45 mph  (65% of NORMAL)
    public static final int NORMAL_SPEED = 0x06;     // Varies with road and location
    public static final int MAXIMUM_SPEED = 0x07;     // "full" throttle

    private final Float[] _speedRatio = {-1.0F, 0.0F, 0.25F, 0.35F, 0.50F, 0.65F, 0.8F, 1.15F};

    /* The ramp rates below are in addition to what the decoder itself does
     */
    public static final int RAMP_NONE = 0x00;  // No ramping - set speed immediately
    public static final int RAMP_FAST = 0x01;     // Fast ramping
    public static final int RAMP_MEDIUM = 0x02;  // Medium ramping
    public static final int RAMP_MED_SLOW = 0x03;  // Medium/slow ramping
    public static final int RAMP_SLOW = 0x04;  // Slow ramping
    public static final int RAMP_SPEEDPROFILE = 0x05; // use speed profile and section distance

    /* Stop tasks codes
     */
    public static final int NO_TASK = 0x00;     // No task at stop
    public static final int END_REVERSAL = 0x01;     // Handle reversing direction at end for back and forth running
    public static final int BEGINNING_RESET = 0x02;     // Handle reseting beginning for back and forth running
    public static final int END_TRAIN = 0x04;     // Ending Transit.

    // operational instance variables
    private static final NamedBean.DisplayOptions USERSYS = NamedBean.DisplayOptions.USERNAME_SYSTEMNAME;
    private ActiveTrain _activeTrain = null;
    private AutoTrainAction _autoTrainAction = null;
    private DccThrottle _throttle = null;
    private AutoEngineer _autoEngineer = null;
    private int _address = -1;
    private int _savedStatus = ActiveTrain.RUNNING;
    private int _currentRampRate = RAMP_NONE; // current Ramp Rate
    private boolean _pausingActive = false;   // true if train pausing thread is active
    private DispatcherFrame dispatcher;

    // persistent instance variables (saved with train info)
    private int _rampRate = RAMP_NONE; // default Ramp Rate
    private float _speedFactor = 1.0f; // default speed factor
    private float _maxSpeed = 0.6f;    // default maximum train speed
    //private TrainDetection _trainDetection = TrainDetection.TRAINDETECTION_NONE; // true if all train cars show occupancy
    private boolean _runInReverse = false;    // true if the locomotive should run through Transit in reverse
    private boolean _soundDecoder = false;    // true if locomotive has a sound decoder
    private volatile float _maxTrainLength = 200.0f; // default train length (scale feet/meters)
    private float _stopBySpeedProfileAdjust = 1.0f;
    private boolean _stopBySpeedProfile = false;
    private boolean _useSpeedProfile = true;

    // accessor functions
    public ActiveTrain getActiveTrain() {
        return _activeTrain;
    }

    public AutoEngineer getAutoEngineer() {
        return _autoEngineer;
    }

    public AutoTrainAction getAutoTrainAction() {
        return _autoTrainAction;
    }

    public RosterEntry getRosterEntry() {
        return re;
    }

    public boolean getForward() {
        return _autoEngineer.getIsForward();
    }

    public void setForward(boolean set) {
        _autoEngineer.setIsForward(set);
    }

    public synchronized float getTargetSpeed() {
        return _autoEngineer.getTargetSpeed();
    }

    public synchronized void setTargetSpeedByPass(float speed) {
         _autoEngineer.setTargetSpeed(speed);
    }

    public synchronized void setTargetSpeed(float speed) {
        if (_autoEngineer.isStopped() && getTargetSpeed() == 0.0f && speed > 0.0f) {
            if (_autoTrainAction.isDelayedStart(speed)) {
                return;
            }
        }
        _autoEngineer.setTargetSpeed(speed);
    }

    public int getSavedStatus() {
        return _savedStatus;
    }

    public void setSavedStatus(int status) {
        _savedStatus = status;
    }

    public synchronized void setCurrentRampRate(int rate) {
        _currentRampRate = rate;
    }

    public int getRampRate() {
        return _rampRate;
    }

    public void setRampRate(int rate) {
        _rampRate = rate;
        _currentRampRate = rate;
    }

    public float getSpeedFactor() {
        return _speedFactor;
    }

    public void setSpeedFactor(float factor) {
        _speedFactor = factor;
    }

    public float getMaxSpeed() {
        return _maxSpeed;
    }

    public void setMaxSpeed(float speed) {
        _maxSpeed = speed;
    }

/**
 * @deprecated Use {@code ActiveTrain.setTrainDetection(TrainDetection value } insteadUse 
 * @param set True if entire train is detectable
 */
    @Deprecated (since="5.7.6",forRemoval=true)
    public void setResistanceWheels(boolean set) {
        if (set) {
           _activeTrain.setTrainDetection(TrainDetection.TRAINDETECTION_WHOLETRAIN);
        } else {
            _activeTrain.setTrainDetection(TrainDetection.TRAINDETECTION_HEADONLY);
        }
    }

    public boolean getRunInReverse() {
        return _runInReverse;
    }

    public void setRunInReverse(boolean set) {
        _runInReverse = set;
    }

    public boolean getSoundDecoder() {
        return _soundDecoder;
    }

    public void setSoundDecoder(boolean set) {
        _soundDecoder = set;
    }

    public float getMaxTrainLength() {
        return _maxTrainLength;
    }

    public void setMaxTrainLength(float length) {
        _maxTrainLength = length;
    }

    public void setUseSpeedProfile(boolean tf) {
        _useSpeedProfile = tf;
    }

    public boolean getUseSpeedProfile() {
        return _useSpeedProfile;
    }

    public void setStopBySpeedProfile(boolean tf) {
        _stopBySpeedProfile = tf;
    }

    public void setStopBySpeedProfileAdjust(float adjust) {
        _stopBySpeedProfileAdjust = adjust;
    }

    /**
     * Get current Signal DisplayName.
     * @return empty String if no signal, otherwise Display Name.
     */
    public String getCurrentSignal() {
        if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALHEAD) {
            return  (_controllingSignal == null  ) ? "" : _controllingSignal.getDisplayName() ;
        } else {
            return (_controllingSignalMast == null  ) ? "" : _controllingSignalMast.getDisplayName();
        }
    }

    /**
     * Get current Signal UserName.
     * @return empty String if no signal, otherwise UserName.
     */
    public String getCurrentSignalUserName() {
        if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALHEAD) {
            return  ( _controllingSignal == null || _controllingSignal.getUserName() == null) ? "" : _controllingSignal.getUserName();
        } else {
            return ( _controllingSignalMast == null || _controllingSignalMast.getUserName() == null) ? "" : _controllingSignalMast.getUserName();        }
    }

    private RosterEntry re = null;
    boolean useSpeedProfile = false;

    /**
     * Initialize new Auto Active Train or get a new throttle after WORKING Sets
     * up the DCC address and initiates creation of a throttle to run the train.
     *
     * @return true if initialized; false otherwise
     */
    public boolean initialize() {
        //clear all flags
        _pausingActive = false;
        _stoppingBySensor = false;
        _stoppingByBlockOccupancy = false;
        _stoppingUsingSpeedProfile = false;
        // get the dispatcher
        dispatcher = InstanceManager.getDefault(DispatcherFrame.class);

        // get decoder address
        try {
            _address = Integer.parseInt(_activeTrain.getDccAddress());
        } catch (NumberFormatException ex) {
            log.warn("invalid dcc address '{}' for {}", _activeTrain.getDccAddress(), _activeTrain.getTrainName());
            return false;
        }
        if ((_address < 1) || (_address > 9999)) {
            log.warn("invalid dcc address '{}' for {}", _activeTrain.getDccAddress(), _activeTrain.getTrainName());
            return false;
        }
        // request a throttle for automatic operation, throttle returned via callback below
        useSpeedProfile = false;
        boolean ok;
        DccLocoAddress addressForRequest = new DccLocoAddress(
            _address,!InstanceManager.throttleManagerInstance().canBeShortAddress(_address));
        if (_activeTrain.getTrainSource() == ActiveTrain.ROSTER) {
            if (_activeTrain.getRosterEntry() != null) {
                re = _activeTrain.getRosterEntry();
                ok = InstanceManager.throttleManagerInstance().requestThrottle(re, this, false);
                if (_useSpeedProfile) {
                    if (re.getSpeedProfile() != null && re.getSpeedProfile().getProfileSize() > 0) {
                        useSpeedProfile = true;
                    }
                }
                log.debug("{}: requested roster entry '{}', address={}, use speed profile={}",
                        _activeTrain.getTrainName(), re.getId(), _address, useSpeedProfile);
            } else {
                ok = InstanceManager.throttleManagerInstance().requestThrottle(addressForRequest, this, false);
                log.debug("{}: requested throttle address={}, roster entry not found", _activeTrain.getTrainName(), _address);
            }
        } else {
            ok = InstanceManager.throttleManagerInstance().requestThrottle(addressForRequest, this, false);
            log.debug("{}: requested throttle address={}", _activeTrain.getTrainName(), _address);
        }
        if (!ok) {
            log.warn("Throttle for locomotive address {} could not be setup.", _address);
            _activeTrain.setMode(ActiveTrain.DISPATCHED);
            return false;
        }
        return true;
    }

    // Throttle feedback method - Initiates running AutoEngineer with the new throttle
    @Override
    public void notifyThrottleFound(DccThrottle t) {
        _throttle = t;
        if (_throttle == null) {
            JmriJOptionPane.showMessageDialog(null, java.text.MessageFormat.format(Bundle.getMessage(
                    "Error28"), new Object[]{_activeTrain.getTrainName()}), Bundle.getMessage("MessageTitle"),
                    JmriJOptionPane.INFORMATION_MESSAGE);
            log.warn("null throttle returned for train '{}' during automatic initialization.", _activeTrain.getTrainName());
            _activeTrain.setMode(ActiveTrain.DISPATCHED);
            return;
        }
        log.debug("{}: New AutoEngineer, address={}, length={}, factor={}, useSpeedProfile={}",
                _activeTrain.getTrainName(),
                _throttle.getLocoAddress(),
                getMaxTrainLength(), _speedFactor, _useSpeedProfile);
        // get off this thread ASAP, some throttles does not completely initialize
        // until this thread finishes
        jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
            if (_autoEngineer != null) {
                log.error("Second Trottle for same loco[{}] - ignoring", _address);
                // at least make sure its going the right way...
                setEngineDirection();
            } else {
                _autoEngineer = new AutoEngineer(t, re);
                _activeTrain.setMode(ActiveTrain.AUTOMATIC);
                // set initial direction
                setEngineDirection();
                _autoEngineer.setRamping(_currentRampRate, dispatcher.getFullRampTime(),
                        dispatcher.getMinThrottleInterval(), _currentRampRate);
            }
            if (_resumingAutomatic) {
                _resumingAutomatic = false;
                _activeTrain.setStatus(ActiveTrain.RUNNING);
                setupNewCurrentSignal(null, true);
                // if no current signal use saved.
                if (!isCurrentSignal()) {
                    restoreSavedSpeedAndDirection();
                } else {
                    setSpeedBySignal();
                }
            } else if (InstanceManager.getDefault(DispatcherFrame.class).getAutoAllocate()) {
                // starting for the first time with automatic allocation of
                // Sections
                // the last of 2 threads must call setSpeedBySignal
                // if the other thread is incomplete _currentAllocated Section
                // will be null
                if (_currentAllocatedSection != null) {
                    setSpeedBySignal();
                }
            }
        }, 500);
    }

    protected DccThrottle getThrottle() {
        return _throttle;
    }

    @Override
    public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
        log.error("Throttle request failed for {} because {}", address, reason);
    }

    /**
     * No steal or share decisions made locally
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
    }

    // more operational variables
    // private final ArrayList<AllocatedSection> _allocatedSectionList = new ArrayList<>();
    private jmri.jmrit.display.layoutEditor.LayoutBlockManager _lbManager = null;
    private AllocatedSection _lastAllocatedSection = null;

    protected Section getLastAllocatedSection() {
      Section as = _activeTrain.getLastAllocatedSection();
       return as;
    }

    private boolean _initialized = false;
    private Section _nextSection = null;                      // train has not reached this Section yet
    private volatile AllocatedSection _currentAllocatedSection = null;    // head of the train is in this Section
    private volatile AllocatedSection _previousAllocatedSection = null;   // previous Section - part of train could still be in this section
    private SignalHead _controllingSignal = null;
    private SignalMast _controllingSignalMast = null;
    private SignalHead _controllingSignalPrev = null;
    private SignalMast _controllingSignalMastPrev = null;
    private PropertyChangeListener _conSignalListener = null;
    private PropertyChangeListener _conSignalMastListener = null;
    private Block _conSignalProtectedBlock = null;
    private volatile Block _currentBlock = null;
    private Block _nextBlock = null;
    private volatile Block _previousBlock = null;
    private boolean _stoppingBySensor = false;
    private Sensor _stopSensor = null;
    private PropertyChangeListener _stopSensorListener = null;
    private PropertyChangeListener _turnoutStateListener = null;
    private boolean _stoppingByBlockOccupancy = false;    // if true, stop when _stoppingBlock goes UNOCCUPIED
    private boolean _stoppingUsingSpeedProfile = false;     // if true, using the speed profile against the roster entry to bring the loco to a stop in a specific distance
    private volatile Block _stoppingBlock = null;
    private boolean _resumingAutomatic = false;  // if true, resuming automatic mode after WORKING session
    private boolean _needSetSpeed = false;  // if true, train will set speed according to signal instead of stopping
    private boolean waitingOnAllocation = false; //if true the train was stopped due to next section not allocated
    // keeps track of and restores previous speed
    private float _savedSpeed = 0.0f;
    private boolean _savedForward = true;

    public void set_useStopSensor(boolean _useStopSensor) {
        this._useStopSensor = _useStopSensor;
    }

    private boolean _useStopSensor = true;                    //used by DispatcherSystem to override use of stop sensor


    protected void saveSpeedAndDirection() {
        _savedSpeed = _autoEngineer.getTargetSpeed();
        _savedForward = _autoEngineer.getIsForward();
    }

    protected void restoreSavedSpeedAndDirection() {
        _autoEngineer.setTargetSpeed(_savedSpeed);
        _autoEngineer.setIsForward(_savedForward);
    }

    // keeps track of number of horn execution threads that are active
    private int _activeHornThreads = 0;

    protected void decrementHornExecution() {
        _activeHornThreads--;
    }

    protected void incrementHornExecution() {
        _activeHornThreads++;
    }

    //
    // Notification methods
    //
    /**
     * Handle notification of changes in section state.
     *
     * @param as the allocated that changed
     */
    protected void handleSectionStateChange(AllocatedSection as) {
        if (!_activeTrain.isInAllocatedList(as)) {
            addAllocatedSection(as);
        }
    }

    /**
     * Handle notification of allocation added to the ActiveTrain allocatedsections table.
     * Subtly different from change in a sections status.
     *
     * @param evt the allocation that changed
     */
    private void handleAnotherSectionAllocatedChange( PropertyChangeEvent evt) {
        if (waitingOnAllocation || InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SECTIONSALLOCATED) {
            waitingOnAllocation = false;
            setSpeedBySignal();
        }
    }

    /**
     * Handle notification of changes in section occupancy.
     *
     * @param as the section that changed
     */
    protected void handleSectionOccupancyChange(AllocatedSection as) {
        if (!_activeTrain.isInAllocatedList(as)) {
            log.debug("Unexpected occupancy change notification - Section {}", as.getSection().getDisplayName(USERSYS));
            return;
        }
        if (as.getSection().getOccupancy() == Section.OCCUPIED) {
            // Section changed to OCCUPIED - process if expected next Section
            if (as.getSection() == _nextSection) {
                setNewCurrentSection(as);
            }
        } else if (as.getSection().getOccupancy() == Section.UNOCCUPIED) {
            jmri.TransitSection ts = as.getTransitSection();
            if (ts != null) {
                _autoTrainAction.removeTransitSection(ts);
            }
        }
    }

    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC",
            justification = "OK to not sync here, no conflict expected")
    protected void handleBlockStateChange(AllocatedSection as, Block b) {
        //Block oldPreviousBlock = _previousBlock;
        if (b.getState() == Block.OCCUPIED) {
            // Block changed to OCCUPIED - train has entered this block
            log.debug("{}: handleBlockStateChange to OCCUPIED section {}, block {}, length {}", _activeTrain.getTrainName(),
                    as.getSection().getDisplayName(USERSYS),
                    b.getDisplayName(USERSYS), getBlockLength(b));
            if (b == _nextBlock || _nextBlock == null) {
                _currentBlock = b;
                // defer setting the next/previous blocks until we know if its required and in what fashion
                // for stopping blocks that action happens after the train has stopped.
                // first check for entering the end point
                if (!_activeTrain.isTransitReversed() && as.getSequence() == _activeTrain.getEndBlockSectionSequenceNumber()) {
                    // are we going to reverse at end
                    if ( _activeTrain.getReverseAtEnd() ) {
                        removeCurrentSignal();
                        stopInCurrentSection(END_REVERSAL);
                    }
                    // are we going continuously without delay
                    else if ( _activeTrain.getResetWhenDone() && _activeTrain.getDelayedRestart() == ActiveTrain.NODELAY) {
                        _activeTrain.setRestart(_activeTrain.getDelayedRestart(),_activeTrain.getRestartDelay(),
                                _activeTrain.getRestartSensor(),_activeTrain.getResetRestartSensor());
                        _activeTrain.setTransitReversed(false);
                        _activeTrain.resetAllAllocatedSections();
                        _previousBlock = null;
                        _nextBlock = getNextBlock(_currentBlock, _currentAllocatedSection);
                        setEngineDirection();
                        if ((_nextSection != null) && !_activeTrain.isInAllocatedList(_nextSection)) {
                            // we need to get a next section
                            InstanceManager.getDefault(DispatcherFrame.class).queueScanOfAllocationRequests();
                            // and then set the signal
                        }
                        // can be mid block
                        setupNewCurrentSignal(null, true);
                        setSpeedBySignal();
                    }
                    // are we restarting later
                    else if ( _activeTrain.getResetWhenDone()) {
                        // entered start block of Transit, must stop and reset for continuing - ignore signal changes till train stopped.
                        removeCurrentSignal();
                        stopInCurrentSection(BEGINNING_RESET);
                    }
                    // else we are ending here
                    else {
                        log.debug("{}: Trip end, stop in Current Section, Block= {}", _activeTrain.getTrainName(), b.getDisplayName(USERSYS));
                        removeCurrentSignal();
                        stopInCurrentSection(END_TRAIN);
                    }
                }
                // are we entering the start point
                else if (_activeTrain.isTransitReversed() && as.getSequence() == _activeTrain.getStartBlockSectionSequenceNumber()) {
                     // are we coming back from a reverse and running continiuosly
                    if ( _activeTrain.getResetWhenDone() && _activeTrain.isTransitReversed() ) {
                        removeCurrentSignal();
                        stopInCurrentSection(BEGINNING_RESET);
                    }
                    // else we are ending here
                    else {
                        log.debug("{}: Trip end, stop in Current Section, Block= {}", _activeTrain.getTrainName(), b.getDisplayName(USERSYS));
                        removeCurrentSignal();
                        stopInCurrentSection(END_TRAIN);
                    }
                } else {
                    // if we are not in first and not in last get the next block
                    //_previousBlock = oldPreviousBlock;
                    _nextBlock = getNextBlock(b, as);
                    if (_nextBlock != null) {
                        // this is a normal block/block change
                        // set the blocks as normal
                        _previousBlock = _currentBlock;
                        _nextBlock = getNextBlock(b, as);
                        setupNewCurrentSignal(as, false);
                    } else {
                        // assume we have reached last block in this transit, for safety sake.
                        log.warn("{}: No next Block from Block= {} Section= {}", _activeTrain.getTrainName(),
                                b.getDisplayName(USERSYS), as.getSection().getDisplayName(USERSYS));
                        removeCurrentSignal();
                        stopInCurrentSection(NO_TASK);
                    }
                }
            } else if (b != _currentBlock) {
                log.trace("{}: block going occupied {} is not _nextBlock or _currentBlock - ignored.",
                        _activeTrain.getTrainName(), b.getDisplayName(USERSYS));
                return;
            }
        } else if (b.getState() == Block.UNOCCUPIED) {
            log.debug("{}: handleBlockStateChange to UNOCCUPIED - Section {}, Block {}, speed {}", _activeTrain.getTrainName(),
                    as.getSection().getDisplayName(USERSYS), b.getDisplayName(USERSYS),
                    _autoEngineer == null ? "" : getTargetSpeed());
            if (_stoppingByBlockOccupancy && (b == _stoppingBlock)) {
                log.trace("{}: setStopNow by block occupancy from Block unoccupied, Block= {}", _activeTrain.getTrainName(), b.getDisplayName(USERSYS));
                _stoppingByBlockOccupancy = false;
                _stoppingBlock = null;
                if (_needSetSpeed) {
                    _needSetSpeed = false;
                    setSpeedBySignal();
                } else {
                    setStopNow();
                }
            }
        }
        _autoTrainAction.handleBlockStateChange(as, b);
    }

    /**
     * support methods
     */
    protected void setEngineDirection() {
        boolean oldFwd = getForward();
        if (_runInReverse) {
            setForward(_activeTrain.isTransitReversed());
        } else {
            setForward(!_activeTrain.isTransitReversed());
        }
        log.debug("[{}]flipping direction was [{}] now [{}]",_activeTrain.getActiveTrainName() ,oldFwd, getForward());
    }

    protected AllocatedSection getCurrentAllocatedSection() {
        return _currentAllocatedSection;
    }

    protected void allocateAFresh() {
        //Reset initialized flag
        _initialized = false;
        // set direction
        _currentAllocatedSection=null;
        _currentBlock=null;
        setForward(!getRunInReverse());
    }

    private void addAllocatedSection(AllocatedSection as) {
        if (!_initialized) {
            // this is first allocated section, get things started
            _initialized = true;
            _nextSection = as.getSection();
            _currentBlock = _activeTrain.getStartBlock();
            if (as.getSection().containsBlock(_currentBlock)) {
                // starting Block is in this allocated section - find next Block
                setNewCurrentSection(as);
                _nextBlock = getNextBlock(_currentBlock, as);
            } else if (as.getSection().connectsToBlock(_currentBlock)) {
                // starting Block is connected to a Block in this allocated section
                EntryPoint ep = as.getSection().getEntryPointFromBlock(_currentBlock, as.getDirection());
                if (ep != null) {
                    _nextBlock = ep.getBlock();
                } else {
                    log.error("failure to get entry point to Transit from Block {}", _currentBlock.getDisplayName(USERSYS));
                }
            }
            if (_nextBlock != null) {
                // set up new current signal, as this a beginning we allow a signal not at end of block
                // to control the speed.
                setupNewCurrentSignal(as,true);
            }
        }
        // if train is stopping for lack of an allocation, set flag to restart it
        if (!_pausingActive && (_lastAllocatedSection == _currentAllocatedSection)
                && isStopping() && (_activeTrain.getStatus() == ActiveTrain.RUNNING)) {
            _needSetSpeed = true;
        }

        // request next allocation if appropriate--Dispatcher must decide whether to allocate it and when
        if ((!InstanceManager.getDefault(DispatcherFrame.class).getAutoAllocate()) && ((_lastAllocatedSection == null)
                || (_lastAllocatedSection.getNextSection() == as.getSection()))) {
            // if AutoAllocate, this is now done in DispatcherFrame.java for all trains
            _lastAllocatedSection = as;
            if (as.getNextSection() != null) {
                Section nSection = as.getNextSection();
                int nextSeq = as.getNextSectionSequence();
                int nextDir = _activeTrain.getAllocationDirectionFromSectionAndSeq(nSection, nextSeq);
                InstanceManager.getDefault(DispatcherFrame.class).requestAllocation(_activeTrain, nSection, nextDir, nextSeq, true, null);
            }
        }
    }

    private boolean isStopping() {
        // here add indicator for new stopping methods, if any are added
        return (_stoppingBySensor || _stoppingByBlockOccupancy || _stoppingUsingSpeedProfile);
    }

    private void removeCurrentSignal() {
        if (_conSignalListener != null) {
            _controllingSignal.removePropertyChangeListener(_conSignalListener);
            _conSignalListener = null;
        }
        _controllingSignalPrev = _controllingSignal;
        _controllingSignal = null;
        if (_conSignalMastListener != null) {
            _controllingSignalMast.removePropertyChangeListener(_conSignalMastListener);
            _conSignalMastListener = null;
        }
        _controllingSignalMastPrev = _controllingSignalMast;
        _controllingSignalMast = null;
        _needSetSpeed = false;
    }

    /**
     * checks for a controlling signal
     * @return true if there is one
     */
    protected boolean isCurrentSignal() {
        if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALHEAD) {
            return _controllingSignal != null;
        } else {
            // SignalMast
            return _controllingSignalMast != null;
        }
    }

    /**
     *
     * @param as current section the train is in, can be null
     * @param forceSpeedChange if true, the speed will be set using the signal mast
     *        even if it is not on the immediate block boundary
     */
    protected synchronized void setupNewCurrentSignal(AllocatedSection as, boolean forceSpeedChange) {
        log.trace("setupNewCurrentSignal Called Section[{}] forceSpeedChange[{}]", as != null ? as.getSectionName() : "null",forceSpeedChange);
        removeCurrentSignal();
        if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALHEAD) {
            SignalHead sh = _lbManager.getFacingSignalHead(_currentBlock, _nextBlock);
            if (sh != null) {
                _controllingSignal = sh;
                _conSignalProtectedBlock = _nextBlock;
                sh.addPropertyChangeListener(_conSignalListener = (PropertyChangeEvent e) -> {
                    if (e.getPropertyName().equals("Appearance")) {
                        // controlling signal has changed appearance
                        setSpeedBySignal();
                    }
                });
                _activeTrain.setControlingSignal(_controllingSignal, _controllingSignalPrev);
                log.debug("new current signal = {}", sh.getDisplayName(USERSYS));
                setSpeedBySignal();
            } else {
                // Note: null signal head will result when exiting throat-to-throat blocks.
                log.debug("new current signal is null - sometimes OK");
            }
        } else if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALMAST) {
            //SignalMast
            SignalMast sm = null;
            Block cB = _currentBlock;
            Block nB = _nextBlock;
            if (as == null) {
                as = _currentAllocatedSection;
            }
            // get signal mast at current block change, if there is no signal mast we will proceed with no change in speed
            // unless forceSpeedChange is true, such as beginning, resets of transit.
            // previous signal mast speed unless the mast is held.
            boolean weAreAtSpeedChangingMast=forceSpeedChange;
            if ( !forceSpeedChange  && nB != null ) {
                sm  = _lbManager.getFacingSignalMast(cB, nB);
                if (sm != null) {weAreAtSpeedChangingMast=true;}
            }

            while (sm == null && nB != null) {
                sm = _lbManager.getFacingSignalMast(cB, nB);
                if (sm == null) {
                    cB = nB;
                    nB = getNextBlock(nB, as);
                }
            }
            if (sm != null) {
                _controllingSignalMast = sm;
                _conSignalProtectedBlock = nB;
                sm.addPropertyChangeListener(_conSignalMastListener = (PropertyChangeEvent e) -> {
                    if (e.getPropertyName().equals("Aspect") || e.getPropertyName().equals("Held")) {
                        // controlling signal has changed appearance or a hold has been released
                        // even if its a hold we still have to use target speed etc else we override pauses and other stop events.
                        setSpeedBySignal();
                    }
                });
                _activeTrain.setControlingSignal(_controllingSignalMast, _controllingSignalMastPrev);
                log.debug("{}: new current signalmast {}({}) for section {}", _activeTrain.getTrainName(), sm.getDisplayName(USERSYS),
                        sm.getAspect(), as.getSection().getDisplayName(USERSYS));
                if ( weAreAtSpeedChangingMast ) {
                    setSpeedBySignal();
                }
            } // Note: null signal head will result when exiting throat-to-throat blocks.
            else {
                log.debug("{}: new current signalmast is null for section {} - sometimes OK", _activeTrain.getTrainName(),
                        as == null ? "Null" : as.getSection().getDisplayName(USERSYS));
            }
        } else {
            setSpeedBySignal();
        }
    }

    @CheckForNull
    private Block getNextBlock(Block b, AllocatedSection as) {
        //if (((_currentBlock == _activeTrain.getEndBlock()) && _activeTrain.getReverseAtEnd()
        //        && (as.getSequence() == _activeTrain.getEndBlockSectionSequenceNumber()))) {
        //    return _previousBlock;
        //}
        if ((_currentBlock == _activeTrain.getStartBlock())
                && _activeTrain.getResetWhenDone() && _activeTrain.isTransitReversed()
                && (as.getSequence() == _activeTrain.getStartBlockSectionSequenceNumber())) {
            return _previousBlock;
        }
        if (as.getNextSection() != null) {
            EntryPoint ep = as.getSection().getExitPointToSection(_nextSection, as.getDirection());
            if ((ep != null) && (ep.getBlock() == b)) {
                // this block is connected to a block in the next section
                return ep.getFromBlock();
            }
        }
        // this allocated section has multiple blocks _or_ there is no next Section
        Block blk = as.getSection().getEntryBlock();
        while (blk != null) {
            if (b == blk) {
                return as.getSection().getNextBlock();
            }
            blk = as.getSection().getNextBlock();
        }
        return null;
    }

    private void setNewCurrentSection(AllocatedSection as) {
        if (as.getSection() == _nextSection) {
            _previousAllocatedSection = _currentAllocatedSection;
            _currentAllocatedSection = as;
            _nextSection = as.getNextSection();
            TransitSection ts = as.getTransitSection();
            if (ts != null) {
                _autoTrainAction.addTransitSection(ts);
            }
            // written the long way for readability
            boolean nextSectionExpected = true;
            if (ts != null &&
                    ts.isSafe() &&
                    _activeTrain.getAllocateMethod() == ActiveTrain.ALLOCATE_BY_SAFE_SECTIONS) {
                nextSectionExpected = false;
            } else if (!_activeTrain.isAllocationReversed() &&
                    _activeTrain.getEndBlockSection() == _currentAllocatedSection.getSection()) {
                nextSectionExpected = false;
            } else if (_activeTrain.isAllocationReversed() &&
                    _activeTrain.getStartBlockSectionSequenceNumber() == _currentAllocatedSection.getSequence()) {
                nextSectionExpected = false;
            }
            log.debug("{}:Next Section Expected[{}]",_activeTrain.getActiveTrainName(),  nextSectionExpected);
            // NOw handled in SetSpeedBySignal()
            // check if new next Section exists but is not allocated to this train excepting above circumstances
            //if ( nextSectionExpected &&_nextSection != null && !_activeTrain.isInAllocatedList(_nextSection)) {
            //    // next section is not allocated to this train, must not enter it, even if signal is OK.
            //    log.warn("Stopping train [{}] in section [{}], as next section [{}] is not allocated",
            //            _activeTrain.getActiveTrainName(),_currentAllocatedSection.getSection().getDisplayName(USERSYS),_nextSection.getDisplayName(USERSYS));
            //    stopInCurrentSection(NO_TASK);
            //    _needSetSpeed = false;
            //}
            // see if we need to rescan as entering safe section.
            if (ts != null &&
                    ts.isSafe() &&
                    _activeTrain.getAllocateMethod() == ActiveTrain.ALLOCATE_BY_SAFE_SECTIONS) {
                InstanceManager.getDefault(DispatcherFrame.class).queueScanOfAllocationRequests();
            }

        }
    }

    // called by above or when resuming after stopped action
    protected synchronized void setSpeedBySignal() {
        log.trace("Set Speed by Signal");
        if (_pausingActive || ((_activeTrain.getStatus() != ActiveTrain.RUNNING)
                && (_activeTrain.getStatus() != ActiveTrain.WAITING)) || ((_controllingSignal == null)
                && InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALHEAD)
                || (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALMAST && (_controllingSignalMast == null
                || (_activeTrain.getStatus() == ActiveTrain.WAITING && !_activeTrain.getStarted())))
                || (_activeTrain.getMode() != ActiveTrain.AUTOMATIC)) {
            // train is pausing or not RUNNING or WAITING in AUTOMATIC mode, or no controlling signal,
            //   don't set speed based on controlling signal
            log.trace("Skip Set Speed By Signal");
            return;
        }
        // only bother to check signal if the next allocation is ours.
        if (checkAllocationsAhead()) {
            if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALHEAD) {
                setSpeedBySignalHead();
            } else if (InstanceManager.getDefault(DispatcherFrame.class)
                    .getSignalType() == DispatcherFrame.SIGNALMAST) {
                setSpeedBySignalMast();
            } else {
                log.trace("{}:Set Speed by BlocksAllocated",_activeTrain.getActiveTrainName());
                setSpeedBySectionsAllocated();
            }
        } else {
            // This might be the last section....
            if (_currentAllocatedSection.getNextSection() == null) {
                stopInCurrentSection(END_TRAIN);
            } else {
                // This will stop it.
                stopInCurrentSection(NO_TASK);
                log.debug("{}:Set Stop",_activeTrain.getActiveTrainName());
                waitingOnAllocation = true;  // flag setSpeedBySignal reuired when another allocation made.
            }
        }
    }

    /*
     * Check at least the next section is allocated
     */
    private boolean checkAllocationsAhead() {
        if (_nextSection != null) {
            // Check that next section is allocated...
            for (AllocatedSection allocatedSection : _activeTrain.getAllocatedSectionList()) {
                if (allocatedSection.getSection() == _nextSection) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setSpeedBySectionsAllocated() {
        if (_stoppingByBlockOccupancy && (_stoppingBlock != null && _stoppingBlock.getState() == Block.UNOCCUPIED)) {
            // we are awaiting a delayed stop
            return;
        }
        int sectionsAhead = 0;
        AllocatedSection as = null;
        for (AllocatedSection allocatedSection : _activeTrain.getAllocatedSectionList()) {
            if (allocatedSection.getSection() == _nextSection) {
                as = allocatedSection;
            }
            if (!allocatedSection.getEntered()) {
                sectionsAhead++;
            }
        }
        float newSpeed = 0.0f;
        log.debug("[{}:SectionsAhead[{}]",_activeTrain.getActiveTrainName() ,sectionsAhead);
        if (checkTurn(as)) {
            switch (sectionsAhead) {
                case 0:
                    newSpeed = 0.0f;
                    break;
                case 1:
                    newSpeed = InstanceManager.getDefault(SignalSpeedMap.class)
                            .getSpeed("Medium");
                    // .getSpeed(InstanceManager.getDefault(DispatcherFrame.class).getStoppingSpeedName());
                    _activeTrain.setStatus(ActiveTrain.RUNNING);
                    break;
                default:
                    newSpeed = InstanceManager.getDefault(SignalSpeedMap.class)
                            .getSpeed("Normal");
                    // .getSpeed(InstanceManager.getDefault(DispatcherFrame.class).getStoppingSpeedName());
                    _activeTrain.setStatus(ActiveTrain.RUNNING);
            }
            // If the train has no _currentAllocatedSection it is in a first block outside transit.
            if (_currentAllocatedSection != null ) {
                for (Block block : _currentAllocatedSection.getSection().getBlockList()) {
                    float speed = getSpeedFromBlock(block);
                    if (speed > 0 && speed < newSpeed) {
                        newSpeed = speed;
                    }
                }
            }
        }
        if (newSpeed > 0) {
            log.trace("setSpeedBySectionsAllocated isStopping[{}]",isStopping());
            cancelStopInCurrentSection();
            setTargetSpeed(getThrottleSettingFromSpeed(newSpeed));
        } else {
            stopInCurrentSection(NO_TASK);
        }
    }

    /**
     * Check that all turnouts in a section have finished setting
     * for passage. If not listens on first bad turnout
     * and rechecks when set.
     * @param as Allocated section whose turnouts need to be checked.
     * @return true if no errors else false
     */
    private boolean checkTurn(AllocatedSection as) {
        if (as != null && as.getAutoTurnoutsResponse() != null) {
            Turnout to = InstanceManager.getDefault(DispatcherFrame.class).getAutoTurnoutsHelper().checkStateAgainstList(as.getAutoTurnoutsResponse());
            if (to != null) {
                // at least one turnout isnt correctly set
                to.addPropertyChangeListener(_turnoutStateListener = (PropertyChangeEvent e) -> {
                    if (e.getPropertyName().equals("KnownState")) {
                        ((Turnout) e.getSource()).removePropertyChangeListener(_turnoutStateListener);
                        setSpeedBySignal();
                    }
                });
                return false;
            }
        }
        return true;
    }

    private void setSpeedBySignalMast() {
        //Set speed using SignalMasts;
        String displayedAspect = _controllingSignalMast.getAspect();
        if (log.isTraceEnabled()) {
            log.trace("{}: Controlling mast {} ({})", _activeTrain.getTrainName(), _controllingSignalMast.getDisplayName(USERSYS), displayedAspect);
            if (_conSignalProtectedBlock == null) {
                log.trace("{}: Protected block is null", _activeTrain.getTrainName());
            } else {
                log.trace("{}: Protected block: {} state: {} speed: {}", _activeTrain.getTrainName(),
                        _conSignalProtectedBlock.getSensor().getDisplayName(USERSYS),
                        (_conSignalProtectedBlock.getSensor().getState() == Block.OCCUPIED ? "OCCUPIED" : "NOT OCCUPIED"),
                        _conSignalProtectedBlock.getBlockSpeed());
            }
        }

        if ((_controllingSignalMast.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER).equals(displayedAspect))
                || !_controllingSignalMast.getLit() || _controllingSignalMast.getHeld()) {
            checkForSignalPassedOrStop(_controllingSignalMast.getDisplayName(USERSYS));
        } else if (_controllingSignalMast.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.PERMISSIVE) != null
                && _controllingSignalMast.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.PERMISSIVE).equals(displayedAspect)) {
            setTargetSpeedState(RESTRICTED_SPEED);
            _activeTrain.setStatus(ActiveTrain.RUNNING);
        } else {

            //if using signalmasts, set speed to lesser of aspect speed and signalmastlogic speed
            //  (minimum speed on the path to next signal, using turnout and block speeds)
            String aspectSpeedStr = (String) _controllingSignalMast.getSignalSystem().getProperty(displayedAspect, "speed");
            log.trace("{}: Signal {} speed {} for aspect {}", _activeTrain.getTrainName(), _controllingSignalMast.getDisplayName(USERSYS), aspectSpeedStr, displayedAspect);
            float speed = -1.0f;
            if (aspectSpeedStr != null) {
                try {
                    speed = Float.parseFloat(aspectSpeedStr);
                } catch (NumberFormatException nx) {
                    try {
                        speed = InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(aspectSpeedStr);
                        log.trace("{}: Signal {} speed from map for {} is {}", _activeTrain.getTrainName(), _controllingSignalMast.getDisplayName(USERSYS), aspectSpeedStr, speed);
                    } catch (IllegalArgumentException ex) {
                        //Considered Normal if the speed does not appear in the map
                        log.trace("{}: Speed not found {}", _activeTrain.getTrainName(), aspectSpeedStr);
                    }
                }
            }
            int aspectSpeed = (int) speed; //save for debug message

            //get maximum speed for the route between current and next signalmasts
            float smLogicSpeed = -1.0f;
            String smDestinationName = "unknown";
            SignalMastLogic smLogic = InstanceManager.getDefault(SignalMastLogicManager.class).getSignalMastLogic(_controllingSignalMast);
            if (smLogic != null) {
                SignalMast smDestination = smLogic.getActiveDestination();
                if (smDestination != null) {
                    smDestinationName = smDestination.getDisplayName(USERSYS);
                    smLogicSpeed = (int) smLogic.getMaximumSpeed(smDestination);
                }
            }

            //use the smaller of aspect speed or route speed
            if (smLogicSpeed > -1.0f && smLogicSpeed < speed) {
                speed = smLogicSpeed;
            }

            log.debug("{}: {}({}) {}({}), Dest: {}, path max: {}",
                    _activeTrain.getTrainName(),
                    _controllingSignalMast.getDisplayName(USERSYS), displayedAspect, aspectSpeedStr, aspectSpeed,
                    smDestinationName, (int) smLogicSpeed);

            if (speed > -1.0f) {
                /* We should work on the basis that the speed required in the current block/section is governed by the signalmast
                 that we have passed and not the one we are approaching when we are accelerating.
                 However when we are decelerating we should be aiming to meet the speed required by the approaching signalmast
                 whether that is to slow down or come to a complete stand still.
                 */
                if (prevSpeed == -1 || speed < prevSpeed) {
                    log.debug("{}: Signal {} setting speed to {} for next", _activeTrain.getTrainName(),
                            _controllingSignalMast.getDisplayName(USERSYS), speed);
                    setTargetSpeedValue(speed);
                } else {
                    log.debug("{}: Signal {} setting speed to {} for previous", _activeTrain.getTrainName(),
                            _controllingSignalMast.getDisplayName(USERSYS), speed);
                    setTargetSpeedValue(prevSpeed);
                }
                prevSpeed = speed;
                _activeTrain.setStatus(ActiveTrain.RUNNING);

            } else {
                log.warn("{}: No specific speeds found so will use the default", _activeTrain.getTrainName());
                setTargetSpeedState(NORMAL_SPEED);
                _activeTrain.setStatus(ActiveTrain.RUNNING);
            }
        }
    }

    private void setSpeedBySignalHead() {
        // a held signal always stop
        if ( _controllingSignal != null && _controllingSignal.getAppearance() == SignalHead.HELD ) {
            // Held - Stop
            stopInCurrentSection(NO_TASK);
            return;
        }

        if (useSpeedProfile) {
            // find speed from signal.
            // find speed from block
            // use least
            float blockSpeed = getSpeedFromBlock(_conSignalProtectedBlock);

            float signalSpeed;
            String signalSpeedName;
            String displayedAspect = _controllingSignal.getAppearanceName();
            try {
                signalSpeedName =
                        InstanceManager.getDefault(SignalSpeedMap.class).getAppearanceSpeed(displayedAspect);
                signalSpeed = InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(signalSpeedName);
            } catch (Throwable ex) { // if _anything_ goes wrong, contain it
                signalSpeed = -1.0f;
                log.warn("{}: Block {} AppearanceSpeed {} not found in SignalSpeedMap",
                        _activeTrain.getTrainName(), _conSignalProtectedBlock.getDisplayName(USERSYS), displayedAspect);
            }
            float useSpeed;
            if (blockSpeed < signalSpeed) {
                useSpeed = blockSpeed;
            } else {
                useSpeed = signalSpeed;
            }

            log.trace("BlockSpeed[{}] SignalSpeed[{}]", blockSpeed, signalSpeed);
            if (useSpeed < 0.01f) {
                checkForSignalPassedOrStop(_controllingSignal.getDisplayName(USERSYS));
            } else {
                setTargetSpeedByProfile(useSpeed);
            }
        } else {
            switch (_controllingSignal.getAppearance()) {
                case SignalHead.DARK:
                case SignalHead.RED:
                case SignalHead.FLASHRED:
                    // May get here from signal changing before Block knows it is occupied, so must
                    //      check Block occupancy sensor, which must change before signal.
                    // check to to see if its allocated to us!!!
                    //      check Block occupancy sensor if it is in an allocated block, which must change before signal
                    // If the train has no _currentAllocatedSection it is in a first block outside transit.
                    checkForSignalPassedOrStop(_controllingSignal.getDisplayName(USERSYS));
                    break;
                case SignalHead.YELLOW:
                case SignalHead.FLASHYELLOW:
                    setTargetSpeedState(SLOW_SPEED);
                    _activeTrain.setStatus(ActiveTrain.RUNNING);
                    break;
                case SignalHead.GREEN:
                case SignalHead.FLASHGREEN:
                    setTargetSpeedState(NORMAL_SPEED);
                    _activeTrain.setStatus(ActiveTrain.RUNNING);
                    break;
                case SignalHead.LUNAR:
                case SignalHead.FLASHLUNAR:
                    setTargetSpeedState(RESTRICTED_SPEED);
                    _activeTrain.setStatus(ActiveTrain.RUNNING);
                    break;
                default:
                    log.warn("Signal Head[{}] has invalid Appearence - using stop",_controllingSignal.getAppearance());
                    stopInCurrentSection(NO_TASK);
            }

        }
    }

    /**
     * Check to see if a stop is really required, or if this is the
     * signal head that was just passed, in which case ignore as the signal goes red before a
     * new signal exists.
     *
     * @param displayName name of signal for debug messages.
     */
    private void checkForSignalPassedOrStop(String displayName) {
        // if current section is null we are in a pre transit block.
        if (_currentAllocatedSection != null) {
            if ((_currentAllocatedSection.isInActiveBlockList(_conSignalProtectedBlock) ||
                    (_nextSection != null && _activeTrain.isInAllocatedList(_nextSection) && _nextSection.containsBlock(_conSignalProtectedBlock)))
                    && _conSignalProtectedBlock.getSensor().getState() == Block.OCCUPIED) {
                // Train has just passed this signal - ignore this signal
                log.debug("{}: _conSignalProtectedBlock [{}] for signal [{}] is the block just past so ignore.", _activeTrain.getTrainName(),
                        _conSignalProtectedBlock.getDisplayName(USERSYS), displayName);
            } else {
                log.debug("{}: stopping for signal [{}] ", _activeTrain.getTrainName(),
                         displayName);
                stopInCurrentSection(NO_TASK);
            }
        }
    }

    protected float getSpeedFromBlock(Block block) {
        String blockSpeedName = block.getBlockSpeed();
        if (blockSpeedName.contains("Global")) {
            blockSpeedName = InstanceManager.getDefault(BlockManager.class).getDefaultSpeed();
        }
        float blockSpeed = -1.0f;
        if (!blockSpeedName.isEmpty()) {
            try {
                blockSpeed = Float.parseFloat(blockSpeedName);
            } catch (NumberFormatException nx) {
                try {
                    blockSpeed = InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(blockSpeedName);
                    log.debug("{} {}: block speed from map for {} is {}",
                            _activeTrain.getTrainName(), block.getDisplayName(USERSYS), blockSpeedName,
                            blockSpeed);
                } catch (Throwable ex) { // if _anything_ goes wrong, contain it
                    //Considered Normal if the speed does not appear in the map
                    log.warn("{}: Block {} Speed {} not found in SignalSpeedMap",
                            _activeTrain.getTrainName(), block.getDisplayName(USERSYS), blockSpeed);
                }
            }
        }
        return blockSpeed;
    }

    float prevSpeed = -1.0f;

    // called to cancel a stopping action that is in progress
    private synchronized void cancelStopInCurrentSection() {
        log.trace("[{}]:Cancel Stopping", _activeTrain.getTrainName());
        cancelStoppingBySensor();
        _stoppingByBlockOccupancy = false;
        _stoppingBlock = null;
        _stoppingUsingSpeedProfile = false;
        _stoppingBlock = null;
        _autoEngineer.slowToStop(false);
    }

    private synchronized void stopInCurrentSection(int task) {
        if (_currentAllocatedSection == null) {
            log.error("{}: Current allocated section null on entry to stopInCurrentSection", _activeTrain.getTrainName());
            setStopNow();
            return;
        }
        log.debug("{}: StopInCurrentSection called for {} task[{}] targetspeed[{}]", _activeTrain.getTrainName(), _currentAllocatedSection.getSection().getDisplayName(USERSYS),task,getTargetSpeed());
        if (getTargetSpeed() == 0.0f || isStopping()) {
            log.debug("{}: train is already stopped or stopping.", _activeTrain.getTrainName());
            // ignore if train is already stopped or if stopping is in progress
            return;
        }
        // if Section has stopping sensors, use them
        if (_currentAllocatedSection.getSection().getState() == Section.FORWARD) {
            _stopSensor = _currentAllocatedSection.getSection().getForwardStoppingSensor();
        } else {
            _stopSensor = _currentAllocatedSection.getSection().getReverseStoppingSensor();
        }
        if (_stopSensor != null && _useStopSensor) {
            if (_stopSensor.getKnownState() == Sensor.ACTIVE) {
                // stop sensor is already active, stop now
                setStopNow();
            } else {
                setDecreasedSpeedBeforeStop();
                _stopSensor.addPropertyChangeListener(_stopSensorListener = (java.beans.PropertyChangeEvent e) -> {
                    handleStopSensorChange(e);
                });
                _stoppingBySensor = true;
            }
        } else if (_useSpeedProfile && _stopBySpeedProfile) {
            log.debug("{}: Section [{}] Section Length[{}] Max Train Length [{}] StopBySpeedProfile [{}]. setStopNow", _activeTrain.getTrainName(),
                    _currentAllocatedSection.getSection().getDisplayName(USERSYS), _currentAllocatedSection.getLength(), _maxTrainLength, _stopBySpeedProfile);
            // stopping by speed profile uses section length to stop
            setTargetSpeedState(STOP_SPEED,useSpeedProfile);
        } else if (_currentAllocatedSection.getLength()  < _maxTrainLength) {
            log.debug("{}: Section [{}] Section Length[{}] Max Train Length [{}]. setStopNow({})",
                    _activeTrain.getTrainName(),
                    _currentAllocatedSection.getSection().getDisplayName(USERSYS),
                    _currentAllocatedSection.getLength(),
                    _maxTrainLength, _stopBySpeedProfile);
            // train will not fit comfortably in the Section, stop it immediately
            setStopNow();
        } else if (_activeTrain.getTrainDetection() == TrainDetection.TRAINDETECTION_WHOLETRAIN) {
            log.debug("{}: train will fit in [{}] ({}>={}), stop when prev block clears.", _activeTrain.getTrainName(),
                    _currentAllocatedSection.getSection().getDisplayName(USERSYS), _currentAllocatedSection.getLength(), _maxTrainLength);
            // train will fit in current allocated Section and has resistance wheels
            // try to stop by watching Section Block occupancy
            if (_currentAllocatedSection.getSection().getNumBlocks() == 1) {
                if (_previousAllocatedSection != null) {
                    Block tBlock;
                    // just because current section has one block does not mean the previous one did.
                    if (_previousAllocatedSection.getSection().getNumBlocks() == 1) {
                       tBlock = _previousAllocatedSection.getSection().getLastBlock();
                    } else {
                       tBlock = _previousAllocatedSection.getSection().getExitBlock();
                    }
                    if ((tBlock != null) && (tBlock.getState() == Block.OCCUPIED)) {
                        _stoppingBlock = tBlock;
                        setStopByBlockOccupancy(false);
                    } else {
                        setStopNow();
                    }
                } else {
                    setStopNow();
                }
            } else {
                // Section has multiple blocks
                Block exitBlock = _currentAllocatedSection.getExitBlock();
                Block enterBlock = _currentAllocatedSection.getEnterBlock(_previousAllocatedSection);
                if (enterBlock == null) {
                    // this is the first Section of the Transit, with train starting in this Section
                    setStopNow();
                } else if (exitBlock == enterBlock) {
                    // entry and exit are from the same Block
                    if ((_previousBlock != null) && (_previousBlock.getState() == Block.OCCUPIED)
                            && (getBlockLength(exitBlock) > _maxTrainLength)) {
                        _stoppingBlock = _previousBlock;
                        setStopByBlockOccupancy(false);
                    } else {
                        setStopNow();
                    }
                } else {
                    // try to move train as far into the Section as it will comfortably fit
                    Block tstBlock = exitBlock;
                    if (tstBlock == null) {
                        if (_currentAllocatedSection.getDirection() == Section.REVERSE) {
                            tstBlock = _currentAllocatedSection.getSection().getBlockBySequenceNumber(0);
                        } else {
                            tstBlock = _currentAllocatedSection.getSection().getBlockBySequenceNumber(
                                    _currentAllocatedSection.getSection().getNumBlocks() - 1);
                        }
                    }
                    int tstLength = getBlockLength(tstBlock);
                    int tstBlockSeq = _currentAllocatedSection.getSection().getBlockSequenceNumber(tstBlock);
                    while ((tstLength < _maxTrainLength) && (tstBlock != enterBlock)) {
                        int newSeqNumber;
                        if (_currentAllocatedSection.getDirection() == Section.REVERSE) {
                            newSeqNumber = tstBlockSeq + 1;
                        } else {
                            newSeqNumber = tstBlockSeq - 1;
                        }
                        tstBlock = _currentAllocatedSection.getSection().getBlockBySequenceNumber(newSeqNumber);
                        tstBlockSeq = newSeqNumber;
                        tstLength += getBlockLength(tstBlock);
                    }
                    if (_maxTrainLength > tstLength) {
                        setStopNow();
                    } else if (tstBlock == enterBlock) {
                        // train fits, but needs all available Blocks
                        Block previousSectionExitBlock = _previousAllocatedSection.getExitBlock();
                        if ((previousSectionExitBlock != null) && (previousSectionExitBlock.getState() == Block.OCCUPIED)) {
                            _stoppingBlock = previousSectionExitBlock;
                            setStopByBlockOccupancy(true);
                        } else {
                            setStopNow();
                        }
                    } else {
                        // train fits, and doesn't need all available Blocks
                        int xSeqNumber = tstBlockSeq + 1;
                        if (_currentAllocatedSection.getDirection() == Section.FORWARD ) {
                            xSeqNumber = tstBlockSeq - 1;
                        }
                        _stoppingBlock = _currentAllocatedSection.getSection().
                                getBlockBySequenceNumber(xSeqNumber);
                        setStopByBlockOccupancy(true);
                    }
                }
            }
        } else {
            // train will fit, but no way to stop it reliably
            setStopNow();
        }
        // even if no task is required it must be run
        // as cleanup happens after train stops.
        Runnable waitForStop = new WaitForTrainToStop(task);
        Thread tWait = jmri.util.ThreadingUtil.newThread(waitForStop, "Wait for stop " + getActiveTrain().getActiveTrainName());
        tWait.start();
    }

    protected synchronized void executeStopTasks(int task) {
        // clean up stopping
        cancelStopInCurrentSection();
        log.trace("exec[{}]",task);
        switch (task) {
            case END_TRAIN:
                _activeTrain.setStatus(ActiveTrain.DONE);
                break;
            case NO_TASK:
                // clean up stop
                break;
            case END_REVERSAL:
                /* Reset _previousBlock to be the _currentBlock if we do a continious reverse otherwise the stop in block method fails
                to stop the loco in the correct block
                 if the first block we come to has a stopped or held signal */
                _activeTrain.setRestart(_activeTrain.getDelayReverseRestart(),_activeTrain.getReverseRestartDelay(),
                        _activeTrain.getReverseRestartSensor(),_activeTrain.getResetReverseRestartSensor());
                _activeTrain.setTransitReversed(true);
                _activeTrain.reverseAllAllocatedSections();
                setEngineDirection();
                _previousBlock = null;
                _nextBlock = getNextBlock(_currentBlock,_currentAllocatedSection);
                if (_activeTrain.getDelayReverseRestart() == ActiveTrain.NODELAY) {
                   _activeTrain.holdAllocation(false);
                    // a reversal can happen in mid section
                    setupNewCurrentSignal(_currentAllocatedSection, true);
                    setSpeedBySignal();
                    if ((_nextSection != null) && !_activeTrain.isInAllocatedList(_nextSection)) {
                        InstanceManager.getDefault(DispatcherFrame.class).queueScanOfAllocationRequests();
                        break;
                    }
                }
                break;
            case BEGINNING_RESET:
                _activeTrain.setRestart(_activeTrain.getDelayedRestart(),_activeTrain.getRestartDelay(),
                        _activeTrain.getRestartSensor(),_activeTrain.getResetRestartSensor());
                if (_activeTrain.getResetWhenDone()) {
                    if (_activeTrain.getDelayedRestart() == ActiveTrain.NODELAY && !_activeTrain.getReverseAtEnd()) {
                        log.error("[{}]: train is continueing without pause, should have been handled in handleBlockStateChange.",_activeTrain.getTrainName());
                    } else {
                        // then active train is delayed
                        _activeTrain.setTransitReversed(false);
                        _activeTrain.resetAllAllocatedSections();
                        _previousBlock = null;
                        _nextBlock = getNextBlock(_currentBlock,_currentAllocatedSection);
                        setEngineDirection();
                        _activeTrain.setRestart(_activeTrain.getDelayedRestart(),_activeTrain.getRestartDelay(),
                                _activeTrain.getRestartSensor(), _activeTrain.getResetRestartSensor());
                        if ((_nextSection != null) && !_activeTrain.isInAllocatedList(_nextSection)) {
                            InstanceManager.getDefault(DispatcherFrame.class).queueScanOfAllocationRequests();
                        }
                        // can be mid block
                        setupNewCurrentSignal(null, true);
                        setSpeedBySignal();

                    }
                } else {
                    // dispatcher cancelled auto restart while train was stopping?
                    log.warn("[{}]resetWhenDone flag reset, likely user cancelling while processing stop",
                            _activeTrain.getActiveTrainName());
                }
                break;
            default:
                log.debug("[{}]Invalid action [{}] in executeStopTasksRequest to execute BEGINNING_RESET cancelled", _activeTrain.getActiveTrainName(),task);
                break;
        }
    }

    /**
     * Remove the stopping sensor
     */
    private void cancelStoppingBySensor() {
        if (_stopSensor != null) {
            _stopSensor.removePropertyChangeListener(_stopSensorListener);
            _stoppingBySensor = false;
            _stopSensorListener = null;
            _stopSensor = null;
        }
    }

    /**
     * When the stopping sensor we are waiting on goes active
     * stop the train or set a new speed and destroy itself
     * @param e  - the property change event
     */
    private synchronized void handleStopSensorChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("KnownState") && (int) e.getNewValue() == Sensor.ACTIVE) {
            _stopSensor.removePropertyChangeListener(_stopSensorListener);
            _stoppingBySensor = false;
            _stopSensorListener = null;
            _stopSensor = null;
            if (_needSetSpeed) {
                _needSetSpeed = false;
                setSpeedBySignal();
            } else {
                setStopNow();
            }
        }
    }

    private synchronized void setStopNow() {
        setStopNow(false);
        }

    private synchronized void setStopNow(boolean useSpeedProfile) {
        setTargetSpeedState(STOP_SPEED,useSpeedProfile);
        if (_currentAllocatedSection == null) {  // this may occur if the train is not in the selected block when initially created and the signal is held.
            _activeTrain.setStatus(ActiveTrain.WAITING);
        } else if (_currentAllocatedSection.getNextSection() == null) {
            // wait for train to stop - this lets action items complete in a timely fashion
            waitUntilStopped();
            _activeTrain.setStatus(ActiveTrain.DONE);
        } else {
            _activeTrain.setStatus(ActiveTrain.WAITING);
        }
    }

    /*
     * When multi block stopping, the stopping block may not be occupied yet.
     */
    private void setStopByBlockOccupancy(boolean ignoreNotOccupied) {
        // note: _stoppingBlock must be set before invoking this method
        //  verify that _stoppingBlock is actually occupied, if not stop immed
        if (_stoppingBlock.getState() == Block.OCCUPIED || ignoreNotOccupied) {
            setDecreasedSpeedBeforeStop();
            _stoppingByBlockOccupancy = true;
        } else {
            setStopNow();
        }
    }

    /**
     * Before stopping by sensor alone, or by clearing previous block,
     * set the speed to the user defined preference.
     */
    private void setDecreasedSpeedBeforeStop() {
        float signalSpeed = 25;
        try {
            signalSpeed = InstanceManager.getDefault(SignalSpeedMap.class)
                    .getSpeed(InstanceManager.getDefault(DispatcherFrame.class).getStoppingSpeedName());
        } catch (IllegalArgumentException ex) {
            log.error("Missing [{}] from Speed table - defaulting to 25",
                    InstanceManager.getDefault(DispatcherFrame.class).getStoppingSpeedName());
        }
        setToAMaximumThrottle(getThrottleSettingFromSpeed(signalSpeed));
    }

    /**
     * Sets the throttle percent unless it is already less than the new setting
     * @param throttleSetting  Max ThrottleSetting required.
     */
    private synchronized void setToAMaximumThrottle(float throttleSetting) {
        if (throttleSetting < getTargetSpeed()) {
            setTargetSpeed(throttleSetting);
        }
    }

    /**
     * Calculates the throttle setting for a given speed.
     * @param speed  the unadjusted speed.
     * @return - throttle setting (a percentage)
     */
    private synchronized float getThrottleSettingFromSpeed(float speed) {
        if (useSpeedProfile) {
            float throttleSetting = _activeTrain.getRosterEntry().getSpeedProfile()
                    .getThrottleSettingFromSignalMapSpeed(speed, getForward());
            return applyMaxThrottleAndFactor(throttleSetting);
        }
        if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALMAST) {
            float mls;
            if (_controllingSignalMast != null) {
                mls = _controllingSignalMast.getSignalSystem().getMaximumLineSpeed();
            } else {
                //plan B
                mls = InstanceManager.getDefault(DispatcherFrame.class).getMaximumLineSpeed();
            }
            float throttleSetting = (speed / mls);
            return applyMaxThrottleAndFactor(throttleSetting);
        } else {
            return applyMaxThrottleAndFactor(speed/100.0f);
        }
    }

    /**
     *
     * @param throttleSetting the throttle setting that would normally be set
     * @return the adjusted throttle setting after applying Max Throttle and Percentage throttle settings
     */
    private synchronized float applyMaxThrottleAndFactor(float throttleSetting) {
        if (throttleSetting > 0.0f) {
            if (throttleSetting > _maxSpeed) {
                return _maxSpeed * _speedFactor;
            }
            return (throttleSetting * _speedFactor); //adjust for train's Speed Factor
        } else {
            return throttleSetting;
        }
    }

    /**
     * sets the throttle based on an index number into _speedRatio array
     * @param speedState  Index value
     */
    private synchronized void setTargetSpeedState(int speedState) {
        setTargetSpeedState(speedState,false);
    }

    /**
     * sets the throttle based on an index number into _speedRatio array
     * @param speedState  Index value
     * @param stopBySpeedProfile if true use speed profile
     */
    private synchronized void setTargetSpeedState(int speedState,boolean stopBySpeedProfile) {
        log.trace("{}: setTargetSpeedState:({})",_activeTrain.getTrainName(),speedState);
        _autoEngineer.slowToStop(false);
        if (speedState > STOP_SPEED) {
            cancelStopInCurrentSection();
            if (_currentRampRate == RAMP_SPEEDPROFILE && _useSpeedProfile) {
                // we are going to ramp up  / down using section length and speed profile
                _autoEngineer.setTargetSpeed(_currentAllocatedSection.getLengthRemaining(_currentBlock) * _stopBySpeedProfileAdjust, speedState);
            } else {
                setTargetSpeed(applyMaxThrottleAndFactor(_speedRatio[speedState]));
            }
        } else if (stopBySpeedProfile) {
            // we are going to stop by profile
            _stoppingUsingSpeedProfile = true;
            _autoEngineer.setTargetSpeed(_currentAllocatedSection.getLengthRemaining(_currentBlock) * _stopBySpeedProfileAdjust, 0.0f);
        } else {
            _autoEngineer.setHalt(true);
            setTargetSpeed(0.0f);
        }
    }

    private synchronized void setTargetSpeedByProfile(float speedState) {
        // the speed comes in as units of warrents (mph, kph, mm/s etc)
            try {
                float throttleSetting = _activeTrain.getRosterEntry().getSpeedProfile().getThrottleSettingFromSignalMapSpeed(speedState, getForward());
                log.debug("{}: setTargetSpeedByProfile: {} SpeedState[{}]",
                        _activeTrain.getTrainName(),
                        throttleSetting,
                        speedState);
                if (throttleSetting > 0.009 && _currentRampRate != RAMP_SPEEDPROFILE && _useSpeedProfile) {
                    cancelStopInCurrentSection();
                    setTargetSpeed(applyMaxThrottleAndFactor(throttleSetting)); // apply speed factor and max
                } else if (throttleSetting > 0.009) {
                    cancelStopInCurrentSection();
                    _autoEngineer.setTargetSpeed(_currentAllocatedSection.getLengthRemaining(_currentBlock)  * _stopBySpeedProfileAdjust , throttleSetting);
                } else if (useSpeedProfile && _stopBySpeedProfile) {
                    setTargetSpeed(0.0f);
                    _stoppingUsingSpeedProfile = true;
                    _autoEngineer.setTargetSpeed(_currentAllocatedSection.getLengthRemaining(_currentBlock)  * _stopBySpeedProfileAdjust, 0.0f);
                } else {
                    _autoEngineer.slowToStop(false);
                    setTargetSpeed(0.0f);
                    _autoEngineer.setHalt(true);
                }
            } catch (Exception ex) {
                log.error("setTargetSpeedByProfile crashed - Emergency Stop: ", ex );
                _autoEngineer.slowToStop(false);
                setTargetSpeed(-1.0f);
                _autoEngineer.setHalt(true);
            }
        }

    /**
     * Pass in speed as shown on dialogs, and convert to decimal speed needed by
     * throttle.
     */
    private synchronized void setTargetSpeedValue(float speed) {
        log.debug("{}: setTargetSpeedValue: Speed[{}]",_activeTrain.getTrainName(),speed);
        if (useSpeedProfile) {
            setTargetSpeedByProfile(speed);
            return;
        }
        _autoEngineer.slowToStop(false);
        float mls;
        if (_controllingSignalMast != null) {
            mls = _controllingSignalMast.getSignalSystem().getMaximumLineSpeed();
        } else {
            mls = InstanceManager.getDefault(DispatcherFrame.class).getMaximumLineSpeed();
        }
        float decSpeed = (speed / mls);
        if (decSpeed > 0.0f) {
            cancelStopInCurrentSection();
            setTargetSpeed(applyMaxThrottleAndFactor(decSpeed));
        } else {
            setTargetSpeed(0.0f);
            _autoEngineer.setHalt(true);
        }
    }

    private int getBlockLength(Block b) {
        if (b == null) {
            return (0);
        }
        float fLength = b.getLengthMm() / (float) InstanceManager.getDefault(DispatcherFrame.class).getScale().getScaleFactor();
        if (InstanceManager.getDefault(DispatcherFrame.class).getUseScaleMeters()) {
            return (int) (fLength * 0.001f);
        }
        return (int) (fLength * 0.00328084f);
    }

    /**
     * Initiates running in manual mode with external throttle.
     * <p>
     * This method is triggered by an action in the Transit. The throttle in use
     * for automatic operation is dispatched.
     */
    protected void initiateWorking() {
        if (_activeTrain.getStatus() != ActiveTrain.WORKING) {
            _activeTrain.setMode(ActiveTrain.DISPATCHED);
            _activeTrain.setStatus(ActiveTrain.WORKING);
            saveSpeedAndDirection();
            if (_autoEngineer != null) {
                _autoEngineer.setHalt(true);
                waitUntilStopped();
                _autoEngineer.abort();
                InstanceManager.throttleManagerInstance().releaseThrottle(_throttle, this);
                _autoEngineer = null;
                _throttle = null;
            }
        }
    }

    /**
     * Returns when train is stopped.
     * <p>
     * Note: Provides for _autoEngineer becoming null during wait Ties up the
     * current autoActiveTrain thread.
     */
    protected void waitUntilStopped() {
        boolean doneWaiting = false;
        while (!doneWaiting) {
            if (_autoEngineer != null) {
                doneWaiting = _autoEngineer.isStopped();
            } else {
                doneWaiting = true;
            }
            if (!doneWaiting) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // ignore this exception
                }
            }
        }
    }

    /**
     * Resumes automatic running after a working session using an external
     * throttle This method is triggered by the dispatcher hitting the "Resume
     * Auto Running" button A new throttle is acquired to allow automatic
     * running to resume
     */
    protected void resumeAutomaticRunning() {
        if ((_activeTrain.getStatus() == ActiveTrain.WORKING)
                || (_activeTrain.getStatus() == ActiveTrain.READY)) {
            _autoTrainAction.cancelDoneSensor();
            if (initialize()) {
                _resumingAutomatic = true;
            } else {
                log.error("Failed to initialize throttle when resuming automatic mode.");
            }
        }
    }

    /**
     * Pause the auto active train for a specified number of fast clock minutes.
     *
     * @param fastMinutes the number of minutes to pause the train
     * @return the thread waiting on the pause or null if already paused
     */
    public Thread pauseTrain(int fastMinutes) {
        if (_pausingActive) {
            // if a pause train thread is currently active, ignore this call
            return (null);
        }
        Runnable pauseTrain = new PauseTrain(fastMinutes);
        Thread tPause = jmri.util.ThreadingUtil.newThread(pauseTrain, "pause train " + _activeTrain.getTrainName());
        tPause.start();
        return tPause;
    }

    public void terminate() {
        // here add code to stop the train and release its throttle if it is in autoRun
        while (_activeHornThreads > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // ignore this exception
            }
        }
        _autoTrainAction.clearRemainingActions();
        if (_autoEngineer != null) {
            _autoEngineer.setHalt(true);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // ignore this exception
            }
            waitUntilStopped();
            _autoEngineer.abort();
            InstanceManager.throttleManagerInstance().releaseThrottle(_throttle, this);
        }
    }

    public void dispose() {
        if (_controllingSignalMast != null && _conSignalMastListener != null) {
            _controllingSignalMast.removePropertyChangeListener(_conSignalMastListener);
        }
        _controllingSignalMast = null;
        _conSignalMastListener = null;
    }

// _________________________________________________________________________________________
    // This class waits for train stop in a separate thread
    class WaitForTrainToStop implements Runnable {

        public WaitForTrainToStop(int task) {
            _task = task;
        }

        @Override
        public void run() {
            boolean waitingOnTrain = true;
            try {
                while (waitingOnTrain) {
                    if ((getAutoEngineer() != null) && (getAutoEngineer().isStopped())) {
                        waitingOnTrain = false;
                    } else {
                        Thread.sleep(_delay);
                    }
                }
                log.trace("executing task[{}]",_task);
                executeStopTasks(_task);
            } catch (InterruptedException e) {
                log.warn("Waiting for train to stop interrupted - stop tasks not executing");
            } catch (Exception e) {
                log.error("Waiting for train to stop crashed - stop tasks not executing.", e);
            }
        }

        private final int _delay = 91;
        private int _task = 0;
    }

    /**
     * Pause the train in a separate thread. Train is stopped, then restarted
     * after specified number of fast Minutes have elapsed.
     */
    class PauseTrain implements Runnable {
        /**
         * Create a PauseTrain
         *
         * @param fastMinutes the number of fast clock minutes to pause the
         *                    train
         */
        public PauseTrain(int fastMinutes) {
            _fastMinutes = fastMinutes;
        }

        @Override
        public void run() {
            // set to pause at a fast ramp rate
            _pausingActive = true;
            _savedTargetSpeed = getTargetSpeed();
            _savedRampRate = getRampRate();
            setCurrentRampRate(RAMP_FAST);
            stopInCurrentSection(NO_TASK);
            // wait for train to stop
            boolean waitNow = true;
            boolean keepGoing = true;
            while (waitNow) {
                try {
                    Thread.sleep(101);
                    if (_autoEngineer != null) {
                        if (_autoEngineer.isStopped()) {
                            waitNow = false;
                        }
                    } else {
                        waitNow = false;
                    }
                } catch (InterruptedException e) {
                    log.trace("InterruptedException while waiting to stop for pause-indicates action cancelled.", e);
                    waitNow = false;
                    keepGoing = false;
                }
            }
            _activeTrain.setStatus(ActiveTrain.PAUSED);
            if (keepGoing) {
                // wait for specified fast clock time
                Timebase _clock = InstanceManager.getDefault(jmri.Timebase.class);
                java.beans.PropertyChangeListener _clockListener = (java.beans.PropertyChangeEvent e) -> {
                    _fastMinutes--;
                };
                _clock.addMinuteChangeListener(_clockListener);
                // wait for fast minutes to tick away
                waitNow = true;
                while (waitNow) {
                    try {
                        Thread.sleep(501);
                        if (_fastMinutes <= 0) {
                            waitNow = false;
                        }
                    } catch (InterruptedException e) {
                        log.trace("InterruptedException indicates action cancelled.", e);
                        keepGoing = false;
                    }
                }
                _clock.removeMinuteChangeListener(_clockListener);
            }
            _pausingActive = false;
            if (keepGoing) {
                // this thread was not interrupted
                //   resume running - restore speed, status, and ramp rate
                setCurrentRampRate(_savedRampRate);
                setTargetSpeed(_savedTargetSpeed);
                _activeTrain.setStatus(ActiveTrain.RUNNING);
                setSpeedBySignal();
            }
        }
        private int _fastMinutes = 0;
        private float _savedTargetSpeed = 0.0f;
        private int _savedRampRate = RAMP_NONE;
    }

    // _________________________________________________________________________________________
    // this class handles the interface with the throttle
    // (This class started from code by Pete Cressman contained in Warrant.java.)
    class AutoEngineer  {

        AutoEngineer(DccThrottle throttle, RosterEntry rosterEntry) {
            this.throttle = throttle;
            this.rosterEntry = rosterEntry;
        }

        private DccThrottle throttle;
        private int ramping;
        private boolean speedProfileStoppingIsRunning = false;
        private float speedIncrement = 0.0f; //will be recalculated
        private float targetSpeed;
        private RosterEntry rosterEntry;
        private int throttleInterval;

        public void setRamping(int ramping, int fullRampTime, int minThrottleInterval, int rampRate) {
            this.ramping = ramping;
            this.throttleInterval = minThrottleInterval;
            //calculate speed increment to use in each minInterval time
            speedIncrement = (100.0f / ((float) fullRampTime / minThrottleInterval)
                    / rampRate) / 100.0f;
            log.debug("{}: _speedIncrement={}", throttle.getLocoAddress(), speedIncrement);
        }

        public  void setIsForward(boolean isForward) {
            throttle.setIsForward(isForward);
        }

        public boolean getIsForward() {
            return(throttle.getIsForward());
        }

        public void setTargetSpeed(float speed) {
            log.debug("Set TargetSpeed[{}]",speed);
            stopAllTimers();
            targetSpeed = speed;
            if (ramping == RAMP_NONE || ramping == RAMP_SPEEDPROFILE ) {
                throttle.setSpeedSetting(speed);
            } else {
                rampToTarget();
            }
        }

        public float getTargetSpeed(){
            return(targetSpeed);
        }

        /**
         * Flag from user's control.
         *
         * @param halt true to immediately stop the train; false otherwise
         */
        public void setHalt(boolean halt) {
            if (halt) {
                this.setSpeedImmediate(0.0f);
            }
        }

        public void setTargetSpeed(float distance, float speed) {
            log.debug("Set Target Speed[{}] with distance{{}] from speed[{}]",speed,distance,throttle.getSpeedSetting());
            stopAllTimers();
            if (rosterEntry != null) {
                rosterEntry.getSpeedProfile().setExtraInitialDelay(1500f);
                rosterEntry.getSpeedProfile().changeLocoSpeed(_throttle, distance, speed);
                speedProfileStoppingIsRunning = true;
                targetSpeed = speed;
            } else {
                setTargetSpeed((0.0f));
            }
        }

        public void slowToStop(boolean on) {
            stopAllTimers();
            if (on) {
                log.debug("SlowToStopOn");
                setTargetSpeed((0.0f));
            }
        }

        public void stopAllTimers() {
            if (speedProfileStoppingIsRunning) {
                re.getSpeedProfile().cancelSpeedChange();
                speedProfileStoppingIsRunning = false;
            }
            if (rampingTimer != null) {
                rampingTimer.stop();
                rampingTimer = null;
            }
        }

        LinkedList<SpeedSetting> stepQueue;
        private javax.swing.Timer rampingTimer;

        private void rampToTarget() {
            log.debug("RampToTarget[{}]current[{}]", getTargetSpeed(), throttle.getSpeedSetting());
            stepQueue = new LinkedList<>();
            if (throttle.getSpeedSetting() <= getTargetSpeed()) {
                // Up
                float newSpeed = throttle.getSpeedSetting();
                while (newSpeed < getTargetSpeed()) {
                    newSpeed += speedIncrement;
                    if (newSpeed > getTargetSpeed()) {
                        newSpeed = getTargetSpeed();
                    }
                    log.trace("NewSpeedUp[{}]",newSpeed);
                    stepQueue.add(new SpeedSetting(newSpeed, throttleInterval));
                }
            } else {
                // Down
                    float newSpeed = throttle.getSpeedSetting();
                    while (newSpeed > getTargetSpeed()) {
                        newSpeed -= speedIncrement;
                        if (newSpeed < getTargetSpeed()) {
                            newSpeed = getTargetSpeed();
                        }
                        log.trace("NewSpeedDown[{}]",newSpeed);
                        stepQueue.add(new SpeedSetting(newSpeed, throttleInterval));
                    }
            }
            if (rampingTimer == null) { //If this is the first time round then kick off the speed change
                setNextStep();
            }
        }

        private void finishChange() {
            if (rampingTimer != null) {
                rampingTimer.stop();
            }
            rampingTimer = null;
            stepQueue.clear();
            stepQueue = null;
        }

        synchronized void setNextStep() {
                if (stepQueue.isEmpty()) {
                    log.trace("Empty");
                    finishChange();
                    return;
                }
                SpeedSetting ss = stepQueue.getFirst();
                if (ss.getDuration() == 0) {
                    log.trace("Duratiom Zero");
                    finishChange();
                    return;
                }
                stepQueue.removeFirst();
                log.trace("Set New Speed[{}]",ss.getSpeedStep());
                throttle.setSpeedSetting(ss.getSpeedStep());
                rampingTimer = new javax.swing.Timer(ss.getDuration(), (java.awt.event.ActionEvent e) -> {
                    setNextStep();
                });
                rampingTimer.setRepeats(false);
                rampingTimer.start();
            }

        private class SpeedSetting {

            float step = 0.0f;
            int duration = 0;

            SpeedSetting(float step, int duration) {
                this.step = step;
                this.duration = duration;
            }

            float getSpeedStep() {
                return step;
            }

            int getDuration() {
                return duration;
            }
        }

        /**
         * Set the train speed directly, bypassing ramping.
         *
         * @param speed 0.0 (stop) to 1.0 (full)
         */
        public synchronized void setSpeedImmediate(float speed) {
            log.trace("{}: setting speed directly to {}%", _activeTrain.getTrainName(), (int) (speed * 100));
            stopAllTimers();
            targetSpeed = speed;
            throttle.setSpeedSetting(targetSpeed);
        }

        /**
         * Check if train is moving or stopped.
         *
         * @return true if stopped; false otherwise
         */
        public synchronized boolean isStopped() {
            // when stopping by speed profile you must refresh the throttle speed.
            return throttle.getSpeedSetting() <= 0.0004f;
        }

        /**
         * Check if train is moving at its current requested speed.
         *
         * @return true if at requested speed; false otherwise
         */
        public synchronized boolean isAtSpeed() {
            return java.lang.Math.abs(throttle.getSpeedSetting() - targetSpeed) <= 0.01;
        }

        /**
         * Flag from user to end run.
         */
        public void abort() {
            stopAllTimers();
        }

        protected void setFunction(int cmdNum, boolean isSet) {
            throttle.setFunction(cmdNum, isSet);
        }
    }

    /**
     * Convert ramp rate name, stored as a string into the constant value
     * assigned.
     *
     * @param rampRate  name of ramp rate, such as "RAMP_FAST"
     * @return integer representing a ramprate constant value
     */
    public static int getRampRateFromName(String rampRate) {
        if (rampRate.equals(Bundle.getMessage("RAMP_FAST"))) {
            return RAMP_FAST;
        } else if (rampRate.equals(Bundle.getMessage("RAMP_MEDIUM"))) {
            return RAMP_MEDIUM;
        } else if (rampRate.equals(Bundle.getMessage("RAMP_MED_SLOW"))) {
            return RAMP_MED_SLOW;
        } else if (rampRate.equals(Bundle.getMessage("RAMP_SLOW"))) {
            return RAMP_SLOW;
        } else if (rampRate.equals(Bundle.getMessage("RAMP_SPEEDPROFILE"))) {
            return RAMP_SPEEDPROFILE;
        }
        return RAMP_NONE;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AutoActiveTrain.class);
}
