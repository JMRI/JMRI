// AbstractTurnout.java
package jmri.implementation;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.PushbuttonPacket;
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.TurnoutOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for the Turnout interface.
 * <P>
 * Implements basic feedback modes:
 * <UL>
 * <LI>NONE feedback, where the KnownState and CommandedState track each other.
 * <LI>ONESENSOR feedback where the state of a single sensor specifies THROWN vs
 * CLOSED
 * <LI>TWOSENSOR feedback, where one sensor specifies THROWN and another CLOSED.
 * </UL>
 * If you want to implement some other feedback, override and modify
 * setCommandedState() here.
 * <P>
 * Implements the parameter binding support.
 * <P>
 * Note that we consider it an error for there to be more than one object that
 * corresponds to a particular physical turnout on the layout.
 * <p>
 * Turnout system names are always upper case.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2009
 * @version $Revision$
 */
public abstract class AbstractTurnout extends AbstractNamedBean implements
        Turnout, java.io.Serializable, java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -1138214787466499528L;

    protected AbstractTurnout(String systemName) {
        super(systemName.toUpperCase());
    }

    protected AbstractTurnout(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameTurnout");
    }

    /**
     * Handle a request to change state, typically by sending a message to the
     * layout in some child class. Public version (used by TurnoutOperator)
     * sends the current commanded state without changing it.
     *
     * @param s new state value
     */
    abstract protected void forwardCommandChangeToLayout(int s);

    protected void forwardCommandChangeToLayout() {
        forwardCommandChangeToLayout(_commandedState);
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					       	Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state
    /**
     * Sets a new Commanded state, if need be notifying the listeners, but does
     * NOT send the command downstream. This is used when a new commanded state
     * is noticed from another command.
     */
    protected void newCommandedState(int s) {
        if (_commandedState != s) {
            int oldState = _commandedState;
            _commandedState = s;
            firePropertyChange("CommandedState", Integer.valueOf(oldState),
                    Integer.valueOf(_commandedState));
        }
    }

    public int getKnownState() {
        return _knownState;
    }

    /**
     * Public access to changing turnout state. Sets the commanded state and, if
     * appropriate starts a TurnoutOperator to do its thing. If there is no
     * TurnoutOperator (not required or nothing suitable) then just tell the
     * layout and hope for the best.
     */
    public void setCommandedState(int s) {
        log.debug("set commanded state for turnout " + getSystemName() + " to "
                + s);
        newCommandedState(s);
        myOperator = getTurnoutOperator(); // MUST set myOperator before starting the thread
        if (myOperator == null) {
            forwardCommandChangeToLayout(s);
            // optionally handle feedback
            if (_activeFeedbackType == DIRECT) {
                newKnownState(s);
            }
        } else {
            myOperator.start();
        }
    }

    public int getCommandedState() {
        return _commandedState;
    }

    /**
     * Add a protected newKnownState() for use by implementations.
     * <P>
     * Use this to update internal information when a state change is detected
     * <em>outside</em> the Turnout object, e.g. via feedback from sensors on
     * the layout.
     * <P>
     * If the layout status of the Turnout is observed to change to THROWN or
     * CLOSED, this also sets the commanded state, because it's assumed that
     * somebody somewhere commanded that move. If it's observed to change to
     * UNKNOWN or INCONSISTENT, that's perhaps either an error or a move in
     * progress, and no change is made to the commanded state.
     * <P>
     * This implementation sends a command to the layout for the new state if
     * going to THROWN or CLOSED, because there may be others listening to
     * network state.
     * <P>
     * Not intended for general use, e.g. for users to set the KnownState.
     *
     * @param s New state value
     */
    protected void newKnownState(int s) {
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", Integer.valueOf(oldState),
                    Integer.valueOf(_knownState));
        }
        // if known state has moved to Thrown or Closed,
        // set the commanded state to match
        if ((_knownState == THROWN && _commandedState != THROWN)
                || (_knownState == CLOSED && _commandedState != CLOSED)) {
            newCommandedState(_knownState);
        }
    }

    /**
     * Show whether state is one you can safely run trains over
     *
     * @return	true iff state is a valid one and the known state is the same as
     *         commanded
     */
    public boolean isConsistentState() {
        return _commandedState == _knownState
                && (_commandedState == CLOSED || _commandedState == THROWN);
    }

    /**
     * The name pretty much says it.
     * <P>
     * Triggers all listeners, etc. For use by the TurnoutOperator classes.
     */
    void setKnownStateToCommanded() {
        newKnownState(_commandedState);
    }

    /**
     * Implement a shorter name for setCommandedState.
     * <P>
     * This generally shouldn't be used by Java code; use setCommandedState
     * instead. The is provided to make Jython script access easier to read.
     * <P>
     * Note that getState() and setState(int) are not symmetric: getState is the
     * known state, and set state modifies the commanded state.
     */
    public void setState(int s) {
        setCommandedState(s);
    }

    /**
     * Implement a shorter name for getKnownState.
     * <P>
     * This generally shouldn't be used by Java code; use getKnownState instead.
     * The is provided to make Jython script access easier to read.
     * <P>
     * Note that getState() and setState(int) are not symmetric: getState is the
     * known state, and set state modifies the commanded state.
     */
    public int getState() {
        return getKnownState();
    }

    protected String[] _validFeedbackNames = {"DIRECT", "ONESENSOR",
        "TWOSENSOR"};

    protected int[] _validFeedbackModes = {DIRECT, ONESENSOR, TWOSENSOR};

    protected int _validFeedbackTypes = DIRECT | ONESENSOR | TWOSENSOR;

    protected int _activeFeedbackType = DIRECT;

    private int _knownState = UNKNOWN;

    private int _commandedState = UNKNOWN;

    private int _numberOutputBits = 1;

    /* Number of bits to control a turnout - defaults to one */
    private int _controlType = 0;

    /* Type of turnout control - defaults to 0 for 'steady state' */
    public int getNumberOutputBits() {
        return _numberOutputBits;
    }

    public void setNumberOutputBits(int num) {
        _numberOutputBits = num;
    }

    public int getControlType() {
        return _controlType;
    }

    public void setControlType(int num) {
        _controlType = num;
    }

    public int getValidFeedbackTypes() {
        return _validFeedbackTypes;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public String[] getValidFeedbackNames() {
        return _validFeedbackNames;
    }

    public void setFeedbackMode(String mode) throws IllegalArgumentException {
        for (int i = 0; i < _validFeedbackNames.length; i++) {
            if (mode.equals(_validFeedbackNames[i])) {
                setFeedbackMode(_validFeedbackModes[i]);
                return;
            }
        }
        throw new IllegalArgumentException("Unexpected mode: " + mode);
    }

    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        // check for error - following removed the low bit from mode
        int test = mode & (mode - 1);
        if (test != 0) {
            throw new IllegalArgumentException("More than one bit set: " + mode);
        }
        // set the value
        int oldMode = _activeFeedbackType;
        _activeFeedbackType = mode;
        if (oldMode != _activeFeedbackType) {
            firePropertyChange("feedbackchange", Integer.valueOf(oldMode),
                    Integer.valueOf(_activeFeedbackType));
        }
        // unlock turnout if feedback is changed 
        setLocked(CABLOCKOUT, false);
    }

    public int getFeedbackMode() {
        return _activeFeedbackType;
    }

    public String getFeedbackModeName() {
        for (int i = 0; i < _validFeedbackNames.length; i++) {
            if (_activeFeedbackType == _validFeedbackModes[i]) {
                return _validFeedbackNames[i];
            }
        }
        throw new IllegalArgumentException("Unexpected internal mode: "
                + _activeFeedbackType);
    }

    public void setInverted(boolean inverted) {
        boolean oldInverted = _inverted;
        _inverted = inverted;
        if (oldInverted != _inverted) {
            firePropertyChange("inverted", Boolean.valueOf(oldInverted),
                    Boolean.valueOf(_inverted));
            int state = _knownState;
            if (state == THROWN) {
                newKnownState(CLOSED);
            } else if (state == CLOSED) {
                newKnownState(THROWN);
            }

        }
    }

    /**
     * Get the turnout inverted state. If true, commands sent to the layout are
     * reversed. Thrown becomes Closed, and Closed becomes Thrown.
     * <P>
     * Used in polling loops in system-specific code, so made final to allow
     * optimization.
     */
    final public boolean getInverted() {
        return _inverted;
    }

    protected boolean _inverted = false;

    /**
     * Determine if the turnouts can be inverted. If true inverted turnouts
     * supported.
     */
    public boolean canInvert() {
        return false;
    }

    /**
     * Turnouts that are locked should only respond to JMRI commands to change
     * state. We simulate a locked turnout by monitoring the known state
     * (turnout feedback is required) and if we detect that the known state has
     * changed, negate it by forcing the turnout to return to the commanded
     * state. Turnout that have local buttons can also be locked if their
     * decoder supports it.
     *
     * @param locked
     */
    public void setLocked(int turnoutLockout, boolean locked) {
        boolean firechange = false;
        if ((turnoutLockout & CABLOCKOUT) > 0 && _cabLockout != locked) {
            firechange = true;
            if (canLock(CABLOCKOUT)) {
                _cabLockout = locked;
            } else {
                _cabLockout = false;
            }
        }
        if ((turnoutLockout & PUSHBUTTONLOCKOUT) > 0
                && _pushButtonLockout != locked) {
            firechange = true;
            if (canLock(PUSHBUTTONLOCKOUT)) {
                _pushButtonLockout = locked;
                // now change pushbutton lockout state on layout
                turnoutPushbuttonLockout();
            } else {
                _pushButtonLockout = false;
            }
        }
        if (firechange) {
            firePropertyChange("locked", Boolean.valueOf(!locked), Boolean.valueOf(
                    locked));
        }
    }

    /**
     * Determine if turnout is locked. Returns true if turnout is locked. There
     * are two types of locks, cab lockout, and pushbutton lockout.
     */
    public boolean getLocked(int turnoutLockout) {
        if (turnoutLockout == CABLOCKOUT) {
            return _cabLockout;
        } else if (turnoutLockout == PUSHBUTTONLOCKOUT) {
            return _pushButtonLockout;
        } else if (turnoutLockout == (CABLOCKOUT + PUSHBUTTONLOCKOUT)) {
            return _cabLockout || _pushButtonLockout;
        } else {
            return false;
        }

    }

    protected boolean _cabLockout = false;

    protected boolean _pushButtonLockout = false;

    protected boolean _enableCabLockout = false;

    protected boolean _enablePushButtonLockout = false;

    public boolean canLock(int turnoutLockout) {
        return false;
    }

    public void enableLockOperation(int turnoutLockout, boolean enabled) {
    }

    /**
     * When true, report to console anytime a cab attempts to change the state
     * of a turnout on the layout. When a turnout is cab locked, only JMRI is
     * allowed to change the state of a turnout.
     */
    public void setReportLocked(boolean reportLocked) {
        boolean oldReportLocked = _reportLocked;
        _reportLocked = reportLocked;
        if (oldReportLocked != _reportLocked) {
            firePropertyChange("reportlocked", Boolean.valueOf(oldReportLocked),
                    Boolean.valueOf(_reportLocked));
        }
    }

    /**
     * When true, report to console anytime a cab attempts to change the state
     * of a turnout on the layout. When a turnout is cab locked, only JMRI is
     * allowed to change the state of a turnout.
     */
    public boolean getReportLocked() {
        return _reportLocked;
    }

    protected boolean _reportLocked = true;

    /**
     * Valid stationary decoder names
     */
    protected String[] _validDecoderNames = PushbuttonPacket
            .getValidDecoderNames();

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public String[] getValidDecoderNames() {
        return _validDecoderNames;
    }

    // set the turnout decoder default to unknown
    protected String _decoderName = PushbuttonPacket.unknown;

    public String getDecoderName() {
        return _decoderName;
    }

    public void setDecoderName(String decoderName) {
        _decoderName = decoderName;
    }

    abstract protected void turnoutPushbuttonLockout(boolean locked);

    protected void turnoutPushbuttonLockout() {
        turnoutPushbuttonLockout(_pushButtonLockout);
    }

    /*
     * Support for turnout automation (see TurnoutOperation and related classes)
     */
    protected TurnoutOperator myOperator;

    protected TurnoutOperation myTurnoutOperation;

    protected boolean inhibitOperation = true; // do not automate this turnout, even if globally operations are on

    public TurnoutOperator getCurrentOperator() {
        return myOperator;
    }

    public TurnoutOperation getTurnoutOperation() {
        return myTurnoutOperation;
    }

    public void setTurnoutOperation(TurnoutOperation toper) {
        if (log.isDebugEnabled()) {
            log.debug("setTurnoutOperation Called for turnout {}.  Operation type {}", this.getSystemName(), toper);
        }
        TurnoutOperation oldOp = myTurnoutOperation;
        if (myTurnoutOperation != null) {
            myTurnoutOperation.removePropertyChangeListener(this);
        }
        myTurnoutOperation = toper;
        if (myTurnoutOperation != null) {
            myTurnoutOperation.addPropertyChangeListener(this);
        }
        firePropertyChange("TurnoutOperationState", oldOp, myTurnoutOperation);
    }

    protected void operationPropertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getSource() == myTurnoutOperation) {
            if (((TurnoutOperation) evt.getSource()).isDeleted()) {
                setTurnoutOperation(null);
            }
        }
    }

    public boolean getInhibitOperation() {
        return inhibitOperation;
    }

    public void setInhibitOperation(boolean io) {
        inhibitOperation = io;
    }

    /**
     * find the TurnoutOperation class for this turnout, and get an instance of
     * the corresponding operator Override this function if you want another way
     * to choose the operation
     *
     * @return	newly-instantiated TurnoutOPerator, or null if nothing suitable
     */
    protected TurnoutOperator getTurnoutOperator() {
        TurnoutOperator to = null;
        if (!inhibitOperation) {
            if (myTurnoutOperation != null) {
                to = myTurnoutOperation.getOperator(this);
            } else {
                TurnoutOperation toper = TurnoutOperationManager.getInstance()
                        .getMatchingOperation(this,
                                getFeedbackModeForOperation());
                if (toper != null) {
                    to = toper.getOperator(this);
                }
            }
        }
        return to;
    }

    /**
     * Allow an actual turnout class to transform private feedback types into
     * ones that the generic turnout operations know about
     *
     * @return	apparent feedback mode for operation lookup
     */
    protected int getFeedbackModeForOperation() {
        return getFeedbackMode();
    }

    /*
     * support for associated sensor or sensors
     */
    //Sensor getFirstSensor() = null;
    private NamedBeanHandle<Sensor> _firstNamedSensor;

    //Sensor getSecondSensor() = null;
    private NamedBeanHandle<Sensor> _secondNamedSensor;

    public void provideFirstFeedbackSensor(String pName) throws jmri.JmriException {
        if (InstanceManager.sensorManagerInstance() != null) {
            if (pName == null || pName.equals("")) {
                provideFirstFeedbackNamedSensor(null);
            } else {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                if (sensor != null) {
                    provideFirstFeedbackNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
                } else {
                    log.error("Sensor '" + pName + "' not available");
                    throw new jmri.JmriException("Sensor '" + pName + "' not available");
                }
            }
        } else {
            log.error("No SensorManager for this protocol");
            throw new jmri.JmriException("No Sensor Manager Found");
        }
    }

    public void provideFirstFeedbackNamedSensor(NamedBeanHandle<Sensor> s) {
        if (getFirstSensor() != null) {
            getFirstSensor().removePropertyChangeListener(this);
        }

        _firstNamedSensor = s;

        // if need be, set listener
        if (getFirstSensor() != null) {
            getFirstSensor().addPropertyChangeListener(this, s.getName(), "Feedback Sensor for " + getDisplayName());
        }

    }

    public Sensor getFirstSensor() {
        if (_firstNamedSensor == null) {
            return null;
        }
        return _firstNamedSensor.getBean();
    }

    public NamedBeanHandle<Sensor> getFirstNamedSensor() {
        return _firstNamedSensor;
    }

    public void provideSecondFeedbackSensor(String pName) throws jmri.JmriException {
        if (InstanceManager.sensorManagerInstance() != null) {
            if (pName == null || pName.equals("")) {
                provideSecondFeedbackNamedSensor(null);
            } else {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                if (sensor != null) {
                    provideSecondFeedbackNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
                } else {
                    log.error("Sensor '" + pName + "' not available");
                    throw new jmri.JmriException("Sensor '" + pName + "' not available");
                }
            }
        } else {
            log.error("No SensorManager for this protocol");
            throw new jmri.JmriException("No Sensor Manager Found");
        }
    }

    public void provideSecondFeedbackNamedSensor(NamedBeanHandle<Sensor> s) {
        // if need be, clean listener
        if (getSecondSensor() != null) {
            getSecondSensor().removePropertyChangeListener(this);
        }

        _secondNamedSensor = s;

        // if need be, set listener
        if (getSecondSensor() != null) {
            getSecondSensor().addPropertyChangeListener(this, s.getName(), "Feedback Sensor for " + getDisplayName());
        }
    }

    public Sensor getSecondSensor() {
        if (_secondNamedSensor == null) {
            return null;
        }
        return _secondNamedSensor.getBean();
    }

    public NamedBeanHandle<Sensor> getSecondNamedSensor() {
        return _secondNamedSensor;
    }

    public void setInitialKnownStateFromFeedback() {
        if (_activeFeedbackType == ONESENSOR) {
            // ONESENSOR feedback 
            if (getFirstSensor() != null) {
                // set according to state of sensor
                int sState = getFirstSensor().getKnownState();
                if (sState == Sensor.ACTIVE) {
                    newKnownState(THROWN);
                } else if (sState == Sensor.INACTIVE) {
                    newKnownState(CLOSED);
                }
            } else {
                log.warn("expected Sensor 1 not defined - " + getSystemName());
                newKnownState(UNKNOWN);
            }
        } else if (_activeFeedbackType == TWOSENSOR) {
            // TWOSENSOR feedback
            int s1State = Sensor.UNKNOWN;
            int s2State = Sensor.UNKNOWN;
            if (getFirstSensor() != null) {
                s1State = getFirstSensor().getKnownState();
            } else {
                log.warn("expected Sensor 1 not defined - " + getSystemName());
            }
            if (getSecondSensor() != null) {
                s2State = getSecondSensor().getKnownState();
            } else {
                log.warn("expected Sensor 2 not defined - " + getSystemName());
            }
            // set Turnout state according to sensors
            if ((s1State == Sensor.ACTIVE) && (s2State == Sensor.INACTIVE)) {
                newKnownState(THROWN);
            } else if ((s1State == Sensor.INACTIVE) && (s2State == Sensor.ACTIVE)) {
                newKnownState(CLOSED);
            } else if (_knownState != UNKNOWN) {
                newKnownState(UNKNOWN);
            }
        } else {
            // nothing required at this time for other modes
        }
    }

    /**
     * React to sensor changes by changing the KnownState if using an
     * appropriate sensor mode
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getSource() == myTurnoutOperation) {
            operationPropertyChange(evt);
        } else if (evt.getSource() == getFirstSensor()
                || evt.getSource() == getSecondSensor()) {
            sensorPropertyChange(evt);
        }
    }

    protected void sensorPropertyChange(java.beans.PropertyChangeEvent evt) {
        // top level, find the mode
        if (_activeFeedbackType == ONESENSOR) {
            // check for match
            if (evt.getSource() == getFirstSensor()) {
                // check change type
                if (!evt.getPropertyName().equals("KnownState")) {
                    return;
                }
                // OK, now have to handle it
                int mode = ((Integer) evt.getNewValue()).intValue();
                if (mode == Sensor.ACTIVE) {
                    newKnownState(THROWN);
                } else if (mode == Sensor.INACTIVE) {
                    newKnownState(CLOSED);
                }
            } else {
                // unexected mismatch
                log.warn("expected sensor " + getFirstNamedSensor().getName()
                        + " was " + ((Sensor) evt.getSource()).getSystemName());
            }
            // end ONESENSOR block
        } else if (_activeFeedbackType == TWOSENSOR) {
            // check change type
            if (!evt.getPropertyName().equals("KnownState")) {
                return;
            }
            // OK, now have to handle it
            int mode = ((Integer) evt.getNewValue()).intValue();
            Sensor s = (Sensor) evt.getSource();
            if ((mode == Sensor.ACTIVE) && (s == getSecondSensor())) {
                newKnownState(CLOSED);
            } else if ((mode == Sensor.ACTIVE) && (s == getFirstSensor())) {
                newKnownState(THROWN);
            } else if (!(((getFirstSensor().getKnownState() == Sensor.ACTIVE) && (getSecondSensor()
                    .getKnownState() == Sensor.INACTIVE)) || ((getFirstSensor()
                    .getKnownState() == Sensor.INACTIVE) && (getSecondSensor()
                    .getKnownState() == Sensor.ACTIVE)))) // INCONSISTENT if sensor has transitioned to an inconsistent state
            {
                newKnownState(INCONSISTENT);
            }
            // end TWOSENSOR block
        } else // don't need to do anything
        {
            return;
        }
    }

    public void setBinaryOutput(boolean state) {
        binaryOutput = true;
    }
    protected boolean binaryOutput = false;

    public void dispose() {
        if (getFirstSensor() != null) {
            getFirstSensor().removePropertyChangeListener(this);
        }
        _firstNamedSensor = null;
        if (getSecondSensor() != null) {
            getSecondSensor().removePropertyChangeListener(this);
        }
        _secondNamedSensor = null;
        super.dispose();
    }

    String _divergeSpeed = "";
    String _straightSpeed = "";
    //boolean useBlockSpeed = true;

    //float speedThroughTurnout = 0;
    public float getDivergingLimit() {
        if ((_divergeSpeed == null) || (_divergeSpeed == "")) {
            return -1;
        }

        String speed = _divergeSpeed;
        if (_divergeSpeed.equals("Global")) {
            speed = InstanceManager.turnoutManagerInstance().getDefaultThrownSpeed();
        }
        if (speed.equals("Block")) {
            return -1;
        }
        try {
            return new Float(speed);
            //return Integer.parseInt(_blockSpeed);
        } catch (NumberFormatException nx) {
            //considered normal if the speed is not a number.
        }
        try {
            return jmri.implementation.SignalSpeedMap.getMap().getSpeed(speed);
        } catch (Exception ex) {
            return -1;
        }
    }

    public String getDivergingSpeed() {
        if (_divergeSpeed.equals("Global")) {
            return ("Use Global " + InstanceManager.turnoutManagerInstance().getDefaultThrownSpeed());
        }
        if (_divergeSpeed.equals("Block")) {
            return ("Use Block Speed");
        }
        return _divergeSpeed;
    }

    public void setDivergingSpeed(String s) throws JmriException {
        if (s == null) {
            throw new JmriException("Value of requested turnout thrown speed can not be null");
        }
        if (_divergeSpeed.equals(s)) {
            return;
        }
        if (s.contains("Global")) {
            s = "Global";
        } else if (s.contains("Block")) {
            s = "Block";
        } else {
            try {
                Float.parseFloat(s);
            } catch (NumberFormatException nx) {
                try {
                    jmri.implementation.SignalSpeedMap.getMap().getSpeed(s);
                } catch (Exception ex) {
                    throw new JmriException("Value of requested block speed is not valid");
                }
            }
        }
        String oldSpeed = _divergeSpeed;
        _divergeSpeed = s;
        firePropertyChange("TurnoutDivergingSpeedChange", oldSpeed, s);
    }

    public float getStraightLimit() {
        if ((_straightSpeed == null) || (_straightSpeed == "")) {
            return -1;
        }
        String speed = _straightSpeed;
        if (_straightSpeed.equals("Global")) {
            speed = InstanceManager.turnoutManagerInstance().getDefaultClosedSpeed();
        } else if (speed.equals("Block")) {
            return -1;
        }
        try {
            return new Float(speed);
        } catch (NumberFormatException nx) {
            //considered normal if the speed is not a number.
        }
        try {
            return jmri.implementation.SignalSpeedMap.getMap().getSpeed(speed);
        } catch (Exception ex) {
            return -1;
        }
    }

    public String getStraightSpeed() {
        if (_straightSpeed.equals("Global")) {
            return ("Use Global " + InstanceManager.turnoutManagerInstance().getDefaultClosedSpeed());
        }
        if (_straightSpeed.equals("Block")) {
            return ("Use Block Speed");
        }
        return _straightSpeed;
    }

    public void setStraightSpeed(String s) throws JmriException {
        if (s == null) {
            throw new JmriException("Value of requested turnout straight speed can not be null");
        }
        if (_straightSpeed.equals(s)) {
            return;
        }
        if (s.contains("Global")) {
            s = "Global";
        } else if (s.contains("Block")) {
            s = "Block";
        } else {
            try {
                Float.parseFloat(s);
            } catch (NumberFormatException nx) {
                try {
                    jmri.implementation.SignalSpeedMap.getMap().getSpeed(s);
                } catch (Exception ex) {
                    throw new JmriException("Value of requested turnout straight speed is not valid");
                }
            }
        }
        String oldSpeed = _straightSpeed;
        _straightSpeed = s;
        firePropertyChange("TurnoutStraightSpeedChange", oldSpeed, s);
    }

    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            if (evt.getOldValue().equals(getFirstSensor()) || evt.getOldValue().equals(getSecondSensor())) {
                java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);
                throw new java.beans.PropertyVetoException(Bundle.getMessage("InUseSensorTurnoutVeto", getDisplayName()), e); //IN18N
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) {
            //log.info("Call to do delete"); //IN18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractTurnout.class.getName());
}

/* @(#)AbstractTurnout.java */
