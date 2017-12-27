package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import jmri.LocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Hoffman Copyright (C) 2011
 */
public class MultiThrottleController extends ThrottleController {

    public MultiThrottleController(char id, String key, ThrottleControllerListener tcl, ControllerInterface ci) {
        super(id, tcl, ci);
        log.debug("New MT controller");
        locoKey = key;
    }

    /**
     * Builds a header to send to the wi-fi device for use in a message.
     * Includes a separator - {@literal <;>}
     *
     * @param chr the character indicating what action is performed
     * @return a pre-assembled header for this DccThrottle
     */
    public String buildPacketWithChar(char chr) {
        return ("M" + whichThrottle + chr + locoKey + "<;>");
    }


    /*
     * Send a message to the wi-fi device that a bound property of a DccThrottle
     * has changed.  Currently only handles function state.
     * Current Format:  Header + F(0 or 1) + function number
     *
     * Event may be from regular throttle or consist throttle, but is handled the same.
     *
     * Bound params: SpeedSteps, IsForward, SpeedSetting, F##, F##Momentary
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String eventName = event.getPropertyName();
        if (log.isDebugEnabled()) {
            log.debug("property change: " + eventName);
        }
        if (eventName.startsWith("F")) {
            if (eventName.contains("Momentary")) {
                return;
            }
            StringBuilder message = new StringBuilder(buildPacketWithChar('A'));

            try {
                if ((Boolean) event.getNewValue()) {
                    message.append("F1");
                } else {
                    message.append("F0");
                }
                message.append(eventName.substring(1));
            } catch (ClassCastException cce) {
                log.debug("Invalid event value. {}", cce.getMessage());
            } catch (IndexOutOfBoundsException oob) {
                log.debug("Invalid event name. {}", oob.getMessage());
            }

            for (ControllerInterface listener : controllerListeners) {
                listener.sendPacketToDevice(message.toString());
            }
        }
        if (eventName.matches("SpeedSteps")) {
            sendSpeedStepMode(throttle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendFunctionLabels(RosterEntry re) {

        if (re != null) {
            StringBuilder functionString = new StringBuilder(buildPacketWithChar('L'));

            int i;
            for (i = 0; i < 29; i++) {
                functionString.append("]\\[");
                if ((re.getFunctionLabel(i) != null)) {
                    functionString.append(re.getFunctionLabel(i));
                }
            }
            for (ControllerInterface listener : controllerListeners) {
                listener.sendPacketToDevice(functionString.toString());
            }
        }
    }

    /**
     * This replaces the previous method of sending a string of function states,
     * and now sends them individually, the same as a property change would.
     *
     * @param t the throttle to send the staes of
     */
    @Override
    public void sendAllFunctionStates(DccThrottle t) {
        log.debug("Sending state of all functions");

        try {
            for (int cnt = 0; cnt < 29; cnt++) {
                Method getF = t.getClass().getMethod("getF" + cnt, (Class[]) null);

                StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
                if ((Boolean) getF.invoke(t, (Object[]) null)) {
                    message.append("F1");
                } else {
                    message.append("F0");
                }
                message.append(cnt);
                for (ControllerInterface listener : controllerListeners) {
                    listener.sendPacketToDevice(message.toString());
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
            log.warn(ea.getLocalizedMessage(), ea);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendCurrentSpeed(DccThrottle t) {
        StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
        message.append("V");
        message.append(Math.round(t.getSpeedSetting() / speedMultiplier));
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendCurrentDirection(DccThrottle t) {
        StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
        message.append("R");
        message.append(t.getIsForward() ? "1" : "0");
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendSpeedStepMode(DccThrottle t) {
        StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
        message.append("s");
        message.append(throttle.getSpeedStepMode());
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendAllMomentaryStates(DccThrottle t) {
        log.debug("Sending momentary state of all functions");

        try {
            for (int cnt = 0; cnt < 29; cnt++) {
                Method getF = t.getClass().getMethod("getF" + cnt + "Momentary", (Class[]) null);

                StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
                if ((Boolean) getF.invoke(t, (Object[]) null)) {
                    message.append("m1");
                } else {
                    message.append("m0");
                }
                message.append(cnt);
                for (ControllerInterface listener : controllerListeners) {
                    listener.sendPacketToDevice(message.toString());
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
            log.warn(ea.getLocalizedMessage(), ea);
        }
    }

    /**
     * {@inheritDoc} A + indicates the address was acquired, - indicates
     * released
     */
    @Override
    public void sendAddress() {
        for (ControllerInterface listener : controllerListeners) {
            if (isAddressSet) {
                listener.sendPacketToDevice(buildPacketWithChar('+'));
            } else {
                listener.sendPacketToDevice(buildPacketWithChar('-'));
            }
        }
    }

    public void sendStealAddress() {
        StringBuilder message = new StringBuilder(buildPacketWithChar('S'));
        message.append(locoKey);
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }
    }

    /**
     * Send a steal required message to the connected device prior to disposing
     * of this MTC
     *
     * @param address of DCC locomotive involved in the steal
     */
    @Override
    public void notifyStealThrottleRequired(LocoAddress address) {
        sendStealAddress();
        notifyFailedThrottleRequest(address, "Steal Required");
    }

    public void requestStealAddress(String action) {
        log.debug("requestStealAddress: {}", action);
        int addr = Integer.parseInt(action.substring(1));
        boolean isLong;
        isLong = (action.charAt(0) == 'L');
        InstanceManager.throttleManagerInstance().stealThrottleRequest(addr, isLong, this, true);
    }

    private final static Logger log = LoggerFactory.getLogger(MultiThrottleController.class);

}
