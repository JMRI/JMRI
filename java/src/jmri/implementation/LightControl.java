package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.swing.Timer;
import jmri.InstanceManager;
import jmri.Light;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Timebase;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each LightControl object is linked to a specific Light, and provides one of
 * the controls available for switching the Light ON/OFF in response to time or
 * events occurring on the layout.
 * <p>
 * Each LightControl holds the information for one control of the parent Light.
 * <p>
 * Each Light may have as many controls as desired by the user. It is the user's
 * responsibility to ensure that the various control mechanisms do not conflict
 * with one another.
 * <p>
 * Available control types are those defined in the Light.java interface.
 * Control types: SENSOR_CONTROL FAST_CLOCK_CONTROL TURNOUT_STATUS_CONTROL
 * TIMED_ON_CONTROL TWO_SENSOR_CONTROL
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2010
 */
public class LightControl {

    /**
     * Main constructor methods
     */
    public LightControl() {
    }

    public LightControl(jmri.Light l) {
        _parentLight = l;
    }

    // instance variables - saved with Light in configuration file
    private int _controlType = Light.NO_CONTROL;    // control type
    private String _controlSensorName = "";   // controlling Sensor if SENSOR_CONTROL
    protected int _controlSensorSense = Sensor.ACTIVE;  // sense of Sensor for Light ON
    private int _fastClockOnHour = 0;         // on Hour if FAST_CLOCK_CONTROL
    private int _fastClockOnMin = 0;          // on Minute if FAST_CLOCK_CONTROL
    private int _fastClockOffHour = 0;        // off Hour if FAST_CLOCK_CONTROL
    private int _fastClockOffMin = 0;         // off Minute if FAST_CLOCK_CONTROL
    private String _controlTurnoutName = "";  // turnout whose status is shown if TURNOUT_STATUS_CONTROL
    private int _turnoutState = Turnout.CLOSED;  // turnout state corresponding to this Light ON
    private String _timedSensorName = "";     // trigger Sensor if TIMED_ON_CONTROL
    protected int _timeOnDuration = 0;          // duration (milliseconds) if TIMED_ON_CONTROL
    private String _controlSensor2Name = ""; // second controlling sensor if TWO_SENSOR_CONTROL

    /*
     * Create a New LightControl from existing,
     * for use when editing a LightControl
     *
     * @param lc the LightControl to be copied
     */
    public LightControl(@Nonnull LightControl lc) {
        this._controlType = lc._controlType;
        this._controlSensorName = lc._controlSensorName;
        this._controlSensorSense = lc._controlSensorSense;
        this._fastClockOnHour = lc._fastClockOnHour;
        this._fastClockOnMin = lc._fastClockOnMin;
        this._fastClockOffHour = lc._fastClockOffHour;
        this._fastClockOffMin = lc._fastClockOffMin;
        this._controlTurnoutName = lc._controlTurnoutName;
        this._turnoutState = lc._turnoutState;
        this._timedSensorName = lc._timedSensorName;
        this._timeOnDuration = lc._timeOnDuration;
        this._controlSensor2Name = lc._controlSensor2Name;
    }
    
    /*
     * Test if a LightControl is equal to this one
     *
     * @param o the LightControl object to be checked
     * @return True if the LightControl is equal, else false
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LightControl)) {
            return false;
        }
        LightControl that = (LightControl) o;
        if (that._controlType != this._controlType) return false;
        boolean _shouldReturn = true;
        switch(_controlType) {
            case Light.NO_CONTROL : 
                break;
            case Light.SENSOR_CONTROL : 
                if ((! that._controlSensorName.equals(this._controlSensorName)) ||
                    ( that._controlSensorSense != this._controlSensorSense)) _shouldReturn = false;
                break;
            case Light.FAST_CLOCK_CONTROL : 
                if ((that.getFastClockOffCombined() != this.getFastClockOffCombined()) ||
                    (that.getFastClockOnCombined() != this.getFastClockOnCombined())) _shouldReturn = false;
                break;
            case Light.TURNOUT_STATUS_CONTROL : 
                if ((! that._controlTurnoutName.equals(this._controlTurnoutName)) ||
                    (that._turnoutState != this._turnoutState)) _shouldReturn = false;
                break;
            case Light.TIMED_ON_CONTROL : 
                if ((! that._timedSensorName.equals(this._timedSensorName)) ||
                    (that._timeOnDuration != this._timeOnDuration)) _shouldReturn = false;
                break;
            case Light.TWO_SENSOR_CONTROL : 
                if ((! that._controlSensorName.equals(this._controlSensorName)) ||
                    (that._controlSensorSense != this._controlSensorSense) ||
                    (! that._controlSensor2Name.equals(this._controlSensor2Name))) _shouldReturn = false;
                break;
            default:
                // unexpected _controlType value
                jmri.util.Log4JUtil.warnOnce(log, "Unexpected _controlType = {}", _controlType);
        }
        return _shouldReturn;
    }
    
    @Override
    public int hashCode() {
        // matches with equals() by contract
        return _controlType;
    }

    /*
     * Get the control type used by the Control
     *
     * @return the Control Type, eg. FAST_CLOCK_CONTROL
     */
    public int getControlType() {
        return _controlType;
    }

    /*
     * Set the control type used by the Control
     * Does NOT update any changelisteners
     *
     * @param type the Control Type, eg. FAST_CLOCK_CONTROL
     */
    public void setControlType(int type) {
        _controlType = type;
    }

    /*
     * Set Sensor 1 used by the 1 Sensor and 2 Sensor Control
     * Does NOT update any changelisteners
     * If Sensor not present and name not empty, is provided by the SensorManager
     * when #activateLightControl() is called
     *
     * @param type the Sensor name
     */
    public void setControlSensorName(String sensorName) {
        _controlSensorName = sensorName;
    }

    /*
     * Get the Sensor State used by the 1 Sensor Control
     *
     * @return Sensor.ACTIVE or Sensor.INACTIVE
     */
    public int getControlSensorSense() {
        return _controlSensorSense;
    }

    /*
     * Get the Sensor 1 name for 1 and 2 Sensor Control Types.
     *
     * @return  If a Sensor is registered, returns the Sensor.getName()
     *          else the Sensor Name as set by #setControlSensorName
     */
    public String getControlSensorName() {
        if (_namedControlSensor != null) {
            return _namedControlSensor.getName();
        }
        return _controlSensorName;
    }

    /*
     * Set the Sensor State used by the Control
     * Does NOT update any changelisteners
     *
     * @param sense The state to react to, eg. Sensor.ACTIVE or Sensor.INACTIVE
     */
    public void setControlSensorSense(int sense) {
        if ( sense != Sensor.ACTIVE && sense != Sensor.INACTIVE ) {
            log.error("Incorrect Sensor State Set");
        } else {
            _controlSensorSense = sense;
        }
    }

    /*
     * Get the Fast Clock On Hour.
     *
     * @return  On Hour value
     */
    public int getFastClockOnHour() {
        return _fastClockOnHour;
    }

    /*
     * Get the Fast Clock On Minute.
     *
     * @return  On Minute value
     */
    public int getFastClockOnMin() {
        return _fastClockOnMin;
    }

    /*
     * Get the Fast Clock On Hours and Minutes Combined
     * Convenience method of separate getFastClockOnHour() and getFastClockOnMin()
     * @return  Total combined Minute value
     */
    protected int getFastClockOnCombined() {
        return _fastClockOnHour*60+_fastClockOnMin;
    }

    /*
     * Get the Fast Clock Off Hour.
     *
     * @return  Off Hour value
     */
    public int getFastClockOffHour() {
        return _fastClockOffHour;
    }

    /*
     * Get the Fast Clock Off Minute.
     *
     * @return  Off Minute value
     */
    public int getFastClockOffMin() {
        return _fastClockOffMin;
    }
    
    /*
     * Get the Fast Clock Off Hours and Minutes Combined
     * Convenience method of separate getFastClockOnHour() and getFastClockOnMin()
     * @return  Total combined Minute value
     */
    protected int getFastClockOffCombined() {
        return _fastClockOffHour*60+_fastClockOffMin;
    }

    /*
     * Set a Fast Clock LightControl Schedule.
     *
     * @param onHour Hour the Light should switch On
     * @param onMin Minute the Light should switch On
     * @param offHour Hour the Light should switch Off
     * @param offMin Minute the Light should switch Off     * 
     */
    public void setFastClockControlSchedule(int onHour, int onMin, int offHour, int offMin) {
        _fastClockOnHour = onHour;
        _fastClockOnMin = onMin;
        _fastClockOffHour = offHour;
        _fastClockOffMin = offMin;
    }

    /*
     * Get the LightControl Turnout Name.
     *
     * @return  The Turnout name
     */
    public String getControlTurnoutName() {
        return _controlTurnoutName;
    }

    /*
     * Set the Turnout used by the Control
     * Does NOT update any changelisteners
     * <p>
     * A Turnout of this name is provided by the TurnoutManager 
     * on LightControl Initialisation
     *
     * @param turnoutName The Turnout name
     */
    public void setControlTurnout(String turnoutName) {
        _controlTurnoutName = turnoutName;
    }

    /*
     * Get the LightControl Turnout Name.
     *
     * @return  The Turnout name
     */
    public int getControlTurnoutState() {
        return _turnoutState;
    }

    /*
     * Set the Turnout State used by the Control
     * Does NOT update any changelisteners
     *
     * @param state Turnout state to act on, eg. Turnout.CLOSED or Turnout.THROWN
     */
    public void setControlTurnoutState(int state) {
        if ( state != Turnout.CLOSED && state != Turnout.THROWN ) {
            log.error("Incorrect Turnout State Set");
        } else {
            _turnoutState = state;
        }
    }

    /*
     * Get the Timed On Trigger Sensor name.
     *
     * @return  If a Sensor is registered, returns the Sensor.getName()
     *          else the Sensor Name as set by #setControlTimedOnSensorName
     */
    public String getControlTimedOnSensorName() {
        if (_namedTimedControlSensor != null) {
            return _namedTimedControlSensor.getName();
        }
        return _timedSensorName;
    }

    /*
     * Set Sensor used by the Timed On Control
     * Does NOT update any changelisteners
     *
     * @param sensorName the Sensor name to be used for the On Trigger
     */
    public void setControlTimedOnSensorName(String sensorName) {
        _timedSensorName = sensorName;
    }

    /*
     * Get the Timed On Control Duration
     *
     * @return duration in ms
     */
    public int getTimedOnDuration() {
        return _timeOnDuration;
    }

    /*
     * Set Duration used by the Timed On Control
     * Does NOT update any changeListeners
     *
     * @param duration in ms following the Sensor On Trigger
     */
    public void setTimedOnDuration(int duration) {
        _timeOnDuration = duration;
    }

    /*
     * Get the Second Sensor name.
     * as used in the 2 Sensor Control Group.
     *
     * @return  If a 2nd Sensor is registered, returns the Sensor.getName()
     *          else the 2nd Sensor Name as set by #setControlSensor2Name
     */
    public String getControlSensor2Name() {
        if (_namedControlSensor2 != null) {
            return _namedControlSensor2.getName();
        }
        return _controlSensor2Name;
    }

    /*
     * Set Sensor 2 used by the 2 Sensor Control
     * Does NOT update any changelisteners
     *
     * @param type the Sensor 2 name
     */
    public void setControlSensor2Name(String sensorName) {
        _controlSensor2Name = sensorName;
    }

    /*
     * Set Light to control
     * Does NOT update any changelisteners
     *
     * @param l the Light object to control
     */
    public void setParentLight(Light l) {
        _parentLight = l;
    }

    // operational instance variables - not saved between runs
    private Light _parentLight = null;        // Light that is being controlled   
    private boolean _active = false;
    private NamedBeanHandle<Sensor> _namedControlSensor = null;
    private PropertyChangeListener _sensorListener = null;
    private NamedBeanHandle<Sensor> _namedControlSensor2 = null;
    private PropertyChangeListener _sensor2Listener = null;
    private PropertyChangeListener _timebaseListener = null;
    private Timebase _clock = null;
    private Turnout _controlTurnout = null;
    private PropertyChangeListener _turnoutListener = null;
    private NamedBeanHandle<Sensor> _namedTimedControlSensor = null;
    private PropertyChangeListener _timedSensorListener = null;
    private Timer _timedControlTimer = null;
    private java.awt.event.ActionListener _timedControlListener = null;
    private int _timeNow;
    private PropertyChangeListener _parentLightListener = null;
    private final jmri.NamedBeanHandleManager nbhm = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    /**
     * Get a Textual Description
     * eg. Light Control TestLight ON when TestSensor is Active
     * eg. Light Control ON at 14:00, OFF at 15:00.
     * 
     * @param lightName the Light Name, can be empty.
     * @return An I18N full-text description of thiscontrol
     */
    public String getDescriptionText(String lightName){
        StringBuilder name = new StringBuilder(jmri.jmrit.beantable.LightTableAction.lightControlTitle);
        name.append(" ");
        name.append(lightName);
        name.append(" ");
        name.append(jmri.jmrit.beantable.LightTableAction.getDescriptionText(this, getControlType()));
        return name.toString();
    }

    /**
     * Activates a Light Control by control type. This method tests the control
     * type, and set up a control mechanism, appropriate for the control type.
     * Adds PropertyChangeListeners to Sensors / Turnout / Fast Clock as necessary
     */
    public void activateLightControl() {
        // skip if Light Control is already active
        if (_active) {
            return;
        }
            
        if (_parentLight == null){
            log.error("No Parent Light when activating LightControl");
            return;
        }
        
        // register LightControl with Parent Light to indicate Control
        // in use if user attempts to delete light
        _parentLight.addPropertyChangeListener(
            _parentLightListener = (PropertyChangeEvent e) -> {
        },_parentLight.toString(), getDescriptionText("") );
        
        // activate according to control type
        switch (_controlType) {
            case Light.SENSOR_CONTROL:
                _namedControlSensor = null;
                if (!_controlSensorName.isEmpty()) {
                    Sensor sen = InstanceManager.sensorManagerInstance().
                            provideSensor(_controlSensorName);
                    _namedControlSensor = nbhm.getNamedBeanHandle(_controlSensorName, sen);
                }
                if (_namedControlSensor != null) {
                    // if sensor state is currently known, set light accordingly
                    oneSensorChanged( _namedControlSensor.getBean().getKnownState() );
                    // listen for change in sensor state
                    _namedControlSensor.getBean().addPropertyChangeListener(
                        _sensorListener = (PropertyChangeEvent e) -> {
                            if (e.getPropertyName().equals("KnownState")) {
                                oneSensorChanged( (int) e.getNewValue() );
                            }
                    }, _controlSensorName, getDescriptionText(_parentLight.getDisplayName()));
                    _active = true;
                } else {
                    // control sensor does not exist
                    log.error("Light {} is linked to a Sensor that does not exist: {}",
                        _parentLight.getSystemName(), _controlSensorName);
                }
                break;
            case Light.FAST_CLOCK_CONTROL:
                if (areFollowerTimesFaulty(_parentLight.getLightControlList())){
                    log.error("Light has multiple actions for the same time in {}",
                        getDescriptionText(_parentLight.getDisplayName()));
                }
                if (_clock == null) {
                    _clock = InstanceManager.getDefault(jmri.Timebase.class);
                }
                // initialize light based on current fast time
                updateClockControlLightFollower();
                // set up to listen for time changes on a minute basis
                _clock.addMinuteChangeListener(
                    _timebaseListener = (PropertyChangeEvent e) -> {
                        updateClockControlLightFollower();
                    });
                _active = true;
                break;
            case Light.TURNOUT_STATUS_CONTROL:
                try {
                    _controlTurnout = InstanceManager.turnoutManagerInstance().
                            provideTurnout(_controlTurnoutName);
                } catch (IllegalArgumentException e) {
                    // control turnout does not exist
                    log.error("Light " + _parentLight.getSystemName()
                            + " is linked to a Turnout that does not exist: " + _controlSensorName);
                    return;
                }
                // set light based on current turnout state if known
                oneTurnoutChanged( _controlTurnout.getKnownState() );
                // listen for change in turnout state
                _controlTurnout.addPropertyChangeListener(
                    _turnoutListener = (PropertyChangeEvent e) -> {
                        if (e.getPropertyName().equals("KnownState")) {
                            oneTurnoutChanged( (int) e.getNewValue() );
                        }
                    }, _controlTurnoutName, getDescriptionText(_parentLight.getDisplayName()));
                _active = true;
                break;
            case Light.TIMED_ON_CONTROL:
                if (!_timedSensorName.isEmpty()) {
                    Sensor sen = InstanceManager.sensorManagerInstance().
                            provideSensor(_timedSensorName);
                    _namedTimedControlSensor = nbhm.getNamedBeanHandle(_timedSensorName, sen);
                }
                if (_namedTimedControlSensor != null) {
                    if (_parentLight.getEnabled()) {
                        // set initial state off
                        _parentLight.setState(Light.OFF);
                    }
                    
                    addNamedTimedControlListener();
                    // listen for change in timed control sensor state
                    _active = true;
                } else {
                    // timed control sensor does not exist
                    log.error("Light " + _parentLight.getSystemName()
                            + " is linked to a Sensor that does not exist: " + _timedSensorName);
                }
                break;
            case Light.TWO_SENSOR_CONTROL:
                _namedControlSensor = null;
                _namedControlSensor2 = null;
                if (!_controlSensorName.isEmpty()) {
                    Sensor sen = InstanceManager.sensorManagerInstance().
                            provideSensor(_controlSensorName);
                    _namedControlSensor = nbhm.getNamedBeanHandle(_controlSensorName, sen);
                }
                if (!_controlSensor2Name.isEmpty()) {
                    Sensor sen = InstanceManager.sensorManagerInstance().
                            provideSensor(_controlSensor2Name);
                    _namedControlSensor2 = nbhm.getNamedBeanHandle(_controlSensor2Name, sen);
                }
                if ((_namedControlSensor != null) && (_namedControlSensor2 != null)) {
                    // if sensor state is currently known, set light accordingly
                    twoSensorChanged();
                    // listen for change in sensor states
                    _sensorListener = addTwoSensorListener(_namedControlSensor.getBean());
                    _sensor2Listener = addTwoSensorListener(_namedControlSensor2.getBean());
                    _active = true;
                } else {
                    // at least one control sensor does not exist
                    log.error("Light " + _parentLight.getSystemName()
                            + " with 2 Sensor Control is linked to a Sensor that does not exist.");
                }
                break;
            default:
                log.error("Unexpected control type when activating Light: {}", _parentLight);
        }
        
    }
    
    /**
     * Property Change Listener for Two Sensor.
     */
    private PropertyChangeListener addTwoSensorListener(Sensor sensor) {
        PropertyChangeListener pcl;
        sensor.addPropertyChangeListener(
            pcl = (PropertyChangeEvent e) -> {
                if (e.getPropertyName().equals("KnownState")) {
                    twoSensorChanged();
                }
            }, sensor.getDisplayName(), getDescriptionText(_parentLight.getDisplayName()));
        return pcl;
    }
    
    /**
     * Add a Timed Control Listener to a Sensor.
     * 
     */
    private void addNamedTimedControlListener(){
        _namedTimedControlSensor.getBean().addPropertyChangeListener(
            _timedSensorListener = (PropertyChangeEvent e) -> {
                if (e.getPropertyName().equals("KnownState")
                    && (int) e.getNewValue() == Sensor.ACTIVE
                    && _timedControlTimer == null 
                    && _parentLight.getEnabled()) {
                    // Turn light on
                    _parentLight.setState(Light.ON);
                    // Create a timer if one does not exist
                    _timedControlListener = new TimeLight();
                    _timedControlTimer = new Timer(_timeOnDuration,
                            _timedControlListener);
                    // Start the Timer to turn the light OFF
                    _timedControlTimer.start();
                }
            }, 
        _timedSensorName, getDescriptionText(_parentLight.getDisplayName()));
    }

    /**
     * Internal routine for handling sensor change or startup
     * for the 1 Sensor Control Type
     */
    private void oneSensorChanged(int newSensorState){
        if (!_parentLight.getEnabled()) {
            return;  // ignore property change if user disabled Light
        }
        if (newSensorState == Sensor.ACTIVE) {
            if (_controlSensorSense == Sensor.ACTIVE) {
                // Turn light on
                _parentLight.setState(Light.ON);
            } else {
                // Turn light off
                _parentLight.setState(Light.OFF);
            }
        } else if (newSensorState == Sensor.INACTIVE) {
            if (_controlSensorSense == Sensor.INACTIVE) {
                // Turn light on
                _parentLight.setState(Light.ON);
            } else {
                // Turn light off
                _parentLight.setState(Light.OFF);
            }
        }
    }
    
    /**
     * Internal routine for handling Turnout change or startup
     * for the TURNOUT_STATUS_CONTROL Control Type
     */
    private void oneTurnoutChanged(int newTurnoutState){
        if (!_parentLight.getEnabled()) {
            return;  // ignore property change if user disabled light
        }
        if (newTurnoutState == Turnout.CLOSED) {
            if (_turnoutState == Turnout.CLOSED) {
                // Turn light on
                _parentLight.setState(Light.ON);
            } else {
                // Turn light off
                _parentLight.setState(Light.OFF);
            }
        } else if (newTurnoutState == Turnout.THROWN) {
            if (_turnoutState == Turnout.THROWN) {
                // Turn light on
                _parentLight.setState(Light.ON);
            } else {
                // Turn light off
                _parentLight.setState(Light.OFF);
            }
        }
    }

    /**
     * Internal routine for handling sensor changes
     * for the 2 Sensor Control Type
     */
    protected void twoSensorChanged() {
        if (!_parentLight.getEnabled()) {
            return;  // ignore property change if user disabled Light
        }
        int kState = _namedControlSensor.getBean().getKnownState();
        int kState2 = _namedControlSensor2.getBean().getKnownState();
        if (_controlSensorSense == Sensor.ACTIVE) {
            if ((kState == Sensor.ACTIVE) || (kState2 == Sensor.ACTIVE)) {
                // Turn light on
                _parentLight.setState(Light.ON);
            } else {
                // Turn light off
                _parentLight.setState(Light.OFF);
            }
        } else if (_controlSensorSense == Sensor.INACTIVE) {
            if ((kState == Sensor.INACTIVE) || (kState2 == Sensor.INACTIVE)) {
                // Turn light on
                _parentLight.setState(Light.ON);
            } else {
                // Turn light off
                _parentLight.setState(Light.OFF);
            }
        }
    }

    /**
     * Internal routine for seeing if we have the latest time to control the FastClock Follower.
     * <p>
     * Takes previous day times
     * 
     * @return True if we have the most recent time ( either on or off ), otherwise False.
     */
    private boolean isMasterFastClockFollower(){
        List<Integer> otherControlTimes= new ArrayList<>();
        List<Integer> thisControlTimes= new ArrayList<>();
        
        // put all other times in a single List to compare
        _parentLight.getLightControlList().forEach((otherLc) -> {
            if (otherLc!=this && otherLc.getControlType()==Light.FAST_CLOCK_CONTROL) {
                // by adding 1440 mins to the today times, we can check yesterday in the same list.
                otherControlTimes.add( otherLc.getFastClockOnCombined() ); // yesterdayOnTime
                otherControlTimes.add( otherLc.getFastClockOffCombined() ); // yesterdayOffTime
                otherControlTimes.add( otherLc.getFastClockOnCombined()+1440 ); // todayOnTime
                otherControlTimes.add( otherLc.getFastClockOffCombined()+1440 ); // todayOffTime
            }
        });
        // log.debug("{} other control times in list {}",otherControlTimes.size(),otherControlTimes);
        
        thisControlTimes.add( getFastClockOnCombined() ); // yesterdayOnTime
        thisControlTimes.add( getFastClockOffCombined() ); // yesterdayOffTime
        thisControlTimes.add( getFastClockOnCombined()+1440 ); // todayOnTime
        thisControlTimes.add( getFastClockOffCombined()+1440 ); // todayOffTime
        
        otherControlTimes.removeIf( e -> ( e > ( _timeNow +1440 ) )); // remove future times
        thisControlTimes.removeIf( e -> ( e > ( _timeNow +1440 ) )); // remove future times
        
        if (otherControlTimes.isEmpty()){
            return true;
        }
        return Collections.max(thisControlTimes) >= Collections.max(otherControlTimes);
    }
    
    /**
     * Check to see if we have the FastClock Follower has unique times for a single Light Control.
     * <p>
     * Hour / Minute combination must be unique for each Light to avoid flicker.
     * 
     * @return true if the clock on time equals the off time, otherwise false.
     */
    public boolean onOffTimesFaulty() {
        return (getFastClockOnCombined()==getFastClockOffCombined());
    }
    
    /**
     * @param time Combined hours / mins to check against.
     */
    private Predicate<LightControl> isFastClockEqual(int time) {
        return p -> ( !(p==this) && (
            p.getFastClockOnCombined() == time || p.getFastClockOffCombined() == time ) );
    }
    
    /**
     * Check to see if we have the FastClock Follower has unique times for a single Light.
     * <p>
     * Hour / Minute combination must be unique for each Light to avoid flicker.
     * 
     * @param compareList the ArrayList of other Light Controls to compare against
     * @return true if there are multiple exact same times
     */
    public boolean areFollowerTimesFaulty( ArrayList<LightControl> compareList ) {
        if (onOffTimesFaulty()){
            return true;
        }
        return (compareList.stream().anyMatch(isFastClockEqual(getFastClockOnCombined())) || 
            compareList.stream().anyMatch(isFastClockEqual(getFastClockOffCombined())));
    }
    
    /**
     * Updates the local int of the FastClock Time
     */
    @SuppressWarnings("deprecation")
    private void setTheTime(){
        Date now = _clock.getTime();
        _timeNow = now.getHours() * 60 + now.getMinutes();
    }

    /**
     * Updates the status of a Light under FAST_CLOCK_CONTROL. This method is
     * called every FastClock minute.
     */
    private void updateClockControlLightFollower() {
        if (!_parentLight.getEnabled()) {
            return;  // ignore property change if user disabled Light
        }
        if (_clock != null) {
            setTheTime();
            // log.debug("updateClockControl, now is {} master {}",_timeNow,isMasterFastClockFollower());
            if (!isMasterFastClockFollower()){
                return;
            }
            int state = _parentLight.getState();
            if (getFastClockOnCombined() <= getFastClockOffCombined()) {
                // on and off the same day
                if ((_timeNow < getFastClockOnCombined()) || (_timeNow >= getFastClockOffCombined())) {
                    // Light should be OFF
                    if (state == Light.ON) {
                        logTimeChanges("OFF");
                        _parentLight.setState(Light.OFF);
                    }
                } else {
                    // Light should be ON
                    if (state == Light.OFF) {
                        logTimeChanges("ON");
                        _parentLight.setState(Light.ON);
                    }
                }
            } else {
                // on and off - different days
                if ((_timeNow >= getFastClockOnCombined()) || (_timeNow < getFastClockOffCombined())) {
                    // Light should be ON
                    if (state == Light.OFF) {
                        logTimeChanges("ON");
                        _parentLight.setState(Light.ON);
                    }
                } else {
                    // Light should be OFF
                    if (state == Light.ON) {
                        logTimeChanges("OFF");
                        _parentLight.setState(Light.OFF);
                    }
                }
            }
        }
    }
    
    /**
     * Outputs Time and Light Change info to log file.
     * eg Output "DEBUG - 11:05 Setting Light My Light 2751 OFF"
     */
    private void logTimeChanges(String onOrOff){
        log.debug("{}:{} Setting Light {} {}",
            (_timeNow/60),String.format("%02d", (_timeNow % 60)),
            _parentLight.getDisplayName(),onOrOff);
    }

    /**
     * Deactivates a LightControl by control type. This method tests the control
     * type, and deactivates the control mechanism, appropriate for the control
     * type.
     */
    public void deactivateLightControl() {
        // skip if Light Control is not active
        if (_active) {
            _parentLight.removePropertyChangeListener(_parentLightListener);
            if (_sensorListener != null) {
                _namedControlSensor.getBean().removePropertyChangeListener(_sensorListener);
                _sensorListener = null;
            }
            if ((_clock != null) && (_timebaseListener != null)) {
                _clock.removeMinuteChangeListener(_timebaseListener);
                _timebaseListener = null;
            }
            if (_turnoutListener != null) {
                _controlTurnout.removePropertyChangeListener(_turnoutListener);
                _turnoutListener = null;
            }
            if (_timedSensorListener != null) {
                _namedTimedControlSensor.getBean().removePropertyChangeListener(_timedSensorListener);
                _timedSensorListener = null;
            }
            if (_timedControlTimer != null) {
                _timedControlTimer.stop();
                if (_timedControlListener != null) {
                    _timedControlTimer.removeActionListener(_timedControlListener);
                    _timedControlListener = null;
                }
                _timedControlTimer = null;
            }
            if (_sensor2Listener != null) {
                _namedControlSensor2.getBean().removePropertyChangeListener(_sensor2Listener);
                _sensor2Listener = null;
            }
            _active = false;
        }
    }

    /**
     * Class for defining ActionListener for TIMED_ON_CONTROL
     */
    private class TimeLight implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            // Turn Light OFF
            _parentLight.setState(Light.OFF);
            // Turn Timer OFF
            _timedControlTimer.stop();
            _timedControlTimer = null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LightControl.class);
}
