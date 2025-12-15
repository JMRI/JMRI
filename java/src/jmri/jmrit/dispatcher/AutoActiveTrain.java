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
    public static final int RAMP_PHYSICS = 0x06; // physics-based acceleration

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
    private DispatcherFrame _dispatcher;
    
    // persistent instance variables (saved with train info)
    private int _rampRate = RAMP_NONE; // default Ramp Rate
    private float _speedFactor = 1.0f; // default speed factor
    private float _maxSpeed = 1.0f;    // default maximum train speed
    // Maximum speed in scale km/h (0.0f = disabled; use throttle % cap)
    private float _maxSpeedScaleKmh = 0.0f;
    private float _minReliableOperatingSpeed = 0.0f;
    private boolean _runInReverse = false;    // true if the locomotive should run through Transit in reverse
    private boolean _soundDecoder = false;    // true if locomotive has a sound decoder
    private long _MaxTrainLength = 600; // default train length mm.
    private float _stopBySpeedProfileAdjust = 1.0f;
    private boolean _stopBySpeedProfile = false;
     // Distance-based stopping (HEAD/TAIL reference) — runtime memory
     private float _stopByDistanceMm = 0.0f;          // 0.0f => feature disabled
     private boolean _stopByDistanceRefTail = false;  // false => HEAD; true => TAIL
    
     /** Returns the configured distance to stop into the block (mm); 0.0f means disabled. 
     * @return _stopByDistanceRefTail */
     public boolean isStopByDistanceRefTail() {
         return _stopByDistanceRefTail; 
         }
     public float getStopByDistanceMm() {
         return _stopByDistanceMm; 
         }
    
     /** Sets whether the stop reference is TAIL (true) or HEAD (false). */
     public void setStopByDistanceRefTail(boolean tail) { _stopByDistanceRefTail = tail; }
    
     /** Sets the configured distance to stop into the block (mm). */
     public void setStopByDistanceMm(float mm) { _stopByDistanceMm = (mm > 0.0f) ? mm : 0.0f; }
    
     /** Returns true if the stop reference is TAIL (add train length); false for HEAD. */
    private boolean _useSpeedProfileRequested = true;
    private int _functionLight = 0;
    private int _functionBell = 1;
    private int _functionHorn = 2;

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

    /**
     * Manually set the train throttle Function value.
     * Value passed through to the Throttle.
     * @param functionNum the function number.
     * @param isSet true is on, false is off.
     */
    public void setFunction(int functionNum, boolean isSet) {
        _autoEngineer.setFunction(functionNum, isSet);
    }

    public synchronized float getTargetSpeed() {
        return _autoEngineer.getTargetSpeed();
    }

    public synchronized void setTargetSpeedByPass(float speed) {
        _autoEngineer.setTargetSpeed(-1.0f, speed);
    }

    public synchronized void setTargetSpeedByPass(float distance, float speed) {
        if (distance < 0.0f) {
            _autoEngineer.setTargetSpeed(speed);
        } else {
            _autoEngineer.setTargetSpeed(distance, speed);
        }
   }

    public synchronized void setTargetSpeed(float speed) {
        if (_autoEngineer.isStopped() && getTargetSpeed() == 0.0f && speed > 0.0f) {
            if (_autoTrainAction.isDelayedStart(-1.0f, speed)) {
                return;
            }
        }
        _autoEngineer.setTargetSpeed(speed);
    }

    public synchronized void setTargetSpeed(float distance, float speed) {
        if (_autoEngineer.isStopped() && getTargetSpeed() == 0.0f && speed > 0.0f) {
            if (_autoTrainAction.isDelayedStart(distance, speed)) {
                return;
            }
        }
        _autoEngineer.setTargetSpeed(distance, speed);
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
        if (_autoEngineer != null ) {
            _autoEngineer.setSpeedLimits(_minReliableOperatingSpeed, _maxSpeed, _speedFactor);
        }
    }
    
    public float getMaxSpeedScaleKmh() { return _maxSpeedScaleKmh; }
    public void setMaxSpeedScaleKmh(float kmh) { _maxSpeedScaleKmh = kmh; }

    /**
     * gets the lowest speed as a percentage of throttle that the loco reliably operates.
     * @return percentage throttle
     */
    public float getMinReliableOperatingSpeed() {
        return _minReliableOperatingSpeed;
    }

    /**
     * Sets the lowest speed as a percentage of throttle that the loco reliably operates.
     * @param speed percentage of throttle.
     */
    public void setMinReliableOperatingSpeed(float speed) {
        _minReliableOperatingSpeed = speed;
        if (_autoEngineer != null ) {
            _autoEngineer.setSpeedLimits(_minReliableOperatingSpeed, _maxSpeed, _speedFactor);
        }
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

    /**
     *
     * @return train length in MM.
     */
    public long getMaxTrainLengthMM() {
        return _MaxTrainLength;
    }

    /**
     * Set Train length in Scale Meters
     * @param length length of train in meterd
     * @param scaleFactor as supplied by scale object
     */
    public void setMaxTrainLength(double length, double scaleFactor) {
        _MaxTrainLength =  (long) (length * 1000.0 * scaleFactor);
        log.trace("setMaxTrainLength[{}]",_MaxTrainLength);
    }

    public void setUseSpeedProfile(boolean tf) {
        _useSpeedProfileRequested = tf;
    }

    public boolean getUseSpeedProfile() {
        return _useSpeedProfileRequested;
    }

    public void setStopBySpeedProfile(boolean tf) {
        _stopBySpeedProfile = tf;
    }

    public void setStopBySpeedProfileAdjust(float adjust) {
        _stopBySpeedProfileAdjust = adjust;
    }

    public boolean getStopBySpeedProfile() {
        return _stopBySpeedProfile;
    }

    public float getStopBySpeedProfileAdjust() {
        return _stopBySpeedProfileAdjust;
    }
    /**
     * Set the F-Number for the light
     * @param value F-Number
     */
    public void setFunctionLight(int value) {
        _functionLight = value;
    }
    /**
     * Returns the F-Number for the light.
     * @return F-Number
     */
    public int getFunctionLight() {
        return _functionLight;
    }
    /**
     * Set the F-Number for the Bell
     * @param value F-Number
     */
    public void setFunctionBell(int value) {
        _functionBell = value;
    }
    /**
     * Returns the F-Number for the Bell.
     * @return F-Number
     */
    public int getFunctionBell() {
        return _functionBell;
    }
    /**
     * Set the F-Number for the Horn
     * @param value F-Number
     */
    public void setFunctionHorn(int value) {
        _functionHorn = value;
    }
    /**
     * Returns the F-Number for the Horn.
     * @return F-Number
     */
    public int getFunctionHorn() {
        return _functionHorn;
    }

    /**
     * Get current Signal DisplayName.
     * @return empty String if no signal, otherwise Display Name.
     */
    public String getCurrentSignal() {
        if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALHEAD) {
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
        if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALHEAD) {
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
        _dispatcher = InstanceManager.getDefault(DispatcherFrame.class);

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
                if (_useSpeedProfileRequested) {
                    if (re.getSpeedProfile() != null && re.getSpeedProfile().getProfileSize() > 0) {
                        useSpeedProfile = true;
                    }
                }
                log.debug("{}: requested roster entry '{}', address={}, use speed profile requested={} usespeedprofile set={}",
                        _activeTrain.getTrainName(), re.getId(), _address, _useSpeedProfileRequested, useSpeedProfile);
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
        log.debug("{}: New AutoEngineer, address={}, length (mm)={}, factor={}, useSpeedProfile={}",
                _activeTrain.getTrainName(),
                _throttle.getLocoAddress(),
                getMaxTrainLengthMM(), _speedFactor, useSpeedProfile);
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
                _autoEngineer.setRamping(_currentRampRate, _dispatcher.getFullRampTime(),
                        _dispatcher.getMinThrottleInterval(), _currentRampRate);
                _autoEngineer.setSpeedLimits(_minReliableOperatingSpeed, _maxSpeed, _speedFactor);
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
            } else if (_dispatcher.getAutoAllocate()) {
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
    private Turnout _turnoutStateNeeded = null;
    private PropertyChangeListener _turnoutStateListener = null;
    private boolean _stoppingByBlockOccupancy = false;    // if true, stop when _stoppingBlock goes UNOCCUPIED
    private boolean _stoppingUsingSpeedProfile = false;     // if true, using the speed profile against the roster entry to bring the loco to a stop in a specific distance
    // Distance stop is armed (waiting to start at the section's first occupied block)
    private boolean _distanceStopPending = false;
    // If true, the pending distance stop is an approach-to-min (hold until sensor), not a stop-to-zero
    private boolean _distanceStopPendingToMin = false;
    private float _distanceStopPendingMm = 0.0f;
    private int _distanceStopPendingTask = NO_TASK;
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
    
     // --- Physics runtime state (added) ---
     private float _additionalWeightTonnes = 0.0f;      // extra consist mass in metric tonnes (t)
     private float _rollingResistanceCoeff = 0.002f;    // dimensionless c_rr; default ~0.002
    
     public void setAdditionalTrainWeightMetricTonnes(float tonnes) {
         _additionalWeightTonnes = Math.max(0.0f, tonnes);
     }
     public float getAdditionalTrainWeightMetricTonnes() { return _additionalWeightTonnes; }
    
     public void setRollingResistanceCoeff(float value) {
         _rollingResistanceCoeff = Math.max(0.0f, value);
     }
     public float getRollingResistanceCoeff() { return _rollingResistanceCoeff; }

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
        if (waitingOnAllocation || _activeTrain.getSignalType() == DispatcherFrame.SECTIONSALLOCATED) {
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
             // If a distance stop is pending, start exactly at the first block INSIDE the current section
             if (_distanceStopPending && _currentAllocatedSection != null) {
                 Block enter = _currentAllocatedSection.getEnterBlock(_previousAllocatedSection);
                 if (enter == b) {
                     float mm = _distanceStopPendingMm;
                     int taskPending = _distanceStopPendingTask;
                     boolean toMin = _distanceStopPendingToMin;
                     _distanceStopPending = false;
                     _distanceStopPendingToMin = false;
                     _stoppingUsingSpeedProfile = true;         // commit to distance-based braking
                     cancelStopInCurrentSection();              // cancel any other stop mode/ramping
                     Runnable controller = new DistanceStopController(mm, taskPending, toMin);
                     Thread t = jmri.util.ThreadingUtil.newThread(controller, "DistanceStopPlanner " + getActiveTrain().getActiveTrainName());
                     t.start();

                 }
             }
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
                            _dispatcher.queueScanOfAllocationRequests();
                            // and then set the signal
                        }
                        // can be mid block
                        setupNewCurrentSignal(null, true);
                        setSpeedBySignal();
                    }
                    // are we restarting later
                    else if ( _activeTrain.getResetWhenDone()) {
                        // We enter this code for each block in the section.
                        // If we stop in the farthest block eg Block 3 in a 3 Block Section
                        // nothing special is required when starting.
                        // If we stop in Block 1 of a 3 block section, and enter this code
                        // when starting off again, so its just an advance of the _nextBlock.
                        // we can tell which situation it is by looking
                        // whether the _nextSection is not null and allocated to us.
                        if ( _nextSection == null || !_activeTrain.isInAllocatedList(_nextSection)) {
                            removeCurrentSignal();
                            _nextBlock = getNextBlock(_currentBlock, _currentAllocatedSection);
                            stopInCurrentSection(BEGINNING_RESET);
                        } else {
                            _nextBlock = getNextBlock(_currentBlock, _currentAllocatedSection);
                        }
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
                        //if (_nextBlock.getState() == Block.OCCUPIED) {
                        //    handleBlockStateChange(as, _nextBlock);
                        //}
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
            } else {
                if (!isStopping() && _dispatcher.getUseOccupiedTrackSpeed()) {
                    setSpeedBySignal();
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

    /*
     * Reverse lookup for allocated section.
     */
    protected AllocatedSection getAllocatedSectionForSection(Section s) {
        for (AllocatedSection allocatedSection : _activeTrain.getAllocatedSectionList()) {
            if (allocatedSection.getSection() == s) {
                return allocatedSection;
            }
        }
        return null;
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
        if ((!_dispatcher.getAutoAllocate()) && ((_lastAllocatedSection == null)
                || (_lastAllocatedSection.getNextSection() == as.getSection()))) {
            // if AutoAllocate, this is now done in DispatcherFrame.java for all trains
            _lastAllocatedSection = as;
            if (as.getNextSection() != null) {
                Section nSection = as.getNextSection();
                int nextSeq = as.getNextSectionSequence();
                int nextDir = _activeTrain.getAllocationDirectionFromSectionAndSeq(nSection, nextSeq);
                _dispatcher.requestAllocation(_activeTrain, nSection, nextDir, nextSeq, true, null);
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
        if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALHEAD) {
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
        if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALHEAD) {
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
            } else {
                // Note: null signal head will result when exiting throat-to-throat blocks.
                log.warn("new current signal is null - sometimes OK");
            }
            setSpeedBySignal();
        } else if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALMAST) {
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
                } else {
                    checkForGhost();
                }
            } else {
                // There is a missing signal mast at a block boundary.
                // If the next block is allocated to this train we can continue.
                // If the train was stopped here we can try and restart it. Either way we use
                // setting setSpeedBySectionsAllocated as a way out of the dilemma.
                log.debug("{}: new current signalmast is null for section {} - sometimes OK", _activeTrain.getTrainName(),
                        as == null ? "Null" : as.getSection().getDisplayName(USERSYS));
                if (_nextBlock == null || ! _activeTrain.getBlockList().contains(_nextBlock) ||  _autoEngineer.isStopped()) {
                    log.warn("{}: new current signalmast is null for section {} and next block is not this trains. Temporarily continuing by allocations", _activeTrain.getTrainName(),
                            as == null ? "Null" : as.getSection().getDisplayName(USERSYS));
                    setSpeedBySectionsAllocated();
                }
                checkForGhost();
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
                _dispatcher.queueScanOfAllocationRequests();
            }

        }
    }

    // Criteria for being able to set or get a speed.
    protected boolean canSpeedBeSetOrChecked() {
        if (_pausingActive || getAutoEngineer() == null ||
                ((_activeTrain.getStatus() != ActiveTrain.RUNNING) &&
                        (_activeTrain.getStatus() != ActiveTrain.WAITING) &&
                        !_activeTrain.getStarted()) ||
                (_activeTrain.getMode() != ActiveTrain.AUTOMATIC)) {
            log.debug("{}:Train is not currently eligible for settingspeed or checking ghosts",_activeTrain.getActiveTrainName());
            return false;
        }
        return true;
    }

    // called by above or when resuming after stopped action
    protected synchronized void setSpeedBySignal() {
        log.trace("Set Speed by Signal");
        if (!canSpeedBeSetOrChecked()) {
            log.trace("[{}]:cannot set speed.",getActiveTrain().getActiveTrainName());
            return;
        }
         // Do not alter speed while a distance-based stop is active or armed,
         // EXCEPT we must always honor a STOP/DANGER/HELD signal to avoid overruns.
         if (_stoppingUsingSpeedProfile || _distanceStopPending) {
             // SignalHead case
             if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALHEAD && _controllingSignal != null) {
                 // HELD is an absolute stop; RED/FLASHRED/DARK are treated as stop in head logic
                 int app = _controllingSignal.getAppearance();
                 if (app == SignalHead.HELD || app == SignalHead.RED || app == SignalHead.FLASHRED || app == SignalHead.DARK) {
                     checkForSignalPassedOrStop(_controllingSignal.getDisplayName(USERSYS));
                     return;
                 }
             }
             // SignalMast case
             if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALMAST && _controllingSignalMast != null) {
                 final String aspect = _controllingSignalMast.getAspect();
                 final String danger = _controllingSignalMast.getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.DANGER);
                 if (_controllingSignalMast.getHeld() || !_controllingSignalMast.getLit() || (danger != null && danger.equals(aspect))) {
                     checkForSignalPassedOrStop(_controllingSignalMast.getDisplayName(USERSYS));
                     return;
                 }
             }
             // Otherwise, allow the distance-stop to proceed without interference.
             log.trace("[{}]: distance stop active/pending — suppressing setSpeedBySignal", getActiveTrain().getActiveTrainName());
             return;
         }

        // only bother to check signal if the next allocation is ours.
        // and the turnouts have been set
        if (checkAllocationsAhead() && checkTurn(getAllocatedSectionForSection(_nextSection))) {
            if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALHEAD
                    && _controllingSignal != null) {
                setSpeedBySignalHead();
            } else if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALMAST
                    && _controllingSignalMast != null) {
                setSpeedBySignalMast();
            } else {
                log.trace("{}:Set Speed by BlocksAllocated",_activeTrain.getActiveTrainName());
                setSpeedBySectionsAllocated();
            }
            checkForGhost();
        } else {
            // This might be the last section....
            if (_currentAllocatedSection != null && _currentAllocatedSection.getNextSection() == null) {
                stopInCurrentSection(END_TRAIN);
            } else {
                // This will stop it.
                stopInCurrentSection(NO_TASK);
                log.debug("{}:Set Stop",_activeTrain.getActiveTrainName());
                waitingOnAllocation = true;  // flag setSpeedBySignal required when another allocation made.
            }
        }
    }

    private void checkForGhost() {
        if (!canSpeedBeSetOrChecked()) {
            log.trace("[{}]:cannot check for ghost.",getActiveTrain().getActiveTrainName());
            return;
        }
        if ( !(getTargetSpeed() == 0.0f || isStopping())
                && _nextBlock != null
                && _currentBlock != null
                && _nextBlock.getSensor() != null
                && _nextBlock.getIsGhost()) {
            if ( _currentBlock.getIsGhost()) {
                log.error("Stopping due to two consecutive no sensor blocks [{}], [{}]",
                        _currentBlock.getDisplayName(), _nextBlock.getDisplayName());
            } else {
                try {
                    _currentBlock.addPropertyChangeListener(new DarkTerritoryListener(_nextBlock.getSensor()));
                    _nextBlock.getSensor().setKnownState(Sensor.ACTIVE);
                } catch (jmri.JmriException ex) {
                    log.error("Error entering darkterratory");
                }
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
        if (!canSpeedBeSetOrChecked()) {
            log.trace("[{}]:cannot set speed.",getActiveTrain().getActiveTrainName());
            return;
        }
        
        // Do not alter speed while a distance-based stop is active or armed
        if (_stoppingUsingSpeedProfile || _distanceStopPending) {
            log.trace("[{}]: distance stop active/pending — suppressing setSpeedBySectionsAllocated", getActiveTrain().getActiveTrainName());
            return;
        }

        if (_stoppingByBlockOccupancy && (_stoppingBlock != null && _stoppingBlock.getState() == Block.UNOCCUPIED)) {
            // we are awaiting a delayed stop
            return;
        }
        int sectionsAhead = 0;
        for (AllocatedSection allocatedSection : _activeTrain.getAllocatedSectionList()) {
            if (!allocatedSection.getEntered()) {
                sectionsAhead++;
            }
        }
        float newSpeed = 0.0f;
        log.debug("[{}:SectionsAhead[{}]",_activeTrain.getActiveTrainName() ,sectionsAhead);
            switch (sectionsAhead) {
                case 0:
                    newSpeed = 0.0f;
                    break;
                case 1:
                    newSpeed = InstanceManager.getDefault(SignalSpeedMap.class)
                            .getSpeed("Medium");
                    // .getSpeed(_dispatcher.getStoppingSpeedName());
                    _activeTrain.setStatus(ActiveTrain.RUNNING);
                    break;
                default:
                    newSpeed = InstanceManager.getDefault(SignalSpeedMap.class)
                            .getSpeed("Normal");
                    // .getSpeed(_dispatcher.getStoppingSpeedName());
                    _activeTrain.setStatus(ActiveTrain.RUNNING);
            }
            if (_dispatcher.getUseOccupiedTrackSpeed()) {
                newSpeed = getMinSpeedOfOccupiedBlocks(newSpeed);
            }
            // see if needs to slow for next block.
            if (newSpeed > 0 && _nextBlock != null) {
                float speed = getSpeedFromBlock(_nextBlock);
                if (speed < newSpeed) {
                    // slow for next block
                    newSpeed = speed;
                }
            }
        if (newSpeed > 0) {
            log.trace("setSpeedBySectionsAllocated isStopping[{}]",isStopping());
            cancelStopInCurrentSection();
            setTargetSpeed(getThrottleSettingFromSpeed(newSpeed));
        } else {
            waitingOnAllocation = true;
            stopInCurrentSection(NO_TASK);
        }
    }

    // Check for speed of incoming blocks.
    // in and out speed in is throttle percent.
    private float getMinSpeedOfOccupiedBlocks(float speed) {
        if (!_dispatcher.getUseOccupiedTrackSpeed()) {
            return speed;
        }
        // get slowest speed of any entered and still occupied
        // or entered but not released (HEADONLY / HEADANDTAIL
        float newSpeed = speed;
        for (AllocatedSection asE : _activeTrain.getAllocatedSectionList()) {
            if (asE.getEntered()) {
                for (Block b : asE.getSection().getBlockList()) {
                    if (b.getState() == Block.OCCUPIED
                            || _activeTrain.getTrainDetection() != TrainDetection.TRAINDETECTION_WHOLETRAIN ) {
                        if (getSpeedFromBlock(b) < newSpeed) {
                            newSpeed = getSpeedFromBlock(b);
                        }
                    }
                }
            }
        }
        log.trace("{}: getMinSpeedOfOccupiedBlocks Org Speed [{}] New [{}]",
                _activeTrain.getActiveTrainName(), speed, newSpeed);
        return newSpeed;
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
            if (_turnoutStateNeeded  != null && _turnoutStateListener != null) {
                _turnoutStateNeeded.removePropertyChangeListener("KnownState",_turnoutStateListener);
                _turnoutStateNeeded = null;
                _turnoutStateListener =null;
            }
            _turnoutStateNeeded = _dispatcher.getAutoTurnoutsHelper().checkStateAgainstList(as.getAutoTurnoutsResponse());
            if (_turnoutStateNeeded != null) {
                _turnoutStateNeeded.addPropertyChangeListener("KnownState",_turnoutStateListener = (PropertyChangeEvent e) -> {
                    _turnoutStateNeeded.removePropertyChangeListener("KnownState",_turnoutStateListener);
                         _turnoutStateListener=null;
                         _turnoutStateNeeded=null;
                        setSpeedBySignal();
                });
                return false;
            }
        }
        return true;
    }

    private void setSpeedBySignalMast() {
        //Set speed using SignalMasts;
        if (_controllingSignalMast == null) {
            // temporarily revert to by sections allocated
            setSpeedBySectionsAllocated();
            return;
        }
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
            // Adjust for occupied blocks.
            if (_dispatcher.getUseOccupiedTrackSpeed()) {
                speed = getMinSpeedOfOccupiedBlocks(speed);
            }
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
                setTargetSpeedByProfile(useSpeed,_stopBySpeedProfileAdjust,true);
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
        _autoEngineer.slowToStop(false);
    }


    /** Clamp utility */
    private static float clamp(float v, float lo, float hi) {
        return (v < lo) ? lo : ((v > hi) ? hi : v);
    }
    
    /** Clamp throttle [% 0..1] */
    private static float clampThrottle(float pct) {
        if (pct < 0.0f) return 0.0f;
        if (pct > 1.0f) return 1.0f;
        return pct;
    }
    
    /**
     * Convert a throttle percentage [0..1] to speed (mm/s) using the roster profile.
     * We interrogate the profile via getDistanceTravelled(forward, speedStep, duration) to obtain mm/s.
     */
    private float speedMmsFromThrottle(float throttlePct, boolean forward) {
        if (re == null || re.getSpeedProfile() == null) return 0.0f;
        float pct = clampThrottle(throttlePct);                // ensure 0..1
        return re.getSpeedProfile().getSpeed(pct, forward);    // mm/s
    }
    
    /**
     * Invert the roster profile: find the throttle [% 0..1] that yields the requested speed (mm/s).
     * Uses a binary search over [minReliable..max] throttle.
     */
    private float throttleForSpeedMms(float targetMms, boolean forward) {
        // Bracket: min reliable ↔ max speed (percent throttle)
        float loPct = clampThrottle(_minReliableOperatingSpeed);
        float hiPct = clampThrottle(_maxSpeed);
        float loMms = speedMmsFromThrottle(loPct, forward);
        float hiMms = speedMmsFromThrottle(hiPct, forward);
    
        if (targetMms <= loMms) return loPct;
        if (targetMms >= hiMms) return hiPct;
    
        // Binary search to ~0.1% throttle precision
        float midPct = 0.0f;
        for (int i = 0; i < 16; i++) {
            midPct = 0.5f * (loPct + hiPct);
            float midMms = speedMmsFromThrottle(midPct, forward);
            if (midMms < targetMms) {
                loPct = midPct;
            } else {
                hiPct = midPct;
            }
        }
        return clampThrottle(midPct);
    }
    
    private synchronized void stopInCurrentSection(int task) {
        if (_currentAllocatedSection == null) {
            log.error("{}: Current allocated section null on entry to stopInCurrentSection", _activeTrain.getTrainName());
            setStopNow();
            return;
        }
        log.debug("{}: StopInCurrentSection called for {} task[{}] targetspeed[{}]", _activeTrain.getTrainName(), _currentAllocatedSection.getSection().getDisplayName(USERSYS),task,getTargetSpeed());        

        /* =======================================================================
         * Distance-based stopping (destination section only) — custom planner.
         * We compute a constant-deceleration braking curve to stop exactly at 'distanceMm'
         * and drive the throttle ourselves via AutoEngineer.setSpeedImmediate(...).
         *
         * No dependency on RosterSpeedProfile.changeLocoSpeed or AutoEngineer.setTargetSpeed(distance,...).
         * We only read profile speeds via re.getSpeedProfile().getDistanceTravelled(...) to invert throttle ↔ mm/s.
         *
         * TODO (future): extend to signal stop points inside sections using the same controller,
         * with an explicit per-section stop origin.
         * ======================================================================= */
        boolean distanceEnabled = (_stopByDistanceMm > 0.0f);
        // Direction-aware profile availability (we must have speeds for the current direction)
        boolean profileAvailable = false;
        if (re != null && re.getSpeedProfile() != null) {
            boolean forward = _autoEngineer.getIsForward();
            profileAvailable = forward ? re.getSpeedProfile().hasForwardSpeeds()
                                       : re.getSpeedProfile().hasReverseSpeeds();
        }
    
        // Resolve the section's stopping sensor for the current travel direction (do not mutate _stopSensor yet)
        Sensor stopSensorCandidate = null;
        if (_currentAllocatedSection != null) {
            if (_currentAllocatedSection.getSection().getState() == Section.FORWARD) {
                stopSensorCandidate = _currentAllocatedSection.getSection().getForwardStoppingSensor();
            } else {
                stopSensorCandidate = _currentAllocatedSection.getSection().getReverseStoppingSensor();
            }
        }
    
        // Combined mode = user opted into Stop-by-Distance, profile is available, and a stopping sensor is present & in use
        boolean combinedMode = distanceEnabled && profileAvailable && (_useStopSensor) && (stopSensorCandidate != null);
    
        if ((distanceEnabled && profileAvailable) && !_stoppingUsingSpeedProfile && !_distanceStopPending) {
    
            // Compute requested travel distance from section entry to stop reference
            final float distanceMmBase = _stopByDistanceMm + (_stopByDistanceRefTail ? getMaxTrainLengthMM() : 0.0f);
    
            if (combinedMode) {
                // --- New combined behaviour ---
                // We will decelerate to MinimumReliableOperatingSpeed within distanceMmBase, then hold until the stop sensor fires.
    
                // Decide whether to start NOW (already past the section entry) or ARM to start at the entry block
                Block enter = (_currentAllocatedSection != null)
                        ? _currentAllocatedSection.getEnterBlock(_previousAllocatedSection)
                        : null;
    
                if (enter == null || enter.getState() == Block.OCCUPIED) {
                    // Start immediately from current position (adjust remaining distance if we’re already partway in)
                    float remainingMm = distanceMmBase;
                    if (_currentAllocatedSection != null && _currentBlock != null) {
                        float sectionLen = _currentAllocatedSection.getActualLength();
                        float lenRemaining = _currentAllocatedSection.getLengthRemaining(_currentBlock);
                        float progressed = Math.max(0.0f, sectionLen - lenRemaining);
                        remainingMm = distanceMmBase - progressed;
                    }
                    if (remainingMm <= 0.0f) {
                        // Already at/inside the target – assert crawl and fall through to sensor wait
                        float vMin = speedMmsFromThrottle(_minReliableOperatingSpeed, _autoEngineer.getIsForward());
                        float thrMin = throttleForSpeedMms(vMin, _autoEngineer.getIsForward());
                        _autoEngineer.setSpeedImmediate(clampThrottle(thrMin));
                    } else {
                        // Plan a decel to min (NOT to zero), and do not finish with stop here
                        _stoppingUsingSpeedProfile = true;        // suppress setSpeedBySignal until done
                        cancelStopInCurrentSection();              // cancel any other ramping/stop modes
                        Runnable controller = new DistanceStopController(remainingMm, task, /*toMinOnly*/ true);
                        Thread t = jmri.util.ThreadingUtil.newThread(controller,
                                "DistanceStopPlanner " + getActiveTrain().getActiveTrainName());
                        t.start();
                    }
    
                    // Now arm the stop sensor, but do NOT pre-lower to a generic stopping speed
                    _stopSensor = stopSensorCandidate;
                    if (_stopSensor.getKnownState() == Sensor.ACTIVE) {
                        setStopNow();  // sensor is already made – stop immediately
                    } else {
                        _stopSensor.addPropertyChangeListener(_stopSensorListener = (java.beans.PropertyChangeEvent e) -> {
                            handleStopSensorChange(e);
                        });
                        _stoppingBySensor = true;
                    }
                    return; // combined branch handled
                }
    
                // Not yet at the section entry: arm a pending approach-to-min plan and the stop sensor listener now
                _distanceStopPending = true;
                _distanceStopPendingToMin = true;
                _distanceStopPendingMm = distanceMmBase;
                _distanceStopPendingTask = task;
    
                _stopSensor = stopSensorCandidate;
                if (_stopSensor.getKnownState() == Sensor.ACTIVE) {
                    setStopNow();
                } else {
                    _stopSensor.addPropertyChangeListener(_stopSensorListener = (java.beans.PropertyChangeEvent e) -> {
                        handleStopSensorChange(e);
                    });
                    _stoppingBySensor = true;
                }
                return; // wait for entry OCCUPIED to start the approach-to-min plan
            }
    
            // --- Legacy pure distance stop (ramp to ZERO at the distance) ---
            // Case A/B logic (start now or arm pending), just like before.
            Block enter = (_currentAllocatedSection != null)
                    ? _currentAllocatedSection.getEnterBlock(_previousAllocatedSection)
                    : null;
    
            if (enter == null || enter.getState() == Block.OCCUPIED) {
                float remainingMm = distanceMmBase;
                if (_currentAllocatedSection != null && _currentBlock != null) {
                    float sectionLen = _currentAllocatedSection.getActualLength();
                    float lenRemaining = _currentAllocatedSection.getLengthRemaining(_currentBlock);
                    float progressed = Math.max(0.0f, sectionLen - lenRemaining);
                    remainingMm = distanceMmBase - progressed;
                }
                if (remainingMm <= 0.0f) {
                    setStopNow();
                } else {
                    _stoppingUsingSpeedProfile = true;
                    cancelStopInCurrentSection();
                    Runnable controller = new DistanceStopController(remainingMm, task /*, toMinOnly = false*/);
                    Thread t = jmri.util.ThreadingUtil.newThread(controller,
                            "DistanceStopPlanner " + getActiveTrain().getActiveTrainName());
                    t.start();
                }
                return;
            }
    
            // Arm pending pure distance stop
            _distanceStopPending = true;
            _distanceStopPendingToMin = false;
            _distanceStopPendingMm = distanceMmBase;
            _distanceStopPendingTask = task;
            return;
        }

        // =======================================================================
         // Do not exit before destination stop logic;
         // only bail out if the train is already at zero AND no profile/distance stop is requested.
         if (getTargetSpeed() == 0.0f && !_stopBySpeedProfile && _stopByDistanceMm <= 0.0f) {
             log.debug("{}: already stopped and no planned stop requested — skipping stop planning.", _activeTrain.getTrainName());
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
        } else if (useSpeedProfile && _stopBySpeedProfile) {
            log.debug("{}: Section [{}] Section Length[{}] Max Train Length [{}] StopBySpeedProfile [{}]. setStopNow", _activeTrain.getTrainName(),
                    _currentAllocatedSection.getSection().getDisplayName(USERSYS), _currentAllocatedSection.getActualLength(), getMaxTrainLengthMM(), _stopBySpeedProfile);
            // stopping by speed profile uses section length to stop

            setTargetSpeedState(STOP_SPEED,useSpeedProfile);
            
        } else if (_currentAllocatedSection.getActualLength()  < getMaxTrainLengthMM()) {
            log.debug("{}: Section [{}] Section Length[{}] Max Train Length [{}]. setStopNow({})",
                    _activeTrain.getTrainName(),
                    _currentAllocatedSection.getSection().getDisplayName(USERSYS),
                    _currentAllocatedSection.getActualLength(),
                    getMaxTrainLengthMM(), _stopBySpeedProfile);
            // train will not fit comfortably in the Section, stop it immediately
            setStopNow();
        } else if (_activeTrain.getTrainDetection() == TrainDetection.TRAINDETECTION_WHOLETRAIN) {
            log.debug("{}: train will fit in [{}] ({}>={}), stop when prev block clears.", _activeTrain.getTrainName(),
                    _currentAllocatedSection.getSection().getDisplayName(USERSYS), _currentAllocatedSection.getActualLength(), getMaxTrainLengthMM());
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
                            && (getBlockLength(exitBlock) > getMaxTrainLengthMM())) {
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
                    while ((tstLength < getMaxTrainLengthMM()) && (tstBlock != enterBlock)) {
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
                    if (getMaxTrainLengthMM() > tstLength) {
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
        _stoppingUsingSpeedProfile = false;  // queued stop has completed; allow normal speed logic again
        _dispatcher.queueReleaseOfCompletedAllocations();
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
                        _dispatcher.queueScanOfAllocationRequests();
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
                            _dispatcher.queueScanOfAllocationRequests();
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
        //  verify that _stoppingBlock is actually occupied, if not stop immediately
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
                    .getSpeed(_dispatcher.getStoppingSpeedName());
        } catch (IllegalArgumentException ex) {
            log.error("Missing [{}] from Speed table - defaulting to 25",
                    _dispatcher.getStoppingSpeedName());
        }
        if (getThrottleSettingFromSpeed(signalSpeed) < getTargetSpeed()) {
            if (useSpeedProfile) {
                // use 75 percent or normal amount, dont clear isstopping for ramping.
                setTargetSpeedByProfile(signalSpeed,_stopBySpeedProfileAdjust*0.75f,false);
            } else {
                setTargetSpeed(signalSpeed/100.0f);
            }
        }
    }

    ///**
    // * Sets the throttle percent unless it is already less than the new setting
    // * @param throttleSetting  Max ThrottleSetting required.
    // */
    //private synchronized void setToAMaximumThrottle(float throttleSetting) {
    //    if (throttleSetting < getTargetSpeed()) {
    //        setTargetSpeed(throttleSetting);
    //    }
    //}

    /**
     * Calculates the throttle setting for a given speed.
     * @param speed  the unadjusted speed.
     * @return - throttle setting (a percentage)
     */
    private synchronized float getThrottleSettingFromSpeed(float speed) {
        if (useSpeedProfile) {
            float throttleSetting = _activeTrain.getRosterEntry().getSpeedProfile()
                    .getThrottleSettingFromSignalMapSpeed(speed, getForward());
            return throttleSetting;
        }
        if (_activeTrain.getSignalType() == DispatcherFrame.SIGNALMAST) {
            float mls;
            if (_controllingSignalMast != null) {
                mls = _controllingSignalMast.getSignalSystem().getMaximumLineSpeed();
            } else {
                //plan B
                mls = _dispatcher.getMaximumLineSpeed();
            }
            float throttleSetting = (speed / mls);
            return throttleSetting;
        } else {
            return speed/100.0f;
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
        if (_currentAllocatedSection == null) {
            log.debug("_currentAllocatedSection == null in setTargetSpeedState");
            return;
        }
        _autoEngineer.slowToStop(false);
       
        float stoppingDistanceAdjust =  _stopBySpeedProfileAdjust *
                ( _activeTrain.isTransitReversed() ?
                _currentAllocatedSection.getTransitSection().getRevStopPerCent() :
                    _currentAllocatedSection.getTransitSection().getFwdStopPerCent());
        log.debug("stoppingDistanceAdjust[{}] isReversed[{}] stopBySpeedProfileAdjust[{}]",stoppingDistanceAdjust,
                _activeTrain.isTransitReversed(),_stopBySpeedProfileAdjust );
        if (speedState > STOP_SPEED) {
            cancelStopInCurrentSection();
            if (_currentRampRate == RAMP_SPEEDPROFILE && useSpeedProfile) {
                // we are going to ramp up  / down using section length and speed profile
                _autoEngineer.setTargetSpeed(_currentAllocatedSection.getLengthRemaining(_currentBlock)
                        * stoppingDistanceAdjust, speedState);
            } else {
                setTargetSpeed(_speedRatio[speedState]);
            }
        } else if (stopBySpeedProfile) {
            // we are going to stop by profile
            _stoppingUsingSpeedProfile = true;
            _autoEngineer.setTargetSpeed(_currentAllocatedSection.getLengthRemaining(_currentBlock)
                    * stoppingDistanceAdjust, 0.0f);
        } else {
            _autoEngineer.setHalt(true);
            setTargetSpeed(0.0f);
        }
    }

    private synchronized void setTargetSpeedByProfile(float speedState, float stopBySpeedProfileAdjust, boolean cancelStopping) {
        // the speed comes in as units of warrents (mph, kph, mm/s etc)
            try {
                float throttleSetting = _activeTrain.getRosterEntry().getSpeedProfile().getThrottleSettingFromSignalMapSpeed(speedState, getForward());
                log.debug("{}: setTargetSpeedByProfile: {} SpeedState[{}]",
                        _activeTrain.getTrainName(),
                        throttleSetting,
                        speedState);
                if (throttleSetting > 0.009 && _currentRampRate != RAMP_SPEEDPROFILE && useSpeedProfile) {
                    if (cancelStopping) {cancelStopInCurrentSection();}
                    setTargetSpeed(throttleSetting); // apply speed factor and max
                } else if (throttleSetting > 0.009) {
                    if (cancelStopping) {cancelStopInCurrentSection();}
                    setTargetSpeed(_currentAllocatedSection.getLengthRemaining(_currentBlock)  * stopBySpeedProfileAdjust , throttleSetting);
                } else if (useSpeedProfile && _stopBySpeedProfile) {
                    setTargetSpeed(0.0f);
                    _stoppingUsingSpeedProfile = true;
                    _autoEngineer.setTargetSpeed(_currentAllocatedSection.getLengthRemaining(_currentBlock)  * stopBySpeedProfileAdjust, 0.0f);
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
            setTargetSpeedByProfile(speed,_stopBySpeedProfileAdjust,true);
            return;
        }
        _autoEngineer.slowToStop(false);
        float mls;
        if (_controllingSignalMast != null) {
            mls = _controllingSignalMast.getSignalSystem().getMaximumLineSpeed();
        } else {
            mls = _dispatcher.getMaximumLineSpeed();
        }
        float decSpeed = (speed / mls);
        if (decSpeed > 0.0f) {
            cancelStopInCurrentSection();
            setTargetSpeed(decSpeed);
        } else {
            setTargetSpeed(0.0f);
            _autoEngineer.setHalt(true);
        }
    }

    private int getBlockLength(Block b) {
        if (b == null) {
            return (0);
        }
        return (int) b.getLengthMm();
//        float fLength = b.getLengthMm() / (float) _dispatcher.getScale().getScaleFactor();
//        if (_dispatcher.getUseScaleMeters()) {
//            return (int) (fLength * 0.001f);
//        }
//        return (int) (fLength * 0.00328084f);
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
        if (_turnoutStateNeeded != null && _turnoutStateListener != null) {
            _turnoutStateNeeded.removePropertyChangeListener(_turnoutStateListener);
        }
        _turnoutStateNeeded = null;
        _turnoutStateListener = null;
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
    


    private class DistanceStopController implements Runnable {
        private final float TargetDistanceMm;
        private final int Task;
        private final boolean ToMinOnly; // true = approach-to-min only (hold until sensor); false = stop-to-zero
    
        DistanceStopController(float distanceMm, int task) {
            this(distanceMm, task, false);
        }
        DistanceStopController(float distanceMm, int task, boolean toMinOnly) {
            this.TargetDistanceMm = (distanceMm > 0.0f ? distanceMm : 0.0f);
            this.Task = task;
            this.ToMinOnly = toMinOnly;
        }

    

    @Override public void run() {
        try {
            final boolean forward = _autoEngineer.getIsForward();

            // Seed current physics from ACTUAL throttle setting
            float throttleNow = clampThrottle(getThrottle().getSpeedSetting());
            float v0 = speedMmsFromThrottle(throttleNow, forward);

            final float s = TargetDistanceMm;
            if (s <= 0.0f) {
                if (ToMinOnly) {
                    // Assert crawl, then let sensor listener do the final stop
                    float vMin = speedMmsFromThrottle(_minReliableOperatingSpeed, forward);
                    float thrMin = throttleForSpeedMms(vMin, forward);
                    _autoEngineer.setSpeedImmediate(clampThrottle(thrMin));
                    return;
                } else {
                    _autoEngineer.setSpeedImmediate(0.0f);
                    Thread tWait = jmri.util.ThreadingUtil.newThread(new WaitForTrainToStop(Task),
                            "Wait for stop " + getActiveTrain().getActiveTrainName());
                    tWait.start();
                    return;
                }
            }

            // Overrun compensation only when stopping to ZERO (not for approach-to-min)
            float overrunSec = 0.0f;
            if (!ToMinOnly && re != null && re.getSpeedProfile() != null) {
                overrunSec = _autoEngineer.getIsForward()
                        ? re.getSpeedProfile().getOverRunTimeForward()
                        : re.getSpeedProfile().getOverRunTimeReverse();
                if (overrunSec < 0.0f) overrunSec = 0.0f;
            }

            final long baseMs = Math.max(_dispatcher.getMinThrottleInterval(), 50);
            final float dt = baseMs / 1000.0f;

            final float vMin = speedMmsFromThrottle(_minReliableOperatingSpeed, forward);
            final float vMax = speedMmsFromThrottle(_maxSpeed, forward);

            float sAdjusted = ToMinOnly ? s : (s - (vMin * overrunSec));
            if (sAdjusted < Math.max(0.0f, 0.5f * vMin)) {
                sAdjusted = Math.max(0.0f, 0.5f * vMin);
            }

            v0 = clamp(v0, vMin, vMax);

            // If stopping to ZERO, decel all the way; else decel to vMin and hold
            final float a = (v0 > 0.0f) ? -(v0 * v0) / (2.0f * sAdjusted) : 0.0f;

            java.util.LinkedList<Float> throttleSteps = new java.util.LinkedList<>();
            java.util.LinkedList<Integer> durationSteps = new java.util.LinkedList<>();

            float travelled = 0.0f;
            float v = v0;

            final int tailMs = Math.max(20, (int) Math.floor(_dispatcher.getMinThrottleInterval() / 2.0));
            final float tailDt = tailMs / 1000.0f;

            while (travelled < sAdjusted) {
                float remaining = sAdjusted - travelled;

                if (ToMinOnly && vMin > 0.0f && remaining <= vMin * tailDt) {
                    // Short, precise final slice to land exactly at vMin
                    float dtTail = remaining / vMin;
                    float latencySecTail = Math.min(dtTail * 0.5f, Math.max(0.0f, (baseMs / 2000.0f)));
                    dtTail = Math.max(0.001f, dtTail - latencySecTail);
                    int lastMs = Math.max(1, Math.round(dtTail * 1000.0f));
                    float thrApplied = throttleForSpeedMms(vMin, forward);
                    throttleSteps.add(clampThrottle(thrApplied));
                    durationSteps.add(lastMs);
                    break;
                }

                float dtSec = dt;

                if (!ToMinOnly && a != 0.0f && v > 0.0f) {
                    // No special split here; normal constant decel to zero case falls through
                } else if (a != 0.0f && v > vMin) {
                    // Approach-to-min: split the step to hit vMin precisely
                    float tToMin = (v - vMin) / Math.abs(a);
                    if (tToMin > 0.0f && tToMin < dtSec) {
                        dtSec = tToMin;
                    }
                }

                float vNext = v + a * dtSec;
                float vStepStart = ToMinOnly ? Math.max(v, vMin) : v;
                float vStepEnd   = ToMinOnly ? Math.max(vNext, vMin) : Math.max(vNext, 0.0f);
                float vMid = 0.5f * (vStepStart + vStepEnd);

                float deltaS;
                if (!ToMinOnly) {
                    deltaS = v * dtSec + 0.5f * a * dtSec * dtSec;
                    v = Math.max(0.0f, vNext);
                } else if (a != 0.0f && v > vMin && vNext >= vMin) {
                    deltaS = v * dtSec + 0.5f * a * dtSec * dtSec;
                    v = vNext;
                } else {
                    deltaS = vMin * dtSec;
                    v = vMin;
                }

                if (deltaS > remaining && vMid > 0.0f) {
                    float dtFinal = remaining / vMid;
                    float latencySec = Math.min(dtFinal * 0.5f, Math.max(0.0f, (baseMs / 2000.0f)));
                    dtFinal = Math.max(0.001f, dtFinal - latencySec);
                    int msFinal = Math.max(1, Math.round(dtFinal * 1000.0f));
                    float thrApplied = throttleForSpeedMms(vMid, forward);
                    throttleSteps.add(clampThrottle(thrApplied));
                    durationSteps.add(msFinal);
                    break;
                }

                int ms = Math.max(1, Math.round(dtSec * 1000.0f));
                float thrApplied = (vMid <= 0.0f) ? 0.0f : throttleForSpeedMms(vMid, forward);
                // Pre-divide for SpeedFactor so final effective throttle is what we computed
                float thrCmd = (_speedFactor > 0.0f) ? (thrApplied / _speedFactor) : thrApplied;
                float cmdMin = (_speedFactor > 0.0f) ? (_minReliableOperatingSpeed / _speedFactor) : _minReliableOperatingSpeed;
                float cmdMax = (_speedFactor > 0.0f) ? (_maxSpeed / _speedFactor) : _maxSpeed;
                thrCmd = clampThrottle(clamp(thrCmd, cmdMin, cmdMax));
                throttleSteps.add(thrCmd);
                durationSteps.add(ms);
                travelled += deltaS;

                if (!ToMinOnly && v <= 0.0f && travelled < s) {
                    break;
                }
            }

            // Append a final zero step ONLY for stop-to-zero mode
            if (!ToMinOnly) {
                throttleSteps.add(0.0f);
                durationSteps.add((int) baseMs);
            }

            // Execute the plan
            int n = throttleSteps.size();
            float[] thrArr = new float[n];
            int[] durArr = new int[n];
            for (int i = 0; i < n; i++) {
                thrArr[i] = throttleSteps.get(i);
                durArr[i] = Math.max(1, durationSteps.get(i));
            }
            
            log.debug("[{}] PHYS schedule: steps={}, firstThr={}, lastThr={}, baseMs={}",
                AutoActiveTrain.this._activeTrain.getTrainName(),
                n, thrArr[0], thrArr[n-1], baseMs);
            
            _autoEngineer.runPlannedSpeedSchedule(thrArr, durArr);

            // If we planned a stop-to-zero, wait-for-stop and then finish tasks;
            // for approach-to-min we just return (sensor listener will stop us)
            if (!ToMinOnly) {
                Thread tWait = jmri.util.ThreadingUtil.newThread(new WaitForTrainToStop(Task),
                        "Wait for stop " + getActiveTrain().getActiveTrainName());
                tWait.start();
            }
        } catch (RuntimeException ex) {
            log.warn("{}: DistanceStopController failed; issuing emergency stop",
                     _activeTrain.getTrainName(), ex);
            _autoEngineer.setSpeedImmediate(0.0f);
        }
        // Do NOT clear _stoppingUsingSpeedProfile here; executeStopTasks() clears it after a full stop.
    }
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
            // TODO: use stop in section or block?
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
                // Set speed by signal also works if signal missing
                // so we dont need to restore a previous value.
                _activeTrain.setStatus(ActiveTrain.RUNNING);
                setSpeedBySignal();
            }
        }
        private int _fastMinutes = 0;
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
        private float minReliableOperatingSpeed;
        private float maxSpeed;
        private float speedFactor;

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
        stopAllTimers();
    
        // Physics ramp: only if enabled AND speed profile exists for current direction
        boolean physicsRamp = (ramping == RAMP_PHYSICS);
        boolean forward = getIsForward();
        boolean profileAvailable = false;
        if (AutoActiveTrain.this.re != null && AutoActiveTrain.this.re.getSpeedProfile() != null) {
            profileAvailable = forward
                    ? AutoActiveTrain.this.re.getSpeedProfile().hasForwardSpeeds()
                    : AutoActiveTrain.this.re.getSpeedProfile().hasReverseSpeeds();
        }
    

        log.debug("[{}] setTargetSpeed: ramping={}, physicsRamp={}, profileAvailable={}, forward={}, speedArg={}",
            AutoActiveTrain.this._activeTrain.getTrainName(),
            ramping, physicsRamp, profileAvailable, forward, speed);

        
        if (physicsRamp && profileAvailable) {
            runPhysicsAccelerationPlanner(speed, forward);
            return;
        }
    
        // Fallback to existing behaviour
        targetSpeed = applyMaxThrottleAndFactor(speed);
        log.debug("setTargetSpeed: Set Speed[{}] adjusted to TargetSpeed[{}] ", speed, targetSpeed);
        if (ramping == RAMP_NONE || ramping == RAMP_SPEEDPROFILE) {
            throttle.setSpeedSetting(targetSpeed);
        } else {
            rampToTarget();
        }
    }

        public float getTargetSpeed(){
            return(targetSpeed);
        }

        /**
        *
        * @param throttleSetting the throttle setting that would normally be set
        * @return the adjusted throttle setting after applying Max Throttle and Percentage throttle settings
        */
        private float applyMaxThrottleAndFactor(float throttleSetting) {
            // Apply speedFactor first (this is how the existing code behaves)
            float applied = (throttleSetting > 0.0f) ? (throttleSetting * speedFactor) : throttleSetting;

            if (applied <= 0.0f) { return applied; }

            // Compute the active upper cap:
            //  - If a scale km/h cap is set AND a speed profile exists in the current direction,
            //    derive an equivalent throttle cap using the roster profile + layout scale ratio.
            //  - Otherwise, fall back to the throttle % cap (maxSpeed).
            float maxApplied;
            boolean forward = getIsForward();
            boolean profileAvailable = false;
            if (AutoActiveTrain.this.re != null && AutoActiveTrain.this.re.getSpeedProfile() != null) {
                // Direction-aware availability
                profileAvailable = forward ? AutoActiveTrain.this.re.getSpeedProfile().hasForwardSpeeds()
                                           : AutoActiveTrain.this.re.getSpeedProfile().hasReverseSpeeds();
            }

            if (AutoActiveTrain.this._maxSpeedScaleKmh > 0.0f && profileAvailable && AutoActiveTrain.this._dispatcher != null) {
                // scale km/h -> actual mm/s
                float kmh = AutoActiveTrain.this._maxSpeedScaleKmh;
                float scaleRatio = (float) AutoActiveTrain.this._dispatcher.getScale().getScaleRatio();
                float modelKmh = kmh / ((scaleRatio <= 0.0f) ? 1.0f : scaleRatio);
                float targetMms = modelKmh * 277.7778f; // 1 km/h = 277.7778 mm/s
                // Invert the roster profile to get the required throttle [% 0..1]
                float thrCapPct = throttleForSpeedMms(targetMms, forward);
                // This cap applies to the FINAL applied throttle (after speedFactor),
                // so clamp 'applied' directly to thrCapPct.
                maxApplied = thrCapPct;
            } else {
                // Fallback to the existing throttle % cap
                maxApplied = maxSpeed;
            }

            // Enforce min and max caps
            if (applied > maxApplied) { applied = maxApplied; }
            if (applied < minReliableOperatingSpeed) { applied = minReliableOperatingSpeed; }

            return applied;
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

        /**
         * Set the limits and adjustment factore for train speed.
         * Active train will calculate the required setting and it will be adjusted if not 0.0f
         * required setting * speed Factor  then test for less than max and greater than min.
         * @param minReliableOperatingSpeed lowest throttle % train will reliably move.
         * @param maxSpeed max throttle % for train.
         * @param speedFactor multiplier
         */
        public void setSpeedLimits(float minReliableOperatingSpeed, float maxSpeed, float speedFactor) {
            this.minReliableOperatingSpeed = minReliableOperatingSpeed;
            this.maxSpeed = maxSpeed;
            this.speedFactor = speedFactor;
        }

        public void setTargetSpeed(float distance, float speed) {           
            log.debug("Set Target Speed[{}] with distance{{}] from speed[{}]",speed,distance,throttle.getSpeedSetting());
            stopAllTimers();
            if (rosterEntry != null) {
                rosterEntry.getSpeedProfile().setExtraInitialDelay(1500f);
                rosterEntry.getSpeedProfile().setMinMaxLimits(minReliableOperatingSpeed, maxSpeed);                            
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
            // target already adjusted.
            log.debug("RampToTarget[{}]current[{}]", getTargetSpeed(), throttle.getSpeedSetting());
            stepQueue = new LinkedList<>();
            if (throttle.getSpeedSetting() == getTargetSpeed()) {
                return;
            } else if (throttle.getSpeedSetting() < getTargetSpeed()) {
                // Up (accelerate)
                float newSpeed = throttle.getSpeedSetting();
                if (newSpeed < minReliableOperatingSpeed) {
                    stepQueue.add(new SpeedSetting(minReliableOperatingSpeed, throttleInterval));
                    newSpeed = minReliableOperatingSpeed;
                }
                while (newSpeed < getTargetSpeed()) {
                    newSpeed += speedIncrement;
                    if (newSpeed > getTargetSpeed()) {
                        newSpeed = getTargetSpeed();
                    }
                    log.trace("NewSpeedUp[{}]", newSpeed);
                    stepQueue.add(new SpeedSetting(newSpeed, throttleInterval));
                }
            } else {
                // Down (decelerate)
                boolean andStop = false;
                if (getTargetSpeed() <= 0.0f) {
                    andStop = true;
                }
                float newSpeed = throttle.getSpeedSetting();
                while (newSpeed > getTargetSpeed()) {
                    newSpeed -= speedIncrement;
                    if (newSpeed < getTargetSpeed()) {
                        newSpeed = getTargetSpeed();
                    }
                    log.trace("NewSpeedDown[{}]", newSpeed);
                    stepQueue.add(new SpeedSetting(newSpeed, throttleInterval));
                }
                if (andStop) {
                    stepQueue.add(new SpeedSetting(0.0f, throttleInterval));
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
            targetSpeed = applyMaxThrottleAndFactor(speed);
            throttle.setSpeedSetting(targetSpeed);
        }        

        /**
         * Run a pre-computed throttle/time schedule using the existing stepQueue and rampingTimer.
         * Speeds are throttle percentages [0..1], durations are milliseconds.
         */
        public synchronized void runPlannedSpeedSchedule(float[] throttles, int[] durationsMs) {
            stopAllTimers();
            stepQueue = new LinkedList<>();
            int n = Math.min(throttles.length, durationsMs.length);
            for (int i = 0; i < n; i++) {
                float adj;
                if (AutoActiveTrain.this._stoppingUsingSpeedProfile) {
                    // During a distance-based stop, use the planner's throttle values directly (already clamped to 0..1).
                    adj = clampThrottle(throttles[i]);
                } else {
                    adj = applyMaxThrottleAndFactor(throttles[i]); // normal path
                }
                int dur = Math.max(1, durationsMs[i]);
                stepQueue.add(new SpeedSetting(adj, dur));
            }
            setNextStep();
        }
        
        private void runPhysicsAccelerationPlanner(float targetThrottlePct, boolean forward) {
            // --- Read current speed via profile in MODEL units (mm/s -> m/s) ---
            float throttleNow     = clampThrottle(throttle.getSpeedSetting());
            float v0_mms          = AutoActiveTrain.this.speedMmsFromThrottle(throttleNow, forward);   // model mm/s
            float v0_model_ms     = v0_mms / 1000.0f;                                                  // model m/s
        
            // Target speed from requested throttle (MODEL units)
            float vTarget_mms     = AutoActiveTrain.this.speedMmsFromThrottle(targetThrottlePct, forward); // model mm/s
            float vTarget_model_ms= vTarget_mms / 1000.0f;                                                  // model m/s
        
            // Layout scale ratio (e.g., HO ≈ 87). Reuse ONE local only.
            float scaleRatio = (AutoActiveTrain.this._dispatcher != null)
                    ? (float) AutoActiveTrain.this._dispatcher.getScale().getScaleRatio()
                    : 1.0f;
        
            // Convert MODEL speeds to FULL-SCALE speeds (m/s) for physics math
            float v0_fs         = v0_model_ms * scaleRatio;
            float vTarget_fs    = vTarget_model_ms * scaleRatio;
        
            // Enforce caps in FULL-SCALE km/h space, then convert to FULL-SCALE m/s
            float kmhCapInfo    = AutoActiveTrain.this.getMaxSpeedScaleKmh();                 // 0.0f => disabled
            float kmhCapRoster  = (AutoActiveTrain.this.re != null) ? AutoActiveTrain.this.re.getPhysicsMaxSpeedKmh() : 0.0f;
            float vCap_fs_info  = (kmhCapInfo   > 0.0f) ? (kmhCapInfo   / 3.6f) : Float.POSITIVE_INFINITY;
            float vCap_fs_roster= (kmhCapRoster > 0.0f) ? (kmhCapRoster / 3.6f) : Float.POSITIVE_INFINITY;
            vTarget_fs = Math.min(vTarget_fs, Math.min(vCap_fs_info, vCap_fs_roster));
        
            // Respect min reliable speed floor (MODEL units) → FULL-SCALE
            float vMin_mms      = AutoActiveTrain.this.speedMmsFromThrottle(minReliableOperatingSpeed, forward);
            float vMin_model_ms = vMin_mms / 1000.0f;
            float vMin_fs       = vMin_model_ms * scaleRatio;
        
            // If target below floor, raise to floor
            if (vTarget_fs < vMin_fs) vTarget_fs = vMin_fs;
        
            // --- Physics parameters (FULL-SCALE SI) ---
            float rosterKg = (AutoActiveTrain.this.re != null) ? AutoActiveTrain.this.re.getPhysicsWeightKg() : 0.0f;
            float extraKg  = AutoActiveTrain.this.getAdditionalTrainWeightMetricTonnes() * 1000.0f;
            float massKg   = Math.max(1.0f, rosterKg + extraKg);     // avoid div-by-zero
        
            float g        = 9.80665f;
            float c_rr     = AutoActiveTrain.this.getRollingResistanceCoeff();
            float P_W      = (AutoActiveTrain.this.re != null) ? (AutoActiveTrain.this.re.getPhysicsPowerKw() * 1000.0f) : 0.0f;
            float F_TE     = (AutoActiveTrain.this.re != null) ? (AutoActiveTrain.this.re.getPhysicsTractiveEffortKn() * 1000.0f) : 0.0f;
            RosterEntry.TractionType traction = (AutoActiveTrain.this.re != null)
                    ? AutoActiveTrain.this.re.getPhysicsTractionType()
                    : RosterEntry.TractionType.DIESEL_ELECTRIC;
        
            // --- Integration step: align physics dt to write interval ---
            int   baseMs = Math.max(AutoActiveTrain.this._dispatcher.getMinThrottleInterval(), 50);
            float dt     = baseMs / 1000.0f;
        
            java.util.LinkedList<Float>  thrSteps = new java.util.LinkedList<>();
            java.util.LinkedList<Integer> durSteps = new java.util.LinkedList<>();
        
            // Start at current speed or at floor, in FULL-SCALE
            float v_fs = Math.max(v0_fs, vMin_fs);
            int safety = 0;
        
            // --- DEBUG (optional): uncomment if you want to see starting point ---
            // log.debug("[{}] PHYS start: v0_fs={}, vTarget_fs={}, vMin_fs={}",
            //     AutoActiveTrain.this._activeTrain.getTrainName(), v_fs, vTarget_fs, vMin_fs);
        
            while (v_fs < vTarget_fs && safety < 10000) {
                float v_guard = Math.max(0.01f, v_fs);   // guard 1 cm/s to avoid P/v blow-up
        
                // Drive force model (FULL-SCALE)
                float F_power = (P_W > 0.0f) ? (P_W / v_guard) : Float.POSITIVE_INFINITY;
                float F_drive;
                if (traction == RosterEntry.TractionType.STEAM) {
                    // Steam: constant force until power limit dominates
                    F_drive = Math.min(F_TE, F_power);
                } else {
                    // Diesel/Electric: constant power once rolling, limited by starting TE at low speed
                    F_drive = Math.min((F_TE > 0.0f) ? F_TE : Float.POSITIVE_INFINITY, F_power);
                }
        
                // Rolling resistance and acceleration (FULL-SCALE)
                float F_rr = c_rr * massKg * g;
                float a_fs = (F_drive - F_rr) / massKg;
                if (a_fs < 0.0f) a_fs = 0.0f;
        
                // Predict next speed
                float v_next_fs = v_fs + a_fs * dt;
                if (v_next_fs <= v_fs) break;   // numerical guard
        
                // If this slice overshoots the FINAL target, shorten the slice to land exactly at the target
                float stepDt    = dt;
                boolean finalStep = false;
                if (a_fs > 0.0f && v_next_fs > vTarget_fs) {
                    stepDt    = (vTarget_fs - v_fs) / a_fs;                 // exact time to reach target
                    stepDt    = Math.max(0.001f, stepDt);
                    finalStep = true;
                    v_next_fs = v_fs + a_fs * stepDt;                       // equals vTarget_fs
                }
        
                // Mid-step FULL-SCALE speed for throttle mapping
                float v_mid_fs = 0.5f * (v_fs + v_next_fs);
        
                // Convert mid-step FULL-SCALE speed back to MODEL mm/s for roster profile inversion
                float v_mid_model_ms = v_mid_fs / scaleRatio;
                float v_mid_mms      = v_mid_model_ms * 1000.0f;
        
                float thrApplied = AutoActiveTrain.this.throttleForSpeedMms(v_mid_mms, forward);
                float thrCmd     = (speedFactor > 0.0f) ? (thrApplied / speedFactor) : thrApplied;
                thrCmd           = clampThrottle(clamp(thrCmd, minReliableOperatingSpeed, maxSpeed));
        
                thrSteps.add(Float.valueOf(thrCmd));
                durSteps.add(Integer.valueOf(Math.max(1, Math.round(stepDt * 1000.0f))));
        
                v_fs = v_next_fs;
                safety++;
        
                if (finalStep) break;
            }
        
            // No steps? Just set immediate (use MODEL speed for inversion)
            if (thrSteps.isEmpty()) {
                float finalThr = AutoActiveTrain.this.throttleForSpeedMms(vTarget_model_ms * 1000.0f, forward);
                float thrCmd   = (speedFactor > 0.0f) ? (finalThr / speedFactor) : finalThr;
                thrCmd         = clampThrottle(clamp(thrCmd, minReliableOperatingSpeed, maxSpeed));
                throttle.setSpeedSetting(thrCmd);
                targetSpeed = thrCmd;
                return;
            }
        
            // Execute planned schedule
            int n = thrSteps.size();
            float[] thrArr = new float[n];
            int[]   durArr = new int[n];
            for (int i = 0; i < n; i++) {
                thrArr[i] = thrSteps.get(i).floatValue();
                durArr[i] = Math.max(1, durSteps.get(i).intValue());
            }
            AutoActiveTrain.this._stoppingUsingSpeedProfile = false;
            runPlannedSpeedSchedule(thrArr, durArr);
            targetSpeed = thrArr[n - 1];
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
        } else if (rampRate.equals(Bundle.getMessage("RAMP_PHYSICS"))) {
            return RAMP_PHYSICS;
        }
        return RAMP_NONE;
    }

    /*
     * Listener for switching Ghost blocks to unoccupied
     */
    static class DarkTerritoryListener implements PropertyChangeListener {
        private Sensor sensor;

        public DarkTerritoryListener(Sensor sensor) {
            this.sensor = sensor;
            log.trace("Sensor[{}]",sensor.getDisplayName());
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("state")) {
                ((Block) e.getSource()).removePropertyChangeListener(this);
                if (e.getNewValue().equals(Block.UNOCCUPIED)) {
                    try {
                        log.trace("Sensor INACTIVE[{}]", sensor.getDisplayName());
                        sensor.setKnownState(Sensor.INACTIVE);
                    } catch (jmri.JmriException ex) {
                        log.error("Error leaving darkterratory");
                    }
                }
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AutoActiveTrain.class);
}
