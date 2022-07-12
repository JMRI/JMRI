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

    private int _delay = 3;     // Delay in seconds
    private final Map<String, Turnout> _turnouts = new HashMap<>();


    public SimulateTurnoutFeedback(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user, Category.OTHER);
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
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
            TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
            for (Turnout t : tm.getNamedBeanSet()) {
                addTurnoutListener(t);
            }
            tm.addPropertyChangeListener("beans", this);
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
            tm.removePropertyChangeListener("beans", this);
            _listenersAreRegistered = false;
        }
    }

    private void addTurnoutListener(Turnout turnout) {
        if (!_turnouts.containsKey(turnout.getSystemName())) {
            _turnouts.put(turnout.getSystemName(), turnout);
            turnout.addPropertyChangeListener("CommandedState", _turnoutListener);
//            System.out.format("Turnout: add prop: %s%n", turnout.getDisplayName());
        }
    }

    private void removeTurnoutListener(Turnout turnout) {
        _turnouts.remove(turnout.getSystemName());
        turnout.removePropertyChangeListener("CommandedState", _turnoutListener);
//        System.out.format("Turnout: remove prop: %s%n", turnout.getDisplayName());
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("beans")) {
            if (!(evt.getSource() instanceof TurnoutManager)) return;
            TurnoutManager manager = (TurnoutManager)evt.getSource();
            if (evt.getNewValue() != null) {
                String sysName = evt.getNewValue().toString();
                Turnout turnout = manager.getBySystemName(sysName);
                if (_listenersAreRegistered && (turnout != null)) {
                    addTurnoutListener(turnout);
                }
            } else if (evt.getOldValue() != null) {
                String sysName = evt.getOldValue().toString();
                Turnout turnout = _turnouts.get(sysName);
                if (_listenersAreRegistered && (turnout != null)) {
                    removeTurnoutListener(turnout);
                }
            }
        }

        System.out.format("Source: %s, name: %s, old: %s, new: %s%n", evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    private class TurnoutListener implements PropertyChangeListener {

        private final Map<Turnout, TurnoutTimerTask> _timerTasks = new HashMap<>();

        private void manageTurnout(Turnout t, int newState) {
            if (_timerTasks.containsKey(t)) {
                TurnoutTimerTask task = _timerTasks.get(t);
                task.cancel();
                _timerTasks.remove(t);
            }
            TurnoutTimerTask task = new TurnoutTimerTask(t, newState);
            TimerUtil.schedule(task, _delay * 1000L);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            System.out.format("Turnout: Source: %s, name: %s, old: %s, new: %s%n", evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            if (evt.getPropertyName().equals("CommandedState")) {
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

    private static class TurnoutTimerTask extends java.util.TimerTask {

        private final Turnout _turnout;
        private final int _newState;

        private TurnoutTimerTask(Turnout t, int newState) {
            _turnout = t;
            _newState = newState;
            startMove();
        }

        private void startMove() {
            System.out.format("Start move turnout %s%n", _turnout.getDisplayName());

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
            System.out.format("Stop move turnout %s%n", _turnout.getDisplayName());

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
            stopMove();
        }
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimulateTurnoutFeedback.class);
}
