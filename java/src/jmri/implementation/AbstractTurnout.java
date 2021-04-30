package jmri.implementation;

import java.beans.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.*;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for the Turnout interface.
 * <p>
 * Implements basic feedback modes:
 * <ul>
 * <li>NONE feedback, where the KnownState and CommandedState track each other.
 * <li>ONESENSOR feedback where the state of a single sensor specifies THROWN vs
 * CLOSED
 * <li>TWOSENSOR feedback, where one sensor specifies THROWN and another CLOSED.
 * </ul>
 * If you want to implement some other feedback, override and modify
 * setCommandedState() here.
 * <p>
 * Implements the parameter binding support.
 * <p>
 * Note that we consider it an error for there to be more than one object that
 * corresponds to a particular physical turnout on the layout.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2009
 */
public abstract class AbstractTurnout extends AbstractNamedBean implements
        Turnout, PropertyChangeListener {

    private Turnout leadingTurnout = null;
    private boolean followingCommandedState = true;

    protected AbstractTurnout(String systemName) {
        super(systemName);
    }

    protected AbstractTurnout(String systemName, String userName) {
        super(systemName, userName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getBeanType() {
        return Bundle.getMessage("BeanNameTurnout");
    }

    private final String closedText = InstanceManager.turnoutManagerInstance().getClosedText();
    private final String thrownText = InstanceManager.turnoutManagerInstance().getThrownText();

    /**
     * Handle a request to change state, typically by sending a message to the
     * layout in some child class. Public version (used by TurnoutOperator)
     * sends the current commanded state without changing it.
     * Implementing classes will typically check the value of s and send a system specific sendMessage command.
     *
     * @param s new state value
     */
    abstract protected void forwardCommandChangeToLayout(int s);

    protected void forwardCommandChangeToLayout() {
        forwardCommandChangeToLayout(_commandedState);
    }

    /**
     * Preprocess a Turnout state change request for {@link #forwardCommandChangeToLayout(int)}
     * Public access to allow use in tests.
     *
     * @param newState the Turnout state command value passed
     * @return true if a Turnout.CLOSED was requested and Turnout is not set to _inverted
     */
    public boolean stateChangeCheck(int newState) throws IllegalArgumentException {
        // sort out states
        if ((newState & Turnout.CLOSED) != 0) {
            if (statesOk(newState)) {
                // request a CLOSED command (or THROWN if inverted)
                return (!_inverted);
            } else {
                throw new IllegalArgumentException("Can't set state for Turnout " + newState);
            }
        }
        // request a THROWN command (or CLOSED if inverted)
        return (_inverted);
    }

    /**
     * Look for the case in which the state is neither Closed nor Thrown, which we can't handle.
     * Separate method to allow it to be used in {@link #stateChangeCheck} and Xpa/MqttTurnout.
     *
     * @param state the Turnout state passed
     * @return false if s = Turnout.THROWN, which is what we want
     */
    protected boolean statesOk(int state) {
        if ((state & Turnout.THROWN) != 0) {
            // this is the disaster case!
            log.error("Cannot command both CLOSED and THROWN");
            return false;
        }
        return true;
    }

    /**
     * Set a new Commanded state, if need be notifying the listeners, but do
     * NOT send the command downstream.
     * <p>
     * This is used when a new commanded state
     * is noticed from another command.
     *
     * @param s new state
     */
    protected void newCommandedState(int s) {
        if (_commandedState != s) {
            int oldState = _commandedState;
            _commandedState = s;
            firePropertyChange("CommandedState", oldState, _commandedState);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getKnownState() {
        return _knownState;
    }

    /**
     * Public access to changing turnout state. Sets the commanded state and, if
     * appropriate, starts a TurnoutOperator to do its thing. If there is no
     * TurnoutOperator (not required or nothing suitable) then just tell the
     * layout and hope for the best.
     *
     * @param s commanded state to set
     */
    @Override
    public void setCommandedState(int s) {
        log.debug("set commanded state for turnout {} to {}", getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME),
                (s == Turnout.CLOSED ? closedText : thrownText));
        newCommandedState(s);
        myOperator = getTurnoutOperator(); // MUST set myOperator before starting the thread
        if (myOperator == null) {
            log.debug("myOperator NULL");
            forwardCommandChangeToLayout(s);
            // optionally handle feedback
            if (_activeFeedbackType == DIRECT) {
                newKnownState(s);
            } else if (_activeFeedbackType == DELAYED) {
                newKnownState(INCONSISTENT);
                jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> { newKnownState(s); },
                         DELAYED_FEEDBACK_INTERVAL );
            }
        } else {
            log.debug("myOperator NOT NULL");
            myOperator.start();
        }
    }

    /**
     * Duration in Milliseconds of delay for DELAYED feedback mode.
     * <p>
     * Defined as "public non-final" so it can be changed in e.g.
     * the jython/SetDefaultDelayedTurnoutDelay script.
     */
    public static int DELAYED_FEEDBACK_INTERVAL = 4000;

    protected Thread thr;
    protected Runnable r;
    private LocalDateTime nextWait;

    /** {@inheritDoc}
     * Used in {@link jmri.implementation.DefaultRoute#setRoute()} and
     * {@link jmri.implementation.MatrixSignalMast#updateOutputs(char[])}.
     */
    @Override
    public void setCommandedStateAtInterval(int s) {
        nextWait = InstanceManager.turnoutManagerInstance().outputIntervalEnds();
        // nextWait time is calculated using actual turnoutInterval in TurnoutManager
        if (nextWait.isAfter(LocalDateTime.now())) { // don't sleep if nextWait =< now()
            log.debug("Turnout now() = {}, waitUntil = {}", LocalDateTime.now(), nextWait);
            // insert wait before sending next output command to the layout
            r = () -> {
                log.debug("go to sleep for {} ms...", Math.max(0L, LocalDateTime.now().until(nextWait, ChronoUnit.MILLIS)));
                try {
                    Thread.sleep(Math.max(0L, LocalDateTime.now().until(nextWait, ChronoUnit.MILLIS))); // nextWait might have passed in the meantime
                    log.debug("back from sleep, forward on {}", LocalDateTime.now());
                    setCommandedState(s);
                } catch (InterruptedException ex) {
                    log.debug("setCommandedStateAtInterval(s) interrupted at {}", LocalDateTime.now());
                    Thread.currentThread().interrupt(); // retain if needed later
                }
            };
            thr = new Thread(r);
            thr.start();
        } else {
            log.debug("nextWait has passed");
            setCommandedState(s);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getCommandedState() {
        return _commandedState;
    }

    /**
     * Add a newKnownState() for use by implementations.
     * <p>
     * Use this to update internal information when a state change is detected
     * <em>outside</em> the Turnout object, e.g. via feedback from sensors on
     * the layout.
     * <p>
     * If the layout status of the Turnout is observed to change to THROWN or
     * CLOSED, this also sets the commanded state, because it's assumed that
     * somebody somewhere commanded that move. If it's observed to change to
     * UNKNOWN or INCONSISTENT, that's perhaps either an error or a move in
     * progress, and no change is made to the commanded state.
     * <p>
     * This implementation sends a command to the layout for the new state if
     * going to THROWN or CLOSED, because there may be others listening to
     * network state.
     * <p>
     * This method is not intended for general use, e.g. for users to set the
     * KnownState, so it doesn't appear in the Turnout interface.
     * <p>
     * On change, fires Property Change "KnownState".
     * @param s New state value
     */
    public void newKnownState(int s) {
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", oldState, _knownState);
        }
        _knownState = s;
        // if known state has moved to Thrown or Closed,
        // set the commanded state to match
        if ((_knownState == THROWN && _commandedState != THROWN)
                || (_knownState == CLOSED && _commandedState != CLOSED)) {
            newCommandedState(_knownState);
        }
    }

    /**
     * Show whether state is one you can safely run trains over.
     *
     * @return true if state is a valid one and the known state is the same as
     *         commanded.
     */
    @Override
    public boolean isConsistentState() {
        return _commandedState == _knownState
                && (_commandedState == CLOSED || _commandedState == THROWN);
    }

    /**
     * The name pretty much says it.
     * <p>
     * Triggers all listeners, etc. For use by the TurnoutOperator classes.
     */
    void setKnownStateToCommanded() {
        newKnownState(_commandedState);
    }

    /**
     * Implement a shorter name for setCommandedState.
     * <p>
     * This generally shouldn't be used by Java code; use setCommandedState
     * instead. The is provided to make Jython script access easier to read.
     * <p>
     * Note that getState() and setState(int) are not symmetric: getState is the
     * known state, and set state modifies the commanded state.
     * @param s new state
     */
    @Override
    public void setState(int s) {
        setCommandedState(s);
    }

    /**
     * Implement a shorter name for getKnownState.
     * <p>
     * This generally shouldn't be used by Java code; use getKnownState instead.
     * The is provided to make Jython script access easier to read.
     * <p>
     * Note that getState() and setState(int) are not symmetric: getState is the
     * known state, and set state modifies the commanded state.
     * @return current state
     */
    @Override
    public int getState() {
        return getKnownState();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String describeState(int state) {
        switch (state) {
            case THROWN: return Bundle.getMessage("TurnoutStateThrown");
            case CLOSED: return Bundle.getMessage("TurnoutStateClosed");
            default: return super.describeState(state);
        }
    }

    protected String[] _validFeedbackNames = {"DIRECT", "ONESENSOR",
        "TWOSENSOR", "DELAYED"};

    protected int[] _validFeedbackModes = {DIRECT, ONESENSOR, TWOSENSOR, DELAYED};

    protected int _validFeedbackTypes = DIRECT | ONESENSOR | TWOSENSOR | DELAYED;

    protected int _activeFeedbackType = DIRECT;

    private int _knownState = UNKNOWN;

    private int _commandedState = UNKNOWN;

    private int _numberOutputBits = 1;

    /** Number of bits to control a turnout - defaults to one */
    private int _controlType = 0;

    /** Type of turnout control - defaults to 0 for /'steady state/' */
    @Override
    public int getNumberOutputBits() {
        return _numberOutputBits;
    }

    /** {@inheritDoc} */
    @Override
    public void setNumberOutputBits(int num) {
        _numberOutputBits = num;
    }

    /** {@inheritDoc} */
    @Override
    public int getControlType() {
        return _controlType;
    }

    /** {@inheritDoc} */
    @Override
    public void setControlType(int num) {
        _controlType = num;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Integer> getValidFeedbackModes() {
        Set<Integer> modes = new HashSet<>();
        Arrays.stream(_validFeedbackModes).forEach(modes::add);
        return modes;
    }

    /** {@inheritDoc} */
    @Override
    public int getValidFeedbackTypes() {
        return _validFeedbackTypes;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String[] getValidFeedbackNames() {
        return Arrays.copyOf(_validFeedbackNames, _validFeedbackNames.length);
    }

    /** {@inheritDoc} */
    @Override
    public void setFeedbackMode(@Nonnull String mode) throws IllegalArgumentException {
        for (int i = 0; i < _validFeedbackNames.length; i++) {
            if (mode.equals(_validFeedbackNames[i])) {
                setFeedbackMode(_validFeedbackModes[i]);
                setInitialKnownStateFromFeedback();
                return;
            }
        }
        throw new IllegalArgumentException("Unexpected mode: " + mode);
    }

    /** 
     * On change, fires Property Change "feedbackchange".
     * {@inheritDoc}
     */
    @Override
    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        // check for error - following removed the low bit from mode
        int test = mode & (mode - 1);
        if (test != 0) {
            throw new IllegalArgumentException("More than one bit set: " + mode);
        }
        // set the value
        int oldMode = _activeFeedbackType;
        _activeFeedbackType = mode;
        // unlock turnout if feedback is changed
        setLocked(CABLOCKOUT, false);
        if (oldMode != _activeFeedbackType) {
            firePropertyChange("feedbackchange", oldMode,
                    _activeFeedbackType);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getFeedbackMode() {
        return _activeFeedbackType;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getFeedbackModeName() {
        for (int i = 0; i < _validFeedbackNames.length; i++) {
            if (_activeFeedbackType == _validFeedbackModes[i]) {
                return _validFeedbackNames[i];
            }
        }
        throw new IllegalArgumentException("Unexpected internal mode: "
                + _activeFeedbackType);
    }

    /** {@inheritDoc} */
    @Override
    public void requestUpdateFromLayout() {
        if (_activeFeedbackType == ONESENSOR || _activeFeedbackType == TWOSENSOR) {
            Sensor s1 = getFirstSensor();
            if (s1 != null) s1.requestUpdateFromLayout();
        }
        if (_activeFeedbackType == TWOSENSOR) {
            Sensor s2 = getSecondSensor();
            if (s2 != null) s2.requestUpdateFromLayout();
        }
    }

    /** 
     * On change, fires Property Change "inverted".
     * {@inheritDoc}
     */
    @Override
    public void setInverted(boolean inverted) {
        boolean oldInverted = _inverted;
        _inverted = inverted;
        if (oldInverted != _inverted) {
            int state = _knownState;
            if (state == THROWN) {
                newKnownState(CLOSED);
            } else if (state == CLOSED) {
                newKnownState(THROWN);
            }
            firePropertyChange("inverted", oldInverted, _inverted);
        }
    }

    /**
     * Get the turnout inverted state. If true, commands sent to the layout are
     * reversed. Thrown becomes Closed, and Closed becomes Thrown.
     * <p>
     * Used in polling loops in system-specific code, so made final to allow
     * optimization.
     *
     * @return inverted status
     */
    @Override
    final public boolean getInverted() {
        return _inverted;
    }

    protected boolean _inverted = false;

    /**
     * Determine if the turnouts can be inverted. If true, inverted turnouts
     * are supported.
     * @return invert supported
     */
    @Override
    public boolean canInvert() {
        return false;
    }

    /**
     * Turnouts that are locked should only respond to JMRI commands to change
     * state.
     * We simulate a locked turnout by monitoring the known state (turnout 
     * feedback is required) and if we detect that the known state has
     * changed, 
     * negate it by forcing the turnout to return to the commanded
     * state.
     * Turnouts that have local buttons can also be locked if their
     * decoder supports it.
     * On change, fires Property Change "locked".
     *
     * @param turnoutLockout lockout state to monitor. Possible values
     *                       {@link #CABLOCKOUT}, {@link #PUSHBUTTONLOCKOUT}.
     *                       Can be combined to monitor both states.
     * @param locked         true if turnout to be locked
     */
    @Override
    public void setLocked(int turnoutLockout, boolean locked) {
        boolean firechange = false;
        if ((turnoutLockout & CABLOCKOUT) != 0 && _cabLockout != locked) {
            firechange = true;
            if (canLock(CABLOCKOUT)) {
                _cabLockout = locked;
            } else {
                _cabLockout = false;
            }
        }
        if ((turnoutLockout & PUSHBUTTONLOCKOUT) != 0
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
            firePropertyChange("locked", !locked, locked);
        }
    }

    /**
     * Determine if turnout is locked. There
     * are two types of locks: cab lockout, and pushbutton lockout.
     *
     * @param turnoutLockout turnout to check
     * @return locked state, true if turnout is locked
     */
    @Override
    public boolean getLocked(int turnoutLockout) {
        switch (turnoutLockout) {
            case CABLOCKOUT:
                return _cabLockout;
            case PUSHBUTTONLOCKOUT:
                return _pushButtonLockout;
            case CABLOCKOUT + PUSHBUTTONLOCKOUT:
                return _cabLockout || _pushButtonLockout;
            default:
                return false;
        }
    }

    protected boolean _cabLockout = false;

    protected boolean _pushButtonLockout = false;

    protected boolean _enableCabLockout = false;

    protected boolean _enablePushButtonLockout = false;

    /**
     * This implementation by itself doesn't provide locking support.
     * Override this in subclasses that do.
     *
     * @return One of 0 for none
     */
    @Override
    public int getPossibleLockModes() { return 0; }

    /**
     * This implementation by itself doesn't provide locking support.
     * Override this in subclasses that do.
     *
     * @return false for not supported
     */
    @Override
    public boolean canLock(int turnoutLockout) {
        return false;
    }

    /** {@inheritDoc} 
     * Not implemented in AbstractTurnout.
     */
    @Override
    public void enableLockOperation(int turnoutLockout, boolean enabled) {
    }

    /**
     * When true, report to console anytime a cab attempts to change the state
     * of a turnout on the layout.
     * When a turnout is cab locked, only JMRI is
     * allowed to change the state of a turnout.
     * On setting changed, fires Property Change "reportlocked".
     *
     * @param reportLocked report locked state
     */
    @Override
    public void setReportLocked(boolean reportLocked) {
        boolean oldReportLocked = _reportLocked;
        _reportLocked = reportLocked;
        if (oldReportLocked != _reportLocked) {
            firePropertyChange("reportlocked", oldReportLocked,
                    _reportLocked);
        }
    }

    /**
     * When true, report to console anytime a cab attempts to change the state
     * of a turnout on the layout. When a turnout is cab locked, only JMRI is
     * allowed to change the state of a turnout.
     *
     * @return report locked state
     */
    @Override
    public boolean getReportLocked() {
        return _reportLocked;
    }

    protected boolean _reportLocked = true;

    /**
     * Valid stationary decoder names.
     */
    protected String[] _validDecoderNames = PushbuttonPacket
            .getValidDecoderNames();

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String[] getValidDecoderNames() {
        return Arrays.copyOf(_validDecoderNames, _validDecoderNames.length);
    }

    // set the turnout decoder default to unknown
    protected String _decoderName = PushbuttonPacket.unknown;

    /** {@inheritDoc} */
    @Override
    public String getDecoderName() {
        return _decoderName;
    }

    /** 
     * {@inheritDoc} 
     * On change, fires Property Change "decoderNameChange".
     */
    @Override
    public void setDecoderName(final String decoderName) {
        if (!(Objects.equals(_decoderName, decoderName))) {
            String oldName = _decoderName;
            _decoderName = decoderName;
            firePropertyChange("decoderNameChange", oldName, decoderName);
        }
    }

    abstract protected void turnoutPushbuttonLockout(boolean locked);

    protected void turnoutPushbuttonLockout() {
        turnoutPushbuttonLockout(_pushButtonLockout);
    }

    /*
     * Support for turnout automation (see TurnoutOperation and related classes).
     */
    protected TurnoutOperator myOperator;

    protected TurnoutOperation myTurnoutOperation;

    protected boolean inhibitOperation = true; // do not automate this turnout, even if globally operations are on

    public TurnoutOperator getCurrentOperator() {
        return myOperator;
    }

    /** {@inheritDoc} */
    @Override
    public TurnoutOperation getTurnoutOperation() {
        return myTurnoutOperation;
    }

    /** 
     * {@inheritDoc} 
     * Fires Property Change "TurnoutOperationState".
     */
    @Override
    public void setTurnoutOperation(TurnoutOperation toper) {
        log.debug("setTurnoutOperation Called for turnout {}.  Operation type {}", this.getSystemName(), toper);
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

    /** {@inheritDoc} */
    @Override
    public boolean getInhibitOperation() {
        return inhibitOperation;
    }

    /** {@inheritDoc} */
    @Override
    public void setInhibitOperation(boolean io) {
        inhibitOperation = io;
    }

    /**
     * Find the TurnoutOperation class for this turnout, and get an instance of
     * the corresponding operator. Override this function if you want another way
     * to choose the operation.
     *
     * @return newly-instantiated TurnoutOperator, or null if nothing suitable
     */
    protected TurnoutOperator getTurnoutOperator() {
        TurnoutOperator to = null;
        if (!inhibitOperation) {
            if (myTurnoutOperation != null) {
                to = myTurnoutOperation.getOperator(this);
            } else {
                TurnoutOperation toper = InstanceManager.getDefault(TurnoutOperationManager.class)
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
     * ones that the generic turnout operations know about.
     *
     * @return    apparent feedback mode for operation lookup
     */
    protected int getFeedbackModeForOperation() {
        return getFeedbackMode();
    }

    /**
     * Support for associated sensor or sensors.
     */
    //Sensor getFirstSensor() = null;
    private NamedBeanHandle<Sensor> _firstNamedSensor;

    //Sensor getSecondSensor() = null;
    private NamedBeanHandle<Sensor> _secondNamedSensor;

    /** {@inheritDoc} */
    @Override
    public void provideFirstFeedbackSensor(String pName) throws jmri.JmriException, IllegalArgumentException {
        if (InstanceManager.getNullableDefault(SensorManager.class) != null) {
            if (pName == null || pName.isEmpty()) {
                provideFirstFeedbackNamedSensor(null);
            } else {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                provideFirstFeedbackNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
            }
        } else {
            log.error("No SensorManager for this protocol");
            throw new jmri.JmriException("No Sensor Manager Found");
        }
    }

    /** 
     * On change, fires Property Change "TurnoutFeedbackFirstSensorChange".
     * @param s the Handle for First Feedback Sensor
     */
    public void provideFirstFeedbackNamedSensor(NamedBeanHandle<Sensor> s) {
        // remove existing if any
        Sensor temp = getFirstSensor();
        if (temp != null) {
            temp.removePropertyChangeListener(this);
        }

        _firstNamedSensor = s;

        // if need be, set listener
        temp = getFirstSensor();  // might have changed
        if (temp != null) {
            temp.addPropertyChangeListener(this, s.getName(), "Feedback Sensor for " + getDisplayName());
        }
        // set initial state
        setInitialKnownStateFromFeedback();
        firePropertyChange("turnoutFeedbackFirstSensorChange", temp, s);
    }

    /** {@inheritDoc} */
    @Override
    public Sensor getFirstSensor() {
        if (_firstNamedSensor == null) {
            return null;
        }
        return _firstNamedSensor.getBean();
    }

    /** {@inheritDoc} */
    @Override
    public NamedBeanHandle<Sensor> getFirstNamedSensor() {
        return _firstNamedSensor;
    }

    /** {@inheritDoc} */
    @Override
    public void provideSecondFeedbackSensor(String pName) throws jmri.JmriException, IllegalArgumentException {
        if (InstanceManager.getNullableDefault(SensorManager.class) != null) {
            if (pName == null || pName.isEmpty()) {
                provideSecondFeedbackNamedSensor(null);
            } else {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                provideSecondFeedbackNamedSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
            }
        } else {
            log.error("No SensorManager for this protocol");
            throw new jmri.JmriException("No Sensor Manager Found");
        }
    }

    /** 
     * On change, fires Property Change "TurnoutFeedbackSecondSensorChange".
     * @param s the Handle for Second Feedback Sensor
     */
    public void provideSecondFeedbackNamedSensor(NamedBeanHandle<Sensor> s) {
        // remove existing if any
        Sensor temp = getSecondSensor();
        if (temp != null) {
            temp.removePropertyChangeListener(this);
        }

        _secondNamedSensor = s;

        // if need be, set listener
        temp = getSecondSensor();  // might have changed
        if (temp != null) {
            temp.addPropertyChangeListener(this, s.getName(), "Feedback Sensor for " + getDisplayName());
        }
        // set initial state
        setInitialKnownStateFromFeedback();
        firePropertyChange("turnoutFeedbackSecondSensorChange", temp, s);
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public Sensor getSecondSensor() {
        if (_secondNamedSensor == null) {
            return null;
        }
        return _secondNamedSensor.getBean();
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public NamedBeanHandle<Sensor> getSecondNamedSensor() {
        return _secondNamedSensor;
    }

    /** {@inheritDoc} */
    @Override
    public void setInitialKnownStateFromFeedback() {
        Sensor firstSensor = getFirstSensor();
        if (_activeFeedbackType == ONESENSOR) {
            // ONESENSOR feedback
            if (firstSensor != null) {
                // set according to state of sensor
                int sState = firstSensor.getKnownState();
                if (sState == Sensor.ACTIVE) {
                    newKnownState(THROWN);
                } else if (sState == Sensor.INACTIVE) {
                    newKnownState(CLOSED);
                }
            } else {
                log.warn("expected Sensor 1 not defined - {}", getSystemName());
                newKnownState(UNKNOWN);
            }
        } else if (_activeFeedbackType == TWOSENSOR) {
            // TWOSENSOR feedback
            int s1State = Sensor.UNKNOWN;
            int s2State = Sensor.UNKNOWN;
            if (firstSensor != null) {
                s1State = firstSensor.getKnownState();
            } else {
                log.warn("expected Sensor 1 not defined - {}", getSystemName());
            }
            Sensor secondSensor = getSecondSensor();
            if (secondSensor != null) {
                s2State = secondSensor.getKnownState();
            } else {
                log.warn("expected Sensor 2 not defined - {}", getSystemName());
            }
            // set Turnout state according to sensors
            if ((s1State == Sensor.ACTIVE) && (s2State == Sensor.INACTIVE)) {
                newKnownState(THROWN);
            } else if ((s1State == Sensor.INACTIVE) && (s2State == Sensor.ACTIVE)) {
                newKnownState(CLOSED);
            } else if (_knownState != UNKNOWN) {
                newKnownState(UNKNOWN);
            }
        // nothing required at this time for other modes
        }
    }

    /**
     * React to sensor changes by changing the KnownState if using an
     * appropriate sensor mode.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == myTurnoutOperation) {
            operationPropertyChange(evt);
        } else if (evt.getSource() == getFirstSensor()
                || evt.getSource() == getSecondSensor()) {
            sensorPropertyChange(evt);
        } else if (evt.getSource() == leadingTurnout) {
            leadingTurnoutPropertyChange(evt);
        }
    }

    protected void sensorPropertyChange(PropertyChangeEvent evt) {
        // top level, find the mode
        Sensor src = (Sensor) evt.getSource();
        Sensor s1 = getFirstSensor();
        if (src == null || s1 == null) {
            log.warn("Turnout feedback sensors configured incorrectly ");
            return; // can't complete
        }

        if (_activeFeedbackType == ONESENSOR) {
            // check for match
            if (src == s1) {
                // check change type
                if (!evt.getPropertyName().equals("KnownState")) {
                    return;
                }
                // OK, now handle it
                switch ((Integer) evt.getNewValue()) {
                    case Sensor.ACTIVE:
                        newKnownState(THROWN);
                        break;
                    case Sensor.INACTIVE:
                        newKnownState(CLOSED);
                        break;
                    default:
                        newKnownState(INCONSISTENT);
                        break;
                }
            } else {
                // unexpected mismatch
                NamedBeanHandle<Sensor> firstNamed = getFirstNamedSensor();
                if (firstNamed != null) {
                    log.warn("expected sensor {} was {}", firstNamed.getName(), src.getSystemName());
                } else {
                    log.error("unexpected (null) sensors");
                }
            }
            // end ONESENSOR block
        } else if (_activeFeedbackType == TWOSENSOR) {
            // check change type
            if (!evt.getPropertyName().equals("KnownState")) {
                return;
            }
            // OK, now handle it
            Sensor s2 = getSecondSensor();
            if (s2 == null) {
                log.warn("Turnout feedback sensor 2 configured incorrectly ");
                return; // can't complete
            }
            if (s1.getKnownState() == Sensor.INACTIVE && s2.getKnownState() == Sensor.ACTIVE) {
                newKnownState(CLOSED);
            } else if (s1.getKnownState() == Sensor.ACTIVE && s2.getKnownState() == Sensor.INACTIVE) {
                newKnownState(THROWN);
            } else if (s1.getKnownState() == Sensor.UNKNOWN && s2.getKnownState() == Sensor.UNKNOWN) {
                newKnownState(UNKNOWN);
            } else {
                newKnownState(INCONSISTENT);
            }
            // end TWOSENSOR block
        }
    }

    protected void leadingTurnoutPropertyChange(PropertyChangeEvent evt) {
        int state = (int) evt.getNewValue();
        if ("KnownState".equals(evt.getPropertyName())
                && leadingTurnout != null) {
            if (followingCommandedState || state != leadingTurnout.getCommandedState()) {
                newKnownState(state);
            } else {
                newKnownState(getCommandedState());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryOutput(boolean state) {
        binaryOutput = true;
    }
    protected boolean binaryOutput = false;

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        Sensor temp;
        temp = getFirstSensor();
        if (temp != null) {
            temp.removePropertyChangeListener(this);
        }
        _firstNamedSensor = null;
        temp = getSecondSensor();
        if (temp != null) {
            temp.removePropertyChangeListener(this);
        }
        _secondNamedSensor = null;
        super.dispose();
    }

    private String _divergeSpeed = "";
    private String _straightSpeed = "";
    // private boolean useBlockSpeed = true;
    // private float speedThroughTurnout = 0;

    /** {@inheritDoc} */
    @Override
    public float getDivergingLimit() {
        if ((_divergeSpeed == null) || (_divergeSpeed.isEmpty())) {
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
            return Float.parseFloat(speed);
            //return Integer.parseInt(_blockSpeed);
        } catch (NumberFormatException nx) {
            //considered normal if the speed is not a number.
        }
        try {
            return jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(speed);
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDivergingSpeed() {
        if (_divergeSpeed.equals("Global")) {
            return (Bundle.getMessage("UseGlobal", "Global") + " " + InstanceManager.turnoutManagerInstance().getDefaultThrownSpeed());
        }
        if (_divergeSpeed.equals("Block")) {
            return (Bundle.getMessage("UseGlobal", "Block Speed"));
        }
        return _divergeSpeed;
    }

    /** 
     * {@inheritDoc} 
     * On change, fires Property Change "TurnoutDivergingSpeedChange".
     */
    @Override
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
                    jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(s);
                } catch (IllegalArgumentException ex) {
                    throw new JmriException("Value of requested block speed is not valid");
                }
            }
        }
        String oldSpeed = _divergeSpeed;
        _divergeSpeed = s;
        firePropertyChange("TurnoutDivergingSpeedChange", oldSpeed, s);
    }

    /** {@inheritDoc} */
    @Override
    public float getStraightLimit() {
        if ((_straightSpeed == null) || (_straightSpeed.isEmpty())) {
            return -1;
        }
        String speed = _straightSpeed;
        if (_straightSpeed.equals("Global")) {
            speed = InstanceManager.turnoutManagerInstance().getDefaultClosedSpeed();
        }
        if (speed.equals("Block")) {
            return -1;
        }
        try {
            return Float.parseFloat(speed);
        } catch (NumberFormatException nx) {
            //considered normal if the speed is not a number.
        }
        try {
            return jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(speed);
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getStraightSpeed() {
        if (_straightSpeed.equals("Global")) {
            return (Bundle.getMessage("UseGlobal", "Global") + " " + InstanceManager.turnoutManagerInstance().getDefaultClosedSpeed());
        }
        if (_straightSpeed.equals("Block")) {
            return (Bundle.getMessage("UseGlobal", "Block Speed"));
        }
        return _straightSpeed;
    }

    /**
     * {@inheritDoc}
     * On change, fires Property Change "TurnoutStraightSpeedChange".
     */
    @Override
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
                    jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(s);
                } catch (IllegalArgumentException ex) {
                    throw new JmriException("Value of requested turnout straight speed is not valid");
                }
            }
        }
        String oldSpeed = _straightSpeed;
        _straightSpeed = s;
        firePropertyChange("TurnoutStraightSpeedChange", oldSpeed, s);
    }

    /** {@inheritDoc} */
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            Object old = evt.getOldValue();
            if (old.equals(getFirstSensor()) || old.equals(getSecondSensor()) || old.equals(leadingTurnout)) {
                java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);
                throw new java.beans.PropertyVetoException(Bundle.getMessage("InUseSensorTurnoutVeto", getDisplayName()), e); // NOI18N
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            if (bean.equals(getFirstSensor())) {
                report.add(new NamedBeanUsageReport("TurnoutFeedback1"));  // NOI18N
            }
            if (bean.equals(getSecondSensor())) {
                report.add(new NamedBeanUsageReport("TurnoutFeedback2"));  // NOI18N
            }
            if (bean.equals(getLeadingTurnout())) {
                report.add(new NamedBeanUsageReport("LeadingTurnout")); // NOI18N
            }
        }
        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCanFollow() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public Turnout getLeadingTurnout() {
        return leadingTurnout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeadingTurnout(@CheckForNull Turnout turnout) {
        if (isCanFollow()) {
            Turnout old = leadingTurnout;
            leadingTurnout = turnout;
            firePropertyChange("LeadingTurnout", old, leadingTurnout);
            if (old != null) {
                old.removePropertyChangeListener("KnownState", this);
            }
            if (leadingTurnout != null) {
                leadingTurnout.addPropertyChangeListener("KnownState", this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeadingTurnout(@CheckForNull Turnout turnout, boolean followingCommandedState) {
        setLeadingTurnout(turnout);
        setFollowingCommandedState(followingCommandedState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFollowingCommandedState() {
        return followingCommandedState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFollowingCommandedState(boolean following) {
        followingCommandedState = following;
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractTurnout.class);

}
