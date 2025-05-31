package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.TimerUtil;

/**
 * Simulates turnout feedback.
 * @author Daniel Bergqvist (C) 2022
 */
public class SimulateTurnoutFeedback extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final TurnoutListener _turnoutListener = new TurnoutListener();
    private final Map<Turnout, TurnoutTimerTask> _timerTasks = new HashMap<>();

    private int _delay = 3;     // Delay in seconds
    private final Map<String, TurnoutInfo> _turnouts = new HashMap<>();
    private boolean _hasBeenExecuted = false;


    public SimulateTurnoutFeedback(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user, LogixNG_Category.OTHER);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        SimulateTurnoutFeedback copy = new SimulateTurnoutFeedback(sysName, userName);
        copy.setComment(getComment());
        copy._delay = _delay;
        return manager.registerAction(copy);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "SimulateTurnoutFeedback_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "SimulateTurnoutFeedback_Long", _delay);
    }

    @Override
    public void setup() {
        // Do nothing
    }

    @Override
    public void execute() throws JmriException {
        if (!_hasBeenExecuted && _listenersAreRegistered) {
            registerTurnoutListeners();
        }
        _hasBeenExecuted = true;
    }

    private void registerTurnoutListeners() {
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        for (Turnout t : tm.getNamedBeanSet()) {
            addTurnoutListener(t);
        }
        tm.addPropertyChangeListener(Manager.PROPERTY_BEANS, this);
        tm.addVetoableChangeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
            if (_hasBeenExecuted) {
                registerTurnoutListeners();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
            for (Turnout t : tm.getNamedBeanSet()) {
                removeTurnoutListener(t);
            }
            tm.removePropertyChangeListener(Manager.PROPERTY_BEANS, this);
            tm.removeVetoableChangeListener(this);
            _listenersAreRegistered = false;
        }
    }

    private boolean hasTurnoutFeedback(Turnout t) {
        switch (t.getFeedbackMode()) {
            case Turnout.DIRECT:
            case Turnout.SIGNAL:
            case Turnout.DELAYED:
                return false;

            case Turnout.ONESENSOR:
                return t.getFirstSensor() != null;

            case Turnout.TWOSENSOR:
                return t.getFirstSensor() != null && t.getSecondSensor() != null;

            case Turnout.EXACT:
            case Turnout.INDIRECT:
            case Turnout.MONITORING:
            case Turnout.LNALTERNATE:
                return true;

            default:
                log.debug("Unsupported turnout feedback mode: {}, {}", t.getFeedbackMode(), t.getFeedbackModeName());
                return false;
        }
    }

    private void addTurnoutListener(Turnout turnout) {
        if (!_turnouts.containsKey(turnout.getSystemName())) {
            TurnoutInfo ti = new TurnoutInfo(turnout);
            _turnouts.put(turnout.getSystemName(), ti);
            turnout.addPropertyChangeListener(Turnout.PROPERTY_FEEDBACK_MODE, this);
            turnout.addPropertyChangeListener(Turnout.PROPERTY_TURNOUT_FEEDBACK_FIRST_SENSOR, this);
            turnout.addPropertyChangeListener(Turnout.PROPERTY_TURNOUT_FEEDBACK_SECOND_SENSOR, this);
            if (hasTurnoutFeedback(turnout)) {
                turnout.addPropertyChangeListener(Turnout.PROPERTY_COMMANDED_STATE, _turnoutListener);
                ti._hasListener = true;
            }
        }
    }

    private void removeTurnoutListener(Turnout turnout) {
        TurnoutInfo ti = _turnouts.remove(turnout.getSystemName());
        turnout.removePropertyChangeListener(Turnout.PROPERTY_FEEDBACK_MODE, this);
        turnout.removePropertyChangeListener(Turnout.PROPERTY_TURNOUT_FEEDBACK_FIRST_SENSOR, this);
        turnout.removePropertyChangeListener(Turnout.PROPERTY_TURNOUT_FEEDBACK_SECOND_SENSOR, this);
        if (ti != null && ti._hasListener) {
            turnout.removePropertyChangeListener(Turnout.PROPERTY_COMMANDED_STATE, _turnoutListener);
            ti._hasListener = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (Manager.PROPERTY_BEANS.equals(evt.getPropertyName())) {
            if (!(evt.getSource() instanceof TurnoutManager)) {
                log.error("Non-TurnoutManager sent event : {}", evt);
                return;
            }
            TurnoutManager manager = (TurnoutManager)evt.getSource();
            if (evt.getNewValue() != null) {
                String sysName = evt.getNewValue().toString();
                Turnout turnout = manager.getBySystemName(sysName);
                if (_listenersAreRegistered && (turnout != null)) {
                    addTurnoutListener(turnout);
                }
            } else if (evt.getOldValue() != null) {
                String sysName = evt.getOldValue().toString();
                TurnoutInfo turnoutInfo = _turnouts.get(sysName);
                if (_listenersAreRegistered
                        && (turnoutInfo != null)
                        && (turnoutInfo._turnout != null)) {
                    removeTurnoutListener(turnoutInfo._turnout);
                }
            }
        }

        String evp = evt.getPropertyName();
        if ( Turnout.PROPERTY_FEEDBACK_MODE.equals(evp)
                || Turnout.PROPERTY_TURNOUT_FEEDBACK_FIRST_SENSOR.equals(evp)
                || Turnout.PROPERTY_TURNOUT_FEEDBACK_SECOND_SENSOR.equals(evp)) {

            TurnoutInfo ti = _turnouts.get(evt.getSource().toString());
            if (hasTurnoutFeedback(ti._turnout)) {
                if (!ti._hasListener) {
                    ti._turnout.addPropertyChangeListener(Turnout.PROPERTY_COMMANDED_STATE, _turnoutListener);
                    ti._hasListener = true;
                }
            } else {
                if (ti._hasListener) {
                    ti._turnout.removePropertyChangeListener(Turnout.PROPERTY_COMMANDED_STATE, _turnoutListener);
                    ti._hasListener = false;
                }
            }
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ( Manager.PROPERTY_DO_DELETE.equals(evt.getPropertyName())
            && evt.getOldValue() instanceof Turnout ) {
                removeTurnoutListener((Turnout) evt.getOldValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    private static class TurnoutInfo {

        private final Turnout _turnout;
        private boolean _hasListener;

        TurnoutInfo(Turnout turnout) {
            _turnout = turnout;
        }
    }


    private class TurnoutListener implements PropertyChangeListener {

        private void manageTurnout(Turnout t, int newState) {
            synchronized (_timerTasks) {
                if (_timerTasks.containsKey(t)) {
                    TurnoutTimerTask task = _timerTasks.get(t);
                    task.cancel();
                    _timerTasks.remove(t);
                }
                TurnoutTimerTask task = new TurnoutTimerTask(t, newState);
                _timerTasks.put(t, task);
                TimerUtil.schedule(task, _delay * 1000L);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
//            System.out.format("Source: %s, name: %s, old: %s, new: %s%n", evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            if ( Turnout.PROPERTY_COMMANDED_STATE.equals(evt.getPropertyName())) {
                String sysName = evt.getSource().toString();
                TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
                Turnout t = tm.getBySystemName(sysName);
                if (t == null) {
                    log.error("Turnout {} does not exists in manager {}", sysName, tm);
                    return;
                }

                switch (t.getFeedbackMode()) {
                    case Turnout.DIRECT:
                    case Turnout.SIGNAL:
                    case Turnout.DELAYED:
                        // Do nothing
                        break;

                    case Turnout.EXACT:
                    case Turnout.INDIRECT:
                    case Turnout.MONITORING:
                    case Turnout.ONESENSOR:
                    case Turnout.TWOSENSOR:
                    case Turnout.LNALTERNATE:
                        // Hardware feedback
                        manageTurnout(t, (int) evt.getNewValue());
                        break;

                    default:
                        log.debug("Unsupported turnout feedback mode: {}, {}", t.getFeedbackMode(), t.getFeedbackModeName());
                }
            }
        }

    }

    private class TurnoutTimerTask extends java.util.TimerTask {

        private final Turnout _turnout;
        private final int _newState;

        private TurnoutTimerTask(Turnout t, int newState) {
            _turnout = t;
            _newState = newState;
            startMove();
        }

        private void startMove() {
            Sensor sensor1;
            Sensor sensor2;

            switch (_turnout.getFeedbackMode()) {
                case Turnout.EXACT:
                    // Hardware feedback
                    break;

                case Turnout.INDIRECT:
                    // Hardware feedback
                    break;

                case Turnout.MONITORING:
                    // Hardware feedback
                    break;

                case Turnout.ONESENSOR:
                    // Do nothing
                    break;

                case Turnout.TWOSENSOR:
                    sensor1 = _turnout.getFirstSensor();
                    if (sensor1 != null) sensor1.setCommandedState(Sensor.INACTIVE);
                    sensor2 = _turnout.getSecondSensor();
                    if (sensor2 != null) sensor2.setCommandedState(Sensor.INACTIVE);
                    break;

                case Turnout.LNALTERNATE:
                    // Hardware feedback
                    break;

                default:
                    log.debug("Unsupported turnout feedback mode: {}, {}", _turnout.getFeedbackMode(), _turnout.getFeedbackModeName());
            }
        }

        private void stopMove() {
            Sensor sensor;

            switch (_turnout.getFeedbackMode()) {
                case Turnout.EXACT:
                    if (_turnout instanceof jmri.jmrix.loconet.LnTurnout) {
                        jmri.jmrix.loconet.LnTurnout lnTurnout = (jmri.jmrix.loconet.LnTurnout)_turnout;
                        lnTurnout.newKnownState(_newState);
                    } else if (_turnout instanceof jmri.jmrix.lenz.XNetTurnout) {
                        jmri.jmrix.lenz.XNetTurnout xnetTurnout = (jmri.jmrix.lenz.XNetTurnout)_turnout;
                        xnetTurnout.newKnownState(_newState);
                    } else {
                        log.warn("Unknown type of turnout {}, {}", _turnout.getSystemName(), _turnout.getDisplayName());
                    }
                    break;

                case Turnout.INDIRECT:
                    // Hardware feedback
                    break;

                case Turnout.MONITORING:
                    // Hardware feedback
                    break;

                case Turnout.ONESENSOR:
                    sensor = _turnout.getFirstSensor();
                    if (_newState == Turnout.CLOSED) {
                        if (sensor != null) sensor.setCommandedState(Sensor.INACTIVE);
                    } else if (_newState == Turnout.THROWN) {
                        if (sensor != null) sensor.setCommandedState(Sensor.ACTIVE);
                    }
                    break;

                case Turnout.TWOSENSOR:
                    if (_newState == Turnout.CLOSED) {
                        sensor = _turnout.getSecondSensor();
                        if (sensor != null) sensor.setCommandedState(Sensor.ACTIVE);
                    } else if (_newState == Turnout.THROWN) {
                        sensor = _turnout.getFirstSensor();
                        if (sensor != null) sensor.setCommandedState(Sensor.ACTIVE);
                    }
                    break;

                case Turnout.LNALTERNATE:
                    // Hardware feedback
                    break;

                default:
                    log.debug("Unsupported turnout feedback mode: {}, {}", _turnout.getFeedbackMode(), _turnout.getFeedbackModeName());
            }
        }

        @Override
        public void run() {
            synchronized (_timerTasks) {
                _timerTasks.remove(_turnout);
            }
            stopMove();
        }
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimulateTurnoutFeedback.class);
}
