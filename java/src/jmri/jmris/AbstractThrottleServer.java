package jmri.jmris;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the JMRI Throttles and a network connection
 *
 * @author Paul Bender Copyright (C) 2015
 */
abstract public class AbstractThrottleServer implements ThrottleListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractThrottleServer.class);
    protected ArrayList<Throttle> throttleList;

    public AbstractThrottleServer() {
        throttleList = new ArrayList<>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(LocoAddress address) throws IOException;

    abstract public void sendErrorStatus() throws IOException;

    abstract public void sendThrottleFound(LocoAddress address) throws IOException;

    abstract public void sendThrottleReleased(LocoAddress address) throws IOException;

    abstract public void parsecommand(String statusString) throws JmriException, IOException;

    /*
     * Set Throttle Speed and Direction
     *
     * @param l LocoAddress of the locomotive to change speed of.
     * @param speed float representing the speed, -1 for emergency stop.
     * @param isForward boolean, true if forward, false if reverse or
     * undefined.
     */
    public void setThrottleSpeedAndDirection(LocoAddress l, float speed, boolean isForward) {
        // get the throttle for the address.
        throttleList.forEach(t -> {
            if (t.getLocoAddress() == l) {
                // set the speed and direction.
                t.setSpeedSetting(speed);
                t.setIsForward(isForward);
            }
        });
    }

    /*
     * Set Throttle Functions on/off
     *
     * @param l LocoAddress of the locomotive to change speed of.
     * @param fList an ArrayList of boolean values indicating whether the
     *         function is active or not.
     */
    public void setThrottleFunctions(LocoAddress l, ArrayList<Boolean> fList) {
        // get the throttle for the address.
        throttleList.forEach(t -> {
            if (t.getLocoAddress() == l) {
                for (int i = 0; i < fList.size(); i++) {
                    try {
                        java.lang.reflect.Method setter = t.getClass()
                                .getMethod("setF" + i, boolean.class);
                        setter.invoke(t, fList.get(i));
                    } catch (java.lang.NoSuchMethodException
                            | java.lang.IllegalAccessException
                            | java.lang.reflect.InvocationTargetException ex1) {
                        log.error("", ex1);
                        try {
                            sendErrorStatus();
                        } catch (IOException ioe) {
                            log.error("Error writing to network port");
                        }
                    }
                }
            }
        });
    }


    /*
     * Request a throttle for the specified address from the default
     * Throttle Manager.
     *
     * @param l LocoAddress of the locomotive to request.
     */
    public void requestThrottle(LocoAddress l) {
        ThrottleManager t = InstanceManager.throttleManagerInstance();
        boolean result;
        result = t.requestThrottle(l, this, false); 
        if (!result) {
            try {
                sendErrorStatus();
            } catch (IOException ioe) {
                log.error("Error writing to network port");
            }
        }
    }

    /*
     * Release a throttle for the specified address from the default
     * Throttle Manager.
     *
     * @param l LocoAddress of the locomotive to request.
     */
    public void releaseThrottle(LocoAddress l) {
        ThrottleManager t = InstanceManager.throttleManagerInstance();
        t.cancelThrottleRequest(l, this);
        if (l instanceof DccLocoAddress) {
            throttleList.forEach(throttle -> {
                if (throttle.getLocoAddress() == l) {
                    t.releaseThrottle((DccThrottle) throttle, this);
                    throttleList.remove(throttle);
                    try {
                        sendThrottleReleased(l);
                    } catch (IOException ioe) {
                        log.error("Error writing to network port");
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyThrottleFound(DccThrottle t) {
        throttleList.add(t);
        t.addPropertyChangeListener(new ThrottlePropertyChangeListener(this, t));
        try {
            sendThrottleFound(t.getLocoAddress());
        } catch (java.io.IOException ioe) {
            //Something failed writing data to the port.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        try {
            sendErrorStatus();
        } catch (java.io.IOException ioe) {
            //Something failed writing data to the port.
        }
    }

    /**
     * No steal or share decisions made locally
     * <p>
     * {@inheritDoc}
     * @deprecated since 4.15.7; use #notifyDecisionRequired
     */
    @Override
    @Deprecated
    public void notifyStealThrottleRequired(LocoAddress address) {
        InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL );
    }

    /**
     * No steal or share decisions made locally
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
    }

    // internal class used to propagate back end throttle changes
    // to the clients.
    static class ThrottlePropertyChangeListener implements PropertyChangeListener {

        protected AbstractThrottleServer clientserver = null;
        protected Throttle throttle = null;

        ThrottlePropertyChangeListener(AbstractThrottleServer ts, Throttle t) {
            clientserver = ts;
            throttle = t;
        }

        // update the state of this throttle if any of the properties change
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            switch (e.getPropertyName()) {
                case Throttle.SPEEDSETTING:
                case Throttle.SPEEDSTEPS:
                case Throttle.ISFORWARD:
                    try {
                        clientserver.sendStatus(throttle.getLocoAddress());
                    } catch (IOException ioe) {
                        log.error("Error writing to network port");
                    }
                    break;
                default:
                    for (int i = 0; i <= 28; i++) {
                        if (e.getPropertyName().equals("F" + i)) {
                            try {
                                clientserver.sendStatus(throttle.getLocoAddress());
                            } catch (IOException ioe) {
                                log.error("Error writing to network port");
                            }
                            break; // stop the loop, only one function property will be matched.
                        } else if (e.getPropertyName().equals("F" + i + "Momentary")) {
                            try {
                                clientserver.sendStatus(throttle.getLocoAddress());
                            } catch (IOException ioe) {
                                log.error("Error writing to network port");
                            }
                            break; // stop the loop, only one function property will be matched.
                        }
                    }
                    break;
            }

            log.debug("Property change event received {} / {}", e.getPropertyName(), e.getNewValue());
        }
    }

}
